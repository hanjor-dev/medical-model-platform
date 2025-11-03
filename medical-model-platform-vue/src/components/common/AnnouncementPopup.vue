<template>
  <el-dialog
    v-model="visible"
    :title="'系统公告'"
    width="500px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="!announcement?.forceRead"
    class="announcement-dialog"
  >
    <div class="announcement-content">
      <div class="announcement-header">
        <h3 class="announcement-title">{{ announcement?.title }}</h3>
        <div class="announcement-meta">
          <span class="publish-time">{{ formatTime(announcement?.createTime) }}</span>
        </div>
      </div>
      
      <div class="announcement-body">
        <div 
          class="announcement-text" 
          v-html="announcement?.content"
        ></div>
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button 
          v-if="!announcement?.forceRead" 
          @click="handleClose"
        >
          稍后查看
        </el-button>
        <el-button 
          type="primary" 
          @click="handleConfirm"
        >
          {{ announcement?.forceRead ? '我知道了' : '立即查看' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import { ref, watch } from 'vue'

export default {
  name: 'AnnouncementPopup',
  props: {
    modelValue: {
      type: Boolean,
      default: false
    },
    announcement: {
      type: Object,
      default: null
    }
  },
  emits: ['update:modelValue', 'confirm', 'close'],
  setup(props, { emit }) {
    const visible = ref(props.modelValue)

    // 监听 modelValue 变化
    watch(() => props.modelValue, (newVal) => {
      visible.value = newVal
    })

    // 监听 visible 变化
    watch(visible, (newVal) => {
      emit('update:modelValue', newVal)
    })

    // 格式化时间
    const formatTime = (time) => {
      if (!time) return ''
      return new Date(time).toLocaleString()
    }

    // 处理确认（仅关闭并上抛，由外层统一处理跳转与已读）
    const handleConfirm = () => {
      visible.value = false
      emit('confirm', props.announcement)
    }

    // 处理关闭
    const handleClose = () => {
      visible.value = false
      emit('close', props.announcement)
    }

    return {
      visible,
      formatTime,
      handleConfirm,
      handleClose
    }
  }
}
</script>

<style lang="scss" scoped>
.announcement-dialog {
  .announcement-content {
    .announcement-header {
      margin-bottom: 16px;
      
      .announcement-title {
        margin: 0 0 8px 0;
        font-size: 18px;
        font-weight: 600;
        color: #303133;
        line-height: 1.4;
      }
      
      .announcement-meta {
        display: flex;
        align-items: center;
        gap: 12px;
        
        
        
        .publish-time {
          font-size: 12px;
          color: #909399;
        }
      }
    }
    
    .announcement-body {
      .announcement-text {
        font-size: 14px;
        line-height: 1.6;
        color: #606266;
        
        :deep(p) {
          margin: 0 0 12px 0;
          
          &:last-child {
            margin-bottom: 0;
          }
        }
        
        :deep(img) {
          max-width: 100%;
          height: auto;
        }
        
        :deep(a) {
          color: #409eff;
          text-decoration: none;
          
          &:hover {
            text-decoration: underline;
          }
        }
      }
    }
  }
  
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}

:deep(.el-dialog__header) {
  padding: 20px 20px 0 20px;
  
  .el-dialog__title {
    font-size: 16px;
    font-weight: 600;
  }
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog__footer) {
  padding: 0 20px 20px 20px;
}
</style>
