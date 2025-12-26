<template>
    <view class="points-mall-page">
        <!-- 沉浸式背景 -->
        <view class="page-bg"></view>

        <!-- 自定义导航栏占位 (用于适配刘海屏) -->
        <view class="nav-placeholder" :style="{ height: statusBarHeight + 44 + 'px' }">
            <view class="nav-content" :style="{ paddingTop: statusBarHeight + 'px' }">
                <view class="nav-title">积分商城</view>
            </view>
        </view>

        <!-- 顶部积分卡片区域 -->
        <view class="header-container">
            <view class="points-card-wrap">
                <view class="points-card">
                    <!-- 背景纹理 -->
                    <view class="card-texture"></view>
                    
                    <view class="card-content">
                        <view class="user-points">
                            <view class="label">可用积分</view>
                            <view class="value-row">
                                <text class="num">{{ userStore.userInfo?.integration || 0 }}</text>
                                <text class="unit">pts</text>
                            </view>
                        </view>
                        
                        <!-- 签到按钮 -->
                        <view class="signin-box" :class="{ 'is-signed': todaySignedIn }" @click="handleSignIn">
                            <view class="icon-calendar">
                                <text class="iconfont icon-calendar-check" v-if="todaySignedIn"></text>
                                <text class="iconfont icon-calendar" v-else></text>
                            </view>
                            <view class="signin-text">
                                <text class="main-text">{{ todaySignedIn ? '已签到' : '签到领分' }}</text>
                                <text class="sub-text" v-if="todaySignedIn && continuousDays > 0">连续{{ continuousDays }}天</text>
                                <text class="sub-text" v-else>+10积分</text>
                            </view>
                        </view>
                    </view>
                </view>
                
                <!-- 装饰阴影 -->
                <view class="card-shadow"></view>
            </view>
        </view>

        <!-- 优惠券列表 -->
        <view class="mall-content">
            <view class="section-header">
                <text class="title">好券兑换</text>
                <text class="subtitle">积分当钱花，咖啡免费喝</text>
            </view>

            <view class="coupon-list">
                <view 
                    v-for="coupon in couponList" 
                    :key="coupon.id" 
                    class="coupon-ticket"
                    :class="{ 
                        'is-disabled': isButtonDisabled(coupon),
                        'is-expired': getRemainingDays(coupon.endTime) !== null && getRemainingDays(coupon.endTime) < 0
                    }"
                >
                    <!-- 左侧：金额/折扣 -->
                    <view class="ticket-left">
                        <view class="amount-wrap">
                            <text class="symbol">¥</text>
                            <text class="amount">{{ coupon.amount }}</text>
                        </view>
                        <view class="tag">全场通用</view>
                    </view>

                    <!-- 中间：信息 -->
                    <view class="ticket-center">
                        <view class="coupon-title">{{ coupon.name }}</view>
                        <view class="coupon-desc">{{ coupon.desc }}</view>
                        <view class="coupon-date" :class="getExpireClass(coupon.endTime)">
                            {{ getExpireText(coupon.endTime) }}
                        </view>
                    </view>

                    <!-- 分割线 -->
                    <view class="ticket-line"></view>

                    <!-- 右侧：操作 -->
                    <view class="ticket-right">
                        <view class="points-price">
                            <text class="p-num">{{ coupon.points }}</text>
                            <text class="p-unit">积分</text>
                        </view>
                        <button 
                            class="exchange-btn"
                            :class="getBtnStatusClass(coupon)"
                            @click.stop="handleExchange(coupon)"
                        >
                            {{ getButtonText(coupon) }}
                        </button>
                    </view>

                    <!-- 状态印章 (已领取/已抢光等) -->
                    <view class="stamp-mark" v-if="isCouponRedeemed(coupon)">
                        <text>已拥有</text>
                    </view>
                </view>

                <!-- 加载状态 (手动实现，替代 uni-load-more) -->
                <view class="loading-state">
                    <view class="loading-text" v-if="loading">
                        <view class="loading-spinner"></view>
                        <text>加载中...</text>
                    </view>
                    <view class="loading-text" v-else-if="!hasMore">
                        <text>没有更多了</text>
                    </view>
                    <view class="loading-text" v-else>
                        <text>上拉加载更多</text>
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { getStatusBarHeight } from '@/utils/system.js'
// 假设这些接口在你项目中存在，如果没有请自行替换为实际路径
import { getRedeemableCoupons, redeemCoupon, signIn, getTodaySignIn } from '@/services/promotion.js'

const userStore = useUserStore()
const statusBarHeight = ref(0)
const couponList = ref([])

// 分页相关
const page = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)
const loading = ref(false)

// 签到相关
const todaySignedIn = ref(false)
const continuousDays = ref(0)

onMounted(() => {
    statusBarHeight.value = getStatusBarHeight()
    loadCoupons(true)
    userStore.fetchUserInfo()
    checkTodaySignIn()
})

// 下拉刷新
onPullDownRefresh(async () => {
    await Promise.all([
        loadCoupons(true),
        userStore.fetchUserInfo(),
        checkTodaySignIn()
    ])
    uni.stopPullDownRefresh()
})

// 触底加载
onReachBottom(() => {
    if (hasMore.value && !loading.value) {
        loadCoupons(false)
    }
})

// 设置导航栏颜色
onShow(() => {
    uni.setNavigationBarColor({
        frontColor: '#ffffff',
        backgroundColor: '#000000'
    })
})

const loadCoupons = async (refresh = false) => {
    if (loading.value) return
    loading.value = true
    
    try {
        if (refresh) {
            page.value = 1
            hasMore.value = true
        } else {
            page.value++
        }
        
        // 模拟接口调用，实际请解开下面的注释
        const res = await getRedeemableCoupons(page.value, pageSize.value)
        const resList = res.records || res.data || res || []
        
        const list = resList.map(item => ({
            id: item.id,
            amount: item.amount,
            name: item.name,
            desc: item.note || '适用所有饮品',
            points: item.points,
            endTime: item.endTime || item.end_time,
            isRedeemed: item.isRedeemed === true || item.isRedeemed === 'true' || item.isRedeemed === 1,
            redeemedCount: item.redeemedCount || 0,
            perLimit: item.perLimit || 0
        }))
        
        if (refresh) {
            couponList.value = list
        } else {
            couponList.value = [...couponList.value, ...list]
        }
        
        hasMore.value = couponList.value.length < (res.total || 0)
        
    } catch (e) {
        console.error('加载失败', e)
        if (!refresh && page.value > 1) page.value--
    } finally {
        loading.value = false
    }
}

// 辅助函数
const isCouponRedeemed = (coupon) => {
    return coupon.isRedeemed === true || coupon.isRedeemed === 'true' || coupon.isRedeemed === 1
}

const isButtonDisabled = (coupon) => {
    const currentPoints = userStore.userInfo?.integration || 0
    const pointsInsufficient = currentPoints < coupon.points
    const isExpired = getRemainingDays(coupon.endTime) !== null && getRemainingDays(coupon.endTime) < 0
    return pointsInsufficient || isCouponRedeemed(coupon) || isExpired
}

const getBtnStatusClass = (coupon) => {
    if (isCouponRedeemed(coupon)) return 'btn-redeemed'
    if (getRemainingDays(coupon.endTime) !== null && getRemainingDays(coupon.endTime) < 0) return 'btn-expired'
    const currentPoints = userStore.userInfo?.integration || 0
    if (currentPoints < coupon.points) return 'btn-insufficient'
    return 'btn-normal'
}

const getButtonText = (coupon) => {
    const remainingDays = getRemainingDays(coupon.endTime)
    if (remainingDays !== null && remainingDays < 0) return '已过期'
    if (isCouponRedeemed(coupon)) return '已兑换'
    const currentPoints = userStore.userInfo?.integration || 0
    if (currentPoints < coupon.points) return '积分不足'
    return '立即兑换'
}

// 简单的日期计算
const getRemainingDays = (endTime) => {
    if (!endTime) return null
    try {
        const end = new Date(endTime).setHours(0,0,0,0)
        const now = new Date().setHours(0,0,0,0)
        return Math.ceil((end - now) / (86400000))
    } catch { return null }
}

const getExpireText = (endTime) => {
    const days = getRemainingDays(endTime)
    if (days === null) return '长期有效'
    if (days < 0) return '已过期'
    if (days === 0) return '今日到期'
    return `有效期剩 ${days} 天`
}

const getExpireClass = (endTime) => {
    const days = getRemainingDays(endTime)
    if (days !== null && days <= 3 && days >= 0) return 'text-urgent'
    return ''
}

// 签到逻辑
const checkTodaySignIn = async () => {
    if (!userStore.isLogin) return
    try {
        const res = await getTodaySignIn()
        // 响应拦截器已经返回 data.data，所以 res 直接就是签到记录对象或 null
        todaySignedIn.value = res !== null && res !== undefined
        continuousDays.value = res?.continuousDays || 0
    } catch (e) { console.error(e) }
}

const handleSignIn = async () => {
    if (!userStore.isLogin) return uni.navigateTo({ url: '/pages/login/index' })
    if (todaySignedIn.value) return
    
    uni.showLoading({ title: '签到中' })
    try {
        const res = await signIn()
        todaySignedIn.value = true
        continuousDays.value = (res.data?.continuousDays || continuousDays.value + 1)
        await userStore.fetchUserInfo()
        uni.showToast({ title: '签到成功 +10积分', icon: 'none' })
    } catch (e) {
        uni.showToast({ title: e.message || '签到失败', icon: 'none' })
    } finally {
        uni.hideLoading()
    }
}

const handleExchange = (coupon) => {
    if (isButtonDisabled(coupon)) return
    
    uni.showModal({
        title: '兑换确认',
        content: `确定使用 ${coupon.points} 积分兑换一张 ${coupon.name} 吗？`,
        confirmColor: '#333333',
        success: async (res) => {
            if (res.confirm) {
                uni.showLoading({ title: '处理中' })
                try {
                    await redeemCoupon(coupon.id)
                    uni.showToast({ title: '兑换成功' })
                    await userStore.fetchUserInfo()
                    loadCoupons(true)
                } catch (e) {
                    uni.showToast({ title: e.message || '兑换失败', icon: 'none' })
                } finally {
                    uni.hideLoading()
                }
            }
        }
    })
}
</script>

<style lang="scss" scoped>
.points-mall-page {
    min-height: 100vh;
    background-color: #f7f8fa;
    position: relative;
    padding-bottom: 40rpx;
}

// 顶部背景
.page-bg {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 500rpx;
    background: linear-gradient(180deg, #1a1a1a 0%, #333333 60%, #f7f8fa 100%);
    z-index: 0;
}

// 导航栏
.nav-placeholder {
    width: 100%;
    z-index: 100;
    position: relative;
    
    .nav-content {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        height: 44px;
        z-index: 100;
        // 背景渐变过渡效果可结合 onPageScroll 实现，此处简单处理
        color: #fff;
    }
    
    .nav-title {
        font-size: 34rpx;
        font-weight: 600;
    }
}

// 头部区域
.header-container {
    position: relative;
    z-index: 10;
    padding: 20rpx 32rpx 0;
}

.points-card-wrap {
    position: relative;
}

.points-card {
    background: linear-gradient(135deg, #2c2c2c 0%, #1a1a1a 100%);
    border-radius: 24rpx;
    padding: 40rpx;
    position: relative;
    overflow: hidden;
    border: 1px solid rgba(255, 255, 255, 0.1);
    color: #ebdcb0; // 浅金色字体
    
    .card-texture {
        position: absolute;
        top: -50%;
        right: -20%;
        width: 400rpx;
        height: 400rpx;
        background: radial-gradient(circle, rgba(255,215,0,0.1) 0%, rgba(0,0,0,0) 70%);
        opacity: 0.6;
        pointer-events: none;
    }
}

.card-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: relative;
    z-index: 2;
}

.user-points {
    .label {
        font-size: 24rpx;
        opacity: 0.8;
        margin-bottom: 8rpx;
    }
    .value-row {
        display: flex;
        align-items: baseline;
        
        .num {
            font-size: 72rpx;
            font-weight: bold;
            line-height: 1;
            font-family: 'DIN Alternate', sans-serif; // 如果有数字字体
            background: linear-gradient(180deg, #fffbec 0%, #ebdcb0 100%);
            -webkit-background-clip: text;
            color: transparent;
        }
        .unit {
            font-size: 24rpx;
            margin-left: 8rpx;
            opacity: 0.8;
        }
    }
}

.signin-box {
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);
    padding: 12rpx 24rpx;
    border-radius: 50rpx;
    display: flex;
    align-items: center;
    gap: 16rpx;
    border: 1px solid rgba(255, 255, 255, 0.15);
    transition: all 0.3s;
    
    &:active {
        transform: scale(0.96);
        background: rgba(255, 255, 255, 0.15);
    }
    
    &.is-signed {
        background: rgba(255, 255, 255, 0.05);
        opacity: 0.8;
    }
    
    .icon-calendar {
        font-size: 32rpx;
    }
    
    .signin-text {
        display: flex;
        flex-direction: column;
        
        .main-text {
            font-size: 24rpx;
            font-weight: 600;
            line-height: 1.2;
        }
        .sub-text {
            font-size: 20rpx;
            opacity: 0.7;
            line-height: 1.2;
        }
    }
}

.card-shadow {
    position: absolute;
    bottom: -10rpx;
    left: 20rpx;
    right: 20rpx;
    height: 20rpx;
    background: rgba(0,0,0,0.3);
    filter: blur(10px);
    border-radius: 50%;
    z-index: -1;
}

// 商城内容区
.mall-content {
    position: relative;
    z-index: 10;
    padding: 40rpx 32rpx;
}

.section-header {
    margin-bottom: 30rpx;
    
    .title {
        font-size: 36rpx;
        font-weight: bold;
        color: #333;
        margin-right: 16rpx;
    }
    .subtitle {
        font-size: 24rpx;
        color: #999;
    }
}

.coupon-list {
    display: flex;
    flex-direction: column;
    gap: 24rpx;
}

// 票券样式
.coupon-ticket {
    background: #fff;
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    position: relative;
    overflow: hidden;
    box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
    height: 180rpx;
    
    // 票券左右打孔效果
    &::before, &::after {
        content: '';
        position: absolute;
        width: 32rpx;
        height: 32rpx;
        background-color: #f7f8fa; // 与页面背景色一致
        border-radius: 50%;
        top: 50%;
        transform: translateY(-50%);
        z-index: 2;
    }
    &::before { left: -16rpx; }
    &::after { right: 200rpx; transform: translate(50%, -50%); } // 右侧分割孔位置
    
    // 禁用态
    &.is-disabled {
        opacity: 0.7;
        filter: grayscale(0.8);
    }
}

.ticket-left {
    width: 160rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    
    .amount-wrap {
        color: #e64340; // 价格红
        font-weight: bold;
        display: flex;
        align-items: baseline;
        
        .symbol { font-size: 28rpx; margin-right: 4rpx; }
        .amount { font-size: 60rpx; line-height: 1; }
    }
    
    .tag {
        font-size: 20rpx;
        color: #e64340;
        background: rgba(230, 67, 64, 0.1);
        padding: 2rpx 12rpx;
        border-radius: 8rpx;
        margin-top: 8rpx;
    }
}

.ticket-center {
    flex: 1;
    padding: 0 20rpx;
    display: flex;
    flex-direction: column;
    justify-content: center;
    
    .coupon-title {
        font-size: 30rpx;
        font-weight: bold;
        color: #333;
        margin-bottom: 8rpx;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }
    
    .coupon-desc {
        font-size: 22rpx;
        color: #999;
        margin-bottom: 8rpx;
    }
    
    .coupon-date {
        font-size: 20rpx;
        color: #bbb;
        
        &.text-urgent {
            color: #ff9500;
        }
    }
}

.ticket-line {
    width: 2rpx;
    height: 80%;
    border-left: 2rpx dashed #eee;
    position: absolute;
    right: 200rpx;
}

.ticket-right {
    width: 200rpx;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding-left: 10rpx;
    
    .points-price {
        color: #c69c6d; // 咖啡金
        margin-bottom: 16rpx;
        
        .p-num { font-size: 36rpx; font-weight: bold; margin-right: 4rpx; }
        .p-unit { font-size: 22rpx; }
    }
    
    .exchange-btn {
        margin: 0;
        padding: 0;
        width: 140rpx;
        height: 52rpx;
        line-height: 52rpx;
        font-size: 24rpx;
        border-radius: 26rpx;
        
        &.btn-normal {
            background: linear-gradient(90deg, #333333 0%, #1a1a1a 100%);
            color: #ebdcb0;
        }
        
        &.btn-insufficient {
            background: #f5f5f5;
            color: #999;
        }
        
        &.btn-redeemed {
            background: transparent;
            color: #999;
            border: 1px solid #eee;
        }
        
        &.btn-expired {
            background: #eee;
            color: #bbb;
        }
        
        &::after { border: none; }
    }
}

.stamp-mark {
    position: absolute;
    right: -10rpx;
    top: -10rpx;
    width: 100rpx;
    height: 100rpx;
    border: 4rpx solid #ccc;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    transform: rotate(30deg);
    opacity: 0.3;
    pointer-events: none;
    
    text {
        font-size: 20rpx;
        color: #999;
        font-weight: bold;
        border: 2rpx solid #999;
        padding: 4rpx;
    }
}

.loading-state {
    padding: 30rpx 0;
    
    .loading-text {
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 12rpx;
        color: #999;
        font-size: 24rpx;
        
        .loading-spinner {
            width: 28rpx;
            height: 28rpx;
            border: 3rpx solid #e0e0e0;
            border-top-color: #999;
            border-radius: 50%;
            animation: spin 0.8s linear infinite;
        }
    }
}

@keyframes spin {
    to { transform: rotate(360deg); }
}
</style>