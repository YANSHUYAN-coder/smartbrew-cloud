<template>
	<view class="privacy-container">
        <!-- 顶部占位，如果不需要导航栏背景可去掉 -->
		<view class="status-placeholder"></view>
		<scroll-view scroll-y class="content">
			<view class="title">隐私政策</view>
			<view class="update-time">更新日期：2024年12月22日</view>
			
			<view class="section">
				<view class="sub-title">1. 我们收集的信息</view>
				<text class="text">
					为了向您提供更优质的咖啡服务，我们可能会收集您的基本个人信息（如手机号、头像、昵称）、地理位置信息（用于为您推荐最近的门店）以及您的咖啡口味偏好。
				</text>
			</view>

			<view class="section">
				<view class="sub-title">2. 信息的使用</view>
				<text class="text">
					您的信息将主要用于订单处理、会员权益管理、AI智能口味推荐以及必要的系统安全校验。我们承诺绝不将您的个人信息出售给第三方。
				</text>
			</view>

			<view class="section">
				<view class="sub-title">3. 信息的存储与安全</view>
				<text class="text">
					我们采用行业标准的加密技术存储您的数据，并实施严格的权限访问控制，以防止您的信息泄露、滥用或遭到未经授权的访问。
				</text>
			</view>

			<view class="section">
				<view class="sub-title">4. 您的权利</view>
				<text class="text">
					您可以随时通过“个人资料”修改您的昵称、头像等信息，或者通过“账号与安全”注销您的账号。
				</text>
			</view>

			<view class="section">
				<view class="sub-title">5. 联系我们</view>
				<text class="text">
					如果您对本隐私政策有任何疑问，请通过官方客服或应用内反馈与我们联系。
				</text>
			</view>
		</scroll-view>
	</view>
</template>

<style lang="scss" scoped>
	.privacy-container {
		background-color: #fff;
		min-height: 100vh;
		padding: 40rpx;
	}
    /* 顶部状态栏占位 */
	.status-placeholder {
		height: var(--status-bar-height);
		width: 100%;
	}

	.content {
		height: 100%;
	}

	.title {
		font-size: 40rpx;
		font-weight: bold;
		text-align: center;
		margin-bottom: 20rpx;
		color: #333;
	}

	.update-time {
		font-size: 24rpx;
		color: #999;
		text-align: center;
		margin-bottom: 60rpx;
	}

	.section {
		margin-bottom: 40rpx;
		.sub-title {
			font-size: 32rpx;
			font-weight: bold;
			color: #333;
			margin-bottom: 16rpx;
		}
		.text {
			font-size: 28rpx;
			color: #666;
			line-height: 1.6;
			display: block;
		}
	}
</style>

