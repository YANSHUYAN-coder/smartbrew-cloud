<template>
  <view class="settings-page">
    <view class="menu-list">
      <view class="menu-item" @click="goProfileEdit">
        <text class="menu-name">个人资料</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="handleSecurity">
        <text class="menu-name">账号安全</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item">
        <text class="menu-name">隐私设置</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <!-- 登录按钮 -->
    <view class="login-btn" @click="goLogin">登录账号</view>

    <view class="logout-btn" @click="handleLogout">退出登录</view>
  </view>
</template>

<script setup>
const goProfileEdit = () => {
  uni.navigateTo({ url: '/pages/profile/edit' })
}

const goLogin = () => {
  uni.navigateTo({ url: '/pages/login/index' })
}

const handleSecurity = () => {
  uni.showToast({ title: '修改密码功能开发中', icon: 'none' })
}

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: async (res) => {
      if (res.confirm) {
        // 1. 清除本地缓存
        uni.removeStorageSync('token')
        uni.removeStorageSync('userInfo')
        
        // 2. (可选) 调用后端注销接口，虽然是无状态JWT，但可以用来记录日志
        // await request.post('/app/member/logout')
        
        uni.showToast({ title: '已退出' })
        
        // 3. 返回首页或登录页
        setTimeout(() => {
          uni.switchTab({ url: '/pages/home/index' })
        }, 1000)
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.settings-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-top: 20rpx;
}

.menu-list {
  background-color: white;
  padding: 0 32rpx;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 0;
  border-bottom: 1rpx solid #f9f9f9;
}
.menu-item:last-child { border-bottom: none; }
.menu-name { font-size: 28rpx; color: #333; }
.menu-arrow { font-size: 28rpx; color: #ccc; }

.login-btn {
  margin-top: 40rpx;
  background-color: #6f4e37;
  color: #ffffff;
  text-align: center;
  padding: 28rpx;
  font-size: 30rpx;
  border-radius: 12rpx;
}

.logout-btn {
  margin-top: 40rpx;
  background-color: white;
  color: #ff4d4f;
  text-align: center;
  padding: 32rpx;
  font-size: 30rpx;
}
</style>