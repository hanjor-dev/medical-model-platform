# ConfirmDialog 确认弹窗组件

## 概述

`ConfirmDialog` 是一个现代化的、可复用的确认弹窗组件，用于替换浏览器默认的 `confirm()` 弹窗。它提供了更好的用户体验和更丰富的自定义选项。

## 特性

- 🎨 现代化的设计风格，与项目整体风格保持一致
- 🔧 高度可定制，支持多种弹窗类型
- 📱 响应式设计，支持移动端
- 🎭 流畅的动画效果
- ♿ 支持键盘操作和可访问性
- 🚀 基于 Promise 的异步操作

## 基本用法

### 1. 直接使用组件

```vue
<template>
  <div>
    <button @click="showDialog = true">显示弹窗</button>
    
    <ConfirmDialog
      v-model:visible="showDialog"
      title="确认操作"
      message="确定要执行此操作吗？"
      type="warning"
      @confirm="handleConfirm"
      @cancel="handleCancel"
    />
  </div>
</template>

<script>
import { ref } from 'vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'

export default {
  components: { ConfirmDialog },
  setup() {
    const showDialog = ref(false)
    
    const handleConfirm = () => {
      console.log('用户确认了操作')
      showDialog.value = false
    }
    
    const handleCancel = () => {
      console.log('用户取消了操作')
      showDialog.value = false
    }
    
    return {
      showDialog,
      handleConfirm,
      handleCancel
    }
  }
}
</script>
```

### 2. 使用工具函数（推荐）

```javascript
import { showConfirmDialog, showLogoutConfirm, showDeleteConfirm } from '@/utils/confirmDialog'

// 基本确认弹窗
const handleBasicConfirm = async () => {
  const confirmed = await showConfirmDialog({
    title: '操作确认',
    message: '确定要执行此操作吗？',
    type: 'info'
  })
  
  if (confirmed) {
    console.log('用户确认了操作')
  } else {
    console.log('用户取消了操作')
  }
}

// 退出登录确认
const handleLogout = async () => {
  const confirmed = await showLogoutConfirm()
  if (confirmed) {
    // 执行退出登录逻辑
    await logout()
  }
}

// 删除确认
const handleDelete = async (itemName) => {
  const confirmed = await showDeleteConfirm(itemName)
  if (confirmed) {
    // 执行删除逻辑
    await deleteItem()
  }
}
```

## 组件属性

| 属性名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `visible` | Boolean | `false` | 弹窗是否可见 |
| `type` | String | `'info'` | 弹窗类型：`info`、`warning`、`danger` |
| `title` | String | `'确认'` | 弹窗标题 |
| `message` | String | - | 弹窗消息（必填） |
| `description` | String | `''` | 弹窗描述（可选） |
| `confirmText` | String | `'确定'` | 确认按钮文本 |
| `cancelText` | String | `'取消'` | 取消按钮文本 |
| `closable` | Boolean | `true` | 是否显示关闭按钮 |
| `loading` | Boolean | `false` | 是否显示加载状态 |
| `maskClosable` | Boolean | `false` | 是否允许点击遮罩关闭 |

## 事件

| 事件名 | 说明 | 回调参数 |
|--------|------|----------|
| `confirm` | 用户点击确认按钮时触发 | - |
| `cancel` | 用户点击取消按钮时触发 | - |
| `close` | 弹窗关闭时触发 | - |

## 弹窗类型

### 1. 信息弹窗 (info)
- 蓝色主题
- 适用于一般性确认操作
- 图标：ℹ

### 2. 警告弹窗 (warning)
- 橙色主题
- 适用于需要用户注意的操作
- 图标：⚠

### 3. 危险弹窗 (danger)
- 红色主题
- 适用于危险或不可逆的操作
- 图标：⚠

## 工具函数

### showConfirmDialog(options)
通用的确认弹窗显示函数。

```javascript
const confirmed = await showConfirmDialog({
  title: '自定义标题',
  message: '自定义消息',
  description: '可选的描述信息',
  type: 'warning',
  confirmText: '确认',
  cancelText: '取消',
  closable: true,
  maskClosable: false
})
```

### showLogoutConfirm()
显示退出登录确认弹窗。

```javascript
const confirmed = await showLogoutConfirm()
if (confirmed) {
  // 执行退出登录
}
```

### showDeleteConfirm(itemName)
显示删除确认弹窗。

```javascript
const confirmed = await showDeleteConfirm('用户张三')
if (confirmed) {
  // 执行删除操作
}
```

### showDangerConfirm(options)
显示危险操作确认弹窗。

```javascript
const confirmed = await showDangerConfirm({
  message: '确定要格式化硬盘吗？',
  description: '此操作将清除所有数据且无法恢复'
})
```

### showWarningConfirm(options)
显示警告确认弹窗。

```javascript
const confirmed = await showWarningConfirm({
  message: '确定要修改系统配置吗？',
  description: '修改后可能需要重启系统'
})
```

### showInfoConfirm(options)
显示信息确认弹窗。

```javascript
const confirmed = await showInfoConfirm({
  message: '确定要保存当前更改吗？'
})
```

## 样式定制

组件使用 SCSS 编写，支持通过 CSS 变量进行主题定制：

```scss
:root {
  --confirm-dialog-primary-color: #1890ff;
  --confirm-dialog-danger-color: #ff4d4f;
  --confirm-dialog-warning-color: #fa8c16;
  --confirm-dialog-border-radius: 12px;
  --confirm-dialog-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}
```

## 最佳实践

1. **使用工具函数**：对于简单的确认操作，推荐使用工具函数而不是直接使用组件
2. **选择合适的类型**：根据操作的危险程度选择合适的弹窗类型
3. **提供清晰的描述**：对于复杂操作，使用 `description` 属性提供更多上下文信息
4. **异步操作**：对于需要等待的操作，使用 `loading` 属性提供视觉反馈
5. **错误处理**：使用 try-catch 包装确认弹窗调用，处理可能的异常

## 注意事项

- 组件使用 `Teleport` 渲染到 `body` 元素，确保正确的层级关系
- 弹窗会自动处理键盘事件（ESC 键关闭）
- 在移动端，弹窗会自动调整为垂直布局
- 组件会自动清理资源，无需手动销毁 