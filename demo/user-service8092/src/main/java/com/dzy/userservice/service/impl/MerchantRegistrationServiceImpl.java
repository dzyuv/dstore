package com.dzy.userservice.service.impl;

import com.dzy.common.constants.Constants;
import com.dzy.common.entity.Merchant;
import com.dzy.common.entity.User;
import com.dzy.common.exception.BusinessException;
import com.dzy.userservice.dto.MerchantApplyRequest;
import com.dzy.userservice.dto.MerchantReapplyRequest;
import com.dzy.userservice.mapper.MerchantMapper;
import com.dzy.userservice.mapper.UserMapper;
import com.dzy.userservice.service.MerchantRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MerchantRegistrationServiceImpl implements MerchantRegistrationService {

    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public void apply(MerchantApplyRequest request) {
        User existUser = userMapper.selectByPhone(request.getPhone());
        if (existUser != null) {
            throw new BusinessException("该手机号已被注册，请直接登录");
        }

        Merchant exist = merchantMapper.selectActiveApplyByPhone(request.getPhone());
        if (exist != null) {
            if (Constants.MERCHANT_PENDING.equals(exist.getStatus())) {
                throw new BusinessException("已有待审核的入驻申请");
            } else if (Constants.MERCHANT_REJECTED.equals(exist.getStatus())) {
                throw new BusinessException("申请已被驳回，请修改后重新提交");
            }
        }

        String merchantNo = "M" + System.currentTimeMillis()
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Merchant merchant = new Merchant();
        merchant.setUserId(null);
        merchant.setMerchantNo(merchantNo);
        merchant.setCompanyName(request.getCompanyName());
        merchant.setLegalPerson(request.getLegalPerson());
        merchant.setIdCard(request.getIdCard());
        merchant.setBusinessLicense(request.getBusinessLicense());
        merchant.setBankAccount(request.getBankAccount());
        merchant.setPhone(request.getPhone());
        merchant.setStatus(Constants.MERCHANT_PENDING);
        merchant.setAuditRemark(null);
        merchantMapper.insert(merchant);
    }

    @Override
    @Transactional
    public void reapply(MerchantReapplyRequest request) {
        Merchant merchant = merchantMapper.selectById(request.getMerchantId());
        if (merchant == null) {
            throw new BusinessException("申请记录不存在");
        }
        if (!Constants.MERCHANT_REJECTED.equals(merchant.getStatus())) {
            throw new BusinessException("只有被驳回的申请才能重新提交");
        }

        User existUser = userMapper.selectByPhone(request.getPhone());
        if (existUser != null && !existUser.getId().equals(merchant.getUserId())) {
            throw new BusinessException("该手机号已被其他用户注册");
        }

        merchant.setCompanyName(request.getCompanyName());
        merchant.setLegalPerson(request.getLegalPerson());
        merchant.setIdCard(request.getIdCard());
        merchant.setBusinessLicense(request.getBusinessLicense());
        merchant.setBankAccount(request.getBankAccount());
        merchant.setPhone(request.getPhone());
        merchant.setStatus(Constants.MERCHANT_PENDING);
        merchant.setAuditRemark(null);
        merchantMapper.update(merchant);
    }

    @Override
    public Merchant getApprovedMerchantByUserId(Long userId) {
        Merchant merchant = merchantMapper.selectByUserId(userId);
        if (merchant == null || !Constants.MERCHANT_APPROVED.equals(merchant.getStatus())) {
            throw new BusinessException("商家账号不存在或未审核通过");
        }
        return merchant;
    }
}