package com.dzy.userservice.mapper;

import com.dzy.userservice.entity.SmsCode;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SmsCodeMapper {

    @Insert("INSERT INTO sms_code(phone, code, scene, expire_at, used, created_at) " +
            "VALUES(#{phone}, #{code}, #{scene}, #{expireAt}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SmsCode smsCode);

    @Select("SELECT * FROM sms_code WHERE phone=#{phone} AND scene=#{scene} AND used=0 " +
            "ORDER BY id DESC LIMIT 1")
    SmsCode selectLatest(@Param("phone") String phone, @Param("scene") String scene);

    @Update("UPDATE sms_code SET used=1 WHERE id=#{id}")
    int markUsed(Long id);
}
