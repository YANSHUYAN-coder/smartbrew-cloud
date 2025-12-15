/**
 * 系统信息工具
 */

// 获取状态栏高度
export const getStatusBarHeight = () => {
  const systemInfo = uni.getSystemInfoSync()
  return systemInfo.statusBarHeight || 0
}

// 获取安全区域信息
export const getSafeAreaInsets = () => {
  const systemInfo = uni.getSystemInfoSync()
  return {
    top: systemInfo.safeAreaInsets?.top || getStatusBarHeight(),
    bottom: systemInfo.safeAreaInsets?.bottom || 0,
    left: systemInfo.safeAreaInsets?.left || 0,
    right: systemInfo.safeAreaInsets?.right || 0
  }
}

// 获取导航栏高度（包含状态栏）
export const getNavigationBarHeight = () => {
  const statusBarHeight = getStatusBarHeight()
  // 导航栏高度通常是 44px，加上状态栏高度
  return statusBarHeight + 44
}

