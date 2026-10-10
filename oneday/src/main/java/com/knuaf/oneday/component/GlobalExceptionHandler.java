package com.knuaf.oneday.component; // 패키지명 확인

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // IllegalArgumentException이 터지면 이 메서드가 낚아칩니다.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        // 1. 에러 메시지를 꺼냄 ("이미 수강 신청한 과목입니다.")
        String errorMessage = ex.getMessage();

        // 2. 400 Bad Request 또는 409 Conflict 상태 코드와 함께 메시지 반환
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
    }

    // 동시 요청으로 UNIQUE 제약(예: 수강 중복 등록)을 위반하면 409로 응답합니다.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 등록된 데이터입니다.");
    }
}
