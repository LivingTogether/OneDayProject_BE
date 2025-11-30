package com.knuaf.oneday.dto;

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
    }
}