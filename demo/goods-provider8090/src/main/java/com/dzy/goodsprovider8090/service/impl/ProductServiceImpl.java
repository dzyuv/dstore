package com.dzy.goodsprovider8090.service.impl;

import com.dzy.common.constants.Constants;
import com.dzy.common.entity.PageResult;
import com.dzy.common.entity.ResultJSON;
import com.dzy.common.exception.BusinessException;
import com.dzy.goodsprovider8090.client.UserClient;
import com.dzy.goodsprovider8090.dto.ProductCreateRequest;
import com.dzy.goodsprovider8090.dto.ProductUpdateRequest;
import com.dzy.goodsprovider8090.dto.SkuRequest;
import com.dzy.goodsprovider8090.dto.StockAdjustRequest;
import com.dzy.goodsprovider8090.entity.Category;
import com.dzy.goodsprovider8090.entity.Product;
import com.dzy.goodsprovider8090.entity.ProductSku;
import com.dzy.goodsprovider8090.entity.StockLog;
import com.dzy.goodsprovider8090.mapper.CategoryMapper;
import com.dzy.goodsprovider8090.mapper.ProductMapper;
import com.dzy.goodsprovider8090.mapper.ProductSkuMapper;
import com.dzy.goodsprovider8090.mapper.StockLogMapper;
import com.dzy.goodsprovider8090.service.ProductService;
import com.dzy.goodsprovider8090.vo.ProductDetailVO;
import com.dzy.goodsprovider8090.vo.ProductListVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ProductSkuMapper skuMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private StockLogMapper stockLogMapper;
    @Autowired(required = false)
    private UserClient userClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ProductDetailVO detail(Long productId, boolean consumerView) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (consumerView && !Constants.PRODUCT_ON_SALE.equals(product.getStatus())) {
            throw new BusinessException(404, "商品未上架或已下架");
        }
        List<ProductSku> skus = skuMapper.selectByProduct(productId);
        if (consumerView) {
            skus = skus.stream()
                    .filter(s -> Constants.SKU_ON.equals(s.getStatus()))
                    .collect(Collectors.toList());
        }
        return buildDetailVO(product, skus);
    }

    @Override
    public ProductDetailVO merchantDetail(Long userId, Long productId) {
        Long merchantId = requireMerchantId(userId);
        Product product = requireMerchantProduct(productId, merchantId);
        List<ProductSku> skus = skuMapper.selectByProduct(productId);
        return buildDetailVO(product, skus);
    }

    @Override
    public PageResult<ProductListVO> searchOnSale(String keyword, Long categoryId, Long storeId, int page, int size) {
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 50);
        int offset = (page - 1) * size;
        List<ProductListVO> list = productMapper.searchOnSale(keyword, categoryId, storeId, offset, size);
        long total = productMapper.countOnSale(keyword, categoryId, storeId);
        return PageResult.of(list, total, page, size);
    }

    @Override
    public PageResult<ProductListVO> merchantList(Long userId, String keyword, String status, int page, int size) {
        Long merchantId = requireMerchantId(userId);
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 50);
        int offset = (page - 1) * size;
        return PageResult.of(
                productMapper.merchantSearch(merchantId, keyword, status, offset, size),
                productMapper.merchantCount(merchantId, keyword, status),
                page, size);
    }

    @Override
    public PageResult<ProductListVO> adminList(String keyword, String status, Long merchantId, int page, int size) {
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 50);
        int offset = (page - 1) * size;
        return PageResult.of(
                productMapper.adminSearch(keyword, status, merchantId, offset, size),
                productMapper.adminCount(keyword, status, merchantId),
                page, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Product create(Long userId, ProductCreateRequest request) {
        Long merchantId = requireMerchantId(userId);
        validateStoreBelongsToMerchant(request.getStoreId(), merchantId);
        Category category = categoryMapper.selectById(request.getCategoryId());
        if (category == null || category.getStatus() == null || category.getStatus() != 1) {
            throw new BusinessException("分类不存在或已禁用");
        }

        Product product = new Product();
        product.setStoreId(request.getStoreId());
        product.setMerchantId(merchantId);
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName().trim());
        product.setMainImage(request.getMainImage());
        product.setDetail(request.getDetail());
        boolean onSale = Boolean.TRUE.equals(request.getOnSale());
        product.setStatus(onSale ? Constants.PRODUCT_ON_SALE : Constants.PRODUCT_OFF_SALE);
        productMapper.insert(product);

        for (SkuRequest s : request.getSkus()) {
            insertSku(product.getId(), s);
        }
        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Product update(Long userId, ProductUpdateRequest request) {
        Long merchantId = requireMerchantId(userId);
        Product product = requireMerchantProduct(request.getProductId(), merchantId);
        if (request.getCategoryId() != null) {
            Category category = categoryMapper.selectById(request.getCategoryId());
            if (category == null || category.getStatus() == null || category.getStatus() != 1) {
                throw new BusinessException("分类不存在或已禁用");
            }
            product.setCategoryId(request.getCategoryId());
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            product.setName(request.getName().trim());
        }
        if (request.getMainImage() != null) {
            product.setMainImage(request.getMainImage());
        }
        if (request.getDetail() != null) {
            product.setDetail(request.getDetail());
        }
        productMapper.update(product);
        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long userId, Long productId, String status) {
        Long merchantId = requireMerchantId(userId);
        Product product = requireMerchantProduct(productId, merchantId);

        if (Constants.PRODUCT_PLATFORM_OFF.equals(product.getStatus())
                && Constants.PRODUCT_ON_SALE.equals(status)) {
            throw new BusinessException("平台已下架该商品，不可自行上架");
        }
        if (!Constants.PRODUCT_ON_SALE.equals(status) && !Constants.PRODUCT_OFF_SALE.equals(status)) {
            throw new BusinessException("无效状态，仅支持 ON_SALE / OFF_SALE");
        }
        if (Constants.PRODUCT_ON_SALE.equals(status)) {
            List<ProductSku> skus = skuMapper.selectByProduct(productId);
            boolean hasOn = skus.stream().anyMatch(s ->
                    Constants.SKU_ON.equals(s.getStatus())
                            && safe(s.getPhysicalStock()) - safe(s.getLockedStock()) > 0);
            if (!hasOn) {
                throw new BusinessException("上架失败：至少需要一个启用且有可用库存的规格");
            }
        }
        productMapper.updateStatus(productId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void platformOff(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        productMapper.updateStatus(productId, Constants.PRODUCT_PLATFORM_OFF);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offlineByMerchant(Long merchantId) {
        productMapper.offlineByMerchant(merchantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offlineByStore(Long storeId) {
        productMapper.offlineByStore(storeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long productId) {
        Long merchantId = requireMerchantId(userId);
        Product product = requireMerchantProduct(productId, merchantId);
        if (Constants.PRODUCT_ON_SALE.equals(product.getStatus())) {
            throw new BusinessException("请先下架商品再删除");
        }
        List<ProductSku> skus = skuMapper.selectByProduct(productId);
        for (ProductSku sku : skus) {
            if (safe(sku.getLockedStock()) > 0) {
                throw new BusinessException("存在锁定库存，无法删除，请等待订单完成");
            }
        }
        skuMapper.deleteByProduct(productId);
        productMapper.deleteByIdAndMerchant(productId, merchantId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductSku addSku(Long userId, Long productId, SkuRequest request) {
        Long merchantId = requireMerchantId(userId);
        requireMerchantProduct(productId, merchantId);
        return insertSku(productId, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductSku updateSku(Long userId, Long productId, SkuRequest request) {
        Long merchantId = requireMerchantId(userId);
        requireMerchantProduct(productId, merchantId);
        if (request.getId() == null) {
            throw new BusinessException("SKU ID不能为空");
        }
        ProductSku sku = skuMapper.selectById(request.getId());
        if (sku == null || !productId.equals(sku.getProductId())) {
            throw new BusinessException("SKU不存在或不属于该商品");
        }
        sku.setSkuName(request.getSkuName());
        sku.setPrice(request.getPrice());
        if (request.getImage() != null) {
            sku.setImage(request.getImage());
        }
        if (request.getBarcode() != null) {
            sku.setBarcode(request.getBarcode());
        }
        if (request.getStatus() != null) {
            if (!Constants.SKU_ON.equals(request.getStatus()) && !Constants.SKU_OFF.equals(request.getStatus())) {
                throw new BusinessException("SKU状态仅支持 ON/OFF");
            }
            sku.setStatus(request.getStatus());
        }
        skuMapper.update(sku);
        return skuMapper.selectById(sku.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSku(Long userId, Long productId, Long skuId) {
        Long merchantId = requireMerchantId(userId);
        requireMerchantProduct(productId, merchantId);
        ProductSku sku = skuMapper.selectById(skuId);
        if (sku == null || !productId.equals(sku.getProductId())) {
            throw new BusinessException("SKU不存在或不属于该商品");
        }
        if (safe(sku.getLockedStock()) > 0) {
            throw new BusinessException("该规格存在锁定库存，无法删除");
        }
        if (skuMapper.countByProduct(productId) <= 1) {
            throw new BusinessException("商品至少保留一个规格");
        }
        skuMapper.deleteByIdAndProduct(skuId, productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductSku adjustStock(Long userId, StockAdjustRequest request) {
        Long merchantId = requireMerchantId(userId);
        ProductSku sku = skuMapper.selectByIdForUpdate(request.getSkuId());
        if (sku == null) {
            throw new BusinessException("SKU不存在");
        }
        Product product = productMapper.selectById(sku.getProductId());
        if (product == null || !merchantId.equals(product.getMerchantId())) {
            throw new BusinessException("无权操作该规格库存");
        }
        int qty = request.getChangeQty();
        if (qty == 0) {
            throw new BusinessException("调整数量不能为0");
        }
        int rows = skuMapper.adjustPhysical(sku.getId(), qty);
        if (rows == 0) {
            throw new BusinessException("库存调整失败，调整后物理库存不能低于锁定库存");
        }
        ProductSku after = skuMapper.selectById(sku.getId());
        StockLog log = new StockLog();
        log.setSkuId(sku.getId());
        log.setChangeType(Constants.STOCK_ADJUST);
        log.setChangeQty(qty);
        log.setPhysicalAfter(after.getPhysicalStock());
        log.setLockedAfter(after.getLockedStock());
        log.setBizNo("ADJ-" + System.currentTimeMillis());
        log.setRemark(request.getRemark() == null ? "商家调整库存" : request.getRemark());
        stockLogMapper.insert(log);
        return after;
    }

    @Override
    public Map<String, Object> getSkuDetail(Long skuId) {
        ProductSku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException(404, "SKU不存在");
        }
        Product product = productMapper.selectById(sku.getProductId());
        Map<String, Object> data = new HashMap<>();
        data.put("sku", sku);
        data.put("product", product);
        data.put("availableStock", safe(sku.getPhysicalStock()) - safe(sku.getLockedStock()));
        return data;
    }

    @Override
    public List<StockLog> stockLogs(Long skuId, int limit) {
        if (limit < 1) limit = 50;
        if (limit > 200) limit = 200;
        return stockLogMapper.selectBySku(skuId, limit);
    }

    // ---------- private helpers ----------

    private ProductSku insertSku(Long productId, SkuRequest s) {
        ProductSku sku = new ProductSku();
        sku.setProductId(productId);
        sku.setSkuName(s.getSkuName().trim());
        sku.setPrice(s.getPrice());
        sku.setImage(s.getImage());
        sku.setBarcode(s.getBarcode());
        int stock = s.getStock() == null ? 0 : s.getStock();
        sku.setPhysicalStock(stock);
        sku.setLockedStock(0);
        sku.setStatus(s.getStatus() == null ? Constants.SKU_ON : s.getStatus());
        if (!Constants.SKU_ON.equals(sku.getStatus()) && !Constants.SKU_OFF.equals(sku.getStatus())) {
            throw new BusinessException("SKU状态仅支持 ON/OFF");
        }
        skuMapper.insert(sku);
        if (stock > 0) {
            StockLog log = new StockLog();
            log.setSkuId(sku.getId());
            log.setChangeType(Constants.STOCK_ADJUST);
            log.setChangeQty(stock);
            log.setPhysicalAfter(stock);
            log.setLockedAfter(0);
            log.setBizNo("INIT-" + sku.getId());
            log.setRemark("初始入库");
            stockLogMapper.insert(log);
        }
        return sku;
    }

    private ProductDetailVO buildDetailVO(Product product, List<ProductSku> skus) {
        ProductDetailVO vo = new ProductDetailVO();
        vo.setProduct(product);
        List<ProductDetailVO.SkuVO> skuVOs = skus.stream()
                .map(ProductDetailVO.SkuVO::from)
                .collect(Collectors.toList());
        vo.setSkus(skuVOs);
        if (!skuVOs.isEmpty()) {
            vo.setMinPrice(skuVOs.stream().map(ProductDetailVO.SkuVO::getPrice)
                    .min(BigDecimal::compareTo).orElse(null));
            vo.setMaxPrice(skuVOs.stream().map(ProductDetailVO.SkuVO::getPrice)
                    .max(BigDecimal::compareTo).orElse(null));
            vo.setTotalAvailableStock(skuVOs.stream()
                    .mapToInt(s -> s.getAvailableStock() == null ? 0 : s.getAvailableStock())
                    .sum());
        } else {
            vo.setTotalAvailableStock(0);
        }
        return vo;
    }

    private Long requireMerchantId(Long userId) {
        if (userClient == null) {
            throw new BusinessException("用户服务不可用，无法校验商家身份");
        }
        ResultJSON resp;
        try {
            resp = userClient.getMerchantByUser(userId);
        } catch (Exception e) {
            throw new BusinessException("用户服务调用失败: " + e.getMessage());
        }
        if (resp == null || resp.getCode() != 200 || resp.getData() == null) {
            throw new BusinessException(resp == null ? "商家身份校验失败" : resp.getMsg());
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> merchant = objectMapper.convertValue(resp.getData(), Map.class);
        Object id = merchant.get("id");
        if (id == null) {
            throw new BusinessException("商家信息不完整");
        }
        Object status = merchant.get("status");
        if (status != null && !Constants.MERCHANT_APPROVED.equals(String.valueOf(status))) {
            throw new BusinessException("商家未审核通过或已禁用");
        }
        return Long.valueOf(id.toString());
    }

    private void validateStoreBelongsToMerchant(Long storeId, Long merchantId) {
        if (userClient == null) {
            return;
        }
        ResultJSON resp;
        try {
            resp = userClient.getStore(storeId);
        } catch (Exception e) {
            throw new BusinessException("校验门店失败: " + e.getMessage());
        }
        if (resp == null || resp.getCode() != 200 || resp.getData() == null) {
            throw new BusinessException("门店不存在");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> store = objectMapper.convertValue(resp.getData(), Map.class);
        Object mid = store.get("merchantId");
        if (mid == null || !merchantId.equals(Long.valueOf(mid.toString()))) {
            throw new BusinessException("门店不属于当前商家");
        }
        Object status = store.get("status");
        if (status != null && Integer.parseInt(status.toString()) == 0) {
            throw new BusinessException("门店已休息，无法创建商品");
        }
    }

    private Product requireMerchantProduct(Long productId, Long merchantId) {
        Product product = productMapper.selectByIdAndMerchant(productId, merchantId);
        if (product == null) {
            throw new BusinessException("商品不存在或不属于当前商家");
        }
        return product;
    }

    private int safe(Integer v) {
        return v == null ? 0 : v;
    }
}
