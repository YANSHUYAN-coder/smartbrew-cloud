/**
 * 用户相关 API
 */
import { get, post } from '@/utils/request.js'
import { BASE_URL } from '@/utils/config.js'

/**
 * 更新用户信息
 * @param {Object} data 用户信息对象
 * @returns {Promise}
 */
export const updateUserInfo = (data) => {
  return post('/app/member/update', data)
}

/**
 * 获取用户信息
 * @returns {Promise}
 */
export const getUserInfo = () => {
  return get('/app/member/info')
}

/**
 * 获取个人中心统计信息
 * @returns {Promise} 返回积分、优惠券、订单统计等信息
 */
export const getProfileStatistics = () => {
  return get('/app/member/statistics')
}

/**
 * 退出登录
 * @returns {Promise}
 */
export const logout = () => {
  return post('/auth/logout')
}

/**
 * 发送手机验证码
 * @param {String} mobile 手机号
 * @returns {Promise}
 */
export const sendCode = (mobile) => {
  return post(`/auth/sendCode?mobile=${mobile}`)
}

/**
 * 手机号验证码登录
 * @param {Object} data { mobile, code }
 * @returns {Promise}
 */
export const loginByMobile = (data) => {
  return post('/auth/loginByMobile', data)
}

/**
 * 上传头像
 * @param {String} filePath 图片文件路径（临时路径）
 * @returns {Promise} 返回上传结果
 */
export const uploadAvatar = (filePath) => {
  return new Promise((resolve, reject) => {
    // 获取 token
    const token = uni.getStorageSync('token')
    
    uni.uploadFile({
      url: BASE_URL + '/app/member/upload/avatar',
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        try {
          const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
          // 根据后端返回的数据结构处理
          if (data.code === 200 || data.success) {
            resolve(data.data || data)
          } else {
            uni.showToast({
              title: data.message || '上传失败',
              icon: 'none'
            })
            reject(new Error(data.message || '上传失败'))
          }
        } catch (error) {
          console.error('解析上传响应失败', error)
          reject(error)
        }
      },
      fail: (error) => {
        uni.showToast({
          title: '上传失败',
          icon: 'none'
        })
        reject(error)
      }
    })
  })
}

