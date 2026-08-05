package com.dzy.userservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MerchantReapplyRequest extends MerchantApplyRequest {
    @NotNull(message = "商家ID不能为空")
    private Long merchantId;
}