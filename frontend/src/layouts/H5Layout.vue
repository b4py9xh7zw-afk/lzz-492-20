<template>
  <div class="h5-layout min-h-screen bg-gray-50 pb-16">
    <van-nav-bar
      :title="navTitle"
      fixed
      placeholder
      class="bg-white shadow-sm"
    >
      <template #right>
        <van-button size="small" type="primary" @click="switchToPC">
          切换到PC端
        </van-button>
      </template>
    </van-nav-bar>
    <div class="px-4 py-2">
      <router-view :key="route.path" />
    </div>
    <van-tabbar v-model="activeTab" route fixed placeholder>
      <van-tabbar-item icon="photo-o" to="/h5/file">文件</van-tabbar-item>
      <van-tabbar-item icon="warning-o" to="/h5/injury">工伤</van-tabbar-item>
      <van-tabbar-item icon="calendar-o" to="/h5/schedule">排班</van-tabbar-item>
      <van-tabbar-item icon="orders-o" to="/h5/work">工作</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/h5/user">用户</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const activeTab = ref(0)

const navTitle = computed(() => {
  const p = route.path
  if (p.startsWith('/h5/injury')) {
    if (p.includes('/create')) return '现场工伤上报'
    if (/\d+$/.test(p)) return '工伤上报详情'
    return '工伤上报'
  }
  if (p.startsWith('/h5/schedule')) return '排班档案'
  const titles = {
    '/h5/file': '文件管理',
    '/h5/work': '工作管理',
    '/h5/user': '用户管理'
  }
  return titles[p] || '蓝领招聘排班平台'
})

const switchToPC = () => {
  // 获取当前路径，将/h5替换为/pc
  const currentPath = route.path
  const pcPath = currentPath.replace('/h5', '/pc')
  router.push(pcPath)
}
</script>

<style scoped>
.h5-layout {
  padding-bottom: 50px;
}
</style>
