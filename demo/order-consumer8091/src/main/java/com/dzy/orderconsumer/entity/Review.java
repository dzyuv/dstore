package com.dzy.orderconsumer.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Review {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long productId;
    private Long skuId;
    private Integer score;
    private String content;
    private String images;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}