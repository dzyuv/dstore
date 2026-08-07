<template>
  <div class="page">
    <div class="page-card">
      <div class="flex-between" style="margin-bottom: 16px">
        <h2 style="margin: 0">商品监管</h2>
      </div>

      <el-form :inline="true">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" clearable placeholder="商品名称" @keyup.enter="load(1)" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 140px">
            <el-option label="在售" value="ON_SALE" />
            <el-option label="已下架" value="OFF_SALE" />
            <el-option label="平台下架" value="PLATFORM_OFF" />
          </el-select>
        </el-form-item>
        <el-form-item label="商家ID">
          <el-input v-model="query.merchantId" clearable placeholder="可选" style="width: 120px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load(1)">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商品名称" min-width="180" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="merchantId" label="商家ID" width="100" />
        <el-table-column prop="storeId" label="门店ID" width="100" />
        <el-table-column label="价格" width="140">
          <template #default="{ row }">
            ¥{{ formatPrice(row.minPrice) }}
            <span v-if="row.maxPrice != null && row.maxPrice !== row.minPrice">
              ~{{ formatPrice(row.maxPrice) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="totalAvailableStock" label="可用库存" width="100" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PLATFORM_OFF'"
              link
              type="primary"
              @click="onRestore(row)"
            >
              恢复
            </el-button>
            <el-button
              link
              type="danger"
              :disabled="row.status === 'PLATFORM_OFF'"
              @click="onPlatformOff(row)"
            >
              强制下架
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="query.size"
          :current-page="query.page"
          @current-change="load"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminGoodsList, platformOff, restoreFromPlatformOff } from '@/api/goods'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ keyword: '', status: '', merchantId: '', page: 1, size: 10 })

function formatPrice(v) {
  return v == null ? '0.00' : Number(v).toFixed(2)
}
function statusText(s) {
  return { ON_SALE: '在售', OFF_SALE: '已下架', PLATFORM_OFF: '平台下架' }[s] || s
}
function statusType(s) {
  return { ON_SALE: 'success', OFF_SALE: 'info', PLATFORM_OFF: 'danger' }[s] || 'info'
}

async function load(page = 1) {
  query.page = page
  loading.value = true
  try {
    const res = await adminGoodsList({
      keyword: query.keyword || undefined,
      status: query.status || undefined,
      merchantId: query.merchantId || undefined,
      page: query.page,
      size: query.size
    })
    list.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function onPlatformOff(row) {
  await ElMessageBox.confirm(
    `确认强制下架「${row.name}」？下架后商家不可自行上架。`,
    '平台监管',
    { type: 'warning' }
  )
  await platformOff(row.id)
  ElMessage.success('已强制下架')
  load(query.page)
}

async function onRestore(row) {
  await ElMessageBox.confirm(
    `确认恢复「${row.name}」？恢复后状态将变为"已下架"，商家可自行上架。`,
    '平台监管',
    { type: 'warning' }
  )
  await restoreFromPlatformOff(row.id)
  ElMessage.success('已恢复')
  load(query.page)
}

onMounted(() => load(1))
</script>

<style scoped>
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
