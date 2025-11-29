package com.knuaf.oneday.service;

import com.knuaf.oneday.entity.Activity;
import com.knuaf.oneday.repository.ActivityRepository;
import com.knuaf.oneday.dto.ActivityDto;
import com.knuaf.oneday.entity.User;
import com.knuaf.oneday.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    public ActivityService(ActivityRepository activityRepository, UserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    // 1. 활동 추가
    public void createActivity(String userId, ActivityDto.Request request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Activity activity = new Activity(
                request.getCategory(),
                request.getTitle(),
                request.getDetail(),
                request.getYear(),
                user
        );
        activityRepository.save(activity);
    }

    // 2. 내 활동 목록 조회
    @Transactional(readOnly = true)
    public List<ActivityDto.Response> getMyActivities(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(()->new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        return activityRepository.findByUser_StudentId(user.getStudentId()).stream()
                .map(a ->new ActivityDto.Response(
                        a.getId(),
                        a.getCategory(),
                        a.getTitle(),
                        a.getDetail(),
                        a.getYear()))
                .collect(Collectors.toList());
    }

    // 3. 활동 수정
    public void updateActivity(String userId, Long activityId, ActivityDto.Request request) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("해당 활동 내역이 없습니다."));

        // 본인의 활동인지 검증
        if (!activity.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        activity.setCategory(request.getCategory());
        activity.setTitle(request.getTitle());
        activity.setDetail(request.getDetail());
        activity.setYear(request.getYear());
        // JPA Dirty Checking으로 인해 save 호출 안 해도 자동 업데이트됨
    }

    // 4. 활동 삭제
    public void deleteActivity(String userId, Long activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("해당 활동 내역이 없습니다."));

        if (!activity.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        activityRepository.delete(activity);
    }
}
