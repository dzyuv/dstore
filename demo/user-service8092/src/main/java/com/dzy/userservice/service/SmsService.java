package com.dzy.userservice.service;

public interface SmsService {
    /** 发送验证码，演示环境返回验证码便于联调 */
    String sendCode(String phone, String scene);

    void verifyCode(String phone, String scene, String code);
}
