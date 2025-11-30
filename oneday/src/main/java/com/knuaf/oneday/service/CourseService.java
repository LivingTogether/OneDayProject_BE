package com.knuaf.oneday.service;

import com.knuaf.oneday.dto.CourseRegisterDto;
import com.knuaf.oneday.dto.CourseUpdateDto;
import com.knuaf.oneday.dto.UserAttendResponseDto;
import com.knuaf.oneday.entity.Lecture;
import com.knuaf.oneday.entity.User;
import com.knuaf.oneday.entity.UserAttend;
import com.knuaf.oneday.repository.LectureRepository;
import com.knuaf.oneday.repository.UserAttendRepository;
import com.knuaf.oneday.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.knuaf.oneday.component.LectureTableNameProvider;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final EntityManager em; // ★ Native Query를 날리기 위한 매니저
    private final LectureRepository lectureRepository;
    private final UserAttendRepository userAttendRepository;
    private final UserRepository userRepository;
    private final CreditService creditService;
    // [등록] registerCourse
    @Transactional
    public void registerCourse(Long studentId, CourseRegisterDto request) {


        String tableName = "lecture_list";

        // 쿼리 작성 (파라미터 이름을 :lecture로 통일)
        String sql = "SELECT * FROM " + tableName + " WHERE lec_id = :lecture";
        System.out.println("4. 실행될 SQL: " + sql);

        Lecture lecture = null;
        try {
            lecture = (Lecture) em.createNativeQuery(sql, Lecture.class)
                    // [중요] SQL의 :lecture 와 이름을 정확히 맞춰야 함
                    // 만약 잘라낸 ID를 써야 한다면 request.getLecId() 대신 변수명(realLecId)을 넣으세요.
                    .setParameter("lecture", request.getLecId())
                    .getSingleResult();

        } catch (NoResultException e) {
            // DB에 데이터가 없을 때 발생하는 전용 에러
            throw new IllegalArgumentException("해당 학기에 존재하지 않는 과목입니다. (ID: " + request.getLecId() + ")");

        } catch (Exception e) {
            // 문법 오류나 파라미터 바인딩 오류 등 기타 에러
            System.err.println(">> [치명적 에러] SQL 실행 중 문제가 발생했습니다.");
            e.printStackTrace(); // ★ 콘솔에 빨간색으로 상세 에러 로그를 출력함
            throw new IllegalArgumentException("시스템 에러 발생: " + e.getMessage());
        }
        if (userAttendRepository.existsByStudentIdAndLecId(studentId, request.getLecId())) {
            throw new IllegalArgumentException("이미 수강 신청한 과목입니다.");
        }

        UserAttend newHistory = UserAttend.builder()
                .studentId(studentId)
                .lecture(lecture)
                .lecType(request.getLecType())
                .receivedGrade(request.getReceived_grade())
                .grade(request.getGrade())
                .semester(request.getSemester())
                .build();

        userAttendRepository.save(newHistory).getIdx();
        creditService.recalculateTotalCredits(studentId);
    }

    // [수정] updateCourseGrade
    @Transactional
    public void updateCourseGrade(Long studentId, CourseUpdateDto request) {

        // ★ 2. 조회할 때 자른 ID 사용
        UserAttend userAttend = userAttendRepository.findByStudentIdAndLecId(studentId, request.getLecId())
                .orElseThrow(() -> new IllegalArgumentException("신청하지 않은 과목입니다."));

        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보가 없습니다."));

        userAttend.changeGrade(request.getReceivedGrade());
        userAttend.changeLecType(request.getLecType());
        userAttendRepository.save(userAttend);
        creditService.recalculateTotalCredits(studentId);
    }

    // [삭제] deleteCourse
    @Transactional
    public void deleteCourse(Long studentId, String rawLecId) {

        UserAttend userAttend = userAttendRepository.findByStudentIdAndLecId(studentId, rawLecId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 수강 내역이 없습니다."));

       userAttendRepository.delete(userAttend);
        creditService.recalculateTotalCredits(studentId);
    }

    private String parseLecId(String rawId) {
        // null이거나 8자리보다 짧으면 그냥 원본 반환 (에러 방지)
        if (rawId == null || rawId.length() < 8) {
            return rawId;
        }
        // 만약 "-" 기준으로 자르고 싶다면 아래 코드 사용
        return rawId.split("-")[0];
    }

    @Transactional(readOnly = true)
    public List<UserAttendResponseDto> getMyCourseHistory(Long studentId) {

        // 1. 내 학번으로 저장된 모든 내역 조회
        List<UserAttend> historyList = userAttendRepository.findAllByStudentId(studentId);

        // 2. DTO로 변환하여 반환
        return historyList.stream()
                .map(UserAttendResponseDto::from)
                .collect(Collectors.toList());
    }


}