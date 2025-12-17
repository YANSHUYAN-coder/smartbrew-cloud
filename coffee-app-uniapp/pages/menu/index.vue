<template>
	<view class="menu-page">
		<!-- 1. 顶部 Header (固定) -->
		<view class="header-wrapper" :style="{ paddingTop: statusBarHeight + 'px' }">
			<!-- 搜索与切换行 -->
			<view class="header-content">
				<view class="search-bar">
					<uni-icons custom-prefix="iconfont" type="icon-search"
					color="#000" size="24"></uni-icons>
					<input class="search-input" type="text" placeholder="搜拿铁/美式/甜点" disabled @click="handleSearch" />
				</view>
				<!-- 业务切换胶囊 -->
				<view class="order-type-switch">
					<view class="switch-item" :class="{ active: orderType === 'pickup' }" @click="orderType = 'pickup'">
						自提</view>
					<view class="switch-item" :class="{ active: orderType === 'delivery' }"
						@click="orderType = 'delivery'">外送</view>
				</view>
			</view>

			<!-- 门店/地址提示 -->
			<view class="shop-info">
				<view class="shop-name-row">
					<text class="shop-name">智咖·云</text>
					<text class="distance">距您 120m</text>
				</view>
				<text class="shop-desc" v-if="orderType === 'pickup'">营业中 07:30-22:00 · 制作约3分钟</text>
				<text class="shop-desc" v-else>免配送费 · 预计30分钟送达</text>
			</view>
		</view>

		<!-- 2. 主体内容区 (左右联动) -->
		<view class="menu-container">
			<!-- 左侧分类栏 -->
			<scroll-view class="category-sidebar" scroll-y :scroll-into-view="leftScrollId" scroll-with-animation>
				<view v-for="(cat, index) in categories" :key="cat.id" :id="'cat-left-' + index" class="category-item"
					:class="{ active: activeCategoryIndex === index }" @click="handleCategoryClick(index)">
					<!-- <image :src="cat.icon" class="category-icon" v-if="cat.icon">{{ cat.icon }}</image> -->
					<text class="category-name">{{ cat.name }}</text>
					<!-- 选中指示器 -->
					<view v-if="activeCategoryIndex === index" class="active-indicator"></view>
					<!-- 分类角标 (模拟：仅第一个分类 HOT) -->
					<view v-if="index === 0" class="cat-badge">NEW</view>
				</view>
				<!-- 底部占位 -->
				<view style="height: 100rpx;"></view>
			</scroll-view>

			<!-- 右侧商品列表 -->
			<scroll-view 
				class="product-list" 
				scroll-y 
				:scroll-into-view="rightScrollId" 
				scroll-with-animation
				@scroll="onRightScroll"
				:refresher-enabled="true"
				:refresher-triggered="refreshing"
				@refresherrefresh="onRefresh"
				@refresherrestore="onRefreshRestore">
				<view class="product-wrapper">
					<view v-for="(cat, cIndex) in categories" :key="cat.id" :id="'cat-right-' + cIndex"
						class="category-section">
						<!-- 分类标题 -->
						<view class="section-title">{{ cat.name }}</view>

						<!-- 商品卡片 -->
						<!-- 逻辑：如果是 'new' 分类，展示所有新品(模拟前2个)；否则展示对应分类商品 -->
						<view
							v-for="prod in (cat.id === 7 ? products.slice(0,2) : products.filter(p => p.categoryId === cat.id))"
							:key="prod.id" class="product-item" @click="openSkuModal(prod)">
							<image :src="prod.image" mode="aspectFill" class="prod-img" />
							<view class="prod-info">
								<view class="prod-header">
									<text class="prod-name">{{ prod.name }}</text>
									<text class="prod-desc">{{ prod.desc }}</text>
								</view>

								<view class="prod-tags">
									<text class="tag" v-if="cat.id === 'new'">本周新品</text>
									<text class="tag" v-else>人气热销</text>
									<text class="tag">好评 {{ prod.rating }}</text>
								</view>

								<view class="prod-footer">
									<text class="price">¥<text class="price-num">{{ prod.price }}</text></text>
									<!-- 选规格按钮 -->
									<view class="add-btn-wrapper" @click.stop="openSkuModal(prod)">
										<text class="spec-btn">选规格</text>
										<!-- 如果有数量，显示角标 -->
										<view class="badge" v-if="getCartCountById(prod.id) > 0">
											{{ getCartCountById(prod.id) }}
										</view>
									</view>
								</view>
							</view>
						</view>
					</view>
					<!-- 底部垫高，防止被购物车遮挡 -->
					<view style="height: 180rpx;"></view>
				</view>
			</scroll-view>
		</view>

		<!-- 3. 悬浮购物车条 -->
		<view class="cart-bar-wrapper" v-if="cartStore.totalCount > 0">
			<view class="cart-bar" @click="handleCartClick">
				<view class="cart-icon-wrap">
					<image src="https://img.icons8.com/color/96/shopping-bag--v1.png" class="cart-img" />
					<view class="total-badge">{{ cartStore.totalCount }}</view>
				</view>
				<view class="price-info">
					<text class="total-price">¥{{ cartStore.totalPrice.toFixed(2) }}</text>
					<text class="delivery-tip">预计 15 分钟后可取</text>
				</view>
				<view class="checkout-btn" @click.stop="handleCheckout">
					去结算
				</view>
			</view>
		</view>

		<!-- 4. 规格选择弹窗 (高级毛玻璃效果) -->
		<view class="sku-modal-mask" v-if="showModal" @click="closeModal">
			<view class="sku-modal" @click.stop>
				<!-- 头部 -->
				<view class="modal-header">
					<image :src="currentProduct.image" class="modal-img" mode="aspectFill" />
					<view class="modal-info">
						<text class="modal-name">{{ currentProduct.name }}</text>
						<text class="modal-desc">{{ currentProduct.desc }}</text>
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
						<text class="symbol">¥</text>
						<text class="num">{{ selectedSku ? selectedSku.price : currentProduct.price }}</text>
						<text class="selected-spec">已选: {{ getSelectedSpecText() }}</text>
					</view>
					<view class="action-box">
						<!-- 简单的加减器 -->
						<view class="stepper" v-if="currentTempCount > 0">
							<view class="step-btn minus" @click="updateTempCount(-1)">-</view>
							<text class="step-num">{{ currentTempCount }}</text>
							<view class="step-btn plus" @click="updateTempCount(1)">+</view>
						</view>
						<view class="add-cart-btn" v-else @click="updateTempCount(1)">
							加入购物车
						</view>
					</view>
				</view>
			</view>
		</view>

	</view>
</template>

<script setup>
	import {
		ref,
		computed,
		onMounted
	} from 'vue'
	import {
		getStatusBarHeight
	} from '@/utils/system.js'
	import {
		PRODUCTS
	} from '@/utils/data.js'
	import {
		useCartStore
	} from '@/store/cart.js'
	
	import { getCategories } from '@/services/categories.js'
	import { getMenuVO, getProductDetail } from '@/services/product.js'


	const statusBarHeight = ref(0)
	const cartStore = useCartStore()

	// 状态管理
	const orderType = ref('pickup') // pickup | delivery
	const activeCategoryIndex = ref(0)
	const leftScrollId = ref('cat-left-0')
	const rightScrollId = ref('')
	const showModal = ref(false)
	const currentProduct = ref({})
	const currentTempCount = ref(0)
	const skuList = ref([]) // SKU列表
	const specGroups = ref([]) // 规格分组（按key分组）
	const selectedSpecs = ref({}) // 选中的规格 {容量: '大杯', 温度: '冰', ...}
	const selectedSku = ref(null) // 当前选中的SKU
	const refreshing = ref(false) // 下拉刷新状态

	// 数据别名
	// 【新增】在最前面添加新品分类
	const categories = ref([])
	const products = ref([])
	
	// --- 左右联动逻辑 ---
	const handleCategoryClick = (index) => {
		activeCategoryIndex.value = index
		rightScrollId.value = 'cat-right-' + index
	}

	// 模拟右侧滚动监听 (UniApp中精确监听需要 boundingClientRect)
	// 这里简化处理：暂时不做滚动右侧自动高亮左侧的复杂计算，
	// 实际开发中可以使用 uni.createIntersectionObserver
	const onRightScroll = (e) => {
		// 留给后续优化
	}

	// --- 弹窗逻辑 ---
	const openSkuModal = async (product) => {
		currentProduct.value = product
		currentTempCount.value = 0
		selectedSpecs.value = {}
		selectedSku.value = null
		
		try {
			// 获取商品详情（包含SKU列表）
			const detail = await getProductDetail(product.id)
			console.log('商品详情', detail)
			
			if (detail && detail.skuList && detail.skuList.length > 0) {
				skuList.value = detail.skuList
				// 解析规格并分组
				parseSpecGroups(detail.skuList)
				// 默认选中第一个SKU
				selectSku(detail.skuList[0])
			} else {
				// 如果没有SKU，使用商品基础价格
				skuList.value = []
				specGroups.value = []
				selectedSku.value = {
					price: product.price,
					stock: 999
				}
				// 没有SKU时，直接更新购物车数量显示
				updateCartCountDisplay()
			}
			
			showModal.value = true
			
			// 如果有SKU，在选择SKU后会自动更新购物车数量显示
		} catch (error) {
			console.error('获取商品详情失败', error)
			uni.showToast({
				title: '加载商品详情失败',
				icon: 'none'
			})
		}
	}
	
	// 解析规格分组
	const parseSpecGroups = (skus) => {
		const groupsMap = {}
		
		// 遍历所有SKU，提取规格
		skus.forEach(sku => {
			if (sku.spec) {
				try {
					const specs = typeof sku.spec === 'string' ? JSON.parse(sku.spec) : sku.spec
					specs.forEach(spec => {
						const key = spec.key
						const value = spec.value
						
						if (!groupsMap[key]) {
							groupsMap[key] = {
								key: key,
								values: new Set()
							}
						}
						groupsMap[key].values.add(value)
					})
				} catch (e) {
					console.error('解析规格失败', e)
				}
			}
		})
		
		// 转换为数组格式
		specGroups.value = Object.values(groupsMap).map(group => ({
			key: group.key,
			values: Array.from(group.values)
		}))
	}
	
	// 选择规格
	const selectSpec = (specKey, specValue) => {
		selectedSpecs.value[specKey] = specValue
		// 根据选中的规格查找对应的SKU
		findMatchingSku()
		// 更新购物车数量显示（基于当前选中的规格）
		updateCartCountDisplay()
	}
	
	// 查找匹配的SKU
	const findMatchingSku = () => {
		const selectedKeys = Object.keys(selectedSpecs.value)
		if (selectedKeys.length === 0) {
			// 如果还没有选择任何规格，使用第一个SKU
			if (skuList.value.length > 0) {
				selectSku(skuList.value[0])
			}
			return
		}
		
		// 查找匹配的SKU
		const matchedSku = skuList.value.find(sku => {
			if (!sku.spec) return false
			
			try {
				const specs = typeof sku.spec === 'string' ? JSON.parse(sku.spec) : sku.spec
				// 检查所有选中的规格是否都匹配
				return selectedKeys.every(key => {
					const selectedValue = selectedSpecs.value[key]
					return specs.some(spec => spec.key === key && spec.value === selectedValue)
				}) && specs.length === selectedKeys.length
			} catch (e) {
				return false
			}
		})
		
		if (matchedSku) {
			selectSku(matchedSku)
		}
	}
	
	// 选择SKU
	const selectSku = (sku) => {
		selectedSku.value = sku
		// 如果SKU有规格，自动设置选中的规格
		if (sku.spec) {
			try {
				const specs = typeof sku.spec === 'string' ? JSON.parse(sku.spec) : sku.spec
				specs.forEach(spec => {
					selectedSpecs.value[spec.key] = spec.value
				})
			} catch (e) {
				console.error('解析SKU规格失败', e)
			}
		}
		// 更新购物车数量显示
		updateCartCountDisplay()
	}
	
	// 更新购物车数量显示（基于当前选中的规格）
	const updateCartCountDisplay = () => {
		if (!currentProduct.value.id) return
		
		// 构建当前选中规格的商品对象
		const productWithSku = {
			...currentProduct.value,
			selectedSku: selectedSku.value,
			selectedSpecs: { ...selectedSpecs.value },
			price: selectedSku.value ? selectedSku.value.price : currentProduct.value.price
		}
		
		// 查找购物车中该规格的商品数量
		const cartItem = cartStore.findCartItem(productWithSku)
		currentTempCount.value = cartItem ? cartItem.quantity : 0
	}
	
	// 获取已选规格文本
	const getSelectedSpecText = () => {
		const texts = []
		specGroups.value.forEach(group => {
			if (selectedSpecs.value[group.key]) {
				texts.push(selectedSpecs.value[group.key])
			}
		})
		return texts.length > 0 ? texts.join(', ') : '请选择规格'
	}
	
	// 检查规格是否被选中
	const isSpecSelected = (specKey, specValue) => {
		return selectedSpecs.value[specKey] === specValue
	}

	const closeModal = () => {
		showModal.value = false
	}

	const updateTempCount = (delta) => {
		console.log("已选择规格",selectedSpecs.value);
		// 检查是否已选择规格（如果有SKU的话）
		if (skuList.value.length > 0 && !selectedSku.value) {
			uni.showToast({
				title: '请先选择规格',
				icon: 'none'
			})
			return
		}
		
		// 构建包含SKU信息的商品对象
		const productWithSku = {
			...currentProduct.value,
			selectedSku: selectedSku.value,
			selectedSpecs: { ...selectedSpecs.value },
			// 使用SKU的价格，如果没有SKU则使用商品基础价格
			price: selectedSku.value ? selectedSku.value.price : currentProduct.value.price
		}
		
		// 查找购物车中是否已存在该商品（相同规格）
		const cartItem = cartStore.findCartItem(productWithSku)
		const currentCartCount = cartItem ? cartItem.quantity : 0
		
		const newCount = currentCartCount + delta
		if (newCount < 0) return
		
		// 更新临时计数（用于UI显示）
		currentTempCount.value = newCount
		
		// 同步到购物车 Store
		if (delta > 0) {
			// 增加数量
			cartStore.addToCart(productWithSku, 1).then(() => {
				// 显示成功提示
				uni.showToast({
					title: '已加入购物车',
					icon: 'success',
					duration: 1000
				})
			}).catch(error => {
				console.error('加入购物车失败', error)
				// 回滚数量
				currentTempCount.value = currentCartCount
			})
		} else {
			// 减少数量
			const cartKey = cartStore.getCartItemKey(productWithSku)
			cartStore.updateQuantity(cartKey, -1).catch(error => {
				console.error('更新购物车失败', error)
				// 回滚数量
				currentTempCount.value = currentCartCount
			})
		}
	}

	// 获取商品在购物车中的总数量（所有规格的总和）
	const getCartCountById = (pid) => {
		return cartStore.getProductTotalCount(pid)
	}

	const handleCartClick = () => {
		uni.switchTab({
			url: '/pages/cart/index'
		})
	}

	const handleCheckout = () => {
		uni.navigateTo({
			url: '/pages/order/confirm'
		})
	}

	const handleSearch = () => {
		uni.showToast({
			title: '搜索功能',
			icon: 'none'
		})
	}

	// 加载菜单数据（提取为独立函数，方便刷新时调用）
	const loadMenuData = async () => {
		try {
			const menuData = await getMenuVO()
			console.log("后端返回的菜单数据", menuData)
			
			// 直接使用后端返回的数据，无需转换
			if (menuData && menuData.categories && menuData.products) {
				// 分类列表直接使用
				categories.value = menuData.categories
				
				// 商品列表需要映射字段名，确保与模板一致
				products.value = menuData.products.map(product => ({
					...product,
					// 字段名映射：后端 picUrl -> 前端 image
					image: product.picUrl || product.image || 'https://via.placeholder.com/180',
					// 字段名映射：后端 description -> 前端 desc
					desc: product.description || product.desc || '',
					// 确保有评分字段
					rating: product.rating || 4.5,
					// categoryId 已经是 Long 类型，直接使用
					categoryId: product.categoryId
				}))
			}
			
			console.log("分类列表", categories.value)
			console.log("商品列表", products.value)
			return true
		} catch (error) {
			console.error("获取菜单数据失败", error)
			uni.showToast({
				title: '加载菜单失败',
				icon: 'none'
			})
			return false
		}
	}

	// 下拉刷新处理
	const onRefresh = async () => {
		refreshing.value = true
		await loadMenuData()
		// 延迟一下，让用户看到刷新效果
		setTimeout(() => {
			refreshing.value = false
		}, 300)
	}

	// 刷新恢复处理
	const onRefreshRestore = () => {
		refreshing.value = false
	}

	onMounted(async () => {
		statusBarHeight.value = getStatusBarHeight()
		await loadMenuData()
	})
</script>

<style lang="scss" scoped>
	/* 定义主题色 */
	$primary: #6f4e37;
	$bg-gray: #f8f8f8;

	.menu-page {
		height: 100vh;
		display: flex;
		flex-direction: column;
		background-color: white;
	}

	/* --- 1. Header 样式 --- */
	.header-wrapper {
		background-color: white;
		z-index: 50;
		box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.03);
	}

	.header-content {
		display: flex;
		align-items: center;
		padding: 16rpx 24rpx;
		gap: 20rpx;
	}

	.search-bar {
		flex: 1;
		height: 64rpx;
		background-color: #f5f5f5;
		border-radius: 32rpx;
		display: flex;
		align-items: center;
		padding: 0 24rpx;
	}

	.search-icon {
		font-size: 28rpx;
		color: #999;
		margin-right: 10rpx;
	}

	.search-input {
		font-size: 26rpx;
		width: 100%;
		color: #333;
	}

	.order-type-switch {
		display: flex;
		background-color: #f5f5f5;
		border-radius: 32rpx;
		padding: 4rpx;
		height: 64rpx;
		box-sizing: border-box;
	}

	.switch-item {
		padding: 0 24rpx;
		font-size: 26rpx;
		color: #666;
		border-radius: 28rpx;
		display: flex;
		align-items: center;
		transition: all 0.2s;
	}

	.switch-item.active {
		background-color: #333;
		color: #d4af37;
		/* 黑金配色 */
		font-weight: bold;
	}

	.shop-info {
		padding: 0 24rpx 20rpx;
	}

	.shop-name-row {
		display: flex;
		align-items: baseline;
		gap: 12rpx;
		margin-bottom: 4rpx;
	}

	.shop-name {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
	}

	.distance {
		font-size: 22rpx;
		color: #999;
	}

	.shop-desc {
		font-size: 22rpx;
		color: #666;
	}

	/* --- 2. 主体容器 --- */
	.menu-container {
		flex: 1;
		display: flex;
		overflow: hidden;
	}

	/* 左侧分类 */
	.category-sidebar {
		width: 180rpx;
		background-color: #f7f8fa;
		height: 100%;
	}

	.category-item {
		height: 100rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 26rpx;
		color: #666;
		position: relative;
		transition: all 0.2s;
	}

	.category-item.active {
		background-color: white;
		color: #333;
		font-weight: bold;
		font-size: 28rpx;
	}

	.active-indicator {
		position: absolute;
		left: 0;
		top: 30rpx;
		bottom: 30rpx;
		width: 6rpx;
		background-color: $primary;
		border-radius: 0 4rpx 4rpx 0;
	}

	.cat-badge {
		position: absolute;
		top: 10rpx;
		right: 10rpx;
		font-size: 16rpx;
		background: #ff4d4f;
		color: white;
		padding: 2rpx 6rpx;
		border-radius: 6rpx;
		transform: scale(0.8);
	}

	/* 右侧商品 */
	.product-list {
		flex: 1;
		height: 100%;
		background-color: white;
	}

	.category-section {
		padding: 20rpx 24rpx 0;
	}

	.section-title {
		font-size: 26rpx;
		color: #333;
		font-weight: bold;
		margin-bottom: 20rpx;
		padding-left: 8rpx;
	}

	/* 商品卡片 */
	.product-item {
		display: flex;
		margin-bottom: 40rpx;
		min-height: 180rpx;
	}

	.prod-img {
		width: 180rpx;
		height: 180rpx;
		border-radius: 16rpx;
		background-color: #eee;
		flex-shrink: 0;
	}

	.prod-info {
		flex: 1;
		margin-left: 20rpx;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		padding-bottom: 8rpx;
		min-width: 0; /* 防止 flex 子元素溢出 */
	}

	.prod-header {
		flex: 1;
		display: flex;
		flex-direction: column;
		min-width: 0; /* 防止文字溢出 */
	}

	.prod-name {
		font-size: 30rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 8rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
		line-height: 1.4;
	}

	.prod-desc {
		font-size: 22rpx;
		color: #999;
		line-height: 1.5;
		overflow: hidden;
		text-overflow: ellipsis;
		display: -webkit-box;
		-webkit-line-clamp: 2; /* 限制显示2行 */
		line-clamp: 2; /* 标准属性，兼容性更好 */
		-webkit-box-orient: vertical;
		word-break: break-all; /* 允许在单词内换行 */
		margin-bottom: 8rpx;
	}

	.prod-tags {
		display: flex;
		gap: 8rpx;
		margin-top: 8rpx;
		flex-wrap: wrap; /* 允许标签换行 */
	}

	.tag {
		font-size: 20rpx;
		color: $primary;
		background: rgba(111, 78, 55, 0.1);
		padding: 2rpx 8rpx;
		border-radius: 6rpx;
		white-space: nowrap; /* 标签文字不换行 */
		flex-shrink: 0; /* 标签不收缩 */
	}

	.prod-footer {
		display: flex;
		justify-content: space-between;
		align-items: flex-end;
		margin-top: 12rpx;
		flex-shrink: 0; /* 底部区域不收缩 */
	}

	.price {
		font-size: 24rpx;
		color: $primary;
		font-weight: bold;
	}

	.price-num {
		font-size: 34rpx;
	}

	.add-btn-wrapper {
		position: relative;
		flex-shrink: 0; /* 按钮不收缩 */
	}

	.spec-btn {
		background-color: $primary;
		color: white;
		font-size: 22rpx;
		padding: 10rpx 24rpx;
		border-radius: 24rpx;
		font-weight: bold;
		white-space: nowrap; /* 按钮文字不换行 */
	}

	.badge {
		position: absolute;
		top: -12rpx;
		right: -12rpx;
		background-color: #ff4d4f;
		color: white;
		font-size: 20rpx;
		min-width: 32rpx;
		height: 32rpx;
		border-radius: 16rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		border: 2rpx solid white;
	}

	/* --- 3. 悬浮购物车 --- */
	.cart-bar-wrapper {
		position: fixed;
		bottom: 30rpx;
		/* 距离底部 */
		left: 30rpx;
		right: 30rpx;
		z-index: 90;
	}

	.cart-bar {
		background-color: #222;
		height: 100rpx;
		border-radius: 50rpx;
		display: flex;
		align-items: center;
		padding-left: 140rpx;
		/* 留出图标位置 */
		padding-right: 6rpx;
		position: relative;
		box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.3);
	}

	.cart-icon-wrap {
		position: absolute;
		left: 0;
		bottom: 0;
		width: 120rpx;
		height: 120rpx;
		/* 溢出部分用于展示大图标 */
	}

	.cart-img {
		width: 100%;
		height: 100%;
		transform: scale(1.1) translateY(-10rpx);
	}

	.total-badge {
		position: absolute;
		top: 10rpx;
		right: 10rpx;
		background-color: #ff4d4f;
		color: white;
		font-size: 22rpx;
		padding: 4rpx 12rpx;
		border-radius: 20rpx;
		border: 2rpx solid #222;
	}

	.price-info {
		flex: 1;
		display: flex;
		flex-direction: column;
		justify-content: center;
	}

	.total-price {
		color: white;
		font-size: 34rpx;
		font-weight: bold;
	}

	.delivery-tip {
		color: #888;
		font-size: 20rpx;
	}

	.checkout-btn {
		background-color: $primary;
		color: white;
		height: 88rpx;
		padding: 0 48rpx;
		border-radius: 44rpx;
		display: flex;
		align-items: center;
		font-size: 30rpx;
		font-weight: bold;
	}

	/* --- 4. 规格弹窗 --- */
	.sku-modal-mask {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background-color: rgba(0, 0, 0, 0.5);
		z-index: 100;
		display: flex;
		align-items: flex-end;
	}

	.sku-modal {
		width: 100%;
		background-color: white;
		border-radius: 32rpx 32rpx 0 0;
		padding: 32rpx;
		animation: slideUp 0.3s ease-out;
	}

	@keyframes slideUp {
		from {
			transform: translateY(100%);
		}

		to {
			transform: translateY(0);
		}
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
	}

	.modal-desc {
		font-size: 24rpx;
		color: #999;
	}

	.close-btn {
		position: absolute;
		top: 0;
		right: 0;
		padding: 10rpx;
		font-size: 40rpx;
		color: #999;
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
		color: #666;
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
		background-color: #f5f5f5;
		border-radius: 8rpx;
		font-size: 24rpx;
		color: #333;
		border: 2rpx solid transparent;
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
		color: #999;
		font-size: 24rpx;
	}

	.modal-footer {
		display: flex;
		justify-content: space-between;
		align-items: center;
		border-top: 1rpx solid #eee;
		padding-top: 20rpx;
	}

	.price-box {
		display: flex;
		align-items: baseline;
		flex-direction: column;
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
		color: #999;
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
		border: 2rpx solid #ddd;
		color: #666;
	}

	.step-btn.plus {
		background-color: $primary;
		color: white;
	}

	.step-num {
		font-size: 30rpx;
		font-weight: bold;
		min-width: 40rpx;
		text-align: center;
	}
</style>