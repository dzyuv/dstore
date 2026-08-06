<template>
  <div class="page">
    <el-card class="page-card">
      <h2>商家审核</h2>
      <el-table :data="list" stripe>
        <el-table-column prop="companyName" label="公司名称" />
        <el-table-column prop="legalPerson" label="法人" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PENDING' ? 'warning' : 'success'">
              {{ row.status === 'PENDING' ? '待审核' : row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="auditMerchant(row, true)">通过</el-button>
            <el-button type="danger" size="small" @click="auditMerchant(row, false)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPendingMerchants, auditMerchant } from '@/api/admin'

const list = ref([])
const loading = ref(false)

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await getPendingMerchants()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function auditMerchant(row, approved) {
  await ElMessageBox.confirm(`确认${approved ? '通过' : '驳回'}该商家入驻申请？`, '提示')
  await auditMerchant({
    merchantId: row.id,
    approved,
    remark: approved ? '' : '审核不通过'
  })
  ElMessage.success('操作成功')
  await load()
}
</script>
