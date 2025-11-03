<template>
  <div class="login-page">
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
      <!-- 登录容器 -->
      <div class="login-container">
        <!-- 登录标题 -->
        <h2 class="login-title">登录</h2>

        <!-- 登录表单 -->
        <form class="login-form" @submit.prevent="handleLogin">
          <div class="form-group">
            <label class="form-label" for="username">用户名或邮箱</label>
            <input 
              type="text" 
              id="username" 
              v-model="loginForm.username"
              class="form-input" 
              :class="{ 'error': usernameError, 'success': usernameSuccess }"
              placeholder="请输入用户名或邮箱"
              autocomplete="username"
              required
              @focus="clearFieldError('username')"
              @input="clearFieldError('username')"
            >
          </div>

          <div class="form-group">
            <label class="form-label" for="password">密码</label>
            <input 
              type="password" 
              id="password" 
              v-model="loginForm.password"
              class="form-input" 
              :class="{ 'error': passwordError, 'success': passwordSuccess }"
              placeholder="请输入密码"
              autocomplete="current-password"
              required
              @focus="clearFieldError('password')"
              @input="clearFieldError('password')"
            >
          </div>

          <!-- 记住密码 -->
          <div class="remember-section">
            <div class="checkbox-group">
              <div 
                class="custom-checkbox" 
                :class="{ 'checked': rememberPassword }"
                @click="toggleRememberPassword"
              ></div>
              <label class="checkbox-label" @click="toggleRememberPassword">记住密码</label>
            </div>
            <a href="#" class="forgot-password" @click.prevent="handleForgotPassword">忘记密码？</a>
          </div>

          <!-- 登录按钮 -->
          <button type="submit" class="login-button" :class="{ 'loading': isLoading }" :disabled="isLoading">
            <span class="button-text" v-show="!isLoading">登录</span>
            <div class="button-loading" v-show="isLoading"></div>
          </button>
        </form>

        <!-- 其他操作 -->
        <div class="other-actions">
          <span>还没有账户？</span>
          <router-link to="/register" class="register-link">立即注册</router-link>
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { handleException } from '@/utils/exceptionHandler'
import { ElMessage } from 'element-plus'

export default {
  name: 'LoginPage',
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()

    // 响应式数据
    const loginForm = reactive({
      username: '',
      password: ''
    })

    const isLoading = ref(false)
    const showLoadingOverlay = ref(false)
    const rememberPassword = ref(false)

    // 字段验证状态
    const usernameError = ref(false)
    const usernameSuccess = ref(false)
    const passwordError = ref(false)
    const passwordSuccess = ref(false)

    // 方法
    const clearFieldError = (field) => {
      if (field === 'username') {
        usernameError.value = false
        usernameSuccess.value = false
      } else if (field === 'password') {
        passwordError.value = false
        passwordSuccess.value = false
      }
    }

    const toggleRememberPassword = () => {
      const newValue = !rememberPassword.value
      rememberPassword.value = newValue
      
      if (newValue) {
        // 勾选记住密码时，保存当前输入的用户名和密码
        if (loginForm.username.trim()) {
          localStorage.setItem('savedUsername', loginForm.username.trim())
        }
        if (loginForm.password.trim()) {
          const encodedPassword = btoa(loginForm.password.trim())
          localStorage.setItem('savedPassword', encodedPassword)
        }
        localStorage.setItem('rememberPassword', 'true')
      } else {
        // 取消勾选时，清除当前显示的用户名和密码，并清除保存的信息
        loginForm.username = ''
        loginForm.password = ''
        localStorage.removeItem('savedUsername')
        localStorage.removeItem('savedPassword')
        localStorage.setItem('rememberPassword', 'false')
      }
    }

    const validateField = (field) => {
      const value = loginForm[field].trim()
      let isValid = true

      if (field === 'username') {
        if (!value) {
          isValid = false
        } else if (!isValidEmail(value) && value.length < 3) {
          isValid = false
        }
      } else if (field === 'password') {
        if (!value) {
          isValid = false
        } else if (value.length < 6) {
          isValid = false
        }
      }

      if (isValid) {
        if (field === 'username') {
          usernameError.value = false
          usernameSuccess.value = true
        } else if (field === 'password') {
          passwordError.value = false
          passwordSuccess.value = true
        }
      } else {
        if (field === 'username') {
          usernameSuccess.value = false
          usernameError.value = true
        } else if (field === 'password') {
          passwordSuccess.value = false
          passwordError.value = true
        }
      }

      return isValid
    }

    const isValidEmail = (email) => {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      return emailRegex.test(email)
    }

    const handleLogin = async () => {
      // 在提交时进行完整验证
      const usernameValid = validateField('username')
      const passwordValid = validateField('password')
      
      if (!usernameValid || !passwordValid) {
        // 显示验证错误消息
        if (window.$message) {
          window.$message.warning('请检查输入信息是否正确')
        }
        return
      }

      try {
        isLoading.value = true
        showLoadingOverlay.value = true

        // 调用登录API
        const result = await authStore.login(loginForm)
        
        if (result.success) {
          // 显示成功消息
          if (window.$message) {
            window.$message.success('登录成功！正在跳转到主控制台...')
          }
          
          // 保存记住密码状态
          if (rememberPassword.value) {
            localStorage.setItem('savedUsername', loginForm.username)
            // 保存密码（进行简单编码处理）
            const encodedPassword = btoa(loginForm.password)
            localStorage.setItem('savedPassword', encodedPassword)
            localStorage.setItem('rememberPassword', 'true')
          } else {
            localStorage.removeItem('savedUsername')
            localStorage.removeItem('savedPassword')
            localStorage.setItem('rememberPassword', 'false')
          }

          // 简化权限验证流程，直接跳转到控制台
          // 权限验证由路由守卫统一处理
          console.log('登录成功，跳转到控制台')
          router.push('/dashboard')
        } else {
          // 使用统一的错误处理
          ElMessage.error(result.message || '登录失败，请重试')
        }
      } catch (error) {
        // 使用异常处理工具处理错误
        handleException(error, '用户登录')
      } finally {
        isLoading.value = false
        showLoadingOverlay.value = false
      }
    }

    const handleForgotPassword = () => {
      if (window.$message) {
        window.$message.info('忘记密码功能：在实际项目中会跳转到密码重置页面')
      }
    }



    // 生命周期
    onMounted(() => {
      // 检查记住密码状态
      const remembered = localStorage.getItem('rememberPassword') === 'true'
      if (remembered) {
        rememberPassword.value = true
        // 恢复保存的用户名
        const savedUsername = localStorage.getItem('savedUsername')
        if (savedUsername) {
          loginForm.username = savedUsername
        }
        // 恢复保存的密码
        const savedPassword = localStorage.getItem('savedPassword')
        if (savedPassword) {
          try {
            loginForm.password = atob(savedPassword)
          } catch (error) {
            // 如果解码失败，清除保存的密码
            localStorage.removeItem('savedPassword')
            console.warn('保存的密码解码失败，已清除')
          }
        }
      } else {
        // 如果没有记住密码，确保不显示任何保存的信息
        rememberPassword.value = false
        // 不自动填充任何信息，让用户手动输入
      }

      // 自动聚焦到用户名输入框
      const usernameInput = document.getElementById('username')
      if (usernameInput) {
        usernameInput.focus()
      }
    })

    return {
      loginForm,
      isLoading,
      showLoadingOverlay,
      rememberPassword,
      usernameError,
      usernameSuccess,
      passwordError,
      passwordSuccess,
      clearFieldError,
      toggleRememberPassword,
      validateField,
      handleLogin,
      handleForgotPassword
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

.login-page {
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

/* 登录容器 */
.login-container {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08), 0 8px 16px rgba(0, 0, 0, 0.06);
  padding: 50px 60px;
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

/* Logo和标题 */
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

/* 登录标题 */
.login-title {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a1a;
  text-align: center;
  margin-bottom: 35px;
  font-family: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  letter-spacing: -0.3px;
}

/* 登录表单 */
.login-form {
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 24px;
  position: relative;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.form-input {
  width: 100%;
  padding: 12px 20px;
  border: 2px solid #e8e8e8;
  border-radius: 6px;
  font-size: 16px;
  transition: all 0.3s ease;
  background: #fafafa;
}

.form-input:focus {
  outline: none;
  border-color: #91d5ff;
  background: white;
  box-shadow: 0 0 0 2px rgba(145, 213, 255, 0.2);
}

.form-input.error {
  border-color: #ff4d4f;
  background: #fff2f0;
}

.form-input.success {
  border-color: #52c41a;
  background: #f6ffed;
}

/* 记住密码 */
.remember-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 25px;
}



.checkbox-group {
  display: flex;
  align-items: center;
}

.custom-checkbox {
  width: 18px;
  height: 18px;
  border: 2px solid #d9d9d9;
  border-radius: 4px;
  margin-right: 8px;
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
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
}

.forgot-password {
  color: #1890ff;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s ease;
}

.forgot-password:hover {
  color: #40a9ff;
}

/* 登录按钮 */
.login-button {
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

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(24, 144, 255, 0.25);
  background: linear-gradient(135deg, #40a9ff, #1890ff);
}

.login-button:active {
  transform: translateY(0);
  box-shadow: 0 8px 20px rgba(24, 144, 255, 0.2);
}

.login-button:disabled {
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

.login-button.loading .button-text {
  display: none;
}

.login-button.loading .button-loading {
  display: block;
}

/* 其他操作 */
.other-actions {
  text-align: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.register-link {
  color: #1890ff;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.3s ease;
}

.register-link:hover {
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
  
  .login-container {
    padding: 40px 30px;
  }
  
  .login-title {
    font-size: 28px;
    margin-bottom: 30px;
  }
  
  .form-group {
    margin-bottom: 20px;
  }
  
  .form-input {
    padding: 12px 15px;
    font-size: 14px;
  }

  .login-button {
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
  
  .login-container {
    padding: 30px 20px;
  }
  
  .login-title {
    font-size: 24px;
    margin-bottom: 25px;
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