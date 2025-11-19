// src/main/java/com/example/roadmap/controller/CourseController.java

package com.example.roadmap.controller;

import com.example.roadmap.dto.CourseRequest;
import com.example.roadmap.dto.CourseResponse;
import com.example.roadmap.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses") // 공통 URI 접두사
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // 학점 추가 (POST)
    // URI: POST /api/v1/courses
    @PostMapping
    public ResponseEntity<Long> addCourse(@RequestBody CourseRequest request) {
        Long savedId = courseService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedId);
    }

    // 학점 목록 조회 (GET)
    // URI: GET /api/v1/courses
    @GetMapping
    public ResponseEntity<List<CourseResponse>> findAllCourses() {
        List<CourseResponse> courses = courseService.findAll();
        return ResponseEntity.ok(courses);
    }

    // 학점 수정 (PATCH)
    // URI: PATCH /api/v1/courses/{id}
    @PatchMapping("/{id}")
    public ResponseEntity<Long> updateCourse(
            @PathVariable Long id,
            @RequestBody CourseRequest request) {
        Long updatedId = courseService.update(id, request);
        return ResponseEntity.ok(updatedId);
    }

    // 학점 삭제 (DELETE)
    // URI: DELETE /api/v1/courses/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.ok().build(); // 200 OK와 본문 없음 반환
    }
}