package com.dzy.orderconsumer.controller;

import com.dzy.common.constants.Constants;
import com.dzy.common.entity.ResultJSON;
import com.dzy.orderconsumer.entity.Delivery;
import com.dzy.orderconsumer.entity.Order;
import com.dzy.orderconsumer.mapper.DeliveryMapper;
import com.dzy.orderconsumer.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/deliveries")
public class DeliveryController {

    @Autowired
    private DeliveryMapper deliveryMapper;
    @Autowired
    private OrderMapper orderMapper;

    /** 消费者查看物流 */
    @GetMapping("/{orderNo}")
    public ResultJSON get(@PathVariable String orderNo) {
        return ResultJSON.success(deliveryMapper.selectByOrderNo(orderNo));
    }

    /**
     * 商家更新配送信息/状态
     * body: carrier, trackingNo, status, remark
     * status: WAIT_PICK → PICKING → PICKED → DELIVERING → DELIVERED
     */
    @PutMapping("/{orderNo}")
    public ResultJSON update(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                             @PathVariable String orderNo,
                             @RequestBody Map<String, String> body) {
        Delivery delivery = deliveryMapper.selectByOrderNo(orderNo);
        if (delivery == null) {
            return ResultJSON.error(404, "配送单不存在");
        }
        if (body.get("carrier") != null) delivery.setCarrier(body.get("carrier"));
        if (body.get("trackingNo") != null) delivery.setTrackingNo(body.get("trackingNo"));
        if (body.get("status") != null) delivery.setStatus(body.get("status"));
        if (body.get("remark") != null) delivery.setRemark(body.get("remark"));
        deliveryMapper.update(delivery);

        // 同步订单状态
        String st = delivery.getStatus();
        if (Constants.DELIVERY_PICKING.equals(st)) {
            orderMapper.updateStatus(orderNo, Constants.ORDER_PICKING, null);
        } else if (Constants.DELIVERY_PICKED.equals(st)) {
            orderMapper.updateStatus(orderNo, Constants.ORDER_PICKED, null);
        } else if (Constants.DELIVERY_DELIVERING.equals(st)) {
            orderMapper.updateStatus(orderNo, Constants.ORDER_DELIVERING, null);
        } else if (Constants.DELIVERY_DELIVERED.equals(st)) {
            orderMapper.updateStatus(orderNo, Constants.ORDER_DELIVERED, null);
        }
        return ResultJSON.success(delivery);
    }
}