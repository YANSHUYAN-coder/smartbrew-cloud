<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="title">
        <h2>SmartBrew Cloud</h2>
        <p>智咖云管理后台</p>
      </div>
      <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-width="0">
        <el-form-item prop="phone">
          <el-input v-model="loginForm.phone" placeholder="手机号" prefix-icon="Iphone" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" @click="handleLogin" :loading="loading">
            登录
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
  phone: '', // Changed from username to phone to match backend
  password: ''
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
    localStorage.setItem('token', res.token) // Store token
    // Store user info if needed
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
  background-color: #2d3a4b;
}
.login-card {
  width: 400px;
}
.title {
  text-align: center;
  margin-bottom: 30px;
}
</style>
