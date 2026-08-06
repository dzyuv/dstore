package com.dzy.orderconsumer.controller;

import com.dzy.common.constants.Constants;
import com.dzy.common.entity.ResultJSON;
import com.dzy.orderconsumer.entity.Order;
import com.dzy.orderconsumer.entity.OrderItem;
import com.dzy.orderconsumer.entity.Review;
import com.dzy.orderconsumer.mapper.OrderItemMapper;
import com.dzy.orderconsumer.mapper.OrderMapper;
import com.dzy.orderconsumer.mapper.ReviewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;

    /**
     * 订单完成后评价：同一订单同一商品限评价一次，可修改。
     * body: orderNo, productId, skuId?, score(1-5), content?, images?
     */
    @PostMapping
    public ResultJSON create(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                             @RequestBody Map<String, Object> body) {
        if (body.get("orderNo") == null || body.get("productId") == null || body.get("score") == null) {
            return ResultJSON.error(400, "orderNo、productId、score 不能为空");
        }
        String orderNo = body.get("orderNo").toString();
        Long productId = Long.valueOf(body.get("productId").toString());
        int score = Integer.parseInt(body.get("score").toString());
        if (score < 1 || score > 5) {
            return ResultJSON.error(400, "评分须在 1-5 分之间");
        }

        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            return ResultJSON.error(404, "订单不存在");
        }
        if (!Constants.ORDER_COMPLETED.equals(order.getStatus())) {
            return ResultJSON.error(400, "仅已完成订单可评价");
        }

        // 校验商品属于该订单
        List<OrderItem> items = orderItemMapper.selectByOrderNo(orderNo);
        boolean inOrder = items.stream().anyMatch(i -> productId.equals(i.getProductId()));
        if (!inOrder) {
            return ResultJSON.error(400, "该商品不属于此订单");
        }

        Long skuId = body.get("skuId") == null ? 0L : Long.valueOf(body.get("skuId").toString());
        String content = body.get("content") == null ? null : body.get("content").toString();
        String images = body.get("images") == null ? null : body.get("images").toString();

        Review existing = reviewMapper.selectByOrderAndProduct(orderNo, productId);
        if (existing != null) {
            // 需求：一单一次可修改
            existing.setSkuId(skuId);
            existing.setScore(score);
            existing.setContent(content);
            existing.setImages(images);
            existing.setStatus(Constants.REVIEW_VISIBLE);
            reviewMapper.update(existing);
            return ResultJSON.success(existing);
        }

        Review review = new Review();
        review.setOrderNo(orderNo);
        review.setUserId(userId);
        review.setProductId(productId);
        review.setSkuId(skuId);
        review.setScore(score);
        review.setContent(content);
        review.setImages(images);
        review.setStatus(Constants.REVIEW_VISIBLE);
        reviewMapper.insert(review);
        return ResultJSON.success(review);
    }

    @GetMapping("/product/{productId}")
    public ResultJSON listByProduct(@PathVariable Long productId) {
        return ResultJSON.success(reviewMapper.selectByProduct(productId));
    }

    @GetMapping("/order/{orderNo}")
    public ResultJSON listByOrder(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                                  @PathVariable String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            return ResultJSON.error(404, "订单不存在");
        }
        return ResultJSON.success(reviewMapper.selectByOrderNo(orderNo));
    }

    /** 管理员屏蔽评价 */
    @PutMapping("/{id}/hide")
    public ResultJSON hide(@RequestHeader(value = Constants.HEADER_ROLE, required = false) String role,
                           @PathVariable Long id) {
        if (role != null && !Constants.ROLE_ADMIN.equals(role)) {
            return ResultJSON.error(403, "仅管理员可屏蔽评价");
        }
        reviewMapper.updateStatus(id, Constants.REVIEW_HIDDEN);
        return ResultJSON.success();
    }
}
