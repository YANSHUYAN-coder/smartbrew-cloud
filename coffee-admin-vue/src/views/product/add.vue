<template>
  <div class="product-add-container">
    <el-card>
      <el-steps :active="activeStep" finish-status="success" align-center style="margin-bottom: 30px;">
        <el-step title="基础信息" />
        <el-step title="规格库存" />
      </el-steps>

      <el-form ref="formRef" :model="form" label-width="120px" v-show="activeStep === 0">
        <el-form-item label="商品名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="商品图片">
          <el-upload action="#" list-type="picture-card" :auto-upload="false">
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input type="textarea" v-model="form.description" />
        </el-form-item>
        <el-form-item label="基础价格">
          <el-input-number v-model="form.price" :precision="2" :step="0.1" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="activeStep = 1">下一步</el-button>
        </el-form-item>
      </el-form>

      <div v-show="activeStep === 1">
        <div style="margin-bottom: 20px;">
          <el-button type="primary" size="small" @click="handleAddSku">添加规格</el-button>
        </div>
        <el-table :data="form.skus" border>
          <el-table-column label="规格 (如: 大杯/热/半糖)">
            <template #default="scope">
              <el-input v-model="scope.row.specs" placeholder='{"size": "大", "temp": "热"}' />
            </template>
          </el-table-column>
          <el-table-column label="价格" width="150">
            <template #default="scope">
              <el-input-number v-model="scope.row.price" :precision="2" :step="0.1" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="库存" width="150">
            <template #default="scope">
              <el-input-number v-model="scope.row.stock" :min="0" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button link type="danger" @click="removeSku(scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div style="margin-top: 30px; text-align: center;">
          <el-button @click="activeStep = 0">上一步</el-button>
          <el-button type="primary" @click="handleSubmit">提交商品</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const activeStep = ref(0)

const form = reactive({
  name: '',
  description: '',
  price: 0,
  skus: [] as any[]
})

const handleAddSku = () => {
  form.skus.push({
    specs: '',
    price: form.price,
    stock: 100
  })
}

const removeSku = (index: number) => {
  form.skus.splice(index, 1)
}

const handleSubmit = () => {
  console.log('Submit', form)
  ElMessage.success('商品发布成功')
  router.push('/product/list')
}
</script>

<style scoped>
.product-add-container {
  max-width: 800px;
  margin: 0 auto;
}
</style>

