/**
 * 确认弹窗工具函数
 * 提供便捷的确认弹窗调用方法
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15
 */

import { createApp, h } from 'vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'

/**
 * 显示确认弹窗
 * @param {Object} options - 弹窗配置选项
 * @param {string} options.title - 弹窗标题
 * @param {string} options.message - 弹窗消息
 * @param {string} [options.description] - 弹窗描述（可选）
 * @param {string} [options.type='info'] - 弹窗类型：info, warning, danger
 * @param {string} [options.confirmText='确定'] - 确认按钮文本
 * @param {string} [options.cancelText='取消'] - 取消按钮文本
 * @param {boolean} [options.closable=true] - 是否可关闭
 * @param {boolean} [options.maskClosable=false] - 是否允许点击遮罩关闭
 * @returns {Promise<boolean>} 返回Promise，用户确认返回true，取消返回false
 */
export function showConfirmDialog(options = {}) {
  return new Promise((resolve) => {
    // 创建临时容器
    const container = document.createElement('div')
    document.body.appendChild(container)
    
    // 创建Vue应用实例
    const app = createApp({
      render() {
        return h(ConfirmDialog, {
          visible: true,
          title: options.title || '确认',
          message: options.message,
          description: options.description || '',
          type: options.type || 'info',
          confirmText: options.confirmText || '确定',
          cancelText: options.cancelText || '取消',
          closable: options.closable !== false,
          maskClosable: options.maskClosable || false,
          onConfirm: () => {
            cleanup()
            resolve(true)
          },
          onCancel: () => {
            cleanup()
            resolve(false)
          },
          onClose: () => {
            cleanup()
            resolve(false)
          }
        })
      }
    })
    
    // 挂载应用
    app.mount(container)
    
    // 清理函数
    function cleanup() {
      app.unmount()
      document.body.removeChild(container)
    }
  })
}

/**
 * 显示危险操作确认弹窗
 * @param {Object} options - 弹窗配置选项
 * @returns {Promise<boolean>} 返回Promise
 */
export function showDangerConfirm(options) {
  return showConfirmDialog({
    type: 'danger',
    title: '危险操作确认',
    confirmText: '确认删除',
    ...options
  })
}

/**
 * 显示警告确认弹窗
 * @param {Object} options - 弹窗配置选项
 * @returns {Promise<boolean>} 返回Promise
 */
export function showWarningConfirm(options) {
  return showConfirmDialog({
    type: 'warning',
    title: '操作确认',
    ...options
  })
}

/**
 * 显示信息确认弹窗
 * @param {Object} options - 弹窗配置选项
 * @returns {Promise<boolean>} 返回Promise
 */
export function showInfoConfirm(options) {
  return showConfirmDialog({
    type: 'info',
    title: '确认',
    ...options
  })
}

/**
 * 显示退出登录确认弹窗
 * @returns {Promise<boolean>} 返回Promise
 */
export function showLogoutConfirm() {
  return showConfirmDialog({
    type: 'info',
    message: '确定要退出登录吗？',
    description: '退出后需要重新登录才能访问系统',
    confirmText: '退出登录',
    cancelText: '取消'
  })
}

/**
 * 显示删除确认弹窗
 * @param {string} itemName - 要删除的项目名称
 * @returns {Promise<boolean>} 返回Promise
 */
export function showDeleteConfirm(itemName) {
  return showConfirmDialog({
    type: 'danger',
    title: '删除确认',
    message: `确定要删除"${itemName}"吗？`,
    description: '删除后无法恢复，请谨慎操作',
    confirmText: '确认删除',
    cancelText: '取消'
  })
}

/**
 * 显示保存确认弹窗
 * @param {string} message - 确认消息
 * @returns {Promise<boolean>} 返回Promise
 */
export function showSaveConfirm(message = '确定要保存当前更改吗？') {
  return showConfirmDialog({
    type: 'info',
    title: '保存确认',
    message,
    confirmText: '保存',
    cancelText: '取消'
  })
} 