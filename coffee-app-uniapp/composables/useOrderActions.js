import { cancelOrder as cancelOrderApi } from '@/services/order.js'
import { alipay } from '@/services/pay.js'

/**
 * 订单相关通用操作逻辑
 */
export function useOrderActions() {
  /**
   * 支付订单通用逻辑
   * @param {Object} order 订单对象或订单ID
   * @param {Function} onSuccess 成功后的回调函数
   */
  const handlePayOrder = async (order, onSuccess) => {
    const orderId = typeof order === 'object' ? order.id : order
    
    try {
      uni.showLoading({ title: '正在发起支付...' })
      
      // 1. 处理沙箱环境（仅开发环境）
      if (process.env.NODE_ENV === 'development') {
        // #ifdef APP-PLUS
        const EnvUtils = plus.android.importClass("com.alipay.sdk.app.EnvUtils")
        if (EnvUtils) {
          EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX)
        }
        // #endif
      }

      // 2. 获取后端签名的支付串
      const orderStr = await alipay(orderId)
      uni.hideLoading()

      // 3. 调起支付
      uni.requestPayment({
        provider: 'alipay',
        orderInfo: orderStr,
        success: (res) => {
          uni.showToast({ title: '支付成功', icon: 'success' })
          if (onSuccess) onSuccess()
        },
        fail: (err) => {
          console.error('支付失败:', err)
          uni.showToast({ title: '支付未完成', icon: 'none' })
        }
      })
    } catch (error) {
      uni.hideLoading()
      console.error('支付请求异常:', error)
      uni.showToast({ title: error.message || '发起支付失败', icon: 'none' })
    }
  }

  /**
   * 取消订单通用逻辑
   * @param {Object} order 订单对象或订单ID
   * @param {Function} onSuccess 成功后的回调函数
   */
  const handleCancelOrder = (order, onSuccess) => {
    const orderId = typeof order === 'object' ? order.id : order
    
    uni.showModal({
      title: '提示',
      content: '确定要取消该订单吗？',
      confirmColor: '#6f4e37',
      success: async (res) => {
        if (res.confirm) {
          try {
            uni.showLoading({ title: '正在取消...' })
            await cancelOrderApi(orderId)
            uni.hideLoading()
            
            uni.showToast({
              title: '取消成功',
              icon: 'success'
            })
            
            // 触发成功回调
            if (onSuccess && typeof onSuccess === 'function') {
              onSuccess()
            }
          } catch (error) {
            uni.hideLoading()
            uni.showToast({
              title: error.message || '取消失败',
              icon: 'none'
            })
          }
        }
      }
    })
  }

  return {
    handleCancelOrder,
    handlePayOrder
  }
}

