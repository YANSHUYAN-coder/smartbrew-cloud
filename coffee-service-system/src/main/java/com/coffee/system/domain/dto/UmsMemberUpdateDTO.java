package com.coffee.system.domain.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UmsMemberUpdateDTO {
    private String nickname;
    private String avatar;
    private Integer gender;
    private LocalDate birthday;
    private String city;
    private String job;
    private String signature;
}