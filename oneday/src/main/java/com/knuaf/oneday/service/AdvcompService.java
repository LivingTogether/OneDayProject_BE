package com.knuaf.oneday.service;

import com.knuaf.oneday.dto.AdvcompRequest;
import com.knuaf.oneday.dto.AdvcompResponse;
import com.knuaf.oneday.entity.Advcomp;
import com.knuaf.oneday.repository.AdvCompRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdvcompService {

    private final AdvCompRepository advcompRepository;

    // 1. 생성 (Create)
    public Long create(AdvcompRequest request) {
        if (advcompRepository.existsByUser_StudentId(request.getStudentId())) {
            throw new IllegalArgumentException("이미 등록된 학번입니다.");
        }
        return advcompRepository.save(request.toEntity()).getUser().getStudentId();
    }

    // 2. 조회 (Read)
    @Transactional(readOnly = true)
    public AdvcompResponse getInfo(Long studentId) {
        Advcomp entity = advcompRepository.findByUser_StudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학번의 정보가 없습니다."));
        return AdvcompResponse.from(entity);
    }

    // 3. 수정 (Patch) - 값이 있는 것만 수정
    public void update(Long studentId, AdvcompRequest request) {
        Advcomp entity = advcompRepository.findByUser_StudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학번의 정보가 없습니다."));

        // DTO(카멜) -> Entity(스네이크) 매핑하면서 null 체크
        if (request.getAbeekGeneral() != null) entity.setAbeek_general(request.getAbeekGeneral());
        if (request.getAbeekTotal() != null) entity.setAbeek_total(request.getAbeekTotal());
        if (request.getBaseMajor() != null) entity.setBase_major(request.getBaseMajor());
        if (request.getEnginMajor() != null) entity.setEngin_major(request.getEnginMajor());
    }

    // 4. 삭제 (Delete)
    public void delete(Long studentId) {
        if (!advcompRepository.existsByUser_StudentId(studentId)) {
            throw new IllegalArgumentException("삭제할 정보가 없습니다.");
        }
        advcompRepository.deleteByUser_StudentId(studentId);
    }
}