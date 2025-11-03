<template>
  <div class="my-credits-page">
    <div class="page-content">
      <el-card class="content-card">
        <template #header>
          <div class="list-header">
            <AppBreadcrumb />
            <div class="header-tools">
              <span class="last-updated" v-if="lastUpdated">更新于：{{ lastUpdated }}</span>
              <el-tooltip content="刷新">
                <el-button size="small" circle @click="refreshAll" :loading="refreshing">
                  <el-icon><Refresh /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </div>
        </template>
        <el-tabs v-model="activeTab">
          <template #extra>
            <div class="tabs-extra" v-if="activeTab === 'transactions'">
              <el-button size="small" @click="exportCsv">导出明细</el-button>
            </div>
          </template>
          <el-tab-pane label="概览" name="overview">
            <!-- 顶部Hero：柔和渐变 + 信息概览 + 快捷操作 -->
            <div class="overview-hero">
              <div class="hero-main">
                <div class="hero-title">我的积分概览</div>
                <div class="hero-sub">快速了解你的积分余额与近期变化</div>
              </div>
              <div class="hero-actions">
                <el-button size="small" type="primary" class="btn-hero btn-hero-primary" @click="goScenarioPricing">计价规则</el-button>
                <el-button size="small" type="warning" class="btn-hero btn-hero-warning" @click="openRedeem">兑换码</el-button>
              </div>
              
              <div class="hero-glow"></div>
            </div>

            <!-- 骨架屏 -->
            <el-skeleton :rows="4" animated v-if="refreshing && balances.length === 0" class="overview-skeleton" />

            <!-- 内容网格 -->
            <div class="overview-grid" v-else>
              <div class="balances">
                <div class="section-title">账户余额</div>
                <div class="balance-cards">
                  <div
                    v-for="acc in balances"
                    :key="acc.typeCode"
                    class="balance-card glossy"
                    :style="{ '--accent': acc.color }"
                  >
                    <div class="card-head">
                      <span class="type-name">{{ acc.typeName }}</span>
                      <span class="unit">（{{ acc.unitName }}）</span>
                    </div>
                    <div class="card-body">
                      <div class="amount">{{ formatAmount(acc.balance, acc.decimalPlaces) }}</div>
                      <div class="stats">
                        <span>累计获得：{{ formatAmount(acc.totalEarned, acc.decimalPlaces) }}</span>
                        <span>累计消费：{{ formatAmount(acc.totalConsumed, acc.decimalPlaces) }}</span>
                      </div>
                    </div>
                    <div class="shine"></div>
                  </div>
                </div>
              </div>

              <div class="statistics">
                <div class="section-title-row">
                  <div class="section-title">统计</div>
                  <el-radio-group v-model="statPeriod" size="small" class="period-switch">
                    <el-radio-button label="7d">近7天</el-radio-button>
                    <el-radio-button label="30d">近30天</el-radio-button>
                    <el-radio-button label="all">总计</el-radio-button>
                  </el-radio-group>
                </div>

                  <div class="stat-card">
                  <div class="stat-content">
                    <div class="kpi-grid">
                      <div class="kpi-card income">
                        <div class="kpi-title">收入</div>
                        <div class="kpi-value">+{{ formatAmount(currentStats.income, 2) }}</div>
                      </div>
                      <div class="kpi-card expense">
                        <div class="kpi-title">支出</div>
                        <div class="kpi-value">-{{ formatAmount(currentStats.expense, 2) }}</div>
                      </div>
                      <div class="kpi-card net">
                        <div class="kpi-title">净增</div>
                        <div class="kpi-value">{{ formatAmount(currentStats.net, 2) }}</div>
                      </div>
                  </div>
                  </div>
                  <div class="chart-container">
                    <svg class="sparkline" :viewBox="spark.viewBox" preserveAspectRatio="none">
                      <polyline :points="currentSpark.points" fill="none" :stroke="currentSpark.color" stroke-width="2" />
                      <circle v-if="currentSpark.points" :cx="currentSparkLast.x" :cy="currentSparkLast.y" r="1.6" :fill="currentSpark.color" />
                    </svg>
                  </div>
                </div>
              </div>
            </div>


          </el-tab-pane>

          <el-tab-pane label="交易记录" name="transactions">
            <div class="transactions-tab">
            <div class="filters">
              <div class="colored-select" :style="{ '--type-color': selectedFilterTypeColor }">
                <el-select v-model="filters.type" placeholder="积分类型" clearable class="filter-item w-140">
                  <el-option
                    v-for="t in typeOptions"
                    :key="t.typeCode"
                    :label="t.typeName"
                    :value="t.typeCode"
                    :style="{ color: getTypeColor(t) }"
                  >
                    <span :style="{ color: getTypeColor(t) }">{{ t.typeName }}<span v-if="Number(t.status) === 0" style="color:#909399">（已禁用）</span></span>
                  </el-option>
                </el-select>
              </div>
              <el-select v-model="filters.txType" placeholder="交易类型" clearable class="filter-item w-140">
                <el-option v-for="opt in txTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
              <el-select v-model="filters.scenario" placeholder="场景" clearable class="filter-item w-180">
                <el-option v-for="opt in scenarioOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
              <el-date-picker
                v-model="filters.range"
                type="datetimerange"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                class="filter-item w-200"
              />
              <el-input v-model="filters.keyword" placeholder="关键词（说明/场景）" class="filter-item w-180" @keyup.enter="applyFilters" />
              <div class="filter-actions">
                <el-button type="primary" @click="applyFilters">查询</el-button>
                <el-button @click="resetFilters">重置</el-button>
              </div>
            </div>

            <el-table :data="pagedTransactions" border stripe size="small">
              <el-table-column type="index" label="#" width="60" :index="indexMethod" />
              <el-table-column prop="time" label="时间" width="180" />
              <el-table-column label="积分类型" width="90">
                <template #default="{ row }">
                  <span :style="{ color: getTypeColorByCode(row.typeCode) }">{{ row.typeName }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="scenarioName" label="使用场景" width="100" show-overflow-tooltip />
              <el-table-column label="金额" width="100" align="right">
                <template #default="{ row }">
                  <span class="number-text" :style="{ color: Number(row.amount) > 0 ? '#67c23a' : (Number(row.amount) < 0 ? '#f56c6c' : '#303133') }">
                    {{ Number(row.amount) > 0 ? '+' : (Number(row.amount) < 0 ? '-' : '') }}{{ formatAmount(Math.abs(Number(row.amount || 0)), 2) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="交易后余额" width="110" align="right">
                <template #default="{ row }">
                  <span class="number-text">{{ row.balanceAfterText }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="desc" label="说明" show-overflow-tooltip />
              <el-table-column label="操作" width="90" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" size="small" @click="openTxDetail(row)">查看详情</el-button>
                </template>
              </el-table-column>
              <template #empty>
                <EmptyBox size="table" title="暂无交易记录" desc="可更换类型、交易类型或时间范围后重试" />
              </template>
            </el-table>

            <div class="table-footer" v-if="filteredTransactions.length > 0">
              <CommonPagination
                :total="filteredTransactions.length"
                :current="page"
                :size="pageSize"
                :show-info="false"
                align="right"
                @update:current="val => page = val"
                @update:size="val => (pageSize = val, page = 1)"
              />
            </div>
            </div>
          </el-tab-pane>

          <!-- 兑换码管理（仅超级管理员可见） -->
          <el-tab-pane v-if="isSuperAdmin" label="兑换码管理" name="redeem-admin">
            <div class="redeem-admin">
              <div class="redeem-admin-header" style="margin-bottom: 12px; display:flex; justify-content: space-between; align-items:center;">
                <div class="section-title">兑换码</div>
                <el-button type="primary" @click="openGenDialog">生成兑换码</el-button>
              </div>

              <el-table :data="redeemPage.records" border stripe size="small" class="mt-12 redeem-admin-table">
                <el-table-column label="#" type="index" width="60" />
                <el-table-column prop="codeKey" label="兑换码" min-width="420">
                  <template #default="{ row }">
                    <div class="code-cell">
                      <el-input :model-value="row.codeKey" readonly class="code-input" />
                      <el-button type="primary" size="small" @click="copy(row.codeKey)">复制</el-button>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="类型" width="90" align="center">
                  <template #default="{ row }">
                    <span :style="{ color: getTypeColorByCode(row.creditTypeCode) }">{{ getTypeLabelByCode(row.creditTypeCode) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="数量" width="80" align="center">
                  <template #default="{ row }">
                    <span class="amount-center">{{ formatAmount(row.amount, 0) }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag v-if="row.status===0" type="warning">待使用</el-tag>
                    <el-tag v-else-if="row.status===1" type="success">已兑换</el-tag>
                    <el-tag v-else-if="row.status===2" type="info">已失效</el-tag>
                    <el-tag v-else type="danger">已作废</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="过期时间" width="150">
                  <template #default="{ row }">{{ formatDateTimeStr(row.expireTime) }}</template>
                </el-table-column>
                <el-table-column prop="createdByName" label="创建人" width="120" />
                <el-table-column prop="redeemedByName" label="兑换人" width="120" />
                <el-table-column label="兑换时间" width="150">
                  <template #default="{ row }">{{ formatDateTimeStr(row.redeemedTime) }}</template>
                </el-table-column>
                <el-table-column prop="remark" label="备注" show-overflow-tooltip />
              </el-table>

              <div class="table-footer" v-if="redeemPage.total>0">
                <CommonPagination
                  :total="redeemPage.total"
                  :current="redeemQuery.pageNum"
                  :size="redeemQuery.pageSize"
                  :show-info="false"
                  align="right"
                  @update:current="val => (redeemQuery.pageNum = val, loadRedeemPage())"
                  @update:size="val => (redeemQuery.pageSize = val, redeemQuery.pageNum = 1, loadRedeemPage())"
                />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
  <!-- 交易详情弹窗 -->
  <el-dialog v-model="txDetailVisible" title="交易详情" width="560px">
    <div v-if="currentTx">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="时间">{{ currentTx.time }}</el-descriptions-item>
        <el-descriptions-item label="积分类型">{{ currentTx.typeName }}</el-descriptions-item>
        <el-descriptions-item label="交易类型">{{ currentTx.txTypeName }}</el-descriptions-item>
        <el-descriptions-item label="场景">{{ currentTx.scenarioName }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ currentTx.amountText }}</el-descriptions-item>
        <el-descriptions-item label="交易后余额">{{ currentTx.balanceAfterText }}</el-descriptions-item>
        <el-descriptions-item label="说明">{{ currentTx.desc }}</el-descriptions-item>
      </el-descriptions>
    </div>
    <template #footer>
      <el-button type="primary" @click="txDetailVisible = false">确定</el-button>
    </template>
  </el-dialog>

  <!-- 兑换码弹窗（使用）- 多彩卡片式与动画 -->
  <el-dialog v-model="redeemVisible" title="兑换码 🎁" width="560px" class="redeem-dialog" :close-on-click-modal="false">
    <div class="redeem-hero-card">
      <div class="hero-left">
        <div class="hero-title">输入兑换码领取积分</div>
        <div class="hero-sub">先查询 · 再兑换 · 即刻到账</div>
        <div class="input-row">
          <el-input v-model.trim="redeemForm.code" placeholder="请输入兑换码" @keyup.enter="fetchRedeemInfo">
            <template #append>
              <el-button :loading="redeemInfoLoading" @click="fetchRedeemInfo">查询</el-button>
            </template>
          </el-input>
        </div>
      </div>
      <div class="hero-right">
        <div class="floating-badge">积分好礼</div>
        <div class="sparkles"></div>
      </div>
      <div class="glow"></div>
    </div>

    <div v-if="redeemInfo && !redeemResult" class="redeem-mini-card pop-in" :style="{ '--accent': getTypeColorByCode(redeemInfo.creditTypeCode) }">
      <div class="caption">该兑换码内容</div>
      <div class="content-centered">
        <div class="type-and-amount">
          <span class="amount" :style="{ color: getTypeColorByCode(redeemInfo.creditTypeCode) }">{{ redeemInfo.amount ?? '-' }}</span>
          <span class="unit">{{ getTypeLabelByCode(redeemInfo.creditTypeCode) }}</span>
        </div>
        <div class="meta one-line">
          <template v-if="redeemInfo.expireTime">
            <span class="expiry">过期：{{ formatDateTimeStr(redeemInfo.expireTime) }}</span>
          </template>
          <template v-if="redeemInfo.expireTime && redeemInfo.remark">
            <span class="divider">·</span>
          </template>
          <template v-if="redeemInfo.remark">
            <span class="remark">{{ redeemInfo.remark }}</span>
          </template>
        </div>
        <el-tag class="status-badge" size="small" :type="statusTagType(redeemInfo.status)">
          {{ redeemInfo.statusDesc || redeemInfo.statusName || (redeemInfo.status===0 ? '待使用' : (redeemInfo.status===1 ? '已兑换' : (redeemInfo.status===2 ? '已失效' : '未知')) ) }}
        </el-tag>
      </div>
    </div>

    <div v-if="redeemResult" class="redeem-success-card pop-in" :style="{ '--accent': getTypeColorByCode(redeemResult.creditTypeCode) }">
      <div class="celebrate">
        <div class="icon">🎉</div>
        <div class="title">兑换成功！</div>
 
      </div>
      <div class="success-core">
        <div class="type-and-amount">
          <span class="unit" :style="{ color: getTypeColorByCode(redeemResult.creditTypeCode) }">{{ getTypeLabelByCode(redeemResult.creditTypeCode) }}</span>
          <span class="amount" :style="{ color: getTypeColorByCode(redeemResult.creditTypeCode) }">{{ redeemResult.addedAmount ?? redeemResult.amount ?? '-' }}</span>
        </div>
        <div class="time">{{ formatDateTimeStr(redeemResult.redeemedTime) || '-' }}</div>
      </div>
      <div class="success-glow"></div>
    </div>

    <canvas ref="confettiCanvas" v-show="showConfetti" class="confetti-canvas"></canvas>

    <template #footer>
      <template v-if="!redeemResult">
        <el-tooltip content="先查询兑换码信息再兑换">
          <span>
            <el-button class="btn-cta" :loading="redeemLoading" :disabled="!redeemInfo" @click="submitRedeem">立即兑换</el-button>
          </span>
        </el-tooltip>
      </template>
      <template v-else>
        <el-button class="btn-cta" type="primary" @click="closeRedeemDialog">关闭</el-button>
      </template>
    </template>
  </el-dialog>

  <!-- 生成兑换码弹窗（管理员） -->
  <el-dialog v-model="genDialogVisible" title="生成兑换码" width="520px" class="gen-dialog" @closed="onGenDialogClosed">
    <el-form :model="genForm" label-width="96px">
      <el-form-item label="积分类型">
        <el-select v-model="genForm.creditTypeCode" placeholder="请选择">
          <el-option v-for="t in typeOptions" :key="t.typeCode" :label="t.typeName" :value="t.typeCode" />
        </el-select>
      </el-form-item>
      <el-form-item label="数量">
        <el-input v-model.number="genForm.amount" placeholder="正数" />
      </el-form-item>
      <el-form-item label="过期时间">
        <el-date-picker v-model="genForm.expire" type="datetime" placeholder="可选" value-format="YYYY-MM-DD HH:mm:ss" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="genForm.remark" placeholder="备注（可选）" />
      </el-form-item>
      <div v-if="lastCodeKey" class="result-box fade-in">
        <div class="result-title">生成结果</div>
        <div class="result-row">
          <el-input :model-value="lastCodeKey" readonly />
          <el-button type="primary" @click="copyLastCode" :disabled="!lastCodeKey">复制</el-button>
        </div>
      
      </div>
    </el-form>
    <template #footer>
      <div class="gen-footer">
        <div class="spacer"></div>
        <el-button v-if="!hideGenButtons" @click="genDialogVisible = false">取消</el-button>
        <el-tooltip v-if="!hideGenButtons" :content="canGenerate ? '生成兑换码' : '参数未变更，避免重复生成'">
          <span>
            <el-button type="primary" :loading="genLoading" :disabled="!canGenerate" @click="generateCode()">生成</el-button>
          </span>
        </el-tooltip>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import { defineComponent, ref, computed, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import AppBreadcrumb from '@/components/common/Breadcrumb.vue'
import EmptyBox from '@/components/common/EmptyBox.vue'
import CommonPagination from '@/components/common/Pagination.vue'
import { myCreditsApi, creditTypeApi, creditTransactionApi, creditScenarioApi, redeemCodeApi } from '@/api/credit-system'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { onBeforeUnmount } from 'vue'

export default defineComponent({
  name: 'MyCredits',
  components: { CommonPagination, Refresh, AppBreadcrumb, EmptyBox },
  setup() {
    const auth = useAuthStore()
    const route = useRoute()
    const router = useRouter()
    const activeTab = ref(route.query.tab || 'overview')

    // sync tab <-> route.query.tab
    watch(() => route.query.tab, (tab) => {
      if (tab && tab !== activeTab.value) activeTab.value = tab
    })
    watch(activeTab, (tab) => {
      if (route.query.tab !== tab) {
        router.replace({ query: { ...route.query, tab } }).catch(() => {})
      }
    })

    // 类型字典
    const typeOptions = ref([])
    const typeColorMap = reactive({})
    const typeLabelMap = reactive({})

    // 余额与统计
    const balances = ref([])

    // 交易类型选项
    const txTypeOptions = ref([])
    const txTypeLabelMap = reactive({})
    const scenarioDict = ref({})
    const scenarioOptions = ref([])
    const txDetailVisible = ref(false)
    const currentTx = ref(null)

    const loadTxTypeOptions = async () => {
      try {
        const resp = await creditTransactionApi.getTransactionTypeOptions()
        if (resp && resp.code === 200 && Array.isArray(resp.data)) {
          txTypeOptions.value = resp.data.map(it => ({ label: it.label || it.text || it.value, value: it.value }))
          txTypeLabelMap && txTypeOptions.value.forEach(it => { txTypeLabelMap[it.value] = it.label })
        }
      // eslint-disable-next-line no-empty
      // eslint-disable-next-line no-empty
      // eslint-disable-next-line no-empty
      } catch {}
    }

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

    const formatAmount = (num, dp = 0) => {
      return Number(num).toLocaleString('zh-CN', { minimumFractionDigits: dp, maximumFractionDigits: dp })
    }
    const formatDateTimeStr = (val) => {
      if (!val) return ''
      // accept formats: 'YYYY-MM-DD HH:mm:ss', ISO, or ms timestamp
      let d
      if (typeof val === 'number') d = new Date(val)
      else if (typeof val === 'string') d = new Date(val.replace('T',' ').replace(/-/g,'/'))
      else if (val instanceof Date) d = val
      else return String(val)
      if (Number.isNaN(d.getTime())) return String(val)
      const pad = (n) => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
    }

    // 实际交易数据
    const allTransactions = ref([])

    const filters = ref({ type: '', txType: '', scenario: '', keyword: '', range: [] })
    const page = ref(1)
    const pageSize = ref(10)

    const filteredTransactions = computed(() => {
      return allTransactions.value.filter(row => {
        if (filters.value.type && row.typeCode !== filters.value.type) return false
        if (filters.value.txType && row.txType !== filters.value.txType) return false
        if (filters.value.scenario && row.scenarioCode !== filters.value.scenario) return false
        if (filters.value.keyword && !(`${row.desc} ${row.scenarioName}`.includes(filters.value.keyword))) return false
        return true
      })
    })

    const pagedTransactions = computed(() => {
      const start = (page.value - 1) * pageSize.value
      return filteredTransactions.value.slice(start, start + pageSize.value)
    })

    // 表格自然数序号（跨页连续）
    const indexMethod = (index) => (page.value - 1) * pageSize.value + index + 1

    const applyFilters = async () => { page.value = 1; await loadTransactions() }
    const resetFilters = async () => { filters.value = { type: '', txType: '', scenario: '', keyword: '', range: [] }; page.value = 1; await loadTransactions() }
    const exportCsv = () => {
      const header = ['序号','时间','类型','场景','金额','交易后余额','说明']
      const rows = filteredTransactions.value.map((r, i) => [i + 1, r.time, r.typeName, r.scenarioName, r.amountText, r.balanceAfterText, r.desc])
      const csv = [header, ...rows].map(a => a.join(',')).join('\n')
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = 'my-credits-transactions.csv'
      link.click()
      URL.revokeObjectURL(link.href)
    }

    // 加载类型、余额、统计、交易
    const loadTypes = async () => {
      const resp = await creditTypeApi.getCreditTypeList({ pageNum: 1, pageSize: 999 })
      if (resp && resp.code === 200) {
        const list = Array.isArray(resp.data?.records) ? resp.data.records : (resp.data?.list || resp.data || [])
        typeOptions.value = list
        list.forEach(t => {
          if (t.typeCode) {
            typeColorMap[t.typeCode] = t.colorCode || t.displayColor || t.color || '#333'
            typeLabelMap[t.typeCode] = t.typeName || t.typeCode
          }
        })
      }
    }

    const loadBalances = async () => {
      const resp = await myCreditsApi.getMyCreditsBalance()
      if (resp && resp.code === 200) {
        const accs = Array.isArray(resp.data?.accounts) ? resp.data.accounts : []
        balances.value = accs.map(a => ({
          typeCode: a.creditTypeCode,
          typeName: typeLabelMap[a.creditTypeCode] || a.creditTypeName || a.creditTypeCode,
          unitName: a.unitName,
          color: a.colorCode || typeColorMap[a.creditTypeCode] || '#333',
          decimalPlaces: a.decimalPlaces ?? 0,
          balance: Number(a.balance || 0),
          totalEarned: Number(a.totalEarned || 0),
          totalConsumed: Number(a.totalConsumed || 0)
        }))
      } else {
        balances.value = []
      }
    }

    const loadTransactions = async () => {
      try {
      const params = {
        pageNum: 1,
        pageSize: 1000,
        creditTypeCode: filters.value.type || undefined,
        transactionType: filters.value.txType || undefined,
        scenarioCode: filters.value.scenario || undefined,
        keyword: filters.value.keyword || undefined,
        startTimeStr: Array.isArray(filters.value.range) && filters.value.range[0] ? filters.value.range[0] : undefined,
        endTimeStr: Array.isArray(filters.value.range) && filters.value.range[1] ? filters.value.range[1] : undefined
      }
      const resp = await myCreditsApi.getMyCreditsHistory(params)
      const data = resp?.data
      const records = Array.isArray(data) ? data : (Array.isArray(data?.records) ? data.records : [])
      allTransactions.value = records.map(r => ({
        id: r.id,
        time: (r.createTime || '').replace('T', ' '),
        typeCode: r.creditTypeCode,
        typeName: typeLabelMap[r.creditTypeCode] || r.creditTypeName || r.creditTypeCode,
        txType: r.transactionType,
        txTypeName: txTypeLabelMap[r.transactionType] || r.transactionTypeDesc || mapTxType(r.transactionType),
        scenarioCode: r.scenarioCode,
        scenarioName: r.scenarioName || scenarioDict.value[r.scenarioCode] || r.scenarioCode || '-',
        amount: Number(r.amount || 0),
        amountText: formatAmount(Number(r.amount || 0), 2),
        balanceAfter: Number(r.balanceAfter || 0),
        balanceAfterText: formatAmount(Number(r.balanceAfter || 0), 2),
        desc: r.description || '-'
      }))
      } catch (e) {
        allTransactions.value = []
      }
    }

    // 本地统计（7/30/总）
    const calcStatsInRange = (days) => {
      let rows = allTransactions.value
      if (typeof days === 'number' && days > 0) {
        const end = new Date().getTime()
        const start = end - days * 24 * 60 * 60 * 1000
        rows = rows.filter(r => {
          const ts = new Date(r.time).getTime()
          return !Number.isNaN(ts) && ts >= start && ts <= end
        })
      }
      let income = 0
      let expense = 0
      for (const r of rows) {
        const v = Number(r.amount || 0)
        if (v > 0) income += v
        else if (v < 0) expense += Math.abs(v)
      }
      return { income, expense, net: income - expense }
    }
    const stats7d = computed(() => calcStatsInRange(7))
    const stats30d = computed(() => calcStatsInRange(30))
    const statsAll = computed(() => calcStatsInRange(null))

    // 统计周期切换
    const statPeriod = ref('7d')
    const currentStats = computed(() => {
      if (statPeriod.value === '30d') return stats30d.value
      if (statPeriod.value === 'all') return statsAll.value
      return stats7d.value
    })

    // simple sparkline based on net values per day
    const buildDailyNetSeries = (days) => {
      const end = new Date()
      const start = new Date(end.getTime() - days * 24 * 60 * 60 * 1000)
      const map = new Map()
      const fmt = (d) => `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
      for (let i = 0; i < days; i++) {
        const d = new Date(start.getTime() + i * 24 * 60 * 60 * 1000)
        map.set(fmt(d), 0)
      }
      for (const r of allTransactions.value) {
        const t = new Date(r.time)
        if (Number.isNaN(t.getTime())) continue
        const key = fmt(t)
        if (!map.has(key)) continue
        const v = Number(r.amount || 0)
        map.set(key, (map.get(key) || 0) + v)
      }
      return Array.from(map.values())
    }
    const toSparkPoints = (series, width = 100, height = 28) => {
      if (!series || series.length === 0) return ''
      const min = Math.min(...series, 0)
      const max = Math.max(...series, 0)
      const span = max - min || 1
      const stepX = series.length > 1 ? (width / (series.length - 1)) : width
      const points = series.map((v, i) => {
        const x = (i * stepX).toFixed(2)
        const y = (height - ((v - min) / span) * height).toFixed(2)
        return `${x},${y}`
      })
      return points.join(' ')
    }
    const spark = { viewBox: '0 0 100 28' }
    const spark7 = computed(() => ({ points: toSparkPoints(buildDailyNetSeries(7)) }))
    const spark30 = computed(() => ({ points: toSparkPoints(buildDailyNetSeries(30)) }))
    const sparkAll = computed(() => {
      // pick last 90 days for readability
      const days = 90
      return { points: toSparkPoints(buildDailyNetSeries(days)) }
    })

    const currentSpark = computed(() => {
      if (statPeriod.value === '30d') return { ...spark30.value, color: '#67C23A' }
      if (statPeriod.value === 'all') return { ...sparkAll.value, color: '#E6A23C' }
      return { ...spark7.value, color: '#409EFF' }
    })

    const currentSparkLast = computed(() => {
      const points = (currentSpark.value?.points || '').split(' ')
      const last = points[points.length - 1] || ''
      const [x, y] = last.split(',')
      return { x, y }
    })

    // 场景字典与筛选项
    const loadScenariosDict = async () => {
      try {
        const resp = await creditScenarioApi.getCreditScenarioList({ pageNum: 1, pageSize: 999 })
        if (resp && resp.code === 200) {
          const data = resp.data
          const list = Array.isArray(data?.records) ? data.records : (data?.list || data || [])
          const dict = {}
          list.forEach(s => { if (s.scenarioCode) dict[s.scenarioCode] = s.scenarioName || s.scenarioCode })
          scenarioDict.value = dict
          scenarioOptions.value = list.filter(s => s.scenarioCode).map(s => ({ value: s.scenarioCode, label: s.scenarioName || s.scenarioCode }))
          // 仅保留筛选与名称映射所需数据
        }
      // eslint-disable-next-line no-empty
      } catch {}
    }

    // 场景规则入口（统一页）
    const goScenarioPricing = () => { router.push({ name: 'ScenarioPricing' }).catch(() => {}) }

    // 兑换码
    const redeemVisible = ref(false)
    const redeemLoading = ref(false)
    const redeemInfoLoading = ref(false)
    const redeemForm = ref({ code: '' })
    const redeemInfo = ref(null)
    const redeemResult = ref(null)
    const confettiCanvas = ref(null)
    const showConfetti = ref(false)
    let confettiTimer = null
    const openRedeem = () => {
      redeemForm.value = { code: '' }
      redeemInfo.value = null
      redeemResult.value = null
      redeemVisible.value = true
    }

    // 兑换码管理（超级管理员）
    const genLoading = ref(false)
    const genDialogVisible = ref(false)
    const genForm = ref({ creditTypeCode: '', amount: 1, expire: '', remark: '' })
    const lastCodeKey = ref('')
    const lastGenFingerprint = ref('')
    const canGenerate = ref(true)
    const hideGenButtons = ref(false)
    const redeemPage = ref({ total: 0, records: [] })
    const redeemQuery = ref({ pageNum: 1, pageSize: 10, keyword: '', status: null })

    const openGenDialog = () => {
      genForm.value = { creditTypeCode: (typeOptions.value?.[0]?.typeCode) || '', amount: 1, expire: '', remark: '' }
      lastCodeKey.value = ''
      lastGenFingerprint.value = ''
      canGenerate.value = true
      hideGenButtons.value = false
      genDialogVisible.value = true
    }

    const onGenDialogClosed = () => {
      // 清理状态
      lastCodeKey.value = ''
      lastGenFingerprint.value = ''
      canGenerate.value = true
      hideGenButtons.value = false
    }

    const loadRedeemPage = async () => {
      try {
        const resp = await redeemCodeApi.page({
          pageNum: redeemQuery.value.pageNum,
          pageSize: redeemQuery.value.pageSize,
          keyword: redeemQuery.value.keyword,
          status: redeemQuery.value.status
        })
        if (resp && resp.code === 200) {
          redeemPage.value = resp.data || { total: 0, records: [] }
        }
      // eslint-disable-next-line no-empty
      } catch {}
    }

    const buildFingerprint = () => {
      const f = {
        creditTypeCode: genForm.value.creditTypeCode,
        amount: genForm.value.amount,
        expire: genForm.value.expire || '',
        remark: genForm.value.remark || ''
      }
      return JSON.stringify(f)
    }

    const recomputeCanGenerate = () => {
      canGenerate.value = buildFingerprint() !== lastGenFingerprint.value
    }

    const generateCode = async () => {
      if (!genForm.value.creditTypeCode) { ElMessage.warning('请选择积分类型'); return }
      if (!Number(genForm.value.amount) || Number(genForm.value.amount) <= 0) { ElMessage.warning('请输入有效数量'); return }
      // 二次保护：参数未变化则阻止
      if (!canGenerate.value) { ElMessage.info('参数未变更，如需重复生成可点击“仍要生成”'); return }
      genLoading.value = true
      try {
        const resp = await redeemCodeApi.generate({
          creditTypeCode: genForm.value.creditTypeCode,
          amount: genForm.value.amount,
          expireTime: genForm.value.expire || undefined,
          remark: genForm.value.remark || undefined
        })
        if (resp && resp.code === 200 && resp.data) {
          lastCodeKey.value = resp.data.codeKey
          ElMessage.success('生成成功')
          await loadRedeemPage()
          // 记录指纹并禁用再次生成（除非参数变更）
          lastGenFingerprint.value = buildFingerprint()
          canGenerate.value = false
          hideGenButtons.value = true
        }
      } catch (e) {
        ElMessage.error(e?.message || '生成失败')
      } finally {
        genLoading.value = false
      }
    }

    // 监听生成表单变更，自动恢复“生成”可用状态
    watch(() => [genForm.value.creditTypeCode, genForm.value.amount, genForm.value.expire, genForm.value.remark], () => {
      recomputeCanGenerate()
      // 参数一旦变更，恢复按钮显示
      hideGenButtons.value = false
    })

    // 移除“仍要生成”逻辑

    // 移除复制并关闭相关交互

    const copy = async (text) => {
      try { await navigator.clipboard.writeText(text); ElMessage.success('已复制') } catch { ElMessage.error('复制失败') }
    }

    const fetchRedeemInfo = async () => {
      if (!redeemForm.value.code) { ElMessage.warning('请输入兑换码'); return }
      redeemInfoLoading.value = true
      redeemResult.value = null
      try {
        const resp = await myCreditsApi.getRedeemInfo(redeemForm.value.code)
        if (resp && resp.code === 200) {
          const info = resp.data || {}
          redeemInfo.value = {
            ...info,
            creditTypeCode: info.creditTypeCode,
            amount: info.amount
          }
          if (!redeemInfo.value) {
            ElMessage.warning('未查询到兑换码信息')
          }
        } else {
          ElMessage.error(resp?.message || '查询失败')
          redeemInfo.value = null
        }
      } catch (e) {
        ElMessage.error(e?.message || '查询失败')
        redeemInfo.value = null
      } finally {
        redeemInfoLoading.value = false
      }
    }

    const submitRedeem = async () => {
      if (!redeemForm.value.code) { ElMessage.warning('请输入兑换码'); return }
      redeemLoading.value = true
      try {
        const resp = await myCreditsApi.exchangeCredits({ code: redeemForm.value.code })
        if (resp && resp.code === 200) {
        ElMessage.success('兑换成功')
          const data = resp.data || {}
          redeemResult.value = {
            ...data,
            creditTypeCode: data.creditTypeCode || redeemInfo.value?.creditTypeCode,
            addedAmount: data.addedAmount || data.amount || redeemInfo.value?.amount,
            redeemedTime: data.redeemedTime || new Date().toISOString().replace('T',' ').slice(0,19)
          }
        await Promise.all([loadBalances(), loadTransactions()])
        // 若当前为管理员并展示兑换码管理页，同步刷新兑换码列表
        if (auth?.user?.role === 'SUPER_ADMIN') {
          await loadRedeemPage()
        }
          triggerConfetti()
        } else {
          ElMessage.error(resp?.message || '兑换失败')
        }
      } catch (e) {
        ElMessage.error((e && e.message) || '兑换失败')
      } finally {
        redeemLoading.value = false
      }
    }
    const closeRedeemDialog = () => { redeemVisible.value = false }
    const getTypeColor = (t) => (t?.colorCode || t?.displayColor || t?.color || typeColorMap[t?.typeCode] || '#000')
    const getTypeColorByCode = (code) => (typeColorMap[code] || '#000')
    const getTypeLabelByCode = (code) => (typeLabelMap[code] || code)
    const selectedFilterTypeColor = computed(() => {
      const code = filters.value.type
      if (!code) return '#000'
      return typeColorMap[code] || '#000'
    })
    const statusTagType = (status) => {
      if (status === 0) return 'warning'
      if (status === 1) return 'success'
      if (status === 2) return 'info'
      return 'danger'
    }

    const openTxDetail = (row) => { currentTx.value = row; txDetailVisible.value = true }

    const lastUpdated = ref('')
    const refreshing = ref(false)
    const updateTimestamp = () => {
      const d = new Date()
      const pad = (n) => String(n).padStart(2, '0')
      lastUpdated.value = `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
    }
    const init = async () => {
      refreshing.value = true
      await Promise.all([loadTypes(), loadTxTypeOptions(), loadScenariosDict()])
      await Promise.all([loadBalances(), loadTransactions()])
      updateTimestamp()
      refreshing.value = false
    }

    onMounted(() => { init(); if (auth?.user?.role === 'SUPER_ADMIN') { genForm.value.creditTypeCode = (typeOptions.value?.[0]?.typeCode) || ''; loadRedeemPage() } })

    const resizeConfetti = () => {
      const c = confettiCanvas.value
      if (!c) return
      const dlg = c.closest('.el-dialog')
      if (!dlg) return
      const rect = dlg.getBoundingClientRect()
      c.width = Math.floor(rect.width)
      c.height = Math.floor(rect.height)
    }

    const triggerConfetti = () => {
      const c = confettiCanvas.value
      if (!c) return
      resizeConfetti()
      showConfetti.value = true
      const ctx = c.getContext('2d')
      const colors = ['#FF6B6B','#FFD93D','#6BCB77','#4D96FF','#B967FF']
      const pieces = Array.from({ length: 60 }).map(() => ({
        x: Math.random() * c.width,
        y: -10 - Math.random() * 40,
        r: 4 + Math.random() * 6,
        vx: -1 + Math.random() * 2,
        vy: 2 + Math.random() * 2,
        a: Math.random() * Math.PI,
        color: colors[Math.floor(Math.random()*colors.length)]
      }))
      const start = performance.now()
      const duration = 1600
      const draw = (t) => {
        const elapsed = t - start
        ctx.clearRect(0,0,c.width,c.height)
        pieces.forEach(p => {
          p.x += p.vx
          p.y += p.vy
          p.a += 0.1
          ctx.save()
          ctx.translate(p.x, p.y)
          ctx.rotate(p.a)
          ctx.fillStyle = p.color
          ctx.fillRect(-p.r/2, -p.r/2, p.r, p.r)
          ctx.restore()
        })
        if (elapsed < duration) {
          confettiTimer = requestAnimationFrame(draw)
        } else {
          showConfetti.value = false
          ctx.clearRect(0,0,c.width,c.height)
        }
      }
      if (confettiTimer) cancelAnimationFrame(confettiTimer)
      confettiTimer = requestAnimationFrame(draw)
    }

    const stopConfetti = () => {
      if (confettiTimer) {
        cancelAnimationFrame(confettiTimer)
        confettiTimer = null
      }
      showConfetti.value = false
    }

    const onResize = () => { if (showConfetti.value) resizeConfetti() }
    window.addEventListener('resize', onResize)
    onBeforeUnmount(() => {
      window.removeEventListener('resize', onResize)
      stopConfetti()
    })

    return {
      isSuperAdmin: computed(() => auth?.user?.role === 'SUPER_ADMIN'),
      activeTab,
      typeOptions,
      balances,
      stats7d,
      stats30d,
      statsAll,
      spark,
      spark7,
      spark30,
      sparkAll,
      statPeriod,
      currentStats,
      currentSpark,
      currentSparkLast,
      txTypeOptions,
      scenarioOptions,
      // redeem admin
      genForm,
      genLoading,
      genDialogVisible,
      onGenDialogClosed,
      lastCodeKey,
      lastGenFingerprint,
      canGenerate,
      hideGenButtons,
      redeemPage,
      redeemQuery,
      loadRedeemPage,
      openGenDialog,
      generateCode,
      copy,
      filters,
      page,
      pageSize,
      filteredTransactions,
      pagedTransactions,
      indexMethod,
      applyFilters,
      resetFilters,
      exportCsv,
      formatAmount,
      formatDateTimeStr,
      getTypeColor,
      getTypeColorByCode,
      getTypeLabelByCode,
      selectedFilterTypeColor,
      statusTagType,
      goScenarioPricing,
      // toolbar actions
      refreshAll: async () => { refreshing.value = true; await Promise.all([loadTypes(), loadTxTypeOptions(), loadScenariosDict()]); await Promise.all([loadBalances(), loadTransactions()]); updateTimestamp(); refreshing.value = false },
      openRedeem,
      // tx detail dialog
      txDetailVisible,
      currentTx,
      openTxDetail,
      // redeem dialog
      redeemVisible,
      redeemForm,
      redeemLoading,
      redeemInfoLoading,
      redeemInfo,
      redeemResult,
      fetchRedeemInfo,
      submitRedeem,
      closeRedeemDialog,
      confettiCanvas,
      showConfetti,
      lastUpdated,
      refreshing
    }
  }
})
</script>

<style lang="scss" scoped>
.my-credits-page {
  padding: 8px 0 12px; /* 全局去壳后，缩小页级留白 */
  min-height: calc(100vh - var(--app-header-height) - var(--main-padding-v));
  display: flex;
  flex-direction: column;

  .page-content { flex: 1; display: flex; min-height: 0; }

  .content-card {
    flex: 1 1 auto;
    width: 100%;
    min-height: 100%;
    display: flex;
    flex-direction: column;
    :deep(.el-card__body) { flex: 1; display: flex; flex-direction: column; min-height: 0; }
    :deep(.el-tabs) { display: flex; flex-direction: column; height: 100%; }
    :deep(.el-tabs__content) { flex: 1; display: flex; flex-direction: column; min-height: 0; }
    :deep(.el-tab-pane) { flex: 1; display: flex; flex-direction: column; min-height: 0; }
    .list-header { display: flex; justify-content: space-between; align-items: center; }
    .list-title { display: inline-flex; align-items: center; gap: 8px; font-weight: 600; color: #303133; font-size: 15px; }
    .header-tools { display: inline-flex; align-items: center; gap: 8px; }
    .last-updated { color: #909399; font-size: 12px; }

    .overview-hero {
      position: relative;
      overflow: hidden;
      border: 1px solid #e6e8eb;
      border-radius: 12px;
      padding: 16px 16px 18px;
      margin-bottom: 14px;
      background: radial-gradient(120% 120% at 0% 0%, #f6faff 0%, #ffffff 40%),
                  linear-gradient(180deg, #ffffff 0%, #fafcff 100%);
      .hero-main { display: flex; flex-direction: column; gap: 6px; }
      .hero-title { font-size: 18px; font-weight: 700; color: #1f2d3d; letter-spacing: 0.2px; }
      .hero-sub { color: #606266; font-size: 13px; }
      .hero-actions { position: absolute; right: 16px; top: 50%; transform: translateY(-50%); display: inline-flex; align-items: center; gap: 8px; }
      .hero-updated { margin-top: 10px; color: #909399; font-size: 12px; }
      .hero-glow {
        position: absolute; right: -40px; top: -40px; width: 180px; height: 180px; border-radius: 50%;
        background: radial-gradient(circle, rgba(64,158,255,0.18), rgba(64,158,255,0));
        filter: blur(2px);
        pointer-events: none;
        animation: floatGlow 6s ease-in-out infinite alternate;
      }
    }
    @keyframes floatGlow { from { transform: translateY(0px); } to { transform: translateY(8px); } }

    .overview-toolbar { margin-bottom: 12px; }
    .overview-grid {
      display: grid;
      grid-template-columns: 2fr 1fr;
      gap: 20px;
      max-width: 1240px;
      margin-bottom: 20px;
    }

    .section-title { font-weight: 600; margin-bottom: 12px; font-size: 14px; color: #303133; }

    .balance-cards {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
      gap: 14px;
    }
    .balance-card {
      position: relative;
      border: 1px solid #e6e8eb; border-radius: 12px; padding: 12px;
      background: linear-gradient(180deg, #ffffff 0%, #fafafa 100%);
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
      transition: box-shadow .2s ease, transform .2s ease;
      .card-head { font-size: 14px; margin-bottom: 8px; display: flex; align-items: baseline; gap: 4px; }
      .type-name { font-weight: 700; color: var(--accent); }
      .unit { color: #909399; font-size: 12px; }
      .card-body {
        .amount { font-size: 26px; font-weight: 800; margin-bottom: 6px; text-align: right; font-variant-numeric: tabular-nums; color: var(--accent); }
        .stats { color: #777; font-size: 12px; display: flex; gap: 16px; justify-content: space-between; }
      }
      &:hover { box-shadow: 0 2px 12px rgba(64, 158, 255, 0.10); transform: translateY(-2px); }
      &.glossy { background-image: linear-gradient(180deg, rgba(255,255,255,0.8), rgba(250,250,250,0.9)); }
      .shine {
        position: absolute; inset: 0; pointer-events: none; border-radius: 12px;
        background: linear-gradient(120deg, rgba(255,255,255,0) 20%, rgba(255,255,255,0.6) 40%, rgba(255,255,255,0) 60%);
        transform: translateX(-100%);
        transition: transform .6s ease;
      }
      &:hover .shine { transform: translateX(100%); }
    }

    .section-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
    .period-switch :deep(.el-radio-button__inner) { padding: 6px 10px; }
    .period-switch :deep(.is-active .el-radio-button__inner) { background: #ecf5ff; color: #409EFF; border-color: #409EFF; }
    .stat-card { 
      border: 1px solid #e6e8eb; 
      border-radius: 12px; 
      padding: 16px; 
      display: grid; 
      grid-template-columns: 1fr 200px; 
      gap: 16px; 
      align-items: center; 
      background: #fff; 
      box-shadow: 0 1px 3px rgba(0,0,0,0.05);
    }
    .stat-content { display: flex; flex-direction: column; gap: 12px; }
    .kpi-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
    .kpi-card { 
      border: 1px solid #e6e8eb; 
      border-radius: 8px; 
      padding: 12px; 
      background: #fafbfc; 
      text-align: center;
      transition: all 0.2s ease;
    }
    .kpi-card:hover { 
      background: #f5f7fa; 
      transform: translateY(-1px); 
      box-shadow: 0 2px 8px rgba(0,0,0,0.08);
    }
    .kpi-card .kpi-title { font-size: 12px; color: #909399; margin-bottom: 4px; }
    .kpi-card .kpi-value { font-size: 18px; font-weight: 700; font-variant-numeric: tabular-nums; }
    .kpi-card.income .kpi-value { color: #67C23A; }
    .kpi-card.expense .kpi-value { color: #F56C6C; }
    .kpi-card.net .kpi-value { color: #303133; }
    .chart-container { display: flex; align-items: center; justify-content: center; }
    .stat-rows { display: flex; flex-direction: column; gap: 6px; }
    .stat-subtitle { font-weight: 600; color: #303133; margin-bottom: 4px; }
    .stat-row { display: flex; justify-content: space-between; color: #606266; font-size: 13px; }
    .stat-row.total { font-weight: 700; color: #303133; }
    .text-success { color: #67C23A; }
    .text-danger { color: #F56C6C; }
    .sparkline { width: 100%; height: 28px; margin-top: 6px; }

    // numeric alignment helper
    .number-text {
      text-align: right;
      font-variant-numeric: tabular-nums;
    }
    .el-table .cell .number-text {
      display: inline-block;
      min-width: 80px;
    }
    :deep(.el-table) {
      --row-height: 42px;
    }
    :deep(.el-table__row) {
      height: var(--row-height);
    }

    /* 交易记录字体增大 */
    .transactions-tab {
      display: flex; flex-direction: column; min-height: 0; flex: 1;
      :deep(.el-table) { font-size: 14px; }
      :deep(.el-table .cell) { font-size: 14px; }
      :deep(.el-button.is-link) { font-size: 13px; }
    }

    .filters { display: flex; flex-wrap: nowrap; gap: 10px; margin-bottom: 14px; align-items: center; }
    .filter-item { width: 160px; }
    .w-140 { width: 140px; }
    .w-180 { width: 180px; }
    .w-200 { width: 200px; }
    .filter-actions { margin-left: auto; display: flex; gap: 8px; }

    .table-footer { margin-top: auto; padding-top: 12px; display: flex; justify-content: flex-end; }

    /* 兑换码管理 tab 使用同样的弹性布局，保证分页吸底 */
    .redeem-admin { display: flex; flex-direction: column; min-height: 0; flex: 1; }
    .overview-skeleton { margin-bottom: 16px; }
    .rule-card-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 14px;
    }
    .rule-card {
      border: 1px solid #e6e8eb; border-radius: 8px; padding: 12px; background: #fff;
      display: flex; flex-direction: column; gap: 10px; cursor: default;
      transition: box-shadow .2s ease, transform .2s ease;
    }
    .rule-card:hover { box-shadow: 0 2px 10px rgba(0,0,0,0.06); transform: translateY(-1px); }
    .rule-card-head { display: flex; align-items: baseline; justify-content: space-between; gap: 8px; }
    .rule-name { font-weight: 600; color: #303133; }
    .rule-price { font-weight: 700; }
    .rule-card-meta { display: flex; flex-wrap: wrap; gap: 6px; }
    .badge { background: #ecf5ff; color: #409EFF; border-radius: 4px; padding: 2px 6px; font-size: 12px; }
    .badge.ok { background: #f0f9eb; color: #67C23A; }
    .badge.off { background: #fef0f0; color: #F56C6C; }
    .rule-desc-text { color: #606266; font-size: 12px; line-height: 1.5; height: 36px; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; }
    .empty-tip { color: #909399; padding: 20px; text-align: center; }

    /* 响应式 */
    @media (max-width: 992px) {
      .overview-grid { grid-template-columns: 1fr; }
      .stat-card { grid-template-columns: 1fr; gap: 12px; }
      .chart-container { margin-top: 12px; }
    }
    @media (max-width: 640px) {
      .kpi-grid { grid-template-columns: 1fr; }
      .list-title { font-size: 14px; }
      .hero-actions { position: static !important; margin-top: 8px; }
      .stat-card { padding: 12px; }
    }
  }
  /* Hero按钮美化 */
  .btn-hero {
    border: none;
    height: 30px;
    line-height: 30px;
    padding: 0 14px;
    border-radius: 999px;
    font-weight: 600;
    letter-spacing: 0.2px;
    box-shadow: 0 6px 16px rgba(64,158,255,0.18);
    transition: transform .15s ease, box-shadow .15s ease, filter .15s ease;
  }
  .btn-hero:hover { transform: translateY(-1px); filter: brightness(1.03); }
  .btn-hero:active { transform: translateY(0); filter: brightness(0.98); }
  .btn-hero-primary {
    background: linear-gradient(135deg, #3a8ee6 0%, #409eff 60%, #66b1ff 100%);
    color: #fff;
  }
  .btn-hero-warning {
    background: linear-gradient(135deg, #e6a23c 0%, #f3b760 60%, #f5c179 100%);
    color: #fff;
    box-shadow: 0 6px 16px rgba(230,162,60,0.22);
  }
}

/* 生成兑换码弹窗样式与动画 */
.gen-dialog :deep(.el-dialog__header) {
  background: linear-gradient(90deg, rgba(64,158,255,0.08), rgba(103,194,58,0.08));
  border-bottom: 1px solid #eef2f6;
}
.gen-dialog :deep(.el-dialog__title) {
  font-weight: 700;
  color: #1f2d3d;
}
.gen-dialog :deep(.el-dialog) {
  border-radius: 12px;
}
.gen-dialog :deep(.el-dialog__body) {
  background: linear-gradient(180deg, #ffffff 0%, #fbfcff 100%);
}
.gen-footer {
  width: 100%;
  display: flex;
  align-items: center;
}
.gen-footer .left-tools { display: inline-flex; gap: 8px; }
.gen-footer .spacer { flex: 1; }

.result-box {
  margin-top: 8px;
  padding: 12px;
  border: 1px dashed #c6e2ff;
  border-radius: 8px;
  background: linear-gradient(180deg, #f5faff 0%, #ffffff 100%);
}
.result-title { color: #909399; font-size: 12px; margin-bottom: 6px; }
.result-row { display: flex; gap: 8px; align-items: center; }
.result-tip { color: #a0a3a8; font-size: 12px; margin-top: 6px; }

/* 简单淡入动画 */
.fade-in { animation: fadeIn .24s ease-out; }
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 兑换码查询/结果卡片 */
.redeem-info-card, .redeem-result-card {
  margin-top: 8px;
  border: 1px solid #e6e8eb;
  border-radius: 10px;
  background: linear-gradient(180deg, #ffffff 0%, #fbfbfe 100%);
  padding: 12px;
}
.info-title { font-weight: 600; color: #303133; margin-bottom: 8px; }
.info-grid { display: grid; grid-template-columns: 1fr; gap: 6px; }
.info-row { display: flex; justify-content: space-between; font-size: 13px; }
.info-row .label { color: #909399; }
.info-row .value { color: #303133; font-weight: 600; }
</style>

<style lang="scss" scoped>
/* 兑换弹窗-多彩卡片式与动画 */
.redeem-dialog :deep(.el-dialog__header) {
  background: linear-gradient(90deg, rgba(255,107,107,0.08), rgba(77,150,255,0.08));
  border-bottom: 1px solid #eef2f6;
}
.redeem-dialog :deep(.el-dialog) { border-radius: 14px; overflow: hidden; }
.redeem-dialog :deep(.el-dialog__body) { background: linear-gradient(180deg, #ffffff 0%, #fbfcff 100%); }

.redeem-hero-card {
  position: relative;
  display: grid;
  grid-template-columns: 1.6fr 1fr;
  gap: 12px;
  border: 1px solid #e6e8eb;
  border-radius: 12px;
  padding: 14px;
  background:
    radial-gradient(140% 160% at 0% 0%, rgba(255,217,61,0.12) 0%, rgba(255,255,255,0) 60%),
    radial-gradient(160% 140% at 100% 0%, rgba(77,150,255,0.12) 0%, rgba(255,255,255,0) 60%),
    linear-gradient(180deg, #ffffff 0%, #fafbff 100%);
}
.redeem-hero-card .hero-left { display: flex; flex-direction: column; gap: 8px; }
.redeem-hero-card .hero-title { font-size: 16px; font-weight: 800; color: #1f2d3d; }
.redeem-hero-card .hero-sub { font-size: 12px; color: #606266; }
.redeem-hero-card .input-row { margin-top: 2px; }
.redeem-hero-card .hero-right { position: relative; }
.redeem-hero-card .floating-badge {
  position: absolute; right: 8px; top: 8px; padding: 4px 10px; border-radius: 999px;
  font-weight: 700; font-size: 12px; color: #fff; background: linear-gradient(135deg, #ff6b6b, #ff9f40);
  box-shadow: 0 6px 14px rgba(255,107,107,0.24);
  animation: floatBadge 3s ease-in-out infinite;
  pointer-events: none;
}
.redeem-hero-card .sparkles {
  position: absolute; right: 6px; bottom: 6px; width: 80px; height: 80px;
  background: radial-gradient(circle at 30% 30%, rgba(185,103,255,0.32), rgba(185,103,255,0));
  filter: blur(2px);
  pointer-events: none;
}
.redeem-hero-card .glow {
  position: absolute; left: -20px; top: -20px; width: 140px; height: 140px; border-radius: 50%;
  background: radial-gradient(circle, rgba(75,222,151,0.18), rgba(75,222,151,0));
  animation: floatGlow 6s ease-in-out infinite alternate;
  pointer-events: none;
}
@keyframes floatBadge { 0% { transform: translateY(0); } 50% { transform: translateY(-4px); } 100% { transform: translateY(0); } }

.redeem-preview-card {
  position: relative;
  display: grid; grid-template-columns: 1.5fr 1fr; gap: 10px; margin-top: 12px;
  border: 1px solid #e6e8eb; border-radius: 12px; padding: 14px; background: #fff;
  box-shadow: 0 6px 18px rgba(0,0,0,0.04);
}
.redeem-preview-card .card-left { display: flex; flex-direction: column; gap: 8px; }
.redeem-preview-card .pill { display: inline-block; padding: 2px 8px; border-radius: 999px; color: #fff; font-size: 12px; font-weight: 700; }
.redeem-preview-card .amount { display: flex; align-items: baseline; gap: 8px; }
.redeem-preview-card .amount .num { font-size: 28px; font-weight: 900; }
.redeem-preview-card .amount .label { color: #606266; font-weight: 700; }
.redeem-preview-card .meta { color: #606266; font-size: 13px; }
.redeem-preview-card .card-right { position: relative; overflow: hidden; border-left: 1px dashed #eef2f6; }
.redeem-preview-card .shine { position: absolute; inset: 0; background: linear-gradient(120deg, rgba(255,255,255,0) 20%, rgba(255,255,255,0.6) 40%, rgba(255,255,255,0) 60%); transform: translateX(-100%); }
.redeem-preview-card:hover .shine { transform: translateX(100%); transition: transform .8s ease; }
.redeem-preview-card .decor-circle { position: absolute; width: 80px; height: 80px; border-radius: 50%; right: 12px; top: 12px; background: radial-gradient(circle, var(--accent, #409EFF), rgba(255,255,255,0)); opacity: .12; }
.redeem-preview-card .decor-triangle { position: absolute; right: 30px; bottom: 16px; width: 0; height: 0; border-left: 16px solid transparent; border-right: 16px solid transparent; border-bottom: 28px solid var(--accent, #409EFF); opacity: .08; }

.redeem-success-card {
  position: relative;
  margin-top: 12px;
  border: 1px solid rgba(103,194,58,0.35);
  border-radius: 12px;
  padding: 14px;
  background:
    radial-gradient(160% 180% at 0% 0%, rgba(103,194,58,0.12) 0%, rgba(255,255,255,0) 60%),
    linear-gradient(180deg, #ffffff 0%, #f8fffb 100%);
  box-shadow: 0 8px 24px rgba(103,194,58,0.12);
}
.redeem-success-card .celebrate { text-align: center; }
.redeem-success-card .celebrate .icon { font-size: 28px; }
.redeem-success-card .celebrate .title { font-size: 18px; font-weight: 900; color: #1f2d3d; margin-top: 4px; }
.redeem-success-card .celebrate .desc { color: #606266; font-size: 12px; margin-top: 2px; }
.redeem-success-card .success-grid { margin-top: 10px; display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.redeem-success-card .success-grid .row { display: flex; justify-content: space-between; font-size: 13px; }
/* 新的成功核心布局 */
.redeem-success-card .success-core { margin-top: 10px; display: flex; flex-direction: column; align-items: center; gap: 6px; }
.redeem-success-card .success-core .type-and-amount { display: inline-flex; align-items: baseline; gap: 8px; }
.redeem-success-card .success-core .type-and-amount .unit { font-weight: 800; color: #303133; }
.redeem-success-card .success-core .type-and-amount .amount { font-size: 28px; font-weight: 900; line-height: 1; }
.redeem-success-card .success-core .time { color: #606266; font-size: 13px; }
.redeem-success-card .success-glow { position: absolute; right: -24px; top: -24px; width: 160px; height: 160px; border-radius: 50%; background: radial-gradient(circle, rgba(64,158,255,0.18), rgba(64,158,255,0)); filter: blur(2px); pointer-events: none; }

.confetti-canvas {
  position: absolute;
  inset: 54px 0 0 0; /* below header */
  width: 100%;
  height: calc(100% - 54px);
  pointer-events: none;
}

.pop-in { animation: popIn .28s ease-out; }
@keyframes popIn { from { opacity: 0; transform: translateY(-6px) scale(0.98); } to { opacity: 1; transform: translateY(0) scale(1); } }

  /* 极简领取信息卡 */
  .redeem-mini-card {
    position: relative;
    border: 1px solid #e6e8eb;
    border-radius: 12px;
    padding: 12px 12px 12px 14px;
    background: linear-gradient(180deg, #ffffff, #fafbff);
    overflow: hidden;
    margin-top: 10px; /* 与上方输入区域拉开间距 */
  }
  .redeem-mini-card .caption {
    position: absolute;
    top: 8px; left: 14px;
    font-size: 12px; color: #9aa1aa;
    letter-spacing: .5px;
  }
  .redeem-mini-card::before {
    content: "";
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4px;
    background: var(--accent);
    opacity: .8;
  }
  .redeem-mini-card::after {
    content: "";
    position: absolute;
    top: 0; left: -40%;
    width: 40%; height: 100%;
    background: linear-gradient(120deg, rgba(255,255,255,0) 0%, rgba(255,255,255,.7) 50%, rgba(255,255,255,0) 100%);
    transform: skewX(-18deg);
    animation: shine 2.4s ease-in-out infinite;
  }
  @keyframes shine { 0% { left: -50%; } 60% { left: 120%; } 100% { left: 120%; } }
  .redeem-mini-card .content-centered {
    display: flex; flex-direction: column; align-items: center; justify-content: center;
    gap: 8px; padding-top: 12px; padding-bottom: 6px;
    min-height: 72px;
    position: relative;
  }
  .redeem-mini-card .type-and-amount { display: inline-flex; align-items: baseline; gap: 8px; }
  .redeem-mini-card .type-and-amount .amount { font-size: 28px; font-weight: 900; line-height: 1; text-shadow: 0 1px 0 rgba(0,0,0,.03); }
  .redeem-mini-card .type-and-amount .unit { font-weight: 700; color: #606266; }
  .redeem-mini-card .meta { color: #6b7280; font-size: 13px; display: inline-flex; align-items: center; gap: 8px; }
  .redeem-mini-card .meta.one-line { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  .redeem-mini-card .meta .divider { color: #d0d3d8; }
  .redeem-mini-card .meta .remark { color: #4b5563; }
  .redeem-mini-card .status-badge {
    position: absolute; right: 10px; top: 8px;
  }

  /* CTA统一样式 */
  .btn-cta {
    border: none;
    height: 32px;
    line-height: 32px;
    padding: 0 16px;
    border-radius: 999px;
    font-weight: 700;
    color: #fff;
    background: linear-gradient(135deg, #3a8ee6 0%, #409eff 60%, #66b1ff 100%);
    box-shadow: 0 6px 16px rgba(64,158,255,0.18);
    transition: transform .15s ease, box-shadow .15s ease, filter .15s ease;
  }
  .btn-cta:hover { transform: translateY(-1px); filter: brightness(1.03); }
  .btn-cta:active { transform: translateY(0); filter: brightness(0.98); }
  .btn-cta.is-disabled, .btn-cta:disabled { opacity: .7; box-shadow: none; filter: grayscale(10%); }

  /* 兑换码管理表格样式优化 */
  .redeem-admin-table .code-cell { display: inline-flex; gap: 8px; align-items: center; width: 100%; }
  .redeem-admin-table .code-input { width: 100%; }
  .redeem-admin-table .amount-center { display: inline-block; min-width: 48px; text-align: center; font-variant-numeric: tabular-nums; }
</style>


