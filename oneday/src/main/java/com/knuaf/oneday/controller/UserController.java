package com.knuaf.oneday.controller;

import com.knuaf.oneday.dto.MypageRequest;
import com.knuaf.oneday.dto.SignupRequest;
import com.knuaf.oneday.dto.UserUpdateDto;
import com.knuaf.oneday.entity.User;
import com.knuaf.oneday.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Controller
@RequestMapping("/api/auth")
//@CrossOrigin(origins = "http://16.176.198.162:8080") // 3000번 포트 허용
  
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/home")
    public String home(){
        return "home";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @PostMapping("/withdraw")
    public String withdraw(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request){
        if(userDetails != null) {
            userService.deleteUser(userDetails.getUsername());
        }

        HttpSession session = request.getSession(false);
        if(session != null){
            session.invalidate();
        }

        return "redirect:/api/auth/home";
    }
    @GetMapping("/signup")
    public String signup(){
        return "signup";
    }

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody SignupRequest request){
        try {
            userService.register(request);
            // 성공 시 200 OK와 메시지 전달
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } catch (Exception e) {
            // 실패 시(예: 중복 ID 등) 400 Bad Request 전달
            return ResponseEntity.badRequest().body("회원가입 실패: " + e.getMessage());
        }
    }
 //로그인 경로 우회 X
    @GetMapping("/mypage")
    public ResponseEntity<User> showMyPage(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getMyInfo(userDetails.getUsername());

        return ResponseEntity.ok(user); // templates/mypage.html 파일을 찾아감
    }
/*
@GetMapping("/mypage")
@ResponseBody
public ResponseEntity<User> showMyPage(@AuthenticationPrincipal UserDetails userDetails) {
    User user;
    // 1. 로그인을 한 경우 (userDetails가 존재함)
    if (userDetails != null) {
        // DB에서 진짜 내 정보 가져오기
        user = userService.getMyInfo(userDetails.getUsername());
    }
    // 2. 로그인을 안 한 경우 (Guest)
    else {
        user = new User();
        user.setUserId("Guest");
        user.setStudentId(2022000000); // String 타입이라면 따옴표, Long이면 L 붙이기
        user.setMajor("글로벌소프트웨어융합전공");

        // 엔티티 필드명에 맞춰서 setter 사용 (User 엔티티 필드명 확인 필요)
        // 보통 자바는 CamelCase(specificMajor)를 씁니다.
        try {
            // 엔티티 필드명이 스네이크케이스라면 이렇게, 카멜케이스라면 setSpecificMajor
            user.setSpecific_major("세부 트랙");

            user.setEng_score(0L);
            user.setTotal_credit(0L);
            user.setGeneral_credit(0L);
            user.setMajor_credit(0L);
            user.setInternship(false);
        } catch (Exception e) {
            // 혹시 필드명이 달라서 에러날까봐 try-catch 해둠 (필요 없으면 삭제)
            UserController.log.warn("Guest user setting error: " + e.getMessage());
        }
    }
    // HTML로 user 객체 전달
    user.setPassword("");

    return ResponseEntity.ok(user);
}*/

    // ✅ 2. 마이페이지 정보 수정 처리 (POST)
    @PostMapping("/mypage/update")
    @ResponseBody
    public ResponseEntity<String> updateMyPage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserUpdateDto updateDto) { // @RequestBody -> @ModelAttribute 변경

        if(userDetails == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        System.out.println("Update Request 도착:");
        System.out.println("User ID: " + userDetails.getUsername());
        System.out.println("Specific Major: " + updateDto.getSpecific_major());
        System.out.println("Eng Score: " + updateDto.getEng_score());
        System.out.println("Internship: " + updateDto.getInternship());

        // 서비스 호출하여 정보 수정
        userService.updateUserInfo(userDetails.getUsername(), updateDto);

        // 수정 후 다시 마이페이지로 돌아가서 변경된 값 확인
        return ResponseEntity.ok("정보가 성공적으로 수정되었습니다.");
    }

    @GetMapping("/welcome")
    public String welcome(){
        return "welcome";
    }
}
