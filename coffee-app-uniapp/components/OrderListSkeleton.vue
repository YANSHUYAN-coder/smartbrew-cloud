<template>
	<view class="order-list-skeleton">
		<!-- 标签栏骨架 -->
		<view class="skeleton-tabs">
			<view v-for="i in 5" :key="i" class="skeleton-tab-item">
				<view class="skeleton-line" style="width: 80rpx;"></view>
			</view>
		</view>

		<!-- 订单卡片骨架 -->
		<view class="skeleton-order-list">
			<view v-for="i in 3" :key="i" class="skeleton-order-card">
				<!-- 订单头部 -->
				<view class="skeleton-order-header">
					<view class="skeleton-line" style="width: 200rpx;"></view>
					<view class="skeleton-line" style="width: 100rpx;"></view>
				</view>

				<!-- 商品列表 -->
				<view class="skeleton-order-goods">
					<view v-for="j in 2" :key="j" class="skeleton-goods-item">
						<view class="skeleton-goods-image"></view>
						<view class="skeleton-goods-info">
							<view class="skeleton-line" style="width: 200rpx; margin-bottom: 12rpx;"></view>
							<view class="skeleton-line" style="width: 150rpx; margin-bottom: 12rpx;"></view>
							<view class="skeleton-line" style="width: 100rpx;"></view>
						</view>
					</view>
				</view>

				<!-- 订单底部 -->
				<view class="skeleton-order-footer">
					<view class="skeleton-line" style="width: 150rpx;"></view>
					<view class="skeleton-line" style="width: 120rpx;"></view>
				</view>
			</view>
		</view>
	</view>
</template>

<style lang="scss" scoped>
.order-list-skeleton {
	background: #f7f8fa;
	min-height: 100vh;
}

.skeleton-tabs {
	display: flex;
	padding: 24rpx 32rpx;
	background: white;
	gap: 32rpx;
	overflow-x: auto;
}

.skeleton-tab-item {
	flex-shrink: 0;
}

.skeleton-order-list {
	padding: 24rpx 32rpx;
}

.skeleton-order-card {
	background: white;
	border-radius: 24rpx;
	padding: 32rpx;
	margin-bottom: 24rpx;
}

.skeleton-order-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 24rpx;
}

.skeleton-order-goods {
	margin-bottom: 24rpx;
}

.skeleton-goods-item {
	display: flex;
	margin-bottom: 24rpx;
}

.skeleton-goods-item:last-child {
	margin-bottom: 0;
}

.skeleton-goods-image {
	width: 120rpx;
	height: 120rpx;
	border-radius: 12rpx;
	background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
	background-size: 200% 100%;
	animation: skeleton-loading 1.5s ease-in-out infinite;
	margin-right: 24rpx;
	flex-shrink: 0;
}

.skeleton-goods-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: center;
}

.skeleton-order-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding-top: 24rpx;
	border-top: 1rpx solid #f0f0f0;
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

