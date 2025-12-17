import { defineStore } from 'pinia'

// 本地存储 Key 统一管理，避免魔法字符串
const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'

export const useUserStore = defineStore('user', {
  state: () => ({
    // 初始化时从本地存储恢复，保证 App 重启后仍然保持登录态
    token: uni.getStorageSync(TOKEN_KEY) || '',
    userInfo: uni.getStorageSync(USER_INFO_KEY) || null
  }),

  getters: {
    // 是否已登录
    isLogin: (state) => !!state.token
  },

  actions: {
    /**
     * 设置登录用户信息（登录 / 刷新用户信息时调用）
     */
    setUser(token, userInfo) {
      this.token = token || ''
      this.userInfo = userInfo || null

      // 简单持久化到本地存储
      if (this.token) {
        uni.setStorageSync(TOKEN_KEY, this.token)
      } else {
        uni.removeStorageSync(TOKEN_KEY)
      }

      if (this.userInfo) {
        uni.setStorageSync(USER_INFO_KEY, this.userInfo)
      } else {
        uni.removeStorageSync(USER_INFO_KEY)
      }
    },

    /**
     * 从本地存储主动恢复一次（可用于 App 启动时兜底调用）
     */
    loadFromStorage() {
      const storedToken = uni.getStorageSync(TOKEN_KEY)
      const storedUser = uni.getStorageSync(USER_INFO_KEY)
      this.token = storedToken || ''
      this.userInfo = storedUser || null
    },

    /**
     * 清除本地用户信息（退出登录时调用）
     */
    clearUser() {
      this.token = ''
      this.userInfo = null
      uni.removeStorageSync(TOKEN_KEY)
      uni.removeStorageSync(USER_INFO_KEY)
    }
  }
})


