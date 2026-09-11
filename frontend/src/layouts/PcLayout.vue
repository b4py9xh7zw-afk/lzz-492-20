<template>
  <div class="pc-layout min-h-screen bg-gray-50">
    <el-container>
      <el-header class="bg-white shadow-sm">
        <div class="flex items-center justify-between h-full px-6">
          <h1 class="text-xl font-bold text-gray-800">工作管理系统</h1>
          <div class="flex items-center gap-4">
            <el-button 
              type="primary" 
              size="small" 
              @click="switchToH5"
              class="rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200"
            >
              <el-icon><RefreshRight /></el-icon>
              切换到H5端
            </el-button>
            <div class="text-sm text-gray-600">PC端</div>
          </div>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px" class="bg-white shadow-sm">
          <el-menu
            :default-active="activeMenu"
            router
            class="border-r-0"
          >
            <el-menu-item index="/pc/file">
              <el-icon><Document /></el-icon>
              <span>文件管理</span>
            </el-menu-item>
            <el-menu-item index="/pc/work">
              <el-icon><Briefcase /></el-icon>
              <span>工作管理</span>
            </el-menu-item>
            <el-menu-item index="/pc/user">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main class="p-6">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Document, Briefcase, RefreshRight, User } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const activeMenu = computed(() => route.path)

const switchToH5 = () => {
  // 获取当前路径，将/pc替换为/h5
  const currentPath = route.path
  const h5Path = currentPath.replace('/pc', '/h5')
  router.push(h5Path)
}
</script>

<style scoped>
.pc-layout :deep(.el-header) {
  padding: 0;
  height: 60px;
  line-height: 60px;
}

.pc-layout :deep(.el-aside) {
  height: calc(100vh - 60px);
}

.pc-layout :deep(.el-main) {
  height: calc(100vh - 60px);
  overflow-y: auto;
}
</style>
