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
      <!-- 账号输入 -->
      <view class="input-group">
        <view class="icon-box">
          <text class="icon">👤</text> <!-- 实际项目中可用 iconfont 或 image -->
        </view>
        <input 
          class="input" 
          type="text" 
          v-model="form.username" 
          placeholder="请输入用户名/手机号" 
          placeholder-class="placeholder" 
        />
      </view>
      
      <!-- 密码输入 -->
      <view class="input-group">
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

<script>
import { post, request } from '@/utils/request.js'
import { useUserStore } from '@/store/user.js'

export default {
  data() {
    return {
      isLogin: true, // true 为登录模式, false 为注册模式
      form: {
        username: '13800138000', // 实际作为手机号 phone 使用
        password: '123456',
        confirmPassword: ''
      }
    }
  },
  methods: {
    // 切换 登录/注册 模式
    toggleMode() {
      this.isLogin = !this.isLogin;
      // 切换时清空表单，提升体验
      this.form = {
        username: '',
        password: '',
        confirmPassword: ''
      };
    },
    
    // 提交表单
    async handleSubmit() {
      // 1. 基础校验
      if (!this.form.username || !this.form.password) {
        uni.showToast({ title: '请填写完整信息', icon: 'none' });
        return;
      }
      
      // 2. 注册模式下的密码确认校验
      if (!this.isLogin && this.form.password !== this.form.confirmPassword) {
        uni.showToast({ title: '两次密码输入不一致', icon: 'none' });
        return;
      }

      // 3. 调用后端接口
      if (this.isLogin) {
        await this.handleLogin();
      } else {
        await this.handleRegister();
      }
    },

    // 调用 /auth/login
    async handleLogin() {
      const userStore = useUserStore()
      try {
        uni.showLoading({ title: '登录中...' });
        // 后端使用 phone + password 登录，这里把用户名字段当作手机号传递
        const res = await post('/auth/login', {
          phone: this.form.username,
          password: this.form.password
        });

        // 根据 request.js 的拦截器，这里的 res 已经是 data.data，期待结构为：
        // { token, refreshToken, user }
        const { token, refreshToken, user } = res || {}

        // 先保存 token / refreshToken / 用户信息（需要用于后续请求与刷新）
        userStore.setUser({
          token: token || '',
          refreshToken: refreshToken || '',
          userInfo: user || null
        });

        // 登录成功后，立即获取完整的用户信息并更新 store
        // 这样可以确保 store 中的数据是最新且完整的，统一数据来源
        try {
          const userInfoRes = await request({
            url: '/app/member/info',
            method: 'GET'
          })
          if (userInfoRes.data) {
            userStore.setUser(res.token, userInfoRes.data)
          }
        } catch (e) {
          console.warn('获取用户详细信息失败，使用登录接口返回的数据', e)
          // 如果获取详细信息失败，使用登录接口返回的基础信息也是可以的
        }

        uni.hideLoading();
        uni.showToast({ title: '登录成功', icon: 'success' });

        // 触发登录成功事件，通知 App.vue 连接 WebSocket
        uni.$emit('userLoginSuccess')

        // 短暂停留后跳转到首页或个人中心
        setTimeout(() => {
          uni.switchTab({ url: '/pages/home/index' });
        }, 800);
      } catch (e) {
        uni.hideLoading();
        // 错误提示已在拦截器里弹出，这里再兜底一下
        // console.error('登录失败:', e);
      }
    },

    // 调用 /auth/register
    async handleRegister() {
      try {
        uni.showLoading({ title: '注册中...' });
        const res = await post('/auth/register', {
          phone: this.form.username,
          password: this.form.password
        });

        uni.hideLoading();

        // 注册成功，后端也会返回 token + user，这里简单提示后切回登录模式
        uni.showToast({ title: '注册成功，请登录', icon: 'success' });

        // 自动切换回登录模式，并保留刚输入的手机号
        this.isLogin = true;
        this.form.password = '';
        this.form.confirmPassword = '';
      } catch (e) {
        uni.hideLoading();
        // console.error('注册失败:', e);
      }
    }
  }
}
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
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
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
</style>