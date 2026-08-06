package com.dzy.goodsprovider8090.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ProductCreateRequest {

    @NotNull(message = "门店ID不能为空")
    private Long storeId;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String mainImage;

    private String detail;

    /** 创建后是否直接上架，默认 false */
    private Boolean onSale;

    @NotEmpty(message = "至少需要一个规格 SKU")
    @Valid
    private List<SkuRequest> skus;
}
