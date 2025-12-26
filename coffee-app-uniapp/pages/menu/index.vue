<template>
	<view class="menu-page">
		<!-- 1. 顶部 Header (固定) -->
		<view class="header-wrapper" :style="{ paddingTop: statusBarHeight + 'px' }">
			<!-- 搜索与切换行 -->
			<view class="header-content">
				<view class="search-bar">
					<uni-icons custom-prefix="iconfont" type="icon-sousuo"
					color="#000" size="16"></uni-icons>
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
		<!-- 骨架屏 -->
		<MenuSkeleton v-if="loading" />

		<!-- 实际内容 -->
		<view v-else class="menu-container">
			<!-- 左侧分类栏 -->
			<scroll-view class="category-sidebar" scroll-y :scroll-into-view="leftScrollId" scroll-with-animation>
				<view v-for="(cat, index) in categories" :key="cat.id" :id="'cat-left-' + index" class="category-item"
					:class="{ active: activeCategoryIndex === index }" @click="handleCategoryClick(index)">
					<!-- <image :src="cat.icon" class="category-icon" v-if="cat.icon">{{ cat.icon }}</image> -->
					<text class="category-name">{{ cat.name }}</text>
					<!-- 选中指示器 -->
					<view v-if="activeCategoryIndex === index" class="active-indicator"></view>
					<!-- 分类角标 (仅对虚拟分类 '人气新品' 显示 NEW) -->
					<view v-if="cat.id === -1" class="cat-badge">NEW</view>
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
				@refresherrestore="onRefreshRestore"
				@scrolltolower="loadMoreProducts">
				<view class="product-wrapper">
					<view v-for="(cat, cIndex) in categories" :key="cat.id" :id="'cat-right-' + cIndex"
						class="category-section">
						<!-- 分类标题 -->
						<view class="section-title">{{ cat.name }}</view>

						<!-- 商品卡片 -->
						<!-- 逻辑：如果是 'new' 分类，展示所有新品(模拟前2个)；否则展示对应分类商品 -->
						<view
							v-for="prod in getDisplayProducts(cat)"
							:key="prod.id" class="product-item" @click="openSkuModal(prod)">
							<image :src="prod.image" mode="aspectFill" class="prod-img" lazy-load />
							<view class="prod-info">
								<view class="prod-header">
									<text class="prod-name">{{ prod.name }}</text>
									<text class="prod-desc">{{ prod.desc }}</text>
								</view>

								<view class="prod-tags">
									<text class="tag" v-if="cat.id === -1">人气新品</text>
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
					<!-- 加载更多提示 -->
					<view class="load-more" v-if="hasMoreProducts">
						<text class="load-text">加载中...</text>
					</view>
					<view class="load-more" v-else-if="allProducts.length > displayedProductCount">
						<text class="load-text">没有更多了</text>
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

		<!-- 4. 规格选择弹窗 -->
		<SkuModal v-model:show="showModal" :product="currentProduct" />

	</view>
</template>

<script setup>
	import {
		ref,
		computed,
		onMounted,
		nextTick,
		watch
	} from 'vue'
	import {
		getStatusBarHeight
	} from '@/utils/system.js'
	import {
		useCartStore
	} from '@/store/cart.js'
	import { convertImageUrl } from '@/utils/image.js'
	import { getMenuVO, getProductDetail } from '@/services/product.js'
	import SkuModal from '@/components/SkuModal.vue'
	import MenuSkeleton from '@/components/MenuSkeleton.vue'


	const statusBarHeight = ref(0)
	const cartStore = useCartStore()

	// 状态管理
	const orderType = ref('pickup') // pickup | delivery
	const activeCategoryIndex = ref(0)
	const leftScrollId = ref('cat-left-0')
	const rightScrollId = ref('')
	const showModal = ref(false)
	const currentProduct = ref({})
	const refreshing = ref(false) // 下拉刷新状态
	const loading = ref(true) // 页面加载状态

	// --- 左右联动相关变量 ---
	const categoryTops = ref([]) // 存储右侧每个分类 section 的 top 值
	let isClickScroll = false // 标记是否由点击左侧触发的滚动，防止冲突

	// 数据别名
	// 【新增】在最前面添加新品分类
	const categories = ref([])
	const allProducts = ref([]) // 所有商品数据
	const displayedProductCount = ref(10) // 当前显示的商品数量（每个分类的前N个）
	const hasMoreProducts = ref(false) // 是否还有更多商品
	const loadingMore = ref(false) // 是否正在加载更多
	const productPerPage = ref(10) // 每次加载的商品数量
	
	// --- 左右联动逻辑 ---
	const handleCategoryClick = (index) => {
		isClickScroll = true
		activeCategoryIndex.value = index
		rightScrollId.value = 'cat-right-' + index
		
		// 动画结束后重置标识位
		setTimeout(() => {
			isClickScroll = false
		}, 500)
	}

	// 计算右侧各分类的高度区间
	const calcCategoryTops = () => {
		nextTick(() => {
			const query = uni.createSelectorQuery()
			query.selectAll('.category-section').boundingClientRect(data => {
				if (data && data.length > 0) {
					// 记录相对于第一个 section 的位置
					const baseTop = data[0].top
					categoryTops.value = data.map(item => item.top - baseTop)
				}
			}).exec()
		})
	}

	// 获取要显示的商品列表（支持分页，已移除新品分类的数量限制）
	const getDisplayProducts = (cat) => {
		// 注意：虚拟分类ID现在是 -1 (Long 类型在 JS 中可能需要注意比较，这里用严格相等)
		const categoryId = cat.id
		const categoryProducts = allProducts.value.filter(p => p.categoryId === categoryId)
		
		// 计算当前分类应该显示的商品数量
		// 简化处理：每个分类显示最多 displayedProductCount 个商品
		return categoryProducts.slice(0, displayedProductCount.value)
	}

	// 加载更多商品
	const loadMoreProducts = () => {
		if (loadingMore.value || !hasMoreProducts.value) return
		
		loadingMore.value = true
		// 模拟加载延迟，让用户看到加载效果
		setTimeout(() => {
			const newCount = displayedProductCount.value + productPerPage.value
			// 计算所有分类中商品最多的分类的商品数量
			const maxProductsPerCategory = Math.max(
				...categories.value.map(cat => {
					// 虚拟分类ID为-1，不需要特殊逻辑，直接过滤即可
					return allProducts.value.filter(p => p.categoryId === cat.id).length
				}),
				0
			)
			
			displayedProductCount.value = Math.min(newCount, maxProductsPerCategory)
			hasMoreProducts.value = displayedProductCount.value < maxProductsPerCategory
			loadingMore.value = false
			
			// 高度变化，重新计算位置
			calcCategoryTops()
		}, 300)
	}

	// 监听右侧滚动
	const onRightScroll = (e) => {
		if (isClickScroll) return 

		const scrollTop = e.detail.scrollTop
		// 增加一个小偏移量，提升体验
		const threshold = 20 
		
		let index = 0
		for (let i = 0; i < categoryTops.value.length; i++) {
			if (scrollTop >= categoryTops.value[i] - threshold) {
				index = i
			} else {
				break
			}
		}
		
		if (activeCategoryIndex.value !== index) {
			activeCategoryIndex.value = index
			// 同步左侧滚动
			leftScrollId.value = 'cat-left-' + index
		}
	}

	// --- 弹窗逻辑 ---
	const openSkuModal = (product) => {
		currentProduct.value = product
		showModal.value = true
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
		uni.navigateTo({
			url: '/pages/menu/search'
		})
	}

	// 加载菜单数据（提取为独立函数，方便刷新时调用）
	const loadMenuData = async () => {
		try {
			loading.value = true
			const menuData = await getMenuVO()
			
			// 直接使用后端返回的数据，无需转换
			if (menuData && menuData.categories && menuData.products) {
				// 1. 商品列表需要映射字段名，确保与模板一致
				const mappedProducts = menuData.products.map(product => ({
					...product,
					// 字段名映射：后端 picUrl -> 前端 image，并转换为代理 URL
					image: convertImageUrl(product.picUrl || product.image) || 'https://via.placeholder.com/180',
					// 字段名映射：后端 description -> 前端 desc
					desc: product.description || product.desc || '',
					// 确保有评分字段
					rating: product.rating || 4.5,
					// categoryId 已经是 Long 类型，直接使用
					categoryId: product.categoryId,
					// 确保有新品和推荐状态字段
					newStatus: product.newStatus || 0,
					recommendStatus: product.recommendStatus || 0
				}))
				
				// 2. 前端构建"人气新品"虚拟分类
				// 筛选逻辑：newStatus=1 且 recommendStatus=1
				const hotProducts = mappedProducts
					.filter(p => p.newStatus === 1 && p.recommendStatus === 1)
					.map(p => ({
						...p,
						categoryId: -1 // 归属到虚拟分类 ID
					}))

				// 3. 如果有新品，则在分类列表最前面插入虚拟分类，并将虚拟商品加入总列表
				if (hotProducts.length > 0) {
					// 插入虚拟分类到最前面
					menuData.categories.unshift({
						id: -1,
						name: '人气新品'
					})
					// 将虚拟分类的商品加入总列表
					mappedProducts.push(...hotProducts)
				}

				// 4. 分类列表直接使用（已包含虚拟分类）
				categories.value = menuData.categories
				
				// 5. 存储所有商品数据
				allProducts.value = mappedProducts
				
				// 重置显示数量
				displayedProductCount.value = productPerPage.value
				// 判断是否还有更多商品（计算所有分类中商品最多的分类的商品数量）
				const maxProductsPerCategory = Math.max(
					...categories.value.map(cat => {
						return mappedProducts.filter(p => p.categoryId === cat.id).length
					}),
					0
				)
				hasMoreProducts.value = maxProductsPerCategory > productPerPage.value

				// 数据加载后，计算各分类位置以实现左右联动
				calcCategoryTops()
			}
			return true
		} catch (error) {
			console.error("获取菜单数据失败", error)
			uni.showToast({
				title: '加载菜单失败',
				icon: 'none'
			})
			return false
		} finally {
			loading.value = false
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

	/* 加载更多 */
	.load-more {
		padding: 40rpx 0;
		text-align: center;
	}

	.load-text {
		font-size: 24rpx;
		color: #999;
	}
</style>