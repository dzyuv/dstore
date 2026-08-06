package com.dzy.goodsprovider8090.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {
    private Long id;
    private Long storeId;
    private Long merchantId;
    private Long categoryId;
    private String name;
    private String mainImage;
    private String detail;
    private String status;
    private LocalDateTime onSaleTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}