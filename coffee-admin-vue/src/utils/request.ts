import axios from 'axios'
import { ElMessage } from 'element-plus'
import { log } from 'node:console'

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
        console.log("config.headers['Authorization']",config.headers['Authorization'])
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
    console.log('err' + error)
    ElMessage({
      message: error.message,
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

export default service

