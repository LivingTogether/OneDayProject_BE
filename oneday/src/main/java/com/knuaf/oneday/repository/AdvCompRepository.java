package com.knuaf.oneday.repository;

import com.knuaf.oneday.entity.Advcomp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdvCompRepository extends JpaRepository<Advcomp,Long> {
    Optional<Advcomp> findByStudentId(Long studentId);
}
