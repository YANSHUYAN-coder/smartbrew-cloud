<template>
    <view class="coupon-page">
        <!-- 顶部导航栏 -->
        <view class="tab-header" :style="{ paddingTop: statusBarHeight + 'px' }">
            <view v-for="(tab, index) in tabs" :key="index" class="tab-item" :class="{ active: currentTab === index }"
                @click="switchTab(index)">
                <text class="tab-text">{{ tab.name }}</text>
                <view class="tab-line" v-if="currentTab === index"></view>
            </view>
        </view>

        <!-- 优惠券列表 -->
        <view class="coupon-list-container">
            <view class="coupon-list">
                <view v-for="coupon in couponList" :key="coupon.id" class="coupon-item"
                    :class="{ disabled: currentTab !== 0 }">
                    <view class="coupon-left">
                        <view class="coupon-amount" :class="{ 'amount-disabled': currentTab !== 0 }">
                            <text class="symbol">¥</text>
                            <text class="value">{{ coupon.amount || 0 }}</text>
                        </view>
                        <view class="coupon-info">
                            <text class="coupon-name" :class="{ 'text-disabled': currentTab !== 0 }">
                                {{ coupon.couponName || '优惠券' }}
                            </text>
                            <text class="coupon-desc" v-if="coupon.note">{{ coupon.note }}</text>
                            <text class="coupon-code">券码：{{ coupon.couponCode }}</text>
                            <text class="coupon-time">有效期至：{{ formatDate(coupon.expireTime || coupon.endTime || coupon.startTime) }}</text>
                        </view>
                    </view>
                    <view class="coupon-right">
                        <view class="status-badge" :class="getStatusClass(coupon.useStatus)">
                            {{ getStatusTextByStatus(coupon.useStatus) }}
                        </view>
                        <button class="use-btn" v-if="currentTab === 0 && coupon.useStatus === 0"
                            @click="handleUse">去使用</button>
                    </view>
                </view>

                <!-- 加载状态 -->
                <view v-if="loading" class="loading-more">
                    <text>加载中...</text>
                </view>

                <!-- 空状态 -->
                <view v-if="!loading && couponList.length === 0" class="empty-state">
                    <text class="empty-icon">🎟️</text>
                    <text class="empty-text">暂无{{ getStatusText(currentTab) }}优惠券</text>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getMyCoupons } from '@/services/promotion.js'
import { getStatusBarHeight } from '@/utils/system.js'
import { formatDate } from '@/utils/date.js'
const statusBarHeight = ref(0)
const currentTab = ref(0)
const tabs = [
    { name: '未使用', status: 0 },
    { name: '已使用', status: 1 },
    { name: '已过期', status: 2 }
]

const couponList = ref([])
const loading = ref(false)

onMounted(() => {
    statusBarHeight.value = getStatusBarHeight()
    loadCoupons()
})

onPullDownRefresh(() => {
    loadCoupons()
})

const switchTab = (index) => {
    if (currentTab.value === index) return
    currentTab.value = index
    couponList.value = []
    loadCoupons()
}

const loadCoupons = async () => {
    loading.value = true
    try {
        const status = tabs[currentTab.value].status
        const res = await getMyCoupons(status)
        // 兼容后端返回格式
        const list = res.data || res || []

        couponList.value = list.map(item => ({
            ...item,
            // 后端返回的字段名是 coupon_name，需要映射为 couponName
            couponName: item.couponName || item.coupon_name || '优惠券',
            amount: item.amount || 0
        }))

    } catch (e) {
        console.error('加载优惠券失败', e)
        uni.showToast({ title: '加载失败', icon: 'none' })
    } finally {
        loading.value = false
        uni.stopPullDownRefresh()
    }
}

const handleUse = () => {
    uni.switchTab({
        url: '/pages/menu/index'
    })
}

// formatDate 已从工具类导入

const getStatusText = (tabIndex) => {
    return tabs[tabIndex].name
}

const getStatusTextByStatus = (useStatus) => {
    if (useStatus === 0) return '未使用'
    if (useStatus === 1) return '已使用'
    return '已过期'
}

const getStatusClass = (useStatus) => {
    if (useStatus === 0) return 'status-active'
    if (useStatus === 1) return 'status-used'
    return 'status-expired'
}
</script>

<style lang="scss" scoped>
.coupon-page {
    min-height: 100vh;
    background-color: #f5f5f5;
    display: flex;
    flex-direction: column;
}

.tab-header {
    display: flex;
    background-color: white;
    padding: 0 32rpx;
    position: sticky;
    top: 0;
    z-index: 10;
}

.tab-item {
    flex: 1;
    height: 88rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    position: relative;

    &.active {
        .tab-text {
            color: #333;
            font-weight: bold;
        }
    }
}

.tab-text {
    font-size: 28rpx;
    color: #999;
    transition: all 0.2s;
}

.tab-line {
    position: absolute;
    bottom: 0;
    width: 40rpx;
    height: 4rpx;
    background-color: #6f4e37;
    border-radius: 2rpx;
}

.coupon-list-container {
    padding: 24rpx 32rpx;
    flex: 1;
}

.coupon-list {
    display: flex;
    flex-direction: column;
    gap: 24rpx;
}

.coupon-item {
    background-color: white;
    border-radius: 16rpx;
    display: flex;
    overflow: hidden;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
    min-height: 180rpx;

    &.disabled {
        background-color: #fafafa;

        .coupon-left {
            border-right-color: #e0e0e0;
        }
    }
}

.coupon-left {
    flex: 1;
    padding: 24rpx;
    display: flex;
    align-items: center;
    border-right: 2rpx dashed #eee;
    position: relative;

    &::after,
    &::before {
        content: '';
        position: absolute;
        right: -12rpx;
        width: 24rpx;
        height: 24rpx;
        border-radius: 50%;
        background-color: #f5f5f5;
    }

    &::after {
        top: -12rpx;
    }

    &::before {
        bottom: -12rpx;
    }
}

.coupon-amount {
    margin-right: 24rpx;
    color: #e64340;
    display: flex;
    align-items: baseline;
    min-width: 100rpx;
    justify-content: center;

    &.amount-disabled {
        color: #999;
    }

    .symbol {
        font-size: 24rpx;
    }

    .value {
        font-size: 56rpx;
        font-weight: bold;
        line-height: 1;
    }
}

.coupon-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8rpx;

    .coupon-name {
        font-size: 30rpx;
        font-weight: bold;
        color: #333;

        &.text-disabled {
            color: #999;
        }
    }

    .coupon-code {
        font-size: 22rpx;
        color: #666;
        font-family: monospace;
    }

    .coupon-desc {
        font-size: 20rpx;
        color: #999;
    }

    .coupon-time {
        font-size: 20rpx;
        color: #999;
    }
}

.coupon-right {
    width: 160rpx;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    gap: 16rpx;
    padding: 16rpx 0;
}

.status-badge {
    font-size: 24rpx;
    padding: 4rpx 12rpx;
    border-radius: 8rpx;

    &.status-active {
        color: #6f4e37;
        background-color: rgba(111, 78, 55, 0.1);
    }

    &.status-used {
        color: #999;
        background-color: #f0f0f0;
    }

    &.status-expired {
        color: #ccc;
        background-color: #f5f5f5;
    }
}

.use-btn {
    margin: 0;
    padding: 0 24rpx;
    height: 48rpx;
    line-height: 48rpx;
    font-size: 24rpx;
    background-color: #6f4e37;
    color: white;
    border-radius: 24rpx;

    &::after {
        border: none;
    }
}

.empty-state {
    padding: 100rpx 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 24rpx;

    .empty-icon {
        font-size: 80rpx;
        opacity: 0.5;
    }

    .empty-text {
        color: #999;
        font-size: 28rpx;
    }
}

.loading-more {
    text-align: center;
    padding: 24rpx;
    color: #999;
    font-size: 24rpx;
}
</style>
