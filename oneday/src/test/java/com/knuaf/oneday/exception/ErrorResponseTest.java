package com.knuaf.oneday.exception;

import com.knuaf.oneday.dto.SignupRequest;
import com.knuaf.oneday.service.UserService;
import com.knuaf.oneday.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 상황별로 ErrorCode에 맞는 상태 코드와 공통 에러 응답 형식이 내려가는지 검증합니다. (#61)
 */
@AutoConfigureMockMvc
class ErrorResponseTest extends IntegrationTestSupport {

    private static final String LOGIN_ID = "tester";
    private static final long STUDENT_ID = 2021000001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService.register(signup(LOGIN_ID, STUDENT_ID, "심화컴퓨팅전공"));
        insertLecture("COMP0204", "프로그래밍기초", 3, "공학전공");
    }

    @Test
    void 로그인하지_않으면_401() throws Exception {
        expectError(mockMvc.perform(get("/api/course/history")), ErrorCode.UNAUTHORIZED);
    }

    @Test
    void 로그인_실패는_401() throws Exception {
        expectError(mockMvc.perform(post("/api/auth/login")
                .param("userId", LOGIN_ID).param("password", "wrong")), ErrorCode.LOGIN_FAILED);
    }

    @Test
    void 없는_과목을_등록하면_404() throws Exception {
        expectError(registerCourse("NONE0000"), ErrorCode.LECTURE_NOT_FOUND);
    }

    @Test
    void 신청하지_않은_과목을_삭제하면_404() throws Exception {
        expectError(mockMvc.perform(delete("/api/course/COMP0204").with(user(LOGIN_ID))), ErrorCode.COURSE_NOT_FOUND);
    }

    @Test
    void 이미_수강한_과목을_다시_등록하면_409() throws Exception {
        registerCourse("COMP0204").andExpect(status().isOk());
        expectError(registerCourse("COMP0204"), ErrorCode.COURSE_ALREADY_REGISTERED);
    }

    @Test
    void 같은_아이디로_다시_가입하면_409() throws Exception {
        expectError(postSignup(LOGIN_ID, 2021000002L, "심화컴퓨팅전공"), ErrorCode.DUPLICATE_RESOURCE);
    }

    @Test
    void 알_수_없는_전공으로_가입하면_400() throws Exception {
        expectError(postSignup("newbie", 2021000003L, "경영학과"), ErrorCode.INVALID_MAJOR);
    }

    @Test
    void 잘못된_요청_본문은_400() throws Exception {
        expectError(mockMvc.perform(post("/api/course/register").with(user(LOGIN_ID))
                .contentType(MediaType.APPLICATION_JSON).content("{not json")), ErrorCode.INVALID_INPUT);
    }

    @Test
    void 다른_사람의_활동을_수정하면_403() throws Exception {
        userService.register(signup("other", 2021000004L, "심화컴퓨팅전공"));
        String activity = """
                {"category":"수상","title":"해커톤","detail":"대상","year":"2025"}
                """;
        mockMvc.perform(post("/api/activity").with(user("other"))
                .contentType(MediaType.APPLICATION_JSON).content(activity)).andExpect(status().isOk());
        Long activityId = jdbcTemplate.queryForObject("SELECT id FROM activity", Long.class);

        expectError(mockMvc.perform(put("/api/activity/" + activityId).with(user(LOGIN_ID))
                .contentType(MediaType.APPLICATION_JSON).content(activity)), ErrorCode.FORBIDDEN);
    }

    private ResultActions registerCourse(String lecId) throws Exception {
        return mockMvc.perform(post("/api/course/register").with(user(LOGIN_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"lecId":"%s","grade":1,"semester":1,"lecType":"공학전공","received_grade":4.0}
                        """.formatted(lecId)));
    }

    private ResultActions postSignup(String userId, long studentId, String major) throws Exception {
        return mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"userId":"%s","password":"pw","studentId":%d,"name":"테스터","major":"%s"}
                        """.formatted(userId, studentId, major)));
    }

    private void expectError(ResultActions result, ErrorCode errorCode) throws Exception {
        result.andExpect(status().is(errorCode.getStatus().value()))
                .andExpect(jsonPath("$.status").value(errorCode.getStatus().value()))
                .andExpect(jsonPath("$.code").value(errorCode.name()))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    private static SignupRequest signup(String userId, long studentId, String major) {
        SignupRequest request = new SignupRequest();
        request.setUserId(userId);
        request.setPassword("password");
        request.setStudentId(studentId);
        request.setName("테스터");
        request.setMajor(major);
        return request;
    }
}
