<template>
  <div class="announcement-center-page">
    <div class="page-content">
      <el-card class="content-card">
        <template #header>
          <AppBreadcrumb :items="fixedCrumbs" />
        </template>

        <template v-if="loading">
          <el-row :gutter="24" class="grid">
            <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="n in 8" :key="n">
              <el-skeleton animated :rows="4" style="padding: 14px;" />
            </el-col>
          </el-row>
        </template>

        <template v-else-if="list.length === 0">
          <el-empty description="暂无公告" />
        </template>

        <el-row v-else :gutter="24" class="grid">
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in list" :key="item.id">
            <div class="card" @click="goDetail(item.id)">
              <div class="thumb">
                <img :src="item.coverUrl || defaultCover" alt="thumb" />
              </div>
              <div class="info">
                <div class="title" :title="item.title">{{ item.title }}</div>
                <div class="desc" v-html="item.summary || ''"></div>
                <div class="meta">
                  <span class="time">{{ formatTime(item.createTime) }}</span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>

        <CommonPagination
          class="pager"
          :current="page.current"
          :size="page.size"
          :total="page.total"
          :page-sizes="[12, 24, 48]"
          @update:current="(v) => (page.current = v)"
          @update:size="(v) => (page.size = v)"
          @current-change="handlePageChange"
          @size-change="handlePageSizeChange"
        />
      </el-card>
    </div>
  </div>
  
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { announcementApi } from '@/api/notify/announcement.js'
import cardBgImg from '@/static/公告背景.jpg'
import CommonPagination from '@/components/common/Pagination.vue'
import AppBreadcrumb from '@/components/common/Breadcrumb.vue'

const router = useRouter()

const fixedCrumbs = [
  { title: '消息与公告' },
  { title: '平台公告' }
]

const query = ref({ keyword: '' })
const page = ref({ current: 1, size: 12, total: 0 })

const list = ref([])
const defaultCover = cardBgImg
const loading = ref(false)

const formatTime = (val) => (val ? dayjs(val).format('YYYY/MM/DD HH:mm') : '')


const load = async () => {
  loading.value = true
  const resp = await announcementApi.getVisible({
    pageNum: page.value.current,
    pageSize: page.value.size,
    keyword: query.value.keyword
  })
  if (resp?.code === 200) {
    list.value = resp.data?.records || []
    page.value.total = resp.data?.total || 0
  }
  loading.value = false
}


const handlePageChange = (p) => {
  page.value.current = p
  load()
}

const handlePageSizeChange = (s) => {
  page.value.size = s
  page.value.current = 1
  load()
}

const goDetail = (id) => {
  router.push({ name: 'AnnouncementDetail', params: { id } })
}

onMounted(load)
</script>

<style lang="scss" scoped>
.announcement-center-page {
  padding: 8px 0 12px;
  background: transparent;
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - var(--app-header-height) - var(--main-padding-v));

  .content-card {
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    flex: 1 1 auto;
    min-height: 100%;
    display: flex;
    flex-direction: column;
    :deep(.el-card__body) { flex: 1; display: flex; flex-direction: column; min-height: 0; }

    .grid { margin-top: 0; }
    .card {
      background: #fff;
      border-radius: 8px;
      overflow: hidden;
      margin-bottom: 20px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.06);
      cursor: pointer;
      transition: transform .2s ease;
      &:hover { transform: translateY(-4px); }
      .thumb { height: 110px; background: #f5f7fa; position: relative; img{ width: 100%; height: 100%; object-fit: cover; object-position: center; } }
      .thumb::after { content: ""; position: absolute; inset: 0; background: linear-gradient(0deg, rgba(255,255,255,0.15), rgba(255,255,255,0.15)); pointer-events: none; }
      .info { padding: 10px 12px; }
      .title { font-size: 16px; font-weight: 600; line-height: 20px; height: 40px; overflow: hidden; }
      .desc { margin-top: 6px; color: #606266; height: 32px; overflow: hidden; }
      .meta { margin-top: 6px; color: #909399; font-size: 12px; }
    }

    .pager { padding: 12px 0 24px; margin-top: auto; }

    /* 局部覆盖通用分页样式：去白底与顶边框；信息左、分页居中 */
    .pager:deep(.common-pagination) {
      display: grid !important;
      grid-template-columns: 1fr auto 1fr !important;
      align-items: center !important;
      justify-content: initial !important;
      background: transparent !important;
      border-top: none !important;
      padding: 0 !important;
    }
    .pager:deep(.el-pagination) {
      background: transparent !important;
    }
    .pager:deep(.pagination-info) {
      grid-column: 1 !important;
      justify-self: start !important;
    }
    .pager:deep(.pagination-main) {
      grid-column: 2 !important;
      justify-self: center !important;
    }
  }
}
</style>


