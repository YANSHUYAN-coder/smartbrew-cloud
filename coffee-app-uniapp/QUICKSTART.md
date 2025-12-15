# 快速开始指南

## 前置要求

- Node.js >= 14.0.0
- npm 或 yarn
- HBuilderX（可选，用于可视化开发）或 VS Code

## 安装步骤

### 1. 安装依赖

```bash
cd coffee-app-uniapp
npm install
```

### 2. 配置图标（可选）

如果需要显示底部导航栏图标，请将图标文件放置在 `static/tabbar/` 目录下：
- home.png / home-active.png
- menu.png / menu-active.png
- cart.png / cart-active.png
- profile.png / profile-active.png

如果暂时没有图标，tabBar 会只显示文字，不影响使用。

### 3. 运行项目

#### H5 开发（浏览器）

```bash
npm run dev:h5
```

访问 http://localhost:8080

#### 微信小程序开发

**方式一：使用 HBuilderX（推荐）**
1. 使用 HBuilderX 打开项目
2. 选择运行 -> 运行到小程序模拟器 -> 微信开发者工具

**方式二：使用命令行**
```bash
# 需要先安装 @dcloudio/uni-cli
npm install -g @dcloudio/uni-cli
uni -p mp-weixin
```

#### APP 开发

使用 HBuilderX 打开项目，选择运行到手机或模拟器

## 项目说明

### 主要功能

1. **首页** (`pages/home/index.vue`)
   - 门店定位
   - 搜索功能
   - Banner 展示
   - 功能入口
   - 商品推荐

2. **点单页** (`pages/menu/index.vue`)
   - 分类导航
   - 商品列表
   - 快速加购

3. **购物车** (`pages/cart/index.vue`)
   - 商品管理
   - 数量调整
   - 价格计算
   - 结算入口

4. **我的** (`pages/profile/index.vue`)
   - 用户信息
   - 数据统计
   - 订单管理
   - 功能列表

### 状态管理

使用 Pinia 管理购物车状态，文件位置：`store/cart.js`

### API 对接

API 工具文件：`utils/request.js`
API 接口文件：`api/` 目录

需要修改 `utils/request.js` 中的 `BASE_URL` 为实际后端地址。

### 数据

目前使用模拟数据，文件位置：`utils/data.js`

对接后端后，可以在各个页面中调用 API 替换模拟数据。

## 常见问题

### 1. 图标不显示

如果底部导航栏图标不显示，请检查：
- 图标文件是否存在
- 图标路径是否正确
- 图标格式是否为 PNG

### 2. 样式问题

项目使用 rpx 作为单位，会自动适配不同屏幕。如果样式异常，检查：
- 是否使用了正确的单位（rpx）
- 是否引入了全局样式

### 3. 网络请求失败

检查：
- `utils/request.js` 中的 BASE_URL 是否正确
- 后端服务是否启动
- 是否配置了跨域

## 下一步

1. 对接后端 API
2. 添加用户登录功能
3. 完善商品详情页
4. 实现订单结算流程
5. 添加支付功能
6. 优化用户体验

## 技术支持

如有问题，请查看：
- [uniapp 官方文档](https://uniapp.dcloud.net.cn/)
- [Vue 3 文档](https://cn.vuejs.org/)
- [Pinia 文档](https://pinia.vuejs.org/zh/)

