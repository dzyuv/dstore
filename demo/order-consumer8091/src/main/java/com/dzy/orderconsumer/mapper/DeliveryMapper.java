package com.dzy.orderconsumer.mapper;

import com.dzy.orderconsumer.entity.Delivery;
import org.apache.ibatis.annotations.*;

@Mapper
public interface DeliveryMapper {

    @Insert("INSERT INTO delivery(order_no, store_id, carrier, tracking_no, status, remark) " +
            "VALUES(#{orderNo}, #{storeId}, #{carrier}, #{trackingNo}, #{status}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Delivery delivery);

    @Select("SELECT * FROM delivery WHERE order_no = #{orderNo}")
    Delivery selectByOrderNo(String orderNo);

    @Update("UPDATE delivery SET carrier = #{carrier}, tracking_no = #{trackingNo}, status = #{status}, remark = #{remark} WHERE order_no = #{orderNo}")
    int update(Delivery delivery);
}