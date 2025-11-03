<template>
  <div class="user-log-page">
      <!-- 日志列表区域 -->
      <div class="log-list-section">
        <el-card class="list-card" shadow="never">
          <template #header>
            <div class="list-header">
              <AppBreadcrumb />
            </div>
          </template>
  
          <!-- 工具栏（筛选表单） -->
          <div class="filter-content">
            <el-form :model="queryForm" class="filter-form">
              <!-- 第一行：非网格，弹性换行布局，顶部标签 -->
              <div class="filter-inline">
                <el-form-item label="用户：">
                  <el-input
                    v-model="queryForm.username"
                    placeholder="账号/昵称/邮箱"
                    clearable
                    @keyup.enter="handleSearch"
                  />
                </el-form-item>
                <el-form-item label="状态：">
                  <el-select v-model="queryForm.status" placeholder="选择状态" clearable>
                    <el-option label="成功" :value="1" />
                    <el-option label="失败" :value="0" />
                  </el-select>
                </el-form-item>
                <el-form-item label="操作模块：">
                  <el-select 
                    v-model="queryForm.operationModule" 
                    placeholder="选择操作模块" 
                    clearable
                    @change="handleModuleChange"
                  >
                    <el-option 
                      v-for="module in moduleOptions" 
                      :key="module.value" 
                      :label="module.label" 
                      :value="module.value" 
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="关键词搜索：">
                  <el-input
                    v-model="queryForm.keyword"
                    placeholder="搜索日志内容关键词"
                    clearable
                    @keyup.enter="handleSearch"
                  />
                </el-form-item>
              </div>

              <!-- 第二行：非网格，左右分布 -->
              <div class="filter-inline second">
                <el-form-item label="时间：">
                  <el-date-picker
                    v-model="timeRange"
                    type="datetimerange"
                    :shortcuts="dateShortcuts"
                    range-separator="至"
                    start-placeholder="开始时间"
                    end-placeholder="结束时间"
                    format="YYYY-MM-DD HH:mm:ss"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    @change="handleTimeRangeChange"
                    clearable
                    class="w-260"
                  />
                </el-form-item>
                <div class="filter-actions">
                  <el-button type="primary" @click="handleSearch" :loading="loading" class="with-icon-gap">
                    <el-icon><Search /></el-icon>
                    <span class="btn-text">搜索</span>
                  </el-button>
                  <el-button @click="handleReset" class="with-icon-gap">
                    <el-icon><Refresh /></el-icon>
                    <span class="btn-text">重置</span>
                  </el-button>
                  <el-dropdown trigger="click" @command="handleMoreCommand">
                    <el-button class="with-icon-gap">
                      <span class="btn-text">更多</span>
                      <el-icon><CaretBottom /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="export" :disabled="exportLoading" class="success-item">
                          <el-icon><Download /></el-icon>
                          导出
                        </el-dropdown-item>
                        <el-dropdown-item command="clean" divided class="danger-item">
                          <el-icon><Delete /></el-icon>
                          清理
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>
            </el-form>
          </div>

          <!-- 日志表格 -->
          <el-table 
            :data="logList" 
            v-loading="loading"
            stripe
            border
            style="width: 100%"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            
            <el-table-column prop="operationTime" label="时间" width="180" sortable>
              <template #default="{ row }">
                <div class="log-time">
                  <el-icon><Clock /></el-icon>
                  {{ formatDateTime(row.operationTime) }}
                </div>
              </template>
            </el-table-column>
  
            
  
            <el-table-column prop="username" label="用户" width="220">
              <template #default="{ row }">
                <div class="user-info">
                  <el-avatar :size="24" class="user-avatar">
                    {{ row.username ? row.username.charAt(0).toUpperCase() : 'U' }}
                  </el-avatar>
                  <div class="user-names">
                    <span class="username">{{ row.username || '未知' }}</span>
                    <span class="nickname" v-if="row.userNickname || row.nickname">（{{ row.userNickname || row.nickname }}）</span>
                  </div>
                </div>
              </template>
            </el-table-column>
  
            <el-table-column prop="operationModule" label="模块" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.operationModule" type="info" size="small">
                  {{ getModuleLabel(row.operationModule) }}
                </el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>

            <el-table-column prop="operationType" label="类型" width="150">
              <template #default="{ row }">
                <el-tag size="small">{{ row.operationType || '-' }}</el-tag>
              </template>
            </el-table-column>
  
            
  
            <el-table-column prop="operationDesc" label="日志描述" min-width="150">
              <template #default="{ row }">
                <div class="log-description">
                  {{ row.operationDesc || row.description || row.loginMessage || '-' }}
                </div>
              </template>
            </el-table-column>
  
            <el-table-column prop="operationStatus" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="getStatusValue(row) === 1 ? 'success' : 'danger'" size="small">
                  {{ getStatusValue(row) === 1 ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
  
            
  
            <el-table-column prop="location" label="ip 地址" width="180">
              <template #default="{ row }">
                <div class="ip-info">
                  <span>{{ formatLocationDisplay(row) }}</span>
                </div>
              </template>
            </el-table-column>
  
            
  
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button 
                  type="primary" 
                  size="small" 
                  text
                  @click="showLogDetail(row)"
                >
                  详情
                </el-button>
              </template>
            </el-table-column>
            <template #empty>
              <EmptyBox size="table" title="暂无日志数据" desc="可修改时间范围、关键词或模块筛选后重试" />
            </template>
          </el-table>
  
          <!-- 分页 -->
          <CommonPagination
            v-model:current="pagination.pageNum"
            v-model:size="pagination.pageSize"
            :total="pagination.total"
            :defaultPageSize="10"
            @size-change="handlePageSizeChange"
            @current-change="handleCurrentPageChange"
          />
        </el-card>
      </div>
  
      <!-- 日志详情弹窗 -->
      <el-dialog
        v-model="detailDialogVisible"
        :teleported="false"
        :title="`日志详情`"
        width="800px"
        destroy-on-close
      >
        <div class="log-detail" v-if="currentLog">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
            
            <el-descriptions-item label="用户ID">{{ currentLog.userId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ currentLog.username || '-' }}</el-descriptions-item>
            <el-descriptions-item label="操作时间">{{ formatDateTime(currentLog.operationTime) }}</el-descriptions-item>
            <el-descriptions-item label="操作状态">
              <el-tag :type="getStatusValue(currentLog) === 1 ? 'success' : 'danger'">
                {{ getStatusValue(currentLog) === 1 ? '成功' : '失败' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="IP地址">{{ currentLog.ipAddress || '-' }}</el-descriptions-item>
            <el-descriptions-item label="地理位置">{{ currentLog.location || '-' }}</el-descriptions-item>
            
            <!-- 操作日志字段 -->
              <el-descriptions-item label="操作模块">{{ getModuleLabel(currentLog.operationModule) || '-' }}</el-descriptions-item>
              <el-descriptions-item label="操作类型">{{ currentLog.operationType || '-' }}</el-descriptions-item>
              <el-descriptions-item label="请求方法">{{ currentLog.requestMethod || '-' }}</el-descriptions-item>
              <el-descriptions-item label="请求URL">{{ currentLog.requestUrl || '-' }}</el-descriptions-item>
              <el-descriptions-item label="耗时">{{ currentLog.costTime ? currentLog.costTime + 'ms' : '-' }}</el-descriptions-item>
            
          </el-descriptions>
  
          <!-- 详细信息 -->
          <div class="detail-sections">
            <el-card v-if="currentLog.description" class="detail-section">
              <template #header>操作描述</template>
              <div class="detail-content">{{ currentLog.description }}</div>
            </el-card>
  
            <el-card v-if="currentLog.requestParams" class="detail-section">
              <template #header>请求参数</template>
              <div class="detail-content code-content">
                <pre>{{ formatJson(currentLog.requestParams) }}</pre>
              </div>
            </el-card>
  
            <el-card v-if="currentLog.responseResult" class="detail-section">
              <template #header>响应结果</template>
              <div class="detail-content code-content">
                <pre>{{ formatJson(currentLog.responseResult) }}</pre>
              </div>
            </el-card>
  
            <el-card v-if="currentLog.errorMessage" class="detail-section error">
              <template #header>错误信息</template>
              <div class="detail-content error-content">{{ currentLog.errorMessage }}</div>
            </el-card>
          </div>
        </div>
      </el-dialog>
  
      <!-- 清理日志确认弹窗 -->
      <el-dialog
        v-model="cleanDialogVisible"
        :teleported="false"
        title="清理过期日志"
        width="500px"
      >
        <div class="clean-dialog-content">
          <el-alert
            title="警告"
            description="此操作将永久删除过期的日志数据，无法恢复！"
            type="warning"
            :closable="false"
            style="margin-bottom: 20px;"
          />
          
          <el-form :model="cleanForm" label-width="100px">
            <el-form-item label="保留天数：">
              <el-input-number
                v-model="cleanForm.retentionDays"
                :min="1"
                :max="365"
                placeholder="输入保留天数"
              />
              <div class="form-tip">删除 {{ cleanForm.retentionDays }} 天前的日志数据</div>
            </el-form-item>
          </el-form>
        </div>
        
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="cleanDialogVisible = false">取消</el-button>
            <el-button type="danger" @click="handleCleanLogs" :loading="cleanLoading">
              确认清理
            </el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </template>
  
  <script>
  import { defineComponent, reactive, ref, onMounted } from 'vue'
  import { ElMessage } from 'element-plus'
  import { 
    Search, Refresh, Download,
    Clock, Delete, CaretBottom
  } from '@element-plus/icons-vue'
  import { userApi } from '@/api/user'
import CommonPagination from '@/components/common/Pagination.vue'
import EmptyBox from '@/components/common/EmptyBox.vue'
  import AppBreadcrumb from '@/components/common/Breadcrumb.vue'
  
  export default defineComponent({
    name: 'UserLog',
    components: {
      Search, Refresh, Download,
      Clock, Delete, CaretBottom,
      CommonPagination,
      AppBreadcrumb,
      EmptyBox
    },
    setup() {
      // 响应式数据
      const loading = ref(false)
      const exportLoading = ref(false)
      const cleanLoading = ref(false)
      
      // 查询表单
      const queryForm = reactive({
        quickTimeRange: '',
        startTime: '',
        endTime: '',
        username: '',
        status: null,
        
        operationModule: '',
        
        keyword: ''
      })
  
      // 时间范围
      const timeRange = ref([])
      const dateShortcuts = [
        {
          text: '今天',
          value: () => {
            const start = new Date();
            start.setHours(0, 0, 0, 0)
            const end = new Date();
            return [start, end]
          }
        },
        {
          text: '昨天',
          value: () => {
            const start = new Date();
            start.setDate(start.getDate() - 1)
            start.setHours(0, 0, 0, 0)
            const end = new Date();
            end.setDate(end.getDate() - 1)
            end.setHours(23, 59, 59, 999)
            return [start, end]
          }
        },
        {
          text: '近7天',
          value: () => {
            const end = new Date();
            const start = new Date();
            start.setDate(end.getDate() - 7)
            return [start, end]
          }
        },
        {
          text: '近30天',
          value: () => {
            const end = new Date();
            const start = new Date();
            start.setDate(end.getDate() - 30)
            return [start, end]
          }
        }
      ]
  
      // 分页数据
      const pagination = reactive({
        pageNum: 1,
        pageSize: 10,
        total: 0
      })
  
      // 日志列表
      const logList = ref([])
      const selectedLogs = ref([])
      const currentLog = ref(null)
      const detailDialogVisible = ref(false)
      const cleanDialogVisible = ref(false)
 
      // 选项数据
      const moduleOptions = ref([])
      const moduleLabelMap = ref({})
      
      const cleanForm = reactive({
        retentionDays: 30
      })
  
      
  
      // 初始化数据
      onMounted(() => {
        // 默认近7天
        const end = new Date()
        const start = new Date()
        start.setDate(end.getDate() - 7)
        timeRange.value = [
          `${start.getFullYear()}-${String(start.getMonth()+1).padStart(2,'0')}-${String(start.getDate()).padStart(2,'0')} 00:00:00`,
          `${end.getFullYear()}-${String(end.getMonth()+1).padStart(2,'0')}-${String(end.getDate()).padStart(2,'0')} 23:59:59`
        ]
        queryForm.startTime = timeRange.value[0]
        queryForm.endTime = timeRange.value[1]
        loadLogList()
        loadModuleOptions()
        
      })
  
      // 监听模块变化，加载对应的类型选项
      
  
      // 加载日志列表
      const loadLogList = async () => {
        try {
          loading.value = true
          const params = {
            ...queryForm,
            pageNum: pagination.pageNum,
            pageSize: pagination.pageSize
          }
          
          // 清理空值参数
          Object.keys(params).forEach(key => {
            if (params[key] === '' || params[key] === null || params[key] === undefined) {
              delete params[key]
            }
          })
  
          const response = await userApi.getUserLogs(params)
          
          if (response.success) {
            const data = response.data || {}
            logList.value = data.records || []
            pagination.total = data.total || 0
            pagination.pageNum = data.current || 1
            pagination.pageSize = data.size || 20
          } else {
            ElMessage.error(response.message || '加载日志列表失败')
            logList.value = []
            pagination.total = 0
          }
        } catch (error) {
          console.error('加载日志列表失败:', error)
          ElMessage.error('加载日志列表失败')
          logList.value = []
          pagination.total = 0
        } finally {
          loading.value = false
        }
      }
  
      // 加载模块选项
      const loadModuleOptions = async () => {
        try {
          const response = await userApi.getLogModules()
          if (response.success && response.data) {
            const raw = Array.isArray(response.data) ? response.data : []
            moduleOptions.value = raw.map(it => {
              if (typeof it === 'string') {
                return { label: it, value: it }
              }
              return it
            })
            // 构建 英文->中文 的映射
            moduleLabelMap.value = moduleOptions.value.reduce((acc, cur) => {
              acc[cur.value] = cur.label
              return acc
            }, {})
          }
        } catch (error) {
          console.error('加载模块选项失败:', error)
        }
      }
  
      // 加载类型选项
      
  
      
  
      // 处理时间范围变化
      const handleTimeRangeChange = (value) => {
        if (value && value.length === 2) {
          queryForm.startTime = value[0]
          queryForm.endTime = value[1]
          queryForm.quickTimeRange = '' // 清除快速时间选择
        } else {
          queryForm.startTime = ''
          queryForm.endTime = ''
        }
      }
  
      // 处理模块变化
      const handleModuleChange = () => {}

      const getModuleLabel = (val) => {
        if (!val) return ''
        return moduleLabelMap.value[val] || val
      }
  
      // 搜索
      const handleSearch = () => {
        pagination.pageNum = 1
        loadLogList()
        
      }
  
      // 重置
      const handleReset = () => {
        Object.assign(queryForm, {
          startTime: '',
          endTime: '',
          username: '',
          status: null,
          
          operationModule: '',
          
          keyword: ''
        })
        const end = new Date()
        const start = new Date()
        start.setDate(end.getDate() - 7)
        timeRange.value = [
          `${start.getFullYear()}-${String(start.getMonth()+1).padStart(2,'0')}-${String(start.getDate()).padStart(2,'0')} 00:00:00`,
          `${end.getFullYear()}-${String(end.getMonth()+1).padStart(2,'0')}-${String(end.getDate()).padStart(2,'0')} 23:59:59`
        ]
        queryForm.startTime = timeRange.value[0]
        queryForm.endTime = timeRange.value[1]
        
        pagination.pageNum = 1
        loadLogList()
        
      }
  
      // 刷新
      const handleRefresh = () => { loadLogList() }
  
      
  
      // 导出日志
      const handleExport = async () => {
        try {
          exportLoading.value = true
          
          const params = { ...queryForm }
          // 清理空值参数
          Object.keys(params).forEach(key => {
            if (params[key] === '' || params[key] === null || params[key] === undefined) {
              delete params[key]
            }
          })
  
          const blobData = await userApi.exportLogs(params)
          
          // 创建下载链接（CSV）
          const blob = new Blob([blobData], { type: 'text/csv;charset=utf-8;' })
          const url = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = url
          link.download = `用户日志_${new Date().toISOString().slice(0, 10)}.csv`
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          window.URL.revokeObjectURL(url)
          
          ElMessage.success('导出成功')
        } catch (error) {
          console.error('导出失败:', error)
          ElMessage.error('导出失败')
        } finally {
          exportLoading.value = false
        }
      }

      // 更多下拉命令
      const handleMoreCommand = (cmd) => {
        if (cmd === 'export') {
          ElMessage.info('功能暂未开放')
          return
        }
        if (cmd === 'clean') return showCleanDialog()
      }
  
      // 显示清理对话框
      const showCleanDialog = () => {
        cleanDialogVisible.value = true
      }
  
      // 清理过期日志
      const handleCleanLogs = async () => {
        try {
          cleanLoading.value = true
          
          const response = await userApi.cleanExpiredLogs({
            retentionDays: cleanForm.retentionDays
          })
          
          if (response.success) {
            const deletedCount = typeof response.data === 'number'
              ? response.data
              : (response?.data?.deletedCount ?? 0)
            ElMessage.success(response.message || `成功清理了 ${deletedCount} 条过期日志`)
            cleanDialogVisible.value = false
            loadLogList()
            
          } else {
            ElMessage.error(response.message || '清理失败')
          }
        } catch (error) {
          console.error('清理失败:', error)
          ElMessage.error('清理失败')
        } finally {
          cleanLoading.value = false
        }
      }
  
      // 分页相关
      const handlePageSizeChange = (size) => {
        pagination.pageSize = size
        pagination.pageNum = 1
        loadLogList()
      }
  
      const handleCurrentPageChange = (page) => {
        pagination.pageNum = page
        loadLogList()
      }
  
      // 表格选择
      const handleSelectionChange = (selection) => {
        selectedLogs.value = selection
      }
  
      // 显示日志详情
      const showLogDetail = async (row) => {
        try {
          const response = await userApi.getLogDetail(row.id)
          if (response.success) {
            currentLog.value = response.data
            detailDialogVisible.value = true
          } else {
            ElMessage.error(response.message || '获取日志详情失败')
          }
        } catch (error) {
          console.error('获取日志详情失败:', error)
          ElMessage.error('获取日志详情失败')
        }
      }
  
      // 工具函数
      const formatDateTime = (dateTime) => {
        if (!dateTime) return '-'
        const normalized = typeof dateTime === 'string' && dateTime.includes(' ')
          ? dateTime.replace(' ', 'T')
          : dateTime
        const date = new Date(normalized)
        if (isNaN(date.getTime())) return dateTime
        return date.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        })
      }
  
      const getStatusValue = (row) => {
        return row?.status ?? row?.operationStatus ?? null
      }
  
      const getOperationTypeLabel = (type) => {
        if (!type) return '-'
        const map = {
          CREATE: '创建',
          UPDATE: '更新',
          DELETE: '删除',
          ENABLE: '启用',
          DISABLE: '禁用',
          EXPORT: '导出',
          IMPORT: '导入',
          CLEAN: '清理',
          BATCH_DELETE: '批量删除',
          BATCH_UPDATE: '批量更新',
          SORT_UPDATE: '排序更新',
          ALLOCATE: '分配'
        }
        return map[type] || type
      }

      // 地点显示：ip（地点）
      const formatLocationDisplay = (row) => {
        const ip = row?.ipAddress || row?.ip || ''
        const location = row?.location || ''
        if (ip && location) return `${ip}（${location}）`
        if (ip) return ip
        if (location) return location
        return '-'
      }

      
  
      const formatJson = (jsonString) => {
        try {
          if (typeof jsonString === 'string') {
            const parsed = JSON.parse(jsonString)
            return JSON.stringify(parsed, null, 2)
          }
          return JSON.stringify(jsonString, null, 2)
        } catch (error) {
          return jsonString
        }
      }
  
      return {
        // 响应式数据
        loading,
        exportLoading,
        cleanLoading,
        detailDialogVisible,
        cleanDialogVisible,
        queryForm,
        timeRange,
        pagination,
        logList,
        selectedLogs,
        currentLog,
        moduleOptions,
        
        cleanForm,
        
  
        // 方法
        handleTimeRangeChange,
        handleModuleChange,
        handleSearch,
        handleReset,
        handleRefresh,
        
        handleExport,
        handleMoreCommand,
        showCleanDialog,
        handleCleanLogs,
        handlePageSizeChange,
        handleCurrentPageChange,
        handleSelectionChange,
        showLogDetail,
  
        // 工具函数
        formatDateTime,
        getStatusValue,
        getOperationTypeLabel,
        formatLocationDisplay,
        dateShortcuts,
        
        formatJson,
        getModuleLabel
      }
    }
  })
  </script>
  
  <style lang="scss" scoped>
  .user-log-page {
    padding: 8px 0 12px;
    background: transparent;
    display: flex;
    flex-direction: column;
    min-height: calc(100vh - var(--app-header-height) - var(--main-padding-v));
    
    .page-header { margin-bottom: 12px; }
    
    // 筛选区域
    .filter-section {
      margin-bottom: 20px;
      
      .filter-card {
        .filter-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .filter-title {
            display: flex;
            align-items: center;
            gap: 8px;
            font-weight: 600;
            color: #303133;
          }
          
          
        }
        
        .filter-content {
          padding-top: 10px;
          
          // 缩小筛选控件宽度（减少约40%）
          :deep(.el-input),
          :deep(.el-select),
          :deep(.el-date-editor) {
            width: 100%;
          }
          
          .filter-row {
            margin-bottom: 20px;
            
            &:last-child {
              margin-bottom: 0;
            }
          }
          
          .filter-item {
            display: flex;
            align-items: center;
            gap: 8px;
            
            .filter-label {
              min-width: 110px;
              font-size: 14px;
              color: #606266;
              text-align: right;
              white-space: nowrap;
            }
            
            // 默认不撑满，使用实用类控制宽度
            .el-select,
            .el-input,
            .el-date-editor {
              flex: 0 0 auto;
            }
            
            // 宽度工具类
            .w-160 { width: 160px; }
            .w-180 { width: 180px; }
            .w-200 { width: 200px; }
            .w-220 { width: 220px; }
            .w-240 { width: 240px; }
            .w-260 { width: 260px; }
            .w-300 { width: 300px; }
            .w-340 { width: 340px; }
            .w-380 { width: 380px; }
          }
          
          .filter-actions {
            display: flex;
            justify-content: flex-end;
            gap: 10px;
            
            
            .el-button {
              margin: 0;
            }
            
            .with-icon-gap {
              display: inline-flex;
              align-items: center;
              gap: 6px;
            }
            .with-icon-gap .btn-text { margin-left: 2px; }
            &.right { text-align: right; }

            // 下拉菜单项颜色
            :deep(.el-dropdown-menu__item.success-item) { color: #67c23a; }
            :deep(.el-dropdown-menu__item.danger-item) { color: #f56c6c; }
          }
        }
      }
    }
  
    // 统计区域
    .statistics-section {
      margin-bottom: 20px;
      
      .statistics-card {
        .statistics-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .statistics-title {
            display: flex;
            align-items: center;
            gap: 8px;
            font-weight: 600;
            color: #303133;
          }
          
          .close-btn {
            color: #909399;
            
            &:hover {
              color: #606266;
            }
          }
        }
        
        .statistics-content {
          .stat-item {
            display: flex;
            align-items: center;
            padding: 20px;
            border-radius: 8px;
            background: #fff;
            border: 1px solid #e4e7ed;
            transition: all 0.3s;
            
            &:hover {
              box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
            }
            
            .stat-icon {
              width: 48px;
              height: 48px;
              display: flex;
              align-items: center;
              justify-content: center;
              border-radius: 50%;
              margin-right: 16px;
              
              .el-icon {
                font-size: 24px;
              }
            }
            
            .stat-content {
              flex: 1;
              
              .stat-value {
                font-size: 24px;
                font-weight: 600;
                line-height: 1;
                margin-bottom: 4px;
              }
              
              .stat-label {
                font-size: 14px;
                color: #909399;
              }
            }
            
            &.success {
              .stat-icon {
                background: #f0f9ff;
                color: #67c23a;
              }
              .stat-value {
                color: #67c23a;
              }
            }
            
            &.primary {
              .stat-icon {
                background: #ecf5ff;
                color: #409eff;
              }
              .stat-value {
                color: #409eff;
              }
            }
            
            &.danger {
              .stat-icon {
                background: #fef0f0;
                color: #f56c6c;
              }
              .stat-value {
                color: #f56c6c;
              }
            }
            
            &.warning {
              .stat-icon {
                background: #fdf6ec;
                color: #e6a23c;
              }
              .stat-value {
                color: #e6a23c;
              }
            }
            
            &.info {
              .stat-icon {
                background: #f4f4f5;
                color: #909399;
              }
              .stat-value {
                color: #909399;
              }
            }
          }
        }
      }
    }
  
    // 日志列表区域
    .log-list-section {
      display: flex;
      flex: 1;
      min-height: 0;
      .list-card {
        box-shadow: none;
        flex: 1 1 auto;
        width: 100%;
        min-height: 100%;
        display: flex;
        flex-direction: column;
        :deep(.el-card__body) { flex: 1; display: flex; flex-direction: column; min-height: 0; }
        // 工具栏（筛选表单）样式，匹配页面上的筛选DOM
        .filter-content {
          padding-top: 10px;
          .filter-row { display: none; }

          // 非网格模式：行内弹性布局
          .filter-inline {
            display: flex;
            flex-wrap: wrap;
            align-items: flex-end;
            gap: 16px 16px;
            margin-bottom: 16px;
          }
          .filter-inline.second {
            align-items: center;
          }
          .filter-inline .el-form-item {
            margin-bottom: 0;
          }
          .filter-inline .el-form-item__label {
            width: auto !important;
            min-width: 0;
            padding-right: 2px;
            white-space: nowrap;
          }
          .filter-inline .el-input,
          .filter-inline .el-select {
            width: 240px;
          }
          .filter-inline .w-260 { width: 260px !important; }
          .filter-inline .w-220 { width: 220px !important; }
          .filter-inline .w-200 { width: 200px !important; }
          .filter-inline .spacer { display: none; }

          // 让时间范围选择器的自定义宽度类生效（解决 scoped 样式不命中、以及组件默认最小宽度）
          :deep(.el-date-editor.w-220),
          :deep(.el-range-editor.w-220),
          :deep(.w-220.el-date-editor),
          :deep(.w-220.el-range-editor),
          :deep(.w-220.el-date-editor.el-range-editor),
          :deep(.w-220) {
            width: 260px !important;
            max-width: 400px !important;
            min-width: 0 !important;
          }

          .filter-actions {
            display: flex;
            justify-content: flex-start;
            gap: 10px;
            .el-button { margin: 0; }
            .with-icon-gap { display: inline-flex; align-items: center; gap: 6px; }
            .with-icon-gap .btn-text { margin-left: 2px; }
          }

          // 按钮另起一行显示在时间选择器下方
          .filter-inline.second .filter-actions {
            flex: 0 0 100%;
            width: 100%;
            margin-top: 0;
            justify-content: flex-end;
          }

          // 宽度工具类（用于控件）
          .w-160 { width: 160px; }
          .w-180 { width: 180px; }
          .w-200 { width: 200px; }
          .w-220 { width: 220px; }
          .w-240 { width: 240px; }
          .w-260 { width: 260px; }
          .w-300 { width: 300px; }
          .w-340 { width: 340px; }
          .w-380 { width: 380px; }
        }
        .list-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .list-title {
            display: flex;
            align-items: center;
            gap: 8px;
            font-weight: 600;
            color: #303133;
          }
          
          .list-actions {
            display: flex;
            align-items: center;
            gap: 12px;
            
            .page-size-selector {
              display: flex;
              align-items: center;
              gap: 8px;
              font-size: 14px;
              color: #606266;
              
              .el-select {
                width: 80px;
              }
            }
          }
        }
        
        // 表格样式
        :deep(.el-table) {
          margin-top: 20px;
          
          .log-time {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 13px;
            
            .el-icon {
              color: #909399;
            }
          }
          
          .user-info {
            display: flex;
            align-items: center;
            gap: 8px;
            
            .user-avatar {
              flex-shrink: 0;
            }
            
            .username {
              font-size: 13px;
              color: #303133;
            }
          }
          
          .log-description {
            font-size: 13px;
            color: #606266;
            line-height: 1.4;
            max-width: 260px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          
          .ip-info {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 13px;
            
            .el-icon {
              color: #909399;
            }
          }
        }
        
        .pagination-wrapper {
          margin-top: 20px;
          display: flex;
          justify-content: center;
        }
      }
    }
  
    // 日志详情弹窗
    .log-detail {
      .detail-sections {
        margin-top: 20px;
        
        .detail-section {
          margin-bottom: 16px;
          
          &:last-child {
            margin-bottom: 0;
          }
          
          &.error {
            :deep(.el-card__header) {
              background: #fef0f0;
              color: #f56c6c;
            }
          }
          
          .detail-content {
            font-size: 14px;
            line-height: 1.6;
            color: #303133;
            
            &.code-content {
              background: #f5f5f5;
              border-radius: 4px;
              padding: 12px;
              
              pre {
                margin: 0;
                font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
                font-size: 12px;
                line-height: 1.5;
                white-space: pre-wrap;
                word-break: break-all;
              }
            }
            
            &.error-content {
              color: #f56c6c;
              background: #fef0f0;
              border-radius: 4px;
              padding: 12px;
            }
          }
        }
      }
      
      .user-agent {
        font-size: 12px;
        color: #909399;
        word-break: break-all;
        line-height: 1.5;
      }
    }
  
    // 清理对话框
    .clean-dialog-content {
      .form-tip {
        font-size: 12px;
        color: #909399;
        padding-left: 10px;
      }
    }
  }
  
  // Teleport的下拉菜单项着色（全局深度选择）
  :deep(.el-dropdown-menu__item.success-item) { color: #67c23a; }
  :deep(.el-dropdown-menu__item.danger-item) { color: #f56c6c; }

  // 响应式设计
  @media (max-width: 1200px) {
    .user-log-page {
      .filter-content {
        .filter-row {
          .el-col {
            margin-bottom: 16px;
          }
        }
      }
      
      .statistics-content {
        .el-col {
          margin-bottom: 16px;
        }
      }
    }
  }
  
  @media (max-width: 768px) {
    .user-log-page {
      padding: 10px;
      
      .page-header {
        .page-title {
          font-size: 20px;
        }
      }
      
      .filter-content {
        .filter-item {
          flex-direction: column;
          align-items: flex-start;
          gap: 8px;
          
          .filter-label {
            min-width: auto;
            text-align: left;
          }
          
          .el-select,
          .el-input,
          .el-date-picker {
            width: 100%;
          }
        }
      }
      
      .list-header {
        flex-direction: column;
        gap: 12px;
        align-items: flex-start;
        
        .list-actions {
          width: 100%;
          justify-content: space-between;
        }
      }
    }
  }
  </style> 