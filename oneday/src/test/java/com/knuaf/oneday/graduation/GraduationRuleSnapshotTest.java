package com.knuaf.oneday.graduation;

import com.knuaf.oneday.dto.GraduationCheckResponse;
import com.knuaf.oneday.dto.GraduationCheckResponse.CheckItem;
import com.knuaf.oneday.dto.SignupRequest;
import com.knuaf.oneday.service.IntegratedGraduationService;
import com.knuaf.oneday.service.UserService;
import com.knuaf.oneday.support.IntegrationTestSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 현재 졸업 판정 규칙(기준 학점, 필수 과목)을 그대로 고정하는 특성화 테스트입니다. (#59)
 * 리팩터링 후에도 같은 결과가 나와야 합니다.
 * 규칙을 의도적으로 바꿨다면 ./gradlew test -DupdateSnapshot=true 로 스냅샷을 갱신합니다.
 */
class GraduationRuleSnapshotTest extends IntegrationTestSupport {

    static final List<String> MAJORS = List.of("심화컴퓨팅전공", "글로벌SW융합전공", "인공지능컴퓨팅전공");
    // 코드상 기준이 바뀌는 경계(2011/2012, 2017/2018, 2020/2021, 2021/2022, 2022/2023)를 모두 포함
    static final List<Integer> ENTRY_YEARS = List.of(2011, 2016, 2019, 2021, 2022, 2023, 2025);
    static final List<String> SPECIFIC_MAJORS = List.of("다중전공", "없음");

    private static final Path SNAPSHOT = Path.of("src/test/resources/graduation/detail-snapshot.txt");
    private static final boolean UPDATE = Boolean.getBoolean("updateSnapshot");
    private static final Map<String, String> actual = new TreeMap<>();
    private static Map<String, String> expected;

    @Autowired
    private UserService userService;

    @Autowired
    private IntegratedGraduationService integratedGraduationService;

    static Stream<GraduationCase> cases() {
        return GraduationCase.all();
    }

    @BeforeAll
    static void loadSnapshot() throws IOException {
        expected = new TreeMap<>();
        if (Files.exists(SNAPSHOT)) {
            for (String line : Files.readAllLines(SNAPSHOT)) {
                int sep = line.indexOf(" => ");
                if (sep > 0) expected.put(line.substring(0, sep), line.substring(sep + 4));
            }
        }
    }

    @AfterAll
    static void writeSnapshot() throws IOException {
        if (UPDATE) {
            Files.write(SNAPSHOT, actual.entrySet().stream()
                    .map(e -> e.getKey() + " => " + e.getValue())
                    .collect(Collectors.toList()));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void 상세_졸업판정_기준이_스냅샷과_같다(GraduationCase c) {
        long studentId = c.studentId();
        signup(userService, studentId, c.major());
        if (!c.specificMajor().equals("없음")) {
            jdbcTemplate.update("UPDATE users SET specific_major = ? WHERE student_id = ?", c.specificMajor(), studentId);
        }

        GraduationCheckResponse response = integratedGraduationService.checkGraduation(studentId, "테스터");
        // 교양 상한(최대 학점) 규칙은 required에 드러나지 않으므로 교양 50학점일 때의 판정도 함께 기록한다.
        jdbcTemplate.update("UPDATE users SET general_credit = 50 WHERE student_id = ?", studentId);
        GraduationCheckResponse overGeneral = integratedGraduationService.checkGraduation(studentId, "테스터");
        String result = describe(response) + "; 교양50학점=" + generalItem(overGeneral).getMessage();
        actual.put(c.key(), result);

        if (!UPDATE) {
            assertThat(result).isEqualTo(expected.get(c.key()));
        }
    }

    static String describe(GraduationCheckResponse response) {
        List<String> parts = new ArrayList<>();
        parts.add("type=" + response.getMajorType());
        for (CheckItem item : response.getCheckList()) {
            parts.add(item.getCategory() + "=" + item.getRequired());
        }
        parts.add("missing=" + response.getMissingCourses());
        return String.join("; ", parts);
    }

    static CheckItem generalItem(GraduationCheckResponse response) {
        return response.getCheckList().stream()
                .filter(item -> item.getCategory().endsWith("교양학점"))
                .findFirst().orElseThrow();
    }

    static void signup(UserService userService, long studentId, String major) {
        SignupRequest signup = new SignupRequest();
        signup.setUserId("user" + studentId);
        signup.setPassword("password");
        signup.setStudentId(studentId);
        signup.setName("테스터");
        signup.setMajor(major);
        userService.register(signup);
    }

    record GraduationCase(String major, int entryYear, String specificMajor) {
        static Stream<GraduationCase> all() {
            List<GraduationCase> cases = new ArrayList<>();
            for (String major : MAJORS)
                for (int year : ENTRY_YEARS)
                    for (String specific : SPECIFIC_MAJORS)
                        cases.add(new GraduationCase(major, year, specific));
            return cases.stream();
        }

        long studentId() {
            return entryYear * 1_000_000L + 1;
        }

        String key() {
            return major + "|" + entryYear + "|" + specificMajor;
        }

        @Override
        public String toString() {
            return key();
        }
    }
}
