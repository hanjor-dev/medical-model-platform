<template>
  <div class="message-toast-container">
    <!-- 消息提示列表 -->
    <TransitionGroup name="message" tag="div" class="message-list">
      <div
        v-for="message in messages"
        :key="message.id"
        :class="['message-item', `message-${message.type}`]"
        @click="removeMessage(message.id)"
      >
        <div class="message-icon">
          <i v-if="message.type === 'success'" class="icon-success">✓</i>
          <i v-else-if="message.type === 'error'" class="icon-error">✕</i>
          <i v-else-if="message.type === 'warning'" class="icon-warning">⚠</i>
          <i v-else-if="message.type === 'info'" class="icon-info">ℹ</i>
        </div>
        <div class="message-content">
          <div class="message-title" v-if="message.title">{{ message.title }}</div>
          <div class="message-text">{{ message.text }}</div>
        </div>
        <div class="message-close" @click.stop="removeMessage(message.id)">
          <i class="icon-close">×</i>
        </div>
        <div class="message-progress" v-if="message.duration > 0"></div>
      </div>
    </TransitionGroup>
  </div>
</template>

<script>
import { ref } from 'vue'

export default {
  name: 'MessageToast',
  setup() {
    const messages = ref([])
    let messageId = 0

    // 添加消息
    const addMessage = (options) => {
      const {
        type = 'info',
        title = '',
        text = '',
        duration = 3000,
        closable = true
      } = options

      const message = {
        id: ++messageId,
        type,
        title,
        text,
        duration,
        closable,
        timestamp: Date.now()
      }

      messages.value.push(message)

      // 自动移除消息
      if (duration > 0) {
        setTimeout(() => {
          removeMessage(message.id)
        }, duration)
      }

      return message.id
    }

    // 移除消息
    const removeMessage = (id) => {
      const index = messages.value.findIndex(msg => msg.id === id)
      if (index > -1) {
        messages.value.splice(index, 1)
      }
    }

    // 清空所有消息
    const clearAll = () => {
      messages.value = []
    }

    // 成功消息
    const success = (text, title = '', duration = 3000) => {
      return addMessage({ type: 'success', text, title, duration })
    }

    // 错误消息
    const error = (text, title = '', duration = 5000) => {
      return addMessage({ type: 'error', text, title, duration })
    }

    // 警告消息
    const warning = (text, title = '', duration = 4000) => {
      return addMessage({ type: 'warning', text, title, duration })
    }

    // 信息消息
    const info = (text, title = '', duration = 3000) => {
      return addMessage({ type: 'info', text, title, duration })
    }

    // 加载消息
    const loading = (text, title = '') => {
      return addMessage({ type: 'info', text, title, duration: 0 })
    }

    // 暴露方法给父组件
    const messageApi = {
      addMessage,
      removeMessage,
      clearAll,
      success,
      error,
      warning,
      info,
      loading
    }

    // 将API挂载到全局
    if (typeof window !== 'undefined') {
      window.$message = messageApi
    }

    return {
      messages,
      removeMessage,
      messageApi
    }
  }
}
</script>

<style scoped>
.message-toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  pointer-events: none;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 300px;
  max-width: 450px;
  pointer-events: auto;
  position: relative;
  overflow: hidden;
  animation: slideInRight 0.3s ease-out;
}

.message-success {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  color: #52c41a;
}

.message-error {
  background: #fff2f0;
  border: 1px solid #ffccc7;
  color: #ff4d4f;
}

.message-warning {
  background: #fffbe6;
  border: 1px solid #ffe58f;
  color: #faad14;
}

.message-info {
  background: #f0f9ff;
  border: 1px solid #91d5ff;
  color: #1890ff;
}

.message-icon {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
}

.icon-success {
  color: #52c41a;
}

.icon-error {
  color: #ff4d4f;
}

.icon-warning {
  color: #faad14;
}

.icon-info {
  color: #1890ff;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-title {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 4px;
  line-height: 1.4;
}

.message-text {
  font-size: 13px;
  line-height: 1.5;
  word-break: break-word;
}

.message-close {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0.6;
  transition: opacity 0.2s ease;
}

.message-close:hover {
  opacity: 1;
}

.icon-close {
  font-size: 18px;
  font-weight: bold;
}

.message-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 2px;
  background: currentColor;
  opacity: 0.3;
  animation: progress 3s linear;
}

/* 动画效果 */
@keyframes slideInRight {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes slideOutRight {
  from {
    transform: translateX(0);
    opacity: 1;
  }
  to {
    transform: translateX(100%);
    opacity: 0;
  }
}

@keyframes progress {
  from {
    width: 100%;
  }
  to {
    width: 0%;
  }
}

/* 消息进入和离开动画 */
.message-enter-active {
  transition: all 0.3s ease-out;
}

.message-leave-active {
  transition: all 0.3s ease-in;
}

.message-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.message-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message-toast-container {
    top: 10px;
    right: 10px;
    left: 10px;
  }
  
  .message-item {
    min-width: auto;
    max-width: none;
  }
}
</style> 