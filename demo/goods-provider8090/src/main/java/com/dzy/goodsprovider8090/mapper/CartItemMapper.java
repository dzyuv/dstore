package com.dzy.goodsprovider8090.mapper;

import com.dzy.goodsprovider8090.entity.CartItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CartItemMapper {

    @Insert("INSERT INTO cart_item(user_id, store_id, product_id, sku_id, quantity, selected) " +
            "VALUES(#{userId}, #{storeId}, #{productId}, #{skuId}, #{quantity}, #{selected}) " +
            "ON DUPLICATE KEY UPDATE quantity = quantity + #{quantity}, selected = 1, updated_at = NOW()")
    int addOrUpdate(@Param("userId") Long userId,
                    @Param("storeId") Long storeId,
                    @Param("productId") Long productId,
                    @Param("skuId") Long skuId,
                    @Param("quantity") Integer quantity,
                    @Param("selected") Boolean selected);

    @Select("SELECT * FROM cart_item WHERE user_id = #{userId} ORDER BY store_id ASC, id DESC")
    List<CartItem> listByUser(Long userId);

    @Select("SELECT * FROM cart_item WHERE id = #{id} AND user_id = #{userId}")
    CartItem selectByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    @Delete("DELETE FROM cart_item WHERE id = #{id} AND user_id = #{userId}")
    int delete(@Param("id") Long id, @Param("userId") Long userId);

    @Delete("<script>" +
            "DELETE FROM cart_item WHERE user_id = #{userId} AND id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    int deleteBatch(@Param("userId") Long userId, @Param("ids") List<Long> ids);

    @Update("UPDATE cart_item SET quantity = #{quantity}, selected = #{selected}, updated_at = NOW() " +
            "WHERE id = #{id} AND user_id = #{userId}")
    int update(@Param("id") Long id,
               @Param("userId") Long userId,
               @Param("quantity") Integer quantity,
               @Param("selected") Boolean selected);

    @Update("UPDATE cart_item SET selected = #{selected}, updated_at = NOW() WHERE user_id = #{userId}")
    int updateSelectedAll(@Param("userId") Long userId, @Param("selected") Boolean selected);

    @Update("UPDATE cart_item SET selected = #{selected}, updated_at = NOW() " +
            "WHERE user_id = #{userId} AND store_id = #{storeId}")
    int updateSelectedByStore(@Param("userId") Long userId,
                              @Param("storeId") Long storeId,
                              @Param("selected") Boolean selected);

    @Select("SELECT * FROM cart_item WHERE user_id = #{userId} AND selected = 1")
    List<CartItem> listSelected(Long userId);

    @Delete("DELETE FROM cart_item WHERE user_id = #{userId} AND selected = 1")
    int clearSelected(Long userId);

    @Delete("DELETE FROM cart_item WHERE user_id = #{userId} AND sku_id = #{skuId}")
    int deleteBySku(@Param("userId") Long userId, @Param("skuId") Long skuId);
}
