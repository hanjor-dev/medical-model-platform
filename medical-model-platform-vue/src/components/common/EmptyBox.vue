<template>
  <div ref="rootEl" class="empty-box" :class="[sizeClass]" :style="containerStyle">
    <el-empty :description="title || '暂无数据'">
      <template #description>
        <div class="empty-title">{{ title || '暂无数据' }}</div>
        <div v-if="desc" class="empty-desc">{{ desc }}</div>
      </template>
      <div class="empty-actions" v-if="$slots.actions">
        <slot name="actions" />
      </div>
    </el-empty>

    <div v-if="skeleton || normalizedRows > 0" class="skeletons">
      <el-skeleton :rows="normalizedRows" animated />
    </div>
  </div>
  
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'

// eslint-disable-next-line no-undef
const props = defineProps({
  title: { type: String, default: '暂无数据' },
  desc: { type: String, default: '' },
  // 已移除刷新重试按钮，外部可通过 actions 插槽自定义
  // 兼容旧API：icon 已不再使用，保留避免外部报错
  icon: { type: [Boolean, String], default: 'box' },
  // size: table | card | full
  size: { type: String, default: 'card' },
  // skeleton placeholders
  skeleton: { type: Boolean, default: false },
  skeletonRows: { type: Number, default: 0 }
})

// 无默认交互事件

const sizeClass = computed(() => {
  const v = String(props.size || 'card').toLowerCase()
  if (v === 'table') return 'size-table'
  if (v === 'full') return 'size-full'
  return 'size-card'
})

const viewportHeight = ref(typeof window !== 'undefined' ? window.innerHeight : 800)
const rootEl = ref(null)

const getCssVarPx = (name, fallback = 0) => {
  if (typeof window === 'undefined') return fallback
  const val = getComputedStyle(document.documentElement).getPropertyValue(name).trim()
  if (!val) return fallback
  const n = parseFloat(val)
  return Number.isFinite(n) ? n : fallback
}

// 基于组件距视口顶部的 offset 动态计算剩余可视高度
const boxMinHeight = computed(() => {
  if (props.size !== 'full') return 0
  const rect = rootEl.value ? rootEl.value.getBoundingClientRect() : { top: 0 }
  const safeBottomPadding = getCssVarPx('--main-padding-v', 24)
  const available = Math.max(0, viewportHeight.value - (rect.top || 0) - safeBottomPadding)
  return available
})

const containerStyle = computed(() => {
  if (props.size === 'full') {
    return { minHeight: boxMinHeight.value + 'px' }
  }
  if (props.size === 'card') {
    return { minHeight: '240px' }
  }
  if (props.size === 'table') {
    return { minHeight: '160px' }
  }
  return {}
})

const onResize = () => { viewportHeight.value = typeof window !== 'undefined' ? window.innerHeight : viewportHeight.value }
let ro = null

onMounted(() => {
  window.addEventListener('resize', onResize)
  onResize()
  if (typeof ResizeObserver !== 'undefined') {
    ro = new ResizeObserver(() => { onResize() })
    // 监听自身及父容器变化，确保布局变动时能重算高度
    if (rootEl.value) {
      ro.observe(rootEl.value)
      if (rootEl.value.parentElement) ro.observe(rootEl.value.parentElement)
    }
    ro.observe(document.body)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  if (ro) {
    try { ro.disconnect() }
    // eslint-disable-next-line no-empty
    catch (e) {}
    ro = null
  }
})

const normalizedRows = computed(() => {
  const n = Number(props.skeletonRows || 0)
  if (props.skeleton && n === 0) return 6
  return Math.max(0, n)
})

// 兼容旧样式：已改为使用 el-skeleton
</script>

<style scoped>
.empty-box { padding: 40px 16px; text-align: center; color: #64748b; background:#fff; border-radius: 8px; display:flex; flex-direction:column; align-items:center; justify-content:center; }
.empty-box.size-card { min-height: 240px; }
.empty-box.size-table { background: transparent; border-radius: 0; padding: 24px 12px; min-height: 160px; }
.empty-title { color:#0f172a; font-weight:800; font-size: 16px; }
.empty-desc { margin-top: 4px; }
.empty-actions { margin-top: 12px; }
/* 内置按钮已移除，保留样式不再需要 */

/* skeletons */
.skeletons { width: 100%; max-width: 920px; margin-top: 16px; padding: 0 8px; }
</style>


