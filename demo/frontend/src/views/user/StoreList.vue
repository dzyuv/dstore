<template>
  <div class="page">
    <el-card class="page-card">
      <h2>我的门店</h2>
      <el-button type="primary" @click="openStoreDialog = true">新增门店</el-button>
      <el-table :data="list" stripe>
        <el-table-column prop="storeName" label="门店名称" />
        <el-table-column prop="address" label="地址" />
        <el-table-column prop="phone" label="联系方式" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '营业' : '休息' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="editStore(row)">编辑</el-button>
            <el-button link type="danger" @click="deleteStore(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑门店弹窗 -->
    <el-dialog v-model="storeDialog.visible" :title="storeDialog.editId ? '编辑门店' : '新增门店'" width="500px">
      <el-form :model="storeDialog.form" ref="storeFormRef">
        <el-form-item label="门店名称" prop="storeName">
          <el-input v-model="storeDialog.form.storeName" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="storeDialog.form.address" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="storeDialog.form.phone" />
        </el-form-item>
        <el-form-item label="营业时间">
          <el-input v-model="storeDialog.form.businessHours" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="storeDialog.form.status">
            <el-option label="营业" value="1" />
            <el-option label="休息" value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="storeDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="storeDialog.saving" @click="saveStore">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listStores, createStore, updateStore, deleteStore } from '@/api/merchant'

const list = ref([])
const storeDialog = reactive({
  visible: false,
  editId: null,
  saving: false,
  form: { storeName: '', address: '', phone: '', businessHours: '', status: '1' }
})

onMounted(load)

async function load() {
  const res = await listStores()
  list.value = res.data || []
}

async function openStoreDialog(store) {
  if (store) {
    storeDialog.editId = store.id
    storeDialog.form = { ...store }
  } else {
    storeDialog.editId = null
    storeDialog.form = { storeName: '', address: '', phone: '', businessHours: '', status: '1' }
  }
  storeDialog.visible = true
}

async function saveStore() {
  storeDialog.saving = true
  try {
    if (storeDialog.editId) {
      await updateStore(storeDialog.form)
    } else {
      await createStore(storeDialog.form)
    }
    ElMessage.success('操作成功')
    storeDialog.visible = false
    await load()
  } finally {
    storeDialog.saving = false
  }
}

async function deleteStore(id) {
  await ElMessageBox.confirm('确认删除该门店？')
  await deleteStore(id)
  await load()
}
</script>
