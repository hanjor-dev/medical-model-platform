<template>
  <div class="user-credits-page">

    <div class="page-content">
      <el-card class="content-card">
        <template #header>
          <div class="list-header">
            <AppBreadcrumb />
          </div>
        </template>
        <div class="filters">
          <el-input v-model="search.username" placeholder="昵称/用户名/ID" class="filter-item" />
          <div class="filter-actions left">
            <el-button type="primary" @click="applySearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </div>
        </div>

        <div class="table-area">
        <el-table :data="tableData" border row-key="id" v-loading="loading" height="100%">
          <el-table-column type="index" label="编号" width="60" :index="indexMethod" />
          <el-table-column prop="nickname" label="用户昵称" width="140" show-overflow-tooltip />
          <el-table-column prop="role" label="角色" width="120" />
          <el-table-column prop="consumedTotal" label="总消耗积分" width="100" align="center">
            <template #default="{ row }">
              <span :style="{ color: '#f56c6c' }">-{{ formatAmount(Math.abs(Number(row.consumedTotal || 0)), 2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="余额（按类型）">
            <template #default="{ row }">
              <div class="balance-tags">
                <el-tag v-for="acc in row.accounts" :key="acc.typeCode" :type="'info'" class="balance-tag" :style="{ borderColor: acc.color, color: acc.color }">
                  {{ acc.typeName }}：<span class="amount-text">{{ formatAmount(acc.balance, acc.decimalPlaces) }}</span>
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetail(row)">交易记录</el-button>
              <el-divider direction="vertical" />
              <el-button v-if="hasCurrentUserId && !isSelf(row)" type="primary" @click="openAllocate(row)" v-role="['ADMIN','SUPER_ADMIN']">分配积分</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <EmptyBox size="table" title="暂无数据" desc="尝试更换关键词或调整分页后重试" />
          </template>
        </el-table>

        <CommonPagination
          v-if="total > 0"
          :total="total"
          :current="page"
          :size="pageSize"
          :show-info="false"
          align="right"
          @update:current="handlePageChange"
          @update:size="handleSizeChange"
        />
        </div>
      </el-card>
    </div>

    <!-- 用户详情抽屉 -->
    <el-drawer v-model="detailVisible" size="60%" class="detail-drawer">
      <template #header>
        <div class="drawer-header-box">
          <div class="drawer-title">
            <div class="title-left">
              <span class="main-title">积分明细</span>
              <span v-if="detailUser" class="sub-title">{{ detailUser.nickname }}（ID: {{ detailUser.username }}）</span>
            </div>
            <div class="title-stats">
              <div class="stat-chip income">
                <span class="stat-label">收入</span>
                <span class="stat-value" :style="{ color: '#67c23a' }">+{{ formatAmount(stats.totalIncome || 0, 2) }}</span>
              </div>
              <div class="stat-chip expense">
                <span class="stat-label">支出</span>
                <span class="stat-value" :style="{ color: '#f56c6c' }">-{{ formatAmount(stats.totalExpense || 0, 2) }}</span>
              </div>
              <div class="stat-chip net">
                <span class="stat-label">净变动</span>
                <span class="stat-value" :style="{ color: Number(stats.netChange || 0) > 0 ? '#67c23a' : (Number(stats.netChange || 0) < 0 ? '#f56c6c' : '#303133') }">
                  {{ Number(stats.netChange || 0) > 0 ? '+' : (Number(stats.netChange || 0) < 0 ? '-' : '') }}{{ formatAmount(Math.abs(Number(stats.netChange || 0)), 2) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </template>
      <div v-if="detailUser">
        <div class="detail-balances">
          <div class="section-title">账户余额</div>
          <div class="balance-chips">
            <div v-for="acc in detailUser.accounts" :key="acc.typeCode" class="chip">
              <span class="chip-dot" :style="{ background: acc.color }"></span>
              <span class="chip-name" :style="{ color: acc.color }">{{ acc.typeName }}</span>
              <span class="chip-sep">：</span>
              <span class="chip-amount" :style="{ color: acc.color }">{{ formatAmount(acc.balance, acc.decimalPlaces) }}</span>
            </div>
          </div>
        </div>

        <div class="detail-filters">
          <div class="colored-select" :style="{ '--type-color': selectedFilterTypeColor }">
            <el-select v-model="detailFilters.type" placeholder="积分类型" clearable class="filter-item w-140" size="small">
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
          <el-select v-model="detailFilters.txType" placeholder="交易类型" clearable class="filter-item w-140" size="small">
            <el-option v-for="opt in txTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
          <el-date-picker
            v-model="detailFilters.range"
            type="datetimerange"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            class="filter-item w-280"
            size="small"
          />
          <el-button size="small" text class="preset-btn" @click="setRangePreset(7)">近7天</el-button>
          <el-button size="small" text class="preset-btn" @click="setRangePreset(30)">近30天</el-button>
          <el-input v-model="detailFilters.keyword" placeholder="关键词（说明/场景）" class="filter-item w-180" size="small" @keyup.enter="applyDetailFilters" />
          <div class="filter-actions">
            <el-button type="primary" size="small" @click="applyDetailFilters">查询</el-button>
            <el-button size="small" @click="resetDetailFilters">重置</el-button>
          </div>
        </div>

        <el-table :data="pagedDetailTx" border v-loading="detailLoading" size="small" :empty-text="detailLoading ? '加载中...' : '暂无数据'">
          <el-table-column prop="time" label="时间" width="150" />
          <el-table-column label="类型" width="90">
            <template #default="{ row }">
              <span :style="{ color: getTypeColorByCode(row.typeCode) }">{{ row.typeName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="txTypeName" label="交易类型" width="90" />
          <el-table-column prop="scenarioName" label="使用场景" width="110" />
          <el-table-column label="金额" width="90" align="right">
            <template #default="{ row }">
              <span :style="{ color: Number(row.amount) > 0 ? '#67c23a' : (Number(row.amount) < 0 ? '#f56c6c' : '#303133') }">
                {{ Number(row.amount) > 0 ? '+' : (Number(row.amount) < 0 ? '-' : '') }}{{ formatAmount(Math.abs(Number(row.amount || 0)), 2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="交易后余额" width="120" align="right">
            <template #default="{ row }">{{ row.balanceAfterText }}</template>
          </el-table-column>
          <el-table-column prop="desc" label="说明" show-overflow-tooltip />
          <el-table-column label="操作" width="90" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openTxDetail(row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>

        <CommonPagination
          v-if="detailTotal > 0"
          :total="detailTotal"
          :current="detailPage"
          :size="detailPageSize"
          :show-info="false"
          align="right"
          @update:current="handleDetailPageChange"
          @update:size="handleDetailSizeChange"
        />
      </div>
    </el-drawer>

    <!-- 交易详情弹窗 -->
    <el-dialog v-model="txDetailVisible" title="交易详情" width="560px">
      <div v-if="currentTx">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="时间">{{ currentTx.time }}</el-descriptions-item>
          <el-descriptions-item label="积分类型">{{ currentTx.typeName }}</el-descriptions-item>
          <el-descriptions-item label="交易类型">{{ currentTx.txTypeName }}</el-descriptions-item>
          <el-descriptions-item label="使用场景">{{ currentTx.scenarioName }}</el-descriptions-item>
          <el-descriptions-item label="金额">{{ currentTx.amountText }}</el-descriptions-item>
          <el-descriptions-item label="交易后余额">{{ currentTx.balanceAfterText }}</el-descriptions-item>
          <el-descriptions-item label="说明">{{ currentTx.desc }}</el-descriptions-item>
          <el-descriptions-item label="单号">{{ currentTx.orderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ currentTx.operator || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button type="primary" @click="txDetailVisible = false">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配积分弹窗 -->
    <el-dialog v-model="allocateVisible" title="分配积分" width="480px" class="allocate-dialog-default">
      <div v-if="allocateUser">
        <el-form :model="allocateForm" label-width="100px" size="small">
          <el-form-item label="目标用户">
            <el-input v-model="allocateUser.nickname" disabled />
          </el-form-item>
          <el-form-item label="积分类型">
            <div class="colored-select" :style="{ '--type-color': selectedAllocateTypeColor }">
              <el-select v-model="allocateForm.creditTypeCode" placeholder="请选择">
                <el-option v-for="t in enabledTypes" :key="t.typeCode" :label="t.typeName" :value="t.typeCode">
                  <span :style="{ color: getTypeColor(t) }">{{ t.typeName }}</span>
                </el-option>
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="分配数量">
            <el-input v-model.number="allocateForm.amount" type="number" placeholder="请输入数量" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="allocateForm.description" placeholder="备注（可选）" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button size="small" @click="allocateVisible = false">取消</el-button>
        <el-button type="primary" size="small" @click="confirmAllocate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { defineComponent, ref, computed, onMounted, reactive } from 'vue'
import { useAuthStore } from '@/stores/auth'
import CommonPagination from '@/components/common/Pagination.vue'
import EmptyBox from '@/components/common/EmptyBox.vue'
import { userCreditsApi, creditTypeApi, creditScenarioApi, creditTransactionApi } from '@/api/credit-system'
import { ElMessage } from 'element-plus'
import AppBreadcrumb from '@/components/common/Breadcrumb.vue'

export default defineComponent({
  name: 'UserCredits',
  components: { CommonPagination, AppBreadcrumb, EmptyBox },
  setup() {
    const authStore = useAuthStore()
    const currentUserId = computed(() => {
      const u = authStore.getUser()
      if (!u) return undefined
      return u.userId ?? u.id
    })
    const hasCurrentUserId = computed(() => !!currentUserId.value)
    onMounted(() => {
      loadTypes()
      loadTxTypeOptions()
      loadUsers()
    })
    const enabledTypes = ref([])
    const typeOptionsAll = ref([])
    const typeColorMap = reactive({})
    const creditTypeNameDict = reactive({})
    const loading = ref(false)
    const tableData = ref([])
    const total = ref(0)

    // search + pagination
    const search = ref({ username: '' })
    const page = ref(1)
    const pageSize = ref(10)

    const loadTypes = async () => {
      // 1) 启用类型（用于分配默认值）
      const respEnabled = await creditTypeApi.getEnabledTypes()
      if (respEnabled.code === 200) enabledTypes.value = respEnabled.data || []
      // 2) 全部类型（下拉带颜色，禁用标注）
      const respAll = await creditTypeApi.getCreditTypeList({ pageNum: 1, pageSize: 999 })
      if (respAll.code === 200) {
        const list = Array.isArray(respAll.data?.records) ? respAll.data.records : (respAll.data?.list || respAll.data || [])
        typeOptionsAll.value = list
        list.forEach(t => {
          if (t.typeCode) {
            typeColorMap[t.typeCode] = t.colorCode || t.displayColor || t.color || '#000'
            creditTypeNameDict[t.typeCode] = t.typeName || t.typeCode
          }
        })
      }
    }

    const loadUsers = async () => {
      loading.value = true
      try {
        const payload = {
          keyword: search.value.username || undefined,
          current: page.value,
          size: pageSize.value
        }
        const resp = await userCreditsApi.getUserBalancesPage(payload)
        if (resp.code === 200 && resp.data) {
          const records = Array.isArray(resp.data.records) ? resp.data.records : []
          tableData.value = records.map(r => ({
            id: r.userId,
            username: r.username,
            nickname: r.nickname || r.username,
            role: r.role,
            consumedTotal: Number(r.totalConsumed || 0),
            accounts: Array.isArray(r.accounts) ? r.accounts.map(a => ({
              typeCode: a.creditTypeCode,
              typeName: a.creditTypeName || a.creditTypeCode,
              unitName: a.unitName,
              color: a.colorCode,
              decimalPlaces: a.decimalPlaces,
              balance: a.balance
            })) : []
          }))
          total.value = Number(resp.data.total || 0)
        } else {
          tableData.value = []
          total.value = 0
          if (resp && resp.message) ElMessage.error(resp.message)
        }
      } catch (e) {
        tableData.value = []
        total.value = 0
        ElMessage.error((e && e.message) || '加载用户积分失败')
      } finally {
        loading.value = false
      }
    }

    const applySearch = () => { page.value = 1; loadUsers() }
    const resetSearch = () => { search.value = { username: '' }; page.value = 1; loadUsers() }
    const handlePageChange = (val) => { page.value = val; loadUsers() }
    const handleSizeChange = (val) => { page.value = 1; pageSize.value = val; loadUsers() }

    // 表格自然编号（跨页递增）
    const indexMethod = (index) => {
      const base = (page.value - 1) * pageSize.value
      return base + index + 1
    }

    // detail drawer
    const detailVisible = ref(false)
    const detailUser = ref(null)
    const detailLoading = ref(false)
    const txTypeOptions = ref([])
    const loadTxTypeOptions = async () => {
      try {
        const resp = await creditTransactionApi.getTransactionTypeOptions()
        if (resp && resp.code === 200 && Array.isArray(resp.data)) {
          txTypeOptions.value = resp.data
        } else {
          txTypeOptions.value = []
        }
      } catch (_) {
        txTypeOptions.value = []
      }
    }
    const detailFilters = ref({ type: '', txType: '', keyword: '', range: [] })
    const allDetailTx = ref([])
    const stats = ref({ totalIncome: 0, totalExpense: 0, netChange: 0, totalTransactions: 0, incomeCount: 0, expenseCount: 0 })
    const detailPage = ref(1)
    const detailPageSize = ref(20)
    const detailTotal = ref(0)

    const fetchDetail = async () => {
      if (!detailUser.value) return
      detailLoading.value = true
      try {
        const payload = {
          userId: detailUser.value.id,
          creditTypeCode: detailFilters.value.type || undefined,
          transactionType: detailFilters.value.txType || undefined,
          keyword: detailFilters.value.keyword || undefined,
          current: detailPage.value,
          size: detailPageSize.value
        }
        if (Array.isArray(detailFilters.value.range) && detailFilters.value.range.length === 2) {
          const [start, end] = detailFilters.value.range
          if (start) payload.startTime = start
          if (end) payload.endTime = end
        }
        const [resp, statsResp] = await Promise.all([
          userCreditsApi.getTransactionsPage(payload),
          userCreditsApi.getTransactionsStatistics({ ...payload, current: undefined, size: undefined })
        ])
        const records = (resp.code === 200 && resp.data && Array.isArray(resp.data.records)) ? resp.data.records : []
        detailTotal.value = Number(resp.data?.total || records.length)
        allDetailTx.value = records.map(r => ({
          id: r.id,
          time: (r.createTime || '').replace('T', ' '),
          typeCode: r.creditTypeCode,
          typeName: r.creditTypeName || creditTypeNameDict[r.creditTypeCode] || r.creditTypeCode,
          txType: r.transactionType,
          txTypeName: r.transactionTypeDesc || mapTxType(r.transactionType),
          scenarioCode: r.scenarioCode,
          scenarioName: r.scenarioName || mapScenario(r.scenarioCode) || r.scenarioCode || '-',
          amount: Number(r.amount || 0),
          amountText: formatAmount(Number(r.amount || 0), 2),
          balanceAfter: Number(r.balanceAfter || 0),
          balanceAfterText: formatAmount(Number(r.balanceAfter || 0), 2),
          desc: r.description || '-',
          orderNo: r.orderNo || r.bizOrderNo || r.orderId,
          operator: r.operatorName || r.operator || r.createdBy
        }))
        if (statsResp && statsResp.code === 200 && statsResp.data) {
          stats.value = {
            totalIncome: Number(statsResp.data.totalIncome || 0),
            totalExpense: Number(statsResp.data.totalExpense || 0),
            netChange: Number(statsResp.data.netChange || 0),
            totalTransactions: Number(statsResp.data.totalTransactions || 0),
            incomeCount: Number(statsResp.data.incomeCount || 0),
            expenseCount: Number(statsResp.data.expenseCount || 0)
          }
        } else {
          stats.value = { totalIncome: 0, totalExpense: 0, netChange: 0, totalTransactions: 0, incomeCount: 0, expenseCount: 0 }
          if (statsResp && statsResp.message) ElMessage.error(statsResp.message)
        }
      } catch (e) {
        allDetailTx.value = []
        detailTotal.value = 0
        stats.value = { totalIncome: 0, totalExpense: 0, netChange: 0, totalTransactions: 0, incomeCount: 0, expenseCount: 0 }
        ElMessage.error((e && e.message) || '加载交易明细失败')
      } finally {
        detailLoading.value = false
      }
    }

    const openDetail = async (user) => {
      detailUser.value = user
      detailFilters.value = { type: '', txType: '', keyword: '', range: [] }
      detailPage.value = 1
      detailVisible.value = true
      await fetchDetail()
    }

    const filteredDetailTx = computed(() => {
      return allDetailTx.value
    })

    const pagedDetailTx = computed(() => {
      return filteredDetailTx.value
    })

    const applyDetailFilters = () => { detailPage.value = 1; fetchDetail() }
    const resetDetailFilters = () => { detailFilters.value = { type: '', txType: '', keyword: '', range: [] }; detailPage.value = 1; fetchDetail() }
    const handleDetailPageChange = (val) => { detailPage.value = val; fetchDetail() }
    const handleDetailSizeChange = (val) => { detailPage.value = 1; detailPageSize.value = val; fetchDetail() }

    const setRangePreset = (days) => {
      const end = new Date()
      const start = new Date()
      start.setDate(end.getDate() - Number(days || 0))
      const pad = (n) => String(n).padStart(2, '0')
      const fmt = (d) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
      detailFilters.value.range = [fmt(start), fmt(end)]
      applyDetailFilters()
    }

    // 交易详情
    const txDetailVisible = ref(false)
    const currentTx = ref(null)
    const openTxDetail = (row) => { currentTx.value = row; txDetailVisible.value = true }

    // allocate dialog
    const allocateVisible = ref(false)
    const allocateUser = ref(null)
    const allocateForm = ref({ creditTypeCode: undefined, amount: 0, description: '' })
    const openAllocate = (user) => {
      if (user && currentUserId.value && Number(user.id) === Number(currentUserId.value)) {
        ElMessage.warning('不能给自己分配积分')
        return
      }
      allocateUser.value = user
      allocateForm.value = { creditTypeCode: enabledTypes.value[0]?.typeCode, amount: 0, description: '' }
      allocateVisible.value = true
    }
    const confirmAllocate = async () => {
      if (!allocateUser.value) return
      try {
        const payload = { targetUserId: allocateUser.value.id, creditTypeCode: allocateForm.value.creditTypeCode, amount: allocateForm.value.amount, description: allocateForm.value.description }
        const resp = await userCreditsApi.allocateCredits(payload)
        if (resp.code === 200) {
          await loadUsers()
          allocateVisible.value = false
          ElMessage.success('分配成功')
        } else {
          ElMessage.error(resp.message || '分配失败')
        }
      } catch (e) {
        ElMessage.error((e && e.message) || '分配失败')
      }
    }

    const formatAmount = (num, dp = 0) => Number(num).toLocaleString('zh-CN', { minimumFractionDigits: dp, maximumFractionDigits: dp })
    const getTypeColor = (t) => (t?.colorCode || t?.displayColor || t?.color || typeColorMap[t?.typeCode] || '#000')
    const getTypeColorByCode = (code) => (typeColorMap[code] || '#000')
    const selectedFilterTypeColor = computed(() => {
      const code = detailFilters.value.type
      if (!code) return '#000'
      return typeColorMap[code] || '#000'
    })
    const selectedAllocateTypeColor = computed(() => {
      const code = allocateForm.value?.creditTypeCode
      if (!code) return '#000'
      return typeColorMap[code] || '#000'
    })
    const mapTxType = (v) => {
      const map = {
        REWARD: '奖励',
        CONSUME: '消费',
        TRANSFER: '转账',
        REFUND: '退款',
        ADMIN_GRANT: '管理员分配',
        REGISTER: '注册赠送',
        DEDUCT: '扣减',
        EARN: '赚取',
        SPEND: '花费'
      }
      return map[v] || v
    }
    const scenarioDict = ref({})
    const mapScenario = (code) => scenarioDict.value[code] || code
    // 预加载场景字典（名称）
    const loadScenariosDict = async () => {
      try {
        const resp = await creditScenarioApi.getCreditScenarioList({ pageNum: 1, pageSize: 999 })
        if (resp.code === 200) {
          const data = resp.data
          const list = Array.isArray(data?.records) ? data.records : (data?.list || data || [])
          const dict = {}
          list.forEach(s => { if (s.scenarioCode) dict[s.scenarioCode] = s.scenarioName || s.scenarioCode })
          scenarioDict.value = dict
        }
      } catch (_) { /* ignore */ }
    }

    onMounted(() => { loadScenariosDict() })

    return {
      currentUserId,
      hasCurrentUserId,
      isSelf: (row) => currentUserId.value && row && Number(row.id) === Number(currentUserId.value),
      enabledTypes,
      typeOptionsAll,
      loading,
      tableData,
      total,
      // table
      search,
      page, pageSize,
      applySearch, resetSearch,
      handlePageChange, handleSizeChange,
      indexMethod,
      // drawer
      detailVisible, detailUser, txTypeOptions,
      detailFilters, allDetailTx, detailPage, detailPageSize, detailTotal, detailLoading,
      stats,
      openDetail, filteredDetailTx, pagedDetailTx,
      applyDetailFilters, resetDetailFilters,
      handleDetailPageChange, handleDetailSizeChange,
      setRangePreset,
      getTypeColor, getTypeColorByCode, selectedFilterTypeColor,
      txDetailVisible, currentTx, openTxDetail,
      // allocate
      allocateVisible, allocateUser, allocateForm,
      openAllocate, confirmAllocate,
      // utils
      formatAmount,
      mapScenario,
      selectedAllocateTypeColor,
      loadUsers
    }
  }
})
</script>

<style lang="scss" scoped>
.user-credits-page {
  padding: 8px 0 12px;
  display: flex;
  flex-direction: column;
  /* 让页面在首次渲染时占满可用高度 */
  min-height: calc(100vh - var(--app-header-height) - var(--main-padding-v));

  /* 头部标题采用与“类型管理”一致的风格 */
  .list-header { display: flex; justify-content: space-between; align-items: center; }
  .list-title { display: inline-flex; align-items: center; gap: 8px; font-weight: 600; color: #303133; font-size: 14px; }

  .filters { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: nowrap; align-items: center; }
  .filter-item { width: 160px; }
  .filter-actions { display: flex; gap: 8px; }
  .filter-actions.left { margin-left: 0; }

  .balance-tags { display: flex; flex-wrap: wrap; gap: 6px; }
  .balance-tag { background: #fff; border: 1px solid #ebeef5; }
  .amount-text { font-variant-numeric: tabular-nums; }

  /* 仅在抽屉中缩小字体与紧凑化 */
  .detail-drawer {
    font-size: 12px;
    .section-title { font-size: 12px; }
    .el-table { font-size: 12px; }
    .el-button { font-size: 12px; }
    .drawer-header-box {
      width: 100%;
      background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%);
      border: 1px solid #ebeef5;
      border-radius: 8px;
      padding: 8px 12px;
    }
  }
  .detail-balances { margin-bottom: 12px; }
  /* 缩小标题区域与余额区域的垂直间距 */
  :deep(.el-drawer__header) { margin-bottom: 8px; }
  .section-title { font-weight: 600; margin: 12px 0; }
  .balance-chips { display: flex; flex-wrap: wrap; gap: 8px; }
  .chip { display: inline-flex; align-items: center; padding: 4px 8px; border: 1px dashed #dcdfe6; border-radius: 16px; background: #fff; }
  .chip-dot { width: 6px; height: 6px; border-radius: 50%; margin-right: 6px; }
  .chip-name { font-size: 12px; font-weight: 600; }
  .chip-sep { margin: 0 4px; color: #c0c4cc; }
  .chip-amount { font-variant-numeric: tabular-nums; font-weight: 600; color: #303133; }
  .chip-unit { margin-left: 4px; color: #909399; font-size: 12px; }

  .drawer-title { display: flex; align-items: flex-start; flex-direction: column; gap: 6px; }
  .main-title { font-size: 14px; font-weight: 600; margin-right: 8px; }
  .sub-title { color: #909399; font-size: 12px; }
  .title-stats { display: inline-flex; align-items: center; gap: 8px; flex-wrap: wrap; }
  .stat-chip { display: inline-flex; gap: 6px; align-items: baseline; padding: 4px 8px; border-radius: 12px; background: #f5f7fa; border: 1px solid #ebeef5; }
  .stat-chip.income { background: #f0f9eb; border-color: #e1f3d8; }
  .stat-chip.expense { background: #fef0f0; border-color: #fde2e2; }
  .stat-chip.net { background: #f5f7fa; }
  .stat-item { display: inline-flex; align-items: baseline; gap: 6px; }
  .stat-label { color: #909399; font-size: 12px; }
  .stat-value { font-weight: 600; font-variant-numeric: tabular-nums; }

  .detail-filters { display: flex; gap: 8px; align-items: center; margin: 8px 0 12px; flex-wrap: nowrap; }
  .preset-btn { color: #909399; }
  .panel-soft { background: #fafafa; border: 1px solid #ebeef5; border-radius: 8px; padding: 8px; }
  .w-140 { width: 140px; }
  .w-150 { width: 150px; }
  .w-160 { width: 160px; }
  .w-180 { width: 180px; }
  .w-200 { width: 200px; }
  .w-280 { width: 280px; }
  .w-320 { width: 320px; }
  .ml8 { margin-left: 8px; }

  /* 分配弹窗默认样式增强：放大字号、表单项对齐、输入宽度统一 */
  :deep(.allocate-dialog-default) {
    .el-dialog__title { font-size: 16px; }
    .el-dialog__body { font-size: 14px; }
    .el-form-item__label { font-size: 14px; }
    .el-input, .el-select, .el-textarea {
      width: 320px;
    }
    /* 下拉选项随积分类型着色 */
    .colored-select .el-select .el-select__wrapper {
      --el-select-input-color: var(--type-color, #303133);
    }
  }
}

/* 主体区域与卡片：占满宽高，避免“先小后大”的抖动 */
.page-content { flex: 1; display: flex; min-height: 0; }
.content-card {
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
/* 表格区域填充，分页固定在底部 */
.table-area {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
</style>


