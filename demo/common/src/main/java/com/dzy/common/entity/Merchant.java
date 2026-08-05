package com.dzy.common.entity;

import lombok.Data;

@Data
public class Merchant {
    private Long id;
    private Long userId;
    private String merchantNo;
    private String companyName;
    private String legalPerson;
    private String idCard;
    private String businessLicense;
    private String bankAccount;
    private String phone;
    private String status;
    private String auditRemark;
}
