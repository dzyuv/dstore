package com.dzy.userservice.controller;

import com.dzy.common.constants.Constants;
import com.dzy.common.entity.ResultJSON;
import com.dzy.userservice.dto.StoreCreateRequest;
import com.dzy.userservice.dto.StoreUpdateRequest;
import com.dzy.userservice.entity.Store;
import com.dzy.userservice.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/merchant/stores")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @GetMapping
    public ResultJSON list(@RequestHeader(Constants.HEADER_USER_ID) Long userId) {
        List<Store> stores = storeService.listStores(userId);
        return ResultJSON.success(stores);
    }

    @GetMapping("/{storeId}")
    public ResultJSON get(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                                 @PathVariable Long storeId) {
        Store store = storeService.getStoreDetail(userId, storeId);
        return ResultJSON.success(store);
    }

    @PostMapping
    public ResultJSON add(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                                 @Valid @RequestBody StoreCreateRequest request) {
        Store store = storeService.addStore(userId, request);
        return ResultJSON.success(store);
    }

    @PutMapping
    public ResultJSON update(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                                    @Valid @RequestBody StoreUpdateRequest request) {
        Store store = storeService.updateStore(userId, request);
        return ResultJSON.success(store);
    }

    @DeleteMapping("/{storeId}")
    public ResultJSON delete(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                                   @PathVariable Long storeId) {
        storeService.deleteStore(userId, storeId);
        return ResultJSON.success();
    }
}