<template>
  <view class="store-list-page" :class="themeClass">
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-back" @click="goBack">
        <uni-icons type="left" size="24" color="#333"></uni-icons>
      </view>
      <text class="page-title">选择门店</text>
      <view style="width: 48rpx;"></view>
    </view>

    <scroll-view 
      scroll-y 
      class="list-scroll"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="store-list" v-if="stores.length > 0">
        <view 
          v-for="store in stores" 
          :key="store.id" 
          class="store-item click-active"
          :class="{ active: currentStoreId === store.id }"
          @click="selectStore(store)"
        >
          <view class="store-info">
            <view class="name-row">
              <text class="store-name">{{ store.name }}</text>
              <text class="status-tag" :class="store.openStatus === 1 ? 'open' : 'closed'">
                {{ store.openStatus === 1 ? '营业中' : '休息中' }}
              </text>
            </view>
            <view class="address-row">
              <text class="store-address">{{ store.address }}</text>
              <text class="distance-text" v-if="store.distanceText">{{ store.distanceText }}</text>
            </view>
            <view class="bottom-row">
              <view class="info-item">
                <uni-icons type="phone" size="14" color="#999"></uni-icons>
                <text class="info-text">{{ store.phone }}</text>
              </view>
              <view class="info-item">
                <uni-icons type="location" size="14" color="#999"></uni-icons>
                <text class="info-text">{{ store.businessHours }}</text>
              </view>
            </view>
          </view>
          <view class="select-icon" v-if="currentStoreId === store.id">
            <uni-icons type="checkmarkempty" size="20" color="#6f4e37"></uni-icons>
          </view>
        </view>
      </view>
      <view class="empty-state" v-else>
        <text class="empty-text">暂无门店信息</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getStoreList } from '@/services/store.js'
import { getDistance } from '@/services/common.js'
import { useAppStore } from '@/store/app.js'
import { useUserStore } from '@/store/user.js'
import { getStatusBarHeight } from '@/utils/system.js'

const stores = ref([])
const statusBarHeight = ref(0)
const refreshing = ref(false)
const appStore = useAppStore()
const userStore = useUserStore()

const currentStoreId = computed(() => appStore.currentStore?.id)
const themeClass = computed(() => userStore.isDarkMode ? 'theme-dark' : 'theme-light')

const fetchStores = async () => {
  try {
    // 1. 获取用户定位
    let userCoords = null
    try {
      const location = await new Promise((resolve, reject) => {
        uni.getLocation({
          type: 'gcj02',
          success: resolve,
          fail: reject
        })
      })
      userCoords = { longitude: location.longitude, latitude: location.latitude }
    } catch (e) {
      console.warn('门店列表页获取定位失败')
    }

    // 2. 获取按距离排序的门店列表
    const res = await getStoreList(userCoords)
    const storesData = res.data || res || []
    
    // 3. 为列表中的每个门店计算并附加距离展示文字（复用后端的 distance 逻辑或前端计算）
    // 后端 listNearby 已经返回了排序好的列表，我们只需要补充前端展示用的距离文字
    if (userCoords) {
      for (const s of storesData) {
        // 这里可以调用后端的 distance 接口获取格式化文字，或者前端简单计算
        // 为了性能，我们这里在前端根据后端排序后的结果，异步补充距离文本
        try {
          const distanceRes = await getDistance(`${userCoords.longitude},${userCoords.latitude}`, s.id)
          const dData = distanceRes.data || distanceRes
          s.distanceText = dData.distanceText
        } catch (e) {}
      }
    }

    stores.value = storesData
    
    // 如果当前选中的门店在列表中，同步更新它的最新状态到全局 store
    if (appStore.currentStore) {
      const latestStore = stores.value.find(s => s.id === appStore.currentStore.id)
      if (latestStore) {
        appStore.setStore(latestStore)
      }
    }
  } catch (e) {
    console.error('获取门店列表失败', e)
  }
}

const onRefresh = async () => {
  refreshing.value = true
  await fetchStores()
  setTimeout(() => {
    refreshing.value = false
  }, 300)
}

// 同时支持页面级的下拉刷新
onPullDownRefresh(async () => {
  await fetchStores()
  uni.stopPullDownRefresh()
})

const selectStore = (store) => {
  if (store.openStatus === 0) {
    uni.showToast({ title: '该门店休息中', icon: 'none' })
    return
  }
  appStore.setStore(store)
  uni.navigateBack()
}

const goBack = () => uni.navigateBack()

onMounted(() => {
  statusBarHeight.value = getStatusBarHeight()
  fetchStores()
})
</script>

<style lang="scss" scoped>
.store-list-page {
  min-height: 100vh;
  background-color: var(--bg-secondary);
  display: flex;
  flex-direction: column;
}

.nav-bar {
  background-color: var(--bg-primary);
  padding-bottom: 20rpx;
  padding-left: 32rpx;
  padding-right: 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2rpx 10rpx var(--shadow-color);
}

.page-title {
  font-size: 32rpx;
  font-weight: bold;
  color: var(--text-primary);
}

.list-scroll {
  flex: 1;
}

.store-list {
  padding: 24rpx;
}

.store-item {
  background-color: var(--bg-primary);
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 4rpx 16rpx var(--shadow-color);
  border: 2rpx solid transparent;
  
  &.active {
    border-color: #6f4e37;
    background-color: var(--bg-tertiary);
  }
}

.store-info {
  flex: 1;
  min-width: 0; // 核心修复：允许子元素在 Flex 布局中收缩以触发省略号
}

.name-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.store-name {
  font-size: 32rpx;
  font-weight: bold;
  color: var(--text-primary);
}

.status-tag {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  
  &.open {
    background-color: #e6f7ff;
    color: #1890ff;
  }
  
  &.closed {
    background-color: #f5f5f5;
    color: #999;
  }
}

.store-address {
  font-size: 26rpx;
  color: var(--text-secondary);
  flex: 1;
  display: block; // 确保是块级显示
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.address-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
  gap: 20rpx;
}

.distance-text {
  font-size: 24rpx;
  color: #6f4e37;
  font-weight: 500;
  flex-shrink: 0;
}

.bottom-row {
  display: flex;
  gap: 24rpx;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.info-text {
  font-size: 24rpx;
  color: var(--text-tertiary);
}

.empty-state {
  padding-top: 200rpx;
  text-align: center;
  color: var(--text-tertiary);
}
</style>

