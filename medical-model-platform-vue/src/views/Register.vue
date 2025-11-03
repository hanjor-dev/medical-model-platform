<template>
  <div class="register-page">
    <!-- 页面头部Logo和名称 -->
    <div class="header-section">
      <div class="logo-section">
        <div class="logo-title-row">
          <div class="logo">
            <svg class="logo-icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg">
              <path d="M486.6048 973.824L102.4 754.2784v-468.992L512 51.2l409.6 234.0352v336.896h-102.4V344.7296l-307.2-175.5648-307.2 175.616v350.1056l332.5952 190.1056-50.7904 88.8832z" fill="#1890ff"></path>
              <path d="M855.04 256l51.2 88.7296-372.48 215.04-1.9456 3.328L153.6 344.7296 204.8 256l325.12 187.648L855.04 256z" fill="#1890ff"></path>
              <path d="M476.16 665.6l102.4 51.2v-204.8h-102.4v153.6z" fill="#1890ff"></path>
              <path d="M849.8176 691.2l76.8 133.0176-76.8 133.0176h-153.6l-76.8-133.0176 76.8-133.0176h153.6z" fill="#52c41a"></path>
            </svg>
          </div>
          <h1 class="platform-title">Model-Y</h1>
        </div>
        <p class="platform-subtitle">医疗影像模型管理平台</p>
      </div>
    </div>

    <!-- 背景动画 -->
    <div class="background-animation">
      <div class="floating-shape"></div>
      <div class="floating-shape"></div>
      <div class="floating-shape"></div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 注册容器 -->
      <div class="register-container">
        <!-- 注册标题 -->
        <h2 class="register-title">注册</h2>

        <!-- 注册表单 -->
        <form class="register-form" @submit.prevent="handleRegister">
          <!-- 用户名输入 -->
          <div class="form-group">
            <label class="form-label" for="username">用户名 <span class="required">*</span></label>
            <input 
              type="text" 
              id="username" 
              v-model="registerForm.username"
              class="form-input" 
              :class="{ 'error': usernameError, 'success': usernameSuccess }"
              placeholder="请输入用户名（3-20个字符）"
              autocomplete="username"
              required
              @focus="clearFieldError('username')"
              @input="clearFieldError('username')"
              @blur="validateField('username')"
            >
          </div>

          <!-- 邮箱输入 -->
          <div class="form-group">
            <label class="form-label" for="email">邮箱地址 <span class="optional">（可选）</span></label>
            <input 
              type="email" 
              id="email" 
              v-model="registerForm.email"
              class="form-input" 
              :class="{ 'error': emailError, 'success': emailSuccess }"
              placeholder="请输入邮箱地址"
              autocomplete="email"
              @focus="clearFieldError('email')"
              @input="clearFieldError('email')"
              @blur="validateField('email')"
            >
          </div>

          <!-- 昵称输入 -->
          <div class="form-group">
            <label class="form-label" for="nickname">昵称 <span class="optional">（可选）</span></label>
            <input 
              type="text" 
              id="nickname" 
              v-model="registerForm.nickname"
              class="form-input" 
              :class="{ 'error': nicknameError, 'success': nicknameSuccess }"
              placeholder="请输入昵称（1-50个字符）"
              autocomplete="nickname"
              @focus="clearFieldError('nickname')"
              @input="clearFieldError('nickname')"
              @blur="validateField('nickname')"
            >
          </div>

          <!-- 密码输入 -->
          <div class="form-group">
            <label class="form-label" for="password">密码 <span class="required">*</span></label>
            <div class="password-input">
              <input 
                type="password" 
                id="password" 
                v-model="registerForm.password"
                class="form-input" 
                :class="{ 'error': passwordError, 'success': passwordSuccess }"
                placeholder="请输入密码（8-20个字符）"
                autocomplete="new-password"
                required
                @focus="clearFieldError('password')"
                @input="clearFieldError('password')"
                @blur="validateField('password')"
              >
              <button type="button" class="password-toggle" @click="togglePasswordVisibility('password')">
                {{ passwordVisible ? '🙈' : '👁' }}
              </button>
            </div>
          </div>

          <!-- 确认密码输入 -->
          <div class="form-group">
            <label class="form-label" for="confirmPassword">确认密码 <span class="required">*</span></label>
            <div class="password-input">
              <input 
                type="password" 
                id="confirmPassword" 
                v-model="registerForm.confirmPassword"
                class="form-input" 
                :class="{ 'error': confirmPasswordError, 'success': confirmPasswordSuccess }"
                placeholder="请再次输入密码"
                autocomplete="new-password"
                required
                @focus="clearFieldError('confirmPassword')"
                @input="clearFieldError('confirmPassword')"
                @blur="validateField('confirmPassword')"
              >
              <button type="button" class="confirm-password-toggle" @click="togglePasswordVisibility('confirmPassword')">
                {{ confirmPasswordVisible ? '🙈' : '👁' }}
              </button>
            </div>
          </div>

          <!-- 推荐码输入 -->
          <div class="form-group">
            <label class="form-label" for="referralCode">推荐码 <span class="optional">（可选，与团队码互斥）</span></label>
            <input 
              type="text" 
              id="referralCode" 
              v-model="registerForm.referralCode"
              class="form-input" 
              :class="{ 'error': referralTeamXorError }"
              placeholder="请输入推荐码（8位字符）"
              maxlength="20"
              @focus="clearFieldError('referralCode')"
              @input="clearFieldError('referralCode')"
            >
          </div>

          <!-- 团队码输入 -->
          <div class="form-group">
            <label class="form-label" for="teamCode">团队码 <span class="optional">（可选，与推荐码互斥）</span></label>
            <input 
              type="text" 
              id="teamCode" 
              v-model="registerForm.teamCode"
              class="form-input" 
              :class="{ 'error': referralTeamXorError }"
              placeholder="请输入团队码（20位以内）"
              maxlength="20"
              @focus="clearFieldError('teamCode')"
              @input="clearFieldError('teamCode')"
            >
          </div>

          <!-- 条款和条件 -->
          <div class="terms-section">
            <div class="checkbox-group">
              <div 
                class="custom-checkbox" 
                :class="{ 'checked': termsAccepted }"
                @click="toggleTerms"
              ></div>
              <label class="checkbox-label" @click="toggleTerms">
                我已阅读并同意 <a href="#" target="_blank" @click.prevent="showTerms">《用户协议》</a> 和 <a href="#" target="_blank" @click.prevent="showPrivacy">《隐私政策》</a>
              </label>
            </div>
          </div>

          <!-- 注册按钮 -->
          <button type="submit" class="register-button" :class="{ 'loading': isLoading }" :disabled="isLoading || !isFormValid">
            <span class="button-text" v-show="!isLoading">注册</span>
            <div class="button-loading" v-show="isLoading"></div>
          </button>
        </form>

        <!-- 其他操作 -->
        <div class="other-actions">
          <span>已有账户？</span>
          <router-link to="/login" class="login-link">立即登录</router-link>
        </div>
      </div>
    </div>

    <!-- 加载遮罩 -->
    <div class="loading-overlay" v-show="showLoadingOverlay">
      <div class="loading-spinner"></div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { handleException } from '@/utils/exceptionHandler'
import { ElMessage } from 'element-plus'

export default {
  name: 'RegisterPage',
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()

    // 响应式数据
    const registerForm = reactive({
      username: '',
      email: '',
      nickname: '',
      password: '',
      confirmPassword: '',
      referralCode: '',
      teamCode: ''
    })

    const isLoading = ref(false)
    const showLoadingOverlay = ref(false)
    const termsAccepted = ref(false)

    // 密码可见性
    const passwordVisible = ref(false)
    const confirmPasswordVisible = ref(false)

    // 字段验证状态
    const usernameError = ref(false)
    const usernameSuccess = ref(false)
    const emailError = ref(false)
    const emailSuccess = ref(false)
    const nicknameError = ref(false)
    const nicknameSuccess = ref(false)
    const passwordError = ref(false)
    const passwordSuccess = ref(false)
    const confirmPasswordError = ref(false)
    const confirmPasswordSuccess = ref(false)
    const referralTeamXorError = ref(false)

    // 计算属性
    const isFormValid = computed(() => {
      const xorOk = !registerForm.referralCode.trim() || !registerForm.teamCode.trim()
      return registerForm.username.trim() && 
             registerForm.password.trim() && 
             registerForm.confirmPassword.trim() && 
             termsAccepted.value &&
             !usernameError.value &&
             !passwordError.value &&
             !confirmPasswordError.value &&
             xorOk
    })

    // 方法
    const clearFieldError = (field) => {
      if (field === 'username') {
        usernameError.value = false
        usernameSuccess.value = false
      } else if (field === 'email') {
        emailError.value = false
        emailSuccess.value = false
      } else if (field === 'nickname') {
        nicknameError.value = false
        nicknameSuccess.value = false
      } else if (field === 'password') {
        passwordError.value = false
        passwordSuccess.value = false
      } else if (field === 'confirmPassword') {
        confirmPasswordError.value = false
        confirmPasswordSuccess.value = false
      } else if (field === 'referralCode' || field === 'teamCode') {
        referralTeamXorError.value = false
      }
    }

    const togglePasswordVisibility = (field) => {
      if (field === 'password') {
        passwordVisible.value = !passwordVisible.value
        const input = document.getElementById('password')
        if (input) {
          input.type = passwordVisible.value ? 'text' : 'password'
        }
      } else if (field === 'confirmPassword') {
        confirmPasswordVisible.value = !confirmPasswordVisible.value
        const input = document.getElementById('confirmPassword')
        if (input) {
          input.type = confirmPasswordVisible.value ? 'text' : 'password'
        }
      }
    }

    const toggleTerms = () => {
      termsAccepted.value = !termsAccepted.value
    }

    const showTerms = () => {
      ElMessage.info('用户协议：在实际项目中会跳转到用户协议页面')
    }

    const showPrivacy = () => {
      ElMessage.info('隐私政策：在实际项目中会跳转到隐私政策页面')
    }

    const validateField = (field) => {
      const value = registerForm[field].trim()
      let isValid = true

      if (field === 'username') {
        if (!value) {
          isValid = false
        } else if (value.length < 3) {
          isValid = false
        } else if (value.length > 20) {
          isValid = false
        } else if (!/^[a-zA-Z0-9_]+$/.test(value)) {
          isValid = false
        }
      } else if (field === 'email') {
        if (value && !isValidEmail(value)) {
          isValid = false
        }
      } else if (field === 'nickname') {
        if (value && value.length > 50) {
          isValid = false
        }
      } else if (field === 'password') {
        if (!value) {
          isValid = false
        } else if (value.length < 8) {
          isValid = false
        } else if (value.length > 20) {
          isValid = false
        }
      } else if (field === 'confirmPassword') {
        if (!value) {
          isValid = false
        } else if (value !== registerForm.password.trim()) {
          isValid = false
        }
      }

      // XOR 校验（推荐码与团队码二选一，或都不填）
      const bothFilled = registerForm.referralCode.trim() && registerForm.teamCode.trim()
      referralTeamXorError.value = !!bothFilled

      if (isValid) {
        if (field === 'username') {
          usernameError.value = false
          usernameSuccess.value = true
        } else if (field === 'email') {
          emailError.value = false
          emailSuccess.value = true
        } else if (field === 'nickname') {
          nicknameError.value = false
          nicknameSuccess.value = true
        } else if (field === 'password') {
          passwordError.value = false
          passwordSuccess.value = true
        } else if (field === 'confirmPassword') {
          confirmPasswordError.value = false
          confirmPasswordSuccess.value = true
        }
      } else {
        if (field === 'username') {
          usernameSuccess.value = false
          usernameError.value = true
        } else if (field === 'email') {
          emailSuccess.value = false
          emailError.value = true
        } else if (field === 'nickname') {
          nicknameSuccess.value = false
          nicknameError.value = true
        } else if (field === 'password') {
          passwordSuccess.value = false
          passwordError.value = true
        } else if (field === 'confirmPassword') {
          confirmPasswordSuccess.value = false
          confirmPasswordError.value = true
        }
      }

      return isValid && !referralTeamXorError.value
    }

    const isValidEmail = (email) => {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      return emailRegex.test(email)
    }

    const handleRegister = async () => {
      // 验证所有必填字段
      const usernameValid = validateField('username')
      const passwordValid = validateField('password')
      const confirmPasswordValid = validateField('confirmPassword')
      
      // 验证可选字段
      if (registerForm.email.trim()) {
        validateField('email')
      }
      if (registerForm.nickname.trim()) {
        validateField('nickname')
      }
      
      if (!termsAccepted.value) {
        ElMessage.warning('请先同意用户协议和隐私政策')
        return
      }
      
      if (!usernameValid || !passwordValid || !confirmPasswordValid) {
        ElMessage.warning('请检查必填字段信息是否正确')
        return
      }

      // XOR 校验提示
      if (registerForm.referralCode.trim() && registerForm.teamCode.trim()) {
        ElMessage.warning('推荐码与团队码只能填写一个')
        return
      }

      try {
        isLoading.value = true
        showLoadingOverlay.value = true

        // 准备注册数据，过滤空值
        const registerData = {
          username: registerForm.username.trim(),
          password: registerForm.password.trim()
        }
        
        if (registerForm.email.trim()) {
          registerData.email = registerForm.email.trim()
        }
        if (registerForm.nickname.trim()) {
          registerData.nickname = registerForm.nickname.trim()
        }
        if (registerForm.referralCode.trim()) {
          registerData.referralCode = registerForm.referralCode.trim()
        }
        if (registerForm.teamCode.trim()) {
          registerData.teamCode = registerForm.teamCode.trim()
        }

        // 调用注册API
        const result = await authStore.register(registerData)
        
        if (result.success) {
          // 显示成功消息
          ElMessage.success('注册成功！正在跳转到登录页面...')
          
          // 立即跳转，提供更好的用户体验
          router.push('/login')
        } else {
          // 显示错误消息
          ElMessage.error(result.message || '注册失败，请重试')
        }
      } catch (error) {
        // 使用异常处理工具处理错误
        handleException(error, '用户注册')
      } finally {
        isLoading.value = false
        showLoadingOverlay.value = false
      }
    }

    // 生命周期
    onMounted(() => {
      // 自动聚焦到用户名输入框
      const usernameInput = document.getElementById('username')
      if (usernameInput) {
        usernameInput.focus()
      }

      // 从URL读取推荐码 ?refCode=XXXX 或 ?referralCode=XXXX
      try {
        const params = new URLSearchParams(window.location.search || '')
        const code = params.get('refCode') || params.get('refcode') || params.get('referralCode') || params.get('referralcode')
        if (code) {
          registerForm.referralCode = code
        }
      } catch (e) {
        // ignore parse error
      }
    })

    return {
      registerForm,
      isLoading,
      showLoadingOverlay,
      termsAccepted,
      passwordVisible,
      confirmPasswordVisible,
      usernameError,
      usernameSuccess,
      emailError,
      emailSuccess,
      nicknameError,
      nicknameSuccess,
      passwordError,
      passwordSuccess,
      confirmPasswordError,
      confirmPasswordSuccess,
      referralTeamXorError,
      isFormValid,
      clearFieldError,
      togglePasswordVisibility,
      toggleTerms,
      showTerms,
      showPrivacy,
      validateField,
      handleRegister
    }
  }
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.register-page {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  background: linear-gradient(135deg, #f0f8ff 0%, #e6f3ff 50%, #d6e7ff 100%);
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

/* 页面头部Logo和名称 */
.header-section {
  position: relative;
  width: 100%;
  padding: 30px 0 30px 50px;
  text-align: left;
  z-index: 3;
  flex-shrink: 0;
  justify-content: flex-start;
}

.logo-section {
  text-align: left;
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.logo-title-row {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  margin-bottom: 0;
}

.logo {
  width: 60px;
  height: 60px;
  background: #f0f8ff;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0;
  box-shadow: 0 8px 20px rgba(24, 144, 255, 0.15);
}

.logo-icon {
  width: 60px;
  height: 60px;
  display: block;
}

.platform-title {
  font-size: 36px;
  font-weight: 800;
  color: #1a1a1a;
  margin-bottom: 0;
  margin-left: 12px;
  font-family: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  letter-spacing: -0.5px;
}

.platform-subtitle {
  font-size: 16px;
  color: #666;
  font-weight: 400;
  margin-top: 8px;
}

/* 背景动画效果 */
.background-animation {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
}

.floating-shape {
  position: absolute;
  background: rgba(24, 144, 255, 0.15);
  border-radius: 50%;
  animation: float 6s ease-in-out infinite;
}

.floating-shape:nth-child(1) {
  width: 80px;
  height: 80px;
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.floating-shape:nth-child(2) {
  width: 120px;
  height: 120px;
  top: 60%;
  right: 10%;
  animation-delay: 2s;
}

.floating-shape:nth-child(3) {
  width: 60px;
  height: 60px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

/* 主要内容区域 */
.main-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  padding: 20px;
  z-index: 2;
  width: 100%;
  max-width: 500px;
  min-width: 320px;
}

/* 注册容器 */
.register-container {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08), 0 8px 16px rgba(0, 0, 0, 0.06);
  padding: 40px 50px;
  width: 100%;
  animation: slideIn 0.8s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 注册标题 */
.register-title {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a1a;
  text-align: center;
  margin-bottom: 25px;
  font-family: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  letter-spacing: -0.3px;
}

/* 表单样式 */
.register-form {
  margin-bottom: 15px;
}

.form-group {
  margin-bottom: 12px;
  position: relative;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.required {
  color: #ff4d4f;
  margin-left: 2px;
}

.optional {
  color: #999;
  font-size: 12px;
  margin-left: 2px;
}

.form-input {
  width: 100%;
  padding: 15px 20px;
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  font-size: 16px;
  transition: all 0.3s ease;
  background: white;
  position: relative;
  z-index: 10;
}

.form-input:focus {
  outline: none;
  border-color: #91d5ff;
  background: white;
  box-shadow: 0 0 0 3px rgba(145, 213, 255, 0.2);
}

.form-input.error {
  border-color: #ff4d4f;
  background: #fff2f0;
}

.form-input.success {
  border-color: #52c41a;
  background: #f6ffed;
}

/* 密码输入框 */
.password-input {
  position: relative;
  z-index: 10;
}

.password-toggle,
.confirm-password-toggle {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  color: #7f8c8d;
  font-size: 18px;
  z-index: 5;
}

/* 条款和条件 */
.terms-section {
  margin-bottom: 20px;
}

.checkbox-group {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.custom-checkbox {
  width: 18px;
  height: 18px;
  border: 2px solid #d9d9d9;
  border-radius: 4px;
  margin-top: 2px;
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.custom-checkbox.checked {
  background: #1890ff;
  border-color: #1890ff;
}

.custom-checkbox.checked::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-size: 12px;
  font-weight: bold;
}

.checkbox-label {
  font-size: 14px;
  color: #666;
  cursor: pointer;
  line-height: 1.5;
}

.checkbox-label a {
  color: #1890ff;
  text-decoration: none;
}

.checkbox-label a:hover {
  text-decoration: underline;
}

/* 注册按钮 */
.register-button {
  width: 100%;
  padding: 15px;
  background: linear-gradient(135deg, #1890ff, #40a9ff);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.register-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(24, 144, 255, 0.25);
  background: linear-gradient(135deg, #40a9ff, #1890ff);
}

.register-button:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 8px 20px rgba(24, 144, 255, 0.2);
}

.register-button:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.button-loading {
  display: none;
  width: 20px;
  height: 20px;
  border: 2px solid transparent;
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.register-button.loading .button-text {
  display: none;
}

.register-button.loading .button-loading {
  display: block;
}

/* 其他操作 */
.other-actions {
  text-align: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.login-link {
  color: #1890ff;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.3s ease;
}

.login-link:hover {
  color: #40a9ff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-section {
    padding: 20px 0 20px 30px;
  }
  
  .logo {
    width: 50px;
    height: 50px;
  }
  
  .logo-icon {
    width: 50px;
    height: 50px;
  }
  
  .platform-title {
    font-size: 28px;
    margin-left: 10px;
  }
  
  .platform-subtitle {
    font-size: 14px;
  }
  
  .main-content {
    padding: 15px;
    max-width: 450px;
    min-width: 300px;
  }
  
  .register-container {
    padding: 30px 25px;
  }
  
  .register-title {
    font-size: 28px;
    margin-bottom: 20px;
  }
  
  .form-group {
    margin-bottom: 10px;
  }
  
  .form-input {
    padding: 12px 15px;
    font-size: 14px;
  }

  .register-button {
    padding: 12px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .header-section {
    padding: 15px 0 15px 20px;
  }
  
  .logo {
    width: 45px;
    height: 45px;
  }
  
  .logo-icon {
    width: 45px;
    height: 45px;
  }
  
  .platform-title {
    font-size: 24px;
    margin-left: 8px;
  }
  
  .platform-subtitle {
    font-size: 12px;
  }
  
  .main-content {
    padding: 10px;
    max-width: 400px;
    min-width: 280px;
  }
  
  .register-container {
    padding: 25px 20px;
  }
  
  .register-title {
    font-size: 24px;
    margin-bottom: 18px;
  }
}

/* 加载动画 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
</style> 