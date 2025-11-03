<template>
  <div class="pagination-demo">
    <h3>通用分页组件演示</h3>
    
    <!-- 演示数据表格 -->
    <el-table :data="currentPageData" style="width: 100%; margin-bottom: 20px;">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
    </el-table>
    
    <!-- 使用通用分页组件 -->
    <CommonPagination
      v-model:current="pagination.current"
      v-model:size="pagination.size"
      :total="pagination.total"
      :selected-count="selectedCount"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script>
import { ref, reactive, computed } from 'vue'
import CommonPagination from './Pagination.vue'

export default {
  name: 'PaginationDemo',
  components: {
    CommonPagination
  },
  setup() {
    const selectedCount = ref(0)
    
    // 分页数据
    const pagination = reactive({
      current: 1,
      size: 10,
      total: 0
    })
    
    // 模拟数据
    const mockData = []
    for (let i = 1; i <= 85; i++) {
      mockData.push({
        id: i,
        name: `示例数据 ${i}`,
        status: Math.random() > 0.5 ? 1 : 0,
        createTime: new Date(Date.now() - Math.random() * 10000000000).toLocaleDateString()
      })
    }
    
    pagination.total = mockData.length
    
    // 当前页数据
    const currentPageData = computed(() => {
      const start = (pagination.current - 1) * pagination.size
      const end = start + pagination.size
      return mockData.slice(start, end)
    })
    
    // 处理分页变化
    const handleSizeChange = (newSize) => {
      pagination.size = newSize
      pagination.current = 1
    }
    
    const handleCurrentChange = (newPage) => {
      pagination.current = newPage
    }
    
    return {
      pagination,
      selectedCount,
      currentPageData,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.pagination-demo {
  padding: 20px;
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

h3 {
  margin: 0 0 20px 0;
  color: #1890ff;
  font-size: 16px;
  font-weight: 600;
}
</style>
