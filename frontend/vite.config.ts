import { fileURLToPath, URL } from 'node:url'

import { configDefaults, defineConfig } from 'vitest/config'
import { loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [vue(), tailwindcss()],
    // E2E всегда идут на моках, что бы ни стояло в локальном .env.local.
    define: mode === 'e2e' ? { 'import.meta.env.VITE_API_MOCK': JSON.stringify('true') } : {},
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    server: {
      proxy: {
        '/api': {
          target: env.API_PROXY_TARGET || 'http://localhost:8080',
          changeOrigin: true,
          // Бэк ставит cookie входа с Secure, а локально мы на http — Safari и заход по IP её отбрасывают.
          // Только в dev-прокси снимаем Secure; на стенде с HTTPS cookie остаётся как у бэка.
          // Со старой cookie (бэк перезапущен, вход истёк) бэк отвечает 401 даже на открытые запросы,
          // а HttpOnly-cookie скрипт стереть не может — на такой 401 прокси сам её удаляет.
          configure: (proxy) => {
            proxy.on('proxyRes', (res, req) => {
              const cookies = [...(res.headers['set-cookie'] ?? [])].map((c) =>
                c.replace(/;\s*Secure/gi, ''),
              )
              if (res.statusCode === 401 && /(?:^|;\s*)jwt=/.test(req.headers.cookie ?? ''))
                cookies.push('jwt=; Path=/; Max-Age=0; HttpOnly; SameSite=Strict')
              if (cookies.length) res.headers['set-cookie'] = cookies
            })
          },
        },
      },
    },
    test: {
      environment: 'jsdom',
      setupFiles: ['src/test-setup.ts'],
      exclude: [...configDefaults.exclude, 'e2e/**'],
    },
  }
})
