package com.knuaf.roadmap.domain;

import jakarta.persistence.*; //java 기반에서 DB를 쉽게 사용할 수 있는 API 인터페이스
import lombok.*; //어노테이션(주석) 기반으로 코드 자동완성 라이브러리.

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "courses")
public class Course {

    @id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String courseName;

    @Column(nullable = false)
    private String courseCode;

    @Column(nullable = false)
    private Int credits;

    @Column(nullable = false)
    private Double gettingCredits;

    @Column(nullable = false)
    private String semester;

    private String courseType;

    @Builder
    public Course(String courseName, String courseCode, Double credits, String semester, String courseType, Double gettingCredits){
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.semester = semester;
        this.credits = credits;
        this.courseType = courseType;
        this.gettingCredits = gettingCredits;
    }

    public update(String courseName, String courseCode, Double credits, String semester, String courseType, Double gettingCredits){
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.semester = semester;
        this.credits = credits;
        this.courseType = courseType;
        this.gettingCredits = gettingCredits;
    }

}