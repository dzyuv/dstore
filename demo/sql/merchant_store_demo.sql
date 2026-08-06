-- =============================================
-- 商家和门店演示数据（user_db）
-- 说明：不要把 merchant.user_id 指向管理员(id=1)。
-- 推荐流程：
--   1) 前台「商家入驻」提交资质
--   2) 管理员审核通过后系统自动创建商家账号
--   3) 商家登录后自行新增门店
-- 本脚本仅在联调需要固定 merchant_id/store_id 时使用。
-- =============================================

USE user_db;

-- 若已存在演示手机号对应的用户/商家，请先清理或改号后再执行
-- 以下示例：先插入商家用户，再插入 merchant + store

-- 商家用户（密码均为 123456 的 BCrypt，演示用）
-- 哈希需与当前环境 BCrypt 一致；更稳妥的方式是走入驻审核流程
INSERT INTO user (username, password_hash, phone, role, status)
SELECT '13800118800',
       '$2a$10$N.zmIv9.9Y8Q3qY8Y8Y8YuGqJ8Y8Y8Y8Y8Y8Y8Y8Y8Y8Y8Y8Y8Y8e',
       '13800118800',
       'MERCHANT',
       1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user WHERE phone = '13800118800');

-- 注意：上面示例哈希为占位，生产/演示请优先走「入驻审核」创建真实 BCrypt 密码。
-- 若仅需 product_db 演示商品（init.sql 已含 merchant_id=1, store_id=1），
-- 请在审核通过后保证对应 merchant/store 的 id 为 1，或修改商品表中的外键。
