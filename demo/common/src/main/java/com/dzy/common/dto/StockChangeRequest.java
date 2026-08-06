package com.dzy.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class StockChangeRequest {
    private String bizNo;
    private List<Item> items;

    @Data
    public static class Item {
        private Long skuId;
        private Integer quantity;
    }
}
