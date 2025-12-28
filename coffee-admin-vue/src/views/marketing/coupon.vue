<template>
  <div class="app-container">
    <el-card class="filter-container" shadow="never">
      <el-form :inline="true" :model="listQuery">
        <el-form-item label="优惠券名称">
          <el-input v-model="listQuery.name" placeholder="请输入名称" style="width: 200px;" clearable />
        </el-form-item>
        <el-form-item label="优惠券类型">
          <el-select v-model="listQuery.type" placeholder="全部" clearable style="width: 150px">
            <el-option label="全场赠券" :value="0" />
            <el-option label="会员赠券" :value="1" />
            <el-option label="购物赠券" :value="2" />
            <el-option label="注册赠券" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="operator-container" style="margin: 20px 0;">
      <el-button type="primary" @click="handleAdd">添加优惠券</el-button>
    </div>

    <el-table :data="list" v-loading="listLoading" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="名称" align="center" />
      <el-table-column prop="type" label="类型" width="100" align="center">
        <template #default="scope">
          <el-tag>{{ formatType(scope.row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="amount" label="面值" width="100" align="center">
        <template #default="scope">¥{{ scope.row.amount }}</template>
      </el-table-column>
      <el-table-column prop="minPoint" label="使用门槛" width="120" align="center">
        <template #default="scope">满 {{ scope.row.minPoint }} 元</template>
      </el-table-column>
      <el-table-column label="有效期" width="320" align="center">
        <template #default="scope">
          {{ formatTime(scope.row.startTime) }} 至 {{ formatTime(scope.row.endTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="count" label="发行量" width="100" align="center" />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container" style="margin-top: 20px; text-align: right;">
      <el-pagination
          v-model:current-page="listQuery.pageNum"
          v-model:page-size="listQuery.pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="handlePageChange"
      />
    </div>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
      <el-form :model="couponForm" ref="couponFormRef" :rules="rules" label-width="120px">
        <el-form-item label="优惠券名称" prop="name">
          <el-input v-model="couponForm.name" />
        </el-form-item>
        <el-form-item label="优惠券类型" prop="type">
          <el-select v-model="couponForm.type">
            <el-option label="全场赠券" :value="0" />
            <el-option label="会员赠券" :value="1" />
            <el-option label="购物赠券" :value="2" />
            <el-option label="注册赠券" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="面值" prop="amount">
          <el-input-number v-model="couponForm.amount" :min="0" :precision="2" />
          <span style="margin-left: 10px">元</span>
        </el-form-item>
        <el-form-item label="使用门槛" prop="minPoint">
          <el-input-number v-model="couponForm.minPoint" :min="0" :precision="2" />
          <span style="margin-left: 10px">元 (0为无门槛)</span>
        </el-form-item>
        <el-form-item label="发行数量" prop="count">
          <el-input-number v-model="couponForm.count" :min="1" />
        </el-form-item>
        <el-form-item label="每人限领" prop="perLimit">
          <el-input-number v-model="couponForm.perLimit" :min="1" />
        </el-form-item>
        <el-form-item label="有效期" prop="timeRange">
          <el-date-picker
              v-model="timeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="couponForm.note" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleConfirm">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCouponList, createCoupon, updateCoupon, deleteCoupon, type Coupon } from '@/api/coupon'

const list = ref([])
const total = ref(0)
const listLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加优惠券')
const couponFormRef = ref()

const listQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  type: undefined
})

const timeRange = ref([]) // 用于绑定日期选择器

const defaultForm: Coupon = {
  type: 0,
  name: '',
  count: 100,
  amount: 0,
  perLimit: 1,
  minPoint: 0,
  startTime: '',
  endTime: '',
  useType: 0
}

const couponForm = reactive<Coupon>({ ...defaultForm })

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  count: [{ required: true, message: '请输入数量', trigger: 'blur' }],
}

onMounted(() => {
  getList()
})

const getList = () => {
  listLoading.value = true
  getCouponList(listQuery).then(res => {
    if (res && res.records) {
      list.value = res.records
      total.value = Number(res.total)
    } else {
      list.value = []
      total.value = 0
    }
    listLoading.value = false
  }).catch((error) => {
    console.error('获取优惠券列表失败:', error)
    listLoading.value = false
    list.value = []
  })
}

const handleSearch = () => {
  listQuery.pageNum = 1
  getList()
}

const resetSearch = () => {
  listQuery.name = ''
  listQuery.type = undefined
  handleSearch()
}

const handlePageChange = (val: number) => {
  listQuery.pageNum = val
  getList()
}

const handleAdd = () => {
  dialogTitle.value = '添加优惠券'
  Object.assign(couponForm, defaultForm)
  timeRange.value = []
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑优惠券'
  Object.assign(couponForm, row)
  // 设置时间回显
  if (row.startTime && row.endTime) {
    timeRange.value = [row.startTime, row.endTime] as any
  } else {
    timeRange.value = []
  }
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该优惠券吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteCoupon(row.id).then(res => {
      ElMessage.success('删除成功')
      getList()
    })
  })
}

const handleConfirm = () => {
  couponFormRef.value.validate((valid: boolean) => {
    if (valid) {
      // 处理时间
      if (timeRange.value && timeRange.value.length === 2) {
        couponForm.startTime = timeRange.value[0]
        couponForm.enableTime = timeRange.value[0]
        couponForm.endTime = timeRange.value[1]
      }

      const request = couponForm.id
          ? updateCoupon(couponForm.id, couponForm)
          : createCoupon(couponForm)

      request.then(() => {
        ElMessage.success(couponForm.id ? '修改成功' : '添加成功')
        dialogVisible.value = false
        getList()
      })
    }
  })
}

const formatType = (type: number) => {
  const map: Record<number, string> = {
    0: '全场赠券',
    1: '会员赠券',
    2: '购物赠券',
    3: '注册赠券'
  }
  return map[type] || '未知'
}

const formatTime = (time: string) => {
  return time ? time.replace('T', ' ') : ''
}
</script>