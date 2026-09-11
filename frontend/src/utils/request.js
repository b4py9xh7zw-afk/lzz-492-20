import axios from 'axios'
import { ElMessage } from 'element-plus'
import { showToast } from 'vant'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 可以在这里添加token等
    // const token = localStorage.getItem('token')
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`
    // }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      // 判断是PC还是H5
      const isMobile = window.innerWidth <= 767
      if (isMobile) {
        showToast({
          message: res.message || '请求失败',
          type: 'fail'
        })
      } else {
        ElMessage.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    const isMobile = window.innerWidth <= 767
    const message = error.message || '网络错误'
    if (isMobile) {
      showToast({
        message: message,
        type: 'fail'
      })
    } else {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

export default request
