package com.dzy.userservice.mapper;

import com.dzy.userservice.entity.Store;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StoreMapper {

    @Insert("INSERT INTO store(merchant_id, store_name, logo, address, phone, business_hours, status) " +
            "VALUES(#{merchantId}, #{storeName}, #{logo}, #{address}, #{phone}, #{businessHours}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Store store);

    @Update("UPDATE store SET store_name=#{storeName}, logo=#{logo}, address=#{address}, phone=#{phone}, " +
            "business_hours=#{businessHours}, status=#{status} WHERE id=#{id} AND merchant_id=#{merchantId}")
    int update(Store store);

    @Delete("DELETE FROM store WHERE id=#{id} AND merchant_id=#{merchantId}")
    int deleteById(@Param("id") Long id, @Param("merchantId") Long merchantId);

    @Select("SELECT * FROM store WHERE merchant_id = #{merchantId} ORDER BY id DESC")
    List<Store> selectByMerchantId(Long merchantId);

    @Select("SELECT * FROM store WHERE id = #{id} AND merchant_id = #{merchantId}")
    Store selectByIdAndMerchant(@Param("id") Long id, @Param("merchantId") Long merchantId);

    @Select("SELECT * FROM store WHERE id = #{id}")
    Store selectById(Long id);

    @Update("UPDATE store SET status = #{status} WHERE merchant_id = #{merchantId}")
    int updateStatusByMerchant(@Param("merchantId") Long merchantId, @Param("status") Integer status);
}
