<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>AI 知识库维护</span>
          <el-button type="primary" :loading="syncLoading" @click="handleSync">
            同步数据库商品
          </el-button>
        </div>
      </template>

      <div style="margin-bottom: 20px">
        <el-alert
          title="上传说明"
          type="info"
          description="支持 CSV、PDF、Word (.doc/.docx)、Markdown (.md) 和 TXT 格式。CSV 第一列为『问题』，第二列为『答案』。长文档会自动切分并存入向量数据库。"
          show-icon
          :closable="false"
        />
      </div>

      <div class="upload-section">
        <el-upload
          class="upload-demo"
          drag
          :action="uploadUrl"
          :headers="headers"
          name="file"
          accept=".csv,.pdf,.doc,.docx,.md,.txt"
          :on-success="handleSuccess"
          :on-error="handleError"
          :before-upload="beforeUpload"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或 <em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持 CSV, PDF, Word, MD, TXT，大小不超过 10MB
            </div>
          </template>
        </el-upload>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// 状态
const syncLoading = ref(false)

// 构造上传地址
const uploadUrl = computed(() => {
  // 优先使用环境变量，如果没有则默认使用 /api
  const baseApi = import.meta.env.VITE_APP_BASE_API || '/api'
  return baseApi + '/system/ai/importData'
})

// 获取 Token
const headers = {
  Authorization: 'Bearer ' + localStorage.getItem('token')
}

const handleSync = () => {
  ElMessageBox.confirm(
    '同步将把数据库中的所有上架商品及其规格信息更新到 AI 知识库，确定继续吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    syncLoading.value = true
    try {
      const res = await request({
        url: '/system/ai/syncDatabase',
        method: 'post'
      })
      // request.ts 拦截器已经解包了 res.data，如果没报错说明 code=200
      // 这里的 res 就是后端返回的 message 字符串
      ElMessage.success(res || '同步成功')
    } catch (error) {
      console.error('同步请求失败', error)
    } finally {
      syncLoading.value = false
    }
  })
}

const beforeUpload = (file) => {
  const allowedTypes = ['.csv', '.pdf', '.doc', '.docx', '.md', '.txt'];
  const extension = file.name.substring(file.name.lastIndexOf('.')).toLowerCase();
  const isAllowed = allowedTypes.includes(extension);
  
  if (!isAllowed) {
    ElMessage.error('不支持该文件格式!');
    return false;
  }
  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    ElMessage.error('上传文件大小不能超过 10MB!');
    return false;
  }
  return true;
}

const handleSuccess = (res) => {
  if (res.code === 200) {
    ElMessage.success(res.data || '数据导入成功！');
  } else {
    ElMessage.error(res.message || '导入失败');
  }
}

const handleError = () => {
  ElMessage.error('服务器响应异常，请检查网络或后端日志');
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.upload-section {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
.upload-demo {
  width: 100%;
  max-width: 600px;
}
</style>

