package com.dzy.userservice.mapper;

import com.dzy.common.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE username = #{username} OR phone = #{username}")
    User selectByUsername(String username);

    @Select("SELECT COUNT(*) FROM user WHERE phone=#{phone}")
    int countByPhone(@NotBlank(message = "手机号不能为空") @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone);

    @Insert("INSERT INTO user(username, password_hash, phone, role, status, created_at, updated_at) " +
            "VALUES(#{username}, #{passwordHash}, #{phone}, #{role}, 1, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void addUser(User user);

    @Select("SELECT * FROM user WHERE phone = #{phone}")
    User selectByPhone(String phone);

    @Update("UPDATE user SET status = #{status} WHERE id = #{id}")
    int updateUserStatus(@Param("id") Long id, @Param("status") Integer status);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectById(Long id);

    @Select("<script>" +
            "SELECT * FROM user WHERE 1=1 " +
            "<if test='role != null'> AND role = #{role} </if>" +
            "<if test='status != null'> AND status = #{status} </if>" +
            "ORDER BY id DESC" +
            "</script>")
    List<User> selectUserList(@Param("role") String role, @Param("status") Integer status);
}
