<template>
  <div class="page">
    <div class="page-card" v-loading="loading">
      <div class="flex-between" style="margin-bottom: 16px">
        <h2 style="margin: 0">商家入驻审核</h2>
        <el-button @click="load">刷新</el-button>
      </div>

      <el-empty v-if="!list.length && !loading" description="暂无待审核申请" />

      <el-table v-else :data="list" stripe>
        <el-table-column prop="merchantNo" label="申请编号" width="160" />
        <el-table-column prop="companyName" label="公司名称" min-width="160" />
        <el-table-column prop="legalPerson" label="法人" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="idCard" label="身份证" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PENDING' ? 'warning' : 'info'" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button type="primary" size="small" @click="onAudit(row, true)">通过</el-button>
              <el-button type="danger" size="small" @click="onAudit(row, false)">驳回</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPendingMerchants, auditMerchant } from '@/api/admin'

const list = ref([])
const loading = ref(false)

function statusText(s) {
  const map = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回', DISABLED: '已禁用' }
  return map[s] || s
}

async function load() {
  loading.value = true
  try {
    const res = await getPendingMerchants()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function onAudit(row, approved) {
  let remark = ''
  if (approved) {
    await ElMessageBox.confirm(`确认通过「${row.companyName}」的入驻申请？`, '审核通过')
  } else {
    const { value } = await ElMessageBox.prompt('请填写驳回原因', '审核驳回', {
      inputPlaceholder: '驳回原因（必填）',
      inputValidator: (v) => !!(v && v.trim()) || '请填写驳回原因'
    })
    remark = value
  }

  const res = await auditMerchant({
    merchantId: row.id,
    approved,
    remark: remark || undefined
  })

  if (approved && res.data?.initialPassword) {
    await ElMessageBox.alert(
      `审核通过！\n登录账号：${res.data.loginAccount}\n初始密码：${res.data.initialPassword}\n请通知商家登录后修改密码。`,
      '商家账号已创建',
      { confirmButtonText: '知道了' }
    )
  } else {
    ElMessage.success(approved ? '已通过' : '已驳回')
  }
  await load()
}

onMounted(load)
</script>
