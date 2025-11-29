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
    //private final AICompRepository aiRepo;
    @Transactional
    public void recalculateTotalCredits(Long studentId) {
        // 1. 유저 조회 (필요하다면 JOIN FETCH로 성능 최적화 가능)
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        // 2. 수강 내역 전체 조회
        List<UserAttend> allAttends = userAttendRepository.findAllByStudentId(studentId);

        // 3. 트랙별 계산
        if ("심화컴퓨팅전공".equals(user.getMajor())||"플랫폼SW융합전공".equals(user.getMajor())) {
            calculateForAdvComputing(user, allAttends);
        } else if ("글로벌SW융합전공".equals(user.getMajor())) {
            calculateForGlobSw(user, allAttends);
        } else if("인공지능컴퓨팅전공".equals(user.getMajor())) {
            calculateForAIComputing(user, allAttends);
        }
    }

    private void calculateForAdvComputing(User user, List<UserAttend> attends) {
        int abeekGen = 0, baseMaj = 0, enginMaj = 0, etcSum = 0;
        int general = 0, major = 0, multipleSum = 0 ;

        double sumTotalScore = 0.0;
        int calcTotalCredit = 0;

        double sumMajorScore = 0.0;
        int calcMajorCredit = 0;

        for (UserAttend attend : attends) {
            String type = attend.getLecType();
            int credit = attend.getCredit();
            Float grade0bj = attend.getReceivedGrade();

            if ("기본소양".equals(type)) abeekGen += credit;
            else if ("전공기반".equals(type)) {
                baseMaj += credit;
                major += credit;
            }
            else if ("공학전공".equals(type)) {
                enginMaj += credit;
                major += credit;
            }
            else if ("교양".equals(type)) general += credit;
            else if ("전공".equals(type)) major += credit;
            else if ("전공필수".equals(type)) major += credit;
            else if ("전공선택".equals(type)) major += credit;
            else if ("전공기초".equals(type)) major += credit;
            else if ("교양필수".equals(type)) general += credit;
            else if ("교양선택".equals(type)) general += credit;
            else if ("일반선택".equals(type)) etcSum += credit;
           // else if ("복수전공".equals(type)) multipleSum += credit;
           // else if ("부전공".equals(type)) multipleSum += credit;
           // else if ("융합전공".equals(type)) multipleSum += credit;
            if(grade0bj != null){
                float grade = grade0bj;

                sumTotalScore += (grade * credit);
                calcTotalCredit += credit;

                if(isMajorTypeForAdv(type)){
                    sumMajorScore += (grade * credit);
                    calcMajorCredit += credit;
                }
            }
        }

        // ★ [핵심] 유저 객체를 통해 기존 데이터가 있는지 확인
        Advcomp adv = user.getAdvComp();

        if (adv == null) {
            adv = new Advcomp(user);
            advRepo.save(adv); // 없으면 새로 만들어서 저장
        }

        adv.updateCredits(abeekGen, baseMaj, enginMaj);

        updateUserEntity(user, abeekGen + general, baseMaj + enginMaj + major,
                abeekGen + baseMaj + enginMaj + etcSum + general + major,
                sumTotalScore, calcTotalCredit, sumMajorScore, calcMajorCredit);
    }

    private void calculateForGlobSw(User user, List<UserAttend> attends) {
        int majorSum = 0, generalSum = 0, multipleSum = 0, etcSum = 0;

        double sumTotalScore = 0.0;
        int calcTotalCredit = 0;

        double sumMajorScore = 0.0;
        int calcMajorCredit = 0;

        for (UserAttend attend : attends) {
            String type = attend.getLecType();
            int credit = attend.getCredit();
            Float grade0bj = attend.getReceivedGrade();

            if ("전공필수".equals(type)) majorSum += credit;
            else if ("교양".equals(type)) generalSum += credit;
            else if ("전공".equals(type)) majorSum += credit;
            else if ("전공선택".equals(type)) majorSum += credit;
            else if ("전공기초".equals(type)) majorSum += credit;
            else if ("교양필수".equals(type)) generalSum += credit;
            else if ("교양선택".equals(type)) generalSum += credit;
            else if ("일반선택".equals(type)) etcSum += credit;
            else if ("복수전공".equals(type)) multipleSum += credit;
            else if ("부전공".equals(type)) multipleSum += credit;
            else if ("융합전공".equals(type)) multipleSum += credit;
            else etcSum += credit;

            if(grade0bj != null){
                Float grade = grade0bj;

                sumTotalScore += (grade * credit);
                calcTotalCredit += credit;

                if(isMajorTypeForGlob(type)){
                    sumMajorScore += (grade * credit);
                    calcMajorCredit += credit;
                }
            }
        }

        // ★ [핵심] 유저 객체를 통해 확인
        GlobalSW glob = user.getGlobalSW();

        if (glob == null) {
            glob = new GlobalSW(user);
            globRepo.save(glob);
        }

        glob.updateCredits(multipleSum);
        updateUserEntity(user, generalSum, majorSum,
                multipleSum + generalSum + majorSum + etcSum,
                        sumTotalScore, calcTotalCredit, sumMajorScore, calcMajorCredit);
    }
    private void calculateForAIComputing(User user, List<UserAttend> attends) {
        int majorSum = 0, generalSum = 0, multipleSum = 0, etcSum = 0;

        double sumTotalScore = 0.0;
        int calcTotalCredit = 0;

        double sumMajorScore = 0.0;
        int calcMajorCredit = 0;

        for (UserAttend attend : attends) {
            String type = attend.getLecType();
            int credit = attend.getCredit();
            Float grade0bj = attend.getReceivedGrade();

            if ("전공필수".equals(type)) majorSum += credit;
            else if ("교양".equals(type)) generalSum += credit;
            else if ("전공".equals(type)) majorSum += credit;
            else if ("전공선택".equals(type)) majorSum += credit;
            else if ("전공기초".equals(type)) majorSum += credit;
            else if ("교양필수".equals(type)) generalSum += credit;
            else if ("교양선택".equals(type)) generalSum += credit;
            else if ("일반선택".equals(type)) etcSum += credit;
            //else if ("복수전공".equals(type)) multipleSum += credit;
            //else if ("부전공".equals(type)) multipleSum += credit;
            //else if ("융합전공".equals(type)) multipleSum += credit;
            else etcSum += credit;

            if(grade0bj != null){
                Float grade = grade0bj;

                sumTotalScore += (grade * credit);
                calcTotalCredit += credit;

                if(isMajorTypeForAIComp(type)){
                    sumMajorScore += (grade * credit);
                    calcMajorCredit += credit;
                }
            }
        }

        //인지전공은 db생성 따로X
        /*AIComp aicomp = user.getAIComp();
        if (aicomp == null) {
            aicomp = new AIComp(user);
            aiRepo.save(aicomp);
        }

        aicomp.updateCredits();*/
        updateUserEntity(user, generalSum, majorSum,
                multipleSum + generalSum + majorSum + etcSum,
                sumTotalScore, calcTotalCredit, sumMajorScore, calcMajorCredit);
    }

    private void updateUserEntity(User user, int generalCredit, int majorCredit, int TotalCredit,
                                  double sumTotalScore, int calcTotalCredit,
                                  double sumMajorScore, int calcMajorCredit){
        user.updateGeneralCredit(generalCredit);
        user.updateMajorCredit(majorCredit);
        user.updateTotalCredit(TotalCredit);

        double totalGpa = (calcTotalCredit == 0) ? 0.0 : (sumTotalScore / calcTotalCredit);
        double majorGpa = (calcMajorCredit == 0) ? 0.0 : (sumMajorScore / calcMajorCredit);

        // 소수점 2자리 반올림
        totalGpa = Math.round(totalGpa * 100.0) / 100.0;
        majorGpa = Math.round(majorGpa * 100.0) / 100.0;

        // User 엔티티에 GPA 저장 (User에 setter나 필드가 있어야 함)
        user.setTotalgpa(totalGpa);
        user.setMajorgpa(majorGpa);
    }
    private boolean isMajorTypeForAdv(String type){
        return "전공기반".equals(type) || "공학전공".equals(type) ||
                "전공".equals(type) || "전공필수".equals(type)||
                "전공선택".equals(type) || "전공기초".equals(type);
    }
    private boolean isMajorTypeForGlob(String type){
        return "전공".equals(type) || "전공필수".equals(type)||
                "전공선택".equals(type) || "전공기초".equals(type);
    }
    private boolean isMajorTypeForAIComp(String type){
        return "전공".equals(type) || "전공필수".equals(type)||
                "전공선택".equals(type) || "전공기초".equals(type);
    }
}
