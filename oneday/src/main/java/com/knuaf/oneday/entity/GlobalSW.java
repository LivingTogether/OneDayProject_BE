package com.knuaf.oneday.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "globalsw")
public class GlobalSW {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id")
    private User user;

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

    @Builder
    public GlobalSW(User user) {
        this.user = user;
    }

    public void updateCredits(int multiple, int design) {

        this.multipleMajor = multiple;
        this.designLecture = design;
    }
    public void updateEntreLecture(int entreLecture) {this.entreLecture = entreLecture;}
    public void updateOverseasCredits(int overseasCredits) {this.overseasCredits = overseasCredits;}
}
