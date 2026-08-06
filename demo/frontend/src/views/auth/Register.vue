<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2>用户注册</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" placeholder="11 位手机号" />
        </el-form-item>
        <el-form-item label="验证码" prop="smsCode">
          <div class="code-row">
            <el-input v-model="form.smsCode" placeholder="验证码，演示可用 123456" />
            <el-button :disabled="countdown > 0" @click="onSendSms">
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="6-30 位密码" />
        </el-form-item>
        <el-button type="primary" class="full" :loading="loading" @click="onSubmit">注册</el-button>
      </el-form>
      <div class="footer">
        已有账号？
        <router-link to="/login">去登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register, sendSms } from '@/api/user'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const countdown = ref(0)
const form = reactive({ phone: '', smsCode: '', password: '', role: 'CUSTOMER' })
const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  smsCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度 6-30 位', trigger: 'blur' }
  ]
}

async function onSendSms() {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请先填写正确手机号')
    return
  }
  const res = await sendSms({ phone: form.phone, scene: 'REGISTER' })
  ElMessage.success(`验证码已发送${res.data?.demoCode ? '，演示码：' + res.data.demoCode : ''}`)
  countdown.value = 60
  const t = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) clearInterval(t)
  }, 1000)
}

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
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
.full { width: 100%; }
.code-row {
  display: flex;
  gap: 8px;
  width: 100%;
}
.footer {
  margin-top: 16px;
  text-align: center;
  color: #606266;
}
a { color: #409eff; }
</style>
