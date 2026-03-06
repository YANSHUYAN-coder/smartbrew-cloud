/**
 * 图片工具函数
 * 用于处理图片 URL，适配 Nginx 反向代理架构
 */

// 建议你在 config.js 中定义一个纯净的服务器地址（不要带 /api）
// import { IMAGE_SERVER_URL } from './config.js'
const IMAGE_SERVER_URL = 'http://106.12.70.182'; // 替换为你的真实IP或域名

/**
 * 处理图片路径，拼接 Nginx 代理地址
 * @param {string} imageUrl - 原始图片 URL (可能是绝对路径，也可能是相对路径)
 * @returns {string} - 转换后的完整可访问 URL
 */
export function convertImageUrl(imageUrl) {
  if (!imageUrl) {
    return '';
  }

  let finalUrl = imageUrl;

  // 1. 兼容旧数据：如果数据库里还有以前没清理干净的历史遗留前缀，强行截掉，还原为相对路径
  if (finalUrl.includes('http://coffee-minio:9000')) {
    finalUrl = finalUrl.replace('http://coffee-minio:9000', '');
  } else if (finalUrl.includes('http://192.168.14.130:9000')) {
    finalUrl = finalUrl.replace('http://192.168.14.130:9000', '');
  }

  // 2. 如果已经是完整的外网 HTTP 链接（比如微信头像、或者其他云存储的图片），直接原样返回
  if (finalUrl.startsWith('http://') || finalUrl.startsWith('https://')) {
    return finalUrl;
  }

  // 3. 核心逻辑：如果是我们自己 MinIO 的相对路径，拼接上云服务器的 IP
  // 这样请求发出去就是 http://106.12.70.182/coffee-bucket/... 会被 Nginx 完美拦截
  if (finalUrl.startsWith('/coffee-bucket')) {
    return IMAGE_SERVER_URL + finalUrl;
  }

  // 其他情况（比如本地 static 目录下的相对图片），直接返回
  return finalUrl;
}

/**
 * 批量转换商品列表中的图片 URL
 * （这个方法的设计非常好，原样保留即可，它会自动调用上面更新后的逻辑）
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