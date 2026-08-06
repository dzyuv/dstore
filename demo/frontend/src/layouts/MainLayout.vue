<template>
  <el-container class="layout">
    <el-header class="header" height="64px">
      <div class="header-inner">
        <div class="brand" @click="$router.push('/')">
          <el-icon :size="22"><Shop /></el-icon>
          <span>DStore 商城</span>
        </div>
        <el-menu mode="horizontal" :ellipsis="false" :default-active="activeMenu" router class="nav">
          <el-menu-item index="/">商品首页</el-menu-item>
          <el-menu-item index="/cart">购物车</el-menu-item>
          <el-menu-item v-if="userStore.isLogin" index="/orders">我的订单</el-menu-item>
          <el-menu-item v-if="userStore.isLogin" index="/address">地址管理</el-menu-item>
          <!-- 商家入驻：未成为商家时始终可见 -->
          <el-menu-item v-if="!userStore.isMerchant" index="/merchant/apply">商家入驻</el-menu-item>
          <el-menu-item v-if="userStore.isMerchant || userStore.isAdmin" index="/merchant/products">
            商家商品
          </el-menu-item>
          <el-menu-item v-if="userStore.isMerchant" index="/merchant/delivery">
            配送管理
          </el-menu-item>
          <el-menu-item v-if="userStore.isAdmin" index="/admin/merchants">商家管理</el-menu-item>
          <el-menu-item v-if="userStore.isAdmin" index="/admin/users">用户管理</el-menu-item>
          <el-menu-item v-if="userStore.isAdmin" index="/admin/categories">分类管理</el-menu-item>
          <el-menu-item v-if="userStore.isAdmin" index="/admin/products">商品监管</el-menu-item>
          <el-menu-item v-if="userStore.isAdmin" index="/admin/logs">操作日志</el-menu-item>
        </el-menu>
        <div class="user-area">
          <template v-if="userStore.isLogin">
            <el-tag size="small" type="info">{{ roleLabel }}</el-tag>
            <span class="username">{{ userStore.user?.username || userStore.user?.phone }}</span>
            <el-button link type="danger" @click="onLogout">退出</el-button>
          </template>
          <template v-else>
            <el-button type="primary" link @click="$router.push('/login')">登录</el-button>
            <el-button link @click="$router.push('/register')">注册</el-button>
            <el-button link type="warning" @click="$router.push('/merchant/apply')">商家入驻</el-button>
          </template>
        </div>
      </div>
    </el-header>
    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => {
  const p = route.path
  if (p.startsWith('/merchant/apply')) return '/merchant/apply'
  if (p.startsWith('/merchant/products')) return '/merchant/products'
  if (p.startsWith('/merchant/stores')) return '/merchant/stores'
  if (p.startsWith('/admin/merchants')) return '/admin/merchants'
  if (p.startsWith('/admin/users')) return '/admin/users'
  if (p.startsWith('/admin/categories')) return '/admin/categories'
  if (p.startsWith('/admin/products')) return '/admin/products'
  if (p.startsWith('/admin/logs')) return '/admin/logs'
  if (p.startsWith('/orders')) return p.startsWith('/orders/checkout') ? '/cart' : '/orders'
  if (p.startsWith('/address')) return '/address'
  if (p.startsWith('/cart')) return '/cart'
  if (p.startsWith('/goods')) return '/'
  return p
})

const roleLabel = computed(() => {
  const map = { CUSTOMER: '消费者', MERCHANT: '商家', ADMIN: '管理员' }
  return map[userStore.role] || userStore.role || '用户'
})

function onLogout() {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  if (userStore.isLogin && !userStore.user) {
    userStore.fetchMe()
  }
})
</script>

<style scoped>
.layout {
  min-height: 100vh;
}
.header {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 12px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 18px;
  color: #409eff;
  cursor: pointer;
  white-space: nowrap;
}
.nav {
  flex: 1;
  border-bottom: none;
  min-width: 0;
}
.user-area {
  display: flex;
  align-items: center;
  gap: 10px;
  white-space: nowrap;
}
.username {
  font-size: 14px;
  color: #606266;
}
.main {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 0;
}
</style>
