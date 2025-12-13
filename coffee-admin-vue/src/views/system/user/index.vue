<template>
  <div class="user-container">
    <el-card>
      <div class="header-actions">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增用户</el-button>
      </div>

      <el-table :data="safeUserList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="status" label="状态">
           <template #default="scope">
             <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
               {{ scope.row.status === 1 ? '正常' : '禁用' }}
             </el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
        <el-pagination
          v-model:current-page="pageParam.page"
          v-model:page-size="pageParam.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog :title="dialog.title" v-model="dialog.visible" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="密码" v-if="!form.id">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <!-- Roles selection is not yet fully implemented in backend for creating user via this API -->
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser } from '@/api/system/user'

interface User {
  id?: number
  username: string
  phone: string
  status: number
  createTime?: string
}

const loading = ref(false)
const userList = ref<User[]>([])
const total = ref(0)
const pageParam = ref({
  page: 1,
  pageSize: 10
})

// 确保表格数据始终是数组
const safeUserList = computed(() => {
  return Array.isArray(userList.value) ? userList.value : []
})

const dialog = reactive({
  visible: false,
  title: ''
})

const form = reactive({
  id: undefined,
  username: '',
  phone: '',
  password: ''
})

const getList = () => {
    loading.value = true
    getUserList({
      page: pageParam.value.page,
      pageSize: pageParam.value.pageSize
    }).then((res: any) => {
        if (res && res.records && Array.isArray(res.records)) {
            userList.value = res.records
            total.value = res.total || 0
        } else if (Array.isArray(res)) {
            // 兼容旧接口，直接返回数组的情况
            userList.value = res
            total.value = res.length
        } else {
            userList.value = []
            total.value = 0
        }
        loading.value = false
    }).catch((err) => {
        console.error('获取用户列表失败:', err)
        loading.value = false
        userList.value = []
        total.value = 0
    })
}

const handleSizeChange = (size: number) => {
  pageParam.value.pageSize = size
  pageParam.value.page = 1
  getList()
}

const handleCurrentChange = (page: number) => {
  pageParam.value.page = page
  getList()
}

onMounted(() => {
    getList()
})

const handleAdd = () => {
  dialog.title = '新增用户'
  dialog.visible = true
  Object.assign(form, { id: undefined, username: '', phone: '', password: '' })
}

const handleEdit = (row: any) => {
  dialog.title = '编辑用户'
  dialog.visible = true
  Object.assign(form, row)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该用户吗?', '警告').then(() => {
      deleteUser(row.id).then(() => {
          ElMessage.success('删除成功')
          getList()
      })
  })
}

const handleSubmit = () => {
  if (form.id) {
      updateUser(form).then(() => {
          ElMessage.success('更新成功')
          dialog.visible = false
          getList()
      })
  } else {
      createUser(form).then(() => {
          ElMessage.success('创建成功')
          dialog.visible = false
          getList()
      })
  }
}
</script>

<style scoped>
.header-actions {
  margin-bottom: 20px;
}
</style>
