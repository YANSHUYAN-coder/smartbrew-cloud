/**
 * 网络请求工具
 * 用于统一处理 API 请求
 */

import { BASE_URL } from './config.js'

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
  
  // 尝试从响应中提取错误信息
  const getErrorMessage = (responseData) => {
    // 优先使用 Result 格式的 message
    if (responseData && typeof responseData === 'object') {
      if (responseData.message) return responseData.message
      if (responseData.msg) return responseData.msg
      if (responseData.error) return responseData.error
    }
    return '请求失败'
  }
  
  if (statusCode === 200) {
    // 根据后端返回的数据结构处理
    if (data && (data.code === 200 || data.success)) {
      // 如果 data 对象有 data 属性（即使值为 null），返回 data.data；否则返回整个 data 对象
      return ('data' in data) ? data.data : data
    } else {
      // 业务异常：显示后端返回的错误信息
      const errorMessage = getErrorMessage(data)
      uni.showToast({
        title: errorMessage,
        icon: 'none',
        duration: 3000
      })
      return Promise.reject(data || { message: errorMessage })
    }
  } else if (statusCode === 401) {
    // token 过期，跳转登录
    const errorMessage = getErrorMessage(data) || '登录已过期，请重新登录'
    uni.removeStorageSync('token')
    uni.showToast({
      title: errorMessage,
      icon: 'none'
    })
    uni.reLaunch({
      url: '/pages/login/index'
    })
    return Promise.reject(response)
  } else {
    // 其他HTTP错误：尝试从响应体中提取错误信息
    const errorMessage = getErrorMessage(data) || `网络错误 (${statusCode})`
    uni.showToast({
      title: errorMessage,
      icon: 'none',
      duration: 3000
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

