<template>
  <view class="order-detail-page" :class="themeClass">
    <!-- 顶部导航 -->
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-back" @click="goBack">
        <uni-icons type="left" size="24" color="#333"></uni-icons>
      </view>
      <text class="page-title">订单详情</text>
      <view style="width: 48rpx;"></view>
    </view>

    <scroll-view scroll-y class="content-scroll">
      <!-- 订单状态卡片 -->
      <view class="status-card-new" :class="{ 'gift-card-status': isGiftCardOrder() }">
        <!-- 上半部分：状态文字与图标 -->
        <view class="status-header">
          <view class="status-info">
            <text class="status-title">{{ getStatusText(orderDetail.status) }}</text>
            <text class="status-desc-text">{{ getStatusDesc(orderDetail.status) }}</text>

            <!-- 取餐码 (仅在商品订单的制作中/待取餐时显示) -->
            <view class="pickup-code-box" v-if="!isGiftCardOrder() && [1, 2, 3].includes(orderDetail.status) && orderDetail.pickupCode">
              <text class="code-label">取餐码</text>
              <text class="code-value">{{ orderDetail.pickupCode }}</text>
            </view>
          </view>
        </view>

        <!-- 下半部分：进度条 (仅商品订单进行中时显示) -->
        <view class="status-steps" v-if="!isGiftCardOrder() && [0, 1, 2, 3].includes(orderDetail.status)">
          <view class="step-item" :class="{ active: orderDetail.status >= 0 }">
            <view class="step-circle">
              <uni-icons v-if="orderDetail.status >= 0" type="checkmarkempty" size="12" color="#fff"/>
            </view>
            <text class="step-label">已下单</text>
          </view>
          <view class="step-line" :class="{ active: orderDetail.status >= 1 }"></view>
          <view class="step-item" :class="{ active: orderDetail.status >= 1 }">
            <view class="step-circle">
              <uni-icons v-if="orderDetail.status >= 1" type="checkmarkempty" size="12" color="#fff"/>
            </view>
            <text class="step-label">制作中</text>
          </view>
          <view class="step-line" :class="{ active: orderDetail.status >= 3 }"></view>
          <view class="step-item" :class="{ active: orderDetail.status >= 3 }">
            <view class="step-circle">
              <uni-icons v-if="orderDetail.status >= 3" type="checkmarkempty" size="12" color="#fff"/>
            </view>
            <text class="step-label">待取餐</text>
          </view>
        </view>
      </view>

      <!-- 订单取消原因显示 -->
      <view class="remark-section" v-if="orderDetail.status === 5 && orderDetail.cancelReason">
        <view class="section-title" style="color: #ff4d4f;">取消原因</view>
        <text class="remark-text">{{ orderDetail.cancelReason }}</text>
      </view>

      <!-- 配送/取餐信息 (新增强化) -->
      <view class="delivery-info-section" v-if="!isGiftCardOrder()">
        <view class="section-title">{{ orderDetail.deliveryCompany === '门店自提' ? '取餐信息' : '配送信息' }}</view>
        
        <!-- 自提场景 -->
        <template v-if="orderDetail.deliveryCompany === '门店自提'">
          <view class="info-item">
            <text class="info-label">取餐门店</text>
            <text class="info-value">{{ orderDetail.storeName || '智咖·云(总店)' }}</text>
          </view>
          <view class="info-item" v-if="orderDetail.pickupCode">
            <text class="info-label">取餐码</text>
            <text class="info-value highlight-code">{{ orderDetail.pickupCode }}</text>
          </view>
        </template>

        <!-- 外送场景 -->
        <template v-else>
          <view class="info-item">
            <text class="info-label">配送服务</text>
            <text class="info-value">商家配送</text>
          </view>
          <view class="info-item" v-if="orderDetail.deliveryDistance">
            <text class="info-label">配送距离</text>
            <text class="info-value">{{ (orderDetail.deliveryDistance / 1000).toFixed(1) }}km</text>
          </view>
          <view class="address-box">
            <view class="addr-row">
              <text class="addr-name">{{ orderDetail.receiverName }}</text>
              <text class="addr-phone">{{ orderDetail.receiverPhone }}</text>
            </view>
            <text class="addr-detail">
              {{ orderDetail.receiverProvince }}{{ orderDetail.receiverCity }}{{ orderDetail.receiverRegion }}
              {{ orderDetail.receiverDetailAddress }}
            </text>
          </view>
        </template>
      </view>

      <!-- 咖啡卡订单信息 -->
      <view class="gift-card-info-section" v-if="isGiftCardOrder()">
        <view class="section-title">订单类型</view>
        <view class="gift-card-badge">
          <text class="gift-card-icon">☕</text>
          <text class="gift-card-text">咖啡卡订单</text>
        </view>
      </view>

      <!-- 订单信息 -->
      <view class="order-info-section" :class="{ 'gift-card-info': isGiftCardOrder() }">
        <view class="section-title">订单信息</view>
        <view class="info-item">
          <text class="info-label">订单编号</text>
          <text class="info-value">{{ orderDetail.orderSn }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">下单时间</text>
          <text class="info-value">{{ formatTime(orderDetail.createTime) }}</text>
        </view>
        <view class="info-item" v-if="orderDetail.paymentTime">
          <text class="info-label">支付时间</text>
          <text class="info-value">{{ formatTime(orderDetail.paymentTime) }}</text>
        </view>
        <view class="info-item" v-if="!isGiftCardOrder() && orderDetail.deliveryCompany">
          <text class="info-label">配送方式</text>
          <text class="info-value">{{ orderDetail.deliveryCompany }}</text>
        </view>
      </view>

      <!-- 商品列表（仅商品订单显示） -->
      <view class="goods-section"
            v-if="!isGiftCardOrder() && orderDetail.orderItemList && orderDetail.orderItemList.length > 0">
        <view class="section-title">商品清单</view>
        <view class="goods-list">
          <view
              v-for="(item, index) in orderDetail.orderItemList"
              :key="index"
              class="goods-item"
          >
            <image :src="item.productPic" mode="aspectFill" class="goods-img"/>
            <view class="goods-info">
              <text class="goods-name">{{ item.productName }}</text>
              <view class="goods-spec" v-if="item.productAttr">
                <text class="spec-text">{{ item.productAttr }}</text>
              </view>
              <view class="goods-bottom">
                <view class="goods-price">
                  <text class="symbol">¥</text>
                  <text class="price">{{ item.productPrice }}</text>
                </view>
                <text class="goods-quantity">x{{ item.productQuantity }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 费用明细 -->
      <view class="price-section" :class="{ 'gift-card-price': isGiftCardOrder() }">
        <view class="section-title">费用明细</view>
        <view class="price-item">
          <text class="price-label" v-if="!isGiftCardOrder()">商品合计</text>
          <text class="price-label" v-else>咖啡卡金额</text>
          <text class="price-value">¥{{ orderDetail.totalAmount || 0 }}</text>
        </view>
        <view class="price-item" v-if="orderDetail.promotionAmount > 0">
          <text class="price-label">促销优惠</text>
          <text class="price-value discount">-¥{{ orderDetail.promotionAmount }}</text>
        </view>
        <view class="price-item" v-if="orderDetail.couponAmount > 0">
          <text class="price-label">优惠券</text>
          <text class="price-value discount">-¥{{ orderDetail.couponAmount }}</text>
        </view>
        <view class="price-item" v-if="orderDetail.deliveryFee > 0">
          <text class="price-label">配送费</text>
          <text class="price-value">¥{{ orderDetail.deliveryFee }}</text>
        </view>
        <view class="price-item" v-if="orderDetail.coffeeCardDiscountAmount > 0">
          <text class="price-label">咖啡卡优惠</text>
          <text class="price-value discount">-¥{{ orderDetail.coffeeCardDiscountAmount }}</text>
        </view>
        <view class="price-item total">
          <text class="price-label">实付金额</text>
          <text class="price-value total-price">¥{{ orderDetail.payAmount || 0 }}</text>
        </view>
      </view>

      <!-- 备注 -->
      <view class="remark-section" v-if="orderDetail.note">
        <view class="section-title">订单备注</view>
        <!-- 咖啡卡订单：格式化显示 -->
        <view v-if="isGiftCardOrder() && orderDetail.note.startsWith('GIFT_CARD:')" class="gift-card-remark">
          <view class="remark-item" v-for="(value, key) in parseGiftCardNote(orderDetail.note)" :key="key">
            <text class="remark-label">{{ getGiftCardLabel(key) }}：</text>
            <text class="remark-value">{{ formatGiftCardValue(key, value) }}</text>
          </view>
        </view>
        <!-- 普通备注：直接显示 -->
        <text v-else class="remark-text">{{ orderDetail.note }}</text>
      </view>

      <!-- 预留底部安全距离，避免被底部操作栏遮挡 -->
      <view style="height: 200rpx;"></view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="footer-bar" v-if="orderDetail.id">
      <view class="footer-actions">
        <button
            v-if="orderDetail.status === 0"
            class="action-btn cancel-btn"
            @click="onCancelOrderClick"
        >
          取消订单
        </button>
        <button
            v-if="orderDetail.status === 0"
            class="action-btn pay-btn"
            @click="payOrder"
        >
          立即支付
        </button>
        <button
            v-if="orderDetail.status === 3"
            class="action-btn confirm-btn"
            @click="confirmReceive"
        >
          取餐
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import {ref, onMounted, onUnmounted, computed} from 'vue'
import {onLoad} from '@dcloudio/uni-app'
import {getOrderDetail, cancelOrder as apiCancelOrder} from '@/services/order.js' // 直接引入 API
import {getStatusBarHeight} from '@/utils/system.js'
import {formatDateTime} from '@/utils/date.js'
import {useOrderActions} from '@/composables/useOrderActions.js'
import { useUserStore } from '@/store/user.js'

const {handleConfirmReceive, handlePayOrder} = useOrderActions() // 移除 handleCancelOrder，改用本地实现
const userStore = useUserStore()

const statusBarHeight = ref(0)
const orderDetail = ref({})
const orderId = ref(null)
let isUnmounted = false

onUnmounted(() => {
  isUnmounted = true
})

const themeClass = computed(() => userStore.isDarkMode ? 'theme-dark' : 'theme-light')

// 获取订单状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '待付款',
    1: '待制作',
    2: '制作中',
    3: '待取餐',
    4: '已完成',
    5: '已取消'
  }
  return statusMap[status] || '未知'
}

// 获取订单状态描述
const getStatusDesc = (status) => {
  const descMap = {
    0: '请尽快完成支付',
    1: '商家正在准备中',
    2: '正在制作中，请稍候',
    3: '商品已制作完成，请及时取餐',
    4: '订单已完成',
    5: '订单已取消'
  }
  return descMap[status] || ''
}

// 格式化时间
const formatTime = (timeStr) => formatDateTime(timeStr)

// 判断是否是咖啡卡订单
const isGiftCardOrder = () => {
  const order = orderDetail.value
  if (!order) return false
  // 兼容多种情况：数字 1、字符串 "1"、或者旧数据通过 deliveryCompany 判断
  const orderType = order.orderType
  if (orderType === 1 || orderType === '1') {
    return true
  }
  // 兼容旧数据：如果 orderType 不存在，使用 deliveryCompany 判断
  if (orderType === null || orderType === undefined) {
    return order.deliveryCompany === '虚拟商品'
  }
  return false
}

// 解析咖啡卡备注 JSON
const parseGiftCardNote = (note) => {
  if (!note || !note.startsWith('GIFT_CARD:')) {
    return {}
  }
  try {
    const jsonStr = note.replace('GIFT_CARD:', '')
    const data = JSON.parse(jsonStr)
    return data
  } catch (e) {
    console.error('解析咖啡卡备注失败', e)
    return {}
  }
}

// 获取咖啡卡备注字段的中文标签
const getGiftCardLabel = (key) => {
  const labelMap = {
    name: '卡片名称',
    greeting: '祝福语',
    validDays: '有效期'
  }
  return labelMap[key] || key
}

// 格式化咖啡卡备注字段的值
const formatGiftCardValue = (key, value) => {
  if (key === 'validDays') {
    if (value === 365) {
      return '1年'
    } else if (value === 730) {
      return '2年'
    } else {
      return `${value}天`
    }
  }
  if (key === 'greeting' && (!value || value.trim() === '')) {
    return '无'
  }
  return value || '无'
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 加载订单详情
const loadOrderDetail = async () => {
  if (!orderId.value) {
    uni.showToast({
      title: '订单ID不能为空',
      icon: 'none'
    })
    setTimeout(() => {
      if (isUnmounted) return
      uni.navigateBack()
    }, 1500)
    return
  }

  try {
    uni.showLoading({
      title: '加载中...'
    })
    const result = await getOrderDetail(orderId.value)
    if (isUnmounted) return
    orderDetail.value = result || {}
  } catch (error) {
    console.error('加载订单详情失败', error)
    if (!isUnmounted) {
      uni.showToast({
        title: error.message || '加载失败',
        icon: 'none'
      })
      setTimeout(() => {
        if (isUnmounted) return
        uni.navigateBack()
      }, 1500)
    }
  } finally {
    if (!isUnmounted) uni.hideLoading()
  }
}

// 点击取消订单
const onCancelOrderClick = () => {
  const reasons = ['不想要了', '商品选错', '信息填写错误', '其他原因']
  uni.showActionSheet({
    itemList: reasons,
    success: (res) => {
      const reason = reasons[res.tapIndex]
      doCancelOrder(reason)
    }
  })
}

// 执行取消逻辑
const doCancelOrder = async (reason) => {
  try {
    uni.showLoading({title: '处理中'})
    // 调用 services/order.js 中的 cancelOrder 方法
    // 注意：你需要确保 services/order.js 中的 cancelOrder 支持传 reason
    // 如果原本只传 orderId，现在需要改成传 { orderId, reason } 或者直接复用 cancelOrder(orderId, reason)
    await apiCancelOrder(orderId.value, reason)
    if (isUnmounted) return

    uni.showToast({title: '订单已取消', icon: 'success'})
    loadOrderDetail() // 刷新详情
  } catch (e) {
    if (!isUnmounted) {
      uni.showToast({title: e.message || '取消失败', icon: 'none'})
    }
  } finally {
    if (!isUnmounted) uni.hideLoading()
  }
}

// 支付订单
const payOrder = () => {
  handlePayOrder(orderDetail.value.id, () => {
    loadOrderDetail()
  })
}

// 确认收货
const confirmReceive = () => {
  handleConfirmReceive(orderId.value, () => {
    loadOrderDetail()
  })
}

onLoad((options) => {
  orderId.value = options.id
})

onMounted(() => {
  statusBarHeight.value = getStatusBarHeight()
  loadOrderDetail()
})
</script>

<style lang="scss" scoped>
$primary: #6f4e37;

.order-detail-page {
  min-height: 100vh;
  background-color: var(--bg-secondary);
  display: flex;
  flex-direction: column;
  transition: background-color 0.3s;
}

/* 顶部导航 */
.nav-bar {
  background-color: var(--bg-primary);
  padding-bottom: 20rpx;
  padding-left: 32rpx;
  padding-right: 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 50;
  box-shadow: 0 2rpx 10rpx var(--shadow-color);
}

.nav-back {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-title {
  font-size: 36rpx;
  font-weight: bold;
  color: var(--text-primary);
}

/* 内容区域 */
.content-scroll {
  flex: 1;
  /* 预留与底部操作栏等高的内边距，避免被遮挡 */
  padding-bottom: 200rpx;
  box-sizing: border-box;
}

/* 订单状态卡片 */
.status-card-new {
  background-color: var(--bg-primary);
  margin: 24rpx 32rpx;
  padding: 40rpx 32rpx;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx var(--shadow-color);
  overflow: hidden;
  position: relative;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

/* 咖啡卡订单状态卡片样式 */
.status-card-new.gift-card-status {
  background: linear-gradient(135deg, var(--bg-tertiary) 0%, var(--bg-primary) 100%) !important;
  border: 2rpx solid #d4af37 !important;
  box-shadow: 0 8rpx 24rpx rgba(212, 175, 55, 0.15) !important;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 40rpx;
  position: relative;
  z-index: 1;
}

.status-info {
  display: flex;
  flex-direction: column;
}

.status-title {
  font-size: 44rpx;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12rpx;
}

.status-desc-text {
  font-size: 26rpx;
  color: var(--text-tertiary);
  margin-bottom: 24rpx;
}

.pickup-code-box {
  background-color: var(--bg-secondary);
  padding: 12rpx 24rpx;
  border-radius: 12rpx;
  display: inline-flex;
  align-items: baseline;
  align-self: flex-start;
}

.code-label {
  font-size: 24rpx;
  color: var(--text-secondary);
  margin-right: 12rpx;
}

.code-value {
  font-size: 40rpx;
  font-weight: bold;
  color: $primary;
  font-family: monospace;
}

.status-icon-bg {
  width: 120rpx;
  height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0.8;
  transform: rotate(10deg);
}

.icon-emoji {
  font-size: 100rpx;
}

/* 步骤条 */
.status-steps {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16rpx;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 2;
}

.step-circle {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background-color: var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
  transition: all 0.3s;
}

.step-item.active .step-circle {
  background-color: $primary;
  box-shadow: 0 2rpx 8rpx rgba(111, 78, 55, 0.3);
}

.step-label {
  font-size: 22rpx;
  color: var(--text-tertiary);
}

.step-item.active .step-label {
  color: $primary;
  font-weight: bold;
}

.step-line {
  flex: 1;
  height: 4rpx;
  background-color: var(--border-light);
  margin: 0 12rpx;
  margin-bottom: 30rpx; /* 对齐圆圈中心 */
  border-radius: 2rpx;
}

.step-line.active {
  background-color: $primary;
}

/* 通用区块 */
.address-section,
.order-info-section,
.goods-section,
.price-section,
.remark-section,
.gift-card-info-section {
  background-color: var(--bg-primary);
  margin: 24rpx 32rpx;
  padding: 32rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx var(--shadow-color);
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

/* 咖啡卡订单费用明细样式 */
.price-section.gift-card-price {
  background: linear-gradient(135deg, var(--bg-tertiary) 0%, var(--bg-primary) 100%) !important;
  border: 2rpx solid #d4af37 !important;
  box-shadow: 0 4rpx 16rpx rgba(212, 175, 55, 0.1) !important;
}

/* 咖啡卡订单信息 */
.gift-card-badge {
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
  background: linear-gradient(135deg, #6f4e37 0%, #8b6f47 100%);
  border-radius: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(111, 78, 55, 0.2);
}

.gift-card-icon {
  font-size: 36rpx;
  margin-right: 16rpx;
}

.gift-card-text {
  font-size: 28rpx;
  font-weight: bold;
  color: #ffffff;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 24rpx;
}

.delivery-info-section {
  background-color: var(--bg-primary);
  margin: 24rpx 32rpx;
  padding: 32rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx var(--shadow-color);
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.highlight-code {
  color: $primary;
  font-weight: bold;
  font-size: 32rpx;
  letter-spacing: 2rpx;
}

.address-box {
  margin-top: 16rpx;
  padding: 16rpx;
  background-color: var(--bg-secondary);
  border-radius: 12rpx;
}

.addr-row {
  display: flex;
  align-items: baseline;
  gap: 16rpx;
  margin-bottom: 8rpx;
}

.addr-name {
  font-size: 28rpx;
  font-weight: bold;
  color: var(--text-primary);
}

.addr-phone {
  font-size: 26rpx;
  color: var(--text-secondary);
}

.addr-detail {
  font-size: 24rpx;
  color: var(--text-secondary);
  line-height: 1.4;
}

/* 订单信息 */
.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid var(--border-light);
}

.info-item:last-child {
  border-bottom: none;
}

/* 咖啡卡订单信息区块样式 */
.order-info-section.gift-card-info {
  background: linear-gradient(135deg, var(--bg-tertiary) 0%, var(--bg-primary) 100%) !important;
  border: 2rpx solid #d4af37 !important;
  box-shadow: 0 4rpx 16rpx rgba(212, 175, 55, 0.1) !important;
}

.order-info-section.gift-card-info .info-item {
  border-bottom-color: rgba(212, 175, 55, 0.2) !important;
}

.info-label {
  font-size: 28rpx;
  color: var(--text-secondary);
}

.info-value {
  font-size: 28rpx;
  color: var(--text-primary);
}

/* 商品列表 */
.goods-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.goods-item {
  display: flex;
  align-items: flex-start;
}

.goods-img {
  width: 160rpx;
  height: 160rpx;
  border-radius: 16rpx;
  background-color: var(--bg-tertiary);
  margin-right: 24rpx;
}

.goods-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 160rpx;
}

.goods-name {
  font-size: 30rpx;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 8rpx;
}

.goods-spec {
  margin-bottom: 16rpx;
}

.spec-text {
  font-size: 24rpx;
  color: var(--text-tertiary);
  background-color: var(--bg-secondary);
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.goods-bottom {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.goods-price {
  color: $primary;
  font-weight: bold;
}

.symbol {
  font-size: 24rpx;
}

.price {
  font-size: 32rpx;
}

.goods-quantity {
  font-size: 26rpx;
  color: var(--text-secondary);
}

/* 费用明细 */
.price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
}

.price-item.total {
  padding-top: 24rpx;
  border-top: 1rpx solid var(--border-light);
  margin-top: 16rpx;
}

.price-label {
  font-size: 28rpx;
  color: var(--text-secondary);
}

.price-value {
  font-size: 28rpx;
  color: var(--text-primary);
}

.price-value.discount {
  color: #ff4d4f;
}

.price-value.total-price {
  font-size: 36rpx;
  font-weight: bold;
  color: $primary;
}

/* 备注 */
.remark-section {
  background-color: var(--bg-primary);
  margin: 24rpx 32rpx;
  padding: 32rpx;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 16rpx var(--shadow-color);
}

.remark-text {
  font-size: 26rpx;
  color: var(--text-secondary);
  line-height: 1.6;
}

/* 咖啡卡备注格式化样式 */
.gift-card-remark {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.remark-item {
  display: flex;
  align-items: flex-start;
  padding: 12rpx 0;
  border-bottom: 1rpx solid var(--border-light);
}

.remark-item:last-child {
  border-bottom: none;
}

.remark-label {
  font-size: 26rpx;
  color: var(--text-secondary);
  min-width: 120rpx;
  flex-shrink: 0;
}

.remark-value {
  font-size: 26rpx;
  color: var(--text-primary);
  font-weight: 500;
  flex: 1;
}

/* 底部操作栏 */
.footer-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: var(--bg-primary);
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 10rpx var(--shadow-color);
  z-index: 40;
}

.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
}

.action-btn {
  height: 80rpx;
  padding: 0 48rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0;
  border: none;
}

.cancel-btn {
  background-color: transparent;
  color: var(--text-secondary);
  border: 2rpx solid var(--border-color);
}

.pay-btn,
.confirm-btn {
  background-color: $primary;
  color: white;
  box-shadow: 0 4rpx 16rpx rgba(111, 78, 55, 0.3);
}
</style>