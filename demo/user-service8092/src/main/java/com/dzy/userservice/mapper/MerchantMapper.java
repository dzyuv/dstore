package com.dzy.userservice.mapper;

import com.dzy.common.entity.Merchant;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MerchantMapper {

    @Insert("INSERT INTO merchant(user_id, merchant_no, company_name, legal_person, id_card, business_license, " +
            "bank_account, phone, status, audit_remark, created_at, updated_at) " +
            "VALUES(#{userId}, #{merchantNo}, #{companyName}, #{legalPerson}, #{idCard}, #{businessLicense}, " +
            "#{bankAccount}, #{phone}, #{status}, #{auditRemark}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Merchant merchant);

    @Update("UPDATE merchant SET user_id=#{userId}, status=#{status}, audit_remark=#{auditRemark}, updated_at=NOW() WHERE id=#{id}")
    int updateAudit(Merchant merchant);

    @Update("UPDATE merchant SET company_name=#{companyName}, legal_person=#{legalPerson}, id_card=#{idCard}, " +
            "business_license=#{businessLicense}, bank_account=#{bankAccount}, phone=#{phone}, " +
            "status=#{status}, audit_remark=#{auditRemark}, updated_at=NOW() WHERE id=#{id}")
    int update(Merchant merchant);

    @Select("SELECT * FROM merchant WHERE id = #{id}")
    Merchant selectById(Long id);

    @Select("SELECT * FROM merchant WHERE phone = #{phone} AND status IN ('PENDING', 'REJECTED')")
    Merchant selectActiveApplyByPhone(String phone);

    @Select("SELECT * FROM merchant WHERE status = 'PENDING' ORDER BY created_at DESC")
    List<Merchant> selectPendingList();

    @Select("SELECT * FROM merchant WHERE user_id = #{userId}")
    Merchant selectByUserId(Long userId);
}