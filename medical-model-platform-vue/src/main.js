/**
 * 应用入口文件
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 20:10:00
 */

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus, { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'
import { setupPermissionDirectives } from './directives/permission'

// 导入全局样式
import './styles/index.scss'

// 全局ResizeObserver错误处理
const originalConsoleError = console.error
console.error = (...args) => {
  // 过滤掉ResizeObserver的错误信息
  if (
    typeof args[0] === 'string' &&
    args[0].includes('ResizeObserver loop completed with undelivered notifications')
  ) {
    return // 忽略这个错误
  }
  originalConsoleError.apply(console, args)
}

// 全局错误处理
window.addEventListener('error', (e) => {
  if (e.message && e.message.includes('ResizeObserver loop completed with undelivered notifications')) {
    e.stopPropagation()
    e.preventDefault()
    return false
  }
})

// 创建Vue应用实例
const app = createApp(App)

// 创建Pinia状态管理
const pinia = createPinia()

// 使用插件
app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 注册权限指令
setupPermissionDirectives(app)

// 挂载应用
app.mount('#app')

// 全局暴露消息组件，兼容现有 window.ElMessage 调用
window.ElMessage = ElMessage
window.ElMessageBox = ElMessageBox
window.ElNotification = ElNotification

// 开发环境下的调试信息
if (process.env.NODE_ENV === 'development') {
  console.log('🚀 医疗影像模型管理平台前端应用已启动')
  console.log('📱 当前环境:', process.env.NODE_ENV)
}
