package com.dzy.userservice.service.impl;

import com.dzy.common.entity.User;
import com.dzy.common.exception.BusinessException;
import com.dzy.userservice.mapper.UserMapper;
import com.dzy.userservice.dto.LoginRequest;
import com.dzy.userservice.dto.RegisterRequest;
import com.dzy.userservice.service.UserService;
import com.dzy.userservice.util.UserServiceJwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserServiceJwtUtil JwtUtil;

    @Override
    public String login(LoginRequest request) {
        User user=userMapper.selectByUsername(request.getUsername());
        if(user ==null || user.getStatus()==0 || !BCrypt.checkpw(request.getPassword(),user.getPasswordHash())){
            throw new BusinessException("用户名或密码错误");
        }
        return JwtUtil.generateAccessToken(user.getId(),user.getUsername(),user.getRole());
    }

    @Override
    public User register(RegisterRequest request) {
        String role = request.getRole();
        if (role != null && !"CUSTOMER".equals(role)) {
            throw new BusinessException("只支持普通用户注册，商家请通过入驻申请");
        }
        if(userMapper.countByPhone(request.getPhone()) != 0){
            throw new BusinessException("该手机已被注册");
        }
        String encodePassWord =BCrypt.hashpw(request.getPassword(),BCrypt.gensalt());

        User user=new User();
        user.setUsername(request.getPhone());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole()==null?"CUSTOMER": request.getRole());
        user.setStatus(1);
        user.setPasswordHash(encodePassWord);

        userMapper.addUser(user);

        user.setPasswordHash(null);
        return user;
    }
}
