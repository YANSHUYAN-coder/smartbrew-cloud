<template>
  <div>
    <el-card>
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="growth" label="成长值" />
        <el-table-column prop="integration" label="积分" />
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
        <p><strong>成长值:</strong> {{ currentRow.growth }}</p>
        <p><strong>积分:</strong> {{ currentRow.integration }}</p>
        <p><strong>注册时间:</strong> {{ currentRow.createTime }}</p>
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMemberList, updateMemberStatus } from '@/api/member'

interface Member {
  id: number
  username: string
  phone: string
  growth: number
  integration: number
  status: number
  createTime: string
}

const loading = ref(false)
const tableData = ref<Member[]>([])
const dialogVisible = ref(false)
const currentRow = ref<Member | null>(null)

const getList = () => {
    loading.value = true
    getMemberList({}).then((res: any) => {
        tableData.value = res
        loading.value = false
    })
}

onMounted(() => {
    getList()
})

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
    updateMemberStatus({ id: row.id, status: row.status === 1 ? 0 : 1 }).then(() => {
        ElMessage.success(`${action}成功`)
        getList()
    })
  })
}
</script>
