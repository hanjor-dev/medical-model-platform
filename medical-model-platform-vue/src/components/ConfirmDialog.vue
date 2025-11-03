<template>
  <Teleport to="body">
    <Transition name="confirm-dialog">
      <div v-if="visible" class="confirm-dialog-overlay" @click="handleOverlayClick" @keydown="handleKeydown" tabindex="0">
        <div class="confirm-dialog" @click.stop>
          <!-- 弹窗头部（简化版） -->
          <div class="dialog-header" v-if="closable">
            <button 
              class="dialog-close" 
              @click="handleCancel"
              title="关闭"
            >
              <i class="icon-close">×</i>
            </button>
          </div>

          <!-- 弹窗内容（紧凑版） -->
          <div class="dialog-content">
            <div class="dialog-message">{{ message }}</div>
            <div v-if="description" class="dialog-description">{{ description }}</div>
          </div>

          <!-- 弹窗底部按钮 -->
          <div class="dialog-footer">
            <button 
              class="btn btn-secondary" 
              @click="handleCancel"
              :disabled="loading"
            >
              {{ cancelText }}
            </button>
            <button 
              class="btn btn-primary" 
              :class="primaryButtonClass"
              @click="handleConfirm"
              :disabled="loading"
            >
              <span v-if="loading" class="loading-spinner"></span>
              {{ confirmText }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'ConfirmDialog',
  props: {
    // 弹窗是否可见
    visible: {
      type: Boolean,
      default: false
    },
    // 弹窗类型：info, warning, danger
    type: {
      type: String,
      default: 'info',
      validator: (value) => ['info', 'warning', 'danger'].includes(value)
    },
    // 弹窗标题
    title: {
      type: String,
      default: '确认'
    },
    // 弹窗消息
    message: {
      type: String,
      required: true
    },
    // 弹窗描述（可选）
    description: {
      type: String,
      default: ''
    },
    // 确认按钮文本
    confirmText: {
      type: String,
      default: '确定'
    },
    // 取消按钮文本
    cancelText: {
      type: String,
      default: '取消'
    },
    // 是否可关闭
    closable: {
      type: Boolean,
      default: true
    },
    // 是否显示加载状态
    loading: {
      type: Boolean,
      default: false
    },
    // 是否允许点击遮罩关闭
    maskClosable: {
      type: Boolean,
      default: false
    }
  },
  emits: ['confirm', 'cancel', 'close'],
  setup(props, { emit }) {
    // 计算图标样式类
    const iconClass = computed(() => {
      return `icon-${props.type}`
    })

    // 计算主要按钮样式类
    const primaryButtonClass = computed(() => {
      return {
        'btn-danger': props.type === 'danger',
        'btn-warning': props.type === 'warning',
        'btn-info': props.type === 'info'
      }
    })

    // 处理确认按钮点击
    const handleConfirm = () => {
      if (!props.loading) {
        emit('confirm')
      }
    }

    // 处理取消按钮点击
    const handleCancel = () => {
      if (!props.loading) {
        emit('cancel')
        emit('close')
      }
    }

    // 处理遮罩点击
    const handleOverlayClick = () => {
      if (props.maskClosable && !props.loading) {
        emit('cancel')
        emit('close')
      }
    }

    // 处理键盘事件
    const handleKeydown = (event) => {
      if (event.key === 'Escape') {
        handleCancel()
      } else if (event.key === 'Enter') {
        handleConfirm()
      }
    }

    return {
      iconClass,
      primaryButtonClass,
      handleConfirm,
      handleCancel,
      handleOverlayClick,
      handleKeydown
    }
  }
}
</script>

<style lang="scss" scoped>
.confirm-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(4px);
}

.confirm-dialog {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(24, 144, 255, 0.08);
  border: 1px solid #f0f0f0;
  width: 90%;
  max-width: 420px;
  max-height: 90vh;
  overflow: hidden;
  animation: dialogSlideIn 0.3s ease-out;
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 16px 20px 0;
  position: relative;
  background: linear-gradient(to bottom, rgba(250, 250, 250, 0.8), transparent);
}

.dialog-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 20px;
  font-weight: bold;
  
  &.icon-info {
    background: #e6f7ff;
    color: #1890ff;
  }
  
  &.icon-warning {
    background: #fff7e6;
    color: #fa8c16;
  }
  
  &.icon-danger {
    background: #fff2f0;
    color: #ff4d4f;
  }
}

.dialog-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  flex: 1;
}

.dialog-close {
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #8c8c8c;
  transition: all 0.2s ease;
  
  &:hover {
    background: rgba(24, 144, 255, 0.1);
    color: #1890ff;
    transform: scale(1.05);
  }
  
  &:active {
    transform: scale(0.95);
  }
  
  &:focus {
    outline: 2px solid rgba(24, 144, 255, 0.3);
    outline-offset: 2px;
  }
  
  .icon-close {
    font-size: 16px;
    font-weight: bold;
  }
}

.dialog-content {
  padding: 20px 24px 16px;
}

.dialog-message {
  font-size: 16px;
  color: #262626;
  line-height: 1.5;
  margin-bottom: 6px;
  text-align: center;
}

.dialog-description {
  font-size: 14px;
  color: #8c8c8c;
  line-height: 1.4;
  text-align: center;
}

.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 16px 24px 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  background: linear-gradient(to bottom, #fafafa, #f5f5f5);
}

.btn {
  padding: 8px 20px;
  border: 1px solid transparent;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 80px;
  height: 36px;
  
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
  
  &.btn-primary {
    background: #1890ff;
    color: #ffffff;
    border-color: #1890ff;
    
    &:hover:not(:disabled) {
      background: #40a9ff;
      border-color: #40a9ff;
      box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
      transform: translateY(-1px);
    }
    
    &:active:not(:disabled) {
      transform: translateY(0) scale(0.98);
      box-shadow: 0 1px 4px rgba(24, 144, 255, 0.3);
    }
    
    &:focus {
      outline: 2px solid rgba(24, 144, 255, 0.3);
      outline-offset: 2px;
    }
    
    &.btn-danger {
      background: #ff4d4f;
      border-color: #ff4d4f;
      
      &:hover:not(:disabled) {
        background: #ff7875;
        border-color: #ff7875;
        box-shadow: 0 2px 8px rgba(255, 77, 79, 0.2);
        transform: translateY(-1px);
      }
      
      &:active:not(:disabled) {
        transform: translateY(0) scale(0.98);
        box-shadow: 0 1px 4px rgba(255, 77, 79, 0.3);
      }
    }
    
    &.btn-warning {
      background: #fa8c16;
      border-color: #fa8c16;
      
      &:hover:not(:disabled) {
        background: #ffa940;
        border-color: #ffa940;
        box-shadow: 0 2px 8px rgba(250, 140, 22, 0.2);
        transform: translateY(-1px);
      }
      
      &:active:not(:disabled) {
        transform: translateY(0) scale(0.98);
        box-shadow: 0 1px 4px rgba(250, 140, 22, 0.3);
      }
    }
    
    &.btn-info {
      background: #1890ff;
      border-color: #1890ff;
      
      &:hover:not(:disabled) {
        background: #40a9ff;
        border-color: #40a9ff;
        box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
        transform: translateY(-1px);
      }
      
      &:active:not(:disabled) {
        transform: translateY(0) scale(0.98);
        box-shadow: 0 1px 4px rgba(24, 144, 255, 0.3);
      }
    }
  }
  
  &.btn-secondary {
    background: #ffffff;
    color: #595959;
    border-color: #d9d9d9;
    
    &:hover:not(:disabled) {
      color: #1890ff;
      border-color: #1890ff;
      background: #f0f8ff;
      transform: translateY(-1px);
      box-shadow: 0 2px 8px rgba(24, 144, 255, 0.1);
    }
    
    &:active:not(:disabled) {
      transform: translateY(0) scale(0.98);
      box-shadow: 0 1px 4px rgba(24, 144, 255, 0.2);
    }
    
    &:focus {
      outline: 2px solid rgba(24, 144, 255, 0.3);
      outline-offset: 2px;
    }
  }
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 8px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes dialogSlideIn {
  0% {
    opacity: 0;
    transform: scale(0.8) translateY(-20px);
  }
  70% {
    transform: scale(1.02) translateY(-2px);
  }
  100% {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

// 弹窗进入和离开动画
.confirm-dialog-enter-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.confirm-dialog-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.confirm-dialog-enter-from {
  opacity: 0;
  transform: scale(0.8) translateY(-20px);
}

.confirm-dialog-leave-to {
  opacity: 0;
  transform: scale(0.8) translateY(-20px);
}

// 响应式设计
@media (max-width: 768px) {
  .confirm-dialog {
    width: 95%;
    margin: 20px;
    max-width: none;
  }
  
  .dialog-header {
    padding: 16px 16px 0;
  }
  
  .dialog-content {
    padding: 16px 20px 12px;
  }
  
  .dialog-footer {
    padding: 12px 20px 16px;
    gap: 8px;
    
    .btn {
      flex: 1;
      min-width: 0;
      height: 40px;
      font-size: 15px;
    }
  }
}

@media (max-width: 480px) {
  .confirm-dialog {
    width: 98%;
    margin: 10px;
  }
  
  .dialog-content {
    padding: 14px 16px 10px;
  }
  
  .dialog-footer {
    padding: 10px 16px 14px;
    
    .btn {
      height: 44px;
      font-size: 16px;
    }
  }
}
</style> 