/**
 * 网络请求工具
 * 用于统一处理 API 请求 + Token 刷新
 */

import { BASE_URL } from './config.js'

// 刷新相关状态
let isRefreshing = false
let refreshPromise = null
const pendingQueue = [] // [{ resolve, reject, config }]

// 统一获取错误文案
const getErrorMessage = (responseData) => {
  if (responseData && typeof responseData === 'object') {
    if (responseData.message) return responseData.message
    if (responseData.msg) return responseData.msg
    if (responseData.error) return responseData.error
  }
  return '请求失败'
}

// 触发退出登录（401 且刷新失败时调用）
const forceLogout = (message) => {
  uni.removeStorageSync('token')
  uni.removeStorageSync('refreshToken')

  uni.showToast({
    title: message || '登录已过期，请重新登录',
    icon: 'none',
    duration: 3000
  })

  // 全局发出登出事件，方便其它地方（如 WebSocket）同步处理
  uni.$emit && uni.$emit('userLogout')

  uni.reLaunch({
    url: '/pages/login/index'
  })
}

// 请求拦截器：附带最新 token
const requestInterceptor = (config) => {
  const token = uni.getStorageSync('token')
  if (token) {
    config.header = {
      ...config.header,
      'Authorization': `Bearer ${token}`
    }
  }
  return config
}

// 调用后端刷新 token 接口
// 默认使用：POST /auth/refresh，携带 { refreshToken }
// 如果你的实际接口不同，只需要改这里即可
const refreshAccessToken = () => {
  if (isRefreshing && refreshPromise) {
    return refreshPromise
  }

  const refreshToken = uni.getStorageSync('refreshToken')
  if (!refreshToken) {
    return Promise.reject(new Error('暂无刷新令牌'))
  }

  isRefreshing = true
  refreshPromise = new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + '/auth/refresh',
      method: 'POST',
      data: { refreshToken },
      header: {
        'Content-Type': 'application/json'
      },
      success: (res) => {
        const { statusCode, data } = res

        if (statusCode !== 200) {
          reject(new Error(getErrorMessage(data) || '刷新令牌失败'))
          return
        }

        // 兼容 Result 格式：{ code, data: { token, refreshToken, user } }
        if (!data || !(data.code === 200 || data.success)) {
          reject(new Error(getErrorMessage(data) || '刷新令牌失败'))
          return
        }

        const payload = 'data' in data ? data.data : data
        const newToken = payload.token
        const newRefreshToken = payload.refreshToken || refreshToken

        if (!newToken) {
          reject(new Error('刷新接口未返回新 token'))
          return
        }

        // 更新本地存储
        uni.setStorageSync('token', newToken)
        if (newRefreshToken) {
          uni.setStorageSync('refreshToken', newRefreshToken)
        }

        resolve(newToken)
      },
      fail: (error) => {
        reject(error || new Error('刷新令牌网络失败'))
      },
      complete: () => {
        isRefreshing = false
        refreshPromise = null
      }
    })
  })

  return refreshPromise
}

// 响应拦截器（支持 401 自动刷新并重试）
const responseInterceptor = async (response, originalConfig) => {
  const { statusCode, data } = response

  if (statusCode === 200) {
    if (data && (data.code === 200 || data.success)) {
      return ('data' in data) ? data.data : data
    } else {
      const errorMessage = getErrorMessage(data)
      uni.showToast({
        title: errorMessage,
        icon: 'none',
        duration: 3000
      })
      return Promise.reject(data || { message: errorMessage })
    }
  }

  if (statusCode === 401) {
    // 避免死循环：已重试过的请求直接退出登录
    if (originalConfig && originalConfig._retry) {
      forceLogout(getErrorMessage(data) || '登录已过期，请重新登录')
      return Promise.reject(response)
    }

    // 将当前请求加入队列，等待刷新完成后重试
    return new Promise((resolve, reject) => {
      pendingQueue.push({ resolve, reject, originalConfig })

      // 只有第一个 401 会真正触发刷新，其它的只排队
      if (!isRefreshing) {
        refreshAccessToken()
          .then(() => {
            // 刷新成功：依次重试队列中的请求
            const queue = [...pendingQueue]
            pendingQueue.length = 0

            queue.forEach(({ resolve, reject, originalConfig }) => {
              const newConfig = requestInterceptor({
                ...originalConfig,
                _retry: true // 标记为重试请求
              })

              uni.request({
                ...newConfig,
                success: async (res2) => {
                  try {
                    const data2 = await responseInterceptor(res2, newConfig)
                    resolve(data2)
                  } catch (err) {
                    reject(err)
                  }
                },
                fail: (err) => {
                  uni.showToast({
                    title: '网络请求失败',
                    icon: 'none'
                  })
                  reject(err)
                }
              })
            })
          })
          .catch((err) => {
            // 刷新失败：清空队列并强制退出登录
            const queue = [...pendingQueue]
            pendingQueue.length = 0

            forceLogout(getErrorMessage(data) || '登录已过期，请重新登录')

            queue.forEach(({ reject }) => reject(err))
          })
      }
    })
  }

  // 其他 HTTP 错误
  const errorMessage = getErrorMessage(data) || `网络错误 (${statusCode})`
  uni.showToast({
    title: errorMessage,
    icon: 'none',
    duration: 3000
  })
  return Promise.reject(response)
}

/**
 * 通用请求方法
 */
export const request = (options) => {
  return new Promise((resolve, reject) => {
    const baseConfig = {
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...options.header
      },
      _retry: false // 自定义字段，用于避免无限刷新
    }

    // 请求拦截
    const config = requestInterceptor(baseConfig)

    uni.request({
      ...config,
      success: async (res) => {
        try {
          const data = await responseInterceptor(res, config)
          resolve(data)
        } catch (error) {
          reject(error)
        }
      },
      fail: (error) => {
        uni.showToast({
          title: '网络请求失败',
          icon: 'none'
        })
        reject(error)
      }
    })
  })
}

/**
 * GET 请求
 */
export const get = (url, params = {}) => {
  return request({
    url,
    method: 'GET',
    data: params
  })
}

/**
 * POST 请求
 */
export const post = (url, data = {}) => {
  return request({
    url,
    method: 'POST',
    data
  })
}

/**
 * PUT 请求
 */
export const put = (url, data = {}) => {
  return request({
    url,
    method: 'PUT',
    data
  })
}

/**
 * DELETE 请求
 */
export const del = (url, data = {}) => {
  return request({
    url,
    method: 'DELETE',
    data
  })
}

