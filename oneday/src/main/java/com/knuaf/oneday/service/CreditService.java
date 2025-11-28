package com.knuaf.oneday.service;

import com.knuaf.oneday.entity.*;
import com.knuaf.oneday.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditService {

    private final UserRepository userRepository;
    private final UserAttendRepository userAttendRepository;
    private final AdvCompRepository advRepo;
    private final GlobalSWRepository globRepo;

    @Transactional
    public void recalculateTotalCredits(Long studentId) {
        // 1. 유저 조회 (필요하다면 JOIN FETCH로 성능 최적화 가능)
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        // 2. 수강 내역 전체 조회
        List<UserAttend> allAttends = userAttendRepository.findAllByStudentId(studentId);

        // 3. 트랙별 계산
        if ("심화컴퓨팅전공".equals(user.getSpecific_major())) {
            calculateForAdvComputing(user, allAttends);
        } else if ("글로벌소프트웨어융합전공".equals(user.getSpecific_major())) {
            calculateForGlobSw(user, allAttends);
        }
    }

    private void calculateForAdvComputing(User user, List<UserAttend> attends) {
        int abeekGen = 0, baseMaj = 0, enginMaj = 0, etcSum = 0;

        for (UserAttend attend : attends) {
            String type = attend.getLecType();
            int credit = attend.getCredit();

            if ("기본소양".equals(type)) abeekGen += credit;
            else if ("전공기반".equals(type)) baseMaj += credit;
            else if ("공학전공".equals(type)) enginMaj += credit;
            else etcSum += credit;
        }

        // ★ [핵심] 유저 객체를 통해 기존 데이터가 있는지 확인
        Advcomp adv = user.getAdvComp();

        if (adv == null) {
            adv = new Advcomp(user);
            advRepo.save(adv); // 없으면 새로 만들어서 저장
        }

        adv.updateCredits(abeekGen, baseMaj, enginMaj);
        user.updateGeneralCredit(abeekGen);
        user.updateMajorCredit(baseMaj+enginMaj);
        user.updateTotalCredit(abeekGen+baseMaj+enginMaj+etcSum);
    }

    private void calculateForGlobSw(User user, List<UserAttend> attends) {
        int majorSum = 0, generalSum = 0, multipleSum = 0, etcSum = 0;

        for (UserAttend attend : attends) {
            String type = attend.getLecType();
            int credit = attend.getCredit();

            if (type.contains("전공")) majorSum += credit;
            else if (type.contains("교양")) generalSum += credit;
            else if (type.contains("다중")) multipleSum += credit;
            else etcSum += credit;
        }

        // ★ [핵심] 유저 객체를 통해 확인
        GlobalSW glob = user.getGlobalSW();

        if (glob == null) {
            glob = new GlobalSW(user);
            globRepo.save(glob);
        }

        glob.updateCredits(multipleSum);
        user.updateGeneralCredit(generalSum);
        user.updateMajorCredit(majorSum);
        user.updateTotalCredit(multipleSum+generalSum+majorSum+etcSum);
    }
}