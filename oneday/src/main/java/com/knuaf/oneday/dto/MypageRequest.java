package com.knuaf.oneday.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MypageRequest {
    private String userId;
    private String password;
    private Long studentId;
    private String major;
    private String name;

    @JsonProperty("specific_major")
    private String specific_major;

    @JsonProperty("eng_score")
    private Long eng_score;
    private Long total_credit;
    private Long general_credit;
    private Long major_credit;
    private Double majorgpa;
    private Double totalgpa;
    @JsonProperty("internship")
    private Boolean internship;
}
