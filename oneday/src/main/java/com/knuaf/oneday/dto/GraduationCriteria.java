package com.knuaf.oneday.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class GraduationCriteria {
    // ------------------------------------------------
    // 1. [공통/본부] 졸업요건
    // ------------------------------------------------
    private int totalCredits;      // 총 이수학점 (예: 130, 140)
    private int generalMin;        // 교양 최소
    private int generalMax;        // 교양 최대
    private int engScore;          // 토익 기준 (예: 700, 800)
    private int majorCredits;      // 전공 학점 (예: 51, 60, 72)

    // ------------------------------------------------
    // 2. [심화컴퓨터/ABEEK] 전용 요건
    // ------------------------------------------------
    private int abeekGeneral;      // [ABEEK] 기본소양
    private int abeekBaseMajor;    // [ABEEK] 전공기반(MSC)
    private int abeekEnginMajor;   // [ABEEK] 공학전공
    private int abeekTotal;        // [ABEEK] 인증 총점

    // ------------------------------------------------
    // 3. [글로벌SW] 전용 요건 (★ 새로 추가된 부분)
    // ------------------------------------------------
    private int overseasCredits;   // 해외대학 인정학점 기준 (예: 9, 6, 0)
    private int entreLecture;      // 창업교과목 이수학점 기준 (예: 9, 3, 0)

    // ------------------------------------------------
    // 4. 필수 과목 리스트 (Map<코드, 이름>)
    // ------------------------------------------------
    private Map<String, String> requiredCourses;
}