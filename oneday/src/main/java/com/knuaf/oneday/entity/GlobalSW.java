package com.knuaf.oneday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class GlobalSW {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long studentId;
    @Column
    private Integer overseasCredits; // 해외대학 인정학점
    @Column
    private Integer entreLecture; // 창업교과목
    @Column
    private Integer startup; // 스타트업여부
    @Column
    private Integer multipleMajor; // 다중전공
    @Column
    private Integer designLecture; // 종합설계과목

}
