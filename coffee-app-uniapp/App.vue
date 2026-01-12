<script setup>
import {onLaunch, onShow, onHide} from '@dcloudio/uni-app'
import {useUserStore} from '@/store/user.js'
import {BASE_URL} from '@/utils/config.js'
import {watch, onMounted} from 'vue'

const userStore = useUserStore()

const updateNativeTheme = (isDark) => {
  // 【安全检查】如果当前没有页面活跃，跳过原生 UI 设置
  const pages = getCurrentPages()
  if (!pages || pages.length === 0) return

  const frontColor = isDark ? '#ffffff' : '#000000'
  const bgColor = isDark ? '#1A1A1A' : '#ffffff' // 调整顶部导航栏背景色

  uni.setNavigationBarColor({
    frontColor: frontColor,
    backgroundColor: bgColor,
    animation: {duration: 300, timingFunc: 'easeIn'}
  }).catch(() => {
  }) // 忽略非原生导航栏页面的报错

  uni.setTabBarStyle({
    backgroundColor: isDark ? '#1A1A1A' : '#ffffff', // 调整 TabBar 背景色
    color: isDark ? '#999999' : '#999999',
    selectedColor: '#6f4e37',
    borderStyle: isDark ? 'black' : 'white'
  }).catch(() => {
  })
}

// 统一监听主题变化，注入全局类名 (H5端) 并更新原生组件颜色
watch(() => userStore.isDarkMode, (isDark) => {
  // #ifdef H5
  if (isDark) {
    document.documentElement.classList.add('theme-dark')
    document.documentElement.classList.remove('theme-light')
  } else {
    document.documentElement.classList.add('theme-light')
    document.documentElement.classList.remove('theme-dark')
  }
  // #endif

  // 延迟执行原生主题更新，避开实例初始化高峰
  setTimeout(() => {
    updateNativeTheme(isDark)
  }, 300)
})

// WebSocket 状态
let wsConnected = false
let isConnecting = false // 新增：正在连接标志位
let wsReconnectTimer = null
let wsHeartbeatTimer = null
let socketTask = null // 保存 socket 任务实例

// 【新增】防止弹窗重复变量
let lastNotifyOrderId = null // 记录上一次弹窗的订单ID
let isModalShowing = false   // 记录当前是否有弹窗正在显示

// 初始化 WebSocket 的函数
const initWebSocket = () => {
  // 1. 检查是否已有连接或正在连接
  if (wsConnected || isConnecting) {
    console.log('WebSocket 已连接或正在连接中，跳过')
    return
  }

  // 确保用户信息已加载
  if (!userStore.userInfo) {
    userStore.loadFromStorage()
  }

  const userId = userStore.userInfo?.id

  if (!userId) {
    console.log('用户未登录，暂不连接 WebSocket')
    return
  }

  const userIdStr = String(userId)

  // 设置连接中标志
  isConnecting = true

  // URL 处理
  let wsBase = BASE_URL;
  // #ifdef H5
  if (wsBase.startsWith('/')) {
    wsBase = window.location.protocol + '//' + window.location.host + wsBase
  }
  // #endif

  const wsUrl = wsBase.replace('http', 'ws') + '/ws/app/' + userIdStr
  console.log('正在连接 WebSocket:', wsUrl)

  // 关闭旧连接（如果存在）
  if (socketTask) {
    try {
      socketTask.close()
    } catch (e) {
    }
  }

  // 建立新连接
  socketTask = uni.connectSocket({
    url: wsUrl,
    success: () => {
      console.log('WebSocket 连接请求发送成功')
    },
    fail: (err) => {
      console.error('WebSocket 连接请求失败', err)
      isConnecting = false // 失败重置
      scheduleReconnect()
    }
  })

  // 监听连接打开
  uni.onSocketOpen((res) => {
    console.log('✅ WebSocket 连接已打开！')
    wsConnected = true
    isConnecting = false // 连接成功，重置标志

    // 清除重连定时器
    if (wsReconnectTimer) {
      clearTimeout(wsReconnectTimer)
      wsReconnectTimer = null
    }
    // 启动心跳
    startHeartbeat()
  })

  // 监听连接错误
  uni.onSocketError((res) => {
    console.error('❌ WebSocket 连接打开失败，请检查！', res)
    wsConnected = false
    isConnecting = false // 错误重置
    scheduleReconnect()
  })

  // 监听连接关闭
  uni.onSocketClose((res) => {
    console.log('WebSocket 连接已关闭', res)
    wsConnected = false
    isConnecting = false // 关闭重置
    stopHeartbeat()

    // 仅在非主动关闭且登录状态下重连
    if (userStore.isLogin) {
      scheduleReconnect()
    }
  })

  // 监听消息
  uni.onSocketMessage((res) => {
    // console.log('收到服务器消息：', res.data)
    if (res.data === 'pong') return

    try {
      const msg = JSON.parse(res.data)

      // 简单防抖：如果有 messageId 可以用来去重
      // 这里假设没有 id，只处理业务逻辑

      if (msg.type === 'PICKUP_READY') {
        // 1. 去重：如果这个订单ID刚刚才弹过窗，忽略它
        // （防止后端因为网络问题重发同一条消息）
        if (lastNotifyOrderId === msg.orderId) {
          console.log('重复消息已拦截，订单ID:', msg.orderId)
          return
        }

        // 2. 防叠：如果屏幕上已经有一个弹窗没关掉，忽略新消息
        // （防止多个不同订单同时完成导致弹窗叠罗汉）
        if (isModalShowing) {
          console.log('当前已有弹窗显示，暂不处理新消息')
          return
        }

        uni.vibrateLong()
        isModalShowing = true // 🔒 上锁
        lastNotifyOrderId = msg.orderId // 📝 记录ID

        // 检查是否已经在显示 Modal（避免重复弹窗）
        // UniApp 没有直接 API 查 Modal 状态，通常用全局变量或 Store 控制
        // 这里简单直接弹窗，因为 connectSocket 修复后消息不应该重复了
        uni.showModal({
          title: '取餐提醒',
          content: `您的订单 ${msg.pickupCode} 号已制作完成，请前往取餐！`,
          showCancel: false,
          confirmText: '我知道了',
          // 无论点击确定还是点击遮罩关闭，都会触发 complete
          complete: () => {
            isModalShowing = false // 🔓 解锁，允许下一个弹窗出现
          },
          success: function (res) {
            if (res.confirm) {
              uni.navigateTo({
                url: '/pages/order/detail?id=' + msg.orderId
              })
            }
          }
        })
      }
    } catch (e) {
      console.error('消息解析失败', e)
    }
  })
}

// 安排重连
const scheduleReconnect = () => {
  if (wsReconnectTimer) return

  wsReconnectTimer = setTimeout(() => {
    console.log('尝试重新连接 WebSocket...')
    wsReconnectTimer = null
    // 再次检查登录状态和连接状态
    if (userStore.isLogin && !wsConnected && !isConnecting) {
      initWebSocket()
    }
  }, 5000)
}

// 启动心跳
const startHeartbeat = () => {
  stopHeartbeat()
  wsHeartbeatTimer = setInterval(() => {
    if (wsConnected) {
      uni.sendSocketMessage({
        data: 'ping',
        fail: () => {
          console.warn('心跳发送失败')
          wsConnected = false
          // 可以选择在这里触发重连
        }
      })
    }
  }, 15000)
}

const stopHeartbeat = () => {
  if (wsHeartbeatTimer) {
    clearInterval(wsHeartbeatTimer)
    wsHeartbeatTimer = null
  }
}

onLaunch(() => {
  console.log('App Launch')

  // 启动时延迟同步主题，确保第一个页面已渲染
  setTimeout(() => {
    updateNativeTheme(userStore.isDarkMode)
  }, 500)

  // 1. 先尝试从 storage 加载
  const hasUserInfo = userStore.loadFromStorage && userStore.loadFromStorage()

  // 2. 如果加载成功，直接连接
  if (hasUserInfo) {
    initWebSocket()
  }

  // 3. 监听登录成功事件（主要用于：用户刚打开 App 没登录，后来去登录页面登录了）
  uni.$on('userLoginSuccess', () => {
    console.log('收到登录成功事件，尝试连接 WebSocket')
    // 这里依然保留延迟，为了确保 storage 写入完毕
    setTimeout(() => {
      initWebSocket()
    }, 500)
  })

  // 4. 监听退出登录，关闭 Socket
  uni.$on('userLogout', () => {
    if (socketTask) {
      socketTask.close()
    }
    wsConnected = false
    isConnecting = false // 重置连接状态
    stopHeartbeat()

    // 【核心修复】清除重连定时器，防止静默重连
    if (wsReconnectTimer) {
      clearTimeout(wsReconnectTimer)
      wsReconnectTimer = null
    }
    // 清理弹窗状态
    lastNotifyOrderId = null
    isModalShowing = false
    console.log('已清理 WebSocket 所有连接和定时器')
  })
})

onShow(() => {
  // 如果用户已登录但 socket 断开，尝试重连
  if (userStore.isLogin && !wsConnected && !isConnecting) {
    console.log('App Show: 重新检查 WebSocket 连接')
    initWebSocket()
  }
})

onHide(() => {
  // 不关闭，保持后台接收
})
</script>

<style lang="scss">
/* 每个页面公共css */
@import '@/uni_modules/uni-scss/index.scss';

/* 1. 引入改好了 Base64 的 iconfont.css */
@import '@/static/iconfont.css';

@import '@/styles/theme.scss';

/* 2. 注意：这里删除了原来写死的 @font-face 和 .iconfont */
/* 因为它们在上面的 iconfont.css 里已经定义过了，且原来写的路径是错误的 */

view, text, image, scroll-view {
  box-sizing: border-box;
}

page {
  background-color: var(--bg-secondary);
  font-size: 28rpx;
  color: var(--text-primary);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
  transition: background-color 0.3s, color 0.3s;
}

/* 适配微信小程序等不支持 :root 的环境 */
.theme-dark {
  /* 背景色体系 */
  --bg-primary: #1A1A1A;
  --bg-secondary: #000000;
  --bg-tertiary: #333333;
  --card-bg: #252525;

  /* 文字颜色体系 */
  --text-primary: #FFFFFF;
  --text-secondary: #CCCCCC;
  --text-tertiary: #999999;

  /* 边框和分割线 */
  --border-color: #404040;
  --border-light: #333333;
  --shadow-color: rgba(0, 0, 0, 0.6);
}

/* 隐藏滚动条 */
::-webkit-scrollbar {
  display: none;
  width: 0 !important;
  height: 0 !important;
  -webkit-appearance: none;
  background: transparent;
}
</style>