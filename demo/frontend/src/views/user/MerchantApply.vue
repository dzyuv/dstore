<template>
  <div class="page">
    <el-card class="page-card">
      <h2>商家入驻</h2>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="form.companyName" />
        </el-form-item>
        <el-form-item label="法人姓名" prop="legalPerson">
          <el-input v-model="form.legalPerson" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" />
        </el-form-item>
        <el-form-item label="营业执照" prop="businessLicense">
          <el-input v-model="form.businessLicense" />
        </el-form-item>
        <el-form-item label="银行账户" prop="bankAccount">
          <el-input v-model="form.bankAccount" placeholder="JSON 格式" />
        </el-form-item>
        <el-form-item label="联系手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-button type="primary" :loading="loading" @click="onSubmit">提交入驻申请</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { applyMerchant } from '@/api/merchant'

const form = reactive({
  companyName: '',
  legalPerson: '',
  idCard: '',
  businessLicense: '',
  bankAccount: '',
  phone: ''
})
const loading = ref(false)
const formRef = ref()

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await applyMerchant(form)
    ElMessage.success('入驻申请已提交，等待审核')
    // 刷新页面或跳转
  } finally {
    loading.value = false
  }
}
</script>
