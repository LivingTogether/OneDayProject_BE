package com.knuaf.oneday.controller;

import com.knuaf.oneday.dto.AdvGraduationResult;
import com.knuaf.oneday.service.AdvGraduationCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/graduation")
@RequiredArgsConstructor
public class AdvGraduationController {

    private final AdvGraduationCheckService advGraduationService;

    @GetMapping("/check/{studentId}")
    public ResponseEntity<AdvGraduationResult> checkGraduation(@PathVariable Long studentId) {
        AdvGraduationResult result = advGraduationService.checkGraduation(studentId);
        return ResponseEntity.ok(result);
    }
}