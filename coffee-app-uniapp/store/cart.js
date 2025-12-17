import { defineStore } from 'pinia'
import { getCartList, addToCart as addToCartApi, updateCartQuantity, deleteCartItem } from '@/services/cart.js'

// 规格文案顺序常量，避免在解析时出现“魔法字符串”
const SPEC_TEXT_KEYS = ['容量', '温度', '糖度']

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
    syncing: false // 是否正在同步
  }),
  
  getters: {
    totalCount: (state) => {
      return state.items.reduce((sum, item) => sum + item.quantity, 0)
    },
    totalPrice: (state) => {
      return state.items.reduce((sum, item) => sum + item.price * item.quantity, 0)
    }
  },
  
  actions: {
    // 生成购物车项的唯一标识（商品ID + SKU ID 或规格组合）
    getCartItemKey(product) {
      if (product.selectedSku && product.selectedSku.id) {
        // 如果有SKU ID，使用商品ID + SKU ID
        return `${product.id}_${product.selectedSku.id}`
      } else if (product.selectedSpecs && Object.keys(product.selectedSpecs).length > 0) {
        // 如果没有SKU ID但有规格，使用商品ID + 规格组合
        const specKey = Object.keys(product.selectedSpecs)
          .sort()
          .map(key => `${key}:${product.selectedSpecs[key]}`)
          .join('|')
        return `${product.id}_${specKey}`
      } else {
        // 没有规格的商品，只使用商品ID
        return `${product.id}_default`
      }
    },
    
    // 同步购物车数据（从后端加载）
    async syncCart() {
      if (this.syncing) return
      try {
        this.syncing = true
        const cartList = await getCartList()
        // 将后端数据转换为前端格式
        this.items = cartList.map(item => ({
          id: item.productId,
          cartItemId: item.id, // 保存后端购物车项ID
          productSkuId: item.productSkuId,
          image: item.productPic,
          name: item.productName,
          price: item.price,
          quantity: item.quantity,
          selectedSpecs: item.productSubTitle ? this.parseSpecText(item.productSubTitle) : {},
          cartKey: item.productSkuId ? `${item.productId}_${item.productSkuId}` : `${item.productId}_default`
        }))
      } catch (error) {
        console.error('同步购物车失败', error)
        // 如果同步失败，不影响本地使用
      } finally {
        this.syncing = false
      }
    },
    
    // 解析规格文本为对象（如："大杯,热,全糖" -> {容量: "大杯", 温度: "热", 糖度: "全糖"}）
    parseSpecText(specText) {
      if (!specText) return {}
      const specs = specText.split(',').map(s => s.trim())
      // 这里简化处理：按预设顺序（容量、温度、糖度）依次映射
      const result = {}
      specs.forEach((spec, index) => {
        const key = SPEC_TEXT_KEYS[index]
        if (key) {
          result[key] = spec
        }
      })
      return result
    },
    
    // 添加商品到购物车
    async addToCart(product, quantity = 1) {
      try {
        // 构建后端需要的购物车项数据
        const cartItem = {
          productId: product.id,
          productSkuId: product.selectedSku?.id || null,
          productPic: product.image || product.picUrl || '',
          productName: product.name,
          productSubTitle: product.selectedSpecs ? Object.values(product.selectedSpecs).join(',') : '',
          price: product.price,
          quantity: quantity
        }
        
        // 调用后端接口
        await addToCartApi(cartItem)
        
        // 成功后更新本地 store
        const cartKey = this.getCartItemKey(product)
        const existItem = this.items.find(item => {
          const itemKey = this.getCartItemKey(item)
          return itemKey === cartKey
        })
        
        if (existItem) {
          // 如果已存在，增加数量
          existItem.quantity += quantity
        } else {
          // 如果不存在，添加新项（需要重新同步获取 cartItemId）
          // 暂时先添加，下次同步时会更新 cartItemId
          this.items.push({
            ...product,
            quantity: quantity,
            cartKey: cartKey
          })
          // 立即同步一次以获取 cartItemId
          this.syncCart()
        }
      } catch (error) {
        console.error('加入购物车失败', error)
        throw error
      }
    },
    
    // 更新购物车项数量
    async updateQuantity(cartKey, delta) {
      const item = this.items.find(item => {
        const itemKey = item.cartKey || this.getCartItemKey(item)
        return itemKey === cartKey
      })
      
      if (!item) return
      
      const newQuantity = Math.max(0, item.quantity + delta)
      
      // 如果有 cartItemId，调用后端接口
      if (item.cartItemId) {
        try {
          if (newQuantity === 0) {
            // 数量为0，删除
            await deleteCartItem(item.cartItemId)
            this.removeItemByKey(cartKey)
          } else {
            // 更新数量
            await updateCartQuantity(item.cartItemId, newQuantity)
            item.quantity = newQuantity
          }
        } catch (error) {
          console.error('更新购物车数量失败', error)
          throw error
        }
      } else {
        // 如果没有 cartItemId，先同步数据
        await this.syncCart()
        // 同步后重新查找并更新
        const updatedItem = this.items.find(i => {
          const itemKey = i.cartKey || this.getCartItemKey(i)
          return itemKey === cartKey
        })
        if (updatedItem) {
          if (newQuantity === 0) {
            if (updatedItem.cartItemId) {
              await deleteCartItem(updatedItem.cartItemId)
            }
            this.removeItemByKey(cartKey)
          } else {
            if (updatedItem.cartItemId) {
              await updateCartQuantity(updatedItem.cartItemId, newQuantity)
            }
            updatedItem.quantity = newQuantity
          }
        }
      }
    },
    
    // 设置购物车项数量
    setQuantity(cartKey, quantity) {
      const item = this.items.find(item => {
        const itemKey = item.cartKey || this.getCartItemKey(item)
        return itemKey === cartKey
      })
      
      if (item) {
        if (quantity <= 0) {
          this.removeItemByKey(cartKey)
        } else {
          item.quantity = quantity
        }
      }
    },
    
    // 根据唯一标识删除购物车项
    async removeItemByKey(cartKey) {
      const item = this.items.find(item => {
        const itemKey = item.cartKey || this.getCartItemKey(item)
        return itemKey === cartKey
      })
      
      // 如果有 cartItemId，调用后端接口删除
      if (item && item.cartItemId) {
        try {
          await deleteCartItem(item.cartItemId)
        } catch (error) {
          console.error('删除购物车项失败', error)
          throw error
        }
      }
      
      // 从本地 store 中移除
      this.items = this.items.filter(item => {
        const itemKey = item.cartKey || this.getCartItemKey(item)
        return itemKey !== cartKey
      })
    },
    
    // 根据商品ID删除（删除该商品的所有规格）
    removeItem(id) {
      this.items = this.items.filter(item => item.id !== id)
    },
    
    // 清空购物车
    clearCart() {
      this.items = []
    },
    
    // 根据商品ID和规格查找购物车项
    findCartItem(product) {
      const cartKey = this.getCartItemKey(product)
      return this.items.find(item => {
        const itemKey = item.cartKey || this.getCartItemKey(item)
        return itemKey === cartKey
      })
    },
    
    // 获取商品在购物车中的总数量（所有规格的总和）
    getProductTotalCount(productId) {
      return this.items
        .filter(item => item.id === productId)
        .reduce((sum, item) => sum + item.quantity, 0)
    }
  }
})

