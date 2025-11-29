package com.knuaf.oneday.repository;

import com.knuaf.oneday.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByUser_StudentId(Long studentId);
}
