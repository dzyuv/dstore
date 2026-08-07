package com.dzy.goodsprovider8090.service;

import com.dzy.common.entity.PageResult;
import com.dzy.goodsprovider8090.dto.ProductCreateRequest;
import com.dzy.goodsprovider8090.dto.ProductUpdateRequest;
import com.dzy.goodsprovider8090.dto.SkuRequest;
import com.dzy.goodsprovider8090.dto.StockAdjustRequest;
import com.dzy.goodsprovider8090.entity.Product;
import com.dzy.goodsprovider8090.entity.ProductSku;
import com.dzy.goodsprovider8090.entity.StockLog;
import com.dzy.goodsprovider8090.vo.ProductDetailVO;
import com.dzy.goodsprovider8090.vo.ProductListVO;

import java.util.List;
import java.util.Map;

public interface ProductService {

    ProductDetailVO detail(Long productId, boolean consumerView);

    /** 商家查看自己的商品详情 */
    ProductDetailVO merchantDetail(Long userId, Long productId);

    PageResult<ProductListVO> searchOnSale(String keyword, Long categoryId, Long storeId, int page, int size);

    PageResult<ProductListVO> merchantList(Long userId, String keyword, String status, int page, int size);

    PageResult<ProductListVO> adminList(String keyword, String status, Long merchantId, int page, int size);

    Product create(Long userId, ProductCreateRequest request);

    Product update(Long userId, ProductUpdateRequest request);

    void changeStatus(Long userId, Long productId, String status);

    void platformOff(Long productId);

    /** 恢复被平台强制下架的商品：PLATFORM_OFF → OFF_SALE */
    void restoreFromPlatformOff(Long productId);

    void offlineByMerchant(Long merchantId);

    void offlineByStore(Long storeId);

    void delete(Long userId, Long productId);

    ProductSku addSku(Long userId, Long productId, SkuRequest request);

    ProductSku updateSku(Long userId, Long productId, SkuRequest request);

    void deleteSku(Long userId, Long productId, Long skuId);

    ProductSku adjustStock(Long userId, StockAdjustRequest request);

    Map<String, Object> getSkuDetail(Long skuId);

    List<StockLog> stockLogs(Long skuId, int limit);
}
