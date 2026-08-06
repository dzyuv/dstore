-- 商品模块演示数据（可在已有 product_db 上单独执行）
-- 前提：user_db 中存在 merchant id=1 且 store id=1（审核通过的商家及门店）

USE product_db;

-- 若已有同 id 数据可先清理
DELETE FROM stock_log WHERE sku_id IN (SELECT id FROM product_sku WHERE product_id IN (1,2,3));
DELETE FROM cart_item WHERE product_id IN (1,2,3);
DELETE FROM product_sku WHERE product_id IN (1,2,3);
DELETE FROM product WHERE id IN (1,2,3);

INSERT INTO product (id, store_id, merchant_id, category_id, name, main_image, detail, status, on_sale_time) VALUES
(1, 1, 1, 3, '薯片大礼包', 'https://via.placeholder.com/300x300?text=chips', '香脆可口，多口味组合', 'ON_SALE', NOW()),
(2, 1, 1, 4, '鲜榨果汁', 'https://via.placeholder.com/300x300?text=juice', '100% 鲜果压榨，冷藏保存', 'ON_SALE', NOW()),
(3, 1, 1, 5, '洗衣液 2L', 'https://via.placeholder.com/300x300?text=detergent', '强效去渍，温和不伤手', 'ON_SALE', NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), status=VALUES(status);

INSERT INTO product_sku (product_id, sku_name, price, barcode, physical_stock, locked_stock, status) VALUES
(1, '原味 500g', 19.90, 'SKU1001', 100, 0, 'ON'),
(1, '番茄味 500g', 21.90, 'SKU1002', 80, 0, 'ON'),
(1, '混合装 1kg', 35.00, 'SKU1003', 50, 0, 'ON'),
(2, '橙汁 1L', 12.50, 'SKU2001', 200, 0, 'ON'),
(2, '苹果汁 1L', 13.50, 'SKU2002', 150, 0, 'ON'),
(3, '薰衣草香 2L', 29.90, 'SKU3001', 60, 0, 'ON'),
(3, '清新柠檬 2L', 29.90, 'SKU3002', 60, 0, 'ON');
