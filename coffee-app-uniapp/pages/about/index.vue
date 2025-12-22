<template>
	<view class="about-container">
		<!-- 顶部占位，如果不需要导航栏背景可去掉 -->
		<view class="status-placeholder"></view>
		<view class="logo-section">
			<image src="https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?q=80&w=250&auto=format&fit=crop" mode="aspectFit" class="logo"></image>
			<text class="app-name">SmartBrew</text>
			<text class="version">Version 1.0.0</text>
		</view>

		<view class="content-section">
			<view class="section-title">品牌故事</view>
			<text class="section-text">
				SmartBrew 致力于将尖端 AI 技术与传统咖啡工艺相结合。我们相信，每一杯咖啡都应该是一场独特的感官之旅。
				通过我们的智能云端系统，我们为您精准匹配最适合您口味的豆子和冲煮参数，让每一口都散发着智慧与醇香。
			</text>
		</view>

		<view class="menu-list">
			<view class="menu-item" @click="navTo('/pages/about/privacy')">
				<text>隐私政策</text>
				<uni-icons type="right" size="14" color="#ccc"></uni-icons>
			</view>
			<view class="menu-item">
				<text>用户协议</text>
				<uni-icons type="right" size="14" color="#ccc"></uni-icons>
			</view>
			<view class="menu-item" @click="handleUpdate">
				<text>检查更新</text>
				<uni-icons type="right" size="14" color="#ccc"></uni-icons>
			</view>
		</view>

		<view class="footer">
			<text>SmartBrew Coffee App © 2024</text>
		</view>
	</view>
</template>

<script setup>
	const navTo = (url) => {
		uni.navigateTo({ url })
	}

	const handleUpdate = () => {
		uni.showLoading({ title: '检查中...' })
		setTimeout(() => {
			uni.hideLoading()
			uni.showToast({ title: '当前已是最新版本', icon: 'none' })
		}, 1000)
	}
</script>

<style lang="scss" scoped>
	.about-container {
		min-height: 100vh;
		background-color: #fff;
		display: flex;
		flex-direction: column;
		padding: 60rpx 40rpx;
	}
    /* 顶部状态栏占位 */
	.status-placeholder {
		height: var(--status-bar-height);
		width: 100%;
	}

	.logo-section {
		display: flex;
		flex-direction: column;
		align-items: center;
		margin-bottom: 80rpx;

		.logo {
			width: 160rpx;
			height: 160rpx;
			border-radius: 30rpx;
			margin-bottom: 30rpx;
			box-shadow: 0 10rpx 30rpx rgba(0,0,0,0.1);
		}

		.app-name {
			font-size: 40rpx;
			font-weight: bold;
			color: #333;
			margin-bottom: 10rpx;
		}

		.version {
			font-size: 24rpx;
			color: #999;
		}
	}

	.content-section {
		margin-bottom: 60rpx;
		.section-title {
			font-size: 32rpx;
			font-weight: bold;
			color: #333;
			margin-bottom: 20rpx;
		}
		.section-text {
			font-size: 28rpx;
			color: #666;
			line-height: 1.8;
		}
	}

	.menu-list {
		border-top: 1rpx solid #f5f5f5;
		.menu-item {
			display: flex;
			justify-content: space-between;
			align-items: center;
			padding: 30rpx 0;
			border-bottom: 1rpx solid #f5f5f5;
			font-size: 30rpx;
			color: #333;
		}
	}

	.footer {
		margin-top: auto;
		text-align: center;
		padding-top: 60rpx;
		font-size: 24rpx;
		color: #ccc;
	}
</style>

