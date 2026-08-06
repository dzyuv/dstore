package com.dzy.orderconsumer.controller;

import com.dzy.common.constants.Constants;
import com.dzy.common.entity.ResultJSON;
import com.dzy.common.exception.BusinessException;
import com.dzy.orderconsumer.client.GoodsClient;
import com.dzy.orderconsumer.entity.Order;
import com.dzy.orderconsumer.entity.OrderItem;
import com.dzy.orderconsumer.entity.Payment;
import com.dzy.orderconsumer.mapper.OrderItemMapper;
import com.dzy.orderconsumer.mapper.OrderMapper;
import com.dzy.orderconsumer.mapper.PaymentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentMapper paymentMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private GoodsClient goodsClient;

    @PostMapping("/create")
    public ResultJSON create(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                             @RequestBody Map<String, Object> body) {
        String orderNo = body.get("orderNo").toString();
        String channel = body.getOrDefault("channel", Constants.CHANNEL_ALIPAY).toString();

        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            return ResultJSON.error(404, "订单不存在");
        }
        if (!Constants.ORDER_PENDING_PAY.equals(order.getStatus())) {
            return ResultJSON.error(400, "订单状态不允许支付");
        }

        String paymentNo = "P" + System.currentTimeMillis()
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        Payment payment = new Payment();
        payment.setPaymentNo(paymentNo);
        payment.setOrderNo(orderNo);
        payment.setUserId(userId);
        payment.setAmount(order.getTotalAmount());
        payment.setChannel(channel);
        payment.setStatus(Constants.PAY_PENDING);
        payment.setPayUrl("http://localhost/payments/callback/" + paymentNo + "?success=true");
        paymentMapper.insert(payment);

        return ResultJSON.success(Map.of(
                "paymentNo", paymentNo,
                "payUrl", payment.getPayUrl(),
                "amount", payment.getAmount(),
                "channel", channel
        ));
    }

    /** 模拟支付宝/微信回调：成功后扣减库存并更新订单 */
    @GetMapping("/callback/{paymentNo}")
    public ResultJSON callback(@PathVariable String paymentNo,
                               @RequestParam(defaultValue = "true") boolean success) {
        Payment payment = paymentMapper.selectByPaymentNo(paymentNo);
        if (payment == null) {
            return ResultJSON.error(404, "支付单不存在");
        }
        if (Constants.PAY_SUCCESS.equals(payment.getStatus())) {
            return ResultJSON.success("已支付");
        }
        if (!success) {
            paymentMapper.updateStatus(paymentNo, Constants.PAY_FAILED, null);
            return ResultJSON.error(400, "支付失败");
        }

        List<OrderItem> items = orderItemMapper.selectByOrderNo(payment.getOrderNo());
        List<Map<String, Object>> stockItems = new ArrayList<>();
        for (OrderItem oi : items) {
            Map<String, Object> m = new HashMap<>();
            m.put("skuId", oi.getSkuId());
            m.put("quantity", oi.getQuantity());
            stockItems.add(m);
        }
        ResultJSON deduct = goodsClient.deductStock(Map.of("bizNo", payment.getOrderNo(), "items", stockItems));
        if (deduct == null || !deduct.isSuccess()) {
            throw new BusinessException(deduct == null ? "扣库存失败" : deduct.getMsg());
        }

        paymentMapper.updateStatus(paymentNo, Constants.PAY_SUCCESS, "MOCK-" + System.currentTimeMillis());
        orderMapper.updateStatus(payment.getOrderNo(), Constants.ORDER_PAID, null);
        return ResultJSON.success(Map.of("orderNo", payment.getOrderNo(), "status", "PAID"));
    }

    @PostMapping("/refund")
    public ResultJSON refund(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                             @RequestBody Map<String, String> body) {
        String orderNo = body.get("orderNo");
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ResultJSON.error(404, "订单不存在");
        }
        if (Constants.ORDER_PENDING_PAY.equals(order.getStatus())
                || Constants.ORDER_CANCELLED.equals(order.getStatus())
                || Constants.ORDER_REFUNDED.equals(order.getStatus())) {
            return ResultJSON.error(400, "当前状态不可退款");
        }

        List<OrderItem> items = orderItemMapper.selectByOrderNo(orderNo);
        List<Map<String, Object>> stockItems = new ArrayList<>();
        for (OrderItem oi : items) {
            Map<String, Object> m = new HashMap<>();
            m.put("skuId", oi.getSkuId());
            m.put("quantity", oi.getQuantity());
            stockItems.add(m);
        }
        ResultJSON restore = goodsClient.restoreStock(Map.of("bizNo", orderNo, "items", stockItems));
        if (restore == null || !restore.isSuccess()) {
            throw new BusinessException(restore == null ? "恢复库存失败" : restore.getMsg());
        }
        paymentMapper.markRefundedByOrderNo(orderNo, Constants.PAY_REFUNDED);
        orderMapper.updateStatus(orderNo, Constants.ORDER_REFUNDED, "商家退款");
        return ResultJSON.success();
    }
}
