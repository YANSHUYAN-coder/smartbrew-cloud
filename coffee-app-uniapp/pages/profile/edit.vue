<template>
  <view class="profile-edit-page">
    <!-- 1. 头像编辑区 -->
    <view class="avatar-section" @click="handleAvatarClick">
      <view class="avatar-wrapper">
        <image 
          :src="form.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" 
          class="avatar-img" 
          mode="aspectFill" 
        />
        <view class="camera-icon">
          <uni-icons type="camera-filled" size="18" color="#fff"></uni-icons>
        </view>
      </view>
      <text class="avatar-tip">点击更换头像</text>
    </view>

    <!-- 2. 表单区域 -->
    <view class="form-card">
      <!-- 昵称 -->
      <view class="form-item">
        <text class="label">昵称</text>
        <input 
          class="input" 
          type="text" 
          v-model="form.nickname" 
          placeholder="请输入昵称" 
          placeholder-class="placeholder"
        />
      </view>

      <!-- 性别 -->
      <view class="form-item">
        <text class="label">性别</text>
        <view class="gender-radio">
          <view 
            class="radio-opt" 
            :class="{ active: form.gender === 1 }"
            @click="form.gender = 1"
          >
            <text class="icon">♂</text> 男
          </view>
          <view 
            class="radio-opt" 
            :class="{ active: form.gender === 2 }"
            @click="form.gender = 2"
          >
            <text class="icon">♀</text> 女
          </view>
          <view 
            class="radio-opt" 
            :class="{ active: form.gender === 0 }"
            @click="form.gender = 0"
          >
            保密
          </view>
        </view>
      </view>

      <!-- 生日 (Picker) -->
      <view class="form-item">
        <text class="label">生日</text>
        <picker 
          mode="date" 
          :value="form.birthday" 
          start="1950-01-01" 
          :end="endDate"
          @change="handleDateChange"
        >
          <view class="picker-value" :class="{ empty: !form.birthday }">
            {{ form.birthday || '请选择生日' }}
            <uni-icons type="forward" size="14" color="#ccc" style="margin-left: 8rpx;"></uni-icons>
          </view>
        </picker>
      </view>

      <!-- 城市 -->
      <view class="form-item">
        <text class="label">城市</text>
        <input 
          class="input" 
          type="text" 
          v-model="form.city" 
          placeholder="例如：深圳市" 
          placeholder-class="placeholder"
        />
      </view>
      
      <!-- 职业 -->
      <view class="form-item">
        <text class="label">职业</text>
        <input 
          class="input" 
          type="text" 
          v-model="form.job" 
          placeholder="例如：咖啡师" 
          placeholder-class="placeholder"
        />
      </view>

      <!-- 个性签名 -->
      <view class="form-item align-top">
        <text class="label">个性签名</text>
        <textarea 
          class="textarea" 
          v-model="form.signature" 
          placeholder="介绍一下自己吧..." 
          placeholder-class="placeholder" 
          maxlength="50"
        />
      </view>
    </view>

    <!-- 3. 保存按钮 -->
    <view class="footer-btn">
      <button class="save-btn" @click="handleSave" :loading="loading">保存修改</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {request} from '@/utils/request.js'

const loading = ref(false)
const endDate = new Date().toISOString().split('T')[0]

// 表单数据 (对应后端 UmsMemberUpdateDTO)
const form = reactive({
  nickname: '',
  avatar: '',
  gender: 0, // 0-未知 1-男 2-女
  birthday: '',
  city: '',
  job: '',
  signature: ''
})

// 加载当前用户信息
const fetchUserInfo = async () => {
  try {
    const res = await request({
      url: '/app/member/info',
      method: 'GET'
    })
    if (res.data) {
      // 这是一个简单的对象拷贝，实际开发可以用 Object.assign
      const { nickname, avatar, gender, birthday, city, job, signature } = res.data
      form.nickname = nickname || ''
      form.avatar = avatar || ''
      form.gender = gender || 0
      form.birthday = birthday || ''
      form.city = city || ''
      form.job = job || ''
      form.signature = signature || ''
    }
  } catch (e) {
    console.error('获取用户信息失败', e)
  }
}

// 模拟头像上传 (实际项目需要调用上传接口)
const handleAvatarClick = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      // 这里应该调用 uni.uploadFile 上传到服务器拿到 url
      // 演示：直接使用临时路径展示 (注意：后端存临时路径会导致下次无法访问)
      // 真实开发请对接你的 FileUploadController
      form.avatar = tempFilePath 
      uni.showToast({ title: '已选择图片 (需对接上传接口)', icon: 'none' })
    }
  })
}

const handleDateChange = (e) => {
  form.birthday = e.detail.value
}

// 提交保存
const handleSave = async () => {
  if (!form.nickname) {
    return uni.showToast({ title: '昵称不能为空', icon: 'none' })
  }

  loading.value = true
  try {
    await request({
      url: '/app/member/update',
      method: 'POST',
      data: form
    })
    
    uni.showToast({ title: '保存成功', icon: 'success' })
    
    // 更新本地缓存的用户信息 (如果有的话)
    // uni.setStorageSync('userInfo', { ... })
    
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
  fetchUserInfo()
})
</script>

<style lang="scss" scoped>
$primary: #6f4e37;

.profile-edit-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 40rpx;
}

/* 头像区域 */
.avatar-section {
  background-color: white;
  padding: 40rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24rpx;
}

.avatar-wrapper {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  margin-bottom: 20rpx;
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background-color: #eee;
  border: 4rpx solid #f8f8f8;
}

.camera-icon {
  position: absolute;
  bottom: 0;
  right: 0;
  background-color: $primary;
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid white;
}

.avatar-tip {
  font-size: 24rpx;
  color: #999;
}

/* 表单卡片 */
.form-card {
  background-color: white;
  padding: 0 32rpx;
}

.form-item {
  display: flex;
  align-items: center;
  min-height: 100rpx;
  border-bottom: 1rpx solid #f9f9f9;
  padding: 20rpx 0;
}

.form-item:last-child {
  border-bottom: none;
}

.form-item.align-top {
  align-items: flex-start;
}

.label {
  width: 160rpx;
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  text-align: right;
}

.textarea {
  flex: 1;
  height: 160rpx;
  font-size: 28rpx;
  color: #333;
  text-align: left; /* 多行文本通常左对齐更好看 */
  line-height: 1.5;
  padding-top: 4rpx;
}

.placeholder {
  color: #ccc;
}

/* Picker 样式 */
.picker-value {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}
.picker-value.empty {
  color: #ccc;
}

/* 性别单选 */
.gender-radio {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  gap: 20rpx;
}

.radio-opt {
  padding: 10rpx 24rpx;
  border-radius: 30rpx;
  font-size: 26rpx;
  color: #666;
  background-color: #f5f5f5;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.radio-opt.active {
  background-color: rgba(111, 78, 55, 0.1);
  color: $primary;
  font-weight: bold;
}

.icon {
  font-size: 24rpx;
}

/* 底部按钮 */
.footer-btn {
  margin-top: 60rpx;
  padding: 0 40rpx;
}

.save-btn {
  background-color: $primary;
  color: white;
  border-radius: 50rpx;
  font-size: 32rpx;
  height: 88rpx;
  line-height: 88rpx;
  border: none;
  box-shadow: 0 8rpx 20rpx rgba(111, 78, 55, 0.2);
}

.save-btn:active {
  opacity: 0.9;
  transform: scale(0.99);
}
</style>