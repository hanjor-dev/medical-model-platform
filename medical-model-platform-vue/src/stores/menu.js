import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

export const useMenuStore = defineStore('menu', () => {
  // 状态
  const activeMenu = ref('')
  const expandedMenus = ref(new Set())
  
  // 从localStorage恢复展开状态
  const loadExpandedState = () => {
    try {
      const saved = localStorage.getItem('menu-expanded-state')
      if (saved) {
        const expandedArray = JSON.parse(saved)
        
        // 验证数据有效性
        if (Array.isArray(expandedArray) && expandedArray.every(item => typeof item === 'string')) {
          console.log('从localStorage恢复菜单展开状态:', expandedArray)
          expandedMenus.value = new Set(expandedArray)
        } else {
          console.warn('localStorage中的菜单展开状态数据格式无效，清理损坏数据')
          clearExpandedState()
        }
      }
    } catch (error) {
      console.warn('无法加载菜单展开状态，清理损坏数据:', error)
      clearExpandedState()
    }
  }
  
  // 清理localStorage中的菜单展开状态
  const clearExpandedState = () => {
    try {
      localStorage.removeItem('menu-expanded-state')
      console.log('已清理localStorage中的损坏菜单展开状态数据')
    } catch (error) {
      console.error('清理localStorage失败:', error)
    }
  }
  
  // 检查菜单状态是否正常
  const isMenuStateValid = () => {
    return expandedMenus.value instanceof Set && expandedMenus.value.size >= 0
  }
  
  // 获取菜单状态摘要信息
  const getMenuStateSummary = () => {
    return {
      activeMenu: activeMenu.value,
      expandedMenusCount: expandedMenus.value.size,
      expandedMenus: Array.from(expandedMenus.value),
      isStateValid: isMenuStateValid()
    }
  }
  
  // 重置菜单状态
  const resetMenuState = () => {
    console.log('重置菜单状态')
    activeMenu.value = ''
    expandedMenus.value.clear()
    clearExpandedState()
  }
  
  // 验证菜单状态一致性
  const validateMenuState = () => {
    const issues = []
    
    // 检查expandedMenus是否为Set类型
    if (!(expandedMenus.value instanceof Set)) {
      issues.push('expandedMenus不是Set类型')
    }
    
    // 检查activeMenu是否为字符串
    if (typeof activeMenu.value !== 'string') {
      issues.push('activeMenu不是字符串类型')
    }
    
    // 检查localStorage数据一致性
    try {
      const saved = localStorage.getItem('menu-expanded-state')
      if (saved) {
        const savedArray = JSON.parse(saved)
        const currentArray = Array.from(expandedMenus.value)
        
        if (JSON.stringify(savedArray.sort()) !== JSON.stringify(currentArray.sort())) {
          issues.push('localStorage与内存状态不一致')
        }
      }
    } catch (error) {
      issues.push(`localStorage验证失败: ${error.message}`)
    }
    
    if (issues.length > 0) {
      console.warn('菜单状态验证发现问题:', issues)
      return false
    }
    
    console.log('菜单状态验证通过')
    return true
  }
  
  // 修复菜单状态问题
  const fixMenuState = () => {
    console.log('开始修复菜单状态...')
    
    if (!validateMenuState()) {
      console.log('检测到菜单状态问题，进行修复...')
      
      // 尝试从localStorage恢复
      try {
        loadExpandedState()
        if (validateMenuState()) {
          console.log('菜单状态修复成功')
          return true
        }
      } catch (error) {
        console.error('从localStorage恢复失败:', error)
      }
      
      // 如果恢复失败，重置状态
      console.log('恢复失败，重置菜单状态')
      resetMenuState()
      return false
    }
    
    return true
  }
  
  // 保存展开状态到localStorage
  const saveExpandedState = () => {
    try {
      const expandedArray = Array.from(expandedMenus.value)
      localStorage.setItem('menu-expanded-state', JSON.stringify(expandedArray))
      console.log('菜单展开状态已保存到localStorage:', expandedArray)
    } catch (error) {
      console.warn('无法保存菜单展开状态:', error)
    }
  }
  
  // 延迟恢复菜单展开状态（等待菜单结构准备好）
  const delayedLoadExpandedState = () => {
    // 使用更智能的检测方式，而不是硬编码延迟
    const checkAndLoad = () => {
      // 检查是否有权限数据，如果有则说明菜单结构已就绪
      if (typeof window !== 'undefined' && localStorage.getItem('user-menu-permissions')) {
        loadExpandedState()
        console.log('菜单结构已就绪，恢复展开状态完成')
      } else {
        // 如果菜单结构还没准备好，继续等待
        console.log('菜单结构未就绪，继续等待...')
        setTimeout(checkAndLoad, 50)
      }
    }
    
    console.log('开始智能检测菜单结构就绪状态...')
    checkAndLoad()
  }
  
  // 初始化时不自动加载，而是提供手动加载方法
  // loadExpandedState()
  
  // 监听展开状态变化，自动保存
  watch(expandedMenus, saveExpandedState, { deep: true })
  
  // 计算属性
  const isMenuActive = computed(() => (menuPath) => {
    return activeMenu.value === menuPath
  })
  
  const isMenuExpanded = computed(() => (menuKey) => {
    return expandedMenus.value.has(menuKey)
  })
  
  // 动作
  const setActiveMenu = (menuPath) => {
    activeMenu.value = menuPath
  }
  
  // 用户手动切换菜单展开状态
  const toggleMenuExpanded = (menuKey) => {
    if (expandedMenus.value.has(menuKey)) {
      // 如果当前菜单已展开，则折叠它
      expandedMenus.value.delete(menuKey)
      console.log('用户手动折叠菜单:', menuKey)
    } else {
      // 如果当前菜单未展开，则展开它，并关闭其他已展开的菜单
      // 实现手风琴效果：同时只能展开一个一级菜单
      console.log('用户手动展开菜单，关闭其他菜单:', menuKey)
      
      // 先关闭所有已展开的菜单
      expandedMenus.value.clear()
      
      // 再展开当前菜单
      expandedMenus.value.add(menuKey)
      console.log('手风琴效果：关闭其他菜单，展开当前菜单:', menuKey)
    }
  }
  
  // 用户手动保持菜单展开（点击二级菜单时调用）
  const keepMenuExpanded = (menuKey) => {
    // 确保菜单保持展开状态，不受任何自动逻辑影响
    console.log('保持菜单展开，状态已锁定:', menuKey)
    
    // 如果当前菜单未展开，则展开它，但不关闭其他菜单
    // 这样可以保持用户之前手动展开的其他菜单状态
    if (!expandedMenus.value.has(menuKey)) {
      console.log('展开父级菜单:', menuKey)
      expandedMenus.value.add(menuKey)
    }
    
    console.log('菜单展开状态确认:', { menuKey, isExpanded: expandedMenus.value.has(menuKey) })
    console.log('当前所有展开的菜单:', Array.from(expandedMenus.value))
  }
  
  // 展开所有菜单
  const expandAllMenus = (menuPermissions = []) => {
    menuPermissions.forEach(menu => {
      if (menu.children && menu.children.length > 0) {
        expandedMenus.value.add(menu.permission)
        console.log('展开所有菜单:', menu.permission)
      }
    })
  }
  
  // 手风琴效果：展开指定菜单，关闭其他菜单
  const expandMenuAccordion = (menuKey) => {
    console.log('手风琴效果：展开菜单，关闭其他菜单:', menuKey)
    
    // 先关闭所有已展开的菜单
    expandedMenus.value.clear()
    
    // 再展开指定菜单
    expandedMenus.value.add(menuKey)
    
    console.log('手风琴效果执行完成:', { expandedMenu: menuKey, allExpanded: Array.from(expandedMenus.value) })
  }
  
  // 折叠所有菜单
  const collapseAllMenus = () => {
    console.log('收缩菜单栏，折叠所有菜单')
    expandedMenus.value.clear()
  }
  
  // 设置菜单展开状态（仅供外部明确调用，不用于自动逻辑）
  const setMenuExpanded = (menuKey, expanded) => {
    if (expanded) {
      expandedMenus.value.add(menuKey)
      console.log('外部设置菜单展开:', menuKey)
    } else {
      expandedMenus.value.delete(menuKey)
      console.log('外部设置菜单折叠:', menuKey)
    }
  }
  
  // 通用的路由到菜单映射逻辑
  const findMenuByPath = (path, menuPermissions) => {
    // 递归查找匹配路径的菜单
    const findInMenu = (menus, targetPath) => {
      for (const menu of menus) {
        if (menu.path === targetPath) {
          return menu
        }
        if (menu.children && menu.children.length > 0) {
          const found = findInMenu(menu.children, targetPath)
          if (found) return found
        }
      }
      return null
    }
    
    return findInMenu(menuPermissions, path)
  }
  
  // 查找父级菜单
  const findParentMenu = (targetMenu, allMenus) => {
    const findParent = (menus, targetId) => {
      for (const menu of menus) {
        if (menu.children && menu.children.length > 0) {
          const hasChild = menu.children.some(child => child.id === targetId)
          if (hasChild) {
            return menu
          }
          const found = findParent(menu.children, targetId)
          if (found) return found
        }
      }
      return null
    }
    
    return findParent(allMenus, targetMenu.id)
  }
  
  return {
    // 状态
    activeMenu,
    expandedMenus,
    
    // 计算属性
    isMenuActive,
    isMenuExpanded,
    
    // 动作
    setActiveMenu,
    toggleMenuExpanded,
    setMenuExpanded,
    keepMenuExpanded,
    expandAllMenus,
    collapseAllMenus,
    expandMenuAccordion,
    
    // 工具方法
    loadExpandedState,
    delayedLoadExpandedState,
    saveExpandedState,
    findMenuByPath,
    findParentMenu,
    isMenuStateValid,
    getMenuStateSummary,
    resetMenuState,
    validateMenuState,
    fixMenuState
  }
}) 