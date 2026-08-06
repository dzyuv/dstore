package com.dzy.goodsprovider8090.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductStatusRequest {

    @NotBlank(message = "状态不能为空")
    private String status;
}
