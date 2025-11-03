<template>
  <div class="app-breadcrumb">
    <el-breadcrumb :separator="separator">
      <el-breadcrumb-item v-for="(item, idx) in crumbs" :key="item.path || idx" :to="idx < crumbs.length - 1 && item.path ? { path: item.path } : undefined">
        {{ item.title }}
      </el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<script>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { getBreadcrumb } from '@/config/menu'

export default {
  name: 'AppBreadcrumb',
  props: {
    separator: { type: String, default: '›' },
    /**
     * 可选：覆盖当前路径用于生成面包屑
     */
    path: { type: String, default: '' },
    /**
     * 可选：固定面包屑项，优先级最高，如提供则直接使用
     * 形如：[{ title: '消息与公告', path: '/system/message' }, { title: '平台公告' }]
     */
    items: { type: Array, default: () => [] }
  },
  setup(props) {
    const route = useRoute()

    const crumbs = computed(() => {
      if (props.items && props.items.length > 0) {
        return props.items
          .filter(it => it && (it.title || it.path))
          .map(it => ({ title: it.title || it.path, path: it.path }))
      }
      const curPath = props.path || route.path
      const list = getBreadcrumb(curPath) || []
      return list
        .filter(it => it && (it.title || it.path))
        .map(it => ({ title: it.title || it.path, path: it.path }))
    })

    return { crumbs }
  }
}
</script>

<style scoped>
.app-breadcrumb {
  display: flex;
  align-items: center;
  padding: 0;
  background: transparent;
  border: 0;
  border-radius: 0;
  box-shadow: none;
  line-height: 20px;
  overflow: hidden;
}
.app-breadcrumb :deep(.el-breadcrumb) {
  font-size: 13px;
  white-space: nowrap;
}
.app-breadcrumb :deep(.el-breadcrumb__item) {
  max-width: 220px;
}
.app-breadcrumb :deep(.el-breadcrumb__separator) {
  color: #c0c0c0;
  margin: 0 6px;
}
.app-breadcrumb :deep(.el-breadcrumb__inner) {
  display: inline-block;
  vertical-align: bottom;
  font-weight: 500;
  color: #666666;
  overflow: hidden;
  text-overflow: ellipsis;
}
.app-breadcrumb :deep(.el-breadcrumb__inner.is-link) {
  color: #444444;
}
.app-breadcrumb :deep(.el-breadcrumb__inner.is-link:hover) {
  color: #000000;
}
.app-breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #1a1a1a;
  font-weight: 600;
}
</style>


