<template>
	<view class="menu-page">
		<!-- 1. 顶部 Header (固定) -->
		<view class="header-wrapper" :style="{ paddingTop: statusBarHeight + 'px' }">
			<!-- 搜索与切换行 -->
			<view class="header-content">
				<view class="search-bar">
					<text class="search-icon">🔍</text>
					<input class="search-input" type="text" placeholder="搜拿铁/美式/甜点" disabled @click="handleSearch" />
				</view>
				<!-- 业务切换胶囊 -->
				<view class="order-type-switch">
					<view class="switch-item" :class="{ active: orderType === 'pickup' }" @click="orderType = 'pickup'">
						自提</view>
					<view class="switch-item" :class="{ active: orderType === 'delivery' }"
						@click="orderType = 'delivery'">外送</view>
				</view>
			</view>

			<!-- 门店/地址提示 -->
			<view class="shop-info">
				<view class="shop-name-row">
					<text class="shop-name">智咖·云 (科兴科学园店)</text>
					<text class="distance">距您 120m</text>
				</view>
				<text class="shop-desc" v-if="orderType === 'pickup'">营业中 07:30-22:00 · 制作约3分钟</text>
				<text class="shop-desc" v-else>免配送费 · 预计30分钟送达</text>
			</view>
		</view>

		<!-- 2. 主体内容区 (左右联动) -->
		<view class="menu-container">
			<!-- 左侧分类栏 -->
			<scroll-view class="category-sidebar" scroll-y :scroll-into-view="leftScrollId" scroll-with-animation>
				<view v-for="(cat, index) in categories" :key="cat.id" :id="'cat-left-' + index" class="category-item"
					:class="{ active: activeCategoryIndex === index }" @click="handleCategoryClick(index)">
					<view class="category-icon" v-if="cat.icon">{{ cat.icon }}</view>
					<text class="category-name">{{ cat.name }}</text>
					<!-- 选中指示器 -->
					<view v-if="activeCategoryIndex === index" class="active-indicator"></view>
					<!-- 分类角标 (模拟：仅第一个分类 HOT) -->
					<view v-if="index === 0" class="cat-badge">NEW</view>
				</view>
				<!-- 底部占位 -->
				<view style="height: 100rpx;"></view>
			</scroll-view>

			<!-- 右侧商品列表 -->
			<scroll-view class="product-list" scroll-y :scroll-into-view="rightScrollId" scroll-with-animation
				@scroll="onRightScroll">
				<view class="product-wrapper">
					<view v-for="(cat, cIndex) in categories" :key="cat.id" :id="'cat-right-' + cIndex"
						class="category-section">
						<!-- 分类标题 -->
						<view class="section-title">{{ cat.name }}</view>

						<!-- 商品卡片 -->
						<!-- 逻辑：如果是 'new' 分类，展示所有新品(模拟前2个)；否则展示对应分类商品 -->
						<view
							v-for="prod in (cat.id === 'new' ? products.slice(0,2) : products.filter(p => p.categoryId === cat.id))"
							:key="prod.id" class="product-item" @click="openSkuModal(prod)">
							<image :src="prod.image" mode="aspectFill" class="prod-img" />
							<view class="prod-info">
								<view class="prod-header">
									<text class="prod-name">{{ prod.name }}</text>
									<text class="prod-desc">{{ prod.desc }}</text>
								</view>

								<view class="prod-tags">
									<text class="tag" v-if="cat.id === 'new'">本周新品</text>
									<text class="tag" v-else>人气热销</text>
									<text class="tag">好评 {{ prod.rating }}</text>
								</view>

								<view class="prod-footer">
									<text class="price">¥<text class="price-num">{{ prod.price }}</text></text>
									<!-- 选规格按钮 -->
									<view class="add-btn-wrapper" @click.stop="openSkuModal(prod)">
										<text class="spec-btn">选规格</text>
										<!-- 如果有数量，显示角标 -->
										<view class="badge" v-if="getCartCountById(prod.id) > 0">
											{{ getCartCountById(prod.id) }}
										</view>
									</view>
								</view>
							</view>
						</view>
					</view>
					<!-- 底部垫高，防止被购物车遮挡 -->
					<view style="height: 180rpx;"></view>
				</view>
			</scroll-view>
		</view>

		<!-- 3. 悬浮购物车条 -->
		<view class="cart-bar-wrapper" v-if="cartStore.totalCount > 0">
			<view class="cart-bar" @click="handleCartClick">
				<view class="cart-icon-wrap">
					<image src="https://img.icons8.com/color/96/shopping-bag--v1.png" class="cart-img" />
					<view class="total-badge">{{ cartStore.totalCount }}</view>
				</view>
				<view class="price-info">
					<text class="total-price">¥{{ cartStore.totalPrice.toFixed(2) }}</text>
					<text class="delivery-tip">预计 15 分钟后可取</text>
				</view>
				<view class="checkout-btn" @click.stop="handleCheckout">
					去结算
				</view>
			</view>
		</view>

		<!-- 4. 规格选择弹窗 (高级毛玻璃效果) -->
		<view class="sku-modal-mask" v-if="showModal" @click="closeModal">
			<view class="sku-modal" @click.stop>
				<!-- 头部 -->
				<view class="modal-header">
					<image :src="currentProduct.image" class="modal-img" mode="aspectFill" />
					<view class="modal-info">
						<text class="modal-name">{{ currentProduct.name }}</text>
						<text class="modal-desc">{{ currentProduct.desc }}</text>
					</view>
					<view class="close-btn" @click="closeModal">×</view>
				</view>

				<!-- 规格选项 -->
				<scroll-view scroll-y class="modal-scroll">
					<view class="spec-group">
						<text class="spec-title">温度</text>
						<view class="spec-options">
							<view class="spec-opt active">标准冰</view>
							<view class="spec-opt">少冰</view>
							<view class="spec-opt">热</view>
						</view>
					</view>
					<view class="spec-group">
						<text class="spec-title">糖度</text>
						<view class="spec-options">
							<view class="spec-opt active">标准糖</view>
							<view class="spec-opt">半糖</view>
							<view class="spec-opt">不加糖</view>
						</view>
					</view>
				</scroll-view>

				<!-- 底部操作 -->
				<view class="modal-footer">
					<view class="price-box">
						<text class="symbol">¥</text>
						<text class="num">{{ currentProduct.price }}</text>
						<text class="selected-spec">已选: 标准冰, 标准糖</text>
					</view>
					<view class="action-box">
						<!-- 简单的加减器 -->
						<view class="stepper" v-if="currentTempCount > 0">
							<view class="step-btn minus" @click="updateTempCount(-1)">-</view>
							<text class="step-num">{{ currentTempCount }}</text>
							<view class="step-btn plus" @click="updateTempCount(1)">+</view>
						</view>
						<view class="add-cart-btn" v-else @click="updateTempCount(1)">
							加入购物车
						</view>
					</view>
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
		getStatusBarHeight
	} from '@/utils/system.js'
	import {
		PRODUCTS
	} from '@/utils/data.js'
	import {
		useCartStore
	} from '@/store/cart.js'

	import { getCategories } from '@/apis/categories.js'

	const statusBarHeight = ref(0)
	const cartStore = useCartStore()

	// 状态管理
	const orderType = ref('pickup') // pickup | delivery
	const activeCategoryIndex = ref(0)
	const leftScrollId = ref('cat-left-0')
	const rightScrollId = ref('')
	const showModal = ref(false)
	const currentProduct = ref({})
	const currentTempCount = ref(0)

	// 数据别名
	// 【新增】在最前面添加新品分类
	const categories = [{
			id: 'new',
			name: '人气新品',
			icon: '🔥'
		}, // 新增的分类
		{
			id: 'c1',
			name: '大师咖啡'
		},
		{
			id: 'c2',
			name: '零度拿铁'
		},
		{
			id: 'c3',
			name: '瑞纳冰'
		},
		{
			id: 'c4',
			name: '经典甜点'
		},
	];
	const products = PRODUCTS
	
	// 获取商品分类
	console.log("获取商品分类",getCategories());
	

	// --- 左右联动逻辑 ---
	const handleCategoryClick = (index) => {
		activeCategoryIndex.value = index
		rightScrollId.value = 'cat-right-' + index
	}

	// 模拟右侧滚动监听 (UniApp中精确监听需要 boundingClientRect)
	// 这里简化处理：暂时不做滚动右侧自动高亮左侧的复杂计算，
	// 实际开发中可以使用 uni.createIntersectionObserver
	const onRightScroll = (e) => {
		// 留给后续优化
	}

	// --- 弹窗逻辑 ---
	const openSkuModal = (product) => {
		currentProduct.value = product
		// 获取当前商品在购物车中的数量 (简化：只获取总数，不分规格)
		const cartItem = cartStore.items.find(i => i.id === product.id)
		currentTempCount.value = cartItem ? cartItem.quantity : 0
		showModal.value = true
	}

	const closeModal = () => {
		showModal.value = false
	}

	const updateTempCount = (delta) => {
		const newCount = currentTempCount.value + delta
		if (newCount < 0) return
		currentTempCount.value = newCount

		// 同步到 Store
		if (delta > 0) {
			cartStore.addToCart(currentProduct.value)
		} else {
			// 这里简单处理，实际 store 应该有减少方法
			// 暂时先调用 add(-1) 的逻辑需要 store 支持
			// 这里仅仅是演示 UI
			cartStore.addToCart(currentProduct.value) // ⚠️ Mock: 实际应该减少
		}
	}

	const getCartCountById = (pid) => {
		const item = cartStore.items.find(i => i.id === pid)
		return item ? item.quantity : 0
	}

	const handleCartClick = () => {
		uni.switchTab({
			url: '/pages/cart/index'
		})
	}

	const handleCheckout = () => {
		uni.navigateTo({
			url: '/pages/order/confirm'
		})
	}

	const handleSearch = () => {
		uni.showToast({
			title: '搜索功能',
			icon: 'none'
		})
	}

	onMounted(() => {
		statusBarHeight.value = getStatusBarHeight()
	})
</script>

<style lang="scss" scoped>
	/* 定义主题色 */
	$primary: #6f4e37;
	$bg-gray: #f8f8f8;

	.menu-page {
		height: 100vh;
		display: flex;
		flex-direction: column;
		background-color: white;
	}

	/* --- 1. Header 样式 --- */
	.header-wrapper {
		background-color: white;
		z-index: 50;
		box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.03);
	}

	.header-content {
		display: flex;
		align-items: center;
		padding: 16rpx 24rpx;
		gap: 20rpx;
	}

	.search-bar {
		flex: 1;
		height: 64rpx;
		background-color: #f5f5f5;
		border-radius: 32rpx;
		display: flex;
		align-items: center;
		padding: 0 24rpx;
	}

	.search-icon {
		font-size: 28rpx;
		color: #999;
		margin-right: 10rpx;
	}

	.search-input {
		font-size: 26rpx;
		width: 100%;
		color: #333;
	}

	.order-type-switch {
		display: flex;
		background-color: #f5f5f5;
		border-radius: 32rpx;
		padding: 4rpx;
		height: 64rpx;
		box-sizing: border-box;
	}

	.switch-item {
		padding: 0 24rpx;
		font-size: 26rpx;
		color: #666;
		border-radius: 28rpx;
		display: flex;
		align-items: center;
		transition: all 0.2s;
	}

	.switch-item.active {
		background-color: #333;
		color: #d4af37;
		/* 黑金配色 */
		font-weight: bold;
	}

	.shop-info {
		padding: 0 24rpx 20rpx;
	}

	.shop-name-row {
		display: flex;
		align-items: baseline;
		gap: 12rpx;
		margin-bottom: 4rpx;
	}

	.shop-name {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
	}

	.distance {
		font-size: 22rpx;
		color: #999;
	}

	.shop-desc {
		font-size: 22rpx;
		color: #666;
	}

	/* --- 2. 主体容器 --- */
	.menu-container {
		flex: 1;
		display: flex;
		overflow: hidden;
	}

	/* 左侧分类 */
	.category-sidebar {
		width: 180rpx;
		background-color: #f7f8fa;
		height: 100%;
	}

	.category-item {
		height: 100rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 26rpx;
		color: #666;
		position: relative;
		transition: all 0.2s;
	}

	.category-item.active {
		background-color: white;
		color: #333;
		font-weight: bold;
		font-size: 28rpx;
	}

	.active-indicator {
		position: absolute;
		left: 0;
		top: 30rpx;
		bottom: 30rpx;
		width: 6rpx;
		background-color: $primary;
		border-radius: 0 4rpx 4rpx 0;
	}

	.cat-badge {
		position: absolute;
		top: 10rpx;
		right: 10rpx;
		font-size: 16rpx;
		background: #ff4d4f;
		color: white;
		padding: 2rpx 6rpx;
		border-radius: 6rpx;
		transform: scale(0.8);
	}

	/* 右侧商品 */
	.product-list {
		flex: 1;
		height: 100%;
		background-color: white;
	}

	.category-section {
		padding: 20rpx 24rpx 0;
	}

	.section-title {
		font-size: 26rpx;
		color: #333;
		font-weight: bold;
		margin-bottom: 20rpx;
		padding-left: 8rpx;
	}

	/* 商品卡片 */
	.product-item {
		display: flex;
		margin-bottom: 40rpx;
	}

	.prod-img {
		width: 180rpx;
		height: 180rpx;
		border-radius: 16rpx;
		background-color: #eee;
		flex-shrink: 0;
	}

	.prod-info {
		flex: 1;
		margin-left: 20rpx;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		padding-bottom: 8rpx;
	}

	.prod-name {
		font-size: 30rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 8rpx;
	}

	.prod-desc {
		font-size: 22rpx;
		color: #999;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
		max-width: 300rpx;
	}

	.prod-tags {
		display: flex;
		gap: 8rpx;
		margin-top: 8rpx;
	}

	.tag {
		font-size: 20rpx;
		color: $primary;
		background: rgba(111, 78, 55, 0.1);
		padding: 2rpx 8rpx;
		border-radius: 6rpx;
	}

	.prod-footer {
		display: flex;
		justify-content: space-between;
		align-items: flex-end;
	}

	.price {
		font-size: 24rpx;
		color: $primary;
		font-weight: bold;
	}

	.price-num {
		font-size: 34rpx;
	}

	.add-btn-wrapper {
		position: relative;
	}

	.spec-btn {
		background-color: $primary;
		color: white;
		font-size: 22rpx;
		padding: 10rpx 24rpx;
		border-radius: 24rpx;
		font-weight: bold;
	}

	.badge {
		position: absolute;
		top: -12rpx;
		right: -12rpx;
		background-color: #ff4d4f;
		color: white;
		font-size: 20rpx;
		min-width: 32rpx;
		height: 32rpx;
		border-radius: 16rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		border: 2rpx solid white;
	}

	/* --- 3. 悬浮购物车 --- */
	.cart-bar-wrapper {
		position: fixed;
		bottom: 30rpx;
		/* 距离底部 */
		left: 30rpx;
		right: 30rpx;
		z-index: 90;
	}

	.cart-bar {
		background-color: #222;
		height: 100rpx;
		border-radius: 50rpx;
		display: flex;
		align-items: center;
		padding-left: 140rpx;
		/* 留出图标位置 */
		padding-right: 6rpx;
		position: relative;
		box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.3);
	}

	.cart-icon-wrap {
		position: absolute;
		left: 0;
		bottom: 0;
		width: 120rpx;
		height: 120rpx;
		/* 溢出部分用于展示大图标 */
	}

	.cart-img {
		width: 100%;
		height: 100%;
		transform: scale(1.1) translateY(-10rpx);
	}

	.total-badge {
		position: absolute;
		top: 10rpx;
		right: 10rpx;
		background-color: #ff4d4f;
		color: white;
		font-size: 22rpx;
		padding: 4rpx 12rpx;
		border-radius: 20rpx;
		border: 2rpx solid #222;
	}

	.price-info {
		flex: 1;
		display: flex;
		flex-direction: column;
		justify-content: center;
	}

	.total-price {
		color: white;
		font-size: 34rpx;
		font-weight: bold;
	}

	.delivery-tip {
		color: #888;
		font-size: 20rpx;
	}

	.checkout-btn {
		background-color: $primary;
		color: white;
		height: 88rpx;
		padding: 0 48rpx;
		border-radius: 44rpx;
		display: flex;
		align-items: center;
		font-size: 30rpx;
		font-weight: bold;
	}

	/* --- 4. 规格弹窗 --- */
	.sku-modal-mask {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background-color: rgba(0, 0, 0, 0.5);
		z-index: 100;
		display: flex;
		align-items: flex-end;
	}

	.sku-modal {
		width: 100%;
		background-color: white;
		border-radius: 32rpx 32rpx 0 0;
		padding: 32rpx;
		animation: slideUp 0.3s ease-out;
	}

	@keyframes slideUp {
		from {
			transform: translateY(100%);
		}

		to {
			transform: translateY(0);
		}
	}

	.modal-header {
		display: flex;
		margin-bottom: 30rpx;
		position: relative;
	}

	.modal-img {
		width: 160rpx;
		height: 160rpx;
		border-radius: 12rpx;
		margin-right: 20rpx;
	}

	.modal-info {
		flex: 1;
	}

	.modal-name {
		font-size: 32rpx;
		font-weight: bold;
		display: block;
		margin-bottom: 8rpx;
	}

	.modal-desc {
		font-size: 24rpx;
		color: #999;
	}

	.close-btn {
		position: absolute;
		top: 0;
		right: 0;
		padding: 10rpx;
		font-size: 40rpx;
		color: #999;
		line-height: 1;
	}

	.modal-scroll {
		max-height: 500rpx;
		margin-bottom: 30rpx;
	}

	.spec-group {
		margin-bottom: 30rpx;
	}

	.spec-title {
		font-size: 26rpx;
		color: #666;
		margin-bottom: 16rpx;
		display: block;
	}

	.spec-options {
		display: flex;
		flex-wrap: wrap;
		gap: 20rpx;
	}

	.spec-opt {
		padding: 10rpx 30rpx;
		background-color: #f5f5f5;
		border-radius: 8rpx;
		font-size: 24rpx;
		color: #333;
		border: 2rpx solid transparent;
	}

	.spec-opt.active {
		background-color: rgba(111, 78, 55, 0.1);
		color: $primary;
		border-color: $primary;
		font-weight: bold;
	}

	.modal-footer {
		display: flex;
		justify-content: space-between;
		align-items: center;
		border-top: 1rpx solid #eee;
		padding-top: 20rpx;
	}

	.price-box {
		display: flex;
		align-items: baseline;
		flex-direction: column;
	}

	.symbol {
		font-size: 24rpx;
		color: $primary;
		font-weight: bold;
	}

	.num {
		font-size: 40rpx;
		color: $primary;
		font-weight: bold;
	}

	.selected-spec {
		font-size: 20rpx;
		color: #999;
		margin-top: 4rpx;
	}

	.action-box {
		display: flex;
		align-items: center;
	}

	.add-cart-btn {
		background-color: $primary;
		color: white;
		padding: 16rpx 40rpx;
		border-radius: 40rpx;
		font-size: 28rpx;
		font-weight: bold;
	}

	.stepper {
		display: flex;
		align-items: center;
		gap: 20rpx;
	}

	.step-btn {
		width: 50rpx;
		height: 50rpx;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 32rpx;
		font-weight: bold;
	}

	.step-btn.minus {
		border: 2rpx solid #ddd;
		color: #666;
	}

	.step-btn.plus {
		background-color: $primary;
		color: white;
	}

	.step-num {
		font-size: 30rpx;
		font-weight: bold;
		min-width: 40rpx;
		text-align: center;
	}
</style>