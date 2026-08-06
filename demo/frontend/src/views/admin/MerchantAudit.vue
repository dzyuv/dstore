<template>
  <div class="page">
    <div class="page-card" v-loading="loading">
      <div class="flex-between" style="margin-bottom: 16px">
        <h2 style="margin: 0">商家管理</h2>
        <div class="filters">
          <el-radio-group v-model="tab" @change="load">
            <el-radio-button label="PENDING">待审核</el-radio-button>
            <el-radio-button label="ALL">全部</el-radio-button>
            <el-radio-button label="APPROVED">已通过</el-radio-button>
            <el-radio-button label="DISABLED">已封禁</el-radio-button>
            <el-radio-button label="REJECTED">已驳回</el-radio-button>
          </el-radio-group>
          <el-button @click="load">刷新</el-button>
        </div>
      </div>

      <el-empty v-if="!list.length && !loading" description="暂无数据" />

      <el-table v-else :data="list" stripe>
        <el-table-column prop="merchantNo" label="商家编号" width="160" />
        <el-table-column prop="companyName" label="公司名称" min-width="160" />
        <el-table-column prop="legalPerson" label="法人" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="idCard" label="身份证" width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditRemark" label="审核备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button type="primary" size="small" @click="onAudit(row, true)">通过</el-button>
              <el-button type="danger" size="small" @click="onAudit(row, false)">驳回</el-button>
            </template>
            <template v-else-if="row.status === 'APPROVED'">
              <el-button type="danger" size="small" plain @click="onStatus(row, 'DISABLED')">
                封禁
              </el-button>
            </template>
            <template v-else-if="row.status === 'DISABLED'">
              <el-button type="success" size="small" plain @click="onStatus(row, 'APPROVED')">
                恢复
              </el-button>
            </template>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getPendingMerchants,
  getMerchants,
  auditMerchant,
  updateMerchantStatus
} from '@/api/admin'

const list = ref([])
const loading = ref(false)
const tab = ref('PENDING')

function statusText(s) {
  const map = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回', DISABLED: '已封禁' }
  return map[s] || s
}

function statusType(s) {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'info', DISABLED: 'danger' }
  return map[s] || 'info'
}

async function load() {
  loading.value = true
  try {
    if (tab.value === 'PENDING') {
      const res = await getPendingMerchants()
      list.value = res.data || []
    } else if (tab.value === 'ALL') {
      const res = await getMerchants()
      list.value = res.data || []
    } else {
      const res = await getMerchants({ status: tab.value })
      list.value = res.data || []
    }
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

async function onStatus(row, status) {
  if (status === 'DISABLED') {
    await ElMessageBox.confirm(
      `封禁后商家无法登录，其商品将自动下架。确认封禁「${row.companyName}」？`,
      '封禁商家',
      { type: 'warning' }
    )
  } else {
    await ElMessageBox.confirm(
      `恢复后商家可登录，但商品需手动重新上架。确认恢复「${row.companyName}」？`,
      '恢复商家'
    )
  }
  await updateMerchantStatus({ merchantId: row.id, status })
  ElMessage.success(status === 'DISABLED' ? '已封禁' : '已恢复')
  await load()
}

onMounted(load)
</script>

<style scoped>
.filters {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}
.text-muted {
  color: #909399;
}
</style>
