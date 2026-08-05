package com.dzy.userservice.service.impl;

import com.dzy.common.entity.User;
import com.dzy.common.exception.BusinessException;
import com.dzy.userservice.dto.AuditMerchantRequest;
import com.dzy.userservice.dto.MerchantApplyRequest;
import com.dzy.userservice.dto.MerchantReapplyRequest;
import com.dzy.common.entity.Merchant;
import com.dzy.userservice.mapper.MerchantMapper;
import com.dzy.userservice.mapper.UserMapper;
import com.dzy.userservice.service.MerchantRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MerchantRegistrationServiceImpl implements MerchantRegistrationService {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 商家提交初次入驻申请
     */
    @Transactional
    public void apply(MerchantApplyRequest request) {
        // 1. 检查手机号是否已被注册（任何角色）
        User existUser = userMapper.selectByPhone(request.getPhone());
        if (existUser != null) {
            throw new BusinessException("该手机号已被注册，请直接登录");
        }

        // 2. 防止重复提交：检查是否存在PENDING或REJECTED状态的申请
        Merchant exist = merchantMapper.selectActiveApplyByPhone(request.getPhone());
        if (exist != null) {
            if ("PENDING".equals(exist.getStatus())) {
                throw new BusinessException("您已有待审核的入驻申请，请耐心等待");
            } else if ("REJECTED".equals(exist.getStatus())) {
                throw new BusinessException("您之前的申请已被驳回，请修改后重新提交（使用重新提交接口）");
            }
        }

        // 3. 生成商家编号
        String merchantNo = "M" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // 4. 保存商家记录
        Merchant merchant = new Merchant();
        merchant.setUserId(null);
        merchant.setMerchantNo(merchantNo);
        merchant.setCompanyName(request.getCompanyName());
        merchant.setLegalPerson(request.getLegalPerson());
        merchant.setIdCard(request.getIdCard());
        merchant.setBusinessLicense(request.getBusinessLicense());
        merchant.setBankAccount(request.getBankAccount());
        merchant.setPhone(request.getPhone());
        merchant.setStatus("PENDING");
        merchant.setAuditRemark(null);

        merchantMapper.insert(merchant);
    }

    /**
     * 驳回后重新提交（修改资料后重新提交审核）
     */
    @Transactional
    public void reapply(MerchantReapplyRequest request) {
        // 1. 查询该商家申请记录
        Merchant merchant = merchantMapper.selectById(request.getMerchantId());
        if (merchant == null) {
            throw new BusinessException("申请记录不存在");
        }
        if (!"REJECTED".equals(merchant.getStatus())) {
            throw new BusinessException("只有被驳回的申请才能重新提交");
        }

        // 2. 检查手机号是否已被其他用户注册（排除自己）
        User existUser = userMapper.selectByPhone(request.getPhone());
        if (existUser != null && !existUser.getId().equals(merchant.getUserId())) {
            throw new BusinessException("该手机号已被其他用户注册");
        }

        // 3. 更新记录（重置状态为PENDING）
        merchant.setCompanyName(request.getCompanyName());
        merchant.setLegalPerson(request.getLegalPerson());
        merchant.setIdCard(request.getIdCard());
        merchant.setBusinessLicense(request.getBusinessLicense());
        merchant.setBankAccount(request.getBankAccount());
        merchant.setPhone(request.getPhone());
        merchant.setStatus("PENDING");
        merchant.setAuditRemark(null);

        merchantMapper.update(merchant);
    }

    /**
     * 管理员查询待审核列表
     */
    public List<Merchant> getPendingList() {
        return merchantMapper.selectPendingList();
    }

    /**
     * 管理员审核
     */
    @Transactional
    public void audit(AuditMerchantRequest request) {
        Merchant merchant = merchantMapper.selectById(request.getMerchantId());
        if (merchant == null) {
            throw new BusinessException("申请记录不存在");
        }
        if (!"PENDING".equals(merchant.getStatus())) {
            throw new BusinessException("该申请已被处理，请刷新列表");
        }

        if (request.getApproved()) {
            // ----- 审核通过 -----
            // 1. 再次检查手机号是否已被注册（防止管理员操作期间被注册）
            if (userMapper.selectByPhone(merchant.getPhone()) != null) {
                throw new BusinessException("该手机号已被注册，无法创建商家账号");
            }

            // 2. 生成初始密码（6位随机数字）
            String rawPassword = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
            String encodedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

            // 3. 创建用户
            User user = new User();
            user.setUsername(merchant.getPhone());
            user.setPasswordHash(encodedPassword);
            user.setPhone(merchant.getPhone());
            user.setRole("MERCHANT");
            user.setStatus(1);
            userMapper.addUser(user);

            // 4. 更新商家记录
            merchant.setUserId(user.getId());
            merchant.setStatus("APPROVED");
            merchant.setAuditRemark(null);
            merchantMapper.updateAudit(merchant);
            

        } else {
            // ----- 审核驳回 -----
            if (request.getRemark() == null || request.getRemark().trim().isEmpty()) {
                throw new BusinessException("驳回时必须填写驳回原因");
            }
            merchant.setStatus("REJECTED");
            merchant.setAuditRemark(request.getRemark());
            merchantMapper.updateAudit(merchant);
        }
    }

    /**
     * 根据用户ID查询已审核通过的商家信息（供后续门店管理等使用）
     */
    public Merchant getApprovedMerchantByUserId(Long userId) {
        Merchant merchant = merchantMapper.selectByUserId(userId);
        if (merchant == null || !"APPROVED".equals(merchant.getStatus())) {
            throw new BusinessException("商家账号不存在或未审核通过");
        }
        return merchant;
    }
}