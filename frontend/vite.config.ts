import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173, // Matches backend CORS configuration
    https: false, // Set to true in production for PWA
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // Updated backend port
        changeOrigin: true,
        secure: false,
        headers: {
          'X-Requested-With': 'XMLHttpRequest' // Required by backend security
        }
      }
    }
  },
  build: {
    sourcemap: false, // Disabled for security in production
    target: 'es2020',
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    },
    rollupOptions: {
      output: {
        manualChunks: {
          'react-vendor': ['react', 'react-dom'],
          'query-vendor': ['@tanstack/react-query'],
          'form-vendor': ['react-hook-form', 'zod'],
          'chart-vendor': ['recharts'],
          'medical-utils': ['dompurify'] // Will add DOMPurify
        }
      }
    }
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts']
  }
})
