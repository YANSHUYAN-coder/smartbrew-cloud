<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品列表</span>
          <el-button type="primary" @click="openProductDialog">
            <el-icon style="margin-right: 5px"><Plus /></el-icon>发布商品
          </el-button>
        </div>
      </template>

      <!-- 商品表格 -->
      <el-table :data="safeTableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="商品名称"></el-table-column>
        <el-table-column label="图片" width="100">
          <template #default="scope">
            <el-image style="width: 50px; height: 50px; border-radius: 4px;" :src="scope.row.picUrl" fit="cover"></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述"></el-table-column>
        <el-table-column prop="category" label="分类" width="100">
          <template #default="scope">
            <el-tag>{{ scope.row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template #default="scope">¥{{ scope.row.price }}</template>
        </el-table-column>
        <el-table-column prop="sales" label="销量" width="100"></el-table-column>
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="editProduct(scope.row)">编辑</el-button>
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

    <!-- 商品编辑/新增弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '发布新商品'" width="60%">
        <el-form :model="form" label-width="80px">
            <el-form-item label="商品名称">
                <el-input v-model="form.name" placeholder="例如：生椰拿铁"></el-input>
            </el-form-item>
            <el-row>
                <el-col :span="12">
                    <el-form-item label="基础价格">
                        <el-input-number v-model="form.price" :precision="2" :step="1"></el-input-number>
                    </el-form-item>
                </el-col>
                <el-col :span="12">
                    <el-form-item label="商品分类">
                        <el-select v-model="form.category" placeholder="请选择">
                            <el-option label="咖啡" value="咖啡"></el-option>
                            <el-option label="甜点" value="甜点"></el-option>
                            <el-option label="周边" value="周边"></el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-form-item label="商品图片">
                <el-input v-model="form.picUrl" placeholder="输入图片URL"></el-input>
            </el-form-item>
            <el-form-item label="商品描述">
                <el-input type="textarea" v-model="form.description"></el-input>
            </el-form-item>

            <!-- SKU 规格管理 (核心功能) -->
            <el-divider content-position="left">规格库存 (SKU)</el-divider>
            <div style="margin-bottom: 10px;">
                <el-button type="success" size="small" @click="addSkuRow">添加规格</el-button>
            </div>
            <el-table :data="form.skuStockList" border size="small">
                <el-table-column label="SKU编码">
                    <template #default="scope">
                        <el-input v-model="scope.row.skuCode" placeholder="自动生成" disabled></el-input>
                    </template>
                </el-table-column>
                <el-table-column label="规格属性 (JSON)" width="250">
                    <template #default="scope">
                        <el-input v-model="scope.row.spec" placeholder='[{"key":"容量","value":"大杯"}]'></el-input>
                    </template>
                </el-table-column>
                <el-table-column label="价格" width="120">
                    <template #default="scope">
                        <el-input-number v-model="scope.row.price" :min="0" controls-position="right" size="small" style="width: 100%"></el-input-number>
                    </template>
                </el-table-column>
                <el-table-column label="库存" width="120">
                    <template #default="scope">
                        <el-input-number v-model="scope.row.stock" :min="0" controls-position="right" size="small" style="width: 100%"></el-input-number>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="60">
                    <template #default="scope">
                        <el-button type="danger" icon="Delete" circle size="small" @click="removeSkuRow(scope.$index)"></el-button>
                    </template>
                </el-table-column>
            </el-table>

        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitProduct">确认保存</el-button>
            </span>
        </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductList, updateProductStatus } from '@/api/product'

interface Sku {
  id?: number
  skuCode: string
  price: number
  stock: number
  spec: string
}

interface Product {
  id: number | null
  name: string
  picUrl: string
  price: number
  category: string
  description: string
  sales: number
  status: number // 0: off, 1: on
  skuStockList: Sku[]
}

const loading = ref(false)
const tableData = ref<Product[]>([])
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

const form = reactive<Product>({
  id: null,
  name: '',
  price: 0,
  category: '',
  description: '',
  picUrl: '',
  sales: 0,
  status: 1,
  skuStockList: []
})

const getList = () => {
  loading.value = true
  getProductList({
    page: pageParam.value.page,
    pageSize: pageParam.value.pageSize
  }).then((res: any) => {
    if (res && res.records && Array.isArray(res.records)) {
      tableData.value = res.records
      total.value = res.total || 0
    } else if (Array.isArray(res)) {
      // 兼容旧接口，直接返回数组的情况
      tableData.value = res
      total.value = res.length
    } else {
      // 确保始终是数组
      tableData.value = []
      total.value = 0
    }
    loading.value = false
  }).catch((err) => {
    console.error('获取商品列表失败:', err)
    loading.value = false
    // 确保即使出错也是数组
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

const openProductDialog = () => {
  isEdit.value = false
  Object.assign(form, { 
      id: null, 
      name: '', 
      price: 0, 
      category: '', 
      description: '', 
      picUrl: '', 
      sales: 0, 
      status: 1, 
      skuStockList: [] 
  })
  // 添加一个默认 SKU
  form.skuStockList.push({ skuCode: '', price: 0, stock: 100, spec: '[{"key":"规格","value":"默认"}]' })
  dialogVisible.value = true
}

const editProduct = (row: Product) => {
  isEdit.value = true
  Object.assign(form, row)
  // Mock SKU data if empty, for demo purposes as in prototype
  if (!form.skuStockList || form.skuStockList.length === 0) {
      form.skuStockList = [
        { id: 101, skuCode: '202401', price: row.price, stock: 100, spec: '[{"key":"容量","value":"大杯"}]' },
        { id: 102, skuCode: '202402', price: row.price - 2, stock: 50, spec: '[{"key":"容量","value":"中杯"}]' }
      ]
  }
  dialogVisible.value = true
}

const submitProduct = () => {
    loading.value = true
    // Simulate API call delay
    setTimeout(() => {
        // In real app: createProduct(form).then(...)
        // For now just update local list or mock success
        if (isEdit.value) {
            const index = tableData.value.findIndex(item => item.id === form.id)
            if (index !== -1) {
                tableData.value[index] = { ...form }
            }
        } else {
            const newId = Math.max(...tableData.value.map(p => p.id || 0)) + 1
            tableData.value.push({ ...form, id: newId, sales: 0 })
        }
        
        loading.value = false
        dialogVisible.value = false
        ElMessage.success(isEdit.value ? '商品修改成功' : '商品发布成功')
    }, 500)
}

const addSkuRow = () => {
    form.skuStockList.push({ skuCode: '', price: form.price, stock: 100, spec: '' })
}

const removeSkuRow = (index: number) => {
    form.skuStockList.splice(index, 1)
}

const handleStatusChange = (row: Product) => {
    updateProductStatus({ id: row.id, status: row.status }).then(() => {
        ElMessage.success(`商品 ${row.name} 已${row.status === 1 ? '上架' : '下架'}`)
    }).catch(() => {
        // Revert on error
        row.status = row.status === 1 ? 0 : 1
    })
}

const handleDelete = (row: Product) => {
  ElMessageBox.confirm('确认删除该商品吗?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'error',
  }).then(() => {
    // deleteProduct(row.id!).then(...)
    // Mock delete
    const index = tableData.value.findIndex(item => item.id === row.id)
    if (index !== -1) tableData.value.splice(index, 1)
    ElMessage.success('删除成功')
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