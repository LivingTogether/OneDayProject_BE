package com.knuaf.oneday.graduation;

import com.knuaf.oneday.dto.GraduationCheckResponse;
import com.knuaf.oneday.dto.GraduationCheckResponse.CheckItem;
import com.knuaf.oneday.dto.SimpleGraduationResponse;
import com.knuaf.oneday.service.IntegratedGraduationService;
import com.knuaf.oneday.service.SimpleGraduationService;
import com.knuaf.oneday.service.UserService;
import com.knuaf.oneday.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 간단 조회(/api/graduation/simple)와 상세 조회(/api/graduation/my-status)의
 * "학점 요건 충족" 판정이 서로 다른 케이스를 찾습니다. (#59)
 * 졸업 규칙 단일화(#60) 이전에는 126건 중 60건이 어긋났습니다.
 * 영어·현장실습·필수과목처럼 간단 조회가 다루지 않는 항목은 비교에서 제외합니다.
 */
class GraduationConsistencyTest extends IntegrationTestSupport {

    // 회원가입 시 선택 가능한 전공 (플랫폼SW융합전공은 학점 계산에서 심화컴퓨팅과 같이 취급됨)
    private static final List<String> MAJORS =
            List.of("심화컴퓨팅전공", "플랫폼SW융합전공", "글로벌SW융합전공", "인공지능컴퓨팅전공");


    @Autowired
    private UserService userService;

    @Autowired
    private IntegratedGraduationService integratedService;

    @Autowired
    private SimpleGraduationService simpleService;

    @Test
    void 간단_조회와_상세_조회의_학점_판정을_비교한다() {
        List<String> mismatches = new ArrayList<>();
        int compared = 0;
        long seq = 0;

        for (String major : MAJORS) {
            for (int year : GraduationRuleSnapshotTest.ENTRY_YEARS) {
                for (String specific : GraduationRuleSnapshotTest.SPECIFIC_MAJORS) {
                    long studentId = year * 1_000_000L + (++seq);
                    String loginId = "user" + studentId;
                    GraduationRuleSnapshotTest.signup(userService, studentId, major);
                    if (!specific.equals("없음")) {
                        jdbcTemplate.update("UPDATE users SET specific_major = ? WHERE student_id = ?", specific, studentId);
                    }
                    String key = major + "|" + year + "|" + specific;

                    Map<String, Integer> required = requiredCredits(studentId);
                    for (State state : State.values()) {
                        if (state == State.MAJOR_WITHOUT_ABEEK && !required.containsKey("[ABEEK] 공학전공")) continue;
                        applyState(studentId, required, state);
                        compared++;

                        String detail = detailVerdict(studentId);
                        String simple = simpleVerdict(loginId);
                        if (!detail.equals(simple)) {
                            mismatches.add(key + " / " + state + " : 간단=" + simple + ", 상세=" + detail);
                        }
                    }
                }
            }
        }

        System.out.printf("[#59] 간단/상세 비교 %d건 중 불일치 %d건%n", compared, mismatches.size());
        mismatches.forEach(m -> System.out.println("    " + m));
        assertThat(mismatches).isEmpty();
    }

    enum State {
        /** 상세 조회 기준 학점을 정확히 채운 상태 */
        EXACT,
        /** 기준은 채웠지만 교양을 50학점 들은 상태 (교양 상한 초과) */
        OVER_GENERAL,
        /** 전공 학점은 채웠지만 ABEEK 전공기반·공학전공으로 인정된 학점이 없는 상태 (심화컴퓨팅) */
        MAJOR_WITHOUT_ABEEK
    }

    /** 0학점 상태에서 상세 조회를 실행해 학점 관련 기준값을 읽어온다. */
    private Map<String, Integer> requiredCredits(long studentId) {
        return integratedService.checkGraduation(studentId, "테스터").getCheckList().stream()
                .filter(GraduationConsistencyTest::isCreditItem)
                .collect(Collectors.toMap(CheckItem::getCategory, i -> i.getRequired().intValue()));
    }

    private void applyState(long studentId, Map<String, Integer> required, State state) {
        int total = valueOf(required, "총 이수학점");
        int general = valueOf(required, "교양학점");
        int abeekGen = valueOf(required, "[ABEEK] 기본소양");
        int base = valueOf(required, "[ABEEK] 전공기반");
        int engin = valueOf(required, "[ABEEK] 공학전공");
        int major = required.containsKey("[ABEEK] 공학전공") ? base + engin : valueOf(required, "전공학점");

        if (state == State.OVER_GENERAL) general = 50;
        int abeekBase = state == State.MAJOR_WITHOUT_ABEEK ? 0 : base;
        int abeekEngin = state == State.MAJOR_WITHOUT_ABEEK ? 0 : engin;

        jdbcTemplate.update("UPDATE users SET total_credit = ?, major_credit = ?, general_credit = ? WHERE student_id = ?",
                total, major, general, studentId);
        jdbcTemplate.update("UPDATE advcomp SET abeek_general = ?, base_major = ?, engin_major = ?, abeek_total = ? WHERE student_id = ?",
                abeekGen, abeekBase, abeekEngin, abeekGen + abeekBase + abeekEngin, studentId);
    }

    private static int valueOf(Map<String, Integer> required, String name) {
        return required.entrySet().stream()
                .filter(e -> e.getKey().contains(name))
                .mapToInt(Map.Entry::getValue)
                .findFirst().orElse(0);
    }

    private String detailVerdict(long studentId) {
        try {
            GraduationCheckResponse response = integratedService.checkGraduation(studentId, "테스터");
            boolean passed = response.getCheckList().stream()
                    .filter(GraduationConsistencyTest::isCreditItem)
                    .allMatch(CheckItem::isPassed);
            return passed ? "충족" : "미충족";
        } catch (IllegalArgumentException e) {
            return "오류(" + e.getMessage() + ")";
        }
    }

    private String simpleVerdict(String loginId) {
        SimpleGraduationResponse response = simpleService.getSimpleStatus(loginId);
        boolean passed = response.getMissingMajor() == 0 && response.getMissingGeneral() == 0
                && response.getExceededGeneral() == 0
                && response.getCurrentTotal() >= response.getRequiredTotal();
        return passed ? "충족" : "미충족";
    }

    /** 학점 관련 항목만 비교 (트랙 전용 항목·영어·현장실습·필수과목 제외) */
    private static boolean isCreditItem(CheckItem item) {
        String category = item.getCategory();
        if (category.startsWith("[트랙]")) return false;
        return category.startsWith("[ABEEK]") || category.endsWith("학점") || category.endsWith("학점(AI)");
    }
}
