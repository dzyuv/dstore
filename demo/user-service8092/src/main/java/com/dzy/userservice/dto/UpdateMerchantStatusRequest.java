package com.dzy.userservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMerchantStatusRequest {
    @NotNull(message = "商家ID不能为空")
    private Long merchantId;

    @NotNull(message = "状态不能为空")
    private String status;   // DISABLED / APPROVED
}