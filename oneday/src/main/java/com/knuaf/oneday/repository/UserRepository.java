package com.knuaf.oneday.repository;

import com.knuaf.oneday.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    //아이디로 조회
    Optional<User> findByUserId(String userId);
    //학번으로 조회
    Optional<User> findByStudentId(Long studentId);
    //학번으로 조회 + 행 잠금 (같은 학생의 수강 변경·학점 재계산을 순서대로 처리)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.studentId = :studentId")
    Optional<User> findByStudentIdForUpdate(@Param("studentId") Long studentId);
    //아이디로 탈퇴
    void deleteByUserId(String userId);
}
