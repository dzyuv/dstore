<template>
  <div class="page">
    <div class="page-card">
      <div class="flex-between" style="margin-bottom: 16px">
        <h2 style="margin: 0">收货地址</h2>
        <el-button type="primary" @click="openDialog()">新增地址</el-button>
      </div>

      <el-empty v-if="!loading && !list.length" description="暂无收货地址" />

      <div v-loading="loading" class="address-grid">
        <div v-for="addr in list" :key="addr.id" class="addr-card" :class="{ selected: addr.isDefault }">
          <div class="addr-header">
            <span class="addr-name">{{ addr.receiverName }}</span>
            <span class="addr-phone">{{ addr.phone }}</span>
            <el-tag v-if="addr.isDefault" size="small" type="primary">默认</el-tag>
          </div>
          <div class="addr-body">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</div>
          <div class="addr-actions">
            <el-button link type="primary" @click="openDialog(addr)">编辑</el-button>
            <el-button v-if="!addr.isDefault" link type="success" @click="doSetDefault(addr.id)">设为默认</el-button>
            <el-button link type="danger" @click="doDelete(addr.id)">删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 地址弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑地址' : '新增地址'"
      width="520px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="form.receiverName" placeholder="收货人姓名" maxlength="32" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="手机号" maxlength="11" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="省份" prop="province">
              <el-input v-model="form.province" placeholder="省" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="城市" prop="city">
              <el-input v-model="form.city" placeholder="市" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="区/县" prop="district">
              <el-input v-model="form.district" placeholder="区/县" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="form.detail" placeholder="街道、门牌号等" maxlength="128" />
        </el-form-item>
        <el-form-item label="默认地址">
          <el-switch v-model="form.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listAddresses, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/address'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const editingId = ref(null)
const isEdit = ref(false)

const form = reactive({
  receiverName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
})

const rules = {
  receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区/县', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

async function fetchList() {
  loading.value = true
  try {
    const res = await listAddresses()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openDialog(addr) {
  if (addr) {
    editingId.value = addr.id
    isEdit.value = true
    Object.assign(form, {
      receiverName: addr.receiverName,
      phone: addr.phone,
      province: addr.province,
      city: addr.city,
      district: addr.district,
      detail: addr.detail,
      isDefault: !!addr.isDefault
    })
  } else {
    editingId.value = null
    isEdit.value = false
    Object.assign(form, {
      receiverName: '',
      phone: '',
      province: '',
      city: '',
      district: '',
      detail: '',
      isDefault: false
    })
  }
  dialogVisible.value = true
}

async function onSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    const data = { ...form }
    if (editingId.value) {
      await updateAddress(editingId.value, data)
      ElMessage.success('地址已更新')
    } else {
      await addAddress(data)
      ElMessage.success('地址已添加')
    }
    dialogVisible.value = false
    await fetchList()
  } finally {
    saving.value = false
  }
}

async function doDelete(id) {
  await ElMessageBox.confirm('确认删除该地址？', '提示')
  await deleteAddress(id)
  ElMessage.success('已删除')
  await fetchList()
}

async function doSetDefault(id) {
  await setDefaultAddress(id)
  ElMessage.success('已设为默认地址')
  await fetchList()
}

onMounted(() => fetchList())
</script>

<style scoped>
.address-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.addr-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  transition: border-color 0.2s;
}
.addr-card:hover {
  border-color: #409eff;
}
.addr-card.selected {
  border-color: #409eff;
  background: #ecf5ff;
}
.addr-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.addr-name {
  font-weight: 600;
  font-size: 15px;
}
.addr-phone {
  color: #909399;
}
.addr-body {
  color: #606266;
  font-size: 14px;
  margin-bottom: 10px;
  line-height: 1.5;
}
.addr-actions {
  display: flex;
  gap: 4px;
}
@media (max-width: 768px) {
  .address-grid {
    grid-template-columns: 1fr;
  }
}
</style>
