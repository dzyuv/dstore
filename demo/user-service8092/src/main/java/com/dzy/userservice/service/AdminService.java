package com.dzy.userservice.service;

import com.dzy.common.entity.Merchant;
import com.dzy.common.entity.User;
import com.dzy.userservice.dto.AuditMerchantRequest;
import com.dzy.userservice.dto.UpdateMerchantStatusRequest;
import com.dzy.userservice.dto.UpdateUserStatusRequest;
import jakarta.validation.Valid;

import java.util.List;

public interface AdminService {
    List<Merchant> getPendingMerchants();

    void auditMerchant(Long adminId, @Valid AuditMerchantRequest request);

    List<Merchant> getMerchantList(String status);

    void updateMerchantStatus(Long adminId, @Valid UpdateMerchantStatusRequest request);

    List<User> getUserList(String role, Integer status);

    void updateUserStatus(Long adminId, @Valid UpdateUserStatusRequest request);
}
