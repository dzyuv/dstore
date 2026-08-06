package com.dzy.goodsprovider8090.service.impl;

import com.dzy.common.constants.Constants;
import com.dzy.common.exception.BusinessException;
import com.dzy.goodsprovider8090.dto.StockChangeRequest;
import com.dzy.goodsprovider8090.entity.Product;
import com.dzy.goodsprovider8090.entity.ProductSku;
import com.dzy.goodsprovider8090.entity.StockLog;
import com.dzy.goodsprovider8090.mapper.ProductMapper;
import com.dzy.goodsprovider8090.mapper.ProductSkuMapper;
import com.dzy.goodsprovider8090.mapper.StockLogMapper;
import com.dzy.goodsprovider8090.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private ProductSkuMapper skuMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private StockLogMapper stockLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lock(StockChangeRequest request) {
        for (StockChangeRequest.Item item : request.getItems()) {
            ProductSku sku = skuMapper.selectByIdForUpdate(item.getSkuId());
            if (sku == null) {
                throw new BusinessException("SKU不存在: " + item.getSkuId());
            }
            if (!Constants.SKU_ON.equals(sku.getStatus())) {
                throw new BusinessException("规格已停用: " + sku.getSkuName());
            }
            Product product = productMapper.selectById(sku.getProductId());
            if (product == null || !Constants.PRODUCT_ON_SALE.equals(product.getStatus())) {
                throw new BusinessException("商品已下架，无法下单: " +
                        (product == null ? item.getSkuId() : product.getName()));
            }
            int available = safe(sku.getPhysicalStock()) - safe(sku.getLockedStock());
            if (available < item.getQuantity()) {
                throw new BusinessException("库存不足: " + sku.getSkuName()
                        + "，可用=" + available + "，需要=" + item.getQuantity());
            }
            int rows = skuMapper.lockStock(item.getSkuId(), item.getQuantity());
            if (rows == 0) {
                throw new BusinessException("库存锁定失败（可能并发超卖）: " + sku.getSkuName());
            }
            ProductSku after = skuMapper.selectById(item.getSkuId());
            writeLog(item.getSkuId(), Constants.STOCK_LOCK, item.getQuantity(),
                    after.getPhysicalStock(), after.getLockedStock(),
                    request.getBizNo(), "下单锁定");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlock(StockChangeRequest request) {
        for (StockChangeRequest.Item item : request.getItems()) {
            ProductSku sku = skuMapper.selectByIdForUpdate(item.getSkuId());
            if (sku == null) {
                continue;
            }
            int rows = skuMapper.unlockStock(item.getSkuId(), item.getQuantity());
            if (rows == 0) {
                throw new BusinessException("释放锁定失败，锁定库存不足: " + item.getSkuId());
            }
            ProductSku after = skuMapper.selectById(item.getSkuId());
            writeLog(item.getSkuId(), Constants.STOCK_UNLOCK, item.getQuantity(),
                    after.getPhysicalStock(), after.getLockedStock(),
                    request.getBizNo(), "释放锁定");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deduct(StockChangeRequest request) {
        for (StockChangeRequest.Item item : request.getItems()) {
            ProductSku sku = skuMapper.selectByIdForUpdate(item.getSkuId());
            if (sku == null) {
                throw new BusinessException("SKU不存在: " + item.getSkuId());
            }
            int rows = skuMapper.deductPhysicalAndLocked(item.getSkuId(), item.getQuantity());
            if (rows == 0) {
                throw new BusinessException("出库失败，库存不足: " + sku.getSkuName());
            }
            ProductSku after = skuMapper.selectById(item.getSkuId());
            writeLog(item.getSkuId(), Constants.STOCK_DEDUCT, item.getQuantity(),
                    after.getPhysicalStock(), after.getLockedStock(),
                    request.getBizNo(), "支付出库");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restore(StockChangeRequest request) {
        for (StockChangeRequest.Item item : request.getItems()) {
            ProductSku sku = skuMapper.selectByIdForUpdate(item.getSkuId());
            if (sku == null) {
                throw new BusinessException("SKU不存在: " + item.getSkuId());
            }
            skuMapper.restorePhysical(item.getSkuId(), item.getQuantity());
            ProductSku after = skuMapper.selectById(item.getSkuId());
            writeLog(item.getSkuId(), Constants.STOCK_RESTORE, item.getQuantity(),
                    after.getPhysicalStock(), after.getLockedStock(),
                    request.getBizNo(), "退款回库");
        }
    }

    private void writeLog(Long skuId, String type, int qty, Integer physicalAfter,
                          Integer lockedAfter, String bizNo, String remark) {
        StockLog log = new StockLog();
        log.setSkuId(skuId);
        log.setChangeType(type);
        log.setChangeQty(qty);
        log.setPhysicalAfter(safe(physicalAfter));
        log.setLockedAfter(safe(lockedAfter));
        log.setBizNo(bizNo);
        log.setRemark(remark);
        stockLogMapper.insert(log);
    }

    private int safe(Integer v) {
        return v == null ? 0 : v;
    }
}
