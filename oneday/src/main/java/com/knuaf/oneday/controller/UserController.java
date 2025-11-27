package com.knuaf.oneday.controller;

import com.knuaf.oneday.dto.MypageRequest;
import com.knuaf.oneday.dto.SignupRequest;
import com.knuaf.oneday.entity.User;
import com.knuaf.oneday.service.UserService;
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
@CrossOrigin(origins = "http://16.176.198.162:8080") // 3000번 포트 허용
  
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(){
        return "home";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/signup")
    public String signup(){
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUSer(@ModelAttribute SignupRequest request){
        userService.register(request);
        //after signup success, redirect to login page
        return "redirect:/home";
    }
/*
    @GetMapping("/mypage")
    public String showMyPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login"; // 로그인 안 했으면 로그인 창으로
        }
        else {
            User user = new User();
            user.setUserId("guest"); // 아이디: guest
            user.setStudentId(2022000000); // 학번: 0
            user.setMajor("글로벌소프트웨어융합전공"); // 전공: 임시값
            user.setSpecific_major("세부 트랙"); // 세부전공: 안내 메시지

            // 점수 등 숫자 필드도 null이면 에러날 수 있으니 0으로 초기화
            user.setEng_score(0L);
            user.setTotal_credit(0L);
            user.setGeneral_credit(0L);
            user.setMajor_credit(0L);
            user.setInternship(false);
        }

        // HTML로 user 객체 전달
        model.addAttribute("user", user);

        return "mypage";
    }
        // DB에서 내 정보 가져오기
        User user = userService.getMyInfo(userDetails.getUsername());

        // HTML로 데이터 전달 ("user"라는 이름으로 사용 가능)
        model.addAttribute("user", user);

        return "mypage"; // templates/mypage.html 파일을 찾아감
    }
*/
@GetMapping("/mypage")
public String showMyPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
    User user;
    // 1. 로그인을 한 경우 (userDetails가 존재함)
    if (userDetails != null) {
        // DB에서 진짜 내 정보 가져오기
        user = userService.getMyInfo(userDetails.getUsername());
    }
    // 2. 로그인을 안 한 경우 (Guest)
    else {
        user = new User();
        user.setUserId("guest");
        user.setStudentId(2022000000); // String 타입이라면 따옴표, Long이면 L 붙이기
        user.setMajor("글로벌소프트웨어융합전공");

        // 엔티티 필드명에 맞춰서 setter 사용 (User 엔티티 필드명 확인 필요)
        // 예: setSpecificMajor 인지 setSpecific_major 인지 확인하세요.
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
    model.addAttribute("user", user);

    return "mypage";
}

    // ✅ 2. 마이페이지 정보 수정 처리 (POST)
    // HTML form은 기본적으로 PUT을 지원하지 않아 POST를 사용합니다.
    @PostMapping("/mypage/update")
    public String updateMyPage(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute MypageRequest updateDto) { // @RequestBody -> @ModelAttribute 변경

        // 서비스 호출하여 정보 수정
        userService.updateUserInfo(userDetails.getUsername(), updateDto);

        // 수정 후 다시 마이페이지로 돌아가서 변경된 값 확인
        return "redirect:/mypage";
    }

    @GetMapping("/welcome")
    public String welcome(){
        return "welcome";
    }
}
