<template>
  <div class="config-page">
    <el-card class="content-card">
      <template #header>
        <AppBreadcrumb />
      </template>

      <!-- 搜索和筛选工具栏 -->
      <div class="filter-toolbar">
      <!-- 搜索框独立一行 -->
      <div class="search-row">
        <div class="search-section">
          <div class="search-input-wrapper">
            <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <path d="m21 21-4.35-4.35"/>
            </svg>
            <input 
              v-model="searchKeyword" 
              type="text" 
              placeholder="搜索配置key、value、描述..."
              class="search-input"
              @input="filterConfigs"
            />
            <button v-if="searchKeyword" @click="clearSearch" class="clear-search">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
      
      <!-- 分类和刷新按钮另起一行 -->
      <div class="filter-row">
        <div class="category-filter">
          <div class="filter-label">分类筛选：</div>
          <div class="category-tabs">
            <button 
              v-for="category in availableCategories" 
              :key="category.value"
              @click="setSelectedCategory(category.value)"
              :class="['category-tab', { 'active': selectedCategory === category.value }]"
            >
              {{ category.label }}
              <span class="category-count">{{ category.count }}</span>
            </button>
          </div>
        </div>
        
        <div class="toolbar-actions">
          <button 
            v-if="selectedIds.size > 0"
            @click="confirmBatchDelete"
            class="danger-btn"
            :disabled="deleting"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="3 6 5 6 21 6"/>
              <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
              <line x1="10" y1="11" x2="10" y2="17"/>
              <line x1="14" y1="11" x2="14" y2="17"/>
            </svg>
            批量删除 ({{ selectedIds.size }})
          </button>
          <button @click="openCreateDialog" class="refresh-btn">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 5v14M5 12h14"/>
            </svg>
            新增配置
          </button>
          <button @click="loadConfigList" class="refresh-btn">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="1 4 1 10 7 10"/>
              <polyline points="23 20 23 14 17 14"/>
              <path d="M20.49 9A9 9 0 0 0 5.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 0 1 3.51 15"/>
            </svg>
            刷新
          </button>
        </div>
      </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>正在加载配置数据...</p>
      </div>
      
      <!-- 配置项卡片网格 -->
      <div v-else class="config-grid">
      <div 
        v-for="config in filteredConfigs" 
        :key="config.id"
        class="config-card"
        :class="{ 'changed': config.isChanged, 'saving': config.isSaving, 'saved': config.isSaved, 'selected': selectedIds.has(String(config.id)) }"
      >
        <label class="select-checkbox">
          <input type="checkbox" :checked="selectedIds.has(String(config.id))" @change="toggleSelect(config.id, $event.target.checked)" />
          <span class="checkbox-mark"></span>
        </label>
        <!-- 卡片头部 -->
          <div class="card-header">
          <div class="config-actions">
            <button 
              v-if="config.isChanged && !config.isSaving"
              @click="saveConfig(config)"
              class="save-button"
              title="保存更改"
            >
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="20,6 9,17 4,12"/>
              </svg>
            </button>
            <div v-else-if="config.isSaving" class="saving-indicator">
              <div class="mini-spinner"></div>
            </div>
            <div v-else-if="config.isSaved" class="saved-indicator">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="20,6 9,17 4,12"/>
              </svg>
            </div>
          </div>
        </div>

        <!-- 配置描述 -->
        <div class="config-description">
          {{ config.description }}
        </div>

        <!-- 配置键名 -->
        <div class="config-key">
          {{ config.configKey }}
        </div>

        <!-- 配置值编辑区域 -->
        <div class="config-value-section">
          <div class="value-label">配置值：</div>
          <div class="value-input">
            <!-- 布尔类型 -->
            <div v-if="config.configType === 'BOOLEAN'" class="boolean-control">
              <label class="switch">
                <input 
                  type="checkbox" 
                  :checked="config.tempValue === 'true'"
                  @change="updateBooleanValue(config, $event.target.checked)"
                />
                <span class="slider"></span>
              </label>
              <span class="boolean-text">{{ config.tempValue === 'true' ? '启用' : '禁用' }}</span>
            </div>

            <!-- 数字类型 -->
            <input 
              v-else-if="config.configType === 'NUMBER'"
              v-model.number="config.tempValue"
              type="number" 
              class="number-input"
              min="0"
              @input="onConfigChange(config)"
              @blur="onInputBlur(config)"
            />

            <!-- 文本类型 -->
            <input 
              v-else-if="config.configType === 'STRING'"
              v-model="config.tempValue"
              type="text"
              class="text-input"
              :placeholder="'输入文本值...'"
              @input="onConfigChange(config)"
              @blur="onInputBlur(config)"
            />

            <!-- JSON类型 -->
            <textarea 
              v-else-if="config.configType === 'JSON'"
              v-model="config.tempValue"
              class="json-input"
              rows="3"
              placeholder="输入JSON值..."
              @input="onConfigChange(config)"
              @blur="onInputBlur(config)"
            ></textarea>
          </div>
        </div>

        <!-- 卡片底部信息 -->
        <div class="card-footer">
          <div class="update-time">
            最后更新：{{ formatTime(config.updateTime) }}
          </div>
        </div>
      </div>
      </div>

      <!-- 空状态展示（仅在有筛选条件时显示“未找到匹配”） -->
      <EmptyBox
        v-if="!loading && ((searchKeyword && searchKeyword.trim()) || selectedCategory !== 'ALL') && filteredConfigs.length === 0"
        size="card"
        title="未找到匹配的配置项"
        desc="尝试调整搜索条件或筛选器"
      >
        <template #actions>
          <button @click="clearFilters" class="clear-filters-btn">清除筛选条件</button>
        </template>
      </EmptyBox>

      <!-- 无数据状态（仅在没有任何筛选条件时显示“暂无配置数据”） -->
      <EmptyBox
        v-if="!loading && !(searchKeyword && searchKeyword.trim()) && selectedCategory === 'ALL' && configList.length === 0"
        size="card"
        title="暂无配置数据"
        desc="系统中还没有任何配置参数"
      />
    </el-card>

    <!-- 新增配置对话框 -->
    <el-dialog v-model="createDialogVisible" title="新增系统配置" width="560px">
      <el-form :model="createForm" label-width="110px" label-position="left">
        <el-form-item label="配置键 (key)" required>
          <el-input v-model="createForm.configKey" placeholder="示例：system.app.name" />
        </el-form-item>

        <el-form-item label="数据类型" required>
          <el-select v-model="createForm.configType" placeholder="选择类型" @change="onCreateTypeChange">
            <el-option v-for="opt in typeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="配置分类" required>
          <el-select v-model="createForm.configCategoryCode" placeholder="选择分类">
            <el-option v-for="opt in categorySelectOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="配置值 (value)" required>
          <template v-if="createForm.configType === 'BOOLEAN'">
            <el-switch v-model="createFormBool" />
          </template>
          <template v-else-if="createForm.configType === 'NUMBER'">
            <el-input v-model.number="createFormNumber" type="number" placeholder="请输入数字" />
          </template>
          <template v-else-if="createForm.configType === 'JSON'">
            <el-input v-model="createForm.configValue" type="textarea" :rows="4" placeholder='JSON，例如 {"k":"v"}' />
          </template>
          <template v-else>
            <el-input v-model="createForm.configValue" placeholder="请输入文本" />
          </template>
        </el-form-item>

        <el-form-item label="描述">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="配置用途说明" />
        </el-form-item>

        <el-form-item label="状态">
          <el-switch v-model="createFormStatus" />
        </el-form-item>

        <el-form-item label="排序">
          <el-input v-model.number="createForm.sortOrder" type="number" placeholder="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeCreateDialog">取 消</el-button>
          <el-button type="primary" :loading="createLoading" @click="submitCreate">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getConfigList, updateConfig, createConfig, batchDeleteConfigs } from '@/api/system/config'
import { getConfigCategories } from '@/api/system/dict'
import AppBreadcrumb from '@/components/common/Breadcrumb.vue'
import EmptyBox from '@/components/common/EmptyBox.vue'

export default {
  name: 'ConfigPage',
  components: { AppBreadcrumb, EmptyBox },
  setup() {
    // 响应式数据
    const loading = ref(false)
    const loadingMore = ref(false)
    const configList = ref([])
    const pageNum = ref(1)
    const pageSize = ref(12)
    const total = ref(0)
    const hasMore = ref(true)
    const selectedIds = ref(new Set())
    const deleting = ref(false)
    const searchKeyword = ref('')
    const selectedCategory = ref('ALL') // 存储分类编码（如：DICT_1.8），ALL 表示全部

    // 分类选项与可用分类
    // categoryOptions: [{ value: 'DICT_1.8', category: 'SYSTEM', label: '系统配置' }, ...]
    // value: 字典码；category: 业务分类名（如 SYSTEM/USER）；label: 中文名称
    const categoryOptions = ref([{ value: 'ALL', category: 'ALL', label: '全部' }])
    const categoryCounts = ref({ ALL: 0 })
    const categorySelectOptions = computed(() => categoryOptions.value
      .filter(opt => opt.value !== 'ALL')
      .map(opt => ({ value: opt.value, label: opt.label })))
    const availableCategories = computed(() => {
      return categoryOptions.value.map(opt => {
        if (opt.value === 'ALL') {
          return { value: 'ALL', label: opt.label, count: Number(categoryCounts.value.ALL || 0) }
        }
        const count = Number(categoryCounts.value[opt.value] || 0)
        return { value: opt.value, label: opt.label, count }
      })
    })

    // 服务端分页，前端不再本地过滤
    const filteredConfigs = computed(() => configList.value)

    // 方法
    const buildQueryParams = () => {
      const params = {
        pageNum: pageNum.value,
        pageSize: pageSize.value
      }
      if (selectedCategory.value !== 'ALL') {
        // 传递字典码进行后端筛选
        params.configCategoryCode = selectedCategory.value
      }
      if (searchKeyword.value && searchKeyword.value.trim()) {
        params.configKey = searchKeyword.value.trim()
        params.description = searchKeyword.value.trim()
      }
      return params
    }

    const appendConfigs = (records) => {
      const mapped = records.map(config => ({
        ...config,
        id: String(config.id),
        tempValue: config.configType === 'NUMBER' ? Number(config.configValue) : config.configValue,
        isChanged: false,
        isSaving: false,
        isSaved: false
      }))
      configList.value = [...configList.value, ...mapped]
    }

    const loadConfigList = async (reset = false) => {
      if (reset) {
        pageNum.value = 1
        total.value = 0
        hasMore.value = true
        configList.value = []
        selectedIds.value.clear()
      }
      if (pageNum.value === 1) loading.value = true; else loadingMore.value = true
      try {
        const response = await getConfigList(buildQueryParams())
        if (response && response.code === 200) {
          const data = response.data
          let records = []
          let totalCount = 0
          if (data && typeof data === 'object' && Array.isArray(data.records)) {
            records = data.records
            totalCount = Number(data.total || 0)
          } else if (Array.isArray(data)) {
            records = data
            totalCount = data.length
          }
          appendConfigs(records)
          total.value = totalCount
          hasMore.value = configList.value.length < total.value
        } else {
          if (pageNum.value === 1) {
            configList.value = getTestData()
            total.value = configList.value.length
            hasMore.value = false
          }
        }
      } catch (error) {
        if (pageNum.value === 1) {
          console.error('获取配置列表失败:', error)
          ElMessage.error('获取配置列表失败')
          configList.value = getTestData()
          total.value = configList.value.length
          hasMore.value = false
        }
      } finally {
        loading.value = false
        loadingMore.value = false
      }
    }

    // 从后端获取固定分类统计（与分页无关）
    const loadCategoryCounts = async () => {
      try {
        // ALL 总数
        try {
          const respAll = await getConfigList({ pageNum: 1, pageSize: 1 })
          if (respAll && respAll.code === 200) {
            const data = respAll.data
            let totalCount = 0
            if (data && typeof data === 'object' && Array.isArray(data.records)) {
              totalCount = Number(data.total || 0)
            } else if (Array.isArray(data)) {
              totalCount = data.length
            }
            categoryCounts.value = { ...categoryCounts.value, ALL: totalCount }
          }
        } catch (e) {
          // 忽略，保持原值
        }

        // 分类别统计（按分类编码）
        const opts = categoryOptions.value.filter(o => o.value !== 'ALL')
        const promises = opts.map(async (opt) => {
          try {
            const resp = await getConfigList({ pageNum: 1, pageSize: 1, configCategoryCode: opt.value })
            if (resp && resp.code === 200) {
              const data = resp.data
              let totalCount = 0
              if (data && typeof data === 'object' && Array.isArray(data.records)) {
                totalCount = Number(data.total || 0)
              } else if (Array.isArray(data)) {
                totalCount = data.length
              }
              categoryCounts.value = { ...categoryCounts.value, [opt.value]: totalCount }
            } else {
              categoryCounts.value = { ...categoryCounts.value, [opt.value]: 0 }
            }
          } catch (e) {
            categoryCounts.value = { ...categoryCounts.value, [opt.value]: 0 }
          }
        })
        await Promise.all(promises)
      } catch (e) {
        // 忽略统计失败，UI仍可工作
      }
    }

    const loadMore = async () => {
      if (!hasMore.value || loading.value || loadingMore.value) return
      pageNum.value += 1
      await loadConfigList(false)
    }
    const toggleSelect = (id, checked) => {
      const sid = String(id)
      if (checked) {
        selectedIds.value.add(sid)
      } else {
        selectedIds.value.delete(sid)
      }
    }

    const confirmBatchDelete = async () => {
      if (selectedIds.value.size === 0 || deleting.value) return
      try {
        deleting.value = true
        const ids = Array.from(selectedIds.value)
        const resp = await batchDeleteConfigs(ids)
        if (resp && resp.code === 200) {
          ElMessage.success(`删除成功，已删除 ${resp.data} 项`)
          await Promise.all([loadConfigList(true), loadCategoryCounts()])
        } else {
          throw new Error(resp?.message || '批量删除失败')
        }
      } catch (e) {
        console.error('批量删除失败', e)
        ElMessage.error(e.message || '批量删除失败')
      } finally {
        deleting.value = false
      }
    }

    // 加载分类选项（来源于字典 DICT_1 子项），并映射成：code + 业务分类名
    const loadCategoryOptions = async () => {
      try {
        const response = await getConfigCategories()
        if (response && response.code === 200 && Array.isArray(response.data)) {
          const opts = response.data.map(item => {
            const label = item.label || item.value || item.code
            // 后端字典将业务值放在 value（如 SYSTEM/USER/FILE/TASK/SECURITY/CACHE）
            const category = (item.value || '').toUpperCase()
            return { value: item.code, category, label }
          })
          categoryOptions.value = [{ value: 'ALL', category: 'ALL', label: '全部' }, ...opts]
          // 加载分类统计（固定值，不随分页变化）
          loadCategoryCounts()
        }
      } catch (e) {
        console.warn('加载配置分类失败', e)
      }
    }

    let filterTimer = null
    const filterConfigs = () => {
      if (filterTimer) clearTimeout(filterTimer)
      filterTimer = setTimeout(() => {
        loadConfigList(true)
      }, 300)
    }

    const clearSearch = () => {
      searchKeyword.value = ''
      loadConfigList(true)
      // 切换分类不刷新统计（统计应固定）；如需强制刷新，可调用 loadCategoryCounts()
    }

    const setSelectedCategory = (categoryCode) => {
      if (selectedCategory.value === categoryCode) return
      selectedCategory.value = categoryCode
      loadConfigList(true)
    }

    const clearFilters = () => {
      searchKeyword.value = ''
      selectedCategory.value = 'ALL'
      loadConfigList(true)
    }

    const onConfigChange = (config) => {
      config.isChanged = String(config.tempValue) !== String(config.configValue)
      config.isSaved = false
    }

    const updateBooleanValue = async (config, value) => {
      config.tempValue = value.toString()
      onConfigChange(config)
      // 布尔值修改后立即自动保存
      await autoSaveConfig(config)
    }

    // 输入框失去焦点时的处理
    const onInputBlur = async (config) => {
      if (config.isChanged && !config.isSaving) {
        await autoSaveConfig(config)
      }
    }

    // 自动保存配置
    const autoSaveConfig = async (config) => {
      if (!config.isChanged || config.isSaving) {
        return
      }
      await saveConfig(config)
    }

    const saveConfig = async (config) => {
      try {
        config.isSaving = true
        
        // 调用API更新配置
        const updateData = {
          configValue: config.tempValue.toString()
        }
        
        const response = await updateConfig(config.id, updateData)
        
        if (response && response.code === 200) {
          // 更新配置值
          config.configValue = config.tempValue
          config.isChanged = false
          config.isSaved = true
          config.updateTime = new Date().toISOString()
          
          ElMessage.success('配置保存成功')
          
          // 3秒后清除已保存状态
          setTimeout(() => {
            config.isSaved = false
          }, 3000)
        } else {
          throw new Error(response?.message || '保存失败')
        }
      } catch (error) {
        console.error('保存配置失败:', error)
        ElMessage.error(error.message || '保存配置失败')
      } finally {
        config.isSaving = false
      }
    }

    // 新增配置：对话框与表单
    const createDialogVisible = ref(false)
    const createLoading = ref(false)
    const typeOptions = ref([
      { value: 'STRING', label: '字符串', code: 'DICT_1.2' },
      { value: 'NUMBER', label: '数字', code: 'DICT_1.3' },
      { value: 'BOOLEAN', label: '布尔', code: 'DICT_1.4' },
      { value: 'JSON', label: 'JSON', code: 'DICT_1.5' }
    ])
    const createForm = ref({
      configKey: '',
      configValue: '',
      configType: 'STRING',
      configTypeCode: 'DICT_1.2',
      configCategory: '',
      configCategoryCode: '',
      description: '',
      status: 1,
      sortOrder: 0
    })

    const createFormBool = computed({
      get: () => createForm.value.configValue === 'true',
      set: (v) => { createForm.value.configValue = v ? 'true' : 'false' }
    })
    const createFormStatus = computed({
      get: () => createForm.value.status === 1,
      set: (v) => { createForm.value.status = v ? 1 : 0 }
    })
    const createFormNumber = computed({
      get: () => createForm.value.configValue,
      set: (v) => { createForm.value.configValue = v !== null && v !== undefined ? String(v) : '' }
    })

    const openCreateDialog = () => {
      resetCreateForm()
      if (selectedCategory.value !== 'ALL') {
        const cat = categoryOptions.value.find(o => o.value === selectedCategory.value)
        createForm.value.configCategoryCode = cat ? cat.value : ''
        createForm.value.configCategory = cat ? cat.category : ''
      }
      createDialogVisible.value = true
    }
    const closeCreateDialog = () => {
      createDialogVisible.value = false
    }
    const resetCreateForm = () => {
      createForm.value = {
        configKey: '',
        configValue: '',
        configType: 'STRING',
        configTypeCode: 'DICT_1.2',
        configCategory: '',
        configCategoryCode: '',
        description: '',
        status: 1,
        sortOrder: 0
      }
    }
    const onCreateTypeChange = (type) => {
      const opt = typeOptions.value.find(o => o.value === type)
      createForm.value.configTypeCode = opt ? opt.code : null
      if (type === 'BOOLEAN') {
        createForm.value.configValue = 'false'
      } else if (type === 'NUMBER') {
        createForm.value.configValue = '0'
      } else if (type === 'JSON') {
        createForm.value.configValue = ''
      } else {
        createForm.value.configValue = ''
      }
    }
    const submitCreate = async () => {
      const key = (createForm.value.configKey || '').trim()
      const keyPattern = /^[a-z][a-z0-9_.]*$/
      if (!key || !keyPattern.test(key)) {
        ElMessage.error('配置键不能为空，且需小写字母开头，仅含字母/数字/下划线/点号')
        return
      }
      if (!createForm.value.configType) {
        ElMessage.error('请选择配置类型')
        return
      }
      if (!createForm.value.configCategoryCode) {
        ElMessage.error('请选择配置分类')
        return
      }
      if (createForm.value.configValue === '' || createForm.value.configValue === null || createForm.value.configValue === undefined) {
        ElMessage.error('请填写配置值')
        return
      }

      const catOpt = categoryOptions.value.find(o => o.value === createForm.value.configCategoryCode)
      createForm.value.configCategory = catOpt ? catOpt.category : createForm.value.configCategoryCode

      createLoading.value = true
      try {
        const resp = await createConfig({
          configKey: createForm.value.configKey,
          configValue: createForm.value.configValue,
          configType: createForm.value.configType,
          configTypeCode: createForm.value.configTypeCode,
          configCategory: createForm.value.configCategory,
          configCategoryCode: createForm.value.configCategoryCode,
          description: createForm.value.description,
          status: createForm.value.status,
          sortOrder: createForm.value.sortOrder
        })
        if (resp && resp.code === 200) {
          ElMessage.success('新增配置成功')
          createDialogVisible.value = false
          await Promise.all([loadConfigList(true), loadCategoryCounts()])
        } else {
          throw new Error(resp?.message || '新增配置失败')
        }
      } catch (e) {
        console.error('新增配置失败:', e)
        ElMessage.error(e.message || '新增配置失败')
      } finally {
        createLoading.value = false
      }
    }

    // 工具函数
    const getCategoryName = (category) => {
      const names = {
        'SYSTEM': '系统配置',
        'USER': '用户配置',
        'FILE': '文件配置', 
        'TASK': '任务配置',
        'SECURITY': '安全配置',
        'CACHE': '缓存配置'
      }
      return names[category] || '其他配置'
    }

    const getCategoryDescription = (category) => {
      const descriptions = {
        'SYSTEM': '系统核心运行参数和基础配置',
        'USER': '用户相关的配置选项和权限设置',
        'FILE': '文件上传、存储和处理相关配置',
        'TASK': '任务队列和异步处理相关配置',
        'SECURITY': '安全策略和验证相关配置',
        'CACHE': '缓存系统和性能优化配置'
      }
      return descriptions[category] || '其他系统配置选项'
    }

    const formatTime = (time) => {
      if (!time) return ''
      return new Date(time).toLocaleString('zh-CN')
    }
    
    // 获取测试数据
    const getTestData = () => {
      return [
        {
          id: 1,
          configKey: 'system.app.name',
          configValue: '医疗影像模型管理平台',
          configType: 'STRING',
          configCategory: 'SYSTEM',
          description: '应用程序名称，用于显示在页面标题和导航栏中',
          status: 1,
          sort: 1,
          createBy: 'admin',
          updateTime: new Date().toISOString(),
          tempValue: '医疗影像模型管理平台',
          isChanged: false,
          isSaving: false,
          isSaved: false
        },
        {
          id: 2,
          configKey: 'system.app.version',
          configValue: '1.0.0',
          configType: 'STRING',
          configCategory: 'SYSTEM',
          description: '应用程序版本号',
          status: 1,
          sort: 2,
          createBy: 'admin',
          updateTime: new Date().toISOString(),
          tempValue: '1.0.0',
          isChanged: false,
          isSaving: false,
          isSaved: false
        },
        {
          id: 3,
          configKey: 'system.debug.enabled',
          configValue: 'false',
          configType: 'BOOLEAN',
          configCategory: 'SYSTEM',
          description: '是否启用调试模式，开启后会显示详细的错误信息',
          status: 1,
          sort: 3,
          createBy: 'admin',
          updateTime: new Date().toISOString(),
          tempValue: 'false',
          isChanged: false,
          isSaving: false,
          isSaved: false
        },
        {
          id: 4,
          configKey: 'user.default.role',
          configValue: 'USER',
          configType: 'STRING',
          configCategory: 'USER',
          description: '新用户注册时的默认角色',
          status: 1,
          sort: 1,
          createBy: 'admin',
          updateTime: new Date().toISOString(),
          tempValue: 'USER',
          isChanged: false,
          isSaving: false,
          isSaved: false
        },
        {
          id: 5,
          configKey: 'user.session.timeout',
          configValue: '30',
          configType: 'NUMBER',
          configCategory: 'USER',
          description: '用户会话超时时间（分钟）',
          status: 1,
          sort: 2,
          createBy: 'admin',
          updateTime: new Date().toISOString(),
          tempValue: 30,
          isChanged: false,
          isSaving: false,
          isSaved: false
        },
        {
          id: 6,
          configKey: 'file.upload.max.size',
          configValue: '10',
          configType: 'NUMBER',
          configCategory: 'FILE',
          description: '文件上传最大大小限制（MB）',
          status: 1,
          sort: 1,
          createBy: 'admin',
          updateTime: new Date().toISOString(),
          tempValue: 10,
          isChanged: false,
          isSaving: false,
          isSaved: false
        },
        {
          id: 7,
          configKey: 'file.upload.allowed.types',
          configValue: '["jpg", "jpeg", "png", "pdf", "dcm"]',
          configType: 'JSON',
          configCategory: 'FILE',
          description: '允许上传的文件类型列表',
          status: 1,
          sort: 2,
          createBy: 'admin',
          updateTime: new Date().toISOString(),
          tempValue: '["jpg", "jpeg", "png", "pdf", "dcm"]',
          isChanged: false,
          isSaving: false,
          isSaved: false
        },
        {
          id: 8,
          configKey: 'task.max.concurrent',
          configValue: '5',
          configType: 'NUMBER',
          configCategory: 'TASK',
          description: '任务队列最大并发数',
          status: 1,
          sort: 1,
          createBy: 'admin',
          updateTime: new Date().toISOString(),
          tempValue: 5,
          isChanged: false,
          isSaving: false,
          isSaved: false
        },
        {
          id: 9,
          configKey: 'security.password.min.length',
          configValue: '8',
          configType: 'NUMBER',
          configCategory: 'SECURITY',
          description: '密码最小长度要求',
          status: 1,
          sort: 1,
          createBy: 'admin',
          updateTime: new Date().toISOString(),
          tempValue: 8,
          isChanged: false,
          isSaving: false,
          isSaved: false
        },
        {
          id: 10,
          configKey: 'cache.redis.enabled',
          configValue: 'true',
          configType: 'BOOLEAN',
          configCategory: 'CACHE',
          description: '是否启用Redis缓存',
          status: 1,
          sort: 1,
          createBy: 'admin',
          updateTime: new Date().toISOString(),
          tempValue: 'true',
          isChanged: false,
          isSaving: false,
          isSaved: false
        }
      ]
    }

    // 生命周期
    const onScroll = () => {
      const scrollTop = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop
      const windowHeight = window.innerHeight || document.documentElement.clientHeight
      const docHeight = Math.max(document.body.scrollHeight, document.documentElement.scrollHeight)
      if (scrollTop + windowHeight >= docHeight - 200) {
        loadMore()
      }
    }

    onMounted(() => {
      loadCategoryOptions()
      loadConfigList(true)
      window.addEventListener('scroll', onScroll, { passive: true })
    })

    onUnmounted(() => {
      window.removeEventListener('scroll', onScroll)
    })

    return {
      // 响应式数据
      loading,
      configList,
      searchKeyword,
      selectedCategory,
      
      // 计算属性
      availableCategories,
      filteredConfigs,
      categorySelectOptions,
      categoryCounts,
      
      // 方法
      loadConfigList,
      loadCategoryCounts,
      loadMore,
      hasMore,
      loadingMore,
      pageNum,
      pageSize,
      total,
      filterConfigs,
      clearSearch,
      setSelectedCategory,
      clearFilters,
      onConfigChange,
      updateBooleanValue,
      onInputBlur,
      autoSaveConfig,
      saveConfig,
      // 选择删除
      selectedIds,
      deleting,
      toggleSelect,
      confirmBatchDelete,
      // 创建
      createDialogVisible,
      createLoading,
      typeOptions,
      createForm,
      createFormBool,
      createFormStatus,
      createFormNumber,
      openCreateDialog,
      closeCreateDialog,
      submitCreate,
      onCreateTypeChange,
      getCategoryName,
      getCategoryDescription,
      categoryOptions,
      loadCategoryOptions,
      formatTime,
      getTestData
    }
  }
}
</script>

<style scoped>
/* 现代化卡片式配置页面样式 */
.config-page {
  padding: 8px 0 12px;
  background: transparent;
}



.refresh-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #409eff;
  color: white;
  border: 1px solid #409eff;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.refresh-btn:hover {
  background: #337ecc;
  border-color: #337ecc;
}

.danger-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #ff4d4f;
  color: white;
  border: 1px solid #ff4d4f;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.danger-btn:hover {
  background: #d9363e;
  border-color: #d9363e;
}

/* 筛选工具栏 */
.filter-toolbar {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: none;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.search-row {
  display: flex;
  justify-content: flex-start;
  width: 100%;
}

.filter-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  gap: 20px;
}

.toolbar-actions {
  display: flex;
  gap: 12px;
  margin-left: auto;
}

.toolbar-actions .el-button {
  background-color: white;
}

.search-section {
  flex-shrink: 0;
}

.search-input-wrapper {
  position: relative;
  max-width: 400px;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #6b7280;
  pointer-events: none;
}

.search-input {
  width: 100%;
  max-width: 250px;
  padding: 12px 16px 12px 44px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  background-color: #ffffff;
  transition: all 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  background-color: #ffffff;
}

.clear-search {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #6b7280;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
}

.clear-search:hover {
  color: #374151;
  background-color: #f3f4f6;
}

/* 分类筛选 */
.category-filter {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  flex: 1;
  min-width: 200px;
}

.filter-label {
  font-weight: 500;
  color: #374151;
  font-size: 14px;
}

.category-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.category-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  background: #f9fafb;
  color: #6b7280;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}

.category-tab:hover {
  border-color: #409eff;
  color: #409eff;
}

.category-tab.active {
  background: #409eff;
  color: white;
  border-color: #409eff;
}

.category-count {
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
}

.category-tab.active .category-count {
  background: rgba(255, 255, 255, 0.3);
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #6a737d;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #f3f4f6;
  border-top: 3px solid #0366d6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 配置项网格布局 */
.config-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 24px;
}

/* 大屏幕优化 */
@media (min-width: 1920px) {
  .config-grid {
    grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
    gap: 28px;
  }
}

/* 配置卡片 */
.config-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: none;
  border: 1px solid #f1f3f4;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.config-card.selected {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.15);
}

.select-checkbox {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  z-index: 2;
}

.select-checkbox input {
  appearance: none;
  width: 16px;
  height: 16px;
}

.checkbox-mark {
  width: 16px;
  height: 16px;
  border: 1px solid #cbd5e1;
  border-radius: 3px;
  background: #fff;
  display: inline-block;
}

.select-checkbox input:checked + .checkbox-mark {
  background: #409eff;
  border-color: #409eff;
  position: relative;
}

.select-checkbox input:checked + .checkbox-mark::after {
  content: '';
  position: absolute;
  left: 4px;
  top: 0px;
  width: 5px;
  height: 9px;
  border: solid #fff;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.config-card:hover {
  transform: translateY(-4px);
}

.config-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}



/* .config-card.changed {
  border-left: 4px solid #f59e0b;
} */

.config-card.saving {
  border-left: 4px solid #3b82f6;
}

.config-card.saved {
  border-left: 4px solid #10b981;
}

/* 卡片头部 */
.card-header {
  position: relative;
  margin-bottom: 12px;
}



.config-actions {
  display: flex;
  align-items: center;
}

.save-button {
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 6px 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.save-button:hover {
  background: #2563eb;
}

.saving-indicator {
  color: #3b82f6;
  display: flex;
  align-items: center;
}

.saved-indicator {
  color: #10b981;
  display: flex;
  align-items: center;
}

/* 配置项内容 */
.config-description {
  font-size: 15px;
  color: #1f2937;
  line-height: 1.5;
  margin-bottom: 8px;
  font-weight: 600;
}

.config-key {
  font-family: 'SFMono-Regular', 'Consolas', 'Liberation Mono', 'Menlo', monospace;
  font-size: 12px;
  font-weight: 500;
  color: #6b7280;
  background: #f3f4f6;
  padding: 6px 10px;
  border-radius: 6px;
  margin-bottom: 16px;
  word-break: break-all;
  border: 1px solid #e5e7eb;
}

/* 配置值区域 */
.config-value-section {
  margin-bottom: 16px;
  padding: 12px;
  background: #fafbfc;
  border-radius: 6px;
  border: 1px solid #f0f2f5;
}

.value-label {
  font-size: 13px;
  font-weight: 600;
  color: #4b5563;
  margin-bottom: 8px;
}

.value-input {
  width: 100%;
}

/* 卡片底部 */
.card-footer {
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
  background: rgba(249, 250, 251, 0.5);
  margin: 16px -20px -20px;
  padding: 12px 20px;
  border-radius: 0 0 8px 8px;
}

.update-time {
  font-size: 11px;
  color: #9ca3af;
}

.mini-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid #e5e7eb;
  border-top: 2px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}



/* 输入框样式 */
.boolean-control {
  display: flex;
  align-items: center;
  gap: 10px;
}

.number-input {
  width: 150px;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 14px;
  background: #ffffff;
  transition: all 0.2s;
}

.text-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 14px;
  background: #ffffff;
  transition: all 0.2s;
}

.number-input:focus,
.text-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.json-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 13px;
  font-family: 'SFMono-Regular', 'Consolas', 'Liberation Mono', 'Menlo', monospace;
  background: #ffffff;
  transition: all 0.2s;
  resize: vertical;
  min-height: 60px;
}

.json-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ff4d4f;
  transition: 0.2s;
  border-radius: 24px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.2s;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

input:checked + .slider {
  background-color: #52c41a;
}

input:checked + .slider:before {
  transform: translateX(20px);
}

.boolean-text {
  font-size: 14px;
  color: #24292e;
  font-weight: 500;
}





/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
  background: white;
  border-radius: 12px;
  margin-top: 20px;
}

.empty-icon {
  margin-bottom: 16px;
  color: #d1d5db;
}

.empty-state h3 {
  font-size: 18px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 8px 0;
}

.empty-state p {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 16px 0;
}

.clear-filters-btn {
  padding: 8px 16px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.clear-filters-btn:hover {
  background: #2563eb;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .config-grid {
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .config-page {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 20px;
  }
  
  .filter-toolbar {
    padding: 16px;
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  
  .search-section {
    order: 1;
  }
  
  .category-filter {
    order: 2;
    flex-direction: column;
    align-items: flex-start;
  }
  
  .toolbar-actions {
    order: 3;
    margin-left: 0;
    justify-content: flex-end;
  }
  
  .config-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .config-card {
    padding: 16px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f6f8fa;
}

::-webkit-scrollbar-thumb {
  background: #d1d5da;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #959da5;
}
</style> 