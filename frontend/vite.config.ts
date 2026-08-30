import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import { resolve } from 'path'

export default defineConfig({
  plugins: [
    vue(),
    vueJsx({
      oxc: true
    })
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    allowedHosts: ['thexuong.xuansown.id.vn'],
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      },
      '/oauth2': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        configure: (proxy, _options) => {
          proxy.on('proxyReq', (proxyReq, req, _res) => {
            const isLocal = req.headers.host && (req.headers.host.includes('localhost') || req.headers.host.includes('127.0.0.1'))
            proxyReq.setHeader('X-Forwarded-Proto', isLocal ? 'http' : 'https')
            proxyReq.setHeader('X-Forwarded-Host', req.headers.host || 'thexuong.xuansown.id.vn')
          })
        }
      },
      '/login/oauth2': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        configure: (proxy, _options) => {
          proxy.on('proxyReq', (proxyReq, req, _res) => {
            const isLocal = req.headers.host && (req.headers.host.includes('localhost') || req.headers.host.includes('127.0.0.1'))
            proxyReq.setHeader('X-Forwarded-Proto', isLocal ? 'http' : 'https')
            proxyReq.setHeader('X-Forwarded-Host', req.headers.host || 'thexuong.xuansown.id.vn')
          })
        }
      },
      '/vnpay-return': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        configure: (proxy, _options) => {
          proxy.on('proxyReq', (proxyReq, req, _res) => {
            const isLocal = req.headers.host && (req.headers.host.includes('localhost') || req.headers.host.includes('127.0.0.1'))
            proxyReq.setHeader('X-Forwarded-Proto', isLocal ? 'http' : 'https')
            proxyReq.setHeader('X-Forwarded-Host', req.headers.host || 'thexuong.xuansown.id.vn')
          })
        }
      }
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: true
  }
})
