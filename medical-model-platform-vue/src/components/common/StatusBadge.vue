<template>
  <span class="status-badge" :class="`status-${normalized}`">
    <i class="dot"></i>
    <slot>{{ labelMap[normalized] || normalized }}</slot>
  </span>
  
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: String, default: 'draft' }
})

const normalized = computed(() => (props.status || 'draft').toLowerCase())
const labelMap = {
  draft: '草稿',
  published: '已发布',
  offline: '已下线',
  scheduled: '计划中',
  failed: '失败'
}
</script>

<style scoped>
.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.2px;
  color: #0f172a;
}
.status-badge .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  box-shadow: 0 0 0 2px rgba(0,0,0,0.04);
}
.status-published { color: #16a34a; }
.status-draft { color: #64748b; }
.status-offline { color: #0f172a; }
.status-scheduled { color: #075985; }
.status-failed { color: #991b1b; }
</style>


