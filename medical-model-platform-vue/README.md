# 医疗影像模型管理平台 - 前端项目

> 基于Vue 3的现代化医疗影像AI处理平台前端应用

## 🎯 项目概述

这是医疗影像模型管理平台的前端项目，采用现代化的Vue 3技术栈构建，提供完整的用户界面和交互体验。项目与后端API紧密集成，实现用户认证、权限管理、积分系统、系统配置等核心功能。

## 🏗️ 技术架构

### 核心技术栈
- **Vue 3.2.13** - 渐进式JavaScript框架
- **Vue Router 4** - 官方路由管理器
- **Pinia** - Vue 3推荐的状态管理库
- **Element Plus** - 基于Vue 3的组件库
- **Axios** - HTTP客户端
- **SCSS** - CSS预处理器

### 项目结构
```
src/
├── api/           # API接口封装
│   ├── auth/      # 认证相关API
│   ├── user/      # 用户管理API
│   ├── permission/ # 权限管理API
│   ├── credit/    # 积分系统API
│   ├── system/    # 系统配置API
│   └── index.js   # API统一导出
├── assets/        # 静态资源
├── components/    # 公共组件
│   ├── ConfirmDialog.vue  # 通用确认弹窗组件
│   ├── MessageToast.vue   # 消息提示组件
│   └── layout/            # 布局相关组件
├── layouts/       # 页面布局
│   ├── DefaultLayout.vue    # 默认布局
│   └── AuthLayout.vue       # 认证页面布局
├── pages/         # 页面组件
│   ├── auth/      # 认证页面
│   ├── dashboard/ # 仪表板
│   ├── user/      # 用户管理
│   ├── permission/ # 权限管理
│   ├── credit/    # 积分系统
│   └── system/    # 系统配置
├── router/        # 路由配置
│   ├── index.js   # 路由主配置
│   ├── routes.js  # 路由模块
│   └── guards.js  # 路由守卫
├── stores/        # 状态管理
│   ├── auth.js    # 认证状态
│   ├── user.js    # 用户状态
│   ├── permission.js # 权限状态
│   └── index.js   # 状态导出
├── styles/        # 全局样式
│   ├── variables.scss # CSS变量
│   ├── mixins.scss    # 混入
│   ├── common.scss    # 通用样式
│   └── index.scss     # 样式入口
├── utils/         # 工具函数
│   ├── request.js         # HTTP请求封装
│   ├── auth.js            # 认证工具
│   ├── storage.js         # 存储工具
│   ├── validate.js        # 验证工具
│   ├── confirmDialog.js   # 确认弹窗工具函数
│   ├── avatarUtils.js     # 头像处理工具
│   └── index.js           # 工具导出
└── views/         # 视图组件
```

## 🚀 快速开始

### 环境要求
- Node.js 16.0+
- npm 8.0+ 或 yarn 1.22+

### 安装依赖
```bash
npm install
```

### 开发环境启动
```bash
npm run serve
```

### 生产环境构建
```bash
npm run build
```

### 代码检查
```bash
npm run lint
```

## 🔧 功能特性

## 🎨 通用组件使用指南

### UserProfile 个人信息页面组件

完整的个人信息管理页面，支持用户查看和编辑个人信息、上传头像、修改密码等功能。采用GitHub风格的布局设计，左侧固定导航栏，右侧内容区域，提供专业的设置页面体验。

#### 功能特性
- **基本信息管理**: 昵称、真实姓名、邮箱、手机号、性别、个人简介
- **头像管理**: 支持JPG、PNG格式上传，自动压缩和预览
- **密码修改**: 安全的密码修改流程，包含强度验证
- **响应式设计**: 完美适配桌面端、平板端和移动端
- **表单验证**: 完整的客户端表单验证和错误提示

#### 使用方法
```javascript
// 在路由中配置
{
  path: 'profile',
  name: 'UserProfile',
  component: () => import('@/views/profile/index.vue'),
  meta: { 
    title: '个人信息',
    icon: 'user',
    permission: null
  }
}

// 在用户菜单中跳转
router.push('/profile')
```

#### 技术特点
- 基于Vue 3 Composition API构建
- 集成Element Plus组件库
- 支持深色模式
- 完整的TypeScript类型支持
- 模块化的状态管理
- GitHub风格的导航布局
- 响应式设计，完美适配各种设备
- 一页式布局，无滚动条设计

### ConfirmDialog 确认弹窗组件

替换了浏览器默认的 `confirm()` 弹窗，提供更好的用户体验和更丰富的自定义选项。

#### 基本用法

```javascript
import { showLogoutConfirm, showDeleteConfirm } from '@/utils/confirmDialog'

// 退出登录确认
const handleLogout = async () => {
  const confirmed = await showLogoutConfirm()
  if (confirmed) {
    await logout()
  }
}

// 删除确认
const handleDelete = async (itemName) => {
  const confirmed = await showDeleteConfirm(itemName)
  if (confirmed) {
    await deleteItem()
  }
}
```

#### 支持的类型
- **info**: 信息确认（蓝色主题）
- **warning**: 警告确认（橙色主题）  
- **danger**: 危险确认（红色主题）

#### 特性
- 🎨 现代化设计风格
- 📱 响应式布局
- 🎭 流畅动画效果
- 🔧 高度可定制
- ♿ 支持键盘操作

### 用户认证系统
- ✅ 用户登录/注册
- ✅ JWT Token管理
- ✅ 自动Token刷新
- ✅ 记住登录状态

### 权限管理系统
- ✅ 基于角色的访问控制(RBAC)
- ✅ 动态菜单权限

### 通用组件系统
- ✅ 现代化确认弹窗组件 (ConfirmDialog)
- ✅ 消息提示组件 (MessageToast)
- ✅ 个人信息页面组件 (UserProfile)
- ✅ 响应式设计，支持移动端
- ✅ 统一的UI风格和交互体验
- ✅ 路由权限控制
- ✅ 组件级权限控制

### 用户管理功能
- ✅ 个人资料管理
- ✅ 个人信息页面（完整实现）
- ✅ 头像上传和管理
- ✅ 密码修改
- ✅ 子账号管理(管理员)
- ✅ 用户状态管理

### 积分系统
- ✅ 积分余额查询
- ✅ 交易记录管理
- ✅ 积分分配(管理员)
- ✅ 积分类型管理

### 系统配置
- ✅ 配置项CRUD操作
- ✅ 配置分类管理
- ✅ 配置状态控制
- ✅ 批量操作支持

## 🎨 UI设计

### 设计原则
- **现代化**: 采用最新的设计趋势和交互模式
- **响应式**: 支持多种设备和屏幕尺寸
- **一致性**: 统一的视觉语言和交互规范
- **可访问性**: 符合WCAG 2.1 AA标准

### 主题系统
- 支持浅色/深色主题切换
- 可定制的色彩系统
- 响应式断点设计
- 统一的组件样式

## 🔒 安全特性

### 前端安全
- XSS防护
- CSRF防护
- 输入验证和过滤
- 敏感信息脱敏

### 权限控制
- 路由级权限控制
- 组件级权限控制
- API接口权限验证
- 数据权限控制

## 📱 响应式设计

### 断点设置
- **xs**: < 480px (手机)
- **sm**: < 768px (平板)
- **md**: < 992px (小桌面)
- **lg**: < 1200px (桌面)
- **xl**: < 1920px (大桌面)

### 适配策略
- 移动优先设计
- 弹性布局系统
- 触摸友好的交互
- 性能优化

## 🚀 性能优化

### 代码分割
- 路由懒加载
- 组件懒加载
- 第三方库按需引入

### 资源优化
- 图片懒加载
- 静态资源压缩
- CDN加速支持
- 缓存策略

## 🧪 测试策略

### 测试类型
- 单元测试 (Jest)
- 组件测试 (Vue Test Utils)
- E2E测试 (Cypress)
- 性能测试

### 测试覆盖
- 核心业务逻辑
- 用户交互流程
- 错误处理机制
- 边界条件测试

## 📦 部署说明

### 开发环境
```bash
# 启动开发服务器
npm run serve

# 访问地址
http://localhost:8080
```

### 生产环境
```bash
# 构建生产版本
npm run build

# 部署到Web服务器
# 将dist目录内容部署到服务器根目录
```

### 环境配置
```bash
# 开发环境
VUE_APP_BASE_API=http://localhost:8080/api

# 生产环境
VUE_APP_BASE_API=https://your-domain.com/api
```

## 🤝 开发规范

### 代码规范
- 遵循Vue 3 Composition API最佳实践
- 使用ESLint + Prettier进行代码格式化
- 遵循组件命名和文件组织规范
- 完善的注释和文档

### 提交规范
- 使用Conventional Commits规范
- 清晰的提交信息
- 功能分支开发模式
- 代码审查流程

## 📚 相关文档

- [Vue 3官方文档](https://vuejs.org/)
- [Vue Router 4文档](https://router.vuejs.org/)
- [Pinia状态管理](https://pinia.vuejs.org/)
- [Element Plus组件库](https://element-plus.org/)
- [项目API文档](http://localhost:8080/api/swagger-ui/index.html)

## 🐛 问题反馈

如果您在使用过程中遇到问题，请通过以下方式反馈：

1. 查看项目Issues
2. 提交新的Issue
3. 联系开发团队

## 📄 许可证

本项目采用MIT许可证，详见LICENSE文件。

---

> 💡 **提示**: 这是一个企业级的医疗影像管理平台前端项目，采用现代化的技术栈和最佳实践，为医疗AI应用提供完整的用户界面解决方案。
