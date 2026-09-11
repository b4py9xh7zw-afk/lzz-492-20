<template>
  <div class="work-page">
    <el-card class="mb-4 shadow-sm rounded-lg border-0" :body-style="{ padding: '24px' }">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-xl font-bold text-gray-800">工作管理</span>
          <el-button 
            type="primary" 
            @click="handleAdd"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
          >
            <el-icon><Plus /></el-icon>
            新增工作
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="mb-6">
        <el-form-item label="工作名称">
          <el-input 
            v-model="searchForm.workName" 
            placeholder="请输入工作名称" 
            clearable
            class="rounded-lg"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="工作状态">
          <el-select 
            v-model="searchForm.workStatus" 
            placeholder="请选择工作状态" 
            clearable 
            style="width: 180px"
            class="rounded-lg"
          >
            <el-option label="待处理" value="pending" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select 
            v-model="searchForm.priority" 
            placeholder="请选择优先级" 
            clearable 
            style="width: 180px"
            class="rounded-lg"
          >
            <el-option label="低" value="low" />
            <el-option label="普通" value="normal" />
            <el-option label="高" value="high" />
            <el-option label="紧急" value="urgent" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleSearch"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
          >
            查询
          </el-button>
          <el-button 
            @click="handleReset"
            class="rounded-lg"
          >
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <el-table 
        :data="tableData" 
        v-loading="loading" 
        border
        class="rounded-lg overflow-hidden"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="workName" label="工作名称" min-width="150" />
        <el-table-column prop="workContent" label="工作内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="workStatus" label="工作状态" width="120">
          <template #default="{ row }">
            <el-tag 
              :type="getStatusType(row.workStatus)"
              class="rounded-full px-3 py-1"
            >
              {{ getStatusText(row.workStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="120">
          <template #default="{ row }">
            <el-tag 
              :type="getPriorityType(row.priority)"
              class="rounded-full px-3 py-1"
            >
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="workTime" label="工作时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.workTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              link 
              @click="handleEdit(row)"
              class="hover:text-blue-600 transition-colors duration-200"
            >
              编辑
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              link 
              @click="handleDelete(row)"
              class="hover:text-red-600 transition-colors duration-200"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-6 flex justify-end">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          class="rounded-lg"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="600px"
      class="rounded-lg"
      :close-on-click-modal="false"
    >
      <el-form 
        :model="formData" 
        :rules="formRules" 
        ref="formRef"
        label-width="100px"
        class="px-2"
      >
        <el-form-item label="工作名称" prop="workName">
          <el-input 
            v-model="formData.workName" 
            placeholder="请输入工作名称"
            class="rounded-lg"
          />
        </el-form-item>
        <el-form-item label="工作内容" prop="workContent">
          <el-input 
            v-model="formData.workContent" 
            type="textarea" 
            :rows="4"
            placeholder="请输入工作内容"
            class="rounded-lg"
          />
        </el-form-item>
        <el-form-item label="工作状态" prop="workStatus">
          <el-select 
            v-model="formData.workStatus" 
            placeholder="请选择工作状态"
            class="w-full rounded-lg"
          >
            <el-option label="待处理" value="pending" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select 
            v-model="formData.priority" 
            placeholder="请选择优先级"
            class="w-full rounded-lg"
          >
            <el-option label="低" value="low" />
            <el-option label="普通" value="normal" />
            <el-option label="高" value="high" />
            <el-option label="紧急" value="urgent" />
          </el-select>
        </el-form-item>
        <el-form-item label="工作时间">
          <el-date-picker
            v-model="formData.workTime"
            type="datetime"
            placeholder="请选择工作时间"
            class="w-full rounded-lg"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="formData.startTime"
            type="datetime"
            placeholder="请选择开始时间"
            class="w-full rounded-lg"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="formData.endTime"
            type="datetime"
            placeholder="请选择结束时间"
            class="w-full rounded-lg"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input 
            v-model="formData.remark" 
            type="textarea" 
            :rows="3"
            placeholder="请输入备注"
            class="rounded-lg"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button 
            @click="dialogVisible = false"
            class="rounded-lg"
          >
            取消
          </el-button>
          <el-button 
            type="primary" 
            @click="handleSubmit"
            class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
            :loading="submitting"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { workApi } from '@/api/work'
import dayjs from 'dayjs'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增工作')
const formRef = ref(null)

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

const formRules = {
  workName: [
    { required: true, message: '请输入工作名称', trigger: 'blur' }
  ],
  workContent: [
    { required: true, message: '请输入工作内容', trigger: 'blur' }
  ],
  workStatus: [
    { required: true, message: '请选择工作状态', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await workApi.page({
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    workName: '',
    workStatus: '',
    priority: ''
  })
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增工作'
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
  dialogVisible.value = true
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑工作'
  try {
    const res = await workApi.getById(row.id)
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
      dialogVisible.value = true
      if (formRef.value) {
        formRef.value.clearValidate()
      }
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取数据失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        let res
        if (formData.id) {
          res = await workApi.update(formData.id, formData)
        } else {
          res = await workApi.save(formData)
        }
        if (res.code === 200) {
          ElMessage.success(formData.id ? '更新成功' : '新增成功')
          dialogVisible.value = false
          loadData()
        }
      } catch (error) {
        console.error(error)
        ElMessage.error(formData.id ? '更新失败' : '新增失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该工作吗？', '提示', {
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    const res = await workApi.delete(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('删除失败')
    }
  }
}

const getStatusType = (status) => {
  const types = {
    pending: 'info',
    in_progress: 'warning',
    completed: 'success',
    cancelled: 'danger'
  }
  return types[status] || 'info'
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
    low: 'info',
    normal: '',
    high: 'warning',
    urgent: 'danger'
  }
  return types[priority] || ''
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
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadData()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.work-page {
  max-width: 1400px;
  margin: 0 auto;
}

:deep(.el-card) {
  border-radius: 12px;
}

:deep(.el-table) {
  border-radius: 8px;
}

:deep(.el-button) {
  border-radius: 6px;
}

:deep(.el-input__wrapper) {
  border-radius: 6px;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 6px;
}

:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-dialog__body) {
  padding: 24px;
}
</style>
