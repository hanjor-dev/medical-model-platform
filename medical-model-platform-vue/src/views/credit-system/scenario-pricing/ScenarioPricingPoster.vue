/* eslint-disable */
<template>
  <div class="poster-page">
    <div class="hero">
      <div class="hero-bg" />
      <div class="hero-content">
        <h1 class="headline">
          消费计价规则
          <span class="sub">每个场景，都有清晰的规则与价值</span>
        </h1>
      </div>
    </div>

    <div class="poster-grid">
      <div v-for="s in posterScenarios" :key="s.scenarioCode" class="poster-card" :style="{ '--accent': getTypeColorByCode(s.creditTypeCode) }">
        <div class="poster-media">
          <div class="glow" />
          <div class="media-overlay">
            <div class="media-title">{{ s.scenarioName || s.scenarioCode }}</div>
            <div class="media-sub">单价：{{ formatAmount(Number(s.costPerUse || 0), s.decimalPlaces ?? 2) }}/{{ getTypeNameByCode(s.creditTypeCode) }}</div>
          </div>
        </div>
        <div class="poster-body">
          <div class="meta">
            <span class="chip">单次计费</span>
            <span class="chip light">积分类型：{{ getTypeNameByCode(s.creditTypeCode) }}</span>
            <span v-if="hasDailyLimit(s.dailyLimit)" class="chip light">每日限额：{{ s.dailyLimit }}</span>
          </div>
          <div class="desc" :title="s.description || '-'">{{ s.description || '-' }}</div>
        </div>
      </div>
    </div>
  </div>
  
</template>

<script>
// @ts-nocheck
import { defineComponent, ref, computed, onMounted, reactive } from 'vue'
import { creditTypeApi, creditScenarioApi } from '@/api/credit-system/index.js'

export default defineComponent({
  name: 'ScenarioPricingPoster',
  setup() {
    

    /** @typedef {{ typeCode?: string, typeName?: string, colorCode?: string, displayColor?: string, color?: string, status?: number }} CreditType */
    /** @typedef {{ scenarioCode?: string, scenarioName?: string, creditTypeCode?: string, costPerUse?: number|string, decimalPlaces?: number, dailyLimit?: number|string, status?: number, description?: string }} CreditScenario */

    /** @type {import('vue').Ref<CreditType[]>} */
    const typeOptions = ref([])
    /** @type {Record<string, string>} */
    const typeColorMap = reactive({})
    /** @type {Record<string, string>} */
    const typeLabelMap = reactive({})
    /** @type {import('vue').Ref<CreditScenario[]>} */
    const scenarios = ref([])

    const formatAmount = (num, dp = 0) => Number(num).toLocaleString('zh-CN', { minimumFractionDigits: dp, maximumFractionDigits: dp })
    const getTypeNameByCode = (code) => (code ? (typeLabelMap[code] || code) : '')
    const getTypeColorByCode = (code) => (code ? (typeColorMap[code] || '#409EFF') : '#409EFF')
    const hasDailyLimit = (val) => {
      if (val === null || val === undefined) return false
      const s = String(val).trim()
      if (s === '' || s.toLowerCase() === 'null' || s.toLowerCase() === 'undefined') return false
      return Number(s) > 0
    }

    /** @type {import('vue').ComputedRef<any[]>} */
    const filteredScenarios = computed(() => {
      // 页面只展示消费型场景：价格>0，不做额外筛选
      return scenarios.value.filter((s) => Number(s.costPerUse || 0) > 0)
    })

    const loadTypes = async () => {
      const resp = await creditTypeApi.getCreditTypeList({ pageNum: 1, pageSize: 999 })
      if (resp && resp.code === 200) {
        const list = Array.isArray(resp.data?.records) ? resp.data.records : (resp.data?.list || resp.data || [])
        typeOptions.value = list
        list.forEach((t) => {
          if (t.typeCode) {
            typeColorMap[t.typeCode] = t.colorCode || t.displayColor || t.color || '#409EFF'
            typeLabelMap[t.typeCode] = t.typeName || t.typeCode
          }
        })
      }
    }

    const loadScenarios = async () => {
      const resp = await creditScenarioApi.getCreditScenarioList({ pageNum: 1, pageSize: 999 })
      if (resp && resp.code === 200) {
        const data = resp.data
        scenarios.value = Array.isArray(data?.records) ? data.records : (data?.list || data || [])
      } else {
        scenarios.value = []
      }
    }

    onMounted(async () => {
      await Promise.all([loadTypes(), loadScenarios()])
    })

    /** @type {import('vue').ComputedRef<any[]>} */
    const posterScenarios = computed(() => filteredScenarios.value)

    return {
      typeOptions,
      posterScenarios,
      formatAmount,
      getTypeNameByCode,
      getTypeColorByCode,
      hasDailyLimit
    }
  }
})
</script>

<style scoped lang="scss">
.poster-page {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  background: radial-gradient(circle at 20% 0%, #f7faff 0%, #ffffff 60%);
}
.hero {
  position: relative;
  height: 150px;
  overflow: hidden;
}
.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(64,158,255,0.10), rgba(103,194,58,0.10)),
              radial-gradient(800px 200px at 10% -20%, rgba(64,158,255,0.20), transparent 60%),
              radial-gradient(600px 200px at 90% 20%, rgba(230,162,60,0.18), transparent 60%);
  filter: saturate(1.1);
}
.hero-content {
  position: relative;
  z-index: 1;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 24px 20px;
}
.headline {
  margin: 0 0 12px 0;
  font-size: 28px;
  line-height: 1.2;
  letter-spacing: 0.2px;
  color: #1f2d3d;
}
.headline .sub {
  display: block;
  margin-top: 6px;
  font-weight: 400;
  font-size: 14px;
  color: #606266;
}
.hero-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}
.hero-actions .type-filter { width: 220px; }
.hero-actions .kw { width: 260px; }

.poster-grid {
  max-width: 1200px;
  margin: 18px auto 24px;
  padding: 0 20px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}
.poster-card {
  border-radius: 14px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  transition: transform .25s ease, box-shadow .25s ease;
}
.poster-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 40px rgba(0,0,0,0.08);
}
.poster-media {
  position: relative;
  height: 136px;
  background: radial-gradient(600px 140px at 30% 20%, var(--accent, #409EFF), transparent 60%),
              radial-gradient(500px 140px at 80% 0%, rgba(31,45,61,0.5), transparent 60%),
              linear-gradient(180deg, #0b1220, #111827);
}
.poster-media .glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(240px 120px at 60% 80%, rgba(255,255,255,0.20), transparent 60%);
  mix-blend-mode: screen;
}
.media-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 14px;
  color: #e5edf7;
}
.media-title {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: 0.3px;
  color: #ffffff;
}
.media-sub {
  margin-top: 4px;
  font-size: 13px;
  color: #cfd8e3;
}
.ribbon {
  position: absolute;
  top: 14px;
  left: 14px;
  padding: 4px 10px;
  color: #fff;
  font-size: 12px;
  border-radius: 100px;
  box-shadow: 0 6px 20px rgba(0,0,0,0.20);
}
.price {
  position: absolute;
  right: 14px;
  bottom: 14px;
  display: flex;
  align-items: baseline;
  gap: 6px;
}
.price .amount { font-size: 22px; font-weight: 800; letter-spacing: 0.2px; }
.price .unit { color: #cfd8e3; }

.poster-body {
  padding: 14px;
}
.title { font-size: 16px; font-weight: 700; color: #1f2d3d; margin-bottom: 8px; }
.meta { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 8px; }
.chip { background: #ecf5ff; color: #409EFF; padding: 2px 8px; border-radius: 999px; font-size: 12px; }
.chip.light { background: #f4f4f5; color: #606266; }
.chip.on { background: #f0f9eb; color: #67C23A; }
.chip.off { background: #fef0f0; color: #F56C6C; }
.desc { color: #606266; min-height: 40px; line-height: 1.6; }

</style>


