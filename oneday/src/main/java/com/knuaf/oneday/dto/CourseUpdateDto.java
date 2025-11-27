package com.knuaf.oneday.dto;

import lombok.Data;

@Data
public class CourseUpdateDto {
    private String lecId;    // 수정할 수강 내역의 고유 번호 (PK)
    private Float received_grade; // 변경할 성적 (예: "B+")
    private String lecType;
}