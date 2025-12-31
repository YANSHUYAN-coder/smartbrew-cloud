<template>
  <view class="container" :class="themeClass">

    <!-- 页面标题 (自定义导航栏时使用，原生导航栏可忽略) -->
    <view class="page-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <text class="page-title">设置</text>
    </view>


    <!-- 分组 1: 账号与安全 -->
    <view class="section-title">账号</view>
    <view class="section-group">
      <view class="cell-item" hover-class="cell-hover" @click="navTo('/pages/profile/edit')">
        <view class="cell-left">
          <view class="icon-box bg-orange">
            <uni-icons type="person-filled" size="20" color="#fff"></uni-icons>
          </view>
          <text class="cell-title">个人资料</text>
        </view>
        <uni-icons type="right" size="14" color="#ccc"></uni-icons>
      </view>

      <view class="cell-item" hover-class="cell-hover" @click="navTo('/pages/address/list')">
        <view class="cell-left">
          <view class="icon-box bg-green">
            <uni-icons type="location-filled" size="20" color="#fff"></uni-icons>
          </view>
          <text class="cell-title">地址管理</text>
        </view>
        <uni-icons type="right" size="14" color="#ccc"></uni-icons>
      </view>

      <view class="cell-item" hover-class="cell-hover" @click="handleSecurity">
        <view class="cell-left">
          <view class="icon-box bg-blue">
            <uni-icons type="locked-filled" size="20" color="#fff"></uni-icons>
          </view>
          <text class="cell-title">账号与安全</text>
        </view>
        <uni-icons type="right" size="14" color="#ccc"></uni-icons>
      </view>
    </view>

    <!-- 分组 2: 偏好设置 (新增模块) -->
    <view class="section-title">偏好</view>
    <view class="section-group">
      <view class="cell-item">
        <view class="cell-left">
          <view class="icon-box bg-purple">
            <uni-icons type="notification-filled" size="20" color="#fff"></uni-icons>
          </view>
          <text class="cell-title">消息通知</text>
        </view>
        <switch :checked="notifyEnabled" color="#d4a373" style="transform:scale(0.8)" @change="toggleNotify"/>
      </view>
      <view class="cell-item">
        <view class="cell-left">
          <view class="icon-box bg-dark">
            <uni-icons type="moon-filled" size="20" color="#fff"></uni-icons>
          </view>
          <text class="cell-title">深色模式</text>
        </view>
        <switch :checked="darkMode" color="#d4a373" style="transform:scale(0.8)" @change="toggleDarkMode"/>
      </view>
    </view>

    <!-- 分组 3: 通用与支持 -->
    <view class="section-title">更多</view>
    <view class="section-group">
      <view class="cell-item" hover-class="cell-hover" @click="handleClearCache">
        <view class="cell-left">
          <view class="icon-box bg-gray">
            <uni-icons type="trash-filled" size="20" color="#fff"></uni-icons>
          </view>
          <text class="cell-title">清除缓存</text>
        </view>
        <view class="cell-right">
          <text class="cell-tip">{{ cacheSize }}</text>
          <uni-icons type="right" size="14" color="#ccc"></uni-icons>
        </view>
      </view>

      <view class="cell-item" hover-class="cell-hover" @click="navTo('/pages/help/index')">
        <view class="cell-left">
          <view class="icon-box bg-teal">
            <uni-icons type="help-filled" size="20" color="#fff"></uni-icons>
          </view>
          <text class="cell-title">帮助中心</text>
        </view>
        <uni-icons type="right" size="14" color="#ccc"></uni-icons>
      </view>

      <view class="cell-item" hover-class="cell-hover" @click="handleShare">
        <view class="cell-left">
          <view class="icon-box bg-pink">
            <uni-icons type="paperplane-filled" size="20" color="#fff"></uni-icons>
          </view>
          <text class="cell-title">分享给朋友</text>
        </view>
        <uni-icons type="right" size="14" color="#ccc"></uni-icons>
      </view>

      <view class="cell-item" hover-class="cell-hover" @click="navTo('/pages/about/index')">
        <view class="cell-left">
          <view class="icon-box bg-yellow">
            <uni-icons type="info-filled" size="20" color="#fff"></uni-icons>
          </view>
          <text class="cell-title">关于 SmartBrew</text>
        </view>
        <view class="cell-right">
          <text class="cell-tip">v1.0.0</text>
          <uni-icons type="right" size="14" color="#ccc"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 底部操作按钮 -->
    <view class="footer-action">
      <button v-if="hasLogin" class="btn-logout" hover-class="btn-hover" @click="handleLogout">
        退出登录
      </button>
      <button v-else class="btn-login" hover-class="btn-hover" @click="navTo('/pages/login/index')">
        立即登录
      </button>

      <view class="app-info">
        <text class="link" @click="navTo('/pages/about/privacy')">隐私政策</text>
        <text class="divider">|</text>
        <text class="link" @click="navTo('/pages/about/agreement')">用户协议</text>
      </view>
      <text class="copyright">SmartBrew Coffee App © 2024</text>
    </view>
  </view>
</template>

<script setup>
import {
  ref,
  computed,
  onMounted
} from 'vue'
import {
  onShow,
  onPullDownRefresh
} from '@dcloudio/uni-app'
import {useUserStore} from '@/store/user.js'
import {
  getStatusBarHeight
} from '@/utils/system.js'

const statusBarHeight = ref(0)
const cacheSize = ref('0KB')
const userStore = useUserStore()

const themeClass = computed(() => userStore.isDarkMode ? 'theme-dark' : 'theme-light')

// 偏好设置状态
const notifyEnabled = ref(true)
const darkMode = computed(() => userStore.themeMode === 'dark')

// 是否已登录
const hasLogin = computed(() => userStore.isLogin)
// 用户信息
const userInfo = computed(() => userStore.userInfo)

onShow(() => {
  calculateCache()
})
onMounted(() => {
  statusBarHeight.value = getStatusBarHeight()
})

// ... (页面跳转等方法不变)

// 切换深色模式
const toggleDarkMode = (e) => {
  const isDark = e.detail.value
  userStore.setThemeMode(isDark ? 'dark' : 'light')
  uni.showToast({title: isDark ? '已开启深色模式' : '已关闭深色模式', icon: 'none'})
}

// 分享
const handleShare = () => {
  uni.share({
    provider: "weixin",
    scene: "WXSceneSession",
    type: 0,
    href: "http://www.smartbrew.com",
    title: "SmartBrew Coffee",
    summary: "最好的咖啡体验",
    imageUrl: "/static/logo.png",
    success: function (res) {
      console.log("success:" + JSON.stringify(res));
    }
  });
  // 注意：uni.share 在小程序中行为不同，通常需要 onShareAppMessage
}

// 计算缓存
const calculateCache = () => {
  // 模拟
  const size = Math.floor(Math.random() * 5000) / 100
  cacheSize.value = size > 0 ? `${size}MB` : '0KB'
}

// 清除缓存
const handleClearCache = () => {
  uni.showModal({
    title: '提示',
    content: '确定要清除本地缓存吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({title: '清理中...'})
        setTimeout(() => {
          cacheSize.value = '0KB'
          uni.hideLoading()
          uni.showToast({title: '清理完成'})
        }, 800)
      }
    }
  })
}

const handleLogout = () => {
  uni.showModal({
    title: '退出确认',
    content: '确定要退出当前账号吗？',
    confirmColor: '#d4a373',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({title: '退出中...', mask: true})
        try {
          // 调用 store 的 logout
          // 由于 store 中 logout 使用了 finally 且进行了 reLaunch，
          // 这里的代码可能在跳转瞬间执行，也可能不执行，取决于运行环境速度。
          await userStore.logout()
        } catch (e) {
          // 理论上 store.logout 不会抛出异常，这里是为了兜底
          console.error('Logout error', e)
        } finally {
          // 确保关闭 loading (虽然页面可能已经跳转销毁)
          uni.hideLoading()
        }
      }
    }
  })
}

// 下拉刷新
onPullDownRefresh(async () => {
  calculateCache()
  setTimeout(() => {
    uni.stopPullDownRefresh()
  }, 1000)
})
</script>

<style lang="scss" scoped>
/* 页面容器：更柔和的背景色 */
.container {
  min-height: 100vh;
  background-color: var(--bg-secondary);
  padding: 20rpx;
  padding-bottom: 60rpx;
  transition: background-color 0.3s;
}

.page-header {
  // margin-bottom: 30rpx;
  justify-content: center;
  align-items: center;
  display: flex;

  .page-title {
    font-size: 38rpx;
    font-weight: bold;
    color: var(--text-primary);
  }
}


/* 分组标题 */
.section-title {
  font-size: 26rpx;
  color: var(--text-tertiary);
  margin-bottom: 16rpx;
  margin-left: 10rpx;
  font-weight: 500;
}

/* 分组卡片 */
.section-group {
  background-color: var(--bg-primary);
  border-radius: 24rpx;
  margin-bottom: 40rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx var(--shadow-color);
}

/* 列表项 */
.cell-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  background-color: var(--bg-primary);
  position: relative;

  &::after {
    content: '';
    position: absolute;
    left: 100rpx; /* 让线条不贯穿图标 */
    right: 0;
    bottom: 0;
    height: 1px;
    background-color: var(--border-light);
    transform: scaleY(0.5);
  }

  &:last-child::after {
    display: none;
  }
}

.cell-hover {
  background-color: var(--bg-tertiary);
}

/* 左侧 */
.cell-left {
  display: flex;
  align-items: center;

  .icon-box {
    width: 60rpx;
    height: 60rpx;
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 24rpx;

    /* 图标背景色系 (这些可以保持，或者也调暗) */
    &.bg-orange { background-color: #ff9f43; }
    &.bg-green { background-color: #2ecc71; }
    &.bg-blue { background-color: #54a0ff; }
    &.bg-purple { background-color: #5f27cd; }
    &.bg-dark { background-color: #576574; }
    &.bg-gray { background-color: #8395a7; }
    &.bg-teal { background-color: #00d2d3; }
    &.bg-pink { background-color: #ff6b6b; }
    &.bg-yellow { background-color: #feca57; }
  }

  .cell-title {
    font-size: 30rpx;
    color: var(--text-primary);
    font-weight: 500;
  }
}

/* 右侧 */
.cell-right {
  display: flex;
  align-items: center;

  .cell-tip {
    font-size: 26rpx;
    color: var(--text-tertiary);
    margin-right: 10rpx;
  }
}

/* 底部按钮 */
.footer-action {
  margin-top: 60rpx;
  padding: 0 10rpx;
  display: flex;
  flex-direction: column;
  align-items: center;

  .btn-logout {
    width: 100%;
    background-color: var(--bg-primary);
    color: #ff4757;
    font-size: 32rpx;
    font-weight: 600;
    border-radius: 50rpx;
    padding: 22rpx 0;
    border: 2rpx solid var(--border-light);
    margin-bottom: 30rpx;

    &::after {
      border: none;
    }
  }

  .btn-login {
    width: 100%;
    background-color: #d4a373;
    color: #fff;
    font-size: 32rpx;
    font-weight: 600;
    border-radius: 50rpx;
    padding: 22rpx 0;
    margin-bottom: 30rpx;
    box-shadow: 0 8rpx 20rpx rgba(212, 163, 115, 0.3);

    &::after {
      border: none;
    }
  }

  .btn-hover {
    opacity: 0.9;
    transform: scale(0.99);
  }

  .app-info {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16rpx;

    .link {
      font-size: 24rpx;
      color: #d4a373;
      padding: 10rpx;
    }

    .divider {
      font-size: 24rpx;
      color: var(--border-color);
      margin: 0 10rpx;
    }
  }

  .copyright {
    font-size: 22rpx;
    color: var(--text-tertiary);
  }
}
</style>