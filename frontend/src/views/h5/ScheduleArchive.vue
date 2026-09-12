<template>
  <div>
    <van-cell-group inset title="排班档案（含工伤结论回写）">
      <van-field label="日期" readonly is-link :model-value="date" @click="showCalendar = true" />
      <van-popup v-model:show="showCalendar" position="bottom" round>
        <van-calendar
          :show-confirm="false"
          :min-date="minDate"
          :max-date="maxDate"
          @confirm="onCalendar"
        />
      </van-popup>
    </van-cell-group>

    <van-empty v-if="!loading && schedules.length === 0" description="当天暂无排班" />

    <van-card
      v-for="s in schedules"
      :key="s.id"
      :title="`${s.workerName || ('工人#' + s.workerId)}｜${s.postName || ''}`"
      class="mt-3"
    >
      <template #desc>
        <div class="text-xs text-gray-600">
          {{ SHIFT_TEXT[s.shift] || s.shift }}｜{{ s.scheduleDate }}
        </div>
        <div class="mt-1">
          <van-tag :type="tagType(s.scheduleStatus)">{{ statusText(s.scheduleStatus) }}</van-tag>
          <span v-if="s.stopStartDate" class="ml-2 text-xs text-gray-500">停工起 {{ s.stopStartDate }}</span>
          <span v-if="s.resumeDate" class="ml-2 text-xs text-gray-500">复工 {{ s.resumeDate }}</span>
        </div>
      </template>
      <template #footer>
        <van-button v-if="s.injuryReportId" size="mini" type="primary" plain @click="goReport(s.injuryReportId)">
          查看工伤单
        </van-button>
        <van-button size="mini" plain class="ml-1" @click="toggleEvents(s)">
          {{ expanded === s.id ? '收起档案' : '档案留痕' }}
        </van-button>
      </template>
    </van-card>

    <van-cell-group v-if="events.length" inset class="mt-3">
      <van-steps direction="vertical" :active="events.length" active-color="#07c160">
        <van-step v-for="e in events" :key="e.id">
          <h4 class="text-sm font-medium">{{ eventTypeText(e.eventType) }}</h4>
          <p class="text-xs text-gray-500">{{ e.operatorName }}｜{{ (e.eventTime || '').replace('T', ' ') }}</p>
          <p class="text-xs text-gray-600">{{ e.eventContent }}</p>
        </van-step>
      </van-steps>
    </van-cell-group>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { scheduleApi, SHIFT_TEXT } from '@/api/injury'

const router = useRouter()
const date = ref(new Date().toISOString().slice(0, 10))
const showCalendar = ref(false)
const schedules = ref([])
const events = ref([])
const expanded = ref(null)
const loading = ref(false)
const minDate = new Date(2024, 0, 1)
const maxDate = new Date(2030, 11, 31)

const statusText = (s) => ({ normal: '正常排班', injury_stop: '工伤停工', resumed: '已复工' }[s] || s)
const tagType = (s) => ({ normal: 'primary', injury_stop: 'danger', resumed: 'success' }[s] || 'default')
const eventTypeText = (t) => ({ injury_report: '工伤上报', injury_stop: '停工结论', injury_resume: '复工结论' }[t] || t)

const onCalendar = (v) => {
  const pad = (n) => String(n).padStart(2, '0')
  date.value = `${v.getFullYear()}-${pad(v.getMonth() + 1)}-${pad(v.getDate())}`
  showCalendar.value = false
  load()
}

const load = async () => {
  loading.value = true
  try {
    const res = await scheduleApi.list({ date: date.value })
    schedules.value = res.data || []
    events.value = []
    expanded.value = null
  } finally {
    loading.value = false
  }
}

const toggleEvents = async (s) => {
  if (expanded.value === s.id) {
    expanded.value = null
    events.value = []
    return
  }
  expanded.value = s.id
  const res = await scheduleApi.events(s.id)
  events.value = res.data || []
}

const goReport = (id) => router.push(`/h5/injury/${id}`)

load()
</script>
