package com.knuaf.oneday.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimpleGraduationResponse {
    private Long studentId;       // 학번
    private String majorName;     // 전공 이름
    private int currentTotal;     // 현재 들은 총 학점
    private int requiredTotal;    // 졸업 기준 총 학점
    private int missingMajor;     // 부족한 전공 학점 (0이면 완료)
    private int missingGeneral;   // 부족한 교양 학점 (0이면 완료)
    private int exceededGeneral;  // 교양 상한을 넘은 학점 (0이면 정상)
}