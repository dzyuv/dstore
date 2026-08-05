package com.dzy.userservice.controller;

import com.dzy.common.constants.Constants;
import com.dzy.common.entity.ResultJSON;
import com.dzy.userservice.dto.AuditMerchantRequest;
import com.dzy.userservice.dto.UpdateMerchantStatusRequest;
import com.dzy.userservice.dto.UpdateUserStatusRequest;
import com.dzy.userservice.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/merchants/pending")
    public ResultJSON getPendingMerchants(@RequestHeader(Constants.HEADER_USER_ID) Long adminId) {
        return ResultJSON.success(adminService.getPendingMerchants(adminId));
    }

    @PutMapping("/merchants/audit")
    public ResultJSON auditMerchant(@RequestHeader(Constants.HEADER_USER_ID) Long adminId,
                                    @Valid @RequestBody AuditMerchantRequest request) {
        return ResultJSON.success(adminService.auditMerchant(adminId, request));
    }

    @GetMapping("/merchants")
    public ResultJSON getMerchants(@RequestHeader(Constants.HEADER_USER_ID) Long adminId,
                                   @RequestParam(required = false) String status) {
        return ResultJSON.success(adminService.getMerchantList(adminId, status));
    }

    @PutMapping("/merchants/status")
    public ResultJSON updateMerchantStatus(@RequestHeader(Constants.HEADER_USER_ID) Long adminId,
                                           @Valid @RequestBody UpdateMerchantStatusRequest request) {
        adminService.updateMerchantStatus(adminId, request);
        return ResultJSON.success();
    }

    @GetMapping("/users")
    public ResultJSON getUsers(@RequestHeader(Constants.HEADER_USER_ID) Long adminId,
                               @RequestParam(required = false) String role,
                               @RequestParam(required = false) Integer status) {
        return ResultJSON.success(adminService.getUserList(adminId, role, status));
    }

    @PutMapping("/users/status")
    public ResultJSON updateUserStatus(@RequestHeader(Constants.HEADER_USER_ID) Long adminId,
                                       @Valid @RequestBody UpdateUserStatusRequest request) {
        adminService.updateUserStatus(adminId, request);
        return ResultJSON.success();
    }

    @GetMapping("/operation-logs")
    public ResultJSON getOperationLogs(@RequestHeader(Constants.HEADER_USER_ID) Long adminId,
                                       @RequestParam(required = false) String actionType,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        return ResultJSON.success(adminService.getOperationLogs(adminId, actionType, page, size));
    }
}
