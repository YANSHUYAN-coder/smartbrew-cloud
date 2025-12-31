<template>
	<view class="sku-modal-mask" v-if="show" @click="closeModal" :class="themeClass">
		<view class="sku-modal" @click.stop>
			<!-- 头部 -->
			<view class="modal-header">
				<image :src="product.image" class="modal-img" mode="aspectFill" />
				<view class="modal-info">
					<text class="modal-name">{{ product.name }}</text>
					<text class="modal-desc">{{ product.desc }}</text>
				</view>
				<view class="close-btn" @click="closeModal">×</view>
			</view>

			<!-- 规格选项 -->
			<scroll-view scroll-y class="modal-scroll">
				<view v-for="group in specGroups" :key="group.key" class="spec-group">
					<text class="spec-title">{{ group.key }}</text>
					<view class="spec-options">
						<view 
							v-for="value in group.values" 
							:key="value"
							class="spec-opt" 
							:class="{ active: isSpecSelected(group.key, value) }"
							@click="selectSpec(group.key, value)">
							{{ value }}
						</view>
					</view>
				</view>
				<!-- 如果没有规格，显示提示 -->
				<view v-if="specGroups.length === 0" class="no-spec-tip">
					<text>该商品暂无规格选项</text>
				</view>
			</scroll-view>

			<!-- 底部操作 -->
			<view class="modal-footer">
				<view class="price-box">
					<view class="price-num-row">
						<text class="symbol">¥</text>
						<text class="num">{{ selectedSku ? selectedSku.price : product.price }}</text>
					</view>
					<text class="selected-spec">已选: {{ getSelectedSpecText() }}</text>
				</view>
				<view class="action-box">
					<!-- 简单的加减器 -->
					<view class="stepper" v-if="currentTempCount > 0">
						<view class="step-btn minus" @click="updateTempCount(-1, $event)">-</view>
						<text class="step-num">{{ currentTempCount }}</text>
						<view class="step-btn plus" @click="updateTempCount(1, $event)">+</view>
					</view>
					<view class="add-cart-btn" v-else @click="updateTempCount(1, $event)">
						加入购物车
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref, watch, computed } from 'vue'
	import { getProductDetail } from '@/services/product.js'
	import { useCartStore } from '@/store/cart.js'
	import { useUserStore } from '@/store/user.js'
	import { useAppStore } from '@/store/app.js'

	const props = defineProps({
		show: Boolean,
		product: Object
	})

	const emit = defineEmits(['update:show', 'close', 'add-to-cart-anim'])

	const cartStore = useCartStore()
	const userStore = useUserStore()
	const appStore = useAppStore()
	const skuList = ref([])
	const specGroups = ref([])
	const selectedSpecs = ref({})
	const selectedSku = ref(null)
	const currentTempCount = ref(0)

	const themeClass = computed(() => userStore.isDarkMode ? 'theme-dark' : 'theme-light')

	watch(() => props.show, (newVal) => {
		console.log('SkuModal show status:', newVal, 'Product ID:', props.product?.id)
		if (newVal && props.product.id) {
			initData()
		}
	})

	const initData = async () => {
		selectedSpecs.value = {}
		selectedSku.value = null
		currentTempCount.value = 0
		
		try {
			const detail = await getProductDetail(props.product.id)
			if (detail && detail.skuList && detail.skuList.length > 0) {
				skuList.value = detail.skuList
				parseSpecGroups(detail.skuList)
				selectSku(detail.skuList[0])
			} else {
				skuList.value = []
				specGroups.value = []
				selectedSku.value = {
					price: props.product.price,
					stock: 999
				}
				updateCartCountDisplay()
			}
		} catch (error) {
			console.error('获取商品详情失败', error)
		}
	}

	const parseSpecGroups = (skus) => {
		const groupsMap = {}
		skus.forEach(sku => {
			if (sku.spec) {
				try {
					const specs = typeof sku.spec === 'string' ? JSON.parse(sku.spec) : sku.spec
					specs.forEach(spec => {
						const key = spec.key
						const value = spec.value
						if (!groupsMap[key]) {
							groupsMap[key] = { key, values: new Set() }
						}
						groupsMap[key].values.add(value)
					})
				} catch (e) {}
			}
		})
		specGroups.value = Object.values(groupsMap).map(group => ({
			key: group.key,
			values: Array.from(group.values)
		}))
	}

	const selectSpec = (specKey, specValue) => {
		selectedSpecs.value[specKey] = specValue
		findMatchingSku()
		updateCartCountDisplay()
	}

	const findMatchingSku = () => {
		const selectedKeys = Object.keys(selectedSpecs.value)
		if (selectedKeys.length === 0 && skuList.value.length > 0) {
			selectSku(skuList.value[0])
			return
		}
		const matchedSku = skuList.value.find(sku => {
			if (!sku.spec) return false
			try {
				const specs = typeof sku.spec === 'string' ? JSON.parse(sku.spec) : sku.spec
				return selectedKeys.every(key => {
					const val = selectedSpecs.value[key]
					return specs.some(s => s.key === key && s.value === val)
				}) && specs.length === selectedKeys.length
			} catch (e) { return false }
		})
		if (matchedSku) selectSku(matchedSku)
	}

	const selectSku = (sku) => {
		selectedSku.value = sku
		if (sku.spec) {
			try {
				const specs = typeof sku.spec === 'string' ? JSON.parse(sku.spec) : sku.spec
				specs.forEach(s => selectedSpecs.value[s.key] = s.value)
			} catch (e) {}
		}
		updateCartCountDisplay()
	}

	const updateCartCountDisplay = () => {
		const productWithSku = {
			...props.product,
			selectedSku: selectedSku.value,
			selectedSpecs: { ...selectedSpecs.value },
			price: selectedSku.value ? selectedSku.value.price : props.product.price
		}
		const cartItem = cartStore.findCartItem(productWithSku)
		currentTempCount.value = cartItem ? cartItem.quantity : 0
	}

	const updateTempCount = (delta, event) => {
		// 校验门店营业状态
		if (appStore.currentStore && appStore.currentStore.openStatus === 0) {
			uni.showToast({ title: '门店休息中，暂不接单', icon: 'none' })
			return
		}

		if (skuList.value.length > 0 && !selectedSku.value) {
			uni.showToast({ title: '请先选择规格', icon: 'none' })
			return
		}
		const productWithSku = {
			...props.product,
			selectedSku: selectedSku.value,
			selectedSpecs: { ...selectedSpecs.value },
			price: selectedSku.value ? selectedSku.value.price : props.product.price
		}
		const cartItem = cartStore.findCartItem(productWithSku)
		const currentCartCount = cartItem ? cartItem.quantity : 0
		const newCount = currentCartCount + delta
		if (newCount < 0) return
		// 记录是否是第一次加入购物车（当前购物车数量为0且要增加）
		const isFirstAdd = currentCartCount === 0 && delta > 0
		
		currentTempCount.value = newCount
		if (delta > 0) {
			// 触发动画事件
			if (event) {
				emit('add-to-cart-anim', event)
			}
			
			cartStore.addToCart(productWithSku, 1).then(() => {
				uni.showToast({ title: '已加入购物车', icon: 'success', duration: 1000 })
				// 只在第一次加入购物车时关闭弹框
				if (isFirstAdd) {
					setTimeout(() => {
						closeModal()
					}, 500) // 延迟500ms关闭，让用户看到成功提示
				}
			}).catch(() => {
				currentTempCount.value = currentCartCount
			})
		} else {
			const cartKey = cartStore.getCartItemKey(productWithSku)
			cartStore.updateQuantity(cartKey, -1).catch(() => {
				currentTempCount.value = currentCartCount
			})
		}
	}

	const getSelectedSpecText = () => {
		const texts = []
		specGroups.value.forEach(group => {
			if (selectedSpecs.value[group.key]) texts.push(selectedSpecs.value[group.key])
		})
		return texts.length > 0 ? texts.join(', ') : '请选择规格'
	}

	const isSpecSelected = (key, val) => selectedSpecs.value[key] === val

	const closeModal = () => {
		emit('update:show', false)
		emit('close')
	}
</script>

<style lang="scss" scoped>
	$primary: #6f4e37;

	.sku-modal-mask {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background-color: rgba(0, 0, 0, 0.5);
		z-index: 999;
		display: flex;
		align-items: flex-end;
	}

	.sku-modal {
		width: 100%;
		background-color: var(--bg-primary);
		border-radius: 32rpx 32rpx 0 0;
		padding: 32rpx;
		animation: slideUp 0.3s ease-out;
		transition: background-color 0.3s;
	}

	@keyframes slideUp {
		from { transform: translateY(100%); }
		to { transform: translateY(0); }
	}

	.modal-header {
		display: flex;
		margin-bottom: 30rpx;
		position: relative;
	}

	.modal-img {
		width: 160rpx;
		height: 160rpx;
		border-radius: 12rpx;
		margin-right: 20rpx;
	}

	.modal-info {
		flex: 1;
	}

	.modal-name {
		font-size: 32rpx;
		font-weight: bold;
		display: block;
		margin-bottom: 8rpx;
		color: var(--text-primary);
	}

	.modal-desc {
		font-size: 24rpx;
		color: var(--text-tertiary);
	}

	.close-btn {
		position: absolute;
		top: 0;
		right: 0;
		padding: 10rpx;
		font-size: 40rpx;
		color: var(--text-tertiary);
		line-height: 1;
	}

	.modal-scroll {
		max-height: 500rpx;
		margin-bottom: 30rpx;
	}

	.spec-group {
		margin-bottom: 30rpx;
	}

	.spec-title {
		font-size: 26rpx;
		color: var(--text-secondary);
		margin-bottom: 16rpx;
		display: block;
	}

	.spec-options {
		display: flex;
		flex-wrap: wrap;
		gap: 20rpx;
	}

	.spec-opt {
		padding: 10rpx 30rpx;
		background-color: var(--bg-secondary);
		border-radius: 8rpx;
		font-size: 24rpx;
		color: var(--text-primary);
		border: 2rpx solid transparent;
		transition: all 0.2s;
	}

	.spec-opt.active {
		background-color: rgba(111, 78, 55, 0.1);
		color: $primary;
		border-color: $primary;
		font-weight: bold;
	}

	.no-spec-tip {
		text-align: center;
		padding: 40rpx;
		color: var(--text-tertiary);
		font-size: 24rpx;
	}

	.modal-footer {
		display: flex;
		justify-content: space-between;
		align-items: center;
		border-top: 1rpx solid var(--border-light);
		padding-top: 20rpx;
	}

	.price-box {
		display: flex;
		flex-direction: column;
	}

	.price-num-row {
		display: flex;
		align-items: baseline;
	}

	.symbol {
		font-size: 24rpx;
		color: $primary;
		font-weight: bold;
	}

	.num {
		font-size: 40rpx;
		color: $primary;
		font-weight: bold;
	}

	.selected-spec {
		font-size: 20rpx;
		color: var(--text-tertiary);
		margin-top: 4rpx;
	}

	.action-box {
		display: flex;
		align-items: center;
	}

	.add-cart-btn {
		background-color: $primary;
		color: white;
		padding: 16rpx 40rpx;
		border-radius: 40rpx;
		font-size: 28rpx;
		font-weight: bold;
	}

	.stepper {
		display: flex;
		align-items: center;
		gap: 20rpx;
	}

	.step-btn {
		width: 50rpx;
		height: 50rpx;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 32rpx;
		font-weight: bold;
	}

	.step-btn.minus {
		border: 2rpx solid var(--border-color);
		color: var(--text-secondary);
	}

	.step-btn.plus {
		background-color: $primary;
		color: white;
	}

	.step-num {
		font-size: 28rpx;
		font-weight: bold;
		color: var(--text-primary);
	}
</style>

