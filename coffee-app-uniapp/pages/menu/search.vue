<template>
	<view class="search-page">
		<!-- 顶部搜索栏 (固定) -->
		<view class="header-wrapper" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="header-content">
				<view class="search-bar">
					<uni-icons custom-prefix="iconfont" type="search" color="#999" size="18"></uni-icons>
					<input class="search-input" type="text" v-model="searchKeyword" placeholder="搜拿铁/美式/甜点" :focus="true"
						confirm-type="search" @confirm="handleSearch" @input="handleInput" />
					<uni-icons v-if="searchKeyword" type="clear" color="#ccc" size="18" @click="clearSearch"></uni-icons>
				</view>
				<text class="cancel-btn" @click="goBack">取消</text>
			</view>
		</view>

		<!-- 内容区域 -->
		<scroll-view scroll-y class="content-area">
			<!-- 搜索历史 (当无输入时显示) -->
			<view class="history-section" v-if="!searchKeyword && historyList.length > 0">
				<view class="section-header">
					<text class="title">历史搜索</text>
					<uni-icons type="trash" size="16" color="#999" @click="clearHistory"></uni-icons>
				</view>
				<view class="tags-wrapper">
					<view class="tag" v-for="(tag, index) in historyList" :key="index" @click="searchByTag(tag)">
						{{ tag }}
					</view>
				</view>
			</view>

			<!-- 搜索结果 -->
			<view class="result-section" v-if="searchKeyword">
				<view v-if="filteredProducts.length > 0">
					<view v-for="prod in filteredProducts" :key="prod.id" class="product-item"
						@click="openSkuModal(prod)">
						<image :src="prod.image" mode="aspectFill" class="prod-img" />
						<view class="prod-info">
							<view class="prod-header">
								<rich-text class="prod-name" :nodes="highlightKeyword(prod.name)"></rich-text>
								<text class="prod-desc">{{ prod.desc }}</text>
							</view>
							<view class="prod-footer">
								<text class="price">¥<text class="price-num">{{ prod.price }}</text></text>
								<view class="add-btn-wrapper" @click.stop="openSkuModal(prod)">
									<text class="spec-btn">选规格</text>
									<view class="badge" v-if="getCartCountById(prod.id) > 0">
										{{ getCartCountById(prod.id) }}
									</view>
								</view>
							</view>
						</view>
					</view>
				</view>

				<!-- 无结果提示 -->
				<view class="no-result" v-else>
					<image src="/static/empty.png" mode="aspectFit" class="empty-img" />
					<text class="empty-text">未找到相关商品</text>
				</view>
			</view>
		</scroll-view>

		<!-- 规格选择弹窗 -->
		<SkuModal v-model:show="showModal" :product="currentProduct" />
	</view>
</template>

<script setup>
	import {
		ref,
		onMounted,
		computed
	} from 'vue'
	import {
		getStatusBarHeight
	} from '@/utils/system.js'
	import {
		getMenuVO
	} from '@/services/product.js'
	import {
		useCartStore
	} from '@/store/cart.js'
	import SkuModal from '@/components/SkuModal.vue'

	const statusBarHeight = ref(0)
	const searchKeyword = ref('')
	const allProducts = ref([])
	const historyList = ref([])
	const showModal = ref(false)
	const currentProduct = ref({})
	const cartStore = useCartStore()

	const filteredProducts = computed(() => {
		if (!searchKeyword.value.trim()) return []
		const keyword = searchKeyword.value.toLowerCase().trim()
		return allProducts.value.filter(p =>
			p.name.toLowerCase().includes(keyword) ||
			(p.desc && p.desc.toLowerCase().includes(keyword))
		)
	})

	onMounted(async () => {
		statusBarHeight.value = getStatusBarHeight()
		loadHistory()
		await loadProducts()
	})

	const loadProducts = async () => {
		try {
			const menuData = await getMenuVO()
			if (menuData && menuData.products) {
				allProducts.value = menuData.products.map(product => ({
					...product,
					image: product.picUrl || product.image || 'https://via.placeholder.com/180',
					desc: product.description || product.desc || '',
					rating: product.rating || 4.5
				}))
			}
		} catch (error) {
			console.error("加载商品数据失败", error)
		}
	}

	const loadHistory = () => {
		const history = uni.getStorageSync('search_history')
		if (history) {
			historyList.value = JSON.parse(history)
		}
	}

	const saveHistory = (keyword) => {
		if (!keyword) return
		let history = [...historyList.value]
		// 去重并移到最前
		const index = history.indexOf(keyword)
		if (index > -1) {
			history.splice(index, 1)
		}
		history.unshift(keyword)
		// 最多保留 10 条
		if (history.length > 10) history.pop()
		
		historyList.value = history
		uni.setStorageSync('search_history', JSON.stringify(history))
	}

	const clearHistory = () => {
		uni.removeStorageSync('search_history')
		historyList.value = []
	}

	const handleInput = (e) => {
		// 实时搜索，无需额外操作，computed 会自动更新
	}

	const handleSearch = () => {
		if (searchKeyword.value.trim()) {
			saveHistory(searchKeyword.value.trim())
		}
	}

	const searchByTag = (tag) => {
		searchKeyword.value = tag
		handleSearch()
	}

	const clearSearch = () => {
		searchKeyword.value = ''
	}

	const goBack = () => {
		uni.navigateBack()
	}

	const openSkuModal = (product) => {
		currentProduct.value = product
		showModal.value = true
	}

	const getCartCountById = (pid) => {
		return cartStore.getProductTotalCount(pid)
	}
	
	// 高亮关键词
	const highlightKeyword = (text) => {
		if (!searchKeyword.value) return text
		const keyword = searchKeyword.value
		const reg = new RegExp(keyword, 'gi')
		return text.replace(reg, match => `<span style="color: #6f4e37; font-weight: bold;">${match}</span>`)
	}
</script>

<style lang="scss" scoped>
	$primary: #6f4e37;
	$bg-gray: #f8f8f8;

	.search-page {
		height: 100vh;
		display: flex;
		flex-direction: column;
		background-color: white;
	}

	.header-wrapper {
		background-color: white;
		z-index: 50;
		border-bottom: 1rpx solid #eee;
	}

	.header-content {
		display: flex;
		align-items: center;
		padding: 16rpx 24rpx;
		gap: 20rpx;
		height: 44px; // 导航栏标准高度
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

	.search-input {
		flex: 1;
		font-size: 28rpx;
		color: #333;
		margin: 0 10rpx;
	}

	.cancel-btn {
		font-size: 28rpx;
		color: #333;
	}

	.content-area {
		flex: 1;
		background-color: white;
	}

	/* 历史记录 */
	.history-section {
		padding: 32rpx;
	}

	.section-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 24rpx;

		.title {
			font-size: 28rpx;
			font-weight: bold;
			color: #333;
		}
	}

	.tags-wrapper {
		display: flex;
		flex-wrap: wrap;
		gap: 20rpx;
	}

	.tag {
		padding: 10rpx 24rpx;
		background-color: #f5f5f5;
		color: #666;
		font-size: 24rpx;
		border-radius: 28rpx;
	}

	/* 搜索结果 */
	.result-section {
		padding: 0 32rpx;
	}

	.product-item {
		display: flex;
		padding: 32rpx 0;
		border-bottom: 1rpx solid #f5f5f5;

		&:last-child {
			border-bottom: none;
		}
	}

	.prod-img {
		width: 160rpx;
		height: 160rpx;
		border-radius: 12rpx;
		background-color: #eee;
		margin-right: 24rpx;
		flex-shrink: 0;
	}

	.prod-info {
		flex: 1;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
	}

	.prod-name {
		font-size: 30rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 8rpx;
	}

	.prod-desc {
		font-size: 24rpx;
		color: #999;
		overflow: hidden;
		text-overflow: ellipsis;
		display: -webkit-box;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		-webkit-box-orient: vertical;
		white-space: normal;
	}

	.prod-footer {
		display: flex;
		justify-content: space-between;
		align-items: flex-end;
	}

	.price {
		font-size: 24rpx;
		color: $primary;
		font-weight: bold;
	}

	.price-num {
		font-size: 36rpx;
	}

	.add-btn-wrapper {
		position: relative;
	}

	.spec-btn {
		background-color: $primary;
		color: white;
		font-size: 22rpx;
		padding: 10rpx 24rpx;
		border-radius: 24rpx;
		font-weight: bold;
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

	.no-result {
		padding-top: 100rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		
		.empty-img {
			width: 240rpx;
			height: 240rpx;
			margin-bottom: 20rpx;
		}
		
		.empty-text {
			font-size: 28rpx;
			color: #999;
		}
	}
</style>
