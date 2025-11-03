<template>
  <div id="app">
    <!-- 路由视图 -->
    <router-view />
    
    <!-- 全局消息提示组件 -->
    <MessageToast />
  </div>
</template>

<script>
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import MessageToast from '@/components/MessageToast.vue'

export default {
  name: 'App',
  components: {
    MessageToast
  },
  setup() {
    const authStore = useAuthStore()
    
    // 应用启动时检查认证状态
    onMounted(async () => {
      if (authStore.getToken()) {
        console.log('应用启动，检查认证状态...')
        
        // 使用智能获取方法，优先使用缓存
        await authStore.getOrFetchUserInfo()
      }
    })
  }
}
</script>

<style>
#app {
  height: 100vh;
  /* 移除 overflow: hidden，允许下拉菜单等弹出层显示 */
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

/* 全局样式重置 */
* {
  box-sizing: border-box;
}

html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  /* 移除 overflow: hidden，允许下拉菜单等弹出层显示 */
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
