<template>
  <div class="panel">
    <div class="toolbar">
      <div style="margin-left:auto"></div>
      <button class="btn primary" @click="openForm()">新建公告</button>
    </div>

    <div class="table">
      <div class="thead thead--ann">
        <div>标题</div>
        <div>状态</div>
        <div>发布时间</div>
        <div class="right">操作</div>
      </div>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="!list.length" class="empty">
        <EmptyBox :title="errorMsg || '暂无公告'" :desc="errorMsg ? '请检查网络或稍后重试' : '暂无公告内容'" />
      </div>
      <div v-else class="tbody">
        <div class="tr tr--ann" v-for="row in list" :key="row.id">
          <div class="title" @click="onTitleClick(row)">{{ row.title }}</div>
          <div><StatusBadge :status="normalizeStatus(row.status)" /></div>
          <div>{{ displayPublishTime(row) }}</div>
          <div class="right ops">
            <button v-if="normalizeStatus(row.status) === 'draft'" class="link" @click="openForm(row)">编辑</button>
            <button v-if="normalizeStatus(row.status) === 'draft'" class="link orange" @click="confirmPublish(row)">发布</button>
            <button v-if="normalizeStatus(row.status) === 'published'" class="link gray" @click="confirmOffline(row)">下线</button>
            <button v-if="normalizeStatus(row.status) === 'draft'" class="link" @click="preview(row)">预览</button>
            <button v-else class="link" @click="view(row)">查看</button>
          </div>
        </div>
      </div>
    </div>

    <div class="pager" v-if="list.length > 0">
      <Pagination
        :current="page.current"
        :size="page.size"
        :total="page.total"
        @update:current="onPageChange"
        @update:size="onSizeChange"
      />
    </div>

    <ConfirmDialog :show="showConfirm" :title="confirmTitle" :message="confirmMsg" @confirm="onConfirm" @cancel="closeConfirm" />

    <el-dialog v-model="showFormPanel" :title="editing ? '编辑公告' : '新建公告'" width="860px" :close-on-click-modal="false">
      <section class="form">
        <label>标题</label>
        <el-input v-model.trim="form.title" placeholder="请输入公告标题" clearable />

        <label>内容</label>
        <RichTextEditor v-model="form.content" />

        <label>优先级（越大越靠前）</label>
        <el-input-number v-model="form.priority" :min="0" :max="999999" :step="1" controls-position="right" />

        <label>是否强制阅读</label>
        <el-switch v-model="form.forceRead" active-text="强制" inactive-text="否" />

        <label>生效时间</label>
        <el-date-picker
          v-model="form.activeFrom"
          type="datetime"
          placeholder="选择生效日期时间"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DD HH:mm:ss"
          :editable="false"
        />

        <label>失效时间</label>
        <el-date-picker
          v-model="form.activeTo"
          type="datetime"
          placeholder="选择失效日期时间"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DD HH:mm:ss"
          :editable="false"
        />
      </section>
      <template #footer>
        <div class="dialog-footer">
          <el-button class="btn ghost" @click="onPreviewCurrent">预览</el-button>
          <el-button class="btn ghost" @click="closeForm">取消</el-button>
          <el-button type="primary" class="btn primary" :loading="submitting" @click="submit">{{ editing ? '保存' : '创建' }}</el-button>
        </div>
      </template>
    </el-dialog>

    <el-drawer v-model="previewing" :title="previewData?.title || '预览'" size="720px" append-to-body>
      <div class="preview-content" v-html="previewData?.content" />
      <template #footer>
        <el-button class="btn ghost" @click="closePreview">关闭</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { announcementApi } from '@/api/notify/announcement'
import StatusBadge from '@/components/common/StatusBadge.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import RichTextEditor from '@/components/common/RichTextEditor.vue'
import { sanitizeHtml } from '@/utils/sanitize'
import Pagination from '@/components/common/Pagination.vue'
import EmptyBox from '@/components/common/EmptyBox.vue'
import { formatDateTime } from '@/utils/datetime'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { requireText, betweenLen } from '@/utils/validators'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const errorMsg = ref('')
const query = reactive({ keyword: '' })
const page = reactive({ current: 1, size: 10, total: 0 })

const showFormPanel = ref(false)
const submitting = ref(false)
const editing = ref(false)
const currentId = ref(null)
const form = reactive({ title: '', content: '', priority: 0, forceRead: false, activeFrom: '', activeTo: '' })

const showConfirm = ref(false)
const confirmAction = ref(null)
const confirmTitle = ref('请确认')
const confirmMsg = ref('')
let confirmTarget = null

const previewing = ref(false)
const previewData = ref(null)
// 富文本直接存储与展示 HTML，统一使用 sanitizeHtml 进行安全过滤

// 公告中心入口已迁移至控制台

function normalizeStatus(s) {
  if (s === undefined || s === null) return 'draft'
  const num = Number(s)
  if (!Number.isNaN(num)) {
    return ({ 0: 'draft', 1: 'published', 2: 'offline', 3: 'scheduled', 4: 'failed' }[num] || 'draft')
  }
  return String(s).toLowerCase()
}

function displayPublishTime(row) {
  const t = row?.publishTime || row?.firstPushTime || row?.activeFrom || row?.createTime
  return t ? formatDateTime(t) : '-'
}

function openForm(row) {
  editing.value = !!row
  currentId.value = row?.id || null
  form.title = row?.title || ''
  form.content = row?.content || ''
  form.priority = Number.isFinite(row?.priority) ? row.priority : 0
  form.forceRead = row?.forceRead === 1
  form.activeFrom = row?.activeFrom ? row.activeFrom.replace('T', ' ').slice(0,19) : ''
  form.activeTo = row?.activeTo ? row.activeTo.replace('T', ' ').slice(0,19) : ''
  showFormPanel.value = true
}
function closeForm() { showFormPanel.value = false }

function isEmptyHtml(html) {
  const text = (html || '').replace(/<br\s*\/?>/gi, '\n').replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').trim()
  return text.length === 0
}
function validate() {
  const rules = [
    requireText('标题不能为空')(form.title),
    betweenLen(1, 200, '标题最多 200 字')(form.title),
    () => (!isEmptyHtml(form.content) ? true : (window.$message?.error?.('内容不能为空'), false)),
    () => {
      if (form.activeFrom && form.activeTo && new Date(form.activeFrom) > new Date(form.activeTo)) {
        window.$message?.error?.('生效时间必须早于失效时间')
        return false
      }
      return true
    }
  ]
  return rules.every(r => (typeof r === 'function' ? r() : r) === true)
}

async function submit() {
  if (!validate()) return
  submitting.value = true
  try {
    const html = sanitizeHtml(form.content || '')
    const payload = {
      title: form.title,
      content: html,
      priority: Number(form.priority) || 0,
      forceRead: form.forceRead ? 1 : 0,
      activeFrom: form.activeFrom ? dayjs(form.activeFrom).format('YYYY-MM-DDTHH:mm:ss') : undefined,
      activeTo: form.activeTo ? dayjs(form.activeTo).format('YYYY-MM-DDTHH:mm:ss') : undefined
    }
    let resp
    if (editing.value) resp = await announcementApi.update(currentId.value, payload)
    else resp = await announcementApi.create(payload)
    if (resp && resp.code !== 200) {
      throw new Error(resp?.message || '操作失败')
    }
    closeForm()
    await fetchList()
    ElMessage.success(editing.value ? '保存成功' : '创建成功')
  } catch (e) {
    ElMessage.error(e?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

function preview(row) {
  const html = sanitizeHtml(row?.content || '')
  previewData.value = { title: row?.title || '预览', content: html }
  previewing.value = true
}
function closePreview() { previewing.value = false }

function onPreviewCurrent() {
  const html = sanitizeHtml(form.content || '')
  previewData.value = { title: form.title || '预览', content: html }
  previewing.value = true
}

function confirmPublish(row) {
  showConfirm.value = true
  confirmTitle.value = '发布公告'
  confirmMsg.value = `确定发布「${row.title}」吗？`
  confirmAction.value = 'publish'
  confirmTarget = row
}
function confirmOffline(row) {
  showConfirm.value = true
  confirmTitle.value = '下线公告'
  confirmMsg.value = `确定下线「${row.title}」吗？`
  confirmAction.value = 'offline'
  confirmTarget = row
}
function closeConfirm() { showConfirm.value = false }

async function onConfirm() {
  try {
    if (confirmAction.value === 'publish') {
      const resp = await announcementApi.publish(confirmTarget.id, {})
      if (!resp || resp.code !== 200) throw new Error(resp?.message || '发布失败')
      ElMessage.success('发布成功')
    } else if (confirmAction.value === 'offline') {
      const resp = await announcementApi.offline(confirmTarget.id)
      if (!resp || resp.code !== 200) throw new Error(resp?.message || '下线失败')
      ElMessage.success('已下线')
    }
    await fetchList()
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  } finally {
    closeConfirm()
  }
}

async function fetchList() {
  loading.value = true
  errorMsg.value = ''
  try {
    const resp = await announcementApi.getList({ keyword: query.keyword, pageNum: page.current, pageSize: page.size })
    if (!resp || resp.code !== 200) throw new Error(resp?.message || '加载失败')
    const data = resp.data
    const records = Array.isArray(data) ? data : (data?.records || [])
    list.value = records
    page.total = Array.isArray(data) ? (data?.total ?? records.length) : (data?.total ?? 0)
  } catch (e) {
    errorMsg.value = e?.message || '加载失败'
    list.value = []
    page.total = 0
    ElMessage.error(errorMsg.value)
  } finally {
    loading.value = false
  }
}

function onTitleClick(row) {
  const status = normalizeStatus(row?.status)
  if (status === 'published') {
    router.push({ name: 'AnnouncementDetail', params: { id: row.id } })
  } else {
    preview(row)
  }
}

function view(row) {
  onTitleClick(row)
}

onMounted(fetchList)

function onPageChange(val) {
  page.current = val
  fetchList()
}
function onSizeChange(val) {
  page.size = val
  page.current = 1
  fetchList()
}
</script>

<style scoped>
.panel { display:flex; flex-direction: column; gap: 14px; min-height: 0; flex: 1 1 auto; }
.panel :deep(*) { box-sizing: border-box; }
.toolbar { display:flex; gap:8px; background:#fff; border:0; border-radius:10px; padding: 10px; }
.input { height: 34px; padding: 0 10px; border:1px solid #e2e8f0; border-radius: 8px; background:#fff; min-width: 240px; }
.btn { height: 34px; padding: 0 14px; border-radius: 8px; border: 1px solid transparent; background: #1890ff; color: #fff; font-weight: 700; letter-spacing: .2px; cursor: pointer; box-shadow: 0 2px 8px rgba(24,144,255,.18); }
.btn.primary { background:#1890ff; }
.btn.ghost { background:#f8fafc; color:#0f172a; border-color:#e2e8f0; box-shadow:none; }
.btn:disabled { opacity:.6; cursor:not-allowed; }

.table { border:1px solid #e2e8f0; border-radius: 10px; overflow: hidden; background:#fff; display:flex; flex-direction: column; min-height: 0; }
.thead, .tr { display:grid; grid-template-columns: 1fr 140px 180px 220px; align-items:center; gap: 8px; padding: 12px; }
.thead { background:#f8fafc; font-weight: 800; color:#0f172a; }
.tbody { flex:1 1 auto; min-height:0; overflow:auto; }
.tbody .tr { border-top:1px dashed #e2e8f0; }
.title { cursor: pointer; color:#0f172a; font-weight:700; }
.title:hover { text-decoration: underline; }
.right { text-align: right; }
.ops { display:flex; gap: 10px; justify-content: flex-end; }
.link { background: transparent; border:0; color:#2563eb; cursor:pointer; font-weight:700; }
.link.orange { color:#d97706; }
.link.purple { color:#7c3aed; }
.link.gray { color:#64748b; }
.loading, .empty { padding: 20px; text-align:center; color:#64748b; }

.drawer { position: fixed; inset: 0; background: rgba(15,23,42,.45); display: grid; place-items: center; z-index: 100; }
.drawer__panel { width: 860px; max-width: 96vw; background:#fff; border-radius: 12px; box-shadow: 0 24px 60px rgba(2,6,23,.16); display:flex; flex-direction: column; }
.drawer__panel header { padding: 16px 18px 0; }
.drawer__panel .form { padding: 10px 18px 0; display:grid; grid-template-columns: 1fr; gap: 8px; }
.drawer__panel footer { padding: 16px 18px 18px; display:flex; justify-content: flex-end; gap: 10px; }

/* Element Plus Drawer 优化 */
:deep(.el-drawer__header) { margin:0; padding: 14px 18px; border-bottom: 1px solid #edf2f7; font-weight: 800; color:#0f172a; }
:deep(.el-drawer__body) { padding: 0; display:flex; flex-direction: column; height: 100%; }
:deep(.el-drawer__footer) { padding: 12px 18px; border-top: 1px solid #edf2f7; background:#fff; position: sticky; bottom: 0; }

/* 抽屉表单区域滚动与间距 */
.form { flex:1; overflow: auto; padding: 12px 18px; display:grid; grid-template-columns: 1fr; gap: 10px; background:#fff; }
.form label { font-weight:600; color:#0f172a; }
.form .input { height: 34px; }
.form :deep(.el-input__wrapper) { border-radius: 8px; box-shadow: none; border:1px solid #e2e8f0; }
.form :deep(.el-textarea__inner) { border-radius: 8px; border:1px solid #e2e8f0; }

/* Publish section styles */
.publish-row { display:flex; flex-direction: column; gap: 6px; }
.publish-controls { display:flex; align-items:center; gap: 10px; flex-wrap: wrap; }
.publish-desc { color:#64748b; font-size: 12px; line-height: 1.2; }
.publish-row :deep(.el-radio-group .el-radio-button__inner) { padding: 8px 14px; font-weight: 700; }
.publish-row :deep(.el-date-editor.el-input) { width: 220px; }

.preview { position: fixed; inset: 0; background: rgba(15,23,42,.55); display:grid; place-items:center; z-index: 110; }
.preview__panel { width: 800px; max-width: 96vw; background:#fff; border-radius: 12px; box-shadow: 0 24px 60px rgba(2,6,23,.16); overflow:hidden; }
.preview__panel header { padding: 16px 18px 0; }
.preview__panel article { padding: 10px 18px 12px; }
.preview__panel footer { padding: 12px 18px 18px; display:flex; justify-content:flex-end; }

.pager { margin-top: auto; }

/* announcement specific grid */
.thead--ann, .tr--ann { grid-template-columns: 1fr 140px 180px 220px; }

/* Preview content styles */
.preview-content :deep(*) { word-break: break-word; }
.preview-content :deep(img) { max-width: 100%; height: auto; display: block; }
.preview-content :deep(table) { width: 100%; border-collapse: collapse; }
.preview-content :deep(pre) { white-space: pre-wrap; }
</style>



