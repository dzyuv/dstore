package com.dzy.goodsprovider8090.service.impl;

import com.dzy.common.constants.Constants;
import com.dzy.common.exception.BusinessException;
import com.dzy.goodsprovider8090.dto.CartAddRequest;
import com.dzy.goodsprovider8090.dto.CartUpdateRequest;
import com.dzy.goodsprovider8090.entity.CartItem;
import com.dzy.goodsprovider8090.entity.Product;
import com.dzy.goodsprovider8090.entity.ProductSku;
import com.dzy.goodsprovider8090.mapper.CartItemMapper;
import com.dzy.goodsprovider8090.mapper.ProductMapper;
import com.dzy.goodsprovider8090.mapper.ProductSkuMapper;
import com.dzy.goodsprovider8090.service.CartService;
import com.dzy.goodsprovider8090.vo.CartGroupVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartItemMapper cartItemMapper;
    @Autowired
    private ProductSkuMapper skuMapper;
    @Autowired
    private ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Long userId, CartAddRequest request) {
        int quantity = request.getQuantity() == null ? 1 : request.getQuantity();
        ProductSku sku = skuMapper.selectById(request.getSkuId());
        if (sku == null || !Constants.SKU_ON.equals(sku.getStatus())) {
            throw new BusinessException("规格不存在或已停用");
        }
        Product product = productMapper.selectById(sku.getProductId());
        if (product == null || !Constants.PRODUCT_ON_SALE.equals(product.getStatus())) {
            throw new BusinessException("商品未上架");
        }
        int available = safe(sku.getPhysicalStock()) - safe(sku.getLockedStock());
        if (available < 1) {
            throw new BusinessException("库存不足");
        }
        cartItemMapper.addOrUpdate(userId, product.getStoreId(), product.getId(),
                sku.getId(), quantity, true);
    }

    @Override
    public List<CartGroupVO> listGrouped(Long userId) {
        List<CartItem> items = cartItemMapper.listByUser(userId);
        Map<Long, List<CartGroupVO.CartItemVO>> grouped = new LinkedHashMap<>();
        for (CartItem item : items) {
            CartGroupVO.CartItemVO row = toItemVO(item);
            grouped.computeIfAbsent(item.getStoreId(), k -> new ArrayList<>()).add(row);
        }
        return grouped.entrySet().stream().map(e -> {
            CartGroupVO g = new CartGroupVO();
            g.setStoreId(e.getKey());
            g.setItems(e.getValue());
            BigDecimal amount = BigDecimal.ZERO;
            int count = 0;
            for (CartGroupVO.CartItemVO it : e.getValue()) {
                if (Boolean.TRUE.equals(it.getSelected()) && !Boolean.TRUE.equals(it.getInvalid())) {
                    amount = amount.add(it.getAmount() == null ? BigDecimal.ZERO : it.getAmount());
                    count += it.getQuantity() == null ? 0 : it.getQuantity();
                }
            }
            g.setSelectedAmount(amount);
            g.setSelectedCount(count);
            return g;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long userId, Long cartItemId, CartUpdateRequest request) {
        CartItem target = cartItemMapper.selectByIdAndUser(cartItemId, userId);
        if (target == null) {
            throw new BusinessException("购物车项不存在");
        }
        int q = request.getQuantity() == null ? target.getQuantity() : request.getQuantity();
        boolean s = request.getSelected() == null
                ? Boolean.TRUE.equals(target.getSelected())
                : request.getSelected();
        if (q < 1) {
            throw new BusinessException("数量至少为1");
        }
        ProductSku sku = skuMapper.selectById(target.getSkuId());
        if (sku != null) {
            int available = safe(sku.getPhysicalStock()) - safe(sku.getLockedStock());
            if (q > available && available >= 0) {
                throw new BusinessException("超过可用库存，当前可用=" + available);
            }
        }
        cartItemMapper.update(cartItemId, userId, q, s);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long cartItemId) {
        int rows = cartItemMapper.delete(cartItemId, userId);
        if (rows == 0) {
            throw new BusinessException("购物车项不存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的商品");
        }
        cartItemMapper.deleteBatch(userId, ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void selectAll(Long userId, boolean selected) {
        cartItemMapper.updateSelectedAll(userId, selected);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void selectByStore(Long userId, Long storeId, boolean selected) {
        cartItemMapper.updateSelectedByStore(userId, storeId, selected);
    }

    @Override
    public List<CartGroupVO.CartItemVO> listSelected(Long userId) {
        return cartItemMapper.listSelected(userId).stream()
                .map(this::toItemVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearSelected(Long userId) {
        cartItemMapper.clearSelected(userId);
    }

    private CartGroupVO.CartItemVO toItemVO(CartItem item) {
        ProductSku sku = skuMapper.selectById(item.getSkuId());
        Product product = productMapper.selectById(item.getProductId());
        CartGroupVO.CartItemVO row = new CartGroupVO.CartItemVO();
        row.setCartItemId(item.getId());
        row.setStoreId(item.getStoreId());
        row.setMerchantId(product != null ? product.getMerchantId() : null); // 新增
        row.setProductId(item.getProductId());
        row.setSkuId(item.getSkuId());
        row.setQuantity(item.getQuantity());
        row.setSelected(item.getSelected());
        if (product != null) {
            row.setProductName(product.getName());
            row.setProductStatus(product.getStatus());
        }
        if (sku != null) {
            row.setSkuName(sku.getSkuName());
            row.setImage(sku.getImage() != null ? sku.getImage()
                    : (product == null ? null : product.getMainImage()));
            row.setPrice(sku.getPrice());
            row.setSkuStatus(sku.getStatus());
            int available = safe(sku.getPhysicalStock()) - safe(sku.getLockedStock());
            row.setAvailableStock(available);
            boolean invalid = !Constants.PRODUCT_ON_SALE.equals(
                    product == null ? null : product.getStatus())
                    || !Constants.SKU_ON.equals(sku.getStatus());
            row.setInvalid(invalid);
            BigDecimal price = sku.getPrice() == null ? BigDecimal.ZERO : sku.getPrice();
            row.setAmount(price.multiply(BigDecimal.valueOf(item.getQuantity())));
        } else {
            row.setInvalid(true);
            row.setAvailableStock(0);
            row.setAmount(BigDecimal.ZERO);
        }
        return row;
    }

    private int safe(Integer v) {
        return v == null ? 0 : v;
    }
}
