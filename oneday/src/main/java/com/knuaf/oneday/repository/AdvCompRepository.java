package com.knuaf.oneday.repository;

import com.knuaf.oneday.entity.Advcomp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdvCompRepository extends JpaRepository<Advcomp,Long> {
    // studentId로 조회
    Optional<Advcomp> findByUser_StudentId(Long studentId);

    // studentId로 존재 여부 확인
    boolean existsByUser_StudentId(Long studentId);

    // studentId로 삭제
    void deleteByUser_StudentId(Long studentId);
}
