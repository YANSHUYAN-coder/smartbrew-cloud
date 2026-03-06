// 图片处理工具
const BASE_API = import.meta.env.VITE_APP_BASE_API || '/api'

export function convertImageUrl(imageUrl: string): string {
  if (!imageUrl) return ''

  // 以前的那些 includes('coffee-minio') 和代理逻辑全部删掉！
  // 因为现在 Nginx 会统一处理 /coffee-bucket/ 开头的请求

  // 1. 如果你还没来得及跑 SQL 清理历史数据，可以用前端兼容一下旧数据（把前缀强行截掉）
  if (imageUrl.includes('http://coffee-minio:9000')) {
    return imageUrl.replace('http://coffee-minio:9000', '')
  }
  if (imageUrl.includes('http://192.168.14.130:9000')) {
    return imageUrl.replace('http://192.168.14.130:9000', '')
  }

  // 2. 对于新上传的、或者是已经跑过 SQL 清理的数据（格式如: /coffee-bucket/product/xxx.png）
  // 直接返回即可！浏览器会自动把相对路径拼上当前 IP，然后发给 Nginx
  return imageUrl
}
