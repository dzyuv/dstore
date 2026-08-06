package com.dzy.orderconsumer.mapper;

import com.dzy.orderconsumer.entity.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO orders(order_no, user_id, store_id, merchant_id, address_id, receiver_name, receiver_phone, receiver_addr, " +
            "delivery_time, total_amount, status, expire_at) " +
            "VALUES(#{orderNo}, #{userId}, #{storeId}, #{merchantId}, #{addressId}, #{receiverName}, #{receiverPhone}, #{receiverAddr}, " +
            "#{deliveryTime}, #{totalAmount}, #{status}, #{expireAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Select("SELECT * FROM orders WHERE order_no = #{orderNo}")
    Order selectByOrderNo(String orderNo);

    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY id DESC")
    List<Order> selectByUserId(Long userId);

    @Select("SELECT * FROM orders WHERE merchant_id = #{merchantId} ORDER BY id DESC")
    List<Order> selectByMerchant(Long merchantId);

    @Update("UPDATE orders SET status = #{status}, cancel_reason = #{cancelReason} WHERE order_no = #{orderNo}")
    int updateStatus(@Param("orderNo") String orderNo, @Param("status") String status, @Param("cancelReason") String cancelReason);

    /** 查询待支付且已超时的订单（由定时任务解锁库存后取消） */
    @Select("SELECT * FROM orders WHERE status = 'PENDING_PAY' AND expire_at < NOW()")
    List<Order> selectExpiredPending();

    /** 仅当仍为待支付时取消，避免与支付回调并发冲突 */
    @Update("UPDATE orders SET status = 'CANCELLED', cancel_reason = #{reason} " +
            "WHERE order_no = #{orderNo} AND status = 'PENDING_PAY'")
    int cancelIfPendingPay(@Param("orderNo") String orderNo, @Param("reason") String reason);
}