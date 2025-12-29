<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
// 1. 正确引入 BASE_URL，而不是 config
import { BASE_URL } from '@/utils/config.js'

const userStore = useUserStore()

// WebSocket 连接状态
let wsConnected = false
let wsReconnectTimer = null
let wsHeartbeatTimer = null

// 初始化 WebSocket 的函数
const initWebSocket = () => {
  // 确保用户信息已加载
  if (!userStore.userInfo) {
    userStore.loadFromStorage()
  }
  
  const userId = userStore.userInfo?.id

  if (!userId) {
    console.log('用户未登录，暂不连接 WebSocket')
    return
  }

  // 确保 userId 是字符串类型（后端期望 String）
  const userIdStr = String(userId)

  // 如果已经连接，不重复连接
  if (wsConnected) {
    console.log('WebSocket 已连接，跳过重复连接')
    return
  }

  // 2. 修复 URL 拼接逻辑
  // BASE_URL 通常格式为 "http://ip:port/api"
  // 替换 http -> ws 得到 "ws://ip:port/api"
  // 后端 Endpoint 为 "/ws/app/{userId}"
  // 最终拼接为 "ws://ip:port/api/ws/app/{userId}"
  let wsBase = BASE_URL;

  // #ifdef H5
  // H5 开发环境下 BASE_URL 可能仅为 "/api" (相对路径)
  // 需要补全 host 才能建立 WebSocket 连接
  if (wsBase.startsWith('/')) {
    wsBase = window.location.protocol + '//' + window.location.host + wsBase
  }
  // #endif

  const wsUrl = wsBase.replace('http', 'ws') + '/ws/app/' + userIdStr
  console.log('正在连接 WebSocket:', wsUrl)

  uni.connectSocket({
    url: wsUrl,
    success: () => {
      console.log('WebSocket 连接请求发送成功')
    },
    fail: (err) => {
      console.error('WebSocket 连接请求失败', err)
      // 连接失败，5秒后重试
      scheduleReconnect()
    }
  })

  // 监听连接打开
  uni.onSocketOpen((res) => {
    console.log('✅ WebSocket 连接已打开！')
    wsConnected = true
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
    // 连接失败，5秒后重试
    scheduleReconnect()
  })

  // 监听连接关闭
  uni.onSocketClose((res) => {
    console.log('WebSocket 连接已关闭', res)
    wsConnected = false
    // 停止心跳
    stopHeartbeat()
    // 如果用户已登录，尝试重连
    if (userStore.isLogin) {
      scheduleReconnect()
    }
  })

  // 监听消息
  uni.onSocketMessage((res) => {
    console.log('收到服务器消息：', res.data)
    if (res.data === 'pong') return

    try {
      const msg = JSON.parse(res.data)

      if (msg.type === 'PICKUP_READY') {
        // 震动
        uni.vibrateLong()

        // 弹窗
        uni.showModal({
          title: '取餐提醒',
          content: `您的订单 ${msg.pickupCode} 号已制作完成，请前往取餐！`,
          showCancel: false,
          confirmText: '我知道了',
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
  if (wsReconnectTimer) return // 避免重复安排
  
  wsReconnectTimer = setTimeout(() => {
    console.log('尝试重新连接 WebSocket...')
    wsReconnectTimer = null
    if (userStore.isLogin && !wsConnected) {
      initWebSocket()
    }
  }, 5000)
}

// 启动心跳
const startHeartbeat = () => {
  stopHeartbeat() // 先清除旧的定时器
  
  wsHeartbeatTimer = setInterval(() => {
    if (wsConnected) {
      uni.sendSocketMessage({
        data: 'ping',
        fail: () => {
          console.warn('心跳发送失败，可能连接已断开')
          wsConnected = false
        }
      })
    }
  }, 15000)
}

// 停止心跳
const stopHeartbeat = () => {
  if (wsHeartbeatTimer) {
    clearInterval(wsHeartbeatTimer)
    wsHeartbeatTimer = null
  }
}

onLaunch(() => {
  console.log('App Launch')
  // 尝试从本地恢复用户信息，防止页面刷新丢失
  userStore.loadFromStorage && userStore.loadFromStorage()
  initWebSocket()
  
  // 监听登录成功事件，立即连接 WebSocket
  uni.$on('userLoginSuccess', () => {
    console.log('收到登录成功事件，尝试连接 WebSocket')
    setTimeout(() => {
      initWebSocket()
    }, 500) // 延迟500ms，确保用户信息已保存
  })
})

onShow(() => {
  console.log('App Show')
  // 如果用户已登录但 WebSocket 未连接，尝试连接
  if (userStore.isLogin && !wsConnected) {
    console.log('检测到用户已登录但 WebSocket 未连接，尝试连接...')
    initWebSocket()
  }
})

onHide(() => {
  console.log('App Hide')
  // App 隐藏时不断开连接，保持后台接收消息的能力
})
</script>

<style lang="scss">
/* 每个页面公共css */
@import '@/uni_modules/uni-scss/index.scss';
@import '@/static/iconfont.css';

/* 引入字体图标 */
@font-face {
  font-family: 'iconfont';
  src: url('static/iconfont.ttf?t=1650355047728') format('truetype');
}

.iconfont {
  font-family: "iconfont" !important;
  font-size: 16px;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

view, text, image, scroll-view {
  box-sizing: border-box;
}

page {
  background-color: #f5f5f5;
  font-size: 28rpx;
  color: #333;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
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