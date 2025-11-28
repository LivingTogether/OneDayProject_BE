package com.knuaf.oneday.dto;

import com.knuaf.oneday.entity.Advcomp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdvcompRequest {
    // API 요청은 깔끔하게 카멜케이스로 받습니다.
    private Long studentId;
    private Integer abeekGeneral;
    private Integer abeekTotal;
    private Integer baseMajor;
    private Integer enginMajor;

    // DTO -> Entity 변환 (여기서 이름 매핑이 일어납니다)
    public Advcomp toEntity() {
        Advcomp advcomp = new Advcomp();
        advcomp.getUser().setStudentId(this.studentId);
        // null이면 0으로 저장 (생성 시)
        advcomp.setAbeek_general(this.abeekGeneral != null ? this.abeekGeneral : 0);
        advcomp.setAbeek_total(this.abeekTotal != null ? this.abeekTotal : 0);
        advcomp.setBase_major(this.baseMajor != null ? this.baseMajor : 0);
        advcomp.setEngin_major(this.enginMajor != null ? this.enginMajor : 0);
        return advcomp;
    }
}