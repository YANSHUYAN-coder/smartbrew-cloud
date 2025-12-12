<template>
  <el-menu
    active-text-color="#ffd04b"
    background-color="#304156"
    class="el-menu-vertical-demo"
    default-active="dashboard"
    text-color="#fff"
    router
  >
    <div class="logo">
      <h3>☕ 智咖·云 Admin</h3>
    </div>
    
    <el-menu-item index="/dashboard">
      <el-icon><Odometer /></el-icon>
      <span>数据大屏</span>
    </el-menu-item>

    <el-menu-item index="/product/list">
      <el-icon><Coffee /></el-icon>
      <span>商品管理</span>
    </el-menu-item>

    <el-menu-item index="/order/list">
      <el-icon><List /></el-icon>
      <span>订单管理</span>
    </el-menu-item>

    <el-menu-item index="/member/list">
      <el-icon><User /></el-icon>
      <span>会员管理</span>
    </el-menu-item>

    <!-- 保留原有其他菜单但置于底部或隐藏，为了符合原型图，暂时保留但作为扩展 -->
    <el-sub-menu index="/marketing">
      <template #title>
        <el-icon><Present /></el-icon>
        <span>营销中心</span>
      </template>
      <el-menu-item index="/marketing/coupon">优惠券管理</el-menu-item>
    </el-sub-menu>

    <el-sub-menu index="/system">
      <template #title>
        <el-icon><Setting /></el-icon>
        <span>系统管理</span>
      </template>
      <el-menu-item index="/system/user">用户管理</el-menu-item>
      <el-menu-item index="/system/role">角色管理</el-menu-item>
      <el-menu-item index="/system/menu">菜单管理</el-menu-item>
    </el-sub-menu>

  </el-menu>
</template>

<script setup lang="ts">
</script>

<style scoped>
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  border-bottom: 1px solid #1f2d3d;
}
.el-menu {
  border-right: none;
}
</style>

