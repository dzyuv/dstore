package com.dzy.common.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuStockDTO {
    private Long skuId;
    private Long productId;
    private Long storeId;
    private Long merchantId;
    private String productName;
    private String skuName;
    private String image;
    private BigDecimal price;
    private String productStatus;
    private String skuStatus;
    private Integer physicalStock;
    private Integer lockedStock;
    private Integer availableStock;
}
