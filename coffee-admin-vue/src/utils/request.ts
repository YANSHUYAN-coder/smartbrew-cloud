import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 刷新相关状态
let isRefreshing = false
let pendingQueue: any[] = []

// 触发退出登录
const forceLogout = (message?: string) => {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('user')
  
  ElMessage({
    message: message || '登录已过期，请重新登录',
    type: 'warning',
    duration: 3 * 1000
  })
  
  router.push('/login')
}

// 创建 axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/api',
  timeout: 10000 // 增加一点超时时间
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  async response => {
    const res = response.data
    const originalConfig = response.config

    // 这里根据后端 Result 结构判断
    if (res.code === 200 || res.success) {
      return res.data
    }

    // 处理 401 未授权（Token 过期）
    if (res.code === 401) {
      // 如果已经在刷新中了，将请求加入队列
      if (isRefreshing) {
        return new Promise(resolve => {
          pendingQueue.push((token: string) => {
            originalConfig.headers['Authorization'] = 'Bearer ' + token
            resolve(service(originalConfig))
          })
        })
      }

      const refreshToken = localStorage.getItem('refreshToken')
      if (!refreshToken) {
        forceLogout()
        return Promise.reject(new Error('暂无刷新令牌'))
      }

      isRefreshing = true
      
      try {
        // 调用刷新接口
        // 注意：这里使用 axios 直接调用，避免使用封装好的 service 导致死循环
        const refreshRes = await axios.post((import.meta.env.VITE_APP_BASE_API || '/api') + '/auth/refresh', {
          refreshToken
        })

        const { code, data, success } = refreshRes.data
        if (code === 200 || success) {
          const newToken = data.token
          const newRefreshToken = data.refreshToken
          
          localStorage.setItem('token', newToken)
          if (newRefreshToken) {
            localStorage.setItem('refreshToken', newRefreshToken)
          }

          // 执行队列中的请求
          pendingQueue.forEach(callback => callback(newToken))
          pendingQueue = []

          // 重试当前请求
          originalConfig.headers['Authorization'] = 'Bearer ' + newToken
          return service(originalConfig)
        } else {
          forceLogout(res.message || '登录已过期，请重新登录')
          return Promise.reject(new Error('刷新令牌失败'))
        }
      } catch (err) {
        forceLogout()
        return Promise.reject(err)
      } finally {
        isRefreshing = false
      }
    }

    ElMessage({
      message: res.message || 'Error',
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(new Error(res.message || 'Error'))
  },
  error => {
    const { response } = error
    if (response && response.status === 401) {
      forceLogout()
    } else {
      ElMessage({
        message: error.message || '网络错误，请检查网络连接',
        type: 'error',
        duration: 5 * 1000
      })
    }
    return Promise.reject(error)
  }
)

export default service
