package com.dzy.orderconsumer.client;

import com.dzy.common.entity.ResultJSON;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "USER-SERVICE", contextId = "orderUserClient")
public interface UserClient {

    @GetMapping("/users/internal/merchant/by-user/{userId}")
    ResultJSON getMerchantByUser(@PathVariable("userId") Long userId);
}