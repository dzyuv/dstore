<template>
  <div class="page">
    <el-card class="page-card">
      <h2>地址管理</h2>
      <el-button type="primary" @click="dialogVisible = true" style="margin-bottom: 16px">
        新增地址
      </el-button>
      <el-table :data="list" stripe>
        <el-table-column label="收货人" prop="receiverName" />
        <el-table-column label="手机号" prop="phone" />
        <el-table-column label="地址" prop="detail" />
        <el-table-column label="默认" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault" type="success" size="small">默认</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="editAddress(row)">编辑</el-button>
            <el-button link type="danger" @click="deleteAddress(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑地址弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑地址' : '新增地址'" width="500px">
      <el-form :model="addressForm" :rules="rules" ref="addressFormRef">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addressForm.phone" />
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model="addressForm.province" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="addressForm.city" />
        </el-form-item>
        <el-form-item label="区/县" prop="district">
          <el-input v-model="addressForm.district" />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="addressForm.detail" />
        </el-form-item>
        <el-form-item label="设为默认" prop="isDefault">
          <el-switch v-model="addressForm.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSaveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { listAddresses, addAddress, updateAddress, deleteAddress } from '@/api/address'

const list = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const addressForm = reactive({
  receiverName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
})
const rules = { /* 省略规则 */ }
const addressFormRef = ref()

onMounted(load)

async function load() {
  const res = await listAddresses()
  list.value = res.data || []
}

async function onSaveAddress() {
  saving.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateAddress(editId.value, addressForm)
    } else {
      await addAddress(addressForm)
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function deleteAddress(id) {
  await ElMessageBox.confirm('确认删除该地址？')
  await deleteAddress(id)
  await load()
}

function openDialog(address) {
  isEdit.value = !!address
  editId.value = address ? address.id : null
  addressForm.receiverName = address ? address.receiverName : ''
  addressForm.phone = address ? address.phone : ''
  addressForm.province = address ? address.province : ''
  addressForm.city = address ? address.city : ''
  addressForm.district = address ? address.district : ''
  addressForm.detail = address ? address.detail : ''
  addressForm.isDefault = address ? address.isDefault : false
  dialogVisible.value = true
}
</script>
