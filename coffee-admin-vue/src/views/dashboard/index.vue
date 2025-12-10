<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>今日销售额</span>
            </div>
          </template>
          <div class="card-value">¥ 12,345</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>今日订单量</span>
            </div>
          </template>
          <div class="card-value">128</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>AI 服务调用</span>
            </div>
          </template>
          <div class="card-value">5,230</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>会员新增</span>
            </div>
          </template>
          <div class="card-value">12</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <span>销售趋势</span>
          </template>
          <div style="height: 300px; display: flex; align-items: center; justify-content: center; color: #999;">
            (ECharts 图表区域 - 待集成)
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>热销商品 Top 5</span>
          </template>
          <el-table :data="topProducts" style="width: 100%">
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="sales" label="销量" width="100" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const topProducts = ref([
  { name: '生椰拿铁', sales: 450 },
  { name: '美式咖啡', sales: 320 },
  { name: '焦糖玛奇朵', sales: 210 },
  { name: '燕麦拿铁', sales: 180 },
  { name: '卡布奇诺', sales: 150 },
])
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-value {
  font-size: 24px;
  font-weight: bold;
}
</style>

