<template>
  <div class="role-container">
    <el-card>
      <div class="header-actions">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增角色</el-button>
      </div>

      <el-table :data="safeRoleList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="角色名称" width="150" />
        <el-table-column prop="description" label="描述"/>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                    {{ scope.row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
            </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="200"/>
        <el-table-column prop="updateTime" label="修改时间" width="200"/>
        <el-table-column label="操作" width="280">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="primary" icon="CircleCheck" @click="handleAuth(scope.row)">权限分配</el-button>
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
        <div class="permission-tree-container">
            <el-tree
                ref="menuTreeRef"
                :data="menuData"
                show-checkbox
                node-key="id"
                :props="{ label: 'name', children: 'children' }" 
            />
        </div>
        <template #footer>
             <el-button @click="permDialog.visible = false">取消</el-button>
             <el-button type="primary" @click="handlePermSubmit">确定</el-button>
        </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, createRole, updateRole, deleteRole, getRoleMenuIds, updateRoleMenus } from '@/api/system/role'
import { getMenuList } from '@/api/system/menu'

interface Role {
  id?: number
  name: string
  description?: string
  status: number
  createTime?: string
}

const loading = ref(false)
const roleList = ref<Role[]>([])
const menuData = ref<any[]>([])
const total = ref(0)
const pageParam = ref({
  page: 1,
  pageSize: 10
})

// 确保表格数据始终是数组
const safeRoleList = computed(() => {
  return Array.isArray(roleList.value) ? roleList.value : []
})

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
    getRoleList({
      page: pageParam.value.page,
      pageSize: pageParam.value.pageSize
    }).then((res: any) => {
        if (res && res.records && Array.isArray(res.records)) {
            roleList.value = res.records
            total.value = res.total || 0
        } else if (Array.isArray(res)) {
            // 兼容旧接口，直接返回数组的情况
            roleList.value = res
            total.value = res.length
        } else {
            roleList.value = []
            total.value = 0
        }
        loading.value = false
    }).catch((err) => {
        console.error('获取角色列表失败:', err)
        loading.value = false
        roleList.value = []
        total.value = 0
    })
}

// 加载所有菜单供选择
const loadMenus = () => {
    // 获取所有菜单，不进行分页
    getMenuList({
      page: 1,
      pageSize: 1000  // 获取足够多的菜单项
    }).then((res: any) => {
        // 处理分页返回的数据
        if (res && res.records && Array.isArray(res.records)) {
            menuData.value = res.records
        } else if (Array.isArray(res)) {
            menuData.value = res
        } else {
            menuData.value = []
        }
    }).catch(() => {
        menuData.value = []
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

.permission-tree-container {
  max-height: 400px;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #fff;
}

/* 自定义滚动条样式 */
.permission-tree-container::-webkit-scrollbar {
  width: 8px;
}

.permission-tree-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.permission-tree-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.permission-tree-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
