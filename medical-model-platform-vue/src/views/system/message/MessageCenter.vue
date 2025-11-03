<template>
  <div class="message-center-page" v-role="['SUPER_ADMIN']">
    <div class="page-content">
      <el-card class="content-card">
        <template #header>
          <AppBreadcrumb />
        </template>

        <el-tabs v-model="activeTab" class="message-center__tabs" type="card">
          <el-tab-pane label="公告管理" name="announcement">
            <div class="message-center__content">
              <AnnouncementTab />
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { usePermissionStore } from '@/stores/permission'
import AppBreadcrumb from '@/components/common/Breadcrumb.vue'
import AnnouncementTab from './tabs/AnnouncementTab.vue'

const activeTab = ref('announcement')

const permissionStore = usePermissionStore()
onMounted(() => {
  permissionStore.debugPermissions?.()
})
</script>

<style scoped>
.message-center-page {
  padding: 8px 0 12px;
  background: transparent;
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - var(--app-header-height) - var(--main-padding-v));
}

.message-center-page .page-content { flex: 1; display: flex; min-height: 0; }

.content-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  width: 100%;
}

.content-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  min-height: 0;
}

.message-center__tabs {
  margin-top: 4px;
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  min-height: 0;
}

.message-center__tabs :deep(.el-tabs__content) {
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  min-height: 0;
}

.message-center__tabs :deep(.el-tab-pane) {
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  min-height: 0;
}

.message-center__content {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  min-height: 0;
  flex: 1 1 auto;
}
</style>


