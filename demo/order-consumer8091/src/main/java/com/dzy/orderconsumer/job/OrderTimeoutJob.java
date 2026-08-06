package com.dzy.orderconsumer.job;

import com.dzy.orderconsumer.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 待支付超时自动取消（每分钟扫描一次）
 */
@Component
public class OrderTimeoutJob {

    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(fixedDelay = 60_000)
    public void cancelExpiredOrders() {
        try {
            orderMapper.cancelExpired();
        } catch (Exception ignored) {
        }
    }
}