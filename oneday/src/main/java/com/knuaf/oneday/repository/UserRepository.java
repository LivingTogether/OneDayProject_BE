package com.knuaf.oneday.repository;

import com.knuaf.oneday.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    //아이디로 조회
    Optional<User> findByUserId(String userId);
    //학번으로 조회
    Optional<User> findByStudentId(Long studentId);
    //아이디로 탈퇴
    void deleteByUserId(String userId);
}
