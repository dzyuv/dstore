package com.dzy.goodsprovider8090.vo;

import com.dzy.goodsprovider8090.entity.Product;
import com.dzy.goodsprovider8090.entity.ProductSku;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDetailVO {
    private Product product;
    private List<SkuVO> skus;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer totalAvailableStock;

    @Data
    public static class SkuVO {
        private Long id;
        private Long productId;
        private String skuName;
        private BigDecimal price;
        private String image;
        private String barcode;
        private Integer physicalStock;
        private Integer lockedStock;
        private Integer availableStock;
        private String status;

        public static SkuVO from(ProductSku sku) {
            SkuVO vo = new SkuVO();
            vo.setId(sku.getId());
            vo.setProductId(sku.getProductId());
            vo.setSkuName(sku.getSkuName());
            vo.setPrice(sku.getPrice());
            vo.setImage(sku.getImage());
            vo.setBarcode(sku.getBarcode());
            vo.setPhysicalStock(sku.getPhysicalStock());
            vo.setLockedStock(sku.getLockedStock());
            int physical = sku.getPhysicalStock() == null ? 0 : sku.getPhysicalStock();
            int locked = sku.getLockedStock() == null ? 0 : sku.getLockedStock();
            vo.setAvailableStock(Math.max(0, physical - locked));
            vo.setStatus(sku.getStatus());
            return vo;
        }
    }
}
