<template>
  <div class="injury-list">
    <!-- 角色提示条 -->
    <div class="role-bar">
      <van-icon name="manager-o" />
      <span class="ml-1">{{ roleText }}</span>
      <van-tag plain type="primary" class="ml-2">{{ user?.nickname }}</van-tag>
    </div>

    <!-- 状态筛选 -->
    <van-tabs v-model:active="activeStatus" @change="loadList" sticky>
      <van-tab v-for="t in statusTabs" :key="t.value" :title="t.label" :name="t.value" />
    </van-tabs>

    <!-- 主管可新建 -->
    <div v-if="canReport" class="px-3 pt-3">
      <van-button round block type="primary" icon="plus" @click="goCreate">
        现场工伤上报
      </van-button>
    </div>

    <!-- 列表 -->
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh" class="px-3 py-3">
      <van-empty v-if="!loading && list.length === 0" description="暂无工伤上报记录" />

      <van-card
        v-for="item in list"
        :key="item.id"
        :title="`${item.workerName || '工人'} · ${item.postName || '岗位未定'}`"
        :thumb="tagThumb(item)"
        class="injury-card"
        @click="goDetail(item.id)"
      >
        <template #desc>
          <div class="text-xs text-gray-500">{{ item.reportNo }}</div>
          <div class="mt-1 flex items-center text-xs text-gray-600">
            <van-icon name="location-o" class="mr-1" />
            <span class="truncate max-w-[220px]">{{ item.injuryLocation }}</span>
          </div>
          <div class="mt-1 flex items-center text-xs text-gray-600">
            <van-icon name="clock-o" class="mr-1" />{{ item.injuryTime }}
          </div>
          <div v-if="item.hospital" class="mt-1 flex items-center text-xs text-gray-600">
            <van-icon name="hospital-o" class="mr-1" />
            <span class="truncate max-w-[220px]">{{ item.hospital }}</span>
          </div>
        </template>
        <template #footer>
          <van-tag :type="statusTagType(item.reportStatus)" plain>
            {{ STATUS_TEXT[item.reportStatus] || item.reportStatus }}
          </van-tag>
          <van-tag v-if="item.conclusion" :type="item.conclusion === 'stop' ? 'danger' : 'success'" plain class="ml-1">
            {{ CONCLUSION_TEXT[item.conclusion] }}
          </van-tag>
          <van-tag type="warning" plain class="ml-1">材料 {{ item.materialProgress || 0 }}%</van-tag>
        </template>
      </van-card>

      <div v-if="list.length > 0" class="text-center text-xs text-gray-400 py-3">
        共 {{ total }} 条
      </div>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { injuryApi } from '@/api/injury'
import { STATUS_TEXT, CONCLUSION_TEXT } from '@/api/injury'
import { showToast } from 'vant'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || 'null')

const statusTabs = [
  { label: '全部', value: '' },
  { label: '待提交', value: 'draft' },
  { label: '已上报', value: 'reported' },
  { label: '材料齐', value: 'submitted' },
  { label: '已结论', value: 'concluded' }
]
const activeStatus = ref('')
const list = ref([])
const total = ref(0)
const loading = ref(false)
const refreshing = ref(false)

const canReport = computed(() => user && (user.role === 'supervisor' || user.role === 'admin'))
const roleText = computed(() => {
  return {
    admin: '平台管理员（全部数据）',
    supervisor: '现场主管（本项目，可上报/结论）',
    labor: '劳务公司（补充保险资料）',
    enterprise: '用工企业（仅查看本项目）'
  }[user?.role || 'admin']
})

const statusTagType = (s) => ({ draft: 'default', reported: 'primary', submitted: 'success', concluded: 'warning' }[s] || 'default')
const tagThumb = () => ''

const loadList = async () => {
  loading.value = true
  try {
    const res = await injuryApi.page({ current: 1, size: 50, reportStatus: activeStatus.value || undefined })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

const onRefresh = async () => {
  await loadList()
  refreshing.value = false
  showToast('已刷新')
}

const goCreate = () => router.push('/h5/injury/create')
const goDetail = (id) => router.push(`/h5/injury/${id}`)

loadList()
</script>

<style scoped>
.role-bar {
  @apply flex items-center px-3 py-2 text-xs text-gray-600 bg-white;
}
.injury-card {
  margin: 0 0 12px 0;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
}
.injury-card :deep(.van-card__thumb) {
  display: none;
}
</style>
