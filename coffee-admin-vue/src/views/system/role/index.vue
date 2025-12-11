<template>
  <div class="role-container">
    <el-card>
      <div class="header-actions">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增角色</el-button>
      </div>

      <el-table :data="roleList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" width="150" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="primary" icon="CircleCheck" @click="handleAuth(scope.row)">权限分配</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Role Form Dialog -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="角色名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
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

    <!-- Permission Dialog -->
    <el-dialog title="权限分配" v-model="permDialog.visible" width="500px">
        <el-tree
            ref="menuTreeRef"
            :data="menuData"
            show-checkbox
            node-key="id"
            :props="{ label: 'name', children: 'children' }" 
        />
        <template #footer>
             <el-button @click="permDialog.visible = false">取消</el-button>
             <el-button type="primary" @click="handlePermSubmit">确定</el-button>
        </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, createRole, updateRole, deleteRole, getRoleMenuIds, updateRoleMenus } from '@/api/system/role'
import { getMenuList } from '@/api/system/menu'

const loading = ref(false)
const roleList = ref([])
const menuData = ref([])

const dialog = reactive({
  visible: false,
  title: ''
})

const permDialog = reactive({
    visible: false,
    roleId: 0
})

const form = reactive({
  id: undefined,
  name: '',
  description: '',
  status: 1
})

const menuTreeRef = ref()

const getList = () => {
    loading.value = true
    getRoleList({}).then((res: any) => {
        roleList.value = res.records
        loading.value = false
    })
}

// 加载所有菜单供选择
const loadMenus = () => {
    getMenuList().then((res: any) => {
        // Backend returns flat list, tree component can handle flat list if configured or we transform it.
        // Assuming flat list for now, we might need a transform function if backend returns flat list but we want tree.
        // For now, let's just display flat list or assume backend returned tree if I implemented tree.
        // I implemented flat list in backend. So let's just use it.
        // But el-tree expects tree structure for hierarchy.
        // Quick fix: list is flat, so just one level.
        menuData.value = res
    })
}

onMounted(() => {
    getList()
    loadMenus()
})

const handleAdd = () => {
  dialog.title = '新增角色'
  dialog.visible = true
  Object.assign(form, { id: undefined, name: '', description: '', status: 1 })
}

const handleEdit = (row: any) => {
  dialog.title = '编辑角色'
  dialog.visible = true
  Object.assign(form, row)
}

const handleAuth = (row: any) => {
    permDialog.roleId = row.id
    permDialog.visible = true
    getRoleMenuIds(row.id).then((res: any) => {
        nextTick(() => {
            menuTreeRef.value?.setCheckedKeys(res)
        })
    })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该角色吗?', '警告').then(() => {
      deleteRole(row.id).then(() => {
          ElMessage.success('删除成功')
          getList()
      })
  })
}

const handleSubmit = () => {
    if (form.id) {
        updateRole(form).then(() => {
            ElMessage.success('更新成功')
            dialog.visible = false
            getList()
        })
    } else {
        createRole(form).then(() => {
            ElMessage.success('创建成功')
            dialog.visible = false
            getList()
        })
    }
}

const handlePermSubmit = () => {
    const checkedKeys = menuTreeRef.value?.getCheckedKeys()
    updateRoleMenus(permDialog.roleId, checkedKeys).then(() => {
        ElMessage.success('权限分配成功')
        permDialog.visible = false
    })
}
</script>

<style scoped>
.header-actions {
  margin-bottom: 20px;
}
</style>
