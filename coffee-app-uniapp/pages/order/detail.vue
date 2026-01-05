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

    <scroll-view scroll-y class="content-scroll" refresher-enabled="true" :refresher-triggered="refreshing" @refresherrefresh="onRefresh">
      <!-- 订单状态卡片 -->
      <view class="status-card-new" :class="{ 'gift-card-status': isGiftCardOrder() }">
        <!-- 上半部分：状态文字与图标 -->
        <view class="status-header">
          <view class="status-info">
            <text class="status-title">{{ getStatusText(orderDetail.status) }}</text>
            <text class="status-desc-text">{{ getStatusDesc(orderDetail.status) }}</text>

            <!-- 取餐码 (仅在商品订单的制作中/待取餐时显示) -->
            <view class="pickup-code-box"
              v-if="!isGiftCardOrder() && [1, 2, 3].includes(orderDetail.status) && orderDetail.pickupCode">
              <text class="code-label">取餐码</text>
              <text class="code-value">{{ orderDetail.pickupCode }}</text>
            </view>
          </view>
        </view>

        <!-- 下半部分：进度条 (仅商品订单进行中时显示) -->
        <view class="status-steps" v-if="!isGiftCardOrder() && [0, 1, 2, 3].includes(orderDetail.status)">
          <view class="step-item" :class="{ active: orderDetail.status >= 0 }">
            <view class="step-circle">
              <uni-icons v-if="orderDetail.status >= 0" type="checkmarkempty" size="12" color="#fff" />
            </view>
            <text class="step-label">已下单</text>
          </view>
          <view class="step-line" :class="{ active: orderDetail.status >= 1 }"></view>
          <view class="step-item" :class="{ active: orderDetail.status >= 1 }">
            <view class="step-circle">
              <uni-icons v-if="orderDetail.status >= 1" type="checkmarkempty" size="12" color="#fff" />
            </view>
            <text class="step-label">待制作</text>
          </view>
          <view class="step-line" :class="{ active: orderDetail.status >= 2 }"></view>
          <view class="step-item" :class="{ active: orderDetail.status >= 2 }">
            <view class="step-circle">
              <uni-icons v-if="orderDetail.status >= 2" type="checkmarkempty" size="12" color="#fff" />
            </view>
            <text class="step-label">制作中</text>
          </view>
          <view class="step-line" :class="{ active: orderDetail.status >= 3 }"></view>
          <view class="step-item" :class="{ active: orderDetail.status >= 3 }">
            <view class="step-circle">
              <uni-icons v-if="orderDetail.status >= 3" type="checkmarkempty" size="12" color="#fff" />
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

      <!-- 配送/取餐信息 (新增强化) - 已取消订单不显示 -->
      <view class="delivery-info-section" v-if="!isGiftCardOrder() && orderDetail.status !== 5">
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
          <!-- 配送地图 (仅外卖订单显示，且订单未取消) - 前置显示，更醒目 -->
          <view class="delivery-map-container" 
            v-if="orderDetail.status !== 5 && storeLocation.latitude && storeLocation.longitude && !refreshing">
            <map :latitude="storeLocation.latitude" :longitude="storeLocation.longitude" :markers="mapMarkers"
              :polyline="polyline" :scale="14" class="delivery-map" show-location 
              :enable-scroll="false" :enable-zoom="false"></map>
            
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

          <view class="info-item">
            <text class="info-label">配送服务</text>
            <text class="info-value">商家配送</text>
          </view>
          <view class="info-item" v-if="orderDetail.deliveryDistance">
            <text class="info-label">配送距离</text>
            <text class="info-value">{{ (orderDetail.deliveryDistance / 1000).toFixed(1) }}km</text>
          </view>
        </template>
      </view>

      <!-- 商品列表（仅商品订单显示） -->
      <view class="goods-section"
        v-if="!isGiftCardOrder() && orderDetail.orderItemList && orderDetail.orderItemList.length > 0">
        <view class="section-title">商品清单</view>
        <view class="goods-list">
          <view v-for="(item, index) in orderDetail.orderItemList" :key="index" class="goods-item">
            <image :src="item.productPic" mode="aspectFill" class="goods-img" />
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
        <button v-if="orderDetail.status === 0 || orderDetail.status === 1" class="action-btn cancel-btn"
          @click="onCancelOrderClick">
          取消订单
        </button>
        <button v-if="orderDetail.status === 0" class="action-btn pay-btn" @click="payOrder">
          立即支付
        </button>
        <button v-if="orderDetail.status === 3" class="action-btn confirm-btn" @click="confirmReceive">
          取餐
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getOrderDetail, cancelOrder as apiCancelOrder } from '@/services/order.js' // 直接引入 API
import { getStatusBarHeight } from '@/utils/system.js'
import { formatDateTime } from '@/utils/date.js'
import { useOrderActions } from '@/composables/useOrderActions.js'
import { useUserStore } from '@/store/user.js'
import { getStoreInfo } from '@/services/store.js'
import { geocode, getWalkingRoute } from '@/services/common.js'

const { handleConfirmReceive, handlePayOrder } = useOrderActions() // 移除 handleCancelOrder，改用本地实现
const userStore = useUserStore()

const statusBarHeight = ref(0)
const orderDetail = ref({})
const orderId = ref(null)
const storeLocation = ref({ latitude: null, longitude: null })
const receiverLocation = ref({ latitude: null, longitude: null })
const mapMarkers = ref([])
const polyline = ref([])
const refreshing = ref(false)
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

// 下拉刷新
const onRefresh = async () => {
  refreshing.value = true
  try {
    // 清空地图相关数据，重新加载
    mapMarkers.value = []
    polyline.value = []
    storeLocation.value = { latitude: null, longitude: null }
    receiverLocation.value = { latitude: null, longitude: null }
    
    // 重新加载订单详情
    await loadOrderDetail()
  } catch (error) {
    console.error('刷新订单详情失败', error)
  } finally {
    refreshing.value = false
  }
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

    // 如果是外卖订单且订单未取消，加载门店信息和收货地址坐标用于地图展示
    if (result && result.deliveryCompany && result.deliveryCompany !== '门店自提' && result.storeId && result.status !== 5) {
      await loadStoreLocation(result.storeId)
      await loadReceiverLocation(result)
    }
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

// 加载门店位置信息（用于地图展示）
const loadStoreLocation = async (storeId) => {
  try {
    const storeInfo = await getStoreInfo(storeId)
    if (isUnmounted) return

    if (storeInfo && storeInfo.latitude && storeInfo.longitude) {
      storeLocation.value = {
        latitude: parseFloat(storeInfo.latitude),
        longitude: parseFloat(storeInfo.longitude)
      }

      // 添加门店标记点
      mapMarkers.value.push({
        id: 1,
        latitude: storeLocation.value.latitude,
        longitude: storeLocation.value.longitude,
        title: storeInfo.name || orderDetail.value.storeName || '门店',
        width: 40,
        height: 40,
        iconPath: '/static/location-fill.png', // 使用Logo作为门店图标
        callout: {
          content: '📍 ' + (storeInfo.name || orderDetail.value.storeName || '门店位置'),
          color: '#333',
          fontSize: 14,
          borderRadius: 6,
          bgColor: '#fff',
          padding: 8,
          display: 'ALWAYS',
          textAlign: 'center',
          anchorY: -10 // 调整气泡位置
        }
      })
    }
  } catch (error) {
    console.error('加载门店位置失败', error)
    // 地图加载失败不影响订单详情展示
  }
}

// 加载收货地址坐标（用于地图展示）
const loadReceiverLocation = async (order) => {
  try {
    // 构建完整地址
    const fullAddress = `${order.receiverProvince || ''}${order.receiverCity || ''}${order.receiverRegion || ''}${order.receiverDetailAddress || ''}`.trim()
    if (!fullAddress) {
      console.warn('收货地址为空，无法进行地理编码')
      return
    }

    // 调用地理编码API
    const result = await geocode(fullAddress, order.receiverCity)
    if (isUnmounted) return

    if (result && result.latitude && result.longitude) {
      receiverLocation.value = {
        latitude: parseFloat(result.latitude),
        longitude: parseFloat(result.longitude)
      }

      // 添加收货地址标记点
      mapMarkers.value.push({
        id: 2,
        latitude: receiverLocation.value.latitude,
        longitude: receiverLocation.value.longitude,
        title: order.receiverName || '收货地址',
        width: 24,
        height: 24,
        iconPath: '/static/tabbar/profile-active.png', // 使用用户图标作为收货地址
        callout: {
          content: '🎯 ' + (order.receiverName || '收货地址'),
          color: '#333',
          fontSize: 14,
          borderRadius: 6,
          bgColor: '#fff',
          padding: 8,
          display: 'ALWAYS',
          textAlign: 'center',
          anchorY: -10
        }
      })

      // 如果门店位置和收货地址都已加载，绘制配送路线
      if (storeLocation.value.latitude && storeLocation.value.longitude) {
        drawDeliveryRoute()
      }
    } else {
      console.warn('地理编码失败，收货地址：', fullAddress)
    }
  } catch (error) {
    console.error('加载收货地址位置失败', error)
    // 地址解析失败不影响订单详情展示
  }
}

// 绘制配送路线（从门店到收货地址）- 使用真实路线规划
const drawDeliveryRoute = async () => {
  if (!storeLocation.value.latitude || !receiverLocation.value.latitude) {
    return
  }
  
  try {
    // 构建起点和终点坐标字符串
    const origin = `${storeLocation.value.longitude},${storeLocation.value.latitude}`
    const destination = `${receiverLocation.value.longitude},${receiverLocation.value.latitude}`
    
    // 调用路线规划API获取真实路线坐标点
    const response = await getWalkingRoute(origin, destination)
    if (isUnmounted) return
    
    // 检查API返回的数据结构
    const routePoints = response?.data || response
    
    if (routePoints && Array.isArray(routePoints) && routePoints.length > 0) {
      // 将坐标点转换为地图polyline需要的格式
      const points = routePoints.map(point => {
        const [lng, lat] = point.split(',')
        return {
          latitude: parseFloat(lat),
          longitude: parseFloat(lng)
        }
      }).filter(p => !isNaN(p.latitude) && !isNaN(p.longitude)) // 过滤无效坐标
      
      if (points.length > 0) {
        polyline.value = [{
          points: points,
          color: '#6f4e37',
          width: 4,
          dottedLine: false,
          arrowLine: true
        }]
        return // 成功绘制路线，直接返回
      }
    }
    
    // 如果路线规划失败或返回空数据，降级为两点连线
    console.warn('路线规划失败或返回空数据，使用两点连线')
    drawFallbackRoute()
  } catch (error) {
    console.error('获取路线规划失败', error)
    // 降级为两点连线
    drawFallbackRoute()
  }
}

// 绘制降级路线（两点连线）
const drawFallbackRoute = () => {
  if (!storeLocation.value.latitude || !receiverLocation.value.latitude) {
    return
  }
  
  polyline.value = [{
    points: [
      {
        latitude: storeLocation.value.latitude,
        longitude: storeLocation.value.longitude
      },
      {
        latitude: receiverLocation.value.latitude,
        longitude: receiverLocation.value.longitude
      }
    ],
    color: '#6f4e37',
    width: 4,
    dottedLine: false,
    arrowLine: true
  }]
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
    uni.showLoading({ title: '处理中' })
    // 调用 services/order.js 中的 cancelOrder 方法
    // 注意：你需要确保 services/order.js 中的 cancelOrder 支持传 reason
    // 如果原本只传 orderId，现在需要改成传 { orderId, reason } 或者直接复用 cancelOrder(orderId, reason)
    await apiCancelOrder(orderId.value, reason)
    if (isUnmounted) return

    uni.showToast({ title: '订单已取消', icon: 'success' })
    loadOrderDetail() // 刷新详情
  } catch (e) {
    if (!isUnmounted) {
      uni.showToast({ title: e.message || '取消失败', icon: 'none' })
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
$bg-color: #f8f8f8;
$card-bg: #ffffff;
$text-main: #333333;
$text-sub: #666666;
$text-light: #999999;
$border-color: #eeeeee;

.order-detail-page {
  min-height: 100vh;
  background-color: $bg-color;
  display: flex;
  flex-direction: column;
  padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
}

/* 顶部导航 */
.nav-bar {
  background-color: $card-bg;
  padding: 20rpx 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 100;
  // 移除阴影，保持简洁
  // box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05); 
}

.nav-back {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: -16rpx; // 修正视觉偏差
}

.page-title {
  font-size: 34rpx;
  font-weight: 600;
  color: $text-main;
}

/* 内容区域 */
.content-scroll {
  flex: 1;
  padding: 24rpx;
  box-sizing: border-box;
}

/* 通用卡片样式 */
.status-card-new,
.delivery-info-section,
.order-info-section,
.goods-section,
.price-section,
.remark-section,
.gift-card-info-section {
  background-color: $card-bg;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.03); // 更柔和的阴影
  border: none; // 移除边框
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: $text-main;
  margin-bottom: 24rpx;
  position: relative;
  display: flex;
  align-items: center;
  
  &::before {
    content: '';
    width: 6rpx;
    height: 24rpx;
    background-color: $primary;
    border-radius: 4rpx;
    margin-right: 16rpx;
    display: block;
  }
}

/* 订单状态卡片 */
.status-card-new {
  padding: 40rpx 32rpx;
  
  &.gift-card-status {
    background: linear-gradient(135deg, #3d3d3d 0%, #1a1a1a 100%);
    
    .status-title, .status-desc-text {
      color: #ffd700;
    }
    .step-item.active .step-label {
      color: #ffd700;
    }
    .step-item.active .step-circle {
      background-color: #ffd700;
    }
    .step-line.active {
      background-color: #ffd700;
    }
  }
}

.status-header {
  margin-bottom: 40rpx;
}

.status-title {
  font-size: 40rpx;
  font-weight: bold;
  color: $text-main;
  margin-bottom: 8rpx;
}

.status-desc-text {
  font-size: 26rpx;
  color: $text-sub;
}

.pickup-code-box {
  margin-top: 24rpx;
  background-color: rgba($primary, 0.08);
  padding: 16rpx 32rpx;
  border-radius: 12rpx;
  display: inline-block;
  
  .code-label {
    font-size: 24rpx;
    color: $text-sub;
    margin-right: 12rpx;
  }
  
  .code-value {
    font-size: 48rpx;
    font-weight: bold;
    color: $primary;
    font-family: monospace;
    letter-spacing: 2rpx;
  }
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
  min-width: 80rpx;
}

.step-circle {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background-color: #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
  transition: all 0.3s;
  border: 4rpx solid $card-bg; // 增加白边，增加层次感
}

.step-item.active .step-circle {
  background-color: $primary;
  box-shadow: 0 0 0 4rpx rgba($primary, 0.2); // 外发光效果
}

.step-label {
  font-size: 22rpx;
  color: $text-light;
  font-weight: 500;
}

.step-item.active .step-label {
  color: $primary;
  font-weight: 600;
}

.step-line {
  flex: 1;
  height: 4rpx;
  background-color: #e0e0e0;
  margin: 0 -20rpx; // 负margin连接圆点
  margin-bottom: 40rpx; // 对齐圆点中心
  position: relative;
  z-index: 1;
}

.step-line.active {
  background-color: $primary;
}

/* 配送信息 */
.info-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20rpx 0;
  border-bottom: 1rpx solid $border-color;
  
  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  font-size: 28rpx;
  color: $text-sub;
  min-width: 140rpx;
}

.info-value {
  font-size: 28rpx;
  color: $text-main;
  text-align: right;
  flex: 1;
  font-weight: 500;
  
  &.highlight-code {
    color: $primary;
    font-weight: bold;
    font-size: 32rpx;
  }
}

.address-box {
  background-color: #f9f9f9;
  padding: 24rpx;
  border-radius: 16rpx;
  margin-top: 24rpx;
  
  .addr-row {
    margin-bottom: 12rpx;
    display: flex;
    align-items: baseline;
    
    .addr-name {
      font-size: 30rpx;
      font-weight: 600;
      color: $text-main;
      margin-right: 16rpx;
    }
    
    .addr-phone {
      font-size: 26rpx;
      color: $text-sub;
    }
  }
  
  .addr-detail {
    font-size: 26rpx;
    color: $text-sub;
    line-height: 1.5;
  }
}

/* 地图容器 */
.delivery-map-container {
  margin-top: 24rpx;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
  position: relative;
  z-index: 1;
  transform: translateZ(0); /* 开启硬件加速，让地图跟随滚动 */
  -webkit-transform: translateZ(0);
}

.delivery-map {
  width: 100%;
  height: 360rpx;
  position: relative;
}

.map-tips {
  padding: 16rpx 24rpx;
  background-color: $card-bg;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  
  .tips-text {
    font-size: 24rpx;
    color: $text-sub;
    display: flex;
    align-items: center;
    
    &::before {
      margin-right: 8rpx;
    }
  }
}

/* 商品列表 */
.goods-item {
  display: flex;
  margin-bottom: 32rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .goods-img {
    width: 140rpx;
    height: 140rpx;
    border-radius: 16rpx;
    background-color: #f5f5f5;
    margin-right: 24rpx;
    flex-shrink: 0;
  }
  
  .goods-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 4rpx 0;
  }
  
  .goods-name {
    font-size: 28rpx;
    font-weight: 600;
    color: $text-main;
    margin-bottom: 8rpx;
    line-height: 1.4;
  }
  
  .goods-spec {
    display: flex;
    flex-wrap: wrap;
    
    .spec-text {
      font-size: 22rpx;
      color: $text-light;
      background-color: #f5f5f5;
      padding: 4rpx 12rpx;
      border-radius: 8rpx;
      margin-right: 12rpx;
      margin-bottom: 8rpx;
    }
  }
  
  .goods-bottom {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    
    .goods-price {
      color: $text-main;
      font-weight: 600;
      
      .symbol {
        font-size: 24rpx;
      }
      .price {
        font-size: 32rpx;
      }
    }
    
    .goods-quantity {
      font-size: 26rpx;
      color: $text-light;
    }
  }
}

/* 费用明细 */
.price-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20rpx;
  font-size: 26rpx;
  color: $text-sub;
  
  &.total {
    margin-top: 32rpx;
    padding-top: 32rpx;
    border-top: 1rpx dashed $border-color;
    margin-bottom: 0;
    align-items: flex-end;
    
    .price-label {
      font-size: 28rpx;
      font-weight: 500;
      color: $text-main;
    }
    
    .total-price {
      font-size: 40rpx;
      font-weight: bold;
      color: $primary;
    }
  }
  
  .price-value {
    font-weight: 500;
    color: $text-main;
    
    &.discount {
      color: #ff4d4f;
    }
  }
}

/* 底部操作栏 */
.footer-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: $card-bg;
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
  z-index: 99;
}

.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 20rpx;
}

.action-btn {
  min-width: 160rpx;
  height: 72rpx;
  padding: 0 32rpx;
  border-radius: 36rpx;
  font-size: 28rpx;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0;
  
  &::after {
    border: none;
  }
}

.cancel-btn {
  background-color: #f5f5f5;
  color: $text-sub;
  border: 1rpx solid #e0e0e0;
}

.pay-btn, .confirm-btn {
  background-color: $primary;
  color: #fff;
  box-shadow: 0 4rpx 12rpx rgba($primary, 0.3);
}

/* 备注 */
.remark-text {
  font-size: 26rpx;
  color: $text-sub;
  line-height: 1.6;
  background-color: #f9f9f9;
  padding: 20rpx;
  border-radius: 12rpx;
  display: block;
}

/* 咖啡卡信息 */
.gift-card-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24rpx;
  background: linear-gradient(135deg, #4a4a4a 0%, #2b2b2b 100%);
  border-radius: 16rpx;
  color: #ffd700;
  
  .gift-card-icon {
    font-size: 40rpx;
    margin-right: 16rpx;
  }
  
  .gift-card-text {
    font-size: 30rpx;
    font-weight: bold;
  }
}
</style>