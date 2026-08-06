<template>
  <div class="page">
    <div class="page-card search-bar">
      <el-input
        v-model="query.keyword"
        clearable
        placeholder="搜索商品名称"
        class="keyword"
        @keyup.enter="load(1)"
      >
        <template #append>
          <el-button :icon="Search" @click="load(1)" />
        </template>
      </el-input>
      <el-select v-model="query.categoryId" clearable placeholder="全部分类" style="width: 200px" @change="load(1)">
        <el-option
          v-for="c in flatCategories"
          :key="c.id"
          :label="c.label"
          :value="c.id"
        />
      </el-select>
      <el-button type="primary" @click="load(1)">搜索</el-button>
    </div>

    <div class="content">
      <aside class="side page-card" v-loading="catLoading">
        <div class="side-title">商品分类</div>
        <el-tree
          :data="categories"
          node-key="id"
          :props="{ label: 'name', children: 'children' }"
          highlight-current
          default-expand-all
          @node-click="onCategoryClick"
        />
      </aside>

      <section class="list-area">
        <div class="flex-between list-head">
          <span class="text-muted">共 {{ total }} 件在售商品</span>
          <el-radio-group v-model="pageSize" size="small" @change="load(1)">
            <el-radio-button :value="8">8/页</el-radio-button>
            <el-radio-button :value="12">12/页</el-radio-button>
            <el-radio-button :value="20">20/页</el-radio-button>
          </el-radio-group>
        </div>
        <div v-loading="loading">
          <div v-if="list.length" class="goods-grid">
            <GoodsCard
              v-for="item in list"
              :key="item.id"
              :item="item"
              @click="$router.push(`/goods/${item.id}`)"
            />
          </div>
          <el-empty v-else description="暂无上架商品" />
        </div>
        <div class="pager" v-if="total > 0">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            :current-page="query.page"
            @current-change="load"
          />
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import GoodsCard from '@/components/GoodsCard.vue'
import { searchGoods } from '@/api/goods'
import { getCategoryTree } from '@/api/category'

const loading = ref(false)
const catLoading = ref(false)
const list = ref([])
const total = ref(0)
const categories = ref([])
const pageSize = ref(12)
const query = reactive({ keyword: '', categoryId: null, page: 1 })

const flatCategories = computed(() => {
  const result = []
  const walk = (nodes, prefix = '') => {
    for (const n of nodes || []) {
      result.push({ id: n.id, label: prefix + n.name })
      if (n.children?.length) walk(n.children, prefix + n.name + ' / ')
    }
  }
  walk(categories.value)
  return result
})

function onCategoryClick(node) {
  query.categoryId = node.id
  load(1)
}

async function loadCategories() {
  catLoading.value = true
  try {
    const res = await getCategoryTree()
    categories.value = res.data || []
  } finally {
    catLoading.value = false
  }
}

async function load(page = 1) {
  query.page = page
  loading.value = true
  try {
    const res = await searchGoods({
      keyword: query.keyword || undefined,
      categoryId: query.categoryId || undefined,
      page: query.page,
      size: pageSize.value
    })
    const data = res.data || {}
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCategories()
  load(1)
})
</script>

<style scoped>
.search-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}
.keyword {
  flex: 1;
}
.content {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 16px;
}
.side-title {
  font-weight: 600;
  margin-bottom: 12px;
}
.list-head {
  margin-bottom: 12px;
}
.pager {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
@media (max-width: 800px) {
  .content {
    grid-template-columns: 1fr;
  }
  .side {
    display: none;
  }
}
</style>
