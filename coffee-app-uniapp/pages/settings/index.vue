<template>
	<view class="container">
		<!-- 顶部占位，如果不需要导航栏背景可去掉 -->
		<view class="status-placeholder"></view>

		<!-- 分组 1: 账号相关 -->
		<view class="section-group">
			<view class="cell-item" hover-class="cell-hover" @click="navTo('/pages/profile/edit')">
				<view class="cell-left">
					<!-- 如果没有 uni-icons，可以用 text class="iconfont icon-user" 代替 -->
					<uni-icons type="person-filled" size="22" color="#d4a373" class="cell-icon"></uni-icons>
					<text class="cell-title">个人资料</text>
				</view>
				<uni-icons type="right" size="14" color="#ccc"></uni-icons>
			</view>

			<view class="cell-item" hover-class="cell-hover" @click="navTo('/pages/address/list')">
				<view class="cell-left">
					<uni-icons type="location-filled" size="22" color="#d4a373" class="cell-icon"></uni-icons>
					<text class="cell-title">地址管理</text>
				</view>
				<uni-icons type="right" size="14" color="#ccc"></uni-icons>
			</view>

			<view class="cell-item" hover-class="cell-hover" @click="handleSecurity">
				<view class="cell-left">
					<uni-icons type="locked-filled" size="22" color="#d4a373" class="cell-icon"></uni-icons>
					<text class="cell-title">账号与安全</text>
				</view>
				<uni-icons type="right" size="14" color="#ccc"></uni-icons>
			</view>
		</view>

		<!-- 分组 2: 通用设置 -->
		<view class="section-group">
			<view class="cell-item" hover-class="cell-hover" @click="handleClearCache">
				<view class="cell-left">
					<uni-icons type="trash-filled" size="22" color="#8a8a8a" class="cell-icon"></uni-icons>
					<text class="cell-title">清除缓存</text>
				</view>
				<view class="cell-right">
					<text class="cell-tip">{{ cacheSize }}</text>
					<uni-icons type="right" size="14" color="#ccc"></uni-icons>
				</view>
			</view>

			<view class="cell-item" hover-class="cell-hover">
				<view class="cell-left">
					<uni-icons type="eye-filled" size="22" color="#8a8a8a" class="cell-icon"></uni-icons>
					<text class="cell-title">隐私政策</text>
				</view>
				<uni-icons type="right" size="14" color="#ccc"></uni-icons>
			</view>

			<view class="cell-item" hover-class="cell-hover" @click="navTo('/pages/about/index')">
				<view class="cell-left">
					<uni-icons type="info-filled" size="22" color="#8a8a8a" class="cell-icon"></uni-icons>
					<text class="cell-title">关于 SmartBrew</text>
				</view>
				<view class="cell-right">
					<text class="cell-tip">v1.0.0</text>
					<uni-icons type="right" size="14" color="#ccc"></uni-icons>
				</view>
			</view>
		</view>

		<!-- 底部操作按钮 -->
		<view class="footer-action">
			<button v-if="hasLogin" class="btn-logout" hover-class="btn-hover" @click="handleLogout">
				退出登录
			</button>
			<button v-else class="btn-login" hover-class="btn-hover" @click="navTo('/pages/login/index')">
				立即登录
			</button>

			<text class="copyright">SmartBrew Coffee App © 2024</text>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		onMounted,
		computed
	} from 'vue'
	import {
		onShow,
		onPullDownRefresh
	} from '@dcloudio/uni-app'
	import { useUserStore } from '@/store/user.js'

	const cacheSize = ref('0KB')
	const userStore = useUserStore()

	// 是否已登录：由 userStore 统一管理
	const hasLogin = computed(() => userStore.isLogin)

	// 页面显示时仅处理与缓存相关逻辑，登录态依赖 userStore
	onShow(() => {
		// 模拟计算缓存大小
		calculateCache()
	})

	// 通用跳转函数
	const navTo = (url) => {
		if (!hasLogin.value && url.includes('profile')) {
			uni.navigateTo({
				url: '/pages/login/index'
			})
			return
		}
		uni.navigateTo({
			url
		})
	}

	// 模拟安全设置
	const handleSecurity = () => {
		if (!hasLogin.value) return navTo('/pages/login/index')
		uni.showToast({
			title: '修改密码功能开发中',
			icon: 'none'
		})
	}

	// 计算缓存（模拟）
	const calculateCache = () => {
		// 实际开发中可用 uni.getStorageInfoSync()
		const size = Math.floor(Math.random() * 5000) / 100
		cacheSize.value = size > 0 ? `${size}MB` : '0KB'
	}

	// 清除缓存
	const handleClearCache = () => {
		uni.showModal({
			title: '提示',
			content: '确定要清除本地缓存吗？',
			success: (res) => {
				if (res.confirm) {
					uni.showLoading({
						title: '清理中...'
					})
					setTimeout(() => {
						// 这里不要清除 token 和 userInfo，只清除临时文件
						// uni.clearStorageSync() // 慎用，会全清空

						cacheSize.value = '0KB'
						uni.hideLoading()
						uni.showToast({
							title: '清理完成'
						})
					}, 800)
				}
			}
		})
	}

	// 退出登录
	const handleLogout = () => {
		uni.showModal({
			title: '提示',
			content: '确定要退出当前账号吗？',
			confirmColor: '#ff4d4f',
			success: (res) => {
				if (res.confirm) {
					// 1. 通过 userStore 清除关键用户信息（状态 + 本地存储）
					userStore.clearUser()

					uni.showToast({
						title: '已退出',
						icon: 'success'
					})

					// 2. 延迟跳转回首页
					setTimeout(() => {
						uni.reLaunch({
							url: '/pages/index/index'
						})
					}, 1000)
				}
			}
		})
	}

	// 加载设置页面数据
	const loadSettingsData = async () => {
		// 重新计算缓存大小
		calculateCache()
		// 登录状态由 userStore 管理，这里不再重复从本地读取
		return true
	}

	// 下拉刷新
	onPullDownRefresh(async () => {
		await loadSettingsData()
		// 停止下拉刷新动画
		uni.stopPullDownRefresh()
	})
</script>

<style lang="scss" scoped>
	/* 页面容器：浅灰背景 */
	.container {
		min-height: 100vh;
		background-color: #f5f7fa;
		padding: 20rpx 30rpx;
	}

	/* 顶部状态栏占位 */
	.status-placeholder {
		height: var(--status-bar-height);
		width: 100%;
	}

	/* 分组卡片样式 */
	.section-group {
		background-color: #ffffff;
		border-radius: 20rpx;
		margin-bottom: 30rpx;
		overflow: hidden;
		/* 保证圆角 */
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.02);
	}

	/* 单行列表项 */
	.cell-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 34rpx 30rpx;
		border-bottom: 1rpx solid #f9f9f9;
		transition: background-color 0.2s;
		background-color: #fff;
	}

	/* 最后一个元素去掉底边框 */
	.cell-item:last-child {
		border-bottom: none;
	}

	/* 点击态 */
	.cell-hover {
		background-color: #f9f9f9;
	}

	/* 左侧区域 */
	.cell-left {
		display: flex;
		align-items: center;
	}

	/* 图标 */
	.cell-icon {
		margin-right: 20rpx;
		/* 确保图标垂直居中 */
		display: flex;
		align-items: center;
	}

	/* 标题文字 */
	.cell-title {
		font-size: 30rpx;
		color: #333;
		font-weight: 500;
	}

	/* 右侧区域 */
	.cell-right {
		display: flex;
		align-items: center;
	}

	/* 提示文字（如缓存大小、版本号） */
	.cell-tip {
		font-size: 26rpx;
		color: #999;
		margin-right: 10rpx;
	}

	/* 底部按钮区域 */
	.footer-action {
		margin-top: 60rpx;
		padding: 0 20rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	/* 退出登录按钮 */
	.btn-logout {
		width: 100%;
		background-color: #ffffff;
		color: #ff4d4f;
		/* 警示红 */
		font-size: 32rpx;
		font-weight: 600;
		border-radius: 50rpx;
		padding: 10rpx 0;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
		border: none;
	}

	/* 登录按钮 */
	.btn-login {
		width: 100%;
		background-color: #d4a373;
		/* 咖啡主题色 */
		color: #ffffff;
		font-size: 32rpx;
		font-weight: 600;
		border-radius: 50rpx;
		padding: 10rpx 0;
		box-shadow: 0 4rpx 12rpx rgba(212, 163, 115, 0.3);
		border: none;
	}

	.btn-hover {
		opacity: 0.9;
		transform: scale(0.99);
	}

	/* 底部版权信息 */
	.copyright {
		margin-top: 40rpx;
		font-size: 24rpx;
		color: #ccc;
	}
</style>