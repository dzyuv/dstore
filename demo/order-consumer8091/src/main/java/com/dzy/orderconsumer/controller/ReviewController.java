package com.dzy.orderconsumer.controller;

import com.dzy.common.constants.Constants;
import com.dzy.common.entity.ResultJSON;
import com.dzy.orderconsumer.entity.Order;
import com.dzy.orderconsumer.entity.Review;
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

    /** 订单完成后评价（一单一次，可修改） */
    @PostMapping
    public ResultJSON create(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                             @RequestBody Map<String, Object> body) {
        String orderNo = body.get("orderNo").toString();
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            return ResultJSON.error(404, "订单不存在");
        }
        if (!Constants.ORDER_COMPLETED.equals(order.getStatus())) {
            return ResultJSON.error(400, "仅已完成订单可评价");
        }

        Review review = new Review();
        review.setOrderNo(orderNo);
        review.setUserId(userId);
        review.setProductId(Long.valueOf(body.get("productId").toString()));
        review.setSkuId(body.get("skuId") == null ? 0L : Long.valueOf(body.get("skuId").toString()));
        review.setScore(Integer.parseInt(body.get("score").toString()));
        review.setContent(body.get("content") == null ? null : body.get("content").toString());
        review.setImages(body.get("images") == null ? null : body.get("images").toString());
        review.setStatus(Constants.REVIEW_VISIBLE);

        List<Review> exist = reviewMapper.selectByOrderNo(orderNo);
        boolean already = exist.stream().anyMatch(r -> r.getProductId().equals(review.getProductId()));
        if (already) {
            // 简化：已评价则视为修改（重新插入前可先删，演示直接报提示）
            return ResultJSON.error(400, "该商品已评价，如需修改请联系客服（演示版）");
        }
        reviewMapper.insert(review);
        return ResultJSON.success(review);
    }

    @GetMapping("/product/{productId}")
    public ResultJSON listByProduct(@PathVariable Long productId) {
        return ResultJSON.success(reviewMapper.selectByProduct(productId));
    }

    /** 管理员屏蔽评价 */
    @PutMapping("/{id}/hide")
    public ResultJSON hide(@RequestHeader(Constants.HEADER_USER_ID) Long adminId,
                           @PathVariable Long id) {
        reviewMapper.updateStatus(id, Constants.REVIEW_HIDDEN);
        return ResultJSON.success();
    }
}