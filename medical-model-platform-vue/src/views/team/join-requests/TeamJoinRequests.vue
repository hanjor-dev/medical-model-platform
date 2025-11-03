<template>
  <div class="team-join-requests-page">
    <div class="page-content">
      <el-card class="list-card">
        <template #header>
          <div class="list-header">
            <AppBreadcrumb />
          </div>
        </template>

        <!-- 筛选区 -->
        <div class="filter-content">
          <div class="filter-inline">
            <div class="filter-item" v-if="isSuperAdmin">
              <span class="filter-label">团队：</span>
              <el-select v-model="query.teamId" placeholder="选择团队" clearable class="w-200" filterable @change="handleTeamChange">
                <el-option v-for="t in teamOptions" :key="t.id" :label="`${t.teamName}（${Number(t.status) === 1 ? '正常' : '禁用'}）`" :value="String(t.id)" />
              </el-select>
            </div>
            <div class="filter-item" v-if="isAdminOrOwner">
              <span class="filter-label">申请人：</span>
              <el-input v-model="query.applicant" placeholder="用户名/昵称" clearable class="w-200" @input="handleSearch" />
            </div>
            <div class="filter-item">
              <span class="filter-label">状态：</span>
              <el-select v-model="query.status" placeholder="状态" clearable class="w-160" @change="handleSearch">
                <el-option label="全部" value="" />
                <el-option label="待处理" value="PENDING" />
                <el-option label="已同意" value="APPROVED" />
                <el-option label="已拒绝" value="REJECTED" />
              </el-select>
            </div>
            <div class="filter-item">
              <span class="filter-label">申请时间：</span>
              <el-date-picker
                v-model="query.range"
                type="datetimerange"
                value-format="YYYY-MM-DD HH:mm:ss"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                class="w-380"
                @change="handleSearch"
              />
            </div>
            <div class="filter-item">
              <span class="filter-label">备注：</span>
              <el-input v-model="query.keyword" placeholder="备注关键词" clearable class="w-220" @input="handleSearch" />
            </div>
            <div class="filter-actions right same-row">
            </div>
          </div>
        </div>

        <div class="table-area">
          <!-- 表格/空态 -->
          <template v-if="!loading && (!records || records.length === 0)">
            <EmptyBox
              :title="noTeam ? '暂未加入团队' : '暂无申请'"
              :desc="noTeam ? '加入团队后可在此查看和处理加入申请' : '可调整筛选条件或稍后重试'"
            />
          </template>
          <el-table
            v-else
            v-loading="loading"
            :data="records"
            stripe
            row-key="id"
            class="join-request-data-table"
            size="small"
            :header-cell-style="{ textAlign: 'center' }"
            :cell-style="{ textAlign: 'center' }"
            @selection-change="onSelectionChange"
          >
            <el-table-column v-if="isAdminOrOwner" type="selection" width="55" fixed="left" />

          <el-table-column label="#" width="60" align="center" fixed="left">
            <template #default="{ $index }">
              {{ (pagination.current - 1) * pagination.size + $index + 1 }}
            </template>
          </el-table-column>

          <el-table-column prop="applicantName" label="申请人" width="160" />
          <el-table-column prop="remark" label="申请理由" width="240" show-overflow-tooltip />
          <el-table-column prop="status" label="申请状态" width="110">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="申请时间" width="170">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column prop="processedByName" label="处理人" width="140">
            <template #default="{ row }">{{ row.processedByName || row.processedBy || '-' }}</template>
          </el-table-column>
          <el-table-column prop="processedAt" label="处理时间" width="170">
            <template #default="{ row }">{{ formatTime(row.processedAt) }}</template>
          </el-table-column>

          <el-table-column label="操作" width="260" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button size="small" type="primary" @click="() => openDetail(row)">详情</el-button>
                <template v-if="isAdminOrOwner">
                  <el-button size="small" type="success" plain :disabled="statusToString(row.status) !== 'PENDING'" @click="() => approveOne(row)">同意</el-button>
                  <el-button size="small" type="danger" plain :disabled="statusToString(row.status) !== 'PENDING'" @click="() => openReject(row)">拒绝</el-button>
                </template>
                <el-button v-else size="small" type="warning" plain :disabled="statusToString(row.status) !== 'PENDING'" @click="() => cancelMine(row)">撤销</el-button>
              </div>
            </template>
          </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="table-footer">
            <template v-if="records && records.length">
              <CommonPagination
                v-model:current="pagination.current"
                v-model:size="pagination.size"
                :total="total"
                :selected-count="selectedRows.length"
                :default-page-size="10"
                :show-info="false"
                align="right"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </template>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" :title="detailTitle" size="40%" :with-header="true">
      <div class="detail-wrap" v-loading="detailLoading">
        <div class="detail-row"><span class="label">申请人：</span><span class="value">{{ detail?.applicantName }}</span></div>
        <div class="detail-row"><span class="label">申请理由：</span><span class="value">{{ detail?.requestReason || detail?.remark || '-' }}</span></div>
        <div class="detail-row"><span class="label">状态：</span><span class="value"><el-tag :type="getStatusTagType(detail?.status)">{{ getStatusLabel(detail?.status) }}</el-tag></span></div>
        <div class="detail-row"><span class="label">申请时间：</span><span class="value">{{ formatTime(detail?.createdAt) }}</span></div>
        <div class="detail-row"><span class="label">处理人：</span><span class="value">{{ detail?.processedByName || detail?.processedBy || '-' }}</span></div>
        <div class="detail-row"><span class="label">处理时间：</span><span class="value">{{ formatTime(detail?.processedAt) }}</span></div>
        <div class="detail-row"><span class="label">处理备注：</span><span class="value">{{ detail?.processReason || '-' }}</span></div>
        <!-- 处理日志已移除：由后端详情字段直接展示 -->
      </div>
    </el-drawer>

    <!-- 拒绝对话框 -->
    <el-dialog v-model="rejectVisible" title="拒绝申请" width="460px" :close-on-click-modal="false">
      <div class="reject-content">
        <el-input v-model="rejectReason" type="textarea" :rows="4" maxlength="200" show-word-limit placeholder="请输入拒绝原因（可选）" />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmReject">拒绝</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppBreadcrumb from '@/components/common/Breadcrumb.vue'
import EmptyBox from '@/components/common/EmptyBox.vue'
import CommonPagination from '@/components/common/Pagination.vue'
import { useAuthStore } from '@/stores/auth'
import { teamApi } from '@/api/team/index.js'
import { teamJoinRequestApi } from '@/api/team/join-request.js'
import { userApi } from '@/api/user/index.js'

export default {
  name: 'TeamJoinRequests',
  components: { AppBreadcrumb, CommonPagination, EmptyBox },
  setup() {
    const route = useRoute()
    const authStore = useAuthStore()

const teamId = ref((() => {
  const u = authStore?.getUser?.()
  return (u && u.currentTeamId != null) ? String(u.currentTeamId) : null
})())
    const userRole = ref(authStore?.getUser?.()?.role || 'USER')
    const isSuperAdmin = computed(() => userRole.value === 'SUPER_ADMIN')
    const isAdminOrOwner = ref(false)
    const teamOptions = ref([])

    const query = reactive({ teamId: null, status: '', applicant: '', range: [], keyword: '' })
    const pagination = reactive({ current: 1, size: 10 })

    const loading = ref(false)
    const records = ref([])
    const total = ref(0)
    const noTeam = ref(false)
    const selectedRows = ref([])

    // 详情
    const detailVisible = ref(false)
    const detailLoading = ref(false)
    const detail = ref(null)
    const detailLogs = ref([])

    const detailTitle = computed(() => detail.value ? `申请详情 - ${detail.value.applicantName}` : '申请详情')

    // 兼容后端状态数值 0/1/2 与前端字符串
    const statusToString = (s) => {
      if (s === 0 || s === '0') return 'PENDING'
      if (s === 1 || s === '1') return 'APPROVED'
      if (s === 2 || s === '2') return 'REJECTED'
      return String(s)
    }
    const getStatusLabel = (s) => ({ PENDING: '待处理', APPROVED: '已同意', REJECTED: '已拒绝', CANCELLED: '已撤销' }[statusToString(s)] || s)
    const getStatusTagType = (s) => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', CANCELLED: 'info' }[statusToString(s)] || 'info')

    const userNameCache = reactive({})
    const deriveUserDisplayName = (data) => {
      return data?.nickname || data?.nickName || data?.username || data?.userName || data?.name || data?.realName || '-'
    }
    const hydrateApplicantNames = async (rows) => {
      // 后端已返回 applicantName 时不再前端查询
      const ids = Array.from(new Set(rows.filter(r => !r.applicantName).map(r => r.applicantId).filter(id => id && !userNameCache[id])))
      if (!ids.length) return
      try {
        const tasks = ids.map(id => userApi.getUserById(id).then(res => ({ id, res })).catch(() => ({ id, res: null })))
        const results = await Promise.all(tasks)
        results.forEach(({ id, res }) => {
          if (res && res.code === 200) userNameCache[id] = deriveUserDisplayName(res.data)
        })
        // 更新现有行
        rows.forEach(r => { if (userNameCache[r.applicantId]) r.applicantName = userNameCache[r.applicantId] })
      } catch (_) { /* ignore */ }
    }

    const formatTime = (time) => {
      if (!time) return '-'
      // 后端字段为 createTime/processedAt，视图上使用 createdAt/processedAt，统一做兼容
      const t = typeof time === 'string' || typeof time === 'number' ? time : (time?.time || null)
      return t ? new Date(t).toLocaleString('zh-CN') : new Date(time).toLocaleString('zh-CN')
    }

    // 统一将后端 VO 字段映射为前端使用的行结构
    const mapJoinRequest = (it = {}) => ({
      id: it.id,
      applicantId: it.userId,
      applicantName: it.applicantName || it.userNickname || it.nickname || it.username || it.userName || it.realName || String(it.userId || '-'),
      remark: it.requestReason,
      requestReason: it.requestReason,
      status: it.status,
      createdAt: it.createdAt || it.createTime,
      processedBy: it.processedBy,
      processedByName: it.processedByName || '',
      processedAt: it.processedAt
    })

    const onSelectionChange = (selection) => {
      selectedRows.value = selection || []
    }

    const evaluatePrivileges = (teamDetail) => {
      // 根据后端返回的 teamDetail 中的 myTeamRole 或权限字段判断
      const myRole = teamDetail?.myTeamRole || 'MEMBER'
      isAdminOrOwner.value = (myRole === 'OWNER' || myRole === 'ADMIN') || isSuperAdmin.value
    }

    const handleTeamChange = async (val) => {
      if (isSuperAdmin.value) {
        teamId.value = val ? String(val) : null
        pagination.current = 1
        await fetchList()
      }
    }

    const handleSearch = async () => {
      pagination.current = 1
      await fetchList()
    }

    const handleSizeChange = async (size) => {
      pagination.size = size
      pagination.current = 1
      await fetchList()
    }

    const handleCurrentChange = async (current) => {
      pagination.current = current
      await fetchList()
    }

    const approveOne = async (row) => {
      try {
        await ElMessageBox.confirm(`确定同意 “${row.applicantName}” 的加入申请吗？`, '同意申请', { type: 'warning' })
      } catch (_) { return }
      try {
        const res = await teamJoinRequestApi.process({ requestId: row.id, action: 'APPROVE' })
        if (res && res.code === 200) {
          ElMessage.success(res.message || '已同意')
          await fetchList()
        } else {
          ElMessage.error(res?.message || '操作失败')
        }
      } catch (e) {
        ElMessage.error(e?.message || '操作失败')
      }
    }

    const openReject = (row) => {
      rejectTarget.value = row
      rejectReason.value = ''
      rejectVisible.value = true
    }

    const confirmReject = async () => {
      if (!rejectTarget.value) return
      try {
        const res = await teamJoinRequestApi.process({ requestId: rejectTarget.value.id, action: 'REJECT', reason: rejectReason.value || undefined })
        if (res && res.code === 200) {
          ElMessage.success(res.message || '已拒绝')
          rejectVisible.value = false
          await fetchList()
        } else {
          ElMessage.error(res?.message || '操作失败')
        }
      } catch (e) {
        ElMessage.error(e?.message || '操作失败')
      }
    }

    const cancelMine = async (row) => {
      try {
        await ElMessageBox.confirm('确定撤销该申请吗？', '撤销确认', { type: 'warning' })
      } catch (_) { return }
      try {
        const res = await teamJoinRequestApi.cancel(row.id)
        if (res && res.code === 200) {
          ElMessage.success(res.message || '已撤销')
          await fetchList()
        } else {
          ElMessage.error(res?.message || '撤销失败')
        }
      } catch (e) {
        ElMessage.error(e?.message || '撤销失败')
      }
    }

    // 拒绝对话框状态
    const rejectVisible = ref(false)
    const rejectTarget = ref(null)
    const rejectReason = ref('')

    onMounted(async () => {
      await fetchTeamOptionsIfNeed()
      const ok = await ensureTeamId()
      if (ok) await fetchTeamPrivilege()
      await fetchList()
    })

    const ensureTeamId = async () => {
      if (!teamId.value) {
        const qId = route?.query?.teamId ? String(route.query.teamId) : null
        if (qId) teamId.value = qId
        if (!teamId.value) {
          // 强制刷新，避免命中缓存拿不到最新 currentTeamId
          await authStore.refreshUserInfo()
          // 保护性二次刷新，避免读写时序导致数据陈旧
          try { await new Promise(resolve => setTimeout(resolve, 80)); await authStore.refreshUserInfo() } catch (_) { /* ignore */ }
          teamId.value = authStore?.getUser?.()?.currentTeamId || null
        }
      }
      const ok = Boolean(teamId.value)
      noTeam.value = !ok
      return ok
    }

    const fetchTeamOptionsIfNeed = async () => {
      if (!isSuperAdmin.value) return
      try {
        const res = await teamApi.list({ pageNum: 1, pageSize: 200 })
        if (res && res.code === 200) teamOptions.value = Array.isArray(res.data?.records) ? res.data.records : []
      } catch (_) { /* ignore */ }
    }

    const fetchTeamPrivilege = async () => {
      try {
        const res = await teamApi.detail(String(teamId.value))
        if (res && res.code === 200) evaluatePrivileges(res.data)
      } catch (_) { isAdminOrOwner.value = isSuperAdmin.value }
    }

    const fetchList = async () => {
      const ok = await ensureTeamId()
      if (!ok) return
      loading.value = true
      try {
        const params = { pageNum: pagination.current, pageSize: pagination.size }
        if (query.status) params.status = query.status
        if (query.applicant) params.applicant = query.applicant
        if (query.keyword) params.keyword = query.keyword
        if (Array.isArray(query.range) && query.range.length === 2) { params.start = query.range[0]; params.end = query.range[1] }
        const res = await teamJoinRequestApi.list(String(teamId.value), params)
        if (res && res.code === 200) {
          const list = Array.isArray(res.data?.records) ? res.data.records : []
          // 将后端字段映射到前端表格字段
          records.value = list.map(mapJoinRequest)
          // 异步补齐昵称
          await hydrateApplicantNames(records.value)
          total.value = Number(res.data?.total || records.value.length)
        } else {
          ElMessage.error(res?.message || '加载失败')
        }
      } catch (e) {
        ElMessage.error(e?.message || '加载失败')
      } finally {
        loading.value = false
      }
    }

    const openDetail = async (row) => {
      detailVisible.value = true
      detailLoading.value = true
      detail.value = row
      try {
        const dres = await (teamJoinRequestApi.getJoinRequestDetail?.(row.id))
        if (dres && dres.code === 200) {
          detail.value = mapJoinRequest(dres.data || detail.value)
          detailLogs.value = Array.isArray(dres.data?.logs) ? dres.data.logs : []
        } else {
          detailLogs.value = []
        }
      } catch (_) { detailLogs.value = [] }
      detailLoading.value = false
    }

    return {
      // 权限与团队
      isSuperAdmin,
      isAdminOrOwner,
      teamOptions,

      // 查询
      query,
      pagination,
      loading,
      records,
      total,
      noTeam,
      selectedRows,

      // 详情
      detailVisible,
      detailLoading,
      detail,
      detailLogs,
      detailTitle,

      // 拒绝
      rejectVisible,
      rejectReason,

      // 方法
      handleTeamChange,
      handleSearch,
      handleSizeChange,
      handleCurrentChange,
      onSelectionChange,
      openDetail,
      approveOne,
      openReject,
      confirmReject,
      cancelMine,

      // 格式化
      statusToString,
      getStatusLabel,
      getStatusTagType,
      formatTime
    }
  }
}
</script>

<style scoped>
.team-join-requests-page { padding: 8px 0 12px; background: transparent; display:flex; flex-direction:column; min-height: calc(100vh - var(--app-header-height) - var(--main-padding-v)); }

.team-join-requests-page .page-content { flex:1; display:flex; min-height:0; }
.team-join-requests-page .list-card { flex:1 1 auto; width:100%; min-height:100%; display:flex; flex-direction:column; }
.team-join-requests-page .list-card :deep(.el-card__body) { flex:1; display:flex; flex-direction:column; min-height:0; }

.list-card { border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06); }

.filter-content { padding-top: 10px; padding-bottom: 6px; margin-bottom: 16px; }
.filter-inline { display: flex; flex-wrap: wrap; align-items: flex-end; gap: 16px 16px; margin-bottom: 20px; }
.filter-actions.right.same-row { margin-left: auto; width: auto; }
.filter-item { display: flex; align-items: center; gap: 8px; }
.filter-label { min-width: 72px; font-size: 14px; color: #606266; text-align: right; white-space: nowrap; }

.w-160 { width: 160px; }
.w-180 { width: 180px; }
.w-200 { width: 200px; }
.w-220 { width: 220px; }
.w-380 { width: 380px; }

.join-request-data-table { width: 100%; --el-table-row-height: 42px; }
.join-request-data-table :deep(.el-table__cell) { padding-top: 10px; padding-bottom: 10px; }

/* 让表格区域占满卡片剩余空间，分页固定在底部 */
.table-area { flex: 1 1 auto; display: flex; flex-direction: column; min-height: 0; }
.table-footer { margin-top: auto; padding-top: 8px; }

.action-buttons { display: flex; gap: 8px; justify-content: center; width: 100%; }

.detail-wrap { padding: 4px 8px; }
.detail-row { display: flex; margin-bottom: 10px; }
.detail-row .label { width: 90px; color: #666; }
.detail-row .value { color: #303133; }

.logs-table { margin-top: 8px; }

@media (max-width: 1200px) { .filter-inline { gap: 12px; } }
</style>


