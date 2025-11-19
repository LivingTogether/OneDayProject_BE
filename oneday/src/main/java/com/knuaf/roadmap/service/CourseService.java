package com.example.roadmap.service;

import com.example.roadmap.domain.Course;
import com.example.roadmap.dto.CourseRequest;
import com.example.roadmap.dto.CourseResponse;
import com.example.roadmap.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    // 추가 (POST)
    @Transactional
    public Long save(CourseRequest request) {
        Course course = Course.builder()
                .courseName(request.getCourseName())
                .courseCode(request.getCourseCode())
                .credits(request.getCredits())
                .semester(request.getSemester())
                .courseType(request.getCourseType())
                .build();
        return courseRepository.save(course).getId();
    }

    // 목록 조회 (GET)
    @Transactional(readOnly = true)
    public List<CourseResponse> findAll() {
        return courseRepository.findAll().stream()
                .map(CourseResponse::new)
                .collect(Collectors.toList());
    }

    // 수정 (PATCH)
    @Transactional
    public Long update(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 과목 ID입니다: " + id));

        // Course 엔티티의 update 메서드 호출
        course.update(
                request.getCourseName(),
                request.getCourseCode(),
                request.getCredits(),
                request.getSemester(),
                request.getCourseType()
        );
        return id;
    }

    //삭제 (DELETE)
    @Transactional
    public void delete(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 과목 ID입니다: " + id));

        courseRepository.delete(course);
    }
}