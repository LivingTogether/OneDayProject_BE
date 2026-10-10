package com.knuaf.oneday.service;

import com.knuaf.oneday.exception.BusinessException;
import com.knuaf.oneday.exception.ErrorCode;
import com.knuaf.oneday.dto.GraduationCheckResponse;
import com.knuaf.oneday.component.GraduationRules;
import com.knuaf.oneday.dto.GraduationCheckResponse.CheckItem;
import com.knuaf.oneday.dto.GraduationCheckResponse.CreditKind;
import com.knuaf.oneday.dto.GraduationCriteria;
import com.knuaf.oneday.entity.MajorCode;
import com.knuaf.oneday.entity.User;
import com.knuaf.oneday.entity.UserAttend;
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
public class AIGraduationService {

    private final UserRepository userRepository;
    private final UserAttendRepository userAttendRepository;
    private final GraduationRules graduationRules;

    @Transactional(readOnly = true)
    public GraduationCheckResponse checkGraduation(Long studentId) {

        // 1. 유저 정보 조회
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 입학년도 계산
        int entryYear = (int) (studentId / 1000000);

        // 3. [핵심] 학번에 맞는 기준표 가져오기
        GraduationCriteria criteria = graduationRules.criteria(MajorCode.AI_COMPUTING, entryYear);

        List<CheckItem> items = new ArrayList<>();
        boolean allPassed = true;

        // ----------------------------------------------------
        // [학점 및 공통 요건]
        // ----------------------------------------------------

        // 1. 총 이수학점 (140)
        int currentTotal = user.getTotal_credit() != null ? user.getTotal_credit().intValue() : 0;
        if (!addCheckItem(items, "총 이수학점", currentTotal, criteria.getTotalCredits(), CreditKind.TOTAL)) allPassed = false;

        // 2. 전공 이수학점 (72)
        int currentMajor = user.getMajor_credit() != null ? user.getMajor_credit().intValue() : 0;
        if (!addCheckItem(items, "전공학점(AI)", currentMajor, criteria.getMajorCredits(), CreditKind.MAJOR)) allPassed = false;

        // 3. 교양 이수학점 (학번별 Min ~ Max 체크)
        int currentGen = user.getGeneral_credit() != null ? user.getGeneral_credit().intValue() : 0;
        boolean passGen = currentGen >= criteria.getGeneralMin() && currentGen <= criteria.getGeneralMax();
        String msgGen = passGen ? "통과" : (currentGen < criteria.getGeneralMin() ? (currentGen - criteria.getGeneralMin()) + " (부족)" : "초과");

        items.add(CheckItem.builder()
                .category("교양학점")
                .current(currentGen)
                .required(criteria.getGeneralMin())
                .isPassed(passGen)
                .message(msgGen)
                .kind(CreditKind.GENERAL)
                .build());
        if (!passGen) allPassed = false;

        // 4. 영어 성적
        int currentEng = user.getEng_score() != null ? user.getEng_score().intValue() : 0;
        if (!addCheckItem(items, "영어성적(토익)", currentEng, criteria.getEngScore())) allPassed = false;

        // 5. 현장실습
        boolean internship = user.isInternship();
        items.add(CheckItem.builder()
                .category("현장실습")
                .current(internship ? 1 : 0)
                .required(1)
                .isPassed(internship)
                .message(internship ? "이수" : "미이수")
                .build());
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

        Map<String, String> requiredCourses = criteria.getRequiredCourses();
        List<String> missingCourses = new ArrayList<>();

        for (Map.Entry<String, String> entry : requiredCourses.entrySet()) {
            String code = entry.getKey();
            String name = entry.getValue();
            if (!takenCodes.contains(code)) {
                missingCourses.add(name + " (" + code + ")");
            }
        }

        boolean passCourses = missingCourses.isEmpty();
        items.add(CheckItem.builder()
                .category("필수과목 이수")
                .current(requiredCourses.size() - missingCourses.size())
                .required(requiredCourses.size())
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