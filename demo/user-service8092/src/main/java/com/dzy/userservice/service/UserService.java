package com.dzy.userservice.service;

import com.dzy.common.entity.User;
import com.dzy.userservice.dto.LoginRequest;
import com.dzy.userservice.dto.RegisterRequest;
import jakarta.validation.Valid;

public interface UserService {
    String login(@Valid LoginRequest request);

    User register(@Valid RegisterRequest request);
}
