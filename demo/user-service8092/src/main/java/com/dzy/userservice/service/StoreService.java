package com.dzy.userservice.service;

import com.dzy.userservice.dto.StoreCreateRequest;
import com.dzy.userservice.dto.StoreUpdateRequest;
import com.dzy.userservice.entity.Store;

import java.util.List;

public interface StoreService {
    Store addStore(Long userId, StoreCreateRequest request);
    Store updateStore(Long userId, StoreUpdateRequest request);
    void deleteStore(Long userId, Long storeId);
    List<Store> listStores(Long userId);
    Store getStoreDetail(Long userId, Long storeId);
}