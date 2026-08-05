package com.dzy.orderconsumer.controller;

import com.dzy.common.constants.Constants;
import com.dzy.common.entity.ResultJSON;
import com.dzy.common.exception.BusinessException;
import com.dzy.orderconsumer.client.GoodsClient;
import com.dzy.orderconsumer.entity.Delivery;
import com.dzy.orderconsumer.entity.Order;
import com.dzy.orderconsumer.entity.OrderItem;
import com.dzy.orderconsumer.mapper.DeliveryMapper;
import com.dzy.orderconsumer.mapper.OrderItemMapper;
import com.dzy.orderconsumer.mapper.OrderMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private DeliveryMapper deliveryMapper;
    @Autowired
    private GoodsClient goodsClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 下单：校验库存并锁定 → 生成订单 → 待支付
     * body: { storeId, merchantId, addressId, receiverName, receiverPhone, receiverAddr,
     *         deliveryTime, items:[{skuId, productId, productName, skuName, price, quantity}] }
     */
    @PostMapping
    public ResultJSON create(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                             @RequestBody Map<String, Object> body) {
        Long storeId = Long.valueOf(body.get("storeId").toString());
        Long merchantId = body.get("merchantId") == null ? 0L : Long.valueOf(body.get("merchantId").toString());
        Long addressId = Long.valueOf(body.get("addressId").toString());
        String deliveryTime = body.get("deliveryTime") == null ? null : body.get("deliveryTime").toString();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        if (items == null || items.isEmpty()) {
            throw new BusinessException("订单商品不能为空");
        }

        String orderNo = "O" + System.currentTimeMillis()
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // 组装库存锁定请求，并计算金额 / 填充 SKU 信息
        List<Map<String, Object>> lockItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (Map<String, Object> it : items) {
            Long skuId = Long.valueOf(it.get("skuId").toString());
            int qty = Integer.parseInt(it.get("quantity").toString());
            if (qty < 1) {
                throw new BusinessException("购买数量无效");
            }

            ResultJSON skuResp = goodsClient.getSku(skuId);
            if (skuResp == null || !skuResp.isSuccess() || skuResp.getData() == null) {
                throw new BusinessException("商品信息获取失败: " + (skuResp == null ? "服务不可用" : skuResp.getMsg()));
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.convertValue(skuResp.getData(), Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> sku = objectMapper.convertValue(data.get("sku"), Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> product = data.get("product") == null ? null
                    : objectMapper.convertValue(data.get("product"), Map.class);

            if (product != null && !Constants.PRODUCT_ON_SALE.equals(String.valueOf(product.get("status")))) {
                throw new BusinessException("商品已下架: " + product.get("name"));
            }

            BigDecimal price = new BigDecimal(sku.get("price").toString());
            BigDecimal amount = price.multiply(BigDecimal.valueOf(qty));
            total = total.add(amount);

            OrderItem oi = new OrderItem();
            oi.setOrderNo(orderNo);
            oi.setProductId(product == null ? 0L : Long.valueOf(product.get("id").toString()));
            oi.setSkuId(skuId);
            oi.setProductName(product == null ? "商品" : String.valueOf(product.get("name")));
            oi.setSkuName(String.valueOf(sku.get("skuName")));
            oi.setPrice(price);
            oi.setQuantity(qty);
            oi.setAmount(amount);
            orderItems.add(oi);

            Map<String, Object> lockItem = new HashMap<>();
            lockItem.put("skuId", skuId);
            lockItem.put("quantity", qty);
            lockItems.add(lockItem);
        }

        // 锁定库存
        Map<String, Object> lockBody = new HashMap<>();
        lockBody.put("bizNo", orderNo);
        lockBody.put("items", lockItems);
        ResultJSON lockResp = goodsClient.lockStock(lockBody);
        if (lockResp == null || !lockResp.isSuccess()) {
            throw new BusinessException(lockResp == null ? "库存锁定失败" : lockResp.getMsg());
        }

        try {
            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setStoreId(storeId);
            order.setMerchantId(merchantId);
            order.setAddressId(addressId);
            order.setReceiverName(body.getOrDefault("receiverName", "收货人").toString());
            order.setReceiverPhone(body.getOrDefault("receiverPhone", "").toString());
            order.setReceiverAddr(body.getOrDefault("receiverAddr", "").toString());
            order.setDeliveryTime(deliveryTime);
            order.setTotalAmount(total);
            order.setStatus(Constants.ORDER_PENDING_PAY);
            order.setExpireAt(LocalDateTime.now().plusMinutes(30));
            orderMapper.insert(order);

            for (OrderItem oi : orderItems) {
                oi.setOrderId(order.getId());
                orderItemMapper.insert(oi);
            }

            Delivery delivery = new Delivery();
            delivery.setOrderNo(orderNo);
            delivery.setStoreId(storeId);
            delivery.setStatus(Constants.DELIVERY_WAIT_PICK);
            deliveryMapper.insert(delivery);

            Map<String, Object> result = new HashMap<>();
            result.put("orderNo", orderNo);
            result.put("totalAmount", total);
            result.put("status", Constants.ORDER_PENDING_PAY);
            result.put("expireAt", order.getExpireAt());
            return ResultJSON.success(result);
        } catch (Exception e) {
            // 下单失败回滚锁定
            try {
                goodsClient.unlockStock(lockBody);
            } catch (Exception ignored) {
            }
            throw e instanceof BusinessException ? (BusinessException) e
                    : new BusinessException("下单失败: " + e.getMessage());
        }
    }

    @GetMapping
    public ResultJSON list(@RequestHeader(Constants.HEADER_USER_ID) Long userId) {
        return ResultJSON.success(orderMapper.selectByUser(userId));
    }

    @GetMapping("/{orderNo}")
    public ResultJSON detail(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                             @PathVariable String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            return ResultJSON.error(404, "订单不存在");
        }
        List<OrderItem> items = orderItemMapper.selectByOrderNo(orderNo);
        Delivery delivery = deliveryMapper.selectByOrderNo(orderNo);
        return ResultJSON.success(Map.of("order", order, "items", items, "delivery", delivery));
    }

    /** 拣货完成前可取消；待支付取消释放锁定库存 */
    @PostMapping("/{orderNo}/cancel")
    public ResultJSON cancel(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                             @PathVariable String orderNo,
                             @RequestParam(required = false) String reason) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            return ResultJSON.error(404, "订单不存在");
        }
        String st = order.getStatus();
        if (!Constants.ORDER_PENDING_PAY.equals(st)
                && !Constants.ORDER_PAID.equals(st)
                && !Constants.ORDER_PICKING.equals(st)) {
            return ResultJSON.error(400, "拣货完成后不可取消，请申请退款");
        }

        List<OrderItem> items = orderItemMapper.selectByOrderNo(orderNo);
        List<Map<String, Object>> stockItems = new ArrayList<>();
        for (OrderItem oi : items) {
            Map<String, Object> m = new HashMap<>();
            m.put("skuId", oi.getSkuId());
            m.put("quantity", oi.getQuantity());
            stockItems.add(m);
        }
        Map<String, Object> stockBody = Map.of("bizNo", orderNo, "items", stockItems);

        if (Constants.ORDER_PENDING_PAY.equals(st)) {
            goodsClient.unlockStock(stockBody);
        } else {
            // 已支付未完成：恢复物理库存（演示简化）
            goodsClient.restoreStock(stockBody);
        }

        orderMapper.updateStatus(orderNo, Constants.ORDER_CANCELLED,
                reason == null ? "用户取消" : reason);
        return ResultJSON.success();
    }

    @PostMapping("/{orderNo}/confirm")
    public ResultJSON confirm(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                              @PathVariable String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            return ResultJSON.error(404, "订单不存在");
        }
        if (!Constants.ORDER_DELIVERED.equals(order.getStatus())) {
            return ResultJSON.error(400, "仅已送达订单可确认收货");
        }
        orderMapper.updateStatus(orderNo, Constants.ORDER_COMPLETED, null);
        return ResultJSON.success();
    }
}
