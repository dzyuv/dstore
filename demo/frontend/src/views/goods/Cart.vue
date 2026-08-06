<template>
  <div class="page">
    <div class="page-card">
      <div class="flex-between" style="margin-bottom: 16px">
        <h2 style="margin: 0">我的购物车</h2>
        <div>
          <el-button type="danger" plain :disabled="!selectedIds.length" @click="onBatchDelete">
            删除选中
          </el-button>
        </div>
      </div>

      <div v-loading="loading">
        <el-empty v-if="!groups.length" description="购物车是空的，去逛逛吧">
          <el-button type="primary" @click="$router.push('/')">去首页</el-button>
        </el-empty>

        <div v-for="group in groups" :key="group.storeId" class="store-group">
          <div class="store-head">
            <el-checkbox
              :model-value="isStoreAllSelected(group)"
              :indeterminate="isStoreIndeterminate(group)"
              @change="(v) => onSelectStore(group.storeId, v)"
            >
              门店 #{{ group.storeId }}
            </el-checkbox>
            <span class="text-muted">
              已选 {{ group.selectedCount || 0 }} 件，小计
              <span class="price">¥{{ formatPrice(group.selectedAmount) }}</span>
            </span>
          </div>

          <div v-for="item in group.items" :key="item.cartItemId" class="cart-item" :class="{ invalid: item.invalid }">
            <el-checkbox
              :model-value="!!item.selected"
              :disabled="item.invalid"
              @change="(v) => onToggleItem(item, v)"
            />
            <img class="thumb" :src="item.image || placeholder" @error="(e) => (e.target.src = placeholder)" />
            <div class="item-info">
              <div class="name">
                {{ item.productName }}
                <el-tag v-if="item.invalid" size="small" type="danger">已下架</el-tag>
              </div>
              <div class="text-muted">规格：{{ item.skuName }}</div>
              <div class="text-muted">可用库存：{{ item.availableStock }}</div>
            </div>
            <div class="price">¥{{ formatPrice(item.price) }}</div>
            <el-input-number
              :model-value="item.quantity"
              :min="1"
              :max="Math.max(1, item.availableStock || 1)"
              :disabled="item.invalid"
              size="small"
              @change="(v) => onChangeQty(item, v)"
            />
            <div class="price subtotal">¥{{ formatPrice(item.amount) }}</div>
            <el-button link type="danger" @click="onDelete(item)">删除</el-button>
          </div>
        </div>
      </div>

      <div class="footer-bar" v-if="groups.length">
        <div>
          已选 <b>{{ totalSelectedCount }}</b> 件，合计
          <span class="price-lg">¥{{ formatPrice(totalSelectedAmount) }}</span>
        </div>
        <el-button type="warning" size="large" :disabled="!selectedIds.length" @click="goCheckout">
          去结算
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteCartBatch,
  deleteCartItem,
  getCart,
  selectStoreCart,
  updateCartItem
} from '@/api/cart'

const router = useRouter()

const loading = ref(false)
const groups = ref([])

const placeholder = 'data:image/svg+xml,' + encodeURIComponent(
  '<svg xmlns="http://www.w3.org/2000/svg" width="80" height="80"><rect fill="#f2f3f5" width="80" height="80"/></svg>'
)

const selectedIds = computed(() =>
  groups.value.flatMap((g) => g.items.filter((i) => i.selected && !i.invalid).map((i) => i.cartItemId))
)
const totalSelectedCount = computed(() =>
  groups.value.reduce((s, g) => s + (g.selectedCount || 0), 0)
)
const totalSelectedAmount = computed(() =>
  groups.value.reduce((s, g) => s + Number(g.selectedAmount || 0), 0)
)

function formatPrice(v) {
  return Number(v || 0).toFixed(2)
}
function isStoreAllSelected(group) {
  const valid = group.items.filter((i) => !i.invalid)
  return valid.length > 0 && valid.every((i) => i.selected)
}
function isStoreIndeterminate(group) {
  const valid = group.items.filter((i) => !i.invalid)
  const selected = valid.filter((i) => i.selected).length
  return selected > 0 && selected < valid.length
}

async function load() {
  loading.value = true
  try {
    const res = await getCart()
    groups.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function onToggleItem(item, selected) {
  await updateCartItem(item.cartItemId, { selected: !!selected, quantity: item.quantity })
  await load()
}
async function onChangeQty(item, quantity) {
  if (!quantity) return
  await updateCartItem(item.cartItemId, { quantity, selected: item.selected })
  await load()
}
async function onSelectStore(storeId, selected) {
  await selectStoreCart(storeId, !!selected)
  await load()
}
async function onDelete(item) {
  await ElMessageBox.confirm('确认删除该商品？', '提示')
  await deleteCartItem(item.cartItemId)
  ElMessage.success('已删除')
  await load()
}
async function onBatchDelete() {
  await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 件商品？`, '提示')
  await deleteCartBatch(selectedIds.value)
  ElMessage.success('已删除')
  await load()
}

function goCheckout() {
  if (!selectedIds.value.length) {
    ElMessage.warning('请先选择要结算的商品')
    return
  }
  // 按门店分组结算：若选中多门店，提示一次只结算一个门店
  const storeIds = new Set(
    groups.value
      .flatMap((g) => g.items.filter((i) => i.selected && !i.invalid).map((i) => i.storeId))
  )
  if (storeIds.size > 1) {
    ElMessage.warning('一次只能结算同一个门店的商品，请取消其他门店勾选')
    return
  }
  router.push('/orders/checkout')
}

onMounted(load)
</script>

<style scoped>
.store-group {
  border: 1px solid #ebeef5;
  border-radius: 10px;
  margin-bottom: 16px;
  overflow: hidden;
}
.store-head {
  background: #f5f7fa;
  padding: 10px 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.cart-item {
  display: grid;
  grid-template-columns: 24px 72px 1fr 90px 130px 100px 60px;
  gap: 12px;
  align-items: center;
  padding: 14px;
  border-top: 1px solid #f0f2f5;
}
.cart-item.invalid {
  opacity: 0.65;
  background: #fafafa;
}
.thumb {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  object-fit: cover;
  background: #f5f7fa;
}
.name {
  font-weight: 600;
  margin-bottom: 4px;
  display: flex;
  gap: 8px;
  align-items: center;
}
.subtotal {
  text-align: right;
}
.footer-bar {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
@media (max-width: 900px) {
  .cart-item {
    grid-template-columns: 24px 56px 1fr;
    grid-auto-rows: auto;
  }
}
</style>
