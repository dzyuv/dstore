<template>
  <div class="page">
    <div class="page-card" v-loading="loading">
      <div class="flex-between" style="margin-bottom: 16px">
        <h2 style="margin: 0">我的订单</h2>
        <el-button @click="load">刷新</el-button>
      </div>

      <el-empty v-if="!list.length && !loading" description="暂无订单">
        <el-button type="primary" @click="$router.push('/')">去逛逛</el-button>
      </el-empty>

      <el-table v-else :data="list" stripe>
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column label="金额" width="120">
          <template #default="{ row }">¥{{ formatPrice(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="orderStatusType(row.status)">
              {{ orderStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" min-width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="onPay(row)" v-if="row.status === 'PENDING_PAY'">
              去支付
            </el-button>
            <el-button
              link
              type="danger"
              @click="onCancel(row)"
              v-if="row.status === 'PENDING_PAY' || row.status === 'PAID' || row.status === 'PICKING'"
            >
              取消
            </el-button>
            <el-button
              link
              type="success"
              @click="onConfirm(row)"
              v-if="row.status === 'DELIVERED'"
            >
              确认收货
            </el-button>
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
  listOrders,
  cancelOrder,
  confirmOrder,
  createPayment,
  paymentCallback
} from '@/api/order'

const list = ref([])
const loading = ref(false)

function formatPrice(v) {
  return Number(v || 0).toFixed(2)
}

function orderStatusText(status) {
  const map = {
    PENDING_PAY: '待支付',
    PAID: '已支付',
    PICKING: '拣货中',
    PICKED: '已拣货',
    DELIVERING: '配送中',
    DELIVERED: '已送达',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    REFUNDED: '已退款'
  }
  return map[status] || status
}

function orderStatusType(status) {
  const map = {
    PENDING_PAY: 'warning',
    PAID: 'success',
    PICKING: 'info',
    PICKED: 'info',
    DELIVERING: 'primary',
    DELIVERED: 'success',
    COMPLETED: 'success',
    CANCELLED: 'info',
    REFUNDED: 'danger'
  }
  return map[status] || 'info'
}

async function load() {
  loading.value = true
  try {
    const res = await listOrders()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function onPay(row) {
  await ElMessageBox.confirm(`确认支付订单 ${row.orderNo}？（演示环境模拟支付）`, '去支付')
  const payRes = await createPayment({ orderNo: row.orderNo, channel: 'ALIPAY' })
  const paymentNo = payRes.data?.paymentNo
  if (!paymentNo) {
    ElMessage.error('创建支付单失败')
    return
  }
  await paymentCallback(paymentNo, true)
  ElMessage.success('支付成功')
  await load()
}

async function onCancel(row) {
  await ElMessageBox.confirm(`确认取消订单 ${row.orderNo}？`, '提示')
  await cancelOrder(row.orderNo, '用户取消')
  ElMessage.success('订单已取消')
  await load()
}

async function onConfirm(row) {
  await ElMessageBox.confirm('确认已收到商品？', '确认收货')
  await confirmOrder(row.orderNo)
  ElMessage.success('已确认收货')
  await load()
}

onMounted(load)
</script>
