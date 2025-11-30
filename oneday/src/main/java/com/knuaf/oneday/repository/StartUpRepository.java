package com.knuaf.oneday.repository;

import com.knuaf.oneday.entity.StartUp;
import com.knuaf.oneday.entity.User;
import com.knuaf.oneday.entity.UserAttend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StartUpRepository extends JpaRepository<StartUp, Long> {
    // ★ 추가: 학번(studentId)과 강좌번호(lecId)로 내역 찾기
    boolean existsByLecId(String lecId);}