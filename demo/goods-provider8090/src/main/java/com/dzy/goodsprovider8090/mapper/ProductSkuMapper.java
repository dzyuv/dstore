package com.dzy.goodsprovider8090.mapper;

import com.dzy.goodsprovider8090.entity.ProductSku;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductSkuMapper {

    @Insert("INSERT INTO product_sku(product_id, sku_name, price, image, barcode, physical_stock, locked_stock, status) " +
            "VALUES(#{productId}, #{skuName}, #{price}, #{image}, #{barcode}, #{physicalStock}, #{lockedStock}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProductSku sku);

    @Update("UPDATE product_sku SET sku_name=#{skuName}, price=#{price}, image=#{image}, barcode=#{barcode}, " +
            "status=#{status}, updated_at=NOW() WHERE id=#{id} AND product_id=#{productId}")
    int update(ProductSku sku);

    @Select("SELECT * FROM product_sku WHERE product_id = #{productId} ORDER BY id ASC")
    List<ProductSku> selectByProduct(Long productId);

    @Select("SELECT * FROM product_sku WHERE id = #{id}")
    ProductSku selectById(Long id);

    @Select("SELECT * FROM product_sku WHERE id = #{id} FOR UPDATE")
    ProductSku selectByIdForUpdate(Long id);

    @Update("UPDATE product_sku SET locked_stock = locked_stock + #{qty} " +
            "WHERE id = #{id} AND status='ON' AND (physical_stock - locked_stock) >= #{qty}")
    int lockStock(@Param("id") Long id, @Param("qty") Integer qty);

    @Update("UPDATE product_sku SET locked_stock = locked_stock - #{qty} " +
            "WHERE id = #{id} AND locked_stock >= #{qty}")
    int unlockStock(@Param("id") Long id, @Param("qty") Integer qty);

    @Update("UPDATE product_sku SET physical_stock = physical_stock - #{qty}, locked_stock = locked_stock - #{qty} " +
            "WHERE id = #{id} AND physical_stock >= #{qty} AND locked_stock >= #{qty}")
    int deductPhysicalAndLocked(@Param("id") Long id, @Param("qty") Integer qty);

    @Update("UPDATE product_sku SET physical_stock = physical_stock + #{qty} WHERE id = #{id}")
    int restorePhysical(@Param("id") Long id, @Param("qty") Integer qty);

    /** 调整物理库存（可正可负） */
    @Update("UPDATE product_sku SET physical_stock = physical_stock + #{qty}, updated_at=NOW() " +
            "WHERE id = #{id} AND physical_stock + #{qty} >= locked_stock")
    int adjustPhysical(@Param("id") Long id, @Param("qty") Integer qty);

    @Update("UPDATE product_sku SET status = #{status}, updated_at=NOW() WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Delete("DELETE FROM product_sku WHERE id=#{id} AND product_id=#{productId}")
    int deleteByIdAndProduct(@Param("id") Long id, @Param("productId") Long productId);

    @Delete("DELETE FROM product_sku WHERE product_id=#{productId}")
    int deleteByProduct(Long productId);

    @Select("SELECT COUNT(*) FROM product_sku WHERE product_id=#{productId}")
    int countByProduct(Long productId);
}
