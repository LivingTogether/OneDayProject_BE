package com.knuaf.oneday.service;

import com.knuaf.oneday.dto.GraduationCheckResponse;
import com.knuaf.oneday.dto.GraduationCheckResponse.CheckItem;
import com.knuaf.oneday.dto.GraduationCriteria;
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

    // [인공지능컴퓨팅 필수 과목] (이미지 상단 내용 반영)
    // ※ 과목코드는 학교 편람을 확인하여 정확한 코드로 매핑해주세요.
    private static final Map<String, String> AI_REQUIRED_COURSES = new LinkedHashMap<>() {{
        put("COMP0453", "컴퓨팅사고와SW코딩");
        put("COMP0454", "인공지능수학기초");
        put("COME0331", "자료구조");
        put("COMP0319", "알고리즘1");
        put("COMP0324", "인공지능");
        put("ITEC0417", "기계학습개론");
        put("ITEC0401", "종합설계프로젝트1");
    }};

    @Transactional(readOnly = true)
    public GraduationCheckResponse checkGraduation(Long studentId) {

        // 1. 유저 정보 조회
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("User 정보가 없습니다."));

        // 2. 입학년도 계산
        int entryYear = (int) (studentId / 1000000);

        // 3. [핵심] 학번에 맞는 기준표 가져오기
        GraduationCriteria criteria = getCriteriaByYear(entryYear);

        List<CheckItem> items = new ArrayList<>();
        boolean allPassed = true;

        // ----------------------------------------------------
        // [학점 및 공통 요건]
        // ----------------------------------------------------

        // 1. 총 이수학점 (140)
        int currentTotal = user.getTotal_credit() != null ? user.getTotal_credit().intValue() : 0;
        if (!addCheckItem(items, "총 이수학점", currentTotal, criteria.getTotalCredits())) allPassed = false;

        // 2. 전공 이수학점 (72)
        int currentMajor = user.getMajor_credit() != null ? user.getMajor_credit().intValue() : 0;
        if (!addCheckItem(items, "전공학점(AI)", currentMajor, criteria.getMajorCredits())) allPassed = false;

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

    // =========================================================
    // ★ [핵심] 입학년도별 인공지능전공 기준표 생성
    // =========================================================
    private GraduationCriteria getCriteriaByYear(int year) {
        // 연도별 교양 학점 기준
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

        return GraduationCriteria.builder()
                .totalCredits(140)      // 총 140
                .majorCredits(72)       // 전공 72 (인공지능 특화)
                .generalMin(genMin).generalMax(genMax)
                .engScore(700)          // 토익 700
                .requiredCourses(AI_REQUIRED_COURSES) // 필수과목은 전학년 공통으로 보임
                .build();
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