package com.dzy.goodsprovider8090.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductListVO {
    private Long id;
    private Long storeId;
    private Long merchantId;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String mainImage;
    private String status;
    private LocalDateTime onSaleTime;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer skuCount;
    private Integer totalAvailableStock;
}
