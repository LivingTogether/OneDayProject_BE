package com.knuaf.roadmap.dto;

import com.example.roadmap.domain.Course;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CourseResponse {
    private Long id;
    private String courseName;
    private String courseCode;
    private Double credits;
    private String semester;
    private String courseType;

    public CourseResponse(Course course) {
        this.id = course.getId();
        this.courseName = course.getCourseName();
        this.courseCode = course.getCourseCode();
        this.credits = course.getCredits();
        this.semester = course.getSemester();
        this.courseType = course.getCourseType();
    }
}