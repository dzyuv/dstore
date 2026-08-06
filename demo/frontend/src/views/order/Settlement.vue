<template>
  <div class="page">
    <div class="page-card" v-loading="loading">
      <h2 style="margin-top: 0">订单结算</h2>

      <el-empty v-if="!cartItems.length && !loading" description="没有待结算商品">
        <el-button type="primary" @click="$router.push('/cart')">返回购物车</el-button>
      </el-empty>

      <template v-else>
        <el-table :data="cartItems" stripe>
          <el-table-column label="商品" min-width="280">
            <template #default="{ row }">
              <div class="goods-cell">
                <img :src="row.image || placeholder" class="thumb" />
                <div>
                  <div class="name">{{ row.productName }}</div>
                  <div class="text-muted">{{ row.skuName }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="100">
            <template #default="{ row }">¥{{ formatPrice(row.price) }}</template>
          </el-table-column>
          <el-table-column label="数量" width="100" prop="quantity" />
          <el-table-column label="小计" width="120">
            <template #default="{ row }">¥{{ formatPrice(row.amount) }}</template>
          </el-table-column>
        </el-table>

        <el-divider />

        <el-row :gutter="24">
          <el-col :span="14">
            <el-form label-width="90px">
              <el-form-item label="收货地址" required>
                <div class="addr-row">
                  <el-select
                    v-model="addressId"
                    placeholder="请选择收货地址"
                    style="flex: 1"
                    filterable
                  >
                    <el-option
                      v-for="addr in addresses"
                      :key="addr.id"
                      :label="formatAddress(addr)"
                      :value="addr.id"
                    />
                  </el-select>
                  <el-button @click="$router.push('/address')">管理地址</el-button>
                </div>
              </el-form-item>
              <el-form-item label="配送时间">
                <el-input v-model="deliveryTime" placeholder="如：今天 10:00-12:00（可选）" />
              </el-form-item>
            </el-form>
          </el-col>
          <el-col :span="10">
            <div class="price-box">
              <div>商品金额：¥{{ formatPrice(totalAmount) }}</div>
              <div>配送费：¥0.00</div>
              <div class="price-lg" style="margin: 12px 0">应付：¥{{ formatPrice(totalAmount) }}</div>
              <el-button
                type="primary"
                size="large"
                class="full"
                :loading="submitting"
                :disabled="!cartItems.length"
                @click="onSubmitOrder"
              >
                提交订单
              </el-button>
            </div>
          </el-col>
        </el-row>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSelectedCart, clearSelectedCart } from '@/api/cart'
import { listAddresses } from '@/api/address'
import { createOrder, createPayment, paymentCallback } from '@/api/order'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const cartItems = ref([])
const addresses = ref([])
const addressId = ref(null)
const deliveryTime = ref('')

const placeholder =
  'data:image/svg+xml,' +
  encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64"><rect fill="#f2f3f5" width="64" height="64"/></svg>'
  )

const totalAmount = computed(() =>
  cartItems.value.reduce((sum, item) => sum + Number(item.amount || 0), 0)
)

function formatPrice(v) {
  return Number(v || 0).toFixed(2)
}

function formatAddress(addr) {
  if (!addr) return ''
  const full = `${addr.province || ''}${addr.city || ''}${addr.district || ''}${addr.detail || ''}`
  return `${addr.receiverName} ${addr.phone} - ${full}`
}

async function load() {
  loading.value = true
  try {
    const [cartRes, addrRes] = await Promise.all([getSelectedCart(), listAddresses()])
    // 过滤无效商品
    cartItems.value = (cartRes.data || []).filter((i) => !i.invalid)
    addresses.value = addrRes.data || []
    const def = addresses.value.find((a) => a.isDefault) || addresses.value[0]
    if (def) addressId.value = def.id
  } finally {
    loading.value = false
  }
}

async function onSubmitOrder() {
  if (!cartItems.value.length) {
    ElMessage.warning('没有可结算商品')
    return
  }
  if (!addressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  const addr = addresses.value.find((a) => a.id === addressId.value)
  if (!addr) {
    ElMessage.warning('地址无效，请重新选择')
    return
  }

  // 同门店校验
  const storeId = cartItems.value[0].storeId
  const merchantId = cartItems.value[0].merchantId || 0
  if (cartItems.value.some((i) => i.storeId !== storeId)) {
    ElMessage.warning('一次只能结算同一门店商品')
    return
  }

  submitting.value = true
  try {
    const orderRes = await createOrder({
      storeId,
      merchantId,
      addressId: addressId.value,
      receiverName: addr.receiverName,
      receiverPhone: addr.phone,
      receiverAddr: `${addr.province || ''}${addr.city || ''}${addr.district || ''}${addr.detail || ''}`,
      deliveryTime: deliveryTime.value || null,
      items: cartItems.value.map((i) => ({
        skuId: i.skuId,
        productId: i.productId,
        productName: i.productName,
        skuName: i.skuName,
        price: i.price,
        quantity: i.quantity
      }))
    })

    const orderNo = orderRes.data?.orderNo
    // 下单成功后清除购物车已选项
    try {
      await clearSelectedCart()
    } catch {
      /* 忽略清车失败 */
    }

    ElMessage.success('下单成功')

    // 演示：询问是否立即模拟支付
    try {
      await ElMessageBox.confirm(
        `订单 ${orderNo} 已创建，是否立即模拟支付？`,
        '去支付',
        { confirmButtonText: '立即支付', cancelButtonText: '稍后支付' }
      )
      const payRes = await createPayment({ orderNo, channel: 'ALIPAY' })
      const paymentNo = payRes.data?.paymentNo
      if (paymentNo) {
        await paymentCallback(paymentNo, true)
        ElMessage.success('支付成功')
      }
    } catch {
      /* 用户取消支付 */
    }

    router.replace('/orders')
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.goods-cell {
  display: flex;
  gap: 12px;
  align-items: center;
}
.thumb {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  object-fit: cover;
  background: #f5f7fa;
}
.name {
  font-weight: 600;
}
.addr-row {
  display: flex;
  gap: 8px;
  width: 100%;
}
.price-box {
  background: #fafafa;
  border-radius: 10px;
  padding: 16px 20px;
}
.full {
  width: 100%;
}
</style>
