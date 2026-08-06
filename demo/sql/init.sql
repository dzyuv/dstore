-- ============================================================
-- 分布式电商平台初始化脚本
-- 数据库：user_db / product_db / order_db
-- 默认管理员：admin / admin123
-- ============================================================

CREATE DATABASE IF NOT EXISTS user_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS product_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS order_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- -------------------- user_db --------------------
USE user_db;

DROP TABLE IF EXISTS admin_operation_log;
DROP TABLE IF EXISTS sms_code;
DROP TABLE IF EXISTS address;
DROP TABLE IF EXISTS store;
DROP TABLE IF EXISTS merchant;
DROP TABLE IF EXISTS user;

CREATE TABLE user (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    username      VARCHAR(64)  NOT NULL,
    password_hash VARCHAR(128) NOT NULL,
    phone         VARCHAR(20)  NOT NULL,
    role          VARCHAR(20)  NOT NULL COMMENT 'CUSTOMER/MERCHANT/ADMIN',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_phone (phone),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE merchant (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id          BIGINT       NULL,
    merchant_no      VARCHAR(64)  NOT NULL,
    company_name     VARCHAR(128) NOT NULL,
    legal_person     VARCHAR(64)  NOT NULL,
    id_card          VARCHAR(32)  NOT NULL,
    business_license VARCHAR(255) NOT NULL,
    bank_account     VARCHAR(512) NOT NULL,
    phone            VARCHAR(20)  NOT NULL,
    status           VARCHAR(20)  NOT NULL COMMENT 'PENDING/APPROVED/REJECTED/DISABLED',
    audit_remark     VARCHAR(255) NULL,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_merchant_no (merchant_no),
    KEY idx_phone (phone),
    KEY idx_user_id (user_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE store (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    merchant_id    BIGINT       NOT NULL,
    store_name     VARCHAR(128) NOT NULL,
    logo           VARCHAR(255) NULL,
    address        VARCHAR(255) NOT NULL,
    phone          VARCHAR(20)  NOT NULL,
    business_hours VARCHAR(64)  NULL,
    status         TINYINT      NOT NULL DEFAULT 1 COMMENT '1营业 0休息',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_merchant (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE address (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL,
    receiver_name VARCHAR(64)  NOT NULL,
    phone         VARCHAR(20)  NOT NULL,
    province      VARCHAR(32)  NOT NULL,
    city          VARCHAR(32)  NOT NULL,
    district      VARCHAR(32)  NOT NULL,
    detail        VARCHAR(255) NOT NULL,
    is_default    TINYINT(1)   NOT NULL DEFAULT 0,
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sms_code (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone      VARCHAR(20) NOT NULL,
    code       VARCHAR(10) NOT NULL,
    scene      VARCHAR(20) NOT NULL DEFAULT 'REGISTER',
    expire_at  DATETIME    NOT NULL,
    used       TINYINT(1)  NOT NULL DEFAULT 0,
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_phone_scene (phone, scene)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE admin_operation_log (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id      BIGINT       NOT NULL,
    admin_name    VARCHAR(64)  NOT NULL,
    action_type   VARCHAR(64)  NOT NULL,
    target_type   VARCHAR(64)  NOT NULL,
    target_id     VARCHAR(64)  NULL,
    detail        VARCHAR(512) NULL,
    result        VARCHAR(32)  NOT NULL DEFAULT 'SUCCESS',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_admin (admin_id),
    KEY idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 管理员 admin / admin123 （BCrypt）
INSERT INTO user (username, password_hash, phone, role, status)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13800000000', 'ADMIN', 1);

-- -------------------- product_db --------------------
USE product_db;

DROP TABLE IF EXISTS stock_log;
DROP TABLE IF EXISTS cart_item;
DROP TABLE IF EXISTS product_sku;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS category;

CREATE TABLE category (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id   BIGINT       NOT NULL DEFAULT 0,
    name        VARCHAR(64)  NOT NULL,
    level       INT          NOT NULL DEFAULT 1,
    sort_order  INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id      BIGINT       NOT NULL,
    merchant_id   BIGINT       NOT NULL,
    category_id   BIGINT       NOT NULL,
    name          VARCHAR(128) NOT NULL,
    main_image    VARCHAR(255) NULL,
    detail        TEXT         NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'OFF_SALE'
                  COMMENT 'ON_SALE/OFF_SALE/PLATFORM_OFF',
    on_sale_time  DATETIME     NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_store (store_id),
    KEY idx_merchant (merchant_id),
    KEY idx_category (category_id),
    KEY idx_status_time (status, on_sale_time),
    KEY idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product_sku (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id     BIGINT         NOT NULL,
    sku_name       VARCHAR(128)   NOT NULL COMMENT '规格组合名称',
    price          DECIMAL(10,2)  NOT NULL,
    image          VARCHAR(255)   NULL,
    barcode        VARCHAR(64)    NULL,
    physical_stock INT            NOT NULL DEFAULT 0,
    locked_stock   INT            NOT NULL DEFAULT 0,
    status         VARCHAR(20)    NOT NULL DEFAULT 'ON'
                   COMMENT 'ON/OFF',
    created_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE stock_log (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    sku_id         BIGINT       NOT NULL,
    change_type    VARCHAR(32)  NOT NULL
                   COMMENT 'LOCK/UNLOCK/DEDUCT/RESTORE/ADJUST',
    change_qty     INT          NOT NULL,
    physical_after INT          NOT NULL,
    locked_after   INT          NOT NULL,
    biz_no         VARCHAR(64)  NULL,
    remark         VARCHAR(255) NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_sku (sku_id),
    KEY idx_biz (biz_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE cart_item (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    BIGINT  NOT NULL,
    store_id   BIGINT  NOT NULL,
    product_id BIGINT  NOT NULL,
    sku_id     BIGINT  NOT NULL,
    quantity   INT     NOT NULL DEFAULT 1,
    selected   TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_sku (user_id, sku_id),
    KEY idx_user_store (user_id, store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO category (id, parent_id, name, level, sort_order, status) VALUES
(1, 0, '食品饮料', 1, 1, 1),
(2, 0, '日用百货', 1, 2, 1),
(3, 1, '休闲零食', 2, 1, 1),
(4, 1, '饮料冲调', 2, 2, 1),
(5, 2, '清洁用品', 2, 1, 1);

-- 演示商品（merchant_id=1, store_id=1，需先在 user_db 创建对应商家与门店后使用）
-- 也可通过商家端 API 创建；以下数据便于联调消费者浏览
INSERT INTO product (id, store_id, merchant_id, category_id, name, main_image, detail, status, on_sale_time) VALUES
(1, 1, 1, 3, '薯片大礼包', 'https://via.placeholder.com/300x300?text=chips', '香脆可口，多口味组合', 'ON_SALE', NOW()),
(2, 1, 1, 4, '鲜榨果汁', 'https://via.placeholder.com/300x300?text=juice', '100% 鲜果压榨，冷藏保存', 'ON_SALE', NOW()),
(3, 1, 1, 5, '洗衣液 2L', 'https://via.placeholder.com/300x300?text=detergent', '强效去渍，温和不伤手', 'ON_SALE', NOW());

INSERT INTO product_sku (product_id, sku_name, price, image, barcode, physical_stock, locked_stock, status) VALUES
(1, '原味 500g', 19.90, NULL, 'SKU1001', 100, 0, 'ON'),
(1, '番茄味 500g', 21.90, NULL, 'SKU1002', 80, 0, 'ON'),
(1, '混合装 1kg', 35.00, NULL, 'SKU1003', 50, 0, 'ON'),
(2, '橙汁 1L', 12.50, NULL, 'SKU2001', 200, 0, 'ON'),
(2, '苹果汁 1L', 13.50, NULL, 'SKU2002', 150, 0, 'ON'),
(3, '薰衣草香 2L', 29.90, NULL, 'SKU3001', 60, 0, 'ON'),
(3, '清新柠檬 2L', 29.90, NULL, 'SKU3002', 60, 0, 'ON');

INSERT INTO stock_log (sku_id, change_type, change_qty, physical_after, locked_after, biz_no, remark) VALUES
(1, 'ADJUST', 100, 100, 0, 'INIT-1', '初始入库'),
(2, 'ADJUST', 80, 80, 0, 'INIT-2', '初始入库'),
(3, 'ADJUST', 50, 50, 0, 'INIT-3', '初始入库'),
(4, 'ADJUST', 200, 200, 0, 'INIT-4', '初始入库'),
(5, 'ADJUST', 150, 150, 0, 'INIT-5', '初始入库'),
(6, 'ADJUST', 60, 60, 0, 'INIT-6', '初始入库'),
(7, 'ADJUST', 60, 60, 0, 'INIT-7', '初始入库');

-- -------------------- order_db --------------------
USE order_db;

DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS delivery;
DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS orders;

CREATE TABLE orders (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no        VARCHAR(64)    NOT NULL,
    user_id         BIGINT         NOT NULL,
    store_id        BIGINT         NOT NULL,
    merchant_id     BIGINT         NOT NULL,
    address_id      BIGINT         NOT NULL,
    receiver_name   VARCHAR(64)    NOT NULL,
    receiver_phone  VARCHAR(20)    NOT NULL,
    receiver_addr   VARCHAR(512)   NOT NULL,
    delivery_time   VARCHAR(64)    NULL COMMENT '期望配送时间',
    total_amount    DECIMAL(12,2)  NOT NULL,
    status          VARCHAR(20)    NOT NULL
                    COMMENT 'PENDING_PAY/PAID/PICKING/PICKED/DELIVERING/DELIVERED/COMPLETED/CANCELLED/REFUNDED',
    pay_time        DATETIME       NULL,
    cancel_reason   VARCHAR(255)   NULL,
    expire_at       DATETIME       NULL COMMENT '待支付超时时间',
    created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user (user_id),
    KEY idx_store (store_id),
    KEY idx_status_expire (status, expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE order_item (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id     BIGINT         NOT NULL,
    order_no     VARCHAR(64)    NOT NULL,
    product_id   BIGINT         NOT NULL,
    sku_id       BIGINT         NOT NULL,
    product_name VARCHAR(128)   NOT NULL,
    sku_name     VARCHAR(128)   NOT NULL,
    sku_image    VARCHAR(255)   NULL,
    price        DECIMAL(10,2)  NOT NULL,
    quantity     INT            NOT NULL,
    amount       DECIMAL(12,2)  NOT NULL,
    KEY idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE payment (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_no    VARCHAR(64)    NOT NULL,
    order_no      VARCHAR(64)    NOT NULL,
    user_id       BIGINT         NOT NULL,
    amount        DECIMAL(12,2)  NOT NULL,
    channel       VARCHAR(20)    NOT NULL COMMENT 'ALIPAY/WECHAT',
    status        VARCHAR(20)    NOT NULL COMMENT 'PENDING/SUCCESS/FAILED/REFUNDED',
    pay_url       VARCHAR(512)   NULL,
    third_trade_no VARCHAR(128)  NULL,
    paid_at       DATETIME       NULL,
    created_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_payment_no (payment_no),
    KEY idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE delivery (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no        VARCHAR(64)  NOT NULL,
    store_id        BIGINT       NOT NULL,
    carrier         VARCHAR(64)  NULL COMMENT '配送商',
    tracking_no     VARCHAR(64)  NULL COMMENT '运单号',
    status          VARCHAR(20)  NOT NULL DEFAULT 'WAIT_PICK'
                    COMMENT 'WAIT_PICK/PICKING/PICKED/DELIVERING/DELIVERED',
    remark          VARCHAR(255) NULL,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE review (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no     VARCHAR(64)  NOT NULL,
    user_id      BIGINT       NOT NULL,
    product_id   BIGINT       NOT NULL,
    sku_id       BIGINT       NOT NULL,
    score        INT          NOT NULL COMMENT '1-5分',
    content      VARCHAR(1000) NULL,
    images       VARCHAR(1000) NULL COMMENT '图片URL，逗号分隔',
    status       VARCHAR(20)  NOT NULL DEFAULT 'VISIBLE' COMMENT 'VISIBLE/HIDDEN',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_product (order_no, product_id),
    KEY idx_product (product_id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
