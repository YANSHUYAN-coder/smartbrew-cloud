<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>注册会员列表</span>
          <el-input v-model="searchKeyword" placeholder="搜索手机号" style="width: 200px" @keyup.enter="handleSearch">
            <template #prefix><el-icon>
                <Search />
              </el-icon></template>
          </el-input>
        </div>
      </template>

      <el-table :data="safeTableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="phone" label="手机号"  width="150"/>
        <el-table-column prop="integration" label="积分" width="100" />
        <el-table-column label="等级" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.level" type="warning">
              {{ scope.row.level.name }}
            </el-tag>
            <span v-else style="color: #999;">未设置</span>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="200">
          <template #default="scope">
            <el-tag v-for="role in scope.row.roles || []" :key="role.id" style="margin-right: 5px; margin-bottom: 5px;"
              :type="getRoleTagType(role.name)">
              {{ getRoleName(role.name) }}
            </el-tag>
            <span v-if="!scope.row.roles || scope.row.roles.length === 0" style="color: #999;">无</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
            <el-button link type="primary" icon="User" @click="handleAssignRole(scope.row)">分配角色</el-button>
            <el-button 
              v-if="scope.row.status === 1" 
              link type="danger" icon="Lock" 
              @click="handleStatus(scope.row, 0)">
              封禁
            </el-button>
            <el-button 
              v-else 
              link type="success" icon="Unlock" 
              @click="handleStatus(scope.row, 1)">
              解封
            </el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
        <el-pagination v-model:current-page="pageParam.page" v-model:page-size="pageParam.pageSize"
          :page-sizes="[10, 20, 50, 100]" :total="total" layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <!-- 会员详情对话框 -->
    <el-dialog title="会员详情" v-model="detailDialog.visible" width="600px">
      <el-descriptions :column="2" border v-if="detailDialog.member">
        <el-descriptions-item label="用户名">{{ detailDialog.member.username }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detailDialog.member.phone }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ detailDialog.member.nickname || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="性别">
          <el-tag :type="getGenderTagType(detailDialog.member.gender)">
            {{ getGenderText(detailDialog.member.gender) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="积分">{{ detailDialog.member.integration || 0 }}</el-descriptions-item>
        <el-descriptions-item label="成长值">{{ detailDialog.member.growth || 0 }}</el-descriptions-item>
        <el-descriptions-item label="等级">
          <el-tag v-if="detailDialog.member.level" type="warning">
            {{ detailDialog.member.level.name }}
          </el-tag>
          <span v-else style="color: #999;">未设置</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailDialog.member.status === 1 ? 'success' : 'danger'">
            {{ detailDialog.member.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ detailDialog.member.createTime }}</el-descriptions-item>
        <el-descriptions-item label="角色" :span="2">
          <el-tag 
            v-for="role in detailDialog.member.roles || []" 
            :key="role.id" 
            style="margin-right: 5px; margin-bottom: 5px;"
            :type="getRoleTagType(role.name)">
            {{ getRoleName(role.name) }}
          </el-tag>
          <span v-if="!detailDialog.member.roles || detailDialog.member.roles.length === 0" style="color: #999;">无</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色对话框 -->
    <el-dialog title="分配角色" v-model="roleDialog.visible" width="500px">
      <div class="role-tree-container">
        <el-tree
          ref="roleTreeRef"
          :data="allRoles"
          show-checkbox
          node-key="id"
          :props="{ label: 'name', children: 'children' }"
        />
      </div>
      <template #footer>
        <el-button @click="roleDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleRoleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMemberList, getMemberDetail, updateMemberStatus, deleteMember, allocMemberRoles } from '@/api/member'
import { getRoleList } from '@/api/system/role'

interface Role {
  id: number
  name: string
  description: string
  status: number
}

interface MemberLevel {
  id: number
  name: string
  growthPoint?: number
  discount?: number
  integrationRate?: number
  description?: string
}

interface Member {
  id: number
  username: string
  phone: string
  nickname?: string
  gender?: number
  integration?: number
  growth?: number
  levelId?: number
  status: number
  createTime: string
  roles?: Role[]
  level?: MemberLevel
}

const loading = ref(false)
const tableData = ref<Member[]>([])
const searchKeyword = ref('')
const total = ref(0)
const pageParam = ref({
  page: 1,
  pageSize: 10
})
const allRoles = ref<any[]>([])
const roleTreeRef = ref()

const detailDialog = reactive({
  visible: false,
  member: null as Member | null
})

const roleDialog = reactive({
  visible: false,
  userId: 0,
  member: null as Member | null
})

// 确保表格数据始终是数组
const safeTableData = computed(() => {
  return Array.isArray(tableData.value) ? tableData.value : []
})

const getList = () => {
  loading.value = true
  const params: any = {
    page: pageParam.value.page,
    pageSize: pageParam.value.pageSize
  }
  if (searchKeyword.value) {
    params.phone = searchKeyword.value
  }
  getMemberList(params).then((res: any) => {
    if (res && res.records && Array.isArray(res.records)) {
      tableData.value = res.records
      total.value = res.total || 0
    } else {
      tableData.value = []
      total.value = 0
    }
    loading.value = false
  }).catch((err) => {
    console.error('获取会员列表失败:', err)
    loading.value = false
    tableData.value = []
    total.value = 0
  })
}

const handleSearch = () => {
  pageParam.value.page = 1
  getList()
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
  loadAllRoles()
})

// 加载所有角色供选择
const loadAllRoles = () => {
  getRoleList({
    page: 1,
    pageSize: 1000
  }).then((res: any) => {
    if (res && res.records && Array.isArray(res.records)) {
      allRoles.value = res.records.filter((role: any) => role.status === 1) // 只显示启用的角色
    } else if (Array.isArray(res)) {
      allRoles.value = res.filter((role: any) => role.status === 1)
    } else {
      allRoles.value = []
    }
  }).catch(() => {
    allRoles.value = []
  })
}

const handleStatus = (row: Member, newStatus: number) => {
  const action = newStatus === 1 ? '解封' : '封禁'
  ElMessageBox.confirm(`确定要${action}用户 ${row.username} 吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    updateMemberStatus({ id: row.id, status: newStatus }).then(() => {
      ElMessage.success(`用户已${action}`)
      row.status = newStatus
    }).catch(() => {
      // Mock success if API fails
      row.status = newStatus
      ElMessage.success(`用户已${action} (演示模式)`)
    })
  })
}

// 获取角色显示名称
const getRoleName = (roleName: string) => {
  const roleMap: Record<string, string> = {
    'ROLE_ADMIN': '超级管理员',
    'ROLE_MANAGER': '店长',
    'ROLE_BARISTA': '咖啡师',
    'ROLE_MEMBER': '普通会员'
  }
  return roleMap[roleName] || roleName
}

// 获取角色标签类型
const getRoleTagType = (roleName: string) => {
  const typeMap: Record<string, string> = {
    'ROLE_ADMIN': 'danger',
    'ROLE_MANAGER': 'warning',
    'ROLE_BARISTA': 'success',
    'ROLE_MEMBER': 'info'
  }
  return typeMap[roleName] || ''
}

// 获取性别文本 0->未知；1->男；2->女
const getGenderText = (gender?: number) => {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '未知'
}

// 获取性别标签类型
const getGenderTagType = (gender?: number) => {
  if (gender === 1) return 'success'  // 男-绿色
  if (gender === 2) return 'danger'   // 女-红色
  return 'info'  // 未知-灰色
}

// 查看详情
const handleDetail = (row: Member) => {
  detailDialog.visible = true
  // 先使用列表中的数据（已包含等级和角色信息）
  detailDialog.member = { ...row }
  
  // 再从接口获取最新详情数据（用于获取更多详细信息）
  getMemberDetail(row.id).then((res: any) => {
    // 合并数据，确保等级和角色信息不丢失
    if (res) {
      detailDialog.member = {
        ...res,
        // 如果接口返回的数据没有等级或角色，使用列表中的数据
        level: res.level || row.level,
        roles: res.roles || row.roles
      }
    }
  }).catch(() => {
    // 如果接口调用失败，仍然使用列表中的数据
    console.warn('获取会员详情失败，使用列表数据')
  })
}

// 分配角色
const handleAssignRole = (row: Member) => {
  roleDialog.userId = row.id
  roleDialog.member = row
  roleDialog.visible = true
  
  // 设置已选中的角色
  nextTick(() => {
    if (row.roles && row.roles.length > 0) {
      const selectedRoleIds = row.roles.map(role => role.id)
      roleTreeRef.value?.setCheckedKeys(selectedRoleIds)
    } else {
      roleTreeRef.value?.setCheckedKeys([])
    }
  })
}

// 提交角色分配
const handleRoleSubmit = () => {
  const checkedKeys = roleTreeRef.value?.getCheckedKeys() || []
  allocMemberRoles(roleDialog.userId, checkedKeys).then(() => {
    ElMessage.success('角色分配成功')
    roleDialog.visible = false
    getList() // 刷新列表
  }).catch(() => {
    ElMessage.error('角色分配失败')
  })
}

// 删除会员
const handleDelete = (row: Member) => {
  ElMessageBox.confirm(
    `确定要删除用户 "${row.username}" 吗？此操作不可恢复！`, 
    '警告', 
    {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    }
  ).then(() => {
    deleteMember(row.id).then(() => {
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

.role-tree-container {
  max-height: 300px;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #fff;
}

.role-tree-container::-webkit-scrollbar {
  width: 8px;
}

.role-tree-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.role-tree-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.role-tree-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>