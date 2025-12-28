<script setup lang="ts">
import { RouterView } from 'vue-router'
import { onMounted, onBeforeUnmount, shallowRef } from 'vue';
import SockJS from 'sockjs-client/dist/sockjs.min.js';
import Stomp from 'stompjs';
import { ElNotification, ElButton } from 'element-plus'; // 引入 Button 用于测试
import router from '@/router'
// WebSocket 客户端实例
const stompClient = shallowRef<any>(null);
/**
 * 播放提示音
 */
const playAudio = () => {
  // 确保你的 public/static/audio/ 目录下有 new_order.mp3 文件
  const audio = new Audio('https://down.ear0.com:3321/preview?soundid=37418&type=mp3');
  audio.play().catch((e) => {
    console.warn('播放失败，可能是因为用户未与页面交互:', e);
    ElNotification({
      title: '提示',
      message: '请点击页面任意位置以启用语音提示',
      type: 'warning'
    });
  });
};

/**
 * 处理新消息逻辑
 */
const handleNewOrderMessage = (msg: string) => {
  console.log('收到新订单消息:', msg);

  // 1. 播放声音
  playAudio();

  // 2. 弹出右侧通知
  ElNotification({
    title: '新订单通知',
    message: msg,
    type: 'success',
    duration: 0, // 0表示不自动关闭，需手动关闭
    position: 'bottom-right', // 建议放在右下角，不遮挡视线
    onClick: () => {
      console.log('管理员点击了通知，跳转订单列表...');
      router.push('/order/list');
    }
  });
};

/**
 * 初始化 WebSocket 连接
 */
const initWebSocket = () => {
  // 【关键修改】如果之前日志显示连上了，说明地址是对的。
  // 如果之前是 localhost:8082/ws/server 连不上，试着加上 /api
  // 这里我们用之前的成功经验（看你之前的日志是连上了，说明你当时的配置是对的，保持原样或加 /api）
  const socket = new SockJS('http://localhost:8082/api/ws/server');

  stompClient.value = Stomp.over(socket);
  // stompClient.value.debug = () => {}; // 如果不想看控制台刷屏日志，取消注释这行

  stompClient.value.connect(
      {},
      (frame: any) => {
        console.log('✅ WebSocket 连接成功');
        // 订阅 "新订单" 主题
        stompClient.value.subscribe('/topic/newOrder', (response: any) => {
          handleNewOrderMessage(response.body);
        });
      },
      (err: any) => {
        console.error('❌ WebSocket 连接失败，5秒后重试', err);
        setTimeout(() => initWebSocket(), 5000);
      }
  );
};

// 生命周期
onMounted(() => {
  initWebSocket();
});

onBeforeUnmount(() => {
  if (stompClient.value?.connected) {
    stompClient.value.disconnect();
  }
});

// --- 👇 仅用于测试的模拟方法 👇 ---
const testNotification = () => {
  handleNewOrderMessage('这是一条测试订单消息 (ID: 888)');
}
</script>

<template>
  <RouterView />
</template>

<style scoped>
</style>

