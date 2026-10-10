package com.knuaf.oneday.exception;

import org.springframework.http.HttpStatus;

/**
 * API 에러 코드.
 * 응답 형식: {"status": 404, "code": "USER_NOT_FOUND", "message": "사용자를 찾을 수 없습니다."}
 */
public enum ErrorCode {

    // 400 Bad Request
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_MAJOR(HttpStatus.BAD_REQUEST, "전공 정보가 올바르지 않습니다."),

    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),

    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),

    // 404 Not Found
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    LECTURE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 과목입니다."),
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "수강 내역이 없습니다."),
    ACTIVITY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 활동 내역이 없습니다."),
    TRACK_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 학번의 트랙 정보가 없습니다."),

    // 409 Conflict
    COURSE_ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 수강 신청한 과목입니다."),
    TRACK_INFO_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 학번입니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "이미 등록된 데이터입니다."),

    // 500 Internal Server Error
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
