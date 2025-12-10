<template>
  <div>
    <el-card>
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="全部订单" name="all" />
        <el-tab-pane label="待制作" name="1" />
        <el-tab-pane label="制作中" name="2" />
        <el-tab-pane label="待取餐" name="3" />
      </el-tabs>

      <el-table :data="tableData" border style="width: 100%">
        <el-table-column prop="orderSn" label="订单号" width="180" />
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column prop="amount" label="总金额" width="100">
          <template #default="scope">¥{{ scope.row.amount }}</template>
        </el-table-column>
        <el-table-column label="商品信息">
          <template #default="scope">
            <div v-for="item in scope.row.items" :key="item.name">
              {{ item.name }} x {{ item.count }} ({{ item.specs }})
            </div>
          </template>
        </el-table-column>
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
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('all')

const tableData = ref([
  {
    orderSn: '202312100001',
    createTime: '2023-12-10 10:00:00',
    amount: 32.0,
    status: 1, // 待制作
    items: [{ name: '生椰拿铁', count: 1, specs: '大/冰/半糖' }, { name: '贝果', count: 1, specs: '加热' }]
  },
  {
    orderSn: '202312100002',
    createTime: '2023-12-10 10:05:00',
    amount: 18.0,
    status: 2, // 制作中
    items: [{ name: '美式', count: 1, specs: '中/热' }]
  },
  {
    orderSn: '202312100003',
    createTime: '2023-12-10 09:55:00',
    amount: 25.0,
    status: 3, // 待取餐
    items: [{ name: '拿铁', count: 1, specs: '大/热' }]
  }
])

const getStatusType = (status: number) => {
  const map: any = { 0: 'danger', 1: 'warning', 2: 'primary', 3: 'success', 4: 'info', 5: 'info' }
  return map[status]
}

const getStatusText = (status: number) => {
  const map: any = { 0: '待支付', 1: '待制作', 2: '制作中', 3: '待取餐', 4: '已完成', 5: '已取消' }
  return map[status]
}

const handleTabClick = () => {
  // TODO: Filter data
}

const updateStatus = (row: any, newStatus: number) => {
  row.status = newStatus
  ElMessage.success('订单状态更新成功')
}
</script>

