package com.knuaf.oneday.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CourseRegisterDto {
    private String lecId;    // 과목 코드 (예: COMP001)
    private int grade; // 학년
    private int semester; // 몇 년도 몇 학기 테이블에서 찾을지 (202501)
    private String lecType; // 구분
    private Long credit; // 몇학점짜리 수업인지 (3)
    private Float received_grade;    // 내가 받은 성적 (A+)
}