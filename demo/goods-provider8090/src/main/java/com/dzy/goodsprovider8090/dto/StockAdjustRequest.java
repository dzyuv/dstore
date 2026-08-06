package com.dzy.goodsprovider8090.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockAdjustRequest {

    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    /**
     * 调整数量：正数增加物理库存，负数减少（不能减到可用库存以下）
     */
    @NotNull(message = "调整数量不能为空")
    private Integer changeQty;

    private String remark;
}
