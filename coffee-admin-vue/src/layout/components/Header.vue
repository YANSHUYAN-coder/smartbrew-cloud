<template>
  <div class="header-right">
    <el-dropdown>
      <span class="el-dropdown-link">
        管理员 <el-icon class="el-icon--right"><arrow-down /></el-icon>
      </span>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item>个人中心</el-dropdown-item>
          <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const handleLogout = () => {
  // 清除本地存储的token和用户信息
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  // 跳转到登录页
  router.push('/login')
}
</script>

<style scoped>
.header-right {
  margin-right: 20px;
  cursor: pointer;
}
.el-dropdown-link {
  display: flex;
  align-items: center;
}
</style>

