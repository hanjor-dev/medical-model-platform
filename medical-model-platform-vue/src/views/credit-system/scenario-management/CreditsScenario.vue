<template>
  <div class="scenario-management-page">
    <div class="page-content">
      <el-card class="list-card">
        <template #header>
          <div class="list-header">
            <AppBreadcrumb />
          </div>
        </template>
        <!-- 筛选区 -->
        <div class="filter-bar">
          <el-form :inline="true" :model="query" class="filter-form">
            <el-form-item label="关键词">
              <el-input v-model="query.keyword" placeholder="编码/名称/描述" clearable @keyup.enter="handleSearch" style="width: 220px;" />
            </el-form-item>
            <el-form-item label="积分类型">
              <div class="colored-select" :style="{ '--type-color': selectedFilterTypeColor }">
                <el-select v-model="query.creditTypeCode" placeholder="全部" clearable style="width: 180px;">
                  <el-option
                    v-for="t in typeOptionsAll"
                    :key="t.typeCode"
                    :label="t.typeName"
                    :value="t.typeCode"
                    :style="{ color: getTypeColor(t) }"
                  >
                    <span :style="{ color: getTypeColor(t) }">{{ t.typeName }}<span v-if="Number(t.status) === 0" style="color:#909399">（已禁用）</span></span>
                  </el-option>
                </el-select>
              </div>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px;">
                <el-option :value="1" label="启用" />
                <el-option :value="0" label="禁用" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
            <div class="flex-fill" />
            <el-form-item>
              <el-button type="primary" @click="openCreate" v-role="'SUPER_ADMIN'">新建使用场景</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 表格区 -->
        <div class="table-area">
        <el-table :data="tableData" v-loading="loading" border fit style="width: 100%;" :header-cell-style="{ background: '#f5f7fa' }" height="100%">
          <el-table-column type="index" label="编号" width="70" :index="indexMethod" />
          <el-table-column prop="scenarioCode" label="编码" min-width="140" />
          <el-table-column prop="scenarioName" label="名称" min-width="180">
            <template #default="{ row }">
              <span class="name-cell">
                <span class="name-text">{{ row.scenarioName }}</span>
                <el-tooltip v-if="row.description" :content="row.description" placement="top">
                  <el-icon class="desc-icon" :size="16"><QuestionFilled /></el-icon>
                </el-tooltip>
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="creditTypeCode" label="积分类型" min-width="120">
            <template #default="{ row }">
              <span :style="{ color: getColorByCode(row.creditTypeCode) }">
                {{ typeMap[row.creditTypeCode] || row.creditTypeCode }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="每次积分" min-width="120">
            <template #default="{ row }">
              <span :style="{ color: Number(row.costPerUse) >= 0 ? '#f56c6c' : '#67c23a' }">
                {{ Number(row.costPerUse) >= 0 ? '-' : '+' }}{{ Math.abs(Number(row.costPerUse || 0)) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="每日限额" width="110">
            <template #default="{ row }">
              <span>{{ (row.dailyLimit === null || row.dailyLimit === undefined || row.dailyLimit === '') ? '无限额' : row.dailyLimit }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-switch :model-value="row.status === 1"
                         @change="val => handleToggleStatus(row, val)"
                         :style="{ '--el-switch-on-color': '#67c23a', '--el-switch-off-color': '#f56c6c' }" />
            </template>
          </el-table-column>
          <el-table-column prop="updateTime" label="更新时间" min-width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.updateTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="200" class-name="op-col">
            <template #default="{ row }">
              <el-button class="op-btn" type="primary" text @click="openEdit(row)">编辑</el-button>
              <el-button class="op-btn" type="danger" text @click="handleDelete(row)" v-role="'SUPER_ADMIN'">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pager-bar">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            @current-change="loadData"
            @size-change="handleSizeChange"
          />
        </div>
        </div>
      </el-card>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog class="scenario-dialog" :title="formModel.id ? '编辑积分场景' : '创建积分场景'" v-model="dialogVisible" width="560px" destroy-on-close>

      <el-form ref="formRef" :model="formModel" :rules="rules" label-width="108px" class="scenario-form">
        <el-divider content-position="left">基础信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="场景编码" prop="scenarioCode">
              <div class="field-line">
                <el-input v-model="formModel.scenarioCode" placeholder="如 READING、AI_COMPUTE" :disabled="!!formModel.id" @input="handleCodeInput">
                  <template #suffix>
                    <el-tooltip content="自动转大写，允许 A-Z / 0-9 / _" placement="top">
                      <el-icon class="help-icon"><QuestionFilled /></el-icon>
                    </el-tooltip>
                  </template>
                </el-input>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="场景名称" prop="scenarioName">
              <el-input v-model="formModel.scenarioName" placeholder="展示用名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="积分类型" prop="creditTypeCode">
              <div class="colored-select" :style="{ '--type-color': selectedFormTypeColor }">
                <el-select v-model="formModel.creditTypeCode" placeholder="请选择">
                  <el-option
                    v-for="t in typeOptionsEnabled"
                    :key="t.typeCode"
                    :label="t.typeName"
                    :value="t.typeCode"
                    :style="{ color: getTypeColor(t) }"
                  >
                    <span :style="{ color: getTypeColor(t) }">{{ t.typeName }}</span>
                  </el-option>
                </el-select>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="状态" prop="status">
              <el-switch v-model="statusEnabled" :style="{ '--el-switch-on-color': '#67c23a', '--el-switch-off-color': '#f56c6c' }" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">规则与限制</el-divider>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="每日限额" prop="limitMode">
              <div class="field-line">
                <el-radio-group v-model="formModel.limitMode">
                  <el-radio-button label="unlimited">不限额</el-radio-button>
                  <el-radio-button label="limited">限额</el-radio-button>
                </el-radio-group>
                <el-input-number
                  v-if="formModel.limitMode === 'limited'"
                  class="limit-input ml12"
                  v-model="formModel.limitValue"
                  :min="0"
                  :max="999999"
                  :step="1"
                  controls-position="right"
                  :style="{ width: '120px' }"
                />
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="每次积分" prop="costPerUse">
              <div class="field-line">
                <el-input-number v-model="formModel.costPerUse" :step="1" :precision="2" controls-position="right" :style="{ width: '160px' }" />
                <span class="unit ml8"> 积分/次（正数=消耗，负数=奖励）</span>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">描述</el-divider>
        <el-form-item label="" prop="description">
          <el-input type="textarea" v-model="formModel.description" :rows="4" maxlength="100" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="sticky-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAndSubmit()" :loading="saving">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { defineComponent, reactive, ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { QuestionFilled } from '@element-plus/icons-vue'
import AppBreadcrumb from '@/components/common/Breadcrumb.vue'
import { creditScenarioApi, creditTypeApi } from '@/api/credit-system'

export default defineComponent({
  name: 'ScenarioManagement',
  components: { QuestionFilled, AppBreadcrumb },
  setup() {
    const loading = ref(false)
    const saving = ref(false)
    const tableData = ref([])
    const total = ref(0)
    const indexMethod = (index) => {
      const base = (query.pageNum - 1) * query.pageSize
      return base + index + 1
    }

    const typeOptionsEnabled = ref([])
    const typeOptionsAll = ref([])
    const typeMap = reactive({})
    const typeColorMap = reactive({})

    const query = reactive({
      keyword: '',
      creditTypeCode: undefined,
      status: undefined,
      pageNum: 1,
      pageSize: 10
    })

    const dialogVisible = ref(false)
    const formRef = ref()
    const formModel = reactive({
      id: undefined,
      scenarioCode: '',
      scenarioName: '',
      creditTypeCode: '',
      costPerUse: 1.00, // 正数=消耗，负数=奖励
      dailyLimit: null,
      // 新交互：每日限额模式与值
      limitMode: 'unlimited', // unlimited | limited
      limitValue: 0,
      userRoles: 'USER,ADMIN,SUPER_ADMIN',
      status: 1,
      sortOrder: 1,
      description: ''
    })

    // 角色固定为全角色，页面不再展示角色配置

    const rules = {
      scenarioCode: [
        { required: true, message: '请输入场景编码', trigger: 'blur' },
        { min: 1, max: 50, message: '长度 1-50', trigger: 'blur' }
      ],
      scenarioName: [
        { required: true, message: '请输入场景名称', trigger: 'blur' }
      ],
      creditTypeCode: [
        { required: true, message: '请选择积分类型', trigger: 'change' }
      ],
      costPerUse: [
        { required: true, message: '请输入每次积分（可为负）', trigger: 'change' }
      ],
      limitMode: [
        {
          validator: (rule, value, callback) => {
            if (formModel.limitMode === 'limited') {
              const v = Number(formModel.limitValue)
              if (!(v >= 0)) return callback(new Error('请输入有效的每日限额'))
            }
            callback()
          },
          trigger: 'change'
        }
      ],
      status: [
        { required: true, message: '请选择状态', trigger: 'change' }
      ]
    }

    // 状态开关映射
    const statusEnabled = computed({
      get: () => Number(formModel.status) === 1,
      set: (val) => { formModel.status = val ? 1 : 0 }
    })

    // 无

    const handleCodeInput = (val) => {
      const v = String(val || '').toUpperCase().replace(/[^A-Z0-9_]/g, '')
      formModel.scenarioCode = v
    }

    const normalizeList = (data) => {
      if (!data) return { list: [], total: 0 }
      if (Array.isArray(data)) return { list: data, total: data.length }
      if (Array.isArray(data.records)) return { list: data.records, total: data.total || data.records.length }
      if (Array.isArray(data.items)) return { list: data.items, total: data.total || data.items.length }
      if (Array.isArray(data.list)) return { list: data.list, total: data.total || data.list.length }
      return { list: [], total: 0 }
    }

    const loadTypes = async () => {
      try {
        // 1) 创建/编辑使用：仅启用类型
        const respEnabled = await creditTypeApi.getCreditTypeList({ status: 1, pageNum: 1, pageSize: 999 })
        if (respEnabled.code === 200) {
          const { list } = normalizeList(respEnabled.data)
          typeOptionsEnabled.value = list
        }
        // 2) 查询过滤与字典映射：包含禁用类型
        const respAll = await creditTypeApi.getCreditTypeList({ pageNum: 1, pageSize: 999 })
        if (respAll.code === 200) {
          const { list: all } = normalizeList(respAll.data)
          typeOptionsAll.value = all
          Object.keys(typeMap).forEach(k => delete typeMap[k])
          Object.keys(typeColorMap).forEach(k => delete typeColorMap[k])
          all.forEach(t => {
            typeMap[t.typeCode] = t.typeName
            typeColorMap[t.typeCode] = t.colorCode || t.displayColor || t.color || '#000'
          })
        }
      } catch (e) {
        // ignore
      }
    }

    const loadData = async () => {
      loading.value = true
      try {
        const resp = await creditScenarioApi.getCreditScenarioList({
          keyword: query.keyword,
          creditTypeCode: query.creditTypeCode,
          status: query.status,
          pageNum: query.pageNum,
          pageSize: query.pageSize
        })
        if (resp.code === 200) {
          const { list, total: t } = normalizeList(resp.data)
          tableData.value = list
          total.value = Number(t || 0)
        } else {
          ElMessage.error(resp.message || '获取列表失败')
        }
      } catch (e) {
        ElMessage.error(e.message || '获取列表失败')
      } finally {
        loading.value = false
      }
    }

    const handleSearch = () => {
      query.pageNum = 1
      loadData()
    }

    const handleReset = () => {
      query.keyword = ''
      query.creditTypeCode = undefined
      query.status = undefined
      query.pageNum = 1
      query.pageSize = 10
      loadData()
    }

    const handleSizeChange = () => {
      query.pageNum = 1
      loadData()
    }

    const openCreate = () => {
      Object.assign(formModel, {
        id: undefined,
        scenarioCode: '',
        scenarioName: '',
        creditTypeCode: '',
        costPerUse: 1.00,
        dailyLimit: null,
        limitMode: 'unlimited',
        limitValue: 0,
        userRoles: 'USER,ADMIN,SUPER_ADMIN',
        status: 1,
        sortOrder: 1,
        description: ''
      })
      dialogVisible.value = true
    }

    const openEdit = (row) => {
      Object.assign(formModel, {
        id: row.id,
        scenarioCode: row.scenarioCode,
        scenarioName: row.scenarioName,
        creditTypeCode: row.creditTypeCode,
        costPerUse: Number(row.costPerUse ?? 0),
        dailyLimit: row.dailyLimit !== null && row.dailyLimit !== undefined && row.dailyLimit !== '' ? Number(row.dailyLimit) : null,
        userRoles: row.userRoles || '',
        status: Number(row.status ?? 1),
        sortOrder: Number(row.sortOrder ?? 0),
        description: row.description || ''
      })
      // 新交互：根据 dailyLimit 推断模式
      if (formModel.dailyLimit === null || formModel.dailyLimit === undefined || formModel.dailyLimit === '') {
        formModel.limitMode = 'unlimited'
        formModel.limitValue = 0
      } else {
        formModel.limitMode = 'limited'
        formModel.limitValue = Number(formModel.dailyLimit)
      }
      dialogVisible.value = true
    }

    const confirmAndSubmit = async () => {
      const v = Number(formModel.costPerUse)
      const message = (
        `<div class="warn-confirm">` +
        `<div class="warn-icon">⚠️</div>` +
        `<div class="warn-text">` +
        `<div><strong>请确认“每次积分”的正负值是否正确</strong></div>` +
        `<div style="margin-top:6px;color:#606266">正数表示消耗场景类型（扣分），负数表示奖励场景类型（加分）</div>` +
        `<div style="margin-top:8px;color:#d14343">当前填写值：${isNaN(v) ? '无效' : v}</div>` +
        `</div>` +
        `</div>`
      )
      try {
        await ElMessageBox({
          title: '风险提示',
          message,
          dangerouslyUseHTMLString: true,
          type: 'warning',
          customClass: 'top-warning-confirm',
          confirmButtonText: '确认提交',
          cancelButtonText: '返回修改',
          showCancelButton: true,
          center: false
        })
      } catch (_) {
        return
      }
      await handleSubmit()
    }

    const handleSubmit = async (continueCreate = false) => {
      formRef.value?.validate(async (valid) => {
        if (!valid) return
        try {
          saving.value = true
          const payload = { ...formModel }
          // 新交互：根据模式设置 dailyLimit
          payload.dailyLimit = formModel.limitMode === 'limited' ? Number(formModel.limitValue) : null
          // 固定默认：全部角色、排序=1
          payload.userRoles = 'USER,ADMIN,SUPER_ADMIN'
          payload.sortOrder = 1
          if (!payload.id) {
            const { scenarioCode, scenarioName, creditTypeCode, costPerUse, dailyLimit, userRoles, description, sortOrder } = payload
            const createPayload = { scenarioCode, scenarioName, creditTypeCode, costPerUse, dailyLimit, userRoles, description, sortOrder }
            const resp = await creditScenarioApi.createCreditScenario(createPayload)
            if (resp.code === 200) {
              ElMessage.success('创建成功')
              if (continueCreate) {
                openCreate()
              } else {
                dialogVisible.value = false
              }
              loadData()
            } else {
              ElMessage.error(resp.message || '创建失败')
            }
          } else {
            const { scenarioName, creditTypeCode, costPerUse, dailyLimit, userRoles, description, status, sortOrder } = payload
            const updatePayload = { scenarioName, creditTypeCode, costPerUse, dailyLimit, userRoles, description, status, sortOrder }
            const resp = await creditScenarioApi.updateCreditScenario(payload.id, updatePayload)
            if (resp.code === 200) {
              ElMessage.success('更新成功')
              dialogVisible.value = false
              loadData()
            } else {
              ElMessage.error(resp.message || '更新失败')
            }
          }
        } catch (e) {
          ElMessage.error(e.message || '保存失败')
        } finally {
          saving.value = false
        }
      })
    }

    const handleDelete = (row) => {
      ElMessageBox.confirm(`确认删除积分场景【${row.scenarioName || row.scenarioCode}】？`, '提示', { type: 'warning' })
        .then(async () => {
          try {
            const resp = await creditScenarioApi.deleteCreditScenario(row.id)
            if (resp.code === 200) {
              ElMessage.success('删除成功')
              loadData()
            } else {
              ElMessage.error(resp.message || '删除失败')
            }
          } catch (e) {
            ElMessage.error(e.message || '删除失败')
          }
        })
        .catch(() => {})
    }

    const handleToggleStatus = async (row, val) => {
      try {
        const resp = await creditScenarioApi.updateCreditScenario(row.id, { status: val ? 1 : 0 })
        if (resp.code === 200) {
          ElMessage.success('状态已更新')
          loadData()
        } else {
          ElMessage.error(resp.message || '状态更新失败')
        }
      } catch (e) {
        ElMessage.error(e.message || '状态更新失败')
      }
    }

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

    onMounted(async () => {
      await loadTypes()
      await loadData()
    })

    const getTypeColor = (t) => {
      if (!t) return '#000'
      return t.colorCode || t.displayColor || t.color || typeColorMap[t.typeCode] || '#000'
    }

    const getColorByCode = (code) => {
      if (!code) return '#000'
      return typeColorMap[code] || '#000'
    }

    const selectedFilterTypeColor = computed(() => {
      const code = query.creditTypeCode
      if (!code) return '#000'
      return typeColorMap[code] || '#000'
    })

    const selectedFormTypeColor = computed(() => {
      const code = formModel.creditTypeCode
      if (!code) return '#000'
      return typeColorMap[code] || '#000'
    })

    return {
      loading,
      tableData,
      total,
      query,
      dialogVisible,
      formRef,
      formModel,
      rules,
      saving,
      handleCodeInput,
      typeOptionsEnabled,
      typeOptionsAll,
      typeMap,
      statusEnabled,
      getTypeColor,
      getColorByCode,
      selectedFilterTypeColor,
      selectedFormTypeColor,
      handleSearch,
      handleReset,
      handleSizeChange,
      openCreate,
      openEdit,
      handleSubmit,
      confirmAndSubmit,
      handleDelete,
      handleToggleStatus,
      loadData,
      indexMethod,
      formatDateTime
    }
  }
})
</script>

<style lang="scss" scoped>
.scenario-management-page {
  padding: 8px 0 12px;
  display: flex;
  flex-direction: column;
  /* 首屏即占满可用高度 */
  min-height: calc(100vh - var(--app-header-height) - var(--main-padding-v));

  .page-content {
    flex: 1;
    display: flex;
    min-height: 0;
    .list-card {
      border-radius: 8px;
      box-shadow: none;
      flex: 1 1 auto;
      width: 100%;
      min-height: 100%;
      display: flex;
      flex-direction: column;
      :deep(.el-card__body) {
        flex: 1;
        display: flex;
        flex-direction: column;
        min-height: 0;
      }
    }
    .list-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .list-title {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      font-weight: 600;
      color: #303133;
    }
    .filter-bar { margin-bottom: 12px; }
    .pager-bar {
      display: flex;
      justify-content: flex-end;
      margin-top: 12px;
    }
    /* 表格区域伸展，分页固定 */
    .table-area {
      flex: 1 1 auto;
      display: flex;
      flex-direction: column;
      min-height: 0;
    }
    .flex-fill { flex: 1 1 auto; }
    .mr4 { margin-right: 4px; }
    .tip-text { margin-left: 8px; color: #909399; font-size: 12px; }
    .name-cell { display: inline-flex; align-items: center; gap: 6px; }
    .desc-icon { color: #909399; cursor: pointer; font-size: 16px; line-height: 1; display: inline-flex; }
    .op-col :deep(.cell) { font-size: 14px; }
    .op-btn { font-size: 14px; padding: 0 6px; }
    .preview-bar { margin-bottom: 12px; }
    .preview-content { font-size: 13px; display: flex; align-items: center; flex-wrap: wrap; gap: 6px; }
    .pv-name { font-weight: 600; color: #303133; }
    .pv-item { color: #606266; }
    .pv-sep { color: #dcdfe6; margin: 0 4px; }
    /* 顶部预览已移除 */
    .sticky-footer { display: flex; justify-content: center; gap: 8px; }
    .ml8 { margin-left: 8px; }
    .mr8 { margin-right: 8px; }
    .field-line { display: flex; align-items: center; flex-wrap: wrap; }
    .unit { color: #909399; }
    .muted { color: #909399; font-size: 12px; }
    .help-icon { color: var(--el-color-info); cursor: help; }
  }
  // 让选择器选中值按类型颜色显示（受控于 --type-color）
  .colored-select {
    // 选中值区域（ElSelect 的输入框文字）
    :deep(.el-select .el-input__inner),
    :deep(.el-select .el-input__inner::placeholder),
    :deep(.el-select .el-input__inner .el-input__inner) {
      color: var(--type-color, #000);
    }
    :deep(.el-input__inner) { color: var(--type-color, #000); }
    // 对于 Element Plus ≥2.7 内部结构兼容
    :deep(.el-select .el-select__selected-item) { color: var(--type-color, #000); }
  }
  // 弹窗中的表单在 .page-content 之外，这里将样式提到页面根作用域
  .scenario-form {
    :deep(.el-form-item) { margin-bottom: 16px; }
    :deep(.el-divider) { margin: 12px 0; border-color: #f0f2f5; }
    :deep(.el-divider__text.is-left) { font-weight: 500; color: #909399; font-size: 13px; }
    :deep(.el-form-item__label) { font-weight: 400; color: #303133; }

    // 控件宽度：单行输入、选择器定宽，文本域保持自适应
    :deep(.el-input),
    :deep(.el-select),
    :deep(.el-date-editor) {
      width: 360px;
    }
    // 让文本域计数靠近输入框：限制容器宽度而不是内层 textarea 宽度
    :deep(.el-textarea) { width: 360px; }
    .limit-input { margin-left: 12px; }
  }
  /* 将弹窗底部按钮置中（弹窗在 .page-content 之外，因此样式放在根作用域） */
  .sticky-footer { display: flex; justify-content: center; gap: 8px; width: 100%; }
  :deep(.scenario-dialog) .el-dialog__footer { text-align: center; }
  /* 弹窗宽度与响应式最大宽度，并保持左右留白对称 */
  :deep(.scenario-dialog) {
    max-width: calc(100vw - 40px);
    margin-left: auto;
    margin-right: auto;
  }
  
}
</style>
<style lang="scss">
/* 全局：Element Plus MessageBox 由于 teleported 到 body，因此需要非 scoped 样式 */
.el-overlay.is-message-box {
  display: flex;
  align-items: flex-start; /* 顶部对齐 */
  padding-top: 20px; /* 顶部间距 */
}

.el-message-box.top-warning-confirm {
  border-radius: 8px;
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.16);
}
.el-message-box.top-warning-confirm .el-message-box__header {
  background: #fff1f0;
  border-bottom: 1px solid #ffa39e;
}
.el-message-box.top-warning-confirm .el-message-box__title { color: #d4380d; font-weight: 600; }
.el-message-box.top-warning-confirm .el-message-box__content { padding-top: 12px; }
.el-message-box.top-warning-confirm .el-message-box__status { display: none; }
.el-message-box.top-warning-confirm .el-button--primary { background-color: #f5222d; border-color: #f5222d; }

/* 自定义 HTML 内容的样式 */
.el-message-box__message .warn-confirm { display: flex; align-items: flex-start; gap: 10px; }
.el-message-box__message .warn-icon { color: #fa541c; font-size: 22px; line-height: 1.2; }
.el-message-box__message .warn-text { color: #333; }
</style>