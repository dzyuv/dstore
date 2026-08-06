package com.dzy.orderconsumer.mapper;

import com.dzy.orderconsumer.entity.OrderItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    @Insert("INSERT INTO order_item(order_id, order_no, product_id, sku_id, product_name, sku_name, price, quantity, amount) " +
            "VALUES(#{orderId}, #{orderNo}, #{productId}, #{skuId}, #{productName}, #{skuName}, #{price}, #{quantity}, #{amount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderItem item);

    @Select("SELECT * FROM order_item WHERE order_no = #{orderNo}")
    List<OrderItem> selectByOrderNo(String orderNo);
}