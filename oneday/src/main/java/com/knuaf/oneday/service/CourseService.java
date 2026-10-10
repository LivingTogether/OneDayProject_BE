package com.knuaf.oneday.service;

import com.knuaf.oneday.exception.BusinessException;
import com.knuaf.oneday.exception.ErrorCode;
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
        lockUser(studentId);

        String tableName = "lecture_list";

        // 쿼리 작성 (파라미터 이름을 :lecture로 통일)
        String sql = "SELECT * FROM " + tableName + " WHERE lec_id = :lecture";

        Lecture lecture;
        try {
            lecture = (Lecture) em.createNativeQuery(sql, Lecture.class)
                    .setParameter("lecture", request.getLecId())
                    .getSingleResult();
        } catch (NoResultException e) {
            // DB에 데이터가 없을 때 발생하는 전용 에러
            throw new BusinessException(ErrorCode.LECTURE_NOT_FOUND,
                    "존재하지 않는 과목입니다. (ID: " + request.getLecId() + ")");
        }
        if (userAttendRepository.existsByStudentIdAndLecId(studentId, request.getLecId())) {
            throw new BusinessException(ErrorCode.COURSE_ALREADY_REGISTERED);
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
        lockUser(studentId);

        // ★ 2. 조회할 때 자른 ID 사용
        UserAttend userAttend = userAttendRepository.findByStudentIdAndLecId(studentId, request.getLecId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND, "신청하지 않은 과목입니다."));

        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        userAttend.changeGrade(request.getReceivedGrade());
        userAttend.changeLecType(request.getLecType());
        userAttendRepository.save(userAttend);
        creditService.recalculateTotalCredits(studentId);
    }

    // [삭제] deleteCourse
    @Transactional
    public void deleteCourse(Long studentId, String rawLecId) {
        lockUser(studentId);

        UserAttend userAttend = userAttendRepository.findByStudentIdAndLecId(studentId, rawLecId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND, "삭제할 수강 내역이 없습니다."));

       userAttendRepository.delete(userAttend);
        creditService.recalculateTotalCredits(studentId);
    }

    // 학점 합계는 수강 이력 전체를 다시 읽어 계산하므로, 같은 학생의 요청이 동시에 들어오면
    // 서로의 변경을 보지 못하고 합계를 덮어쓸 수 있다. 유저 행을 먼저 잠가 순서대로 처리한다. (#66)
    private void lockUser(Long studentId) {
        userRepository.findByStudentIdForUpdate(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
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