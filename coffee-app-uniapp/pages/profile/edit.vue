<template>
  <view class="profile-edit-container">
    <!-- 顶部背景装饰 -->
    <view class="header-bg"></view>

    <!-- 1. 头像编辑区 -->
    <view class="avatar-section" @click="handleAvatarClick">
      <view class="avatar-wrapper" hover-class="avatar-hover">
        <image 
          :src="form.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" 
          class="avatar-img" 
          mode="aspectFill" 
        />
        <!-- 相机图标徽章 -->
        <view class="camera-badge">
          <uni-icons type="camera-filled" size="16" color="#fff"></uni-icons>
        </view>
      </view>
      <text class="avatar-tip">点击更换头像</text>
    </view>

    <!-- 2. 表单区域 -->
    <view class="form-card">
      
      <!-- 昵称 -->
      <view class="form-item">
        <view class="label-box">
          <text class="label">昵称</text>
          <text class="required">*</text>
        </view>
        <input 
          class="input" 
          type="text" 
          v-model="form.nickname" 
          placeholder="请输入昵称" 
          placeholder-class="placeholder"
        />
      </view>

      <!-- 性别 (胶囊选择器) -->
      <view class="form-item">
        <text class="label">性别</text>
        <view class="gender-capsule">
          <view 
            class="capsule-item" 
            :class="{ 'active male': form.gender === 1 }"
            @click="form.gender = 1"
          >
            <uni-icons v-show="form.gender === 1" type="person-filled" size="14" color="#409eff" style="margin-right:4rpx;"></uni-icons>
            男
          </view>
          <view 
            class="capsule-item" 
            :class="{ 'active female': form.gender === 2 }"
            @click="form.gender = 2"
          >
            <uni-icons v-show="form.gender === 2" type="person-filled" size="14" color="#ff6b81" style="margin-right:4rpx;"></uni-icons>
            女
          </view>
          <view 
            class="capsule-item" 
            :class="{ active: form.gender === 0 }"
            @click="form.gender = 0"
          >
            保密
          </view>
        </view>
      </view>

      <!-- 生日 -->
      <view class="form-item">
        <text class="label">生日</text>
        <picker 
          mode="date" 
          :value="form.birthday" 
          start="1950-01-01" 
          :end="endDate"
          @change="handleDateChange"
          class="picker-box"
        >
          <view class="picker-value" :class="{ empty: !form.birthday }">
            {{ form.birthday || '请选择出生日期' }}
            <uni-icons type="right" size="14" color="#ccc" class="arrow-icon"></uni-icons>
          </view>
        </picker>
      </view>

      <!-- 城市 -->
      <view class="form-item">
        <text class="label">所在城市</text>
        <input 
          class="input" 
          type="text" 
          v-model="form.city" 
          placeholder="请输入城市" 
          placeholder-class="placeholder"
        />
      </view>
    </view>

    <!-- 个性签名卡片 (单独分开更美观) -->
    <view class="form-card bio-card">
      <view class="form-item-col">
        <text class="label">个性签名</text>
        <view class="textarea-wrapper">
          <textarea 
            class="textarea" 
            v-model="form.signature" 
            placeholder="写一句喜欢的话，展示独特的你..." 
            placeholder-class="placeholder" 
            maxlength="50"
            auto-height
          />
          <text class="char-count">{{ form.signature.length }}/50</text>
        </view>
      </view>
    </view>

    <!-- 3. 底部悬浮保存按钮 -->
    <view class="footer-placeholder"></view>
    <view class="footer-fixed">
      <button 
        class="save-btn" 
        hover-class="save-btn-hover" 
        @click="handleSave" 
        :loading="loading"
      >
        保存修改
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { request } from '@/utils/request.js'
import { useUserStore } from '@/store/user.js'

const loading = ref(false)
const endDate = new Date().toISOString().split('T')[0]
const userStore = useUserStore()

// 表单数据
const form = reactive({
  nickname: '',
  avatar: '',
  gender: 0, 
  birthday: '',
  city: '',
  signature: ''
})

// 从 store 填充表单数据
const fillFormFromStore = () => {
  if (userStore.userInfo) {
    Object.assign(form, {
      nickname: userStore.userInfo.nickname || '',
      avatar: userStore.userInfo.avatar || '',
      gender: userStore.userInfo.gender ?? 0,
      birthday: userStore.userInfo.birthday || '',
      city: userStore.userInfo.city || '',
      signature: userStore.userInfo.signature || ''
    })
  }
}

// 从接口获取最新用户信息并更新 store（可选，用于刷新数据）
const refreshUserInfo = async () => {
  try {
    const res = await request({
      url: '/app/member/info',
      method: 'GET'
    })
	console.log("res:",res);
    if (res) {
      // 同步更新 store
      userStore.setUser(userStore.token, res)
      // 更新表单数据
      fillFormFromStore()
    }
  } catch (e) {
    console.error('刷新用户信息失败', e)
  }
}

// 加载用户信息
const loadUserInfo = () => {
  // 由于登录时已经获取完整用户信息并存储到 store，这里直接使用 store 数据即可
  // 如果 store 中没有数据，说明用户可能未登录或数据异常
  if (!userStore.userInfo) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/login/index' })
    }, 1500)
    return
  }
  
  // 从 store 填充表单数据（快速显示，提升用户体验）
  fillFormFromStore()
  
  // 可选：如果需要确保数据是最新的，可以调用 refreshUserInfo()
  // 但通常登录时已经是最新数据，所以这里不需要再请求接口
  // refreshUserInfo()
}

// 模拟头像上传
const handleAvatarClick = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      // 真实开发请先 uni.uploadFile 上传到服务器拿到 url
      form.avatar = tempFilePath 
      uni.showToast({ title: '头像已选中', icon: 'none' })
    }
  })
}

const handleDateChange = (e) => {
  form.birthday = e.detail.value
}

// 提交保存
const handleSave = async () => {
  if (!form.nickname) {
    return uni.showToast({ title: '请填写昵称', icon: 'none' })
  }

  loading.value = true
  try {
    // 提交更新
    await request({
      url: '/app/member/update',
      method: 'POST',
      data: form
    })
    
    // 保存成功后，重新获取用户信息并更新 store
    // 这样可以确保 store 中的数据是最新的
    await refreshUserInfo()
    
    uni.showToast({ title: '保存成功', icon: 'success' })
    
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e) {
    uni.showToast({ title: e.message || '保存失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onLoad(() => {
  loadUserInfo()
})
</script>

<style lang="scss" scoped>
/* 主题色变量 */
$primary: #6f4e37;
$primary-light: #8d6e63;
$primary-dark: #5d4037;
$bg-color: #f7f8fa;
$bg-gradient-start: #f5efe8;
$bg-gradient-end: #f7f8fa;
$text-main: #333;
$text-secondary: #666;
$text-light: #999;
$text-placeholder: #bbb;
$border-color: #f0f0f0;
$border-hover: #e0e0e0;
$card-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
$card-shadow-hover: 0 8rpx 30rpx rgba(0, 0, 0, 0.08);

.profile-edit-container {
  min-height: 100vh;
  background-color: $bg-color;
  position: relative;
  /* 防止 iPhone X 底部遮挡 */
  padding-bottom: env(safe-area-inset-bottom);
}

/* 顶部装饰背景 - 优化渐变效果 */
.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 240rpx;
  background: linear-gradient(180deg, $bg-gradient-start 0%, rgba(245, 239, 232, 0.6) 60%, $bg-gradient-end 100%);
  z-index: 0;
  
  /* 增加微妙的波浪效果 */
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 20rpx;
    background: linear-gradient(180deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.3) 100%);
  }
}

/* 1. 头像区域 - 增强视觉效果 */
.avatar-section {
  position: relative;
  z-index: 1;
  padding: 50rpx 0 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-wrapper {
  position: relative;
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #fff 0%, #f5f5f5 100%);
  padding: 8rpx;
  box-shadow: 0 12rpx 32rpx rgba(111, 78, 55, 0.18), 
              0 4rpx 12rpx rgba(111, 78, 55, 0.12);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  
  &::before {
    content: '';
    position: absolute;
    inset: -4rpx;
    border-radius: 50%;
    background: linear-gradient(135deg, rgba(111, 78, 55, 0.1), rgba(141, 110, 99, 0.1));
    opacity: 0;
    transition: opacity 0.3s;
    z-index: -1;
  }
  
  &:active::before {
    opacity: 1;
  }
}

.avatar-hover {
  transform: scale(0.96);
  
  &::before {
    opacity: 1;
  }
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #e8e8e8 0%, #f5f5f5 100%);
  border: 2rpx solid rgba(255, 255, 255, 0.8);
  transition: transform 0.3s;
}

.camera-badge {
  position: absolute;
  bottom: 8rpx;
  right: 8rpx;
  background: linear-gradient(135deg, $primary 0%, $primary-light 100%);
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 5rpx solid #fff;
  box-shadow: 0 6rpx 16rpx rgba(111, 78, 55, 0.25),
              0 2rpx 6rpx rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  z-index: 2;
  
  &:active {
    transform: scale(0.9);
  }
}

.avatar-tip {
  margin-top: 24rpx;
  font-size: 26rpx;
  color: $text-secondary;
  background: linear-gradient(135deg, rgba(255,255,255,0.9) 0%, rgba(255,255,255,0.7) 100%);
  padding: 10rpx 24rpx;
  border-radius: 24rpx;
  backdrop-filter: blur(10rpx);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
}

/* 2. 通用卡片样式 - 优化阴影和层次 */
.form-card {
  background-color: #fff;
  margin: 0 30rpx 28rpx;
  border-radius: 28rpx;
  padding: 0 32rpx;
  box-shadow: $card-shadow;
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
  overflow: hidden;
  
  /* 顶部微光效果 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 2rpx;
    background: linear-gradient(90deg, transparent, rgba(111, 78, 55, 0.1), transparent);
  }
}

.form-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 112rpx;
  border-bottom: 1rpx solid $border-color;
  transition: background-color 0.2s;
  position: relative;
  
  /* 悬停效果 */
  &:active {
    background-color: rgba(111, 78, 55, 0.02);
  }
  
  &:last-child {
    border-bottom: none;
  }
  
  /* 分割线优化 */
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 32rpx;
    right: 32rpx;
    height: 1rpx;
    background: linear-gradient(90deg, transparent, $border-color 20%, $border-color 80%, transparent);
  }
  
  &:last-child::after {
    display: none;
  }
}

/* 纵向布局的表单项（如个性签名） */
.form-item-col {
  padding: 32rpx 0 28rpx;
  display: flex;
  flex-direction: column;
}

.label-box {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.label {
  font-size: 31rpx;
  color: $text-main;
  font-weight: 600;
  letter-spacing: 0.5rpx;
}

.required {
  color: #ff4d4f;
  font-size: 28rpx;
  font-weight: bold;
  line-height: 1;
  margin-top: 4rpx;
}

.input {
  flex: 1;
  text-align: right;
  font-size: 30rpx;
  color: $text-main;
  margin-left: 32rpx;
  padding: 8rpx 0;
  transition: color 0.2s;
  
  &:focus {
    color: $primary;
  }
}

.placeholder {
  color: $text-placeholder;
  font-size: 28rpx;
}

/* 3. 胶囊性别选择器 - 增强视觉反馈 */
.gender-capsule {
  display: flex;
  background: linear-gradient(135deg, #f8f8f8 0%, #f0f0f0 100%);
  border-radius: 40rpx;
  padding: 6rpx;
  box-shadow: inset 0 2rpx 4rpx rgba(0, 0, 0, 0.04);
}

.capsule-item {
  padding: 12rpx 36rpx;
  border-radius: 36rpx;
  font-size: 27rpx;
  color: $text-secondary;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  cursor: pointer;
  
  &:active {
    transform: scale(0.95);
  }
  
  &.active {
    background: linear-gradient(135deg, #fff 0%, #fafafa 100%);
    color: $text-main;
    font-weight: 600;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.12),
                0 2rpx 4rpx rgba(0, 0, 0, 0.08);
    transform: translateY(-1rpx);
  }
  
  /* 男生选中色 */
  &.active.male {
    color: #409eff;
    background: linear-gradient(135deg, #e3f2fd 0%, #fff 100%);
    
    &::before {
      content: '';
      position: absolute;
      inset: 0;
      border-radius: 36rpx;
      background: linear-gradient(135deg, rgba(64, 158, 255, 0.1), transparent);
    }
  }
  
  /* 女生选中色 */
  &.active.female {
    color: #ff6b81;
    background: linear-gradient(135deg, #ffeef1 0%, #fff 100%);
    
    &::before {
      content: '';
      position: absolute;
      inset: 0;
      border-radius: 36rpx;
      background: linear-gradient(135deg, rgba(255, 107, 129, 0.1), transparent);
    }
  }
}

/* 4. Picker 样式优化 */
.picker-box {
  flex: 1;
  cursor: pointer;
}

.picker-value {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  font-size: 30rpx;
  color: $text-main;
  padding: 8rpx 0;
  transition: color 0.2s;
  
  &.empty {
    color: $text-placeholder;
    font-size: 28rpx;
  }
}

.arrow-icon {
  margin-left: 12rpx;
  margin-top: 2rpx;
  transition: transform 0.2s, color 0.2s;
}

.picker-box:active .arrow-icon {
  transform: translateX(4rpx);
  color: $primary;
}

/* 5. 个性签名 Textarea - 优化视觉层次 */
.bio-card {
  padding: 0 32rpx 16rpx;
}

.textarea-wrapper {
  margin-top: 24rpx;
  background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
  border-radius: 20rpx;
  padding: 24rpx;
  position: relative;
  border: 2rpx solid transparent;
  transition: all 0.3s;
  
  &:focus-within {
    background: #fff;
    border-color: rgba(111, 78, 55, 0.2);
    box-shadow: 0 4rpx 16rpx rgba(111, 78, 55, 0.08);
  }
}

.textarea {
  width: 100%;
  min-height: 140rpx;
  font-size: 29rpx;
  color: $text-main;
  line-height: 1.6;
  background: transparent;
  transition: color 0.2s;
}

.char-count {
  position: absolute;
  bottom: 16rpx;
  right: 24rpx;
  font-size: 24rpx;
  color: $text-light;
  font-weight: 500;
  background: rgba(255, 255, 255, 0.8);
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  backdrop-filter: blur(4rpx);
  transition: color 0.2s;
}

.textarea-wrapper:focus-within .char-count {
  color: $text-secondary;
}

/* 6. 底部固定按钮 - 优化渐变和阴影 */
.footer-placeholder {
  height: 180rpx;
}

.footer-fixed {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  background: linear-gradient(180deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.98) 20%, #fff 100%);
  padding: 24rpx 40rpx calc(24rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.06),
              0 -2rpx 8rpx rgba(0, 0, 0, 0.03);
  z-index: 99;
  backdrop-filter: blur(20rpx);
  box-sizing: border-box;
  
  /* 确保 button 正确显示 */
  button {
    width: 100%;
    margin: 0;
  }
}

.save-btn {
  width: 100%;
  background: linear-gradient(135deg, $primary-light 0%, $primary 50%, $primary-dark 100%);
  color: white;
  border-radius: 50rpx;
  font-size: 33rpx;
  font-weight: 600;
  height: 96rpx;
  line-height: 96rpx;
  border: none;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  box-shadow: 0 12rpx 32rpx rgba(111, 78, 55, 0.35),
              0 4rpx 12rpx rgba(111, 78, 55, 0.25),
              inset 0 1rpx 0 rgba(255, 255, 255, 0.2);
  letter-spacing: 3rpx;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  
  /* 按钮光效 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
    transition: left 0.5s;
  }
  
  &:active::before {
    left: 100%;
  }
  
  /* 重置 button 默认样式 */
  &::after {
    border: none;
  }
}

.save-btn-hover {
  transform: translateY(-2rpx);
  box-shadow: 0 16rpx 40rpx rgba(111, 78, 55, 0.4),
              0 6rpx 16rpx rgba(111, 78, 55, 0.3),
              inset 0 1rpx 0 rgba(255, 255, 255, 0.2);
}

.save-btn:active {
  transform: translateY(0) scale(0.98);
  box-shadow: 0 8rpx 24rpx rgba(111, 78, 55, 0.3),
              0 2rpx 8rpx rgba(111, 78, 55, 0.2);
}
</style>