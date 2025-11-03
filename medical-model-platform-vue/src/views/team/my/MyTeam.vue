<template>
  <div class="my-team-page">
    <el-card class="content-card">
      <template #header>
        <AppBreadcrumb />
      </template>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container" v-loading="loading">
        <p>正在加载团队信息...</p>
      </div>

      <template v-else>
        <!-- SUPER_ADMIN 视图：系统所有团队卡片列表 -->
        <div v-if="isSuperAdmin" class="all-teams-content">
          <div v-if="teamsLoading" class="loading-container" v-loading="teamsLoading">
            <p>正在加载全部团队...</p>
          </div>
          <template v-else>
            <EmptyBox
              v-if="!teamsPage.records.length"
              size="full"
              title="暂无团队"
              desc="当前系统还没有团队，您可以创建一个新团队"
              :skeleton="true"
            >
              <template #actions>
                <el-button type="primary" @click="openCreateTeam">创建团队</el-button>
              </template>
            </EmptyBox>
            <div v-else class="teams-grid">
              <el-card
                v-for="t in teamsPage.records"
                :key="t.id"
                class="team-card"
                shadow="hover"
                @click="() => openManageDrawer(t)"
              >
                <div class="team-card-header">
                  <div class="team-card-title">
                    <span class="badge">TEAM</span>
                    {{ t.teamName }}
                  </div>
                  <el-tag :type="t.status === 1 ? 'success' : 'danger'" size="small" effect="dark">
                    {{ t.status === 1 ? '正常' : '已禁用' }}
                  </el-tag>
                </div>
                <div class="team-card-body">
                  <div class="team-card-row"><span class="label">拥有者：</span><span class="value">{{ t.ownerName || '—' }}</span></div>
                  <div class="team-card-row"><span class="label">成员数：</span><span class="value">{{ t.memberCount ?? 0 }}</span></div>
                  <div v-if="t.description" class="team-card-desc">{{ t.description }}</div>
                </div>
                <div class="team-card-actions" @click.stop>
                  <el-button size="small" @click="$router.push({ path: '/team/members', query: { teamId: t.id } })">查看成员</el-button>
                  <el-button size="small" type="primary" plain @click="() => openManageDrawer(t)">管理</el-button>
                </div>
              </el-card>
            </div>
          </template>
        </div>

        <!-- 普通用户/管理员视图：仅显示所属团队（与当前一致） -->
        <template v-else>
          <!-- 无团队空态 -->
          <EmptyBox
            v-if="!team"
            size="full"
            title="暂无所属团队"
            desc="您当前尚未加入任何团队，可通过邀请链接加入或创建一个新团队"
            :skeleton="true"
          >
            <template #actions>
              <el-button type="success" @click="openJoinDialog">加入团队</el-button>
              <el-button type="primary" @click="openCreateTeam">创建团队</el-button>
            </template>
          </EmptyBox>

          <!-- 团队信息与成员表格 -->
          <div v-else class="team-content">
            <!-- 团队基础信息（Hero 样式卡片） -->
            <el-card shadow="never" class="team-info-card">
              <template #header>
                <div class="card-header">
                  <div class="title">团队信息</div>
                  <div class="actions">
                    <template v-if="isOwner && editBase">
                      <el-button size="small" @click="cancelEdit">取消</el-button>
                      <el-button size="small" type="primary" :loading="savingBase" @click="saveEdit">保存</el-button>
                    </template>
                    <el-button v-if="!editBase" size="small" class="action-btn action-btn--success" @click="openInviteDialog">
                      <el-icon><CirclePlus /></el-icon>
                      <span>邀请成员</span>
                    </el-button>
                    <el-dropdown v-if="!editBase && team" trigger="hover" @command="handleMoreCommand">
                      <el-button size="small" class="action-btn">
                        <el-icon><MoreFilled /></el-icon>
                        <span>更多</span>
                      </el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item v-if="isOwner" command="edit">
                            修改团队信息
                          </el-dropdown-item>
                          <el-dropdown-item command="exit" :disabled="isOwner">
                            退出团队
                          </el-dropdown-item>
                          <el-dropdown-item v-if="isOwner" command="dissolve" divided>
                            解散团队
                          </el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </div>
                </div>
              </template>

              <template v-if="!editBase">
                <div class="team-hero">
                  <div class="hero-main">
                    <div class="hero-left">
                      <div class="team-icon" aria-hidden="true">
                        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                          <circle cx="9" cy="7" r="4"/>
                          <path d="M2 21c0-4 3-7 7-7"/>
                          <circle cx="17" cy="7" r="3"/>
                          <path d="M17 14c3 0 5 2 5 5"/>
                        </svg>
                      </div>
                      <div class="hero-meta">
                      <div class="team-name-row">
                        <div class="team-name">{{ team.teamName }}</div>
                        <el-tag :type="team.status === 1 ? 'success' : 'info'" effect="plain" size="small" class="status-tag">
                          {{ team.status === 1 ? '正常' : '已禁用' }}
                        </el-tag>
                      </div>
                      <div class="team-sub">
                        <span class="owner-label">拥有者</span>
                        <el-tag size="small" type="primary" effect="plain">{{ ownerNicknameOrName }}</el-tag>
                        <span class="dot">•</span>
                        <span class="member-count">成员数 :  {{ team.memberCount ?? members.total ?? 0 }}</span>
                      </div>
                      </div>
                    </div>
                    <div v-if="team.teamCode" class="hero-right">
                      <div class="team-code-badge" @click="copyTeamCode" title="点击复制团队码">
                        <span class="code-label">团队码</span>
                        <span class="code-value">{{ team.teamCode }}</span>
                      </div>
                    </div>
                  </div>
                  <div class="hero-desc" v-if="team.description">
                    {{ team.description }}
                  </div>
                </div>
              </template>

              <template v-else>
                <div class="form-section">
                  <div class="form-section-title">基础信息</div>
                  <el-form :model="editForm" label-width="96px" class="team-edit-form">
                    <el-form-item label="团队名称">
                      <div class="field-col">
                        <el-input v-model="editForm.teamName" maxlength="10" show-word-limit placeholder="请输入团队名称（最多10个字符）" />
                        <div class="field-help">建议 4-8 个字符，简洁易识别。</div>
                      </div>
                    </el-form-item>
                    <el-form-item label="团队描述">
                      <div class="field-col">
                        <el-input v-model="editForm.description" type="textarea" :rows="3" maxlength="255" show-word-limit placeholder="请输入团队描述（最多255个字符）" />
                        <div class="field-help">用一句话介绍您的团队。</div>
                      </div>
                    </el-form-item>
                  </el-form>
                </div>
              </template>
            </el-card>

    <!-- 邀请成员对话框 -->
    <el-dialog v-model="inviteVisible" title="邀请成员" width="560px" :close-on-click-modal="false">
      <div class="invite-block">
        <div class="row">
          <span class="label">团队码</span>
          <div class="value">
            <el-input :model-value="team?.teamCode || ''" placeholder="—" readonly class="code-input">
              <template #append>
                <el-button :icon="DocumentCopy" @click="copyTeamCode">复制</el-button>
              </template>
            </el-input>
          </div>
        </div>
        <div class="row">
          <span class="label">邀请链接</span>
          <div class="value">
            <el-input v-model="inviteLink" readonly class="link-input">
              <template #prefix>
                <el-icon><LinkIcon /></el-icon>
              </template>
              <template #append>
                <el-button :icon="DocumentCopy" @click="copyInviteLink" />
              </template>
            </el-input>
          </div>
        </div>
        <el-alert type="info" :closable="false" show-icon>
          新用户可在注册页填写团队码完成注册并自动加入该团队；已注册用户可在
          <el-link class="inline-link" type="primary" :underline="true" @click="$router.push('/team/my')">我的团队-加入团队</el-link>
          中加入。
        </el-alert>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="inviteVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
            <!-- 模型/AI计算统计（预留位） -->
            <el-card shadow="never" class="overview-card">
              <template #header>
                <div class="card-header">
                  <div class="title">模型 / AI 计算统计（预留）</div>
                  <div class="actions"></div>
                </div>
              </template>
              <EmptyBox size="card" title="统计面板开发中，功能即将上线" icon="search" :skeleton="true" />
            </el-card>
          </div>
        </template>
      </template>
    </el-card>
    
    <!-- 创建团队对话框 -->
    <el-dialog v-model="createVisible" title="创建团队" width="480px" :close-on-click-modal="false">
      <div class="dialog-intro">创建一个新的团队，您将成为该团队的拥有者</div>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="84px" class="create-form">
        <el-form-item label="团队名称" prop="teamName">
          <el-input v-model="createForm.teamName" maxlength="10" show-word-limit placeholder="请输入团队名称（最多10个字符）" />
          <div class="form-help">名称建议简短清晰，创建后可在“团队信息”中修改</div>
        </el-form-item>
        <el-form-item label="团队描述">
          <el-input v-model="createForm.description" type="textarea" :rows="3" maxlength="255" show-word-limit placeholder="可选，简单介绍团队（最多255个字符）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCreate">创建</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 加入团队对话框 -->
    <el-dialog v-model="joinVisible" title="加入团队" width="560px" :close-on-click-modal="false">
      <div class="join-block">
        <div class="row">
          <span class="label">团队码</span>
          <div class="value">
            <el-input v-model="joinCode" maxlength="64" placeholder="请输入团队码">
              <template #prefix>
                <el-icon><LinkIcon /></el-icon>
              </template>
              <template #append>
                <el-button :type="needQuery ? 'primary' : (joinPreview ? 'success' : 'default')" :loading="previewLoading" @click="doPreview">查询</el-button>
              </template>
            </el-input>
          </div>
        </div>

        <div v-if="invalidCode" class="error-tip">团队码不存在或该团队已被禁用，请联系管理员确认</div>

        <div v-if="joinPreview" class="preview-card">
          <div class="preview-header">
            <span class="name">{{ joinPreview.teamName }}</span>
            <el-tag :type="joinPreview.status === 1 ? 'success' : 'info'" effect="plain" size="small">{{ joinPreview.status === 1 ? '正常' : '已禁用' }}</el-tag>
          </div>
          <div class="preview-meta">
            <span class="owner">拥有者：{{ joinPreview.ownerName || '—' }}</span>
            <span class="dot">•</span>
            <span class="mc">成员数：{{ joinPreview.memberCount ?? 0 }}</span>
          </div>
          <div v-if="joinPreview.description" class="preview-desc">{{ joinPreview.description }}</div>
        </div>

        <div class="row">
          <span class="label">申请理由</span>
          <div class="value">
            <el-input v-model="joinReason" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="可选，最多200字" />
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="joinVisible = false">取消</el-button>
          <el-button type="primary" :disabled="!canSubmitJoin" :loading="joinSubmitting" @click="submitJoin">申请加入</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 管理抽屉 -->
    <el-drawer v-model="manageDrawerVisible" size="480px" :with-header="true" :title="manageTeam ? `管理团队 - ${manageTeam.teamName}` : '管理团队'">
      <div class="manage-content">
        <el-form :model="manageForm" label-width="90px" class="manage-form">
          <el-form-item label="团队名称">
            <el-input v-model="manageForm.teamName" placeholder="团队名称" disabled />
          </el-form-item>
          <el-form-item label="团队描述">
            <el-input v-model="manageForm.description" type="textarea" :rows="3" placeholder="团队描述" disabled />
          </el-form-item>
          <el-form-item label="状态">
            <el-switch
              v-model="manageForm.status"
              :active-value="1"
              :inactive-value="0"
              :style="{ '--el-switch-on-color': '#10b981', '--el-switch-off-color': '#ef4444' }"
              @change="toggleTeamStatus"
            />
          </el-form-item>
          <el-form-item label="拥有者">
            <el-tag type="primary" effect="plain">{{ manageTeam?.ownerName || '—' }}</el-tag>
          </el-form-item>
          <el-form-item label="成员数量">
            <span>{{ manageTeam?.memberCount ?? 0 }}</span>
          </el-form-item>
          <el-form-item label="管理员数">
            <span>{{ manageTeam?.adminCount ?? 0 }}</span>
          </el-form-item>
          <el-form-item label="创建时间">
            <span>{{ formatDateTime(manageTeam?.createTime) }}</span>
          </el-form-item>
          <el-form-item label="更新时间">
            <span>{{ formatDateTime(manageTeam?.updateTime) }}</span>
          </el-form-item>
        </el-form>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref, computed, reactive, watch } from 'vue'
import EmptyBox from '@/components/common/EmptyBox.vue'
import AppBreadcrumb from '@/components/common/Breadcrumb.vue'
import { teamApi } from '@/api/team'
import { useAuthStore } from '@/stores/auth'
import { CirclePlus, Link as LinkIcon, DocumentCopy, MoreFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { teamJoinRequestApi } from '@/api/team/join-request'

const loading = ref(false)
const teamsLoading = ref(false)
const membersLoading = ref(false)
const team = ref(null)
const members = ref({ records: [], total: 0, current: 1, size: 10 })
const teamsPage = ref({ records: [], total: 0, current: 1, size: 12 })

// 角色判定
const authStore = useAuthStore()
const isSuperAdmin = computed(() => {
  const role = authStore?.getUser?.()?.role
  return role === 'SUPER_ADMIN'
})

// 创建团队对话框
const createVisible = ref(false)
const createFormRef = ref(null)
const createForm = reactive({ teamName: '', description: '' })
const createRules = {
  teamName: [
    { required: true, message: '请输入团队名称', trigger: 'blur' },
    { min: 2, max: 10, message: '长度 2-10 个字符', trigger: 'blur' }
  ]
}

const ownerNicknameOrName = computed(() => {
  const t = team.value || {}
  return t.ownerName || '—'
})

// 是否为团队拥有者（仅拥有者可编辑基础信息）
const isOwner = computed(() => {
  const t = team.value
  const current = authStore?.getUser?.()
  const currentId = Number(current?.userId ?? current?.id)
  const ownerId = Number(t?.ownerUserId)
  return Boolean(ownerId && currentId && ownerId === currentId)
})

// 管理抽屉
const manageDrawerVisible = ref(false)
const manageTeam = ref(null)
const manageSaving = ref(false)
const manageForm = reactive({ id: null, teamName: '', description: '', status: 1 })

function formatDateTime(value) {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN')
}

function openManageDrawer(t) {
  manageTeam.value = t
  manageForm.id = t.id
  manageForm.teamName = t.teamName || ''
  manageForm.description = t.description || ''
  manageForm.status = typeof t.status === 'number' ? t.status : 1
  manageDrawerVisible.value = true
}

async function saveManage() {
  if (!manageForm.id) return
  manageSaving.value = true
  try {
    let res
    if (isSuperAdmin.value) {
      // 超级管理员仅调用专用状态接口
      res = await teamApi.setStatus(manageForm.id, manageForm.status)
    } else {
      const payload = { id: manageForm.id, teamName: manageForm.teamName, description: manageForm.description }
      res = await teamApi.update(manageForm.id, payload)
    }
    if (res && res.code === 200) {
      manageDrawerVisible.value = false
      // 刷新列表
      await fetchAllTeams(teamsPage.value.current, teamsPage.value.size)
    }
  } finally {
    manageSaving.value = false
  }
}

async function toggleTeamStatus() {
  if (!manageForm.id) return
  // 直接复用保存逻辑，仅更新状态
  await saveManage()
}

async function fetchMyTeam() {
  loading.value = true
  try {
    const res = await teamApi.list({ pageNum: 1, pageSize: 1 })
    const data = res?.data
    const recs = data?.records || []
    team.value = recs[0] || null
    if (team.value) {
      await fetchMembers(members.value.current, members.value.size)
    } else {
      members.value = { records: [], total: 0, current: 1, size: 10 }
    }
  } catch (e) {
    // 出错时也要清空本地状态，避免显示旧数据
    team.value = null
    members.value = { records: [], total: 0, current: 1, size: 10 }
  } finally {
    loading.value = false
  }
}

async function fetchAllTeams(page = 1, size = 12) {
  teamsLoading.value = true
  try {
    const res = await teamApi.list({ pageNum: page, pageSize: size })
    const data = res?.data || {}
    teamsPage.value = {
      records: Array.isArray(data.records) ? data.records : [],
      total: data.total || 0,
      current: data.current || page,
      size: data.size || size
    }
  } finally {
    teamsLoading.value = false
  }
}

async function fetchMembers(page = 1, size = 10) {
  if (!team.value) return
  membersLoading.value = true
  try {
    const res = await teamApi.listMembers(team.value.id, { pageNum: page, pageSize: size })
    const data = res?.data || {}
    members.value = {
      records: Array.isArray(data.records) ? data.records : [],
      total: data.total || 0,
      current: data.current || page,
      size: data.size || size
    }
  } finally {
    membersLoading.value = false
  }
}


function openCreateTeam() {
  createVisible.value = true
}

async function submitCreate() {
  try {
    await createFormRef.value?.validate?.()
  } catch (e) { return }
  const payload = { teamName: createForm.teamName?.trim(), description: createForm.description?.trim() }
  const res = await teamApi.create(payload)
  if (res && res.code === 200) {
    createVisible.value = false
    // 重置表单
    createForm.teamName = ''
    createForm.description = ''
    // 创建团队成功后，刷新前端用户缓存（currentTeamId/currentTeamName 等）
    await authStore.refreshUserInfo()
    // 根据角色刷新视图数据
    if (isSuperAdmin.value) {
      await fetchAllTeams(teamsPage.value.current, teamsPage.value.size)
    } else {
      await fetchMyTeam()
    }
  }
}

onMounted(async () => {
  // 进入页面强制刷新一次用户信息，确保 currentTeamId/currentTeamName 等会话数据与后端同步
  try { await authStore.refreshUserInfo() } catch (_) { /* 忽略刷新失败，继续加载页面数据 */ }
  if (isSuperAdmin.value) {
    await fetchAllTeams()
  } else {
    await fetchMyTeam()
  }
})

// 基础信息编辑（拥有者）
const editBase = ref(false)
const savingBase = ref(false)
const editForm = reactive({ teamName: '', description: '' })

// 邀请对话框
const inviteVisible = ref(false)
const inviteLink = ref('')
const inviteBaseUrl = ref('')

// 加入团队对话框
const joinVisible = ref(false)
const joinCode = ref('')
const joinReason = ref('')
const joinPreview = ref(null)
const previewLoading = ref(false)
const joinSubmitting = ref(false)
const lastQueriedCode = ref('')
const invalidCode = ref(false)
// 提取团队码：支持直接输入团队码或邀请链接（如 /register?teamCode=XXXX）
function extractTeamCode(input) {
  const raw = (input || '').trim()
  if (!raw) return { code: '', isLink: false }
  const looksLikeUrl = /^https?:\/\//i.test(raw) || /^www\./i.test(raw)
  if (looksLikeUrl) {
    try {
      const url = new URL(raw.startsWith('http') ? raw : `https://${raw}`)
      const code = (url.searchParams.get('teamCode') || '').trim()
      if (code) return { code, isLink: true }
      const m = raw.match(/[?&]teamCode=([^&#]+)/i)
      if (m && m[1]) return { code: decodeURIComponent(m[1]).trim(), isLink: true }
      return { code: '', isLink: true }
    } catch (_) {
      const m = raw.match(/[?&]teamCode=([^&#]+)/i)
      if (m && m[1]) return { code: decodeURIComponent(m[1]).trim(), isLink: true }
      return { code: '', isLink: true }
    }
  }
  // 兼容仅粘贴了 teamCode=XXXX 的场景
  const m2 = raw.match(/(?:^|[?&])teamCode=([^&#]+)$/i)
  if (m2 && m2[1]) return { code: decodeURIComponent(m2[1]).trim(), isLink: false }
  return { code: raw, isLink: false }
}
const needQuery = computed(() => {
  const { code } = extractTeamCode(joinCode.value)
  return code && code !== lastQueriedCode.value
})
const canSubmitJoin = computed(() => Boolean(joinPreview.value && !needQuery.value))

function buildInviteLink() {
  const base = (inviteBaseUrl.value || '').trim() || (window.location.origin + '/register')
  const params = new URLSearchParams()
  if (team.value?.teamCode) params.set('teamCode', team.value.teamCode)
  return params.toString() ? `${base}?${params.toString()}` : base
}

function openInviteDialog() {
  inviteLink.value = buildInviteLink()
  inviteVisible.value = true
}

function openJoinDialog() {
  joinCode.value = ''
  joinReason.value = ''
  joinPreview.value = null
  lastQueriedCode.value = ''
  invalidCode.value = false
  joinVisible.value = true
}

async function doPreview() {
  const parsed = extractTeamCode(joinCode.value)
  const code = parsed.code
  if (!code) {
    if (parsed.isLink) {
      invalidCode.value = true
      ElMessage.error('邀请链接格式不正确，缺少 teamCode 参数')
      return
    } else {
      ElMessage.warning('请先输入团队码')
      return
    }
  }
  previewLoading.value = true
  try {
    const res = await teamApi.previewByCode(code)
    if (res && res.code === 200) {
      joinPreview.value = res.data || null
      lastQueriedCode.value = code
      invalidCode.value = !joinPreview.value
      if (!joinPreview.value) ElMessage.error('团队码错误或团队不存在')
    } else {
      // 处理业务失败但HTTP为200的情况
      lastQueriedCode.value = code
      joinPreview.value = null
      invalidCode.value = true
      const msg = (res && (res.message || res.msg)) || '团队码错误或团队不存在'
      ElMessage.error(msg)
    }
  } catch (e) {
    lastQueriedCode.value = code
    joinPreview.value = null
    invalidCode.value = true
    const msg = (e && (e.message || e?.response?.data?.message)) || '团队码错误或团队不存在'
    ElMessage.error(msg)
  } finally {
    previewLoading.value = false
  }
}

// 当团队码输入变化时，重置预览与错误提示，强制用户重新查询
watch(() => joinCode.value, (v) => {
  invalidCode.value = false
  if ((v || '').trim() !== lastQueriedCode.value) {
    joinPreview.value = null
  }
})

async function submitJoin() {
  if (!joinPreview.value) return
  joinSubmitting.value = true
  try {
    const { code } = extractTeamCode(joinCode.value)
    if (!code) {
      ElMessage.error('邀请链接或团队码格式不正确')
      return
    }
    const payload = { teamId: joinPreview.value.id, teamCode: code, requestReason: (joinReason.value || '').trim() }
    const res = await teamJoinRequestApi.submit(payload)
    if (res && res.code === 200) {
      ElMessage.success('已提交加入申请，等待管理员审批')
      joinVisible.value = false
    }
  } finally {
    joinSubmitting.value = false
  }
}

async function copyTeamCode() {
  const code = team.value?.teamCode
  if (!code) return
  await navigator.clipboard.writeText(code)
  ElMessage.success('团队码已复制')
}

async function copyInviteLink() {
  if (!inviteLink.value) return
  await navigator.clipboard.writeText(inviteLink.value)
  ElMessage.success('邀请链接已复制')
}

// removed open-in-new-tab per UI request

function startEdit() {
  if (!team.value) return
  editForm.teamName = team.value.teamName || ''
  editForm.description = team.value.description || ''
  editBase.value = true
}

function cancelEdit() {
  editBase.value = false
}

async function saveEdit() {
  if (!team.value || !isOwner.value) return
  savingBase.value = true
  try {
    const payload = {
      id: team.value.id,
      teamName: editForm.teamName?.trim(),
      description: editForm.description?.trim()
    }
    const res = await teamApi.update(team.value.id, payload)
    if (res && res.code === 200) {
      editBase.value = false
      await fetchMyTeam()
    } else {
      const msg = (res && (res.message || res.msg)) || '保存失败'
      ElMessage.error(msg)
    }
  } finally {
    savingBase.value = false
  }
}

function handleMoreCommand(cmd) {
  if (cmd === 'edit') {
    startEdit()
  } else if (cmd === 'exit') {
    confirmExitTeam()
  } else if (cmd === 'dissolve') {
    confirmDissolveTeam()
  }
}

async function confirmExitTeam() {
  if (!team.value) return
  try {
    await ElMessageBox.confirm('确定要退出当前团队吗？退出后将无法访问团队资源。', '确认退出', { type: 'warning' })
  } catch (_) { return }
  const res = await teamApi.exit(team.value.id)
  if (res && res.code === 200) {
    ElMessage.success('已退出团队')
    // 刷新用户信息，清除 currentTeamId/currentTeamName 等缓存，避免其他页面仍显示旧团队
    await authStore.refreshUserInfo()
    await fetchMyTeam()
  }
}

async function confirmDissolveTeam() {
  if (!team.value || !isOwner.value) return
  try {
    await ElMessageBox.confirm('解散后团队及其成员关系将被移除，确认继续？', '解散团队', { type: 'warning' })
  } catch (_) { return }
  const res = await teamApi.dissolve(team.value.id)
  if (res && res.code === 200) {
    ElMessage.success('团队已解散')
    // 刷新用户信息，清除 currentTeamId/currentTeamName 等缓存，避免其他页面仍显示旧团队
    await authStore.refreshUserInfo()
    // 保护性二次刷新，避免极端情况下的读写时序导致菜单未及时重建
    try { await new Promise(resolve => setTimeout(resolve, 120)); await authStore.refreshUserInfo() } catch (_) { /* noop */ }
    await fetchMyTeam()
  }
}
</script>

<style scoped>
.my-team-page { padding: 8px 0 12px; display: flex; flex-direction: column; min-height: calc(100vh - var(--app-header-height) - var(--main-padding-v)); }

/* 使卡片充满可用高度并让内部内容自适应填充 */
.content-card { flex: 1 1 auto; width: 100%; min-height: 100%; display: flex; flex-direction: column; }
.content-card :deep(.el-card__body) { flex: 1; display: flex; flex-direction: column; min-height: 0; }

.team-info-card { margin-bottom: 16px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-header .title { font-weight: 600; color: #303133; }
.card-header .actions { display: flex; gap: 10px; }
/* 操作按钮放大与可读性提升 */
.action-btn { --btn-bg: #ffffff; --btn-color: #334155; --btn-border: #e5e7eb; --btn-hover-bg: #f6f7fb; --btn-hover-border: #e5e7eb; --btn-hover-color: #1f2937; }
.action-btn { display: inline-flex; align-items: center; gap: 6px; background: var(--btn-bg); color: var(--btn-color); border: 1px solid var(--btn-border); font-weight: 400; border-radius: 8px; }
/* 覆盖 Element Plus small 尺寸，更简洁的尺寸与字号 */
.action-btn.el-button { padding: 6px 12px !important; font-size: 13px; }
.action-btn:hover { background: var(--btn-hover-bg); border-color: var(--btn-hover-border); color: var(--btn-hover-color); }
.action-btn :deep(.el-icon) { font-size: 14px; }
.action-btn--primary { --btn-bg: #f8fafc; --btn-color: #1d4ed8; --btn-border: #e5e7eb; --btn-hover-bg: #f1f5f9; --btn-hover-border: #e5e7eb; --btn-hover-color: #1e40af; }
.action-btn--success { --btn-bg: #f0fdf4; --btn-color: #059669; --btn-border: #e5e7eb; --btn-hover-bg: #ecfdf5; --btn-hover-border: #e5e7eb; --btn-hover-color: #047857; }

.member-data-table { width: 100%; --el-table-row-height: 42px; }
.member-data-table :deep(.el-table__cell) { padding-top: 10px; padding-bottom: 10px; }

/* Hero 卡片样式 */
.team-hero { display: flex; flex-direction: column; gap: 10px; }
.hero-main { display: flex; align-items: center; gap: 14px; justify-content: space-between; }
.hero-left { display: flex; align-items: center; gap: 14px; }
.team-icon { display: flex; align-items: center; justify-content: center; width: 36px; height: 36px; background: #eef2ff; border-radius: 10px; box-shadow: inset 0 0 0 1px #e5e7eb; }
.team-avatar { background: #ecf5ff; color: #409eff; font-weight: 700; }
.avatar-text { font-size: 18px; }
.hero-meta { display: flex; flex-direction: column; gap: 6px; }
.team-name-row { display: flex; align-items: center; gap: 8px; }
.team-name { font-size: 20px; font-weight: 700; color: #1f2937; }
.status-tag { transform: translateY(-1px); }
.team-sub { display: flex; align-items: center; gap: 8px; color: #64748b; font-size: 13px; }
.owner-label { color: #94a3b8; }
.dot { color: #cbd5e1; }
.member-count { color: #475569; }
.hero-desc { margin-top: 4px; color: #374151; line-height: 1.6; background: #fafbfc; border: 1px solid #f0f2f5; border-radius: 6px; padding: 10px 12px; }

/* 团队码行 */
.hero-right { display: flex; align-items: center; gap: 10px; }
/* 团队码徽章重设计：更简洁的浅底、明显的悬停反馈与更强的可读性 */
.team-code-badge { cursor: pointer; user-select: none; display: inline-flex; align-items: center; gap: 10px; padding: 10px 14px; border-radius: 12px; background: linear-gradient(180deg, #ffffff, #f7f9ff); border: 1px solid #e5e9f2; box-shadow: 0 2px 6px rgba(15, 23, 42, 0.05), inset 0 0 0 1px rgba(59,130,246,.06); transition: transform .14s ease, box-shadow .14s ease; }
.team-code-badge:hover { transform: translateY(-2px); box-shadow: 0 8px 22px rgba(15, 23, 42, 0.08), 0 0 0 3px rgba(59,130,246,.12); }
.team-code-badge:active { transform: translateY(0); box-shadow: 0 2px 8px rgba(15, 23, 42, 0.08), 0 0 0 2px rgba(59,130,246,.18); }
.team-code-badge .code-label { font-size: 12px; font-weight: 700; color: #1d4ed8; letter-spacing: .2px; background: #e0e7ff; border: 1px solid #c7d2fe; padding: 3px 7px; border-radius: 999px; box-shadow: inset 0 0 0 1px rgba(255,255,255,.6); }
.team-code-badge .code-value { font-size: 20px; font-weight: 800; letter-spacing: .6px; color: #0b1226; text-shadow: 0 1px 0 rgba(255,255,255,.6); font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace; }

/* 编辑表单美化 */

/* 加载状态与空态复用系统风格 */
.loading-container { display: flex; align-items: center; justify-content: center; padding: 40px 0; color: #6a737d; min-height: calc(100vh - var(--app-header-height) - var(--main-padding-v)); }
.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 80px 20px; text-align: center; background: white; border-radius: 12px; min-height: calc(100vh - var(--app-header-height) - var(--main-padding-v)); }
.empty-icon { margin-bottom: 16px; color: #d1d5db; }
.empty-state h3 { font-size: 18px; font-weight: 600; color: #374151; margin: 0 0 8px 0; }
.empty-state p { font-size: 14px; color: #6b7280; margin: 0 0 16px 0; }
.empty-actions { display: flex; gap: 12px; }

/* 编辑表单容器宽度限制 */
.team-edit-form { max-width: 720px; }
.team-edit-form :deep(.el-textarea),
.team-edit-form :deep(.el-input) { width: 520px; }

/* 邀请对话框 */
.invite-block { display: flex; flex-direction: column; gap: 14px; }
.invite-block .row { display: flex; align-items: center; gap: 12px; }
.invite-block .row .label { width: 80px; color: #64748b; text-align: right; }
.invite-block .row .value { flex: 1; display: flex; align-items: center; gap: 8px; }
.invite-block :deep(.code-input .el-input__wrapper),
.invite-block :deep(.link-input .el-input__wrapper) { box-shadow: 0 1px 2px rgba(0,0,0,.04), inset 0 0 0 1px #e5e7eb; }
.invite-block :deep(.el-input__prefix) { color: #64748b; }
.invite-block :deep(.el-input-group__append .el-button) { border-left: none; }

/* 邀请提示内联链接与文本对齐、继承字号 */
.inline-link { vertical-align: baseline; font-size: inherit; line-height: inherit; }

/* 创建团队弹窗美化 */
.dialog-intro { margin: -8px 0 8px 0; color: #64748b; font-size: 13px; }
.create-form :deep(.el-form-item) { margin-bottom: 16px; }
.create-form .form-help { margin-top: 6px; color: #94a3b8; font-size: 12px; }

/* 加入团队对话框 */
.join-block { display: flex; flex-direction: column; gap: 14px; }
.join-block .row { display: flex; align-items: center; gap: 12px; }
.join-block .row .label { width: 80px; color: #64748b; text-align: right; }
.join-block .row .value { flex: 1; display: flex; align-items: center; gap: 8px; }
.join-block :deep(.el-input-group__append .el-button) { border-left: none; }
.join-block :deep(.el-input-group__append .el-button.el-button--primary) { background-color: var(--el-color-primary); color: #fff; border-color: var(--el-color-primary); }
.join-block :deep(.el-input-group__append .el-button.el-button--success) { background-color: var(--el-color-success); color: #fff; border-color: var(--el-color-success); }
.preview-card { margin-top: 6px; border: 1px solid #e5e7eb; border-radius: 10px; padding: 10px 12px; background: #f9fafb; }
.preview-header { display: flex; align-items: center; justify-content: space-between; }
.preview-header .name { font-weight: 700; color: #111827; }
.preview-meta { display: flex; align-items: center; gap: 8px; color: #64748b; font-size: 13px; margin-top: 6px; }
.preview-desc { margin-top: 8px; color: #374151; line-height: 1.6; }

/* 错误提示 */
.error-tip { color: #ef4444; font-size: 12px; margin-left: 80px; margin-top: -6px; }

/* SUPER_ADMIN 视图 */
.all-teams-content { padding: 8px 0; }
.teams-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
.team-card { border-radius: 14px; position: relative; overflow: hidden; transition: transform .22s ease, box-shadow .22s ease; }
.team-card.fancy { background: linear-gradient(180deg, #ffffff, #fafbfd); border: 1px solid #eef0f3; }
.team-card.fancy::before { content: ""; position: absolute; inset: -1px; background: radial-gradient(600px 120px at top left, rgba(99, 102, 241, .14), transparent 40%), radial-gradient(600px 120px at bottom right, rgba(14, 165, 233, .12), transparent 40%); pointer-events: none; }
.team-card .glow { position: absolute; inset: 0; background: radial-gradient(220px 80px at 10% 0%, rgba(99, 102, 241, .12), transparent), radial-gradient(220px 80px at 90% 100%, rgba(14, 165, 233, .12), transparent); opacity: 0; transition: opacity .25s ease; }
.team-card:hover { transform: translateY(-4px); box-shadow: 0 12px 28px rgba(15, 23, 42, 0.08); }
.team-card:hover .glow { opacity: 1; }
.team-card-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.team-card-title { font-weight: 700; color: #0f172a; letter-spacing: .2px; display: flex; align-items: center; gap: 8px; }
.team-card-title .badge { font-size: 10px; font-weight: 700; color: #6366f1; background: #eef2ff; padding: 2px 6px; border-radius: 999px; border: 1px solid #e0e7ff; }
.team-card-body { color: #4b5563; font-size: 13px; display: flex; flex-direction: column; gap: 6px; }
.team-card-row { display: flex; gap: 6px; }
.team-card-row .label { color: #94a3b8; }
.team-card-row .value { color: #374151; }
.team-card-desc { margin-top: 6px; padding: 8px 10px; background: #f8fafc; border: 1px solid #eef2f7; border-radius: 8px; color: #334155; line-height: 1.6; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 4; line-clamp: 4; -webkit-box-orient: vertical; }
.team-card-actions { margin-top: 10px; display: flex; gap: 8px; }
.status-dot { display: inline-block; width: 6px; height: 6px; border-radius: 50%; margin-right: 6px; }
.status-dot.on { background: #10b981; box-shadow: 0 0 0 0 rgba(16,185,129,.6); animation: pulse 1.8s infinite; }
.status-dot.off { background: #f59e0b; opacity: .9; }
@keyframes pulse { 0% { box-shadow: 0 0 0 0 rgba(16,185,129,.6);} 70% { box-shadow: 0 0 0 10px rgba(16,185,129,0);} 100% { box-shadow: 0 0 0 0 rgba(16,185,129,0);} }

/* 表单分节与字段帮助文案样式 */
.form-section { padding: 10px 12px; background: #fbfbfe; border: 1px solid #eef1f6; border-radius: 10px; }
.form-section + .form-section { margin-top: 12px; }
.form-section-title { font-weight: 600; color: #334155; margin-bottom: 10px; letter-spacing: .2px; }
.team-edit-form :deep(.el-form-item__label) { color: #64748b; }
.field-col { display: flex; flex-direction: column; gap: 6px; }
.field-help { font-size: 12px; color: #94a3b8; }
.team-edit-form :deep(.el-input__wrapper),
.team-edit-form :deep(.el-textarea__inner) { transition: box-shadow .14s ease; }
.team-edit-form :deep(.is-focus .el-input__wrapper),
.team-edit-form :deep(.el-textarea__inner:focus) { box-shadow: 0 0 0 3px rgba(59,130,246,.15); }

/* 补充标准 line-clamp 以通过兼容性校验 */
.team-card-desc { display: -webkit-box; -webkit-line-clamp: 4; -webkit-box-orient: vertical; line-clamp: 4; }
</style>

