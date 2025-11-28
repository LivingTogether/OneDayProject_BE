package com.knuaf.oneday.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Advcomp")
public class Advcomp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;//인덱스

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id")
    private User user;

    private Integer abeek_general=0;//abeek 전문교양
    private Integer abeek_total=0; //abeek 총 학점
    private Integer base_major=0; // 전공기반
    private Integer engin_major=0; // 공학 전공

    public void updateCredits(int gen, int base, int engin) {
        this.abeek_general = gen;
        this.base_major = base;
        this.engin_major = engin;
        this.abeek_total = gen + base + engin; // 총점 자동 계산
    }

    @Builder
    public Advcomp(User user) {
        this.user = user;
    }

}
