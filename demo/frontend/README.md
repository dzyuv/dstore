# DStore 前端

Vue 3 + Vite + Element Plus + Pinia。

## 启动

```bash
cd frontend
npm install
npm run dev
```

开发代理：`/api` → 网关 `http://localhost:80`（见 `vite.config.js`）。

## 页面覆盖（对应需求分析）

| 角色 | 功能 |
|------|------|
| 消费者 | 注册/登录、地址、浏览/搜索、购物车、下单、支付、物流、评价 |
| 商家 | 入驻申请、门店 CRUD、商品/SKU/库存、上下架 |
| 管理员 | 商家审核/封禁恢复、用户启禁用、分类、商品监管、操作日志 |

演示管理员：`admin` / `admin123`（user-service 启动时自动校正密码）。
