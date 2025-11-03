<template>
  <div class="user-messages-page">
    <el-card class="content-card">
      <template #header>
        <div class="header">
          <h3>
            我的消息
            <el-badge v-if="unreadCount" :value="unreadCount" class="unread-badge"/>
          </h3>
          <div class="ops">
            <el-button class="mark-all-btn" type="text" :disabled="!unreadCount" @click="handleMarkAllRead">
              全部已读
            </el-button>
          </div>
        </div>
      </template>

      <div class="list" v-loading="loading">
        <div v-if="!list.length && !loading" class="empty">
          <EmptyBox title="暂无消息" />
        </div>
        <div v-else class="items">
          <div 
            v-for="item in list" 
            :key="item.id" 
            class="item" 
            :class="{ unread: !item.isRead }"
            @click="openDetail(item)"
          >
            <div class="left">
              <div class="title">
                <span class="type-tag"
                      :style="{ color: typeToStyle(item.messageType).color, backgroundColor: typeToStyle(item.messageType).bg, borderColor: typeToStyle(item.messageType).border }">
                  {{ item.messageTypeLabel || typeToLabel(item.messageType) }}
                </span>
                <span class="msg-title">{{ (item.title || '系统消息') }}</span>
                <span v-if="!item.isRead" class="unread-dot"/>
              </div>
              <div class="content">{{ item.content }}</div>
              <div class="meta">
                <span class="time">{{ formatDateTime(item.createTime) }}</span>
              </div>
            </div>
            <div class="right">
              <el-button
                v-if="!item.isRead"
                class="mark-read-btn"
                type="text"
                @click.stop="markSingleRead(item)"
              >标记已读</el-button>
              <el-icon class="arrow"><ArrowRight/></el-icon>
            </div>
          </div>
        </div>
      </div>

      <div class="pager" v-if="total > 0 && hasMore">
        <el-button :loading="loadingMore" type="primary" plain @click="loadMore">加载更多...</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import EmptyBox from '@/components/common/EmptyBox.vue'
// Pagination removed: switched to Load More
import { formatDateTime } from '@/utils/datetime'
import { messageApi } from '@/api/notify/message'
import { ElMessage } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import { useNotificationStore } from '@/stores/notification'

const router = useRouter()
const store = useNotificationStore()
const loading = ref(false)
const list = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const unreadCount = ref(0)
const loadingMore = ref(false)
const hasMore = computed(() => list.value.length < total.value)

// 视觉样式：仅用于颜色映射（保留，无图标）
const typeToStyle = (t) => {
  const k = String(t || '').toLowerCase()
  if (k.includes('warn') || k.includes('警')) {
    return { color: '#b45309', bg: 'rgba(251, 191, 36, 0.12)', border: 'rgba(180, 83, 9, 0.25)' }
  }
  if (k.includes('notice') || k.includes('通知')) {
    return { color: '#2563eb', bg: 'rgba(37, 99, 235, 0.12)', border: 'rgba(37, 99, 235, 0.25)' }
  }
  if (k.includes('system') || k.includes('系统')) {
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

const fetchList = async (append = false) => {
  loading.value = true
  try {
    const { data } = await messageApi.getMyList({ pageNum: pageNum.value, pageSize: pageSize.value })
    const records = data?.records || []
    list.value = append ? list.value.concat(records) : records
    total.value = data?.total || 0
    // 优先使用后端返回的未读总数（若有其字段），否则页面内统计
    const backendUnread = data?.unreadTotal ?? data?.unreadCount
    unreadCount.value = (typeof backendUnread === 'number') ? backendUnread : records.filter(i => !i.isRead).length
  } finally {
    loading.value = false
  }
}

const openDetail = async (item) => {
  // 先跳转详情，详情页会触发标记已读
  router.push(`/messages/${item.id}`)
}

const handleMarkAllRead = async () => {
  try {
    await store.markAllMessagesAsRead()
    ElMessage.success('已全部设为已读')
    // 重新从后端拉取本页列表，保证一致
    pageNum.value = 1
    await fetchList()
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  }
}

const markSingleRead = async (item) => {
  try {
    await store.markMessageAsRead(item.id)
    item.isRead = true
    // 重新从后端拉取一遍当前页，避免本地与后端不一致
    pageNum.value = 1
    await fetchList()
    ElMessage.success('已标记为已读')
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  }
}

async function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  try {
    pageNum.value += 1
    await fetchList(true)
  } finally {
    loadingMore.value = false
  }
}

onMounted(fetchList)
</script>

<style scoped>
.user-messages-page { padding: 8px 0 12px; display:flex; flex-direction:column; min-height: calc(100vh - var(--app-header-height) - var(--main-padding-v)); }
.user-messages-page .content-card { flex:1 1 auto; min-height:100%; display:flex; flex-direction:column; }
.user-messages-page .content-card :deep(.el-card__body) { flex:1; display:flex; flex-direction:column; min-height:0; }
.content-card { border-radius: 12px; overflow: hidden; }
.header { display:flex; justify-content: space-between; align-items: center; }
.header h3 { display:flex; align-items:center; gap:8px; margin:0; font-weight:700; color:#0f172a; }
.unread-badge :deep(.el-badge__content) { background:#ef4444; }

.list { min-height: 300px; flex:1; display:flex; flex-direction:column; }
.items { display:flex; flex-direction: column; }
.item {
  display:flex; gap:12px; padding:12px 14px; cursor: pointer;
  border-bottom:1px solid #f1f5f9; transition: background 0.2s ease, transform 0.2s ease;
}
.item.unread { background: transparent; }
.item:hover { background:#f8fafc; transform: translateY(-1px); }

.left { flex:1; min-width:0; }
.title { font-weight: 600; color:#0f172a; line-height: 1.35; display:flex; align-items:center; gap:6px; flex-wrap:wrap; }
.title .type-prefix { font-weight: 400; color:#64748b; }
.type-tag { 
  display: inline-flex; align-items:center; gap:6px; 
  height:22px; line-height:22px; padding:0 8px; margin-right:8px;
  font-weight: 500; font-size:12px; border-radius:999px; border:1px solid transparent;
}
.content { margin-top:6px; color:#475569; display:-webkit-box; -webkit-line-clamp:2; line-clamp:2; -webkit-box-orient:vertical; overflow:hidden; }
.meta { margin-top:8px; color:#94a3b8; font-size:12px; display:flex; gap:10px; align-items:center; }
.unread-dot { width:8px; height:8px; border-radius:50%; background:#3b82f6; display:inline-block; }

.right { display:flex; align-items:center; gap:8px; }
.mark-read-btn { color:#1f2937; border:1px solid #e5e7eb; padding:4px 8px; border-radius:8px; }
.mark-read-btn.is-disabled { opacity: 0.5; }

.arrow { color:#cbd5e1; }
.item:hover .arrow { color:#94a3b8; }
.mark-all-btn { color:#1f2937; border:1px solid #e5e7eb; padding:6px 10px; border-radius:8px; }
.mark-all-btn.is-disabled { opacity: 0.5; }

.pager { margin-top: 12px; display:flex; justify-content:center; }
</style>

