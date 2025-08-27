package com.kyy.springbootsecuritydemo.work.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HelloJsonDto {

    @JsonProperty("user_name")
    private String userName;
}
