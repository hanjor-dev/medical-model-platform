import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useLayoutStore = defineStore('layout', () => {
  // 状态
  const isSidebarCollapsed = ref(false)
  const isMobileMenuOpen = ref(false)
  
  // 动作
  const toggleSidebar = () => {
    isSidebarCollapsed.value = !isSidebarCollapsed.value
  }
  
  const setSidebarCollapsed = (collapsed) => {
    isSidebarCollapsed.value = collapsed
  }
  
  const toggleMobileMenu = () => {
    isMobileMenuOpen.value = !isMobileMenuOpen.value
  }
  
  const closeMobileMenu = () => {
    isMobileMenuOpen.value = false
  }
  
  const openMobileMenu = () => {
    isMobileMenuOpen.value = true
  }
  
  // 响应式处理
  const handleResize = () => {
    if (window.innerWidth <= 768) {
      isSidebarCollapsed.value = true
      closeMobileMenu()
    }
  }
  
  // 初始化时添加窗口大小监听
  if (typeof window !== 'undefined') {
    window.addEventListener('resize', handleResize)
    handleResize() // 初始检查
  }
  
  return {
    // 状态
    isSidebarCollapsed,
    isMobileMenuOpen,
    
    // 动作
    toggleSidebar,
    setSidebarCollapsed,
    toggleMobileMenu,
    closeMobileMenu,
    openMobileMenu
  }
}) 