<template>
  <div class="user-page">
    <van-nav-bar title="用户管理" fixed>
      <template #right>
        <van-icon name="plus" size="20" @click="handleAdd" />
      </template>
    </van-nav-bar>
    
    <div class="content">
      <van-search
        v-model="searchForm.username"
        placeholder="请输入账号搜索"
        @search="handleSearch"
        @clear="handleSearch"
        class="mb-2"
      />
      
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
            :title="item.username"
            :label="item.nickname || '无昵称'"
            is-link
            @click="handleEdit(item)"
          >
            <template #right-icon>
              <div class="flex gap-2">
                <van-button
                  size="mini"
                  type="warning"
                  @click.stop="handleResetPassword(item)"
                >
                  重置密码
                </van-button>
                <van-button
                  size="mini"
                  type="danger"
                  @click.stop="handleDelete(item)"
                >
                  删除
                </van-button>
              </div>
            </template>
          </van-cell>
        </van-list>
      </van-pull-refresh>
    </div>

    <!-- 新增/编辑弹窗 -->
    <van-popup
      v-model:show="dialogVisible"
      position="bottom"
      :style="{ height: '60%' }"
      round
    >
      <div class="p-4">
        <h3 class="text-lg font-bold mb-4">{{ dialogTitle }}</h3>
        <van-form @submit="handleSubmit">
          <van-cell-group inset>
            <van-field
              v-model="form.username"
              name="username"
              label="账号"
              placeholder="请输入账号"
              :rules="[{ required: true, message: '请输入账号' }]"
              :disabled="isEdit"
              clearable
            />
            <van-field
              v-if="!isEdit"
              v-model="form.password"
              type="password"
              name="password"
              label="密码"
              placeholder="请输入密码"
              :rules="[{ required: true, message: '请输入密码' }]"
              clearable
            />
            <van-field
              v-model="form.nickname"
              name="nickname"
              label="昵称"
              placeholder="请输入昵称"
              clearable
            />
          </van-cell-group>
          <div class="mt-6 flex gap-2">
            <van-button round block @click="dialogVisible = false">取消</van-button>
            <van-button round block type="primary" native-type="submit">确定</van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

    <!-- 重置密码弹窗 -->
    <van-popup
      v-model:show="resetPasswordVisible"
      position="bottom"
      :style="{ height: '40%' }"
      round
    >
      <div class="p-4">
        <h3 class="text-lg font-bold mb-4">重置密码</h3>
        <van-form @submit="handleResetPasswordSubmit">
          <van-cell-group inset>
            <van-field
              v-model="resetPasswordForm.newPassword"
              type="password"
              name="newPassword"
              label="新密码"
              placeholder="请输入新密码"
              :rules="[{ required: true, message: '请输入新密码' }]"
              clearable
            />
          </van-cell-group>
          <div class="mt-6 flex gap-2">
            <van-button round block @click="resetPasswordVisible = false">取消</van-button>
            <van-button round block type="primary" native-type="submit">确定</van-button>
          </div>
        </van-form>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import { userApi } from '@/api/user'

const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const dialogVisible = ref(false)
const resetPasswordVisible = ref(false)
const isEdit = ref(false)
const currentUserId = ref(null)
const list = ref([])

const searchForm = reactive({
  username: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const form = reactive({
  username: '',
  password: '',
  nickname: ''
})

const resetPasswordForm = reactive({
  newPassword: ''
})

const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')

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
    const res = await userApi.page({
      current: pagination.current,
      size: pagination.size,
      username: searchForm.username
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
  isEdit.value = false
  currentUserId.value = null
  Object.assign(form, {
    username: '',
    password: '',
    nickname: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentUserId.value = row.id
  Object.assign(form, {
    username: row.username,
    password: '',
    nickname: row.nickname || ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (isEdit.value) {
      const res = await userApi.update(currentUserId.value, form)
      if (res.code === 200) {
        showToast({ type: 'success', message: '更新成功' })
        dialogVisible.value = false
        handleSearch()
      }
    } else {
      const res = await userApi.save(form)
      if (res.code === 200) {
        showToast({ type: 'success', message: '新增成功' })
        dialogVisible.value = false
        handleSearch()
      }
    }
  } catch (error) {
    console.error(error)
  }
}

const handleResetPassword = (row) => {
  currentUserId.value = row.id
  resetPasswordForm.newPassword = ''
  resetPasswordVisible.value = true
}

const handleResetPasswordSubmit = async () => {
  try {
    const res = await userApi.resetPassword(currentUserId.value, resetPasswordForm.newPassword)
    if (res.code === 200) {
      showToast({ type: 'success', message: '重置密码成功' })
      resetPasswordVisible.value = false
    }
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = async (row) => {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确定要删除该用户吗？'
    })
    const res = await userApi.delete(row.id)
    if (res.code === 200) {
      showToast({ type: 'success', message: '删除成功' })
      handleSearch()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.user-page {
  min-height: 100vh;
  background-color: #f7f8fa;
}

.content {
  padding-top: 46px;
  min-height: calc(100vh - 46px);
}
</style>
