package com.knuaf.oneday.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class UserUpdateDto {
    @JsonProperty("eng_score")
    private Long eng_score;

    @JsonProperty("specific_major")
    private String specific_major;

    @JsonProperty("internship")
    private Boolean internship;
}
