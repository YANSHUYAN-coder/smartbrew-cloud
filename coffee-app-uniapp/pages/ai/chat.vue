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
        <view class="right-placeholder" >
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
          <text class="welcome-title">Hi, 我是您的专属咖啡顾问</text>
          <text class="welcome-desc">我可以为您推荐口味、解答咖啡知识，或者帮您查看今日优惠。</text>
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

          <!-- 消息气泡 -->
          <view class="bubble-box">
            <view class="bubble-content">
              <text :user-select="true">{{ msg.content }}</text>
            </view>
          </view>

          <!-- 用户头像 (右侧) -->
          <view class="avatar-box" v-if="msg.role === 'user'">
            <image :src="userAvatar.value" class="user-avatar" mode="aspectFill"></image>
          </view>
        </view>

        <!-- Loading 状态 -->
        <view class="message-row ai" v-if="isLoading">
          <view class="avatar-box">
            <view class="ai-avatar">🤖</view>
          </view>
          <view class="bubble-box">
            <view class="bubble-content loading">
              <view class="dot"></view>
              <view class="dot"></view>
              <view class="dot"></view>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 垫底高度 -->
      <view style="height: 40rpx;"></view>
    </scroll-view>

    <!-- 底部操作区 -->
    <view class="footer-area">
      <!-- 快捷标签 (当没有输入时显示) -->
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
          placeholder="想喝点什么？告诉我您的口味..." 
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

// 状态管理
const statusBarHeight = ref(0)
const contentHeight = ref(0)
const scrollTop = ref(0)
const inputText = ref('')
const isLoading = ref(false)
const isTyping = ref(false)
const inputFocus = ref(false)

// 导入图片转换工具
import { convertImageUrl } from '@/utils/image.js'

// 用户信息
const userInfo = uni.getStorageSync('userInfo') || {}
const userAvatar = computed(() => {
  return convertImageUrl(userInfo.avatar) || '/static/default-avatar.png'
})

// 快捷标签
const quickTags = [
  '☕️ 有什么新品推荐？',
  '🥛 我乳糖不耐受喝什么？',
  '💤 昨晚没睡好，来杯提神的',
  '🍬 不想太甜，推荐一款',
  '💰 今天有什么优惠券？'
]

// 消息列表
const messageList = ref([])

// 加载历史记录
const loadHistory = async () => {
  try {
    const history = await getChatHistory()
    if (history && history.length > 0) {
      messageList.value = history.map(item => ({
        role: item.role === 'assistant' ? 'ai' : item.role,
        content: item.content
      }))
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载历史记录失败', error)
  }
}

// 初始化
onMounted(() => {
  statusBarHeight.value = getStatusBarHeight()
  // 计算内容区域高度 (屏幕高度 - 导航栏 - 底部输入区)
  const screenHeight = uni.getSystemInfoSync().windowHeight
  contentHeight.value = screenHeight - statusBarHeight.value - 44 - 100 // 粗略估算
  
  // 加载聊天历史
  loadHistory()
})

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    scrollTop.value = 9999999 // 给一个足够大的值
  })
}

// 点击标签
const handleTagClick = (text) => {
  inputText.value = text
  sendMessage()
}

// 关闭键盘
const closeKeyboard = () => {
  uni.hideKeyboard()
  inputFocus.value = false
}

// 清除聊天记录
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

// 发送消息核心逻辑
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
	console.log("responseText",responseText);
    
    // 3. 模拟打字机效果显示 AI 回复
    isLoading.value = false
    await typeWriterEffect(responseText)
    
  } catch (error) {
    isLoading.value = false
    isTyping.value = false
    messageList.value.push({ 
      role: 'ai', 
      content: '抱歉，网络连接似乎有点问题，请稍后再试 😣' 
    })
    scrollToBottom()
  }
}

// 打字机效果模拟
const typeWriterEffect = async (fullText) => {
  // 先推入一个空消息
  const msgIndex = messageList.value.push({ role: 'ai', content: '' }) - 1
  
  const chars = fullText.split('')
  for (let i = 0; i < chars.length; i++) {
    // 稍微随机一点的延迟，模拟思考
    const delay = Math.random() * 30 + 20 
    await new Promise(resolve => setTimeout(resolve, delay))
    
    messageList.value[msgIndex].content += chars[i]
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

/* 消息行 */
.message-row {
  display: flex;
  margin-bottom: 40rpx;
  align-items: flex-start;
  
  &.user {
    // 关键修改：正常 flex 顺序，右对齐
    flex-direction: row; 
    justify-content: flex-end;
    
    .bubble-box {
      background-color: $user-bubble;
      color: #fff;
      // 气泡尖角指向右边 (右上角圆角设小)
      border-radius: 24rpx 4rpx 24rpx 24rpx;
      margin-right: 20rpx; // 气泡距离右侧头像的间距
    }
  }
  
  &.ai {
    // AI 靠左
    flex-direction: row;
    justify-content: flex-start;
    
    .bubble-box {
      background-color: $ai-bubble;
      color: #333;
      border-radius: 4rpx 24rpx 24rpx 24rpx;
      margin-left: 20rpx; // 气泡距离左侧头像的间距
      box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.03);
    }
  }
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
  max-width: 65%;
  padding: 24rpx 32rpx;
  font-size: 30rpx;
  line-height: 1.6;
  position: relative;
  word-break: break-all;
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
    background-color: #ddd; // 默认灰色
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s;
    
    &.active {
      background-color: $primary; // 有内容时变色
      transform: scale(1.05);
      box-shadow: 0 4rpx 12rpx rgba(111, 78, 55, 0.3);
    }
  }
}
</style>