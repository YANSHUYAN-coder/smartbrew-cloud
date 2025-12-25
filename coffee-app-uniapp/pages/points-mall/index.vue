<template>
	<view class="points-mall-page">
		<!-- 顶部积分卡片 -->
		<view class="header-section" :style="{ paddingTop: statusBarHeight + 44 + 'px' }">
			<view class="points-card">
				<view class="points-info">
					<view class="points-label">当前积分</view>
					<view class="points-value">{{ userStore.userInfo?.integration || 0 }}</view>
				</view>
				<view class="points-action" @click="handleSignIn">
					<text>每日签到</text>
				</view>
			</view>
		</view>

		<!-- 优惠券列表 -->
		<view class="coupon-section">
			<view class="section-title">积分兑换好券</view>
			<view class="coupon-list">
				<view v-for="coupon in couponList" :key="coupon.id" class="coupon-item">
					<view class="coupon-left">
						<view class="coupon-amount">
							<text class="symbol">¥</text>
							<text class="value">{{ coupon.amount }}</text>
						</view>
						<view class="coupon-info">
							<text class="coupon-name">{{ coupon.name }}</text>
							<text class="coupon-desc">{{ coupon.desc }}</text>
						</view>
					</view>
					<view class="coupon-right">
						<view class="points-cost">
							<text class="cost-value">{{ coupon.points }}</text>
							<text class="cost-label">积分</text>
						</view>
						<button class="exchange-btn" 
							:class="{ 
								disabled: isButtonDisabled(coupon)
							}"
							:disabled="isButtonDisabled(coupon)"
							@click.stop="handleExchange(coupon)">
							{{ getButtonText(coupon) }}
						</button>
					</view>
				</view>
				
				<!-- 加载状态 -->
				<view v-if="loading" class="loading-more">
					<text>加载中...</text>
				</view>
				<view v-if="!loading && couponList.length > 0 && !hasMore" class="no-more">
					<text>没有更多了</text>
				</view>
				<!-- 空状态 -->
				<view v-if="!loading && couponList.length === 0" class="empty-state">
					<text>暂无可兑换优惠券</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onPullDownRefresh, onReachBottom ,onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user.js'
import { getStatusBarHeight } from '@/utils/system.js'
import { getRedeemableCoupons, redeemCoupon } from '@/services/promotion.js'

const userStore = useUserStore()
const statusBarHeight = ref(0)
const couponList = ref([])

// 分页相关
const page = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)
const loading = ref(false)

onMounted(() => {
	statusBarHeight.value = getStatusBarHeight()
	loadCoupons(true)
	userStore.fetchUserInfo() // 进页面刷新积分
})

// 下拉刷新
onPullDownRefresh(async () => {
	await Promise.all([
		loadCoupons(true),
		userStore.fetchUserInfo()
	])
	uni.stopPullDownRefresh()
})

// 触底加载
onReachBottom(() => {
	if (hasMore.value && !loading.value) {
		loadCoupons(false)
	}
})

const loadCoupons = async (refresh = false) => {
	if (loading.value) return
	loading.value = true
	
	try {
		if (refresh) {
			page.value = 1
			hasMore.value = true
		} else {
			page.value++
		}
		
		const res = await getRedeemableCoupons(page.value, pageSize.value)
		
		// 兼容后端返回 Page 结构 (records)
		const resList = res.records || res.data || res || []
		
		// 调试日志
		console.log('优惠券列表原始数据:', res)
		console.log('优惠券列表 resList:', resList)
		if (resList.length > 0) {
			console.log('第一条优惠券数据:', resList[0])
			console.log('isRedeemed 值:', resList[0].isRedeemed, typeof resList[0].isRedeemed)
		}
		
		const list = resList.map(item => {
			const coupon = {
				id: item.id,
				amount: item.amount,
				name: item.name,
				desc: item.note || '暂无说明',
				points: item.points,
				// 处理布尔值，确保正确解析
				isRedeemed: item.isRedeemed === true || item.isRedeemed === 'true' || item.isRedeemed === 1,
				redeemedCount: item.redeemedCount || item.redeemed_count || 0,
				perLimit: item.perLimit || item.per_limit || 0
			}
			console.log(`优惠券 ${coupon.id} 处理后的 isRedeemed:`, coupon.isRedeemed)
			return coupon
		})
		
		if (refresh) {
			couponList.value = list
		} else {
			couponList.value = [...couponList.value, ...list]
		}
		
		// 判断是否还有更多数据
		hasMore.value = couponList.value.length < (res.total || 0)
		
	} catch (e) {
		console.error('获取优惠券列表失败', e)
		if (!refresh && page.value > 1) {
			page.value-- // 加载失败回退页码
		}
	} finally {
		loading.value = false
	}
}

// 判断按钮是否禁用
const isButtonDisabled = (coupon) => {
	const currentPoints = userStore.userInfo?.integration || 0
	const pointsInsufficient = currentPoints < coupon.points
	const isRedeemed = coupon.isRedeemed === true || coupon.isRedeemed === 'true' || coupon.isRedeemed === 1
	return pointsInsufficient || isRedeemed
}

// 获取按钮文字
const getButtonText = (coupon) => {
	const isRedeemed = coupon.isRedeemed === true || coupon.isRedeemed === 'true' || coupon.isRedeemed === 1
	if (isRedeemed) {
		return coupon.perLimit > 0 ? `已领${coupon.perLimit}张` : '已领取'
	}
	const currentPoints = userStore.userInfo?.integration || 0
	if (currentPoints < coupon.points) {
		return '积分不足'
	}
	return '兑换'
}

const handleSignIn = () => {
	// 签到功能暂未实现后端接口，仅提示
	uni.showToast({
		title: '签到功能开发中',
		icon: 'none'
	})
}

const handleExchange = (coupon) => {
	// 防止重复点击
	if (isButtonDisabled(coupon)) {
		const isRedeemed = coupon.isRedeemed === true || coupon.isRedeemed === 'true' || coupon.isRedeemed === 1
		if (isRedeemed) {
			uni.showToast({
				title: coupon.perLimit > 0 ? `每人限领${coupon.perLimit}张` : '已领取',
				icon: 'none'
			})
		} else {
			uni.showToast({
				title: '积分不足',
				icon: 'none'
			})
		}
		return
	}
	
	const currentPoints = userStore.userInfo?.integration || 0
	if (currentPoints < coupon.points) {
		uni.showToast({
			title: '积分不足',
			icon: 'none'
		})
		return
	}

	uni.showModal({
		title: '确认兑换',
		content: `确定消耗 ${coupon.points} 积分兑换 ${coupon.name} 吗？`,
		success: async (res) => {
			if (res.confirm) {
				uni.showLoading({ title: '兑换中' })
				try {
					await redeemCoupon(coupon.id)
					uni.showToast({
						title: '兑换成功',
						icon: 'success'
					})
					// 刷新积分
					await userStore.fetchUserInfo()
					// 刷新列表（更新库存等）
					// 这里可以选择重新加载列表，或者直接在本地更新库存显示（如果显示库存的话）
					// 为简单起见，这里触发一次静默刷新
					loadCoupons(true)
				} catch (e) {
					console.error(e)
				} finally {
					uni.hideLoading()
				}
			}
		}
	})
}

onShow(() => {
	  uni.setNavigationBarColor({
	    frontColor: '#ffffff', // 只能是 #ffffff 或 #000000
	    backgroundColor: 'transparent' // 在 custom 模式下，背景色通常设为透明，或者跟页面背景一致
	  })
	})
</script>

<style lang="scss" scoped>
.points-mall-page {
	min-height: 100vh;
	background-color: #f5f5f5;
	padding-bottom: 40rpx;
}

.header-section {
	background: linear-gradient(135deg, #333333 0%, #1a1a1a 100%);
	padding-bottom: 60rpx;
	padding-left: 40rpx;
	padding-right: 40rpx;
}

.points-card {
	background-color: rgba(255, 255, 255, 0.1);
	backdrop-filter: blur(10px);
	border-radius: 24rpx;
	padding: 40rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	color: white;
}

.points-label {
	font-size: 28rpx;
	opacity: 0.9;
	margin-bottom: 12rpx;
}

.points-value {
	font-size: 64rpx;
	font-weight: bold;
}

.points-action {
	background-color: white;
	color: #6f4e37;
	padding: 12rpx 24rpx;
	border-radius: 40rpx;
	font-size: 26rpx;
	font-weight: bold;
}

.coupon-section {
	padding: 0 32rpx;
	margin-top: -30rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 24rpx;
	display: none;
}

.coupon-list {
	display: flex;
	flex-direction: column;
	gap: 24rpx;
}

.coupon-item {
	background-color: white;
	border-radius: 20rpx;
	display: flex;
	overflow: hidden;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.coupon-left {
	flex: 1;
	padding: 32rpx;
	display: flex;
	align-items: center;
	border-right: 2rpx dashed #eee;
	position: relative;
	
	&::after {
		content: '';
		position: absolute;
		right: -12rpx;
		top: -12rpx;
		width: 24rpx;
		height: 24rpx;
		border-radius: 50%;
		background-color: #f5f5f5;
	}
	
	&::before {
		content: '';
		position: absolute;
		right: -12rpx;
		bottom: -12rpx;
		width: 24rpx;
		height: 24rpx;
		border-radius: 50%;
		background-color: #f5f5f5;
	}
}

.coupon-amount {
	color: #e64340;
	margin-right: 24rpx;
	display: flex;
	align-items: baseline;
	
	.symbol {
		font-size: 24rpx;
		font-weight: bold;
	}
	
	.value {
		font-size: 56rpx;
		font-weight: bold;
		line-height: 1;
	}
}

.coupon-info {
	display: flex;
	flex-direction: column;
	gap: 8rpx;
	
	.coupon-name {
		font-size: 30rpx;
		font-weight: bold;
		color: #333;
	}
	
	.coupon-desc {
		font-size: 24rpx;
		color: #999;
	}
}

.coupon-right {
	width: 200rpx;
	padding: 32rpx 0;
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	gap: 16rpx;
}

.points-cost {
	display: flex;
	align-items: baseline;
	gap: 4rpx;
	color: #6f4e37;
	
	.cost-value {
		font-size: 36rpx;
		font-weight: bold;
	}
	
	.cost-label {
		font-size: 24rpx;
	}
}

.exchange-btn {
	margin: 0;
	padding: 0;
	width: 140rpx;
	height: 56rpx;
	line-height: 56rpx;
	font-size: 26rpx;
	background-color: #6f4e37;
	color: white;
	border-radius: 28rpx;
	
	&.disabled {
		background-color: #ccc;
		color: #fff;
	}
	
	&::after {
		border: none;
	}
}

.empty-state {
	padding: 60rpx 0;
	text-align: center;
	color: #999;
	font-size: 28rpx;
}

.loading-more, .no-more {
	text-align: center;
	padding: 24rpx 0;
	color: #999;
	font-size: 24rpx;
}
</style>
