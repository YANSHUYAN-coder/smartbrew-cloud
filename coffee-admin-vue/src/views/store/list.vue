<template>
  <div class="app-container">
    <!-- 顶部操作栏 -->
    <div class="filter-container" style="margin-bottom: 20px;">
      <el-button type="primary" icon="Plus" @click="handleCreate">新增门店</el-button>
      <el-button icon="Refresh" @click="getList">刷新</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table v-loading="listLoading" :data="list" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="门店名称" width="180" />
      <el-table-column prop="address" label="详细地址" min-width="200" show-overflow-tooltip />
      <el-table-column prop="phone" label="联系电话" width="150" align="center" />
      <el-table-column label="营业时间" width="150" align="center">
        <template #default="scope">
          <el-tag size="small">{{ scope.row.businessHours }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="营业状态" width="120" align="center">
        <template #default="scope">
          <el-switch
            v-model="scope.row.openStatus"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(scope.row)"
          />
          <span style="margin-left: 10px">{{ scope.row.openStatus === 1 ? '营业中' : '休息中' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="外送状态" width="120" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.deliveryStatus === 1 ? 'success' : 'info'">
            {{ scope.row.deliveryStatus === 1 ? '开启' : '关闭' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleUpdate(scope.row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="textMap[dialogStatus]" v-model="dialogFormVisible">
      <el-form ref="dataForm" :model="temp" :rules="rules" label-width="100px" style="width: 400px; margin-left:50px;">
        <el-form-item label="门店名称" prop="name">
          <el-input v-model="temp.name" placeholder="请输入门店名称" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="temp.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="temp.address" type="textarea" placeholder="请输入详细地址" />
          <el-button type="primary" link icon="Location" @click="openMapPicker" style="margin-top: 5px;">在地图上选择</el-button>
        </el-form-item>
        <el-form-item label="营业时间" prop="businessHours">
          <el-input v-model="temp.businessHours" placeholder="如 08:00-22:00" />
        </el-form-item>
        <el-form-item label="配送半径(m)" prop="deliveryRadius">
          <el-input-number v-model="temp.deliveryRadius" :min="0" />
        </el-form-item>
        <el-form-item label="起步配送费" prop="baseDeliveryFee">
          <el-input-number v-model="temp.baseDeliveryFee" :precision="2" :step="0.1" :min="0" />
        </el-form-item>
        <el-form-item label="外送状态">
          <el-radio-group v-model="temp.deliveryStatus">
            <el-radio :label="1">开启</el-radio>
            <el-radio :label="0">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false">取消</el-button>
          <el-button type="primary" @click="dialogStatus==='create'?createData():updateData()">确认</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 地图选择器弹窗 -->
    <el-dialog title="在地图上选择位置" v-model="mapDialogVisible" width="800px" @opened="initMap">
      <div style="margin-bottom: 15px;">
        <el-input id="tipinput" v-model="mapSearchKeyword" placeholder="请输入关键字搜索地点" clearable>
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <div id="map-container" style="width: 100%; height: 450px; border: 1px solid #eee; border-radius: 8px;"></div>
      <div style="margin-top: 15px; padding: 10px; background-color: #f8f9fa; border-radius: 4px;">
        <span style="font-weight: bold; color: #606266;">当前选中地址：</span>
        <span v-if="addressLoading" style="color: #409eff;"><el-icon class="is-loading"><Loading /></el-icon> 正在解析地址...</span>
        <span v-else style="color: #303133;">{{ selectedPoint.address || '请在地图上点击或搜索位置' }}</span>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="mapDialogVisible = false">取消</el-button>
          <el-button type="primary" :disabled="!selectedPoint.lng || addressLoading" @click="confirmMapSelection">确认选择</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Location, Search, Loading } from '@element-plus/icons-vue'
import { getStoreList, addStore, updateStore, deleteStore, updateOpenStatus } from '@/api/store'

// 声明 AMap 全局变量 (由 index.html 引入)
declare const AMap: any

const list = ref([])
const listLoading = ref(true)
const dialogFormVisible = ref(false)
const dialogStatus = ref('')
const textMap = {
  update: '编辑门店',
  create: '新增门店'
}

// 地图相关
const mapDialogVisible = ref(false)
const mapSearchKeyword = ref('')
const addressLoading = ref(false) // 增加加载状态
let mapInstance: any = null
let marker: any = null
let geocoder: any = null
const selectedPoint = reactive({
  lng: 0,
  lat: 0,
  address: ''
})

const temp = reactive({
  id: undefined,
  name: '',
  address: '',
  phone: '',
  longitude: 0,
  latitude: 0,
  openStatus: 1,
  deliveryStatus: 1,
  businessHours: '08:00-22:00',
  deliveryRadius: 5000,
  baseDeliveryFee: 5.00
})

const rules = {
  name: [{ required: true, message: '名称是必填项', trigger: 'blur' }],
  address: [{ required: true, message: '地址是必填项', trigger: 'blur' }],
  phone: [{ required: true, message: '电话是必填项', trigger: 'blur' }]
}

// 打开地图选择器
const openMapPicker = () => {
  mapDialogVisible.value = true
  // 预设当前经纬度或默认值
  selectedPoint.lng = temp.longitude || 113.943567
  selectedPoint.lat = temp.latitude || 22.548274
  selectedPoint.address = temp.address
}

// 初始化地图
const initMap = () => {
  if (mapInstance) {
    mapInstance.destroy()
  }

  mapInstance = new AMap.Map('map-container', {
    zoom: 15,
    center: [selectedPoint.lng, selectedPoint.lat]
  })

  // 添加点击事件
  mapInstance.on('click', (e: any) => {
    const lng = e.lnglat.getLng()
    const lat = e.lnglat.getLat()
    updateMarker(lng, lat)
    getAddress(lng, lat)
  })

  // 添加搜索功能
  const autoOptions = { input: 'tipinput' }
  const auto = new AMap.AutoComplete(autoOptions)
  const placeSearch = new AMap.PlaceSearch({ 
    map: mapInstance,
    pageSize: 1 // 只显示最匹配的一个
  })
  
  auto.on('select', (e: any) => {
    const poi = e.poi
    if (poi.location) {
      updateMarker(poi.location.lng, poi.location.lat)
      selectedPoint.address = poi.district + poi.address + poi.name
      mapSearchKeyword.value = poi.name
    } else {
      placeSearch.search(poi.name)
    }
  })

  // 监听搜索结果点击（如果有多个结果展示在地图上时）
  placeSearch.on('markerClick', (e: any) => {
    const poi = e.data
    selectedPoint.lng = poi.location.lng
    selectedPoint.lat = poi.location.lat
    selectedPoint.address = poi.cityname + poi.adname + poi.address + poi.name
  })

  // 如果已有经纬度，显示标记
  if (selectedPoint.lng && selectedPoint.lat) {
    updateMarker(selectedPoint.lng, selectedPoint.lat)
  }
}

// 更新标记
const updateMarker = (lng: number, lat: number) => {
  selectedPoint.lng = lng
  selectedPoint.lat = lat
  
  if (marker) {
    marker.setPosition([lng, lat])
  } else {
    marker = new AMap.Marker({
      position: [lng, lat],
      map: mapInstance
    })
  }
  mapInstance.setCenter([lng, lat])
}

// 根据坐标获取地址
const getAddress = (lng: number, lat: number) => {
  if (!geocoder) {
    geocoder = new AMap.Geocoder()
  }
  addressLoading.value = true
  geocoder.getAddress([lng, lat], (status: string, result: any) => {
    addressLoading.value = false
    if (status === 'complete' && result.regeocode) {
      selectedPoint.address = result.regeocode.formattedAddress
    } else {
      ElMessage.error('地址解析失败，请尝试在地图上微调位置')
    }
  })
}

// 确认地图选择
const confirmMapSelection = () => {
  temp.longitude = selectedPoint.lng
  temp.latitude = selectedPoint.lat
  if (selectedPoint.address) {
    temp.address = selectedPoint.address
  }
  mapDialogVisible.value = false
  ElMessage.success('位置选择成功')
}

const getList = async () => {
  listLoading.value = true
  try {
    const res = await getStoreList()
    list.value = res.data || res
  } catch (error) {
    console.error(error)
  } finally {
    listLoading.value = false
  }
}

const handleStatusChange = async (row: any) => {
  try {
    await updateOpenStatus(row.id, row.openStatus)
    ElMessage.success('状态更新成功')
  } catch (error) {
    row.openStatus = row.openStatus === 1 ? 0 : 1
  }
}

const resetTemp = () => {
  Object.assign(temp, {
    id: undefined,
    name: '',
    address: '',
    phone: '',
    longitude: 0,
    latitude: 0,
    openStatus: 1,
    deliveryStatus: 1,
    businessHours: '08:00-22:00',
    deliveryRadius: 5000,
    baseDeliveryFee: 5.00
  })
}

const handleCreate = () => {
  resetTemp()
  dialogStatus.value = 'create'
  dialogFormVisible.value = true
}

const createData = async () => {
  try {
    await addStore(temp)
    dialogFormVisible.value = false
    ElMessage.success('新增成功')
    getList()
  } catch (error) {
    console.error(error)
  }
}

const handleUpdate = (row: any) => {
  Object.assign(temp, row)
  dialogStatus.value = 'update'
  dialogFormVisible.value = true
}

const updateData = async () => {
  try {
    await updateStore(temp)
    dialogFormVisible.value = false
    ElMessage.success('更新成功')
    getList()
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确认删除该门店吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteStore(row.id)
    ElMessage.success('删除成功')
    getList()
  })
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.filter-container {
  display: flex;
  gap: 10px;
}
</style>

