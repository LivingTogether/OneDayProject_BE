package com.knuaf.oneday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "lecture_list") // DB 테이블명
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "lec_id")
    private String lecId;      // 과목번호 (핵심 검색 키)

    @Column(name = "lec_name")
    private String lecName;

    private Integer credit;    // 학점

    @Column(name = "lec_type")
    private String lecType;    // 이수구분 (전필, 교양 등)

    @Column(name = "open_depart")
    private String openDepart;

    private String language;

    // grade는 강의 목록에서는 '대상 학년'으로 쓰일 것 같네요.
    private Integer grade;

}