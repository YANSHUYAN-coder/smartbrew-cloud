<template>
  <div class="menu-container">
    <el-card>
      <div class="header-actions">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增菜单</el-button>
      </div>

      <el-table
        :data="menuList"
        style="width: 100%; margin-bottom: 20px;"
        row-key="id"
        border
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="title" label="菜单名称" width="180" />
        <el-table-column prop="icon" label="图标" width="100">
          <template #default="scope">
            <el-icon v-if="scope.row.icon">
              <component :is="scope.row.icon" />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="orderNum" label="排序" width="80" />
        <el-table-column prop="perms" label="权限标识" />
        <el-table-column prop="path" label="组件路径" />
        <el-table-column prop="hidden" label="可见" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.hidden ? 'info' : 'success'">{{ scope.row.hidden ? '隐藏' : '显示' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="primary" @click="handleAdd(scope.row)">新增下级</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Dialog -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="600px">
      <el-form ref="menuFormRef" :model="form" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="menuOptions"
            :props="{ value: 'id', label: 'title', children: 'children' }"
            value-key="id"
            placeholder="选择上级菜单"
            check-strictly
          />
        </el-form-item>
        <el-form-item label="菜单类型">
          <el-radio-group v-model="form.type">
            <el-radio :label="0">目录</el-radio>
            <el-radio :label="1">菜单</el-radio>
            <el-radio :label="2">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="权限标识" v-if="form.type !== 0">
           <el-input v-model="form.perms" placeholder="如: system:user:list" />
        </el-form-item>
        <el-form-item label="路由地址" v-if="form.type !== 2">
           <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="图标" v-if="form.type !== 2">
           <el-input v-model="form.icon" />
        </el-form-item>
         <el-form-item label="显示排序">
           <el-input-number v-model="form.orderNum" :min="0" />
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
import { ElMessage } from 'element-plus'

// Mock Data
const menuList = ref([
  {
    id: 1,
    parentId: 0,
    title: '系统管理',
    path: '/system',
    perms: '',
    type: 0,
    icon: 'Setting',
    orderNum: 1,
    hidden: false,
    children: [
      { id: 11, parentId: 1, title: '用户管理', path: 'user', perms: 'system:user:list', type: 1, icon: 'User', orderNum: 1, children: [] },
      { id: 12, parentId: 1, title: '角色管理', path: 'role', perms: 'system:role:list', type: 1, icon: 'UserFilled', orderNum: 2, children: [] },
      { id: 13, parentId: 1, title: '菜单管理', path: 'menu', perms: 'system:menu:list', type: 1, icon: 'Menu', orderNum: 3, children: [] },
    ]
  },
  {
    id: 2,
    parentId: 0,
    title: '商品管理',
    path: '/product',
    perms: '',
    type: 0,
    icon: 'Goods',
    orderNum: 2,
    children: [
      { id: 21, parentId: 2, title: '商品列表', path: 'list', perms: 'pms:product:list', type: 1, icon: '', orderNum: 1, children: [] },
      { id: 22, parentId: 2, title: '发布商品', path: 'add', perms: 'pms:product:create', type: 1, icon: '', orderNum: 2, children: [] },
    ]
  }
])

const menuOptions = ref<any[]>([])

const dialog = reactive({
  visible: false,
  title: ''
})

const form = reactive({
  id: undefined,
  parentId: 0,
  type: 1,
  title: '',
  perms: '',
  path: '',
  icon: '',
  orderNum: 0
})

const handleAdd = (row?: any) => {
  dialog.title = '新增菜单'
  dialog.visible = true
  form.parentId = row?.id || 0
  // Build options for tree select (simplified)
  menuOptions.value = [{ id: 0, title: '主目录', children: menuList.value }]
}

const handleEdit = (row: any) => {
  dialog.title = '编辑菜单'
  dialog.visible = true
  Object.assign(form, row)
  menuOptions.value = [{ id: 0, title: '主目录', children: menuList.value }]
}

const handleDelete = (row: any) => {
  ElMessage.confirm('确定删除该菜单?', '警告').then(() => {
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

