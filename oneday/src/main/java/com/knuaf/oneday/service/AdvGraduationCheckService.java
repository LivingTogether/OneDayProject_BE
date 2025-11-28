package com.knuaf.oneday.service;

import com.knuaf.oneday.dto.GraduationCheckResponse;
import com.knuaf.oneday.dto.GraduationCheckResponse.CheckItem;
import com.knuaf.oneday.dto.GraduationCriteria;
import com.knuaf.oneday.entity.Advcomp;
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
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvGraduationCheckService {

    private final UserRepository userRepository;
    private final AdvCompRepository advcompRepository;
    private final UserAttendRepository userAttendRepository;

    // =============================================================
    // 1. 필수 과목 리스트 정의 (이미지 주석 3, 4번 반영)
    // =============================================================

    // [공통] 공학전공 필수 (2012학번 이후 공통 적용 - 주석 4번)
    // 이론 7과목 + 설계 3과목
    private static final Map<String, String> COMMON_MAJOR_REQUIRED = new LinkedHashMap<>() {{
        put("COMP0204", "프로그래밍기초");
        put("COME0331", "자료구조");
        put("COMP0217", "자바프로그래밍");
        put("ELEC0462", "시스템프로그래밍");
        put("COMP0411", "컴퓨터구조");
        put("COMP0312", "운영체제");
        put("COMP0319", "알고리즘1"); // 이미지엔 '알고리즘1'로 표기됨
        // 설계 과목
        put("COMP0205", "기초창의공학설계");
        put("ITEC0401", "종합설계프로젝트1");
        put("ITEC0402", "종합설계프로젝트2");
    }};

    // [변동] 전공기반(MSC) - 2023학번부터 (주석 3번)
    // 기초수학2, 이산수학
    private static final Map<String, String> MSC_2023 = new LinkedHashMap<>() {{
        put("CLTR0819", "기초수학2");
        put("COME0301", "이산수학");
    }};

    // [변동] 전공기반(MSC) - ~2022학번까지 (주석 3번)
    // 수학1, 물리학1, 이산수학
    // ★ 주의: 수학1, 물리학1 코드는 DB 확인 후 수정 필수!
    private static final Map<String, String> MSC_PRE_2023 = new LinkedHashMap<>() {{
        put("MATH1001", "수학1");   // 코드 수정 필요
        put("PHYS1001", "물리학1"); // 코드 수정 필요
        put("COME0301", "이산수학");
    }};


    @Transactional(readOnly = true)
    public GraduationCheckResponse checkGraduation(Long studentId) {
        log.info("심화컴퓨터 졸업요건 체크 시작: {}", studentId);

        // 1. 정보 조회
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("User 정보가 없습니다."));
        Advcomp adv = advcompRepository.findByUser_StudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("심화컴퓨터 이수 정보가 없습니다."));

        // 2. 입학년도 계산
        int entryYear = (int) (studentId / 1000000);

        // 3. 기준 가져오기
        GraduationCriteria criteria = getCriteriaByYear(entryYear);

        List<CheckItem> items = new ArrayList<>();
        boolean allPassed = true;

        // ----------------------------------------------------
        // [본부 요건]
        // ----------------------------------------------------
        int currentTotal = user.getTotal_credit() != null ? user.getTotal_credit().intValue() : 0;
        if (!addCheckItem(items, "[본부] 총 이수학점", currentTotal, criteria.getTotalCredits())) allPassed = false;

        int currentGeneral = user.getGeneral_credit() != null ? user.getGeneral_credit().intValue() : 0;
        boolean passGen = currentGeneral >= criteria.getGeneralMin() && currentGeneral <= criteria.getGeneralMax();
        String msgGen = passGen ? "통과" : (currentGeneral < criteria.getGeneralMin() ? (currentGeneral - criteria.getGeneralMin()) + " (부족)" : "초과");
        items.add(CheckItem.builder().category("[본부] 교양학점").current(currentGeneral).required(criteria.getGeneralMin()).isPassed(passGen).message(msgGen).build());
        if (!passGen) allPassed = false;

        int currentEng = user.getEng_score() != null ? user.getEng_score().intValue() : 0;
        if (!addCheckItem(items, "[본부] 영어성적", currentEng, criteria.getEngScore())) allPassed = false;

        // ----------------------------------------------------
        // [ABEEK 요건]
        // ----------------------------------------------------
        int curAbeekGen = adv.getAbeek_general() != null ? adv.getAbeek_general() : 0;
        if (!addCheckItem(items, "[ABEEK] 기본소양", curAbeekGen, criteria.getAbeekGeneral())) allPassed = false;

        int curBaseMajor = adv.getBase_major() != null ? adv.getBase_major() : 0;
        if (!addCheckItem(items, "[ABEEK] 전공기반", curBaseMajor, criteria.getAbeekBaseMajor())) allPassed = false;

        int curEnginMajor = adv.getEngin_major() != null ? adv.getEngin_major() : 0;
        // 공학전공에는 설계 14학점이 포함되어야 하지만, 여기선 총점만 일단 체크
        if (!addCheckItem(items, "[ABEEK] 공학전공", curEnginMajor, criteria.getAbeekEnginMajor())) allPassed = false;

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

    // =========================================================
    // ★ [핵심] 입학년도별 기준표 생성 (이미지 분석 반영)
    // =========================================================
    private GraduationCriteria getCriteriaByYear(int year) {

        // 1. 필수 과목 리스트 병합 (공통 전공 + 학번별 MSC)
        Map<String, String> requiredCourses = new LinkedHashMap<>(COMMON_MAJOR_REQUIRED);
        // 연도별 교양 학점 기준 다름
        int genMin,genMax=999;
        if(year<=2017){
            genMin = 30;
        }
        else if(year<=2022){
            genMin = 24;
            genMax = 42;
        }
        else if(year<=2023){
            genMin = 30;
        }else{
            genMin = 30;
        }

        if (year >= 2023) {
            requiredCourses.putAll(MSC_2023); // 기초수학2, 이산수학
        } else {
            requiredCourses.putAll(MSC_PRE_2023); // 수학1, 물리1, 이산수학
        }

        // 2. 학점 기준 설정
        // [A] 2023학번 ~ (최신)
        if (year >= 2023) {
            return GraduationCriteria.builder()
                    .totalCredits(140)
                    .generalMin(genMin).generalMax(genMax)
                    .engScore(700)
                    .abeekGeneral(15)
                    .abeekBaseMajor(18)
                    .abeekEnginMajor(60)
                    .abeekTotal(93)
                    .requiredCourses(requiredCourses) // 공통 + MSC_2023
                    .build();
        }
        // [B] 2021 ~ 2022학번
        else if (year >= 2021) {
            return GraduationCriteria.builder()
                    .totalCredits(140)
                    .generalMin(genMin).generalMax(genMax)
                    .engScore(700)
                    .abeekGeneral(15)
                    .abeekBaseMajor(18)
                    .abeekEnginMajor(60)
                    .abeekTotal(93)
                    .requiredCourses(requiredCourses) // 공통 + MSC_PRE_2023
                    .build();
        }
        // [C] 2012 ~ 2020학번 (고학번 - 학점 기준 높음)
        else if (year >= 2012) {
            return GraduationCriteria.builder()
                    .totalCredits(150)      // ★ 150학점
                    .generalMin(genMin).generalMax(genMax)
                    .engScore(700)
                    .abeekGeneral(15)
                    .abeekBaseMajor(21)     // ★ 21학점
                    .abeekEnginMajor(75)    // ★ 75학점
                    .abeekTotal(111)        // ★ 111학점
                    .requiredCourses(requiredCourses) // 공통 + MSC_PRE_2023
                    .build();
        }
        // [D] 2011 이전 (예외 처리)
        else {
            return GraduationCriteria.builder()
                    .totalCredits(140)
                    .generalMin(genMin).generalMax(genMax)
                    .engScore(700)
                    .abeekGeneral(15).abeekBaseMajor(18).abeekEnginMajor(60).abeekTotal(93)
                    .requiredCourses(requiredCourses)
                    .build();
        }
    }

    private boolean addCheckItem(List<CheckItem> items, String category, int current, int required) {
        boolean passed = current >= required;
        items.add(CheckItem.builder()
                .category(category)
                .current(current)
                .required(required)
                .isPassed(passed)
                .message(passed ? "통과" : (current - required) + " (부족)")
                .build());
        return passed;
    }
}