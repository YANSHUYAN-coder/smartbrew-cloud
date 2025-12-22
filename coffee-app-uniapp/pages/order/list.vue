<template>
	<view class="order-list-page">
		<!-- 顶部导航 -->
		<view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-back" @click="goBack">
				<uni-icons type="left" size="24" color="#333"></uni-icons>
			</view>
			<text class="page-title">我的订单</text>
			<view style="width: 48rpx;"></view>
		</view>

		<!-- 状态筛选标签 -->
		<scroll-view class="status-tabs" scroll-x :show-scrollbar="false">
			<view class="status-tabs-content">
				<view 
					v-for="tab in statusTabs" 
					:key="tab.status"
					class="tab-item"
					:class="{ active: currentStatus === tab.status }"
					@click="switchStatus(tab.status)"
				>
					<text class="tab-text">{{ tab.label }}</text>
				</view>
			</view>
		</scroll-view>

		<!-- 订单列表 -->
		<scroll-view 
			scroll-y 
			class="order-scroll"
			:style="{ height: `calc(100vh - ${statusBarHeight + 44 + 100 + 100}px)` }"
			:refresher-enabled="true"
			:refresher-triggered="refreshing"
			@refresherrefresh="onRefresh"
			@refresherrestore="onRefreshRestore"
			@scrolltolower="loadMore"
		>
			<view class="order-list" v-if="orderList.length > 0">
				<view 
					v-for="order in orderList" 
					:key="order.id"
					class="order-card"
					@click="goToDetail(order.id)"
				>
					<!-- 订单头部 -->
					<view class="order-header">
						<view class="order-info">
							<text class="order-sn">订单号：{{ order.orderSn }}</text>
							<text class="order-time">{{ formatTime(order.createTime) }}</text>
						</view>
						<view class="order-status" :class="getStatusClass(order.status)">
							{{ getStatusText(order.status) }}
						</view>
					</view>

					<!-- 订单商品（显示前2个） -->
					<view class="order-goods">
						<view 
							v-for="(item, index) in (order.orderItemList || []).slice(0, 2)" 
							:key="index"
							class="goods-item"
						>
							<image :src="item.productPic" mode="aspectFill" class="goods-img" lazy-load />
							<view class="goods-info">
								<text class="goods-name">{{ item.productName }}</text>
								<view class="goods-spec" v-if="item.productAttr">
									<text class="spec-text">{{ item.productAttr }}</text>
								</view>
								<view class="goods-bottom">
									<text class="goods-price">¥{{ item.productPrice }}</text>
									<text class="goods-quantity">x{{ item.productQuantity }}</text>
								</view>
							</view>
						</view>
						<view v-if="(order.orderItemList || []).length > 2" class="more-goods">
							<text class="more-text">还有 {{ (order.orderItemList || []).length - 2 }} 件商品</text>
						</view>
					</view>

					<!-- 订单底部 -->
					<view class="order-footer">
						<view class="order-total">
							<text class="total-label">共 {{ getTotalCount(order) }} 件商品，合计：</text>
							<text class="total-price">¥{{ order.payAmount }}</text>
						</view>
						<view class="order-actions">
							<button 
								v-if="order.status === 0" 
								class="action-btn cancel-btn"
								@click.stop="cancelOrder(order)"
							>
								取消订单
							</button>
							<button 
								v-if="order.status === 0" 
								class="action-btn pay-btn"
								@click.stop="payOrder(order)"
							>
								立即支付
							</button>
							<button 
								v-if="order.status === 3" 
								class="action-btn confirm-btn"
								@click.stop="confirmReceive(order)"
							>
								确认收货
							</button>
						</view>
					</view>
				</view>

				<!-- 加载更多提示 -->
				<view class="load-more" v-if="hasMore">
					<text class="load-text">加载中...</text>
				</view>
				<view class="load-more" v-else-if="orderList.length > 0">
					<text class="load-text">没有更多了</text>
				</view>

				<view style="height: 40rpx;"></view>
			</view>

			<!-- 空状态 -->
			<view class="empty-state" v-else>
				<image src="https://img.icons8.com/bubbles/200/package.png" class="empty-img" />
				<text class="empty-text">暂无订单</text>
				<text class="empty-sub">快去下单吧</text>
				<button class="go-shop-btn" @click="goToMenu">去点单</button>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getOrderList } from '@/services/order.js'
import { getStatusBarHeight } from '@/utils/system.js'
import { alipay } from '@/services/pay.js'
import { useOrderActions } from '@/composables/useOrderActions.js'

const { handleCancelOrder } = useOrderActions()

const statusBarHeight = ref(0)
const currentStatus = ref(null) // null 表示全部
const orderList = ref([])
const refreshing = ref(false)
const loading = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 状态筛选标签
const statusTabs = [
	{ label: '全部', status: null },
	{ label: '待付款', status: 0 },
	{ label: '待制作', status: 1 },
	{ label: '制作中', status: 2 },
	{ label: '已完成', status: 3 },
	{ label: '已取消', status: 4 }
]

// 获取订单状态文本
const getStatusText = (status) => {
	const statusMap = {
		0: '待付款',
		1: '待制作',
		2: '制作中',
		3: '已完成',
		4: '已关闭',
		5: '无效订单'
	}
	return statusMap[status] || '未知'
}

// 获取订单状态样式类
const getStatusClass = (status) => {
	const classMap = {
		0: 'status-pending',
		1: 'status-waiting',
		2: 'status-making',
		3: 'status-ready',
		4: 'status-cancelled',
		5: 'status-cancelled'
	}
	return classMap[status] || ''
}

// 格式化时间
const formatTime = (timeStr) => {
	if (!timeStr) return ''
	const date = new Date(timeStr)
	const month = String(date.getMonth() + 1).padStart(2, '0')
	const day = String(date.getDate()).padStart(2, '0')
	const hours = String(date.getHours()).padStart(2, '0')
	const minutes = String(date.getMinutes()).padStart(2, '0')
	return `${month}-${day} ${hours}:${minutes}`
}

// 计算订单商品总数
const getTotalCount = (order) => {
	if (!order.orderItemList) return 0
	return order.orderItemList.reduce((sum, item) => sum + item.productQuantity, 0)
}

// 返回上一页
const goBack = () => {
	uni.navigateBack()
}

// 跳转到菜单页
const goToMenu = () => {
	uni.switchTab({
		url: '/pages/menu/index'
	})
}

// 跳转到订单详情
const goToDetail = (orderId) => {
	uni.navigateTo({
		url: `/pages/order/detail?id=${orderId}`
	})
}

// 切换状态筛选
const switchStatus = (status) => {
	if (currentStatus.value === status) return
	currentStatus.value = status
	page.value = 1
	hasMore.value = true
	loadOrderList(true)
}

// 加载订单列表
const loadOrderList = async (reset = false) => {
	if (loading.value || (!hasMore.value && !reset)) return
	
	try {
		loading.value = true
		const currentPage = reset ? 1 : page.value
		
		const params = {
			page: currentPage,
			pageSize: pageSize.value
		}
		if (currentStatus.value !== null) {
			params.status = currentStatus.value
		}
		
		const result = await getOrderList(params)
		
		// 处理分页数据
		const records = result.records || result.data?.records || result.list || []
		total.value = result.total || 0
		
		if (reset) {
			orderList.value = records
			page.value = 1
		} else {
			orderList.value = [...orderList.value, ...records]
		}
		
		// 判断是否还有更多
		hasMore.value = orderList.value.length < total.value
		page.value = currentPage + 1
		
	} catch (error) {
		console.error('加载订单列表失败', error)
		uni.showToast({
			title: '加载失败，请重试',
			icon: 'none'
		})
	} finally {
		loading.value = false
		refreshing.value = false
	}
}

// 下拉刷新
const onRefresh = async () => {
	refreshing.value = true
	await loadOrderList(true)
}

// 刷新恢复
const onRefreshRestore = () => {
	refreshing.value = false
}

// 加载更多
const loadMore = () => {
	if (!loading.value && hasMore.value) {
		loadOrderList(false)
	}
}

// 取消订单
const cancelOrder = (order) => {
	handleCancelOrder(order, () => {
		loadOrderList(true)
	})
}

// 支付订单
const payOrder = (order) => {
	handlePayOrder(order, () => {
		loadOrderList(true)
	})
}

// 确认收货
const confirmReceive = (order) => {
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

onMounted(() => {
	statusBarHeight.value = getStatusBarHeight()
	loadOrderList(true)
})

onShow(() => {
	// 从订单详情页返回时，刷新列表
	loadOrderList(true)
})
</script>

<style lang="scss" scoped>
$primary: #6f4e37;
$bg-color: #f7f8fa;

.order-list-page {
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

/* 状态筛选标签 */
.status-tabs {
	background-color: white;
	width: 100%;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.03);
}

.status-tabs-content {
	display: inline-flex;
	gap: 24rpx;
	padding: 20rpx 32rpx;
	white-space: nowrap;
}

.tab-item {
	padding: 12rpx 24rpx;
	border-radius: 30rpx;
	background-color: #f5f5f5;
	transition: all 0.3s;
	flex-shrink: 0;
}

.tab-item.active {
	background-color: $primary;
}

.tab-text {
	font-size: 26rpx;
	color: #666;
}

.tab-item.active .tab-text {
	color: white;
	font-weight: bold;
}

/* 订单列表 */
.order-scroll {
	flex: 1;
}

.order-list {
	padding: 24rpx 32rpx;
}

.order-card {
	background-color: white;
	border-radius: 24rpx;
	padding: 32rpx;
	margin-bottom: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.order-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 24rpx;
	padding-bottom: 24rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.order-info {
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.order-sn {
	font-size: 28rpx;
	font-weight: bold;
	color: #333;
}

.order-time {
	font-size: 24rpx;
	color: #999;
}

.order-status {
	font-size: 26rpx;
	font-weight: bold;
	padding: 8rpx 16rpx;
	border-radius: 8rpx;
}

.status-pending { color: #ff4d4f; }
.status-waiting { color: #faad14; }
.status-making { color: #1890ff; }
.status-ready { color: #52c41a; }
.status-completed { color: #999; }
.status-cancelled { color: #999; }

.order-goods {
	margin-bottom: 24rpx;
}

.goods-item {
	display: flex;
	align-items: flex-start;
	margin-bottom: 16rpx;
}

.goods-item:last-child {
	margin-bottom: 0;
}

.goods-img {
	width: 120rpx;
	height: 120rpx;
	border-radius: 12rpx;
	background-color: #f9f9f9;
	margin-right: 20rpx;
}

.goods-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	min-height: 120rpx;
}

.goods-name {
	font-size: 28rpx;
	font-weight: 500;
	color: #333;
	margin-bottom: 8rpx;
}

.goods-spec {
	margin-bottom: 12rpx;
}

.spec-text {
	font-size: 22rpx;
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
	font-size: 28rpx;
	font-weight: bold;
	color: $primary;
}

.goods-quantity {
	font-size: 24rpx;
	color: #999;
}

.more-goods {
	padding-top: 16rpx;
	border-top: 1rpx solid #f0f0f0;
	text-align: center;
}

.more-text {
	font-size: 24rpx;
	color: #999;
}

.order-footer {
	padding-top: 24rpx;
	border-top: 1rpx solid #f0f0f0;
}

.order-total {
	display: flex;
	justify-content: flex-end;
	align-items: baseline;
	margin-bottom: 20rpx;
}

.total-label {
	font-size: 26rpx;
	color: #666;
	margin-right: 8rpx;
}

.total-price {
	font-size: 32rpx;
	font-weight: bold;
	color: $primary;
}

.order-actions {
	display: flex;
	justify-content: flex-end;
	gap: 16rpx;
}

.action-btn {
	height: 64rpx;
	padding: 0 32rpx;
	border-radius: 32rpx;
	font-size: 26rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	margin: 0;
	border: none;
}

.cancel-btn {
	background-color: transparent;
	color: #666;
	border: 1rpx solid #ddd;
}

.pay-btn {
	background-color: $primary;
	color: white;
}

.confirm-btn {
	background-color: $primary;
	color: white;
}

/* 加载更多 */
.load-more {
	padding: 40rpx 0;
	text-align: center;
}

.load-text {
	font-size: 24rpx;
	color: #999;
}

/* 空状态 */
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding-top: 200rpx;
}

.empty-img {
	width: 240rpx;
	height: 240rpx;
	margin-bottom: 32rpx;
	opacity: 0.8;
}

.empty-text {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 12rpx;
}

.empty-sub {
	font-size: 26rpx;
	color: #999;
	margin-bottom: 48rpx;
}

.go-shop-btn {
	background-color: white;
	color: $primary;
	border: 2rpx solid $primary;
	padding: 0 64rpx;
	height: 80rpx;
	line-height: 76rpx;
	border-radius: 40rpx;
	font-size: 28rpx;
	font-weight: bold;
}
</style>

