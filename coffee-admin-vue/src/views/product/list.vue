<template>
  <div>
    <el-card>
      <div class="header-actions">
        <el-button type="primary" icon="Plus" @click="$router.push('/product/add')">发布商品</el-button>
      </div>

      <el-table :data="tableData" border style="width: 100%; margin-top: 20px" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="picUrl" label="图片" width="100">
          <template #default="scope">
            <el-image style="width: 50px; height: 50px" :src="scope.row.picUrl" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="sales" label="销量" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="primary" @click="handleStatus(scope.row)">
              {{ scope.row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductList, updateProductStatus, deleteProduct } from '@/api/product'

interface Product {
  id: number
  name: string
  picUrl: string
  price: number
  sales: number
  status: number // 0: off, 1: on
}

const loading = ref(false)
const tableData = ref<Product[]>([])

const getList = () => {
  loading.value = true
  getProductList({}).then((res: any) => {
    tableData.value = res
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

onMounted(() => {
  getList()
})

const handleEdit = (row: Product) => {
  // router.push(`/product/edit/${row.id}`)
  ElMessage.info('编辑功能需完善 Add 页面以支持回显')
}

const handleStatus = (row: Product) => {
  const action = row.status === 1 ? '下架' : '上架'
  ElMessageBox.confirm(`确认${action}该商品吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    updateProductStatus({ id: row.id, status: row.status === 1 ? 0 : 1 }).then(() => {
      ElMessage.success(`${action}成功`)
      getList()
    })
  })
}

const handleDelete = (row: Product) => {
  ElMessageBox.confirm('确认删除该商品吗?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'error',
  }).then(() => {
    deleteProduct(row.id).then(() => {
       ElMessage.success('删除成功')
       getList()
    })
  })
}
</script>

<style scoped>
.header-actions {
  display: flex;
  justify-content: flex-end;
}
</style>
