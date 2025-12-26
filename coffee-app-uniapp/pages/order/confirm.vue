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

		<scroll-view scroll-y class="content-scroll" :style="{ height: scrollHeight }">
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

			<!-- 优惠券选择 -->
			<view class="coupon-section" @click="openCouponPopup">
				<view class="section-row">
					<text class="section-label">优惠券</text>
					<view class="section-right">
						<text class="coupon-text" v-if="selectedCoupon">
							-¥{{ selectedCoupon.amount }}
						</text>
						<text class="coupon-placeholder" v-else-if="availableCoupons.length > 0">
							{{ availableCoupons.length }}张可用
						</text>
						<text class="coupon-placeholder" v-else>无可用优惠券</text>
						<uni-icons type="right" size="16" color="#999"></uni-icons>
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
							<uni-icons  custom-prefix="iconfont" type="icon-alipay" size="20" :color="payType === 1 ? '#1296db' : '#999'"></uni-icons>
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
				<view class="info-row" v-if="couponDiscountAmount > 0">
					<text class="info-label">优惠券抵扣</text>
					<text class="info-value discount">-¥{{ couponDiscountAmount.toFixed(2) }}</text>
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
					:auto-height="true"
				></textarea>
			</view>

			<!-- 底部占位，防止被提交栏遮挡 -->
			<view class="footer-spacer"></view>
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
		
		<!-- 优惠券弹窗遮罩 -->
		<view v-if="showCouponPopup" class="popup-mask" @click="closeCouponPopup">
			<view class="popup-content" @click.stop>
				<view class="popup-header">
					<text class="popup-title">选择优惠券</text>
					<view class="popup-close" @click="closeCouponPopup">
						<uni-icons type="closeempty" size="24" color="#999"></uni-icons>
					</view>
				</view>
				<scroll-view scroll-y class="popup-scroll">
					<view class="popup-coupon-list">
						<!-- 不使用优惠券选项 -->
						<view class="popup-coupon-item no-use" :class="{ active: !selectedCoupon }" @click="clearCoupon">
							<text>不使用优惠券</text>
							<uni-icons v-if="!selectedCoupon" type="checkmarkempty" size="20" color="#6f4e37"></uni-icons>
						</view>
						
						<!-- 优惠券列表 -->
						<view v-if="availableCoupons.length === 0" class="empty-coupons">
							<text class="empty-text">暂无可用优惠券</text>
						</view>
						<view v-for="coupon in availableCoupons" :key="coupon.id" 
							class="popup-coupon-item"
							:class="{ active: selectedCoupon && selectedCoupon.id === coupon.id }"
							@click="selectCoupon(coupon)">
							<view class="popup-coupon-left">
								<view class="popup-coupon-amount">
									<text class="symbol">¥</text>
									<text class="value">{{ coupon.amount }}</text>
								</view>
								<view class="popup-coupon-info">
									<text class="name">{{ coupon.couponName }}</text>
									<text class="desc" v-if="coupon.minPoint > 0">满{{ coupon.minPoint }}元可用</text>
									<text class="desc" v-else>无门槛</text>
									<text class="time">有效期至 {{ formatCouponDate(coupon.expireTime || coupon.endTime) }}</text>
								</view>
							</view>
							<view class="popup-coupon-check" v-if="selectedCoupon && selectedCoupon.id === coupon.id">
								<uni-icons type="checkmarkempty" size="20" color="#6f4e37"></uni-icons>
							</view>
						</view>
					</view>
				</scroll-view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useCartStore } from '@/store/cart.js'
import { useUserStore } from '@/store/user.js'
import { formatDate } from '@/utils/date.js'
import { createOrder } from '@/services/order.js'
import { getGiftCardList } from '@/services/giftcard.js'
import { getMyCoupons } from '@/services/promotion.js'
import { payByCoffeeCard } from '@/services/pay.js'
import { get } from '@/utils/request.js'
import { getStatusBarHeight } from '@/utils/system.js'

// ========== 基础数据 ==========
const statusBarHeight = ref(0)
const cartStore = useCartStore()
const userStore = useUserStore()
const selectedAddress = ref(null)
const remark = ref('')
const submitting = ref(false)
const deliveryFee = ref(0)

// ========== 优惠券相关 ==========
const selectedCoupon = ref(null)
const showCouponPopup = ref(false)
const availableCoupons = ref([])
const loadingCoupons = ref(false)

// ========== 支付相关 ==========
const payType = ref(1) // 1->支付宝；3->咖啡卡
const coffeeCards = ref([])
const selectedCoffeeCardId = ref(null)
const selectedCoffeeCard = ref(null)

// ========== 计算属性 ==========
const selectedCartItems = computed(() => {
	return cartStore.items.filter(item => item.checked)
})

const totalPrice = computed(() => {
	return selectedCartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

const coffeeCardDiscountAmount = computed(() => {
	if (payType.value === 3 && totalPrice.value > 0) {
		return totalPrice.value * 0.1 // 9折，折扣10%
	}
	return 0
})

const couponDiscountAmount = computed(() => {
	if (selectedCoupon.value) {
		return selectedCoupon.value.amount || 0
	}
	return 0
})

const finalPrice = computed(() => {
	let price = totalPrice.value + deliveryFee.value - couponDiscountAmount.value
	if (payType.value === 3) {
		price = price - coffeeCardDiscountAmount.value
	}
	return Math.max(0, price)
})

const scrollHeight = computed(() => {
	const navHeight = statusBarHeight.value + 44 // 导航栏高度
	const footerHeight = 120 // 底部栏高度
	return `calc(100vh - ${navHeight + footerHeight}px)`
})

// ========== 方法 ==========
const getSpecText = (item) => {
	if (item.selectedSpecs && Object.keys(item.selectedSpecs).length > 0) {
		return Object.values(item.selectedSpecs).join(' / ')
	}
	return ''
}

const formatCouponDate = (dateStr) => formatDate(dateStr)

const goBack = () => {
	uni.navigateBack()
}

const selectAddress = () => {
	uni.navigateTo({
		url: '/pages/address/list?select=true'
	})
}

const openCouponPopup = () => {
	showCouponPopup.value = true
}

const closeCouponPopup = () => {
	showCouponPopup.value = false
}

const selectCoupon = (coupon) => {
	selectedCoupon.value = coupon
	closeCouponPopup()
}

const clearCoupon = () => {
	selectedCoupon.value = null
	closeCouponPopup()
}

const selectPaymentType = (type) => {
	payType.value = type
	if (type === 3) {
		loadCoffeeCards()
	} else {
		selectedCoffeeCardId.value = null
		selectedCoffeeCard.value = null
	}
}

const selectCoffeeCard = (card) => {
	selectedCoffeeCardId.value = card.id
	selectedCoffeeCard.value = card
	
	if (card.balance < finalPrice.value) {
		uni.showToast({
			title: '咖啡卡余额不足',
			icon: 'none'
		})
	}
}

// ========== 数据加载 ==========
const loadAvailableCoupons = async () => {
	if (loadingCoupons.value) return
	loadingCoupons.value = true
	
	try {
		const res = await getMyCoupons(0)
		const list = res.data || res || []
		
		// 过滤可用优惠券：未使用且满足使用门槛
		availableCoupons.value = list
			.filter(c => {
				const minPoint = parseFloat(c.minPoint || 0)
				return totalPrice.value >= minPoint
			})
			.map(item => ({
				...item,
				couponName: item.couponName || item.coupon_name || '优惠券',
				amount: parseFloat(item.amount || 0),
				minPoint: parseFloat(item.minPoint || 0),
				endTime: item.endTime || item.end_time
			}))
			.sort((a, b) => b.amount - a.amount) // 按金额降序
		
		// 如果当前选中的优惠券不在可用列表中，清空选择
		if (selectedCoupon.value) {
			const stillAvailable = availableCoupons.value.find(c => c.id === selectedCoupon.value.id)
			if (!stillAvailable) {
				selectedCoupon.value = null
			}
		}
	} catch (e) {
		console.error('加载优惠券失败', e)
		availableCoupons.value = []
	} finally {
		loadingCoupons.value = false
	}
}

const loadCoffeeCards = async () => {
	try {
		const res = await getGiftCardList({ page: 1, pageSize: 100 })
		const cards = res.data?.records || res.records || res.data || []
		coffeeCards.value = cards
			.filter(card => card.status === 1 && parseFloat(card.balance || 0) > 0)
		
		if (coffeeCards.value.length > 0 && !selectedCoffeeCardId.value) {
			selectCoffeeCard(coffeeCards.value[0])
		}
	} catch (error) {
		console.error('加载咖啡卡列表失败', error)
		coffeeCards.value = []
	}
}

const loadDefaultAddress = async () => {
	try {
		const res = await get('/app/address/list')
		const addressList = res.data || res || []
		if (addressList.length > 0) {
			const defaultAddr = addressList.find(addr => addr.defaultStatus === 1) || addressList[0]
			selectedAddress.value = defaultAddr
		}
	} catch (error) {
		console.error('加载地址失败', error)
	}
}

// ========== 订单提交 ==========
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

	// 验证咖啡卡支付
	if (payType.value === 3) {
		if (!selectedCoffeeCardId.value || !selectedCoffeeCard.value) {
			uni.showToast({
				title: '请选择咖啡卡',
				icon: 'none'
			})
			return
		}
		
		if (selectedCoffeeCard.value.balance < finalPrice.value) {
			uni.showToast({
				title: '咖啡卡余额不足，请选择其他支付方式',
				icon: 'none',
				duration: 2000
			})
			return
		}
	}

	// 检查购物车项同步状态
	const cartItemIds = selectedCartItems.value
		.map(item => item.cartItemId)
		.filter(id => id != null)
	
	if (cartItemIds.length !== selectedCartItems.value.length) {
		uni.showToast({
			title: '部分商品未同步，请稍候再试',
			icon: 'none'
		})
		await cartStore.syncCart()
		return
	}

	// 构建订单数据
	const orderData = {
		cartItemIds: cartItemIds,
		addressId: selectedAddress.value.id,
		remark: remark.value || '',
		deliveryCompany: '门店自提',
		payType: payType.value,
		coffeeCardId: payType.value === 3 ? selectedCoffeeCardId.value : null,
		totalAmount: totalPrice.value,
		promotionAmount: 0,
		couponAmount: couponDiscountAmount.value,
		couponHistoryId: selectedCoupon.value ? selectedCoupon.value.id : null,
		payAmount: finalPrice.value
	}

	try {
		submitting.value = true
		
		const result = await createOrder(orderData)
		const orderId = result.id || result.orderId || result.data?.id || result.data?.orderId
		
		if (!orderId) {
			throw new Error('订单创建失败，未获取到订单ID')
		}
		
		// 如果是咖啡卡支付，立即调用支付接口
		if (payType.value === 3) {
			try {
				await payByCoffeeCard(orderId)
				uni.showToast({
					title: '支付成功',
					icon: 'success'
				})
			} catch (payError) {
				console.error('咖啡卡支付失败', payError)
				uni.showToast({
					title: payError.message || '支付失败，请前往订单详情重新支付',
					icon: 'none',
					duration: 2000
				})
				// 支付失败也跳转到订单详情页，用户可以重新支付
				setTimeout(() => {
					uni.redirectTo({
						url: `/pages/order/detail?id=${orderId}`
					}).catch(() => {
						uni.switchTab({
							url: '/pages/cart/index'
						})
					})
				}, 2000)
				return
			}
		} else {
			uni.showToast({
				title: '订单创建成功',
				icon: 'success'
			})
		}
		
		await cartStore.syncCart()
		
		setTimeout(() => {
			uni.redirectTo({
				url: `/pages/order/detail?id=${orderId}`
			}).catch(() => {
				uni.switchTab({
					url: '/pages/cart/index'
				})
			})
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

// ========== 生命周期 ==========
onShow(() => {
	const selectedAddr = uni.getStorageSync('selectedAddress')
	if (selectedAddr) {
		selectedAddress.value = selectedAddr
		uni.removeStorageSync('selectedAddress')
	} else if (!selectedAddress.value) {
		loadDefaultAddress()
	}
	
	// 重新加载优惠券（可能已使用或过期）
	loadAvailableCoupons()
})

onMounted(() => {
	statusBarHeight.value = getStatusBarHeight()
	
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
	
	loadDefaultAddress()
	loadAvailableCoupons()
})

// 监听总价变化，重新过滤优惠券
watch(totalPrice, () => {
	if (availableCoupons.value.length > 0) {
		loadAvailableCoupons()
	}
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

/* 优惠券选择 */
.coupon-section {
	background-color: white;
	margin: 24rpx 32rpx;
	padding: 32rpx;
	border-radius: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.section-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.section-label {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
}

.section-right {
	display: flex;
	align-items: center;
	gap: 8rpx;
}

.coupon-text {
	font-size: 28rpx;
	color: #e64340;
	font-weight: bold;
}

.coupon-placeholder {
	font-size: 28rpx;
	color: #999;
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
	line-height: 1.6;
}

.footer-spacer {
	height: calc(160rpx + env(safe-area-inset-bottom));
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

.footer-bar .total-price {
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

/* 弹窗样式 */
.popup-mask {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background-color: rgba(0, 0, 0, 0.5);
	z-index: 100;
	display: flex;
	align-items: flex-end;
	animation: fadeIn 0.3s;
}

@keyframes fadeIn {
	from {
		opacity: 0;
	}
	to {
		opacity: 1;
	}
}

.popup-content {
	background-color: #f5f5f5;
	border-radius: 24rpx 24rpx 0 0;
	overflow: hidden;
	width: 100%;
	max-height: 70vh;
	display: flex;
	flex-direction: column;
	animation: slideUp 0.3s;
}

@keyframes slideUp {
	from {
		transform: translateY(100%);
	}
	to {
		transform: translateY(0);
	}
}

.popup-header {
	background-color: white;
	padding: 32rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	border-bottom: 1rpx solid #eee;
}

.popup-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.popup-close {
	padding: 8rpx;
}

.popup-scroll {
	flex: 1;
	padding: 24rpx;
	box-sizing: border-box;
}

.popup-coupon-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.empty-coupons {
	padding: 60rpx 0;
	text-align: center;
}

.empty-coupons .empty-text {
	font-size: 28rpx;
	color: #999;
}

.popup-coupon-item {
	background-color: white;
	border-radius: 16rpx;
	padding: 24rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	transition: all 0.3s;
	
	&.no-use {
		padding: 32rpx;
		font-size: 28rpx;
		color: #333;
	}
	
	&.active {
		border: 2rpx solid #6f4e37;
		background-color: #fef8f5;
	}
}

.popup-coupon-left {
	display: flex;
	align-items: center;
	flex: 1;
}

.popup-coupon-amount {
	color: #e64340;
	margin-right: 24rpx;
	display: flex;
	align-items: baseline;
	min-width: 100rpx;
	justify-content: center;
	
	.symbol { 
		font-size: 24rpx; 
	}
	.value { 
		font-size: 48rpx; 
		font-weight: bold; 
	}
}

.popup-coupon-info {
	display: flex;
	flex-direction: column;
	gap: 4rpx;
	
	.name { 
		font-size: 28rpx; 
		font-weight: bold; 
		color: #333; 
	}
	.desc { 
		font-size: 22rpx; 
		color: #666; 
	}
	.time { 
		font-size: 20rpx; 
		color: #999; 
	}
}

.popup-coupon-check {
	display: flex;
	align-items: center;
	margin-left: 16rpx;
}
</style>
