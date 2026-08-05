package com.dzy.userservice.service;

import com.dzy.common.entity.Merchant;
import com.dzy.common.entity.PageResult;
import com.dzy.common.entity.User;
import com.dzy.userservice.dto.AuditMerchantRequest;
import com.dzy.userservice.dto.UpdateMerchantStatusRequest;
import com.dzy.userservice.dto.UpdateUserStatusRequest;
import com.dzy.userservice.entity.AdminOperationLog;

import java.util.List;
import java.util.Map;

public interface AdminService {
    List<Merchant> getPendingMerchants(Long adminId);

    Map<String, Object> auditMerchant(Long adminId, AuditMerchantRequest request);

    List<Merchant> getMerchantList(Long adminId, String status);

    void updateMerchantStatus(Long adminId, UpdateMerchantStatusRequest request);

    List<User> getUserList(Long adminId, String role, Integer status);

    void updateUserStatus(Long adminId, UpdateUserStatusRequest request);

    PageResult<AdminOperationLog> getOperationLogs(Long adminId, String actionType, int page, int size);
}
