<template>
  <div>
    <el-card shadow="never" class="mb-4">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="排班日期">
          <el-date-picker v-model="date" type="date" value-format="YYYY-MM-DD"
                          placeholder="选择日期" @change="load" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table :data="schedules" v-loading="loading" border stripe>
        <el-table-column label="工人" min-width="120">
          <template #default="{ row }">{{ workerName(row.workerId) }}</template>
        </el-table-column>
        <el-table-column prop="postName" label="岗位" width="120" />
        <el-table-column label="班次" width="80">
          <template #default="{ row }">{{ SHIFT_TEXT[row.shift] || row.shift }}</template>
        </el-table-column>
        <el-table-column prop="scheduleDate" label="排班日期" width="120" />
        <el-table-column label="档案状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.scheduleStatus)">{{ statusText(row.scheduleStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="stopStartDate" label="停工开始" width="120" />
        <el-table-column prop="resumeDate" label="复工日期" width="120" />
        <el-table-column prop="conclusion" label="结论" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.conclusion" :type="row.conclusion === 'stop' ? 'danger' : 'success'">
              {{ row.conclusion === 'stop' ? '停工' : '复工' }}
            </el-tag>
            <span v-else class="text-gray-300">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-if="row.injuryReportId" link type="primary" @click="goReport(row.injuryReportId)">
              工伤单
            </el-button>
            <el-button link type="primary" @click="viewEvents(row)">档案事件</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="eventDrawer" size="42%" :title="`档案事件留痕`">
      <el-timeline>
        <el-timeline-item v-for="e in events" :key="e.id"
                          :type="e.eventType === 'injury_resume' ? 'success' : 'danger'"
                          :timestamp="`${e.operatorName || ''}｜${e.eventTime || ''}`">
          <h4 class="font-medium">{{ eventTypeText(e.eventType) }}</h4>
          <p class="text-gray-600 text-sm">{{ e.eventContent }}</p>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-if="!events.length" description="暂无工伤相关事件" />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { scheduleApi, SHIFT_TEXT } from '@/api/injury'

const router = useRouter()
const date = ref(new Date().toISOString().slice(0, 10))
const schedules = ref([])
const workers = ref([])
const loading = ref(false)
const eventDrawer = ref(false)
const events = ref([])

const statusText = (s) => ({ normal: '正常排班', injury_stop: '工伤停工', resumed: '已复工' }[s] || s)
const statusTag = (s) => ({ normal: '', injury_stop: 'danger', resumed: 'success' }[s] || '')
const eventTypeText = (t) => ({ injury_report: '工伤上报', injury_stop: '停工结论', injury_resume: '复工结论' }[t] || t)
const workerName = (id) => workers.value.find(w => w.id === id)?.workerName || `工人#${id}`

const load = async () => {
  loading.value = true
  try {
    const [sRes, wRes] = await Promise.all([
      scheduleApi.list({ date: date.value || undefined }),
      scheduleApi.workers()
    ])
    schedules.value = sRes.data || []
    workers.value = wRes.data || []
  } finally {
    loading.value = false
  }
}

const viewEvents = async (row) => {
  const res = await scheduleApi.events(row.id)
  events.value = res.data || []
  eventDrawer.value = true
}

const goReport = (id) => router.push(`/pc/injury?id=${id}`)

load()
</script>
