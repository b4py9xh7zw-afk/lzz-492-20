<template>
  <div class="file-page">
    <el-card class="mb-4">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-lg font-bold">文件管理</span>
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :data="uploadData"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :show-file-list="false"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>
              上传文件
            </el-button>
          </el-upload>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="mb-4">
        <el-form-item label="文件名称">
          <el-input v-model="searchForm.fileName" placeholder="请输入文件名称" clearable />
        </el-form-item>
        <el-form-item label="文件类型">
          <el-select v-model="searchForm.fileType" placeholder="请选择文件类型" clearable style="width: 200px">
            <el-option label="图片" value="image" />
            <el-option label="文档" value="document" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="originalName" label="原始文件名" />
        <el-table-column prop="fileType" label="文件类型" width="100" />
        <el-table-column prop="fileSize" label="文件大小" width="120">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="uploadUserName" label="上传人" width="120" />
        <el-table-column prop="createTime" label="上传时间" width="180" />
        <el-table-column label="预览" width="100">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handlePreview(row)">
              预览
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 预览对话框 -->
    <el-dialog v-model="previewVisible" title="文件预览" width="800px">
      <div class="text-center">
        <img v-if="previewFile.fileType === 'image'" :src="previewFile.url" class="max-w-full" />
        <div v-else class="py-8">
          <el-icon class="text-6xl text-gray-400"><Document /></el-icon>
          <p class="mt-4 text-gray-600">{{ previewFile.originalName }}</p>
          <el-button type="primary" class="mt-4" @click="window.open(previewFile.url)">
            在新窗口打开
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Document } from '@element-plus/icons-vue'
import { fileApi } from '@/api/file'

const loading = ref(false)
const tableData = ref([])
const previewVisible = ref(false)
const previewFile = ref({})

const searchForm = reactive({
  fileName: '',
  fileType: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const uploadUrl = `${import.meta.env.VITE_API_BASE_URL || '/api'}/file/upload`
const uploadHeaders = {}
const uploadData = {
  uploadUserName: '系统',
  uploadUserId: 1
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await fileApi.page({
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
    fileName: '',
    fileType: ''
  })
  handleSearch()
}

const beforeUpload = (file) => {
  // 检查文件类型，只允许图片
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/bmp', 'image/webp']
  const fileType = file.type
  if (!allowedTypes.includes(fileType)) {
    ElMessage.error('只能上传图片文件（jpg、jpeg、png、gif、bmp、webp）')
    return false
  }
  
  const isLt100M = file.size / 1024 / 1024 < 100
  if (!isLt100M) {
    ElMessage.error('文件大小不能超过100MB')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success('上传成功')
    loadData()
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('上传失败')
}

const handlePreview = (row) => {
  previewFile.value = {
    ...row,
    url: fileApi.preview(row.id)
  }
  previewVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该文件吗？', '提示', {
      type: 'warning'
    })
    const res = await fileApi.delete(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const formatFileSize = (size) => {
  if (!size) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let index = 0
  let fileSize = size
  while (fileSize >= 1024 && index < units.length - 1) {
    fileSize /= 1024
    index++
  }
  return `${fileSize.toFixed(2)} ${units[index]}`
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
.file-page {
  max-width: 1400px;
  margin: 0 auto;
}
</style>
