package com.dzy.goodsprovider8090.mapper;

import com.dzy.goodsprovider8090.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("SELECT * FROM category ORDER BY sort_order ASC, id ASC")
    List<Category> selectAll();

    @Select("SELECT * FROM category WHERE status = 1 ORDER BY sort_order ASC, id ASC")
    List<Category> selectEnabled();

    @Select("SELECT * FROM category WHERE parent_id = #{parentId} ORDER BY sort_order ASC")
    List<Category> selectByParent(@Param("parentId") Long parentId);

    @Select("SELECT * FROM category WHERE id = #{id}")
    Category selectById(Long id);

    @Insert("INSERT INTO category(parent_id, name, level, sort_order, status) " +
            "VALUES(#{parentId}, #{name}, #{level}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    @Update("UPDATE category SET name=#{name}, sort_order=#{sortOrder}, status=#{status}, updated_at=NOW() WHERE id=#{id}")
    int update(Category category);

    @Delete("DELETE FROM category WHERE id=#{id}")
    int delete(Long id);

    @Select("SELECT COUNT(*) FROM category WHERE parent_id = #{parentId}")
    int countChildren(Long parentId);
}
