package com.knuaf.oneday.component;

import com.knuaf.oneday.entity.User;
import com.knuaf.oneday.repository.UserRepository;
import com.knuaf.oneday.service.CreditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 기존 users 학점 합계를 수강 이력 기준으로 한 번에 다시 계산합니다. (#66)
 * 실행: oneday.credit.recalculate-on-startup=true 로 한 번 기동한 뒤 설정을 다시 끕니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "oneday.credit.recalculate-on-startup", havingValue = "true")
public class CreditRecalculationRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final CreditService creditService;

    @Override
    public void run(ApplicationArguments args) {
        int count = 0;
        for (User user : userRepository.findAll()) {
            try {
                creditService.recalculateTotalCredits(user.getStudentId());
                count++;
            } catch (Exception e) {
                log.warn("학점 재계산 실패: studentId={}, reason={}", user.getStudentId(), e.getMessage());
            }
        }
        log.info("학점 재계산 완료: {}명", count);
    }
}
