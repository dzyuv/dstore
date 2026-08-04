package com.dzy.orderconsumer.controller;

import com.dzy.orderconsumer.client.GoodsClient;
import com.dzy.orderconsumer.entity.ResultJSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private GoodsClient goodsClient;


    @GetMapping("/getGoods/{gid}")
    public ResultJSON getGoods(@PathVariable Long gid) {
        return goodsClient.getById(gid);
    }

    @GetMapping("/goodsList")
    public ResultJSON goodsList(@RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "5") int size) {
        return goodsClient.list(page, size);
    }

    @PostMapping("/create")
    public ResultJSON createOrder(@RequestBody Map<String, Object> param) {
        return goodsClient.reduceStock(param);
    }

}