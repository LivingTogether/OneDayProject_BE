package com.knuaf.oneday.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_attend")
public class UserAttend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "student_id")
    private Long studentId; // 누가 들었는지

    // --- 아래는 Lecture에서 복사해올 정보들 ---
    @Column(name = "lec_id")
    private String lecId;

    @Column(name = "lec_name")
    private String lecName;

    private Integer credit;

    @Column(name = "received_grade")
    private Float receivedGrade; // 사용자가 입력한 성적 (A+, B0...)

    @Column(name = "lec_type")
    private String lecType;

    @Column(name = "open_depart")
    private String openDepart;

    private String language;


    @Builder
    public UserAttend(Long studentId, Lecture lecture, Float receivedGrade) {
        this.studentId = studentId;
        this.receivedGrade = receivedGrade;
        // Lecture에서 정보 복사
        this.lecId = lecture.getLecNum();
        this.lecName = lecture.getLecName();
        this.credit = lecture.getCredit();
        this.lecType = lecture.getLecType();
        this.openDepart = lecture.getOpenDepart();
        this.language = lecture.getLanguage();
    }
    public void changeGrade(Float newGrade) {
        this.receivedGrade = newGrade;
    }
    public void changeLecType(String newLecType) {
        this.lecType = newLecType;
    }
}