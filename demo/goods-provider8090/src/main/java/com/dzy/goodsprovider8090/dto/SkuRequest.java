package com.dzy.goodsprovider8090.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuRequest {

    /** 更新时传 id，新增不传 */
    private Long id;

    @NotBlank(message = "规格名称不能为空")
    private String skuName;

    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.01", message = "售价必须大于0")
    private BigDecimal price;

    private String image;

    private String barcode;

    @Min(value = 0, message = "库存不能为负")
    private Integer stock;

    /** ON / OFF，默认 ON */
    private String status;
}
