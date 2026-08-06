package com.dzy.userservice.mapper;

import com.dzy.userservice.entity.AdminOperationLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AdminOperationLogMapper {

    @Insert("INSERT INTO admin_operation_log(admin_id, admin_name, action_type, target_type, target_id, detail, result, created_at) " +
            "VALUES(#{adminId}, #{adminName}, #{actionType}, #{targetType}, #{targetId}, #{detail}, #{result}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AdminOperationLog log);

    @Select("<script>" +
            "SELECT * FROM admin_operation_log WHERE 1=1 " +
            "<if test='actionType != null and actionType != \"\"'> AND action_type = #{actionType} </if>" +
            "ORDER BY id DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<AdminOperationLog> selectPage(@Param("actionType") String actionType,
                                       @Param("offset") int offset,
                                       @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM admin_operation_log WHERE 1=1 " +
            "<if test='actionType != null and actionType != \"\"'> AND action_type = #{actionType} </if>" +
            "</script>")
    long count(@Param("actionType") String actionType);
}
