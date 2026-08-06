package com.dzy.orderconsumer.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long storeId;
    private Long merchantId;
    private Long addressId;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddr;
    private String deliveryTime;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime payTime;
    private String cancelReason;
    private LocalDateTime expireAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}