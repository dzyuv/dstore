package com.dzy.userservice.service.impl;

import com.dzy.common.constants.Constants;
import com.dzy.common.entity.User;
import com.dzy.common.exception.BusinessException;
import com.dzy.userservice.dto.LoginRequest;
import com.dzy.userservice.dto.RegisterRequest;
import com.dzy.userservice.mapper.UserMapper;
import com.dzy.userservice.service.SmsService;
import com.dzy.userservice.service.UserService;
import com.dzy.userservice.util.UserServiceJwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserServiceJwtUtil jwtUtil;
    @Autowired
    private SmsService smsService;

    @Override
    public Map<String, Object> login(LoginRequest request) {
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null || user.getStatus() == 0
                || !BCrypt.checkpw(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }
        String token = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        user.setPasswordHash(null);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        String role = request.getRole();
        if (role != null && !Constants.ROLE_CUSTOMER.equals(role)) {
            throw new BusinessException("只支持普通用户注册，商家请通过入驻申请");
        }
        smsService.verifyCode(request.getPhone(), "REGISTER", request.getSmsCode());

        if (userMapper.countByPhone(request.getPhone()) != 0) {
            throw new BusinessException("该手机已被注册");
        }

        User user = new User();
        user.setUsername(request.getPhone());
        user.setPhone(request.getPhone());
        user.setRole(Constants.ROLE_CUSTOMER);
        user.setStatus(1);
        user.setPasswordHash(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        userMapper.addUser(user);
        user.setPasswordHash(null);
        return user;
    }

    @Override
    public User getById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPasswordHash(null);
        return user;
    }
}
