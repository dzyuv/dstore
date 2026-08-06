package com.dzy.userservice.service.impl;

import com.dzy.common.entity.Merchant;
import com.dzy.common.exception.BusinessException;
import com.dzy.userservice.client.GoodsAdminClient;
import com.dzy.userservice.dto.StoreCreateRequest;
import com.dzy.userservice.dto.StoreUpdateRequest;
import com.dzy.userservice.entity.Store;
import com.dzy.userservice.mapper.StoreMapper;
import com.dzy.userservice.service.MerchantRegistrationService;
import com.dzy.userservice.service.StoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StoreServiceImpl implements StoreService {

    private static final Logger log = LoggerFactory.getLogger(StoreServiceImpl.class);

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private MerchantRegistrationService merchantRegistrationService;

    @Autowired
    private GoodsAdminClient goodsAdminClient;

    @Override
    @Transactional
    public Store addStore(Long userId, StoreCreateRequest request) {
        Merchant merchant = merchantRegistrationService.getApprovedMerchantByUserId(userId);
        Store store = new Store();
        store.setMerchantId(merchant.getId());
        store.setStoreName(request.getStoreName());
        store.setLogo(request.getLogo());
        store.setAddress(request.getAddress());
        store.setPhone(request.getPhone());
        store.setBusinessHours(request.getBusinessHours());
        store.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        storeMapper.insert(store);
        return store;
    }

    @Override
    @Transactional
    public Store updateStore(Long userId, StoreUpdateRequest request) {
        Merchant merchant = merchantRegistrationService.getApprovedMerchantByUserId(userId);
        Store store = storeMapper.selectByIdAndMerchant(request.getStoreId(), merchant.getId());
        if (store == null) {
            throw new BusinessException("门店不存在或不属于该商家");
        }
        if (request.getStoreName() != null) store.setStoreName(request.getStoreName());
        if (request.getLogo() != null) store.setLogo(request.getLogo());
        if (request.getAddress() != null) store.setAddress(request.getAddress());
        if (request.getPhone() != null) store.setPhone(request.getPhone());
        if (request.getBusinessHours() != null) store.setBusinessHours(request.getBusinessHours());
        if (request.getStatus() != null) store.setStatus(request.getStatus());
        storeMapper.update(store);

        // 门店休息时自动下架全部在售商品
        if (request.getStatus() != null && request.getStatus() == 0) {
            try {
                goodsAdminClient.offlineByStore(request.getStoreId());
                log.info("门店 {} 已休息，已触发商品下架", request.getStoreId());
            } catch (Exception e) {
                log.error("门店 {} 下架商品失败", request.getStoreId(), e);
            }
        }

        return store;
    }

    @Override
    @Transactional
    public void deleteStore(Long userId, Long storeId) {
        Merchant merchant = merchantRegistrationService.getApprovedMerchantByUserId(userId);
        int rows = storeMapper.deleteById(storeId, merchant.getId());
        if (rows == 0) {
            throw new BusinessException("门店不存在或不属于该商家");
        }
    }

    @Override
    public List<Store> listStores(Long userId) {
        Merchant merchant = merchantRegistrationService.getApprovedMerchantByUserId(userId);
        return storeMapper.selectByMerchantId(merchant.getId());
    }

    @Override
    public Store getStoreDetail(Long userId, Long storeId) {
        Merchant merchant = merchantRegistrationService.getApprovedMerchantByUserId(userId);
        Store store = storeMapper.selectByIdAndMerchant(storeId, merchant.getId());
        if (store == null) {
            throw new BusinessException("门店不存在或不属于该商家");
        }
        return store;
    }
}