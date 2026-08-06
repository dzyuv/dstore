-- ============================================================
-- 测试数据脚本（在 init.sql 之后执行）
-- 所有测试用户密码均为 admin123
-- ============================================================

-- ==================== user_db ====================
USE user_db;

-- 商家用户
INSERT INTO user (id, username, password_hash, phone, role, status) VALUES
(2, 'merchant1', '$2a$10$5r6R0Hn9asU5B7Qyq2aH9erxgn1wFunAJ5vfVZlWc7LxiFvOvrcwq', '13800000001', 'MERCHANT', 1),
(3, 'merchant2', '$2a$10$5r6R0Hn9asU5B7Qyq2aH9erxgn1wFunAJ5vfVZlWc7LxiFvOvrcwq', '13800000002', 'MERCHANT', 1);

-- 消费者用户
INSERT INTO user (id, username, password_hash, phone, role, status) VALUES
(4, 'customer1', '$2a$10$5r6R0Hn9asU5B7Qyq2aH9erxgn1wFunAJ5vfVZlWc7LxiFvOvrcwq', '13800000003', 'CUSTOMER', 1),
(5, 'customer2', '$2a$10$5r6R0Hn9asU5B7Qyq2aH9erxgn1wFunAJ5vfVZlWc7LxiFvOvrcwq', '13800000004', 'CUSTOMER', 1);

-- 已审核通过的商家
INSERT INTO merchant (id, user_id, merchant_no, company_name, legal_person, id_card, business_license, bank_account, phone, status) VALUES
(1, 2, 'M20240001', '测试商家一号', '张三', '110101199001011234', 'BL-2024-0001', '6222021234567890123', '13800000001', 'APPROVED'),
(2, 3, 'M20240002', '测试商家二号', '李四', '110101199002022345', 'BL-2024-0002', '6222029876543210987', '13800000002', 'APPROVED');

-- 门店
INSERT INTO store (id, merchant_id, store_name, logo, address, phone, business_hours, status) VALUES
(1, 1, '一号旗舰店', NULL, '北京市朝阳区建国路88号', '010-88881001', '09:00-21:00', 1),
(2, 1, '一号折扣店', NULL, '北京市海淀区中关村大街1号', '010-88881002', '10:00-20:00', 1),
(3, 2, '二号优选店', NULL, '上海市浦东新区陆家嘴环路100号', '021-66662001', '08:00-22:00', 1);

-- 消费者收货地址
INSERT INTO address (id, user_id, receiver_name, phone, province, city, district, detail, is_default) VALUES
(1, 4, '王小明', '13800000003', '北京市', '北京市', '朝阳区', '望京SOHO T1 1206', 1),
(2, 4, '王小明', '13800000003', '北京市', '北京市', '海淀区', '五道口华联超市旁', 0),
(3, 5, '赵小红', '13800000004', '上海市', '上海市', '浦东新区', '张江高科技园区1号楼', 1);

-- ==================== product_db ====================
USE product_db;

-- 补充商品数据（init.sql 已有 3 个商品，这里追加更多）
INSERT INTO product (id, store_id, merchant_id, category_id, name, main_image, detail, status, on_sale_time) VALUES
(4, 1, 1, 3, '坚果混合装 500g', 'https://via.placeholder.com/300x300?text=nuts', '每日坚果，混合6种果仁', 'ON_SALE', NOW()),
(5, 1, 1, 4, '冷萃咖啡液 10条', 'https://via.placeholder.com/300x300?text=coffee', '阿拉比卡豆冷萃，即溶即饮', 'ON_SALE', NOW()),
(6, 2, 1, 3, '进口巧克力礼盒', 'https://via.placeholder.com/300x300?text=chocolate', '比利时进口，节日送礼首选', 'ON_SALE', NOW()),
(7, 3, 2, 5, '厨房湿巾 80抽', 'https://via.placeholder.com/300x300?text=wipes', '去油污免洗，一擦即净', 'ON_SALE', NOW()),
(8, 3, 2, 5, '洗洁精 1L', 'https://via.placeholder.com/300x300?text=dishwash', '天然植物配方，温和不伤手', 'ON_SALE', NOW()),
(9, 3, 2, 4, '蜂蜜柚子茶 500g', 'https://via.placeholder.com/300x300?text=tea', '韩国进口，酸甜可口', 'ON_SALE', NOW()),
(10, 1, 1, 3, '海苔夹心脆 200g', 'https://via.placeholder.com/300x300?text=seaweed', '香脆海苔，芝麻夹心', 'OFF_SALE', NULL);

-- SKU
INSERT INTO product_sku (id, product_id, sku_name, price, barcode, physical_stock, locked_stock, status) VALUES
-- 坚果混合装
(8,  4, '每日坚果 500g',     49.90, 'NUT500',  200, 0, 'ON'),
(9,  4, '每日坚果 1kg',      89.00, 'NUT1KG',  100, 0, 'ON'),
-- 冷萃咖啡液
(10, 5, '原味 10条装',       39.90, 'COF10',   300, 0, 'ON'),
(11, 5, '榛果味 10条装',     42.00, 'COF10H',  150, 0, 'ON'),
(12, 5, '焦糖味 10条装',     42.00, 'COF10C',  120, 0, 'ON'),
-- 巧克力礼盒
(13, 6, '经典黑巧 12粒',     128.00, 'CHOC12',  50, 0, 'ON'),
(14, 6, '混合口味 24粒',     238.00, 'CHOC24',  30, 0, 'ON'),
-- 厨房湿巾
(15, 7, '柠檬清香 80抽*3包',  29.90, 'WIPE3',   500, 0, 'ON'),
(16, 7, '柠檬清香 80抽*6包',  49.90, 'WIPE6',   300, 0, 'ON'),
-- 洗洁精
(17, 8, '柠檬味 1L',         15.90, 'DW1LL',   400, 0, 'ON'),
(18, 8, '青苹果味 1L',       16.90, 'DW1LG',   350, 0, 'ON'),
-- 蜂蜜柚子茶
(19, 9, '原味 500g',         35.00, 'TEA500',  180, 0, 'ON'),
(20, 9, '生姜味 500g',       38.00, 'TEA500G', 100, 0, 'ON'),
-- 海苔夹心脆（已下架商品）
(21, 10, '芝麻夹心 200g',    25.00, 'SW200',    80, 0, 'OFF');

-- 库存流水
INSERT INTO stock_log (sku_id, change_type, change_qty, physical_after, locked_after, biz_no, remark) VALUES
(8,  'ADJUST', 200, 200, 0, 'INIT-8',  '初始入库-坚果500g'),
(9,  'ADJUST', 100, 100, 0, 'INIT-9',  '初始入库-坚果1kg'),
(10, 'ADJUST', 300, 300, 0, 'INIT-10', '初始入库-咖啡原味'),
(11, 'ADJUST', 150, 150, 0, 'INIT-11', '初始入库-咖啡榛果'),
(12, 'ADJUST', 120, 120, 0, 'INIT-12', '初始入库-咖啡焦糖'),
(13, 'ADJUST', 50,  50,  0, 'INIT-13', '初始入库-巧克力12粒'),
(14, 'ADJUST', 30,  30,  0, 'INIT-14', '初始入库-巧克力24粒'),
(15, 'ADJUST', 500, 500, 0, 'INIT-15', '初始入库-湿巾3包'),
(16, 'ADJUST', 300, 300, 0, 'INIT-16', '初始入库-湿巾6包'),
(17, 'ADJUST', 400, 400, 0, 'INIT-17', '初始入库-洗洁精柠檬'),
(18, 'ADJUST', 350, 350, 0, 'INIT-18', '初始入库-洗洁精青苹果'),
(19, 'ADJUST', 180, 180, 0, 'INIT-19', '初始入库-柚子茶原味'),
(20, 'ADJUST', 100, 100, 0, 'INIT-20', '初始入库-柚子茶生姜'),
(21, 'ADJUST', 80,  80,  0, 'INIT-21', '初始入库-海苔');

-- ==================== order_db ====================
USE order_db;

-- 订单：各种状态便于联调测试
INSERT INTO orders (id, order_no, user_id, store_id, merchant_id, address_id, receiver_name, receiver_phone, receiver_addr, total_amount, status, pay_time, expire_at) VALUES
-- customer1 的订单
(1, 'ORD20240801001', 4, 1, 1, 1, '王小明', '13800000003', '北京市朝阳区望京SOHO T1 1206', 69.80, 'COMPLETED',  '2024-08-01 10:30:00', NULL),
(2, 'ORD20240802001', 4, 1, 1, 1, '王小明', '13800000003', '北京市朝阳区望京SOHO T1 1206', 89.00, 'PAID',       '2024-08-02 14:00:00', NULL),
(3, 'ORD20240803001', 4, 3, 2, 1, '王小明', '13800000003', '北京市朝阳区望京SOHO T1 1206', 64.80, 'PENDING_PAY', NULL, '2025-12-31 23:59:59'),
-- customer2 的订单
(4, 'ORD20240804001', 5, 3, 2, 3, '赵小红', '13800000004', '上海市浦东新区张江高科技园区1号楼', 128.00, 'DELIVERING', '2024-08-04 09:15:00', NULL),
(5, 'ORD20240805001', 5, 1, 1, 3, '赵小红', '13800000004', '上海市浦东新区张江高科技园区1号楼', 207.90, 'CANCELLED',  NULL, '2024-08-05 12:00:00');

-- 订单明细
INSERT INTO order_item (id, order_id, order_no, product_id, sku_id, product_name, sku_name, sku_image, price, quantity, amount) VALUES
-- ORD20240801001: 薯片大礼包原味 + 鲜榨果汁橙汁 (已完成)
(1, 1, 'ORD20240801001', 1, 1, '薯片大礼包',  '原味 500g', NULL, 19.90, 2, 39.80),
(2, 1, 'ORD20240801001', 2, 4, '鲜榨果汁',    '橙汁 1L',   NULL, 12.50, 2, 25.00),
-- ORD20240802001: 坚果1kg (已支付)
(3, 2, 'ORD20240802001', 4, 9, '坚果混合装 500g', '每日坚果 1kg', NULL, 89.00, 1, 89.00),
-- ORD20240803001: 厨房湿巾3包 + 洗洁精柠檬 (待支付)
(4, 3, 'ORD20240803001', 7, 15, '厨房湿巾 80抽', '柠檬清香 80抽*3包', NULL, 29.90, 1, 29.90),
(5, 3, 'ORD20240803001', 8, 17, '洗洁精 1L',   '柠檬味 1L',        NULL, 15.90, 2, 34.90),
-- ORD20240804001: 巧克力12粒 (配送中)
(6, 4, 'ORD20240804001', 6, 13, '进口巧克力礼盒', '经典黑巧 12粒', NULL, 128.00, 1, 128.00),
-- ORD20240805001: 冷萃原味 + 坚果500g (已取消)
(7, 5, 'ORD20240805001', 5, 10, '冷萃咖啡液 10条', '原味 10条装',  NULL, 39.90, 2, 79.80),
(8, 5, 'ORD20240805001', 4, 1, '薯片大礼包',      '原味 500g',   NULL, 19.90, 3, 59.70);

-- 支付单
INSERT INTO payment (id, payment_no, order_no, user_id, amount, channel, status, third_trade_no, paid_at) VALUES
(1, 'PAY20240801001', 'ORD20240801001', 4, 69.80, 'WECHAT', 'SUCCESS', 'WX-20240801-001', '2024-08-01 10:30:00'),
(2, 'PAY20240802001', 'ORD20240802001', 4, 89.00, 'ALIPAY', 'SUCCESS', 'ALI-20240802-001', '2024-08-02 14:00:00'),
(3, 'PAY20240803001', 'ORD20240803001', 4, 64.80, 'WECHAT', 'PENDING', NULL, NULL),
(4, 'PAY20240804001', 'ORD20240804001', 5, 128.00, 'ALIPAY', 'SUCCESS', 'ALI-20240804-001', '2024-08-04 09:15:00');

-- 配送单
INSERT INTO delivery (id, order_no, store_id, carrier, tracking_no, status) VALUES
(1, 'ORD20240801001', 1, '顺丰速运', 'SF1234567890', 'DELIVERED'),
(2, 'ORD20240804001', 3, '京东物流', 'JD9876543210', 'DELIVERING');

-- 评价
INSERT INTO review (id, order_no, user_id, product_id, sku_id, score, content, status) VALUES
(1, 'ORD20240801001', 4, 1, 1, 5, '薯片很脆很好吃，会回购！', 'VISIBLE'),
(2, 'ORD20240801001', 4, 2, 4, 4, '橙汁味道不错，就是有点甜', 'VISIBLE');

-- ============================================================
-- 快速测试账号
-- ============================================================
-- 管理员  : admin    / admin123  (init.sql 已创建)
-- 商家1   : merchant1 / admin123  (门店: 一号旗舰店、一号折扣店)
-- 商家2   : merchant2 / admin123  (门店: 二号优选店)
-- 消费者1 : customer1 / admin123  (收货地址: 北京朝阳)
-- 消费者2 : customer2 / admin123  (收货地址: 上海浦东)
-- ============================================================
