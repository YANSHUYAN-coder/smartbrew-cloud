<template>
    <view class="gift-page">
        <!-- 顶部导航 -->
        <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
            <view class="nav-left" @click="goBack">
                <uni-icons type="left" size="20" color="#333" />
            </view>
            <text class="nav-title">礼品卡</text>
            <view class="nav-right" />
        </view>

        <!-- 内容区域 -->
        <scroll-view scroll-y class="content-scroll">
            <!-- 顶部简介卡片 -->
            <view class="intro-card">
                <view class="intro-texts">
                    <text class="intro-title">用一杯好咖啡，说一声谢谢</text>
                    <text class="intro-subtitle">购买礼品卡，送给朋友或自己使用，支持余额多次消费。</text>
                </view>
                <view class="intro-badge">
                    <text class="badge-text">BETA</text>
                </view>
            </view>

            <!-- 礼品卡列表（mock 数据） -->
            <view v-if="cards.length" class="card-list">
                <view v-for="card in cards" :key="card.id" class="gift-card-item">
                    <view class="card-top">
                        <view class="card-left">
                            <text class="card-name">{{ card.name }}</text>
                            <text class="card-no">NO. {{ card.cardNo }}</text>
                        </view>
                        <view class="card-right">
                            <text class="currency">¥</text>
                            <text class="amount">{{ card.balance.toFixed(2) }}</text>
                        </view>
                    </view>
                    <view class="card-bottom">
                        <view class="card-meta">
                            <text class="meta-item">面值 ¥{{ card.originalAmount }}</text>
                            <text class="meta-item">有效期至 {{ card.expireDate }}</text>
                        </view>
                        <view class="card-status" :class="`status-${card.status}`">
                            {{ getStatusText(card.status) }}
                        </view>
                    </view>
                </view>
            </view>

            <!-- 空状态 -->
            <view v-else class="empty-box">
                <text class="empty-icon">🎁</text>
                <text class="empty-text">还没有礼品卡</text>
                <text class="empty-subtext">送一杯咖啡给自己或朋友，从一张礼品卡开始</text>
            </view>

            <!-- 预留底部空间 -->
            <view style="height: 160rpx" />
        </scroll-view>

        <!-- 底部操作栏 -->
        <view class="footer-bar">
            <button class="buy-btn" @click="handleBuy">
                立即购卡
            </button>
        </view>
    </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStatusBarHeight } from '@/utils/system.js'
const cards = ref([])
const statusBarHeight = ref(0)

// mock 数据：后续可替换为真实接口
const loadMockCards = () => {
    cards.value = [
        {
            id: 1,
            name: '智咖云 · 咖啡礼遇卡',
            cardNo: 'GC20251223001',
            balance: 120,
            originalAmount: 200,
            expireDate: '2026-12-31',
            status: 'active',
        },
        {
            id: 2,
            name: '节日限定 · 暖心卡',
            cardNo: 'GC20251111008',
            balance: 0,
            originalAmount: 100,
            expireDate: '2025-11-30',
            status: 'used',
        },
    ]
}

const getStatusText = (status) => {
    const map = {
        active: '可用',
        used: '已用完',
        expired: '已过期',
    }
    return map[status] || '未知'
}

const goBack = () => {
    uni.navigateBack()
}

const handleBuy = () => {
    uni.showToast({
        title: '购卡流程待接入',
        icon: 'none',
    })
}

onMounted(() => {
    statusBarHeight.value = getStatusBarHeight()
    loadMockCards()
})
</script>

<style scoped lang="scss">
$primary: #6f4e37;
$bg-color: #f7f8fa;

.gift-page {
    min-height: 100vh;
    background-color: $bg-color;
    display: flex;
    flex-direction: column;
}

.nav-bar {
    height: 88rpx;
    padding: 0 32rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    background-color: #ffffff;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.03);
}

.nav-left,
.nav-right {
    width: 80rpx;
    display: flex;
    align-items: center;
}

.nav-title {
    font-size: 34rpx;
    font-weight: 600;
    color: #333333;
}

.content-scroll {
    flex: 1;
    padding: 24rpx 32rpx 0;
    box-sizing: border-box;
}

.intro-card {
    background: #ffffff;
    border-radius: 24rpx;
    padding: 28rpx 24rpx;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.intro-texts {
    flex: 1;
    padding-right: 16rpx;
}

.intro-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #333333;
    margin-bottom: 8rpx;
}

.intro-subtitle {
    font-size: 24rpx;
    color: #888888;
    line-height: 1.5;
}

.intro-badge {
    background: rgba(111, 78, 55, 0.08);
    border-radius: 999rpx;
    padding: 8rpx 18rpx;
}

.badge-text {
    font-size: 22rpx;
    color: $primary;
    letter-spacing: 2rpx;
}

.card-list {
    display: flex;
    flex-direction: column;
    gap: 24rpx;
}

.gift-card-item {
    background: linear-gradient(135deg, #3c2a21 0%, #6f4e37 60%, #a47148 100%);
    border-radius: 24rpx;
    padding: 28rpx 24rpx;
    color: #ffffff;
    box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.25);
}

.card-top {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24rpx;
}

.card-left {
    display: flex;
    flex-direction: column;
}

.card-name {
    font-size: 30rpx;
    font-weight: 600;
    margin-bottom: 8rpx;
}

.card-no {
    font-size: 22rpx;
    opacity: 0.8;
}

.card-right {
    display: flex;
    align-items: flex-end;
}

.currency {
    font-size: 28rpx;
    margin-right: 4rpx;
}

.amount {
    font-size: 42rpx;
    font-weight: 700;
}

.card-bottom {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.card-meta {
    display: flex;
    flex-direction: column;
    gap: 4rpx;
    font-size: 22rpx;
    opacity: 0.9;
}

.meta-item {
    font-size: 22rpx;
}

.card-status {
    font-size: 22rpx;
    padding: 6rpx 16rpx;
    border-radius: 999rpx;
    background-color: rgba(255, 255, 255, 0.2);
}

.card-status.status-active {
    background-color: rgba(82, 196, 26, 0.25);
}

.card-status.status-used {
    background-color: rgba(120, 120, 120, 0.35);
}

.card-status.status-expired {
    background-color: rgba(245, 108, 108, 0.35);
}

.empty-box {
    margin-top: 120rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    color: #999999;
}

.empty-icon {
    font-size: 72rpx;
    margin-bottom: 16rpx;
}

.empty-text {
    font-size: 28rpx;
    margin-bottom: 8rpx;
}

.empty-subtext {
    font-size: 24rpx;
    color: #bbbbbb;
}

.footer-bar {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    padding: 16rpx 32rpx;
    padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
    background-color: #ffffff;
    box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.buy-btn {
    height: 88rpx;
    background-color: $primary;
    color: #ffffff;
    border-radius: 999rpx;
    font-size: 30rpx;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
    border: none;
}
</style>
