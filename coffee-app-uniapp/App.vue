<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { BASE_URL } from '@/utils/config.js'

const userStore = useUserStore()

// WebSocket 状态
let wsConnected = false
let isConnecting = false // 新增：正在连接标志位
let wsReconnectTimer = null
let wsHeartbeatTimer = null
let socketTask = null // 保存 socket 任务实例

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
    } catch(e) {}
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
        uni.vibrateLong()

        // 检查是否已经在显示 Modal（避免重复弹窗）
        // UniApp 没有直接 API 查 Modal 状态，通常用全局变量或 Store 控制
        // 这里简单直接弹窗，因为 connectSocket 修复后消息不应该重复了
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
    stopHeartbeat()
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