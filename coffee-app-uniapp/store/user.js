import { defineStore } from 'pinia'
import { getUserInfo, getProfileStatistics, logout as apiLogout } from '@/services/user.js'

// 本地存储 Key 统一管理
const TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refreshToken'
const USER_INFO_KEY = 'userInfo'
const THEME_MODE_KEY = 'themeMode'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: uni.getStorageSync(TOKEN_KEY) || '',
    refreshToken: uni.getStorageSync(REFRESH_TOKEN_KEY) || '',
    userInfo: uni.getStorageSync(USER_INFO_KEY) || null,
    themeMode: uni.getStorageSync(THEME_MODE_KEY) || 'auto' // 'light', 'dark', 'auto'
  }),

  getters: {
    isLogin: (state) => !!state.token,
    isDarkMode: (state) => {
      if (state.themeMode === 'auto') {
        // 返回系统主题判定
        const systemInfo = uni.getSystemInfoSync()
        return systemInfo.theme === 'dark'
      }
      return state.themeMode === 'dark'
    }
  },

  actions: {
    setThemeMode(mode) {
      this.themeMode = mode
      uni.setStorageSync(THEME_MODE_KEY, mode)
      
      // H5 端额外处理
      // #ifdef H5
      if (mode === 'dark' || (mode === 'auto' && uni.getSystemInfoSync().theme === 'dark')) {
        document.documentElement.classList.add('theme-dark')
      } else {
        document.documentElement.classList.remove('theme-dark')
      }
      // #endif
    },
    /**
     * 设置/更新用户状态并持久化
     */
    setUser(payload) {
      // 兼容旧的传参方式 setUser(token, userInfo)
      if (arguments.length > 1) {
        this.token = arguments[0] || ''
        this.userInfo = arguments[1] || null
      } else {
        const { token, refreshToken, userInfo } = payload || {}
        // 只有当传入了 undefined 以外的值时才更新，允许只更新部分字段
        if (token !== undefined) this.token = token
        if (refreshToken !== undefined) this.refreshToken = refreshToken
        if (userInfo !== undefined) this.userInfo = userInfo
      }
      this.saveToStorage()
    },

    /**
     * 统一保存到本地存储 (根据当前 State 决定是 Set 还是 Remove)
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
     * 重置所有状态（内部方法）
     */
    resetState() {
      this.token = ''
      this.refreshToken = ''
      this.userInfo = null
      this.saveToStorage() // 调用统一存储方法清理 storage
    },

    /**
     * 退出登录（健壮性修复）
     */
    async logout() {
      // 1. 如果本身就没登录，直接跳过
      if (!this.token) {
        uni.reLaunch({ url: '/pages/login/index' })
        return
      }

      try {
        // 2. 尝试调用后端接口 (尽力而为)
        // 即使 token 过期或网络错误，catch 住错误，不影响本地退出
        await apiLogout()
      } catch (e) {
        console.warn('后端退出接口调用失败或超时，继续执行本地退出', e)
      } finally {
        // 3. 无论 API 成功与否，强制清理本地数据
        this.resetState()

        // 4. 触发全局事件 (用于清理其他 store 数据或 socket 连接)
        uni.$emit('userLogout')

        // 5. 跳转登录页
        uni.reLaunch({ url: '/pages/login/index' })
      }
    },

    /**
     * 刷新用户信息
     */
    async fetchUserInfo() {
      if (!this.token) return
      try {
        const [baseInfo, stats] = await Promise.all([
          getUserInfo(),
          getProfileStatistics().catch(() => ({}))
        ])

        const fullUserInfo = {
          ...(this.userInfo || {}),
          ...(baseInfo || {}),
          integration: stats?.integration ?? baseInfo?.integration ?? 0,
          growth: stats?.growth ?? baseInfo?.growth ?? 0,
          couponCount: stats?.couponCount ?? 0,
          levelId: stats?.levelId ?? baseInfo?.levelId,
          levelName: stats?.levelName ?? baseInfo?.levelName ?? ''
        }

        this.setUser({ userInfo: fullUserInfo })
        return fullUserInfo
      } catch (e) {
        console.error('刷新用户信息失败', e)
      }
    }
  }
})