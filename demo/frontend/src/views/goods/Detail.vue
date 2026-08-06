<template>
  <div class="page" v-loading="loading">
    <template v-if="detail">
      <div class="page-card detail">
        <div class="gallery">
          <img :src="currentImage" alt="" @error="onImgError" />
        </div>
        <div class="info">
          <h1>{{ product.name }}</h1>
          <p class="text-muted detail-text">{{ product.detail || '暂无详情描述' }}</p>
          <div class="price-box">
            <span class="price-lg">¥{{ formatPrice(selectedSku?.price) }}</span>
            <span class="text-muted" v-if="detail.minPrice !== detail.maxPrice">
              区间 ¥{{ formatPrice(detail.minPrice) }} ~ ¥{{ formatPrice(detail.maxPrice) }}
            </span>
          </div>
          <div class="sku-block">
            <div class="label">规格</div>
            <div class="sku-list">
              <el-check-tag
                v-for="sku in skus"
                :key="sku.id"
                :checked="selectedSkuId === sku.id"
                :disabled="sku.availableStock <= 0"
                @change="() => selectSku(sku.id)"
              >
                {{ sku.skuName }}
                <span class="sku-stock">库存{{ sku.availableStock }}</span>
              </el-check-tag>
            </div>
          </div>
          <div class="qty-block">
            <div class="label">数量</div>
            <el-input-number v-model="quantity" :min="1" :max="maxQty" />
            <span class="text-muted">可用库存 {{ selectedSku?.availableStock ?? 0 }}</span>
          </div>
          <div class="actions">
            <el-button type="primary" size="large" :disabled="!canBuy" :loading="adding" @click="onAddCart">
              加入购物车
            </el-button>
            <el-button size="large" @click="$router.push('/')">继续逛</el-button>
          </div>
        </div>
      </div>

      <div class="page-card sku-table" v-if="skus.length">
        <h3>规格一览</h3>
        <el-table :data="skus" stripe>
          <el-table-column prop="skuName" label="规格" />
          <el-table-column label="售价" width="120">
            <template #default="{ row }">¥{{ formatPrice(row.price) }}</template>
          </el-table-column>
          <el-table-column prop="availableStock" label="可用库存" width="120" />
          <el-table-column prop="barcode" label="条码" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ON' ? 'success' : 'info'" size="small">
                {{ row.status === 'ON' ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </template>
    <el-empty v-else-if="!loading" description="商品不存在或已下架" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getGoodsDetail } from '@/api/goods'
import { addToCart } from '@/api/cart'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const adding = ref(false)
const detail = ref(null)
const selectedSkuId = ref(null)
const quantity = ref(1)

const product = computed(() => detail.value?.product || {})
const skus = computed(() => detail.value?.skus || [])
const selectedSku = computed(() => skus.value.find((s) => s.id === selectedSkuId.value))
const maxQty = computed(() => Math.max(1, selectedSku.value?.availableStock || 1))
const canBuy = computed(() => selectedSku.value && selectedSku.value.availableStock > 0)

const placeholder = 'data:image/svg+xml,' + encodeURIComponent(
  '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="400"><rect fill="#f2f3f5" width="400" height="400"/><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" fill="#c0c4cc" font-size="18">暂无图片</text></svg>'
)
const currentImage = computed(() =>
  selectedSku.value?.image || product.value.mainImage || placeholder
)

function formatPrice(v) {
  if (v == null) return '0.00'
  return Number(v).toFixed(2)
}
function selectSku(id) {
  selectedSkuId.value = id
  quantity.value = 1
}
function onImgError(e) {
  e.target.src = placeholder
}

async function load() {
  loading.value = true
  try {
    const res = await getGoodsDetail(route.params.id)
    detail.value = res.data
    const first = (res.data?.skus || []).find((s) => s.availableStock > 0) || res.data?.skus?.[0]
    selectedSkuId.value = first?.id || null
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

async function onAddCart() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (!selectedSku.value) {
    ElMessage.warning('请选择规格')
    return
  }
  adding.value = true
  try {
    await addToCart({ skuId: selectedSku.value.id, quantity: quantity.value })
    ElMessage.success('已加入购物车')
  } finally {
    adding.value = false
  }
}

watch(() => route.params.id, load)
onMounted(load)
</script>

<style scoped>
.detail {
  display: grid;
  grid-template-columns: 420px 1fr;
  gap: 28px;
  margin-bottom: 16px;
}
.gallery {
  background: #f5f7fa;
  border-radius: 12px;
  overflow: hidden;
  aspect-ratio: 1;
}
.gallery img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.info h1 {
  margin: 0 0 10px;
  font-size: 24px;
}
.detail-text {
  line-height: 1.6;
  margin-bottom: 16px;
}
.price-box {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 20px;
  padding: 12px 14px;
  background: #fff7e6;
  border-radius: 8px;
}
.label {
  width: 48px;
  color: #909399;
  flex-shrink: 0;
}
.sku-block, .qty-block {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;
  align-items: flex-start;
}
.sku-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.sku-stock {
  margin-left: 6px;
  font-size: 12px;
  opacity: 0.75;
}
.qty-block {
  align-items: center;
}
.actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}
.sku-table h3 {
  margin: 0 0 12px;
}
@media (max-width: 900px) {
  .detail {
    grid-template-columns: 1fr;
  }
}
</style>
