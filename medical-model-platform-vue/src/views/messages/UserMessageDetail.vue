<template>
  <div class="message-detail-page">
    <el-card class="content-card" v-loading="loading">
      <template #header>
        <div class="header">
          <el-button link type="primary" @click="goBack">
            <el-icon style="margin-right:4px"><ArrowLeft/></el-icon>
            返回
          </el-button>
          <div class="spacer"></div>
          <el-button v-if="!detail?.isRead" type="primary" @click="markAsRead">标记已读</el-button>
        </div>
      </template>

      <div v-if="!detail && !loading" class="empty">
        <EmptyBox title="消息不存在或已被删除" />
      </div>
      <div v-else class="body">
        <div class="head">
          <div class="info">
            <h2 class="title">
              <span class="type-tag"
                    :style="{ color: typeToStyle(detail?.messageType).color, backgroundColor: typeToStyle(detail?.messageType).bg, borderColor: typeToStyle(detail?.messageType).border }">
                {{ detail?.messageTypeLabel || typeToLabel(detail?.messageType) }}
              </span>
              {{ detail?.title || '系统消息' }}
            </h2>
            <div class="meta">
              <span class="time">{{ formatDateTime(detail?.createTime) }}</span>
              <span v-if="detail?.isRead" class="read-label"><el-icon style="margin-right:4px"><CircleCheckFilled/></el-icon>已读</span>
            </div>
          </div>
        </div>
        <div class="content-block">
          <div class="content" v-html="sanitizedContent"></div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import EmptyBox from '@/components/common/EmptyBox.vue'
import { messageApi } from '@/api/notify/message'
import { formatDateTime } from '@/utils/datetime'
import { sanitizeHtml } from '@/utils/sanitize'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled, ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const id = route.params.id?.toString()
const loading = ref(false)
const detail = ref(null)

const sanitizedContent = computed(() => sanitizeHtml(detail.value?.content || ''))

// 视觉样式：类型映射（颜色标签用）
const typeToStyle = (t) => {
  const k = String(t || '').toLowerCase()
  if (k.includes('警') || k.includes('warn')) {
    return { color: '#b45309', bg: 'rgba(251, 191, 36, 0.12)', border: 'rgba(180, 83, 9, 0.25)' }
  }
  if (k.includes('通知') || k.includes('notice')) {
    return { color: '#2563eb', bg: 'rgba(37, 99, 235, 0.12)', border: 'rgba(37, 99, 235, 0.25)' }
  }
  if (k.includes('系统') || k.includes('system')) {
    return { color: '#0ea5e9', bg: 'rgba(14, 165, 233, 0.14)', border: 'rgba(14, 165, 233, 0.28)' }
  }
  return { color: '#059669', bg: 'rgba(5, 150, 105, 0.14)', border: 'rgba(5, 150, 105, 0.28)' }
}

// 类型中文标签映射
const typeToLabel = (t) => {
  const k = String(t || '').toLowerCase()
  if (k.includes('warn')) return '警告'
  if (k.includes('notice') || k.includes('通知')) return '通知'
  if (k.includes('system') || k.includes('系统')) return '系统消息'
  if (k.includes('info')) return '信息'
  return '消息'
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await messageApi.getMyDetail(id)
    detail.value = res?.data || null
    // 进入详情后即标记为已读
    if (detail.value && !detail.value.isRead) {
      // eslint-disable-next-line no-empty
      try { await messageApi.markRead(id) } catch {}
      detail.value.isRead = true
    }
  } finally {
    loading.value = false
  }
}

const goBack = () => router.back()

const markAsRead = async () => {
  try {
    await messageApi.markRead(id)
    detail.value.isRead = true
    ElMessage.success('已标记为已读')
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  }
}

onMounted(fetchDetail)
</script>

<style scoped>
.message-detail-page { padding: 8px 0 12px; display:flex; flex-direction:column; min-height: calc(100vh - var(--app-header-height) - var(--main-padding-v)); }
.content-card { border-radius: 12px; overflow: hidden; flex:1 1 auto; min-height:100%; display:flex; flex-direction:column; }
.content-card :deep(.el-card__body) { flex:1; display:flex; flex-direction:column; min-height:0; }
.header { display:flex; align-items:center; }
.spacer { flex:1; }
.body { padding: 6px 2px; flex:1; display:flex; flex-direction:column; }

.head { display:flex; gap:14px; align-items:flex-start; padding-bottom: 8px; border-bottom:1px solid #f1f5f9; }
.info { flex:1; min-width:0; }
.title { margin: 0 0 6px; font-weight: 700; color:#0f172a; }
.type-tag {
  display:inline-flex; align-items:center; gap:6px;
  height:24px; line-height:24px; padding:0 10px; margin-right:10px;
  font-weight:600; font-size:12px; border-radius:999px; border:1px solid transparent;
}
.meta { color:#94a3b8; font-size:12px; display:flex; gap:12px; align-items:center; }
.read-label { display:inline-flex; align-items:center; color:#10b981; }

.content-block { margin-top: 14px; padding: 14px; background: #fbfcfe; border:1px solid #eef2f7; border-radius: 10px; flex:1; }
.content { line-height: 1.75; color:#334155; white-space: pre-wrap; }
</style>

