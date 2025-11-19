package com.knuaf.roadmap.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CourseRequest {
    @NotBlank
    private String courseName;
    private String courseCode;
    private Long credits;
    private String semester;
    private String courseType;
    private Double gettingCredits
}