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

      <!-- 筛选区域 -->
      <div class="filter-container" style="margin-bottom: 20px; padding: 15px; background: #f5f7fa; border-radius: 4px;">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="关键词">
            <el-input 
              v-model="filterForm.keyword" 
              placeholder="商品名称/描述" 
              clearable
              style="width: 200px;"
              @clear="handleFilter"
              @keyup.enter="handleFilter">
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="分类">
            <el-select 
              v-model="filterForm.categoryId" 
              placeholder="全部分类" 
              clearable
              filterable
              style="width: 150px;"
              @change="handleFilter">
              <el-option 
                v-for="cat in categoryOptions" 
                :key="cat.id" 
                :label="cat.name" 
                :value="cat.id">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select 
              v-model="filterForm.status" 
              placeholder="全部状态" 
              clearable
              style="width: 120px;"
              @change="handleFilter">
              <el-option label="上架" :value="1"></el-option>
              <el-option label="下架" :value="0"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleFilter">
              <el-icon style="margin-right: 5px;"><Search /></el-icon>搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon style="margin-right: 5px;"><Refresh /></el-icon>重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 商品表格 -->
      <el-table :data="safeTableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="商品名称" width="200"></el-table-column>
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
    <el-dialog 
        v-model="dialogVisible" 
        :title="isEdit ? '编辑商品' : '发布新商品'" 
        width="60%"
        align-center
        :close-on-click-modal="false">
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
                        <el-select v-model="form.categoryId" placeholder="请选择分类" filterable style="width: 100%">
                            <el-option 
                                v-for="cat in categoryOptions" 
                                :key="cat.id" 
                                :label="cat.name" 
                                :value="cat.id">
                            </el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-form-item label="商品图片">
                <div v-if="form.picUrl" style="position: relative; display: inline-block; margin-bottom: 10px;">
                    <el-image 
                        :src="form.picUrl" 
                        style="width: 150px; height: 150px; border-radius: 4px; border: 1px solid #dcdfe6;"
                        fit="cover"
                        :preview-src-list="[form.picUrl]"
                    ></el-image>
                    <el-button 
                        type="danger" 
                        icon="Delete" 
                        circle 
                        size="small"
                        style="position: absolute; top: -8px; right: -8px;"
                        @click="removeImage"
                    ></el-button>
                </div>
                <el-upload
                    v-if="!form.picUrl"
                    class="product-image-uploader"
                    :http-request="handleImageUpload"
                    :show-file-list="false"
                    :before-upload="beforeImageUpload"
                    accept="image/*"
                >
                    <el-button type="primary" :loading="imageUploading">
                        <el-icon style="margin-right: 5px;"><Upload /></el-icon>上传图片
                    </el-button>
                </el-upload>
            </el-form-item>
            <el-form-item label="商品描述">
                <el-input type="textarea" v-model="form.description"></el-input>
            </el-form-item>

            <!-- SKU 规格管理 (核心功能) -->
            <el-divider content-position="left">规格库存 (SKU)</el-divider>
            <div style="margin-bottom: 15px;">
                <el-button type="success" size="small" @click="addSkuRow">
                    <el-icon style="margin-right: 5px;"><Plus /></el-icon>添加规格
                </el-button>
                <span style="color: #909399; font-size: 12px; margin-left: 10px;">
                    提示：每个规格可以设置多个属性（如：容量、温度、糖度等）
                </span>
            </div>
            <div v-if="form.skuStockList.length === 0" style="text-align: center; padding: 40px; color: #909399;">
                <el-empty description="暂无规格，请点击上方按钮添加" :image-size="80"></el-empty>
            </div>
            <div class="sku-list-container" v-else>
                <div v-for="(sku, skuIndex) in form.skuStockList" :key="skuIndex" class="sku-item">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
                        <span style="font-weight: 600; color: #303133;">规格 #{{ skuIndex + 1 }}</span>
                        <el-button type="danger" size="small" icon="Delete" @click="removeSkuRow(skuIndex)">删除规格</el-button>
                    </div>
                    <el-row :gutter="20">
                        <el-col :span="12">
                            <el-form-item label="价格" style="margin-bottom: 15px;">
                                <el-input-number 
                                    v-model="sku.price" 
                                    :min="0" 
                                    :precision="2"
                                    :step="1"
                                    controls-position="right" 
                                    style="width: 100%">
                                </el-input-number>
                            </el-form-item>
                        </el-col>
                        <el-col :span="12">
                            <el-form-item label="库存" style="margin-bottom: 15px;">
                                <el-input-number 
                                    v-model="sku.stock" 
                                    :min="0" 
                                    controls-position="right" 
                                    style="width: 100%">
                                </el-input-number>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-form-item label="规格属性" style="margin-bottom: 0;">
                        <div v-for="(spec, specIndex) in sku.specList" :key="specIndex" style="display: flex; gap: 10px; margin-bottom: 10px; align-items: center;">
                            <el-input 
                                v-model="spec.key" 
                                placeholder="属性名（如：容量）" 
                                style="flex: 1;"
                                clearable>
                            </el-input>
                            <el-input 
                                v-model="spec.value" 
                                placeholder="属性值（如：大杯）" 
                                style="flex: 1;"
                                clearable>
                            </el-input>
                            <el-button 
                                type="danger" 
                                icon="Delete" 
                                circle 
                                size="small"
                                @click="removeSpec(skuIndex, specIndex)"
                                :disabled="sku.specList.length === 1">
                            </el-button>
                        </div>
                        <el-button 
                            type="primary" 
                            size="small" 
                            plain
                            @click="addSpec(skuIndex)"
                            style="width: 100%; margin-top: 5px;">
                            <el-icon style="margin-right: 5px;"><Plus /></el-icon>添加属性
                        </el-button>
                        <div style="color: #909399; font-size: 12px; margin-top: 5px;">
                            SKU编码：<span style="color: #606266;">{{ sku.skuCode || '保存后自动生成' }}</span>
                        </div>
                    </el-form-item>
                </div>
            </div>

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
import { Plus, Upload, Delete, Search, Refresh } from '@element-plus/icons-vue'
import { getProductList, updateProductStatus, createProduct, updateProduct, deleteProduct, uploadProductImage, getProductDetail } from '@/api/product'
import { getCategoryList } from '@/api/category'

interface SpecItem {
  key: string
  value: string
}

interface Sku {
  id?: number
  skuCode: string
  price: number
  stock: number
  spec: string
  specList: SpecItem[] // 用于前端编辑的规格列表
}

interface Product {
  id: number | null
  name: string
  picUrl: string
  price: number
  category: string
  categoryId: number | null
  description: string
  sales: number
  status: number // 0: off, 1: on
  skuStockList: Sku[]
}

interface Category {
  id: number
  name: string
  status: number
}

const loading = ref(false)
const imageUploading = ref(false)
const tableData = ref<Product[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const total = ref(0)
const categoryOptions = ref<Category[]>([])
const pageParam = ref({
  page: 1,
  pageSize: 10
})

// 筛选表单
const filterForm = reactive({
  keyword: '',
  categoryId: null as number | null,
  status: null as number | null
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
  categoryId: null,
  description: '',
  picUrl: '',
  sales: 0,
  status: 1,
  skuStockList: []
})

const getList = () => {
  loading.value = true
  // 构建查询参数，包含筛选条件
  const params: any = {
    page: pageParam.value.page,
    pageSize: pageParam.value.pageSize
  }
  // 添加筛选条件（只传有值的）
  if (filterForm.keyword && filterForm.keyword.trim()) {
    params.keyword = filterForm.keyword.trim()
  }
  if (filterForm.categoryId) {
    params.categoryId = filterForm.categoryId
  }
  if (filterForm.status !== null && filterForm.status !== undefined) {
    params.status = filterForm.status
  }
  
  getProductList(params).then((res: any) => {
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

// 筛选处理
const handleFilter = () => {
  pageParam.value.page = 1 // 重置到第一页
  getList()
}

// 重置筛选
const handleReset = () => {
  filterForm.keyword = ''
  filterForm.categoryId = null
  filterForm.status = null
  pageParam.value.page = 1
  getList()
}

// 加载分类列表
const loadCategories = () => {
  getCategoryList({ page: 1, pageSize: 100 }).then((res: any) => {
    if (res && res.records) {
      categoryOptions.value = res.records.filter((cat: Category) => cat.status === 1)
    }
  }).catch(() => {
    ElMessage.error('加载分类列表失败')
  })
}

onMounted(() => {
  getList()
  loadCategories()
})

const openProductDialog = () => {
  isEdit.value = false
  Object.assign(form, { 
      id: null, 
      name: '', 
      price: 0, 
      category: '', 
      categoryId: null,
      description: '', 
      picUrl: '', 
      sales: 0, 
      status: 1, 
      skuStockList: [] 
  })
  // 添加一个默认 SKU
  addSkuRow()
  dialogVisible.value = true
}

const editProduct = (row: Product) => {
  isEdit.value = true
  // 先重置表单
  Object.assign(form, { 
      id: null, 
      name: '', 
      price: 0, 
      category: '', 
      categoryId: null,
      description: '', 
      picUrl: '', 
      sales: 0, 
      status: 1, 
      skuStockList: [] 
  })
  
  loading.value = true
  // 调用详情接口获取完整数据（包含SKU）
  getProductDetail(row.id!).then((res: any) => {
      // 这里的 res 应该是 ProductDTO
      const productDetail = res.data || res
      Object.assign(form, { ...productDetail })
  
  // 如果 categoryId 不存在，尝试根据 category 名称查找
  if (!form.categoryId && form.category) {
    const foundCategory = categoryOptions.value.find(cat => cat.name === form.category)
    if (foundCategory) {
      form.categoryId = foundCategory.id
    }
  }
  
      // 如果SKU列表为空，初始化为空数组
  if (!form.skuStockList) {
    form.skuStockList = []
  }
  // 将 spec JSON 字符串转换为 specList 数组
  form.skuStockList.forEach(sku => {
    if (sku.spec && typeof sku.spec === 'string') {
      try {
        const specArray = JSON.parse(sku.spec)
        sku.specList = Array.isArray(specArray) ? specArray : []
      } catch (e) {
        sku.specList = []
      }
    } else if (Array.isArray(sku.spec)) {
      sku.specList = sku.spec
    } else {
      sku.specList = []
    }
    // 确保至少有一个属性
    if (sku.specList.length === 0) {
      sku.specList.push({ key: '', value: '' })
    }
  })
  dialogVisible.value = true
  }).catch(() => {
      ElMessage.error('获取商品详情失败')
  }).finally(() => {
      loading.value = false
  })
}

// 将 specList 转换为 JSON 字符串
const convertSpecListToJson = (sku: Sku) => {
  if (sku.specList && Array.isArray(sku.specList)) {
    // 过滤掉空的属性
    const validSpecs = sku.specList.filter(spec => spec.key && spec.value)
    return JSON.stringify(validSpecs.length > 0 ? validSpecs : [])
  }
  return sku.spec || '[]'
}

const submitProduct = () => {
  // 验证必填字段
  if (!form.name || !form.name.trim()) {
    ElMessage.warning('请输入商品名称')
    return
  }
  if (!form.categoryId) {
    ElMessage.warning('请选择商品分类')
    return
  }
  if (form.skuStockList.length === 0) {
    ElMessage.warning('请至少添加一个规格')
    return
  }

  // 转换 specList 为 JSON 字符串
  const submitData = { ...form }
  submitData.skuStockList = form.skuStockList.map(sku => ({
    ...sku,
    spec: convertSpecListToJson(sku)
  }))
  // 移除 specList，只保留 spec
  submitData.skuStockList.forEach((sku: any) => {
    delete sku.specList
  })

  loading.value = true
  if (isEdit.value) {
    updateProduct(submitData).then(() => {
      ElMessage.success('商品修改成功')
      loading.value = false
      dialogVisible.value = false
      getList()
    }).catch(() => {
      loading.value = false
      ElMessage.error('商品修改失败')
    })
  } else {
    createProduct(submitData).then(() => {
      ElMessage.success('商品发布成功')
      loading.value = false
      dialogVisible.value = false
      getList()
    }).catch(() => {
      loading.value = false
      ElMessage.error('商品发布失败')
    })
  }
}

const addSkuRow = () => {
    form.skuStockList.push({ 
      skuCode: '', 
      price: form.price || 0, 
      stock: 100, 
      spec: '',
      specList: [{ key: '', value: '' }] // 默认一个空的规格属性
    })
}

const removeSkuRow = (index: number) => {
    form.skuStockList.splice(index, 1)
}

// 添加规格属性
const addSpec = (skuIndex: number) => {
  if (form.skuStockList[skuIndex]) {
    form.skuStockList[skuIndex].specList.push({ key: '', value: '' })
  }
}

// 删除规格属性
const removeSpec = (skuIndex: number, specIndex: number) => {
  if (form.skuStockList[skuIndex] && form.skuStockList[skuIndex].specList.length > 1) {
    form.skuStockList[skuIndex].specList.splice(specIndex, 1)
  }
}

// 删除图片
const removeImage = () => {
  form.picUrl = ''
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
  ElMessageBox.confirm('确认删除该商品吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    deleteProduct(row.id!).then(() => {
      ElMessage.success('删除成功')
      getList()
    }).catch(() => {
      ElMessage.error('删除失败')
    })
  })
}

// 图片上传相关
const beforeImageUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

const handleImageUpload = async (options: any) => {
  const { file } = options
  imageUploading.value = true
  try {
    const imageUrl = await uploadProductImage(file)
    form.picUrl = imageUrl
    ElMessage.success('图片上传成功')
  } catch (error: any) {
    ElMessage.error(error.message || '图片上传失败')
  } finally {
    imageUploading.value = false
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sku-list-container {
  max-height: 500px;
  overflow-y: auto;
  padding-right: 10px;
  margin-right: -10px;
}

.sku-list-container::-webkit-scrollbar {
  width: 8px;
}

.sku-list-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.sku-list-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.sku-list-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.sku-item {
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #fafafa;
}
</style>