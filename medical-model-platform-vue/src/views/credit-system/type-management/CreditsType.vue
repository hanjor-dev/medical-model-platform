<template>
  <div class="type-management-page">
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
              <el-button type="primary" @click="openCreate" v-role="'SUPER_ADMIN'">新建积分类型</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 表格区 -->
        <div class="table-area">
        <el-table :data="tableData" v-loading="loading" border fit style="width: 100%;" :header-cell-style="{ background: '#f5f7fa' }" height="100%">
          <el-table-column type="index" label="编号" width="70" :index="indexMethod" />
          <el-table-column prop="typeCode" label="编码" min-width="140" />
          <el-table-column prop="typeName" label="名称" min-width="180">
            <template #default="{ row }">
              <span class="name-cell">
                <span class="name-text">{{ row.typeName }}</span>
                <el-tooltip v-if="row.description" :content="row.description" placement="top">
                  <el-icon class="desc-icon" :size="16"><QuestionFilled /></el-icon>
                </el-tooltip>
              </span>
            </template>
          </el-table-column>
          <el-table-column label="单位" width="100">
            <template #default="{ row }">
              <el-tag type="info" effect="plain">{{ row.unitName || '积分' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="可转账" width="100">
            <template #default="{ row }">
              <el-tag :type="row.isTransferable ? 'success' : 'info'">{{ row.isTransferable ? '是' : '否' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="小数位" width="90" prop="decimalPlaces" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-switch :model-value="row.status === 1"
                         :disabled="row.__updating === true"
                         @change="val => handleToggleStatus(row, val)"
                         :style="{ '--el-switch-on-color': '#67c23a', '--el-switch-off-color': '#f56c6c' }" />
            </template>
          </el-table-column>
          <el-table-column label="颜色" width="120">
            <template #default="{ row }">
              <div class="color-cell" v-if="row.colorCode">
                <span class="color-dot" :style="{ backgroundColor: row.colorCode }" />
                <span>{{ row.colorCode }}</span>
              </div>
              <span v-else>—</span>
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
          <template #empty>
            <EmptyBox size="table" title="暂无积分类型" desc="可调整筛选条件或点击新建积分类型后重试" />
          </template>
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
    <el-dialog class="type-dialog" :title="formModel.id ? '编辑积分类型' : '创建积分类型'" v-model="dialogVisible" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="formModel" :rules="rules" label-width="108px" class="type-form">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="类型编码" prop="typeCode">
              <el-input v-model="formModel.typeCode" placeholder="如 NORMAL、PREMIUM" :disabled="!!formModel.id" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="类型名称" prop="typeName">
              <el-input v-model="formModel.typeName" placeholder="显示名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="单位名称" prop="unitName">
              <el-input v-model="formModel.unitName" placeholder="如 积分" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="小数位数" prop="decimalPlaces">
              <el-input-number v-model="formModel.decimalPlaces" :min="0" :max="6" :step="1" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="可转账" prop="isTransferable">
              <el-switch v-model="formModel.isTransferable" :style="{ '--el-switch-on-color': '#67c23a', '--el-switch-off-color': '#f56c6c' }" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="颜色代码" prop="colorCode">
              <el-color-picker
                v-model="formModel.colorCode"
                :show-alpha="false"
                color-format="hex"
                @change="onColorChange"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="图标URL" prop="iconUrl">
              <el-input v-model="formModel.iconUrl" placeholder="/icons/xxx.png 或 http(s)://" />
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
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="formModel.description" :rows="3" maxlength="100" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
  
 </template>

<script>
import { defineComponent, reactive, ref, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { QuestionFilled } from '@element-plus/icons-vue'
import { creditTypeApi } from '@/api/credit-system'
import AppBreadcrumb from '@/components/common/Breadcrumb.vue'
import EmptyBox from '@/components/common/EmptyBox.vue'

export default defineComponent({
  name: 'TypeManagement',
  components: { QuestionFilled, AppBreadcrumb, EmptyBox },
  setup() {
    const loading = ref(false)
    const tableData = ref([])
    const total = ref(0)
    const indexMethod = (index) => {
      const base = (query.pageNum - 1) * query.pageSize
      return base + index + 1
    }

    const query = reactive({
      keyword: '',
      status: undefined,
      pageNum: 1,
      pageSize: 10
    })

    const dialogVisible = ref(false)
    const formRef = ref()
    const formModel = reactive({
      id: undefined,
      typeCode: '',
      typeName: '',
      unitName: '积分',
      iconUrl: '',
      colorCode: '',
      decimalPlaces: 0,
      isTransferable: true,
      status: 1,
      sortOrder: 1,
      description: ''
    })

    // 状态开关映射（与场景弹窗一致）
    const statusEnabled = computed({
      get: () => Number(formModel.status) === 1,
      set: (val) => { formModel.status = val ? 1 : 0 }
    })

    const rules = {
      typeCode: [
        { required: true, message: '请输入类型编码', trigger: 'blur' },
        { min: 1, max: 50, message: '长度 1-50', trigger: 'blur' }
      ],
      typeName: [
        { required: true, message: '请输入类型名称', trigger: 'blur' }
      ],
      unitName: [
        { required: true, message: '请输入单位名称', trigger: 'blur' }
      ],
      decimalPlaces: [
        { type: 'number', message: '请输入数字', trigger: 'change' }
      ],
      status: [
        { required: true, message: '请选择状态', trigger: 'change' }
      ],
      colorCode: [
        { validator: (_rule, value, callback) => {
            if (!value) return callback()
            const normalized = normalizeHexColor(value)
            if (!normalized) return callback(new Error('颜色值格式错误，应为 #RRGGBB'))
            callback()
          }, trigger: 'change' }
      ]
    }

    // 单位名称默认与类型名称一致（用户一旦手动编辑 unitName，就不再自动同步）
    const unitNameManuallyEdited = ref(false)
    watch(() => formModel.unitName, (val) => {
      // 只要不是由 typeName 同步写入的，就认为是手动改动
      if (val !== formModel.typeName) unitNameManuallyEdited.value = true
    })
    watch(() => formModel.typeName, (val) => {
      if (!unitNameManuallyEdited.value) {
        formModel.unitName = val || '积分'
      }
    })

    const normalizeList = (data) => {
      if (!data) return { list: [], total: 0 }
      if (Array.isArray(data)) return { list: data, total: data.length }
      if (Array.isArray(data.records)) return { list: data.records, total: data.total || data.records.length }
      if (Array.isArray(data.items)) return { list: data.items, total: data.total || data.items.length }
      if (Array.isArray(data.list)) return { list: data.list, total: data.total || data.list.length }
      return { list: [], total: 0 }
    }

    const loadData = async () => {
      loading.value = true
      try {
        const resp = await creditTypeApi.getCreditTypeList({
          keyword: query.keyword,
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
        typeCode: '',
        typeName: '',
        unitName: '积分',
        iconUrl: '',
        colorCode: '',
        decimalPlaces: 0,
        isTransferable: true,
        status: 1,
        sortOrder: 1,
        description: ''
      })
      unitNameManuallyEdited.value = false
      dialogVisible.value = true
    }

    const openEdit = (row) => {
      Object.assign(formModel, {
        id: row.id,
        typeCode: row.typeCode,
        typeName: row.typeName,
        unitName: row.unitName ?? '积分',
        iconUrl: row.iconUrl || '',
        colorCode: row.colorCode || '',
        decimalPlaces: Number(row.decimalPlaces ?? 0),
        isTransferable: !!row.isTransferable,
        status: Number(row.status ?? 1),
        sortOrder: Number(row.sortOrder ?? 1),
        description: row.description || ''
      })
      unitNameManuallyEdited.value = true
      dialogVisible.value = true
    }

    const handleSubmit = () => {
      formRef.value?.validate(async (valid) => {
        if (!valid) return
        try {
          const payload = { ...formModel }
          // 规范化颜色为 #RRGGBB（大写），若无效则置空以避免后端校验失败
          const normalizedColor = normalizeHexColor(payload.colorCode)
          payload.colorCode = normalizedColor || ''
          if (!payload.id) {
            const { typeCode, typeName, unitName, iconUrl, colorCode, decimalPlaces, isTransferable, sortOrder, description } = payload
            const createPayload = { typeCode, typeName, unitName, iconUrl, colorCode, decimalPlaces, isTransferable, sortOrder, description }
            const resp = await creditTypeApi.createCreditType(createPayload)
            if (resp.code === 200) {
              ElMessage.success('创建成功')
              dialogVisible.value = false
              loadData()
            } else {
              ElMessage.error(resp.message || '创建失败')
            }
          } else {
            const { typeName, unitName, iconUrl, colorCode, decimalPlaces, isTransferable, sortOrder, description, status } = payload
            const updatePayload = { typeName, unitName, iconUrl, colorCode, decimalPlaces, isTransferable, sortOrder, description, status }
            const resp = await creditTypeApi.updateCreditType(payload.id, updatePayload)
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
        }
      })
    }

    const handleDelete = (row) => {
      ElMessageBox.confirm(`确认删除积分类型【${row.typeName || row.typeCode}】？`, '提示', { type: 'warning' })
        .then(async () => {
          try {
            const resp = await creditTypeApi.deleteCreditType(row.id)
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
      // 二次确认：禁用时提醒更强，启用时普通确认
      const targetOn = !!val
      const title = targetOn ? '确认启用该积分类型？' : '确认禁用该积分类型？'
      const confirmText = targetOn ? '启用后可在前台使用该类型' : '禁用后将不可分配或使用该类型'
      try {
        await ElMessageBox.confirm(confirmText, title, { type: targetOn ? 'info' : 'warning' })
      } catch {
        // 取消，直接刷新一次行展示以回到原状态
        loadData()
        return
      }

      // 标记更新中，防止重复点击
      row.__updating = true
      try {
        const resp = await creditTypeApi.updateCreditType(row.id, { status: targetOn ? 1 : 0 })
        if (resp.code === 200) {
          row.status = targetOn ? 1 : 0
          ElMessage.success('状态已更新')
        } else {
          ElMessage.error(resp.message || '状态更新失败')
          // 失败后回滚 UI
          row.status = targetOn ? 0 : 1
        }
      } catch (e) {
        ElMessage.error(e.message || '状态更新失败')
        row.status = targetOn ? 0 : 1
      } finally {
        row.__updating = false
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

    // 规范化颜色字符串为 #RRGGBB（大写）。支持 #RGB、#RRGGBB、rgb()/rgba()，其他返回空字符串
    const normalizeHexColor = (value) => {
      if (!value || typeof value !== 'string') return ''
      const str = value.trim()
      // #RRGGBB or RRGGBB
      const full = str.match(/^#?([0-9a-fA-F]{6})$/)
      if (full) return `#${full[1].toUpperCase()}`
      // #RGB or RGB
      const short = str.match(/^#?([0-9a-fA-F]{3})$/)
      if (short) {
        const [r, g, b] = short[1].split('')
        return `#${(r + r + g + g + b + b).toUpperCase()}`
      }
      // rgb(...) or rgba(...)
      const rgb = str.match(/^rgba?\(\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*(\d{1,3})(?:\s*,\s*(\d*(?:\.\d+)?))?\s*\)$/i)
      if (rgb) {
        const toHex = (n) => {
          const num = Math.max(0, Math.min(255, Number(n)))
          return num.toString(16).padStart(2, '0')
        }
        return `#${toHex(rgb[1])}${toHex(rgb[2])}${toHex(rgb[3])}`.toUpperCase()
      }
      return ''
    }

    const onColorChange = (val) => {
      const normalized = normalizeHexColor(typeof val === 'string' ? val : '')
      formModel.colorCode = normalized || ''
    }

    onMounted(loadData)

    return {
      loading,
      tableData,
      total,
      query,
      dialogVisible,
      formRef,
      formModel,
      rules,
      handleSearch,
      handleReset,
      handleSizeChange,
      openCreate,
      openEdit,
      handleSubmit,
      handleDelete,
      handleToggleStatus,
      loadData,
      indexMethod,
      formatDateTime,
      QuestionFilled,
      statusEnabled,
      onColorChange
    }
  }
})
</script>

<style lang="scss" scoped>
.type-management-page {
  padding: 8px 0 12px;
  display: flex;
  flex-direction: column;
  /* 首屏即占满可用高度，避免“先小后大” */
  min-height: calc(100vh - var(--app-header-height) - var(--main-padding-v));

  .page-content {
    flex: 1;
    display: flex;
    min-height: 0;
    .list-card {
      border-radius: 8px;
      box-shadow: none;
      /* 卡片填满容器 */
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
    .filter-bar {
      margin-bottom: 12px;
    }
    .pager-bar {
      display: flex;
      justify-content: flex-end;
      margin-top: 12px;
    }
    /* 让表格区域可伸展，分页固定在底部 */
    .table-area {
      flex: 1 1 auto;
      display: flex;
      flex-direction: column;
      min-height: 0;
    }
    .flex-fill { flex: 1 1 auto; }
    .name-cell { display: inline-flex; align-items: center; gap: 6px; }
    .desc-icon { color: #909399; cursor: pointer; font-size: 16px; line-height: 1; display: inline-flex; }
    .color-cell {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      .color-dot {
        width: 12px;
        height: 12px;
        border-radius: 50%;
        border: 1px solid #dcdfe6;
        display: inline-block;
      }
    }
    .op-col :deep(.cell) { font-size: 14px; }
    .op-btn { font-size: 14px; padding: 0 6px; }
  }
}
/* 弹窗样式（弹窗在 .page-content 之外，因此放在根作用域） */
:deep(.type-dialog) { max-width: calc(100vw - 40px); margin-left: auto; margin-right: auto; }
:deep(.type-dialog) .el-dialog__footer { text-align: center; }
/* 表单对齐与控件宽度（与场景弹窗保持一致风格） */
.type-form {
  :deep(.el-form-item) { margin-bottom: 16px; }
  :deep(.el-form-item__label) { font-weight: 400; color: #303133; }
  :deep(.el-input),
  :deep(.el-select),
  :deep(.el-date-editor) { width: 360px; }
  :deep(.el-textarea) { width: 360px; }
}
</style>