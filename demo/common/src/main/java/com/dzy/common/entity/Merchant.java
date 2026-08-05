package com.dzy.common.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Merchant {
    private Long id;
    private Long userId;                 // 关联user.id，审核通过前为null
    private String merchantNo;           // 商家编号，自动生成
    private String companyName;
    private String legalPerson;
    private String idCard;
    private String businessLicense;      // 营业执照图片URL
    private String bankAccount;          // JSON格式存储多个银行账户
    private String status;               // PENDING / APPROVED / REJECTED / DISABLED
    private String auditRemark;          // 审核备注
}