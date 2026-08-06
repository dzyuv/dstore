package com.dzy.userservice.client;

import com.dzy.common.entity.ResultJSON;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(value = "GOODS-PROVIDER8090", contextId = "goodsAdminClient")
public interface GoodsAdminClient {

    @PostMapping("/goods/internal/offline-by-merchant")
    ResultJSON offlineByMerchant(@RequestParam("merchantId") Long merchantId);
}
