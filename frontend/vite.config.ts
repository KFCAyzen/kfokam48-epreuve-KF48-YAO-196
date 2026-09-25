import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // En développement, les appels /api partent vers le backend Spring Boot.
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
