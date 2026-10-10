package com.knuaf.oneday.credit;

import com.knuaf.oneday.component.CreditRecalculationRunner;
import com.knuaf.oneday.dto.CourseRegisterDto;
import com.knuaf.oneday.dto.SignupRequest;
import com.knuaf.oneday.repository.UserRepository;
import com.knuaf.oneday.service.CourseService;
import com.knuaf.oneday.service.CreditService;
import com.knuaf.oneday.service.UserService;
import com.knuaf.oneday.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 수강 이력(user_attend)과 users 학점 합계의 정합성을 검증합니다. (#66)
 */
class CreditIntegrityTest extends IntegrationTestSupport {

    /** 운영 DB에서도 그대로 쓰는 검증 쿼리 */
    private static final Path MISMATCH_QUERY = Path.of("db/2026-10-10_credit_integrity_check.sql");

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CreditService creditService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void 심화컴퓨팅_학점_합계는_수강_이력의_합과_같다() {
        long studentId = 2021000001L;
        signup(studentId, "심화컴퓨팅전공");
        register(studentId, "MATH0001", 3, "전공기반");
        register(studentId, "COMP0204", 3, "공학전공");
        register(studentId, "CLTR0001", 3, "기본소양");
        register(studentId, "CLTR0002", 2, "교양");
        register(studentId, "BUSI0001", 3, "다중전공");

        Map<String, Object> user = credits(studentId);
        assertThat(user.get("total_credit")).isEqualTo(14L);
        assertThat(user.get("major_credit")).isEqualTo(6L);
        assertThat(user.get("general_credit")).isEqualTo(5L);
        assertMismatchCount(0);
    }

    @Test
    void 학점_재계산이_실패하면_수강_등록도_롤백된다() {
        long studentId = 2021000002L;
        signup(studentId, "글로벌SW융합전공");
        insertLecture("COMP0204", "프로그래밍기초", 3, "전공필수");

        // 수강 이력 저장 이후 학점 재계산 단계에서 예외를 강제로 발생시킨다.
        CourseService target = AopTestUtils.getTargetObject(courseService);
        Object creditService = ReflectionTestUtils.getField(target, "creditService");
        ReflectionTestUtils.setField(target, "creditService", new FailingCreditService());
        try {
            CourseRegisterDto dto = dto("COMP0204", "전공필수");
            assertThatThrownBy(() -> courseService.registerCourse(studentId, dto))
                    .isInstanceOf(IllegalStateException.class);
        } finally {
            ReflectionTestUtils.setField(target, "creditService", creditService);
        }

        Integer saved = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_attend WHERE student_id = ?", Integer.class, studentId);
        assertThat(saved).isZero();
    }

    @Test
    @Timeout(120)
    void 한_학생이_여러_과목을_동시에_등록해도_합계가_누락되지_않는다() throws Exception {
        long studentId = 2021000003L;
        signup(studentId, "인공지능컴퓨팅전공");
        int courseCount = 20;
        List<CourseRegisterDto> dtos = new ArrayList<>();
        for (int i = 0; i < courseCount; i++) {
            String lecId = "COMP%04d".formatted(i);
            insertLecture(lecId, "과목" + i, 3, "전공");
            dtos.add(dto(lecId, "전공"));
        }

        ExecutorService executor = Executors.newFixedThreadPool(courseCount);
        CountDownLatch ready = new CountDownLatch(courseCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(courseCount);
        for (CourseRegisterDto dto : dtos) {
            executor.submit(() -> {
                try {
                    ready.countDown();
                    start.await();
                    courseService.registerCourse(studentId, dto);
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }
        ready.await();
        start.countDown();
        assertThat(done.await(60, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();

        Map<String, Object> user = credits(studentId);
        Integer attendSum = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(credit), 0) FROM user_attend WHERE student_id = ?", Integer.class, studentId);
        System.out.printf("[#66] 동시 등록 %d과목 -> 수강 이력 합 %d학점, users.total_credit %s학점%n",
                courseCount, attendSum, user.get("total_credit"));

        assertThat(attendSum).isEqualTo(courseCount * 3);
        assertThat(user.get("total_credit")).isEqualTo((long) attendSum);
    }

    @Test
    void 여러_전공_학생의_학점_합계가_검증_쿼리와_일치한다() {
        String[][] attends = {
                {"전공기반", "3"}, {"공학전공", "3"}, {"기본소양", "3"}, {"교양", "2"},
                {"전공필수", "3"}, {"교양필수", "2"}, {"일반선택", "2"}, {"다중전공", "3"}
        };
        String[] majors = {"심화컴퓨팅전공", "플랫폼SW융합전공", "글로벌SW융합전공", "인공지능컴퓨팅전공"};
        for (String[] attend : attends) {
            insertLecture("L" + attend[0].hashCode(), attend[0], Integer.parseInt(attend[1]), attend[0]);
        }
        long studentId = 2022000000L;
        for (String major : majors) {
            studentId++;
            signup(studentId, major);
            for (String[] attend : attends) {
                courseService.registerCourse(studentId, dto("L" + attend[0].hashCode(), attend[0]));
            }
        }

        assertMismatchCount(0);
    }

    @Test
    void 재계산_러너는_어긋난_기존_합계를_바로잡는다() {
        long studentId = 2021000004L;
        signup(studentId, "심화컴퓨팅전공");
        register(studentId, "COMP0204", 3, "공학전공");
        jdbcTemplate.update("UPDATE users SET total_credit = 99, major_credit = 99 WHERE student_id = ?", studentId);
        assertMismatchCount(1);

        new CreditRecalculationRunner(userRepository, creditService)
                .run(null);

        assertMismatchCount(0);
    }

    private void assertMismatchCount(int expected) {
        List<Map<String, Object>> mismatches = jdbcTemplate.queryForList(readQuery());
        System.out.printf("[#66] 학점 합계 불일치 %d건 %s%n", mismatches.size(), mismatches);
        assertThat(mismatches).hasSize(expected);
    }

    private String readQuery() {
        try {
            return Files.readString(MISMATCH_QUERY);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void signup(long studentId, String major) {
        SignupRequest signup = new SignupRequest();
        signup.setUserId("user" + studentId);
        signup.setPassword("password");
        signup.setStudentId(studentId);
        signup.setName("테스터");
        signup.setMajor(major);
        userService.register(signup);
    }

    private void register(long studentId, String lecId, int credit, String lecType) {
        insertLecture(lecId, lecId, credit, lecType);
        courseService.registerCourse(studentId, dto(lecId, lecType));
    }

    private Map<String, Object> credits(long studentId) {
        return jdbcTemplate.queryForMap(
                "SELECT total_credit, major_credit, general_credit FROM users WHERE student_id = ?", studentId);
    }

    private CourseRegisterDto dto(String lecId, String lecType) {
        CourseRegisterDto dto = new CourseRegisterDto();
        ReflectionTestUtils.setField(dto, "lecId", lecId);
        ReflectionTestUtils.setField(dto, "lecType", lecType);
        ReflectionTestUtils.setField(dto, "grade", 1);
        ReflectionTestUtils.setField(dto, "semester", 1);
        ReflectionTestUtils.setField(dto, "received_grade", 4.0f);
        return dto;
    }

    /** 학점 재계산 중 장애를 흉내 내는 대역 */
    private static class FailingCreditService extends CreditService {
        FailingCreditService() {
            super(null, null, null, null, null);
        }

        @Override
        public void recalculateTotalCredits(Long studentId) {
            throw new IllegalStateException("학점 재계산 실패 (테스트용)");
        }
    }
}
