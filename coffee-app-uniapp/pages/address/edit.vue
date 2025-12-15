<template>
  <view class="address-edit-page">
    <view class="form-container">
      <view class="form-item">
        <text class="label">联系人</text>
        <input class="input" type="text" v-model="form.name" placeholder="请输入姓名" placeholder-class="placeholder" />
      </view>
      <view class="form-item">
        <text class="label">手机号</text>
        <input class="input" type="number" v-model="form.phone" maxlength="11" placeholder="请输入手机号" placeholder-class="placeholder" />
      </view>
      <view class="form-item">
        <text class="label">所在地区</text>
        <!-- 这里简化处理，实际可以使用 uni-data-picker -->
        <input class="input" type="text" v-model="regionStr" placeholder="省市区 (如: 广东省 深圳市 南山区)" placeholder-class="placeholder" />
        <!-- <uni-icons type="forward" size="16" color="#ccc"></uni-icons> -->
      </view>
      <view class="form-item align-top">
        <text class="label">详细地址</text>
        <textarea class="textarea" v-model="form.detailAddress" placeholder="街道、楼牌号等" placeholder-class="placeholder" />
      </view>
    </view>

    <view class="form-container" style="margin-top: 24rpx;">
      <view class="form-item switch-item">
        <text class="label">设为默认地址</text>
        <switch :checked="form.defaultStatus === 1" color="#6f4e37" @change="handleDefaultChange" style="transform: scale(0.8)" />
      </view>
    </view>
    
    <view class="delete-btn" v-if="isEdit" @click="handleDelete">删除收货地址</view>

    <view class="footer-btn">
      <button class="save-btn" @click="handleSave">保存</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import request from '@/utils/request.js'

const isEdit = ref(false)
const regionStr = ref('') // 简单的省市区输入，后续优化为选择器
const form = reactive({
  id: null,
  name: '',
  phone: '',
  province: '',
  city: '',
  region: '',
  detailAddress: '',
  defaultStatus: 0
})

onLoad((options) => {
  if (options.type === 'edit') {
    isEdit.value = true
    const data = uni.getStorageSync('editAddress')
    if (data) {
      Object.assign(form, data)
      regionStr.value = `${data.province || ''} ${data.city || ''} ${data.region || ''}`.trim()
    }
  }
})

const handleDefaultChange = (e) => {
  form.defaultStatus = e.detail.value ? 1 : 0
}

const parseRegion = () => {
  // 简单的空格分割解析，实际应使用选择器
  const parts = regionStr.value.split(' ')
  if (parts.length >= 1) form.province = parts[0]
  if (parts.length >= 2) form.city = parts[1]
  if (parts.length >= 3) form.region = parts[2]
}

const handleSave = async () => {
  // 1. 校验
  if (!form.name) return uni.showToast({ title: '请输入姓名', icon: 'none' })
  if (!form.phone || form.phone.length !== 11) return uni.showToast({ title: '请输入正确手机号', icon: 'none' })
  if (!regionStr.value) return uni.showToast({ title: '请输入省市区', icon: 'none' })
  if (!form.detailAddress) return uni.showToast({ title: '请输入详细地址', icon: 'none' })

  parseRegion()

  try {
    const url = isEdit.value ? '/app/address/update' : '/app/address/add'
    await request({
      url: url,
      method: 'POST',
      data: form
    })
    
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1000)
    
  } catch (e) {
    uni.showToast({ title: e.message || '保存失败', icon: 'none' })
  }
}

const handleDelete = () => {
  uni.showModal({
    title: '提示',
    content: '确定要删除该地址吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await request({
            url: `/app/address/delete/${form.id}`,
            method: 'POST'
          })
          uni.showToast({ title: '删除成功' })
          setTimeout(() => uni.navigateBack(), 1000)
        } catch (e) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.address-edit-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-top: 20rpx;
}

.form-container {
  background-color: white;
  padding: 0 32rpx;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 32rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.form-item:last-child {
  border-bottom: none;
}

.form-item.align-top {
  align-items: flex-start;
}

.form-item.switch-item {
  justify-content: space-between;
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
}

.textarea {
  flex: 1;
  height: 120rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.5;
}

.placeholder {
  color: #ccc;
}

.delete-btn {
  margin-top: 24rpx;
  background-color: white;
  color: #ff4d4f;
  text-align: center;
  padding: 32rpx;
  font-size: 28rpx;
}

.footer-btn {
  margin-top: 60rpx;
  padding: 0 32rpx;
}

.save-btn {
  background-color: #6f4e37;
  color: white;
  border-radius: 50rpx;
  font-size: 32rpx;
  height: 88rpx;
  line-height: 88rpx;
}
.save-btn:active { opacity: 0.9; }
</style>