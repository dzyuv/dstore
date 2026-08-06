package com.dzy.goodsprovider8090.controller;

import com.dzy.common.constants.Constants;
import com.dzy.common.entity.ResultJSON;
import com.dzy.goodsprovider8090.dto.CartAddRequest;
import com.dzy.goodsprovider8090.dto.CartUpdateRequest;
import com.dzy.goodsprovider8090.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /** 加入购物车（同 SKU 累加数量） */
    @PostMapping("/items")
    public ResultJSON add(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                          @Valid @RequestBody CartAddRequest request) {
        cartService.add(userId, request);
        return ResultJSON.success();
    }

    /** 按门店分组展示 */
    @GetMapping
    public ResultJSON list(@RequestHeader(Constants.HEADER_USER_ID) Long userId) {
        return ResultJSON.success(cartService.listGrouped(userId));
    }

    /** 修改数量 / 选中状态 */
    @PutMapping("/items/{id}")
    public ResultJSON update(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                             @PathVariable Long id,
                             @Valid @RequestBody CartUpdateRequest request) {
        cartService.update(userId, id, request);
        return ResultJSON.success();
    }

    @DeleteMapping("/items/{id}")
    public ResultJSON delete(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                             @PathVariable Long id) {
        cartService.delete(userId, id);
        return ResultJSON.success();
    }

    /** 批量删除 */
    @DeleteMapping("/items")
    public ResultJSON deleteBatch(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                                  @RequestBody Map<String, List<Long>> body) {
        cartService.deleteBatch(userId, body.get("ids"));
        return ResultJSON.success();
    }

    /** 全选 / 取消全选 */
    @PutMapping("/select-all")
    public ResultJSON selectAll(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                                @RequestParam boolean selected) {
        cartService.selectAll(userId, selected);
        return ResultJSON.success();
    }

    /** 按门店选中 */
    @PutMapping("/select-store")
    public ResultJSON selectStore(@RequestHeader(Constants.HEADER_USER_ID) Long userId,
                                  @RequestParam Long storeId,
                                  @RequestParam boolean selected) {
        cartService.selectByStore(userId, storeId, selected);
        return ResultJSON.success();
    }

    /** 已选中项（下单前预览） */
    @GetMapping("/selected")
    public ResultJSON selected(@RequestHeader(Constants.HEADER_USER_ID) Long userId) {
        return ResultJSON.success(cartService.listSelected(userId));
    }

    /** 下单成功后清除已选中 */
    @DeleteMapping("/selected")
    public ResultJSON clearSelected(@RequestHeader(Constants.HEADER_USER_ID) Long userId) {
        cartService.clearSelected(userId);
        return ResultJSON.success();
    }
}
