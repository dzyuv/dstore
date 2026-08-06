<template>
  <div class="page">
    <div class="page-card" v-loading="loading">
      <div class="flex-between" style="margin-bottom: 16px">
        <h2 style="margin: 0">用户管理</h2>
        <div class="filters">
          <el-select v-model="query.role" clearable placeholder="角色" style="width: 120px" @change="load">
            <el-option label="消费者" value="CUSTOMER" />
            <el-option label="商家" value="MERCHANT" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
          <el-select v-model="query.status" clearable placeholder="状态" style="width: 110px" @change="load">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
          <el-button @click="load">刷新</el-button>
        </div>
      </div>

      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">{{ roleText(row.role) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" min-width="160" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <template v-if="row.role !== 'ADMIN'">
              <el-button
                v-if="row.status === 1"
                link
                type="danger"
                @click="onToggle(row, 0)"
              >
                禁用
              </el-button>
              <el-button v-else link type="success" @click="onToggle(row, 1)">启用</el-button>
            </template>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsers, updateUserStatus } from '@/api/admin'

const list = ref([])
const loading = ref(false)
const query = reactive({ role: undefined, status: undefined })

function roleText(role) {
  return { CUSTOMER: '消费者', MERCHANT: '商家', ADMIN: '管理员' }[role] || role
}

async function load() {
  loading.value = true
  try {
    const params = {}
    if (query.role) params.role = query.role
    if (query.status !== undefined && query.status !== null && query.status !== '') {
      params.status = query.status
    }
    const res = await getUsers(params)
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function onToggle(row, status) {
  const action = status === 1 ? '启用' : '禁用'
  await ElMessageBox.confirm(`确认${action}用户「${row.username}」？`, '用户状态')
  await updateUserStatus({ userId: row.id, status })
  ElMessage.success(`已${action}`)
  await load()
}

onMounted(load)
</script>

<style scoped>
.filters {
  display: flex;
  gap: 8px;
  align-items: center;
}
.text-muted {
  color: #909399;
}
</style>
