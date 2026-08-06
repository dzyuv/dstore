package com.dzy.orderconsumer.mapper;

import com.dzy.orderconsumer.entity.Payment;
import org.apache.ibatis.annotations.*;

@Mapper
public interface PaymentMapper {

    @Insert("INSERT INTO payment(payment_no, order_no, user_id, amount, channel, status, pay_url) " +
            "VALUES(#{paymentNo}, #{orderNo}, #{userId}, #{amount}, #{channel}, #{status}, #{payUrl})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Payment payment);

    @Select("SELECT * FROM payment WHERE payment_no = #{paymentNo}")
    Payment selectByPaymentNo(String paymentNo);

    @Update("UPDATE payment SET status = #{status}, third_trade_no = #{thirdTradeNo}, paid_at = NOW() WHERE payment_no = #{paymentNo}")
    int updateStatus(@Param("paymentNo") String paymentNo, @Param("status") String status, @Param("thirdTradeNo") String thirdTradeNo);

    @Select("SELECT * FROM payment WHERE order_no = #{orderNo} ORDER BY id DESC LIMIT 1")
    Payment selectLatestByOrderNo(String orderNo);

    @Update("UPDATE payment SET status = #{status}, updated_at = NOW() WHERE order_no = #{orderNo} AND status = 'SUCCESS'")
    int markRefundedByOrderNo(@Param("orderNo") String orderNo, @Param("status") String status);
}