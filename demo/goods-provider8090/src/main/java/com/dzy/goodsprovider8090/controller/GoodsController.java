package com.dzy.goodsprovider8090.controller;


import com.dzy.common.entity.Goods;
import com.dzy.common.entity.ResultJSON;
import com.dzy.goodsprovider8090.mapper.GoodsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsMapper goodsMapper;

    @GetMapping("/get/{gid}")
    public ResultJSON getGoodById(@PathVariable long gid) {
        Goods goods = goodsMapper.selectById(gid);
        if (goods != null) {
            return ResultJSON.success(goods);
        }
        return ResultJSON.error(404, "商品不存在");
    }

    @GetMapping("/list")
    public ResultJSON getGoods(@RequestParam int page, @RequestParam int size) {
        List<Goods> all = goodsMapper.selectAll();
        int start = (page - 1) * size;
        int end = Math.min(start + size, all.size());

        Map<String, Object> map = new HashMap<>();
        map.put("list", all.subList(start, end));
        map.put("total", all.size());
        map.put("page", page);
        map.put("size", size);
        return ResultJSON.success(map);
    }


}