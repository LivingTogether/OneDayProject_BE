package com.knuaf.oneday.controller;

import com.knuaf.oneday.dto.AdvcompRequest;
import com.knuaf.oneday.dto.AdvcompResponse;
import com.knuaf.oneday.service.AdvcompService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/advcomp")
@RequiredArgsConstructor
public class AdvcompController {

    private final AdvcompService advcompService;

    // 생성: POST /api/advcomp
    @PostMapping
    public ResponseEntity<String> create(@RequestBody AdvcompRequest request) {
        advcompService.create(request);
        return ResponseEntity.ok("등록되었습니다.");
    }

    // 조회: GET /api/advcomp/{studentId}
    @GetMapping("/{studentId}")
    public ResponseEntity<AdvcompResponse> get(@PathVariable Long studentId) {
        return ResponseEntity.ok(advcompService.getInfo(studentId));
    }

    // 수정: PATCH /api/advcomp/{studentId}
    @PatchMapping("/{studentId}")
    public ResponseEntity<String> update(@PathVariable Long studentId, @RequestBody AdvcompRequest request) {
        advcompService.update(studentId, request);
        return ResponseEntity.ok("수정되었습니다.");
    }

    // 삭제: DELETE /api/advcomp/{studentId}
    @DeleteMapping("/{studentId}")
    public ResponseEntity<String> delete(@PathVariable Long studentId) {
        advcompService.delete(studentId);
        return ResponseEntity.ok("삭제되었습니다.");
    }
}