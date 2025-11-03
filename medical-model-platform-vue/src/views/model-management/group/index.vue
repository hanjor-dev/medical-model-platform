<template>
  <div class="model-group-page">

    <!-- 页面内容 -->
    <div class="page-content">
      <el-card class="content-card">
        <template #header>
          <AppBreadcrumb />
        </template>
        <div class="development-notice">
          <el-empty description="页面开发中">
            <template #image>
              <el-icon :size="80" color="#909399">
                <Collection />
              </el-icon>
            </template>
            <p class="notice-text">模型分组管理页面正在开发中，敬请期待...</p>
            <p class="notice-text">功能包括：分组创建、分组编辑、分组删除等</p>
          </el-empty>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { defineComponent } from 'vue'
import { Collection } from '@element-plus/icons-vue'
import AppBreadcrumb from '@/components/common/Breadcrumb.vue'

export default defineComponent({
  name: 'ModelGroup',
  components: {
    Collection,
    AppBreadcrumb
  },
  setup() {
    // 页面逻辑将在这里实现
    return {}
  }
})
</script>

<style lang="scss" scoped>
.model-group-page {
  padding: 8px 0 12px;
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - var(--app-header-height) - var(--main-padding-v));
  
  .page-header {
    margin-bottom: 20px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 10px 0;
    }
    
    .page-breadcrumb {
      .el-breadcrumb {
        font-size: 14px;
      }
    }
  }
  
  .page-content {
    flex: 1;
    display: flex;
    min-height: 0;
    .content-card {
      flex: 1 1 auto;
      width: 100%;
      min-height: 100%;
      display: flex;
      flex-direction: column;
      :deep(.el-card__body) {
        flex: 1;
        display: flex;
        flex-direction: column;
        min-height: 0;
      }
      .development-notice {
        text-align: center;
        padding: 40px 20px;
        
        .notice-text {
          color: #909399;
          margin: 10px 0;
          font-size: 14px;
        }
      }
    }
  }
}
</style> 