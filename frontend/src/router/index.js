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
      },
      {
        // 工伤上报材料包（PC 端供企业查看、劳务补保险、管理员总览）
        path: 'injury',
        component: () => import('@/views/pc/Injury.vue')
      },
      {
        // 排班档案（复工/停工结论回写查看）
        path: 'schedule',
        component: () => import('@/views/pc/ScheduleArchive.vue')
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
      },
      {
        // 工伤上报材料包
        path: 'injury',
        component: () => import('@/views/h5/InjuryList.vue')
      },
      {
        path: 'injury/create',
        component: () => import('@/views/h5/InjuryReportForm.vue')
      },
      {
        path: 'injury/:id',
        component: () => import('@/views/h5/InjuryDetail.vue')
      },
      {
        // 排班档案（工伤结论回写对象）
        path: 'schedule',
        component: () => import('@/views/h5/ScheduleArchive.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
