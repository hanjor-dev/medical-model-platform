<template>
  <div class="panel">
    <div class="toolbar">
      <input class="input" v-model="query.keyword" placeholder="搜索标题或ID..." @keyup.enter="fetchList" />
      <button class="btn ghost" @click="fetchList">查询</button>
      <div style="margin-left:auto"></div>
      <button class="btn primary" @click="openForm()">新建消息</button>
    </div>

    <div class="table">
      <div class="thead">
        <div>标题</div>
        <div>渠道</div>
        <div>状态</div>
        <div>更新时间</div>
        <div class="right">操作</div>
      </div>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="!list.length" class="empty">
        <EmptyBox :title="errorMsg || '暂无消息'" :desc="errorMsg ? '请检查网络或稍后重试' : '暂无消息内容'" />
      </div>
      <div v-else class="tbody">
        <div class="tr" v-for="item in list" :key="item.id">
          <div class="title">{{ item.title }}</div>
          <div><ChannelBadge :channel="item.channel" /></div>
          <div><StatusBadge :status="item.status" /></div>
          <div>{{ formatDateTime(item.updateTime) }}</div>
          <div class="right ops">
            <button class="link" @click="openForm(item)">编辑</button>
            <button class="link green" @click="confirmPush(item)">推送</button>
            <button v-if="item.status==='published'" class="link gray" @click="confirmOffline(item)">下线</button>
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

    <el-dialog v-model="showFormPanel" :title="editing ? '编辑消息' : '新建消息'" width="860px">
      <section class="form form--dialog">
        <label>标题</label>
        <input class="input input--narrow" v-model.trim="form.title" placeholder="请输入消息标题" />

        <label>接收用户ID</label>
        <input class="input input--narrow" v-model="form.userId" placeholder="如：10086" />

        <label>消息类型</label>
        <el-select v-model="form.messageType" placeholder="请选择类型" filterable class="select--narrow">
          <el-option label="系统" value="system" />
          <el-option label="任务" value="task" />
          <el-option label="积分" value="credit" />
          <el-option label="营销" value="marketing" />
        </el-select>

        <label>内容</label>
        <textarea class="input textarea full" rows="5" v-model="form.content" placeholder="请输入消息内容" />

        <label>定时发送</label>
        <input class="input input--narrow" type="datetime-local" v-model="form.scheduleTime" />
      </section>
      <template #footer>
        <div style="display:flex;justify-content:flex-end;gap:10px;">
          <button class="btn ghost" @click="closeForm">取消</button>
          <button class="btn primary" :disabled="submitting" @click="submit">{{ submitting ? '提交中...' : (editing ? '保存' : '创建') }}</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { messageApi } from '@/api/notify/message'
import StatusBadge from '@/components/common/StatusBadge.vue'
import ChannelBadge from '@/components/common/ChannelBadge.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
 
import Pagination from '@/components/common/Pagination.vue'
import EmptyBox from '@/components/common/EmptyBox.vue'
import { formatDateTime } from '@/utils/datetime'
import { requireText, betweenLen } from '@/utils/validators'

const list = ref([])
const loading = ref(false)
const errorMsg = ref('')
const query = reactive({ keyword: '' })
const page = reactive({ current: 1, size: 10, total: 0 })

const showFormPanel = ref(false)
const submitting = ref(false)
const editing = ref(false)
const currentId = ref(null)
const form = reactive({ userId: '', messageType: '', title: '', content: '', scheduleTime: '' })

const showConfirm = ref(false)
const confirmAction = ref(null)
const confirmTitle = ref('请确认')
const confirmMsg = ref('')
let confirmTarget = null

function openForm(row) {
  editing.value = !!row
  currentId.value = row?.id || null
  form.userId = row?.userId || ''
  form.messageType = row?.messageType || ''
  form.title = row?.title || ''
  form.content = row?.content || ''
  form.scheduleTime = ''
  loadTemplateOptions()
  showFormPanel.value = true
}
function closeForm() { showFormPanel.value = false }

function validate() {
  const rules = [
    requireText('接收用户ID不能为空')(form.userId),
    betweenLen(0, 200, '标题最多 200 字')(form.title),
    requireText('内容不能为空')(form.content)
  ]
  return rules.every(r => r === true)
}

async function submit() {
  if (!validate()) return
  submitting.value = true
  try {
    const schedule = form.scheduleTime ? new Date(form.scheduleTime).toISOString() : undefined
    const payload = {
      userId: Number(form.userId),
      messageType: form.messageType || undefined,
      title: form.title || undefined,
      content: form.content,
      scheduleTime: schedule
    }
    await messageApi.create(payload)
    closeForm()
    await fetchList()
  } finally {
    submitting.value = false
  }
}

function confirmPush(row) {
  showConfirm.value = true
  confirmTitle.value = '推送消息'
  confirmMsg.value = `确定推送「${row.title}」吗？`
  confirmAction.value = 'push'
  confirmTarget = row
}
function confirmOffline(row) {
  showConfirm.value = true
  confirmTitle.value = '下线消息'
  confirmMsg.value = `确定下线「${row.title}」吗？`
  confirmAction.value = 'offline'
  confirmTarget = row
}
function closeConfirm() { showConfirm.value = false }

async function onConfirm() {
  try {
    if (confirmAction.value === 'push') {
      // 当前后端无“推送”接口，跳过
    } else if (confirmAction.value === 'offline') {
      await messageApi.cancel(confirmTarget.id)
    }
    await fetchList()
  } finally {
    closeConfirm()
  }
}

async function fetchList() {
  loading.value = true
  errorMsg.value = ''
  try {
    const { data } = await messageApi.getList({ pageNum: page.current, pageSize: page.size })
    const records = Array.isArray(data) ? data : (data?.records || [])
    list.value = records
    page.total = Array.isArray(data) ? (data?.total ?? records.length) : (data?.total ?? 0)
  } catch (e) {
    errorMsg.value = e?.message || '加载失败'
    list.value = []
    page.total = 0
  } finally {
    loading.value = false
  }
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
.panel__header { display:flex; justify-content: space-between; align-items: flex-end; padding: 12px 14px; background:#fff; border:1px solid #e2e8f0; border-radius: 10px; }
.panel__header h3 { margin:0; font-weight:800; color:#0f172a; }
.panel__header .desc { margin: 4px 0 0; color:#64748b; }
.actions { display:flex; gap: 8px; }

.toolbar { display:flex; gap:8px; background:#fff; border:1px solid #e2e8f0; border-radius:10px; padding: 10px; }
.input { height: 34px; padding: 0 10px; border:1px solid #e2e8f0; border-radius: 8px; background:#fff; min-width: 240px; }
.btn { height: 34px; padding: 0 14px; border-radius: 8px; border: 1px solid transparent; background: #1890ff; color: #fff; font-weight: 700; letter-spacing: .2px; cursor: pointer; box-shadow: 0 2px 8px rgba(24,144,255,.18); }
.btn.primary { background:#1890ff; }
.btn.ghost { background:#f8fafc; color:#0f172a; border-color:#e2e8f0; box-shadow:none; }
.btn:disabled { opacity:.6; cursor:not-allowed; }
.seg { display:flex; gap:6px; }
.seg__btn { height:28px; padding: 0 10px; border:1px solid #e2e8f0; border-radius: 8px; background:#fff; font-weight:700; }
.seg__btn.on { background:#0ea5e9; color:#fff; border-color:#0ea5e9; }

.table { border:1px solid #e2e8f0; border-radius: 10px; overflow: hidden; background:#fff; display:flex; flex-direction: column; min-height: 0; }
.thead, .tr { display:grid; grid-template-columns: 1fr 140px 140px 180px 220px; align-items:center; gap: 8px; padding: 12px; }
.thead { background:#f8fafc; font-weight: 800; color:#0f172a; }
.tbody { flex:1 1 auto; min-height:0; overflow:auto; }
.tbody .tr { border-top:1px dashed #e2e8f0; }
.title { color:#0f172a; font-weight:700; }
.right { text-align: right; }
.ops { display:flex; gap: 10px; justify-content: flex-end; }
.link { background: transparent; border:0; color:#2563eb; cursor:pointer; font-weight:700; }
.link.green { color:#059669; }
.link.purple { color:#7c3aed; }
.link.gray { color:#64748b; }
.loading, .empty { padding: 20px; text-align:center; color:#64748b; }

.drawer { position: fixed; inset: 0; background: rgba(15,23,42,.45); display: grid; place-items: center; z-index: 100; }
.drawer__panel { width: 860px; max-width: 96vw; background:#fff; border-radius: 10px; box-shadow: 0 24px 60px rgba(2,6,23,.16); display:flex; flex-direction: column; }
.drawer__panel header { padding: 14px 18px; border-bottom: 1px solid #edf2f7; }
.drawer__panel .form { flex:1; overflow:auto; padding: 12px 18px; display:grid; grid-template-columns: 1fr; gap: 10px; }
.drawer__panel footer { padding: 12px 18px; border-top: 1px solid #edf2f7; display:flex; justify-content: flex-end; gap: 10px; position: sticky; bottom:0; background:#fff; }

.pager { margin-top: auto; }
.drawer__panel .form label { font-weight:600; color:#0f172a; }
/* Dialog form layout */
.form--dialog { display:grid; grid-template-columns: 120px 1fr; align-items: center; gap: 10px 12px; }
.form--dialog .full { grid-column: 1 / -1; }
.form--dialog .textarea { min-height: 100px; height: auto; }
.form--dialog .input { width: 100%; min-width: 0; }
.form--dialog .input--narrow { width: 60%; }
.form--dialog .select--narrow { width: 60%; }
</style>

