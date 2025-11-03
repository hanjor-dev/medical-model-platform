<template>
  <div class="team-members-page">
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
               <span class="filter-label required">团队：</span>
              <el-select v-model="query.teamId" placeholder="选择团队" clearable class="w-200" filterable @change="handleTeamChange">
                <el-option v-for="t in teamOptions" :key="t.id" :label="`${t.teamName}（${Number(t.status) === 1 ? '正常' : '禁用'}）`" :value="String(t.id)" />
              </el-select>
            </div>
            <div class="filter-item">
              <span class="filter-label">关键词：</span>
              <el-input
                v-model="query.keyword"
                placeholder="搜索用户名/昵称/邮箱"
                clearable
                class="w-220"
                @input="handleSearch"
              />
            </div>
            <div class="filter-item">
              <span class="filter-label">团队角色：</span>
              <el-select v-model="query.teamRole" placeholder="团队角色" clearable class="w-180" @change="handleSearch">
                <el-option label="全部" value="" />
                <el-option v-for="r in teamRoleOptions" :key="r.value" :label="r.label" :value="r.value" />
              </el-select>
            </div>
            <div class="filter-item">
              <span class="filter-label">状态：</span>
              <el-select v-model="query.status" placeholder="状态" clearable class="w-140" @change="handleSearch">
                <el-option label="全部状态" value="" />
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </div>
            <div class="filter-actions right same-row">
              <el-button @click="handleRefresh">刷新成员</el-button>
            </div>
          </div>
        </div>

        <div class="table-area">
          <!-- 表格/空态 -->
          <template v-if="!loading && (!records || records.length === 0)">
            <EmptyBox
              :title="noTeam ? '暂未加入团队' : '暂无成员'"
              :desc="noTeam ? '加入团队后可在此管理成员' : '可调整筛选条件或稍后重试'"
            />
          </template>
          <el-table
            v-else
            v-loading="loading"
            :data="records"
            stripe
            row-key="id"
            class="member-data-table"
            size="small"
            :header-cell-style="{ textAlign: 'center' }"
            :cell-style="{ textAlign: 'center' }"
            @sort-change="handleSortChange"
          >
            <el-table-column label="#" width="60" align="center" fixed="left">
              <template #default="{ $index }">
                {{ (pagination.current - 1) * pagination.size + $index + 1 }}
              </template>
            </el-table-column>
            <el-table-column prop="username" label="用户名" width="150" fixed="left">
              <template #default="{ row }">
                <el-tag type="primary" size="small">{{ row.username }}</el-tag>
              </template>
            </el-table-column>

          <el-table-column prop="nickname" label="昵称" width="140" />
          <el-table-column prop="email" label="邮箱" width="200" show-overflow-tooltip />

          <el-table-column prop="role" label="用户角色" width="120">
            <template #default="{ row }">
              <el-tag :type="getRoleTagType(row.role)" size="small">{{ getRoleLabel(row.role) }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="teamRole" label="团队角色" width="120">
            <template #default="{ row }">
              <el-tag :type="getTeamRoleTagType(row.teamRole)" size="small">{{ getTeamRoleLabel(row.teamRole) }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="status" label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                active-color="#52c41a"
                inactive-color="#d9d9d9"
                @change="() => handleMemberStatusToggle(row)"
                style="--el-switch-on-color: #52c41a; --el-switch-off-color: #ff4d4f"
              />
            </template>
          </el-table-column>

          <el-table-column prop="lastLoginTime" label="最近登录" width="160" sortable="custom">
            <template #default="{ row }">{{ formatTime(row.lastLoginTime) }}</template>
          </el-table-column>

          <el-table-column prop="joinedAt" label="加入时间" width="160" sortable="custom">
            <template #default="{ row }">{{ formatTime(row.joinedAt) }}</template>
          </el-table-column>

          <el-table-column label="操作" width="220" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button size="small" type="primary" @click="() => handleView(row)">查看</el-button>
                <el-dropdown @command="(cmd) => handleRowCommand(cmd, row)">
                  <el-button size="small">
                    更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="role" class="color-primary">
                        <el-icon><EditPen /></el-icon>
                        <span>调整团队角色</span>
                      </el-dropdown-item>
                      <el-dropdown-item v-if="row.teamRole === 'ADMIN' && row.status === 1" command="transfer" class="color-primary">
                        <el-icon><EditPen /></el-icon>
                        <span>转移拥有者</span>
                      </el-dropdown-item>
                      <el-dropdown-item command="remove" divided class="color-danger">
                        <el-icon><Delete /></el-icon>
                        <span>移除成员</span>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>
          </el-table-column>
          </el-table>

          <!-- 分页器 -->
          <div class="table-footer">
            <template v-if="records && records.length">
              <CommonPagination
                v-model:current="pagination.current"
                v-model:size="pagination.size"
                :total="total"
                :default-page-size="10"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </template>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 详情抽屉 -->
    <el-drawer v-model="drawerVisible" :title="detailUser ? `成员详情 - ${detailUser.username}` : '成员详情'" size="40%" :with-header="true">
      <div class="user-detail">
        <div class="detail-row"><span class="label">用户名：</span><span class="value">{{ detailUser?.username }}</span></div>
        <div class="detail-row"><span class="label">昵称：</span><span class="value">{{ detailUser?.nickname }}</span></div>
        <div class="detail-row"><span class="label">邮箱：</span><span class="value">{{ detailUser?.email }}</span></div>
        <div class="detail-row"><span class="label">用户角色：</span><span class="value"><el-tag :type="getRoleTagType(detailUser?.role)">{{ getRoleLabel(detailUser?.role) }}</el-tag></span></div>
        <div class="detail-row"><span class="label">团队角色：</span><span class="value"><el-tag :type="getTeamRoleTagType(detailUser?.teamRole)">{{ getTeamRoleLabel(detailUser?.teamRole) }}</el-tag></span></div>
        <div class="detail-row"><span class="label">状态：</span><span class="value"><el-tag :type="detailUser?.status === 1 ? 'success' : 'danger'">{{ detailUser?.status === 1 ? '启用' : '禁用' }}</el-tag></span></div>
        <div class="detail-row"><span class="label">最近登录：</span><span class="value">{{ formatTime(detailUser?.lastLoginTime) }}</span></div>
        <div class="detail-row"><span class="label">加入时间：</span><span class="value">{{ formatTime(detailUser?.joinedAt) }}</span></div>
        <div class="detail-row"><span class="label">创建时间：</span><span class="value">{{ formatTime(detailUser?.createTime) }}</span></div>
      </div>
    </el-drawer>

    

    <!-- 调整团队角色对话框 -->
    <el-dialog v-model="roleDialogVisible" title="调整团队角色" width="420px" :close-on-click-modal="false">
      <div style="padding: 0 8px;">
        <el-radio-group v-model="editTeamRole">
          <el-radio-button v-for="r in teamRoleOptions" :key="r.value" :label="r.value">{{ r.label }}</el-radio-button>
        </el-radio-group>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmEditRole">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, EditPen, Delete } from '@element-plus/icons-vue'
import AppBreadcrumb from '@/components/common/Breadcrumb.vue'
import EmptyBox from '@/components/common/EmptyBox.vue'
import CommonPagination from '@/components/common/Pagination.vue'
import { useAuthStore } from '@/stores/auth'
import { teamApi } from '@/api/team/index.js'

export default {
  name: 'TeamMembers',
  components: { ArrowDown, EditPen, Delete, AppBreadcrumb, CommonPagination, EmptyBox },
  setup() {
    const route = useRoute()
    const authStore = useAuthStore()

    // 团队ID来自当前用户信息
    const teamId = ref((() => {
      const u = authStore?.getUser?.()
      return (u && u.currentTeamId != null) ? String(u.currentTeamId) : null
    })())
    const isSuperAdmin = ref(authStore?.getUser?.()?.role === 'SUPER_ADMIN')
    const teamOptions = ref([])

    const teamRoleOptions = [
      { label: '拥有者', value: 'OWNER' },
      { label: '管理员', value: 'ADMIN' },
      { label: '成员', value: 'MEMBER' }
    ]

    const query = reactive({ keyword: '', teamRole: '', status: '', teamId: null })
    const pagination = reactive({ current: 1, size: 10 })
    const sortState = reactive({ sortField: 'joinedAt', sortDirection: 'desc' })

    const loading = ref(false)
    const records = ref([])
    const total = ref(0)
    const noTeam = ref(false)

    const drawerVisible = ref(false)
    const detailUser = ref(null)

    

    const roleDialogVisible = ref(false)
    const roleEditTarget = ref(null)
    const editTeamRole = ref('MEMBER')

    const handleSearch = () => {
      pagination.current = 1
      fetchMembers()
    }

    const handleTeamChange = (val) => {
      // 仅 SUPER_ADMIN 生效：切换当前操作的团队
      if (isSuperAdmin.value) {
        teamId.value = val ? String(val) : null
        pagination.current = 1
        fetchMembers()
      }
    }

    const handleRefresh = () => {
      sortState.sortField = 'joinedAt'
      sortState.sortDirection = 'desc'
      pagination.current = 1
      fetchMembers()
      ElMessage.success('已刷新')
    }

    const handleSizeChange = (size) => {
      pagination.size = size
      pagination.current = 1
      fetchMembers()
    }

    const handleCurrentChange = (current) => {
      pagination.current = current
      fetchMembers()
    }

    const handleSortChange = ({ prop, order }) => {
      if (prop && order) {
        sortState.sortField = prop
        sortState.sortDirection = order === 'ascending' ? 'asc' : 'desc'
      } else {
        sortState.sortField = 'joinedAt'
        sortState.sortDirection = 'desc'
      }
      pagination.current = 1
      fetchMembers()
    }

    

    const handleView = (row) => {
      detailUser.value = row
      drawerVisible.value = true
    }

    const handleRowCommand = (cmd, row) => {
      if (cmd === 'role') {
        roleEditTarget.value = row
        editTeamRole.value = row.teamRole || 'MEMBER'
        roleDialogVisible.value = true
      } else if (cmd === 'transfer') {
        handleTransferOwner(row)
      } else if (cmd === 'remove') {
        handleRemove(row)
      }
    }

    const confirmEditRole = async () => {
      if (!roleEditTarget.value) return
      // UI防护：禁止修改 OWNER；禁止导致无管理员（若无OWNER）
      if (roleEditTarget.value.teamRole === 'OWNER' && editTeamRole.value !== 'OWNER') {
        ElMessage.error('不能更改团队拥有者角色')
        return
      }
      // 如果将管理员改为成员，检查当前列表管理员数量与是否存在OWNER
      if (roleEditTarget.value.teamRole === 'ADMIN' && editTeamRole.value === 'MEMBER') {
        const adminCount = records.value.filter(m => m.teamRole === 'ADMIN' && m.status === 1).length
        const hasOwner = records.value.some(m => m.teamRole === 'OWNER' && m.status === 1)
        if (adminCount <= 1 && !hasOwner) {
          ElMessage.error('至少保留一名管理员或拥有者')
          return
        }
      }
      try {
        const res = await teamApi.updateMember(String(teamId.value), String(roleEditTarget.value.userId), { role: editTeamRole.value })
        if (res && res.code === 200) {
          ElMessage.success(res.message || '已保存')
          roleDialogVisible.value = false
          fetchMembers()
        } else {
          ElMessage.error(res?.message || '保存失败')
        }
      } catch (e) {
        ElMessage.error(e?.message || '保存失败')
      }
    }

    const handleTransferOwner = async (row) => {
      // 只有当前OWNER可以转移；后端会强校验，这里做最小UI校验
      if (!row || row.status !== 1) {
        ElMessage.error('仅可转移给启用成员')
        return
      }
      // 不允许转移给当前OWNER本人
      if (row.teamRole === 'OWNER') {
        ElMessage.error('该成员已是拥有者')
        return
      }
      try {
        await ElMessageBox.confirm(`确定将拥有者转移给“${row.nickname || row.username}”吗？转移后你将成为管理员。`, '转移拥有者', { type: 'warning' })
        const res = await teamApi.transferOwner(String(teamId.value), { toUserId: String(row.userId) })
        if (res && res.code === 200) {
          ElMessage.success(res.message || '转移成功')
          // 转移成功后，刷新当前用户信息（currentTeamId/role/permissions 等）与列表
          try { await authStore.refreshUserInfo() } catch (_) { /* ignore */ }
          // 保护性二次刷新，确保菜单与权限树稳定更新
          try { await new Promise(resolve => setTimeout(resolve, 120)); await authStore.refreshUserInfo() } catch (_) { /* ignore */ }
          fetchMembers()
        } else {
          ElMessage.error(res?.message || '转移失败')
        }
      } catch (e) {
        // cancel
      }
    }

    const handleRemove = async (row) => {
      // UI防护：禁止移除OWNER；禁止移除最后一名管理员（若无OWNER）
      if (row.teamRole === 'OWNER') {
        ElMessage.error('不能移除团队拥有者')
        return
      }
      if (row.teamRole === 'ADMIN' && row.status === 1) {
        const adminCount = records.value.filter(m => m.teamRole === 'ADMIN' && m.status === 1).length
        const hasOwner = records.value.some(m => m.teamRole === 'OWNER' && m.status === 1)
        if (adminCount <= 1 && !hasOwner) {
          ElMessage.error('至少保留一名管理员或拥有者')
          return
        }
      }
      try {
        await ElMessageBox.confirm(`确定将成员“${row.nickname || row.username}”移出团队吗？`, '移除成员', { type: 'warning' })
        const res = await teamApi.removeMember(String(teamId.value), String(row.userId))
        if (res && res.code === 200) {
          ElMessage.success(res.message || '已移除')
          fetchMembers()
        } else {
          ElMessage.error(res?.message || '移除失败')
        }
      } catch (e) {
        // cancel
      }
    }

    const handleMemberStatusToggle = async (row) => {
      const target = row.status
      // UI防护：禁止禁用OWNER；禁止禁用最后一名管理员（若无OWNER）
      if (row.teamRole === 'OWNER' && target === 0) {
        ElMessage.error('不能禁用团队拥有者')
        row.status = 1
        return
      }
      if (row.teamRole === 'ADMIN' && target === 0) {
        const adminCount = records.value.filter(m => m.teamRole === 'ADMIN' && m.status === 1).length
        const hasOwner = records.value.some(m => m.teamRole === 'OWNER' && m.status === 1)
        if (adminCount <= 1 && !hasOwner) {
          ElMessage.error('至少保留一名管理员或拥有者')
          row.status = 1
          return
        }
      }
      try {
        const res = await teamApi.updateMember(String(teamId.value), String(row.userId), { status: target })
        if (res && res.code === 200) {
          ElMessage.success(res.message || '状态更新成功')
        } else {
          ElMessage.error(res?.message || '状态更新失败')
          row.status = target === 1 ? 0 : 1
        }
      } catch (e) {
        ElMessage.error(e?.message || '状态更新失败')
        row.status = target === 1 ? 0 : 1
      }
    }

    const getRoleLabel = (role) => ({ SUPER_ADMIN: '超级管理员', ADMIN: '管理员', USER: '普通用户' }[role] || role)
    const getRoleTagType = (role) => ({ SUPER_ADMIN: 'danger', ADMIN: 'warning', USER: 'success' }[role] || 'info')

    const getTeamRoleLabel = (r) => ({ OWNER: '拥有者', ADMIN: '管理员', MEMBER: '成员' }[r] || r)
    const getTeamRoleTagType = (r) => ({ OWNER: 'danger', ADMIN: 'warning', MEMBER: 'success' }[r] || 'info')

    const formatTime = (time) => {
      if (!time) return '-'
      return new Date(time).toLocaleString('zh-CN')
    }

    const hasTeamOption = (idStr) => {
      if (!idStr) return false
      return teamOptions.value.some(t => String(t.id) === String(idStr))
    }

    const ensureTeamOption = async (idStr) => {
      if (!idStr) return
      if (hasTeamOption(idStr)) return
      try {
        const res = await teamApi.detail(String(idStr))
        if (res && res.code === 200 && res.data) {
          const exists = hasTeamOption(idStr)
          if (!exists) teamOptions.value = [...teamOptions.value, res.data]
        }
      } catch (_) { void 0 }
    }

    const fetchMembers = async () => {
      if (!teamId.value) {
        // 用户信息里没有 currentTeamId，尝试从路由查询
        const queryTeamIdStr = route?.query?.teamId ? String(route.query.teamId) : null
        if (isSuperAdmin.value && queryTeamIdStr) {
          query.teamId = queryTeamIdStr
          teamId.value = String(queryTeamIdStr)
        }
        if (!teamId.value) {
          await authStore.refreshUserInfo()
          // 保护性二次刷新，避免缓存与时序问题
          try { await new Promise(resolve => setTimeout(resolve, 80)); await authStore.refreshUserInfo() } catch (_) { /* ignore */ }
          const u = authStore?.getUser?.()
          teamId.value = u && u.currentTeamId != null ? String(u.currentTeamId) : null
        }
        if (!teamId.value) {
          noTeam.value = true
          records.value = []
          total.value = 0
          return
        }
      }
      noTeam.value = false
      loading.value = true
      try {
        // SUPER_ADMIN 优先使用选择的团队过滤
        if (isSuperAdmin.value && query.teamId) {
          teamId.value = String(query.teamId)
          await ensureTeamOption(query.teamId)
        }
        const params = { pageNum: pagination.current, pageSize: pagination.size }
        if (query.teamRole) params.role = query.teamRole
        if (query.status !== '' && query.status !== null && query.status !== undefined) params.status = query.status
        const res = await teamApi.listMembers(String(teamId.value), params)
        if (res && res.code === 200 && res.data) {
          const arr = Array.isArray(res.data.records) ? res.data.records : []
          // 前端关键词过滤（后端暂不支持）
          const kw = (query.keyword || '').trim().toLowerCase()
          const filtered = kw
            ? arr.filter(u => (
                (u.username && u.username.toLowerCase().includes(kw)) ||
                (u.nickname && String(u.nickname).toLowerCase().includes(kw)) ||
                (u.email && u.email.toLowerCase().includes(kw))
              ))
            : arr
          records.value = filtered
          total.value = Number(res.data.total || filtered.length)
        } else {
          ElMessage.error(res?.message || '加载失败')
        }
      } catch (e) {
        ElMessage.error(e?.message || '加载失败')
      } finally {
        loading.value = false
      }
    }

    const applyTeamFromRoute = async () => {
      const qId = route?.query?.teamId ? String(route.query.teamId) : null
      if (qId) {
        teamId.value = String(qId)
        if (isSuperAdmin.value) {
          query.teamId = qId
          await ensureTeamOption(qId)
        }
        pagination.current = 1
        await fetchMembers()
        return true
      }
      return false
    }

    onMounted(async () => {
      if (isSuperAdmin.value) {
        // 获取全部团队供筛选
        try {
          const res = await teamApi.list({ pageNum: 1, pageSize: 200 })
          if (res && res.code === 200) {
            teamOptions.value = Array.isArray(res.data?.records) ? res.data.records : []
          }
        } catch (_) { void 0 }
        const applied = await applyTeamFromRoute()
        if (!applied) await fetchMembers()
      } else {
        await fetchMembers()
      }
    })

    watch(() => route.query.teamId, async (val) => {
      const idStr = val ? String(val) : null
      const id = idStr ? String(idStr) : null
      if (id && id !== teamId.value) {
        teamId.value = id
        if (isSuperAdmin.value) {
          query.teamId = idStr
          await ensureTeamOption(idStr)
        }
        pagination.current = 1
        await fetchMembers()
      }
    })

    return {
      // 状态
      loading,
      records,
      total,
      noTeam,
      query,
      pagination,
      teamRoleOptions,
      isSuperAdmin,
      teamOptions,

      // 详情
      drawerVisible,
      detailUser,

      

      // 角色编辑
      roleDialogVisible,
      roleEditTarget,
      editTeamRole,

      // 方法
      handleSearch,
      handleRefresh,
      handleTeamChange,
      handleSizeChange,
      handleCurrentChange,
      handleSortChange,
      
      handleView,
      handleRowCommand,
      confirmEditRole,
      handleRemove,
      handleTransferOwner,
      handleMemberStatusToggle,
      getRoleLabel,
      getRoleTagType,
      getTeamRoleLabel,
      getTeamRoleTagType,
      formatTime
    }
  }
}
</script>

<style scoped>
.team-members-page { padding: 8px 0 12px; background: transparent; display:flex; flex-direction:column; min-height: calc(100vh - var(--app-header-height) - var(--main-padding-v)); }

.team-members-page .page-content { flex:1; display:flex; min-height:0; }
.team-members-page .list-card { flex:1 1 auto; width:100%; min-height:100%; display:flex; flex-direction:column; }
.team-members-page .list-card :deep(.el-card__body) { flex:1; display:flex; flex-direction:column; min-height:0; }

.list-card { border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06); }

.filter-content { padding-top: 10px; padding-bottom: 6px; margin-bottom: 16px; }
.filter-inline { display: flex; flex-wrap: wrap; align-items: flex-end; gap: 16px 16px; margin-bottom: 20px; }
.filter-inline.second { display: none; }
.filter-actions.right.same-row { margin-left: auto; width: auto; }
.filter-item { display: flex; align-items: center; gap: 8px; }
.filter-label { min-width: 72px; font-size: 14px; color: #606266; text-align: right; white-space: nowrap; }
.filter-label.required::before { content: "*"; color: #ff4d4f; margin-right: 4px; }

.w-180 { width: 180px; }
.w-140 { width: 140px; }
.w-200 { width: 200px; }
.w-220 { width: 220px; }

.member-data-table { width: 100%; --el-table-row-height: 42px; }
.member-data-table :deep(.el-table__cell) { padding-top: 10px; padding-bottom: 10px; }

/* 让表格区域占满卡片剩余空间，分页固定在底部 */
.table-area { flex: 1 1 auto; display: flex; flex-direction: column; min-height: 0; }
.table-footer { margin-top: auto; padding-top: 8px; }

.action-buttons { display: flex; gap: 8px; justify-content: center; width: 100%; }
.color-primary { color: #1677ff; }
.color-danger { color: #ff4d4f; }

.user-detail { padding: 4px 8px; }
.detail-row { display: flex; margin-bottom: 10px; }
.detail-row .label { width: 90px; color: #666; }
.detail-row .value { color: #303133; }

@media (max-width: 1200px) { .filter-inline { gap: 12px; } }
</style>


