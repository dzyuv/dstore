package com.dzy.goodsprovider8090.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CartUpdateRequest {

    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;

    private Boolean selected;
}
