<template>
  <div class="page">
    <div class="page-card" v-loading="loading">
      <h2 style="margin-bottom: 16px">商家配送管理</h2>

      <el-table :data="list" stripe>
        <el-table-column prop="order.orderNo" label="订单号" width="180" />
        <el-table-column prop="order.receiverName" label="收货人" width="100" />
        <el-table-column prop="order.receiverPhone" label="手机号" width="120" />
        <el-table-column label="订单状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="orderStatusType(row.order?.status)">
              {{ orderStatusText(row.order?.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="物流" width="220">
          <template #default="{ row }">
            {{ deliveryStatusText(row.delivery?.status || 'WAIT_PICK') }}
            <span v-if="row.delivery?.trackingNo" class="text-muted">
              （{{ row.delivery.carrier }} {{ row.delivery.trackingNo }}）
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看详情</el-button>
            <el-button
              v-if="canFillDelivery(row.order?.status)"
              link
              type="primary"
              @click="openFillDelivery(row)"
            >
              填写配送信息
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 订单详情抽屉 -->
    <el-drawer v-model="detail.visible" title="订单详情" size="480px">
      <template v-if="detail.data">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="订单号">{{ detail.data.order?.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            {{ orderStatusText(detail.data.order?.status) }}
          </el-descriptions-item>
          <el-descriptions-item label="金额">
            ¥{{ formatPrice(detail.data.order?.totalAmount) }}
          </el-descriptions-item>
          <el-descriptions-item label="收货人">
            {{ detail.data.order?.receiverName }} {{ detail.data.order?.receiverPhone }}
          </el-descriptions-item>
          <el-descriptions-item label="地址">{{ detail.data.order?.receiverAddr }}</el-descriptions-item>
          <el-descriptions-item label="配送时间">
            {{ detail.data.order?.deliveryTime || '—' }}
          </el-descriptions-item>
        </el-descriptions>

        <h4 style="margin: 16px 0 8px">商品明细</h4>
        <el-table :data="detail.data.items || []" size="small" border>
          <el-table-column prop="productName" label="商品" min-width="120" />
          <el-table-column prop="skuName" label="规格" width="100" />
          <el-table-column prop="quantity" label="数量" width="60" />
          <el-table-column label="小计" width="90">
            <template #default="{ row }">¥{{ formatPrice(row.amount) }}</template>
          </el-table-column>
        </el-table>

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
        <el-empty v-else description="暂无物流信息" :image-size="60" />
      </template>
    </el-drawer>

    <!-- 填写配送信息弹窗 -->
    <el-dialog v-model="fill.visible" title="填写配送信息" width="420px">
      <el-form :model="fill.form" label-width="84px">
        <el-form-item label="订单号">
          <span class="text-muted">{{ fill.form.orderNo }}</span>
        </el-form-item>
        <el-form-item label="配送商">
          <el-input v-model="fill.form.carrier" placeholder="如：顺丰 / 美团跑腿 / 自配送" />
        </el-form-item>
        <el-form-item label="运单号">
          <el-input v-model="fill.form.trackingNo" placeholder="配送单号" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="fill.form.remark" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="fill.visible = false">取消</el-button>
        <el-button type="primary" :loading="fill.submitting" @click="submitFillDelivery">提交并开始配送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getMerchantOrders,
  getMerchantOrderDetail,
  updateDeliveryStatus
} from '@/api/order'

const list = ref([])
const loading = ref(false)
const detail = reactive({ visible: false, data: null })
const fill = reactive({
  visible: false,
  submitting: false,
  form: { orderNo: '', carrier: '', trackingNo: '', remark: '' }
})

function formatPrice(v) {
  return v == null ? '0.00' : Number(v).toFixed(2)
}

function orderStatusText(status) {
  const map = {
    PENDING_PAY: '待支付', PAID: '已支付', PICKING: '拣货中', PICKED: '已拣货',
    DELIVERING: '配送中', DELIVERED: '已送达', CANCELLED: '已取消'
  }
  return map[status] || status
}

function orderStatusType(status) {
  const map = {
    PENDING_PAY: 'warning', PAID: 'success', PICKING: 'info', PICKED: 'info',
    DELIVERING: 'primary', DELIVERED: 'success', CANCELLED: 'info'
  }
  return map[status] || 'info'
}

function deliveryStatusText(status) {
  const map = {
    WAIT_PICK: '待拣货', PICKING: '拣货中', PICKED: '已拣货',
    DELIVERING: '配送中', DELIVERED: '已送达'
  }
  return map[status] || status
}

function canFillDelivery(orderStatus) {
  return ['PAID', 'PICKING', 'PICKED'].includes(orderStatus)
}

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
  const res = await getMerchantOrderDetail(row.order?.orderNo)
  detail.data = res.data
  detail.visible = true
}

function openFillDelivery(row) {
  fill.form = { orderNo: row.order?.orderNo || '', carrier: '', trackingNo: '', remark: '' }
  fill.visible = true
}

async function submitFillDelivery() {
  if (!fill.form.carrier.trim() || !fill.form.trackingNo.trim()) {
    ElMessage.warning('请填写配送商和运单号')
    return
  }
  fill.submitting = true
  try {
    await updateDeliveryStatus(fill.form.orderNo, {
      status: 'DELIVERING',
      carrier: fill.form.carrier.trim(),
      trackingNo: fill.form.trackingNo.trim(),
      remark: fill.form.remark.trim() || undefined
    })
    ElMessage.success('已提交，订单进入配送中')
    fill.visible = false
    await load()
  } finally {
    fill.submitting = false
  }
}

onMounted(load)
</script>

<style scoped>
.text-muted {
  color: #909399;
}
</style>
