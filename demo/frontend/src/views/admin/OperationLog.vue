<template>
  <div class="page">
    <div class="page-card" v-loading="loading">
      <div class="flex-between" style="margin-bottom: 16px">
        <h2 style="margin: 0">操作日志</h2>
        <div class="filters">
          <el-select
            v-model="query.actionType"
            clearable
            placeholder="操作类型"
            style="width: 180px"
            @change="onFilter"
          >
            <el-option label="审核商家" value="AUDIT_MERCHANT" />
            <el-option label="封禁商家" value="DISABLE_MERCHANT" />
            <el-option label="恢复商家" value="ENABLE_MERCHANT" />
            <el-option label="用户状态" value="UPDATE_USER_STATUS" />
          </el-select>
          <el-button @click="load">刷新</el-button>
        </div>
      </div>

      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="adminName" label="操作人" width="120" />
        <el-table-column label="操作类型" width="140">
          <template #default="{ row }">{{ actionText(row.actionType) }}</template>
        </el-table-column>
        <el-table-column prop="targetType" label="对象类型" width="110" />
        <el-table-column prop="targetId" label="对象ID" width="100" />
        <el-table-column prop="detail" label="详情" min-width="220" show-overflow-tooltip />
        <el-table-column prop="result" label="结果" width="90" />
        <el-table-column prop="createdAt" label="时间" min-width="160" />
      </el-table>

      <div class="pager" v-if="total > 0">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="query.size"
          :current-page="query.page"
          @current-change="onPage"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getOperationLogs } from '@/api/admin'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ actionType: undefined, page: 1, size: 10 })

function actionText(t) {
  const map = {
    AUDIT_MERCHANT: '审核商家',
    DISABLE_MERCHANT: '封禁商家',
    ENABLE_MERCHANT: '恢复商家',
    UPDATE_USER_STATUS: '用户状态'
  }
  return map[t] || t
}

async function load() {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.actionType) params.actionType = query.actionType
    const res = await getOperationLogs(params)
    list.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function onFilter() {
  query.page = 1
  load()
}

function onPage(p) {
  query.page = p
  load()
}

onMounted(load)
</script>

<style scoped>
.filters {
  display: flex;
  gap: 8px;
  align-items: center;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
