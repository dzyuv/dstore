<template>
  <div class="page">
    <el-card class="page-card">
      <h2>订单结算</h2>
      <el-table :data="cartItems" stripe>
        <el-table-column label="商品" width="280">
          <template #default="{ row }">
            <img :src="row.image || placeholder" class="thumb" />
            <div>
              <div class="name">{{ row.productName }}</div>
              <div class="text-muted">{{ row.skuName }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="100">
          <template #default="{ row }">¥{{ formatPrice(row.price) }}</template>
        </el-table-column>
        <el-table-column label="数量" width="100">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" style="width: 80px" />
          </template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template #default="{ row }">¥{{ formatPrice(row.amount) }}</template>
        </el-table-column>
      </el-table>

      <el-divider />

      <el-row :gutter="24">
        <el-col :span="12">
          <el-form label-width="80px">
            <el-form-item label="收货地址">
              <el-select v-model="addressId" placeholder="请选择地址">
                <el-option v-for="addr in addresses" :key="addr.id" :label="addr.detail" :value="addr.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="配送时间">
              <el-input v-model="deliveryTime" placeholder="如 今天 10:00-12:00" />
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="12">
          <div class="price-box">
            <div>商品金额：¥{{ formatPrice(totalAmount) }}</div>
            <div>配送费：¥0.00</div>
            <div class="price-lg">应付：¥{{ formatPrice(totalAmount) }}</div>
          </div>
          <el-button type="primary" size="large" @click="onSubmitOrder">提交订单</el-button>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getCart, listAddresses } from '@/api/cart'
import { createOrder } from '@/api/order'

const cartItems = ref([])
const addresses = ref([])
const addressId = ref('')
const deliveryTime = ref('')
const totalAmount = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + (item.amount || 0), 0)
})

onMounted(async () => {
  const res = await getCart()
  cartItems.value = res.data.flatMap(g => g.items)
  const addrRes = await listAddresses()
  addresses.value = addrRes.data || []
})

function onSubmitOrder() {
  if (!addressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  ElMessage.info('订单提交成功，等待支付')
  // 实际调用
  createOrder({
    storeId: cartItems.value[0].storeId,
    addressId,
    deliveryTime,
    items: cartItems.value
  })
}
</script>
