/**
 * 权限验证工具：提供权限检查的各种方法
 * 
 * 功能描述：
 * 1. 检查单个权限
 * 2. 检查任意权限（OR逻辑）
 * 3. 检查所有权限（AND逻辑）
 * 4. 支持通配符权限匹配
 * 5. 提供权限验证的便利方法
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15
 */

/**
 * 检查用户是否拥有指定权限
 * @param {string} permissionCode - 需要检查的权限code
 * @param {Array} userPermissions - 用户拥有的权限对象列表
 * @returns {boolean} - 是否拥有权限
 */
export const hasPermission = (permissionCode, userPermissions) => {
  if (!permissionCode || !Array.isArray(userPermissions)) {
    return false
  }
  
  // 检查权限对象列表中是否包含指定权限code
  return userPermissions.some(perm => perm.code === permissionCode)
}

/**
 * 检查用户是否拥有任意一个权限（OR逻辑）
 * @param {Array|string} permissions - 需要检查的权限列表或单个权限
 * @param {Array} userPermissions - 用户拥有的权限列表
 * @returns {boolean} - 是否拥有任意一个权限
 */
export const hasAnyPermission = (permissions, userPermissions) => {
  if (!permissions || !Array.isArray(userPermissions)) {
    return false
  }
  
  // 如果传入的是字符串，转换为数组
  const permArray = Array.isArray(permissions) ? permissions : [permissions]
  
  return permArray.some(permission => hasPermission(permission, userPermissions))
}

/**
 * 检查用户是否拥有所有权限（AND逻辑）
 * @param {Array|string} permissions - 需要检查的权限列表或单个权限
 * @param {Array} userPermissions - 用户拥有的权限列表
 * @returns {boolean} - 是否拥有所有权限
 */
export const hasAllPermissions = (permissions, userPermissions) => {
  if (!permissions || !Array.isArray(userPermissions)) {
    return false
  }
  
  // 如果传入的是字符串，转换为数组
  const permArray = Array.isArray(permissions) ? permissions : [permissions]
  
  return permArray.every(permission => hasPermission(permission, userPermissions))
}

/**
 * 检查用户是否拥有指定角色
 * @param {string|Array} roles - 需要检查的角色
 * @param {string} userRole - 用户角色
 * @returns {boolean} - 是否拥有指定角色
 */
export const hasRole = (roles, userRole) => {
  if (!roles || !userRole) {
    return false
  }
  
  const roleArray = Array.isArray(roles) ? roles : [roles]
  return roleArray.includes(userRole)
}

/**
 * 检查用户是否为超级管理员
 * @param {string} userRole - 用户角色
 * @returns {boolean} - 是否为超级管理员
 */
export const isSuperAdmin = (userRole) => {
  return userRole === 'SUPER_ADMIN'
}

/**
 * 检查用户是否为管理员
 * @param {string} userRole - 用户角色
 * @returns {boolean} - 是否为管理员
 */
export const isAdmin = (userRole) => {
  return ['ADMIN', 'SUPER_ADMIN'].includes(userRole)
}

/**
 * 根据权限过滤数组
 * @param {Array} items - 需要过滤的数组
 * @param {string} permissionKey - 权限字段名
 * @param {Array} userPermissions - 用户权限对象列表
 * @returns {Array} - 过滤后的数组
 */
export const filterByPermission = (items, permissionKey, userPermissions) => {
  if (!Array.isArray(items) || !userPermissions) {
    return []
  }
  
  return items.filter(item => {
    const permission = item[permissionKey]
    if (!permission) return true // 没有权限要求则显示
    
    if (Array.isArray(permission)) {
      return hasAnyPermission(permission, userPermissions)
    } else {
      return hasPermission(permission, userPermissions)
    }
  })
}

/**
 * 获取用户权限的树形结构
 * @param {Array} permissions - 权限列表
 * @returns {Object} - 权限树形结构
 */
export const buildPermissionTree = (permissions) => {
  if (!Array.isArray(permissions)) {
    return {}
  }
  
  const tree = {}
  
  permissions.forEach(permission => {
    const parts = permission.split(':')
    let current = tree
    
    parts.forEach((part, index) => {
      if (!current[part]) {
        current[part] = index === parts.length - 1 ? true : {}
      }
      current = current[part]
    })
  })
  
  return tree
}

/**
 * 检查权限是否有效（格式验证）
 * @param {string} permission - 权限字符串
 * @returns {boolean} - 权限格式是否有效
 */
export const isValidPermission = (permission) => {
  if (typeof permission !== 'string') {
    return false
  }
  
  // 权限格式：模块:功能:操作 或 模块:功能 或 模块
  const pattern = /^[a-zA-Z][a-zA-Z0-9_]*(\.[a-zA-Z][a-zA-Z0-9_]*)*$/
  return pattern.test(permission)
} 