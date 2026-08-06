package com.dzy.goodsprovider8090.service;

import com.dzy.goodsprovider8090.dto.CartAddRequest;
import com.dzy.goodsprovider8090.dto.CartUpdateRequest;
import com.dzy.goodsprovider8090.vo.CartGroupVO;

import java.util.List;

public interface CartService {

    void add(Long userId, CartAddRequest request);

    List<CartGroupVO> listGrouped(Long userId);

    void update(Long userId, Long cartItemId, CartUpdateRequest request);

    void delete(Long userId, Long cartItemId);

    void deleteBatch(Long userId, List<Long> ids);

    void selectAll(Long userId, boolean selected);

    void selectByStore(Long userId, Long storeId, boolean selected);

    List<CartGroupVO.CartItemVO> listSelected(Long userId);

    void clearSelected(Long userId);
}
