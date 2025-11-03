<template>
  <div class="announcement-detail">
    <div class="reading-progress" :style="{ width: progress + '%'}"></div>
    <div class="topbar">
      <button class="back-btn" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </button>
      <i class="sep"></i>
      <span class="crumb">公告详情</span>
    </div>
    <div class="hero" :style="heroStyle">
      <div class="hero-content">
        <h1 class="hero-title">平台公告</h1>
      </div>
    </div>
    <div class="below">
      <div class="container">
        <div class="inner">
          <div v-if="loading" class="loading">加载中...</div>
          <div v-else-if="errorMsg" class="error">{{ errorMsg }}</div>
          <template v-else>
            <div class="header">
              <h2 class="title">{{ detail?.title }}</h2>
              <div class="meta">
                <span>发布时间：{{ formatTime(detail?.createTime) }}</span>
              </div>
            </div>
            <div class="content" v-html="detail?.content"></div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { announcementApi } from '@/api/notify/announcement'
import bannerImg from '@/static/公告banner.jpg'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const detail = ref(null)
const loading = ref(false)
const errorMsg = ref('')
const formatTime = (val) => val ? dayjs(val).format('YYYY/MM/DD HH:mm') : ''

// 顶部横幅样式（与列表页保持一致风格）
const heroBgUrl = bannerImg
const heroStyle = computed(() => ({
  // 顶层加一层半透明白色渐变，弱化背景图颜色；第三层保留渐变兜底
  backgroundImage: `linear-gradient(0deg, rgba(255,255,255,0.2), rgba(255,255,255,0.15)), url(${heroBgUrl}), linear-gradient(135deg, #f8fafc 0%, #eef2f7 100%)`,
  backgroundRepeat: 'no-repeat, no-repeat, no-repeat',
  backgroundPosition: 'center center, center, center',
  backgroundSize: 'cover, cover, cover'
}))

const load = async () => {
  const id = route.params.id
  if (!id) { errorMsg.value = '缺少公告ID'; return }
  loading.value = true
  errorMsg.value = ''
  try {
    const resp = await announcementApi.detail(id)
    if (resp?.code === 200 && resp.data) {
      detail.value = resp.data
    } else {
      throw new Error(resp?.message || '加载失败')
    }
  } catch (e) {
    errorMsg.value = e?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

const goBack = () => router.back()

// 阅读进度条
const progress = ref(0)
const onScroll = () => {
  const el = document.documentElement
  const height = el.scrollHeight - el.clientHeight
  const current = height > 0 ? (el.scrollTop / height) * 100 : 0
  progress.value = Math.max(0, Math.min(100, Math.round(current)))
}

onMounted(() => {
  load()
  window.addEventListener('scroll', onScroll, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<style lang="scss" scoped>
.announcement-detail {
  padding: 12px 0 24px;

  .topbar {
    display: flex;
    align-items: center;
    padding: 6px 0 12px;
  }
  .back-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    height: 32px;
    padding: 0 12px;
    border: 1px solid #e5e7eb;
    background: #fff;
    border-radius: 16px;
    color: #374151;
    cursor: pointer;
    transition: all .15s ease;
  }
  .back-btn:hover { background: #f9fafb; border-color: #d1d5db; }
  .back-btn:active { transform: translateY(1px); }
  .sep {
    width: 1px; height: 16px; background: #e5e7eb; margin: 0 10px;
  }
  .crumb { color: #6b7280; font-size: 14px; }

  .reading-progress {
    position: fixed;
    top: 0;
    left: 0;
    height: 2px;
    background: linear-gradient(90deg, #5b8cff, #40c7ff);
    z-index: 1999;
    transition: width .15s ease;
  }

  .hero {
    height: 180px;
    border-radius: 10px;
    background-position: center;
    background-size: cover;
    background-repeat: no-repeat;
    display: flex;
    align-items: center;
  }
  .hero-content { max-width: 720px; }
  .hero-title { font-size: 40px; font-weight: 800; letter-spacing: .2px; color: #fff; margin-left: 50px; }

  .below { background: #fff; padding: 16px 0 32px; }
  .container { max-width: 960px; margin: 0 auto; padding: 0 16px; }
  .inner { max-width: 780px; margin: 0 auto; }

  .header { margin-top: 0; }
  .title { font-size: 26px; font-weight: 700; margin: 0; color: #1f2329; }
  .meta { color: #8a8f98; margin-top: 6px; font-size: 13px; }

  .content {
    margin-top: 16px;
    background: #fff;
    padding: 18px 20px;
    border-radius: 8px;
    min-height: 240px;
    box-shadow: 0 1px 2px rgba(0,0,0,.03);
    font-size: 15px;
    line-height: 1.8;
  }
  .loading { padding: 24px; color: #64748b; text-align: center; }
  .error { padding: 24px; color: #ef4444; text-align: center; }
  .content p { margin: 10px 0; }
  .content a { color: var(--el-color-primary); word-break: break-word; }
  .content a:hover { text-decoration: underline; background: rgba(0,0,0,0.03); }
  .content ul, .content ol { padding-left: 1.25rem; }
  .content li { margin: 6px 0; }

  @media (max-width: 768px) {
    .container { padding: 0 12px; }
    .hero { height: 120px; }
    .title { font-size: 22px; }
  }
}
</style>


