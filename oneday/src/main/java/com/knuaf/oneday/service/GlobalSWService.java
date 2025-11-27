package com.knuaf.oneday.service;

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
        if (globalSWRepository.existsByStudentId(request.getStudentId())) {
            throw new IllegalArgumentException("이미 등록된 학번입니다.");
        }
        return globalSWRepository.save(request.toEntity()).getStudentId();
    }

    // 2. 조회
    @Transactional(readOnly = true)
    public GlobalSWResponse getInfo(Long studentId) {
        GlobalSW entity = globalSWRepository.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학번의 정보가 없습니다."));
        return GlobalSWResponse.from(entity);
    }

    // 3. 수정 (Patch: 일부 필드만 수정)
    public void update(Long studentId, GlobalSWRequest request) {
        GlobalSW entity = globalSWRepository.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학번의 정보가 없습니다."));

        // null이 아닌 값만 업데이트
        if (request.getOverseasCredits() != null) entity.setOverseasCredits(request.getOverseasCredits());
        if (request.getEntreLecture() != null) entity.setEntreLecture(request.getEntreLecture());
        if (request.getStartup() != null) entity.setStartup(request.getStartup());
        if (request.getMultipleMajor() != null) entity.setMultipleMajor(request.getMultipleMajor());
        if (request.getDesignLecture() != null) entity.setDesignLecture(request.getDesignLecture());
    }

    // 4. 삭제
    public void delete(Long studentId) {
        if (!globalSWRepository.existsByStudentId(studentId)) {
            throw new IllegalArgumentException("삭제할 정보가 없습니다.");
        }
        globalSWRepository.deleteByStudentId(studentId);
    }
}