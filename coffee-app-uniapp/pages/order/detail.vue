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

		<scroll-view scroll-y class="content-scroll" :style="{ height: `calc(100vh - ${statusBarHeight + 44 + 120}px)` }">
			<!-- 订单状态卡片 -->
			<view class="status-card">
				<view class="status-icon" :class="getStatusClass(orderDetail.status)">
					<text class="icon-text">{{ getStatusIcon(orderDetail.status) }}</text>
				</view>
				<text class="status-text">{{ getStatusText(orderDetail.status) }}</text>
				<text class="status-desc" v-if="getStatusDesc(orderDetail.status)">
					{{ getStatusDesc(orderDetail.status) }}
				</text>
			</view>

			<!-- 收货地址 -->
			<view class="address-section" v-if="orderDetail.receiverName">
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

			<!-- 商品列表 -->
			<view class="goods-section">
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

			<view style="height: 40rpx;"></view>
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
					确认收货
				</button>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getOrderDetail, cancelOrder as cancelOrderApi } from '@/services/order.js'
import { getStatusBarHeight } from '@/utils/system.js'

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
	uni.showModal({
		title: '提示',
		content: '确定要取消该订单吗？',
		success: async (res) => {
			if (res.confirm) {
				try {
					await cancelOrderApi(orderId.value)
					uni.showToast({
						title: '取消成功',
						icon: 'success'
					})
					// 重新加载详情
					loadOrderDetail()
				} catch (error) {
					uni.showToast({
						title: error.message || '取消失败',
						icon: 'none'
					})
				}
			}
		}
	})
}

// 支付订单
const payOrder = () => {
	uni.showToast({
		title: '支付功能开发中',
		icon: 'none'
	})
	// TODO: 实现支付逻辑
}

// 确认收货
const confirmReceive = () => {
	uni.showModal({
		title: '提示',
		content: '确定已收到商品吗？',
		success: async (res) => {
			if (res.confirm) {
				// TODO: 调用确认收货接口
				uni.showToast({
					title: '确认收货功能开发中',
					icon: 'none'
				})
			}
		}
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
}

/* 订单状态卡片 */
.status-card {
	background: linear-gradient(135deg, $primary 0%, #8b6f4f 100%);
	margin: 24rpx 32rpx;
	padding: 48rpx 32rpx;
	border-radius: 24rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	color: white;
	box-shadow: 0 8rpx 24rpx rgba(111, 78, 55, 0.3);
}

.status-icon {
	width: 120rpx;
	height: 120rpx;
	border-radius: 50%;
	background-color: rgba(255, 255, 255, 0.2);
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: 24rpx;
}

.icon-text {
	font-size: 64rpx;
}

.status-text {
	font-size: 36rpx;
	font-weight: bold;
	margin-bottom: 12rpx;
}

.status-desc {
	font-size: 24rpx;
	opacity: 0.9;
}

/* 通用区块 */
.address-section,
.order-info-section,
.goods-section,
.price-section,
.remark-section {
	background-color: white;
	margin: 24rpx 32rpx;
	padding: 32rpx;
	border-radius: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
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

