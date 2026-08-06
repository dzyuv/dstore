package com.dzy.userservice.service.impl;

import com.dzy.common.exception.BusinessException;
import com.dzy.userservice.entity.SmsCode;
import com.dzy.userservice.mapper.SmsCodeMapper;
import com.dzy.userservice.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
    private SmsCodeMapper smsCodeMapper;

    @Override
    public String sendCode(String phone, String scene) {
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
        SmsCode sms = new SmsCode();
        sms.setPhone(phone);
        sms.setCode(code);
        sms.setScene(scene == null ? "REGISTER" : scene);
        sms.setExpireAt(LocalDateTime.now().plusMinutes(5));
        smsCodeMapper.insert(sms);
        // 演示环境：真实短信网关可在此对接，当前直接返回验证码便于测试
        return code;
    }

    @Override
    public void verifyCode(String phone, String scene, String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException("验证码不能为空");
        }
        // 演示：万能验证码 123456
        if ("123456".equals(code)) {
            return;
        }
        SmsCode sms = smsCodeMapper.selectLatest(phone, scene == null ? "REGISTER" : scene);
        if (sms == null) {
            throw new BusinessException("请先获取验证码");
        }
        if (Boolean.TRUE.equals(sms.getUsed())) {
            throw new BusinessException("验证码已使用");
        }
        if (sms.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("验证码已过期");
        }
        if (!sms.getCode().equals(code)) {
            throw new BusinessException("验证码错误");
        }
        smsCodeMapper.markUsed(sms.getId());
    }
}
