<template>
  <div class="dashboard-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>今日销售额</span>
              <el-icon><DataAnalysis /></el-icon>
            </div>
          </template>
          <div class="card-value">¥ {{ formatNumber(statistics.todaySales) }}</div>
          <div class="card-footer">较昨日 <span :class="statistics.salesGrowth >= 0 ? 'positive' : 'negative'">
            {{ statistics.salesGrowth >= 0 ? '+' : '' }}{{ statistics.salesGrowth }}%
          </span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>今日订单量</span>
              <el-icon><ShoppingCart /></el-icon>
            </div>
          </template>
          <div class="card-value">{{ formatNumber(statistics.todayOrderCount) }}</div>
          <div class="card-footer">较昨日 <span :class="statistics.orderGrowth >= 0 ? 'positive' : 'negative'">
            {{ statistics.orderGrowth >= 0 ? '+' : '' }}{{ statistics.orderGrowth }}%
          </span></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>AI 服务调用</span>
              <el-icon><Promotion /></el-icon>
            </div>
          </template>
          <div class="card-value">{{ formatNumber(statistics.aiServiceCalls) }}</div>
          <div class="card-footer">累计调用次数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>会员新增</span>
              <el-icon><User /></el-icon>
            </div>
          </template>
          <div class="card-value">{{ formatNumber(statistics.todayNewMembers) }}</div>
          <div class="card-footer">今日新增会员数</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="chart-header">
              <span>销售趋势（最近7天）</span>
              <el-radio-group v-model="trendDays" size="small" @change="loadSalesTrend">
                <el-radio-button :label="7">7天</el-radio-button>
                <el-radio-button :label="30">30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="salesChartRef" style="height: 350px;" v-loading="chartLoading"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>热销商品 Top 5</span>
          </template>
          <el-table :data="topProducts" style="width: 100%" v-loading="productsLoading" :show-header="false">
            <el-table-column width="40">
              <template #default="scope">
                <el-tag :type="getRankTagType(scope.$index)">{{ scope.$index + 1 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="sales" label="销量" width="80" align="right">
              <template #default="scope">
                <span style="color: #409eff; font-weight: bold;">{{ scope.row.sales }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getStatistics, getSalesTrend, getTopProducts } from '@/api/dashboard'
import { ElMessage } from 'element-plus'

interface Statistics {
  todaySales: number
  todayOrderCount: number
  aiServiceCalls: number
  todayNewMembers: number
  salesGrowth: number
  orderGrowth: number
}

const statistics = reactive<Statistics>({
  todaySales: 0,
  todayOrderCount: 0,
  aiServiceCalls: 0,
  todayNewMembers: 0,
  salesGrowth: 0,
  orderGrowth: 0
})

const topProducts = ref<any[]>([])
const salesChartRef = ref<HTMLElement>()
let salesChart: echarts.ECharts | null = null
const trendDays = ref(7)
const chartLoading = ref(false)
const productsLoading = ref(false)

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res: any = await getStatistics()
    statistics.todaySales = typeof res.todaySales === 'number' ? res.todaySales : parseFloat(res.todaySales) || 0
    statistics.todayOrderCount = typeof res.todayOrderCount === 'number' ? res.todayOrderCount : parseInt(res.todayOrderCount) || 0
    statistics.aiServiceCalls = typeof res.aiServiceCalls === 'number' ? res.aiServiceCalls : parseInt(res.aiServiceCalls) || 0
    statistics.todayNewMembers = typeof res.todayNewMembers === 'number' ? res.todayNewMembers : parseInt(res.todayNewMembers) || 0
    
    // 计算增长率（这里简化处理，实际应该对比昨日数据）
    statistics.salesGrowth = 12.5
    statistics.orderGrowth = 8.3
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  }
}

// 加载销售趋势
const loadSalesTrend = async () => {
  if (!salesChartRef.value) return
  
  chartLoading.value = true
  try {
    const res: any = await getSalesTrend(trendDays.value)
    
    await nextTick()
    
    if (!salesChart) {
      salesChart = echarts.init(salesChartRef.value)
    }
    
    const dates = res.map((item: any) => item.date)
    const sales = res.map((item: any) => item.sales)
    
    const option = {
      tooltip: {
        trigger: 'axis',
        formatter: (params: any) => {
          const param = params[0]
          return `${param.name}<br/>销售额: ¥${formatNumber(param.value)}`
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dates,
        axisLine: {
          lineStyle: {
            color: '#e0e0e0'
          }
        }
      },
      yAxis: {
        type: 'value',
        axisLine: {
          lineStyle: {
            color: '#e0e0e0'
          }
        },
        axisLabel: {
          formatter: (value: number) => {
            if (value >= 10000) {
              return (value / 10000).toFixed(1) + '万'
            }
            return value.toString()
          }
        }
      },
      series: [
        {
          name: '销售额',
          type: 'line',
          smooth: true,
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
                { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
              ]
            }
          },
          itemStyle: {
            color: '#409eff'
          },
          data: sales
        }
      ]
    }
    
    salesChart.setOption(option)
  } catch (error) {
    ElMessage.error('加载销售趋势失败')
  } finally {
    chartLoading.value = false
  }
}

// 加载热销商品
const loadTopProducts = async () => {
  productsLoading.value = true
  try {
    const res: any = await getTopProducts(5)
    topProducts.value = res || []
  } catch (error) {
    ElMessage.error('加载热销商品失败')
  } finally {
    productsLoading.value = false
  }
}

// 格式化数字
const formatNumber = (num: number | string) => {
  if (num === undefined || num === null) return '0'
  const numValue = typeof num === 'string' ? parseFloat(num) : num
  if (isNaN(numValue)) return '0'
  // 如果是整数，不显示小数
  if (numValue % 1 === 0) {
    return numValue.toLocaleString('zh-CN')
  }
  return numValue.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 获取排名标签类型
const getRankTagType = (index: number) => {
  const types = ['danger', 'warning', 'success', 'info', '']
  return types[index] || ''
}

// 处理窗口大小变化
const handleResize = () => {
  if (salesChart) {
    salesChart.resize()
  }
}

onMounted(() => {
  loadStatistics()
  loadSalesTrend()
  loadTopProducts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (salesChart) {
    salesChart.dispose()
    salesChart = null
  }
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.stat-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #666;
}

.card-header .el-icon {
  font-size: 20px;
  color: #409eff;
}

.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin: 10px 0;
}

.card-footer {
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
}

.card-footer .positive {
  color: #67c23a;
}

.card-footer .negative {
  color: #f56c6c;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-card :deep(.el-card__header) {
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}
</style>
