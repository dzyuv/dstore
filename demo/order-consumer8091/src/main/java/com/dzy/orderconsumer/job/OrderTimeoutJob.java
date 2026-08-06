package com.dzy.orderconsumer.job;

import com.dzy.common.entity.ResultJSON;
import com.dzy.orderconsumer.client.GoodsClient;
import com.dzy.orderconsumer.entity.Order;
import com.dzy.orderconsumer.entity.OrderItem;
import com.dzy.orderconsumer.mapper.OrderItemMapper;
import com.dzy.orderconsumer.mapper.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 待支付超时自动取消：先原子取消订单，再释放锁定库存（每分钟扫描一次）。
 * 对应需求：待支付持续 30 分钟未付款则自动取消，并释放锁定库存。
 */
@Component
public class OrderTimeoutJob {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutJob.class);

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private GoodsClient goodsClient;

    @Scheduled(fixedDelay = 60_000)
    public void cancelExpiredOrders() {
        List<Order> expired;
        try {
            expired = orderMapper.selectExpiredPending();
        } catch (Exception e) {
            log.warn("查询超时订单失败: {}", e.getMessage());
            return;
        }
        if (expired == null || expired.isEmpty()) {
            return;
        }

        for (Order order : expired) {
            try {
                cancelOne(order);
            } catch (Exception e) {
                log.warn("超时取消订单失败 orderNo={}: {}", order.getOrderNo(), e.getMessage());
            }
        }
    }

    private void cancelOne(Order order) {
        String orderNo = order.getOrderNo();

        // 先尝试释放锁定库存，成功后再取消订单
        List<OrderItem> items = orderItemMapper.selectByOrderNo(orderNo);
        if (items != null && !items.isEmpty()) {
            List<Map<String, Object>> stockItems = new ArrayList<>();
            for (OrderItem oi : items) {
                Map<String, Object> m = new HashMap<>();
                m.put("skuId", oi.getSkuId());
                m.put("quantity", oi.getQuantity());
                stockItems.add(m);
            }

            Map<String, Object> stockBody = new HashMap<>();
            stockBody.put("bizNo", orderNo);
            stockBody.put("items", stockItems);

            ResultJSON unlock = goodsClient.unlockStock(stockBody);
            if (unlock == null || !unlock.isSuccess()) {
                log.error("订单 {} 释放锁定库存失败，暂不取消订单", orderNo);
                return;
            }
        }

        int rows = orderMapper.cancelIfPendingPay(orderNo, "超时未支付自动取消");
        if (rows == 0) {
            log.info("订单 {} 状态已变更，跳过超时取消", orderNo);
            return;
        }

        log.info("订单超时已取消并释放库存: {}", orderNo);
    }
}
