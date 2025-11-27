package com.knuaf.oneday.dto;

import com.knuaf.oneday.entity.GlobalSW;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GlobalSWResponse {
    private Long id;
    private Long studentId;
    private Integer overseasCredits;
    private Integer entreLecture;
    private Integer startup;
    private Integer multipleMajor;
    private Integer designLecture;

    // Entity -> DTO 변환
    public static GlobalSWResponse from(GlobalSW entity) {
        return GlobalSWResponse.builder()
                .id(entity.getId())
                .studentId(entity.getStudentId())
                .overseasCredits(entity.getOverseasCredits())
                .entreLecture(entity.getEntreLecture())
                .startup(entity.getStartup())
                .multipleMajor(entity.getMultipleMajor())
                .designLecture(entity.getDesignLecture())
                .build();
    }
}