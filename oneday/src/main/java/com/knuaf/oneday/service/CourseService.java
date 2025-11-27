package com.knuaf.oneday.service;

import com.knuaf.oneday.dto.CourseRegisterDto;
import com.knuaf.oneday.dto.CourseUpdateDto;
import com.knuaf.oneday.entity.Lecture;
import com.knuaf.oneday.entity.UserAttend;
import com.knuaf.oneday.repository.LectureRepository;
import com.knuaf.oneday.repository.UserAttendRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.knuaf.oneday.component.LectureTableNameProvider;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final EntityManager em; // ★ Native Query를 날리기 위한 매니저
    private final LectureRepository lectureRepository;
    private final UserAttendRepository userAttendRepository;

    // [등록] registerCourse
    @Transactional
    public Long registerCourse(Long studentId, CourseRegisterDto request) {


        String tableName = "lecture_list_2025"+request.getSemester();

        // ★ 2. 쿼리 파라미터에 자른 ID(realLecId) 사용
        String sql = "SELECT * FROM " + tableName + " WHERE lec_num = :lecId"; // 아까 고친 lec_num

        Lecture lecture = null;
        try {
            lecture = (Lecture) em.createNativeQuery(sql, Lecture.class)
                    .setParameter("lecId", request.getLecId()) // ★ 여기도 realLecId 넣기
                    .getSingleResult();
        } catch (Exception e) {
            throw new IllegalArgumentException("해당 학기에 존재하지 않는 과목입니다.");
        }

        UserAttend newHistory = UserAttend.builder()
                .studentId(studentId)
                .lecture(lecture)
                .receivedGrade(request.getReceived_grade())
                .build();

        return userAttendRepository.save(newHistory).getIdx();
    }

    // [수정] updateCourseGrade
    @Transactional
    public void updateCourseGrade(Long studentId, CourseUpdateDto request) {

        // ★ 2. 조회할 때 자른 ID 사용
        UserAttend userAttend = userAttendRepository.findByStudentIdAndLecId(studentId, request.getLecId())
                .orElseThrow(() -> new IllegalArgumentException("신청하지 않은 과목입니다."));

        userAttend.changeGrade(request.getGrade());
        userAttend.changeLecType(request.getLectureType());
        userAttendRepository.save(userAttend);
    }

    // [삭제] deleteCourse
    @Transactional
    public void deleteCourse(Long studentId, String rawLecId) {

        UserAttend userAttend = userAttendRepository.findByStudentIdAndLecId(studentId, rawLecId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 수강 내역이 없습니다."));

        userAttendRepository.delete(userAttend);
    }

    private String parseLecId(String rawId) {
        // null이거나 8자리보다 짧으면 그냥 원본 반환 (에러 방지)
        if (rawId == null || rawId.length() < 8) {
            return rawId;
        }
        // 만약 "-" 기준으로 자르고 싶다면 아래 코드 사용
        return rawId.split("-")[0];
    }
}