<template>
  <div class="page">
    <el-card class="page-card">
      <h2>我的订单</h2>
      <el-table :data="list" stripe>
        <el-table-column prop="orderNo" label="订单号" width="160" />
        <el-table-column prop="totalAmount" label="金额" width="100">
          <template #default="{ row }">¥{{ formatPrice(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="orderStatusType(row.status)">{{ orderStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewOrder(row.orderNo)">详情</el-button>
            <el-button v-if="row.status === 'PENDING_PAY'" link type="danger" @click="cancelOrder(row.orderNo)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listOrders, cancelOrder, createPayment } from '@/api/order'

const list = ref([])
const loading = ref(false)

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await listOrders()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function viewOrder(orderNo) {
  // 跳转到订单详情页
  ElMessage.info('订单详情页待实现')
}

async function cancelOrder(orderNo) {
  await ElMessageBox.confirm('确认取消该订单？')
  await cancelOrder(orderNo)
  ElMessage.success('订单已取消')
  await load()
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
    CANCELLED: '已取消'
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
    CANCELLED: 'danger'
  }
  return map[status] || 'info'
}
</script>
