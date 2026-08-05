package com.dzy.userservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "状态不能为空")
    private Integer status;   // 1启用，0禁用
}