package com.knuaf.oneday.dto;

import com.knuaf.oneday.entity.UserAttend;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserAttendResponseDto {
    private String lecId;       // 과목 번호 (예: COMP001)
    private String lectureName; // 과목명
    private String lecType;
    private int credit;     // 학점
    private Float received_grade;
    // private String semester; // 학기도 필요하다면 UserAttend 엔티티에 필드 추가 후 여기서 반환

    // Entity -> DTO 변환 메서드
    public static UserAttendResponseDto from(UserAttend entity) {
        return UserAttendResponseDto.builder()
                .lecId(entity.getLecId())     // Lecture 객체 안에서 꺼냄
                .lectureName(entity.getLecName())
                .credit(entity.getCredit())
                .lecType(entity.getLecType())
                .received_grade(entity.getReceivedGrade())// 내 성적
                .build();
    }
}