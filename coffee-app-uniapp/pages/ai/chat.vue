<template>
  <view class="chat-container">
    <!-- 自定义顶部导航 -->
    <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-content">
        <view class="back-btn" @click="goBack">
          <uni-icons type="left" size="24" color="#333"></uni-icons>
        </view>
        <view class="title-box">
          <text class="page-title">智能咖啡师</text>
          <text class="status-text">{{ isTyping ? '对方正在输入...' : '在线' }}</text>
        </view>
        <view class="right-placeholder">
          <uni-icons type="trash" size="20" color="#999" @click="handleClearChat"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 聊天内容区域 -->
    <scroll-view 
      class="chat-list" 
      scroll-y 
      :scroll-top="scrollTop"
      :scroll-with-animation="true"
      :style="{ height: contentHeight + 'px' }"
      @click="closeKeyboard"
    >
      <view class="chat-wrapper">
        <!-- 欢迎卡片 -->
        <view class="welcome-card">
          <image src="/static/logo.png" class="welcome-logo" mode="aspectFit"></image>
          <text class="welcome-title" :user-select="true">Hi, 我是您的专属咖啡顾问</text>
          <text class="welcome-desc" :user-select="true">我可以为您推荐口味、解答咖啡知识，或者帮您查看今日优惠。</text>
        </view>

        <!-- 消息流 -->
        <view 
          v-for="(msg, index) in messageList" 
          :key="index" 
          class="message-row"
          :class="msg.role"
        >
          <!-- AI 头像 (左侧) -->
          <view class="avatar-box" v-if="msg.role === 'ai'">
            <view class="ai-avatar">🤖</view>
          </view>

          <!-- 消息内容区域 (包裹气泡和卡片) -->
          <view class="content-container">
            
            <!-- 1. 普通文本气泡 (有内容才显示) -->
            <view class="bubble-box" v-if="msg.content && msg.content.trim()">
              <view class="bubble-content">
                <text :user-select="true" :selectable="true">{{ msg.content }}</text>
              </view>
            </view>

            <!-- 2. 下单商品卡片 (仅 AI 且包含卡片数据时显示) -->
            <view class="order-card" v-if="msg.role === 'ai' && msg.cardData && msg.cardData.type === 'order_card'">
              <view class="card-header">
                <text class="card-title">为您推荐</text>
              </view>
              <view class="card-body">
                <!-- 商品图片，处理 URL -->
                <image :src="getImageUrl(msg.cardData.productPic)" class="product-img" mode="aspectFill"></image>
                <view class="product-info">
                  <text class="p-name">{{ msg.cardData.productName }}</text>
                  <view class="p-specs-box">
                    <text class="p-specs">{{ msg.cardData.specs || '标准规格' }}</text>
                  </view>
                  <view class="p-price-row">
                    <view class="price-box">
                      <text class="symbol">¥</text>
                      <text class="price">{{ msg.cardData.price }}</text>
                    </view>
                    <text class="qty">x{{ msg.cardData.quantity }}</text>
                  </view>
                </view>
              </view>
              <view class="card-footer">
                <view class="total-info">
                  <text>合计:</text>
                  <text class="total-price">¥{{ msg.cardData.totalPrice }}</text>
                </view>
                <button class="buy-btn" @click="handleBuy(msg.cardData)">立即结算</button>
              </view>
            </view>

            <!-- 3. 错误提示卡片 (如果 Tool 返回了 error 类型) -->
            <view class="bubble-box error-bubble" v-if="msg.role === 'ai' && msg.cardData && msg.cardData.type === 'error'">
               <text style="color: #ff4d4f;">⚠️ {{ msg.cardData.msg }}</text>
            </view>

          </view>

          <!-- 用户头像 (右侧) -->
          <view class="avatar-box" v-if="msg.role === 'user'">
            <image :src="userAvatar" class="user-avatar" mode="aspectFill"></image>
          </view>
        </view>

        <!-- Loading 状态 -->
        <view class="message-row ai" v-if="isLoading">
          <view class="avatar-box">
            <view class="ai-avatar">🤖</view>
          </view>
          <view class="content-container">
            <view class="bubble-box">
              <view class="bubble-content loading">
                <view class="dot"></view>
                <view class="dot"></view>
                <view class="dot"></view>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 垫底高度 -->
      <view style="height: 40rpx;"></view>
    </scroll-view>

    <!-- 底部操作区 -->
    <view class="footer-area">
      <!-- 快捷标签 -->
      <scroll-view scroll-x class="quick-tags" v-if="!inputText && !isLoading">
        <view 
          class="tag-item" 
          v-for="(tag, index) in quickTags" 
          :key="index"
          @click="handleTagClick(tag)"
        >
          {{ tag }}
        </view>
      </scroll-view>

      <!-- 输入框 -->
      <view class="input-bar">
        <input 
          class="chat-input" 
          type="text" 
          v-model="inputText" 
          :focus="inputFocus"
          placeholder="想喝点什么？(例如: 一杯热拿铁)" 
          confirm-type="send"
          @confirm="sendMessage"
        />
        <view 
          class="send-btn" 
          :class="{ active: inputText.trim() }"
          @click="sendMessage"
        >
          <uni-icons type="paperplane-filled" size="24" color="#fff"></uni-icons>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { getStatusBarHeight } from '@/utils/system.js'
import { chatWithAi, getChatHistory, clearChatHistory } from '@/services/ai.js'
import { useUserStore } from '@/store/user.js'
import { convertImageUrl } from '@/utils/image.js'
import { searchProducts, getProductDetail } from '@/services/product.js'

// 状态管理
const statusBarHeight = ref(0)
const contentHeight = ref(0)
const scrollTop = ref(0)
const inputText = ref('')
const isLoading = ref(false)
const isTyping = ref(false)
const inputFocus = ref(false)
const userStore = useUserStore()
const userInfo = userStore.userInfo

// 用户头像
const userAvatar = computed(() => {
  return convertImageUrl(userInfo.avatar) || '/static/default-avatar.png'
})

// 快捷标签
const quickTags = [
  '☕️ 有什么新品推荐？',
  '🧊 来一杯冰美式',
  '🥛 拿铁有哪些规格？',
  '🍬 推荐不甜的咖啡',
  '💰 今天有什么优惠券？'
]

// 消息列表
const messageList = ref([])

// 解析JSON并转换为卡片数据（用于历史记录和实时消息）
const parseJsonToCard = async (fullText) => {
  let displayContent = fullText
  let cardData = null

  // 1. 尝试匹配标准的 Markdown JSON 代码块（支持换行符和转义字符）
  const jsonBlockRegex = /```json\s*(\{[\s\S]*?\})\s*```/
  let match = fullText.match(jsonBlockRegex)
  let jsonStr = null

  // 2. 如果没匹配到代码块，尝试匹配裸露的 JSON 对象
  if (!match) {
    // 先尝试匹配包含 "request" 字段的 JSON（订单请求格式，支持单引号和双引号）
    const requestJsonRegex = /(\{\s*["']request["']\s*:\s*\{[\s\S]*?\}\s*\})/
    match = fullText.match(requestJsonRegex)
    
    // 如果还没匹配到，尝试匹配包含 "type" 字段的 JSON
    if (!match) {
      const looseJsonRegex = /(\{\s*["']type["']\s*:\s*["'](order_card|error)["'][\s\S]*?\})/
      match = fullText.match(looseJsonRegex)
    }
  }

  // 3. 提取并解析 JSON 字符串
  if (match) {
    jsonStr = match[1]
    try {
      const data = JSON.parse(jsonStr)
      
      // 如果包含 request 字段，转换为订单卡片
      if (data.request) {
        const requestData = data.request
        // 调用API获取商品详情
        try {
          const productResult = await searchProducts(requestData.productName, 1, 1)
          if (productResult && productResult.records && productResult.records.length > 0) {
            const product = productResult.records[0]
            // 获取商品详情以获取SKU信息
            const detailResult = await getProductDetail(product.id)
            if (detailResult && detailResult.skuList && detailResult.skuList.length > 0) {
              // 尝试匹配SKU规格
              let selectedSku = detailResult.skuList[0]
              const specs = requestData.specs || '标准规格'
              
              // 如果用户指定了规格，尝试匹配
              if (specs && specs !== '标准规格') {
                const matchedSku = detailResult.skuList.find(sku => {
                  if (!sku.spec) return false
                  try {
                    const specJson = typeof sku.spec === 'string' ? JSON.parse(sku.spec) : sku.spec
                    if (Array.isArray(specJson)) {
                      const specValues = specJson.map(s => s.value).join(',')
                      return specValues.includes(specs) || specs.includes(specValues)
                    }
                  } catch (e) {
                    return false
                  }
                  return false
                })
                if (matchedSku) {
                  selectedSku = matchedSku
                }
              }
              
              const quantity = requestData.quantity || 1
              const price = selectedSku.price || product.price
              const totalPrice = price * quantity
              
              // 构建订单卡片数据
              cardData = {
                type: 'order_card',
                productId: product.id,
                skuId: selectedSku.id,
                productName: product.name,
                productPic: product.picUrl,
                specs: specs,
                quantity: quantity,
                price: price,
                totalPrice: totalPrice
              }
            } else {
              // 没有SKU，使用商品默认价格
              const quantity = requestData.quantity || 1
              const price = product.price || 0
              cardData = {
                type: 'order_card',
                productId: product.id,
                skuId: null,
                productName: product.name,
                productPic: product.picUrl,
                specs: requestData.specs || '标准规格',
                quantity: quantity,
                price: price,
                totalPrice: price * quantity
              }
            }
          } else {
            // 商品未找到，显示错误
            cardData = {
              type: 'error',
              msg: `抱歉，未找到商品：${requestData.productName}`
            }
          }
        } catch (error) {
          console.error('获取商品详情失败', error)
          cardData = {
            type: 'error',
            msg: '获取商品信息失败，请稍后重试'
          }
        }
        
        // 移除 JSON 部分
        displayContent = fullText.replace(match[0], '').trim()
        // 清理残留的 markdown 符号
        displayContent = displayContent.replace(/```json/g, '').replace(/```/g, '').trim()
      } 
      // 处理标准格式的 JSON 卡片（type: order_card 或 error）
      else if (data.type === 'order_card' || data.type === 'error') {
        cardData = data
        // 移除 JSON 部分
        displayContent = fullText.replace(match[0], '').trim()
        // 清理残留的 markdown 符号
        displayContent = displayContent.replace(/```/g, '').trim()
      }
    } catch (e) {
      console.error("JSON 解析失败，回退为普通文本", e)
    }
  }

  return { displayContent, cardData }
}

// 加载历史记录
const loadHistory = async () => {
  try {
    const history = await getChatHistory()
    if (history && history.length > 0) {
      // 处理每条历史消息，解析JSON并转换为卡片
      const processedMessages = await Promise.all(
        history.map(async (item) => {
          const role = item.role === 'assistant' ? 'ai' : item.role
          // 只处理AI消息中的JSON
          if (role === 'ai' && item.content) {
            const { displayContent, cardData } = await parseJsonToCard(item.content)
            return {
              role,
              content: displayContent,
              cardData
            }
          }
          return {
            role,
            content: item.content,
            cardData: null
          }
        })
      )
      messageList.value = processedMessages
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载历史记录失败', error)
  }
}

// 图片辅助处理
const getImageUrl = (url) => {
  if (!url) return '/static/logo.png' 
  return convertImageUrl(url)
}

onMounted(() => {
  statusBarHeight.value = getStatusBarHeight()
  const screenHeight = uni.getSystemInfoSync().windowHeight
  // 动态计算高度：屏幕 - 状态栏 - 导航栏(44) - 底部输入区(约110)
  contentHeight.value = screenHeight - statusBarHeight.value - 44 - 110 
  loadHistory()
})

const goBack = () => {
  uni.navigateBack()
}

const scrollToBottom = () => {
  nextTick(() => {
    scrollTop.value = 9999999
  })
}

const handleTagClick = (text) => {
  inputText.value = text
  sendMessage()
}

const closeKeyboard = () => {
  uni.hideKeyboard()
  inputFocus.value = false
}

const handleClearChat = () => {
  uni.showModal({
    title: '提示',
    content: '确定要清空聊天记录吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await clearChatHistory()
          messageList.value = []
          uni.showToast({ title: '记录已清空', icon: 'success' })
        } catch (error) {
          uni.showToast({ title: '清空失败', icon: 'none' })
        }
      }
    }
  })
}

// --- 下单逻辑 ---
const handleBuy = (cardData) => {
  // 跳转到确认订单页面，传递 SKU ID 和数量
  uni.navigateTo({
    url: `/pages/order/confirm?skuId=${cardData.skuId}&count=${cardData.quantity}`
  })
}

// --- 消息处理核心 ---
const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || isLoading.value) return

  // 1. 上屏用户消息
  messageList.value.push({ role: 'user', content: text })
  inputText.value = ''
  scrollToBottom()
  
  isLoading.value = true
  isTyping.value = true

  try {
    // 2. 调用后端 API
    const responseText = await chatWithAi(text)
    // 3. 处理响应（解析 JSON 卡片 + 打字机效果）
    await processResponse(responseText)
  } catch (error) {
    console.error(error)
    isLoading.value = false
    isTyping.value = false
    messageList.value.push({ 
      role: 'ai', 
      content: '抱歉，网络连接似乎有点问题，请稍后再试 😣' 
    })
    scrollToBottom()
  }
}

// 解析响应数据 (包含 JSON 和 文本)
const processResponse = async (fullText) => {
  isLoading.value = false
  
  // 使用统一的JSON解析函数
  const { displayContent, cardData } = await parseJsonToCard(fullText)

  // 渲染逻辑
  // 先推入一个空的 AI 消息容器
  const msgIndex = messageList.value.push({ role: 'ai', content: '', cardData: null }) - 1
  
  // 文本部分打字机效果
  if (displayContent) {
    const chars = displayContent.split('')
    for (let i = 0; i < chars.length; i++) {
       // 如果文字太长，打字速度加快
       const delay = chars.length > 50 ? 10 : 30
       await new Promise(resolve => setTimeout(resolve, delay))
       messageList.value[msgIndex].content += chars[i]
       scrollToBottom()
    }
  }

  // 卡片部分延迟显示
  if (cardData) {
    if (displayContent) await new Promise(resolve => setTimeout(resolve, 200))
    messageList.value[msgIndex].cardData = cardData
    scrollToBottom()
  }

  isTyping.value = false
}
</script>

<style lang="scss" scoped>
$primary: #6f4e37;
$bg-color: #f7f8fa;
$ai-bubble: #ffffff;
$user-bubble: #6f4e37;

.chat-container {
  height: 100vh;
  background-color: $bg-color;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 导航栏 */
.nav-bar {
  background-color: #fff;
  z-index: 100;
  border-bottom: 1rpx solid rgba(0,0,0,0.05);
  
  .nav-content {
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 32rpx;
  }
  
  .title-box {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .page-title {
      font-size: 34rpx;
      font-weight: bold;
      color: #333;
    }
    
    .status-text {
      font-size: 20rpx;
      color: #07c160;
      margin-top: 4rpx;
    }
  }
  
  .back-btn, .right-placeholder {
    width: 48rpx;
    height: 48rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

/* 消息列表 */
.chat-list {
  flex: 1;
  background-color: $bg-color;
}

.chat-wrapper {
  padding: 32rpx;
}

/* 欢迎卡片 */
.welcome-card {
  background-color: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: 60rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.03);
  
  .welcome-logo {
    width: 100rpx;
    height: 100rpx;
    margin-bottom: 24rpx;
    border-radius: 50%;
  }
  
  .welcome-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 16rpx;
  }
  
  .welcome-desc {
    font-size: 26rpx;
    color: #999;
    line-height: 1.5;
  }
}

/* 消息行布局 */
.message-row {
  display: flex;
  margin-bottom: 40rpx;
  align-items: flex-start;
  
  &.user {
    flex-direction: row; 
    justify-content: flex-end;
    
    .bubble-box {
      background-color: $user-bubble;
      color: #fff;
      border-radius: 24rpx 4rpx 24rpx 24rpx;
      margin-right: 20rpx;
    }
  }
  
  &.ai {
    flex-direction: row;
    justify-content: flex-start;
    
    .bubble-box {
      background-color: $ai-bubble;
      color: #333;
      border-radius: 4rpx 24rpx 24rpx 24rpx;
      margin-left: 20rpx;
      box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.03);
    }
  }
}

/* 统一的内容容器，用于包裹气泡和可能的卡片 */
.content-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  max-width: 70%;
}

.avatar-box {
  flex-shrink: 0;
  
  .ai-avatar {
    width: 80rpx;
    height: 80rpx;
    background-color: #fff;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40rpx;
    box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
  }
  
  .user-avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background-color: #ddd;
  }
}

.bubble-box {
  padding: 24rpx 32rpx;
  font-size: 30rpx;
  line-height: 1.6;
  position: relative;
  word-break: break-all;
  user-select: text;
  -webkit-user-select: text;
  width: 100%; 
  box-sizing: border-box;
}

/* 错误提示样式 */
.error-bubble {
    background-color: #fff2f0 !important;
    border: 1rpx solid #ffccc7;
    margin-top: 10rpx;
    margin-left: 20rpx;
    border-radius: 12rpx;
}

/* --- 下单卡片样式 --- */
.order-card {
  width: 100%; // 相对于 content-container
  background-color: #fff;
  border-radius: 16rpx;
  margin-top: 20rpx;
  margin-left: 20rpx; // 对齐气泡
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.08);
  border: 1rpx solid #eee;

  .card-header {
    background-color: #fcfcfc;
    padding: 20rpx 24rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1rpx solid #f0f0f0;

    .card-title {
      font-size: 28rpx;
      font-weight: bold;
      color: #333;
    }
    .card-badge {
      font-size: 20rpx;
      background-color: rgba(111, 78, 55, 0.1);
      color: $primary;
      padding: 4rpx 12rpx;
      border-radius: 8rpx;
    }
  }

  .card-body {
    padding: 24rpx;
    display: flex;
    gap: 20rpx;

    .product-img {
      width: 120rpx;
      height: 120rpx;
      border-radius: 12rpx;
      background-color: #f5f5f5;
      flex-shrink: 0;
    }

    .product-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-between;

      .p-name {
        font-size: 30rpx;
        font-weight: bold;
        color: #333;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .p-specs-box {
        .p-specs {
          font-size: 24rpx;
          color: #999;
          background-color: #f8f8f8;
          padding: 4rpx 10rpx;
          border-radius: 6rpx;
        }
      }

      .p-price-row {
        display: flex;
        justify-content: space-between;
        align-items: flex-end;

        .price-box {
          color: #ff5a5f;
          .symbol { font-size: 24rpx; }
          .price { font-size: 34rpx; font-weight: bold; }
        }
        .qty {
          font-size: 24rpx;
          color: #999;
        }
      }
    }
  }

  .card-footer {
    padding: 20rpx 24rpx;
    background-color: #fff;
    border-top: 1rpx dashed #eee;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .total-info {
      font-size: 26rpx;
      color: #666;
      .total-price {
        color: #333;
        font-weight: bold;
        font-size: 30rpx;
        margin-left: 8rpx;
      }
    }

    .buy-btn {
      margin: 0;
      background: linear-gradient(135deg, #6f4e37, #8b5e3c);
      color: #fff;
      font-size: 26rpx;
      padding: 0 32rpx;
      height: 60rpx;
      line-height: 60rpx;
      border-radius: 30rpx;
      box-shadow: 0 4rpx 12rpx rgba(111, 78, 55, 0.3);
      
      &:active {
        opacity: 0.9;
        transform: scale(0.98);
      }
    }
  }
}

/* Loading 动画 */
.loading {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 10rpx 0;
  
  .dot {
    width: 12rpx;
    height: 12rpx;
    background-color: #ccc;
    border-radius: 50%;
    animation: bounce 1.4s infinite ease-in-out both;
    
    &:nth-child(1) { animation-delay: -0.32s; }
    &:nth-child(2) { animation-delay: -0.16s; }
  }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* 底部区域 */
.footer-area {
  background-color: #fff;
  padding-bottom: calc(env(safe-area-inset-bottom) + 20rpx);
  border-top: 1rpx solid #eee;
  display: flex;
  flex-direction: column;
}

/* 快捷标签 */
.quick-tags {
  white-space: nowrap;
  padding: 20rpx 0 0 20rpx;
  
  .tag-item {
    display: inline-block;
    padding: 12rpx 24rpx;
    background-color: #f5f5f5;
    color: #666;
    font-size: 24rpx;
    border-radius: 30rpx;
    margin-right: 20rpx;
    border: 1rpx solid #eee;
    
    &:active {
      background-color: #eee;
    }
  }
}

/* 输入栏 */
.input-bar {
  display: flex;
  align-items: center;
  padding: 20rpx 32rpx;
  
  .chat-input {
    flex: 1;
    background-color: #f5f5f5;
    height: 80rpx;
    border-radius: 40rpx;
    padding: 0 32rpx;
    font-size: 28rpx;
    margin-right: 20rpx;
  }
  
  .send-btn {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background-color: #ddd;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s;
    
    &.active {
      background-color: $primary;
      transform: scale(1.05);
      box-shadow: 0 4rpx 12rpx rgba(111, 78, 55, 0.3);
    }
  }
}
</style>