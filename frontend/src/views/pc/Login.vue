<template>
  <div class="login-page min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100">
    <el-card class="login-card w-full max-w-md shadow-2xl">
      <template #header>
        <div class="text-center">
          <h2 class="text-2xl font-bold text-gray-800">用户登录</h2>
        </div>
      </template>
      
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="80px">
        <el-form-item label="账号" prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入账号"
            clearable
            size="large"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码"
            show-password
            clearable
            size="large"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <div class="mb-4 p-3 bg-blue-50 rounded-lg">
          <p class="text-sm text-gray-600 mb-2">测试账号：</p>
          <p class="text-sm text-gray-700">账号：<span class="font-semibold">admin</span></p>
          <p class="text-sm text-gray-700">密码：<span class="font-semibold">123456</span></p>
        </div>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            class="w-full"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api/user'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await userApi.login(loginForm)
        if (res.code === 200) {
          ElMessage.success('登录成功')
          // 保存用户信息到localStorage
          localStorage.setItem('user', JSON.stringify(res.data))
          // 跳转到文件管理页面
          router.push('/pc/file')
        }
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-page {
  padding: 20px;
}

.login-card {
  border-radius: 12px;
}
</style>
