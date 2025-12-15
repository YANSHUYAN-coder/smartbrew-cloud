# 智咖云 APP - uniapp 版本

基于 uniapp 框架开发的咖啡点单应用，支持多端发布（H5、小程序、APP）。

## 技术栈

- **框架**: uniapp (Vue 3)
- **状态管理**: Pinia
- **构建工具**: Vite
- **UI**: 自定义样式（参考原 React 原型设计）

## 项目结构

```
coffee-app-uniapp/
├── pages/              # 页面目录
│   ├── home/          # 首页
│   ├── menu/          # 点单页
│   ├── cart/          # 购物车
│   └── profile/       # 我的
├── store/             # 状态管理
│   └── cart.js        # 购物车状态
├── utils/             # 工具函数
│   └── data.js        # 模拟数据
├── static/            # 静态资源
├── App.vue            # 应用入口
├── main.js            # 主入口文件
├── pages.json         # 页面配置
├── manifest.json      # 应用配置
└── vite.config.js     # Vite 配置
```

## 功能特性

### 首页
- 门店定位与搜索
- 沉浸式 Banner 轮播
- 功能金刚区（到店取、外卖、礼品卡、会员）
- 猜你喜欢商品推荐

### 点单页
- 左侧分类导航
- 右侧商品列表
- 商品搜索
- 快速加入购物车

### 购物车
- 商品列表展示
- 数量增减控制
- 总价计算
- 结算功能

### 我的
- 用户信息展示
- 数据概览（积分、优惠券、余额）
- 订单管理
- 功能列表

## 安装与运行

### 安装依赖

```bash
npm install
# 或
yarn install
```

### 开发运行

```bash
# H5 开发
npm run dev:h5

# 微信小程序开发
npm run dev:mp-weixin

# APP 开发
npm run dev:app
```

### 构建打包

```bash
# H5 构建
npm run build:h5

# 微信小程序构建
npm run build:mp-weixin

# APP 构建
npm run build:app
```

## 开发说明

### 状态管理

使用 Pinia 进行状态管理，购物车状态存储在 `store/cart.js` 中。

```javascript
import { useCartStore } from '@/store/cart.js'
const cartStore = useCartStore()
```

### 页面路由

使用 uniapp 的 tabBar 进行底部导航，页面路由配置在 `pages.json` 中。

### 样式说明

- 使用 rpx 作为单位，适配不同屏幕
- 主色调：`#6f4e37`（咖啡色）
- 支持动画效果（fade-in、slide-up 等）

## 注意事项

1. 底部导航栏图标需要放置在 `static/tabbar/` 目录下
2. 图片资源使用网络图片，生产环境建议使用本地资源或 CDN
3. 数据目前为模拟数据，需要对接后端 API
4. 部分功能（如搜索、结算）需要进一步完善

## 后续开发

- [ ] 对接后端 API
- [ ] 完善搜索功能
- [ ] 实现结算页面
- [ ] 添加商品详情页
- [ ] 实现用户登录/注册
- [ ] 添加订单管理功能
- [ ] 优化图片加载
- [ ] 添加骨架屏加载

## 许可证

MIT

