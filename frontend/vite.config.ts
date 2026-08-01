import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  // Where the Vite dev server forwards /api requests. Defaults to the local
  // API Gateway; override with GATEWAY_PROXY_TARGET when the gateway runs
  // elsewhere (e.g. a different host or port).
  const gatewayTarget = env.GATEWAY_PROXY_TARGET ?? 'http://localhost:8080'

  return {
    plugins: [react()],
    server: {
      // The browser calls same-origin /api/*; Vite proxies to the gateway.
      // This keeps the browser talking only to the dev server (no CORS, and it
      // works even when the gateway is not reachable at the browser's own
      // localhost).
      proxy: {
        '/api': {
          target: gatewayTarget,
          changeOrigin: true,
        },
      },
    },
  }
})
