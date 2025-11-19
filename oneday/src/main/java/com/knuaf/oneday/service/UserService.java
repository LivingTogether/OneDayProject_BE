package com.knuaf.oneday.service;

import com.knuaf.oneday.entity.User;
import com.knuaf.oneday.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //회원가입 로직
    public User register(String userId, String password, String name, String StudentId, String major){

        log.info("회원가입 시도: userid={}", userId);

        User user = new User();
        user.setUserId(userId);
        user.setStudentId(StudentId);
        user.setMajor(major);
        user.setName(name);
        //비밀번호 암호화 후 저장
        user.setPassword(passwordEncoder.encode(password));
        User savedUser = userRepository.save(user);

        // 4. 저장 직후 로그 추가
        log.info("회원가입 성공: userId={}", savedUser.getIdx());

        return savedUser;
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: "+userId));

        return new org.springframework.security.core.userdetails.User(
                user.getUserId(),
                user.getPassword(),
                new ArrayList<>() //권한 목록, 일단 비워둠
        );
    }
}
