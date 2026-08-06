<template>
  <div class="page">
    <div class="page-card" v-loading="pageLoading">
      <div class="flex-between" style="margin-bottom: 16px">
        <h2 style="margin: 0">{{ isEdit ? '编辑商品' : '发布商品' }}</h2>
        <el-button @click="$router.push('/merchant/products')">返回列表</el-button>
      </div>

      <el-alert
        v-if="!isEdit && !stores.length"
        type="warning"
        :closable="false"
        show-icon
        title="暂无门店，请先在「门店管理」中创建门店后再发布商品"
        style="margin-bottom: 16px"
      />

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 900px">
        <el-divider content-position="left">基本信息</el-divider>
        <el-form-item label="所属门店" prop="storeId" v-if="!isEdit">
          <el-select
            v-model="form.storeId"
            placeholder="请选择门店"
            style="width: 100%"
            :disabled="!stores.length"
          >
            <el-option
              v-for="s in stores"
              :key="s.id"
              :label="`${s.storeName}（#${s.id}）`"
              :value="s.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-tree-select
            v-model="form.categoryId"
            :data="categories"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            check-strictly
            filterable
            clearable
            style="width: 100%"
            placeholder="选择分类"
          />
        </el-form-item>
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" maxlength="64" show-word-limit placeholder="商品名称" />
        </el-form-item>
        <el-form-item label="主图 URL">
          <el-input v-model="form.mainImage" placeholder="图片地址，可留空" />
        </el-form-item>
        <el-form-item label="详情描述">
          <el-input v-model="form.detail" type="textarea" :rows="4" placeholder="商品详情" />
        </el-form-item>
        <el-form-item label="创建后上架" v-if="!isEdit">
          <el-switch v-model="form.onSale" />
        </el-form-item>

        <template v-if="!isEdit">
          <el-divider content-position="left">规格 SKU（至少一个）</el-divider>
          <div v-for="(sku, idx) in form.skus" :key="sku._uid" class="sku-editor">
            <el-row :gutter="12">
              <el-col :span="6">
                <el-form-item
                  :label="idx === 0 ? '规格名' : ''"
                  :prop="`skus.${idx}.skuName`"
                  :rules="skuNameRule"
                >
                  <el-input v-model="sku.skuName" placeholder="如 原味 500g" />
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item
                  :label="idx === 0 ? '售价' : ''"
                  :prop="`skus.${idx}.price`"
                  :rules="priceRule"
                >
                  <el-input v-model="sku.price" placeholder="0.00" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item
                  :label="idx === 0 ? '库存' : ''"
                  :prop="`skus.${idx}.stock`"
                  :rules="stockRule"
                >
                  <el-input v-model="sku.stock" placeholder="0" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item :label="idx === 0 ? '条码' : ''">
                  <el-input v-model="sku.barcode" placeholder="可选" />
                </el-form-item>
              </el-col>
              <el-col :span="3">
                <el-form-item :label="idx === 0 ? ' ' : ''">
                  <el-button
                    type="danger"
                    link
                    :disabled="form.skus.length <= 1"
                    @click="form.skus.splice(idx, 1)"
                  >
                    删除
                  </el-button>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
          <el-button type="primary" plain @click="addSkuRow">+ 添加规格</el-button>
        </template>

        <el-form-item style="margin-top: 24px">
          <el-button type="primary" :loading="saving" :disabled="!isEdit && !stores.length" @click="onSave">
            保存
          </el-button>
          <el-button @click="$router.push('/merchant/products')">取消</el-button>
        </el-form-item>
      </el-form>

      <!-- 编辑模式：SKU 与库存（不展示流水） -->
      <template v-if="isEdit && productId">
        <el-divider content-position="left">规格与库存管理</el-divider>
        <div class="flex-between" style="margin-bottom: 12px">
          <el-tag :type="statusType(productStatus)">{{ statusText(productStatus) }}</el-tag>
          <el-button type="primary" @click="openSkuDialog()">新增规格</el-button>
        </div>
        <el-table :data="skuList" stripe>
          <el-table-column prop="skuName" label="规格" min-width="120" />
          <el-table-column label="售价" width="110">
            <template #default="{ row }">¥{{ formatPrice(row.price) }}</template>
          </el-table-column>
          <el-table-column prop="physicalStock" label="物理库存" width="100" />
          <el-table-column prop="lockedStock" label="锁定库存" width="100" />
          <el-table-column prop="availableStock" label="可用" width="90" />
          <el-table-column prop="barcode" label="条码" min-width="100" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 'ON' ? 'success' : 'info'">
                {{ row.status === 'ON' ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openSkuDialog(row)">编辑</el-button>
              <el-button link type="warning" @click="openStockDialog(row)">调库存</el-button>
              <el-button link type="danger" @click="onDeleteSku(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </div>

    <!-- SKU 弹窗 -->
    <el-dialog
      v-model="skuDialog.visible"
      :title="skuDialog.form.id ? '编辑规格' : '新增规格'"
      width="480px"
      destroy-on-close
    >
      <el-form :model="skuDialog.form" label-width="80px">
        <el-form-item label="规格名">
          <el-input v-model="skuDialog.form.skuName" />
        </el-form-item>
        <el-form-item label="售价">
          <el-input v-model="skuDialog.form.price" placeholder="0.00" style="width: 100%" />
        </el-form-item>
        <el-form-item label="初始库存" v-if="!skuDialog.form.id">
          <el-input-number v-model="skuDialog.form.stock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="条码">
          <el-input v-model="skuDialog.form.barcode" />
        </el-form-item>
        <el-form-item label="图片">
          <el-input v-model="skuDialog.form.image" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="skuDialog.form.status">
            <el-radio value="ON">启用</el-radio>
            <el-radio value="OFF">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="skuDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="skuDialog.saving" @click="saveSku">确定</el-button>
      </template>
    </el-dialog>

    <!-- 调库存 -->
    <el-dialog v-model="stockDialog.visible" title="调整物理库存" width="420px" destroy-on-close>
      <p class="text-muted">正数增加，负数减少；调整后物理库存不能低于锁定库存。</p>
      <p>当前规格：{{ stockDialog.skuName }}</p>
      <el-input v-model="stockDialog.changeQty" placeholder="输入调整数量，正数增加，负数减少" style="width: 100%" />
      <el-input v-model="stockDialog.remark" placeholder="备注（可选）" style="margin-top: 12px" />
      <template #footer>
        <el-button @click="stockDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="stockDialog.saving" @click="saveStock">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  addSku,
  adjustStock,
  createGoods,
  deleteSku,
  merchantGoodsDetail,
  updateGoods,
  updateSku
} from '@/api/goods'
import { getCategoryTree } from '@/api/category'
import { listStores } from '@/api/merchant'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => {
  const id = route.params.id
  return !!id && id !== 'create'
})
const productId = computed(() => (isEdit.value ? Number(route.params.id) : null))

const formRef = ref()
const pageLoading = ref(false)
const saving = ref(false)
const stores = ref([])
const categories = ref([])
const skuList = ref([])
const productStatus = ref('')

let _skuUid = 1
function newSkuRow() {
  return { _uid: _skuUid++, skuName: '', price: 1, stock: 10, barcode: '' }
}

const form = reactive({
  storeId: null,
  categoryId: null,
  name: '',
  mainImage: '',
  detail: '',
  onSale: true,
  skus: [newSkuRow()]
})

const rules = {
  storeId: [{ required: true, message: '请选择门店', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }]
}
const skuNameRule = [{ required: true, message: '规格名必填', trigger: 'blur' }]
const priceRule = [
  { required: true, message: '售价必填', trigger: 'blur' },
  {
    validator: (_r, v, cb) => {
      if (v == null || Number(v) < 0.01) cb(new Error('售价须大于 0'))
      else cb()
    },
    trigger: 'blur'
  }
]
const stockRule = [
  { required: true, message: '库存必填', trigger: 'blur' },
  {
    validator: (_r, v, cb) => {
      if (v === '' || v == null) { cb(new Error('库存必填')); return }
      const n = Number(v)
      if (!Number.isInteger(n) || n < 0) cb(new Error('库存须为非负整数'))
      else cb()
    },
    trigger: 'blur'
  }
]

const skuDialog = reactive({
  visible: false,
  saving: false,
  form: { id: null, skuName: '', price: 1, stock: 0, barcode: '', image: '', status: 'ON' }
})
const stockDialog = reactive({
  visible: false,
  saving: false,
  skuId: null,
  skuName: '',
  changeQty: 0,
  remark: ''
})

function toastSuccess(message) {
  ElMessage.success(message)
}

function formatPrice(v) {
  return Number(v || 0).toFixed(2)
}
function statusText(s) {
  return { ON_SALE: '在售', OFF_SALE: '已下架', PLATFORM_OFF: '平台下架' }[s] || s
}
function statusType(s) {
  return { ON_SALE: 'success', OFF_SALE: 'info', PLATFORM_OFF: 'danger' }[s] || 'info'
}
function addSkuRow() {
  form.skus.push(newSkuRow())
}

async function loadMeta() {
  const [catRes, storeRes] = await Promise.all([
    getCategoryTree().catch(() => ({ data: [] })),
    listStores().catch(() => ({ data: [] }))
  ])
  categories.value = catRes.data || []
  stores.value = storeRes.data || []
  if (!isEdit.value && stores.value.length === 1) {
    form.storeId = stores.value[0].id
  }
}

async function loadDetail() {
  if (!productId.value) return
  pageLoading.value = true
  try {
    const res = await merchantGoodsDetail(productId.value)
    const p = res.data?.product || res.data || {}
    form.categoryId = p.categoryId
    form.name = p.name || ''
    form.mainImage = p.mainImage || ''
    form.detail = p.detail || ''
    productStatus.value = p.status || ''
    skuList.value = res.data?.skus || []
  } finally {
    pageLoading.value = false
  }
}

async function onSave() {
  await formRef.value.validate()
  if (!isEdit.value) {
    if (!form.skus.length) {
      ElMessage.warning('至少需要一个规格')
      return
    }
    const bad = form.skus.find((s) => {
      if (!s.skuName || !s.skuName.trim()) return true
      const price = Number(s.price)
      if (s.price === '' || s.price == null || isNaN(price) || price < 0.01) return true
      const stock = Number(s.stock)
      if (s.stock === '' || s.stock == null || !Number.isInteger(stock) || stock < 0) return true
      return false
    })
    if (bad) {
      ElMessage.warning('请完整填写每个规格的名称、售价和库存')
      return
    }
  }

  saving.value = true
  try {
    if (isEdit.value) {
      await updateGoods({
        productId: productId.value,
        categoryId: form.categoryId,
        name: form.name.trim(),
        mainImage: form.mainImage || null,
        detail: form.detail || null
      })
      toastSuccess('商品信息已更新')
      await loadDetail()
    } else {
      const res = await createGoods({
        storeId: form.storeId,
        categoryId: form.categoryId,
        name: form.name.trim(),
        mainImage: form.mainImage || null,
        detail: form.detail || null,
        onSale: !!form.onSale,
        skus: form.skus.map((s) => ({
          skuName: s.skuName.trim(),
          price: Number(s.price),
          stock: s.stock == null ? 0 : Number(s.stock),
          barcode: s.barcode || null,
          status: 'ON'
        }))
      })
      const newId = res.data?.id ?? res.data?.product?.id
      toastSuccess('商品创建成功')
      if (newId) {
        await router.replace(`/merchant/products/${newId}`)
        await loadDetail()
      } else {
        await router.push('/merchant/products')
      }
    }
  } finally {
    saving.value = false
  }
}

function openSkuDialog(row) {
  if (row) {
    Object.assign(skuDialog.form, {
      id: row.id,
      skuName: row.skuName,
      price: Number(row.price),
      stock: 0,
      barcode: row.barcode || '',
      image: row.image || '',
      status: row.status || 'ON'
    })
  } else {
    Object.assign(skuDialog.form, {
      id: null,
      skuName: '',
      price: 1,
      stock: 10,
      barcode: '',
      image: '',
      status: 'ON'
    })
  }
  skuDialog.visible = true
}

async function saveSku() {
  if (!skuDialog.form.skuName || !skuDialog.form.price) {
    ElMessage.warning('请填写规格名和售价')
    return
  }
  skuDialog.saving = true
  try {
    const body = {
      id: skuDialog.form.id,
      skuName: skuDialog.form.skuName.trim(),
      price: Number(skuDialog.form.price),
      stock: Number(skuDialog.form.stock || 0),
      barcode: skuDialog.form.barcode || null,
      image: skuDialog.form.image || null,
      status: skuDialog.form.status || 'ON'
    }
    if (skuDialog.form.id) {
      await updateSku(productId.value, body)
    } else {
      await addSku(productId.value, body)
    }
    toastSuccess('规格已保存')
    skuDialog.visible = false
    await loadDetail()
  } finally {
    skuDialog.saving = false
  }
}

async function onDeleteSku(row) {
  await ElMessageBox.confirm('确认删除该规格？至少需保留一个规格。', '提示')
  await deleteSku(productId.value, row.id)
  toastSuccess('已删除')
  await loadDetail()
}

function openStockDialog(row) {
  stockDialog.skuId = row.id
  stockDialog.skuName = row.skuName
  stockDialog.changeQty = 0
  stockDialog.remark = ''
  stockDialog.visible = true
}

async function saveStock() {
  const qty = Number(stockDialog.changeQty)
  if (!qty || !Number.isInteger(qty)) {
    ElMessage.warning('请输入非零整数')
    return
  }
  stockDialog.saving = true
  try {
    await adjustStock({
      skuId: stockDialog.skuId,
      changeQty: qty,
      remark: stockDialog.remark
    })
    toastSuccess('库存已调整')
    stockDialog.visible = false
    await loadDetail()
  } finally {
    stockDialog.saving = false
  }
}

onMounted(async () => {
  await loadMeta()
  if (isEdit.value) await loadDetail()
})
</script>

<style scoped>
.sku-editor {
  background: #fafafa;
  border-radius: 8px;
  padding: 8px 8px 0;
  margin-bottom: 8px;
}
</style>
