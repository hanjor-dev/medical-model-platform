<template>
  <div class="common-pagination" :class="{ 'align-right': align === 'right' }">
    <div v-if="showInfo" class="pagination-info">
      <span class="total-info">共 {{ total }} 条记录</span>
      <span v-if="selectedCount > 0" class="selected-info">已选择 {{ selectedCount }} 项</span>
    </div>
    
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="pageSizes"
      :total="total"
      :layout="layout"
      :hide-on-single-page="hideOnSinglePage"
      :prev-text="prevText"
      :next-text="nextText"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      class="pagination-main"
    />
  </div>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'CommonPagination',
  props: {
    // 当前页码
    current: {
      type: Number,
      default: 1
    },
    // 是否显示左侧信息（总数/已选）
    showInfo: {
      type: Boolean,
      default: true
    },
    // 对齐方式：between | right
    align: {
      type: String,
      default: 'between'
    },
    // 每页条数
    size: {
      type: Number,
      default: 10
    },
    // 总条数
    total: {
      type: Number,
      default: 0
    },
    // 每页显示条数选择器的选项
    pageSizes: {
      type: Array,
      default: () => [5, 10, 20]
    },
    // 默认每页条数（优先级高于size）
    defaultPageSize: {
      type: Number,
      default: null
    },
    // 已选择的条数（用于批量操作提示）
    selectedCount: {
      type: Number,
      default: 0
    },
    // 组件布局
    layout: {
      type: String,
      default: 'sizes, prev, pager, next'
    },
    // 只有一页时是否隐藏
    hideOnSinglePage: {
      type: Boolean,
      default: false
    },
    // 上一页按钮文字
    prevText: {
      type: String,
      default: '上一页'
    },
    // 下一页按钮文字
    nextText: {
      type: String,
      default: '下一页'
    }
  },
  emits: ['update:current', 'update:size', 'size-change', 'current-change'],
  setup(props, { emit }) {
    // 双向绑定当前页
    const currentPage = computed({
      get: () => props.current,
      set: (value) => emit('update:current', value)
    })

    // 双向绑定每页条数：以父级传入的 size 为唯一来源
    const pageSize = computed({
      get: () => props.size,
      set: (value) => emit('update:size', value)
    })

    // 处理每页条数变化
    const handleSizeChange = (newSize) => {
      emit('size-change', newSize)
    }

    // 处理当前页变化
    const handleCurrentChange = (newPage) => {
      emit('current-change', newPage)
    }

    return {
      currentPage,
      pageSize,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.common-pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-top: 1px solid #e8e8e8;
  background: #ffffff;
  flex-shrink: 0;
}

.common-pagination.align-right {
  justify-content: flex-end;
  gap: 12px;
}

.pagination-info {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #666;
  align-items: center;
}

.total-info {
  color: #666;
}

.selected-info {
  color: #1890ff;
  font-weight: 500;
}

.pagination-main {
  --el-pagination-button-color: #1890ff;
  --el-pagination-hover-color: #40a9ff;
  --el-pagination-button-bg-color: transparent;
  --el-pagination-button-border-radius: 6px;
  --el-pagination-button-border: 1px solid #d9d9d9;
  --el-pagination-font-size: 14px;
  --el-pagination-bg-color: #ffffff;
  --el-pagination-text-color: #666;
}

/* Element Plus 分页组件样式覆盖 */
:deep(.el-pagination) {
  .el-pagination__total {
    color: #666;
    font-weight: normal;
  }

  .el-pagination__sizes {
    .el-select {
      .el-input__inner {
        border: 1px solid #d9d9d9;
        border-radius: 6px;
        color: #666;
      }
    }
  }

  .btn-prev,
  .btn-next {
    background: #ffffff;
    border: 1px solid #d9d9d9;
    color: #666;
    border-radius: 6px;
    font-size: 14px;
    padding: 0 12px;
    min-width: 64px;
    height: 32px;
    
    &:hover:not(.disabled) {
      color: #1890ff;
      border-color: #1890ff;
    }
    
    &.disabled {
      color: #c0c4cc;
      border-color: #e4e7ed;
      cursor: not-allowed;
    }
  }

  .el-pager {
    li {
      background: #ffffff;
      border: 1px solid #d9d9d9;
      color: #666;
      border-radius: 6px;
      margin: 0 4px;
      min-width: 32px;
      height: 32px;
      line-height: 30px;
      
      &:hover {
        color: #1890ff;
        border-color: #1890ff;
      }
      
      &.is-active {
        background: #1890ff;
        border-color: #1890ff;
        color: #ffffff;
      }
      
      &.btn-quicknext,
      &.btn-quickprev {
        border: none;
        background: transparent;
        color: #666;
        
        &:hover {
          color: #1890ff;
        }
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .common-pagination {
    flex-direction: column;
    gap: 8px;
    padding: 10px 0;
  }
  
  .pagination-info {
    order: 2;
    justify-content: center;
    font-size: 13px;
  }
  
  .pagination-main {
    order: 1;
  }
  
  :deep(.el-pagination) {
    .btn-prev,
    .btn-next {
      min-width: 56px;
      padding: 0 8px;
    }
    
    .el-pager li {
      min-width: 28px;
      height: 28px;
      line-height: 26px;
      margin: 0 2px;
    }
  }
}

@media (max-width: 480px) {
  .pagination-info {
    flex-direction: column;
    gap: 4px;
    text-align: center;
  }
  
  :deep(.el-pagination) {
    .el-pagination__sizes {
      display: none;
    }
    
    .el-pagination__total {
      display: none;
    }
  }
}
</style>
