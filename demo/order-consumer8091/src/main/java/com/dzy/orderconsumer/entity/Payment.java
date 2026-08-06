package com.dzy.orderconsumer.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Payment {
    private Long id;
    private String paymentNo;
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    private String channel;
    private String status;
    private String payUrl;
    private String thirdTradeNo;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}