package com.knuaf.oneday.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter; // ★ Setter 추가 (나중에 값을 끼워넣기 위해)
import java.util.List;

@Getter
@Setter // ★ 서비스에서 majorType을 나중에 설정하기 위해 필요
@Builder
public class GraduationCheckResponse {

    // ★ 추가: 적용된 전공 트랙 이름 (예: "글로벌SW-융합전공", "심화컴퓨터공학")
    private String majorType;

    private String userName;
    private Long studentId;
    private boolean isGraduationPossible;
    private List<CheckItem> checkList;
    private List<String> missingCourses;

    @Getter
    @Builder
    public static class CheckItem {
        private String category;
        private Number current;
        private Number required;
        private boolean isPassed;
        private String message;

        // 간단 조회가 상세 조회 결과를 요약할 때 쓰는 항목 분류 (API 응답에는 포함하지 않음)
        @JsonIgnore
        private CreditKind kind;
    }

    public enum CreditKind {
        TOTAL,   // 총 이수학점
        MAJOR,   // 전공(ABEEK 전공기반·공학전공 포함)
        GENERAL  // 교양(ABEEK 기본소양 포함)
    }
}