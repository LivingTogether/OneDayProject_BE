package com.knuaf.oneday.controller;

import com.knuaf.oneday.exception.BusinessException;
import com.knuaf.oneday.exception.ErrorCode;
import com.knuaf.oneday.dto.LectureResponseDto;
import com.knuaf.oneday.entity.User;
import com.knuaf.oneday.repository.UserRepository;
import com.knuaf.oneday.service.CourseService;
import com.knuaf.oneday.service.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lecture")
@RequiredArgsConstructor
public class LectureController {

    private final UserRepository userRepository;
    private final CourseService courseService;
    private final LectureService lectureService;

    // 내부 헬퍼 메서드
    private String getMajor(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String loginId = authentication.getName();

        User user = userRepository.findByUserId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return user.getMajor();
    }

    // GET /api/lecture/list?semester=2&keyword=컴퓨터
    @GetMapping("/list")
    public ResponseEntity<List<LectureResponseDto>> getLectureList(
            @RequestParam(required = false) String keyword
    ) {
        List<LectureResponseDto> lectures = lectureService.getLectureList(keyword);
        return ResponseEntity.ok(lectures);
    }

    // GET /api/lecture/standard?grade=1&semester=1
    @GetMapping("/standard")
    public ResponseEntity<List<LectureResponseDto>> getStandardCurriculum(
            Authentication authentication, // 로그인이 안되어 있으면 null이 들어옴
            @RequestParam int grade,
            @RequestParam int semester
    ) {
        String major = getMajor(authentication);

        List<LectureResponseDto> standardList = lectureService.getStandardCourses(major, grade, semester);
        return ResponseEntity.ok(standardList);
    }
}