package com.dzy.orderconsumer.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Delivery {
    private Long id;
    private String orderNo;
    private Long storeId;
    private String carrier;
    private String trackingNo;
    private String status;
    private String remark;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}