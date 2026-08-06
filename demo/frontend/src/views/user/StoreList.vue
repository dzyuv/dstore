<template>
  <div class="page">
    <div class="page-card" v-loading="loading">
      <div class="flex-between" style="margin-bottom: 16px">
        <div>
          <h2 style="margin: 0">门店管理</h2>
          <p class="text-muted" style="margin: 6px 0 0">
            发布商品前请先创建门店。仅审核通过的商家可管理门店。
          </p>
        </div>
        <div>
          <el-button @click="load">刷新</el-button>
          <el-button type="primary" @click="openDialog()">新增门店</el-button>
        </div>
      </div>

      <el-empty v-if="!list.length && !loading" description="暂无门店，请先新增">
        <el-button type="primary" @click="openDialog()">新增门店</el-button>
      </el-empty>

      <el-table v-else :data="list" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="storeName" label="门店名称" min-width="140" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="businessHours" label="营业时间" width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="Number(row.status) === 1 ? 'success' : 'info'" size="small">
              {{ Number(row.status) === 1 ? '营业' : '休息' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button
              link
              :type="Number(row.status) === 1 ? 'warning' : 'success'"
              @click="onToggleStatus(row)"
            >
              {{ Number(row.status) === 1 ? '设为休息' : '设为营业' }}
            </el-button>
            <el-button link type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editId ? '编辑门店' : '新增门店'"
      width="520px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="门店名称" prop="storeName">
          <el-input v-model="form.storeName" maxlength="64" placeholder="如：张三食品旗舰店" />
        </el-form-item>
        <el-form-item label="门店地址" prop="address">
          <el-input v-model="form.address" maxlength="200" placeholder="详细地址" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" maxlength="20" placeholder="门店联系电话" />
        </el-form-item>
        <el-form-item label="营业时间" prop="businessHours">
          <el-input v-model="form.businessHours" placeholder="如：9:00-22:00" />
        </el-form-item>
        <el-form-item label="Logo URL">
          <el-input v-model="form.logo" placeholder="可选，图片地址" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">营业</el-radio>
            <el-radio :value="0">休息</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listStores,
  createStore,
  updateStore,
  deleteStore as deleteStoreApi
} from '@/api/merchant'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const editId = ref(null)
const formRef = ref()

const form = reactive({
  storeName: '',
  address: '',
  phone: '',
  businessHours: '',
  logo: '',
  status: 1
})

const rules = {
  storeName: [{ required: true, message: '请输入门店名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入门店地址', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

function resetForm() {
  form.storeName = ''
  form.address = ''
  form.phone = ''
  form.businessHours = ''
  form.logo = ''
  form.status = 1
  editId.value = null
}

async function load() {
  loading.value = true
  try {
    const res = await listStores()
    list.value = res.data || []
  } catch (e) {
    // 非商家账号会报「商家账号不存在或未审核通过」
    list.value = []
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  if (row) {
    editId.value = row.id
    form.storeName = row.storeName || ''
    form.address = row.address || ''
    form.phone = row.phone || ''
    form.businessHours = row.businessHours || ''
    form.logo = row.logo || ''
    form.status = row.status == null ? 1 : Number(row.status)
  } else {
    resetForm()
  }
  dialogVisible.value = true
}

async function onSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (editId.value) {
      await updateStore({
        storeId: editId.value,
        storeName: form.storeName,
        address: form.address,
        phone: form.phone,
        businessHours: form.businessHours,
        logo: form.logo || null,
        status: Number(form.status)
      })
      ElMessage.success('门店已更新')
    } else {
      await createStore({
        storeName: form.storeName,
        address: form.address,
        phone: form.phone,
        businessHours: form.businessHours,
        logo: form.logo || null,
        status: Number(form.status)
      })
      ElMessage.success('门店已创建')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function onToggleStatus(row) {
  const next = Number(row.status) === 1 ? 0 : 1
  await updateStore({
    storeId: row.id,
    status: next
  })
  ElMessage.success(next === 1 ? '已设为营业' : '已设为休息')
  await load()
}

async function onDelete(row) {
  await ElMessageBox.confirm(`确认删除门店「${row.storeName}」？删除后不可恢复。`, '提示', {
    type: 'warning'
  })
  await deleteStoreApi(row.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>
