package com.dzy.userservice.controller;

import com.dzy.common.entity.ResultJSON;
import com.dzy.userservice.entity.Address;
import com.dzy.userservice.entity.Store;
import com.dzy.userservice.mapper.StoreMapper;
import com.dzy.userservice.service.AddressService;
import com.dzy.userservice.service.MerchantRegistrationService;
import com.dzy.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 供其他微服务内部调用的用户/地址/门店查询接口。
 */
@RestController
@RequestMapping("/users/internal")
public class InternalUserController {

    @Autowired
    private AddressService addressService;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private MerchantRegistrationService merchantRegistrationService;
    @Autowired
    private UserService userService;

    @GetMapping("/{userId}")
    public ResultJSON getUser(@PathVariable Long userId) {
        return ResultJSON.success(userService.getById(userId));
    }

    @GetMapping("/address/{addressId}")
    public ResultJSON getAddress(@PathVariable Long addressId, @RequestParam Long userId) {
        Address address = addressService.getByIdAndUser(addressId, userId);
        return ResultJSON.success(address);
    }

    @GetMapping("/store/{storeId}")
    public ResultJSON getStore(@PathVariable Long storeId) {
        Store store = storeMapper.selectById(storeId);
        return ResultJSON.success(store);
    }

    @GetMapping("/merchant/by-user/{userId}")
    public ResultJSON getMerchantByUser(@PathVariable Long userId) {
        return ResultJSON.success(merchantRegistrationService.getApprovedMerchantByUserId(userId));
    }
}
