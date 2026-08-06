<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2>登录 DStore</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @submit.prevent>
        <el-form-item label="账号（用户名/手机号）" prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button type="primary" class="full" :loading="loading" @click="onSubmit">登录</el-button>
      </el-form>
      <div class="footer">
        还没有账号？
        <router-link to="/register">去注册</router-link>
        <span class="tip">演示管理员可用 admin / admin123（需数据库初始化成功）</span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.replace(route.query.redirect || '/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f3ff, #f5f6f8 50%, #fff7e6);
}
.auth-card {
  width: 420px;
  border-radius: 16px;
}
h2 {
  margin: 0 0 20px;
  text-align: center;
}
.full {
  width: 100%;
}
.footer {
  margin-top: 16px;
  text-align: center;
  color: #606266;
  font-size: 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.tip {
  color: #909399;
  font-size: 12px;
}
a {
  color: #409eff;
}
</style>
