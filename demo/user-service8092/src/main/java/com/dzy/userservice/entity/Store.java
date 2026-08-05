package com.dzy.userservice.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Store {
    private Long id;
    private Long merchantId;
    private String storeName;
    private String logo;
    private String address;
    private String phone;
    private String businessHours;
    private Integer status;   // 1营业，0休息
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}