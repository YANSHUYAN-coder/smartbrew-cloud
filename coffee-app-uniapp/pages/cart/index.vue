<template>
	<view class="cart-page" :class="themeClass">
		<!-- 1. 顶部导航 -->
		<view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<text class="page-title">购物车</text>
			<text class="manage-btn" @click="isManageMode = !isManageMode">
				{{ isManageMode ? '完成' : '管理' }}
			</text>
		</view>

		<!-- 2. 购物车列表 -->
		<scroll-view 
			class="cart-scroll" 
			scroll-y 
			:style="{ height: `calc(100vh - ${statusBarHeight + 44 + 100}px)` }"
			:refresher-enabled="true"
			:refresher-triggered="refreshing"
			@refresherrefresh="onRefresh"
			@refresherrestore="onRefreshRestore">
			<view class="cart-list" v-if="cartItems.length > 0">

				<!-- 商家分组标题 (预留多商家扩展) -->
				<view class="shop-group-header">
					<view class="checkbox" :class="{ checked: isAllChecked }" @click="toggleAllCheck">
						<text class="check-icon" v-if="isAllChecked">✓</text>
					</view>
					<text class="shop-name">智咖·云</text>
					<text class="shop-arrow">›</text>
				</view>

				<!-- 商品卡片 (支持侧滑删除) -->
				<view v-for="(item, index) in cartItems" :key="item.cartKey || `cart-${item.id}-${index}`" class="cart-item-wrapper">
					<!-- 这里使用简单的布局模拟侧滑效果，实际可以使用 uni-swipe-action -->
					<view class="cart-card">
						<!-- 复选框 -->
						<view class="checkbox-area" @click="toggleCheck(item)">
							<view class="checkbox" :class="{ checked: item.checked }">
								<text class="check-icon" v-if="item.checked">✓</text>
							</view>
						</view>

						<!-- 商品图 -->
						<image :src="item.image" mode="aspectFill" class="prod-img" />

						<!-- 信息区 -->
						<view class="info-box">
							<view class="title-row">
								<text class="prod-name">{{ item.name }}</text>
								<text class="delete-icon" v-if="isManageMode" @click="removeItem(item)">×</text>
							</view>
							<view class="spec-row" v-if="getSpecText(item)">
								<text class="spec-tag">{{ getSpecText(item) }}</text>
							</view>
							<view class="bottom-row">
								<view class="price-box">
									<text class="symbol">¥</text>
									<text class="price">{{ item.price }}</text>
								</view>
								<!-- 加减器 -->
								<view class="stepper">
									<view class="step-btn minus" @click.stop="updateQuantity(item, -1)">-</view>
									<input type="number" class="step-input" :value="item.quantity" disabled />
									<view class="step-btn plus" @click.stop="updateQuantity(item, 1)">+</view>
								</view>
							</view>
						</view>
					</view>
				</view>

				<!-- 底部垫高 -->
				<view style="height: 200rpx;"></view>
			</view>

			<!-- 空状态 -->
			<view class="empty-state" v-else>
				<image src="https://img.icons8.com/bubbles/200/shopping-cart.png" class="empty-img" />
				<text class="empty-text">购物车空空如也</text>
				<text class="empty-sub">快去来一杯元气咖啡吧</text>
				<button class="go-shop-btn" @click="goToMenu">去点单</button>
			</view>
		</scroll-view>

		<!-- 3. 底部结算栏 (悬浮) -->
		<view class="footer-bar" v-if="cartItems.length > 0">
			<view class="footer-content">
				<view class="left-section" @click="toggleAllCheck">
					<view class="checkbox" :class="{ checked: isAllChecked }">
						<text class="check-icon" v-if="isAllChecked">✓</text>
					</view>
					<text class="select-all-text">全选</text>
				</view>

				<view class="right-section" v-if="!isManageMode">
					<view class="total-info">
						<text class="total-label">合计:</text>
						<text class="total-symbol">¥</text>
						<text class="total-price">{{ totalPrice.toFixed(2) }}</text>
					</view>
					<button class="checkout-btn" @click="handleCheckout">
						去结算 ({{ totalCount }})
					</button>
				</view>

				<view class="right-section" v-else>
					<button class="delete-btn" @click="removeSelected">删除所选</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		computed,
		onMounted
	} from 'vue'
	import {
		onPullDownRefresh
	} from '@dcloudio/uni-app'
	import {
		useCartStore
	} from '@/store/cart.js'
	import {
		useUserStore
	} from '@/store/user.js'
	import {
		getStatusBarHeight
	} from '@/utils/system.js'

	const statusBarHeight = ref(0)
	const cartStore = useCartStore()
	const userStore = useUserStore()
	const isManageMode = ref(false) // 管理模式开关
	const refreshing = ref(false) // 下拉刷新状态

	const themeClass = computed(() => userStore.isDarkMode ? 'theme-dark' : 'theme-light')

	// --- 计算属性 ---
	const cartItems = computed(() => cartStore.items)

	// 计算总价 (只算选中的)
	const totalPrice = computed(() => {
		return cartItems.value
			.filter(item => item.checked)
			.reduce((sum, item) => sum + item.price * item.quantity, 0)
	})

	const totalCount = computed(() => {
		return cartItems.value
			.filter(item => item.checked)
			.reduce((sum, item) => sum + item.quantity, 0)
	})

	const isAllChecked = computed(() => {
		return cartItems.value.length > 0 && cartItems.value.every(item => item.checked)
	})

	// --- 方法 ---

	// 切换单选（委托给 cartStore 的 action）
	const toggleCheck = (item) => {
		const cartKey = item.cartKey || cartStore.getCartItemKey(item)
		cartStore.toggleItemCheck(cartKey)
	}

	// 全选/反选
	const toggleAllCheck = () => {
		const targetStatus = !isAllChecked.value
		cartItems.value.forEach(item => item.checked = targetStatus)
	}

	// 获取规格文本
	const getSpecText = (item) => {
		if (item.selectedSpecs && Object.keys(item.selectedSpecs).length > 0) {
			return Object.values(item.selectedSpecs).join(' / ')
		}
		return ''
	}

	// 数量加减
	const updateQuantity = async (item, delta) => {
		const cartKey = item.cartKey || cartStore.getCartItemKey(item)
		const newQty = item.quantity + delta
		
		if (newQty <= 0) {
			uni.showModal({
				title: '提示',
				content: '确定要删除该商品吗？',
				success: async (res) => {
					if (res.confirm) {
						try {
							await cartStore.removeItemByKey(cartKey)
						} catch (error) {
							uni.showToast({
								title: '删除失败',
								icon: 'none'
							})
						}
					}
				}
			})
		} else {
			try {
				await cartStore.updateQuantity(cartKey, delta)
			} catch (error) {
				uni.showToast({
					title: '更新失败',
					icon: 'none'
				})
			}
		}
	}

	const removeItem = async (item) => {
		const cartKey = item.cartKey || cartStore.getCartItemKey(item)
		try {
			await cartStore.removeItemByKey(cartKey)
		} catch (error) {
			uni.showToast({
				title: '删除失败',
				icon: 'none'
			})
		}
	}

	const removeSelected = async () => {
		// 批量删除逻辑
		const selectedItems = cartItems.value.filter(i => i.checked)
		try {
			for (const item of selectedItems) {
				const cartKey = item.cartKey || cartStore.getCartItemKey(item)
				await cartStore.removeItemByKey(cartKey)
			}
			isManageMode.value = false
		} catch (error) {
			uni.showToast({
				title: '删除失败',
				icon: 'none'
			})
		}
	}

	const goToMenu = () => {
		uni.switchTab({
			url: '/pages/menu/index'
		})
	}

	const handleCheckout = () => {
		if (totalCount.value === 0) {
			return uni.showToast({
				title: '请选择商品',
				icon: 'none'
			})
		}
		uni.navigateTo({
			url: '/pages/order/confirm'
		})
	}

	// 加载购物车数据
	const loadCartData = async () => {
		try {
			// 从后端同步购物车数据
			await cartStore.syncCart()
		} catch (error) {
			console.error('加载购物车数据失败', error)
			// 如果同步失败，继续使用本地数据
		}
	}

	// 下拉刷新
	const onRefresh = async () => {
		refreshing.value = true
		await loadCartData()
		setTimeout(() => {
			refreshing.value = false
		}, 300)
	}

	// 刷新恢复
	const onRefreshRestore = () => {
		refreshing.value = false
	}

	// 页面下拉刷新（如果 scroll-view 的下拉刷新不工作，可以使用这个）
	onPullDownRefresh(async () => {
		await loadCartData()
		uni.stopPullDownRefresh()
	})

	onMounted(() => {
		statusBarHeight.value = getStatusBarHeight()
		loadCartData()
	})
</script>

<style lang="scss" scoped>
	$primary: #6f4e37;
	$red: #ff4d4f;

	.cart-page {
		min-height: 100vh;
		background-color: var(--bg-secondary);
		display: flex;
		flex-direction: column;
		transition: background-color 0.3s;
	}

	/* --- 1. 顶部导航 --- */
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

	.page-title {
		font-size: 36rpx;
		font-weight: bold;
		color: var(--text-primary);
	}

	.manage-btn {
		font-size: 28rpx;
		color: var(--text-secondary);
	}

	/* --- 2. 列表区域 --- */
	.cart-scroll {
		flex: 1;
	}

	.cart-list {
		padding: 24rpx 32rpx;
	}

	.shop-group-header {
		display: flex;
		align-items: center;
		margin-bottom: 24rpx;
		padding-left: 8rpx;
	}

	.shop-name {
		font-size: 28rpx;
		font-weight: bold;
		color: var(--text-primary);
		margin-left: 16rpx;
		margin-right: 8rpx;
	}

	.shop-arrow {
		font-size: 24rpx;
		color: var(--text-tertiary);
	}

	.cart-item-wrapper {
		margin-bottom: 24rpx;
	}

	.cart-card {
		background-color: var(--bg-primary);
		border-radius: 24rpx;
		padding: 24rpx;
		display: flex;
		align-items: center;
		box-shadow: 0 4rpx 16rpx var(--shadow-color);
	}

	/* 复选框样式 */
	.checkbox-area {
		padding: 20rpx 20rpx 20rpx 0;
		/* 扩大点击区域 */
	}

	.checkbox {
		width: 40rpx;
		height: 40rpx;
		border-radius: 50%;
		border: 2rpx solid var(--border-color);
		display: flex;
		align-items: center;
		justify-content: center;
		transition: all 0.2s;
	}

	.checkbox.checked {
		background-color: $primary;
		border-color: $primary;
	}

	.check-icon {
		color: white;
		font-size: 24rpx;
		font-weight: bold;
	}

	.prod-img {
		width: 160rpx;
		height: 160rpx;
		border-radius: 16rpx;
		background-color: var(--bg-tertiary);
		margin-left: 8rpx;
		margin-right: 24rpx;
	}

	.info-box {
		flex: 1;
		height: 160rpx;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
	}

	.title-row {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
	}

	.prod-name {
		font-size: 30rpx;
		font-weight: bold;
		color: var(--text-primary);
	}

	.delete-icon {
		font-size: 36rpx;
		color: var(--text-tertiary);
		padding: 0 10rpx;
		line-height: 1;
	}

	.spec-row {
		margin-top: -8rpx;
	}

	.spec-tag {
		font-size: 22rpx;
		color: var(--text-secondary);
		background-color: var(--bg-secondary);
		padding: 4rpx 12rpx;
		border-radius: 8rpx;
	}

	.bottom-row {
		display: flex;
		justify-content: space-between;
		align-items: flex-end;
	}

	.price-box {
		color: var(--text-primary);
		font-weight: bold;
	}

	.symbol {
		font-size: 24rpx;
	}

	.price {
		font-size: 34rpx;
	}

	/* 步进器 */
	.stepper {
		display: flex;
		align-items: center;
		background-color: var(--bg-secondary);
		border-radius: 30rpx;
		padding: 2rpx;
	}

	.step-btn {
		width: 48rpx;
		height: 48rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 32rpx;
		color: var(--text-primary);
		font-weight: bold;
	}

	.step-input {
		width: 60rpx;
		text-align: center;
		font-size: 26rpx;
		font-weight: bold;
		color: var(--text-primary);
	}

	/* --- 3. 底部结算栏 --- */
	.footer-bar {
		position: fixed;
		bottom: calc(var(--window-bottom) + 32rpx);
		/* 适配 TabBar 高度 */
		left: 32rpx;
		right: 32rpx;
		height: 110rpx;
		background-color: var(--card-bg);
		backdrop-filter: blur(20rpx);
		border-radius: 60rpx;
		box-shadow: 0 8rpx 32rpx var(--shadow-color);
		display: flex;
		justify-content: center;
		padding: 8rpx;
		z-index: 40;
	}

	.footer-content {
		width: 100%;
		height: 100%;
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding-left: 32rpx;
		padding-right: 8rpx;
	}

	.left-section {
		display: flex;
		align-items: center;
		gap: 16rpx;
	}

	.select-all-text {
		font-size: 26rpx;
		color: var(--text-secondary);
	}

	.right-section {
		display: flex;
		align-items: center;
		gap: 24rpx;
	}

	.total-label {
		font-size: 24rpx;
		color: var(--text-primary);
	}

	.total-symbol {
		font-size: 24rpx;
		color: $primary;
		font-weight: bold;
	}

	.total-price {
		font-size: 36rpx;
		color: $primary;
		font-weight: bold;
		font-family: 'DIN Alternate', sans-serif;
	}

	.checkout-btn {
		background-color: $primary;
		color: white;
		height: 80rpx;
		padding: 0 48rpx;
		border-radius: 40rpx;
		font-size: 28rpx;
		font-weight: bold;
		display: flex;
		align-items: center;
		margin: 0;
		box-shadow: 0 4rpx 16rpx rgba(111, 78, 55, 0.3);
	}

	.delete-btn {
		background-color: transparent;
		color: $red;
		border: 2rpx solid $red;
		height: 72rpx;
		padding: 0 40rpx;
		border-radius: 36rpx;
		font-size: 26rpx;
		line-height: 68rpx;
		margin: 0;
	}

	/* --- 4. 空状态 --- */
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding-top: 160rpx;
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
		color: var(--text-primary);
		margin-bottom: 12rpx;
	}

	.empty-sub {
		font-size: 26rpx;
		color: var(--text-tertiary);
		margin-bottom: 48rpx;
	}

	.go-shop-btn {
		background-color: var(--bg-primary);
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