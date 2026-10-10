package com.knuaf.oneday.service;

import com.knuaf.oneday.exception.BusinessException;
import com.knuaf.oneday.exception.ErrorCode;
import com.knuaf.oneday.dto.GlobalSWRequest;
import com.knuaf.oneday.dto.GlobalSWResponse;
import com.knuaf.oneday.entity.GlobalSW;
import com.knuaf.oneday.repository.GlobalSWRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GlobalSWService {

    private final GlobalSWRepository globalSWRepository;

    // 1. 생성
    public Long create(GlobalSWRequest request) {
        if (globalSWRepository.existsByUser_StudentId(request.getStudentId())) {
            throw new BusinessException(ErrorCode.TRACK_INFO_ALREADY_EXISTS);
        }
        return globalSWRepository.save(request.toEntity()).getUser().getStudentId();
    }

    // 2. 조회
    @Transactional(readOnly = true)
    public GlobalSWResponse getInfo(Long studentId) {
        GlobalSW entity = globalSWRepository.findByUser_StudentId(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRACK_INFO_NOT_FOUND));
        return GlobalSWResponse.from(entity);
    }

    // 3. 수정 (Patch: 일부 필드만 수정)
    public void update(Long studentId, GlobalSWRequest request) {
        GlobalSW entity = globalSWRepository.findByUser_StudentId(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRACK_INFO_NOT_FOUND));

        // null이 아닌 값만 업데이트
        if (request.getOverseasCredits() != null) entity.setOverseasCredits(request.getOverseasCredits());
        if (request.getEntreLecture() != null) entity.setEntreLecture(request.getEntreLecture());
        if (request.getStartup() != null) entity.setStartup(request.getStartup());
        if (request.getMultipleMajor() != null) entity.setMultipleMajor(request.getMultipleMajor());
        if (request.getDesignLecture() != null) entity.setDesignLecture(request.getDesignLecture());
    }

    // 4. 삭제
    public void delete(Long studentId) {
        if (!globalSWRepository.existsByUser_StudentId(studentId)) {
            throw new BusinessException(ErrorCode.TRACK_INFO_NOT_FOUND);
        }
        globalSWRepository.deleteByUser_StudentId(studentId);
    }
}