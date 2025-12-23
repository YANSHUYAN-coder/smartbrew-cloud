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
				<view class="no-result" v-else-if="!loading">
					<image src="/static/empty.png" mode="aspectFit" class="empty-img" />
					<text class="empty-text">未找到相关商品</text>
				</view>
				
				<!-- 加载中提示 -->
				<view class="loading-tip" v-if="loading">
					<text class="load-text">搜索中...</text>
				</view>
				
				<!-- 加载更多 -->
				<view class="load-more" v-if="hasMore && !loading && filteredProducts.length > 0" @click="loadMore">
					<text class="load-text">加载更多</text>
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
		getMenuVO,
		searchProducts
	} from '@/services/product.js'
	import {
		useCartStore
	} from '@/store/cart.js'
	import SkuModal from '@/components/SkuModal.vue'

	const statusBarHeight = ref(0)
	const searchKeyword = ref('')
	const filteredProducts = ref([])
	const historyList = ref([])
	const showModal = ref(false)
	const currentProduct = ref({})
	const cartStore = useCartStore()
	
	// 分页相关
	const currentPage = ref(1)
	const pageSize = ref(20)
	const hasMore = ref(true)
	const loading = ref(false)

	onMounted(() => {
		statusBarHeight.value = getStatusBarHeight()
		loadHistory()
	})

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
		// 实时搜索：输入时触发搜索
		if (searchKeyword.value.trim()) {
			currentPage.value = 1
			hasMore.value = true
			performSearch()
		} else {
			filteredProducts.value = []
		}
	}

	const handleSearch = () => {
		if (searchKeyword.value.trim()) {
			saveHistory(searchKeyword.value.trim())
			currentPage.value = 1
			hasMore.value = true
			performSearch()
		}
	}
	
	// 执行搜索
	const performSearch = async () => {
		if (!searchKeyword.value.trim() || loading.value) return
		
		loading.value = true
		try {
			const result = await searchProducts(searchKeyword.value.trim(), currentPage.value, pageSize.value)
			if (result && result.records) {
				const products = result.records.map(product => ({
					...product,
					image: product.picUrl || product.image || 'https://via.placeholder.com/180',
					desc: product.description || product.desc || '',
					rating: product.rating || 4.5
				}))
				
				if (currentPage.value === 1) {
					filteredProducts.value = products
				} else {
					filteredProducts.value = [...filteredProducts.value, ...products]
				}
				
				// 判断是否还有更多数据
				hasMore.value = result.records.length >= pageSize.value && 
				                (currentPage.value * pageSize.value) < (result.total || 0)
			} else {
				if (currentPage.value === 1) {
					filteredProducts.value = []
				}
				hasMore.value = false
			}
		} catch (error) {
			console.error("搜索商品失败", error)
			uni.showToast({
				title: '搜索失败',
				icon: 'none'
			})
		} finally {
			loading.value = false
		}
	}
	
	// 加载更多
	const loadMore = () => {
		if (hasMore.value && !loading.value && searchKeyword.value.trim()) {
			currentPage.value++
			performSearch()
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
        console.log("11");
        
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
	
	.loading-tip {
		padding: 40rpx 0;
		text-align: center;
	}
	
	.load-more {
		padding: 40rpx 0;
		text-align: center;
	}
	
	.load-text {
		font-size: 24rpx;
		color: #999;
	}
</style>
