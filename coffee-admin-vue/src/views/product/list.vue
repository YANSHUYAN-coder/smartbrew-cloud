<template>
  <div>
    <el-card>
      <div class="header-actions">
        <el-button type="primary" icon="Plus" @click="$router.push('/product/add')">发布商品</el-button>
      </div>

      <el-table :data="tableData" border style="width: 100%; margin-top: 20px" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="pic" label="图片" width="100">
          <template #default="scope">
            <el-image style="width: 50px; height: 50px" :src="scope.row.pic" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.publishStatus === 1 ? 'success' : 'info'">
              {{ scope.row.publishStatus === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="primary" @click="handleStatus(scope.row)">
              {{ scope.row.publishStatus === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination background layout="prev, pager, next" :total="100" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

interface Product {
  id: number
  name: string
  pic: string
  price: number
  stock: number
  publishStatus: number // 0: off, 1: on
}

const loading = ref(false)
const tableData = ref<Product[]>([
  { id: 1, name: '生椰拿铁', pic: 'https://via.placeholder.com/150', price: 18.0, stock: 100, publishStatus: 1 },
  { id: 2, name: '美式咖啡', pic: 'https://via.placeholder.com/150', price: 12.0, stock: 50, publishStatus: 1 },
  { id: 3, name: '已下架新品', pic: 'https://via.placeholder.com/150', price: 22.0, stock: 0, publishStatus: 0 },
])

const handleEdit = (row: Product) => {
  console.log('Edit', row)
  // router.push(`/product/edit/${row.id}`)
  ElMessage.info('编辑功能开发中')
}

const handleStatus = (row: Product) => {
  const action = row.publishStatus === 1 ? '下架' : '上架'
  ElMessageBox.confirm(`确认${action}该商品吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    row.publishStatus = row.publishStatus === 1 ? 0 : 1
    ElMessage.success(`${action}成功`)
  })
}

const handleDelete = (row: Product) => {
  ElMessageBox.confirm('确认删除该商品吗?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'error',
  }).then(() => {
    ElMessage.success('删除成功')
  })
}
</script>

<style scoped>
.header-actions {
  display: flex;
  justify-content: flex-end;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

