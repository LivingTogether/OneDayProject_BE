package com.knuaf.oneday.service;

import com.knuaf.oneday.component.GraduationRules;
import com.knuaf.oneday.dto.GraduationCheckResponse;
import com.knuaf.oneday.dto.GraduationCheckResponse.CheckItem;
import com.knuaf.oneday.dto.GraduationCheckResponse.CreditKind;
import com.knuaf.oneday.dto.SimpleGraduationResponse;
import com.knuaf.oneday.entity.MajorCode;
import com.knuaf.oneday.entity.User;
import com.knuaf.oneday.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 간단 졸업 현황.
 * 별도 기준표를 두지 않고 상세 조회 결과(같은 졸업 규칙 테이블)를 요약해 두 조회의 판정이 어긋나지 않게 합니다.
 */
@Service
@RequiredArgsConstructor
public class SimpleGraduationService {

    private final UserRepository userRepository;
    private final IntegratedGraduationService integratedGraduationService;
    private final GraduationRules graduationRules;

    @Transactional(readOnly = true)
    public SimpleGraduationResponse getSimpleStatus(String userId) {

        // 1. User 정보 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));

        long studentId = user.getStudentId();
        int entryYear = (int) (studentId / 1000000); // 학번 앞 4자리 (입학년도)
        MajorCode majorCode = MajorCode.from(user.getMajor());

        // 2. 상세 조회와 같은 규칙으로 판정
        List<CheckItem> items = integratedGraduationService.checkGraduation(studentId, user.getName()).getCheckList();
        CheckItem total = items.stream()
                .filter(item -> item.getKind() == CreditKind.TOTAL)
                .findFirst().orElseThrow();

        // 3. 부족한 학점 계산 (음수 방지)
        int missingMajor = missing(items, CreditKind.MAJOR);
        int missingGeneral = missing(items, CreditKind.GENERAL);

        // 4. 교양 상한 초과 학점 (2018~2022학번은 최대 42학점)
        int myGeneral = user.getGeneral_credit() != null ? user.getGeneral_credit().intValue() : 0;
        int generalMax = graduationRules.criteria(majorCode, entryYear).getGeneralMax();
        int exceededGeneral = Math.max(0, myGeneral - generalMax);

        // 5. 결과 반환
        return SimpleGraduationResponse.builder()
                .studentId(studentId)
                .majorName(user.getMajor())
                .currentTotal(total.getCurrent().intValue())
                .requiredTotal(total.getRequired().intValue())
                .missingMajor(missingMajor)       // 부족한 전공
                .missingGeneral(missingGeneral)   // 부족한 교양
                .exceededGeneral(exceededGeneral) // 상한을 넘은 교양
                .build();
    }

    private int missing(List<CheckItem> items, CreditKind kind) {
        return items.stream()
                .filter(item -> item.getKind() == kind)
                .mapToInt(item -> Math.max(0, item.getRequired().intValue() - item.getCurrent().intValue()))
                .sum();
    }
}
