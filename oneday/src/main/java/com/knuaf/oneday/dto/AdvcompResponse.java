package com.knuaf.oneday.dto;

import com.knuaf.oneday.entity.Advcomp;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdvcompResponse {
    private Long id;
    private Long studentId;
    private Integer abeekGeneral;
    private Integer abeekTotal;
    private Integer baseMajor;
    private Integer enginMajor;

    // Entity -> DTO 변환
    public static AdvcompResponse from(Advcomp entity) {
        return AdvcompResponse.builder()
                .id(entity.getId())
                .studentId(entity.getUser().getStudentId())
                // 엔티티의 스네이크케이스 필드를 DTO의 카멜케이스로 매핑
                .abeekGeneral(entity.getAbeek_general())
                .abeekTotal(entity.getAbeek_total())
                .baseMajor(entity.getBase_major())
                .enginMajor(entity.getEngin_major())
                .build();
    }
}