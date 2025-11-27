package com.knuaf.oneday.service;

import com.knuaf.oneday.dto.SignupRequest;
import com.knuaf.oneday.dto.MypageRequest;
import com.knuaf.oneday.entity.Advcomp;
import com.knuaf.oneday.entity.User;
import com.knuaf.oneday.repository.AdvCompRepository;
import com.knuaf.oneday.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;
import java.util.ArrayList;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AdvCompRepository advcompRepository;
    private final PasswordEncoder passwordEncoder;

    // 생성자 주입 (여기에 @Lazy 같은 건 필요 없습니다. 파일이 분리되었으니까요!)
    public UserService(UserRepository userRepository, AdvCompRepository advcompRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.advcompRepository = advcompRepository;
    }

    // 1. 회원가입
    public User register(SignupRequest request) {
        User user = new User();
        user.setUserId(request.getUserId());
        // 반드시 암호화해서 저장!
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStudentId(request.getStudentId());
        user.setName(request.getName());
        user.setMajor(request.getMajor());
        User savedUser = userRepository.save(user);

        if(user.getMajor().equals("심화컴퓨팅전공")) {
            Advcomp adv = new Advcomp();
            adv.setStudentId(savedUser.getStudentId());

            adv.setAbeek_general(0);
            adv.setAbeek_total(0);
            adv.setBase_major(0);
            adv.setEngin_major(0);

            advcompRepository.save(adv);
        }
        return savedUser;
    }

    // 2. 로그인 시 사용자 정보 불러오기 (Spring Security 필수)
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        return new org.springframework.security.core.userdetails.User(
                user.getUserId(),
                user.getPassword(),
                new ArrayList<>()
        );
    }

    // 3. 내 정보 조회
    public User getMyInfo(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }

    // 4. 내 정보 수정
    public User updateUserInfo(String userId, MypageRequest request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        user.setMajor(request.getMajor());
        user.setSpecific_major(request.getSpecific_major());
        user.setEng_score(request.getEng_score());
        user.setTotal_credit(request.getTotal_credit());
        user.setGeneral_credit(request.getGeneral_credit());
        user.setMajor_credit(request.getMajor_credit());
        user.setInternship(request.isInternship());

        return userRepository.save(user);
    }
}