<template>
  <div class="menu-container">
    <el-card>
      <div class="header-actions">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增权限</el-button>
      </div>

      <el-table
        :data="menuList"
        style="width: 100%; margin-bottom: 20px;"
        row-key="id"
        border
      >
        <el-table-column prop="name" label="权限名称" width="180" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="url" label="接口路径" />
        <el-table-column prop="method" label="请求方法" width="100" />
        
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Dialog -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="600px">
      <el-form ref="menuFormRef" :model="form" label-width="100px">
        <el-form-item label="权限名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="权限描述">
           <el-input v-model="form.description" />
        </el-form-item>
        <el-form-item label="接口路径">
           <el-input v-model="form.url" />
        </el-form-item>
        <el-form-item label="请求方法">
           <el-select v-model="form.method">
               <el-option label="GET" value="GET" />
               <el-option label="POST" value="POST" />
               <el-option label="PUT" value="PUT" />
               <el-option label="DELETE" value="DELETE" />
           </el-select>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuList, createMenu, updateMenu, deleteMenu } from '@/api/system/menu'

const loading = ref(false)
const menuList = ref([])

const dialog = reactive({
  visible: false,
  title: ''
})

const form = reactive({
  id: undefined,
  name: '',
  description: '',
  url: '',
  method: ''
})

const getList = () => {
    loading.value = true
    getMenuList().then((res: any) => {
        menuList.value = res
        loading.value = false
    })
}

onMounted(() => {
    getList()
})

const handleAdd = () => {
  dialog.title = '新增权限'
  dialog.visible = true
  Object.assign(form, { id: undefined, name: '', description: '', url: '', method: '' })
}

const handleEdit = (row: any) => {
  dialog.title = '编辑权限'
  dialog.visible = true
  Object.assign(form, row)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定删除该权限?', '警告').then(() => {
    deleteMenu(row.id).then(() => {
        ElMessage.success('删除成功')
        getList()
    })
  })
}

const handleSubmit = () => {
    if (form.id) {
        updateMenu(form).then(() => {
            ElMessage.success('更新成功')
            dialog.visible = false
            getList()
        })
    } else {
        createMenu(form).then(() => {
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
