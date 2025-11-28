package com.knuaf.oneday.service;

import com.knuaf.oneday.dto.AdvGraduationResult;
import com.knuaf.oneday.dto.AdvGraduationResult.CheckItem;
import com.knuaf.oneday.entity.Advcomp;
import com.knuaf.oneday.entity.User;
import com.knuaf.oneday.repository.AdvCompRepository;
import com.knuaf.oneday.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdvGraduationCheckService {

    private final UserRepository userRepository;
    private final AdvCompRepository advcompRepository;

    // 2021~2025학번 기준 상수 정의 (요청하신 대로 설계학점, 졸업심사 제외)
    private static final int REQ_TOTAL_CREDIT = 140;   // 총 이수학점
    private static final int REQ_ABEEK_GENERAL = 15;   // 기본소양 (외국어 제외)
    private static final int REQ_BASE_MAJOR = 18;      // 전공기반
    private static final int REQ_ENGIN_MAJOR = 60;     // 공학전공
    private static final int REQ_ABEEK_TOTAL = 93;     // ABEEK 총점
    private static final int REQ_TOEIC = 700;          // 영어 성적

    @Transactional(readOnly = true)
    public AdvGraduationResult checkGraduation(Long studentId) {

        // 1. 사용자 정보 가져오기 (User + Advcomp)
        User user = userRepository.findByStudentId(studentId) // User 엔티티에 findByStudentId 필요 (혹은 userId로 조회)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        Advcomp adv = advcompRepository.findByUser_StudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("심화컴퓨터 이수 정보를 찾을 수 없습니다."));

        List<CheckItem> items = new ArrayList<>();
        boolean allPassed = true;

        // 2. 검증 로직 시작

        // (1) 총 이수학점 (User 엔티티)
        // User 엔티티의 totalCredit이 Long이라 int로 변환
        int currentTotal = user.getTotal_credit() != null ? user.getTotal_credit().intValue() : 0;
        boolean passTotal = currentTotal >= REQ_TOTAL_CREDIT;
        items.add(createItem("총 이수학점", currentTotal, REQ_TOTAL_CREDIT, passTotal));
        if (!passTotal) allPassed = false;

        // (2) 기본소양 (Advcomp 엔티티)
        int currentGeneral = adv.getAbeek_general() != null ? adv.getAbeek_general() : 0;
        boolean passGeneral = currentGeneral >= REQ_ABEEK_GENERAL;
        items.add(createItem("기본소양(교양)", currentGeneral, REQ_ABEEK_GENERAL, passGeneral));
        if (!passGeneral) allPassed = false;

        // (3) 전공기반 (Advcomp 엔티티)
        int currentBase = adv.getBase_major() != null ? adv.getBase_major() : 0;
        boolean passBase = currentBase >= REQ_BASE_MAJOR;
        items.add(createItem("전공기반", currentBase, REQ_BASE_MAJOR, passBase));
        if (!passBase) allPassed = false;

        // (4) 공학전공 (Advcomp 엔티티)
        int currentEngin = adv.getEngin_major() != null ? adv.getEngin_major() : 0;
        boolean passEngin = currentEngin >= REQ_ENGIN_MAJOR;
        items.add(createItem("공학전공", currentEngin, REQ_ENGIN_MAJOR, passEngin));
        if (!passEngin) allPassed = false;

        // (5) ABEEK 총점 (Advcomp 엔티티)
        int currentAbeekTotal = adv.getAbeek_total() != null ? adv.getAbeek_total() : 0;
        boolean passAbeekTotal = currentAbeekTotal >= REQ_ABEEK_TOTAL;
        items.add(createItem("ABEEK 총점", currentAbeekTotal, REQ_ABEEK_TOTAL, passAbeekTotal));
        if (!passAbeekTotal) allPassed = false;

        // (6) 영어 성적 (User 엔티티)
        int currentEngScore = user.getEng_score() != null ? user.getEng_score().intValue() : 0;
        boolean passEngScore = currentEngScore >= REQ_TOEIC;
        items.add(createItem("영어 성적(토익)", currentEngScore, REQ_TOEIC, passEngScore));
        if (!passEngScore) allPassed = false;

        // (7) 현장실습 (User 엔티티 - boolean)
        // User 엔티티에는 boolean internship으로 되어 있으므로 true면 통과로 간주
        boolean isInternshipDone = user.isInternship();
        items.add(CheckItem.builder()
                .category("현장실습")
                .current(isInternshipDone ? 1 : 0) // 1이면 이수, 0이면 미이수
                .required(1)
                .isPassed(isInternshipDone)
                .message(isInternshipDone ? "이수 완료" : "미이수")
                .build());
        if (!isInternshipDone) allPassed = false;

        // 3. 결과 반환
        return AdvGraduationResult.builder()
                .studentId(studentId)
                .isGraduationPossible(allPassed)
                .checkList(items)
                .build();
    }

    // 헬퍼 메서드: 체크 아이템 생성
    private CheckItem createItem(String category, int current, int required, boolean passed) {
        return CheckItem.builder()
                .category(category)
                .current(current)
                .required(required)
                .isPassed(passed)
                .message(passed ? "통과" : (current - required) + " (부족)")
                .build();
    }
}