<template>
  <div class="login-page min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
    <div class="login-card w-full max-w-sm bg-white rounded-2xl shadow-2xl p-6">
      <h2 class="text-2xl font-bold text-center text-gray-800 mb-6">用户登录</h2>
      
      <van-form @submit="handleLogin">
        <van-cell-group inset>
          <van-field
            v-model="loginForm.username"
            name="username"
            label="账号"
            placeholder="请输入账号"
            :rules="[{ required: true, message: '请输入账号' }]"
            clearable
          />
          <van-field
            v-model="loginForm.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请输入密码' }]"
            clearable
          />
        </van-cell-group>
        
        <div class="mb-4 p-3 bg-blue-50 rounded-lg mt-4">
          <p class="text-xs text-gray-600 mb-2">测试账号（密码均为 123456）：</p>
          <p class="text-xs text-gray-700">主管：<span class="font-semibold">supervisor1</span>（手机端上报/结论）</p>
          <p class="text-xs text-gray-700">劳务：<span class="font-semibold">labor1</span>（补充保险资料）</p>
          <p class="text-xs text-gray-700">企业：<span class="font-semibold">enterprise1</span>（仅查看本项目）</p>
          <p class="text-xs text-gray-700">管理员：<span class="font-semibold">admin</span></p>
        </div>
        
        <div class="mt-6">
          <van-button 
            round 
            block 
            type="primary" 
            native-type="submit"
            :loading="loading"
            loading-text="登录中..."
          >
            登录
          </van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { userApi } from '@/api/user'

const router = useRouter()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  loading.value = true
  try {
    const res = await userApi.login(loginForm)
    if (res.code === 200) {
      showToast({ type: 'success', message: '登录成功' })
      // 保存用户信息到localStorage
      localStorage.setItem('user', JSON.stringify(res.data))
      // 跳转到文件管理页面
      router.push('/h5/injury')
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
}

.login-card {
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
