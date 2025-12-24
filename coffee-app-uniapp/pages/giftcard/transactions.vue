<template>
    <view class="transactions-page">
        <!-- 顶部导航 -->
        <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
            <view class="nav-left" @click="goBack">
                <uni-icons type="left" size="20" color="#333" />
            </view>
            <text class="nav-title">交易明细</text>
            <view class="nav-right" />
        </view>

        <!-- 内容区域 -->
        <scroll-view scroll-y class="content-scroll" @scrolltolower="loadMore" :refresher-enabled="true" @refresherrefresh="refresh">
            <!-- 卡片信息 -->
            <view class="card-info-section">
                <text class="card-name">{{ cardName }}</text>
                <text class="card-hint">点击查看交易明细</text>
            </view>

            <!-- 交易明细列表 -->
            <view v-if="transactions.length" class="transactions-list">
                <view v-for="txn in transactions" :key="txn.id" class="transaction-item">
                    <view class="txn-left">
                        <view class="txn-type-icon" :class="`type-${txn.type}`">
                            <uni-icons :type="getTypeIcon(txn.type)" size="20" color="#fff" />
                        </view>
                        <view class="txn-info">
                            <text class="txn-type-text">{{ getTypeText(txn.type) }}</text>
                            <text class="txn-time">{{ formatDateTime(txn.createTime) }}</text>
                            <text class="txn-remark" v-if="txn.remark">{{ txn.remark }}</text>
                        </view>
                    </view>
                    <view class="txn-amount" :class="txn.amount >= 0 ? 'income' : 'expense'">
                        <text v-if="txn.amount >= 0" class="amount-symbol">+</text>
                        <text class="amount-value">¥{{ Math.abs(parseFloat(txn.amount || 0)).toFixed(2) }}</text>
                    </view>
                </view>
            </view>

            <!-- 空状态 -->
            <view v-else-if="!loading" class="empty-box">
                <text class="empty-icon">📋</text>
                <text class="empty-text">暂无交易记录</text>
            </view>

            <!-- 加载更多 -->
            <view v-if="hasMore && transactions.length > 0" class="load-more">
                <text class="load-more-text">{{ loading ? '加载中...' : '上拉加载更多' }}</text>
            </view>

            <!-- 没有更多 -->
            <view v-if="!hasMore && transactions.length > 0" class="no-more">
                <text class="no-more-text">没有更多了</text>
            </view>

            <!-- 预留底部空间 -->
            <view style="height: 40rpx" />
        </scroll-view>
    </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getStatusBarHeight } from '@/utils/system.js'
import { getGiftCardTransactions } from '@/services/giftcard.js'

const statusBarHeight = ref(0)
const cardId = ref(null)
const cardName = ref('咖啡卡')
const transactions = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)

// 加载交易明细
const loadTransactions = async (isRefresh = false) => {
    if (loading.value || !cardId.value) return
    
    try {
        loading.value = true
        if (isRefresh) {
            page.value = 1
            transactions.value = []
        }
        
        const res = await getGiftCardTransactions(cardId.value, {
            page: page.value,
            pageSize: pageSize.value
        })
        
        const txnList = res.data?.records || res.records || res.data || []
        
        if (isRefresh) {
            transactions.value = txnList
        } else {
            transactions.value.push(...txnList)
        }
        
        // 判断是否还有更多
        hasMore.value = txnList.length >= pageSize.value
        if (hasMore.value) {
            page.value++
        }
    } catch (error) {
        console.error('加载交易明细失败', error)
        uni.showToast({
            title: '加载失败，请重试',
            icon: 'none'
        })
    } finally {
        loading.value = false
    }
}

// 加载更多
const loadMore = () => {
    if (hasMore.value && !loading.value) {
        loadTransactions(false)
    }
}

// 下拉刷新
const refresh = () => {
    loadTransactions(true)
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 获取交易类型文本
const getTypeText = (type) => {
    const typeMap = {
        0: '充值/发卡',
        1: '消费',
        2: '退款',
        3: '调整'
    }
    return typeMap[type] || '未知'
}

// 获取交易类型图标
const getTypeIcon = (type) => {
    const iconMap = {
        0: 'wallet-filled',  // 充值
        1: 'shop',           // 消费
        2: 'loop',           // 退款
        3: 'settings'        // 调整
    }
    return iconMap[type] || 'help'
}

// 返回
const goBack = () => {
    uni.navigateBack()
}

// 页面加载
onLoad((options) => {
    if (options.cardId) {
        cardId.value = parseInt(options.cardId)
    }
    if (options.cardName) {
        cardName.value = decodeURIComponent(options.cardName)
    }
})

onMounted(() => {
    statusBarHeight.value = getStatusBarHeight()
    if (cardId.value) {
        loadTransactions(true)
    } else {
        uni.showToast({
            title: '参数错误',
            icon: 'none'
        })
        setTimeout(() => {
            uni.navigateBack()
        }, 1500)
    }
})
</script>

<style scoped lang="scss">
$primary: #6f4e37;
$bg-color: #f7f8fa;

.transactions-page {
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
    padding: 24rpx 32rpx;
    box-sizing: border-box;
}

.card-info-section {
    background: linear-gradient(135deg, #3c2a21 0%, #6f4e37 60%, #a47148 100%);
    border-radius: 24rpx;
    padding: 32rpx;
    margin-bottom: 24rpx;
    color: #ffffff;
    box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.25);
}

.card-name {
    font-size: 32rpx;
    font-weight: 600;
    margin-bottom: 8rpx;
    display: block;
}

.card-hint {
    font-size: 24rpx;
    opacity: 0.8;
    display: block;
}

.transactions-list {
    display: flex;
    flex-direction: column;
    gap: 16rpx;
}

.transaction-item {
    background-color: #ffffff;
    border-radius: 16rpx;
    padding: 24rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.txn-left {
    display: flex;
    align-items: center;
    flex: 1;
    gap: 20rpx;
}

.txn-type-icon {
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}

.txn-type-icon.type-0 {
    background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
}

.txn-type-icon.type-1 {
    background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
}

.txn-type-icon.type-2 {
    background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
}

.txn-type-icon.type-3 {
    background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
}

.txn-info {
    display: flex;
    flex-direction: column;
    gap: 8rpx;
    flex: 1;
}

.txn-type-text {
    font-size: 30rpx;
    font-weight: 500;
    color: #333333;
}

.txn-time {
    font-size: 24rpx;
    color: #999999;
}

.txn-remark {
    font-size: 24rpx;
    color: #666666;
    margin-top: 4rpx;
}

.txn-amount {
    display: flex;
    align-items: center;
    font-size: 32rpx;
    font-weight: 600;
}

.txn-amount.income {
    color: #52c41a;
}

.txn-amount.expense {
    color: #ff4d4f;
}

.amount-symbol {
    font-size: 28rpx;
    margin-right: 4rpx;
}

.amount-value {
    font-size: 32rpx;
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
}

.load-more,
.no-more {
    text-align: center;
    padding: 32rpx 0;
}

.load-more-text,
.no-more-text {
    font-size: 24rpx;
    color: #999999;
}
</style>

