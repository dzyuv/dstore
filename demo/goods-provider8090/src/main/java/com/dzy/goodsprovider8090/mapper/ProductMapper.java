package com.dzy.goodsprovider8090.mapper;

import com.dzy.goodsprovider8090.entity.Product;
import com.dzy.goodsprovider8090.vo.ProductListVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductMapper {

    @Insert("INSERT INTO product(store_id, merchant_id, category_id, name, main_image, detail, status, on_sale_time) " +
            "VALUES(#{storeId}, #{merchantId}, #{categoryId}, #{name}, #{mainImage}, #{detail}, #{status}, " +
            "CASE WHEN #{status}='ON_SALE' THEN NOW() ELSE NULL END)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Update("UPDATE product SET category_id=#{categoryId}, name=#{name}, main_image=#{mainImage}, detail=#{detail}, " +
            "updated_at=NOW() WHERE id=#{id} AND merchant_id=#{merchantId}")
    int update(Product product);

    @Select("SELECT * FROM product WHERE id = #{id}")
    Product selectById(Long id);

    @Select("SELECT * FROM product WHERE id = #{id} AND merchant_id = #{merchantId}")
    Product selectByIdAndMerchant(@Param("id") Long id, @Param("merchantId") Long merchantId);

    @Select("SELECT * FROM product WHERE merchant_id = #{merchantId} ORDER BY id DESC")
    List<Product> selectByMerchant(Long merchantId);

    @Select("SELECT * FROM product WHERE store_id = #{storeId} ORDER BY id DESC")
    List<Product> selectByStore(Long storeId);

    @Update("UPDATE product SET status = #{status}, " +
            "on_sale_time = CASE WHEN #{status}='ON_SALE' THEN NOW() ELSE on_sale_time END, " +
            "updated_at = NOW() WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE product SET status = 'OFF_SALE', updated_at = NOW() " +
            "WHERE merchant_id = #{merchantId} AND status = 'ON_SALE'")
    int offlineByMerchant(@Param("merchantId") Long merchantId);

    @Update("UPDATE product SET status = 'OFF_SALE', updated_at = NOW() " +
            "WHERE store_id = #{storeId} AND status = 'ON_SALE'")
    int offlineByStore(@Param("storeId") Long storeId);

    @Delete("DELETE FROM product WHERE id=#{id} AND merchant_id=#{merchantId}")
    int deleteByIdAndMerchant(@Param("id") Long id, @Param("merchantId") Long merchantId);

    @Select("<script>" +
            "SELECT p.id, p.store_id AS storeId, p.merchant_id AS merchantId, p.category_id AS categoryId, " +
            "c.name AS categoryName, p.name, p.main_image AS mainImage, p.status, p.on_sale_time AS onSaleTime, " +
            "MIN(s.price) AS minPrice, MAX(s.price) AS maxPrice, COUNT(s.id) AS skuCount, " +
            "IFNULL(SUM(CASE WHEN s.status='ON' THEN (s.physical_stock - s.locked_stock) ELSE 0 END), 0) AS totalAvailableStock " +
            "FROM product p " +
            "LEFT JOIN category c ON p.category_id = c.id " +
            "LEFT JOIN product_sku s ON p.id = s.product_id " +
            "WHERE p.status = 'ON_SALE' " +
            "<if test='keyword != null and keyword != \"\"'> AND p.name LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "<if test='categoryId != null'> AND (p.category_id = #{categoryId} OR c.parent_id = #{categoryId}) </if>" +
            "<if test='storeId != null'> AND p.store_id = #{storeId} </if>" +
            "GROUP BY p.id " +
            "ORDER BY p.on_sale_time DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<ProductListVO> searchOnSale(@Param("keyword") String keyword,
                                     @Param("categoryId") Long categoryId,
                                     @Param("storeId") Long storeId,
                                     @Param("offset") int offset,
                                     @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(DISTINCT p.id) FROM product p " +
            "LEFT JOIN category c ON p.category_id = c.id " +
            "WHERE p.status = 'ON_SALE' " +
            "<if test='keyword != null and keyword != \"\"'> AND p.name LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "<if test='categoryId != null'> AND (p.category_id = #{categoryId} OR c.parent_id = #{categoryId}) </if>" +
            "<if test='storeId != null'> AND p.store_id = #{storeId} </if>" +
            "</script>")
    long countOnSale(@Param("keyword") String keyword,
                     @Param("categoryId") Long categoryId,
                     @Param("storeId") Long storeId);

    @Select("<script>" +
            "SELECT p.id, p.store_id AS storeId, p.merchant_id AS merchantId, p.category_id AS categoryId, " +
            "c.name AS categoryName, p.name, p.main_image AS mainImage, p.status, p.on_sale_time AS onSaleTime, " +
            "MIN(s.price) AS minPrice, MAX(s.price) AS maxPrice, COUNT(s.id) AS skuCount, " +
            "IFNULL(SUM(s.physical_stock - s.locked_stock), 0) AS totalAvailableStock " +
            "FROM product p " +
            "LEFT JOIN category c ON p.category_id = c.id " +
            "LEFT JOIN product_sku s ON p.id = s.product_id " +
            "WHERE p.merchant_id = #{merchantId} " +
            "<if test='status != null and status != \"\"'> AND p.status = #{status} </if>" +
            "<if test='keyword != null and keyword != \"\"'> AND p.name LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "GROUP BY p.id ORDER BY p.id DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<ProductListVO> merchantSearch(@Param("merchantId") Long merchantId,
                                       @Param("keyword") String keyword,
                                       @Param("status") String status,
                                       @Param("offset") int offset,
                                       @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM product p WHERE p.merchant_id = #{merchantId} " +
            "<if test='status != null and status != \"\"'> AND p.status = #{status} </if>" +
            "<if test='keyword != null and keyword != \"\"'> AND p.name LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "</script>")
    long merchantCount(@Param("merchantId") Long merchantId,
                       @Param("keyword") String keyword,
                       @Param("status") String status);

    @Select("<script>" +
            "SELECT p.id, p.store_id AS storeId, p.merchant_id AS merchantId, p.category_id AS categoryId, " +
            "c.name AS categoryName, p.name, p.main_image AS mainImage, p.status, p.on_sale_time AS onSaleTime, " +
            "MIN(s.price) AS minPrice, MAX(s.price) AS maxPrice, COUNT(s.id) AS skuCount, " +
            "IFNULL(SUM(s.physical_stock - s.locked_stock), 0) AS totalAvailableStock " +
            "FROM product p " +
            "LEFT JOIN category c ON p.category_id = c.id " +
            "LEFT JOIN product_sku s ON p.id = s.product_id " +
            "WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'> AND p.name LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "<if test='status != null and status != \"\"'> AND p.status = #{status} </if>" +
            "<if test='merchantId != null'> AND p.merchant_id = #{merchantId} </if>" +
            "GROUP BY p.id ORDER BY p.id DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<ProductListVO> adminSearch(@Param("keyword") String keyword,
                                    @Param("status") String status,
                                    @Param("merchantId") Long merchantId,
                                    @Param("offset") int offset,
                                    @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM product p WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'> AND p.name LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "<if test='status != null and status != \"\"'> AND p.status = #{status} </if>" +
            "<if test='merchantId != null'> AND p.merchant_id = #{merchantId} </if>" +
            "</script>")
    long adminCount(@Param("keyword") String keyword,
                    @Param("status") String status,
                    @Param("merchantId") Long merchantId);

    @Select("SELECT COUNT(*) FROM product WHERE category_id = #{categoryId}")
    int countByCategory(Long categoryId);
}
