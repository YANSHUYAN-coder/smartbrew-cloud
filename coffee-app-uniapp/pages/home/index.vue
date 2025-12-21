<template>
	<view class="home-page">
		<!-- 顶部定位（包含状态栏占位） -->
		<view class="header-container" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="header-bar">
				<view class="header-top">
					<view class="location-info" @click="handleLocationClick">
						<text class="location-text">智咖·云</text>
						<text class="chevron">›</text>
					</view>
					<uni-icons custom-prefix="iconfont" type="icon-message"
					color="#000" size="24"></uni-icons>
				</view>
			</view>
		</view>

		<!-- ... (Banner 和 其他部分保持不变) ... -->
		<!-- 沉浸式 Banner -->
		<view class="banner-container">
			<view class="banner">
				<image
					src="https://images.unsplash.com/photo-1447933601403-0c6688de566e?q=80&w=800&auto=format&fit=crop"
					class="banner-image" mode="aspectFill" />
				<view class="banner-content">
					<view class="banner-badge">NEW ARRIVAL</view>
					<text class="banner-title">秋日桂花拿铁</text>
					<text class="banner-desc">金桂飘香，一口入秋</text>
					<button class="banner-btn" @click="handleBannerClick">立即尝鲜</button>
				</view>
			</view>
		</view>

		<!-- AI 智能助手卡片 -->
		<view class="ai-assistant-card" @click="handleSearchClick">
			<view class="ai-card-content">
				<view class="ai-icon-box">
					<text class="ai-icon">🤖</text>
				</view>
				<view class="ai-text-box">
					<text class="ai-title">AI 智能推荐</text>
					<text class="ai-desc">今天适合喝什么？</text>
				</view>
				<uni-icons type="right" size="20" color="#999"></uni-icons>
			</view>
		</view>

		<!-- 功能金刚区 -->
		<view class="function-grid">
			<view v-for="(item, index) in functions" :key="index" class="function-item"
				@click="handleFunctionClick(item)">
				<view class="function-icon">{{ item.icon }}</view>
				<text class="function-name">{{ item.name }}</text>
			</view>
		</view>

		<!-- 本周新品 (New Arrivals) -->
		<view class="new-arrival-section">
			<view class="section-header">
				<view class="title-wrapper">
					<text class="section-title">本周新品</text>
					<text class="title-badge">NEW</text>
				</view>
				<text class="section-more" @click="handleViewAll">全部 ›</text>
			</view>

			<scroll-view class="new-product-scroll" scroll-x show-scrollbar="false">
				<view class="scroll-inner">
					<view v-for="product in newProducts" :key="product.id" class="new-product-card"
						@click="handleProductClick(product)">
						<view class="new-img-box">
							<image :src="product.image" mode="aspectFill" class="new-img" />
							<view class="new-tag">上新</view>
						</view>
						<text class="new-name">{{ product.name }}</text>
						<text class="new-price">¥{{ product.price }}</text>
						<view class="new-add-btn" @click.stop="handleAddToCart(product)">+</view>
					</view>
				</view>
			</scroll-view>
		</view>

		<!-- 推荐列表 -->
		<view class="recommend-section">
			<view class="section-header">
				<text class="section-title">猜你喜欢</text>
				<text class="section-more" @click="handleViewAll">查看全部 ›</text>
			</view>
			<view class="product-grid">
				<view v-for="product in recommendProducts" :key="product.id" class="product-card"
					@click="handleProductClick(product)">
					<view class="product-image-wrapper">
						<image :src="product.image" class="product-image" mode="aspectFill" />
						<view class="product-rating">
							<text class="star">⭐</text>
							<text class="rating-text">{{ product.rating }}</text>
						</view>
					</view>
					<view class="product-info">
						<text class="product-name">{{ product.name }}</text>
						<text class="product-desc">{{ product.desc }}</text>
						<view class="product-footer">
							<text class="product-price">¥{{ product.price }}</text>
							<view class="add-btn" @click.stop="handleAddToCart(product)">
								<text class="add-icon">+</text>
							</view>
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
		onLoad,
		onPullDownRefresh
	} from '@dcloudio/uni-app'
	import {
		PRODUCTS
	} from '@/utils/data.js'
	import {
		useCartStore
	} from '@/store/cart.js'
	import {
		getStatusBarHeight
	} from '@/utils/system.js'

	const cartStore = useCartStore()
	const statusBarHeight = ref(0)

	// 模拟新品数据 (取最后3个商品作为新品)
	const newProducts = computed(() => PRODUCTS.slice(-3))

	const functions = [{
			icon: '☕',
			name: '到店取'
		},
		{
			icon: '🛵',
			name: '外卖'
		},
		{
			icon: '🎁',
			name: '礼品卡'
		},
		{
			icon: '💎',
			name: '会员'
		},
	]

	const recommendProducts = computed(() => PRODUCTS.slice(0, 4))

	const handleLocationClick = () => {
		uni.showToast({
			title: '选择门店',
			icon: 'none'
		})
	}

	// AI 助手点击事件
	const handleSearchClick = () => {
	  // 检查是否登录（可选）
	  const token = uni.getStorageSync('token')
	  if (!token) {
	    uni.navigateTo({ url: '/pages/login/index' })
	    return
	  }
	  
	  // 跳转到 AI 聊天页
	  uni.navigateTo({
	    url: '/pages/ai/chat'
	  })
	}

	const handleBannerClick = () => {
		uni.switchTab({
			url: '/pages/menu/index'
		})
	}

	const handleFunctionClick = (item) => {
		switch (item.name) {
			case '到店取':
				// 跳转到点单页，通过全局状态或参数告知是自提
				uni.setStorageSync('orderType', 'pickup');
				uni.switchTab({
					url: '/pages/menu/index'
				});
				break;

			case '外卖':
				// 跳转到点单页，告知是外卖
				uni.setStorageSync('orderType', 'delivery');
				uni.switchTab({
					url: '/pages/menu/index'
				});
				break;

			case '礼品卡':
				uni.showToast({
					title: '礼品卡功能开发中...',
					icon: 'none'
				});
				// uni.navigateTo({ url: '/pages/gift-card/index' });
				break;

			case '会员':
				// 跳转到“我的”页面
				uni.switchTab({
					url: '/pages/profile/index'
				});
				break;
		}
	}

	const handleViewAll = () => {
		uni.switchTab({
			url: '/pages/menu/index'
		})
	}

	const handleProductClick = (product) => {
		uni.switchTab({
			url: '/pages/menu/index'
		})
	}

	const handleAddToCart = (product) => {
		cartStore.addToCart(product)
		uni.showToast({
			title: '已加入购物车',
			icon: 'success'
		})
	}

	// 加载首页数据
	const loadHomeData = async () => {
		// 这里可以添加加载首页数据的逻辑
		// 例如：获取推荐商品、新品等
		// 目前使用模拟数据，所以暂时不需要
		return true
	}

	// 下拉刷新
	onPullDownRefresh(async () => {
		await loadHomeData()
		// 停止下拉刷新动画
		uni.stopPullDownRefresh()
	})

	onMounted(() => {
		statusBarHeight.value = getStatusBarHeight()
		loadHomeData()
	})
</script>

<style lang="scss" scoped>
	/* ... (保留之前的样式) ... */
	.home-page {
		min-height: 100vh;
		background-color: #f5f5f5;
		padding-bottom: calc(100rpx + env(safe-area-inset-bottom));
	}

	.header-container {
		position: sticky;
		top: 0;
		z-index: 40;
		background-color: rgba(255, 255, 255, 0.95);
		backdrop-filter: blur(10rpx);
		-webkit-backdrop-filter: blur(10rpx);
		box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
		transition: box-shadow 0.3s;
	}

	.header-bar {
		padding: 24rpx 40rpx 24rpx;
	}

	.header-top {
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	/* ... (location-info, bell-icon 样式保持不变) ... */
	.location-info {
		display: flex;
		align-items: center;
		gap: 8rpx;
	}

	.location-text {
		font-size: 40rpx;
		font-weight: bold;
		color: #333;
	}

	.chevron {
		font-size: 32rpx;
		color: #999;
	}


	/* AI 智能助手卡片样式 - 简洁优雅版 */
	.ai-assistant-card {
		padding: 0 40rpx;
		margin-top: 24rpx;
		margin-bottom: 24rpx;
	}

	.ai-card-content {
		background-color: white;
		border-radius: 24rpx;
		padding: 32rpx;
		display: flex;
		align-items: center;
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
		transition: all 0.2s;
		border: 1rpx solid #f0f0f0;
	}

	.ai-card-content:active {
		transform: scale(0.98);
		box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
		background-color: #fafafa;
	}

	.ai-icon-box {
		width: 80rpx;
		height: 80rpx;
		border-radius: 20rpx;
		background: linear-gradient(135deg, #f5f1eb 0%, #e8ddd4 100%);
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 24rpx;
		flex-shrink: 0;
	}

	.ai-icon {
		font-size: 44rpx;
	}

	.ai-text-box {
		flex: 1;
		display: flex;
		flex-direction: column;
		gap: 8rpx;
	}

	.ai-title {
		font-size: 30rpx;
		font-weight: bold;
		color: #333;
	}

	.ai-desc {
		font-size: 24rpx;
		color: #999;
	}

	/* ... (后续样式保持不变) ... */
	.banner-container {
		padding: 32rpx 40rpx;
	}

	.banner {
		height: 384rpx;
		border-radius: 32rpx;
		background: linear-gradient(135deg, #6f4e37 0%, #8d6e53 100%);
		position: relative;
		overflow: hidden;
	}

	.banner-image {
		width: 100%;
		height: 100%;
		opacity: 0.8;
	}

	.banner-content {
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		display: flex;
		flex-direction: column;
		justify-content: center;
		padding: 48rpx;
		color: white;
	}

	.banner-badge {
		font-size: 24rpx;
		font-weight: 500;
		letter-spacing: 2rpx;
		background-color: rgba(255, 255, 255, 0.2);
		backdrop-filter: blur(10rpx);
		padding: 8rpx 16rpx;
		border-radius: 8rpx;
		width: fit-content;
		margin-bottom: 16rpx;
	}

	.banner-title {
		font-size: 48rpx;
		font-weight: bold;
		margin-bottom: 8rpx;
	}

	.banner-desc {
		font-size: 28rpx;
		color: rgba(255, 255, 255, 0.9);
		margin-bottom: 32rpx;
	}

	.banner-btn {
		background-color: white;
		color: #6f4e37;
		padding: 16rpx 32rpx;
		border-radius: 50rpx;
		font-size: 28rpx;
		font-weight: bold;
		width: fit-content;
		border: none;
		transition: transform 0.2s, box-shadow 0.2s;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
	}

	.banner-btn:active {
		transform: scale(0.95);
		box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
	}

	.function-grid {
		display: grid;
		grid-template-columns: repeat(4, 1fr);
		gap: 32rpx;
		padding: 0 40rpx;
	}

	.function-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 16rpx;
	}

	.function-icon {
		width: 96rpx;
		height: 96rpx;
		border-radius: 32rpx;
		background-color: #f5f5f5;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 48rpx;
		box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
		transition: transform 0.2s, background-color 0.2s;
	}

	.function-item:active .function-icon {
		transform: scale(1.1);
		background-color: rgba(111, 78, 55, 0.1);
	}

	.function-name {
		font-size: 24rpx;
		color: #666;
		font-weight: 500;
	}

	.recommend-section {
		padding: 0 40rpx;
		margin-top: 48rpx;
	}

	.section-header {
		display: flex;
		justify-content: space-between;
		align-items: flex-end;
		margin-bottom: 32rpx;
	}

	.section-title {
		font-size: 36rpx;
		font-weight: bold;
		color: #333;
	}

	.section-more {
		font-size: 24rpx;
		color: #999;
	}

	.product-grid {
		display: grid;
		grid-template-columns: repeat(2, 1fr);
		gap: 32rpx;
	}

	.product-card {
		background-color: white;
		border-radius: 24rpx;
		overflow: hidden;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
		transition: transform 0.2s, box-shadow 0.2s;
	}

	.product-card:active {
		transform: scale(0.98);
		box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
	}

	.product-image-wrapper {
		position: relative;
		height: 256rpx;
		overflow: hidden;
	}

	.product-image {
		width: 100%;
		height: 100%;
	}

	.product-rating {
		position: absolute;
		top: 16rpx;
		right: 16rpx;
		background-color: rgba(255, 255, 255, 0.9);
		backdrop-filter: blur(10rpx);
		padding: 8rpx 12rpx;
		border-radius: 8rpx;
		display: flex;
		align-items: center;
		gap: 4rpx;
		font-size: 20rpx;
		font-weight: bold;
	}

	.star {
		font-size: 20rpx;
	}

	.rating-text {
		font-size: 20rpx;
	}

	.product-info {
		padding: 24rpx;
	}

	.product-name {
		font-size: 28rpx;
		font-weight: bold;
		color: #333;
		display: block;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.product-desc {
		font-size: 24rpx;
		color: #999;
		margin-top: 8rpx;
		display: block;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.product-footer {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-top: 24rpx;
	}

	.product-price {
		font-size: 32rpx;
		font-weight: bold;
		color: #6f4e37;
	}

	.add-btn {
		width: 48rpx;
		height: 48rpx;
		border-radius: 50%;
		background-color: #6f4e37;
		color: white;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 4rpx 12rpx rgba(111, 78, 55, 0.3);
		transition: transform 0.2s, box-shadow 0.2s;
	}

	.add-btn:active {
		transform: scale(0.9);
		box-shadow: 0 2rpx 8rpx rgba(111, 78, 55, 0.4);
	}

	.add-icon {
		font-size: 28rpx;
		font-weight: bold;
	}

	/* 新品模块样式 */
	.new-arrival-section {
		padding: 0 40rpx;
		/* 左边留白，右边不留白以便滑动 */
		margin-top: 48rpx;
	}

	.title-wrapper {
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	.title-badge {
		font-size: 20rpx;
		color: white;
		background-color: #ff4d4f;
		/* 醒目的红色 */
		padding: 4rpx 10rpx;
		border-radius: 8rpx;
		font-weight: bold;
		transform: translateY(-2rpx);
	}

	.new-product-scroll {
		width: 100%;
		white-space: nowrap;
		margin-top: 24rpx;
	}

	.scroll-inner {
		display: flex;
		gap: 24rpx;
		padding-right: 40rpx;
		/* 列表尾部留白 */
	}

	.new-product-card {
		display: inline-block;
		width: 240rpx;
		background-color: white;
		border-radius: 20rpx;
		padding: 16rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
		position: relative;
	}

	.new-img-box {
		width: 100%;
		height: 200rpx;
		border-radius: 16rpx;
		overflow: hidden;
		position: relative;
		margin-bottom: 16rpx;
	}

	.new-img {
		width: 100%;
		height: 100%;
	}

	.new-tag {
		position: absolute;
		top: 0;
		left: 0;
		background-color: #6f4e37;
		color: white;
		font-size: 18rpx;
		padding: 4rpx 12rpx;
		border-bottom-right-radius: 16rpx;
	}

	.new-name {
		font-size: 26rpx;
		font-weight: bold;
		color: #333;
		display: block;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
		margin-bottom: 8rpx;
	}

	.new-price {
		font-size: 28rpx;
		font-weight: bold;
		color: #6f4e37;
	}

	.new-add-btn {
		position: absolute;
		bottom: 16rpx;
		right: 16rpx;
		width: 40rpx;
		height: 40rpx;
		border-radius: 50%;
		background-color: #f5f5f5;
		color: #333;
		display: flex;
		align-items: center;
		justify-content: center;
		font-weight: bold;
		font-size: 32rpx;
	}
</style>