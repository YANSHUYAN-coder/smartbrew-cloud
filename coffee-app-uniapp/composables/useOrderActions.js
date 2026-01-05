import { cancelOrder as cancelOrderApi, confirmReceiveOrder as confirmReceiveOrderApi } from '@/services/order.js'
import { alipay, payByCoffeeCard, syncPaymentStatus } from '@/services/pay.js'

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
    const orderObj = typeof order === 'object' ? order : { id: order }
    const orderId = orderObj.id
    
    try {
      uni.showLoading({ title: '正在发起支付...' })
      
      // 判断支付方式：3->咖啡卡，1->支付宝
      const payType = orderObj.payType || 1
      
      if (payType === 3) {
        // 咖啡卡支付
        await payByCoffeeCard(orderId)
        uni.hideLoading()
        uni.showToast({ title: '支付成功', icon: 'success' })
        if (onSuccess) onSuccess()
      } else {
        // 支付宝支付
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
          success: async (res) => {
            console.log('支付宝支付成功回调:', res)
            try {
              // 【核心优化】前端支付成功后，立即通知后端同步一次状态
              await syncPaymentStatus(orderId)
            } catch (e) {
              console.warn('同步支付状态失败，等待系统后台自动同步:', e)
            }
            uni.showToast({ title: '支付成功', icon: 'success' })
            if (onSuccess) onSuccess()
          },
          fail: (err) => {
            console.error('支付失败:', err)
            uni.showToast({ title: '支付未完成', icon: 'none' })
          }
        })
      }
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
    
    // 先弹出原因选择框
    const reasons = ['不想要了', '商品选错', '信息填写错误', '其他原因']
    uni.showActionSheet({
      itemList: reasons,
      success: async (res) => {
        const reason = reasons[res.tapIndex]
        
        try {
          uni.showLoading({ title: '正在取消...' })
          await cancelOrderApi(orderId, reason)
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
    })
  }

  /**
   * 确认收货通用逻辑
   * @param {Object} order 订单对象或订单ID
   * @param {Function} onSuccess 成功后的回调函数
   */
  const handleConfirmReceive = (order, onSuccess) => {
    const orderId = typeof order === 'object' ? order.id : order

    uni.showModal({
      title: '提示',
      content: '确定已收到商品吗？',
      confirmColor: '#6f4e37',
      success: async (res) => {
        if (res.confirm) {
          try {
            uni.showLoading({ title: '正在提交...' })
            await confirmReceiveOrderApi(orderId)
            uni.hideLoading()

            uni.showToast({
              title: '确认收货成功',
              icon: 'success'
            })

            if (onSuccess && typeof onSuccess === 'function') {
              onSuccess()
            }
          } catch (error) {
            uni.hideLoading()
            uni.showToast({
              title: error.message || '确认收货失败',
              icon: 'none'
            })
          }
        }
      }
    })
  }

  return {
    handleCancelOrder,
    handlePayOrder,
    handleConfirmReceive
  }
}

