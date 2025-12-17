import { get, post } from '@/utils/request.js'

/**
 * 获取购物车列表
 */
export const getCartList = () => {
  return get('/app/cart/list')
}

/**
 * 添加商品到购物车
 * @param {Object} cartItem - 购物车项
 * @param {Number} cartItem.productId - 商品ID
 * @param {Number} cartItem.productSkuId - SKU ID（可选）
 * @param {String} cartItem.productPic - 商品图片
 * @param {String} cartItem.productName - 商品名称
 * @param {String} cartItem.productSubTitle - 规格描述（如：大杯,热,全糖）
 * @param {Number} cartItem.price - 价格
 * @param {Number} cartItem.quantity - 数量
 */
export const addToCart = (cartItem) => {
  return post('/app/cart/add', cartItem)
}

/**
 * 更新购物车项数量
 * @param {Number} cartItemId - 购物车项ID
 * @param {Number} quantity - 数量
 */
export const updateCartQuantity = (cartItemId, quantity) => {
  return post('/app/cart/update', {
    cartItemId,
    quantity
  })
}

/**
 * 删除购物车项
 * @param {Number} id - 购物车项ID
 */
export const deleteCartItem = (id) => {
  return post(`/app/cart/delete/${id}`)
}

/**
 * 清空购物车
 */
export const clearCart = () => {
  return post('/app/cart/clear')
}

