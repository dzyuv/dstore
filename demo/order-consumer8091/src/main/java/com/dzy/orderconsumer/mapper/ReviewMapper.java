package com.dzy.orderconsumer.mapper;

import com.dzy.orderconsumer.entity.Review;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReviewMapper {

    @Insert("INSERT INTO review(order_no, user_id, product_id, sku_id, score, content, images, status) " +
            "VALUES(#{orderNo}, #{userId}, #{productId}, #{skuId}, #{score}, #{content}, #{images}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Review review);

    @Select("SELECT * FROM review WHERE order_no = #{orderNo}")
    List<Review> selectByOrderNo(String orderNo);

    @Select("SELECT * FROM review WHERE product_id = #{productId} AND status = 'VISIBLE' ORDER BY id DESC")
    List<Review> selectByProduct(Long productId);

    @Update("UPDATE review SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}