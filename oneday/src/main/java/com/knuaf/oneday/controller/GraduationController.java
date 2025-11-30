package com.knuaf.oneday.controller;

import com.knuaf.oneday.dto.GraduationCheckResponse;
import com.knuaf.oneday.dto.SimpleGraduationResponse;
import com.knuaf.oneday.entity.User;
import com.knuaf.oneday.repository.UserRepository;
import com.knuaf.oneday.service.IntegratedGraduationService;
import com.knuaf.oneday.service.SimpleGraduationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/graduation")
@RequiredArgsConstructor
public class GraduationController {

    private final IntegratedGraduationService integratedService;
    private final UserRepository userRepository; // 학번을 찾기 위해 추가
    private final SimpleGraduationService simpleService;

    // GET /api/graduation/my-status (URL에 학번을 노출하지 않음)
    @GetMapping("/my-status")
    public ResponseEntity<GraduationCheckResponse> checkMyGraduation(
            @AuthenticationPrincipal UserDetails userDetails) {

        // 1. 로그인한 사용자 아이디(userId) 가져오기
        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        String loginId = userDetails.getUsername(); // 시큐리티가 저장한 ID

        // 2. userId로 User 엔티티 찾기 -> studentId 획득
        User user = userRepository.findByUserId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        Long studentId = user.getStudentId();
        String userName = user.getName();
        // 3. 찾은 studentId로 기존 로직 수행
        GraduationCheckResponse response = integratedService.checkGraduation(studentId,userName);

        return ResponseEntity.ok(response);
    }
    // GET /api/graduation/simple
    @GetMapping("/simple")
    public ResponseEntity<SimpleGraduationResponse> getSimpleStatus(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        return ResponseEntity.ok(simpleService.getSimpleStatus(userDetails.getUsername()));
    }
}