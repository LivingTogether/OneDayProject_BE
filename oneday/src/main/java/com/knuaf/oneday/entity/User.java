package com.knuaf.oneday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx; //인덱스
    private String major; //전공
    private String name; // 이름

    @Column(unique = true) // 옵션: 아이디 중복 방지
    private String userId; // 로그인 아이디
    private String password; //

    // 비밀번호
    @Column(name = "student_id", unique = true) // 옵션: 학번 중복 방지
    private long studentId;

    @Column(name = "total_credit") //전체 이수 학점
    private Long total_credit = 0L;

    @Column(name = "major_credit") //전공 이수 학점
    private Long major_credit = 0L;

    @Column(name = "general_credit")//교양 이수 학점
    private Long general_credit = 0L;

    @Column(name = "specific_major")//세부 졸업 트랙
    private String specific_major;

    @Column(name = "total_gpa")//평균 학점
    private Double totalgpa = 0.0;

    @Column(name ="major_gpa")//전공 평균 학점
    private Double majorGpa = 0.0;

    // 양방향 매핑 (선택)
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Advcomp advComp;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private GlobalSW globalSW;

    public void updateGeneralCredit(int credit) {
        this.general_credit = Long.valueOf(credit);
    }
    public void updateTotalCredit(int credit) {
        this.total_credit = Long.valueOf(credit);
    }
    public void updateMajorCredit(int credit) {
        this.major_credit = Long.valueOf(credit);
    }

    private Long eng_score;
    private boolean internship;
    public void setId(Long idx) {
        this.idx = idx;
    }
}
