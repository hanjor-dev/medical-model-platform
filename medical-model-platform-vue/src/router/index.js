/**
 * 路由配置文件
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 20:10:00
 */

import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usePermissionStore } from '@/stores/permission'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  // 捕获所有未匹配的路径，统一重定向到控制台，避免控制台警告
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: {
      title: '登录 - 医疗影像模型管理平台',
      requiresAuth: false
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: {
      title: '注册 - 医疗影像模型管理平台',
      requiresAuth: false
    }
  },
  {
    path: '/',
    component: () => import('@/layouts/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { 
          title: '控制台 - 医疗影像模型管理平台',
          icon: 'dashboard',
          permission: 'dashboard'
        }
      },
      {
        path: 'messages',
        name: 'UserMessages',
        component: () => import('@/views/messages/UserMessages.vue'),
        meta: {
          title: '我的消息',
          permission: null
        }
      },
      {
        path: 'messages/:id',
        name: 'UserMessageDetail',
        component: () => import('@/views/messages/UserMessageDetail.vue'),
        meta: {
          title: '消息详情',
          permission: null
        }
      },
      {
        path: 'announcements',
        name: 'AnnouncementCenter',
        component: () => import('@/views/announcement/AnnouncementCenter.vue'),
        meta: {
          title: '平台热点 - 公告',
          permission: null
        }
      },
      {
        path: 'announcements/:id',
        name: 'AnnouncementDetail',
        component: () => import('@/views/announcement/AnnouncementDetail.vue'),
        meta: {
          title: '公告详情',
          permission: null
        }
      },
      {
        path: 'model-management',
        name: 'ModelManagement',
        meta: { 
          title: '模型管理',
          icon: 'model',
          permission: 'model-management'
        },
        children: [
          {
            path: 'task',
            name: 'ModelTask',
            component: () => import('@/views/model-management/task/index.vue'),
            meta: { 
              title: '计算任务',
              permission: 'model-management:task'
            }
          },
          {
            path: 'list',
            name: 'ModelList',
            component: () => import('@/views/model-management/list/index.vue'),
            meta: { 
              title: '模型列表',
              permission: 'model-management:list'
            }
          },
          {
            path: 'category',
            name: 'ModelCategory',
            component: () => import('@/views/model-management/category/index.vue'),
            meta: { 
              title: '模型分类',
              permission: 'model-management:category'
            }
          },
          {
            path: 'group',
            name: 'ModelGroup',
            component: () => import('@/views/model-management/group/index.vue'),
            meta: { 
              title: '模型分组',
              permission: 'model-management:group'
            }
          }
        ]
      },
      {
        path: 'credit-system',
        name: 'CreditSystem',
        meta: { 
          title: '积分系统',
          icon: 'credit',
          permission: 'credit-system'
        },
        children: [
          {
            path: 'my-credits',
            name: 'MyCredits',
            component: () => import('@/views/credit-system/my-credits/MyCredits.vue'),
            meta: { 
              title: '我的积分',
              permission: 'credit-system:my-credits'
            }
          },
          {
            path: 'scenario-pricing',
            name: 'ScenarioPricing',
            component: () => import('@/views/credit-system/scenario-pricing/ScenarioPricingPoster.vue'),
            meta: {
              title: '消费计价规则',
              permission: 'credit-system:my-credits'
            }
          },
          {
            path: 'user-credits',
            name: 'UserCredits',
            component: () => import('@/views/credit-system/user-credits/UserCredits.vue'),
            meta: { 
              title: '用户积分',
              permission: 'credit-system:user-credits'
            }
          },
          {
            path: 'type-management',
            name: 'CreditTypeManagement',
            component: () => import('@/views/credit-system/type-management/CreditsType.vue'),
            meta: { 
              title: '类型管理',
              permission: 'credit-system:type-management'
            }
          },
          {
            path: 'scenario-management',
            name: 'CreditScenarioManagement',
            component: () => import('@/views/credit-system/scenario-management/CreditsScenario.vue'),
            meta: { 
              title: '场景管理',
              permission: 'credit-system:scenario-management'
            }
          }
        ]
      },
      {
        path: 'user-permission',
        name: 'UserPermission',
        meta: { 
          title: '用户权限',
          icon: 'user',
          permission: 'user-permission'
        },
        children: [
          {
            path: 'user',
            name: 'UserList',
            component: () => import('@/views/user/List.vue'),
            meta: { 
              title: '用户列表',
              permission: 'user-permission:user'
            }
          },
          {
            path: 'permission',
            name: 'PermissionManagement',
            component: () => import('@/views/user/permission/PermissionCenter.vue'),
            meta: { 
              title: '权限管理',
              permission: 'user-permission:permission'
            }
          },
          {
            path: 'log',
            name: 'UserLog',
            component: () => import('@/views/user/log/Log.vue'),
            meta: { 
              title: '用户日志',
              permission: 'user-permission:log'
            }
          }
        ]
      },
      {
        path: 'ai-engine',
        name: 'AiEngine',
        meta: { 
          title: 'AI引擎',
          icon: 'ai',
          permission: 'ai-engine'
        },
        children: [
          {
            path: 'algorithm',
            name: 'AlgorithmManagement',
            component: () => import('@/views/ai-engine/algorithm/index.vue'),
            meta: { 
              title: '算法管理',
              permission: 'ai-engine:algorithm'
            }
          },
          {
            path: 'multi-config',
            name: 'MultiConfig',
            component: () => import('@/views/ai-engine/multi-config/index.vue'),
            meta: { 
              title: '多端配置',
              permission: 'ai-engine:multi-config'
            }
          }
        ]
      },
      {
        path: 'system',
        name: 'System',
        meta: { 
          title: '系统管理',
          icon: 'system',
          permission: 'system'
        },
        children: [
          {
            path: 'config',
            name: 'SystemConfig',
            component: () => import('@/views/system/Config.vue'),
            meta: { 
              title: '系统配置',
              permission: 'system:config'
            }
          },
          {
            path: 'dict',
            name: 'SystemDict',
            component: () => import('@/views/system/Dict.vue'),
            meta: { 
              title: '字典数据',
              permission: 'system:dict'
            }
          },
          {
            path: 'message',
            name: 'SystemMessageCenter',
            component: () => import('@/views/system/message/MessageCenter.vue'),
            meta: {
              title: '消息与公告',
              permission: 'system:message',
              roles: ['SUPER_ADMIN']
            }
          }
        ]
      },
      {
        path: 'team',
        name: 'Team',
        meta: {
          title: '团队管理',
          icon: 'team',
          permission: 'team'
        },
        children: [
          {
            path: 'my',
            name: 'MyTeam',
            component: () => import('@/views/team/my/MyTeam.vue'),
            meta: {
              title: '我的团队',
              permission: 'team:my'
            }
          },
          {
            path: 'members',
            name: 'TeamMembers',
            component: () => import('@/views/team/members/TeamMembers.vue'),
            meta: {
              title: '成员管理',
              permission: 'team:members'
            }
          },
          // 邀请管理页面取消，保留路由占位并重定向到“我的团队”
          {
            path: 'invitations',
            name: 'TeamInvitationsRedirect',
            redirect: '/team/my',
            meta: {
              title: '邀请管理（已合并至我的团队）',
              permission: 'team:my'
            }
          },
          {
            path: 'join-requests',
            name: 'TeamJoinRequests',
            component: () => import('@/views/team/join-requests/TeamJoinRequests.vue'),
            meta: {
              title: '加入申请',
              permission: 'team:join-requests'
            }
          }
        ]
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/profile/index.vue'),
        meta: { 
          title: '个人信息',
          icon: 'user',
          permission: null
        }
      }
    ]
  }
]

// 将兜底路由放在最后，避免拦截正常路由
routes.push({
  path: '/:pathMatch(.*)*',
  redirect: '/dashboard'
})

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title
  }

  console.log('路由守卫触发:', { 
    to: to.path, 
    from: from.path, 
    requiresAuth: to.meta.requiresAuth,
    permission: to.meta.permission,
    meta: to.meta
  })

  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    const authStore = useAuthStore()
    const permissionStore = usePermissionStore()
    
    console.log('当前认证状态:', {
      isAuthenticated: authStore.isAuthenticated,
      hasToken: !!authStore.getToken(),
      userPermissions: permissionStore.getUserPermissions().length,
      permissionsFromStorage: permissionStore.isPermissionsReady()
    })
    
    // 检查认证状态
    if (!authStore.isAuthenticated || !authStore.getUser()) {
      // 尝试检查认证状态
      console.log('检查认证状态...')
      try {
        // 调用checkAuth方法进行完整的认证检查
        const isAuth = await authStore.checkAuth()
        console.log('认证检查结果:', isAuth)
        if (!isAuth) {
          console.log('认证失败，跳转到登录页')
          next('/login')
          return
        }
      } catch (error) {
        console.error('认证检查出错:', error)
        next('/login')
        return
      }
    } else {
      // 即使isAuthenticated为true，也要检查token是否真的有效
      if (authStore.isTokenExpired()) {
        console.log('token已过期，清除认证状态')
        authStore.logout(false) // 不调用后端接口
        next('/login')
        return
      }
    }
    
    // 简化权限检查：如果权限数据未就绪，尝试从localStorage恢复
    if (!permissionStore.isPermissionsReady()) {
      permissionStore.loadPermissionsFromStorage()
    }
    
    // 检查页面权限
    if (to.meta.permission) {
      console.log('检查页面权限:', to.meta.permission)
      const hasAccess = permissionStore.checkPermission(to.meta.permission)
      console.log('权限检查结果:', hasAccess)
      
      if (!hasAccess) {
        // 权限不足，重定向到有权限的页面
        console.warn(`权限不足，无法访问: ${to.path}`)
        
        // 避免无限重定向：检查是否已经在重定向循环中
        if (from.path === to.path) {
          // 如果来源和目标相同，说明在重定向循环中，直接跳转到登录页
          console.error('检测到重定向循环，跳转到登录页')
          next('/login')
          return
        }
        
        // 尝试跳转到用户有权限的页面
        const userPermissions = permissionStore.getUserPermissions()
        const menuPermissions = permissionStore.getMenuPermissions()
        console.log('用户权限信息:', {
          totalPermissions: userPermissions.length,
          menuPermissions: menuPermissions.length,
          firstMenuPath: menuPermissions[0]?.path
        })
        
        if (menuPermissions && menuPermissions.length > 0) {
          // 找到第一个路由系统中真实存在的菜单路径
          const validMenu = menuPermissions.find(m => m.path && router.resolve(m.path).matched.length > 0)
          if (validMenu && validMenu.path !== to.path) {
            console.log('重定向到有权限的有效页面:', validMenu.path)
            next(validMenu.path)
            return
          }
        }
        
        // 兜底跳转到控制台
        next('/dashboard')
        return
      }
    } else {
      // 对于没有设置permission的路由（如profile），直接允许访问
      console.log(`路由 ${to.path} 未设置权限要求，直接允许访问`)
    }
    
    // 检查角色权限
    if (to.meta.roles) {
      const userRole = permissionStore.getUserRole()
      const hasRole = to.meta.roles.includes(userRole)
      if (!hasRole) {
        console.warn(`角色权限不足，无法访问: ${to.path}`)
        next('/dashboard')
        return
      }
    }
  }

  console.log('路由守卫通过，继续导航到:', to.path)
  next()
})

export default router 