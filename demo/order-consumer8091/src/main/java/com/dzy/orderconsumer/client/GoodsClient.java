package com.dzy.orderconsumer.client;

import com.dzy.common.entity.ResultJSON;
import com.dzy.orderconsumer.breaker.GoodsClientBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(value = "GOODS-PROVIDER8090", fallback = GoodsClientBreaker.class)
public interface GoodsClient {

    @GetMapping("/goods/sku/{skuId}")
    ResultJSON getSku(@PathVariable("skuId") Long skuId);

    @GetMapping("/goods/get/{gid}")
    ResultJSON getById(@PathVariable("gid") Long gid);

    @GetMapping("/goods/list")
    ResultJSON list(@RequestParam("page") int page, @RequestParam("size") int size);

    @PostMapping("/goods/reduceStock")
    ResultJSON reduceStock(@RequestBody Map<String, Object> param);

    @PostMapping("/goods/stock/lock")
    ResultJSON lockStock(@RequestBody Map<String, Object> body);

    @PostMapping("/goods/stock/unlock")
    ResultJSON unlockStock(@RequestBody Map<String, Object> body);

    @PostMapping("/goods/stock/deduct")
    ResultJSON deductStock(@RequestBody Map<String, Object> body);

    @PostMapping("/goods/stock/restore")
    ResultJSON restoreStock(@RequestBody Map<String, Object> body);
}
