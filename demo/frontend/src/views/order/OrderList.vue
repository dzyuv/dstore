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
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
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
            <el-button
              link
              type="warning"
              @click="openReview(row)"
              v-if="row.status === 'COMPLETED'"
            >
              评价
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 订单详情 / 物流 -->
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

    <!-- 评价 -->
    <el-dialog v-model="review.visible" title="订单评价" width="480px">
      <div v-if="review.items.length">
        <div v-for="item in review.items" :key="item.productId" class="review-item">
          <div class="name">{{ item.productName }}（{{ item.skuName }}）</div>
          <el-rate v-model="item.score" />
          <el-input
            v-model="item.content"
            type="textarea"
            :rows="2"
            placeholder="说说你的使用体验（可选）"
            style="margin-top: 8px"
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="review.visible = false">取消</el-button>
        <el-button type="primary" :loading="review.submitting" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listOrders,
  getOrderDetail,
  cancelOrder,
  confirmOrder,
  createPayment,
  paymentCallback,
  createReview
} from '@/api/order'

const list = ref([])
const loading = ref(false)
const detail = reactive({ visible: false, data: null })
const review = reactive({
  visible: false,
  orderNo: '',
  items: [],
  submitting: false
})

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

function deliveryStatusText(status) {
  const map = {
    WAIT_PICK: '待拣货',
    PICKING: '拣货中',
    PICKED: '已拣货',
    DELIVERING: '配送中',
    DELIVERED: '已送达'
  }
  return map[status] || status
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

async function openDetail(row) {
  const res = await getOrderDetail(row.orderNo)
  detail.data = res.data
  detail.visible = true
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

async function openReview(row) {
  const res = await getOrderDetail(row.orderNo)
  const items = res.data?.items || []
  // 按 productId 去重评价
  const seen = new Set()
  review.items = []
  for (const it of items) {
    if (seen.has(it.productId)) continue
    seen.add(it.productId)
    review.items.push({
      productId: it.productId,
      skuId: it.skuId,
      productName: it.productName,
      skuName: it.skuName,
      score: 5,
      content: ''
    })
  }
  review.orderNo = row.orderNo
  review.visible = true
}

async function submitReview() {
  if (!review.items.length) return
  review.submitting = true
  try {
    for (const item of review.items) {
      await createReview({
        orderNo: review.orderNo,
        productId: item.productId,
        skuId: item.skuId,
        score: item.score,
        content: item.content || null
      })
    }
    ElMessage.success('评价成功（再次提交可修改）')
    review.visible = false
  } finally {
    review.submitting = false
  }
}

onMounted(load)
</script>

<style scoped>
.review-item {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}
.review-item .name {
  font-weight: 600;
  margin-bottom: 6px;
}
</style>
