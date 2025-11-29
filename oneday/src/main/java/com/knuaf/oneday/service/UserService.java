package com.knuaf.oneday.service;

import com.knuaf.oneday.dto.SignupRequest;
import com.knuaf.oneday.dto.MypageRequest;
import com.knuaf.oneday.entity.Advcomp;
import com.knuaf.oneday.entity.GlobalSW;
import com.knuaf.oneday.entity.User;
import com.knuaf.oneday.entity.UserAttend;
import com.knuaf.oneday.repository.AdvCompRepository;
import com.knuaf.oneday.repository.GlobalSWRepository;
import com.knuaf.oneday.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AdvCompRepository advcompRepository;
    private final PasswordEncoder passwordEncoder;
    private final GlobalSWRepository globalswRepository;
    // 생성자 주입 (여기에 @Lazy 같은 건 필요 없습니다. 파일이 분리되었으니까요!)
    public UserService(UserRepository userRepository, AdvCompRepository advcompRepository, GlobalSWRepository globalswRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.advcompRepository = advcompRepository;
        this.globalswRepository = globalswRepository;
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
            adv.setUser(savedUser);

            adv.setAbeek_general(0);
            adv.setAbeek_total(0);
            adv.setBase_major(0);
            adv.setEngin_major(0);

            advcompRepository.save(adv);
        }
        else if(user.getMajor().equals("글로벌SW융합전공")) {
            GlobalSW gsw = new GlobalSW();
            gsw.setUser(savedUser);

            gsw.setStartup(0);
            gsw.setDesignLecture(0);
            gsw.setEntreLecture(0);
            gsw.setMultipleMajor(0);
            gsw.setOverseasCredits(0);

            globalswRepository.save(gsw);
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
        //user.setMajor(request.getMajor());
        user.setSpecific_major(request.getSpecific_major());
        user.setEng_score(request.getEng_score());
        //user.setTotal_credit(request.getTotal_credit());
        //user.setGeneral_credit(request.getGeneral_credit());
        //user.setMajor_credit(request.getMajor_credit());
        user.setInternship(request.isInternship());

        return userRepository.save(user);
    }

    //5. 회원탈퇴
    public void deleteUser(String userId){
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));

        //advcomp 테이블 데이터 삭제
        advcompRepository.deleteByUser_StudentId(user.getStudentId());
        //globalsw 테이블 데이터 삭제
        globalswRepository.deleteByUser_StudentId(user.getStudentId());

        //aicomp 테이블 데이터 삭제
        //aicompRepository.deleteByUser_StudentId(user.getStudentId());

        //users 테이블 데이터 삭제
        userRepository.deleteByUserId(userId);
    }
}