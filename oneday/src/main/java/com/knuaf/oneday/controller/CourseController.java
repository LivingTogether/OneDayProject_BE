package com.knuaf.oneday.controller;

import com.knuaf.oneday.dto.CourseRegisterDto;
import com.knuaf.oneday.dto.CourseUpdateDto;
import com.knuaf.oneday.dto.LectureResponseDto;
import com.knuaf.oneday.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping("/register")
    public ResponseEntity<String> registerCourse(@RequestBody CourseRegisterDto request) {
        // TODO: 실제로는 Security에서 studentId를 꺼내와야 함 (지금은 임시로 넣음)
        Long tempStudentId = 20241234L;

        courseService.registerCourse(tempStudentId, request);

        return ResponseEntity.ok("수강 내역이 등록되었습니다.");
    }
    // 학점 수정 API
    @PutMapping("/update")
    public ResponseEntity<String> updateGrade(@RequestBody CourseUpdateDto request) {
        Long tempStudentId = 20241234L; // ★ 임시 학번

        courseService.updateCourseGrade(tempStudentId, request);
        return ResponseEntity.ok("성적이 수정되었습니다.");
    }

    // 수강 내역 삭제 API
    // URL 예시: DELETE /api/course/COMP2025 (뒤에 강좌번호가 옴)
    @DeleteMapping("/{lecId}")
    public ResponseEntity<String> deleteCourse(@PathVariable String lecId) {
        Long tempStudentId = 20241234L; // ★ 임시 학번

        courseService.deleteCourse(tempStudentId, lecId);
        return ResponseEntity.ok("수강 취소(삭제)가 완료되었습니다.");
    }

   // @GetMapping("/standard")
    //public ResponseEntity<List<LectureResponseDto>> getCourse() {
      //  List<LectureResponseDto> lectures = lectureService.getLectureList(semester, keyword);
      //  return ResponseEntity.ok(lectures);
   // }

}