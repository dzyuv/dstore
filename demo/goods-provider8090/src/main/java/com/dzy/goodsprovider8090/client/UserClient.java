package com.dzy.goodsprovider8090.client;

import com.dzy.common.entity.ResultJSON;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 调用用户服务，校验商家与门店归属。
 */
@FeignClient(value = "USER-SERVICE", contextId = "goodsUserClient")
public interface UserClient {

    @GetMapping("/users/internal/merchant/by-user/{userId}")
    ResultJSON getMerchantByUser(@PathVariable("userId") Long userId);

    @GetMapping("/users/internal/store/{storeId}")
    ResultJSON getStore(@PathVariable("storeId") Long storeId);
}
