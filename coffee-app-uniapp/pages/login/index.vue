<template>
  <view class="container">
    <!-- 顶部 Logo 区域 -->
    <view class="header">
      <!-- 已替换为在线咖啡主题 Logo 图片，您可以直接预览效果 -->
      <image src="@/static/logo.png" mode="aspectFit" class="logo"></image>
      <text class="title">{{ isLogin ? '欢迎回来' : '创建账号' }}</text>
      <text class="subtitle">{{ isLogin ? '登录以管理您的咖啡订单' : '注册成为 SmartBrew 会员' }}</text>
    </view>

    <!-- 表单区域 -->
    <view class="form-box">

      <!-- 登录方式切换 Tab -->
      <view class="login-tabs" v-if="isLogin">
        <view
            class="tab-item"
            :class="{ active: loginType === 'password' }"
            @click="loginType = 'password'"
        >
          账号登录
        </view>
        <view
            class="tab-item"
            :class="{ active: loginType === 'mobile' }"
            @click="loginType = 'mobile'"
        >
          验证码登录
        </view>
      </view>

      <!-- 账号/手机号输入 -->
      <view class="input-group">
        <view class="icon-box">
          <text class="icon">👤</text> <!-- 实际项目中可用 iconfont 或 image -->
        </view>
        <input
            class="input"
            type="text"
            v-model="form.username"
            placeholder="请输入手机号"
            placeholder-class="placeholder"
        />
      </view>

      <!-- 密码输入 (密码登录或注册时显示) -->
      <view class="input-group" v-if="!isLogin || loginType === 'password'">
        <view class="icon-box">
          <text class="icon">🔒</text>
        </view>
        <input
            class="input"
            type="password"
            v-model="form.password"
            placeholder="请输入密码"
            placeholder-class="placeholder"
        />
      </view>

      <!-- 验证码输入 (验证码登录时显示) -->
      <view class="input-group" v-if="isLogin && loginType === 'mobile'">
        <view class="icon-box">
          <text class="icon">📩</text>
        </view>
        <input
            class="input"
            type="number"
            v-model="form.code"
            placeholder="请输入验证码"
            placeholder-class="placeholder"
            maxlength="6"
        />
        <view
            class="code-btn"
            :class="{ disabled: countdown > 0 }"
            @click="handleSendCode"
        >
          {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
        </view>
      </view>

      <!-- 确认密码 (仅注册模式显示) -->
      <view class="input-group" v-if="!isLogin">
        <view class="icon-box">
          <text class="icon">🛡️</text>
        </view>
        <input
            class="input"
            type="password"
            v-model="form.confirmPassword"
            placeholder="请再次输入密码"
            placeholder-class="placeholder"
        />
      </view>

      <!-- 提交按钮 -->
      <button class="btn-submit" hover-class="btn-hover" @click="handleSubmit">
        {{ isLogin ? '登 录' : '注 册' }}
      </button>

      <!-- 底部切换链接 -->
      <view class="toggle-box">
        <text class="toggle-text">{{ isLogin ? '还没有账号？' : '已有账号？' }}</text>
        <text class="toggle-btn" @click="toggleMode">{{ isLogin ? '立即注册' : '立即登录' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import {ref, reactive, onMounted, onUnmounted} from 'vue'
import {post, request} from '@/utils/request.js'
import {sendCode, loginByMobile} from '@/services/user.js'
import {useUserStore} from '@/store/user.js'

// Store 实例
const userStore = useUserStore()

// 状态定义
const isLogin = ref(true) // true 为登录模式, false 为注册模式
const loginType = ref('password') // 'password': 密码登录, 'mobile': 验证码登录
const countdown = ref(0)
let timer = null

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  code: ''
})

// 切换 登录/注册 模式
const toggleMode = () => {
  isLogin.value = !isLogin.value
  // 切换时重置表单
  form.password = ''
  form.confirmPassword = ''
  form.code = ''
  if (!isLogin.value) {
    // 注册默认为账号密码模式（或者你可以根据需求调整）
    loginType.value = 'password'
  }
}

// 发送验证码
const handleSendCode = async () => {
  if (countdown.value > 0) return

  if (!form.username) {
    uni.showToast({title: '请输入手机号', icon: 'none'})
    return
  }

  if (!/^1\d{10}$/.test(form.username)) {
    uni.showToast({title: '手机号格式不正确', icon: 'none'})
    return
  }

  try {
    uni.showLoading({title: '发送中...'})
    await sendCode(form.username)
    uni.hideLoading()
    uni.showToast({title: '验证码已发送', icon: 'none'})

    // 开始倒计时
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)

  } catch (e) {
    uni.hideLoading()
    console.error('发送验证码失败:', e)
  }
}

// 组件销毁时清除定时器
onUnmounted(() => {
  if (timer) clearInterval(timer)
})

// 提交表单
const handleSubmit = async () => {
  // 1. 公共校验：手机号/用户名
  if (!form.username) {
    uni.showToast({title: '请输入账号/手机号', icon: 'none'})
    return
  }

  // 2. 根据模式校验
  if (isLogin.value && loginType.value === 'mobile') {
    // 验证码登录模式
    if (!/^1\d{10}$/.test(form.username)) {
      uni.showToast({title: '请输入正确的手机号', icon: 'none'})
      return
    }
    if (!form.code) {
      uni.showToast({title: '请输入验证码', icon: 'none'})
      return
    }
  } else {
    // 密码登录或注册模式
    if (!form.password) {
      uni.showToast({title: '请输入密码', icon: 'none'})
      return
    }
    // 注册模式下的密码确认校验
    if (!isLogin.value && form.password !== form.confirmPassword) {
      uni.showToast({title: '两次密码输入不一致', icon: 'none'})
      return
    }
  }

  // 3. 调用后端接口
  if (isLogin.value) {
    if (loginType.value === 'mobile') {
      await handleMobileLogin()
    } else {
      await handleLogin()
    }
  } else {
    await handleRegister()
  }
}

// 手机验证码登录逻辑
const handleMobileLogin = async () => {
  try {
    uni.showLoading({title: '登录中...', mask: true})

    const res = await loginByMobile({
      mobile: form.username,
      code: form.code
    })

    await processLoginSuccess(res)

  } catch (e) {
    uni.hideLoading()
    console.error('手机验证码登录异常:', e)
  }
}

// 账号密码登录逻辑
const handleLogin = async () => {
  try {
    uni.showLoading({title: '登录中...', mask: true})
	console.log("password：",form.password);
    // 调用登录接口
    const res = await post('/auth/login', {
      phone: form.username,
      password: form.password
    })
	
    await processLoginSuccess(res)

  } catch (e) {
    uni.hideLoading()
    console.error('登录异常:', e)
  }
}

// 统一处理登录成功
const processLoginSuccess = async (res) => {
  // 解构返回数据
  const {token, refreshToken, user, userInfo} = res || {}
  // 注意：有些接口返回 user，有些返回 userInfo，这里做个兼容，优先取 userInfo
  const finalUserInfo = userInfo || user

  // 1. 先保存基础 Token 信息
  userStore.setUser({
    token: token || '',
    refreshToken: refreshToken || '',
    userInfo: finalUserInfo || null
  })

  // 2. 尝试获取更完整的用户信息 (如果接口返回的信息不全)
  // 这里简单判断，如果返回的信息里有 id 就认为还可以，否则再查一次
  if (!finalUserInfo || !finalUserInfo.id) {
    try {
      const userInfoRes = await request({
        url: '/app/member/info',
        method: 'GET'
      })
      const fullInfo = userInfoRes.data || userInfoRes
      if (fullInfo) {
        userStore.setUser({userInfo: fullInfo})
      }
    } catch (e) {
      console.warn('获取详情失败', e)
    }
  }

  uni.hideLoading()
  uni.showToast({title: '登录成功', icon: 'success'})

  // 触发全局登录成功事件
  uni.$emit('userLoginSuccess')

  // 延迟跳转
  setTimeout(() => {
    uni.switchTab({url: '/pages/home/index'})
  }, 800)
}

// 注册逻辑
const handleRegister = async () => {
  try {
    uni.showLoading({title: '注册中...', mask: true})

    // 校验手机号格式
    if (!/^1\d{10}$/.test(form.username)) {
      uni.showToast({title: '手机号格式不正确', icon: 'none'})
      uni.hideLoading()
      return
    }

    await post('/auth/register', {
      phone: form.username,
      password: form.password
    })

    uni.hideLoading()
    uni.showToast({title: '注册成功，请登录', icon: 'success'})

    // 注册成功后自动切回登录模式
    isLogin.value = true
    loginType.value = 'password'
    form.password = ''
    form.confirmPassword = ''

  } catch (e) {
    uni.hideLoading()
    console.error('注册异常:', e)
  }
}

onMounted(() => {
  console.log("refreshToken", userStore.refreshToken);
});
</script>

<style lang="scss">
/* 页面容器：全屏浅灰背景，flex 居中布局 */
page {
  background-color: #f5f7fa;
  height: 100%;
}

.container {
  padding: 40px 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 顶部 Header 样式 */
.header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 40px;

  .logo {
    width: 80px;
    height: 80px;
    margin-bottom: 15px;
    border-radius: 15px;
    // 如果没有图片，用背景色占位
    background-color: #e0e0e0;
  }

  .title {
    font-size: 24px;
    font-weight: bold;
    color: #333;
    margin-bottom: 8px;
  }

  .subtitle {
    font-size: 14px;
    color: #999;
  }
}

/* 表单卡片样式 */
.form-box {
  width: 100%;
  background-color: #ffffff;
  padding: 30px 20px;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
}

/* 输入框组样式 */
.input-group {
  display: flex;
  align-items: center;
  background-color: #f8f8f8;
  border-radius: 12px;
  margin-bottom: 20px;
  padding: 0 15px;
  height: 50px;
  transition: all 0.3s;

  /* 获得焦点时的边框高亮 */
  &:focus-within {
    background-color: #fff;
    box-shadow: 0 0 0 2px #d4a373; /* 咖啡色系高亮 */
  }

  .icon-box {
    width: 30px;
    display: flex;
    justify-content: center;
    margin-right: 10px;

    .icon {
      font-size: 18px;
    }
  }

  .input {
    flex: 1;
    height: 100%;
    font-size: 15px;
    color: #333;
  }

  .placeholder {
    color: #bbb;
  }
}

/* 按钮样式 */
.btn-submit {
  background-color: #d4a373; /* 咖啡主题色 */
  color: #fff;
  border-radius: 12px;
  height: 50px;
  line-height: 50px;
  font-size: 16px;
  font-weight: bold;
  margin-top: 30px;
  border: none;

  &::after {
    border: none;
  }
}

.btn-hover {
  opacity: 0.9;
  transform: scale(0.98);
}

/* 底部切换按钮样式 */
.toggle-box {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;

  .toggle-text {
    color: #999;
  }

  .toggle-btn {
    color: #d4a373;
    margin-left: 5px;
    font-weight: bold;
  }
}

/* 登录方式 Tab 样式 */
.login-tabs {
  display: flex;
  justify-content: center;
  margin-bottom: 25px;

  .tab-item {
    font-size: 16px;
    color: #999;
    padding-bottom: 5px;
    margin: 0 15px;
    position: relative;
    transition: all 0.3s;

    &.active {
      color: #333;
      font-weight: bold;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 20px;
        height: 3px;
        background-color: #d4a373;
        border-radius: 2px;
      }
    }
  }
}

/* 验证码按钮样式 */
.code-btn {
  font-size: 14px;
  color: #d4a373;
  padding: 5px 10px;
  margin-right: 5px;
  font-weight: bold;

  &.disabled {
    color: #ccc;
  }

  &:active {
    opacity: 0.7;
  }
}
</style>