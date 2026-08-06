package com.dzy.orderconsumer.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderItem {
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long productId;
    private Long skuId;
    private String productName;
    private String skuName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}