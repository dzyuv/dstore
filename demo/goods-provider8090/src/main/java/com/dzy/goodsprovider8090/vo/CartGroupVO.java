package com.dzy.goodsprovider8090.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartGroupVO {
    private Long storeId;
    private List<CartItemVO> items;
    private BigDecimal selectedAmount;
    private Integer selectedCount;

    @Data
    public static class CartItemVO {
        private Long cartItemId;
        private Long storeId;
        private Long merchantId;   // 新增 - 用于下单
        private Long productId;
        private Long skuId;
        private String productName;
        private String skuName;
        private String image;
        private BigDecimal price;
        private Integer quantity;
        private Boolean selected;
        private Integer availableStock;
        private String productStatus;
        private String skuStatus;
        /** 商品已下架或 SKU 停用 */
        private Boolean invalid;
        private BigDecimal amount;
    }
}
