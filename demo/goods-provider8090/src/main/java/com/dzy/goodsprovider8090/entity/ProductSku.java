package com.dzy.goodsprovider8090.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductSku {
    private Long id;
    private Long productId;
    private String skuName;
    private BigDecimal price;
    private String image;
    private String barcode;
    private Integer physicalStock;
    private Integer lockedStock;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}