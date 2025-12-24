<template>
    <view class="buy-page">
        <!-- 顶部导航 -->
        <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
            <view class="nav-left" @click="goBack">
                <uni-icons type="left" size="20" color="#333" />
            </view>
            <text class="nav-title">购买咖啡卡</text>
            <view class="nav-right" />
        </view>

        <!-- 内容区域 -->
        <scroll-view scroll-y class="content-scroll" :enable-back-to-top="true" :scroll-with-animation="true">
            <!-- 面额选择 -->
            <view class="section">
                <text class="section-title">选择面额</text>
                <view class="amount-grid">
                    <view v-for="amount in presetAmounts" :key="amount" class="amount-item"
                        :class="{ active: selectedAmount === amount }" @click="selectAmount(amount)">
                        <text class="amount-value">¥{{ amount }}</text>
                    </view>
                    <view class="amount-item custom" :class="{ active: isCustomAmount }" @click="selectCustomAmount">
                        <text class="amount-value">自定义</text>
                    </view>
                </view>

                <!-- 自定义金额输入 -->
                <view v-if="isCustomAmount" class="custom-input-box">
                    <text class="input-label">输入金额</text>
                    <view class="input-wrapper">
                        <text class="currency-symbol">¥</text>
                        <input v-model="customAmount" type="digit" placeholder="请输入金额" class="amount-input"
                            @input="handleCustomAmountInput" />
                    </view>
                    <text class="input-hint">最低 ¥10，最高 ¥5000</text>
                </view>
            </view>

            <!-- 卡片名称 -->
            <view class="section">
                <text class="section-title">卡片名称（可选）</text>
                <input v-model="cardName" type="text" placeholder="例如：咖啡会员卡、节日限定卡" class="text-input"  maxlength="20"
                    cursor-spacing="20"  :adjust-position="true" />
            </view>

            <!-- 祝福语 -->
            <view class="section">
                <text class="section-title">祝福语（可选）</text>
                <textarea v-model="greeting" placeholder="写下你的祝福..." class="textarea-input" maxlength="100" />
                <text class="char-count">{{ greeting.length }}/100</text>
            </view>

            <!-- 订单信息预览 -->
            <view class="preview-section">
                <view class="preview-item">
                    <text class="preview-label">面额</text>
                    <text class="preview-value">¥{{ finalAmount }}</text>
                </view>
                <view class="preview-item">
                    <text class="preview-label">有效期</text>
                    <text class="preview-value">365天</text>
                </view>
                <view class="preview-item highlight">
                    <text class="preview-label">会员折扣</text>
                    <text class="preview-value">9折优惠</text>
                </view>
            </view>

            <!-- 预留底部空间 -->
            <view style="height: 200rpx" />
        </scroll-view>

        <!-- 底部操作栏 -->
        <view class="footer-bar">
            <view class="price-info">
                <text class="price-label">实付金额</text>
                <text class="price-value">¥{{ finalAmount }}</text>
            </view>
            <button class="submit-btn" :disabled="!canSubmit" :class="{ disabled: !canSubmit }" @click="handleSubmit">
                立即购买
            </button>
        </view>
    </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getStatusBarHeight } from '@/utils/system.js'
import { createGiftCardOrder } from '@/services/giftcard.js'
import { alipay } from '@/services/pay.js'

const statusBarHeight = ref(0)

// 预设面额
const presetAmounts = [50, 100, 200, 500]

// 选中的金额
const selectedAmount = ref(100)
const isCustomAmount = ref(false)
const customAmount = ref('')

// 卡片信息
const cardName = ref('')
const greeting = ref('')

// 计算最终金额
const finalAmount = computed(() => {
    if (isCustomAmount.value) {
        const amount = parseFloat(customAmount.value) || 0
        return amount.toFixed(2)
    }
    return selectedAmount.value.toFixed(2)
})

// 是否可以提交
const canSubmit = computed(() => {
    if (isCustomAmount.value) {
        const amount = parseFloat(customAmount.value) || 0
        return amount >= 10 && amount <= 5000
    }
    return selectedAmount.value > 0
})

// 选择预设面额
const selectAmount = (amount) => {
    selectedAmount.value = amount
    isCustomAmount.value = false
    customAmount.value = ''
}

// 选择自定义金额
const selectCustomAmount = () => {
    isCustomAmount.value = true
    selectedAmount.value = 0
}

// 处理自定义金额输入
const handleCustomAmountInput = (e) => {
    const value = e.detail.value
    const num = parseFloat(value)
    if (num && (num < 10 || num > 5000)) {
        // 可以在这里提示用户金额范围
    }
}

// 处理卡片名称输入
const handleCardNameInput = (e) => {
    cardName.value = e.detail.value
}

// 处理输入框聚焦（确保键盘能弹出）
const handleInputFocus = (e) => {
    // 延迟一下，确保键盘能正常弹出
    setTimeout(() => {
        // 可以在这里添加滚动逻辑，确保输入框可见
    }, 100)
}

// 返回
const goBack = () => {
    uni.navigateBack()
}

// 提交购卡
const handleSubmit = async () => {
    if (!canSubmit.value) {
        return
    }

    const amount = parseFloat(finalAmount.value)
    if (amount < 10 || amount > 5000) {
        uni.showToast({
            title: '金额范围：¥10 - ¥5000',
            icon: 'none'
        })
        return
    }

    try {
        uni.showLoading({ title: '正在创建订单...' })

        // 1. 创建礼品卡订单
        const data = {
            amount: amount,
            name: cardName.value || '咖啡会员卡',
            greeting: greeting.value || '',
            validDays: 365
        }

        const orderResult = await createGiftCardOrder(data)
        const orderId = orderResult.id || orderResult.data?.id
        uni.hideLoading()

        if (!orderId) {
            uni.showToast({
                title: '创建订单失败',
                icon: 'none'
            })
            return
        }

        // 2. 处理沙箱环境（仅开发环境）
        if (process.env.NODE_ENV === 'development') {
            // #ifdef APP-PLUS
            const EnvUtils = plus.android.importClass("com.alipay.sdk.app.EnvUtils")
            if (EnvUtils) {
                EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX)
            }
            // #endif
        }

        // 3. 获取支付串并调起支付
        uni.showLoading({ title: '正在发起支付...' })
        const orderStr = await alipay(orderId)
        uni.hideLoading()

        uni.requestPayment({
            provider: 'alipay',
            orderInfo: orderStr,
            success: (res) => {
                uni.showToast({
                    title: '支付成功',
                    icon: 'success'
                })
                // 支付成功后返回礼品卡列表页
                setTimeout(() => {
                    uni.navigateBack()
                }, 1500)
            },
            fail: (err) => {
                console.error('支付失败:', err)
                uni.showToast({
                    title: '支付未完成',
                    icon: 'none'
                })
            }
        })

    } catch (error) {
        uni.hideLoading()
        console.error('购卡流程异常:', error)
        uni.showToast({
            title: error.message || '购卡失败，请重试',
            icon: 'none'
        })
    }
}

onMounted(() => {
    statusBarHeight.value = getStatusBarHeight()
})
</script>

<style scoped lang="scss">
$primary: #6f4e37;
$bg-color: #f7f8fa;

.buy-page {
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

.section {
    background-color: #ffffff;
    border-radius: 24rpx;
    padding: 32rpx;
    margin-bottom: 24rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #333333;
    margin-bottom: 24rpx;
    display: block;
}

.amount-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16rpx;
}

.amount-item {
    height: 100rpx;
    background-color: #f7f8fa;
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2rpx solid transparent;
    transition: all 0.3s;
}

.amount-item.active {
    background-color: rgba(111, 78, 55, 0.1);
    border-color: $primary;
}

.amount-value {
    font-size: 32rpx;
    font-weight: 600;
    color: #333333;
}

.amount-item.active .amount-value {
    color: $primary;
}

.custom-input-box {
    margin-top: 24rpx;
}

.input-label {
    font-size: 26rpx;
    color: #666666;
    margin-bottom: 16rpx;
    display: block;
}

.input-wrapper {
    display: flex;
    align-items: center;
    background-color: #f7f8fa;
    border-radius: 12rpx;
    padding: 0 24rpx;
    height: 88rpx;
}

.currency-symbol {
    font-size: 32rpx;
    font-weight: 600;
    color: #333333;
    margin-right: 12rpx;
}

.amount-input {
    flex: 1;
    font-size: 32rpx;
    font-weight: 600;
    color: #333333;
}

.input-hint {
    font-size: 22rpx;
    color: #999999;
    margin-top: 12rpx;
    display: block;
}

.text-input{
    width: 100%;
    background-color: #f7f8fa;
    border-radius: 12rpx;
    padding: 0 24rpx;
    font-size: 28rpx;
    color: #333333;
    box-sizing: border-box;
    height: 88rpx;
}
.textarea-input {
    width: 100%;
    background-color: #f7f8fa;
    border-radius: 12rpx;
    padding: 20rpx 24rpx;
    font-size: 28rpx;
    color: #333333;
    box-sizing: border-box;
    /* 确保可以输入 - 移除可能影响交互的属性 */
    border: none;
    outline: none;
}

.textarea-input {
    min-height: 160rpx;
    line-height: 1.6;
}

.char-count {
    font-size: 22rpx;
    color: #999999;
    text-align: right;
    margin-top: 12rpx;
    display: block;
}

.preview-section {
    background-color: #ffffff;
    border-radius: 24rpx;
    padding: 32rpx;
    margin-bottom: 24rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.preview-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f0f0f0;
}

.preview-item:last-child {
    border-bottom: none;
}

.preview-label {
    font-size: 28rpx;
    color: #666666;
}

.preview-value {
    font-size: 32rpx;
    font-weight: 600;
    color: $primary;
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
    display: flex;
    align-items: center;
    gap: 24rpx;
}

.price-info {
    flex: 1;
    display: flex;
    flex-direction: column;
}

.price-label {
    font-size: 24rpx;
    color: #999999;
    margin-bottom: 4rpx;
}

.price-value {
    font-size: 40rpx;
    font-weight: bold;
    color: $primary;
}

.submit-btn {
    height: 88rpx;
    padding: 0 48rpx;
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

.submit-btn.disabled {
    background-color: #cccccc;
    color: #999999;
}
</style>
