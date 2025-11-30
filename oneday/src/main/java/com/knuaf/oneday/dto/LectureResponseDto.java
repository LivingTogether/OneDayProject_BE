package com.knuaf.oneday.dto;

import com.knuaf.oneday.entity.Lecture;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureResponseDto {
    private String lecId;       // 과목 ID (DB PK가 아니라 과목코드일 수도 있음)
    private String lectureName; // 강의명
    private String lectureType;
    private Integer credit;     // 학점
    //private Float receivedGrade;

    // Entity -> DTO 변환 메서드 (편의상 추가)
    public static LectureResponseDto from(Lecture lecture) {
        return LectureResponseDto.builder()
                .lecId(lecture.getLecId()) // Entity 필드명에 맞게 수정 필요
                .lectureName(lecture.getLecName())
                .lectureType(lecture.getLecType())
                .credit(lecture.getCredit())
                .build();
    }
}