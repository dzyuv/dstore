<template>
  <div class="page apply-page">
    <div class="page-card apply-card">
      <h2 style="margin-top: 0">商家入驻申请</h2>
      <p class="text-muted tip">
        提交后由管理员审核。审核通过将为您创建商家账号（手机号即登录账号），初始密码会展示给管理员。
      </p>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px" style="max-width: 640px">
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="form.companyName" placeholder="营业执照上的公司全称" />
        </el-form-item>
        <el-form-item label="法人姓名" prop="legalPerson">
          <el-input v-model="form.legalPerson" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" maxlength="18" placeholder="18 位身份证号" />
        </el-form-item>
        <el-form-item label="营业执照" prop="businessLicense">
          <el-input v-model="form.businessLicense" placeholder="图片 URL 或证照编号（演示可填任意）" />
        </el-form-item>
        <el-form-item label="银行账户" prop="bankAccount">
          <el-input
            v-model="form.bankAccount"
            placeholder='如：工商银行 6222****1234'
          />
        </el-form-item>
        <el-form-item label="联系手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" placeholder="审核通过后作为商家登录账号" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSubmit">提交入驻申请</el-button>
          <el-button @click="$router.push('/')">返回首页</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { applyMerchant } from '@/api/merchant'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({
  companyName: '',
  legalPerson: '',
  idCard: '',
  businessLicense: '',
  bankAccount: '',
  phone: ''
})

const rules = {
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  legalPerson: [{ required: true, message: '请输入法人姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await applyMerchant({ ...form })
    ElMessage.success('入驻申请已提交，请等待管理员审核')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.apply-page {
  display: flex;
  justify-content: center;
  padding-top: 32px;
}
.apply-card {
  width: 100%;
  max-width: 720px;
}
.apply-card :deep(.el-form) {
  margin: 0 auto;
}
.tip {
  margin-bottom: 20px;
  line-height: 1.6;
}
</style>
