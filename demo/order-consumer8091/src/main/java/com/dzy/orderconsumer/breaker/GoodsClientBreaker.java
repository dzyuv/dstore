package com.dzy.orderconsumer.breaker;

import com.dzy.common.entity.ResultJSON;
import com.dzy.orderconsumer.client.GoodsClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GoodsClientBreaker implements GoodsClient {

    @Override
    public ResultJSON getSku(Long skuId) {
        return ResultJSON.error(503, "商品服务不可用");
    }

    @Override
    public ResultJSON getById(Long gid) {
        return ResultJSON.error(503, "商品服务不可用");
    }

    @Override
    public ResultJSON list(int page, int size) {
        return ResultJSON.error(503, "商品服务不可用");
    }

    @Override
    public ResultJSON reduceStock(Map<String, Object> param) {
        return ResultJSON.error(503, "库存服务不可用");
    }

    @Override
    public ResultJSON lockStock(Map<String, Object> body) {
        return ResultJSON.error(503, "库存锁定失败，商品服务不可用");
    }

    @Override
    public ResultJSON unlockStock(Map<String, Object> body) {
        return ResultJSON.error(503, "库存解锁失败");
    }

    @Override
    public ResultJSON deductStock(Map<String, Object> body) {
        return ResultJSON.error(503, "库存扣减失败");
    }

    @Override
    public ResultJSON restoreStock(Map<String, Object> body) {
        return ResultJSON.error(503, "库存恢复失败");
    }
}
