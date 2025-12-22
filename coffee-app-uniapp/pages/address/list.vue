<template>
  <view class="address-list-page">
    <!-- 顶部状态栏占位 -->
    <view class="status-placeholder"></view>
    
    <!-- 列表区域 -->
    <scroll-view 
      scroll-y 
      class="list-scroll"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @refresherrestore="onRefreshRestore">
      <view class="address-list" v-if="addressList.length > 0">
        <view 
          v-for="(item, index) in addressList" 
          :key="item.id"
          class="address-item"
          @click="selectAddress(item)"
        >
          <view class="info-left">
            <view class="user-row">
              <text class="name">{{ item.name }}</text>
              <text class="phone">{{ item.phone }}</text>
              <text class="tag default" v-if="item.defaultStatus === 1">默认</text>
              <text class="tag label" v-if="item.label">{{ item.label }}</text>
            </view>
            <view class="address-text">
              {{ item.province }}{{ item.city }}{{ item.region }} {{ item.detailAddress }}
            </view>
          </view>
          <view class="edit-icon" @click.stop="editAddress(item)">
            <uni-icons type="compose" size="24" color="#999"></uni-icons>
          </view>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view class="empty-state" v-else>
        <image src="https://img.icons8.com/bubbles/200/map-marker.png" class="empty-img" />
        <text class="empty-text">暂无收货地址</text>
      </view>
      
      <view style="height: 120rpx;"></view> <!-- 底部垫高 -->
    </scroll-view>

    <!-- 底部按钮 -->
    <view class="footer-btn">
      <button class="add-btn" @click="addAddress">
        <uni-icons type="plusempty" size="20" color="#fff" style="margin-right: 10rpx;"></uni-icons>
        新增收货地址
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {request} from '@/utils/request.js'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'

const addressList = ref([])
const refreshing = ref(false) // 下拉刷新状态
const isSelectMode = ref(false) // 是否为选择模式

// 获取地址列表
const fetchList = async () => {
  try {
    const res = await request({
      url: '/app/address/list',
      method: 'GET'
    })
    addressList.value = res.data || res
  } catch (e) {
    console.error(e)
  }
}

// 新增地址
const addAddress = () => {
  uni.navigateTo({ url: '/pages/address/edit' })
}

// 编辑地址
const editAddress = (item) => {
  // 传递对象需要编码，或者只传ID去下个页面查
  // 这里简单起见，把 item 存到本地缓存或者全局变量里，下个页面去取
  uni.setStorageSync('editAddress', item)
  uni.navigateTo({ url: '/pages/address/edit?type=edit' })
}

// 选择地址 (如果是从确认订单页跳过来的)
const selectAddress = (item) => {
  if (isSelectMode.value) {
    // 选择模式：保存选中的地址并返回
    uni.setStorageSync('selectedAddress', item)
    uni.navigateBack()
  } else {
    // 非选择模式：可以编辑或查看详情
    editAddress(item)
  }
}

// 下拉刷新
const onRefresh = async () => {
  refreshing.value = true
  await fetchList()
  setTimeout(() => {
    refreshing.value = false
  }, 300)
}

// 刷新恢复
const onRefreshRestore = () => {
  refreshing.value = false
}

// 页面下拉刷新（如果 scroll-view 的下拉刷新不工作，可以使用这个）
onPullDownRefresh(async () => {
  await fetchList()
  uni.stopPullDownRefresh()
})

// 每次页面显示时刷新列表
onShow(() => {
  fetchList()
})

// 页面加载时检查是否为选择模式
onMounted(() => {
  // 检查 URL 参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || {}
  if (options.select === 'true') {
    isSelectMode.value = true
  }
})
</script>

<style lang="scss" scoped>
.address-list-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

/* 顶部状态栏占位 */
.status-placeholder {
  height: var(--status-bar-height);
  width: 100%;
}

.list-scroll {
  flex: 1;
  height: 0; /* flex 自适应关键 */
}

.address-list {
  padding: 24rpx;
}

.address-item {
  background-color: white;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.02);
}

.info-left {
  flex: 1;
  margin-right: 32rpx;
}

.user-row {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-right: 16rpx;
}

.phone {
  font-size: 28rpx;
  color: #999;
  margin-right: 16rpx;
}

.tag {
  font-size: 20rpx;
  padding: 2rpx 8rpx;
  border-radius: 6rpx;
  margin-right: 8rpx;
}

.tag.default {
  background-color: #6f4e37;
  color: white;
}

.tag.label {
  background-color: #f0f0f0;
  color: #666;
}

.address-text {
  font-size: 26rpx;
  color: #666;
  line-height: 1.4;
}

.edit-icon {
  padding: 10rpx;
  border-left: 1rpx solid #eee;
  padding-left: 24rpx;
}

/* 空状态 */
.empty-state {
  padding-top: 200rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.empty-img { width: 200rpx; height: 200rpx; opacity: 0.5; margin-bottom: 20rpx; }
.empty-text { font-size: 28rpx; color: #999; }

/* 底部按钮 */
.footer-btn {
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  background-color: white;
  box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.05);
}

.add-btn {
  background-color: #6f4e37;
  color: white;
  border-radius: 50rpx;
  font-size: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  border: none;
}
.add-btn:active { opacity: 0.9; }
</style>