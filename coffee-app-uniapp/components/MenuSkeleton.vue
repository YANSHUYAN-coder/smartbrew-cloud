<template>
	<view class="menu-skeleton">
		<!-- 左侧分类骨架 -->
		<view class="skeleton-category-list">
			<view v-for="i in 6" :key="i" class="skeleton-category-item">
				<view class="skeleton-line" style="width: 80rpx;"></view>
			</view>
		</view>

		<!-- 右侧商品列表骨架 -->
		<view class="skeleton-product-list">
			<view v-for="i in 8" :key="i" class="skeleton-product-item">
				<view class="skeleton-product-image"></view>
				<view class="skeleton-product-info">
					<view class="skeleton-line" style="width: 200rpx; margin-bottom: 16rpx;"></view>
					<view class="skeleton-line" style="width: 150rpx; margin-bottom: 16rpx;"></view>
					<view class="skeleton-line" style="width: 100rpx;"></view>
				</view>
			</view>
		</view>
	</view>
</template>

<style lang="scss" scoped>
.menu-skeleton {
	display: flex;
	height: calc(100vh - 100rpx);
}

.skeleton-category-list {
	width: 200rpx;
	background: #f5f5f5;
	padding: 24rpx 0;
}

.skeleton-category-item {
	padding: 24rpx 16rpx;
	margin-bottom: 16rpx;
}

.skeleton-product-list {
	flex: 1;
	padding: 24rpx;
	background: white;
}

.skeleton-product-item {
	display: flex;
	margin-bottom: 32rpx;
}

.skeleton-product-image {
	width: 180rpx;
	height: 180rpx;
	border-radius: 16rpx;
	background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
	background-size: 200% 100%;
	animation: skeleton-loading 1.5s ease-in-out infinite;
	margin-right: 24rpx;
	flex-shrink: 0;
}

.skeleton-product-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: center;
}

.skeleton-line {
	height: 32rpx;
	background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
	background-size: 200% 100%;
	border-radius: 8rpx;
	animation: skeleton-loading 1.5s ease-in-out infinite;
}

@keyframes skeleton-loading {
	0% {
		background-position: 200% 0;
	}
	100% {
		background-position: -200% 0;
	}
}
</style>

