/**
 * 菜单权限配置：定义菜单结构和权限要求
 * 
 * 功能描述：
 * 1. 定义系统菜单结构
 * 2. 配置每个菜单的权限要求
 * 3. 支持多级菜单嵌套
 * 4. 支持动态权限验证
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15
 */

/**
 * 菜单配置结构说明：
 * - path: 路由路径
 * - title: 菜单标题
 * - icon: 菜单图标
 * - permission: 所需权限（字符串或数组）
 * - children: 子菜单列表
 * - hidden: 是否隐藏（true表示隐藏）
 * - sort: 排序号（数字越小越靠前）
 */

export const menuConfig = [
  {
    path: '/dashboard',
    title: '控制台',
    icon: 'dashboard',
    permission: 'dashboard',
    sort: 1
  },
  {
    path: '/model-management',
    title: '模型管理',
    icon: 'model',
    permission: 'model-management',
    sort: 2,
    children: [
      {
        path: '/model-management/task',
        title: '计算任务',
        permission: 'model-management:task',
        sort: 1
      },
      {
        path: '/model-management/list',
        title: '模型列表',
        permission: 'model-management:list',
        sort: 2
      },
      {
        path: '/model-management/category',
        title: '模型分类',
        permission: 'model-management:category',
        sort: 3
      },
      {
        path: '/model-management/group',
        title: '模型分组',
        permission: 'model-management:group',
        sort: 4
      }
    ]
  },
  {
    path: '/credit-system',
    title: '积分系统',
    icon: 'credit',
    permission: 'credit-system',
    sort: 3,
    children: [
      {
        path: '/credit-system/my-credits',
        title: '我的积分',
        permission: 'credit-system:my-credits',
        sort: 1
      },
      {
        path: '/credit-system/user-credits',
        title: '用户积分',
        permission: 'credit-system:user-credits',
        sort: 2
      },
      {
        path: '/credit-system/type-management',
        title: '类型管理',
        permission: 'credit-system:type-management',
        sort: 3
      },
      {
        path: '/credit-system/scenario-management',
        title: '场景管理',
        permission: 'credit-system:scenario-management',
        sort: 4
      }
    ]
  },
  {
    path: '/user-permission',
    title: '用户权限',
    icon: 'user',
    permission: 'user-permission',
    sort: 4,
    children: [
      {
        path: '/user-permission/user',
        title: '用户列表',
        permission: 'user-permission:user',
        sort: 1
      },
      {
      },
      {
        path: '/user-permission/permission',
        title: '权限管理',
        permission: 'user-permission:permission',
        sort: 3
      },
      {
        path: '/user-permission/log',
        title: '用户日志',
        permission: 'user-permission:log',
        sort: 4
      }
    ]
  },
  {
    path: '/ai-engine',
    title: 'AI引擎',
    icon: 'ai',
    permission: 'ai-engine',
    sort: 5,
    children: [
      {
        path: '/ai-engine/algorithm',
        title: '算法管理',
        permission: 'ai-engine:algorithm',
        sort: 1
      },
      {
        path: '/ai-engine/multi-config',
        title: '多端配置',
        permission: 'ai-engine:multi-config',
        sort: 2
      }
    ]
  },
  {
    path: '/team',
    title: '团队管理',
    icon: 'team',
    permission: 'team',
    sort: 6,
    children: [
      {
        path: '/team/my',
        title: '我的团队',
        permission: 'team:my',
        sort: 1
      },
      {
        path: '/team/members',
        title: '成员管理',
        permission: 'team:members',
        sort: 2
      },
      // 邀请管理入口取消，改在“我的团队”内提供邀请功能
      // 保留占位以便将来恢复时快速启用
      // { path: '/team/invitations', title: '邀请管理', permission: 'team:invitations', sort: 3, hidden: true },
      {
        path: '/team/join-requests',
        title: '加入申请',
        permission: 'team:join-requests',
        sort: 4
      }
    ]
  },
  {
    path: '/system',
    title: '系统管理',
    icon: 'system',
    permission: 'system',
    sort: 7,
    children: [
      {
        path: '/system/config',
        title: '系统配置',
        permission: 'system:config',
        sort: 1
      },
      {
        path: '/system/dict',
        title: '字典数据',
        permission: 'system:dict',
        sort: 2
      },
      {
        path: '/system/message',
        title: '消息与公告',
        permission: 'system:message',
        sort: 3,
        // 为面包屑提供映射，但不在侧边栏展示
        children: [
          { path: '/announcements', title: '平台热点 - 公告', hidden: true },
          { path: '/announcements/:id', title: '公告详情', hidden: true }
        ]
      }
    ]
  },
  
]

/**
 * 获取菜单配置
 * @returns {Array} 菜单配置数组
 */
export const getMenuConfig = () => menuConfig

/**
 * 根据路径获取菜单项
 * @param {string} path 菜单路径
 * @returns {Object|null} 菜单项或null
 */
export const getMenuByPath = (path) => {
  const findMenu = (menus, targetPath) => {
    for (const menu of menus) {
      if (menu.path === targetPath) {
        return menu
      }
      if (menu.children) {
        const found = findMenu(menu.children, targetPath)
        if (found) return found
      }
    }
    return null
  }
  
  return findMenu(menuConfig, path)
}

/**
 * 获取面包屑导航
 * @param {string} path 当前路径
 * @returns {Array} 面包屑数组
 */
export const getBreadcrumb = (path) => {
  const findBreadcrumb = (menus, targetPath, parent = []) => {
    for (const menu of menus) {
      const currentPath = [...parent, menu]
      
      if (menu.path === targetPath) {
        return currentPath
      }
      
      if (menu.children) {
        const found = findBreadcrumb(menu.children, targetPath, currentPath)
        if (found) return found
      }
    }
    return null
  }
  
  const result = findBreadcrumb(menuConfig, path)
  return result || []
}

/**
 * 获取所有菜单路径
 * @returns {Array} 所有菜单路径数组
 */
export const getAllMenuPaths = () => {
  const paths = []
  
  const collectPaths = (menus) => {
    for (const menu of menus) {
      paths.push(menu.path)
      if (menu.children) {
        collectPaths(menu.children)
      }
    }
  }
  
  collectPaths(menuConfig)
  return paths
}

/**
 * 检查菜单是否有权限
 * @param {Object} menu 菜单项
 * @param {Array} userPermissions 用户权限列表
 * @returns {boolean} 是否有权限
 */
export const checkMenuPermission = (menu, userPermissions) => {
  if (!menu.permission) return true
  
  if (Array.isArray(menu.permission)) {
    return menu.permission.some(perm => userPermissions.includes(perm))
  }
  
  return userPermissions.includes(menu.permission)
} 