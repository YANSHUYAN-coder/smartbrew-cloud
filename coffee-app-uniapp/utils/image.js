/**
 * 图片工具函数
 * 用于处理图片 URL，包括代理转换等
 */

import { BASE_URL } from './config.js'

/**
 * 将 MinIO 的图片 URL 转换为代理 URL
 * @param {string} imageUrl - 原始图片 URL
 * @returns {string} - 转换后的 URL
 */
export function convertImageUrl(imageUrl) {
  if (!imageUrl) {
    return imageUrl
  }
  
  // 如果是 MinIO 的 URL（包含 192.168.14.130:9000 或 coffee-minio），转换为代理 URL
  if (imageUrl.includes('192.168.14.130:9000') || imageUrl.includes('coffee-minio') || imageUrl.includes('coffee-bucket')) {
    // 使用后端代理接口
    const proxyUrl = `${BASE_URL}/app/image/proxy?url=${encodeURIComponent(imageUrl)}`
    return proxyUrl
  }
  
  // 其他 URL 直接返回
  return imageUrl
}

/**
 * 批量转换商品列表中的图片 URL
 * @param {Array} products - 商品列表
 * @returns {Array} - 转换后的商品列表
 */
export function convertProductImages(products) {
  if (!Array.isArray(products)) {
    return products
  }
  
  return products.map(product => ({
    ...product,
    image: convertImageUrl(product.picUrl || product.image),
    picUrl: convertImageUrl(product.picUrl)
  }))
}

