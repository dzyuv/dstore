package com.dzy.goodsprovider8090.mapper;



import com.dzy.common.entity.Goods;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GoodsMapper {
    Goods selectById(Long id);
    List<Goods> selectAll();
}
