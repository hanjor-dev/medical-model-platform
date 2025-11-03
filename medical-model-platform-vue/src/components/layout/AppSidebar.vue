<template>
  <aside class="app-sidebar" :class="{ 'sidebar-collapsed': isCollapsed }">
    <nav class="sidebar-nav">
      <ul class="menu-list">
        <!-- 动态渲染菜单项 -->
        <li 
          v-for="menu in filteredMenus" 
          :key="menu.id" 
          class="menu-item"
        >
          <!-- 有子菜单的情况 -->
          <div v-if="menu.children && menu.children.length > 0" class="menu-group">
            <div class="menu-header" 
                 :class="{ expanded: isMenuExpanded(menu.permission) }"
                 @click="toggleSubmenu(menu.permission)"
                 :title="isCollapsed ? menu.title : (isMenuExpanded(menu.permission) ? '点击折叠菜单' : '点击展开菜单（将关闭其他已展开的菜单）')">
              <span class="menu-icon">
                <div v-if="menu.icon && menu.icon.startsWith('<svg')" v-html="menu.icon" class="icon"></div>
                <svg v-else class="icon" viewBox="0 0 1024 1024" width="20" height="20">
                  <path d="M796.601376 533.883517c29.17676 9.213714 56.306028 25.081776 79.340312 45.556695-16.891808 45.044822 5.630603 95.208374 50.675425 112.612055 6.142476 2.047492 12.284951 4.094984 18.427427 4.606857 6.142476 29.688633 6.142476 60.912884 0 90.601517-47.604187 6.654349-80.875931 51.187298-73.709709 98.791485 1.023746 6.654349 2.559365 13.308697 5.11873 19.451173-23.034284 20.474919-49.651679 35.831109-79.340312 45.556695-30.200506-37.366727-85.482787-42.99733-122.849515-12.284951-5.11873 4.094984-9.725587 8.701841-13.82057 13.82057-29.17676-9.213714-56.306028-25.081776-79.340312-45.556695 18.427427-44.532949-3.071238-95.208374-47.092314-113.635801-6.654349-2.559365-13.308697-4.606857-19.963046-5.630603-6.142476-29.688633-6.142476-60.912884 0-90.601517 47.604187-7.166222 80.364058-51.699171 72.685963-99.303358-1.023746-6.142476-2.559365-11.773079-4.606857-17.915554 22.522411-20.474919 49.139806-35.831109 78.316566-45.556695 30.200506 37.366727 84.970914 42.99733 122.337642 12.796824 4.606857-4.094984 9.213714-8.189968 12.796824-12.796824M660.443164 460.685681c-7.678095-1.023746-15.356189-1.023746-22.522411 0-38.902346 12.796824-74.733455 33.271744-105.445834 60.912884-23.546157 20.986792-30.712379 54.770409-18.427427 83.435296 3.071238 7.166222-0.511873 15.356189-7.678095 17.915554-1.023746 0.511873-2.047492 0.511873-3.583111 1.023746-30.712379 4.094984-55.794155 27.129268-62.448503 57.841647-8.701841 40.949838-9.213714 82.923423 0 123.873261 6.142476 29.688633 30.200506 52.211044 59.889138 57.329773 7.678095 0.511873 13.82057 6.654349 13.308698 14.332444v1.535618c-10.749333 28.153014-3.071238 60.401011 19.963046 80.364058 30.712379 27.641141 66.543487 48.11606 105.445834 60.912885 28.664887 9.213714 59.889139 0.511873 79.340311-22.522411 4.606857-6.142476 13.308697-7.166222 19.451174-2.559365 1.023746 0.511873 1.535619 1.535619 2.559365 2.559365-13.82057-15.868062-34.29549-25.593649-55.794155-25.593649" fill="currentColor"/>
                </svg>
                <span class="submenu-indicator"></span>
              </span>
              <span class="menu-title">{{ menu.title }}</span>
              <span class="arrow" :class="{ expanded: isMenuExpanded(menu.permission) }">▼</span>
            </div>
            <ul class="submenu-list" :class="{ open: isMenuExpanded(menu.permission) }">
              <li 
                v-for="submenu in menu.children" 
                :key="submenu.id" 
                class="submenu-item"
              >
                <router-link 
                  :to="submenu.path" 
                  class="submenu-link" 
                  active-class="active" 
                  :title="isCollapsed ? submenu.title : ''"
                  @click.stop="keepMenuExpanded(menu.permission)">
                  {{ submenu.title }}
                </router-link>
              </li>
            </ul>
          </div>
          
          <!-- 没有子菜单的情况 -->
          <router-link 
            v-else 
            :to="menu.path" 
            class="menu-link" 
            active-class="active"
            :title="isCollapsed ? menu.title : ''"
          >
            <span class="menu-icon">
              <div v-if="menu.icon && menu.icon.startsWith('<svg')" v-html="menu.icon" class="icon"></div>
              <svg v-else class="icon" viewBox="0 0 1024 1024" width="20" height="20">
                <path d="M810.666667 213.333333l-85.333334-54.186666V42.666667h85.333334zM810.666667 512h85.333333v-53.717333l-384-268.8-384 268.8V512h85.333333v384h128v-61.056a170.666667 170.666667 0 1 1 341.333334 0V896h128v-384z m85.333333 469.333333h-298.666667v-146.389333a85.333333 85.333333 0 1 0-170.666666 0V981.333333H128v-384H42.666667V413.866667L512 85.333333l469.333333 328.533334V597.333333h-85.333333v384z" fill="currentColor"/>
              </svg>
            </span>
            <span class="menu-title">{{ menu.title }}</span>
          </router-link>
        </li>
      </ul>
    </nav>
    
    <div class="sidebar-footer">
      <div class="menu-controls">
        <button class="collapse-btn" @click="handleSidebarToggle" :title="isCollapsed ? '展开侧边栏' : '折叠侧边栏'">
          {{ isCollapsed ? '▶' : '◀' }}
        </button>
      </div>
    </div>
  </aside>
</template>

<script>
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { usePermissionStore } from '@/stores/permission'
import { useMenuStore } from '@/stores/menu'
import { getIcon } from '@/utils/iconUtils'

export default {
  name: 'AppSidebar',
  props: {
    isCollapsed: {
      type: Boolean,
      default: false
    }
  },
  emits: ['toggle'],
  setup(props, { emit }) {
    const route = useRoute()
    const permissionStore = usePermissionStore()
    const menuStore = useMenuStore()
    
    // 获取过滤后的菜单
    const filteredMenus = computed(() => {
      return permissionStore.getMenuPermissions()
    })
    
    // 检查菜单是否展开
    const isMenuExpanded = (menuKey) => {
      return menuStore.isMenuExpanded(menuKey)
    }
    
    // 切换子菜单展开状态
    const toggleSubmenu = (menuKey) => {
      menuStore.toggleMenuExpanded(menuKey)
    }
    
    // 保持菜单展开状态
    const keepMenuExpanded = (menuKey) => {
      menuStore.keepMenuExpanded(menuKey)
    }
    
    // 处理侧边栏收缩，自动折叠已展开的菜单
    const handleSidebarToggle = () => {
      if (!props.isCollapsed) {
        menuStore.collapseAllMenus()
      }
      emit('toggle')
    }
    
    // 获取菜单图标
    const getMenuIcon = (icon) => {
      return getIcon(icon, { width: 20, height: 20 })
    }
    
    // 使用watch监听权限数据变化，确保数据加载完成后再初始化
    watch(filteredMenus, (newMenus, oldMenus) => {
      if (newMenus.length > 0 && route.path && newMenus !== oldMenus) {
        menuStore.setActiveMenu(route.path)
        menuStore.delayedLoadExpandedState()
      }
    }, { immediate: true })
    
    return {
      filteredMenus,
      isMenuExpanded,
      toggleSubmenu,
      keepMenuExpanded,
      getMenuIcon,
      permissionStore,
      handleSidebarToggle
    }
  }
}
</script>

<style lang="scss" scoped>
/* 侧边栏 */
.app-sidebar {
  grid-area: sidebar;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-right: 1px solid rgba(24, 144, 255, 0.1);
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  overflow: hidden;
  box-shadow: 2px 0 20px rgba(0, 0, 0, 0.08);
  /* 使侧边栏在桌面端固定在视口内，不随右侧内容滚动 */
  position: sticky;
  top: 64px;
  height: calc(100vh - 64px);
  align-self: start;
}

.sidebar-nav {
  flex: 1;
  padding: 20px 0;
  /* 侧边栏自身滚动，避免带动页面布局滚动 */
  overflow-y: auto;
}

.menu-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.menu-item {
  margin-bottom: 4px;
}

.menu-link {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  color: #4a5568;
  text-decoration: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  border-radius: 0 8px 8px 0;
  margin-right: 16px;
  font-weight: 500;
  background: transparent;
}

.menu-link:hover {
  background: rgba(24, 144, 255, 0.08);
  color: #1890ff;
  /* 去掉悬浮动画效果 */
  /* transform: translateX(6px); */
  /* box-shadow: 0 4px 16px rgba(24, 144, 255, 0.15); */
}

.menu-link.active {
  background: rgba(24, 144, 255, 0.08);
  color: #1890ff;
  font-weight: 600 !important;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.08);
}

.menu-link.active .menu-title {
  font-weight: 600 !important;
}

.menu-link.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #1890ff;
  border-radius: 0 2px 2px 0;
}

.menu-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.menu-icon .icon {
  width: 20px;
  height: 20px;
  fill: currentColor;
}

.menu-icon .icon svg {
  width: 20px;
  height: 20px;
  fill: currentColor;
}

.menu-icon .submenu-indicator {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 6px;
  height: 6px;
  background: #1890ff;
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.sidebar-collapsed .menu-icon .submenu-indicator {
  opacity: 1;
}

.menu-link:hover .menu-icon {
  color: #1890ff;
}

.menu-link.active .menu-icon {
  color: #1890ff;
}

.menu-title {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  opacity: 1;
  transition: opacity 0.3s ease;
  flex: 1;
}

.sidebar-collapsed .menu-title {
  opacity: 0;
}

.menu-group {
  cursor: pointer;
}

.menu-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  color: #4a5568;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 0 8px 8px 0;
  margin-right: 16px;
  font-weight: 500;
  background: transparent;
  position: relative;
}

.menu-header:hover {
  background: rgba(24, 144, 255, 0.08);
  color: #1890ff;
  /* 去掉悬浮动画效果 */
  /* transform: translateX(6px); */
  /* box-shadow: 0 4px 16px rgba(24, 144, 255, 0.15); */
}

.menu-header.expanded {
  background: rgba(24, 144, 255, 0.08);
  color: #1890ff;
}

.arrow {
  margin-left: auto;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 12px;
  color: #a0aec0;
  flex-shrink: 0;
}

.arrow.expanded {
  transform: rotate(180deg);
  color: #1890ff;
}

.submenu-list {
  list-style: none;
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(24, 144, 255, 0.02);
  margin: 0 16px 0 0;
  border-radius: 0 8px 8px 0;
  padding: 0;
}

.submenu-list.open {
  max-height: 300px; /* 优化高度，平衡显示效果和动画性能 */
}

.submenu-item {
  margin-bottom: 2px;
}

.submenu-link {
  display: block;
  padding: 12px 24px 12px 64px;
  color: #718096;
  text-decoration: none;
  font-size: 13px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  font-weight: 400;
}

.submenu-link::before {
  content: '';
  position: absolute;
  left: 40px;
  top: 50%;
  transform: translateY(-50%);
  width: 6px;
  height: 6px;
  background: #cbd5e0;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.submenu-link:hover {
  background: rgba(24, 144, 255, 0.08);
  color: #1890ff;
  padding-left: 68px;
  /* 去掉悬浮动画效果 */
  /* transform: translateX(6px); */
  /* box-shadow: 0 4px 16px rgba(24, 144, 255, 0.15); */
}

.submenu-link:hover::before {
  background: #1890ff;
  transform: translateY(-50%) scale(1.2);
}

.submenu-link.active {
  background: rgba(24, 144, 255, 0.08);
  color: #1890ff;
  font-weight: 600 !important;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.08);
}

.submenu-link.active::before {
  content: '';
  position: absolute;
  left: 40px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 16px;
  background: #1890ff;
  border-radius: 2px;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid rgba(24, 144, 255, 0.1);
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(10px);
}

.menu-controls {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.control-btn {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(24, 144, 255, 0.2);
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #718096;
  backdrop-filter: blur(10px);
}

.control-btn:hover {
  background: rgba(24, 144, 255, 0.1);
  border-color: #1890ff;
  color: #1890ff;
  /* 去掉悬浮动画效果 */
  /* transform: scale(1.05); */
  /* box-shadow: 0 4px 16px rgba(24, 144, 255, 0.2); */
}

.collapse-btn {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(24, 144, 255, 0.2);
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #718096;
  backdrop-filter: blur(10px);
}

.collapse-btn:hover {
  background: rgba(24, 144, 255, 0.1);
  border-color: #1890ff;
  color: #1890ff;
  /* 去掉悬浮动画效果 */
  /* transform: scale(1.05); */
  /* box-shadow: 0 4px 16px rgba(24, 144, 255, 0.2); */
}

/* 折叠状态下的样式 */
.sidebar-collapsed .submenu-list {
  display: none !important;
}

.sidebar-collapsed .submenu-list.open {
  display: none !important;
}

.sidebar-collapsed .submenu-list.hover-expand {
  display: block !important;
  position: absolute;
  left: 100%;
  top: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  min-width: 160px;
  z-index: 1000;
  margin: 0;
  padding: 8px 0;
  border: 1px solid #e2e8f0;
}

.sidebar-collapsed .submenu-list.hover-expand .submenu-item {
  margin: 0;
}

.sidebar-collapsed .submenu-list.hover-expand .submenu-link {
  padding: 12px 16px;
  color: #4a5568;
  font-size: 14px;
  border-radius: 0;
  margin: 0 8px;
  border-radius: 6px;
  display: block;
  text-decoration: none;
  transition: all 0.2s ease;
}

.sidebar-collapsed .submenu-list.hover-expand .submenu-link:hover {
  background: rgba(24, 144, 255, 0.08);
  color: #1890ff;
  transform: none;
  padding-left: 16px;
  box-shadow: none;
}

.sidebar-collapsed .submenu-list.hover-expand .submenu-link::before {
  display: none;
}

.sidebar-collapsed .arrow {
  display: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-sidebar {
    /* 移动端以抽屉形式覆盖显示，覆盖sticky行为 */
    position: fixed;
    left: -100%;
    top: 56px;
    width: 100%;
    height: calc(100vh - 56px);
    z-index: 999;
    transition: left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    background: #fafbfc;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  }
  
  .app-sidebar.open {
    left: 0;
  }
}
</style> 