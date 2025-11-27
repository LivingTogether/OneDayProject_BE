package com.knuaf.oneday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Advcomp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;//인덱스
    private Long studentId;// 학번

    private Integer abeek_general;//abeek 전문교양
    private Integer abeek_total; //abeek 총 학점
    private Integer base_major; // 전공기반
    private Integer engin_major; // 공학 전공
}
