<template>
  <div class="login-container">
    <div class="login-mask"></div>
    <el-card class="login-card">
      <div class="title">
        <div class="logo-wrapper">
          <span class="coffee-icon">☕</span>
        </div>
        <h2>智咖·云 Admin</h2>
        <p>智能零售管理系统</p>
      </div>
      <el-form ref="loginFormRef" :model="loginForm" :rules="rules" size="large">
        <el-form-item prop="phone">
          <el-input 
            v-model="loginForm.phone" 
            placeholder="手机号" 
            prefix-icon="Iphone" 
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="密码" 
            prefix-icon="Lock" 
            show-password 
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-button" @click="handleLogin" :loading="loading">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/login'

const router = useRouter()
const loading = ref(false)

const loginForm = reactive({
  phone: '1380013802',
  password: '123456'
})

const rules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = () => {
  loading.value = true
  login(loginForm).then((res: any) => {
    loading.value = false
    ElMessage.success('登录成功')
    localStorage.setItem('token', res.token)
    if (res.refreshToken) {
      localStorage.setItem('refreshToken', res.refreshToken)
    }
    localStorage.setItem('user', JSON.stringify(res.user))
    router.push('/')
  }).catch(() => {
    loading.value = false
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url('https://images.unsplash.com/photo-1497935586351-b67a49e012bf?q=80&w=1920&auto=format&fit=crop');
  background-size: cover;
  background-position: center;
  position: relative;
}

.login-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5); /* Dark overlay */
  z-index: 1;
}

.login-card {
  width: 420px;
  padding: 20px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  z-index: 2;
  border: none;
}

.title {
  text-align: center;
  margin-bottom: 30px;
  color: #3e2723; /* Dark coffee color */
}

.title h2 {
  font-size: 28px;
  margin: 10px 0;
  font-weight: 600;
}

.title p {
  color: #795548;
  font-size: 14px;
  margin: 0;
}

.logo-wrapper {
  margin-bottom: 10px;
}

.coffee-icon {
  font-size: 48px;
}

.login-button {
  width: 100%;
  background-color: #6d4c41; /* Coffee color */
  border-color: #6d4c41;
  font-size: 16px;
  letter-spacing: 4px;
}

.login-button:hover {
  background-color: #5d4037;
  border-color: #5d4037;
}

:deep(.el-input__wrapper) {
  background-color: #f5f5f5;
  box-shadow: none !important;
  border: 1px solid #dcdfe6;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #8d6e63;
}
</style>
