<template>
  <div class="dict-page">
    <!-- 操作栏 -->
    <div class="dict-operations">
      <el-card class="operations-card">
        <template #header>
          <AppBreadcrumb />
        </template>
        <!-- 第一行：搜索和筛选 -->
        <div class="search-filter-row">
          <div class="search-group">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索字典名称、标签..."
              prefix-icon="Search"
              clearable
              @input="handleSearch"
              class="search-input"
            />
            <el-input
              v-model="searchForm.dictCode"
              placeholder="搜索字典编码..."
              prefix-icon="Search"
              clearable
              @input="handleSearch"
              class="search-input"
            />
          </div>
          
          <div class="filter-group">
            <el-select 
              v-model="searchForm.module" 
              placeholder="选择模块" 
              clearable
              @change="handleSearch"
              :loading="modulesLoading"
            >
              <el-option label="全部模块" value="" />
              <el-option 
                v-for="module in moduleOptions" 
                :key="module.value" 
                :label="module.label" 
                :value="module.value" 
              />
            </el-select>
            
            <el-select 
              v-model="searchForm.status" 
              placeholder="状态" 
              clearable
              @change="handleSearch"
            >
              <el-option label="全部状态" value="" />
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
            
            <el-select 
              v-model="searchForm.level" 
              placeholder="层级" 
              clearable
              @change="handleSearch"
            >
              <el-option label="全部层级" value="" />
              <el-option label="一级" :value="1" />
              <el-option label="二级" :value="2" />
              <el-option label="三级及以下" :value="3" />
            </el-select>
          </div>
        </div>
        
        <!-- 第二行：操作按钮 -->
        <div class="action-row">
          <div class="action-group">
            <el-button type="primary" @click="handleAdd">
              新增字典
            </el-button>
            <el-button @click="handleRefresh">
              刷新
            </el-button>
            <el-dropdown @command="handleBatchCommand" v-if="selectedRows?.length > 0">
              <el-button>
                批量操作<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="enable">批量启用</el-dropdown-item>
                  <el-dropdown-item command="disable">批量禁用</el-dropdown-item>
                  <el-dropdown-item command="delete">批量删除</el-dropdown-item>
                  <el-dropdown-item command="export">导出选中</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-dropdown @command="handleImportExport">
              <el-button>
                更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="import">导入字典</el-dropdown-item>
                  <el-dropdown-item command="export-all">导出全部</el-dropdown-item>
                  <el-dropdown-item command="refresh-cache">刷新缓存</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 字典表格 -->
    <div class="dict-table">
      <el-card class="table-card">
        <el-table
          :data="tableData"
          v-loading="loading"
          @selection-change="handleSelectionChange"
          @sort-change="handleSortChange"
          @row-dblclick="handleRowDoubleClick"
          row-key="id"
          class="dict-data-table"
          stripe
        >
          <!-- 选择列 -->
          <el-table-column type="selection" fixed="left" />
          
          <!-- 字典编码 -->
          <el-table-column 
            prop="dictCode" 
            label="字典编码" 
            sortable="custom"
            fixed="left"
          >
            <template #default="{ row }">
              <el-tag type="primary" size="small">{{ row.dictCode }}</el-tag>
            </template>
          </el-table-column>
          
          <!-- 字典名称 -->
          <el-table-column 
            prop="dictName" 
            label="字典名称" 
            sortable="custom"
            show-overflow-tooltip
          />
          
          <!-- 字典标签 -->
          <el-table-column 
            prop="dictLabel" 
            label="字典标签" 
            show-overflow-tooltip
          />
          
          <!-- 所属模块 -->
          <el-table-column prop="module" label="所属模块" sortable="custom">
            <template #default="{ row }">
              <el-tag :type="getModuleTagType(row.module)" size="small">
                {{ row.module }}
              </el-tag>
            </template>
          </el-table-column>
          
          <!-- 层级路径 -->
          <el-table-column prop="level" label="层级路径" sortable="custom">
            <template #default="{ row }">
              <div class="path-cell">
                <el-tag size="small" round>L{{ row.level }}</el-tag>
                <span class="path-text">{{ row.path }}</span>
              </div>
            </template>
          </el-table-column>
          
          <!-- 状态 -->
          <el-table-column prop="status" label="状态" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                @change="handleStatusToggle(row)"
                :disabled="row.hasChildren"
                active-color="#52c41a"
                inactive-color="#d9d9d9"
                style="--el-switch-on-color: #52c41a; --el-switch-off-color: #ff4d4f"
              />
            </template>
          </el-table-column>
          
          <!-- 创建时间 -->
          <el-table-column 
            prop="createTime" 
            label="创建时间" 
            sortable="custom"
            show-overflow-tooltip
          >
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
          
          <!-- 操作列 -->
          <el-table-column label="操作" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button 
                  size="small" 
                  type="primary"
                  plain
                  @click="handleEdit(row)"
                  class="edit-btn"
                >
                  编辑
                </el-button>
                
                <el-button 
                  size="small" 
                  type="danger"
                  plain
                  @click="handleDelete(row)"
                  class="delete-btn"
                  :disabled="row.hasChildren"
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
          <template #empty>
            <EmptyBox size="table" title="暂无字典数据" desc="可尝试调整筛选条件或创建新字典" />
          </template>
        </el-table>
        <!-- 分页器（固定在卡片底部） -->
        <div class="table-footer">
          <CommonPagination
            v-model:current="pagination.current"
            v-model:size="pagination.size"
            :total="pagination.total"
            :selected-count="selectedRows?.length || 0"
            :default-page-size="10"
            :show-info="false"
            align="right"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="450px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <div style="padding: 0 20px;">
        <el-form
          ref="dictForm"
          :model="dictFormData"
          :rules="dictFormRules"
          label-width="80px"
        >
          <el-form-item label="父级字典" prop="parentId">
            <el-cascader
              v-model="dictFormData.parentId"
              :options="parentCascaderData"
              :props="cascaderProps"
              clearable
              filterable
              :show-all-levels="true"
              separator=" > "
              placeholder="未选择父级则默认为顶级字典"
              style="width: 250px"
              :loading="treeLoading"
              :disabled="parentCascaderData.length === 0 && !treeLoading"
            >
              <template #empty>
                <div style="text-align: center; padding: 20px; color: #999;">
                  <span v-if="!dictFormData.module">请先选择所属模块</span>
                  <span v-else>该模块下暂无可选择的父级字典</span>
                </div>
              </template>
            </el-cascader>
          </el-form-item>

          <el-form-item label="字典名称" prop="dictName">
            <el-input
              v-model="dictFormData.dictName"
              style="width: 250px"
            />
          </el-form-item>

          <el-form-item label="字典标签" prop="dictLabel">
            <el-input
              v-model="dictFormData.dictLabel"
              style="width: 250px"
            />
          </el-form-item>

          <el-form-item label="所属模块" prop="module">
            <el-select
              v-model="dictFormData.module"
              placeholder="请选择所属模块"
              style="width: 250px"
              @change="handleModuleChange"
              :loading="modulesLoading"
            >
              <el-option 
                v-for="module in moduleOptions" 
                :key="module.value" 
                :label="module.label" 
                :value="module.value" 
              />
            </el-select>
          </el-form-item>

          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="dictFormData.status">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="描述" prop="description">
            <el-input
              v-model="dictFormData.description"
              type="textarea"
              :rows="3"
              style="width: 250px"
              resize="none"
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button 
            @click="dialogVisible = false" 
            :disabled="submitLoading"
          >
            取消
          </el-button>
          <el-button 
            type="primary" 
            @click="handleSubmit" 
            :loading="submitLoading"
          >
            {{ dialogType === 'add' ? '确定' : '保存' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import CommonPagination from '@/components/common/Pagination.vue'
import EmptyBox from '@/components/common/EmptyBox.vue'
import AppBreadcrumb from '@/components/common/Breadcrumb.vue'
import * as dictApi from '@/api/system/dict'

export default {
  name: 'DictPage',
  components: {
    ArrowDown,
    CommonPagination,
    AppBreadcrumb,
    EmptyBox
  },
  setup() {
    // 响应式数据
    const loading = ref(false)
    const submitLoading = ref(false)
    const treeLoading = ref(false)
    const modulesLoading = ref(false)
    const selectedRows = ref([])
    const dialogVisible = ref(false)
    const dialogType = ref('add')
    const dictForm = ref(null)
    const dictData = ref([])
    const moduleOptions = ref([])

    // 搜索表单 - 对应后端SystemDictQueryDTO
    const searchForm = reactive({
      keyword: '',        // 关键词搜索，同时搜索字典名称和标签
      dictCode: '',       // 字典编码
      module: '',         // 所属模块
      status: '',         // 字典状态
      parentId: null,     // 父级字典ID
      level: ''           // 字典层级
    })
    
    // 排序状态 - 由表格列头点击控制
    const sortState = reactive({
      sortField: 'createTime',     // 排序字段，默认按创建时间
      sortDirection: 'desc'        // 排序方向，默认降序（最新的在前）
    })

    // 分页数据 - 对应后端分页参数
    const pagination = reactive({
      current: 1,    // 当前页码
      size: 10,      // 每页大小
      total: 0       // 总数
    })

    // 字典表单数据
    const dictFormData = reactive({
      id: null,
      dictName: '',
      dictLabel: '',
      module: '',
      parentId: null,
      status: 1,
      description: ''
    })
    
    // 父级字典树形数据
    const parentTreeData = ref([])
    
    // 父级字典级联选择器数据
    const parentCascaderData = ref([])
    
    // 级联选择器配置
    const cascaderProps = {
      children: 'children',
      label: 'dictName',
      value: 'id',
      checkStrictly: true,  // 允许选择任意一级
      emitPath: false       // 只返回最后一级的值
    }
    


    // 表单验证规则
    const dictFormRules = {
      dictName: [
        { required: true, message: '请输入字典名称', trigger: 'blur' },
        { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
      ],
      dictLabel: [
        { required: true, message: '请输入字典标签', trigger: 'blur' }
      ],
      module: [
        { required: true, message: '请选择所属模块', trigger: 'change' }
      ]
    }



    // 表格数据 - 直接使用后端返回的分页数据，不再前端过滤
    const tableData = computed(() => {
      return dictData.value || []
    })

    // 对话框标题
    const dialogTitle = computed(() => {
      return dialogType.value === 'add' ? '新增字典' : '编辑字典'
    })



    // 加载数据
    const loadData = async () => {
      loading.value = true
      try {
        // 构建符合后端SystemDictQueryDTO的参数
        const queryParams = {
          // 分页参数 - 使用后端要求的字段名
          pageNum: pagination.current,        // 后端期望pageNum
          pageSize: pagination.size,          // 后端期望pageSize
          
          // 查询参数 - 使用keyword统一搜索字典名称和标签
          keyword: searchForm.keyword,        // keyword用于同时搜索字典名称和标签
          dictCode: searchForm.dictCode,      // dictCode用于搜索字典编码
          module: searchForm.module,
          status: searchForm.status !== '' ? searchForm.status : null,
          parentId: searchForm.parentId,
          level: searchForm.level !== '' ? searchForm.level : null,
          sortField: sortState.sortField,     // 使用sortState中的排序字段
          sortDirection: sortState.sortDirection  // 使用sortState中的排序方向
        }
        
        // 移除空值参数
        Object.keys(queryParams).forEach(key => {
          if (queryParams[key] === '' || queryParams[key] === null || queryParams[key] === undefined) {
            delete queryParams[key]
          }
        })
        
        const result = await dictApi.getDictList(queryParams)
        
        if (result.code === 200) {
          dictData.value = result.data.records || []
          pagination.total = result.data.total || 0
          // 分页状态已经正确同步，无需额外处理
        } else {
          ElMessage.error(result.message || '获取数据失败')
        }
      } catch (error) {
        console.error('加载数据失败:', error)
        ElMessage.error('加载数据失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }

    // 工具函数
    const getModuleTagType = (module) => {
      const types = {
        'SYSTEM': 'primary',
        'USER': 'success',
        'FILE': 'info',
        'TASK': 'warning',
        'REPORT': 'danger'
      }
      return types[module] || 'default'
    }

    const formatTime = (time) => {
      if (!time) return '-'
      return new Date(time).toLocaleString('zh-CN')
    }

    const resetForm = () => {
      Object.assign(dictFormData, {
        id: null,
        dictName: '',
        dictLabel: '',
        module: '',
        parentId: null,
        status: 1,
        description: ''
      })
      
      // 清空父级字典树数据
      parentTreeData.value = []
      parentCascaderData.value = []
      
      if (dictForm.value) {
        dictForm.value.clearValidate()
      }
    }

    // 事件处理函数
    const handleSearch = () => {
      pagination.current = 1
      loadData()
    }

    const handleRefresh = () => {
      loadData()
    }

    const handleAdd = async () => {
      dialogType.value = 'add'
      resetForm()
      // 加载所有模块的父级字典树数据（不限制模块）
      await loadParentTreeData()
      dialogVisible.value = true
    }

    const handleEdit = async (row) => {
      dialogType.value = 'edit'
      Object.assign(dictFormData, row)
      
      // 如果有模块信息，则加载该模块的父级字典树
      if (row.module) {
        await loadParentTreeData(row.module)
      }
      
      dialogVisible.value = true
    }

    const handleDelete = async (row) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除字典 "${row.dictName}" 吗？`,
          '确认删除',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
        )

        const result = await dictApi.deleteDict(row.id)
        if (result.code === 200) {
          ElMessage.success('删除成功')
          await loadData()
        } else {
          ElMessage.error(result.message || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
          ElMessage.error('删除失败，请稍后重试')
        }
      }
    }

    const handleSubmit = async () => {
      if (!dictForm.value) return

      try {
        await dictForm.value.validate()
        submitLoading.value = true

        // 准备提交数据，确保parentId正确处理
        const submitData = {
          ...dictFormData,
          // 如果parentId为null或undefined，则设为0（表示顶级字典）
          parentId: dictFormData.parentId || 0
        }

        console.log('提交的字典数据:', submitData)

        let result
        if (dialogType.value === 'add') {
          result = await dictApi.createDict(submitData)
        } else {
          result = await dictApi.updateDict(submitData.id, submitData)
        }

        if (result.code === 200) {
          ElMessage.success(dialogType.value === 'add' ? '添加成功' : '保存成功')
          dialogVisible.value = false
          await loadData()
        } else {
          ElMessage.error(result.message || '操作失败')
        }
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error('操作失败，请稍后重试')
      } finally {
        submitLoading.value = false
      }
    }

    const handleDialogClose = () => {
      resetForm()
    }

    const handleSelectionChange = (selection) => {
      selectedRows.value = selection
    }



    const handleRowDoubleClick = (row) => {
      handleEdit(row)
    }

    const handleStatusToggle = async (row) => {
      try {
        const result = await dictApi.updateDictStatus(row.id, row.status)
        if (result.code === 200) {
          ElMessage.success('状态更新成功')
        } else {
          ElMessage.error(result.message || '状态更新失败')
          // 恢复原状态
          row.status = row.status === 1 ? 0 : 1
        }
      } catch (error) {
        console.error('状态更新失败:', error)
        ElMessage.error('状态更新失败，请稍后重试')
        // 恢复原状态
        row.status = row.status === 1 ? 0 : 1
      }
    }

    const handleBatchCommand = async (command) => {
      if (!selectedRows.value || selectedRows.value.length === 0) {
        ElMessage.warning('请选择要操作的数据')
        return
      }

      try {
        switch (command) {
          case 'delete': {
        await ElMessageBox.confirm(
              `确定要删除选中的 ${selectedRows.value.length} 项数据吗？`,
              '确认批量删除',
          {
                confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
              }
            )
            
            const ids = selectedRows.value.map(row => row.id)
            const result = await dictApi.batchDeleteDict(ids)
            
            if (result.code === 200) {
              ElMessage.success('批量删除成功')
              selectedRows.value = []
              await loadData()
            } else {
              ElMessage.error(result.message || '批量删除失败')
            }
            break
          }
          
          case 'enable': {
            await ElMessageBox.confirm(
              `确定要启用选中的 ${selectedRows.value.length} 项数据吗？`,
              '确认批量启用',
              {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
              }
            )
            
            const ids = selectedRows.value.map(row => row.id)
            const result = await dictApi.batchEnableDict(ids)
            
            if (result.code === 200) {
              ElMessage.success('批量启用成功')
              selectedRows.value = []
              await loadData()
            } else {
              ElMessage.error(result.message || '批量启用失败')
            }
            break
          }
          
          case 'disable': {
            await ElMessageBox.confirm(
              `确定要禁用选中的 ${selectedRows.value.length} 项数据吗？`,
              '确认批量禁用',
              {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
              }
            )

            const ids = selectedRows.value.map(row => row.id)
            const result = await dictApi.batchDisableDict(ids)

            if (result.code === 200) {
              ElMessage.success('批量禁用成功')
        selectedRows.value = []
              await loadData()
            } else {
              ElMessage.error(result.message || '批量禁用失败')
            }
            break
          }
            
          case 'export': {
            ElMessage.info('导出功能暂未开放，敬请期待')
            break
          }
    }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('批量操作失败:', error)
          ElMessage.error('操作失败，请稍后重试')
        }
      }
    }

    const handleImportExport = async (command) => {
      try {
        switch (command) {
          case 'import': {
            ElMessage.info('导入功能暂未开放')
            break
          }
            
          case 'export-all': {
            ElMessage.info('导出功能暂未开放')
            break
          }
            
          case 'refresh-cache': {
            try {
              loading.value = true
              const result = await dictApi.refreshDictCache()
              
              if (result.code === 200) {
                ElMessage.success('缓存刷新成功')
              } else {
                ElMessage.error(result.message || '缓存刷新失败')
              }
            } catch (error) {
              console.error('缓存刷新失败:', error)
              ElMessage.error('缓存刷新失败，请稍后重试')
            } finally {
              loading.value = false
            }
            break
          }
        }
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败，请稍后重试')
      }
    }

    const handleSizeChange = (size) => {
      pagination.size = size
      pagination.current = 1  // 重置到第一页
      loadData()
    }

    const handleCurrentChange = (current) => {
      pagination.current = current
      loadData()
    }

    // 处理表格排序变化
    const handleSortChange = ({ prop, order }) => {
      if (prop && order) {
        // 将Element Plus的排序方向转换为后端期望的格式
        sortState.sortField = prop
        sortState.sortDirection = order === 'ascending' ? 'asc' : 'desc'
      } else {
        // 清除排序，恢复默认排序
        sortState.sortField = 'createTime'
        sortState.sortDirection = 'desc'
      }
      
      // 排序变化时重置到第一页并重新加载数据
      pagination.current = 1
      loadData()
    }
    
    // 处理模块变化 - 重新加载父级字典树
    const handleModuleChange = async (module) => {
      try {
        // 保存当前选中的父级ID
        const currentParentId = dictFormData.parentId
        
        // 重新加载该模块的父级字典树
        await loadParentTreeData(module)
        
        // 如果有选中的父级字典，检查是否仍然有效
        if (currentParentId) {
          const isParentExists = checkParentExists(parentCascaderData.value, currentParentId)
          if (!isParentExists) {
            // 如果当前选中的父级不在新模块中，则清空选择
            dictFormData.parentId = null
            console.log('当前选择的父级字典不属于新模块，已清空选择')
          }
        }
      } catch (error) {
        console.error('加载父级字典树失败:', error)
        ElMessage.error('加载父级字典树失败')
      }
    }
    
    // 检查父级字典是否存在于树形数据中
    const checkParentExists = (treeData, parentId) => {
      if (!treeData || !Array.isArray(treeData) || !parentId) {
        return false
      }
      
      for (const item of treeData) {
        if (item.id === parentId || item.value === parentId) {
          return true
        }
        // 递归检查子节点
        if (item.children && checkParentExists(item.children, parentId)) {
          return true
        }
      }
      return false
    }
    
    // 转换树形数据为级联选择器数据格式
    const convertToCascaderData = (treeData) => {
      if (!treeData || !Array.isArray(treeData)) {
        return []
      }
      
      return treeData.map(item => {
        const cascaderItem = {
          id: item.id,
          dictName: item.dictName,
          value: item.id,
          label: item.dictName,
          disabled: item.status === 0  // 禁用状态的字典不能选择
        }
        
        // 如果有子节点，递归处理
        if (item.children && item.children.length > 0) {
          cascaderItem.children = convertToCascaderData(item.children)
        }
        
        return cascaderItem
      })
    }
    
    // 加载模块列表数据
    const loadModuleOptions = async () => {
      try {
        modulesLoading.value = true
        const result = await dictApi.getDictModules()
        
        if (result.code === 200) {
          // 后端现在直接返回对象数组格式 [{value: "SYSTEM", label: "系统模块"}]
          moduleOptions.value = result.data || []
        } else {
          moduleOptions.value = []
          console.error('获取模块列表失败:', result.message)
          ElMessage.error(result.message || '获取模块列表失败')
        }
      } catch (error) {
        moduleOptions.value = []
        console.error('获取模块列表失败:', error)
        ElMessage.error('获取模块列表失败，请稍后重试')
      } finally {
        modulesLoading.value = false
      }
    }

    // 加载父级字典树数据
    const loadParentTreeData = async (module = null) => {
      try {
        treeLoading.value = true
        console.log('开始加载父级字典树数据，模块:', module)
        const result = await dictApi.getDictTreeOptions(module)
        console.log('字典树数据响应:', result)
        
        if (result.code === 200) {
          // 后端已经返回树形结构，直接使用
          parentTreeData.value = result.data || []
          // 为级联选择器转换数据格式
          parentCascaderData.value = convertToCascaderData(result.data || [])
          console.log('父级字典树数据:', parentTreeData.value)
          console.log('级联选择器数据:', parentCascaderData.value)
        } else {
          parentTreeData.value = []
          parentCascaderData.value = []
          console.error('获取父级字典树失败:', result.message)
          ElMessage.error(result.message || '获取父级字典树失败')
        }
      } catch (error) {
        parentTreeData.value = []
        parentCascaderData.value = []
        console.error('获取父级字典树失败:', error)
        ElMessage.error('获取父级字典树失败，请稍后重试')
      } finally {
        treeLoading.value = false
      }
    }

    // 监听搜索条件变化
    watch(
      () => [searchForm.keyword, searchForm.dictCode, searchForm.module, searchForm.status, searchForm.level, searchForm.parentId],
      () => {
        pagination.current = 1  // 搜索条件变化时重置到第一页
        loadData()  // 搜索条件变化时重新加载数据
      },
      { deep: true }
    )

    // 组件挂载时加载数据
    onMounted(() => {
      loadData()
      loadModuleOptions()
    })

    return {
      // 响应式数据
      loading,
      submitLoading,
      treeLoading,
      modulesLoading,
      selectedRows,
      dialogVisible,
      dialogType,
      dictForm,
      dictData,
      moduleOptions,
      searchForm,
      sortState,
      pagination,
      dictFormData,
      dictFormRules,
      parentTreeData,
      parentCascaderData,
      cascaderProps,
      
      // 计算属性
      tableData,
      dialogTitle,
      
      // 方法
      loadData,
      loadModuleOptions,
      getModuleTagType,
      formatTime,
      resetForm,
      handleSearch,
      handleRefresh,
      handleAdd,
      handleEdit,
      handleDelete,
      handleSubmit,
      handleDialogClose,
      handleSelectionChange,
      handleSortChange,
      handleRowDoubleClick,
      handleStatusToggle,
      handleBatchCommand,
      handleImportExport,
      handleSizeChange,
      handleCurrentChange,
      loadParentTreeData,
      handleModuleChange
    }
  }
}
</script>

<style scoped>
.dict-page {
  padding: 8px 0 12px;
  background: transparent;
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - var(--app-header-height) - var(--main-padding-v));
}

.dict-operations {
  margin-bottom: 20px;
}

.operations-card { border-radius: 8px; box-shadow: none; }

.search-filter-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  gap: 20px;
}

.search-group {
  display: flex;
  gap: 12px;
  flex: 1;
}

.search-input {
  max-width: 250px;
}

.filter-group {
  display: flex;
  gap: 12px;
}

.filter-group .el-select {
  width: 120px;
}

.action-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-group {
  display: flex;
  gap: 12px;
}

.dict-table {
  margin-bottom: 20px;
}

.table-card { border-radius: 8px; box-shadow: none; display: flex; flex-direction: column; min-height: 0; flex: 1 1 auto; width: 100%; }
.table-card :deep(.el-card__body) { display: flex; flex-direction: column; flex: 1 1 auto; min-height: 0; }

.dict-data-table {
  width: 100%;
}

/* 使表格卡片区域可拉伸，分页固定在底部 */
.dict-table { display: flex; flex: 1 1 auto; min-height: 0; width: 100%; }
.dict-data-table { flex: 1 1 auto; min-height: 0; }
.table-footer { margin-top: auto; padding-top: 8px; }
/* 仅在字典页，避免表格与分页出现双重分割线 */
.table-footer :deep(.common-pagination) { border-top: 0; padding-top: 0; }

.path-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.path-text {
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #666;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.edit-btn, .delete-btn {
  padding: 4px 8px;
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .search-filter-row {
    flex-direction: column;
    gap: 15px;
  }
  
  .search-group {
  width: 100%;
}

  .filter-group {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .dict-page {
    padding: 10px;
  }
  
  .action-row {
    flex-direction: column;
    gap: 15px;
  }
  
  .action-group {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}

/* 现代化弹窗样式 */
.modern-dialog {
  .el-dialog {
    border-radius: 16px;
    box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
    border: none;
    overflow: hidden;
  }
  
  .el-dialog__header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border-bottom: none;
    padding: 24px 32px 20px;
    
    .el-dialog__title {
      font-size: 20px;
      font-weight: 700;
      color: white;
      text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
    }
    
    .el-dialog__headerbtn {
      top: 20px;
      right: 24px;
      
      .el-dialog__close {
        color: rgba(255, 255, 255, 0.8);
        font-size: 18px;
        font-weight: bold;
        
        &:hover {
          color: white;
          transform: scale(1.1);
        }
      }
    }
  }
  
  .el-dialog__body {
    padding: 32px;
    background: #ffffff;
    max-height: 70vh;
    overflow-y: auto;
  }
  
  .el-dialog__footer {
    padding: 20px 32px 32px;
    background: #f8fafe;
    border-top: 1px solid #e8ecf4;
  }
}

/* 简洁表单样式 */
.clean-form {
  .form-section {
    margin-bottom: 28px;
    
    &:last-child {
      margin-bottom: 16px;
    }
  }
  
  .section-header {
    margin-bottom: 20px;
    
    h4 {
      margin: 0;
      font-size: 15px;
      font-weight: 600;
      color: #374151;
  display: flex;
  align-items: center;
      
      &::before {
        content: '';
        width: 3px;
        height: 16px;
        background: linear-gradient(135deg, #3b82f6, #1d4ed8);
        border-radius: 2px;
        margin-right: 10px;
      }
    }
  }
  
  .form-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
    
    &.hierarchy-grid {
      grid-template-columns: 2fr 1fr;
    }
  }
  
  .el-form-item {
    margin-bottom: 0;
    
    .el-form-item__label {
      color: #4b5563;
      font-weight: 500;
      font-size: 14px;
      margin-bottom: 8px;
      line-height: 1;
  height: auto;
    }
    
    .el-input,
    .el-select,
    .el-tree-select,
    .el-cascader,
    .el-textarea {
      .el-input__wrapper,
      .el-textarea__inner {
        border-radius: 8px;
        border: 1.5px solid #d1d5db;
        transition: all 0.2s ease;
        
        &:hover {
          border-color: #9ca3af;
        }
        
        &.is-focus {
          border-color: #3b82f6;
          box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }
      }
    }
    
    .el-input-number {
      width: 100%;
      
      .el-input__wrapper {
        border-radius: 8px;
        border: 1.5px solid #d1d5db;
        
        &:hover {
          border-color: #9ca3af;
        }
        
        &.is-focus {
          border-color: #3b82f6;
          box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }
      }
    }
    
    &.parent-select,
    &.sort-input {
      grid-column: span 1;
    }
  }
  
  .status-radio {
    .el-radio {
      margin-right: 24px;
      
      .el-radio__label {
        color: #4b5563;
        font-weight: 500;
      }
      
      &.is-checked .el-radio__label {
        color: #3b82f6;
        font-weight: 600;
      }
    }
  }
}

/* 分步表单样式 */
.step-form {
  .form-section {
    margin-bottom: 32px;
    background: #ffffff;
    border-radius: 12px;
    border: 1px solid #e8ecf4;
    padding: 24px;
    position: relative;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    &:hover {
      border-color: #d1d9e6;
      box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
    }
  }
  
  .section-title {
  display: flex;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 2px solid #f1f5f9;
    
    .title-icon {
      font-size: 18px;
      margin-right: 8px;
    }
    
    .title-text {
      font-size: 16px;
      font-weight: 600;
      color: #1e293b;
      margin-right: auto;
    }
    
    .required-indicator {
      font-size: 12px;
      color: #ef4444;
      background: #fef2f2;
      padding: 2px 8px;
  border-radius: 4px;
      font-weight: 500;
    }
    
    .optional-indicator {
  font-size: 12px;
      color: #64748b;
      background: #f8fafc;
      padding: 2px 8px;
      border-radius: 4px;
      font-weight: 500;
    }
  }
  
  .form-row {
    margin-bottom: 20px;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  .form-field {
    margin-bottom: 0;
    
    .el-form-item__label {
      margin-bottom: 8px;
      line-height: 1.4;
      display: block;
      
      .field-label {
        display: block;
        font-size: 14px;
        font-weight: 600;
        color: #374151;
        margin-bottom: 4px;
        
        &.required::after {
          content: ' *';
          color: #ef4444;
          font-weight: bold;
        }
      }
      
      .field-tip {
        display: block;
  font-size: 12px;
        color: #6b7280;
        font-weight: 400;
        line-height: 1.3;
      }
    }
    
    .el-input, .el-select, .el-tree-select, .el-cascader {
      .el-input__wrapper {
        border-radius: 8px;
        border: 2px solid #e5e7eb;
        transition: all 0.2s ease;
        background: #ffffff;
        
        &:hover {
          border-color: #d1d5db;
        }
        
        &.is-focus {
          border-color: #667eea;
          box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }
      }
    }
    
    .el-textarea {
      .el-textarea__inner {
        border-radius: 8px;
        border: 2px solid #e5e7eb;
        transition: all 0.2s ease;
        background: #ffffff;
        
        &:hover {
          border-color: #d1d5db;
        }
        
        &:focus {
          border-color: #667eea;
          box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }
      }
    }
    
    .el-input-number {
      width: 100%;
      
      .el-input__wrapper {
        border-radius: 8px;
        border: 2px solid #e5e7eb;
    
    &:hover {
          border-color: #d1d5db;
        }
        
        &.is-focus {
          border-color: #667eea;
          box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }
      }
    }
  }
  
  .status-radio-group {
    display: flex;
    gap: 12px;
    
    .el-radio-button {
      .el-radio-button__inner {
        border-radius: 8px;
        border: 2px solid #e5e7eb;
        background: #ffffff;
        color: #374151;
        font-weight: 500;
        padding: 12px 20px;
        transition: all 0.2s ease;
        
        .radio-content {
          display: flex;
          align-items: center;
          gap: 6px;
          
          .radio-icon {
            font-size: 14px;
          }
          
          .radio-text {
            font-size: 14px;
          }
        }
    
    &:hover {
          border-color: #d1d5db;
          background: #f9fafb;
        }
      }
      
      &.is-active .el-radio-button__inner {
        border-color: #667eea;
        background: #f0f4ff;
        color: #667eea;
      }
    }
  }
}

/* 选择框选项样式 */
.option-content {
  display: flex;
  align-items: center;
    gap: 12px;
  padding: 4px 0;
  
  .option-icon {
    font-size: 16px;
    width: 20px;
    text-align: center;
  }
  
  .option-text {
    flex: 1;
    
    .option-label {
      font-size: 14px;
      font-weight: 500;
      color: #374151;
      line-height: 1.2;
    }
    
    .option-desc {
      font-size: 12px;
      color: #6b7280;
      line-height: 1.2;
      margin-top: 2px;
    }
  }
}

/* 弹窗底部样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  
  .cancel-button {
    color: #6b7280;
    border-color: #d1d5db;
    background: #ffffff;
    
    &:hover {
      color: #374151;
      border-color: #9ca3af;
      background: #f9fafb;
    }
  }
  
  .submit-button {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    font-weight: 600;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
    
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }
    
    &:active {
      transform: translateY(0);
    }
  }
}

/* 垂直表单样式 */
.vertical-form {
  .el-form-item {
    margin-bottom: 20px;
    
    .el-form-item__label {
      color: #374151;
      font-weight: 500;
      font-size: 14px;
      margin-bottom: 8px;
    }
    
    .el-input, .el-select, .el-tree-select, .el-cascader {
      .el-input__wrapper {
        border-radius: 6px;
        border: 1px solid #d1d5db;
        transition: all 0.2s ease;
        
        &:hover {
          border-color: #9ca3af;
        }
        
        &.is-focus {
          border-color: #3b82f6;
          box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
        }
      }
    }
    
    .el-textarea {
      .el-textarea__inner {
        border-radius: 6px;
        border: 1px solid #d1d5db;
        transition: all 0.2s ease;
        
        &:hover {
          border-color: #9ca3af;
        }
        
        &:focus {
          border-color: #3b82f6;
          box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
        }
      }
    }
    
    .el-radio-group {
      .el-radio {
        margin-right: 20px;
        
        .el-radio__label {
          color: #4b5563;
          font-weight: 500;
        }
        
        &.is-checked .el-radio__label {
          color: #3b82f6;
        }
      }
    }
  }
}

/* 单列布局的表单项 */
.clean-form .form-section:last-child .el-form-item {
  grid-column: span 2;
}

/* 弹窗操作区域 */
.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #f0f2f5;
  background: #fafbfc;
  margin: 0 -24px -24px;
  border-radius: 0 0 12px 12px;
  
  .el-button {
    border-radius: 8px;
    padding: 10px 20px;
    font-weight: 500;
    font-size: 14px;
    
    &:not([disabled]):hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
    
    &.el-button--primary {
      background: linear-gradient(135deg, #3b82f6, #1d4ed8);
      border: none;
      
      &:hover {
        background: linear-gradient(135deg, #2563eb, #1e40af);
      }
    }
  }
}

/* 级联选择器样式优化 */
.el-cascader {
  .el-cascader__tags {
    .el-tag {
      background: #f0f4ff;
      border-color: #667eea;
      color: #667eea;
      border-radius: 6px;
      margin-right: 6px;
      margin-bottom: 4px;
      
      .el-tag__close {
        color: #667eea;
        
        &:hover {
          background: #667eea;
          color: white;
        }
      }
    }
  }
  
  .el-cascader__collapse-tags {
    .el-tag {
      background: #e5e7eb;
      border-color: #d1d5db;
      color: #6b7280;
      
      &:hover {
        background: #f3f4f6;
      }
    }
  }
}

/* 级联选择器下拉面板样式 */
:deep(.el-cascader-panel) {
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  
  .el-cascader-menu {
    border-radius: 8px;
    
    .el-cascader-node {
      padding: 8px 12px;
      border-radius: 4px;
      margin: 2px 4px;
      transition: all 0.2s ease;
      
      &:hover {
        background: #f0f4ff;
        color: #667eea;
      }
      
      &.is-active {
        background: #667eea;
        color: white;
        font-weight: 500;
      }
      
      &.is-disabled {
        background: #f5f5f5;
        color: #c0c4cc;
        cursor: not-allowed;
      }
      
      .el-cascader-node__label {
        font-size: 14px;
      }
      
      .el-cascader-node__postfix {
        .el-icon {
          color: #667eea;
          font-size: 12px;
        }
      }
    }
  }
  
  .el-cascader-menu:not(:last-child) {
    border-right: 1px solid #f0f2f5;
  }
}

/* 级联选择器空状态样式 */
.cascader-empty {
  text-align: center;
  padding: 20px;
  color: #9ca3af;
  font-size: 14px;
  line-height: 1.5;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .modern-dialog {
    .el-dialog {
      width: 95% !important;
      margin: 5vh auto;
      border-radius: 12px;
    }
    
    .el-dialog__body {
      padding: 20px;
    }
    
    .el-dialog__footer {
      padding: 16px 20px 20px;
    }
  }
  
  .step-form {
    .form-section {
      padding: 16px;
      margin-bottom: 20px;
    }
    
    .section-title {
      margin-bottom: 16px;
      
      .title-text {
        font-size: 14px;
      }
      
      .required-indicator,
      .optional-indicator {
      display: none;
      }
    }
    
    .form-row {
      margin-bottom: 16px;
    }
    
    .status-radio-group {
      flex-direction: column;
      gap: 8px;
      
      .el-radio-button .el-radio-button__inner {
        text-align: center;
        padding: 10px 16px;
      }
    }
  }
  
  .dialog-footer {
    flex-direction: column;
    gap: 12px;
    
    .el-button {
      width: 100%;
      order: 2;
      
      &.submit-button {
        order: 1;
      }
    }
  }
  
  .clean-form {
    .form-grid {
      grid-template-columns: 1fr;
      gap: 16px;
      
      &.hierarchy-grid {
        grid-template-columns: 1fr;
      }
    }
    
    .el-form-item.parent-select,
    .el-form-item.sort-input {
      grid-column: span 1;
    }
  }
}

/* 简洁弹窗样式 */
.el-dialog {
  border-radius: 8px;
}

.el-dialog__header {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.el-dialog__body {
  padding: 0;
}

.el-dialog__footer {
  padding: 16px 20px;
  border-top: 1px solid #ebeef5;
  text-align: right;
}
</style> 
