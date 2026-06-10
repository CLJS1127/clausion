# Clausion 프론트엔드 로컬 실행 (vite dev, HTTPS https://localhost:5173)
# /api, /ws-chat 는 vite proxy 로 http://localhost:8080 (백엔드) 로 전달됨.

Set-Location "$PSScriptRoot\frontend"
npm run dev
