<template>
  <div class="user-container">
    <el-card>
      <div class="header-actions">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增用户</el-button>
      </div>

      <el-table :data="userList" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickName" label="昵称" />
        <el-table-column prop="roleName" label="角色" />
        <el-table-column prop="status" label="状态">
           <template #default="scope">
             <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" />
           </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="text" icon="Key" @click="handleResetPwd(scope.row)">重置密码</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="dialog.title" v-model="dialog.visible" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickName" />
        </el-form-item>
        <el-form-item label="密码" v-if="!form.id">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item label="角色">
           <el-select v-model="form.roleId" placeholder="请选择角色">
             <el-option label="超级管理员" :value="1" />
             <el-option label="店长" :value="2" />
             <el-option label="咖啡师" :value="3" />
           </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const userList = ref([
  { id: 1, username: 'admin', nickName: '系统管理员', roleName: '超级管理员', status: 1, createTime: '2023-10-01' },
  { id: 2, username: 'manager', nickName: '张店长', roleName: '店长', status: 1, createTime: '2023-10-05' },
])

const dialog = reactive({
  visible: false,
  title: ''
})

const form = reactive({
  id: undefined,
  username: '',
  nickName: '',
  password: '',
  roleId: undefined,
  status: 1
})

const handleAdd = () => {
  dialog.title = '新增用户'
  dialog.visible = true
  Object.assign(form, { id: undefined, username: '', nickName: '', password: '', roleId: undefined, status: 1 })
}

const handleEdit = (row: any) => {
  dialog.title = '编辑用户'
  dialog.visible = true
  Object.assign(form, row)
  // map role name back to id for mock
  form.roleId = row.roleName === '超级管理员' ? 1 : 2
}

const handleResetPwd = (row: any) => {
  ElMessageBox.prompt('请输入新密码', '重置密码', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  }).then(({ value }) => {
    ElMessage.success('密码重置成功')
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该用户吗?', '警告').then(() => {
    ElMessage.success('删除成功')
  })
}

const handleSubmit = () => {
  ElMessage.success('保存成功')
  dialog.visible = false
}
</script>

<style scoped>
.header-actions {
  margin-bottom: 20px;
}
</style>

