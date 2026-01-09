// 图片处理工具
const BASE_API = import.meta.env.VITE_APP_BASE_API || '/api'

export function convertImageUrl(imageUrl: string): string {
  if (!imageUrl) return ''
  
  // 匹配内网 MinIO 地址 (包含 IP 或 容器名)
  if (imageUrl.includes('coffee-minio') || imageUrl.includes('192.168.248.128')) {
    // 转换为后端代理地址
    // 使用后端提供的通用代理接口 /app/image/proxy
    return `${BASE_API}/app/image/proxy?url=${encodeURIComponent(imageUrl)}`
  }
  
  return imageUrl
}
