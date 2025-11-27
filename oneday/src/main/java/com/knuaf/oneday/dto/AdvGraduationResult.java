package com.knuaf.oneday.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class AdvGraduationResult {
    private Long studentId;
    private boolean isGraduationPossible; // 전체 통과 여부
    private List<CheckItem> checkList;    // 세부 항목 리스트

    @Getter
    @Builder
    public static class CheckItem {
        private String category;   // 구분 (예: 총 이수학점, 공학전공...)
        private Number current;    // 내 점수 (학점 or 점수)
        private Number required;   // 기준 점수
        private boolean isPassed;  // 통과 여부
        private String message;    // 부족할 경우 메시지 (예: "-3학점 부족")
    }
}