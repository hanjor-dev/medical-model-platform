<template>
  <div class="notification-bell" @click="toggleDropdown">
    <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="bell-badge">
      <el-icon class="bell-icon" :class="{ 'has-notification': unreadCount > 0 }">
        <Bell />
      </el-icon>
    </el-badge>
    
    <!-- 消息下拉列表 -->
    <el-popover
      v-model:visible="dropdownVisible"
      placement="bottom-end"
      :width="280"
      trigger="manual"
      :teleported="true"
      :popper-options="{ strategy: 'fixed' }"
      popper-class="notification-popover"
    >
      <template #reference>
        <div></div>
      </template>
      
      <div class="notification-dropdown">
        <div class="dropdown-header">
          <h4>消息通知</h4>
          <el-button 
            v-if="unreadCount > 0" 
            type="primary" link
            size="small"
            @click="markAllAsRead"
          >
            全部已读
          </el-button>
        </div>
        
        <div class="notification-list">
          <div v-if="notifications.length === 0" class="empty-state">
            <el-icon><Bell /></el-icon>
            <p>暂无消息</p>
          </div>
          
          <div 
            v-for="notification in notifications" 
            :key="notification.id"
            class="notification-item"
            :class="{ 'unread': !notification.isRead }"
            @click="handleNotificationClick(notification)"
          >
            <div class="notification-content">
              <div class="notification-title">
                {{ notification.title || '系统消息' }}
                <span v-if="!notification.isRead" class="unread-dot"></span>
              </div>
              <div class="notification-preview">{{ notification.content }}</div>
              <div class="notification-time">{{ formatTime(notification.createTime) }}</div>
            </div>
            <div class="tail">
            </div>
          </div>
        </div>
        
        <div class="dropdown-footer">
          <el-button type="primary" link @click="viewAllMessages">查看全部</el-button>
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'
// message list is fetched via store to keep single data source
import { useNotificationStore } from '@/stores/notification'

export default {
  name: 'NotificationBell',
  components: {
    Bell
  },
  setup() {
    const router = useRouter()
    const dropdownVisible = ref(false)
    const store = useNotificationStore()
    const notifications = computed(() => (store.notifications.value || []).slice(0, 3))
    const unreadCount = computed(() => store.unreadMessageCount.value)

    // 类型中文标签映射
    const typeToLabel = (t) => {
      const k = String(t || '').toLowerCase()
      if (k.includes('warn')) return '警告'
      if (k.includes('notice') || k.includes('通知')) return '通知'
      if (k.includes('system') || k.includes('系统')) return '系统消息'
      if (k.includes('info')) return '信息'
      return '消息'
    }

    // 获取消息列表
    const fetchNotifications = async () => {
      try {
        await store.fetchMessages()
      } catch (error) {
        console.error('获取消息列表失败:', error)
      }
    }

    // 标记所有消息为已读
    const markAllAsRead = async () => {
      try {
        await store.markAllMessagesAsRead()
        ElMessage.success('已全部设为已读')
      } catch (error) {
        console.error('标记已读失败:', error)
        ElMessage.error('操作失败')
      }
    }

    // 处理消息点击
    const handleNotificationClick = async (notification) => {
      if (!notification.isRead) {
        try {
          await store.markMessageAsRead(notification.id)
        } catch (e) {
          notification.isRead = true
        }
      }
      
      // 跳转到消息详情
      router.push(`/messages/${notification.id}`)
      dropdownVisible.value = false
    }

    // 查看全部消息
    const viewAllMessages = () => {
      router.push('/messages')
      dropdownVisible.value = false
    }

    // 切换下拉框显示
    const toggleDropdown = () => {
      dropdownVisible.value = !dropdownVisible.value
      if (dropdownVisible.value) {
        fetchNotifications()
      }
    }

    // 格式化时间
    const formatTime = (time) => {
      if (!time) return ''
      const date = new Date(time)
      const now = new Date()
      const diff = now - date
      
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
      return date.toLocaleDateString()
    }

    onMounted(() => {
      // WebSocket 初始化已由 AppLayout 统一管理，这里只加载消息
      fetchNotifications()
    })


    return {
      dropdownVisible,
      notifications,
      unreadCount,
      toggleDropdown,
      markAllAsRead,
      handleNotificationClick,
      viewAllMessages,
      formatTime,
      typeToLabel
    }
  }
}
</script>

<style lang="scss" scoped>
.notification-bell {
  position: relative;
  cursor: pointer;
  
  .bell-badge {
    .bell-icon {
      font-size: 20px;
      color: #606266;
      transition: color 0.3s;
      
      &.has-notification {
        color: #409eff;
      }
    }
  }
}

.notification-dropdown {
  .dropdown-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    border-bottom: 1px solid #f1f5f9;
    
    h4 {
      margin: 0;
      font-size: 14px;
      font-weight: 700;
      color: #0f172a;
    }
  }
  
  .notification-list {
    max-height: 300px;
    overflow-y: auto;
    
    .empty-state {
      text-align: center;
      padding: 40px 20px;
      color: #909399;
      
      .el-icon {
        font-size: 32px;
        margin-bottom: 8px;
      }
      
      p {
        margin: 0;
        font-size: 14px;
      }
    }
    
    .notification-item {
      display: flex;
      align-items: center;
      padding: 12px 16px;
      border-bottom: 1px solid #f1f5f9;
      cursor: pointer;
      transition: background 0.2s ease;
      
      &:hover {
        background: #f8fafc;
      }
      
      &.unread { background: transparent; }
      
      .notification-content {
        flex: 1;
        
        .notification-title {
          font-size: 14px;
          color: #0f172a;
          margin-bottom: 4px;
          line-height: 1.4;
          font-weight: 700;
          display:flex; align-items:center; gap:6px; flex-wrap:wrap;
        }
        
        .notification-time {
          font-size: 12px;
          color: #94a3b8;
        }
        .notification-preview {
          margin: 0 0 6px;
          color: #475569;
          font-size: 13px;
          display: -webkit-box;
          -webkit-line-clamp: 1;
          line-clamp: 1;
          -webkit-box-orient: vertical;
          overflow: hidden;
          word-break: break-word;
        }
      }
      .tail { display:flex; align-items:center; gap:8px; }
      .unread-dot { width:8px; height:8px; border-radius:50%; background:#3b82f6; display:inline-block; }
    }
  }
  
  .dropdown-footer {
    padding: 12px 16px;
    border-top: 1px solid #f1f5f9;
    text-align: center;
  }
}

:deep(.notification-popover) {
  padding: 0;
}
</style>

