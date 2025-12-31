<template>
  <view class="fly-cart-container" v-if="dots.length > 0">
    <view 
      v-for="(dot, index) in dots" 
      :key="dot.id"
      class="fly-dot"
      :style="{
        left: dot.x + 'px',
        top: dot.y + 'px',
        opacity: dot.opacity,
        transform: `scale(${dot.scale})`
      }"
    >
      <image v-if="dot.image" :src="dot.image" class="dot-img" mode="aspectFill" />
      <view v-else class="dot-inner"></view>
    </view>
  </view>
</template>

<script setup>
import { reactive, onUnmounted } from 'vue'

const dots = reactive([])
let idCounter = 0
let isUnmounted = false

onUnmounted(() => {
  isUnmounted = true
  dots.length = 0 // 清空所有正在进行的动画
})

/**
 * 触发抛物线动画
 */
const startAnimation = (options) => {
  if (isUnmounted) return // 如果组件已卸载，不再开启新动画
// ... (后面逻辑省略)
  const { startX, startY, endX, endY, image } = options
  
  if (isNaN(startX) || isNaN(startY)) {
    console.warn('抛物线动画起点坐标无效:', startX, startY)
    return
  }

  const id = idCounter++
  const dot = reactive({
    id,
    x: startX,
    y: startY,
    opacity: 1,
    scale: 1,
    image
  })
  
  dots.push(dot)
  
  // 控制点 (x, y) - 抛物线顶点
  const cpX = (startX + endX) / 2
  const cpY = Math.min(startY, endY) - 150
  
  const duration = 600 
  const startTime = Date.now()
  
  const step = () => {
    const now = Date.now()
    const progress = Math.min((now - startTime) / duration, 1)
    
    // 二阶贝塞尔公式
    const t = progress
    const invT = 1 - t
    
    dot.x = invT * invT * startX + 2 * t * invT * cpX + t * t * endX
    dot.y = invT * invT * startY + 2 * t * invT * cpY + t * t * endY
    
    // 动画效果：先放大后缩小
    dot.scale = progress < 0.2 ? (1 + progress * 2) : (1.4 - progress * 0.9)
    
    if (progress > 0.8) {
      dot.opacity = 1 - (progress - 0.8) / 0.2
    }
    
    if (progress < 1) {
      if (isUnmounted) return // 如果过程中组件卸载，立即停止循环
      // #ifdef H5
      requestAnimationFrame(step)
      // #endif
      // #ifndef H5
      setTimeout(step, 16)
      // #endif
    } else {
      const idx = dots.findIndex(d => d.id === id)
      if (idx > -1) dots.splice(idx, 1)
    }
  }
  
  step()
}

defineExpose({
  startAnimation
})
</script>

<style lang="scss" scoped>
.fly-cart-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  pointer-events: none;
  z-index: 9999;
}

.fly-dot {
  position: absolute;
  width: 40rpx;
  height: 40rpx;
  margin-left: -20rpx;
  margin-top: -20rpx;
}

.dot-inner {
  width: 100%;
  height: 100%;
  background-color: #6f4e37;
  border-radius: 50%;
  box-shadow: 0 0 10rpx rgba(111, 78, 55, 0.5);
}

.dot-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 2rpx solid white;
  box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.1);
}
</style>

