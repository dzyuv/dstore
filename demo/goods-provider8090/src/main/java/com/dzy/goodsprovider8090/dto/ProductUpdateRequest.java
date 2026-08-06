package com.dzy.goodsprovider8090.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductUpdateRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    private Long categoryId;

    private String name;

    private String mainImage;

    private String detail;
}
