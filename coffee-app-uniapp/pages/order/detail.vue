<template>
	<view class="order-detail-page">
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
			<view class="status-card-new">
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
						<view class="step-circle"><uni-icons v-if="orderDetail.status >= 0" type="checkmarkempty" size="12" color="#fff" /></view>
						<text class="step-label">已下单</text>
					</view>
					<view class="step-line" :class="{ active: orderDetail.status >= 1 }"></view>
					<view class="step-item" :class="{ active: orderDetail.status >= 1 }">
						<view class="step-circle"><uni-icons v-if="orderDetail.status >= 1" type="checkmarkempty" size="12" color="#fff" /></view>
						<text class="step-label">制作中</text>
					</view>
					<view class="step-line" :class="{ active: orderDetail.status >= 3 }"></view>
					<view class="step-item" :class="{ active: orderDetail.status >= 3 }">
						<view class="step-circle"><uni-icons v-if="orderDetail.status >= 3" type="checkmarkempty" size="12" color="#fff" /></view>
						<text class="step-label">待取餐</text>
					</view>
				</view>
			</view>

			<!-- 收货地址（仅商品订单显示） -->
			<view class="address-section" v-if="!isGiftCardOrder() && orderDetail.receiverName && orderDetail.receiverName !== '虚拟商品'">
				<view class="section-title">收货信息</view>
				<view class="address-info">
					<view class="address-header">
						<text class="receiver-name">{{ orderDetail.receiverName }}</text>
						<text class="receiver-phone">{{ orderDetail.receiverPhone }}</text>
					</view>
					<view class="address-detail">
						{{ orderDetail.receiverProvince }}{{ orderDetail.receiverCity }}{{ orderDetail.receiverRegion }} {{ orderDetail.receiverDetailAddress }}
					</view>
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
			<view class="order-info-section">
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
				<view class="info-item" v-if="orderDetail.deliveryCompany">
					<text class="info-label">配送方式</text>
					<text class="info-value">{{ orderDetail.deliveryCompany }}</text>
				</view>
			</view>

			<!-- 商品列表（仅商品订单显示） -->
			<view class="goods-section" v-if="!isGiftCardOrder() && orderDetail.orderItemList && orderDetail.orderItemList.length > 0">
				<view class="section-title">商品清单</view>
				<view class="goods-list">
					<view 
						v-for="(item, index) in orderDetail.orderItemList" 
						:key="index"
						class="goods-item"
					>
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
			<view class="price-section">
				<view class="section-title">费用明细</view>
				<view class="price-item">
					<text class="price-label">商品合计</text>
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
				<view class="price-item total">
					<text class="price-label">实付金额</text>
					<text class="price-value total-price">¥{{ orderDetail.payAmount || 0 }}</text>
				</view>
			</view>

			<!-- 备注 -->
			<view class="remark-section" v-if="orderDetail.note">
				<view class="section-title">订单备注</view>
				<text class="remark-text">{{ orderDetail.note }}</text>
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
					@click="cancelOrder"
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
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getOrderDetail } from '@/services/order.js'
import { getStatusBarHeight } from '@/utils/system.js'
import { useOrderActions } from '@/composables/useOrderActions.js'

const { handleCancelOrder, handleConfirmReceive, handlePayOrder } = useOrderActions()

const statusBarHeight = ref(0)
const orderDetail = ref({})
const orderId = ref(null)

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
	if (status === 4) return '已关闭'
	return statusMap[status] || '未知'
}

// 获取订单状态图标
const getStatusIcon = (status) => {
	const iconMap = {
		0: '💳',
		1: '⏰',
		2: '☕',
		3: '🚚',
		4: '✓',
		5: '✗'
	}
	if (status === 4) return '✗'
	return iconMap[status] || '📦'
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
	if (status === 4) return '订单已关闭'
	return descMap[status] || ''
}

// 获取订单状态样式类
const getStatusClass = (status) => {
	const classMap = {
		0: 'status-pending',
		1: 'status-waiting',
		2: 'status-making',
		3: 'status-ready',
		4: 'status-completed',
		5: 'status-cancelled'
	}
	if (status === 4) return 'status-cancelled'
	return classMap[status] || ''
}

// 格式化时间
const formatTime = (timeStr) => {
	if (!timeStr) return ''
	const date = new Date(timeStr)
	const year = date.getFullYear()
	const month = String(date.getMonth() + 1).padStart(2, '0')
	const day = String(date.getDate()).padStart(2, '0')
	const hours = String(date.getHours()).padStart(2, '0')
	const minutes = String(date.getMinutes()).padStart(2, '0')
	return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 判断是否是咖啡卡订单
const isGiftCardOrder = () => {
	return orderDetail.value.deliveryCompany === '虚拟商品'
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
			uni.navigateBack()
		}, 1500)
		return
	}

	try {
		uni.showLoading({
			title: '加载中...'
		})
		const result = await getOrderDetail(orderId.value)
		orderDetail.value = result || {}
	} catch (error) {
		console.error('加载订单详情失败', error)
		uni.showToast({
			title: error.message || '加载失败',
			icon: 'none'
		})
		setTimeout(() => {
			uni.navigateBack()
		}, 1500)
	} finally {
		uni.hideLoading()
	}
}

// 取消订单
const cancelOrder = () => {
	handleCancelOrder(orderId.value, () => {
		loadOrderDetail()
	})
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
$bg-color: #f7f8fa;

.order-detail-page {
	min-height: 100vh;
	background-color: $bg-color;
	display: flex;
	flex-direction: column;
}

/* 顶部导航 */
.nav-bar {
	background-color: white;
	padding-bottom: 20rpx;
	padding-left: 32rpx;
	padding-right: 32rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	position: sticky;
	top: 0;
	z-index: 50;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.03);
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
	color: #333;
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
	background-color: white;
	margin: 24rpx 32rpx;
	padding: 40rpx 32rpx;
	border-radius: 24rpx;
	box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.04);
	overflow: hidden;
	position: relative;
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
	color: #333;
	margin-bottom: 12rpx;
}

.status-desc-text {
	font-size: 26rpx;
	color: #999;
	margin-bottom: 24rpx;
}

.pickup-code-box {
	background-color: #f7f8fa;
	padding: 12rpx 24rpx;
	border-radius: 12rpx;
	display: inline-flex;
	align-items: baseline;
	align-self: flex-start;
}

.code-label {
	font-size: 24rpx;
	color: #666;
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
	background-color: #e0e0e0;
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
	color: #999;
}

.step-item.active .step-label {
	color: $primary;
	font-weight: bold;
}

.step-line {
	flex: 1;
	height: 4rpx;
	background-color: #f0f0f0;
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
	background-color: white;
	margin: 24rpx 32rpx;
	padding: 32rpx;
	border-radius: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

/* 咖啡卡订单信息 */
.gift-card-badge {
	display: flex;
	align-items: center;
	padding: 20rpx 24rpx;
	background: linear-gradient(135deg, #6f4e37 0%, #8b6f47 100%);
	border-radius: 16rpx;
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
	color: #333;
	margin-bottom: 24rpx;
}

/* 收货地址 */
.address-info {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.address-header {
	display: flex;
	align-items: center;
	gap: 24rpx;
}

.receiver-name {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.receiver-phone {
	font-size: 28rpx;
	color: #666;
}

.address-detail {
	font-size: 26rpx;
	color: #666;
	line-height: 1.6;
}

/* 订单信息 */
.info-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx 0;
	border-bottom: 1rpx solid #f0f0f0;
}

.info-item:last-child {
	border-bottom: none;
}

.info-label {
	font-size: 28rpx;
	color: #666;
}

.info-value {
	font-size: 28rpx;
	color: #333;
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
	background-color: #f9f9f9;
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
	color: #333;
	margin-bottom: 8rpx;
}

.goods-spec {
	margin-bottom: 16rpx;
}

.spec-text {
	font-size: 24rpx;
	color: #999;
	background-color: #f7f8fa;
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
	color: #666;
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
	border-top: 1rpx solid #f0f0f0;
	margin-top: 16rpx;
}

.price-label {
	font-size: 28rpx;
	color: #666;
}

.price-value {
	font-size: 28rpx;
	color: #333;
}

.price-value.discount {
	color: #52c41a;
}

.price-value.total-price {
	font-size: 36rpx;
	font-weight: bold;
	color: $primary;
}

/* 备注 */
.remark-text {
	font-size: 26rpx;
	color: #666;
	line-height: 1.6;
}

/* 底部操作栏 */
.footer-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background-color: white;
	padding: 24rpx 32rpx;
	padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
	box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
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
	color: #666;
	border: 2rpx solid #ddd;
}

.pay-btn,
.confirm-btn {
	background-color: $primary;
	color: white;
	box-shadow: 0 4rpx 16rpx rgba(111, 78, 55, 0.3);
}
</style>

