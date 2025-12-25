<template>
	<view class="skeleton-container">
		<!-- 骨架屏内容 -->
		<slot>
			<!-- 默认骨架屏 -->
			<view class="skeleton-item" v-for="i in rows" :key="i">
				<view class="skeleton-line" :style="{ width: lineWidth }"></view>
			</view>
		</slot>
	</view>
</template>

<script setup>
defineProps({
	rows: {
		type: Number,
		default: 3
	},
	lineWidth: {
		type: String,
		default: '100%'
	}
})
</script>

<style lang="scss" scoped>
.skeleton-container {
	width: 100%;
}

.skeleton-item {
	margin-bottom: 24rpx;
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

