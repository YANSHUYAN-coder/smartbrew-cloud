import { defineStore } from 'pinia'
import { getUserInfo, getProfileStatistics, logout as apiLogout } from '@/services/user.js'

// 本地存储 Key 统一管理
const TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refreshToken'
const USER_INFO_KEY = 'userInfo'

export const useUserStore = defineStore('user', {
  state: () => ({
    // 初始化时从本地存储恢复
    token: uni.getStorageSync(TOKEN_KEY) || '',
    refreshToken: uni.getStorageSync(REFRESH_TOKEN_KEY) || '',
    userInfo: uni.getStorageSync(USER_INFO_KEY) || null
  }),

  getters: {
    // 是否已登录
    isLogin: (state) => !!state.token
  },

  actions: {
    /**
     * 设置登录用户信息
     * 支持传入 { token, refreshToken, userInfo } 对象，或者单独传入
     */
    setUser(payload) {
      // 兼容旧的传参方式 setUser(token, userInfo)
      if (arguments.length > 1) {
        this.token = arguments[0] || ''
        this.userInfo = arguments[1] || null
      } else {
        // 新方式传入对象
        const { token, refreshToken, userInfo } = payload || {}
        if (token !== undefined) this.token = token
        if (refreshToken !== undefined) this.refreshToken = refreshToken
        if (userInfo !== undefined) this.userInfo = userInfo
      }

      // 持久化存储
      this.saveToStorage()
    },

    /**
     * 统一保存到本地存储
     */
    saveToStorage() {
      if (this.token) uni.setStorageSync(TOKEN_KEY, this.token)
      else uni.removeStorageSync(TOKEN_KEY)

      if (this.refreshToken) uni.setStorageSync(REFRESH_TOKEN_KEY, this.refreshToken)
      else uni.removeStorageSync(REFRESH_TOKEN_KEY)

      if (this.userInfo) uni.setStorageSync(USER_INFO_KEY, this.userInfo)
      else uni.removeStorageSync(USER_INFO_KEY)
    },

    /**
     * 从本地存储主动恢复一次（App 启动时调用）
     */
    loadFromStorage() {
      this.token = uni.getStorageSync(TOKEN_KEY) || ''
      this.refreshToken = uni.getStorageSync(REFRESH_TOKEN_KEY) || ''
      this.userInfo = uni.getStorageSync(USER_INFO_KEY) || null
      return !!this.token
    },

    /**
     * 退出登录（清除所有状态）
     */
    async logout() {
      try {
        // 调用后端退出接口（可选，尽力而为）
        await apiLogout()
      } catch (e) {
        console.warn('后端退出失败，继续清理本地缓存', e)
      }

      this.token = ''
      this.refreshToken = ''
      this.userInfo = null

      uni.removeStorageSync(TOKEN_KEY)
      uni.removeStorageSync(REFRESH_TOKEN_KEY)
      uni.removeStorageSync(USER_INFO_KEY)

      // 触发全局事件
      uni.$emit('userLogout')

      // 跳转登录页
      uni.reLaunch({ url: '/pages/login/index' })
    },

    /**
     * 刷新用户信息（从服务器拉取最新数据）
     */
    async fetchUserInfo() {
      if (!this.token) return
      try {
        // 并行请求基础信息和统计信息
        const [baseInfo, stats] = await Promise.all([
          getUserInfo(),
          getProfileStatistics().catch(() => ({})) // 统计信息失败不影响主流程，给个空对象兜底
        ])

        // 合并数据
        const fullUserInfo = {
          ...(this.userInfo || {}), // 保留原有信息
          ...(baseInfo || {}),      // 覆盖基础信息
          // 合并统计数据
          integration: stats?.integration ?? baseInfo?.integration ?? 0,
          growth: stats?.growth ?? baseInfo?.growth ?? 0,
          couponCount: stats?.couponCount ?? 0,
          levelId: stats?.levelId ?? baseInfo?.levelId,
          levelName: stats?.levelName ?? baseInfo?.levelName ?? ''
        }

        // 更新状态并持久化
        this.setUser({ userInfo: fullUserInfo })
        return fullUserInfo
      } catch (e) {
        console.error('刷新用户信息失败', e)
        // Token 失效等严重错误会在 request.js 拦截，这里主要是处理网络或业务异常
      }
    }
  }
})