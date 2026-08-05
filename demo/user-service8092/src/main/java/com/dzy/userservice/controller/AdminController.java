package com.dzy.userservice.controller;

import com.dzy.common.constants.Constants;
import com.dzy.common.entity.ResultJSON;
import com.dzy.common.entity.User;
import com.dzy.userservice.dto.AuditMerchantRequest;
import com.dzy.userservice.dto.UpdateMerchantStatusRequest;
import com.dzy.userservice.dto.UpdateUserStatusRequest;
import com.dzy.common.entity.Merchant;
import com.dzy.userservice.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // ---------- 商家审核 ----------
    @GetMapping("/merchants/pending")
    public ResultJSON getPendingMerchants(@RequestHeader(Constants.HEADER_USER_ID) Long adminId) {
        List<Merchant> list = adminService.getPendingMerchants();
        return ResultJSON.success(list);
    }

    @PutMapping("/merchants/audit")
    public ResultJSON auditMerchant(@RequestHeader(Constants.HEADER_USER_ID) Long adminId,
                                          @Valid @RequestBody AuditMerchantRequest request) {
        adminService.auditMerchant(adminId, request);
        return ResultJSON.success();
    }

    // ---------- 商家管控 ----------
    @GetMapping("/merchants")
    public ResultJSON getMerchants(@RequestHeader(Constants.HEADER_USER_ID) Long adminId,
                                                   @RequestParam(required = false) String status) {
        // 管理员身份校验在 service 中执行
        List<Merchant> list = adminService.getMerchantList(status);
        return ResultJSON.success(list);
    }

    @PutMapping("/merchants/status")
    public ResultJSON updateMerchantStatus(@RequestHeader(Constants.HEADER_USER_ID) Long adminId,
                                                 @Valid @RequestBody UpdateMerchantStatusRequest request) {
        adminService.updateMerchantStatus(adminId, request);
        return ResultJSON.success();
    }

    // ---------- 用户管理 ----------
    @GetMapping("/users")
    public ResultJSON getUsers(@RequestHeader(Constants.HEADER_USER_ID) Long adminId,
                                           @RequestParam(required = false) String role,
                                           @RequestParam(required = false) Integer status) {
        List<User> list = adminService.getUserList(role, status);
        return ResultJSON.success(list);
    }

    @PutMapping("/users/status")
    public ResultJSON updateUserStatus(@RequestHeader(Constants.HEADER_USER_ID) Long adminId,
                                             @Valid @RequestBody UpdateUserStatusRequest request) {
        adminService.updateUserStatus(adminId, request);
        return ResultJSON.success();
    }
}