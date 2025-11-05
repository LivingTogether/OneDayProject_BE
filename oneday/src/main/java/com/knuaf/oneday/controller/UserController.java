package com.knuaf.oneday.controller;

import com.knuaf.oneday.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
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
        //log.info("test");
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUSer(@RequestParam String username,@RequestParam String password){
        log.info("test");
        userService.register(username, password);
        //after signup success, redirect to login page
        log.info("redirection");
        return "redirect:/home";
    }

    @GetMapping("/welcome")
    public String welcome(){
        return "welcome";
    }
}
