package com.dzy.goodsprovider8090.controller;

import com.dzy.common.constants.Constants;
import com.dzy.common.entity.ResultJSON;
import com.dzy.common.exception.BusinessException;
import com.dzy.goodsprovider8090.dto.ProductCreateRequest;
import com.dzy.goodsprovider8090.dto.ProductStatusRequest;
import com.dzy.goodsprovider8090.dto.ProductUpdateRequest;
import com.dzy.goodsprovider8090.dto.SkuRequest;
import com.dzy.goodsprovider8090.dto.StockAdjustRequest;
import com.dzy.goodsprovider8090.dto.StockChangeRequest;
import com.dzy.goodsprovider8090.service.ProductService;
import com.dzy.goodsprovider8090.service.StockService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商品模块：消费者浏览、商家管理、管理员监管、库存内部接口。
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private ProductService productService;
    @Autowired
    private StockService stockService;

    // ==================== 消费者 ====================

    /** 商品详情（仅上架可见） */
    @GetMapping("/detail/{productId}")
    public ResultJSON detail(@PathVariable Long productId) {
        return ResultJSON.success(productService.detail(productId, true));
    }

    /** 上架商品搜索：名称模糊 + 分类筛选，按上架时间倒序 */
    @GetMapping({"/list", "/search"})
    public ResultJSON search(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Long categoryId,
                             @RequestParam(required = false) Long storeId,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int size) {
        return ResultJSON.success(productService.searchOnSale(keyword, categoryId, storeId, page, size));
    }

    /** SKU 详情（订单服务 Feign 调用） */
    @GetMapping("/sku/{skuId}")
    public ResultJSON getSku(@PathVariable Long skuId) {
        return ResultJSON.success(productService.getSkuDetail(skuId));
    }

    // ==================== 商家 ====================

    /** 创建商品（SPU + SKUs） */
    @PostMapping
    public ResultJSON create(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                             @RequestHeader(value = Constants.HEADER_ROLE, required = false) String role,
                             @Valid @RequestBody ProductCreateRequest request) {
        requireMerchant(role);
        return ResultJSON.success(productService.create(userId, request));
    }

    /** 修改商品基本信息 */
    @PutMapping
    public ResultJSON update(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                             @RequestHeader(value = Constants.HEADER_ROLE, required = false) String role,
                             @Valid @RequestBody ProductUpdateRequest request) {
        requireMerchant(role);
        return ResultJSON.success(productService.update(userId, request));
    }

    /** 商家商品列表 */
    @GetMapping("/merchant/list")
    public ResultJSON merchantList(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                                   @RequestHeader(value = Constants.HEADER_ROLE, required = false) String role,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        requireMerchant(role);
        return ResultJSON.success(productService.merchantList(userId, keyword, status, page, size));
    }

    /** 商家查看自己的商品详情（含下架商品与全部 SKU） */
    @GetMapping("/merchant/{productId}")
    public ResultJSON merchantDetail(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                                     @RequestHeader(value = Constants.HEADER_ROLE, required = false) String role,
                                     @PathVariable Long productId) {
        requireMerchant(role);
        return ResultJSON.success(productService.merchantDetail(userId, productId));
    }

    /** 上下架：ON_SALE / OFF_SALE；PLATFORM_OFF 不可自行上架 */
    @PutMapping("/{productId}/status")
    public ResultJSON changeStatus(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                                   @RequestHeader(value = Constants.HEADER_ROLE, required = false) String role,
                                   @PathVariable Long productId,
                                   @Valid @RequestBody ProductStatusRequest request) {
        requireMerchant(role);
        productService.changeStatus(userId, productId, request.getStatus());
        return ResultJSON.success();
    }

    /** 删除商品（需先下架且无锁定库存） */
    @DeleteMapping("/{productId}")
    public ResultJSON delete(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                             @RequestHeader(value = Constants.HEADER_ROLE, required = false) String role,
                             @PathVariable Long productId) {
        requireMerchant(role);
        productService.delete(userId, productId);
        return ResultJSON.success();
    }

    /** 新增 SKU */
    @PostMapping("/{productId}/skus")
    public ResultJSON addSku(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                             @RequestHeader(value = Constants.HEADER_ROLE, required = false) String role,
                             @PathVariable Long productId,
                             @Valid @RequestBody SkuRequest request) {
        requireMerchant(role);
        return ResultJSON.success(productService.addSku(userId, productId, request));
    }

    /** 修改 SKU */
    @PutMapping("/{productId}/skus")
    public ResultJSON updateSku(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                                @RequestHeader(value = Constants.HEADER_ROLE, required = false) String role,
                                @PathVariable Long productId,
                                @Valid @RequestBody SkuRequest request) {
        requireMerchant(role);
        return ResultJSON.success(productService.updateSku(userId, productId, request));
    }

    /** 删除 SKU */
    @DeleteMapping("/{productId}/skus/{skuId}")
    public ResultJSON deleteSku(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                                @RequestHeader(value = Constants.HEADER_ROLE, required = false) String role,
                                @PathVariable Long productId,
                                @PathVariable Long skuId) {
        requireMerchant(role);
        productService.deleteSku(userId, productId, skuId);
        return ResultJSON.success();
    }

    /** 调整物理库存（正加负减） */
    @PostMapping("/stock/adjust")
    public ResultJSON adjustStock(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                                  @RequestHeader(value = Constants.HEADER_ROLE, required = false) String role,
                                  @Valid @RequestBody StockAdjustRequest request) {
        requireMerchant(role);
        return ResultJSON.success(productService.adjustStock(userId, request));
    }

    /** 库存流水 */
    @GetMapping("/stock-logs/{skuId}")
    public ResultJSON stockLogs(@PathVariable Long skuId,
                                @RequestParam(defaultValue = "50") int limit) {
        return ResultJSON.success(productService.stockLogs(skuId, limit));
    }

    // ==================== 管理员 ====================

    /** 强制下架 */
    @PutMapping("/admin/{productId}/platform-off")
    public ResultJSON platformOff(@RequestHeader(value = Constants.HEADER_ROLE, required = false) String role,
                                  @PathVariable Long productId) {
        requireAdmin(role);
        productService.platformOff(productId);
        return ResultJSON.success();
    }

    /** 全平台商品检索 */
    @GetMapping("/admin/list")
    public ResultJSON adminList(@RequestHeader(value = Constants.HEADER_ROLE, required = false) String role,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) Long merchantId,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size) {
        requireAdmin(role);
        return ResultJSON.success(productService.adminList(keyword, status, merchantId, page, size));
    }

    // ==================== 内部 / 订单 Feign ====================

    /** 商家封禁时自动下架全部在售商品 */
    @PostMapping("/internal/offline-by-merchant")
    public ResultJSON offlineByMerchant(@RequestParam Long merchantId) {
        productService.offlineByMerchant(merchantId);
        return ResultJSON.success();
    }

    @PostMapping("/stock/lock")
    public ResultJSON lockStock(@Valid @RequestBody StockChangeRequest request) {
        stockService.lock(request);
        return ResultJSON.success();
    }

    @PostMapping("/stock/unlock")
    public ResultJSON unlockStock(@Valid @RequestBody StockChangeRequest request) {
        stockService.unlock(request);
        return ResultJSON.success();
    }

    @PostMapping("/stock/deduct")
    public ResultJSON deductStock(@Valid @RequestBody StockChangeRequest request) {
        stockService.deduct(request);
        return ResultJSON.success();
    }

    @PostMapping("/stock/restore")
    public ResultJSON restoreStock(@Valid @RequestBody StockChangeRequest request) {
        stockService.restore(request);
        return ResultJSON.success();
    }

    private void requireMerchant(String role) {
        if (role != null
                && !Constants.ROLE_MERCHANT.equals(role)
                && !Constants.ROLE_ADMIN.equals(role)) {
            throw new BusinessException("仅商家可操作");
        }
    }

    private void requireAdmin(String role) {
        if (role != null && !Constants.ROLE_ADMIN.equals(role)) {
            throw new BusinessException("仅管理员可操作");
        }
    }
}
