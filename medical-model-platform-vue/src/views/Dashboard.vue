<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h1>控制台</h1>
      <p>欢迎使用医疗影像模型管理平台</p>
    </div>
    
    <!-- 权限调试信息 -->
    <div class="debug-section" v-if="showDebug">
      <h3>权限调试信息</h3>
      <div class="debug-content">
        <div class="debug-item">
          <strong>认证状态:</strong> {{ authStore.isAuthenticated ? '已认证' : '未认证' }}
        </div>
        <div class="debug-item">
          <strong>Token:</strong> {{ authStore.getToken() ? '存在' : '不存在' }}
        </div>
        <div class="debug-item">
          <strong>用户权限数量:</strong> {{ permissionStore.getUserPermissions().length }}
        </div>
        <div class="debug-item">
          <strong>菜单权限数量:</strong> {{ permissionStore.getMenuPermissions().length }}
        </div>
        <div class="debug-item">
          <strong>当前路由:</strong> {{ $route.path }}
        </div>
        <div class="debug-item">
          <strong>路由权限要求:</strong> {{ $route.meta.permission || '无' }}
        </div>
        <div class="debug-item">
          <strong>权限检查结果:</strong> 
          <span v-if="$route.meta.permission" :class="permissionStore.checkPermission($route.meta.permission) ? 'success' : 'error'">
            {{ permissionStore.checkPermission($route.meta.permission) ? '通过' : '失败' }}
          </span>
          <span v-else>无要求</span>
        </div>
      </div>
      <button @click="showDebug = false" class="debug-toggle">隐藏调试信息</button>
    </div>
    
    <div class="dashboard-content">
      <div class="stats-grid">
        <!-- 公告中心卡片入口 -->
        <div class="stat-card clickable" @click="goAnnouncementCenter">
          <div class="stat-icon">📢</div>
          <div class="stat-content">
            <h3>公告中心</h3>
            <p>查看平台公告与通知</p>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">📊</div>
          <div class="stat-content">
            <h3>数据统计</h3>
            <p>查看平台使用数据和统计信息</p>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon">⚙️</div>
          <div class="stat-content">
            <h3>系统配置</h3>
            <p>管理系统配置和参数设置</p>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon">👥</div>
          <div class="stat-content">
            <h3>用户管理</h3>
            <p>管理用户账户和权限设置</p>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon">🔐</div>
          <div class="stat-content">
            <h3>权限管理</h3>
            <p>配置角色和权限规则</p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 调试按钮 -->
    <button @click="showDebug = true" class="debug-toggle" v-if="!showDebug">显示调试信息</button>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { usePermissionStore } from '@/stores/permission'
import { useAuthStore } from '@/stores/auth'

export default {
  name: 'DashboardPage',
  setup() {
    const router = useRouter()
    const permissionStore = usePermissionStore()
    const authStore = useAuthStore()
    const showDebug = ref(false)
    
    // 获取用户权限信息
    const userPermissions = computed(() => {
      return permissionStore.getUserPermissions()
    })
    
    // 获取菜单权限信息
    const menuPermissions = computed(() => {
      return permissionStore.getMenuPermissions()
    })
    
    // 检查特定权限
    const hasPermission = (permissionCode) => {
      return permissionStore.checkPermission(permissionCode)
    }
    
    // 快捷操作函数
    const createUser = () => {
      console.log('创建用户')
      // 实现创建用户逻辑
    }
    
    const uploadModel = () => {
      console.log('上传模型')
      // 实现上传模型逻辑
    }
    
    const systemConfig = () => {
      console.log('系统配置')
      // 实现系统配置逻辑
    }
    
    const adminPanel = () => {
      console.log('管理面板')
      // 实现管理面板逻辑
    }
    
    const goAnnouncementCenter = () => {
      router.push({ name: 'AnnouncementCenter' })
    }
    
    return {
      permissionStore,
      authStore,
      userPermissions,
      menuPermissions,
      hasPermission,
      createUser,
      uploadModel,
      systemConfig,
      adminPanel,
      goAnnouncementCenter,
      showDebug
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.dashboard-header {
  text-align: center;
  margin-bottom: 32px;
  
  h1 {
    font-size: 2.5rem;
    color: #2c3e50;
    margin-bottom: 8px;
  }
  
  p {
    font-size: 1.1rem;
    color: #7f8c8d;
  }
}

.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: transform 0.2s ease;
  
  &:hover {
    transform: translateY(-2px);
  }
  
  &.clickable { cursor: pointer; }
  
  .stat-icon {
    font-size: 2.5rem;
  }
  
  .stat-content {
    h3 {
      margin: 0 0 8px 0;
      color: #7f8c8d;
      font-size: 0.9rem;
      font-weight: 500;
    }
    
    p {
      margin: 0;
      font-size: 0.9rem;
      color: #7f8c8d;
    }
  }
}

.quick-actions {
  h2 {
    margin-bottom: 20px;
    color: #2c3e50;
    font-size: 1.5rem;
  }
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 1rem;
  font-weight: 500;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
  }
  
  .action-icon {
    font-size: 2rem;
  }
  
  &.primary {
    background: #3498db;
    color: white;
    
    &:hover {
      background: #2980b9;
    }
  }
  
  &.success {
    background: #2ecc71;
    color: white;
    
    &:hover {
      background: #27ae60;
    }
  }
  
  &.warning {
    background: #f39c12;
    color: white;
    
    &:hover {
      background: #e67e22;
    }
  }
  
  &.danger {
    background: #e74c3c;
    color: white;
    
    &:hover {
      background: #c0392b;
    }
  }
}

.recent-activities {
  h2 {
    margin-bottom: 20px;
    color: #2c3e50;
    font-size: 1.5rem;
  }
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  
  .activity-icon {
    font-size: 1.5rem;
  }
  
  .activity-content {
    flex: 1;
    
    p {
      margin: 0 0 4px 0;
      color: #2c3e50;
    }
    
    .activity-time {
      font-size: 0.85rem;
      color: #7f8c8d;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .actions-grid {
    grid-template-columns: 1fr;
  }
  
  .dashboard-header h1 {
    font-size: 2rem;
  }
}

/* 调试信息样式 */
.debug-section {
  background-color: #f9f9f9;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

  h3 {
    margin-top: 0;
    margin-bottom: 15px;
    color: #34495e;
    font-size: 1.2rem;
  }

  .debug-content {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin-bottom: 15px;
  }

  .debug-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 15px;
    background-color: #ecf0f1;
    border-radius: 8px;
    font-size: 0.9rem;
    color: #34495e;

    strong {
      font-weight: 600;
      color: #2c3e50;
    }

    .success {
      color: #2ecc71;
      font-weight: bold;
    }

    .error {
      color: #e74c3c;
      font-weight: bold;
    }
  }

  .debug-toggle {
    background-color: #3498db;
    color: white;
    padding: 10px 20px;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    font-size: 0.9rem;
    font-weight: 500;
    transition: background-color 0.2s ease;

    &:hover {
      background-color: #2980b9;
    }
  }
}
</style> 