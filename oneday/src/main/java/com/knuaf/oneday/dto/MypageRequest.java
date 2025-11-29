package com.knuaf.oneday.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MypageRequest {
    private String userId;
    private String password;
    private Long studentId;
    private String major;
    private String name;
    private String specific_major;
    private Long eng_score;
    private Long total_credit;
    private Long general_credit;
    private Long major_credit;
    private Double majorgpa;
    private Double totalgpa;
    private boolean internship;
}
