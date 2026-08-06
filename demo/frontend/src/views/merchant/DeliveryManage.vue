<template>
  <div class="page">
    <div class="page-card" v-loading="loading">
      <h2 style="margin-bottom: 16px">商家配送管理</h2>

      <el-table :data="list" stripe>
        <el-table-column prop="orderNo" label="订单号" width="160" />
        <el-table-column prop="receiverName" label="收货人" width="100" />
        <el-table-column prop="receiverPhone" label="手机号" width="120" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="orderStatusType(row.status)">
              {{ orderStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="物流" width="180">
          <template #default="{ row }">
            {{ deliveryStatusText(row.delivery?.status || 'WAIT_PICK') }}
            <span v-if="row.delivery?.trackingNo" class="text-muted">（{{ row.delivery.trackingNo }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看详情</el-button>
            <el-button
              v-if="['WAIT_PICK', 'PICKING'].includes(row.delivery?.status)"
              link
              type="primary"
              @click="updateDelivery(row, 'PICKING')"
            >
              拣货完成
            </el-button>
            <el-button
              v-if="['PICKING', 'PICKED'].includes(row.delivery?.status)"
              link
              type="primary"
              @click="updateDelivery(row, 'DELIVERING')"
            >
              配送中
            </el-button>
            <el-button
              v-if="row.delivery?.status === 'DELIVERING'"
              link
              type="primary"
              @click="updateDelivery(row, 'DELIVERED')"
            >
              已送达
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 订单详情抽屉 -->
    <el-drawer v-model="detail.visible" title="订单详情" size="480px">
      <template v-if="detail.data">
        <!-- same as OrderList detail drawer -->
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="订单号">{{ detail.data.order?.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            {{ orderStatusText(detail.data.order?.status) }}
          </el-descriptions-item>
          <el-descriptions-item label="收货人">
            {{ detail.data.order?.receiverName }} {{ detail.data.order?.receiverPhone }}
          </el-descriptions-item>
        </el-descriptions>

        <h4 style="margin: 16px 0 8px">物流信息</h4>
        <el-descriptions v-if="detail.data.delivery" :column="1" border size="small">
          <el-descriptions-item label="状态">
            {{ deliveryStatusText(detail.data.delivery.status) }}
          </el-descriptions-item>
          <el-descriptions-item label="配送商">
            {{ detail.data.delivery.carrier || '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="运单号">
            {{ detail.data.delivery.trackingNo || '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="备注">
            {{ detail.data.delivery.remark || '—' }}
          </el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMerchantOrders, updateDeliveryStatus } from '@/api/order'

const list = ref([])
const loading = ref(false)
const detail = reactive({ visible: false, data: null })

async function load() {
  loading.value = true
  try {
    const res = await getMerchantOrders()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function openDetail(row) {
  // call merchant detail if needed, or just show
  detail.data = row
  detail.visible = true
}

async function updateDelivery(row, status) {
  await ElMessageBox.confirm(`确认将订单 ${row.orderNo} 更新为「${deliveryStatusText(status)}」？`, '配送状态')
  await updateDeliveryStatus(row.orderNo, { status })
  ElMessage.success('配送状态已更新')
  await load()
}

const orderStatusText = (status) => {
  const map = { PENDING_PAY: '待支付', PAID: '已支付', PICKING: '拣货中', PICKED: '已拣货', DELIVERING: '配送中', DELIVERED: '已送达', CANCELLED: '已取消' }
  return map[status] || status
}

const deliveryStatusText = (status) => {
  const map = { WAIT_PICK: '待拣货', PICKING: '拣货中', PICKED: '已拣货', DELIVERING: '配送中', DELIVERED: '已送达' }
  return map[status] || status
}

const orderStatusType = (status) => {
  const map = { PENDING_PAY: 'warning', PAID: 'success', PICKING: 'info', PICKED: 'info', DELIVERING: 'primary', DELIVERED: 'success', CANCELLED: 'info' }
  return map[status] || 'info'
}

onMounted(load)
</script>

<style scoped>
.text-muted {
  color: #909399;
}
</style>