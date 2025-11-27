package com.knuaf.oneday.dto;

import com.knuaf.oneday.entity.GlobalSW;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GlobalSWRequest {
    private Long studentId;
    private Integer overseasCredits; // 해외대학 인정학점
    private Integer entreLecture;    // 창업교과목
    private Integer startup;         // 스타트업 여부
    private Integer multipleMajor;   // 다중전공
    private Integer designLecture;   // 종합설계과목

    // DTO -> Entity 변환
    public GlobalSW toEntity() {
        GlobalSW globalSW = new GlobalSW();
        globalSW.setStudentId(this.studentId);
        // 값이 없으면 0으로 초기화 (생성 시)
        globalSW.setOverseasCredits(this.overseasCredits != null ? this.overseasCredits : 0);
        globalSW.setEntreLecture(this.entreLecture != null ? this.entreLecture : 0);
        globalSW.setStartup(this.startup != null ? this.startup : 0);
        globalSW.setMultipleMajor(this.multipleMajor != null ? this.multipleMajor : 0);
        globalSW.setDesignLecture(this.designLecture != null ? this.designLecture : 0);
        return globalSW;
    }
}