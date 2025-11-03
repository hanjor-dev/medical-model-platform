/**
 * 权限指令：支持组件级权限控制
 * 
 * 功能描述：
 * 1. v-permission 指令：根据权限显示/隐藏元素
 * 2. v-role 指令：根据角色显示/隐藏元素
 * 3. 支持单个权限、多个权限、角色验证
 * 4. 自动集成权限Store
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15
 */

import { usePermissionStore } from '@/stores/permission'
import { hasPermission, hasAnyPermission, hasRole } from '@/utils/permission'

/**
 * 权限指令
 * 用法：
 * v-permission="'user:read'"           // 单个权限
 * v-permission="['user:read', 'user:write']"  // 多个权限（OR逻辑）
 * v-permission="{ permission: 'user:read', mode: 'AND' }"  // 详细配置
 */
export const permission = {
  mounted(el, binding) {
    const permissionStore = usePermissionStore()
    const userPermissions = permissionStore.getUserPermissions()
    
    let hasAccess = false
    const value = binding.value
    
    if (typeof value === 'string') {
      // 单个权限
      hasAccess = hasPermission(value, userPermissions)
    } else if (Array.isArray(value)) {
      // 多个权限（OR逻辑）
      hasAccess = hasAnyPermission(value, userPermissions)
    } else if (typeof value === 'object' && value.permission) {
      // 详细配置
      if (Array.isArray(value.permission)) {
        if (value.mode === 'AND') {
          hasAccess = value.permission.every(perm => hasPermission(perm, userPermissions))
        } else {
          hasAccess = hasAnyPermission(value.permission, userPermissions)
        }
      } else {
        hasAccess = hasPermission(value.permission, userPermissions)
      }
    }
    
    if (!hasAccess) {
      // 没有权限时隐藏元素
      el.style.display = 'none'
    }
  },
  
  updated(el, binding) {
    // 权限更新时重新检查
    const permissionStore = usePermissionStore()
    const userPermissions = permissionStore.getUserPermissions()
    
    let hasAccess = false
    const value = binding.value
    
    if (typeof value === 'string') {
      hasAccess = hasPermission(value, userPermissions)
    } else if (Array.isArray(value)) {
      hasAccess = hasAnyPermission(value, userPermissions)
    } else if (typeof value === 'object' && value.permission) {
      if (Array.isArray(value.permission)) {
        if (value.mode === 'AND') {
          hasAccess = value.permission.every(perm => hasPermission(perm, userPermissions))
        } else {
          hasAccess = hasAnyPermission(value.permission, userPermissions)
        }
      } else {
        hasAccess = hasPermission(value.permission, userPermissions)
      }
    }
    
    if (hasAccess) {
      el.style.display = ''
    } else {
      el.style.display = 'none'
    }
  }
}

/**
 * 角色指令
 * 用法：
 * v-role="'ADMIN'"           // 单个角色
 * v-role="['ADMIN', 'SUPER_ADMIN']"  // 多个角色（OR逻辑）
 */
export const role = {
  mounted(el, binding) {
    const permissionStore = usePermissionStore()
    const userRole = permissionStore.getUserRole()
    
    let hasAccess = false
    const value = binding.value
    
    if (typeof value === 'string') {
      hasAccess = hasRole(value, userRole)
    } else if (Array.isArray(value)) {
      hasAccess = hasRole(value, userRole)
    }
    
    if (!hasAccess) {
      el.style.display = 'none'
    }
  },
  
  updated(el, binding) {
    const permissionStore = usePermissionStore()
    const userRole = permissionStore.getUserRole()
    
    let hasAccess = false
    const value = binding.value
    
    if (typeof value === 'string') {
      hasAccess = hasRole(value, userRole)
    } else if (Array.isArray(value)) {
      hasAccess = hasRole(value, userRole)
    }
    
    if (hasAccess) {
      el.style.display = ''
    } else {
      el.style.display = 'none'
    }
  }
}

/**
 * 按钮权限指令
 * 用法：
 * v-button-permission="'user:create'"  // 按钮权限
 */
export const buttonPermission = {
  mounted(el, binding) {
    const permissionStore = usePermissionStore()
    const buttonPermissions = permissionStore.getButtonPermissions()
    
    const requiredPermission = binding.value
    if (typeof requiredPermission === 'string' && !buttonPermissions.some(btn => btn.permissionCode === requiredPermission)) {
      el.style.display = 'none'
    }
  },
  
  updated(el, binding) {
    const permissionStore = usePermissionStore()
    const buttonPermissions = permissionStore.getButtonPermissions()
    
    const requiredPermission = binding.value
    if (typeof requiredPermission === 'string') {
      if (buttonPermissions.some(btn => btn.permissionCode === requiredPermission)) {
        el.style.display = ''
      } else {
        el.style.display = 'none'
      }
    }
  }
}

/**
 * 注册所有权限指令
 * @param {Object} app - Vue应用实例
 */
export function setupPermissionDirectives(app) {
  app.directive('permission', permission)
  app.directive('role', role)
  app.directive('button-permission', buttonPermission)
}

/**
 * 权限检查函数（可在组件中使用）
 * @param {string|Array} permission - 权限要求
 * @returns {boolean} - 是否有权限
 */
export function checkPermission(permission) {
  const permissionStore = usePermissionStore()
  const userPermissions = permissionStore.getUserPermissions()
  
  if (typeof permission === 'string') {
    return hasPermission(permission, userPermissions)
  } else if (Array.isArray(permission)) {
    return hasAnyPermission(permission, userPermissions)
  }
  return false
}

/**
 * 角色检查函数（可在组件中使用）
 * @param {string|Array} role - 角色要求
 * @returns {boolean} - 是否有角色
 */
export function checkRole(role) {
  const permissionStore = usePermissionStore()
  const userRole = permissionStore.getUserRole()
  
  return hasRole(role, userRole)
} 