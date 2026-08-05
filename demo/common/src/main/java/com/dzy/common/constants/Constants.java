package com.dzy.common.constants;

public final class Constants {

    private Constants() {}

    // ---------- 请求头 ----------
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USERNAME = "X-Username";
    public static final String HEADER_ROLE = "X-Role";
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    // ---------- 角色 ----------
    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ROLE_MERCHANT = "MERCHANT";
    public static final String ROLE_ADMIN = "ADMIN";

    // ---------- 商家状态 ----------
    public static final String MERCHANT_PENDING = "PENDING";
    public static final String MERCHANT_APPROVED = "APPROVED";
    public static final String MERCHANT_REJECTED = "REJECTED";
    public static final String MERCHANT_DISABLED = "DISABLED";

    // ---------- 商品状态 ----------
    public static final String PRODUCT_ON_SALE = "ON_SALE";
    public static final String PRODUCT_OFF_SALE = "OFF_SALE";
    public static final String PRODUCT_PLATFORM_OFF = "PLATFORM_OFF";

    public static final String SKU_ON = "ON";
    public static final String SKU_OFF = "OFF";

    // ---------- 库存变动类型 ----------
    public static final String STOCK_LOCK = "LOCK";
    public static final String STOCK_UNLOCK = "UNLOCK";
    public static final String STOCK_DEDUCT = "DEDUCT";
    public static final String STOCK_RESTORE = "RESTORE";
    public static final String STOCK_ADJUST = "ADJUST";

    // ---------- 订单状态 ----------
    public static final String ORDER_PENDING_PAY = "PENDING_PAY";
    public static final String ORDER_PAID = "PAID";
    public static final String ORDER_PICKING = "PICKING";
    public static final String ORDER_PICKED = "PICKED";
    public static final String ORDER_DELIVERING = "DELIVERING";
    public static final String ORDER_DELIVERED = "DELIVERED";
    public static final String ORDER_COMPLETED = "COMPLETED";
    public static final String ORDER_CANCELLED = "CANCELLED";
    public static final String ORDER_REFUNDED = "REFUNDED";

    // ---------- 支付 ----------
    public static final String PAY_PENDING = "PENDING";
    public static final String PAY_SUCCESS = "SUCCESS";
    public static final String PAY_FAILED = "FAILED";
    public static final String PAY_REFUNDED = "REFUNDED";
    public static final String CHANNEL_ALIPAY = "ALIPAY";
    public static final String CHANNEL_WECHAT = "WECHAT";

    // ---------- 配送状态 ----------
    public static final String DELIVERY_WAIT_PICK = "WAIT_PICK";
    public static final String DELIVERY_PICKING = "PICKING";
    public static final String DELIVERY_PICKED = "PICKED";
    public static final String DELIVERY_DELIVERING = "DELIVERING";
    public static final String DELIVERY_DELIVERED = "DELIVERED";

    // ---------- 评价 ----------
    public static final String REVIEW_VISIBLE = "VISIBLE";
    public static final String REVIEW_HIDDEN = "HIDDEN";

    // ---------- 待支付超时（毫秒）30 分钟 ----------
    public static final long ORDER_PAY_TIMEOUT_MS = 30 * 60 * 1000L;
}
