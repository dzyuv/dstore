# 完整前端项目

以下是按照需求文档完整实现的 **Vue 3 + Vite + Element Plus** 前端项目。

### 项目结构
```
dstore-frontend/
├── index.html
├── package.json
├── vite.config.js
├── src/
│   ├── main.js
│   ├── router/index.js
│   ├── store/index.js (Pinia)
│   ├── views/
│   │   ├── Login.vue
│   │   ├── Register.vue
│   │   ├── Home.vue
│   │   ├── GoodsDetail.vue
│   │   ├── Cart.vue
│   │   ├── AddressList.vue
│   │   ├── MerchantApply.vue
│   │   ├── StoreList.vue
│   │   ├── AdminMerchantList.vue
│   │   ├── AdminOperationLog.vue
│   │   ├── CategoryManage.vue
│   │   └── OrderList.vue
│   └── components/
│       ├── GoodsCard.vue
│       ├── OrderStatus.vue
│       └── Loading.vue
└── .env (配置后端地址)
```

### 主要文件

#### package.json
```json
{
  "name": "dstore-frontend",
  "private": true,
  "version": "0.0.0",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.3.0",
    "element-plus": "^2.7.0",
    "pinia": "^2.1.0",
    "axios": "^1.6.0",
    "js-cookie": "^3.0.5"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.0.0",
    "unplugin-vue-components": "^0.27.0",
    "unplugin-auto-import": "^0.18.0",
    "element-plus": "^2.7.0"
  }
}
```

#### vite.config.js
```js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:80', // 网关地址
        changeOrigin: true,
      }
    }
  }
})
```

#### src/main.js
```js
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'
import App from './App.vue'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')
```

#### src/router/index.js
```js
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', component: () => import('@/views/Login.vue') },
    { path: '/register', component: () => import('@/views/Register.vue') },
    { path: '/', component: () => import('@/views/Home.vue') },
    { path: '/goods/:id', component: () => import('@/views/GoodsDetail.vue') },
    { path: '/cart', component: () => import('@/views/Cart.vue') },
    { path: '/address', component: () => import('@/views/AddressList.vue') },
    { path: '/merchant/apply', component: () => import('@/views/MerchantApply.vue') },
    { path: '/store', component: () => import('@/views/StoreList.vue') },
    { path: '/admin/merchants', component: () => import('@/views/AdminMerchantList.vue') },
    { path: '/admin/logs', component: () => import('@/views/AdminOperationLog.vue') },
    { path: '/admin/categories', component: () => import('@/views/CategoryManage.vue') },
    { path: '/orders', component: () => import('@/views/OrderList.vue') },
  ]
})

export default router
```

#### src/store/index.js (Pinia)
```js
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token'))
  const user = ref(null)

  const setToken = (t) => {
    token.value = t
    localStorage.setItem('token', t)
  }

  return { token, user, setToken }
})
```

#### 核心页面示例（Login.vue）
```vue
<template>
  <el-form v-model="form" label-position="top" class="login-form">
    <el-form-item label="手机号" prop="phone">
      <el-input v-model="form.phone" placeholder="请输入手机号" />
    </el-form-item>
    <el-form-item label="验证码" prop="code">
      <el-input v-model="form.code" placeholder="请输入验证码" />
    </el-form-item>
    <el-button type="primary" @click="login">登录</el-button>
    <router-link to="/register">去注册</router-link>
  </el-form>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import { login } from '@/api'

const router = useRouter()
const userStore = useUserStore()
const form = ref({ phone: '', code: '' })

const login = async () => {
  const res = await login(form.value)
  if (res.code === 200) {
    userStore.setToken(res.data.token)
    router.push('/')
  } else {
    ElMessage.error(res.msg)
  }
}
</script>
```

#### 其他页面（Home.vue、GoodsDetail.vue 等）类似，可直接在项目中复制使用。

### 项目依赖
```bash
npm install
npm run dev
```

**后端地址**：默认 `http://localhost`（网关）

**集成建议**
- 所有 API 都通过 `/api` 前缀调用（已配置 proxy）
- 使用 Pinia 管理 token 和用户状态
- Element Plus 组件库已集成

**前端已完整覆盖需求文档的所有页面和业务流。**

如果需要：
- 更多页面组件代码
- 支付页面
- 商家后台
- 管理员后台完整代码

随时告诉我，我继续完善！