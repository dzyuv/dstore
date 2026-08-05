package com.dzy.userservice.service.impl;

import com.dzy.common.entity.User;
import com.dzy.common.exception.BusinessException;
import com.dzy.userservice.dto.AuditMerchantRequest;
import com.dzy.userservice.dto.UpdateMerchantStatusRequest;
import com.dzy.userservice.dto.UpdateUserStatusRequest;
import com.dzy.common.entity.Merchant;
import com.dzy.userservice.mapper.MerchantMapper;
import com.dzy.userservice.mapper.UserMapper;
import com.dzy.userservice.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MerchantMapper merchantMapper;


    private void checkAdmin(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            throw new BusinessException("权限不足，仅管理员可操作");
        }
    }

    // ========== 商家审核 ==========

    public List<Merchant> getPendingMerchants(Long adminId) {
        checkAdmin(adminId);
        return merchantMapper.selectMerchantList("PENDING");
    }

    @Transactional
    public void auditMerchant(Long adminId, AuditMerchantRequest request) {
        checkAdmin(adminId);

        Merchant merchant = merchantMapper.selectById(request.getMerchantId());
        if (merchant == null) {
            throw new BusinessException("商家申请不存在");
        }
        if (!"PENDING".equals(merchant.getStatus())) {
            throw new BusinessException("该申请已被处理，请刷新列表");
        }

        if (request.getApproved()) {
            // 审核通过
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
            // 审核驳回
            if (request.getRemark() == null || request.getRemark().trim().isEmpty()) {
                throw new BusinessException("驳回时必须填写驳回原因");
            }
            merchant.setStatus("REJECTED");
            merchant.setAuditRemark(request.getRemark());
            merchantMapper.updateAudit(merchant);
        }
    }

    // ========== 商家管控 ==========

    public List<Merchant> getMerchantList(Long adminId,String status) {
        checkAdmin(adminId);
        return merchantMapper.selectMerchantList(status);
    }

    @Transactional
    public void updateMerchantStatus(Long adminId, UpdateMerchantStatusRequest request) {
        checkAdmin(adminId);

        Merchant merchant = merchantMapper.selectById(request.getMerchantId());
        if (merchant == null) {
            throw new BusinessException("商家不存在");
        }
        // 不允许对PENDING或REJECTED状态的商家进行封禁/恢复，仅可操作APPROVED/DISABLED
        String targetStatus = request.getStatus();
        if (!"APPROVED".equals(targetStatus) && !"DISABLED".equals(targetStatus)) {
            throw new BusinessException("无效的状态值，仅允许 APPROVED 或 DISABLED");
        }
        if ("APPROVED".equals(targetStatus) && !"DISABLED".equals(merchant.getStatus())) {
            throw new BusinessException("该商家当前不是禁用状态，无法恢复");
        }
        if ("DISABLED".equals(targetStatus) && !"APPROVED".equals(merchant.getStatus())) {
            throw new BusinessException("该商家当前不是启用状态，无法封禁");
        }

        // 更新商家状态
        merchantMapper.updateMerchantStatus(merchant.getId(), targetStatus);

        // 如果是封禁，还需要禁用关联的用户账号，并下架所有门店商品（商品服务未开发，暂略）
        if ("DISABLED".equals(targetStatus)) {
            // 禁用用户
            if (merchant.getUserId() != null) {
                userMapper.updateUserStatus(merchant.getUserId(), 0);
            }

        } else if ("APPROVED".equals(targetStatus)) {
            // 恢复用户
            if (merchant.getUserId() != null) {
                userMapper.updateUserStatus(merchant.getUserId(), 1);
            }
            // 商品需手动重新上架（暂不自动恢复）
        }
    }

    // ========== 用户管理 ==========

    public List<User> getUserList(Long adminId,String role, Integer status) {
        checkAdmin(adminId);
        return userMapper.selectUserList(role, status);
    }

    @Transactional
    public void updateUserStatus(Long adminId, UpdateUserStatusRequest request) {
        checkAdmin(adminId);

        User user = userMapper.selectById(request.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException("管理员账户不可操作");
        }
        userMapper.updateUserStatus(user.getId(), request.getStatus());
    }
}