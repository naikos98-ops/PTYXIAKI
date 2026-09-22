import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/admin': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        bypass: (req) => {
          if (req.url.startsWith('/admin/dashboard')) {
            return req.url
          }
        }
      },
      '/logout': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/user': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/login': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        bypass: (req) => {
          if (req.method === 'GET') {
            return req.url
          }
        }
      },
      '/register': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        bypass: (req) => {
          if (req.method === 'GET') {
            return req.url // Serve Vue page for GET
          }
        }
      }
    }
  }
})
