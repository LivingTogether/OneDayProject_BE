package com.knuaf.oneday.controller;

import com.knuaf.oneday.exception.BusinessException;
import com.knuaf.oneday.exception.ErrorCode;
import com.knuaf.oneday.dto.ActivityDto;
import com.knuaf.oneday.service.ActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    // 조회: GET /api/activity
    @GetMapping
    public ResponseEntity<List<ActivityDto.Response>> getActivities(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return ResponseEntity.ok(activityService.getMyActivities(userDetails.getUsername()));
    }

    // 추가: POST /api/activity
    @PostMapping
    public ResponseEntity<String> addActivity(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ActivityDto.Request request) {
        activityService.createActivity(userDetails.getUsername(), request);
        return ResponseEntity.ok("활동이 추가되었습니다.");
    }

    // 수정: PUT /api/activity/{id}
    @PutMapping("/{id}")
    public ResponseEntity<String> updateActivity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody ActivityDto.Request request) {
        activityService.updateActivity(userDetails.getUsername(), id, request);
        return ResponseEntity.ok("활동이 수정되었습니다.");
    }

    // 삭제: DELETE /api/activity/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteActivity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        activityService.deleteActivity(userDetails.getUsername(), id);
        return ResponseEntity.ok("활동이 삭제되었습니다.");
    }
}