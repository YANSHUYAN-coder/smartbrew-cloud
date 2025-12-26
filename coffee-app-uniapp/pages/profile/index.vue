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
							<text class="user-name">{{userInfo.nickname || '未登录'}}</text>
							<view class="level-badge" v-if="profileStats && profileStats.levelName">{{ profileStats.levelName }}</view>
							<view class="level-badge" v-else-if="userStore.isLogin">普通会员</view>
						</view>
						<text class="user-desc" v-if="profileStats && profileStats.needGrowth > 0">再消费 {{ profileStats.needGrowth }} 成长值升级为{{ profileStats.nextLevelName }}</text>
						<text class="user-desc" v-else-if="profileStats && profileStats.levelName && !profileStats.nextLevelName">已是最高等级</text>
						<text class="user-desc" v-else-if="userStore.isLogin">完善信息，享受更多权益</text>
					</view>
				</view>

				<!-- 数据概览 -->
				<view class="stats-row">
					<view v-for="(stat, index) in stats" :key="index" class="stat-item" @click="navTo(stat.path)">
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
					<view class="order-icon-wrapper">
						<!-- <text class="order-icon">{{ order.icon }}</text> -->
						<uni-icons custom-prefix="iconfont" :type="order.icon" size="24" color="#666"></uni-icons>
						<!-- 小红点显示数量（仅待付款、制作中、待取餐显示，已完成不显示） -->
						<view v-if="order.key !== 'completed' && getOrderCount(order.key) > 0" class="order-badge">
							<text class="badge-text">{{ getOrderCount(order.key) > 99 ? '99+' : getOrderCount(order.key) }}</text>
						</view>
					</view>
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
	import { getGiftCardTotalBalance } from '@/services/giftcard.js'
	import { getProfileStatistics } from '@/services/user.js'

	const statusBarHeight = ref(0)
	const userStore=new useUserStore()
	const userInfo =ref({})
	const profileStats = ref({
		integration: 0,
		couponCount: 0,
		levelName: '',
		nextLevelName: '',
		needGrowth: 0,
		orderCounts: {
			pendingPayment: 0,
			making: 0,
			pendingPickup: 0,
			completed: 0
		}
	})
	
	// 计算属性：转换后的头像 URL
	const avatarUrl = computed(() => {
		return convertImageUrl(userInfo.value?.avatar) || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=200&auto=format&fit=crop'
	})

	const stats = ref([{
			label: '积分',
			val: '0',
            path: '/pages/integration/history' // 积分跳积分明细
		},
		{
			label: '优惠券',
			val: '0',
            path: '/pages/coupon/list' // 优惠券跳我的优惠券
		},
		{
			label: '余额',
			val: '0.00',
            path: '/pages/giftcard/index' // 余额跳礼品卡
		}
	])

	const orderTypes = [{
			icon: 'icon-daizhifu',
			label: '待付款',
			key: 'pendingPayment' // 对应 profileStats.orderCounts.pendingPayment
		},
		{
			icon: 'icon-drink',
			label: '制作中',
			key: 'making' // 对应 profileStats.orderCounts.making
		},
		{
			icon: 'icon-daiqucan',
			label: '待取餐',
			key: 'pendingPickup' // 对应 profileStats.orderCounts.pendingPickup
		},
		{
			icon: 'icon-yiwancheng',
			label: '已完成',
			key: 'completed' // 对应 profileStats.orderCounts.completed（不显示小红点）
		},
	]

	// 获取订单数量
	const getOrderCount = (key) => {
		if (!profileStats.value || !profileStats.value.orderCounts) {
			return 0
		}
		return profileStats.value.orderCounts[key] || 0
	}

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
			'制作中': 2,  // 制作中对应状态 2
			'待取餐': 3,
			'已完成': 4
		}
		const status = statusMap[order.label]
		if (status !== undefined) {
			uni.navigateTo({
				url: `/pages/order/list?status=${status}`
			})
		} else {
			// 如果没有匹配的状态，跳转到全部订单
			uni.navigateTo({
				url: '/pages/order/list'
			})
		}
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
		userInfo.value = userStore.userInfo
		console.log("userInfo", userInfo.value);
		
		// 如果已登录，加载所有统计数据
		if (userStore.isLogin) {
			try {
				// 1. 加载统计信息（积分、优惠券、订单统计、会员等级）
				const statsRes = await getProfileStatistics()
				console.log('统计信息响应:', statsRes)
				
				// 处理响应数据（request.js 已经解析了 data.data，这里直接使用 statsRes）
				const statsData = statsRes || {}
				
				if (statsData) {
					// 更新 profileStats
					profileStats.value = {
						integration: statsData.integration || 0,
						couponCount: statsData.couponCount || 0,
						levelName: statsData.levelName || '普通会员',
						nextLevelName: statsData.nextLevelName || '',
						needGrowth: statsData.needGrowth || 0,
						orderCounts: statsData.orderCounts || {
							pendingPayment: 0,
							making: 0,
							pendingPickup: 0,
							completed: 0
						}
					}
					
					console.log('更新后的 profileStats:', profileStats.value)
					
					// 更新积分显示
					const integrationIndex = stats.value.findIndex(s => s.label === '积分')
					if (integrationIndex !== -1) {
						stats.value[integrationIndex].val = (statsData.integration || 0).toLocaleString()
					}
					
					// 更新优惠券显示
					const couponIndex = stats.value.findIndex(s => s.label === '优惠券')
					if (couponIndex !== -1) {
						stats.value[couponIndex].val = (statsData.couponCount || 0).toString()
					}
				}
				
				// 2. 加载咖啡卡总余额
				try {
					const balanceRes = await getGiftCardTotalBalance()
					const balance = balanceRes?.data || balanceRes || 0
					// 更新余额显示
					const balanceIndex = stats.value.findIndex(s => s.label === '余额')
					if (balanceIndex !== -1) {
						stats.value[balanceIndex].val = parseFloat(balance).toFixed(2)
					}
				} catch (error) {
					console.error('加载咖啡卡余额失败', error)
					// 失败时保持默认值 0.00
				}
			} catch (error) {
				console.error('加载统计信息失败', error)
				// 失败时保持默认值
			}
		}
		
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

	.order-icon-wrapper {
		position: relative;
		display: inline-block;
	}

	.order-icon {
		font-size: 44rpx;
	}

	.order-label {
		font-size: 20rpx;
		color: #666;
	}

	/* 订单数量小红点（圆形） */
	.order-badge {
		position: absolute;
		top: -8rpx;
		right: -16rpx;
		background-color: #ff4757;
		border-radius: 50%;
		width: 32rpx;
		height: 32rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		border: 2rpx solid #ffffff;
		box-shadow: 0 2rpx 8rpx rgba(255, 71, 87, 0.3);
		/* 固定宽高，确保是完美的圆形 */
	}

	.badge-text {
		color: #ffffff;
		font-size: 20rpx;
		font-weight: bold;
		line-height: 1;
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