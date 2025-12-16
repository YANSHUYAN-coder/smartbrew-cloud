/**
 * 网络请求工具
 * 用于统一处理 API 请求
 */

const BASE_URL = '/api' // 根据实际情况修改

// 请求拦截器
const requestInterceptor = (config) => {
  // 添加 token
  const token = uni.getStorageSync('token')
  if (token) {
    config.header = {
      ...config.header,
      'Authorization': `Bearer ${token}`
    }
  }
  return config
}

// 响应拦截器
const responseInterceptor = (response) => {
  const { statusCode, data } = response
  if (statusCode === 200) {
    // 根据后端返回的数据结构处理
    if (data.code === 200 || data.success) {
      return data.data || data
    } else {
      uni.showToast({
        title: data.message || '请求失败',
        icon: 'none'
      })
      return Promise.reject(data)
    }
  } else if (statusCode === 401) {
    // token 过期，跳转登录
    uni.removeStorageSync('token')
    uni.reLaunch({
      url: '/pages/login/index'
    })
    return Promise.reject(response)
  } else {
    uni.showToast({
      title: '网络错误',
      icon: 'none'
    })
    return Promise.reject(response)
  }
}

/**
 * 通用请求方法
 */
export const request = (options) => {
  return new Promise((resolve, reject) => {
    // 请求拦截
    const config = requestInterceptor({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...options.header
      }
    })

    uni.request({
      ...config,
      success: (res) => {
        try {
          const data = responseInterceptor(res)
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

