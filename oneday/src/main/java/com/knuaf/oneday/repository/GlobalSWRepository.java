package com.knuaf.oneday.repository;

import com.knuaf.oneday.entity.GlobalSW;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GlobalSWRepository extends JpaRepository<GlobalSW, Long> {
    // 학번으로 조회
    Optional<GlobalSW> findByUser_StudentId(Long studentId);

    // 학번 존재 여부
    boolean existsByUser_StudentId(Long studentId);

    // 학번으로 삭제
    void deleteByUser_StudentId(Long studentId);
}