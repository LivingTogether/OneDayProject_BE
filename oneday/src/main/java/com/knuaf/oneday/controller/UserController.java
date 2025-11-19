package com.knuaf.oneday.controller;

import com.knuaf.oneday.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@CrossOrigin(origins = "http://localhost:8080") // 3000번 포트 허용

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
    public String registerUSer(@RequestParam String userId,@RequestParam String password, @RequestParam String StudentId,@RequestParam String name, @RequestParam String major){
        log.info("test");
        userService.register(userId, password, name, StudentId, major );
        //after signup success, redirect to login page
        return "redirect:/home";
    }

    @GetMapping("/welcome")
    public String welcome(){
        return "welcome";
    }
}
