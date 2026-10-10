package com.knuaf.oneday.service;

import com.knuaf.oneday.dto.GraduationCheckResponse;
import com.knuaf.oneday.component.GraduationRules;
import com.knuaf.oneday.dto.GraduationCheckResponse.CheckItem;
import com.knuaf.oneday.dto.GraduationCheckResponse.CreditKind;
import com.knuaf.oneday.dto.GraduationCriteria;
import com.knuaf.oneday.entity.Advcomp;
import com.knuaf.oneday.entity.MajorCode;
import com.knuaf.oneday.entity.User;
import com.knuaf.oneday.entity.UserAttend;
import com.knuaf.oneday.repository.AdvCompRepository;
import com.knuaf.oneday.repository.UserAttendRepository;
import com.knuaf.oneday.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvGraduationCheckService {

    private final UserRepository userRepository;
    private final AdvCompRepository advcompRepository;
    private final UserAttendRepository userAttendRepository;
    private final GraduationRules graduationRules;

    @Transactional(readOnly = true)
    public GraduationCheckResponse checkGraduation(Long studentId) {
        log.info("심화컴퓨터 졸업요건 체크 시작: {}", studentId);

        // 1. 정보 조회
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("User 정보가 없습니다."));
        // 아직 ABEEK 이수 정보가 없으면 0학점으로 판정
        Advcomp adv = advcompRepository.findByUser_StudentId(studentId)
                .orElseGet(Advcomp::new);

        // 2. 입학년도 계산
        int entryYear = (int) (studentId / 1000000);

        // 3. 기준 가져오기
        GraduationCriteria criteria = graduationRules.criteria(MajorCode.ADVANCED_COMPUTING, entryYear);

        List<CheckItem> items = new ArrayList<>();
        boolean allPassed = true;

        // ----------------------------------------------------
        // [본부 요건]
        // ----------------------------------------------------
        int currentTotal = user.getTotal_credit() != null ? user.getTotal_credit().intValue() : 0;
        if (!addCheckItem(items, "[본부] 총 이수학점", currentTotal, criteria.getTotalCredits(), CreditKind.TOTAL)) allPassed = false;

        int currentGeneral = user.getGeneral_credit() != null ? user.getGeneral_credit().intValue() : 0;
        boolean passGen = currentGeneral >= criteria.getGeneralMin() && currentGeneral <= criteria.getGeneralMax();
        String msgGen = passGen ? "통과" : (currentGeneral < criteria.getGeneralMin() ? (currentGeneral - criteria.getGeneralMin()) + " (부족)" : "초과");
        items.add(CheckItem.builder().category("[본부] 교양학점").current(currentGeneral).required(criteria.getGeneralMin()).isPassed(passGen).message(msgGen).kind(CreditKind.GENERAL).build());
        if (!passGen) allPassed = false;

        int currentEng = user.getEng_score() != null ? user.getEng_score().intValue() : 0;
        if (!addCheckItem(items, "[본부] 영어성적", currentEng, criteria.getEngScore())) allPassed = false;

        // ----------------------------------------------------
        // [ABEEK 요건]
        // ----------------------------------------------------
        int curAbeekGen = adv.getAbeek_general() != null ? adv.getAbeek_general() : 0;
        if (!addCheckItem(items, "[ABEEK] 기본소양", curAbeekGen, criteria.getAbeekGeneral(), CreditKind.GENERAL)) allPassed = false;

        int curBaseMajor = adv.getBase_major() != null ? adv.getBase_major() : 0;
        if (!addCheckItem(items, "[ABEEK] 전공기반", curBaseMajor, criteria.getAbeekBaseMajor(), CreditKind.MAJOR)) allPassed = false;

        int curEnginMajor = adv.getEngin_major() != null ? adv.getEngin_major() : 0;
        // 공학전공에는 설계 14학점이 포함되어야 하지만, 여기선 총점만 일단 체크
        if (!addCheckItem(items, "[ABEEK] 공학전공", curEnginMajor, criteria.getAbeekEnginMajor(), CreditKind.MAJOR)) allPassed = false;

        int curAbeekTotal = adv.getAbeek_total() != null ? adv.getAbeek_total() : 0;
        if (!addCheckItem(items, "[ABEEK] 인증총점", curAbeekTotal, criteria.getAbeekTotal())) allPassed = false;

        boolean internship = user.isInternship();
        items.add(CheckItem.builder().category("현장실습").current(internship ? 1 : 0).required(1).isPassed(internship).message(internship ? "이수" : "미이수").build());
        if (!internship) allPassed = false;

        // ----------------------------------------------------
        // [필수 과목 체크]
        // ----------------------------------------------------
        List<UserAttend> takenLectures = userAttendRepository.findByStudentId(studentId);
        Set<String> takenCodes = takenLectures.stream()
                .map(UserAttend::getLecId)
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toSet());

        Map<String, String> targetCourses = criteria.getRequiredCourses();
        List<String> missingCourses = new ArrayList<>();

        for (Map.Entry<String, String> entry : targetCourses.entrySet()) {
            String code = entry.getKey();
            String name = entry.getValue();
            if (!takenCodes.contains(code)) {
                missingCourses.add(name + " (" + code + ")");
            }
        }

        boolean passCourses = missingCourses.isEmpty();
        items.add(CheckItem.builder()
                .category("필수과목 이수")
                .current(targetCourses.size() - missingCourses.size())
                .required(targetCourses.size())
                .isPassed(passCourses)
                .message(passCourses ? "모두 이수함" : missingCourses.size() + "과목 미이수")
                .build());

        if (!passCourses) allPassed = false;

        return GraduationCheckResponse.builder()
                .studentId(studentId)
                .isGraduationPossible(allPassed)
                .checkList(items)
                .missingCourses(missingCourses)
                .build();
    }

    private boolean addCheckItem(List<CheckItem> items, String category, int current, int required) {
        return addCheckItem(items, category, current, required, null);
    }

    private boolean addCheckItem(List<CheckItem> items, String category, int current, int required, CreditKind kind) {
        boolean passed = current >= required;
        items.add(CheckItem.builder()
                .category(category)
                .current(current)
                .required(required)
                .isPassed(passed)
                .message(passed ? "통과" : (current - required) + " (부족)")
                .kind(kind)
                .build());
        return passed;
    }
}