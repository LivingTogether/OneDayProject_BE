package com.knuaf.oneday.repository;

import com.knuaf.oneday.entity.Advcomp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdvCompRepository extends JpaRepository<Advcomp,Long> {
    // studentId로 조회
    Optional<Advcomp> findByStudentId(Long studentId);

    // studentId로 존재 여부 확인
    boolean existsByStudentId(Long studentId);

    // studentId로 삭제
    void deleteByStudentId(Long studentId);
}
