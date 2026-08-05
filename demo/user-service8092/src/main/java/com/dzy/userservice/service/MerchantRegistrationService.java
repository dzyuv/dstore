package com.dzy.userservice.service;

import com.dzy.common.entity.Merchant;
import com.dzy.userservice.dto.MerchantApplyRequest;
import com.dzy.userservice.dto.MerchantReapplyRequest;
import jakarta.validation.Valid;

public interface MerchantRegistrationService {
    void apply(@Valid MerchantApplyRequest request);

    void reapply(@Valid MerchantReapplyRequest request);

    Merchant getApprovedMerchantByUserId(Long userId);
}
