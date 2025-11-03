<template>
  <div class="profile-page-github">
    <!-- 左侧导航栏 -->
    <div class="sidebar-nav">
      <div class="nav-header">
        <h2 class="nav-title">设置</h2>
      </div>
      
      <nav class="nav-menu">
        <ul class="nav-list">
          <li 
            v-for="item in navItems" 
            :key="item.key"
            class="nav-item"
            :class="{ 'active': activeNav === item.key }"
            @click="switchNav(item.key)"
          >
            <el-icon class="nav-icon">
              <component :is="item.icon" />
            </el-icon>
            <span class="nav-text">{{ item.label }}</span>
            <el-badge 
              v-if="item.badge" 
              :value="item.badge" 
              :hidden="item.badge === 0"
              class="nav-badge"
            />
          </li>
        </ul>
      </nav>
    </div>

    <!-- 右侧内容区域 -->
    <div class="content-area">
      <!-- 个人资料页面 -->
      <div v-if="activeNav === 'profile'" class="content-page profile-page">
        <div class="page-header">
          <AppBreadcrumb />
        </div>
        
        <div class="page-content">
          <!-- 加载状态 -->
          <div v-if="isLoadingUserInfo" class="loading-container" v-loading="isLoadingUserInfo">
            <p>正在加载用户信息...</p>
          </div>
          
          <!-- 用户信息内容 -->
          <div v-else>
            <!-- 头像设置 -->
            <div class="form-section">
              <h3 class="section-title">头像</h3>
              <div class="avatar-section">
                <div class="avatar-container">
                  <el-avatar 
                    :size="80" 
                    :src="userInfo.avatar || ''" 
                    class="user-avatar"
                  >
                    <span class="avatar-text">
                      {{ (userInfo.nickname || userInfo.username || '用户')?.charAt(0)?.toUpperCase() || 'U' }}
                    </span>
                  </el-avatar>
                  <div class="avatar-actions">
                    <el-upload
                      class="avatar-uploader"
                      action="#"
                      :show-file-list="false"
                      :before-upload="beforeAvatarUpload"
                      :http-request="handleAvatarUpload"
                    >
                      <el-button size="small" type="primary">更换头像</el-button>
                    </el-upload>
                  </div>
                </div>
                <div class="avatar-info">
                  <p class="avatar-tip">推荐使用正方形图片，至少 200x200 像素</p>
                  <p class="avatar-format">支持 JPG、PNG 格式，文件大小不超过 2MB</p>
                </div>
              </div>
            </div>

            <!-- 基本信息 -->
            <div class="form-section">
              <h3 class="section-title">基本信息</h3>
              <el-form 
                ref="basicFormRef"
                :model="basicForm" 
                :rules="basicFormRules"
                label-width="0"
                class="form-grid"
              >
                <div class="form-item">
                  <label class="form-label">用户名</label>
                  <el-form-item prop="username">
                    <el-input 
                      v-model="basicForm.username" 
                      :disabled="true"
                      class="form-input"
                    />
                  </el-form-item>
                  <p class="form-help">用户名不可修改</p>
                </div>
                
                <div class="form-item">
                  <label class="form-label">昵称</label>
                  <el-form-item prop="nickname">
                    <el-input 
                      v-model="basicForm.nickname" 
                      placeholder="请输入昵称"
                      maxlength="20"
                      show-word-limit
                      class="form-input"
                    />
                  </el-form-item>
                </div>
                
                <div class="form-item">
                  <label class="form-label">邮箱</label>
                  <el-form-item prop="email">
                    <el-input 
                      v-model="basicForm.email" 
                      placeholder="请输入邮箱地址"
                      type="email"
                      class="form-input"
                    />
                  </el-form-item>
                </div>
                
                <div class="form-item">
                  <label class="form-label">手机号</label>
                  <el-form-item prop="phone">
                    <el-input 
                      v-model="basicForm.phone" 
                      placeholder="请输入手机号"
                      maxlength="11"
                      class="form-input"
                    />
                  </el-form-item>
                </div>
              </el-form>
            </div>

            <!-- 操作按钮 -->
            <div class="form-actions">
              <el-button type="primary" @click="saveProfile" :loading="isSaving">
                保存更改
              </el-button>
              <el-button @click="resetForm">重置</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 账户设置页面 -->
      <div v-if="activeNav === 'account'" class="content-page account-page">
        <div class="page-header">
          <AppBreadcrumb />
        </div>
        
        <div class="page-content">
          <div class="form-section">
            <h3 class="section-title">账户信息</h3>
            <div class="info-list">
              <div class="info-item">
                <span class="info-label">用户ID</span>
                <span class="info-value">{{ userInfo.userId }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">注册时间</span>
                <span class="info-value">{{ formatDate(userInfo.createTime) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">最后登录</span>
                <span class="info-value">{{ formatDate(userInfo.lastLoginTime) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">账户状态</span>
                <el-tag :type="userInfo.status === 'active' ? 'success' : 'danger'" size="small">
                  {{ getStatusText(userInfo.status) }}
                </el-tag>
              </div>
            </div>
          </div>

          <div class="form-section">
            <h3 class="section-title">偏好设置</h3>
            <div class="preference-list">
              <div class="preference-item">
                <div class="preference-info">
                  <span class="preference-label">主题设置</span>
                  <span class="preference-desc">选择您偏好的界面主题</span>
                </div>
                <el-select v-model="preferences.theme" class="preference-select">
                  <el-option label="浅色" value="light" />
                  <el-option label="深色" value="dark" />
                </el-select>
              </div>
            </div>
          </div>

          <div class="form-actions">
            <el-button type="primary" @click="savePreferences" :loading="isSaving">
              保存设置
            </el-button>
          </div>
        </div>
      </div>

      <!-- 安全设置页面 -->
      <div v-if="activeNav === 'security'" class="content-page security-page">
        <div class="page-header">
          <AppBreadcrumb />
        </div>
        
        <div class="page-content">
          <div class="form-section">
            <h3 class="section-title">密码设置</h3>
            <div class="security-item">
              <div class="security-info">
                <span class="security-label">登录密码</span>
              </div>
              <el-button type="primary" @click="showPasswordModal">
                修改密码
              </el-button>
            </div>
          </div>

          <div class="form-section">
            <h3 class="section-title">安全选项</h3>
            <div class="security-options">
              <div class="security-option">
                <div class="option-info">
                  <span class="option-label">两步验证</span>
                  <span class="option-desc">使用手机或邮箱验证码增强账户安全性</span>
                </div>
                <el-switch v-model="securitySettings.twoFactor" />
              </div>
              
              <div class="security-option">
                <div class="option-info">
                  <span class="option-label">登录通知</span>
                  <span class="option-desc">异常登录时发送通知到您的邮箱</span>
                </div>
                <el-switch v-model="securitySettings.loginNotification" />
              </div>
              
              <div class="security-option">
                <div class="option-info">
                  <span class="option-label">会话管理</span>
                  <span class="option-desc">管理当前登录的设备会话</span>
                </div>
                <el-button size="small" @click="showSessions">
                  查看会话
                </el-button>
              </div>
            </div>
          </div>

          <div class="form-actions">
            <el-button type="primary" @click="saveSecuritySettings" :loading="isSaving">
              保存设置
            </el-button>
          </div>
        </div>
      </div>

      <!-- 通知设置页面 -->
      <div v-if="activeNav === 'notifications'" class="content-page notifications-page">
        <div class="page-header">
          <AppBreadcrumb />
        </div>
        
        <div class="page-content">
          <div class="form-section" v-loading="isLoadingPrefs" style="margin-bottom: 28px;">
            <h3 class="section-title">通知渠道</h3>
            <div class="notification-options">
              <div class="notification-option" v-for="ch in channelPrefs" :key="ch.channelCode">
                <div class="option-info">
                  <span class="option-label option-label-strong" :class="'option-label--' + (channelCodeToBusinessValue[ch.channelCode] || ch.channelCode).toLowerCase()">
                    {{ channelLabelMap[channelCodeToBusinessValue[ch.channelCode] || ch.channelCode] || ch.channelCode }}
                  </span>
                  <span class="option-desc">{{ ch.description || '按渠道控制是否接收此类消息' }}</span>
                </div>
                <div class="option-actions" style="display:flex;align-items:center;gap:12px;">
                  <el-switch v-model="ch.enabledBool" :disabled="isLockedChannel(ch.channelCode)" />
                </div>
              </div>
            </div>
          </div>

          <div class="form-section" v-loading="isLoadingPrefs" style="margin-top: 20px;">
            <h3 class="section-title">通知类型</h3>
            <div class="notification-options">
              <div class="notification-option" v-for="tp in typePrefs" :key="tp.typeCode">
                <div class="option-info">
                  <span class="option-label option-label-strong" :class="'option-label--' + (typeCodeToBusinessValue[tp.typeCode] || tp.typeCode).toLowerCase()">
                    {{ typeLabelMap[typeCodeToBusinessValue[tp.typeCode] || tp.typeCode] || tp.typeCode }}
                  </span>
                  <span class="option-desc">{{ tp.description || '按类型控制是否接收此类消息' }}</span>
                </div>
                <el-switch v-model="tp.enabledBool" :disabled="isLockedType(tp.typeCode)" />
              </div>
            </div>
          </div>

          <div class="form-actions">
            <el-button type="primary" @click="saveNotificationSettings" :loading="isSaving">
              保存设置
            </el-button>
          </div>
        </div>
      </div>

      <!-- 我的引荐页面 -->
      <div v-if="activeNav === 'referrals'" class="content-page referrals-page">
        <div class="page-header">
          <AppBreadcrumb />
        </div>
        
        <div class="page-content referrals-content">
          <!-- 引荐码区域 -->
          <div class="referral-section">
            <h3 class="section-title">引荐信息</h3>
            <div class="referral-grid">
              <div class="referral-item">
                <label class="referral-label">您的专属引荐码</label>
                <div class="referral-input-group">
                  <el-input 
                    v-model="referralInfo.code" 
                    readonly
                    class="referral-input"
                    :loading="isLoadingReferralCode"
                    placeholder="加载中..."
                  />
                  <el-button 
                    type="primary" 
                    @click="copyToClipboard(referralInfo.code)"
                    :icon="Copy"
                    :disabled="!referralInfo.code || isLoadingReferralCode"
                  >
                    复制
                  </el-button>
                </div>
              </div>
              <div class="referral-item">
                <label class="referral-label">引荐链接</label>
                <div class="referral-input-group">
                  <el-input 
                    v-model="referralInfo.link" 
                    readonly
                    class="referral-input"
                    :loading="isLoadingReferralCode"
                    placeholder="加载中..."
                  />
                  <el-button 
                    type="primary" 
                    @click="copyToClipboard(referralInfo.link)"
                    :icon="Copy"
                    :disabled="!referralInfo.link || isLoadingReferralCode"
                  >
                    复制
                  </el-button>
                </div>
              </div>
            </div>
          </div>

                      <!-- 引荐用户列表 -->
          <div class="referral-users-section">
            <h3 class="section-title">引荐用户列表</h3>
            
            <!-- 有数据或加载中：展示表格 -->
            <div class="table-container" v-if="hasReferralData || isLoadingReferrals">
              <el-table 
                :data="referralUsers.list" 
                v-loading="isLoadingReferrals"
                class="referral-table"
                empty-text="暂无引荐用户"
                :height="tableHeight"
                :max-height="400"
                style="width: 100%"
              >
                <el-table-column prop="nickname" label="用户昵称" min-width="120">
                  <template #default="{ row }">
                    <div v-if="row" class="user-info">
                      <el-avatar 
                        :size="32" 
                        :src="row.avatar || ''" 
                        class="user-avatar"
                      >
                        <span class="avatar-text">
                          {{ (row.nickname || row.username || '用户')?.charAt(0)?.toUpperCase() || 'U' }}
                        </span>
                      </el-avatar>
                      <div class="user-details">
                        <div class="user-name">{{ row.nickname || row.username || '未设置昵称' }}</div>
                        <div class="user-email">{{ row.email || '未设置邮箱' }}</div>
                      </div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="registerTime" label="注册时间" min-width="150">
                  <template #default="{ row }">
                    {{ formatDateTime(row.registerTime) }}
                  </template>
                </el-table-column>
                <el-table-column prop="lastLoginTime" label="最后登录" min-width="150">
                  <template #default="{ row }">
                    <span v-if="row.lastLoginTime" class="last-login-time">
                      {{ formatDateTime(row.lastLoginTime) }}
                    </span>
                    <el-tag v-else type="info" size="small">
                      从未登录
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="contributionCredits" label="贡献积分" width="120">
                  <template #default="{ row }">
                    <span class="reward-credits">
                      +{{ row.contributionCredits || 0 }}
                    </span>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <!-- 无数据且非加载中：展示空数据盒子 -->
            <div v-else class="referral-empty-box">
              <div class="empty-box-inner">
                <div class="empty-graphic"></div>
                <div class="empty-title">暂无引荐用户</div>
                <div class="empty-desc">分享您的引荐链接，邀请好友注册即可获得积分奖励</div>
              </div>
            </div>
            
            <!-- 分页组件（仅在有数据时显示） -->
            <CommonPagination
              v-if="hasReferralData"
              v-model:current="referralUsers.currentPage"
              v-model:size="referralUsers.pageSize"
              :total="referralUsers.total"
              :default-page-size="10"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 密码修改弹窗 -->
    <el-dialog
      v-model="passwordModalVisible"
      title="修改密码"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form 
        ref="passwordFormRef"
        :model="passwordForm" 
        :rules="passwordFormRules"
        label-width="100px"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input 
            v-model="passwordForm.oldPassword" 
            type="password" 
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            placeholder="请输入新密码"
            show-password
          />
          <div class="password-strength" v-if="passwordForm.newPassword">
            <div class="strength-bar">
              <div 
                class="strength-fill" 
                :class="passwordStrength.class"
                :style="{ width: passwordStrength.percentage + '%' }"
              ></div>
            </div>
            <span class="strength-text">{{ passwordStrength.text }}</span>
          </div>
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="passwordModalVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handlePasswordChange"
          :loading="isPasswordChanging"
        >
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  User, 
  Camera, 
  Lock, 
  InfoFilled,
  Phone,
  Message,
  Operation,
  Setting,
  Copy
} from '@element-plus/icons-vue'
import { notifyPrefsApi } from '@/api/notify/prefs'
import ChannelBadge from '@/components/common/ChannelBadge.vue'
import { userApi } from '@/api/user'
import { referralApi } from '@/api/referral'
import { useAuthStore } from '@/stores/auth'
import CommonPagination from '@/components/common/Pagination.vue'
import AppBreadcrumb from '@/components/common/Breadcrumb.vue'

// 防抖工具函数
const debounce = (func, wait) => {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout)
      func(...args)
    }
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

export default {
  name: 'UserProfileGithub',
  components: {
    User,
    Camera,
    Lock,
    InfoFilled,
    Phone,
    Message,
    Operation,
    Setting,
    Copy,
    CommonPagination,
    AppBreadcrumb,
    ChannelBadge
  },
  setup() {
    // 响应式数据
    const activeNav = ref('profile')
    const isSaving = ref(false)
    const isPasswordChanging = ref(false)
    const passwordModalVisible = ref(false)
    const isLoadingUserInfo = ref(false)
    const tableHeight = ref(300) // 添加表格高度控制
    
    // 表单引用
    const passwordFormRef = ref()
    const basicFormRef = ref()

    // 获取认证store
    const authStore = useAuthStore()

    // 导航项目
    const navItems = [
      { key: 'profile', label: '个人资料', icon: 'User', badge: 0 },
      { key: 'account', label: '账户设置', icon: 'Setting', badge: 0 },
      { key: 'security', label: '安全设置', icon: 'Lock', badge: 0 },
      { key: 'notifications', label: '通知设置', icon: 'Message', badge: 0 },
      { key: 'referrals', label: '我的引荐', icon: 'User', badge: 0 }
    ]

    // 用户信息（从后端获取或缓存）
    const userInfo = reactive({
      userId: '',
      username: '',
      nickname: '',
      email: '',
      phone: '',
      avatar: '',
      status: 'active',
      createTime: '',
      lastLoginTime: '',
      lastPasswordChange: '',
      roleName: '',
      credits: 0
    })

    // 基本信息表单
    const basicForm = reactive({
      username: '',
      nickname: '',
      email: '',
      phone: ''
    })

    // 密码修改表单
    const passwordForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })

    // 偏好设置
    const preferences = reactive({
      theme: 'light'
    })

    // 安全设置
    const securitySettings = reactive({
      twoFactor: false,
      loginNotification: true
    })

    // 通知偏好（从后端获取并回显）
    const isLoadingPrefs = ref(false)
    const channelPrefs = reactive([])
    const typePrefs = reactive([])
    const channelLabelMap = {}
    const channelDescMap = {}
    const typeLabelMap = {}
    const typeDescMap = {}
    const channelCodeToBusinessValue = {}
    const typeCodeToBusinessValue = {}

    // 锁定规则：站内(inbox)与系统(system)不可关闭
    const isLockedChannel = (code) => {
      try {
        const key = (channelCodeToBusinessValue[code] || code || '').toLowerCase()
        return key === 'inbox'
      } catch (e) {
        return false
      }
    }

    const isLockedType = (code) => {
      try {
        const key = (typeCodeToBusinessValue[code] || code || '').toLowerCase()
        return key === 'system'
      } catch (e) {
        return false
      }
    }

    // 引荐相关数据
    const isLoadingReferrals = ref(false)
    const isLoadingReferralCode = ref(false)
    const referralInfo = reactive({
      code: '',
      link: '',
      shareDescription: ''
    })

    // 引荐用户列表数据
    const referralUsers = reactive({
      list: [], // 初始化为空数组，从API获取真实数据
      currentPage: 1,
      pageSize: 10, // 默认10条每页
      total: 0
    })

    // 表单验证规则
    const basicFormRules = {
      nickname: [
        { required: true, message: '请输入昵称', trigger: 'blur' },
        { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
      ],
      email: [
        // 允许为空，但如果填写了必须符合邮箱格式
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ],
      phone: [
        // 允许为空，但如果填写了必须符合手机号格式
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
      ]
    }

    const passwordFormRules = {
      oldPassword: [
        { required: true, message: '请输入当前密码', trigger: 'blur' }
      ],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 8, max: 20, message: '密码长度必须在8-20个字符之间', trigger: 'blur' },
        { 
          pattern: /^(?=.*[a-zA-Z])(?=.*\d)/, 
          message: '密码必须包含字母和数字', 
          trigger: 'blur' 
        }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入新密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== passwordForm.newPassword) {
              callback(new Error('两次输入密码不一致'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ]
    }

    // 计算属性
    const passwordStrength = computed(() => {
      const password = passwordForm.newPassword
      if (!password) return { text: '', percentage: 0, class: '' }
      
      let score = 0
      
      // 长度检查（8-20字符）
      if (password.length >= 8) score += 20
      if (password.length >= 12) score += 20
      if (password.length >= 16) score += 10
      
      // 字符类型检查
      if (/[a-zA-Z]/.test(password)) score += 20
      if (/\d/.test(password)) score += 20
      if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) score += 10
      
      if (score <= 40) {
        return { text: '弱', percentage: score, class: 'weak' }
      } else if (score <= 80) {
        return { text: '中', percentage: score, class: 'medium' }
      } else {
        return { text: '强', percentage: score, class: 'strong' }
      }
    })

    // 引荐列表是否有数据（用于控制空态与分页显示）
    const hasReferralData = computed(() => {
      try {
        return Array.isArray(referralUsers.list) && referralUsers.list.length > 0
      } catch (e) {
        return false
      }
    })

    // 方法
    const switchNav = (navKey) => {
      activeNav.value = navKey
      
      // 如切换到通知设置，获取通知偏好
      if (navKey === 'notifications') {
        fetchPrefsFromServer()
      }

      // 如切换到引荐页面，获取引荐相关数据
      if (navKey === 'referrals') {
        // 获取引荐码信息
        fetchReferralCode()
        // 获取引荐用户列表
        fetchReferralUsers()
        // 延迟计算表格高度，等待DOM更新
        setTimeout(calculateTableHeight, 150)
      }
    }

    // 获取用户信息（优先从缓存获取，缓存过期时从后端获取）
    const fetchUserInfo = async () => {
      try {
        isLoadingUserInfo.value = true
        
        // 首先尝试从认证store获取用户信息（优先使用缓存）
        const authUser = await authStore.getOrFetchUserInfo()
        
        if (authUser) {
          // 设置基础信息
          userInfo.userId = authUser.userId || ''
          userInfo.username = authUser.username || ''
          userInfo.nickname = authUser.nickname || ''
          userInfo.roleName = authUser.role || ''
          
          // 同步到表单
          basicForm.username = userInfo.username
          basicForm.nickname = userInfo.nickname
          
          // 检查是否需要从后端获取详细信息
          // 如果认证store中已有完整信息，则跳过API调用
          const needsDetailedInfo = !authUser.email || 
                                   !authUser.phone || 
                                   !authUser.avatar ||
                                   !authUser.createTime ||
                                   !authUser.lastLoginTime;
          
          if (needsDetailedInfo) {
            console.log('认证store中信息不完整，从后端获取详细信息')
            
            // 从后端获取详细用户信息
            const response = await userApi.getUserProfile()
            
            if (response.code === 200 && response.data) {
              const userData = response.data
              
              // 更新用户信息
              Object.assign(userInfo, {
                userId: userData.id || userData.userId || '',
                username: userData.username || '',
                nickname: userData.nickname || '',
                email: userData.email || '',
                phone: userData.phone || '',
                avatar: userData.avatar || '',
                status: userData.status === 1 ? 'active' : 'inactive',
                createTime: userData.createTime || '',
                lastLoginTime: userData.lastLoginTime || '',
                lastPasswordChange: userData.lastPasswordChange || '',
                roleName: userData.role || '',
                credits: userData.credits || 0
              })
              
              // 同步到表单
              basicForm.username = userInfo.username
              basicForm.nickname = userInfo.nickname
              basicForm.email = userInfo.email
              basicForm.phone = userInfo.phone
              
              // 更新认证store中的用户信息
              authStore.setUser({
                ...authUser,
                nickname: userInfo.nickname,
                email: userInfo.email,
                phone: userInfo.phone,
                avatar: userInfo.avatar,
                createTime: userInfo.createTime,
                lastLoginTime: userInfo.lastLoginTime
              })
              
              console.log('用户详细信息获取成功:', userInfo)
            } else {
              throw new Error(response.message || '获取用户信息失败')
            }
          } else {
            console.log('认证store中信息完整，直接使用缓存数据')
            
            // 使用认证store中的完整信息
            Object.assign(userInfo, {
              userId: authUser.userId || '',
              username: authUser.username || '',
              nickname: authUser.nickname || '',
              email: authUser.email || '',
              phone: authUser.phone || '',
              avatar: authUser.avatar || '',
              status: 'active', // 默认状态
              createTime: authUser.createTime || '',
              lastLoginTime: authUser.lastLoginTime || '',
              lastPasswordChange: authUser.lastPasswordChange || '',
              roleName: authUser.role || '',
              credits: authUser.credits || 0
            })
            
            // 同步到表单
            basicForm.username = userInfo.username
            basicForm.nickname = userInfo.nickname
            basicForm.email = userInfo.email
            basicForm.phone = userInfo.phone
            
            console.log('使用认证store中的缓存数据:', userInfo)
          }
        } else {
          throw new Error('无法获取用户信息')
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        ElMessage.error(`获取用户信息失败: ${error.message}`)
        
        // 如果后端获取失败，至少显示认证store中的基础信息
        const authUser = authStore.getUser()
        if (authUser) {
          userInfo.username = authUser.username || ''
          userInfo.nickname = authUser.nickname || ''
          basicForm.username = userInfo.username
          basicForm.nickname = userInfo.nickname
        }
      } finally {
        isLoadingUserInfo.value = false
      }
    }

    // 统一的保存方法，减少重复代码
    const saveSettings = async (type, data, successMessage) => {
      try {
        isSaving.value = true
        
        switch (type) {
          case 'profile': {
            // 调用后端API更新用户信息
            const response = await userApi.updateUserProfile({
              nickname: data.nickname || '',
              email: data.email || '', // 允许空字符串
              phone: data.phone || '', // 允许空字符串
              avatar: data.avatar || ''
            })
            
            if (response.code === 200) {
              // 更新本地用户信息
              Object.assign(userInfo, {
                nickname: data.nickname || '',
                email: data.email || '',
                phone: data.phone || '',
                avatar: data.avatar || ''
              })
              
              // 更新认证store中的用户信息
              const authUser = authStore.getUser()
              if (authUser) {
                authStore.setUser({
                  ...authUser,
                  nickname: data.nickname || '',
                  email: data.email || '',
                  phone: data.phone || ''
                })
              }
            } else {
              throw new Error(response.message || '保存失败')
            }
            break
          }
            
          case 'preferences': {
            // 偏好设置保存逻辑（暂时使用模拟）
            await new Promise(resolve => setTimeout(resolve, 1000))
            break
          }
            
          case 'security': {
            // 安全设置保存逻辑（暂时使用模拟）
            await new Promise(resolve => setTimeout(resolve, 1000))
            break
          }
            
          case 'notifications': {
            // 通知设置保存逻辑（暂时使用模拟）
            await new Promise(resolve => setTimeout(resolve, 1000))
            break
          }
        }
        
        ElMessage.success(successMessage)
      } catch (error) {
        console.error(`${type}保存失败:`, error)
        handleSaveError(error, type)
      } finally {
        isSaving.value = false
      }
    }

    // 统一的错误处理方法
    const handleSaveError = (error, type) => {
      const typeNames = {
        profile: '个人资料',
        preferences: '偏好设置',
        security: '安全设置',
        notifications: '通知设置'
      }
      
      if (error.response?.status === 400) {
        ElMessage.error(`${typeNames[type]}数据有误，请检查后重试`)
      } else if (error.response?.status === 401) {
        ElMessage.error('登录已过期，请重新登录')
      } else if (error.response?.status === 403) {
        ElMessage.error('权限不足，无法修改设置')
      } else {
        ElMessage.error(`${typeNames[type]}保存失败: ${error.message || '网络错误，请重试'}`)
      }
    }

    // 防抖处理的保存方法
    const debouncedSaveProfile = debounce(async () => {
      try {
        const valid = await basicFormRef.value.validate()
        if (valid) {
          await saveSettings('profile', basicForm, '个人资料保存成功')
        }
      } catch (error) {
        ElMessage.error('表单验证失败，请检查输入')
      }
    }, 500)

    const debouncedSavePreferences = debounce(async () => {
      await saveSettings('preferences', preferences, '偏好设置保存成功')
    }, 500)

    const debouncedSaveSecuritySettings = debounce(async () => {
      await saveSettings('security', securitySettings, '安全设置保存成功')
    }, 500)

    const debouncedSaveNotificationSettings = debounce(async () => {
      await savePrefsToServer()
    }, 500)

    // 原有的保存方法（保持兼容性，但使用新的统一方法）
    const saveProfile = async () => {
      await debouncedSaveProfile()
    }

    const savePreferences = async () => {
      await debouncedSavePreferences()
    }

    const saveSecuritySettings = async () => {
      await debouncedSaveSecuritySettings()
    }

    const saveNotificationSettings = async () => {
      await debouncedSaveNotificationSettings()
    }

    // 将 enabled 从 number<0/1> 与 boolean 双向映射
    const normalizePrefsFromServer = (channels, types) => {
      try {
        channelPrefs.splice(0, channelPrefs.length, ...(Array.isArray(channels) ? channels : []))
        channelPrefs.forEach(ch => {
          ch.enabledBool = (ch.enabled === 1 || ch.enabled === true)
          const businessKey = channelCodeToBusinessValue[ch.channelCode] || ch.channelCode
          ch.description = ch.description || channelDescMap[businessKey] || ''
          // 锁定项强制开启
          if (isLockedChannel(ch.channelCode)) ch.enabledBool = true
        })
        typePrefs.splice(0, typePrefs.length, ...(Array.isArray(types) ? types : []))
        typePrefs.forEach(tp => {
          tp.enabledBool = (tp.enabled === 1 || tp.enabled === true)
          const businessKey = typeCodeToBusinessValue[tp.typeCode] || tp.typeCode
          tp.description = tp.description || typeDescMap[businessKey] || ''
          // 锁定项强制开启
          if (isLockedType(tp.typeCode)) tp.enabledBool = true
        })
      } catch (e) {
        console.warn('normalizePrefsFromServer 失败', e)
      }
    }

    // 已不再使用徽章展示，保留函数避免潜在引用报错
    const mapChannelForBadge = (code) => {
      const c = (code || '').toLowerCase()
      return c
    }

    const fetchPrefsFromServer = async () => {
      try {
        isLoadingPrefs.value = true
        const [optCh, optTp] = await Promise.all([
          notifyPrefsApi.getChannelOptions(),
          notifyPrefsApi.getTypeOptions()
        ])

        if (optCh?.code === 200 && Array.isArray(optCh.data)) {
          // 建立渠道映射：DICT编码 -> 业务值/标签
          optCh.data.forEach(item => {
            const dictCode = item.code
            const label = item.label || dictCode
            const value = (item.value || '').toLowerCase()
            channelLabelMap[value || dictCode] = label
            channelDescMap[value || dictCode] = item.description || ''
            if (dictCode) channelCodeToBusinessValue[dictCode] = value || dictCode
          })
        }

        if (optTp?.code === 200 && Array.isArray(optTp.data)) {
          optTp.data.forEach(item => {
            const dictCode = item.code
            const label = item.label || dictCode
            const value = (item.value || '').toLowerCase()
            typeLabelMap[value || dictCode] = label
            typeDescMap[value || dictCode] = item.description || ''
            if (dictCode) typeCodeToBusinessValue[dictCode] = value || dictCode
          })
        }

        const [chRes, tpRes] = await Promise.all([
          notifyPrefsApi.getMyChannelPrefs(),
          notifyPrefsApi.getMyTypePrefs()
        ])

        if (chRes.code === 200 && tpRes.code === 200) {
          normalizePrefsFromServer(chRes.data || [], tpRes.data || [])
        } else {
          throw new Error(chRes.message || tpRes.message || '获取通知偏好失败')
        }
      } catch (error) {
        console.error('获取通知偏好失败:', error)
        ElMessage.error(`获取通知偏好失败: ${error.message || '网络错误'}`)
      } finally {
        isLoadingPrefs.value = false
      }
    }

    const savePrefsToServer = async () => {
      try {
        isSaving.value = true
        const channelsPayload = channelPrefs.map(ch => ({
          channelCode: ch.channelCode,
          enabled: isLockedChannel(ch.channelCode) ? 1 : (ch.enabledBool ? 1 : 0)
        }))
        const typesPayload = typePrefs.map(tp => ({
          typeCode: tp.typeCode,
          enabled: isLockedType(tp.typeCode) ? 1 : (tp.enabledBool ? 1 : 0)
        }))
        const [chSave, tpSave] = await Promise.all([
          notifyPrefsApi.saveMyChannelPrefs(channelsPayload),
          notifyPrefsApi.saveMyTypePrefs(typesPayload)
        ])
        if (chSave.code === 200 && tpSave.code === 200) {
          ElMessage.success('通知偏好保存成功')
        } else {
          throw new Error(chSave.message || tpSave.message || '保存失败')
        }
      } catch (error) {
        console.error('保存通知偏好失败:', error)
        ElMessage.error(`保存通知偏好失败: ${error.message || '网络错误'}`)
      } finally {
        isSaving.value = false
      }
    }

    const resetForm = () => {
      Object.assign(basicForm, {
        username: userInfo.username,
        nickname: userInfo.nickname,
        email: userInfo.email,
        phone: userInfo.phone
      })
      ElMessage.info('表单已重置')
    }

    const beforeAvatarUpload = (file) => {
      const isJPG = file.type === 'image/jpeg'
      const isPNG = file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isJPG && !isPNG) {
        ElMessage.error('头像只能是 JPG 或 PNG 格式!')
        return false
      }
      if (!isLt2M) {
        ElMessage.error('头像大小不能超过 2MB!')
        return false
      }
      return true
    }

    const handleAvatarUpload = async (options) => {
      try {
        // 创建FormData
        const formData = new FormData()
        formData.append('avatar', options.file)
        
        // 调用后端API上传头像
        const response = await userApi.uploadUserAvatar(formData)
        
        if (response.code === 200 && response.data) {
          // 更新头像URL
          userInfo.avatar = response.data.avatarUrl || response.data.url || ''
          
          // 更新认证store中的用户信息
          const authUser = authStore.getUser()
          if (authUser) {
            authStore.setUser({
              ...authUser,
              avatar: userInfo.avatar
            })
          }
          
          ElMessage.success('头像上传成功')
        } else {
          throw new Error(response.message || '头像上传失败')
        }
      } catch (error) {
        console.error('头像上传失败:', error)
        if (error.response?.status === 413) {
          ElMessage.error('文件过大，请选择小于2MB的图片')
        } else if (error.response?.status === 415) {
          ElMessage.error('文件格式不支持，请选择JPG或PNG格式')
        } else {
          ElMessage.error(`头像上传失败: ${error.message || '网络错误，请重试'}`)
        }
      }
    }

    const showPasswordModal = () => {
      passwordModalVisible.value = true
    }

    const handlePasswordChange = async () => {
      try {
        const valid = await passwordFormRef.value.validate()
        if (valid) {
          isPasswordChanging.value = true
          
          // 调用后端API修改密码
          const response = await userApi.changePassword({
            oldPassword: passwordForm.oldPassword,
            newPassword: passwordForm.newPassword
          })
          
          if (response.code === 200) {
            ElMessage.success('密码修改成功，请重新登录')
            
            // 清除缓存的用户信息
            authStore.logout()
            
            // 延迟跳转到登录页，让用户看到成功提示
            setTimeout(() => {
              // 跳转到登录页
              window.location.href = '/login'
            }, 1500)
            
            // 不需要关闭对话框和重置表单，因为即将退出登录
          } else {
            throw new Error(response.message || '密码修改失败')
          }
        }
      } catch (error) {
        console.error('密码修改失败:', error)
        
        // 详细的错误处理
        if (error.response) {
          const { status, data } = error.response
          switch (status) {
            case 400:
              ElMessage.error(data.message || '请求参数错误，请检查输入')
              break
            case 401:
              ElMessage.error('当前密码错误，请重新输入')
              break
            case 403:
              ElMessage.error('权限不足，无法修改密码')
              break
            case 500:
              ElMessage.error('服务器内部错误，请稍后重试')
              break
            default:
              ElMessage.error(data.message || '密码修改失败，请重试')
          }
        } else if (error.message) {
          ElMessage.error(`密码修改失败: ${error.message}`)
        } else {
          ElMessage.error('密码修改失败，请重试')
        }
      } finally {
        isPasswordChanging.value = false
      }
    }

    const resetPasswordForm = () => {
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      passwordFormRef.value?.resetFields()
    }

    const formatDate = (dateStr) => {
      if (!dateStr) return 'N/A'
      return new Date(dateStr).toLocaleDateString('zh-CN')
    }

    // 新增：精确到秒级的时间格式化函数
    const formatDateTime = (dateStr) => {
      if (!dateStr) return 'N/A'
      try {
        const date = new Date(dateStr)
        // 检查日期是否有效
        if (isNaN(date.getTime())) {
          return 'N/A'
        }
        
        // 格式化为：2025-01-20 15:45:30
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        const seconds = String(date.getSeconds()).padStart(2, '0')
        
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
      } catch (error) {
        console.error('时间格式化失败:', error)
        return 'N/A'
      }
    }

    const getStatusText = (status) => {
      const statusMap = {
        active: '正常',
        inactive: '禁用',
        pending: '待审核'
      }
      return statusMap[status] || '未知'
    }

    const showSessions = () => {
      ElMessage.info('会话管理功能开发中...')
    }



    // 复制到剪贴板功能
    const copyToClipboard = async (text) => {
      try {
        if (navigator.clipboard && window.isSecureContext) {
          await navigator.clipboard.writeText(text)
        } else {
          // 降级方案
          const textArea = document.createElement('textarea')
          textArea.value = text
          textArea.style.position = 'fixed'
          textArea.style.left = '-999999px'
          textArea.style.top = '-999999px'
          document.body.appendChild(textArea)
          textArea.focus()
          textArea.select()
          document.execCommand('copy')
          document.body.removeChild(textArea)
        }
        ElMessage.success('已复制到剪贴板')
      } catch (error) {
        console.error('复制失败:', error)
        ElMessage.error('复制失败，请手动复制')
      }
    }

    // 分页处理函数
    const handleSizeChange = (val) => {
      referralUsers.pageSize = val
      referralUsers.currentPage = 1
      // 这里可以调用API重新获取数据
      fetchReferralUsers()
    }

    const handleCurrentChange = (val) => {
      referralUsers.currentPage = val
      // 这里可以调用API重新获取数据
      fetchReferralUsers()
    }

    // 获取引荐码信息
    const fetchReferralCode = async () => {
      try {
        isLoadingReferralCode.value = true
        console.log('开始获取引荐码信息...')
        
        const response = await referralApi.getReferralCode()
        console.log('引荐码API响应:', response)
        
        if (response.code === 200 && response.data) {
          // 直接使用API返回的数据
          referralInfo.code = response.data.referralCode || ''
          referralInfo.link = response.data.referralLink || ''
          referralInfo.shareDescription = response.data.shareDescription || '分享给朋友，获得积分奖励'
          
          console.log('引荐码信息更新成功:', {
            code: referralInfo.code,
            link: referralInfo.link
          })
        } else {
          throw new Error(response.message || '获取引荐码信息失败')
        }
      } catch (error) {
        console.error('获取引荐码信息失败:', error)
        ElMessage.error(`获取引荐码信息失败: ${error.message || '未知错误'}`)
      } finally {
        isLoadingReferralCode.value = false
      }
    }

    // 获取引荐用户列表
    const fetchReferralUsers = async () => {
      try {
        isLoadingReferrals.value = true
        console.log('开始获取引荐用户列表...', {
          page: referralUsers.currentPage,
          pageSize: referralUsers.pageSize
        })
        
        // 调用API获取引荐用户数据
        const response = await referralApi.getReferralUsers(
          referralUsers.currentPage,
          referralUsers.pageSize
        )
        
        console.log('引荐用户列表API响应:', response)
        
        if (response.code === 200 && response.data) {
          // 检查数据格式，支持分页格式
          if (response.data.list && Array.isArray(response.data.list)) {
            // 分页格式: { list: [], total: 0, currentPage: 1, pageSize: 10 }
            referralUsers.list = response.data.list.filter(u => u && typeof u === 'object')
            referralUsers.total = response.data.total || 0
            referralUsers.currentPage = response.data.currentPage || referralUsers.currentPage
          } else if (Array.isArray(response.data)) {
            // 简单数组格式
            referralUsers.list = response.data.filter(u => u && typeof u === 'object')
            referralUsers.total = response.data.length
          } else {
            // 其他格式，尝试直接使用
            referralUsers.list = []
            referralUsers.total = 0
            console.warn('引荐用户列表数据格式不正确:', response.data)
          }
          
          console.log('引荐用户列表更新成功:', {
            count: referralUsers.list.length,
            total: referralUsers.total,
            currentPage: referralUsers.currentPage
          })
        } else {
          throw new Error(response.message || '获取引荐用户列表失败')
        }
      } catch (error) {
        console.error('获取引荐用户列表失败:', error)
        ElMessage.error(`获取引荐用户列表失败: ${error.message || '未知错误'}`)
        
        // 失败时重置数据
        referralUsers.list = []
        referralUsers.total = 0
      } finally {
        isLoadingReferrals.value = false
      }
    }

    // 监听来自AppHeader的切换事件
    const handleSwitchToReferrals = () => {
      switchNav('referrals')
    }

    // ResizeObserver错误处理
    const handleResizeObserverError = () => {
      // 抑制ResizeObserver错误
      const resizeObserverErrorHandler = (e) => {
        if (e.message === 'ResizeObserver loop completed with undelivered notifications.') {
          e.stopPropagation()
          e.preventDefault()
          return false
        }
      }
      
      window.addEventListener('error', resizeObserverErrorHandler)
      return resizeObserverErrorHandler
    }
    
    // 计算表格高度
    const calculateTableHeight = () => {
      // 使用nextTick确保 DOM已更新
      nextTick(() => {
        try {
          const container = document.querySelector('.referral-users-section')
          if (container) {
            const containerHeight = container.clientHeight
            const titleHeight = 40 // section-title高度
            const paginationHeight = 60 // 分页组件高度
            const padding = 32 // 内边距
            
            const calculatedHeight = Math.max(200, containerHeight - titleHeight - paginationHeight - padding)
            tableHeight.value = Math.min(calculatedHeight, 400) // 最大高度400px
          }
        } catch (error) {
          console.warn('计算表格高度失败:', error)
          tableHeight.value = 300 // 默认高度
        }
      })
    }
    
    // 生命周期
    onMounted(() => {
      console.log('GitHub风格个人信息页面加载完成')
      
      // 处理ResizeObserver错误
      const errorHandler = handleResizeObserverError()
      
      fetchUserInfo() // 在组件挂载时获取用户信息
      // 预加载通知偏好（用户进入通知设置页时也会再次刷新）
      fetchPrefsFromServer()
      
      // 如果默认页面是引荐页面，初始化引荐数据
      if (activeNav.value === 'referrals') {
        fetchReferralCode()
        fetchReferralUsers()
        // 延迟计算表格高度
        setTimeout(calculateTableHeight, 100)
      }
      
      // 监听来自AppHeader的事件
      window.addEventListener('switchToReferrals', handleSwitchToReferrals)
      
      // 监听窗口大小变化
      window.addEventListener('resize', calculateTableHeight)
      
      // 清理函数
      onUnmounted(() => {
        window.removeEventListener('switchToReferrals', handleSwitchToReferrals)
        window.removeEventListener('resize', calculateTableHeight)
        window.removeEventListener('error', errorHandler)
      })
    })

    // 清理事件监听器
    onUnmounted(() => {
      window.removeEventListener('switchToReferrals', handleSwitchToReferrals)
    })

    return {
      // 响应式数据
      activeNav,
      isSaving,
      isPasswordChanging,
      passwordModalVisible,
      isLoadingUserInfo,
      navItems,
      userInfo,
      basicForm,
      passwordForm,
      preferences,
      securitySettings,
      
      // 表单引用
      passwordFormRef,
      basicFormRef,
      
      // 验证规则
      basicFormRules,
      passwordFormRules,
      
      // 计算属性
      passwordStrength,
      hasReferralData,
      
      // 图标组件
      Copy,
      
      // 方法
      switchNav,
      fetchUserInfo,
      saveProfile,
      resetForm,
      savePreferences,
      saveSecuritySettings,
      saveNotificationSettings,
      // 通知偏好
      isLoadingPrefs,
      channelPrefs,
      typePrefs,
      channelLabelMap,
      typeLabelMap,
      channelCodeToBusinessValue,
      typeCodeToBusinessValue,
      isLockedChannel,
      isLockedType,
      mapChannelForBadge,
      beforeAvatarUpload,
      handleAvatarUpload,
      showPasswordModal,
      handlePasswordChange,
      resetPasswordForm,
      formatDate,
      formatDateTime,
      getStatusText,
      showSessions,
      copyToClipboard,
      handleSizeChange,
      handleCurrentChange,
      fetchReferralUsers,
      fetchReferralCode,
      
      // 引荐相关数据
      isLoadingReferrals,
      isLoadingReferralCode,
      referralInfo,
      referralUsers,
      
      // 表格相关
      tableHeight,
      calculateTableHeight
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/views/profile-github.scss';
</style>