import react from '@vitejs/plugin-react'
import { defineConfig } from 'vitest/config'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // En développement, les appels /api partent vers le backend Spring Boot.
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
  test: {
    // DOM simulé : les tests tournent sans navigateur ni backend.
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    // Heures affichées stables quel que soit le poste qui lance les tests.
    env: { TZ: 'UTC' },
  },
})
