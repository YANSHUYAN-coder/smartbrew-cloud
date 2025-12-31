<template>
  <view class="address-edit-page">
    <view class="form-container">
      <!-- 顶部状态栏占位 -->
      <view class="status-placeholder"></view>

      <view class="form-item">
        <text class="label">联系人</text>
        <input class="input" type="text" v-model="form.name" placeholder="请输入姓名" placeholder-class="placeholder" />
      </view>
      <view class="form-item">
        <text class="label">手机号</text>
        <input class="input" type="number" v-model="form.phone" maxlength="11" placeholder="请输入手机号"
          placeholder-class="placeholder" />
      </view>
      <view class="form-item">
        <text class="label">所在地区</text>
        <picker 
          mode="multiSelector" 
          :range="multiArray" 
          range-key="label"
          :value="multiIndex"
          @change="handleRegionChange"
          @columnchange="handleColumnChange"
          class="picker-box"
        >
          <view class="picker-value" :class="{ empty: !regionStr }">
            {{ regionStr || '请选择省市区' }}
            <uni-icons type="right" size="14" color="#ccc" class="arrow-icon"></uni-icons>
          </view>
        </picker>
      </view>
      <view class="form-item align-top">
        <text class="label">详细地址</text>
        <view class="textarea-container">
          <textarea class="textarea" v-model="form.detailAddress" placeholder="街道、楼牌号等" placeholder-class="placeholder" />
          <view class="map-btn" @click="chooseLocation">
            <uni-icons type="location-filled" size="20" color="#6f4e37"></uni-icons>
            <text class="map-btn-text">地图选择</text>
          </view>
        </view>
      </view>
    </view>

    <view class="form-container" style="margin-top: 24rpx;">
      <view class="form-item switch-item">
        <text class="label">设为默认地址</text>
        <switch :checked="form.defaultStatus === 1" color="#6f4e37" @change="handleDefaultChange"
          style="transform: scale(0.8)" />
      </view>
    </view>

    <view class="delete-btn" v-if="isEdit" @click="handleDelete">删除收货地址</view>

    <view class="footer-btn">
      <button class="save-btn" @click="handleSave">保存</button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { request } from '@/utils/request.js'
import { getRegions, regeo } from '@/services/common.js'

const isEdit = ref(false)
const regionStr = ref('')
const regionData = ref([])
const multiArray = ref([[], [], []])
const multiIndex = ref([0, 0, 0])
let isUnmounted = false

onUnmounted(() => {
  isUnmounted = true
})

const form = reactive({
  id: null,
  name: '',
  phone: '',
  province: '',
  city: '',
  region: '',
  detailAddress: '',
  defaultStatus: 0,
  longitude: null,
  latitude: null
})

// 唤起地图选择位置
const chooseLocation = () => {
  uni.chooseLocation({
    success: async (res) => {
      // res 包含：name(地点名), address(详细地址), longitude(经度), latitude(纬度)
      form.detailAddress = res.name || res.address
      form.longitude = res.longitude
      form.latitude = res.latitude
      
      // 调用逆地理编码接口，自动填充省市区
      try {
        const locationStr = `${res.longitude},${res.latitude}`
        const regeoRes = await regeo(locationStr)
        
        // 【安全检查】如果页面已经关闭，不再更新状态
        if (isUnmounted) return

        // 兼容不同的数据返回格式
        const component = regeoRes.data || regeoRes
        
        if (component) {
          form.province = component.province || ''
          form.city = typeof component.city === 'string' ? component.city : (component.province || '')
          form.region = component.district || ''
          regionStr.value = `${form.province} ${form.city} ${form.region}`.trim()
          
          uni.showToast({ title: '已同步省市区', icon: 'none' })
        }
      } catch (e) {
        console.error('逆地理编码失败', e)
      }
    },
    fail: (err) => {
      console.log('地图选择失败', err)
      if (err.errMsg && (err.errMsg.includes('auth deny') || err.errMsg.includes('auth fail'))) {
        uni.showModal({
          title: '提示',
          content: '请在系统设置中开启位置信息权限',
          showCancel: false
        })
      }
    }
  })
}

// 加载地区数据
const loadRegionsData = async () => {
  try {
    const res = await getRegions()
    if (res && res.length > 0) {
      regionData.value = res
      // 初始化三级联动数据
      updateMultiArray(0, 0, 0)
    }
  } catch (e) {
    console.error('获取地区数据失败', e)
  }
}

// 更新多列选择器数据
const updateMultiArray = (pIdx, cIdx, dIdx) => {
  const provinces = regionData.value
  const cities = provinces[pIdx]?.children || []
  const districts = cities[cIdx]?.children || []
  
  multiArray.value = [
    provinces,
    cities,
    districts
  ]
}

// 列变化触发
const handleColumnChange = (e) => {
  const { column, value } = e.detail
  multiIndex.value[column] = value
  
  if (column === 0) {
    // 省份变化，重置市、区为 0
    multiIndex.value[1] = 0
    multiIndex.value[2] = 0
    updateMultiArray(value, 0, 0)
  } else if (column === 1) {
    // 城市变化，重置区为 0
    multiIndex.value[2] = 0
    updateMultiArray(multiIndex.value[0], value, 0)
  }
}

// confirm 选择
const handleRegionChange = (e) => {
  const indices = e.detail.value
  const p = multiArray.value[0][indices[0]]
  const c = multiArray.value[1][indices[1]]
  const d = multiArray.value[2][indices[2]]
  
  if (p && c) {
    form.province = p.label
    form.city = c.label
    form.region = d ? d.label : ''
    regionStr.value = `${form.province} ${form.city} ${form.region}`.trim()
  }
}

onLoad((options) => {
  loadRegionsData()
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

const handleSave = async () => {
  // 1. 校验
  if (!form.name) return uni.showToast({ title: '请输入姓名', icon: 'none' })
  if (!form.phone || form.phone.length !== 11) return uni.showToast({ title: '请输入正确手机号', icon: 'none' })
  if (!regionStr.value) return uni.showToast({ title: '请输入省市区', icon: 'none' })
  if (!form.detailAddress) return uni.showToast({ title: '请输入详细地址', icon: 'none' })

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

/* 顶部状态栏占位 */
.status-placeholder {
  height: var(--status-bar-height);
  width: 100%;
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

.picker-box {
  flex: 1;
  cursor: pointer;
}

.picker-value {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  font-size: 28rpx;
  color: #333;
  transition: color 0.2s;

  &.empty {
    color: #ccc;
  }
}

.arrow-icon {
  margin-left: 12rpx;
  transition: transform 0.2s;
}

.textarea-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.textarea {
  width: 100%;
  height: 120rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.5;
  margin-bottom: 16rpx;
}

.map-btn {
  display: flex;
  align-items: center;
  align-self: flex-end;
  padding: 12rpx 24rpx;
  background-color: #fdf6ec;
  border-radius: 30rpx;
  border: 1rpx solid #faecd8;
}

.map-btn-text {
  font-size: 24rpx;
  color: #6f4e37;
  margin-left: 8rpx;
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

.save-btn:active {
  opacity: 0.9;
}
</style>