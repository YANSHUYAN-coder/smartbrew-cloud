<template>
  <div class="role-container">
    <el-card>
      <div class="header-actions">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增角色</el-button>
      </div>

      <el-table :data="roleList" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleKey" label="权限字符" width="150" />
        <el-table-column prop="sort" label="显示顺序" width="100" />
        <el-table-column prop="status" label="状态" width="100">
           <template #default="scope">
             <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" />
           </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="primary" icon="CircleCheck" @click="handleAuth(scope.row)">数据权限</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Role Form Dialog -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="角色名称">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="权限字符">
          <el-input v-model="form.roleKey" placeholder="如: admin" />
        </el-form-item>
        <el-form-item label="显示顺序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单权限">
           <el-tree
            ref="menuTreeRef"
            :data="menuData"
            show-checkbox
            node-key="id"
            :props="{ label: 'title', children: 'children' }"
           />
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
import { ref, reactive, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const roleList = ref([
  { id: 1, roleName: '超级管理员', roleKey: 'admin', sort: 1, status: 1, createTime: '2023-10-01' },
  { id: 2, roleName: '店长', roleKey: 'shop_manager', sort: 2, status: 1, createTime: '2023-10-02' },
  { id: 3, roleName: '咖啡师', roleKey: 'barista', sort: 3, status: 1, createTime: '2023-10-03' }
])

// Mock Menu Data for Tree
const menuData = ref([
  { id: 1, title: '系统管理', children: [
    { id: 11, title: '用户管理' },
    { id: 12, title: '角色管理' },
    { id: 13, title: '菜单管理' }
  ]},
  { id: 2, title: '商品管理', children: [
    { id: 21, title: '商品列表' },
    { id: 22, title: '发布商品' }
  ]},
  { id: 3, title: '订单管理', children: [
    { id: 31, title: '订单列表' }
  ]}
])

const dialog = reactive({
  visible: false,
  title: ''
})

const form = reactive({
  id: undefined,
  roleName: '',
  roleKey: '',
  sort: 0,
  status: 1,
  menuIds: [] as number[]
})

const menuTreeRef = ref()

const handleAdd = () => {
  dialog.title = '新增角色'
  dialog.visible = true
  Object.assign(form, { id: undefined, roleName: '', roleKey: '', sort: 0, status: 1, menuIds: [] })
  nextTick(() => {
    menuTreeRef.value?.setCheckedKeys([])
  })
}

const handleEdit = (row: any) => {
  dialog.title = '编辑角色'
  dialog.visible = true
  Object.assign(form, row)
  // Mock checked keys
  nextTick(() => {
    menuTreeRef.value?.setCheckedKeys([11, 21]) 
  })
}

const handleAuth = (row: any) => {
  ElMessage.info('数据权限配置开发中')
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该角色吗?', '警告').then(() => {
    ElMessage.success('删除成功')
  })
}

const handleSubmit = () => {
  const checkedKeys = menuTreeRef.value?.getCheckedKeys()
  console.log('Selected permissions:', checkedKeys)
  ElMessage.success('保存成功')
  dialog.visible = false
}
</script>

<style scoped>
.header-actions {
  margin-bottom: 20px;
}
</style>

