<template>
  <div class="file-page">
    <van-search
      v-model="searchForm.fileName"
      placeholder="请输入文件名称搜索"
      @search="handleSearch"
      @clear="handleSearch"
    />
    
    <van-uploader
      v-model="fileList"
      :after-read="afterRead"
      :max-count="1"
      class="mb-4"
    >
      <van-button icon="plus" type="primary" block>上传文件</van-button>
    </van-uploader>
    
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="loadData"
      >
        <van-cell
          v-for="item in list"
          :key="item.id"
          :title="item.originalName"
          :label="`${item.fileType} | ${formatFileSize(item.fileSize)} | ${item.uploadUserName}`"
          is-link
          @click="handlePreview(item)"
        >
          <template #right-icon>
            <van-button
              size="mini"
              type="danger"
              @click.stop="handleDelete(item)"
            >
              删除
            </van-button>
          </template>
        </van-cell>
      </van-list>
    </van-pull-refresh>

    <!-- 预览弹窗 -->
    <van-image-preview
      v-model:show="showPreview"
      :images="previewImages"
      :start-position="0"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { fileApi } from '@/api/file'

const route = useRoute()

const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showPreview = ref(false)
const previewImages = ref([])
const fileList = ref([])
const list = ref([])

const searchForm = reactive({
  fileName: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const loadData = async () => {
  // 如果是刷新或列表为空，重置分页参数
  if (refreshing.value || list.value.length === 0) {
    pagination.current = 1
    if (refreshing.value) {
      list.value = []
      finished.value = false
    }
  }
  
  loading.value = true
  try {
    const res = await fileApi.page({
      current: pagination.current,
      size: pagination.size,
      fileName: searchForm.fileName
    })
    if (res.code === 200) {
      if (refreshing.value || pagination.current === 1) {
        // 刷新或第一页，直接替换数据
        list.value = res.data.records
        refreshing.value = false
      } else {
        // 滚动加载，追加数据
        list.value.push(...res.data.records)
      }
      pagination.total = res.data.total
      
      // 判断是否还有更多数据
      if (list.value.length >= res.data.total) {
        finished.value = true
      } else {
        // 只有还有更多数据时才递增页码
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

// 刷新数据（重置状态并重新加载第一页）
const refreshData = () => {
  pagination.current = 1
  list.value = []
  finished.value = false
  refreshing.value = true
  loadData()
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

const afterRead = async (file) => {
  // 检查文件类型，只允许图片
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/bmp', 'image/webp']
  const fileType = file.file.type
  if (!allowedTypes.includes(fileType)) {
    showToast({ type: 'fail', message: '只能上传图片文件（jpg、jpeg、png、gif、bmp、webp）' })
    fileList.value = []
    return
  }
  
  try {
    const res = await fileApi.upload(file.file)
    if (res.code === 200) {
      showToast({ type: 'success', message: '上传成功' })
      fileList.value = []
      refreshData()
    }
  } catch (error) {
    showToast({ type: 'fail', message: '上传失败' })
    fileList.value = []
  }
}

const handlePreview = (row) => {
  if (row.fileType === 'image') {
    previewImages.value = [fileApi.preview(row.id)]
    showPreview.value = true
  } else {
    window.open(fileApi.preview(row.id))
  }
}

const handleDelete = async (row) => {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确定要删除该文件吗？'
    })
    const res = await fileApi.delete(row.id)
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

// 监听路由变化，确保切换页面时刷新数据
watch(() => route.path, (newPath) => {
  if (newPath === '/h5/file') {
    searchForm.fileName = ''
    fileList.value = []
    refreshData()
  }
}, { immediate: false })

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.file-page {
  min-height: calc(100vh - 100px);
}
</style>
