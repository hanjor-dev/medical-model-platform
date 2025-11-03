<template>
  <div class="main-layout" :class="{ 'sidebar-collapsed': isSidebarCollapsed }">
    <!-- 顶部导航栏 -->
    <AppHeader />
    
    <!-- 侧边栏 -->
    <AppSidebar 
      :is-collapsed="isSidebarCollapsed"
      @toggle="toggleSidebar"
    />
    
    <!-- 主内容区域 -->
    <AppMain />
    
    <!-- 公告弹窗 -->
    <AnnouncementPopup 
      v-model="showAnnouncementPopupValue"
      :announcement="currentAnnouncementValue"
      @confirm="handleAnnouncementConfirm"
      @close="handleAnnouncementClose"
    />
  </div>
</template>

<script>
import { computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useLayoutStore } from '@/stores/layout'
import { useNotificationStore } from '@/stores/notification'
import { useAuthStore } from '@/stores/auth'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AppMain from '@/components/layout/AppMain.vue'
import AnnouncementPopup from '@/components/common/AnnouncementPopup.vue'

export default {
  name: 'AppLayout',
  components: {
    AppHeader,
    AppSidebar,
    AppMain,
    AnnouncementPopup
  },
  setup() {
    const layoutStore = useLayoutStore()
    const router = useRouter()
    const notificationStore = useNotificationStore()
    const authStore = useAuthStore()
    
    const isSidebarCollapsed = computed(() => layoutStore.isSidebarCollapsed)
    const showAnnouncementPopupValue = computed({
      get: () => notificationStore.showAnnouncementPopup.value,
      set: (val) => { notificationStore.showAnnouncementPopup.value = val }
    })
    const currentAnnouncementValue = computed(() => notificationStore.currentAnnouncement.value)
    
    const toggleSidebar = () => {
      layoutStore.toggleSidebar()
    }
    
    const handleAnnouncementConfirm = async (announcement) => {
      try {
        if (announcement?.id) {
          // 先导航到详情
          await router.push({ name: 'AnnouncementDetail', params: { id: announcement.id } })
          // 再标记为已读并刷新未读计数
          await notificationStore.markAnnouncementAsRead(announcement.id)
        }
      } finally {
        // 关闭弹窗
        notificationStore.closeAnnouncementPopup()
      }
    }
    
    const handleAnnouncementClose = (announcement) => {
      console.log('公告关闭:', announcement)
    }
    
    // 监听登录状态变化，只有在用户登录后才初始化WebSocket
    watch(() => authStore.isAuthenticated, (isAuthenticated, wasAuthenticated) => {
      if (isAuthenticated && !wasAuthenticated) {
        // 用户刚登录，初始化通知系统
        console.log('[AppLayout] 用户已登录，初始化通知系统')
        notificationStore.initWebSocket()
        notificationStore.initNotifications()
      } else if (!isAuthenticated && wasAuthenticated) {
        // 用户刚登出，清理通知系统
        console.log('[AppLayout] 用户已登出，清理通知系统')
        notificationStore.resetNotifications()
      }
    }, { immediate: true })
    
    onMounted(() => {
      // 如果用户已经登录，立即初始化通知系统
      if (authStore.isAuthenticated) {
        console.log('[AppLayout] 组件挂载时用户已登录，初始化通知系统')
        notificationStore.initWebSocket()
        notificationStore.initNotifications()
      } else {
        console.log('[AppLayout] 组件挂载时用户未登录，等待登录后初始化通知系统')
      }
    })
    
    onUnmounted(() => {
      // 清理通知系统
      notificationStore.stopWebSocket()
    })
    
    return {
      isSidebarCollapsed,
      showAnnouncementPopupValue,
      currentAnnouncementValue,
      toggleSidebar,
      handleAnnouncementConfirm,
      handleAnnouncementClose
    }
  }
}
</script>

<style lang="scss" scoped>
.main-layout {
  display: grid;
  grid-template-areas: 
    "header header"
    "sidebar main";
  grid-template-columns: 220px 1fr;
  grid-template-rows: 64px 1fr;
  min-height: 100vh; /* 改为最小高度，允许内容扩展 */
  /* 移除 overflow: hidden，允许下拉菜单等弹出层显示 */
  transition: all 0.3s ease;
  
  &.sidebar-collapsed {
    grid-template-columns: 70px 1fr;
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .main-layout {
    grid-template-columns: 200px 1fr;
    
    &.sidebar-collapsed {
      grid-template-columns: 70px 1fr;
    }
  }
}

@media (max-width: 768px) {
  .main-layout {
    grid-template-areas: 
      "header"
      "main";
    grid-template-columns: 1fr;
    grid-template-rows: 56px 1fr;
  }
}
</style> 