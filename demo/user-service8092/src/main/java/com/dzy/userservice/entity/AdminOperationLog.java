package com.dzy.userservice.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminOperationLog {
    private Long id;
    private Long adminId;
    private String adminName;
    private String actionType;
    private String targetType;
    private String targetId;
    private String detail;
    private String result;
    private LocalDateTime createdAt;
}
