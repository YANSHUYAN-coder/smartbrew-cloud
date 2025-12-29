<template>
  <view class="message-page">
    <!-- 顶部工具栏 -->
    <view class="toolbar" v-if="list.length > 0">
      <view class="read-all-btn" @click="handleReadAll">
        <uni-icons type="checkmarkempty" size="14" color="#666"></uni-icons>
        <text>全部已读</text>
      </view>
    </view>

    <!-- 消息列表 -->
    <scroll-view scroll-y class="message-list" @scrolltolower="loadMore" refresher-enabled @refresherrefresh="onRefresh" :refresher-triggered="isRefreshing">
      <view class="message-item" v-for="(item, index) in list" :key="item.id" @click="handleMessageClick(item, index)">
        
        <view class="msg-time">{{ formatTime(item.createTime) }}</view>
        
        <view class="msg-card" :class="{ 'is-read': item.isRead === 1 }">
          <view class="card-header">
            <view class="header-left">
              <!-- 根据类型显示不同图标 -->
              <view class="icon-box" :class="getIconClass(item.type)">
                <uni-icons :type="getIconType(item.type)" size="20" color="#fff"></uni-icons>
              </view>
              <text class="title">{{ item.title }}</text>
            </view>
            <view class="red-dot" v-if="item.isRead === 0"></view>
          </view>
          
          <view class="card-content">
            {{ item.content }}
          </view>
          
          <!-- 如果是订单消息且有业务ID，显示查看详情 -->
          <view class="card-footer" v-if="item.type === 1 && item.bizId">
            <text>查看详情</text>
            <uni-icons type="right" size="14" color="#999"></uni-icons>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="!loading && list.length === 0" class="empty-state">
        <image src="/static/empty-message.png" mode="widthFix" style="width: 200rpx; height: 200rpx; opacity: 0.5;" />
        <text>暂无消息通知</text>
      </view>
      
      <!-- 加载更多 -->
      <view class="loading-more" v-if="list.length > 0">
        <text>{{ loading ? '加载中...' : (hasMore ? '上拉加载更多' : '没有更多了') }}</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMessageList, markAsRead } from '@/services/message.js'
import { formatDateTime } from '@/utils/date.js'

const list = ref([])
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const hasMore = ref(true)
const isRefreshing = ref(false)

// 格式化时间
const formatTime = (time) => {
  return formatDateTime(time)
}

// 获取图标类型
const getIconType = (type) => {
  // 0-系统, 1-订单, 2-资产
  const map = { 0: 'notification-filled', 1: 'cart-filled', 2: 'wallet-filled' }
  return map[type] || 'notification-filled'
}

// 获取图标背景样式类
const getIconClass = (type) => {
  const map = { 0: 'system', 1: 'order', 2: 'asset' }
  return map[type] || 'system'
}

// 加载数据
const loadData = async (refresh = false) => {
  if (loading.value) return
  loading.value = true
  
  if (refresh) {
    page.value = 1
    hasMore.value = true
  }

  try {
    const res = await getMessageList({
      page: page.value,
      pageSize: pageSize.value
    })
    
    if (refresh) {
      list.value = res.records
    } else {
      list.value = [...list.value, ...res.records]
    }
    
    hasMore.value = list.value.length < res.total
    if (hasMore.value) {
      page.value++
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    isRefreshing.value = false
  }
}

// 下拉刷新
const onRefresh = () => {
  isRefreshing.value = true
  loadData(true)
}

// 上拉加载
const loadMore = () => {
  if (hasMore.value && !loading.value) {
    loadData()
  }
}

// 点击消息
const handleMessageClick = async (item, index) => {
  // 1. 标记已读
  if (item.isRead === 0) {
    try {
      await markAsRead({ id: item.id })
      list.value[index].isRead = 1
      // 触发全局事件更新角标（如果有）
      uni.$emit('messageRead') 
    } catch (e) {
      console.error(e)
    }
  }

  // 2. 业务跳转
  if (item.type === 1 && item.bizId) {
    // 订单消息跳转详情
    uni.navigateTo({
      url: `/pages/order/detail?id=${item.bizId}`
    })
  }
}

// 全部已读
const handleReadAll = () => {
  uni.showModal({
    title: '提示',
    content: '确定将所有消息标记为已读吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await markAsRead({}) // 不传ID代表全部已读
          // 本地更新状态
          list.value.forEach(item => item.isRead = 1)
          uni.showToast({ title: '操作成功', icon: 'none' })
          uni.$emit('messageRead')
        } catch (e) {
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}

onMounted(() => {
  loadData(true)
})
</script>

<style lang="scss" scoped>
.message-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.toolbar {
  padding: 20rpx 32rpx;
  display: flex;
  justify-content: flex-end;
  background-color: #fff;
}

.read-all-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 26rpx;
  color: #666;
  padding: 8rpx 16rpx;
  background-color: #f8f8f8;
  border-radius: 24rpx;
}

.message-list {
  flex: 1;
  overflow: hidden;
  padding: 20rpx 32rpx;
  box-sizing: border-box;
}

.message-item {
  margin-bottom: 40rpx;
  
  .msg-time {
    text-align: center;
    font-size: 24rpx;
    color: #999;
    margin-bottom: 20rpx;
  }
}

.msg-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
  transition: opacity 0.3s;
  
  &.is-read {
    opacity: 0.8;
    .title { color: #666; }
    .icon-box { filter: grayscale(0.8); }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 20rpx;
  }
  
  .icon-box {
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &.system { background: linear-gradient(135deg, #4facfe, #00f2fe); }
    &.order { background: linear-gradient(135deg, #ff9a9e, #fecfef); }
    &.asset { background: linear-gradient(135deg, #f6d365, #fda085); }
  }
  
  .title {
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
  }
  
  .red-dot {
    width: 16rpx;
    height: 16rpx;
    background-color: #ff4d4f;
    border-radius: 50%;
  }
}

.card-content {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
  margin-bottom: 20rpx;
  padding-left: 92rpx; // 对齐图标右侧
}

.card-footer {
  border-top: 1rpx solid #f0f0f0;
  padding-top: 20rpx;
  margin-top: 20rpx;
  padding-left: 92rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 26rpx;
  color: #666;
}

.loading-more {
  text-align: center;
  padding: 30rpx 0;
  color: #999;
  font-size: 24rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 200rpx;
  
  text {
    font-size: 28rpx;
    color: #999;
    margin-top: 20rpx;
  }
}
</style>