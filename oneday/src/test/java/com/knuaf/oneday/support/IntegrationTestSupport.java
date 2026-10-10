package com.knuaf.oneday.support;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * 통합 테스트 공통 설정.
 * 테스트 클래스 전체가 하나의 PostgreSQL 컨테이너를 공유합니다.
 */
@SpringBootTest
public abstract class IntegrationTestSupport {

    @ServiceConnection
    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanUpDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE user_attend, advcomp, globalsw, activity, users, lecture_list, startup_course RESTART IDENTITY CASCADE");
    }

    protected void insertLecture(String lecId, String lecName, int credit, String lecType) {
        jdbcTemplate.update(
                "INSERT INTO lecture_list (lec_id, lec_name, credit, lec_type, open_depart, language, grade) VALUES (?, ?, ?, ?, ?, ?, ?)",
                lecId, lecName, credit, lecType, "컴퓨터학부", "KR", 1);
    }
}
