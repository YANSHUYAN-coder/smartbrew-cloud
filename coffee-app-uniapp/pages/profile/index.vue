<template>
	<view class="profile-page">
		<!-- 头部卡片（包含状态栏占位） -->
		<view class="profile-header" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="header-bg">
				<view class="bg-blur"></view>
			</view>
			<view class="header-content">
				<view class="user-info">
					<image @click="navTo('/pages/profile/edit')"
						:src="avatarUrl"
						class="avatar" mode="aspectFill" />
					<view class="user-details">
						<view class="user-name-row">
							<text class="user-name">{{userInfo.nickname}}</text>
							<view class="level-badge">LV.3</view>
						</view>
						<text class="user-desc">再消费 2 杯升级为黑金会员</text>
					</view>
				</view>

				<!-- 数据概览 -->
				<view class="stats-row">
					<view v-for="(stat, index) in stats" :key="index" class="stat-item">
						<text class="stat-value">{{ stat.val }}</text>
						<text class="stat-label">{{ stat.label }}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 订单卡片 -->
		<view class="order-card">
			<view class="card-header" @click="goToOrderList">
				<text class="card-title">我的订单</text>
				<text class="card-more">全部 ›</text>
			</view>
			<view class="order-types">
				<view v-for="(order, index) in orderTypes" :key="index" class="order-type-item"
					@click="handleOrderTypeClick(order)">
					<text class="order-icon">{{ order.icon }}</text>
					<text class="order-label">{{ order.label }}</text>
				</view>
			</view>
		</view>

		<!-- 功能列表 -->
		<view class="function-list">
			<view v-for="(item, index) in functions" :key="index" class="function-item"
				@click="handleFunctionClick(item)">
				<text class="function-name">{{ item.name }}</text>
				<view class="function-right">
					<text class="function-desc" v-if="item.desc">{{ item.desc }}</text>
					<text class="chevron">›</text>
				</view>
			</view>
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
		onLoad,
		onShow,
		onPullDownRefresh
	} from '@dcloudio/uni-app'
	import {
		getStatusBarHeight
	} from '@/utils/system.js'
	import {useUserStore} from '@/store/user.js'
	import { convertImageUrl } from '@/utils/image.js'

	const statusBarHeight = ref(0)
	const userStore=new useUserStore()
	const userInfo =ref({})
	
	// 计算属性：转换后的头像 URL
	const avatarUrl = computed(() => {
		return convertImageUrl(userInfo.value?.avatar) || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200&auto=format&fit=crop'
	})

	const stats = [{
			label: '积分',
			val: '1,204'
		},
		{
			label: '优惠券',
			val: '3'
		},
		{
			label: '余额',
			val: '0.00'
		}
	]

	const orderTypes = [{
			icon: '💳',
			label: '待付款'
		},
		{
			icon: '⏰',
			label: '制作中'
		},
		{
			icon: '🚚',
			label: '待取餐'
		},
		{
			icon: '✓',
			label: '已完成'
		},
	]

	const functions = [{
			name: '收货地址',
			desc: ''
		},
		{
			name: '联系客服',
			desc: '9:00 - 21:00'
		},
		{
			name: '关于我们',
			desc: 'v1.0.0'
		},
		{
			name: '设置',
			desc: ''
		},
	]

	const handleOrderTypeClick = (order) => {
		// 根据订单类型跳转到订单列表页，并筛选对应状态
		const statusMap = {
			'待付款': 0,
			'制作中': 2,
			'待取餐': 3,
			'已完成': 4
		}
		const status = statusMap[order.label]
		uni.navigateTo({
			url: `/pages/order/list?status=${status !== undefined ? status : ''}`
		})
	}

	const goToOrderList = () => {
		uni.navigateTo({
			url: '/pages/order/list'
		})
	}

	const handleFunctionClick = (item) => {
		switch (item.name) {
			case '收货地址':
				uni.navigateTo({
					url:"/pages/address/list"
				})
				break;
		
			case '联系客服':
				uni.showActionSheet({
				    itemList: ['拨打客服电话', '在线客服'],
				    success: (res) => {
				      if (res.tapIndex === 0) {
				        uni.makePhoneCall({ phoneNumber: '400-888-8888' })
				      } else {
				        uni.showToast({ title: '连接中...', icon: 'loading' })
				      }
				    }
				  })
				break;
		
			case '关于我们':
				uni.showModal({
				    title: '关于智咖·云',
				    content: '智咖·云是一个 AI 驱动的智能咖啡零售平台。\n当前版本: v1.0.0',
				    showCancel: false
				  })
				break;
		
			case '设置':
				uni.navigateTo({
					url:"/pages/settings/index"
				})
				break;
		}
	}

	// 加载个人中心数据
	const loadProfileData = async () => {
		// 这里可以添加加载用户信息、订单统计等数据的逻辑
		// 例如：获取用户信息、积分、优惠券数量等
		// 目前使用模拟数据，所以暂时不需要
		userInfo.value=userStore.userInfo
		console.log("userInfo",userInfo.value);
		return true
	}
	
	// 是否已登录：由 userStore 统一管理
	const hasLogin = computed(() => userStore.isLogin)
	
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

	// 下拉刷新
	onPullDownRefresh(async () => {
		await loadProfileData()
		// 停止下拉刷新动画
		uni.stopPullDownRefresh()
	})

	onMounted(() => {
		statusBarHeight.value = getStatusBarHeight()
		loadProfileData()
	})

	onLoad(() => {
		console.log('我的页面加载')
	})
	
	onShow(() => {
	  uni.setNavigationBarColor({
	    frontColor: '#ffffff', // 只能是 #ffffff 或 #000000
	    backgroundColor: 'transparent' // 在 custom 模式下，背景色通常设为透明，或者跟页面背景一致
	  })
	})
</script>

<style lang="scss" scoped>
	.profile-page {
		min-height: 100vh;
		background-color: #f5f5f5;
		padding-bottom: calc(100rpx + env(safe-area-inset-bottom));
	}

	.profile-header {
		background-color: #333;
		color: white;
		padding: 40rpx 48rpx 160rpx;
		border-radius: 0 0 80rpx 80rpx;
		position: relative;
		overflow: hidden;
	}

	.header-bg {
		position: absolute;
		top: 0;
		right: 0;
		width: 256rpx;
		height: 256rpx;
	}

	.bg-blur {
		width: 100%;
		height: 100%;
		background-color: rgba(255, 255, 255, 0.1);
		border-radius: 50%;
		filter: blur(60rpx);
		transform: translate(50%, -50%);
	}

	.header-content {
		position: relative;
		z-index: 10;
	}

	.user-info {
		display: flex;
		align-items: center;
	}

	.avatar {
		width: 128rpx;
		height: 128rpx;
		border-radius: 50%;
		border: 4rpx solid rgba(255, 255, 255, 0.3);
	}

	.user-details {
		margin-left: 32rpx;
	}

	.user-name-row {
		display: flex;
		align-items: center;
		gap: 16rpx;
	}

	.user-name {
		font-size: 40rpx;
		font-weight: bold;
	}

	.level-badge {
		font-size: 20rpx;
		background-color: #d4af37;
		color: #000;
		padding: 4rpx 12rpx;
		border-radius: 4rpx;
		font-weight: bold;
	}

	.user-desc {
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.6);
		margin-top: 8rpx;
		display: block;
	}

	.stats-row {
		display: flex;
		justify-content: space-between;
		margin-top: 64rpx;
		padding: 0 32rpx;
	}

	.stat-item {
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.stat-value {
		font-size: 36rpx;
		font-weight: bold;
		font-family: monospace;
	}

	.stat-label {
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.6);
		margin-top: 8rpx;
	}

	.order-card {
		margin: -96rpx 32rpx 0;
		background-color: white;
		border-radius: 32rpx;
		padding: 32rpx;
		box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
		position: relative;
		z-index: 20;
	}

	.card-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 32rpx;
	}

	.card-title {
		font-size: 28rpx;
		font-weight: bold;
		color: #333;
	}

	.card-more {
		font-size: 24rpx;
		color: #999;
	}

	.order-types {
		display: flex;
		justify-content: space-between;
	}

	.order-type-item {
		display: inline-flex;
		flex-direction: column;
		align-items: center;
		gap: 16rpx;
		min-width: 120rpx;
		transition: transform 0.2s;
		flex-shrink: 0;
	}

	.order-type-item:active {
		transform: scale(0.95);
	}

	.order-icon {
		font-size: 44rpx;
	}

	.order-label {
		font-size: 20rpx;
		color: #666;
	}

	.function-list {
		margin: 32rpx;
		background-color: white;
		border-radius: 32rpx;
		overflow: hidden;
		box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
	}

	.function-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 32rpx;
		border-bottom: 1rpx solid #f0f0f0;
		transition: background-color 0.2s;
	}

	.function-item:active {
		background-color: #f9f9f9;
	}

	.function-item:last-child {
		border-bottom: none;
	}

	.function-name {
		font-size: 28rpx;
		font-weight: 500;
		color: #333;
	}

	.function-right {
		display: flex;
		align-items: center;
		gap: 16rpx;
	}

	.function-desc {
		font-size: 24rpx;
		color: #999;
	}

	.chevron {
		font-size: 28rpx;
		color: #ccc;
	}
</style>