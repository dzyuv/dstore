package com.dzy.userservice.controller;

import com.dzy.common.entity.ResultJSON;
import com.dzy.userservice.dto.MerchantApplyRequest;
import com.dzy.userservice.dto.MerchantReapplyRequest;
import com.dzy.userservice.service.MerchantRegistrationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/merchant")
public class MerchantRegistrationController {

    @Autowired
    private MerchantRegistrationService merchantRegistrationService;

    // 商家提交入驻申请（公开）
    @PostMapping("/apply")
    public ResultJSON apply(@Valid @RequestBody MerchantApplyRequest request) {
        merchantRegistrationService.apply(request);
        return ResultJSON.success();
    }

    // 商家重新提交（需商家登录，因为要获取其已有的merchantId）
    @PutMapping("/reapply")
    public ResultJSON reapply(@Valid @RequestBody MerchantReapplyRequest request) {
        merchantRegistrationService.reapply(request);
        return ResultJSON.success();
    }
}