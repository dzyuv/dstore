package com.dzy.userservice.mapper;

import com.dzy.common.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE username= #{username}")
    User selectByUsername(@NotBlank(message = "用户名不能为空") String username);

    @Select("SELECT COUNT(*) FROM user WHERE phone=#{phone}")
    int countByPhone(@NotBlank(message = "手机号不能为空") @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone);

    @Insert("INSERT INTO user(username, password_hash, phone, role, status, created_at, updated_at) " +
            "VALUES(#{username}, #{passwordHash}, #{phone}, #{role}, 1, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void addUser(User user);
}
