<template>
  <div class="page">
    <div class="page-card">
      <div class="flex-between" style="margin-bottom: 16px">
        <h2 style="margin: 0">商家商品管理</h2>
        <el-button type="primary" @click="$router.push('/merchant/products/create')">
          <el-icon><Plus /></el-icon> 发布商品
        </el-button>
      </div>

      <el-form :inline="true" @submit.prevent>
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
        <el-form-item>
          <el-button type="primary" @click="load(1)">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="商品" min-width="220">
          <template #default="{ row }">
            <div class="goods-cell">
              <img :src="row.mainImage || placeholder" class="thumb" @error="(e) => (e.target.src = placeholder)" />
              <div>
                <div class="name">{{ row.name }}</div>
                <div class="text-muted">ID: {{ row.id }} · {{ row.categoryName || '-' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="140">
          <template #default="{ row }">
            <span class="price">¥{{ formatPrice(row.minPrice) }}</span>
            <span v-if="row.maxPrice != null && row.maxPrice !== row.minPrice" class="text-muted">
              ~{{ formatPrice(row.maxPrice) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="skuCount" label="规格数" width="90" />
        <el-table-column prop="totalAvailableStock" label="可用库存" width="100" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/merchant/products/${row.id}`)">管理</el-button>
            <el-button
              v-if="row.status !== 'ON_SALE'"
              link
              type="success"
              :disabled="row.status === 'PLATFORM_OFF'"
              @click="onStatus(row, 'ON_SALE')"
            >
              上架
            </el-button>
            <el-button
              v-if="row.status === 'ON_SALE'"
              link
              type="warning"
              @click="onStatus(row, 'OFF_SALE')"
            >
              下架
            </el-button>
            <el-button link type="danger" @click="onDelete(row)">删除</el-button>
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
import { changeGoodsStatus, deleteGoods, merchantGoodsList } from '@/api/goods'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ keyword: '', status: '', page: 1, size: 10 })
const placeholder = 'data:image/svg+xml,' + encodeURIComponent(
  '<svg xmlns="http://www.w3.org/2000/svg" width="48" height="48"><rect fill="#f2f3f5" width="48" height="48"/></svg>'
)

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
    const res = await merchantGoodsList({
      keyword: query.keyword || undefined,
      status: query.status || undefined,
      page: query.page,
      size: query.size
    })
    list.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function onStatus(row, status) {
  const tip = status === 'ON_SALE' ? '确认上架该商品？' : '确认下架该商品？'
  await ElMessageBox.confirm(tip, '提示')
  await changeGoodsStatus(row.id, status)
  ElMessage.success('操作成功')
  load(query.page)
}

async function onDelete(row) {
  await ElMessageBox.confirm('删除前请确保已下架且无锁定库存，确认删除？', '危险操作', { type: 'warning' })
  await deleteGoods(row.id)
  ElMessage.success('已删除')
  load(query.page)
}

onMounted(() => load(1))
</script>

<style scoped>
.goods-cell {
  display: flex;
  gap: 10px;
  align-items: center;
}
.thumb {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  object-fit: cover;
}
.name {
  font-weight: 600;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
