<template>
  <div class="page">
    <div class="page-card">
      <div class="flex-between" style="margin-bottom: 16px">
        <h2 style="margin: 0">分类管理</h2>
        <el-button type="primary" @click="openDialog()">新增顶级分类</el-button>
      </div>

      <el-table
        :data="tree"
        v-loading="loading"
        row-key="id"
        default-expand-all
        :tree-props="{ children: 'children' }"
        stripe
      >
        <el-table-column prop="name" label="分类名称" min-width="200" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="level" label="层级" width="80" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="String(row.status) === '1' ? 'success' : 'info'">
              {{ String(row.status) === '1' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="row.level >= 3" @click="openDialog(null, row)">
              加子类
            </el-button>
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialog.visible" :title="dialogTitle" width="440px">
      <el-form :model="dialog.form" label-width="90px">
        <el-form-item label="父分类" v-if="dialog.parentName">
          <el-input :model-value="dialog.parentName" disabled />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="dialog.form.name" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dialog.form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="dialog.form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createCategory,
  deleteCategory,
  getAdminCategoryTree,
  updateCategory
} from '@/api/category'

const loading = ref(false)
const tree = ref([])
const dialog = reactive({
  visible: false,
  saving: false,
  editId: null,
  parentName: '',
  form: { parentId: 0, name: '', sortOrder: 0, status: 1 }
})

const dialogTitle = computed(() => {
  if (dialog.editId) return '编辑分类'
  if (dialog.form.parentId) return '新增子分类'
  return '新增顶级分类'
})

async function load() {
  loading.value = true
  try {
    const res = await getAdminCategoryTree()
    tree.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openDialog(row, parent) {
  if (row) {
    dialog.editId = row.id
    dialog.parentName = ''
    dialog.form = {
      parentId: row.parentId || 0,
      name: row.name,
      sortOrder: row.sortOrder || 0,
      status: Number(row.status ?? 1)
    }
  } else {
    dialog.editId = null
    dialog.parentName = parent ? parent.name : ''
    dialog.form = {
      parentId: parent ? parent.id : 0,
      name: '',
      sortOrder: 0,
      status: 1
    }
  }
  dialog.visible = true
}

async function onSave() {
  if (!dialog.form.name?.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  dialog.saving = true
  try {
    if (dialog.editId) {
      await updateCategory(dialog.editId, dialog.form)
    } else {
      await createCategory(dialog.form)
    }
    ElMessage.success('保存成功')
    dialog.visible = false
    await load()
  } finally {
    dialog.saving = false
  }
}

async function onDelete(row) {
  await ElMessageBox.confirm(`确认删除分类「${row.name}」？需无子分类且无关联商品。`, '提示')
  await deleteCategory(row.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>
