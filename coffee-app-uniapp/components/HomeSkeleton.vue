<template>
	<view class="home-skeleton">
		<!-- Banner 骨架 -->
		<view class="skeleton-banner">
			<view class="skeleton-banner-content">
				<view class="skeleton-badge"></view>
				<view class="skeleton-title"></view>
				<view class="skeleton-desc"></view>
				<view class="skeleton-btn"></view>
			</view>
		</view>

		<!-- AI 助手卡片骨架 -->
		<view class="skeleton-ai-card">
			<view class="skeleton-ai-icon"></view>
			<view class="skeleton-ai-text">
				<view class="skeleton-line" style="width: 200rpx; margin-bottom: 16rpx;"></view>
				<view class="skeleton-line" style="width: 150rpx;"></view>
			</view>
		</view>

		<!-- 功能金刚区骨架 -->
		<view class="skeleton-function-grid">
			<view v-for="i in 4" :key="i" class="skeleton-function-item">
				<view class="skeleton-function-icon"></view>
				<view class="skeleton-line" style="width: 80rpx; margin-top: 16rpx;"></view>
			</view>
		</view>

		<!-- 本周新品骨架 -->
		<view class="skeleton-section">
			<view class="skeleton-section-header">
				<view class="skeleton-line" style="width: 200rpx;"></view>
				<view class="skeleton-line" style="width: 100rpx;"></view>
			</view>
			<scroll-view class="skeleton-scroll" scroll-x>
				<view class="skeleton-scroll-inner">
					<view v-for="i in 4" :key="i" class="skeleton-product-card">
						<view class="skeleton-product-image"></view>
						<view class="skeleton-line" style="width: 120rpx; margin-top: 16rpx;"></view>
						<view class="skeleton-line" style="width: 80rpx; margin-top: 8rpx;"></view>
					</view>
				</view>
			</scroll-view>
		</view>

		<!-- 推荐商品骨架 -->
		<view class="skeleton-section">
			<view class="skeleton-section-header">
				<view class="skeleton-line" style="width: 150rpx;"></view>
				<view class="skeleton-line" style="width: 100rpx;"></view>
			</view>
			<view class="skeleton-waterfall">
				<view v-for="i in 6" :key="i" class="skeleton-product-card-large">
					<view class="skeleton-product-image-large"></view>
					<view class="skeleton-line" style="width: 80%; margin-top: 16rpx;"></view>
					<view class="skeleton-line" style="width: 60%; margin-top: 8rpx;"></view>
				</view>
			</view>
		</view>
	</view>
</template>

<style lang="scss" scoped>
.home-skeleton {
	padding: 32rpx 40rpx;
}

.skeleton-banner {
	height: 384rpx;
	border-radius: 32rpx;
	background: linear-gradient(90deg, var(--bg-tertiary) 25%, var(--border-light) 50%, var(--bg-tertiary) 75%);
	background-size: 200% 100%;
	animation: skeleton-loading 1.5s ease-in-out infinite;
	position: relative;
	overflow: hidden;
	margin-bottom: 24rpx;
}

.skeleton-banner-content {
	position: absolute;
	bottom: 48rpx;
	left: 48rpx;
	right: 48rpx;
}

.skeleton-badge {
	width: 160rpx;
	height: 48rpx;
	background: rgba(255, 255, 255, 0.3);
	border-radius: 8rpx;
	margin-bottom: 16rpx;
}

.skeleton-title {
	width: 300rpx;
	height: 48rpx;
	background: rgba(255, 255, 255, 0.5);
	border-radius: 8rpx;
	margin-bottom: 8rpx;
}

.skeleton-desc {
	width: 250rpx;
	height: 32rpx;
	background: rgba(255, 255, 255, 0.4);
	border-radius: 8rpx;
	margin-bottom: 32rpx;
}

.skeleton-btn {
	width: 200rpx;
	height: 64rpx;
	background: rgba(255, 255, 255, 0.6);
	border-radius: 50rpx;
}

.skeleton-ai-card {
	display: flex;
	align-items: center;
	padding: 32rpx;
	background: var(--bg-primary);
	border-radius: 24rpx;
	margin-bottom: 24rpx;
}

.skeleton-ai-icon {
	width: 80rpx;
	height: 80rpx;
	border-radius: 20rpx;
	background: linear-gradient(90deg, var(--bg-tertiary) 25%, var(--border-light) 50%, var(--bg-tertiary) 75%);
	background-size: 200% 100%;
	animation: skeleton-loading 1.5s ease-in-out infinite;
	margin-right: 24rpx;
	flex-shrink: 0;
}

.skeleton-ai-text {
	flex: 1;
}

.skeleton-function-grid {
	display: flex;
	justify-content: space-between;
	margin-bottom: 32rpx;
}

.skeleton-function-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	flex: 1;
}

.skeleton-function-icon {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	background: linear-gradient(90deg, var(--bg-tertiary) 25%, var(--border-light) 50%, var(--bg-tertiary) 75%);
	background-size: 200% 100%;
	animation: skeleton-loading 1.5s ease-in-out infinite;
}

.skeleton-section {
	margin-bottom: 48rpx;
}

.skeleton-section-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 24rpx;
	padding: 0 8rpx;
}

.skeleton-scroll {
	white-space: nowrap;
}

.skeleton-scroll-inner {
	display: flex;
	gap: 24rpx;
}

.skeleton-product-card {
	width: 200rpx;
	flex-shrink: 0;
}

.skeleton-product-image {
	width: 200rpx;
	height: 200rpx;
	border-radius: 16rpx;
	background: linear-gradient(90deg, var(--bg-tertiary) 25%, var(--border-light) 50%, var(--bg-tertiary) 75%);
	background-size: 200% 100%;
	animation: skeleton-loading 1.5s ease-in-out infinite;
}

.skeleton-waterfall {
	display: flex;
	flex-wrap: wrap;
	justify-content: space-between;
}

.skeleton-product-card-large {
	width: 48%;
	margin-bottom: 24rpx;
}

.skeleton-product-image-large {
	width: 100%;
	height: 300rpx;
	border-radius: 16rpx;
	background: linear-gradient(90deg, var(--bg-tertiary) 25%, var(--border-light) 50%, var(--bg-tertiary) 75%);
	background-size: 200% 100%;
	animation: skeleton-loading 1.5s ease-in-out infinite;
}

.skeleton-line {
	height: 32rpx;
	background: linear-gradient(90deg, var(--bg-tertiary) 25%, var(--border-light) 50%, var(--bg-tertiary) 75%);
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

