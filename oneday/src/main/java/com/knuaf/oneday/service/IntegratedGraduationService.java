package com.knuaf.oneday.service;

import com.knuaf.oneday.exception.BusinessException;
import com.knuaf.oneday.exception.ErrorCode;
import com.knuaf.oneday.dto.GraduationCheckResponse;
import com.knuaf.oneday.entity.MajorCode;
import com.knuaf.oneday.entity.User;
import com.knuaf.oneday.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntegratedGraduationService {

    private final UserRepository userRepository;
    private final AdvGraduationCheckService advService;    // 심컴
    private final GSWGraduationCheckService gswService;    // 글솝
    private final AIGraduationService aiService;           // 인공지능

    @Transactional(readOnly = true)
    public GraduationCheckResponse checkGraduation(Long studentId, String userName) {

        // 1. 유저 정보 조회
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 전공 코드로 변환 (알 수 없는 전공이면 예외)
        MajorCode majorCode = MajorCode.from(user.getMajor());
        String major = majorCode.getDisplayName();
        String specificMajor = (user.getSpecific_major() == null) ? "" : user.getSpecific_major().trim();

        GraduationCheckResponse response = switch (majorCode) {
            case ADVANCED_COMPUTING, PLATFORM_SW -> advService.checkGraduation(studentId);
            case AI_COMPUTING -> aiService.checkGraduation(studentId);
            case GLOBAL_SW -> gswService.checkGraduation(studentId);
        };

        String majorTypeName;
        if (majorCode == MajorCode.GLOBAL_SW) {
            // 글솝은 세부전공이 곧 트랙 이름
            majorTypeName = specificMajor.isEmpty()
                    ? majorCode.getTrackName() + "(트랙미정)"
                    : "글로벌SW-" + specificMajor; // 예: "글로벌SW-해외복수학위"
        } else {
            // 다중전공 여부에 따라 이름만 다르게 표시 (로직은 동일)
            majorTypeName = specificMajor.equals("다중전공")
                    ? majorCode.getTrackName() + "(다중전공)"
                    : majorCode.getTrackName();
        }

        // 3. 결과 DTO에 최종 전공 이름 설정
        response.setMajorType(majorTypeName);
        response.setUserName(userName);
        log.info("이름: {},학번: {}, 전공: {}, 세부: {}, 최종판정: {}", userName,studentId, major, specificMajor, majorTypeName);

        return response;
    }
}