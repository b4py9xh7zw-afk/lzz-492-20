<template>
  <div class="user-page">
    <el-card class="mb-4">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-lg font-bold">用户管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="mb-4">
        <el-form-item label="账号">
          <el-input v-model="searchForm.username" placeholder="请输入账号" clearable />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="searchForm.nickname" placeholder="请输入昵称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="账号" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" size="small" @click="handleResetPassword(row)">重置密码</el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="500px"
      @close="handleDialogClose"
    >
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog v-model="resetPasswordVisible" title="重置密码" width="400px">
      <el-form :model="resetPasswordForm" :rules="resetPasswordRules" ref="resetPasswordFormRef" label-width="100px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetPasswordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPasswordVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPasswordSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { userApi } from '@/api/user'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const resetPasswordVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const resetPasswordFormRef = ref(null)
const currentUserId = ref(null)

const searchForm = reactive({
  username: '',
  nickname: ''
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

const formRules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const resetPasswordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' }
  ]
}

const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')

const loadData = async () => {
  loading.value = true
  try {
    const res = await userApi.page({
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
    username: '',
    nickname: ''
  })
  handleSearch()
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

const handleDialogClose = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          const res = await userApi.update(currentUserId.value, form)
          if (res.code === 200) {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            loadData()
          }
        } else {
          const res = await userApi.save(form)
          if (res.code === 200) {
            ElMessage.success('新增成功')
            dialogVisible.value = false
            loadData()
          }
        }
      } catch (error) {
        console.error(error)
      }
    }
  })
}

const handleResetPassword = (row) => {
  currentUserId.value = row.id
  resetPasswordForm.newPassword = ''
  resetPasswordVisible.value = true
}

const handleResetPasswordSubmit = async () => {
  if (!resetPasswordFormRef.value) return
  
  await resetPasswordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await userApi.resetPassword(currentUserId.value, resetPasswordForm.newPassword)
        if (res.code === 200) {
          ElMessage.success('重置密码成功')
          resetPasswordVisible.value = false
        }
      } catch (error) {
        console.error(error)
      }
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      type: 'warning'
    })
    const res = await userApi.delete(row.id)
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
.user-page {
  max-width: 1400px;
  margin: 0 auto;
}
</style>
