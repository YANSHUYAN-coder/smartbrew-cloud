import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    // orderType: 'pickup' (到店取) | 'delivery' (外卖配送)
    orderType: uni.getStorageSync('orderType') || 'pickup',
    // 当前选择的门店信息
    currentStore: uni.getStorageSync('currentStore') || null,
    // 标记本场启动是否已完成“自动定位选店”
    hasAutoSelected: false
  }),
  
  actions: {
    setOrderType(type) {
      this.orderType = type
      uni.setStorageSync('orderType', type)
    },
    setStore(store) {
      this.currentStore = store
      uni.setStorageSync('currentStore', store)
    },
    markAutoSelected() {
      this.hasAutoSelected = true
    }
  }
})

