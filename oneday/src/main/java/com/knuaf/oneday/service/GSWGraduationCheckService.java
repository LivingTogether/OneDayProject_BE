package com.knuaf.oneday.service;

import com.knuaf.oneday.dto.GraduationCheckResponse;
import com.knuaf.oneday.dto.GraduationCheckResponse.CheckItem;
import com.knuaf.oneday.dto.GraduationCriteria;
import com.knuaf.oneday.entity.GlobalSW;
import com.knuaf.oneday.entity.User;
import com.knuaf.oneday.entity.UserAttend;
import com.knuaf.oneday.repository.GlobalSWRepository;
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
public class GSWGraduationCheckService {

    private final UserRepository userRepository;
    private final GlobalSWRepository globalSWRepository;
    private final UserAttendRepository userAttendRepository;

    private enum Track {
        MULTI_MAJOR,       // 다중전공 트랙
        OVERSEAS_DEGREE,   // 해외복수학위 트랙
        MASTER_LINK        // 학석사연계 트랙
    }

    // [전공 필수 과목] (이미지 3번 - 전공학점 주석 참조)
    // 모든 학번/트랙 공통 적용
    private static final Map<String, String> COMMON_REQUIRED_COURSES = new LinkedHashMap<>() {{
        put("COMP0204", "프로그래밍기초");
        put("COME0331", "자료구조");
        put("COMP0312", "운영체제");
        put("GLSO0216", "알고리즘실습");
    }};

    @Transactional(readOnly = true)
    public GraduationCheckResponse checkGraduation(Long studentId) {

        // 1. 정보 조회
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("User 없음"));
        GlobalSW global = globalSWRepository.findByUser_StudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("GlobalSW 없음"));

        // 2. 트랙 및 학번 파악
        String specificMajor = (user.getSpecific_major() == null) ? "" : user.getSpecific_major().trim();
        Track track = determineTrack(specificMajor);
        int entryYear = (int) (studentId / 1000000);

        // 3. [핵심] 기준표(Criteria) 가져오기 (학번 + 트랙 조합)
        GraduationCriteria criteria = getCriteriaByYearAndTrack(entryYear, track);

        List<CheckItem> items = new ArrayList<>();
        boolean allPassed = true;

        // ----------------------------------------------------
        // [본부/공통 요건]
        // ----------------------------------------------------
        // 1. 총 이수학점 (130)
        int currentTotal = user.getTotal_credit() != null ? user.getTotal_credit().intValue() : 0;
        if (!addCheckItem(items, "[공통] 총 이수학점", currentTotal, criteria.getTotalCredits())) allPassed = false;

        // 2. 전공 이수학점 (51)
        int currentMajor = user.getMajor_credit() != null ? user.getMajor_credit().intValue() : 0;
        if (!addCheckItem(items, "[공통] SW전공학점", currentMajor, criteria.getMajorCredits())) allPassed = false;

        // 3. 교양학점 (학번별 범위 체크)
        int currentGen = user.getGeneral_credit() != null ? user.getGeneral_credit().intValue() : 0;
        boolean passGen = currentGen >= criteria.getGeneralMin() && currentGen <= criteria.getGeneralMax();
        String msgGen = passGen ? "통과" : (currentGen < criteria.getGeneralMin() ? (currentGen - criteria.getGeneralMin()) + " (부족)" : "초과");
        items.add(CheckItem.builder().category("[공통] 교양학점").current(currentGen).required(criteria.getGeneralMin()).isPassed(passGen).message(msgGen).build());
        if (!passGen) allPassed = false;

        // 4. 영어 성적 (트랙별 기준 적용)
        int currentEng = user.getEng_score() != null ? user.getEng_score().intValue() : 0;
        if (!addCheckItem(items, "[트랙] 영어성적(토익)", currentEng, criteria.getEngScore())) allPassed = false;


        // ----------------------------------------------------
        // [트랙별 특수 요건] (GlobalSW 엔티티 활용)
        // ----------------------------------------------------
        int overseas = global.getOverseasCredits() != null ? global.getOverseasCredits() : 0;
        int entre = global.getEntreLecture() != null ? global.getEntreLecture() : 0;
        int startup = global.getStartup() != null ? global.getStartup() : 0;
        int design = global.getDesignLecture() != null ? global.getDesignLecture() : 0;
        // multipleMajor는 다중전공 '이수학점'이 아니라 '이수여부(혹은 학점)'를 체크해야 하는데,
        // 여기서는 편의상 "36학점 이상이면 이수한 것으로 간주"하거나 "1학점 이상이면 이수 중"으로 처리
        int multiMajorCredits = global.getMultipleMajor() != null ? global.getMultipleMajor() : 0;

        // [A] 해외대학 인정학점
        if (criteria.getOverseasCredits() > 0) {
            if (!addCheckItem(items, "[트랙] 해외대학 인정학점", overseas, criteria.getOverseasCredits())) allPassed = false;
        }

        // [B] 창업교과목
        if (criteria.getEntreLecture() > 0) {
            if (!addCheckItem(items, "[트랙] 창업교과목", entre, criteria.getEntreLecture())) allPassed = false;
        }
/**
        // [C] 스타트업 창업 (다중전공 필수)
        if (track == Track.MULTI_MAJOR) {
            boolean passStartup = startup > 0;
            items.add(CheckItem.builder().category("[트랙] 스타트업 창업").current(startup).required(1).isPassed(passStartup).message(passStartup ? "창업완료" : "미창업").build());
            if (!passStartup) allPassed = false;
        }
**/
        // [D] 현장실습 (다중전공, 학석사 필수)
        if (track == Track.MULTI_MAJOR || track == Track.MASTER_LINK) {
            boolean isInternship = user.isInternship();
            items.add(CheckItem.builder().category("[트랙] 현장실습(3학점)").current(isInternship?1:0).required(1).isPassed(isInternship).message(isInternship?"이수":"미이수").build());
            if (!isInternship) allPassed = false;
        }

        // [E] 종합설계 (다중전공 필수 - 2022학번부터)
        if (track == Track.MULTI_MAJOR && entryYear >= 2022) {
            if (!addCheckItem(items, "[트랙] 종합설계과목", design, 3)) allPassed = false;
        }

        // [F] 다중전공 이수 (다중전공 트랙 필수)
        if (track == Track.MULTI_MAJOR) {
            // 다중전공 이수 여부를 학점(36)으로 판단한다고 가정
            if (!addCheckItem(items, "[트랙] 다중전공 이수학점", multiMajorCredits, 36)) allPassed = false;
        }


        // ----------------------------------------------------
        // [필수 과목 체크]
        // ----------------------------------------------------
        List<UserAttend> takenLectures = userAttendRepository.findByStudentId(studentId);
        Set<String> takenCodes = takenLectures.stream().map(UserAttend::getLecId).filter(Objects::nonNull).map(String::trim).collect(Collectors.toSet());

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
        items.add(CheckItem.builder().category("필수과목 이수").current(requiredCourses.size() - missingCourses.size()).required(requiredCourses.size()).isPassed(passCourses).message(passCourses ? "모두 이수함" : "미이수 과목 있음").build());
        if (!passCourses) allPassed = false;

        return GraduationCheckResponse.builder()
                .studentId(studentId)
                .isGraduationPossible(allPassed)
                .checkList(items)
                .missingCourses(missingCourses)
                .build();
    }

    // =========================================================
    // ★ [핵심] 학번과 트랙에 따른 기준표 생성
    // =========================================================
    private GraduationCriteria getCriteriaByYearAndTrack(int year, Track track) {

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

        // 2. 트랙별 기준 설정 (영어, 해외, 창업 등)
        int eng = 700;
        int overseas = 0;
        int entre = 0;

        switch (track) {
            case MULTI_MAJOR: // 다중전공
                eng = 700;
                overseas = 9;
                entre = 9;
                break;
            case OVERSEAS_DEGREE: // 해외복수학위
                eng = 800; // 높음
                overseas = 18; // '복수학위 or 교환학생 1년' -> 학점으로 환산(임의 18)하거나 별도 로직 필요
                entre = 3;
                break;
            case MASTER_LINK: // 학석사연계
                eng = 700;
                overseas = 6;
                entre = 0;
                break;
        }

        return GraduationCriteria.builder()
                .totalCredits(130)      // 공통 130
                .majorCredits(51)       // 공통 51
                .generalMin(genMin).generalMax(genMax)
                .engScore(eng)
                .overseasCredits(overseas)
                .entreLecture(entre)
                .requiredCourses(COMMON_REQUIRED_COURSES) // 모든 트랙 과목 동일
                .build();
    }

    private Track determineTrack(String specificMajor) {
        if (specificMajor.equals("해외복수학위")) return Track.OVERSEAS_DEGREE;
        if (specificMajor.equals("학석사연계")) return Track.MASTER_LINK;
        if (specificMajor.equals("다중전공")) return Track.MULTI_MAJOR; // DB에 "다중전공"이라고 저장된다고 가정

        // 예외: 융합/복수/부전공 등으로 저장된 경우도 다중전공으로 처리
        if (specificMajor.contains("전공")) return Track.MULTI_MAJOR;

        return Track.MULTI_MAJOR; // 기본값
    }

    private boolean addCheckItem(List<CheckItem> items, String category, int current, int required) {
        boolean passed = current >= required;
        items.add(CheckItem.builder().category(category).current(current).required(required).isPassed(passed).message(passed ? "통과" : (current - required) + " (부족)").build());
        return passed;
    }
}