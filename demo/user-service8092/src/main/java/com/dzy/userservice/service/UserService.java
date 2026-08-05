package com.dzy.userservice.service;

import com.dzy.common.entity.User;
import com.dzy.userservice.dto.LoginRequest;
import com.dzy.userservice.dto.RegisterRequest;

import java.util.Map;

public interface UserService {
    Map<String, Object> login(LoginRequest request);

    User register(RegisterRequest request);

    User getById(Long userId);
}
