package com.knuaf.oneday.repository;

import com.knuaf.oneday.entity.Lecture;
import com.knuaf.oneday.entity.UserAttend;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserAttendRepository extends JpaRepository<UserAttend, Long> {

    // ★ 추가: 학번(studentId)과 강좌번호(lecId)로 내역 찾기
    Optional<UserAttend> findByStudentIdAndLecId(Long studentId, String lecId);

    //Optional<> findByStudentId(String studentId);
    // 삭제를 위해 존재하는지 확인하는 메서드
    boolean existsByStudentIdAndLecId(Long studentId, String lecId);
    List<UserAttend> findAllByStudentId(Long studentId);}
