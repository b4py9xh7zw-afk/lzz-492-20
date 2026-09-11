import { createRouter, createWebHistory } from 'vue-router'
import { isMobile } from '@/utils/device'

const routes = [
  {
    path: '/',
    redirect: () => {
      return isMobile() ? '/h5/login' : '/pc/login'
    }
  },
  {
    path: '/pc/login',
    component: () => import('@/views/pc/Login.vue')
  },
  {
    path: '/h5/login',
    component: () => import('@/views/h5/Login.vue')
  },
  {
    path: '/pc',
    component: () => import('@/layouts/PcLayout.vue'),
    children: [
      {
        path: '',
        redirect: '/pc/file'
      },
      {
        path: 'file',
        component: () => import('@/views/pc/File.vue')
      },
      {
        path: 'work',
        component: () => import('@/views/pc/Work.vue')
      },
      {
        path: 'user',
        component: () => import('@/views/pc/User.vue')
      }
    ]
  },
  {
    path: '/h5',
    component: () => import('@/layouts/H5Layout.vue'),
    children: [
      {
        path: '',
        redirect: '/h5/file'
      },
      {
        path: 'file',
        component: () => import('@/views/h5/File.vue')
      },
      {
        path: 'work',
        component: () => import('@/views/h5/Work.vue')
      },
      {
        path: 'user',
        component: () => import('@/views/h5/User.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
