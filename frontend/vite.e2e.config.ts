// E2E 브라우저 테스트용 HTTP 설정 (basic-ssl 제외, 포트 5174)
// 사용: npx vite --config vite.e2e.config.ts
// 백엔드 CORS 허용 목록에 5174가 없으므로 Origin을 5173으로 재작성한다.
import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const proxyTarget = env.VITE_PROXY_TARGET || 'http://localhost:8080'
  const rewriteOrigin = {
    configure: (proxy: { on: (e: string, cb: (proxyReq: { setHeader: (k: string, v: string) => void }) => void) => void }) => {
      proxy.on('proxyReq', (proxyReq) => {
        proxyReq.setHeader('origin', 'http://localhost:5173')
      })
    },
  }

  return {
    plugins: [react(), tailwindcss()],
    server: {
      port: 5174,
      proxy: {
        '/api': {
          target: proxyTarget,
          changeOrigin: true,
          ...rewriteOrigin,
        },
        '/ws-chat': {
          target: proxyTarget,
          changeOrigin: true,
          ws: true,
          ...rewriteOrigin,
        },
      },
    },
  }
})
