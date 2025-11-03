<template>
  <div class="user-list-page">
    <div class="page-content">
      <el-card class="list-card">
        <template #header>
          <div class="list-header">
            <AppBreadcrumb />
          </div>
        </template>
        <!-- 筛选与操作（单卡片内，结构对齐日志页） -->
        <div class="filter-content">
          <div class="filter-inline">
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
              <span class="filter-label">团队：</span>
              <el-select v-model="query.teamId" placeholder="选择团队" clearable filterable class="w-240" @change="handleSearch">
                <el-option label="全部团队" :value="''" />
                <el-option v-for="t in teamOptions" :key="t.id" :label="t.teamName" :value="t.id" />
              </el-select>
            </div>
            <div class="filter-item">
              <span class="filter-label">角色：</span>
              <el-select v-model="query.role" placeholder="角色" clearable class="w-180" @change="handleSearch">
                <el-option label="全部角色" value="" />
                <el-option
                  v-for="r in roleOptions"
                  :key="r.value"
                  :label="r.label"
                  :value="r.value"
                />
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
              <el-button @click="handleRefresh">刷新</el-button>
              <el-dropdown @command="handleBatchCommand">
                <el-button>
                  批量操作<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="enable">批量启用</el-dropdown-item>
                    <el-dropdown-item command="disable">批量禁用</el-dropdown-item>
                    <el-dropdown-item command="reset">批量重置密码</el-dropdown-item>
                    <el-dropdown-item command="delete" divided>批量删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-dropdown @command="handleMoreCommand">
                <el-button>
                  更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="export-all">导出全部</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>

        <!-- 表格 -->
        <el-table
          v-loading="loading"
          :data="displayedUsers"
          stripe
          row-key="id"
          class="user-data-table"
          size="small"
          :header-cell-style="{ textAlign: 'center' }"
          :cell-style="{ textAlign: 'center' }"
          height="100%"
          @selection-change="handleSelectionChange"
          @sort-change="handleSortChange"
          @row-dblclick="handleEdit"
        >
          <el-table-column type="selection" width="55" fixed="left" />

          <el-table-column prop="username" label="用户名" width="150" fixed="left">
            <template #default="{ row }">
              <el-tag type="primary" size="small">{{ row.username }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="nickname" label="昵称" width="140" />

          <el-table-column prop="email" label="邮箱" width="200" show-overflow-tooltip />

          <el-table-column prop="role" label="角色" width="120">
            <template #default="{ row }">
              <el-tag :type="getRoleTagType(row.role)" size="small">{{ getRoleLabel(row.role) }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="status" label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="lastLoginTime" label="最近登录" width="160" sortable="custom">
            <template #default="{ row }">{{ formatTime(row.lastLoginTime) }}</template>
          </el-table-column>

          <el-table-column prop="createTime" label="创建时间" width="160" sortable="custom">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>

          <el-table-column label="操作" width="240" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button size="small" type="primary" @click="() => handleView(row)">查看</el-button>
                <el-dropdown @command="(cmd) => handleRowCommand(cmd, row)">
                  <el-button size="small">
                    更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="permission" class="color-primary" :disabled="row.role === 'SUPER_ADMIN'">
                        <el-icon><EditPen /></el-icon>
                        <span>权限</span>
                      </el-dropdown-item>
                      
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>
          </el-table-column>

          <template #empty>
            <EmptyBox size="table" title="暂无用户数据" desc="可尝试变更团队、关键词或角色/状态筛选" />
          </template>
        </el-table>

        <!-- 分页器（合并至卡片内） -->
        <CommonPagination
          v-model:current="pagination.current"
          v-model:size="pagination.size"
          :total="total"
          :selected-count="selectedRows.length"
          :default-page-size="10"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </el-card>
    </div>

    

    <!-- 详情抽屉 -->
    <el-drawer v-model="drawerVisible" :title="detailUser ? `用户详情 - ${detailUser.username}` : '用户详情'" size="40%" :with-header="true">
      <div class="user-detail">
        <div class="detail-row"><span class="label">用户名：</span><span class="value">{{ detailUser?.username }}</span></div>
        <div class="detail-row"><span class="label">昵称：</span><span class="value">{{ detailUser?.nickname }}</span></div>
        <div class="detail-row"><span class="label">邮箱：</span><span class="value">{{ detailUser?.email }}</span></div>
        <div class="detail-row"><span class="label">角色：</span><span class="value"><el-tag :type="getRoleTagType(detailUser?.role)">{{ getRoleLabel(detailUser?.role) }}</el-tag></span></div>
        <div class="detail-row"><span class="label">状态：</span><span class="value"><el-tag :type="detailUser?.status === 1 ? 'success' : 'danger'">{{ detailUser?.status === 1 ? '启用' : '禁用' }}</el-tag></span></div>
        <div class="detail-row"><span class="label">最近登录：</span><span class="value">{{ formatTime(detailUser?.lastLoginTime) }}</span></div>
        <div class="detail-row"><span class="label">创建时间：</span><span class="value">{{ formatTime(detailUser?.createTime) }}</span></div>
        <div class="detail-section">
          <div class="section-title">生效权限</div>
          <div class="perm-tags">
            <el-tag v-for="p in detailPermissions" :key="p" type="info" size="small" effect="plain">{{ p }}</el-tag>
          </div>
        </div>
      </div>
    </el-drawer>

    

    <!-- 重置密码对话框 -->
    <el-dialog v-model="resetDialogVisible" title="重置密码" width="460px" :close-on-click-modal="false">
      <div class="reset-content">
        <p>确定要重置用户 <strong>{{ targetUser?.username }}</strong> 的密码吗？</p>
        <p class="tip" v-if="!resetResult">将把该用户密码重置为系统默认密码。</p>
        <div v-if="resetResult" class="reset-result">
          <el-alert type="success" :closable="false" show-icon>
            <template #title>
              已成功重置！新的登录密码：
              <code class="pwd-code">{{ resetResult }}</code>
            </template>
          </el-alert>
          <p class="tip">请妥善告知用户并建议其首次登录后尽快修改密码。</p>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="onResetDialogClose">{{ resetResult ? '关闭' : '取消' }}</el-button>
          <el-button v-if="!resetResult" type="warning" @click="confirmResetPassword">重置</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 权限对话框 -->
    <el-dialog v-model="permDialogVisible" title="用户权限" width="720px" :close-on-click-modal="false">
      <template #header>
        <div class="perm-dialog-header">
          <div class="perm-dialog-header-row">
            <div class="perm-dialog-title">用户权限</div>
            <div class="perm-dialog-tip">角色默认权限不可修改，允许额外赋权/除权</div>
          </div>
          <div class="perm-dialog-user">
            <div class="user-meta">
              <span class="meta-label">账号</span>
              <el-tag size="small" type="primary" effect="plain">{{ permTargetUser?.username }}</el-tag>
              <span class="meta-sep">/</span>
              <span class="meta-label">用户名</span>
              <el-tag size="small" effect="plain">{{ permTargetUser?.nickname || '未设置' }}</el-tag>
              <span class="meta-sep">/</span>
              <span class="meta-label">角色</span>
              <el-tag size="small" type="success" effect="plain">{{ getRoleLabel(permTargetUser?.role) }}</el-tag>
            </div>
          </div>
        </div>
      </template>
      <div v-loading="permDialogLoading">
        <el-scrollbar style="height: 420px;">
          <el-tree
            ref="permTreeRef"
            :data="permTreeData"
            :props="permTreeProps"
            show-checkbox
            :check-strictly="true"
            @check-change="onPermCheckChange"
            node-key="id"
            default-expand-all
            class="permission-tree"
          />
        </el-scrollbar>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="permDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="savePermissionDialog">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
  
</template>

<script>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, EditPen } from '@element-plus/icons-vue'
import AppBreadcrumb from '@/components/common/Breadcrumb.vue'
import CommonPagination from '@/components/common/Pagination.vue'
import EmptyBox from '@/components/common/EmptyBox.vue'
import { userApi } from '@/api/user/index.js'
import { permissionApi } from '@/api/permission/index.js'

export default {
  name: 'UserListPage',
  components: {
    ArrowDown,
    EditPen,
    AppBreadcrumb,
    CommonPagination,
    EmptyBox
  },
  setup() {
    // 功能开关（点击时提示未开放）
    const featureBatchEnabled = false
    const featureExportEnabled = false
    // 角色选项
    const roleOptions = [
      { label: '超级管理员', value: 'SUPER_ADMIN' },
      { label: '管理员', value: 'ADMIN' },
      { label: '普通用户', value: 'USER' }
    ]

    // 查询条件
    const query = reactive({
      keyword: '',
      role: '',
      status: '',
      teamId: ''
    })

    // 排序状态
    const sortState = reactive({
      sortField: 'createTime',
      sortDirection: 'desc'
    })

    // 分页
    const pagination = reactive({
      current: 1,
      size: 10
    })

    // 选择行
    const selectedRows = ref([])

    // 服务端数据
    const loading = ref(false)
    const tableData = ref([])
    const total = ref(0)
    const teamOptions = ref([])

    // 详情与对话框
    const drawerVisible = ref(false)
    const detailUser = ref(null)
    const detailPermissions = ref([])
    const dialogVisible = ref(false) // 已废弃：不再新增/编辑用户
    const formRef = ref(null)
    const dialogType = ref('add')
    const formData = reactive({})
    const formRules = {}
    const resetDialogVisible = ref(false)
    const targetUser = ref(null)
    const resetResult = ref('')

    // 计算：过滤与排序
    const filteredUsers = computed(() => {
      let data = [...tableData.value]
      // 关键词
      if (query.keyword && query.keyword.trim()) {
        const kw = query.keyword.trim().toLowerCase()
        data = data.filter(u =>
          (u.username && u.username.toLowerCase().includes(kw)) ||
          (u.nickname && String(u.nickname).toLowerCase().includes(kw)) ||
          (u.email && u.email.toLowerCase().includes(kw))
        )
      }
      // 角色
      if (query.role) {
        data = data.filter(u => u.role === query.role)
      }
      // 状态
      if (query.status !== '' && query.status !== null && query.status !== undefined) {
        data = data.filter(u => u.status === query.status)
      }
      // 排序
      if (sortState.sortField) {
        const field = sortState.sortField
        const dir = sortState.sortDirection === 'asc' ? 1 : -1
        data.sort((a, b) => {
          const va = a[field] ?? 0
          const vb = b[field] ?? 0
          if (va === vb) return 0
          return va > vb ? dir : -dir
        })
      }
      return data
    })

    // 当前页数据
    const displayedUsers = computed(() => {
      // 当前页数据已由后端分页，这里仅做页内筛选/排序
      return filteredUsers.value
    })

    // 方法区
    const handleSearch = () => {
      pagination.current = 1
      fetchList()
    }

    // eslint-disable-next-line no-unused-vars
    const onPermCheckChange = (node, checked) => {
      if (adjustingChecks.value) return
      if (!permTreeRef.value) return
      const id = node?.id
      if (id === undefined || id === null) return
      // 基线叶子或锁定父级：不允许更改，立即回滚
      if (baseSnapshot.value.has(id) || lockedParentSnapshot.value.has(id)) {
        const keys = Array.from(initialLockedChecked.value)
        permTreeRef.value.setCheckedKeys(keys)
        return
      }
      // 自动联动父级选中/取消
      const collectAncestors = (leafId) => {
        const result = []
        let p = parentOf.value?.get(leafId)
        while (p !== null && p !== undefined) {
          result.push(p)
          p = parentOf.value.get(p)
        }
        return result
      }
      if (checked) {
        const ancestors = collectAncestors(id)
        if (ancestors.length) {
          adjustingChecks.value = true
          ancestors.forEach(a => permTreeRef.value.setChecked(a, true, false))
          adjustingChecks.value = false
        }
      } else {
        const checkedLeaves = new Set(permTreeRef.value.getCheckedKeys(true) || [])
        const ancestors = collectAncestors(id)
        if (ancestors.length) {
          adjustingChecks.value = true
          ancestors.forEach(a => {
            if (lockedParentSnapshot.value.has(a) || baseSnapshot.value.has(a)) return
            let needed = false
            checkedLeaves.forEach(leaf => {
              if (needed) return
              const chain = collectAncestors(leaf)
              if (chain.includes(a)) needed = true
            })
            if (!needed) permTreeRef.value.setChecked(a, false, false)
          })
          adjustingChecks.value = false
        }
      }
      // 允许修改的叶子：同步更新快照集合（不加入锁定）
      const current = new Set(permTreeRef.value.getCheckedKeys(true))
      initialLockedChecked.value = new Set([
        ...Array.from(initialLockedChecked.value).filter(k => baseSnapshot.value.has(k) || lockedParentSnapshot.value.has(k)),
        ...Array.from(current).filter(k => !baseSnapshot.value.has(k) && !lockedParentSnapshot.value.has(k))
      ])
    }

    const handleSelectionChange = (selection) => {
      selectedRows.value = selection || []
    }

    const handleSortChange = ({ prop, order }) => {
      if (prop && order) {
        sortState.sortField = prop
        sortState.sortDirection = order === 'ascending' ? 'asc' : 'desc'
      } else {
        sortState.sortField = 'createTime'
        sortState.sortDirection = 'desc'
      }
      pagination.current = 1
    }

    const handleSizeChange = (size) => {
      pagination.size = size
      pagination.current = 1
      fetchList()
    }

    const handleCurrentChange = (current) => {
      pagination.current = current
      fetchList()
    }

    const handleRefresh = () => {
      sortState.sortField = 'createTime'
      sortState.sortDirection = 'desc'
      pagination.current = 1
      fetchList()
      ElMessage.success('已刷新')
    }

    const handleAdd = () => {}

    const handleEdit = () => {}

    const handleDelete = async () => {}

    const handleSubmit = async () => {}

    const openResetDialog = (row) => {
      targetUser.value = row
      resetResult.value = ''
      resetDialogVisible.value = true
    }

    const confirmResetPassword = async () => {}

    const onResetDialogClose = () => {
      resetDialogVisible.value = false
      resetResult.value = ''
    }

    const handleStatusToggle = async () => {}

    const handleBatchCommand = async (cmd) => {
      if (!featureBatchEnabled) {
        ElMessage.info('批量操作功能暂不开放')
        return
      }
      if (!selectedRows.value.length) {
        ElMessage.warning('请选择要操作的用户')
        return
      }
      const ids = selectedRows.value.map(u => u.id)
      try {
        if (cmd === 'delete') {
          await ElMessageBox.confirm(`确定删除选中的 ${ids.length} 个用户吗？`, '批量删除', { type: 'warning' })
          // 待后端提供批量接口
          selectedRows.value = []
          ElMessage.success('批量删除成功（演示）')
        } else if (cmd === 'enable' || cmd === 'disable') {
          const val = cmd === 'enable' ? 1 : 0
          selectedRows.value = []
          ElMessage.success(`批量${val === 1 ? '启用' : '禁用'}成功（演示）`)
        } else if (cmd === 'reset') {
          ElMessage.success('批量重置密码成功（演示）')
        }
      } catch (e) {
        // cancel
      }
    }

    const handleMoreCommand = (cmd) => {
      if (!featureExportEnabled) {
        ElMessage.info('导出功能暂不开放')
        return
      }
      if (cmd === 'export-all') {
        ElMessage.info('导出功能暂未开放（演示）')
      }
    }

    const permDialogVisible = ref(false)
    const permDialogLoading = ref(false)
    const permTargetUser = ref(null)
    const permTreeRef = ref(null)
    const permTreeData = ref([])
    const permTreeProps = { children: 'children', label: 'permissionName', disabled: 'disabled' }
    const baseSnapshot = ref(new Set())
    const lockedParentSnapshot = ref(new Set())
    const initialLockedChecked = ref(new Set())
    const baselineAllIds = ref([])
    const parentOf = ref(new Map())
    const adjustingChecks = ref(false)

    const openPermissionDialog = async (row) => {
      permTargetUser.value = row
      permDialogVisible.value = true
      permDialogLoading.value = true
      try {
        const [treeRes, roleRes, overrideRes] = await Promise.all([
          permissionApi.getPermissionTree(),
          permissionApi.getRolePermissions(row.role),
          permissionApi.getUserOverrides(row.id)
        ])
        // tree
        permTreeData.value = Array.isArray(treeRes?.data) ? treeRes.data : []

        // 统一ID类型（以树节点的id类型为准）
        const findFirstId = (arr) => {
          let fid = null
          const dfs = (nodes) => {
            if (!nodes || fid !== null) return
            for (const n of nodes) {
              if (n && n.id !== undefined && n.id !== null) { fid = n.id; break }
              if (n && n.children) dfs(n.children)
            }
          }
        
          dfs(arr)
          return fid
        }
        const firstId = findFirstId(permTreeData.value)
        const idIsNumber = typeof firstId === 'number'
        const castIds = (ids) => (Array.isArray(ids) ? ids.map(v => idIsNumber ? Number(v) : String(v)) : [])

        // 收集父节点ID集合，用于识别叶子
        const parentIdSet = new Set()
        const parentMap = new Map()
        const collectParents = (arr, parentId = null) => {
          if (!Array.isArray(arr)) return
          arr.forEach(n => {
            if (Array.isArray(n.children) && n.children.length > 0) {
              parentIdSet.add(n.id)
              parentMap.set(n.id, parentId)
              collectParents(n.children, n.id)
            } else {
              parentMap.set(n.id, parentId)
            }
          })
        }
        collectParents(permTreeData.value)
        parentOf.value = parentMap

        // 基线：优先使用后端返回的 baselinePermissionIds；兜底使用角色权限
        const baselineIdsRaw = Array.isArray(overrideRes?.data?.baselinePermissionIds)
          ? overrideRes.data.baselinePermissionIds
          : (roleRes?.data || []).map(p => p.id)
        const baselineIds = castIds(baselineIdsRaw)
        baselineAllIds.value = baselineIds
        const baselineLeafIds = (baselineIds || []).filter(id => !parentIdSet.has(id))

        // 用户已有覆盖：仅取叶子，保持可编辑
        const allowIdsRaw = Array.isArray(overrideRes?.data?.allowPermissionIds) ? overrideRes.data.allowPermissionIds : []
        const allowIds = castIds(allowIdsRaw)

        // 锁定的父级（当任一子项在基线内时，父级需选中且禁用）
        let lockedParentIds = Array.isArray(overrideRes?.data?.lockedParentPermissionIds)
          ? castIds(overrideRes.data.lockedParentPermissionIds) : []

        // 若后端未返回锁定父级，则在前端根据基线叶子反推所有祖先作为锁定父级
        if (!lockedParentIds || lockedParentIds.length === 0) {
          const collectAncestors = (ids) => {
            const result = new Set()
            ;(ids || []).forEach(id => {
              let p = parentMap.get(id)
              while (p !== null && p !== undefined) {
                result.add(p)
                p = parentMap.get(p)
              }
            })
            return Array.from(result)
          }
          lockedParentIds = collectAncestors(baselineLeafIds)
        }
        lockedParentSnapshot.value = new Set(lockedParentIds)

        // 基线叶子（不可编辑）
        baseSnapshot.value = new Set(baselineLeafIds)

        // 禁用基线叶子节点与需锁定的父级
        const disableBase = (arr) => {
          if (!Array.isArray(arr)) return
          arr.forEach(n => {
            if (baseSnapshot.value.has(n.id) || lockedParentSnapshot.value.has(n.id)) n.disabled = true
            if (n.children) disableBase(n.children)
          })
        }
        disableBase(permTreeData.value)

        await nextTick()
        // 勾选：基线叶子 + 已有覆盖叶子 + 需锁定的父级
        const initialChecked = Array.from(new Set([...
          baselineLeafIds, ...allowIds, ...lockedParentIds
        ]))
        permTreeRef.value && permTreeRef.value.setCheckedKeys(initialChecked)

        // 记录初始勾选集用于后续校正
        initialLockedChecked.value = new Set(initialChecked)
      } catch (e) {
        // ignore
      } finally {
        permDialogLoading.value = false
      }
    }

    const savePermissionDialog = async () => {
      if (!permTreeRef.value || !permTargetUser.value) return
      // 提交“所有选中节点（含父级）”作为增量，且包含角色基线
      const checkedAllIds = permTreeRef.value.getCheckedKeys(false) || []
      const extras = checkedAllIds.filter(id => !baseSnapshot.value.has(id))
      const allowFullSet = Array.from(new Set([...(baselineAllIds.value || []), ...extras]))
      try {
        const payload = {
          userId: Number(permTargetUser.value.id),
          allowPermissionIds: allowFullSet.map(v => Number(v)),
          denyPermissionIds: []
        }
        const res = await permissionApi.updateUserOverrides(payload)
        if (res && res.code === 200) {
          ElMessage.success(res.message || '已保存')
          permDialogVisible.value = false
        } else {
          ElMessage.error(res?.message || '保存失败')
        }
      } catch (e) {
        ElMessage.error(e?.message || '保存失败')
      }
    }

    const handleRowCommand = (cmd, row) => {
      if (cmd === 'edit') {
        handleEdit(row)
      } else if (cmd === 'permission') {
        openPermissionDialog(row)
      } else if (cmd === 'reset') {
        openResetDialog(row)
      } else if (cmd === 'delete') {
        handleDelete(row)
      }
    }

    const handleView = async (row) => {
      detailUser.value = row
      drawerVisible.value = true
      try {
        const res = await permissionApi.getUserEffectivePermissions(row.id)
        if (res && res.code === 200 && Array.isArray(res.data)) {
          // 显示权限名称；若无名称则退回编码
          detailPermissions.value = res.data.map(p => p.permissionName || p.permissionCode)
        } else {
          detailPermissions.value = []
        }
      } catch (e) {
        detailPermissions.value = []
      }
    }

    const getRoleLabel = (role) => {
      const map = { SUPER_ADMIN: '超级管理员', ADMIN: '管理员', USER: '普通用户' }
      return map[role] || role
    }

    const getRoleTagType = (role) => {
      const map = { SUPER_ADMIN: 'danger', ADMIN: 'warning', USER: 'success' }
      return map[role] || 'info'
    }

    const formatTime = (time) => {
      if (!time) return '-'
      return new Date(time).toLocaleString('zh-CN')
    }

    const fetchList = async () => {
      loading.value = true
      try {
        const params = { page: pagination.current, size: pagination.size }
        if (query.teamId) params.teamId = query.teamId
        const res = await userApi.getUserList(params)
        if (res && res.code === 200 && res.data) {
          tableData.value = Array.isArray(res.data.records) ? res.data.records : []
          total.value = Number(res.data.total || 0)
        } else {
          ElMessage.error(res?.message || '加载失败')
        }
      } catch (e) {
        ElMessage.error(e?.message || '加载失败')
      } finally {
        loading.value = false
      }
    }

    const fetchTeamOptions = async () => {
      try {
        const res = await (await import('@/api/team/index.js')).teamApi.list({ pageNum: 1, pageSize: 200 })
        const records = Array.isArray(res?.data?.records) ? res.data.records : []
        teamOptions.value = records.map(r => ({ id: r.id, teamName: r.teamName }))
      } catch (_) { teamOptions.value = [] }
    }

    onMounted(() => {
      fetchTeamOptions()
      fetchList()
    })

    return {
      // 功能开关
      featureBatchEnabled,
      featureExportEnabled,
      // 状态
      roleOptions,
      query,
      sortState,
      pagination,
      selectedRows,
      loading,
      tableData,
      total,
      filteredUsers,
      displayedUsers,
      teamOptions,

      // 详情与对话框
      drawerVisible,
      detailUser,
      detailPermissions,
      dialogVisible,
      dialogType,
      formRef,
      formData,
      formRules,
      resetDialogVisible,
      targetUser,
      resetResult,

      // 权限对话框
      permDialogVisible,
      permDialogLoading,
      permTargetUser,
      permTreeRef,
      permTreeData,
      permTreeProps,
      baseSnapshot,

      // 方法
      handleSearch,
      handleSelectionChange,
      handleSortChange,
      handleSizeChange,
      handleCurrentChange,
      handleRefresh,
      handleAdd,
      handleEdit,
      handleDelete,
      handleSubmit,
      openResetDialog,
      confirmResetPassword,
      onResetDialogClose,
      handleStatusToggle,
      handleBatchCommand,
      handleMoreCommand,
      handleRowCommand,
      handleView,
      openPermissionDialog,
      savePermissionDialog,
      onPermCheckChange,
      getRoleLabel,
      getRoleTagType,
      formatTime,
      fetchList
    }
  }
}
</script>

<style scoped>
.user-list-page { padding: 8px 0 12px; background: transparent; display:flex; flex-direction:column; min-height: calc(100vh - var(--app-header-height) - var(--main-padding-v)); }

.user-list-page .page-content { flex:1; display:flex; min-height:0; }

.list-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  flex: 1 1 auto;
  width: 100%;
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

.list-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

/* 参考日志页的筛选布局样式 */
.filter-content {
  padding-top: 10px;
  /* 增加与表格的间距：通过内边距避免外边距塌陷，再加底部外边距 */
  padding-bottom: 6px;
  margin-bottom: 16px;
}

.filter-inline {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 16px 16px;
  margin-bottom: 20px;
}

/* 已合并按钮至首行 */
.filter-inline.second { display: none; }

/* 让按钮与筛选同一水平行靠右贴边 */
.filter-actions.right.same-row {
  margin-left: auto;
  width: auto; /* 避免充满整行导致换行 */
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  min-width: 60px;
  font-size: 14px;
  color: #606266;
  text-align: right;
  white-space: nowrap;
}

/* 宽度工具类，和日志页保持一致 */
.w-160 { width: 160px; }
.w-180 { width: 180px; }
.w-140 { width: 140px; }
.w-200 { width: 200px; }
.w-220 { width: 220px; }
.w-240 { width: 240px; }
.w-260 { width: 260px; }
.w-300 { width: 300px; }
.w-340 { width: 340px; }
.w-380 { width: 380px; }

/* 列表卡片头部（与日志页一致） */
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.list-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

/* 行内操作按钮右对齐 */
.filter-actions.right {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  width: 100%;
}

.user-data-table {
  width: 100%;
  /* 略微增大行高 */
  --el-table-row-height: 42px;
  flex: 1 1 auto;
}

/* 兼容性兜底：增大单元格上下内边距，轻微提升行高 */
.user-data-table :deep(.el-table__cell) {
  padding-top: 10px;
  padding-bottom: 10px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  width: 100%;
}

/* 下拉项颜色强化 */
.color-primary { color: #1677ff; }
.color-warning { color: #faad14; }
.color-danger { color: #ff4d4f; }

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.user-detail {
  padding: 4px 8px;
}

.detail-row {
  display: flex;
  margin-bottom: 10px;
}

.detail-row .label {
  width: 90px;
  color: #666;
}

.detail-row .value {
  color: #303133;
}

.detail-section {
  margin-top: 16px;
}

.section-title {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 10px;
}

.perm-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.permission-tree {
  --el-tree-node-content-height: 30px;
}

/* 权限对话框头部样式 */
.perm-dialog-header {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding-top: 6px;
}

.perm-dialog-title {
  font-weight: 600;
  font-size: 16px;
  color: #1f2937;
}

.perm-dialog-header-row {
  display: flex;
  align-items: baseline;
  justify-content: flex-start;
  gap: 12px;
}

.perm-dialog-user {
  font-size: 13px;
  color: #64748b;
  display: flex;
  justify-content: center;
}

.perm-username {
  color: #1677ff;
  font-weight: 700;
}

.perm-role {
  color: #16a34a;
}

.perm-dialog-tip {
  font-size: 12px;
  color: #94a3b8;
}

/* 用户信息元数据行 */
.user-meta {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  color: #475569;
}

.meta-label {
  color: #94a3b8;
}

.meta-sep {
  color: #cbd5e1;
}

/* 权限名称颜色优化 */
.permission-tree :deep(.el-tree-node__label) {
  color: #334155;
}

.permission-tree :deep(.el-tree-node__content:hover .el-tree-node__label) {
  color: #1677ff;
}

.permission-tree :deep(.is-disabled .el-tree-node__label) {
  color: #94a3b8;
}

/* 响应式 */
@media (max-width: 1200px) {
  .filter-inline { gap: 12px; }
}

@media (max-width: 768px) {
  .user-list-page { padding: 10px; }
}
</style>