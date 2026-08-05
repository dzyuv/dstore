package com.dzy.userservice.service.impl;

import com.dzy.common.constants.Constants;
import com.dzy.common.entity.Merchant;
import com.dzy.common.entity.PageResult;
import com.dzy.common.entity.User;
import com.dzy.common.exception.BusinessException;
import com.dzy.userservice.client.GoodsAdminClient;
import com.dzy.userservice.dto.AuditMerchantRequest;
import com.dzy.userservice.dto.UpdateMerchantStatusRequest;
import com.dzy.userservice.dto.UpdateUserStatusRequest;
import com.dzy.userservice.entity.AdminOperationLog;
import com.dzy.userservice.mapper.AdminOperationLogMapper;
import com.dzy.userservice.mapper.MerchantMapper;
import com.dzy.userservice.mapper.StoreMapper;
import com.dzy.userservice.mapper.UserMapper;
import com.dzy.userservice.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private AdminOperationLogMapper logMapper;
    @Autowired(required = false)
    private GoodsAdminClient goodsAdminClient;

    private User checkAdmin(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || !Constants.ROLE_ADMIN.equals(user.getRole())) {
            throw new BusinessException("权限不足，仅管理员可操作");
        }
        return user;
    }

    private void saveLog(User admin, String actionType, String targetType, String targetId, String detail) {
        AdminOperationLog log = new AdminOperationLog();
        log.setAdminId(admin.getId());
        log.setAdminName(admin.getUsername());
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        log.setResult("SUCCESS");
        logMapper.insert(log);
    }

    @Override
    public List<Merchant> getPendingMerchants(Long adminId) {
        checkAdmin(adminId);
        return merchantMapper.selectMerchantList(Constants.MERCHANT_PENDING);
    }

    @Override
    @Transactional
    public Map<String, Object> auditMerchant(Long adminId, AuditMerchantRequest request) {
        User admin = checkAdmin(adminId);
        Merchant merchant = merchantMapper.selectById(request.getMerchantId());
        if (merchant == null) {
            throw new BusinessException("商家申请不存在");
        }
        if (!Constants.MERCHANT_PENDING.equals(merchant.getStatus())) {
            throw new BusinessException("该申请已被处理，请刷新列表");
        }

        Map<String, Object> result = new HashMap<>();
        if (Boolean.TRUE.equals(request.getApproved())) {
            if (userMapper.selectByPhone(merchant.getPhone()) != null) {
                throw new BusinessException("该手机号已被注册，无法创建商家账号");
            }
            // 生成 6 位初始密码（演示环境返回给管理员，生产应短信发送）
            String rawPassword = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
            User user = new User();
            user.setUsername(merchant.getPhone());
            user.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
            user.setPhone(merchant.getPhone());
            user.setRole(Constants.ROLE_MERCHANT);
            user.setStatus(1);
            userMapper.addUser(user);

            merchant.setUserId(user.getId());
            merchant.setStatus(Constants.MERCHANT_APPROVED);
            merchant.setAuditRemark(null);
            merchantMapper.updateAudit(merchant);

            result.put("approved", true);
            result.put("userId", user.getId());
            result.put("loginAccount", merchant.getPhone());
            result.put("initialPassword", rawPassword);
            result.put("message", "审核通过，已创建商家账号（请通知商家修改初始密码）");

            saveLog(admin, "AUDIT_MERCHANT", "MERCHANT", String.valueOf(merchant.getId()),
                    "审核通过，账号=" + merchant.getPhone());
        } else {
            if (request.getRemark() == null || request.getRemark().trim().isEmpty()) {
                throw new BusinessException("驳回时必须填写驳回原因");
            }
            merchant.setStatus(Constants.MERCHANT_REJECTED);
            merchant.setAuditRemark(request.getRemark());
            merchantMapper.updateAudit(merchant);
            result.put("approved", false);
            result.put("message", "已驳回");
            saveLog(admin, "AUDIT_MERCHANT", "MERCHANT", String.valueOf(merchant.getId()),
                    "审核驳回：" + request.getRemark());
        }
        return result;
    }

    @Override
    public List<Merchant> getMerchantList(Long adminId, String status) {
        checkAdmin(adminId);
        return merchantMapper.selectMerchantList(status);
    }

    @Override
    @Transactional
    public void updateMerchantStatus(Long adminId, UpdateMerchantStatusRequest request) {
        User admin = checkAdmin(adminId);
        Merchant merchant = merchantMapper.selectById(request.getMerchantId());
        if (merchant == null) {
            throw new BusinessException("商家不存在");
        }

        String targetStatus = request.getStatus();
        if (!Constants.MERCHANT_APPROVED.equals(targetStatus)
                && !Constants.MERCHANT_DISABLED.equals(targetStatus)) {
            throw new BusinessException("无效的状态值，仅允许 APPROVED 或 DISABLED");
        }
        if (Constants.MERCHANT_APPROVED.equals(targetStatus)
                && !Constants.MERCHANT_DISABLED.equals(merchant.getStatus())) {
            throw new BusinessException("该商家当前不是禁用状态，无法恢复");
        }
        if (Constants.MERCHANT_DISABLED.equals(targetStatus)
                && !Constants.MERCHANT_APPROVED.equals(merchant.getStatus())) {
            throw new BusinessException("该商家当前不是启用状态，无法封禁");
        }

        merchantMapper.updateMerchantStatus(merchant.getId(), targetStatus);

        if (Constants.MERCHANT_DISABLED.equals(targetStatus)) {
            if (merchant.getUserId() != null) {
                userMapper.updateUserStatus(merchant.getUserId(), 0);
            }
            // 门店置为休息
            storeMapper.updateStatusByMerchant(merchant.getId(), 0);
            // 商品全部下架（失败不阻断主流程）
            try {
                if (goodsAdminClient != null) {
                    goodsAdminClient.offlineByMerchant(merchant.getId());
                }
            } catch (Exception ignored) {
            }
            saveLog(admin, "DISABLE_MERCHANT", "MERCHANT", String.valueOf(merchant.getId()), "封禁商家并下架商品");
        } else {
            if (merchant.getUserId() != null) {
                userMapper.updateUserStatus(merchant.getUserId(), 1);
            }
            // 恢复后商品需商家手动上架
            saveLog(admin, "ENABLE_MERCHANT", "MERCHANT", String.valueOf(merchant.getId()), "恢复商家");
        }
    }

    @Override
    public List<User> getUserList(Long adminId, String role, Integer status) {
        checkAdmin(adminId);
        List<User> list = userMapper.selectUserList(role, status);
        list.forEach(u -> u.setPasswordHash(null));
        return list;
    }

    @Override
    @Transactional
    public void updateUserStatus(Long adminId, UpdateUserStatusRequest request) {
        User admin = checkAdmin(adminId);
        User user = userMapper.selectById(request.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (Constants.ROLE_ADMIN.equals(user.getRole())) {
            throw new BusinessException("管理员账户不可操作");
        }
        userMapper.updateUserStatus(user.getId(), request.getStatus());
        saveLog(admin, "UPDATE_USER_STATUS", "USER", String.valueOf(user.getId()),
                "status=" + request.getStatus());
    }

    @Override
    public PageResult<AdminOperationLog> getOperationLogs(Long adminId, String actionType, int page, int size) {
        checkAdmin(adminId);
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        int offset = (page - 1) * size;
        List<AdminOperationLog> list = logMapper.selectPage(actionType, offset, size);
        long total = logMapper.count(actionType);
        return PageResult.of(list, total, page, size);
    }
}
