<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>分类列表</span>
          <el-button type="primary" @click="openCategoryDialog">
            <el-icon style="margin-right: 5px"><Plus /></el-icon>新增分类
          </el-button>
        </div>
      </template>

      <!-- 分类表格 -->
      <el-table :data="safeTableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="分类名称" width="150"></el-table-column>
        <el-table-column label="图标" width="100">
          <template #default="scope">
            <el-image 
              v-if="scope.row.icon" 
              style="width: 40px; height: 40px; border-radius: 4px;" 
              :src="scope.row.icon" 
              fit="cover">
            </el-image>
            <span v-else>无图标</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序权重" width="100">
          <template #default="scope">
            <el-tag type="info">{{ scope.row.sort || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-switch 
                v-model="scope.row.status" 
                :active-value="1" 
                :inactive-value="0"
                @change="handleStatusChange(scope.row)">
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="editCategory(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 分类编辑/新增弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="500px">
        <el-form :model="form" label-width="100px">
            <el-form-item label="分类名称" required>
                <el-input v-model="form.name" placeholder="例如：咖啡"></el-input>
            </el-form-item>
            <el-form-item label="分类图标">
                <el-input v-model="form.icon" placeholder="输入图标URL"></el-input>
            </el-form-item>
            <el-form-item label="排序权重">
                <el-input-number 
                    v-model="form.sort" 
                    :min="0" 
                    :max="999" 
                    controls-position="right"
                    style="width: 100%">
                </el-input-number>
                <div style="color: #909399; font-size: 12px; margin-top: 5px;">
                  数值越大，排序越靠前
                </div>
            </el-form-item>
            <el-form-item label="状态">
                <el-radio-group v-model="form.status">
                    <el-radio :label="1">启用</el-radio>
                    <el-radio :label="0">禁用</el-radio>
                </el-radio-group>
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitCategory">确认保存</el-button>
            </span>
        </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getCategoryList, 
  updateCategoryStatus, 
  createCategory, 
  updateCategory, 
  deleteCategory 
} from '@/api/category'

interface Category {
  id: number | null
  name: string
  icon: string
  sort: number
  status: number // 1: 启用, 0: 禁用
  createTime?: string
  updateTime?: string
}

const loading = ref(false)
const tableData = ref<Category[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const total = ref(0)
const pageParam = ref({
  page: 1,
  pageSize: 10
})

// 确保表格数据始终是数组
const safeTableData = computed(() => {
  return Array.isArray(tableData.value) ? tableData.value : []
})

const form = reactive<Category>({
  id: null,
  name: '',
  icon: '',
  sort: 0,
  status: 1
})

const getList = () => {
  loading.value = true
  getCategoryList({
    page: pageParam.value.page,
    pageSize: pageParam.value.pageSize
  }).then((res: any) => {
    if (res && res.records && Array.isArray(res.records)) {
      tableData.value = res.records
      total.value = res.total || 0
    } else if (Array.isArray(res)) {
      tableData.value = res
      total.value = res.length
    } else {
      tableData.value = []
      total.value = 0
    }
    loading.value = false
  }).catch((err) => {
    console.error('获取分类列表失败:', err)
    loading.value = false
    tableData.value = []
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

const openCategoryDialog = () => {
  isEdit.value = false
  Object.assign(form, { 
    id: null, 
    name: '', 
    icon: '', 
    sort: 0, 
    status: 1 
  })
  dialogVisible.value = true
}

const editCategory = (row: Category) => {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const submitCategory = () => {
  if (!form.name || form.name.trim() === '') {
    ElMessage.warning('请输入分类名称')
    return
  }
  
  loading.value = true
  if (isEdit.value) {
    updateCategory(form).then(() => {
      ElMessage.success('分类修改成功')
      loading.value = false
      dialogVisible.value = false
      getList()
    }).catch(() => {
      loading.value = false
      ElMessage.error('分类修改失败')
    })
  } else {
    createCategory(form).then(() => {
      ElMessage.success('分类添加成功')
      loading.value = false
      dialogVisible.value = false
      getList()
    }).catch(() => {
      loading.value = false
      ElMessage.error('分类添加失败')
    })
  }
}

const handleStatusChange = (row: Category) => {
  updateCategoryStatus({ id: row.id, status: row.status }).then(() => {
    ElMessage.success(`分类 ${row.name} 已${row.status === 1 ? '启用' : '禁用'}`)
  }).catch(() => {
    // Revert on error
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  })
}

const handleDelete = (row: Category) => {
  ElMessageBox.confirm('确认删除该分类吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    deleteCategory(row.id!).then(() => {
      ElMessage.success('删除成功')
      getList()
    }).catch(() => {
      ElMessage.error('删除失败')
    })
  })
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

