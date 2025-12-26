<template>
    <view class="history-page">
        <!-- 沉浸式背景 -->
        <view class="page-bg"></view>

        <!-- 自定义导航栏 -->
        <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
            <view class="nav-content">
                <view class="nav-left"></view>
                <view class="nav-title">积分明细</view>
                <view class="nav-right"></view>
            </view>
        </view>

        <!-- 统计/筛选区域 (可选，预留位置) -->
        <!-- <view class="filter-section"> ... </view> -->

        <!-- 列表内容 -->
        <view class="list-container" :style="{ paddingTop: (statusBarHeight + 44 + 10) + 'px' }">
            <view v-for="(item, index) in historyList" :key="item.id || index" class="history-card">
                <view class="card-left">
                    <view class="type-row">
                        <text class="type-name">{{ item.changeTypeDesc || '积分变动' }}</text>
                    </view>
                    <view class="date-row">
                        <text class="date-text">{{ formatTime(item.createTime) }}</text>
                    </view>
                    <view class="note-row" v-if="item.note">
                        <text class="note-text">{{ item.note }}</text>
                    </view>
                </view>
                
                <view class="card-right">
                    <view class="points-num" :class="item.changePoints > 0 ? 'is-add' : 'is-minus'">
                        <text class="symbol">{{ item.changePoints > 0 ? '+' : '' }}</text>
                        <text class="value">{{ item.changePoints }}</text>
                    </view>
                    <view class="balance-text" v-if="item.afterPoints !== undefined">
                        余额 {{ item.afterPoints }}
                    </view>
                </view>
            </view>

            <!-- 加载状态 -->
            <view class="loading-wrap">
                <view class="loading-spinner" v-if="loading"></view>
                <text class="loading-text" v-if="loading">加载中...</text>
                <text class="loading-text" v-else-if="!hasMore && historyList.length > 0">没有更多记录了</text>
                <text class="loading-text" v-else-if="!loading && hasMore">上拉加载更多</text>
            </view>

            <!-- 空状态 -->
            <view v-if="!loading && historyList.length === 0" class="empty-state">
                <view class="empty-icon-box">
                    <text class="empty-symbol">¥</text>
                </view>
                <text class="empty-text">暂无积分变动记录</text>
            </view>
        </view>
    </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import { getStatusBarHeight } from '@/utils/system.js'
// 确保路径正确
import { getIntegrationHistory } from '@/services/promotion.js'

const statusBarHeight = ref(0)
const historyList = ref([])
const page = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)
const loading = ref(false)

onMounted(() => {
    statusBarHeight.value = getStatusBarHeight()
    loadHistory(true)
})

onShow(() => {
    // 设置状态栏文字颜色为白色，适应深色背景
    uni.setNavigationBarColor({
        frontColor: '#ffffff',
        backgroundColor: '#000000'
    })
})

onPullDownRefresh(async () => {
    await loadHistory(true)
    uni.stopPullDownRefresh()
})

onReachBottom(() => {
    if (hasMore.value && !loading.value) {
        loadMore()
    }
})

const loadHistory = async (refresh = false) => {
    if (loading.value) return
    loading.value = true

    try {
        if (refresh) {
            page.value = 1
            hasMore.value = true
        }

        const res = await getIntegrationHistory(page.value, pageSize.value)
        const data = res.records || res.data?.records || res.data || []
        const total = res.total || 0

        if (refresh) {
            historyList.value = data
        } else {
            historyList.value = [...historyList.value, ...data]
        }

        // 判断是否还有更多
        if (data.length < pageSize.value) {
            hasMore.value = false
        }

    } catch (e) {
        console.error('加载积分明细失败', e)
        uni.showToast({ title: '加载失败', icon: 'none' })
    } finally {
        loading.value = false
    }
}

const loadMore = () => {
    if (hasMore.value && !loading.value) {
        page.value++
        loadHistory(false)
    }
}

const formatTime = (timeStr) => {
    if (!timeStr) return ''
    const date = new Date(timeStr)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hour = String(date.getHours()).padStart(2, '0')
    const minute = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hour}:${minute}`
}

</script>

<style lang="scss" scoped>
.history-page {
    min-height: 100vh;
    background-color: #f7f8fa;
    position: relative;
}

// 深色背景区域
.page-bg {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 400rpx;
    background: linear-gradient(180deg, #1a1a1a 0%, #333333 100%);
    z-index: 0;
    
    // 底部弧形遮罩，让过渡更自然
    &::after {
        content: '';
        position: absolute;
        bottom: -40rpx;
        left: 0;
        width: 100%;
        height: 80rpx;
        background: #f7f8fa;
        border-radius: 50% 50% 0 0 / 100% 100% 0 0;
        transform: scaleX(1.2);
    }
}

// 导航栏
.nav-bar {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    z-index: 100;
    
    .nav-content {
        height: 44px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 32rpx;
        color: #fff;
    }
    
    .nav-left {
        width: 60rpx;
        height: 100%;
        display: flex;
        align-items: center;
        font-size: 40rpx;
        font-weight: 300;
    }
    
    .nav-title {
        font-size: 34rpx;
        font-weight: 600;
        letter-spacing: 2rpx;
        justify-content: center;
        align-items: center;
    }
    
    .nav-right {
        width: 60rpx;
    }
}

// 列表区域
.list-container {
    position: relative;
    z-index: 1;
    padding-left: 32rpx;
    padding-right: 32rpx;
    padding-bottom: 40rpx;
}

.history-card {
    background: #fff;
    border-radius: 20rpx;
    padding: 32rpx;
    margin-bottom: 24rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.03);
    transition: transform 0.1s;
    
    &:active {
        transform: scale(0.99);
    }
}

.card-left {
    flex: 1;
    margin-right: 20rpx;
    
    .type-name {
        font-size: 30rpx;
        font-weight: bold;
        color: #333;
        margin-bottom: 8rpx;
        display: block;
    }
    
    .date-row {
        margin-bottom: 6rpx;
        
        .date-text {
            font-size: 24rpx;
            color: #999;
        }
    }
    
    .note-row {
        .note-text {
            font-size: 24rpx;
            color: #666;
            background: #f8f8f8;
            padding: 4rpx 12rpx;
            border-radius: 8rpx;
        }
    }
}

.card-right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    
    .points-num {
        font-size: 36rpx;
        font-weight: bold;
        font-family: 'DIN Alternate', sans-serif;
        
        &.is-add {
            color: #52c41a; // 绿色
        }
        
        &.is-minus {
            color: #ff4d4f; // 红色
        }
    }
    
    .balance-text {
        font-size: 22rpx;
        color: #bbb;
        margin-top: 4rpx;
    }
}

// 加载状态
.loading-wrap {
    padding: 20rpx 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 16rpx;
    
    .loading-spinner {
        width: 32rpx;
        height: 32rpx;
        border: 3rpx solid #ddd;
        border-top-color: #c69c6d;
        border-radius: 50%;
        animation: spin 0.8s linear infinite;
    }
    
    .loading-text {
        font-size: 24rpx;
        color: #999;
    }
}

@keyframes spin {
    to { transform: rotate(360deg); }
}

// 空状态
.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding-top: 200rpx;
    
    .empty-icon-box {
        width: 120rpx;
        height: 120rpx;
        background: #f0f0f0;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 24rpx;
        
        .empty-symbol {
            font-size: 60rpx;
            color: #ccc;
            font-family: serif;
        }
    }
    
    .empty-text {
        font-size: 28rpx;
        color: #999;
    }
}
</style>