/**
 * 项目配置文件
 * 统一管理 API 地址等配置
 */

// 获取本机 IP 地址（需要手动修改）
const DEV_IP = '192.168.110.110' // ⚠️ 替换为你的电脑 IP 地址
const DEV_PORT = 8081

// 生产环境地址
const PROD_DOMAIN = 'http://your-production-domain.com'

// 根据平台和环境获取 BASE_URL
const getBaseUrl = () => {
  // #ifdef H5
  // H5 环境：开发环境使用代理，生产环境使用完整 URL
  return process.env.NODE_ENV === 'development' 
    ? '/api'  // 开发环境使用代理
    : `${PROD_DOMAIN}/api`  // 生产环境
  // #endif

  // #ifdef APP-PLUS
  // APP 环境：必须使用完整的 HTTP URL
  return process.env.NODE_ENV === 'development'
    ? `https://691efe72.r36.cpolar.top/api`  // 开发环境
    : `${PROD_DOMAIN}/api`  // 生产环境
  // #endif

  // #ifdef MP
  // 小程序环境：必须使用完整的 HTTPS URL
  return process.env.NODE_ENV === 'development'
    ? `http://${DEV_IP}:${DEV_PORT}/api`  // 开发环境（需要配置合法域名）
    : `https://${PROD_DOMAIN}/api`  // 生产环境必须是 HTTPS
  // #endif

  // 默认值
  return '/api'
}

export const BASE_URL = getBaseUrl()

// 其他配置可以在这里添加
export const config = {
  // API 超时时间（毫秒）
  timeout: 10000,
  
  // 其他配置...
}
export default {
  BASE_URL,
  config
}

