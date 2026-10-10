package com.knuaf.oneday.course;

import com.knuaf.oneday.dto.SignupRequest;
import com.knuaf.oneday.service.UserService;
import com.knuaf.oneday.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * 같은 학번·과목으로 수강 등록 요청이 동시에 들어올 때 중복 저장 여부를 검증합니다. (#65)
 */
@AutoConfigureMockMvc
class CourseRegisterConcurrencyTest extends IntegrationTestSupport {

    private static final int REQUEST_COUNT = 100;
    private static final long STUDENT_ID = 2021000001L;
    private static final String LOGIN_ID = "tester";
    private static final String LEC_ID = "COMP0204";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        SignupRequest signup = new SignupRequest();
        signup.setUserId(LOGIN_ID);
        signup.setPassword("password");
        signup.setStudentId(STUDENT_ID);
        signup.setName("테스터");
        signup.setMajor("심화컴퓨팅전공");
        userService.register(signup);

        insertLecture(LEC_ID, "프로그래밍기초", 3, "공학전공");
    }

    @Test
    @Timeout(120)
    void 같은_과목을_동시에_100번_등록해도_한_건만_저장된다() throws Exception {
        String body = """
                {"lecId":"%s","grade":1,"semester":1,"lecType":"공학전공","received_grade":4.5}
                """.formatted(LEC_ID);

        ExecutorService executor = Executors.newFixedThreadPool(REQUEST_COUNT);
        CountDownLatch ready = new CountDownLatch(REQUEST_COUNT);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(REQUEST_COUNT);
        Map<Integer, AtomicInteger> statusCounts = new ConcurrentHashMap<>();

        for (int i = 0; i < REQUEST_COUNT; i++) {
            executor.submit(() -> {
                try {
                    ready.countDown();
                    start.await();
                    int status = mockMvc.perform(post("/api/course/register")
                                    .with(user(LOGIN_ID))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(body))
                            .andReturn().getResponse().getStatus();
                    statusCounts.computeIfAbsent(status, k -> new AtomicInteger()).incrementAndGet();
                } catch (Exception e) {
                    statusCounts.computeIfAbsent(-1, k -> new AtomicInteger()).incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }
        ready.await();
        start.countDown();
        assertThat(done.await(60, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();

        Integer saved = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_attend WHERE student_id = ? AND lec_id = ?",
                Integer.class, STUDENT_ID, LEC_ID);
        System.out.printf("[#65] 동시 요청 %d건 -> 저장 %d건, 응답 코드 분포 %s%n", REQUEST_COUNT, saved, statusCounts);

        assertThat(saved).isEqualTo(1);
        assertThat(statusCounts.get(200)).hasValue(1);
        assertThat(statusCounts.get(409)).hasValue(REQUEST_COUNT - 1);
    }
}
