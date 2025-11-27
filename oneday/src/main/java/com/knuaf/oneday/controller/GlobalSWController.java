package com.knuaf.oneday.controller;

import com.knuaf.oneday.dto.GlobalSWRequest;
import com.knuaf.oneday.dto.GlobalSWResponse;
import com.knuaf.oneday.service.GlobalSWService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/globalsw")
@RequiredArgsConstructor
public class GlobalSWController {

    private final GlobalSWService globalSWService;

    // 생성: POST /api/globalsw
    @PostMapping
    public ResponseEntity<String> create(@RequestBody GlobalSWRequest request) {
        globalSWService.create(request);
        return ResponseEntity.ok("등록되었습니다.");
    }

    // 조회: GET /api/globalsw/{studentId}
    @GetMapping("/{studentId}")
    public ResponseEntity<GlobalSWResponse> get(@PathVariable Long studentId) {
        return ResponseEntity.ok(globalSWService.getInfo(studentId));
    }

    // 수정: PATCH /api/globalsw/{studentId}
    @PatchMapping("/{studentId}")
    public ResponseEntity<String> update(@PathVariable Long studentId, @RequestBody GlobalSWRequest request) {
        globalSWService.update(studentId, request);
        return ResponseEntity.ok("수정되었습니다.");
    }

    // 삭제: DELETE /api/globalsw/{studentId}
    @DeleteMapping("/{studentId}")
    public ResponseEntity<String> delete(@PathVariable Long studentId) {
        globalSWService.delete(studentId);
        return ResponseEntity.ok("삭제되었습니다.");
    }
}