package com.knuaf.oneday.service;

import com.knuaf.oneday.dto.GraduationCheckResponse;
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
                .orElseThrow(() -> new IllegalArgumentException("해당 학번의 학생 정보가 없습니다."));

        // Null 방지 및 공백 제거
        String major = (user.getMajor() == null) ? "" : user.getMajor().trim();
        String specificMajor = (user.getSpecific_major() == null) ? "" : user.getSpecific_major().trim();

        GraduationCheckResponse response;
        String majorTypeName;

        // 2. 전공(major)에 따른 라우팅
        if (major.contains("심화컴퓨터") || major.contains("심화컴퓨팅")) {
            // [심화컴퓨터공학]
            response = advService.checkGraduation(studentId);

            // 다중전공 여부에 따라 이름만 다르게 표시 (로직은 동일)
            if (specificMajor.equals("다중전공")) {
                majorTypeName = "심화컴퓨터공학(다중전공)";
            } else {
                majorTypeName = "심화컴퓨터공학";
            }

        } else if (major.contains("인공지능")) {
            // [인공지능컴퓨팅전공]
            response = aiService.checkGraduation(studentId);

            if (specificMajor.equals("다중전공")) {
                majorTypeName = "인공지능컴퓨팅전공(다중전공)";
            } else {
                majorTypeName = "인공지능컴퓨팅전공";
            }

        } else if (major.contains("글로벌") || major.contains("Global")) {
            // [글로벌SW융합전공] -> 여긴 specific_major가 로직을 결정함
            response = gswService.checkGraduation(studentId);

            // 글솝은 세부전공이 곧 트랙 이름
            if (specificMajor.isEmpty()) {
                majorTypeName = "글로벌SW융합전공(트랙미정)";
            } else {
                majorTypeName = "글로벌SW-" + specificMajor; // 예: "글로벌SW-해외복수학위"
            }

        } else {
            // DB에 전공 정보가 없거나 이상한 경우
            throw new IllegalArgumentException("전공 정보가 올바르지 않습니다. (현재값: " + major + ")");
        }

        // 3. 결과 DTO에 최종 전공 이름 설정
        response.setMajorType(majorTypeName);
        response.setUserName(userName);
        log.info("이름: {},학번: {}, 전공: {}, 세부: {}, 최종판정: {}", userName,studentId, major, specificMajor, majorTypeName);

        return response;
    }
}