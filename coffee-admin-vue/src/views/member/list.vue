<template>
  <div>
    <el-card>
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="memberLevel" label="会员等级">
          <template #default="scope">
            <el-tag v-if="scope.row.memberLevel === 1" type="info">银牌</el-tag>
            <el-tag v-else-if="scope.row.memberLevel === 2" type="warning">金牌</el-tag>
            <el-tag v-else type="danger">钻石</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button link type="primary" @click="handleDetail(scope.row)">详情</el-button>
            <el-button link :type="scope.row.status === 1 ? 'danger' : 'success'" @click="handleStatus(scope.row)">
              {{ scope.row.status === 1 ? '封号' : '解封' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="会员详情" width="30%">
      <div v-if="currentRow">
        <p><strong>ID:</strong> {{ currentRow.id }}</p>
        <p><strong>用户名:</strong> {{ currentRow.username }}</p>
        <p><strong>手机号:</strong> {{ currentRow.phone }}</p>
        <p><strong>成长值:</strong> 1200</p>
        <p><strong>积分:</strong> 350</p>
        <p><strong>注册时间:</strong> 2023-10-01</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

interface Member {
  id: number
  username: string
  phone: string
  memberLevel: number
  status: number // 1: normal, 0: banned
}

const tableData = ref<Member[]>([
  { id: 1001, username: 'UserA', phone: '13800138000', memberLevel: 1, status: 1 },
  { id: 1002, username: 'UserB', phone: '13900139000', memberLevel: 3, status: 1 },
  { id: 1003, username: 'BadGuy', phone: '13700137000', memberLevel: 1, status: 0 },
])

const dialogVisible = ref(false)
const currentRow = ref<Member | null>(null)

const handleDetail = (row: Member) => {
  currentRow.value = row
  dialogVisible.value = true
}

const handleStatus = (row: Member) => {
  const action = row.status === 1 ? '封号' : '解封'
  ElMessageBox.confirm(`确认对该用户进行${action}操作吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.success(`${action}成功`)
  })
}
</script>

