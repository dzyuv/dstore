package com.dzy.userservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuditMerchantRequest {
    @NotNull(message = "商家ID不能为空")
    private Long merchantId;

    @NotNull(message = "审核结果不能为空")
    private Boolean approved;        // true=通过，false=驳回

    private String remark;           // 驳回原因（驳回时必填）
}