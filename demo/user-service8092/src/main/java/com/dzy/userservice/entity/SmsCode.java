package com.dzy.userservice.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SmsCode {
    private Long id;
    private String phone;
    private String code;
    private String scene;
    private LocalDateTime expireAt;
    private Boolean used;
    private LocalDateTime createdAt;
}
