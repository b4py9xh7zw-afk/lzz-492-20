<template>
  <div class="work-page">
    <van-search
      v-model="searchForm.workName"
      placeholder="请输入工作名称搜索"
      @search="handleSearch"
      @clear="handleSearch"
      class="mb-3"
    />
    
    <div class="filter-bar mb-3 px-2 flex gap-2">
      <van-dropdown-menu>
        <van-dropdown-item 
          v-model="searchForm.workStatus" 
          :options="statusOptions"
          @change="handleSearch"
        >
          <template #title>
            <span class="text-sm">状态: {{ getStatusText(searchForm.workStatus) || '全部' }}</span>
          </template>
        </van-dropdown-item>
        <van-dropdown-item 
          v-model="searchForm.priority" 
          :options="priorityOptions"
          @change="handleSearch"
        >
          <template #title>
            <span class="text-sm">优先级: {{ getPriorityText(searchForm.priority) || '全部' }}</span>
          </template>
        </van-dropdown-item>
      </van-dropdown-menu>
    </div>

    <div class="fixed bottom-20 right-4 z-50">
      <van-button
        type="primary"
        round
        icon="plus"
        size="large"
        @click="handleAdd"
        class="shadow-lg"
      >
        新增
      </van-button>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="loadData"
      >
        <div 
          v-for="item in list" 
          :key="item.id"
          class="work-card mb-3 mx-2 rounded-lg shadow-sm bg-white overflow-hidden"
          @click="handleEdit(item)"
        >
          <div class="p-4">
            <div class="flex items-center justify-between mb-2">
              <h3 class="text-base font-semibold text-gray-800 flex-1">{{ item.workName }}</h3>
              <van-tag 
                :type="getStatusType(item.workStatus)"
                class="ml-2 rounded-full"
              >
                {{ getStatusText(item.workStatus) }}
              </van-tag>
            </div>
            <p class="text-sm text-gray-600 mb-3 line-clamp-2">{{ item.workContent }}</p>
            <div class="flex items-center justify-between text-xs text-gray-500">
              <div class="flex items-center gap-3">
                <span v-if="item.workTime">
                  <van-icon name="clock-o" /> {{ formatDateTime(item.workTime) }}
                </span>
                <van-tag 
                  :type="getPriorityType(item.priority)"
                  size="small"
                  class="rounded-full"
                >
                  {{ getPriorityText(item.priority) }}
                </van-tag>
              </div>
              <van-button
                size="mini"
                type="danger"
                @click.stop="handleDelete(item)"
                class="rounded-full"
              >
                删除
              </van-button>
            </div>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>

    <!-- 新增/编辑弹窗 -->
    <van-popup
      v-model:show="popupVisible"
      :title="popupTitle"
      position="bottom"
      :style="{ height: '80%' }"
      round
      closeable
      close-icon-position="top-right"
    >
      <div class="popup-content p-4">
        <van-form @submit="handleSubmit" ref="formRef">
          <van-cell-group inset>
            <van-field
              v-model="formData.workName"
              name="workName"
              label="工作名称"
              placeholder="请输入工作名称"
              :rules="[{ required: true, message: '请输入工作名称' }]"
            />
            <van-field
              v-model="formData.workContent"
              name="workContent"
              label="工作内容"
              type="textarea"
              rows="4"
              placeholder="请输入工作内容"
              :rules="[{ required: true, message: '请输入工作内容' }]"
            />
            <van-field
              v-model="workStatusDisplay"
              name="workStatus"
              label="工作状态"
              placeholder="请选择工作状态"
              is-link
              readonly
              @click="showStatusPicker = true"
              :rules="[{ required: true, message: '请选择工作状态' }]"
            />
            <van-field
              v-model="priorityDisplay"
              name="priority"
              label="优先级"
              placeholder="请选择优先级"
              is-link
              readonly
              @click="showPriorityPicker = true"
              :rules="[{ required: true, message: '请选择优先级' }]"
            />
            <van-field
              v-model="formData.workTime"
              name="workTime"
              label="工作时间"
              placeholder="请选择工作时间"
              is-link
              readonly
              @click="showWorkTimePicker = true"
            />
            <van-field
              v-model="formData.startTime"
              name="startTime"
              label="开始时间"
              placeholder="请选择开始时间"
              is-link
              readonly
              @click="showStartTimePicker = true"
            />
            <van-field
              v-model="formData.endTime"
              name="endTime"
              label="结束时间"
              placeholder="请选择结束时间"
              is-link
              readonly
              @click="showEndTimePicker = true"
            />
            <van-field
              v-model="formData.remark"
              name="remark"
              label="备注"
              type="textarea"
              rows="3"
              placeholder="请输入备注"
            />
          </van-cell-group>
          <div class="p-4">
            <van-button 
              round 
              block 
              type="primary" 
              native-type="submit"
              :loading="submitting"
              class="mb-3"
            >
              {{ formData.id ? '更新' : '新增' }}
            </van-button>
            <van-button 
              round 
              block 
              @click="popupVisible = false"
            >
              取消
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

    <!-- 状态选择器 -->
    <van-popup v-model:show="showStatusPicker" position="bottom">
      <van-picker
        :columns="statusColumns"
        @confirm="onStatusConfirm"
        @cancel="showStatusPicker = false"
      />
    </van-popup>

    <!-- 优先级选择器 -->
    <van-popup v-model:show="showPriorityPicker" position="bottom">
      <van-picker
        :columns="priorityColumns"
        @confirm="onPriorityConfirm"
        @cancel="showPriorityPicker = false"
      />
    </van-popup>

    <!-- 工作时间选择器 -->
    <van-popup v-model:show="showWorkTimePicker" position="bottom">
      <van-datetime-picker
        v-model="workTimeValue"
        type="datetime"
        title="选择工作时间"
        @confirm="onWorkTimeConfirm"
        @cancel="showWorkTimePicker = false"
      />
    </van-popup>

    <!-- 开始时间选择器 -->
    <van-popup v-model:show="showStartTimePicker" position="bottom">
      <van-datetime-picker
        v-model="startTimeValue"
        type="datetime"
        title="选择开始时间"
        @confirm="onStartTimeConfirm"
        @cancel="showStartTimePicker = false"
      />
    </van-popup>

    <!-- 结束时间选择器 -->
    <van-popup v-model:show="showEndTimePicker" position="bottom">
      <van-datetime-picker
        v-model="endTimeValue"
        type="datetime"
        title="选择结束时间"
        @confirm="onEndTimeConfirm"
        @cancel="showEndTimePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { workApi } from '@/api/work'
import dayjs from 'dayjs'

const route = useRoute()

const loading = ref(false)
const submitting = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const popupVisible = ref(false)
const popupTitle = ref('新增工作')
const formRef = ref(null)
const showStatusPicker = ref(false)
const showPriorityPicker = ref(false)
const showWorkTimePicker = ref(false)
const showStartTimePicker = ref(false)
const showEndTimePicker = ref(false)

const list = ref([])

const searchForm = reactive({
  workName: '',
  workStatus: '',
  priority: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formData = reactive({
  id: null,
  workName: '',
  workContent: '',
  workStatus: 'pending',
  priority: 'normal',
  workTime: null,
  startTime: null,
  endTime: null,
  remark: ''
})

const workTimeValue = ref(new Date())
const startTimeValue = ref(new Date())
const endTimeValue = ref(new Date())

// 工作状态和优先级的显示文本（响应式）
const workStatusDisplay = ref('')
const priorityDisplay = ref('')

const statusOptions = [
  { text: '全部', value: '' },
  { text: '待处理', value: 'pending' },
  { text: '进行中', value: 'in_progress' },
  { text: '已完成', value: 'completed' },
  { text: '已取消', value: 'cancelled' }
]

const priorityOptions = [
  { text: '全部', value: '' },
  { text: '低', value: 'low' },
  { text: '普通', value: 'normal' },
  { text: '高', value: 'high' },
  { text: '紧急', value: 'urgent' }
]

const statusColumns = [
  { text: '待处理', value: 'pending' },
  { text: '进行中', value: 'in_progress' },
  { text: '已完成', value: 'completed' },
  { text: '已取消', value: 'cancelled' }
]

const priorityColumns = [
  { text: '低', value: 'low' },
  { text: '普通', value: 'normal' },
  { text: '高', value: 'high' },
  { text: '紧急', value: 'urgent' }
]

const loadData = async () => {
  if (refreshing.value || list.value.length === 0) {
    pagination.current = 1
    if (refreshing.value) {
      list.value = []
      finished.value = false
    }
  }
  
  loading.value = true
  try {
    const res = await workApi.page({
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    })
    if (res.code === 200) {
      if (refreshing.value || pagination.current === 1) {
        list.value = res.data.records
        refreshing.value = false
      } else {
        list.value.push(...res.data.records)
      }
      pagination.total = res.data.total
      
      if (list.value.length >= res.data.total) {
        finished.value = true
      } else {
        pagination.current++
      }
    }
  } catch (error) {
    console.error(error)
    finished.value = true
  } finally {
    loading.value = false
  }
}

const onRefresh = () => {
  finished.value = false
  refreshing.value = true
  loadData()
}

const handleSearch = () => {
  pagination.current = 1
  list.value = []
  finished.value = false
  refreshing.value = false
  loadData()
}

const handleAdd = () => {
  popupTitle.value = '新增工作'
  Object.assign(formData, {
    id: null,
    workName: '',
    workContent: '',
    workStatus: 'pending',
    priority: 'normal',
    workTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    startTime: null,
    endTime: null,
    remark: ''
  })
  workStatusDisplay.value = getStatusText('pending')
  priorityDisplay.value = getPriorityText('normal')
  workTimeValue.value = new Date()
  startTimeValue.value = new Date()
  endTimeValue.value = new Date()
  popupVisible.value = true
}

const handleEdit = async (item) => {
  popupTitle.value = '编辑工作'
  try {
    const res = await workApi.getById(item.id)
    if (res.code === 200) {
      Object.assign(formData, {
        id: res.data.id,
        workName: res.data.workName,
        workContent: res.data.workContent,
        workStatus: res.data.workStatus,
        priority: res.data.priority,
        workTime: res.data.workTime ? dayjs(res.data.workTime).format('YYYY-MM-DD HH:mm:ss') : null,
        startTime: res.data.startTime ? dayjs(res.data.startTime).format('YYYY-MM-DD HH:mm:ss') : null,
        endTime: res.data.endTime ? dayjs(res.data.endTime).format('YYYY-MM-DD HH:mm:ss') : null,
        remark: res.data.remark || ''
      })
      workStatusDisplay.value = getStatusText(res.data.workStatus)
      priorityDisplay.value = getPriorityText(res.data.priority)
      workTimeValue.value = res.data.workTime ? new Date(res.data.workTime) : new Date()
      startTimeValue.value = res.data.startTime ? new Date(res.data.startTime) : new Date()
      endTimeValue.value = res.data.endTime ? new Date(res.data.endTime) : new Date()
      popupVisible.value = true
    }
  } catch (error) {
    console.error(error)
    showToast({ type: 'fail', message: '获取数据失败' })
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  submitting.value = true
  try {
    let res
    if (formData.id) {
      res = await workApi.update(formData.id, formData)
    } else {
      res = await workApi.save(formData)
    }
    if (res.code === 200) {
      showToast({ type: 'success', message: formData.id ? '更新成功' : '新增成功' })
      popupVisible.value = false
      refreshData()
    }
  } catch (error) {
    console.error(error)
    showToast({ type: 'fail', message: formData.id ? '更新失败' : '新增失败' })
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (item) => {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确定要删除该工作吗？'
    })
    const res = await workApi.delete(item.id)
    if (res.code === 200) {
      showToast({ type: 'success', message: '删除成功' })
      refreshData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const refreshData = () => {
  pagination.current = 1
  list.value = []
  finished.value = false
  refreshing.value = true
  loadData()
}

const onStatusConfirm = ({ selectedOptions }) => {
  formData.workStatus = selectedOptions[0].value
  workStatusDisplay.value = getStatusText(selectedOptions[0].value)
  showStatusPicker.value = false
}

const onPriorityConfirm = ({ selectedOptions }) => {
  formData.priority = selectedOptions[0].value
  priorityDisplay.value = getPriorityText(selectedOptions[0].value)
  showPriorityPicker.value = false
}

const onWorkTimeConfirm = (value) => {
  formData.workTime = dayjs(value).format('YYYY-MM-DD HH:mm:ss')
  showWorkTimePicker.value = false
}

const onStartTimeConfirm = (value) => {
  formData.startTime = dayjs(value).format('YYYY-MM-DD HH:mm:ss')
  showStartTimePicker.value = false
}

const onEndTimeConfirm = (value) => {
  formData.endTime = dayjs(value).format('YYYY-MM-DD HH:mm:ss')
  showEndTimePicker.value = false
}

const getStatusType = (status) => {
  const types = {
    pending: 'primary',
    in_progress: 'warning',
    completed: 'success',
    cancelled: 'danger'
  }
  return types[status] || 'primary'
}

const getStatusText = (status) => {
  const texts = {
    pending: '待处理',
    in_progress: '进行中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return texts[status] || status
}

const getPriorityType = (priority) => {
  const types = {
    low: 'primary',
    normal: 'default',
    high: 'warning',
    urgent: 'danger'
  }
  return types[priority] || 'default'
}

const getPriorityText = (priority) => {
  const texts = {
    low: '低',
    normal: '普通',
    high: '高',
    urgent: '紧急'
  }
  return texts[priority] || priority
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('MM-DD HH:mm')
}

watch(() => route.path, (newPath) => {
  if (newPath === '/h5/work') {
    searchForm.workName = ''
    searchForm.workStatus = ''
    searchForm.priority = ''
    refreshData()
  }
}, { immediate: false })

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.work-page {
  min-height: calc(100vh - 100px);
  padding-bottom: 20px;
}

.work-card {
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.work-card:active {
  transform: scale(0.98);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.popup-content {
  height: 100%;
  overflow-y: auto;
}


:deep(.van-card) {
  border-radius: 12px;
}

:deep(.van-cell) {
  border-radius: 8px;
}

:deep(.van-button) {
  border-radius: 20px;
}

:deep(.van-tag) {
  border-radius: 12px;
}
</style>
