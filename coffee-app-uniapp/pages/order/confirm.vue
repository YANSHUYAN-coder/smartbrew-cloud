<template>
	<view class="order-confirm-page">
		<!-- 顶部导航 -->
		<view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-back" @click="goBack">
				<uni-icons type="left" size="24" color="#333"></uni-icons>
			</view>
			<text class="page-title">确认订单</text>
			<view style="width: 48rpx;"></view>
		</view>

		<scroll-view scroll-y class="content-scroll" :style="{ height: `calc(100vh - ${statusBarHeight + 44 + 120}px)` }">
			<!-- 收货地址 -->
			<view class="address-section" @click="selectAddress">
				<view v-if="selectedAddress" class="address-info">
					<view class="address-header">
						<text class="receiver-name">{{ selectedAddress.name }}</text>
						<text class="receiver-phone">{{ selectedAddress.phone }}</text>
					</view>
					<view class="address-detail">
						{{ selectedAddress.province }}{{ selectedAddress.city }}{{ selectedAddress.region }} {{ selectedAddress.detailAddress }}
					</view>
				</view>
				<view v-else class="address-empty">
					<uni-icons type="location" size="24" color="#999"></uni-icons>
					<text class="empty-text">请选择收货地址</text>
				</view>
				<view class="address-arrow">
					<uni-icons type="right" size="20" color="#999"></uni-icons>
				</view>
			</view>

			<!-- 商品列表 -->
			<view class="goods-section">
				<view class="section-title">商品信息</view>
				<view class="goods-list">
					<view v-for="(item, index) in selectedCartItems" :key="item.cartKey || index" class="goods-item">
						<image :src="item.image" mode="aspectFill" class="goods-img" />
						<view class="goods-info">
							<text class="goods-name">{{ item.name }}</text>
							<view class="goods-spec" v-if="getSpecText(item)">
								<text class="spec-text">{{ getSpecText(item) }}</text>
							</view>
							<view class="goods-bottom">
								<view class="goods-price">
									<text class="symbol">¥</text>
									<text class="price">{{ item.price }}</text>
								</view>
								<text class="goods-quantity">x{{ item.quantity }}</text>
							</view>
						</view>
					</view>
				</view>
			</view>

			<!-- 支付方式选择 -->
			<view class="payment-section">
				<view class="section-title">支付方式</view>
				<view class="payment-options">
					<view 
						class="payment-option" 
						:class="{ active: payType === 1 }"
						@click="selectPaymentType(1)"
					>
						<view class="payment-left">
							<uni-icons type="wallet" size="20" :color="payType === 1 ? '#6f4e37' : '#999'"></uni-icons>
							<text class="payment-name">支付宝</text>
						</view>
						<view class="payment-right" v-if="payType === 1">
							<uni-icons type="checkmarkempty" size="16" color="#6f4e37"></uni-icons>
						</view>
					</view>
					<view 
						class="payment-option" 
						:class="{ active: payType === 3 }"
						@click="selectPaymentType(3)"
					>
						<view class="payment-left">
							<uni-icons type="wallet-filled" size="20" :color="payType === 3 ? '#6f4e37' : '#999'"></uni-icons>
							<text class="payment-name">咖啡卡</text>
							<text class="payment-desc" v-if="payType === 3 && coffeeCardDiscountAmount > 0">
								（9折优惠 -¥{{ coffeeCardDiscountAmount.toFixed(2) }}）
							</text>
						</view>
						<view class="payment-right" v-if="payType === 3">
							<uni-icons type="checkmarkempty" size="16" color="#6f4e37"></uni-icons>
						</view>
					</view>
				</view>
				
				<!-- 咖啡卡选择（仅在选择咖啡卡支付时显示） -->
				<view v-if="payType === 3" class="coffee-card-selector">
					<view class="selector-title">选择咖啡卡</view>
					<view v-if="coffeeCards.length === 0" class="no-cards">
						<text class="no-cards-text">暂无可用咖啡卡</text>
						<text class="no-cards-hint">请先购买咖啡卡</text>
					</view>
					<view v-else class="card-list">
						<view 
							v-for="card in coffeeCards" 
							:key="card.id"
							class="card-item"
							:class="{ active: selectedCoffeeCardId === card.id }"
							@click="selectCoffeeCard(card)"
						>
							<view class="card-info">
								<text class="card-name">{{ card.name || '咖啡会员卡' }}</text>
								<text class="card-balance">余额：¥{{ parseFloat(card.balance || 0).toFixed(2) }}</text>
							</view>
							<view class="card-check" v-if="selectedCoffeeCardId === card.id">
								<uni-icons type="checkmarkempty" size="16" color="#6f4e37"></uni-icons>
							</view>
						</view>
					</view>
				</view>
			</view>

			<!-- 订单信息 -->
			<view class="order-info-section">
				<view class="info-row">
					<text class="info-label">商品合计</text>
					<text class="info-value">¥{{ totalPrice.toFixed(2) }}</text>
				</view>
				<view class="info-row" v-if="deliveryFee > 0">
					<text class="info-label">配送费</text>
					<text class="info-value">¥{{ deliveryFee.toFixed(2) }}</text>
				</view>
				<view class="info-row" v-if="coffeeCardDiscountAmount > 0">
					<text class="info-label">咖啡卡折扣（9折）</text>
					<text class="info-value discount">-¥{{ coffeeCardDiscountAmount.toFixed(2) }}</text>
				</view>
				<view class="info-row total-row">
					<text class="info-label">实付金额</text>
					<text class="info-value total-price">¥{{ finalPrice.toFixed(2) }}</text>
				</view>
			</view>

			<!-- 备注 -->
			<view class="remark-section">
				<view class="section-title">备注</view>
				<textarea 
					v-model="remark" 
					placeholder="如有特殊要求，请在此填写（选填）" 
					class="remark-input"
					maxlength="200"
				></textarea>
			</view>

			<view style="height: 40rpx;"></view>
		</scroll-view>

		<!-- 底部提交栏 -->
		<view class="footer-bar">
			<view class="footer-left">
				<text class="total-label">合计:</text>
				<text class="total-symbol">¥</text>
				<text class="total-price">{{ finalPrice.toFixed(2) }}</text>
			</view>
			<button class="submit-btn" @click="submitOrder" :disabled="submitting">
				{{ submitting ? '提交中...' : '提交订单' }}
			</button>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useCartStore } from '@/store/cart.js'
import { useUserStore } from '@/store/user.js'
import { createOrder } from '@/services/order.js'
import { getGiftCardList } from '@/services/giftcard.js'
import { get } from '@/utils/request.js'
import { getStatusBarHeight } from '@/utils/system.js'

const statusBarHeight = ref(0)
const cartStore = useCartStore()
const userStore = useUserStore()
const selectedAddress = ref(null)
const remark = ref('')
const submitting = ref(false)
const deliveryFee = ref(0) // 配送费

// 支付方式：1->支付宝；3->咖啡卡
const payType = ref(1) // 默认支付宝
const coffeeCards = ref([]) // 咖啡卡列表
const selectedCoffeeCardId = ref(null) // 选中的咖啡卡ID
const selectedCoffeeCard = ref(null) // 选中的咖啡卡对象

// 获取选中的购物车商品
const selectedCartItems = computed(() => {
	return cartStore.items.filter(item => item.checked)
})

// 计算总价
const totalPrice = computed(() => {
	return selectedCartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

// 计算咖啡卡折扣金额（9折，即原价 × 0.1）
const coffeeCardDiscountAmount = computed(() => {
	if (payType.value === 3 && totalPrice.value > 0) {
		// 打九折，折扣金额 = 原价 × 0.1
		return totalPrice.value * 0.1
	}
	return 0
})

// 计算最终价格（商品总价 + 配送费 - 咖啡卡折扣）
const finalPrice = computed(() => {
	let price = totalPrice.value + deliveryFee.value
	if (payType.value === 3) {
		price = price - coffeeCardDiscountAmount.value
	}
	return Math.max(0, price) // 确保不为负数
})

// 获取规格文本
const getSpecText = (item) => {
	if (item.selectedSpecs && Object.keys(item.selectedSpecs).length > 0) {
		return Object.values(item.selectedSpecs).join(' / ')
	}
	return ''
}

// 返回上一页
const goBack = () => {
	uni.navigateBack()
}

// 选择地址
const selectAddress = () => {
	uni.navigateTo({
		url: '/pages/address/list?select=true'
	})
}

// 选择支付方式
const selectPaymentType = (type) => {
	payType.value = type
	if (type === 3) {
		// 选择咖啡卡支付时，加载咖啡卡列表
		loadCoffeeCards()
	} else {
		// 选择其他支付方式时，清空选中的咖啡卡
		selectedCoffeeCardId.value = null
		selectedCoffeeCard.value = null
	}
}

// 选择咖啡卡
const selectCoffeeCard = (card) => {
	selectedCoffeeCardId.value = card.id
	selectedCoffeeCard.value = card
	
	// 检查余额是否足够
	if (card.balance < finalPrice.value) {
		uni.showToast({
			title: '咖啡卡余额不足',
			icon: 'none'
		})
	}
}

// 加载咖啡卡列表
const loadCoffeeCards = async () => {
	try {
		const res = await getGiftCardList({ page: 1, pageSize: 100 })
		const cards = res.data?.records || res.records || res.data || []
		// 只显示可用状态的咖啡卡（status = 1）
		coffeeCards.value = cards.filter(card => card.status === 1 && parseFloat(card.balance || 0) > 0)
		
		// 如果有可用咖啡卡，默认选择第一张
		if (coffeeCards.value.length > 0 && !selectedCoffeeCardId.value) {
			selectCoffeeCard(coffeeCards.value[0])
		}
	} catch (error) {
		console.error('加载咖啡卡列表失败', error)
		coffeeCards.value = []
	}
}

// 加载默认地址
const loadDefaultAddress = async () => {
	try {
		const res = await get('/app/address/list')
		// 处理不同的返回格式
		const addressList = res.data || res || []
		if (addressList.length > 0) {
			// 优先选择默认地址
			const defaultAddr = addressList.find(addr => addr.defaultStatus === 1) || addressList[0]
			selectedAddress.value = defaultAddr
		}
	} catch (error) {
		console.error('加载地址失败', error)
	}
}

// 提交订单
const submitOrder = async () => {
	// 验证登录
	if (!userStore.isLogin) {
		uni.showModal({
			title: '提示',
			content: '请先登录',
			success: (res) => {
				if (res.confirm) {
					uni.navigateTo({
						url: '/pages/login/index'
					})
				}
			}
		})
		return
	}

	// 验证地址
	if (!selectedAddress.value) {
		uni.showToast({
			title: '请选择收货地址',
			icon: 'none'
		})
		return
	}

	// 验证商品
	if (selectedCartItems.value.length === 0) {
		uni.showToast({
			title: '请选择商品',
			icon: 'none'
		})
		return
	}

	// 如果选择咖啡卡支付，验证咖啡卡
	if (payType.value === 3) {
		if (!selectedCoffeeCardId.value || !selectedCoffeeCard.value) {
			uni.showToast({
				title: '请选择咖啡卡',
				icon: 'none'
			})
			return
		}
		
		// 检查余额是否足够
		if (selectedCoffeeCard.value.balance < finalPrice.value) {
			uni.showToast({
				title: '咖啡卡余额不足，请选择其他支付方式',
				icon: 'none',
				duration: 2000
			})
			return
		}
	}

	// 检查购物车项是否都有 cartItemId（需要先同步到后端）
	const cartItemIds = selectedCartItems.value
		.map(item => item.cartItemId)
		.filter(id => id != null)
	
	if (cartItemIds.length !== selectedCartItems.value.length) {
		uni.showToast({
			title: '部分商品未同步，请稍候再试',
			icon: 'none'
		})
		// 尝试同步一次
		await cartStore.syncCart()
		return
	}

	// 构建订单数据（优化：只传ID，后端根据ID查询）
	const orderData = {
		// 选中的购物车项ID列表
		cartItemIds: cartItemIds,
		
		// 收货地址ID
		addressId: selectedAddress.value.id,
		
		// 备注
		remark: remark.value || '',
		
		// 配送方式
		deliveryCompany: '门店自提', // 默认门店自提
		
		// 支付方式：1->支付宝；3->咖啡卡
		payType: payType.value,
		
		// 咖啡卡ID（如果使用咖啡卡支付）
		coffeeCardId: payType.value === 3 ? selectedCoffeeCardId.value : null,
		
		// 订单金额（可选，用于后端校验，后端会重新计算）
		totalAmount: totalPrice.value,
		promotionAmount: 0, // 促销金额
		couponAmount: 0, // 优惠券金额
		payAmount: finalPrice.value
	}

	try {
		submitting.value = true
		
		const result = await createOrder(orderData)
		
		uni.showToast({
			title: '订单创建成功',
			icon: 'success'
		})
		
		// 后端已经删除了购物车项，这里只需要同步一下本地 store
		await cartStore.syncCart()
		
		// 跳转到订单详情或返回购物车
		setTimeout(() => {
			const orderId = result.id || result.orderId || result.data?.id || result.data?.orderId
			if (orderId) {
				// 如果有订单详情页面，跳转到详情页
				// 否则跳转回购物车
				uni.redirectTo({
					url: `/pages/order/detail?id=${orderId}`
				}).catch(() => {
					// 如果订单详情页面不存在，返回购物车
					uni.switchTab({
						url: '/pages/cart/index'
					})
				})
			} else {
				// 没有订单ID，返回购物车
				uni.switchTab({
					url: '/pages/cart/index'
				})
			}
		}, 1500)
		
	} catch (error) {
		console.error('创建订单失败', error)
		uni.showToast({
			title: error.message || '创建订单失败，请重试',
			icon: 'none',
			duration: 2000
		})
	} finally {
		submitting.value = false
	}
}

// 页面显示时检查是否有选中的地址（从地址列表页返回时）
onShow(() => {
	// 检查是否有从地址列表页选择的地址
	const selectedAddr = uni.getStorageSync('selectedAddress')
	if (selectedAddr) {
		selectedAddress.value = selectedAddr
		uni.removeStorageSync('selectedAddress')
	} else if (!selectedAddress.value) {
		// 如果没有选中的地址，加载默认地址
		loadDefaultAddress()
	}
})

onMounted(() => {
	statusBarHeight.value = getStatusBarHeight()
	
	// 检查是否有选中的商品
	if (selectedCartItems.value.length === 0) {
		uni.showModal({
			title: '提示',
			content: '购物车中没有选中的商品',
			showCancel: false,
			success: () => {
				uni.navigateBack()
			}
		})
		return
	}
	
	// 加载默认地址
	loadDefaultAddress()
})
</script>

<style lang="scss" scoped>
$primary: #6f4e37;
$bg-color: #f7f8fa;

.order-confirm-page {
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

/* 收货地址 */
.address-section {
	background-color: white;
	margin: 24rpx 32rpx;
	padding: 32rpx;
	border-radius: 24rpx;
	display: flex;
	align-items: center;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.address-info {
	flex: 1;
}

.address-header {
	display: flex;
	align-items: center;
	margin-bottom: 16rpx;
}

.receiver-name {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-right: 24rpx;
}

.receiver-phone {
	font-size: 28rpx;
	color: #666;
}

.address-detail {
	font-size: 26rpx;
	color: #666;
	line-height: 1.5;
}

.address-empty {
	flex: 1;
	display: flex;
	align-items: center;
	gap: 16rpx;
}

.empty-text {
	font-size: 28rpx;
	color: #999;
}

.address-arrow {
	margin-left: 16rpx;
}

/* 商品列表 */
.goods-section {
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

/* 支付方式选择 */
.payment-section {
	background-color: white;
	margin: 24rpx 32rpx;
	padding: 32rpx;
	border-radius: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.payment-options {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.payment-option {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 24rpx;
	border: 2rpx solid #eee;
	border-radius: 16rpx;
	transition: all 0.3s;
}

.payment-option.active {
	border-color: #6f4e37;
	background-color: #fef8f5;
}

.payment-left {
	display: flex;
	align-items: center;
	gap: 16rpx;
	flex: 1;
}

.payment-name {
	font-size: 30rpx;
	font-weight: 500;
	color: #333;
}

.payment-desc {
	font-size: 24rpx;
	color: #6f4e37;
	margin-left: 12rpx;
}

.payment-right {
	display: flex;
	align-items: center;
}

/* 咖啡卡选择器 */
.coffee-card-selector {
	margin-top: 24rpx;
	padding-top: 24rpx;
	border-top: 1rpx solid #eee;
}

.selector-title {
	font-size: 28rpx;
	font-weight: 500;
	color: #666;
	margin-bottom: 16rpx;
}

.no-cards {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 40rpx 0;
}

.no-cards-text {
	font-size: 28rpx;
	color: #999;
	margin-bottom: 8rpx;
}

.no-cards-hint {
	font-size: 24rpx;
	color: #ccc;
}

.card-list {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
}

.card-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx;
	border: 2rpx solid #eee;
	border-radius: 12rpx;
	background-color: #fafafa;
	transition: all 0.3s;
}

.card-item.active {
	border-color: #6f4e37;
	background-color: #fef8f5;
}

.card-info {
	display: flex;
	flex-direction: column;
	gap: 8rpx;
	flex: 1;
}

.card-name {
	font-size: 28rpx;
	font-weight: 500;
	color: #333;
}

.card-balance {
	font-size: 24rpx;
	color: #666;
}

.card-check {
	display: flex;
	align-items: center;
}

/* 订单信息 */
.order-info-section {
	background-color: white;
	margin: 24rpx 32rpx;
	padding: 32rpx;
	border-radius: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.info-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16rpx;
}

.info-row:last-child {
	margin-bottom: 0;
}

.info-label {
	font-size: 28rpx;
	color: #666;
}

.info-value {
	font-size: 28rpx;
	color: #333;
}

.info-value.discount {
	color: #6f4e37;
	font-weight: 500;
}

.total-row {
	padding-top: 16rpx;
	border-top: 1rpx solid #eee;
	margin-top: 16rpx;
}

.total-row .info-label {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
}

.total-price {
	font-size: 36rpx;
	font-weight: bold;
	color: $primary;
}

/* 备注 */
.remark-section {
	background-color: white;
	margin: 24rpx 32rpx;
	padding: 32rpx;
	border-radius: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.remark-input {
	width: 100%;
	min-height: 120rpx;
	font-size: 28rpx;
	color: #333;
	background-color: #f7f8fa;
	border-radius: 16rpx;
	padding: 20rpx;
	box-sizing: border-box;
}

/* 底部提交栏 */
.footer-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background-color: white;
	padding: 24rpx 32rpx;
	padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
	display: flex;
	justify-content: space-between;
	align-items: center;
	box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
	z-index: 40;
}

.footer-left {
	display: flex;
	align-items: baseline;
}

.total-label {
	font-size: 28rpx;
	color: #333;
	margin-right: 8rpx;
}

.total-symbol {
	font-size: 24rpx;
	color: $primary;
	font-weight: bold;
}

.total-price {
	font-size: 40rpx;
	color: $primary;
	font-weight: bold;
	font-family: 'DIN Alternate', sans-serif;
}

.submit-btn {
	background-color: $primary;
	color: white;
	height: 80rpx;
	padding: 0 64rpx;
	border-radius: 40rpx;
	font-size: 30rpx;
	font-weight: bold;
	display: flex;
	align-items: center;
	justify-content: center;
	margin: 0;
	box-shadow: 0 4rpx 16rpx rgba(111, 78, 55, 0.3);
}

.submit-btn[disabled] {
	opacity: 0.6;
}
</style>

