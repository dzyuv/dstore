package com.dzy.userservice.controller;

import com.dzy.common.constants.Constants;
import com.dzy.common.entity.ResultJSON;
import com.dzy.common.entity.User;
import com.dzy.userservice.dto.LoginRequest;
import com.dzy.userservice.dto.RegisterRequest;
import com.dzy.userservice.dto.SendSmsRequest;
import com.dzy.userservice.service.SmsService;
import com.dzy.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private SmsService smsService;

    @PostMapping("/sms/send")
    public ResultJSON sendSms(@Valid @RequestBody SendSmsRequest request) {
        String code = smsService.sendCode(request.getPhone(),
                request.getScene() == null ? "REGISTER" : request.getScene());
        // 演示环境返回验证码；生产环境应移除
        return ResultJSON.success(Map.of("message", "验证码已发送", "demoCode", code));
    }

    @PostMapping("/login")
    public ResultJSON login(@Valid @RequestBody LoginRequest request) {
        return ResultJSON.success(userService.login(request));
    }

    @PostMapping
    public ResultJSON register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return ResultJSON.success(user);
    }

    @GetMapping("/me")
    public ResultJSON me(@RequestHeader(Constants.HEADER_USER_ID) Long userId) {
        return ResultJSON.success(userService.getById(userId));
    }
}
