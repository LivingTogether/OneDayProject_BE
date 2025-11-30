package com.knuaf.oneday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "startup_course") // DB 테이블명
public class StartUp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "lec_id")
    private String lecId;      // 과목번호 (핵심 검색 키)

    @Column(name = "lec_name")
    private String lecName;



}