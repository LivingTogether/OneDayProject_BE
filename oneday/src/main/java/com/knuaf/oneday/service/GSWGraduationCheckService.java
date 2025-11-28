package com.knuaf.oneday.service;

import com.knuaf.oneday.dto.GraduationCheckResponse;
import com.knuaf.oneday.dto.GraduationCheckResponse.CheckItem;
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

    // 필수 과목 리스트 (학교 커리큘럼에 맞춰 수정 필수!)
    private static final Map<String, String> REQUIRED_COURSES = new LinkedHashMap<>() {{
        put("COMP0204", "프로그래밍기초");
        put("COME0331", "자료구조");
        put("GLSO0216", "알고리즘실습");
        put("COMP0312", "운영체제");
    }};

    @Transactional(readOnly = true)
    public GraduationCheckResponse checkGraduation(Long studentId) {

        // 1. 데이터 조회
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("User 정보가 없습니다."));
        GlobalSW global = globalSWRepository.findByUser_StudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("GlobalSW 정보가 없습니다."));

        // 2. [수정됨] specific_major 값을 보고 트랙 결정
        String specificMajor = (user.getSpecific_major() == null) ? "" : user.getSpecific_major().trim();
        Track track;

        switch (specificMajor) {
            case "해외복수학위" -> track = Track.OVERSEAS_DEGREE;
            case "학석사연계" -> track = Track.MASTER_LINK;
            case "다중전공" ->
                // ★ 수정 포인트: DB에 "다중전공"이라고 저장된 경우 처리
                    track = Track.MULTI_MAJOR;
            default -> {
                // 예외: 값이 없거나 이상하면 로그 남기고 기본값(다중전공) 처리
                log.warn("글로벌SW 세부전공 불명확: '{}'. 다중전공 트랙으로 처리합니다.", specificMajor);
                track = Track.MULTI_MAJOR;
            }
        }

        log.info("학번: {}, 세부전공: {}, 결정된 트랙: {}", studentId, specificMajor, track);

        // 3. 값 준비
        List<CheckItem> items = new ArrayList<>();
        boolean allPassed = true;

        int totalCredit = (user.getTotal_credit() == null) ? 0 : user.getTotal_credit().intValue();
        int engScore = (user.getEng_score() == null) ? 0 : user.getEng_score().intValue();
        int majorCredit = (user.getMajor_credit() == null) ? 0 : user.getMajor_credit().intValue();
        int generalCredit = (user.getGeneral_credit() == null) ? 0 : user.getGeneral_credit().intValue();
        boolean internship = user.isInternship();

        int overseas = (global.getOverseasCredits() == null) ? 0 : global.getOverseasCredits();
        int entreLecture = (global.getEntreLecture() == null) ? 0 : global.getEntreLecture();
        int startup = (global.getStartup() == null) ? 0 : global.getStartup();
        int multipleMajor = (global.getMultipleMajor() == null) ? 0 : global.getMultipleMajor();
        int designLecture = (global.getDesignLecture() == null) ? 0 : global.getDesignLecture();

        int entryYear = (int) (studentId / 1000000);

        // ----------------------------------------------------
        // [공통 요건 검사]
        // ----------------------------------------------------

        // 1. 총 이수학점
        if (!addCheckItem(items, "총 이수학점", totalCredit, 130)) allPassed = false;

        // 2. SW전공학점
        if (!addCheckItem(items, "SW전공학점", majorCredit, 51)) allPassed = false;

        // 3. 교양학점
        int reqGenMin = 30, reqGenMax = 999;
        if (entryYear >= 2018 && entryYear <= 2022) { reqGenMin = 24; reqGenMax = 42; }

        boolean passGen = (generalCredit >= reqGenMin) && (generalCredit <= reqGenMax);
        items.add(CheckItem.builder()
                .category("교양학점")
                .current(generalCredit)
                .required(reqGenMin)
                .isPassed(passGen)
                .message(passGen ? "통과" : "기준 미달/초과")
                .build());
        if (!passGen) allPassed = false;

        // ----------------------------------------------------
        // [트랙별 요건 설정]
        // ----------------------------------------------------
        int reqEng = 700;
        int reqOverseas = 0;
        int reqEntre = 0;
        boolean reqIntern = false;
        boolean reqStartup = false;
        int reqDesign = 0;
        int reqMultiMajorCredits = 0;

        switch (track) {
            case MULTI_MAJOR:
                reqOverseas = 9;
                reqIntern = true;
                reqEntre = 9;
                reqStartup = true;
                reqDesign = 3;
                reqMultiMajorCredits = 36; // ★ 다중전공 트랙은 36학점 필수
                break;
            case OVERSEAS_DEGREE:
                reqEng = 800;
                reqEntre = 3;
                break;
            case MASTER_LINK:
                reqOverseas = 6;
                reqIntern = true;
                break;
        }

        // ----------------------------------------------------
        // [트랙별 검사 실행]
        // ----------------------------------------------------

        // 영어
        if (!addCheckItem(items, "영어성적(토익)", engScore, reqEng)) allPassed = false;

        // 글로벌 역량
        if (reqOverseas > 0) {
            if (!addCheckItem(items, "글로벌역량(해외학점)", overseas, reqOverseas)) allPassed = false;
        }

        // 기술창업 역량
        if (reqIntern) {
            items.add(CheckItem.builder().category("현장실습").current(internship ? 1 : 0).required(1)
                    .isPassed(internship).message(internship ? "이수" : "미이수").build());
            if (!internship) allPassed = false;
        }
        if (reqEntre > 0) {
            if (!addCheckItem(items, "창업교과목", entreLecture, reqEntre)) allPassed = false;
        }
        if (reqStartup) {
            items.add(CheckItem.builder().category("스타트업 창업").current(startup).required(1)
                    .isPassed(startup > 0).message(startup > 0 ? "창업함" : "미창업").build());
            if (startup == 0) allPassed = false;
        }

        // SW융합 역량 (다중전공 & 설계)
        if (reqMultiMajorCredits > 0) {
            if (!addCheckItem(items, "다중전공 이수학점", multipleMajor, reqMultiMajorCredits)) allPassed = false;
        }
        if (reqDesign > 0) {
            if (!addCheckItem(items, "종합설계과목", designLecture, reqDesign)) allPassed = false;
        }

        // ----------------------------------------------------
        // [필수 과목 이수 여부 체크]
        // ----------------------------------------------------
        List<UserAttend> takenLectures = userAttendRepository.findByStudentId(studentId);
        Set<String> takenCodes = takenLectures.stream()
                .map(UserAttend::getLecId)
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toSet());

        List<String> missingCourses = new ArrayList<>();
        for (Map.Entry<String, String> entry : REQUIRED_COURSES.entrySet()) {
            String code = entry.getKey();
            String name = entry.getValue();
            if (!takenCodes.contains(code)) {
                missingCourses.add(name + " (" + code + ")");
            }
        }

        boolean passCourses = missingCourses.isEmpty();
        items.add(CheckItem.builder()
                .category("필수과목 이수")
                .current(REQUIRED_COURSES.size() - missingCourses.size())
                .required(REQUIRED_COURSES.size())
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