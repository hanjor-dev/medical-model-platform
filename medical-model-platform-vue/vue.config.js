const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 8081,
    open: true,
    // 关闭开发环境覆盖层中的运行时报错弹层，避免 ResizeObserver 噪音
    client: {
      overlay: false
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8080',  // 后端服务地址，不包含/api
        changeOrigin: true,
        pathRewrite: {
          '^/api': '/api'  // 将/api前缀重写为空，这样/auth/login会变成/auth/login
        }
      },
      // 临时禁用 WebSocket 代理，后续将重建通用组件
    }
  }
})
