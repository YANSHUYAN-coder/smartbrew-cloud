import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/api', // url = base url + request url
  timeout: 5000 // request timeout
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 在发送请求之前做些什么
    // 例如：config.headers['Authorization'] = 'Bearer ' + getToken()
    const token = localStorage.getItem('token')
    if (token) {
        // 后端 JwtAuthenticationFilter 明确检查了 startsWith(AuthConstants.TOKEN_PREFIX) 即 "Bearer "
        // 且登录接口返回的 token 只是 raw token 字符串，所以前端必须手动拼接 Bearer
        config.headers['Authorization'] = 'Bearer ' + token
      }
    return config
  },
  error => {
    // 对请求错误做些什么
    console.log(error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    // 这里根据后端 Result 结构判断
    // 假设后端返回结构: { code: 200, message: 'success', data: ... }
    if (res.code !== 200) {
      // 如果是 401 未授权错误（token 过期）
      if (res.code === 401) {
        // 清除本地存储
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        // 提示用户
        ElMessage({
          message: res.message || '登录已过期，请重新登录',
          type: 'warning',
          duration: 3 * 1000
        })
        // 跳转到登录页
        router.push('/login')
        return Promise.reject(new Error(res.message || '登录已过期'))
      }
      ElMessage({
        message: res.message || 'Error',
        type: 'error',
        duration: 5 * 1000
      })
      return Promise.reject(new Error(res.message || 'Error'))
    } else {
      return res.data
    }
  },
  error => {
    // 处理 HTTP 状态码错误（如 401 Unauthorized）
    if (error.response) {
      const { status, data } = error.response
      // HTTP 401 未授权（token 过期或无效）
      if (status === 401) {
        // 清除本地存储
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        // 提示用户
        ElMessage({
          message: data?.message || '登录已过期，请重新登录',
          type: 'warning',
          duration: 3 * 1000
        })
        // 跳转到登录页
        router.push('/login')
        return Promise.reject(error)
      }
      // 其他 HTTP 错误
      ElMessage({
        message: data?.message || error.message || '请求失败',
        type: 'error',
        duration: 5 * 1000
      })
    } else {
      // 网络错误等其他错误
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

