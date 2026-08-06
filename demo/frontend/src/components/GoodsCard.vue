<template>
  <div class="card" @click="$emit('click')">
    <div class="cover">
      <img :src="cover" :alt="item.name" @error="onImgError" />
      <el-tag v-if="item.status && item.status !== 'ON_SALE'" size="small" class="badge" type="info">
        {{ statusText }}
      </el-tag>
    </div>
    <div class="body">
      <div class="name" :title="item.name">{{ item.name }}</div>
      <div class="meta text-muted">
        <span v-if="item.categoryName">{{ item.categoryName }}</span>
        <span v-if="item.skuCount != null">{{ item.skuCount }} 规格</span>
      </div>
      <div class="flex-between">
        <div class="price">
          <template v-if="item.minPrice != null">
            ¥{{ formatPrice(item.minPrice) }}
            <span v-if="item.maxPrice != null && item.maxPrice !== item.minPrice" class="text-muted">
              起
            </span>
          </template>
          <template v-else>—</template>
        </div>
        <span class="text-muted" v-if="item.totalAvailableStock != null">
          库存 {{ item.totalAvailableStock }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  item: { type: Object, required: true }
})
defineEmits(['click'])

const placeholder = 'data:image/svg+xml,' + encodeURIComponent(
  '<svg xmlns="http://www.w3.org/2000/svg" width="300" height="300"><rect fill="#f2f3f5" width="300" height="300"/><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" fill="#c0c4cc" font-size="16">暂无图片</text></svg>'
)

const cover = computed(() => props.item.mainImage || placeholder)

const statusText = computed(() => {
  const m = { ON_SALE: '在售', OFF_SALE: '已下架', PLATFORM_OFF: '平台下架' }
  return m[props.item.status] || props.item.status
})

function formatPrice(v) {
  return Number(v).toFixed(2)
}

function onImgError(e) {
  e.target.src = placeholder
}
</script>

<style scoped>
.card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
  border: 1px solid #ebeef5;
}
.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}
.cover {
  position: relative;
  aspect-ratio: 1;
  background: #f5f7fa;
}
.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.badge {
  position: absolute;
  top: 8px;
  left: 8px;
}
.body {
  padding: 12px;
}
.name {
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;
  height: 42px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.meta {
  margin: 6px 0 8px;
  display: flex;
  gap: 10px;
}
</style>
