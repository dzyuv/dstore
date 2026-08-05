package com.dzy.userservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StoreUpdateRequest {
    @NotNull(message = "门店ID不能为空")
    private Long storeId;
    private String storeName;
    private String logo;
    private String address;
    private String phone;
    private String businessHours;
    private Integer status;
}