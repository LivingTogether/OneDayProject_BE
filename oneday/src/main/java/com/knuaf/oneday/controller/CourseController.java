package com.knuaf.oneday.controller;

import com.knuaf.oneday.dto.CourseRegisterDto;
import com.knuaf.oneday.dto.CourseUpdateDto;
import com.knuaf.oneday.dto.UserAttendResponseDto;
import com.knuaf.oneday.entity.User;
import com.knuaf.oneday.repository.UserRepository;
import com.knuaf.oneday.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final UserRepository userRepository;

    private Long getStudentId(Authentication authentication) {
        // 1. 시큐리티 컨텍스트에서 로그인 아이디(예: "sion") 꺼내기
        String loginId = authentication.getName();

        // 2. DB에서 유저 정보 찾기
        User user = userRepository.findByUserId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 3. 유저 엔티티에 저장된 실제 학번(Long) 반환
        return user.getStudentId();
    }

    // 1. 수강 내역 등록
    @PostMapping("/register")
    public ResponseEntity<String> registerCourse(
            Authentication authentication, // ★ 인증 객체 주입
            @RequestBody CourseRegisterDto request
    ) {
        Long studentId = getStudentId(authentication); // ★ 헬퍼 메서드 호출

        courseService.registerCourse(studentId, request);
        return ResponseEntity.ok("수강 내역이 등록되었습니다.");
    }

    // 2. 학점 수정
    @PutMapping("/update")
    public ResponseEntity<String> updateGrade(
            Authentication authentication, // ★ 인증 객체 주입
            @RequestBody CourseUpdateDto request
    ) {
        Long studentId = getStudentId(authentication); // ★ 헬퍼 메서드 호출

        courseService.updateCourseGrade(studentId, request);
        return ResponseEntity.ok("성적이 수정되었습니다.");
    }

    // 3. 수강 내역 삭제
    @DeleteMapping("/{lecId}")
    public ResponseEntity<String> deleteCourse(
            Authentication authentication, // ★ 인증 객체 주입
            @PathVariable String lecId
    ) {
        Long studentId = getStudentId(authentication); // ★ 헬퍼 메서드 호출

        courseService.deleteCourse(studentId, lecId);
        return ResponseEntity.ok("수강 취소(삭제)가 완료되었습니다.");
    }

    // 4. 내 수강 이력 조회
    @GetMapping("/history")
    public ResponseEntity<List<UserAttendResponseDto>> getMyHistory(
            Authentication authentication // ★ 인증 객체 주입
    ) {
        Long studentId = getStudentId(authentication); // ★ 헬퍼 메서드 호출

        List<UserAttendResponseDto> myCourses = courseService.getMyCourseHistory(studentId);
        return ResponseEntity.ok(myCourses);
    }
}