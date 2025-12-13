<template>
  <div class="member-level-container">
    <el-card>
      <div class="header-actions">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增等级</el-button>
      </div>

      <el-table
        :data="safeLevelList"
        style="width: 100%; margin-bottom: 20px;"
        border
        v-loading="loading"
      >
        <el-table-column prop="name" label="等级名称" width="150" />
        <el-table-column prop="growthPoint" label="成长值门槛" width="120" />
        <el-table-column prop="discount" label="折扣率" width="100">
          <template #default="scope">
            {{ (scope.row.discount * 100).toFixed(0) }}%
          </template>
        </el-table-column>
        <el-table-column prop="integrationRate" label="积分倍率" width="100">
          <template #default="scope">
            {{ scope.row.integrationRate }}x
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"/>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button 
              link 
              :type="scope.row.status === 1 ? 'warning' : 'success'"
              @click="handleToggleStatus(scope.row)">
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
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

    <!-- Dialog -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="600px">
      <el-form ref="levelFormRef" :model="form" label-width="120px">
        <el-form-item label="等级名称" required>
          <el-input v-model="form.name" placeholder="如：普通会员、银卡会员" />
        </el-form-item>
        <el-form-item label="成长值门槛" required>
          <el-input-number v-model="form.growthPoint" :min="0" :step="10" style="width: 100%" />
          <div style="color: #999; font-size: 12px; margin-top: 5px;">会员成长值达到此值可升级到此等级</div>
        </el-form-item>
        <el-form-item label="折扣率" required>
          <el-input-number 
            v-model="form.discount" 
            :min="0" 
            :max="1" 
            :step="0.05" 
            :precision="2"
            style="width: 100%" 
          />
          <div style="color: #999; font-size: 12px; margin-top: 5px;">0.00-1.00，1.00表示无折扣，0.90表示9折</div>
        </el-form-item>
        <el-form-item label="积分倍率" required>
          <el-input-number 
            v-model="form.integrationRate" 
            :min="0.1" 
            :max="10" 
            :step="0.1" 
            :precision="2"
            style="width: 100%" 
          />
          <div style="color: #999; font-size: 12px; margin-top: 5px;">积分获取倍率，1.00表示正常倍率</div>
        </el-form-item>
        <el-form-item label="等级描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="描述此等级的权益和特权" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getMemberLevelList, 
  createMemberLevel, 
  updateMemberLevel, 
  deleteMemberLevel,
  updateMemberLevelStatus
} from '@/api/system/memberLevel'

interface MemberLevel {
  id?: number
  name: string
  growthPoint?: number
  discount?: number
  integrationRate?: number
  description?: string
  status?: number
  createTime?: string
}

const loading = ref(false)
const levelList = ref<MemberLevel[]>([])
const total = ref(0)
const pageParam = ref({
  page: 1,
  pageSize: 10
})

// 确保表格数据始终是数组
const safeLevelList = computed(() => {
  return Array.isArray(levelList.value) ? levelList.value : []
})

const dialog = reactive({
  visible: false,
  title: ''
})

const form = reactive<MemberLevel>({
  id: undefined,
  name: '',
  growthPoint: 0,
  discount: 1.00,
  integrationRate: 1.00,
  description: '',
  status: 1
})

const getList = () => {
  loading.value = true
  getMemberLevelList({
    page: pageParam.value.page,
    pageSize: pageParam.value.pageSize
  }).then((res: any) => {
    if (res && res.records && Array.isArray(res.records)) {
      levelList.value = res.records
      total.value = res.total || 0
    } else if (Array.isArray(res)) {
      levelList.value = res
      total.value = res.length
    } else {
      levelList.value = []
      total.value = 0
    }
    loading.value = false
  }).catch((err) => {
    console.error('获取等级列表失败:', err)
    loading.value = false
    levelList.value = []
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
  dialog.title = '新增等级'
  dialog.visible = true
  Object.assign(form, {
    id: undefined,
    name: '',
    growthPoint: 0,
    discount: 1.00,
    integrationRate: 1.00,
    description: '',
    status: 1
  })
}

const handleEdit = (row: any) => {
  dialog.title = '编辑等级'
  dialog.visible = true
  Object.assign(form, row)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定删除该等级吗？删除后无法恢复！', '警告', {
    type: 'warning'
  }).then(() => {
    deleteMemberLevel(row.id).then(() => {
      ElMessage.success('删除成功')
      getList()
    }).catch(() => {
      ElMessage.error('删除失败')
    })
  })
}

const handleToggleStatus = (row: any) => {
  const action = row.status === 1 ? '禁用' : '启用'
  const newStatus = row.status === 1 ? 0 : 1
  
  ElMessageBox.confirm(`确定要${action}等级 "${row.name}" 吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    updateMemberLevelStatus({
      id: row.id,
      status: newStatus
    }).then(() => {
      ElMessage.success(`${action}成功`)
      row.status = newStatus
      getList()
    }).catch(() => {
      ElMessage.error(`${action}失败`)
    })
  })
}

const handleSubmit = () => {
  if (!form.name) {
    ElMessage.warning('请输入等级名称')
    return
  }
  
  if (form.id) {
    updateMemberLevel(form).then(() => {
      ElMessage.success('更新成功')
      dialog.visible = false
      getList()
    }).catch(() => {
      ElMessage.error('更新失败')
    })
  } else {
    createMemberLevel(form).then(() => {
      ElMessage.success('创建成功')
      dialog.visible = false
      getList()
    }).catch(() => {
      ElMessage.error('创建失败')
    })
  }
}
</script>

<style scoped>
.header-actions {
  margin-bottom: 20px;
}
</style>

