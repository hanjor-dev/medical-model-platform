<template>
  <header class="app-header">
    <div class="header-left">
      <div class="logo-section">
        <div class="logo-clickable" @click="goToHome" title="点击返回首页">
          <div class="logo">
            <svg t="1756299919532" class="logo-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="7543" width="32" height="32">
              <path d="M486.6048 973.824L102.4 754.2784v-468.992L512 51.2l409.6 234.0352v336.896h-102.4V344.7296l-307.2-175.5648-307.2 175.616v350.1056l332.5952 190.1056-50.7904 88.8832z" fill="#1890ff" p-id="7544"></path>
              <path d="M855.04 256l51.2 88.7296-372.48 215.04-1.9456 3.328L153.6 344.7296 204.8 256l325.12 187.648L855.04 256z" fill="#1890ff" p-id="7545"></path>
              <path d="M476.16 665.6l102.4 51.2v-204.8h-102.4v153.6z" fill="#1890ff" p-id="7546"></path>
              <path d="M849.8176 691.2l76.8 133.0176-76.8 133.0176h-153.6l-76.8-133.0176 76.8-133.0176h153.6z" fill="#52c41a" p-id="7547"></path>
            </svg>
          </div>
          <h1 class="product-name">Model-Y</h1>
        </div>
        <h2 class="system-title">医疗影像模型管理平台</h2>
      </div>
    </div>
    
    <div class="header-right">
      <!-- 消息通知小铃铛 -->
      <NotificationBell />
      
      <div class="user-info" :class="{ active: isUserDropdownVisible }" @click="toggleUserDropdown">
        <div class="user-avatar">
          <!-- 动态头像显示 -->
          <img v-if="userAvatar.type === 'image'" 
               :src="userAvatar.content" 
               :alt="userAvatar.alt"
               @error="handleAvatarError"
               class="avatar-image" />
          <div v-else-if="userAvatar.type === 'svg'" 
               v-html="userAvatar.content" 
               class="avatar-svg" />
        </div>
        <div class="username">{{ userDisplayName }}</div>
        <div class="dropdown-arrow">▼</div>
        
        <!-- 用户下拉菜单 -->
        <div class="user-dropdown" :class="{ show: isUserDropdownVisible }">
          <!-- 团队信息独立区域（置于积分区域之上，整行背景） -->
          <div v-if="currentTeamName" class="dropdown-team-section" @click.stop="goToMyTeam" title="前往我的团队">
            <span class="team-icon" aria-hidden="true">
              <svg t="1760410864831" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4635" width="16" height="16">
                <path d="M845.4 119.1H179.6L64.5 449l448 454.4 448-454.4-115.1-329.9zM559.1 657.8l-39.6 39.6-277.3-277.3 39.6-39.6 238.7 238.7 239.8-239.8 38.5 38.5-239.7 239.9z" p-id="4636"></path>
              </svg>
            </span>
            <span class="team-label">我的团队</span>
            <span class="team-name" :title="currentTeamName">{{ currentTeamName }}</span>
          </div>

          <!-- 下拉菜单头部（积分区域） -->
          <div class="dropdown-header" @click.stop="goToMyCredits" title="前往我的积分">
            <div class="dropdown-user-balance">
              <div v-if="isLoadingCredits" class="balance-item">
                <span class="balance-label">加载中...</span>
                <span class="balance-value">
                  <span class="loading-dots">...</span>
                </span>
              </div>
              <div v-else-if="creditsData && creditsData.accounts && creditsData.accounts.length > 0" 
                   v-for="account in creditsData.accounts" 
                   :key="account.creditTypeCode" 
                   class="balance-item">
                <span class="balance-label" :style="{ color: getAccountColor(account) }">{{ account.creditTypeName || account.creditTypeCode }}</span>
                <span class="balance-value" :style="{ color: getAccountColor(account) }">{{ formatBalance(account) }}</span>
              </div>
              <div v-else class="balance-item">
                <span class="balance-label">暂无积分账户</span>
                <span class="balance-value">-</span>
              </div>
            </div>
          </div>
          
          <!-- 下拉菜单列表 -->
          <ul class="dropdown-menu">
            <li class="dropdown-item">
              <a href="#" class="dropdown-link" @click.prevent="handleMenuAction('profile')">
                <span class="dropdown-icon">
                  <svg t="1756374947026" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="6888" width="16" height="16">
                    <path d="M128 196.2496a17.0496 17.0496 0 0 0-17.0496 17.1008v597.2992c0 9.4208 7.6288 17.1008 17.0496 17.1008h768c9.4208 0 17.0496-7.68 17.0496-17.1008V213.3504a17.0496 17.0496 0 0 0-17.0496-17.1008h-768z m-68.2496 17.1008c0-37.7344 30.5664-68.3008 68.2496-68.3008h768c37.6832 0 68.2496 30.5664 68.2496 68.3008v597.2992c0 37.7344-30.5664 68.3008-68.2496 68.3008h-768a68.2496 68.2496 0 0 1-68.2496-68.3008V213.3504z m192 234.6496a110.9504 110.9504 0 1 1 187.5456 80.1792 153.5488 153.5488 0 0 1 76.9536 133.12 25.6 25.6 0 0 1-51.2 0 102.4 102.4 0 1 0-204.8 0 25.6 25.6 0 0 1-51.2 0c0-56.8832 30.976-106.5472 76.9536-133.12a110.592 110.592 0 0 1-34.304-80.1792z m110.8992-59.7504a59.7504 59.7504 0 1 0 0 119.5008 59.7504 59.7504 0 0 0 0-119.5008z m209.1008 38.4a25.6 25.6 0 0 1 25.6-25.6H768a25.6 25.6 0 0 1 0 51.2h-170.6496a25.6 25.6 0 0 1-25.6-25.6z m68.2496 145.1008a25.6 25.6 0 1 0 0 51.2H768a25.6 25.6 0 0 0 0-51.2h-128z" fill="currentColor" p-id="6888"></path>
                  </svg>
                </span>
                我的账号
              </a>
            </li>

            <li class="dropdown-item">
              <a href="#" class="dropdown-link" @click="handleMenuAction('transactions')">
                <span class="dropdown-icon">
                  <svg t="1756375077497" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="10593" width="16" height="16">
                    <path d="M504.576 351.648H272.192c-9.024 0-16.352 7.328-16.352 16.352s7.328 16.352 16.352 16.352h232.384c9.024 0 16.352-7.328 16.352-16.352s-7.328-16.352-16.352-16.352zM693.632 272c0-8.704-7.04-15.744-15.744-15.744H271.616c-8.704 0-15.744 7.04-15.744 15.744s7.04 15.744 15.744 15.744h406.24c8.736 0 15.776-7.04 15.776-15.744zM271.616 672.256c-8.704 0-15.744 7.04-15.744 15.744s7.04 15.744 15.744 15.744h99.648c8.704 0 15.744-7.04 15.744-15.744s-7.04-15.744-15.744-15.744h-99.648z" fill="currentColor" p-id="10594"></path>
                    <path d="M192.32 874.144V179.936c0-11.328 8.608-21.216 18.4-21.216h572.352c9.824 0 18.4 9.92 18.4 21.216v269.12H833.6v-269.12c0-29.408-22.656-53.344-50.528-53.344H210.72c-27.872 0-50.528 23.936-50.528 53.344v694.208c0 29.408 22.656 53.344 50.528 53.344h247.808V895.36H210.72c-9.824 0-18.4-9.888-18.4-21.216z" fill="currentColor" p-id="10595"></path>
                    <path d="M699.104 486.016c-129.696 0-234.56 107.328-234.56 236.576s104.864 233.76 234.56 233.76 238.08-104.544 238.08-233.76-108.416-236.576-238.08-236.576z m102.688 315.2c8.832 0 16 7.168 16 16s-7.168 16-16 16h-84.896v31.648c0 8.832-7.168 16-16 16s-16-7.168-16-16v-31.648h-84.928c-8.832 0-16-7.168-16-16s7.168-16 16-16h84.928v-33.344h-84.928c-8.832 0-16-7.168-16-16s7.168-16 16-16h83.552L603.2 618.528c-4.96-7.296-3.104-17.248 4.192-22.24 7.264-4.896 17.216-3.136 22.24 4.192l71.264 104.16 71.232-104.16c5.056-7.328 15.072-9.088 22.24-4.192 7.296 4.992 9.152 14.976 4.192 22.24l-80.288 117.344h83.52c8.832 0 16 7.168 16 16s-7.168 16-16 16h-84.896v33.344h84.896zM520.896 464c0-9.024-7.328-16.352-16.352-16.352H272.192c-9.024 0-16.352 7.328-16.352 16.352s7.328 16.352 16.352 16.352h232.384c9.024 0 16.32-7.328 16.32-16.352z" fill="currentColor" p-id="10596"></path>
                  </svg>
                </span>
                交易记录
              </a>
            </li>
            

          </ul>
          
          <!-- 下拉菜单底部 -->
          <div class="dropdown-footer">
            <button class="logout-btn" @click="handleLogout">
              退出登录
            </button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getUserAvatar, getUserDisplayName } from '@/utils/avatarUtils'
import { ElMessage } from 'element-plus'
import { showLogoutConfirm } from '@/utils/confirmDialog'
import { creditTypeApi } from '@/api/credit-system'
import NotificationBell from '@/components/common/NotificationBell.vue'

export default {
  name: 'AppHeader',
  components: { NotificationBell },
  setup() {
    const router = useRouter()
    
    // 直接初始化auth store
    const authStore = useAuthStore()
    
    // 添加调试信息，检查store是否正确初始化
    console.log('AppHeader setup - authStore:', authStore)
    console.log('AppHeader setup - authStore methods:', {
      getUserCredits: typeof authStore?.getUserCredits,
      getCreditsLoadingState: typeof authStore?.getCreditsLoadingState,
      fetchUserCredits: typeof authStore?.fetchUserCredits
    })
    
    // 创建响应式的积分数据状态，用于模板绑定
    const creditsData = computed(() => authStore.creditsData)
    const isLoadingCredits = computed(() => authStore.isLoadingCredits)
    const typeColorMap = ref({})
    
    const isUserDropdownVisible = ref(false)
    
    // 直接使用auth store的积分数据状态
    // 不需要计算属性，直接绑定到store状态
    
    const userInfo = computed(() => {
      // 添加安全检查，防止authStore未初始化
      if (!authStore) {
        // 只在开发环境显示警告
        if (process.env.NODE_ENV === 'development') {
          console.warn('userInfo computed: authStore未初始化')
        }
        return {
          username: '用户',
          nickname: '',
          realName: '',
          avatar: ''
        }
      }
      
      const user = authStore.getUser()
      if (!user) {
        // 只有当已经认证但用户数据为空时才显示警告
        if (authStore.isAuthenticated && authStore.getToken()) {
          console.warn('userInfo computed: 已认证但用户数据为空，可能需要重新获取用户信息')
        }
        return {
          username: '用户',
          nickname: '',
          realName: '',
          avatar: ''
        }
      }
      
      return {
        username: user.username || '用户',
        nickname: user.nickname || '',
        realName: user.realName || '',
        avatar: user.avatar || ''
      }
    })
    
    const userAvatar = computed(() => {
      return userInfo.value ? getUserAvatar(userInfo.value) : getUserAvatar({
        username: '用户',
        nickname: '',
        realName: '',
        avatar: ''
      })
    })
    
    const userDisplayName = computed(() => {
      return userInfo.value ? getUserDisplayName(userInfo.value) : '用户'
    })
    
    // 团队信息：从认证用户信息中读取当前团队名称
    const currentTeamName = computed(() => {
      try {
        const u = authStore?.getUser?.()
        // 会话中的 currentTeamName 为空时，不展示旧值
        return (u && typeof u.currentTeamName === 'string') ? u.currentTeamName : ''
      } catch (_) {
        return ''
      }
    })
    
    // 积分数据获取逻辑已简化，直接使用authStore.fetchUserCredits()
    
    const toggleUserDropdown = () => {
      isUserDropdownVisible.value = !isUserDropdownVisible.value
      
      // 当下拉菜单打开时，如果没有积分数据则获取
      if (isUserDropdownVisible.value && !creditsData.value) {
        authStore.fetchUserCredits()
      }
      if (isUserDropdownVisible.value && Object.keys(typeColorMap.value).length === 0) {
        fetchCreditTypeColors()
      }
    }
    
    const hideUserDropdown = () => {
      isUserDropdownVisible.value = false
    }
    
    const showNotifications = () => {
      ElMessage.info('通知中心功能开发中...')
    }
    
    const handleMenuAction = async (action) => {
      hideUserDropdown()
      
      switch(action) {
        case 'profile':
          router.push('/profile')
          break
        case 'transactions':
          router.push('/credit-system/my-credits?tab=transactions')
          break

        default:
          console.warn('未知的菜单动作:', action)
          break
      }
    }
    
    const handleLogout = async () => {
      try {
        const confirmed = await showLogoutConfirm()
        if (confirmed) {
          if (!authStore) {
            console.error('退出登录: authStore未初始化')
            ElMessage.error('系统错误，无法退出登录')
            return
          }
          await authStore.logout()
          ElMessage.success('已退出登录')
          router.push('/login')
        }
      } catch (error) {
        console.error('退出登录错误:', error)
        ElMessage.error('退出登录失败，请重试')
      }
    }
    
    // 点击品牌标识跳转到首页
    const goToHome = () => {
      router.push('/dashboard')
    }

    // 跳转到我的团队
    const goToMyTeam = () => {
      router.push('/team/my')
    }

    // 跳转到我的积分（概览）
    const goToMyCredits = () => {
      hideUserDropdown()
      router.push('/credit-system/my-credits?tab=overview')
    }

    // 处理头像加载失败
    const handleAvatarError = (event) => {
      console.warn('头像加载失败，切换到默认头像')
      
      // 隐藏失败的图片
      event.target.style.display = 'none'
      
      // 获取父容器
      const avatarContainer = event.target.parentElement
      
      // 创建默认头像元素
      const defaultAvatar = document.createElement('div')
      defaultAvatar.className = 'avatar-svg'
      defaultAvatar.innerHTML = getUserAvatar(userInfo.value).content
      
      // 替换失败的头像
      avatarContainer.appendChild(defaultAvatar)
    }
    
    // 点击外部隐藏下拉菜单
    const handleClickOutside = (event) => {
      if (!event.target.closest('.user-info')) {
        hideUserDropdown()
      }
    }
    
    onMounted(() => {
      document.addEventListener('click', handleClickOutside)
      
      // 组件挂载完成后，如果没有积分数据则获取
      if (!creditsData.value) {
        authStore.fetchUserCredits()
      }
      // 预加载积分类型颜色
      fetchCreditTypeColors()
    })
    
    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside)
    })
    
    // 根据积分账户或类型映射获取颜色
    const getAccountColor = (account) => {
      if (!account) return '#1890ff'
      return account.colorCode || typeColorMap.value[account.creditTypeCode] || '#1890ff'
    }

    // 余额格式化
    const formatBalance = (account) => {
      const value = Number(account?.balance ?? 0)
      const dp = Number(account?.decimalPlaces)
      if (!isNaN(dp)) {
        return value.toLocaleString('zh-CN', {
          minimumFractionDigits: dp,
          maximumFractionDigits: dp
        })
      }
      return value.toLocaleString('zh-CN')
    }

    // 拉取类型颜色
    const fetchCreditTypeColors = async () => {
      try {
        const res = await creditTypeApi.getCreditTypeList({ status: 1 })
        if (res && res.code === 200 && Array.isArray(res.data)) {
          const map = {}
          res.data.forEach(t => {
            if (t?.typeCode) {
              map[t.typeCode] = t.colorCode || '#1890ff'
            }
          })
          typeColorMap.value = map
        }
      } catch (e) {
        // 静默失败，不影响下拉展示
        console.warn('加载积分类型颜色失败:', e?.message || e)
      }
    }

    return {
      isUserDropdownVisible,
      userInfo,
      userAvatar,
      userDisplayName,
      currentTeamName,
      creditsData,
      isLoadingCredits,
      getAccountColor,
      formatBalance,
      toggleUserDropdown,
      showNotifications,
      handleMenuAction,
      handleLogout,
      goToHome,
      goToMyTeam,
      goToMyCredits,
      handleAvatarError
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/components/header.scss';

/* 用户下拉 - 团队信息与分隔样式 */
.dropdown-team-section {
  padding: 8px 16px;
  border-bottom: 1px solid #f0f0f0;
  text-align: center;
  cursor: pointer;
  &:hover {
    background-color: #fafafa;
  }
  .team-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: #1677ff;
    margin-right: 6px;
    vertical-align: middle;
  }
  .team-label {
    display: inline-block;
    margin-right: 6px;
    color: #595959;
    font-size: 13px;
    vertical-align: middle;
  }
  .team-name {
    color: #1f1f1f;
    font-weight: 400;
    font-size: 14px;
    display: block;
    margin-top: 8px;
    max-width: 100%;
    background-color: #e6f4ff;
    border: 1px solid #91caff;
    border-radius: 0;
    padding: 2px 8px;
    box-sizing: border-box;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

/* 积分区域 hover 与手型指针 */
.dropdown-header {
  cursor: pointer;
  &:hover {
    background-color: #fafafa;
  }
}
</style> 