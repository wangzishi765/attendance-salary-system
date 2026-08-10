import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  // 使用相对路径，保证打包后放到 SpringBoot static 目录也能正常访问
  base: './',
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    open: true,
    proxy: {
      // 开发环境把 /api 代理到后端
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    // 打包直接输出到后端静态资源目录，实现单 jar 部署
    outDir: '../backend/src/main/resources/static',
    emptyOutDir: true
  }
})
