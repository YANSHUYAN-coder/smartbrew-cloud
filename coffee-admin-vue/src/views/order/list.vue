<template>
  <div>
    <el-card>
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="全部订单" name="-1" />
        <el-tab-pane label="待制作" name="1" />
        <el-tab-pane label="制作中" name="2" />
        <el-tab-pane label="待取餐" name="3" />
      </el-tabs>

      <el-table :data="safeTableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="orderSn" label="订单号" width="180" />
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column prop="payAmount" label="总金额" width="100">
          <template #default="scope">¥{{ scope.row.payAmount }}</template>
        </el-table-column>
        <el-table-column prop="receiverName" label="收货人" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button v-if="scope.row.status === 1" type="primary" size="small" @click="updateStatus(scope.row, 2)">开始制作</el-button>
            <el-button v-if="scope.row.status === 2" type="success" size="small" @click="updateStatus(scope.row, 3)">制作完成</el-button>
            <el-button v-if="scope.row.status === 3" type="warning" size="small" @click="updateStatus(scope.row, 4)">通知取餐</el-button>
            <el-button v-if="scope.row.status === 0" type="danger" size="small">取消订单</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getOrderList, updateOrderStatus } from '@/api/order'

const activeTab = ref('-1')
const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const pageParam = ref({
  page: 1,
  pageSize: 10
})

// 确保表格数据始终是数组
const safeTableData = computed(() => {
  return Array.isArray(tableData.value) ? tableData.value : []
})

const getStatusType = (status: number) => {
  const map: any = { 0: 'danger', 1: 'warning', 2: 'primary', 3: 'success', 4: 'info', 5: 'info' }
  return map[status]
}

const getStatusText = (status: number) => {
  const map: any = { 0: '待支付', 1: '待制作', 2: '制作中', 3: '待取餐', 4: '已完成', 5: '已取消' }
  return map[status]
}

const getList = () => {
    loading.value = true
    const status = activeTab.value === '-1' ? undefined : parseInt(activeTab.value)
    const params: any = {
      page: pageParam.value.page,
      pageSize: pageParam.value.pageSize
    }
    if (status !== undefined) {
      params.status = status
    }
    getOrderList(params).then((res: any) => {
        if (res && res.records && Array.isArray(res.records)) {
          tableData.value = res.records
          total.value = res.total || 0
        } else {
          tableData.value = []
          total.value = 0
        }
        loading.value = false
    }).catch(() => {
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

const handleTabClick = () => {
  pageParam.value.page = 1
  getList()
}

const updateStatus = (row: any, newStatus: number) => {
  updateOrderStatus({ id: row.id, status: newStatus }).then(() => {
      ElMessage.success('订单状态更新成功')
      getList()
  })
}

onMounted(() => {
    getList()
})
</script>
