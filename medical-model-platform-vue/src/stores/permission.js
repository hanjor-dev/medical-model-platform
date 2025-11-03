/**
 * 权限管理Store：管理用户权限、菜单权限和按钮权限
 * 
 * 功能描述：
 * 1. 存储和管理用户权限信息
 * 2. 提供权限验证方法
 * 3. 根据权限过滤菜单
 * 4. 支持动态权限更新
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const usePermissionStore = defineStore('permission', () => {
  // 状态
  const permissions = ref([]) // 完整权限对象列表
  const menuPermissions = ref([]) // 菜单权限
  const buttonPermissions = ref([]) // 按钮权限
  const permissionTree = ref({}) // 权限树形结构
  const userRole = ref('') // 用户角色

  // 从localStorage恢复权限数据
  const loadPermissionsFromStorage = () => {
    try {
      const storedPermissions = localStorage.getItem('user-permissions')
      const storedRole = localStorage.getItem('user-role')
      const storedMenuPermissions = localStorage.getItem('user-menu-permissions')
      
      if (storedPermissions && storedRole && storedMenuPermissions) {
        permissions.value = JSON.parse(storedPermissions)
        userRole.value = JSON.parse(storedRole)
        menuPermissions.value = JSON.parse(storedMenuPermissions)
        
        // 重新构建权限树
        permissionTree.value = buildPermissionTree(permissions.value)
        return true
      }
    } catch (error) {
      console.warn('从localStorage恢复权限数据失败:', error)
      clearPermissionsFromStorage()
    }
    return false
  }
  
  // 保存权限数据到localStorage
  const savePermissionsToStorage = () => {
    try {
      if (permissions.value.length > 0 && userRole.value) {
        localStorage.setItem('user-permissions', JSON.stringify(permissions.value))
        localStorage.setItem('user-role', JSON.stringify(userRole.value))
        localStorage.setItem('user-menu-permissions', JSON.stringify(menuPermissions.value))
      }
    } catch (error) {
      console.error('保存权限数据到localStorage失败:', error)
    }
  }
  
  // 清除localStorage中的权限数据
  const clearPermissionsFromStorage = () => {
    try {
      localStorage.removeItem('user-permissions')
      localStorage.removeItem('user-role')
      localStorage.removeItem('user-menu-permissions')
      console.log('localStorage中的权限数据已清除')
    } catch (error) {
      console.error('清除localStorage权限数据失败:', error)
    }
  }
  
  // 初始化时尝试从localStorage恢复权限数据
  // 注意：这里不自动调用，而是在需要时手动调用
  // loadPermissionsFromStorage()
  
  // 提供一个初始化方法
  const initializeFromStorage = () => {
    return loadPermissionsFromStorage()
  }

  // 计算属性
  const hasMenuPermission = computed(() => (menuPath) => {
    return menuPermissions.value.some(menu => menu.path === menuPath)
  })

  const hasButtonPermission = computed(() => (buttonCode) => {
    return buttonPermissions.value.some(btn => btn.code === buttonCode)
  })

  // 获取用户权限
  const getUserPermissions = () => permissions.value

  // 获取用户角色
  const getUserRole = () => userRole.value

  // 设置权限信息
  const setPermissions = (userData) => {
    console.log('开始设置权限信息:', userData)
    
    try {
      const { role, permissions: userPermissions } = userData  // 重命名参数，避免冲突
      
      if (!userPermissions || !Array.isArray(userPermissions)) {
        console.error('权限数据无效:', userPermissions)
        return
      }
      
      console.log('设置角色和权限:', {
        role,
        permissionsCount: userPermissions.length,
        permissions: userPermissions.map(p => ({ id: p.id, code: p.code, name: p.name, type: p.type }))
      })
      
      // 先清空现有数据，避免数据混乱
      permissions.value = []
      menuPermissions.value = []
      buttonPermissions.value = []
      permissionTree.value = {}
      userRole.value = ''
      
      // 设置角色
      userRole.value = role
      
      // 统一规范化ID类型为字符串，避免后端Long序列化为字符串导致的类型不一致
      const normalizedPermissions = userPermissions.map(p => ({
        ...p,
        id: p.id !== null && p.id !== undefined ? String(p.id) : p.id,
        parentId: p.parentId !== null && p.parentId !== undefined ? String(p.parentId) : p.parentId
      }))

      // 设置权限列表 - 使用规范化后的数据
      permissions.value = [...normalizedPermissions]
      
      // 分离按钮权限
      buttonPermissions.value = normalizedPermissions.filter(perm => perm.type === 'BUTTON')
      
      // 构建权限树
      permissionTree.value = buildPermissionTree(normalizedPermissions)
      
      // 构建菜单结构
      const menuStructure = buildMenuFromPermissions(normalizedPermissions)
      console.log('构建的菜单结构:', menuStructure)
      
      // 设置菜单权限
      menuPermissions.value = [...menuStructure] // 使用展开运算符创建新数组
      
      // 验证数据设置是否成功
      console.log('权限数据设置验证:', {
        roleSet: userRole.value,
        permissionsSet: permissions.value.length,
        menuPermissionsSet: menuPermissions.value.length,
        buttonPermissionsSet: buttonPermissions.value.length
      })
      
      // 自动保存到localStorage
      savePermissionsToStorage()
      
      console.log('权限信息设置完成:', {
        role: userRole.value,
        totalPermissions: permissions.value.length,
        menuPermissions: menuPermissions.value.length,
        buttonPermissions: buttonPermissions.value.length,
        menuStructure: menuPermissions.value
      })
      
    } catch (error) {
      console.error('设置权限信息时出错:', error)
      // 出错时清空权限数据
      clearPermissions()
    }
  }

  // 构建权限树形结构
  const buildPermissionTree = (permissions) => {
    const tree = {}
    
    permissions.forEach(permission => {
      if (permission.parentId === null) {
        // 顶级权限
        tree[permission.id] = {
          ...permission,
          children: []
        }
      } else {
        // 子权限
        const parent = findPermissionById(permissions, permission.parentId)
        if (parent) {
          if (!parent.children) parent.children = []
          parent.children.push(permission)
        }
      }
    })
    
    return tree
  }

  // 根据ID查找权限
  const findPermissionById = (permissions, id) => {
    return permissions.find(p => p.id === id)
  }

  // 根据权限对象列表构建菜单结构
  const buildMenuFromPermissions = (permissions) => {
    console.log('开始构建菜单，输入权限数据:', permissions)
    
    const menuPerms = permissions.filter(p => p.type === 'MENU')
    console.log('过滤后的菜单权限:', menuPerms)
    
    // 检查每个权限的parentId字段
    menuPerms.forEach(perm => {
      console.log(`权限 ${perm.name} (${perm.code}):`, {
        id: perm.id,
        parentId: perm.parentId,
        hasParentId: 'parentId' in perm,
        parentIdType: typeof perm.parentId
      })
    })
    
    // 构建顶级菜单 - 处理可能不存在的parentId字段
    const topLevelMenus = menuPerms
      .filter(menu => {
        // 如果parentId字段不存在，或者parentId为null/undefined，则认为是顶级菜单
        const isTopLevel = !('parentId' in menu) || menu.parentId === null || menu.parentId === undefined
        console.log(`菜单 ${menu.name}: parentId=${menu.parentId}, isTopLevel=${isTopLevel}`)
        return isTopLevel
      })
      .map(menu => ({
        id: menu.id,
        path: menu.path,
        title: menu.name,  // 使用 'name' 而不是 'permissionName'
        icon: menu.icon,
        permission: menu.code,  // 使用 'code' 而不是 'permissionCode'
        sort: menu.sort || 0,
        children: generateSubMenus(menuPerms, menu.id)
      }))
      .sort((a, b) => (a.sort || 0) - (b.sort || 0))
    
    console.log('构建完成的顶级菜单:', topLevelMenus)
    return topLevelMenus
  }

  // 生成子菜单
  const generateSubMenus = (permissions, parentId) => {
    const subMenus = permissions
      .filter(p => {
        // 检查是否有parentId字段，并且值等于指定的parentId
        const hasParentId = 'parentId' in p
        const isChild = hasParentId && p.parentId === parentId
        console.log(`检查子菜单 ${p.name}: hasParentId=${hasParentId}, parentId=${p.parentId}, targetParentId=${parentId}, isChild=${isChild}`)
        return isChild
      })
      .map(menu => ({
        id: menu.id,
        path: menu.path,
        title: menu.name,  // 使用 'name' 而不是 'permissionName'
        permission: menu.code,  // 使用 'code' 而不是 'permissionCode'
        sort: menu.sort || 0
      }))
      .sort((a, b) => (a.sort || 0) - (b.sort || 0))
    
    console.log(`为父菜单ID ${parentId} 生成的子菜单:`, subMenus)
    return subMenus
  }

  // 检查单个权限
  const checkPermission = (permissionCode) => {
    console.log('开始权限检查:', {
      permissionCode,
      totalPermissions: permissions.value.length,
      availablePermissions: permissions.value.map(p => ({ code: p.code, name: p.name, type: p.type }))
    })
    
    const hasPermission = permissions.value.some(p => p.code === permissionCode)
    
    console.log('权限检查结果:', {
      permissionCode,
      hasPermission,
      matchedPermission: permissions.value.find(p => p.code === permissionCode)
    })
    
    return hasPermission
  }

  // 检查任意权限
  const checkAnyPermission = (permissionCodes) => {
    if (!Array.isArray(permissionCodes)) {
      permissionCodes = [permissionCodes]
    }
    return permissionCodes.some(code => checkPermission(code))
  }

  // 检查所有权限
  const checkAllPermissions = (permissionCodes) => {
    if (!Array.isArray(permissionCodes)) {
      permissionCodes = [permissionCodes]
    }
    return permissionCodes.every(code => checkPermission(code))
  }

  // 根据权限code获取权限对象
  const getPermissionByCode = (permissionCode) => {
    return permissions.value.find(p => p.code === permissionCode)
  }

  // 获取权限树
  const getPermissionTree = () => permissionTree.value

  // 清除权限信息
  const clearPermissions = () => {
    permissions.value = []
    menuPermissions.value = []
    buttonPermissions.value = []
    permissionTree.value = {}
    userRole.value = ''
    
    // 同时清除localStorage中的权限数据
    clearPermissionsFromStorage()
  }

  // 获取菜单权限列表
  const getMenuPermissions = () => menuPermissions.value

  // 获取按钮权限列表
  const getButtonPermissions = () => buttonPermissions.value

  // 检查权限数据是否就绪
  const isPermissionsReady = () => {
    return permissions.value.length > 0 && menuPermissions.value.length > 0
  }
  
  // 等待权限数据就绪
  const waitForPermissions = async (timeout = 2000) => {
    const startTime = Date.now()
    
    while (!isPermissionsReady() && (Date.now() - startTime) < timeout) {
      await new Promise(resolve => setTimeout(resolve, 50))
    }
    
    return isPermissionsReady()
  }

  // 检查权限数据一致性
  const validatePermissionsConsistency = () => {
    return userRole.value && permissions.value.length > 0 && menuPermissions.value.length > 0
  }

  // 调试方法：打印当前权限状态
  const debugPermissions = () => {
    console.log('权限Store状态:', {
      userRole: userRole.value,
      permissionsCount: permissions.value.length,
      menuPermissionsCount: menuPermissions.value.length
    })
  }

  return {
    // 状态
    permissions,
    menuPermissions,
    buttonPermissions,
    permissionTree,
    userRole,
    
    // 计算属性
    hasMenuPermission,
    hasButtonPermission,
    
    // 方法
    getUserPermissions,
    getUserRole,
    setPermissions,
    checkPermission,
    checkAnyPermission,
    checkAllPermissions,
    getPermissionByCode,
    getPermissionTree,
    clearPermissions,
    getMenuPermissions,
    getButtonPermissions,
    isPermissionsReady,
    waitForPermissions,
    validatePermissionsConsistency,
    
    // 权限持久化方法
    loadPermissionsFromStorage,
    savePermissionsToStorage,
    clearPermissionsFromStorage,
    initializeFromStorage,
    debugPermissions
  }
}) 