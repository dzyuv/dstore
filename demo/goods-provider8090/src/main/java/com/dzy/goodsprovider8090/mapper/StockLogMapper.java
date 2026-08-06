package com.dzy.goodsprovider8090.mapper;

import com.dzy.goodsprovider8090.entity.StockLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StockLogMapper {

    @Insert("INSERT INTO stock_log(sku_id, change_type, change_qty, physical_after, locked_after, biz_no, remark, created_at) " +
            "VALUES(#{skuId}, #{changeType}, #{changeQty}, #{physicalAfter}, #{lockedAfter}, #{bizNo}, #{remark}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StockLog log);

    @Select("SELECT * FROM stock_log WHERE sku_id = #{skuId} ORDER BY id DESC LIMIT #{limit}")
    List<StockLog> selectBySku(@Param("skuId") Long skuId, @Param("limit") int limit);

    @Select("SELECT * FROM stock_log WHERE biz_no = #{bizNo} ORDER BY id ASC")
    List<StockLog> selectByBizNo(String bizNo);
}
