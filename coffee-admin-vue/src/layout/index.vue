<template>
  <div class="common-layout">
    <el-container>
      <el-aside width="200px">
        <Sidebar/>
      </el-aside>
      <el-container>
        <el-header>
          <Header/>
        </el-header>
        <el-main>
          <router-view/>
        </el-main>
        <el-footer class="admin-footer" height="40px">
          <span>© 2026 SmartBrew 智咖·云 版权所有</span>
          <a href="https://beian.miit.gov.cn/" target="_blank">
            粤ICP备2026024110号-1
          </a>
          <div style="display:inline-block; margin-left:20px;">
            <img src="@/assets/ghs.png" style="vertical-align: middle; margin-right: 4px;"/>
            <a href="https://beian.mps.gov.cn/#/query/webSearch?code=44011102484703" rel="noreferrer" target="_blank">粤公网安备44011102484703号</a>
          </div>
        </el-footer>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import Sidebar from './components/Sidebar.vue'
import Header from './components/Header.vue'
</script>

<style scoped>
.common-layout {
  height: 100vh;
}

.el-container {
  height: 100%;
}

.el-aside {
  background-color: #304156;
  color: #fff;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}

.admin-footer {
  background-color: #f0f2f5;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 13px;
  color: #939393;
}

.admin-footer a {
  color: #939393;
  text-decoration: none;
  margin-left: 15px;
}

.admin-footer a:hover {
  text-decoration: underline;
}
</style>

