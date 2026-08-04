package com.dzy.userservice.controller;


import com.dzy.common.entity.ResultJSON;
import com.dzy.common.entity.User;
import com.dzy.userservice.dto.LoginRequest;
import com.dzy.userservice.dto.RegisterRequest;
import com.dzy.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResultJSON login(@Valid @RequestBody LoginRequest request){
        String token =userService.login(request);
        return ResultJSON.success(token);
    }

    @PostMapping
    public ResultJSON register(@Valid @RequestBody RegisterRequest request){
        User user=userService.register(request);
        return ResultJSON.success(user);
    }
}
