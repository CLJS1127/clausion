# Clausion 백엔드 로컬 실행 — 호스트 설치 인프라 사용
#   PostgreSQL 18 (service: postgresql-x64-18)  localhost:5432
#   Redis        (service: Redis)                localhost:6379
#   RabbitMQ     (service: RabbitMQ)             localhost:5672
# 접속 정보는 application.yml 기본값과 .env 를 따른다.
#
# 시크릿(OPENAI_API_KEY 등)은 이 파일에 직접 쓰지 말고 .env 에 둔다.
# (.env 는 git에 올라가지 않는다 — 템플릿은 .env.example 참고)
#
# Gradle 데몬이 환경변수를 forked JVM 에 전달하지 못하는 이슈가 있어
# bootJar 빌드 후 java -jar 로 직접 실행한다. 코드 수정 후 재실행하면 재빌드된다.

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

# .env 의 환경변수 로드
if (Test-Path "$root\.env") {
    Get-Content "$root\.env" | Where-Object { $_ -match '^\s*[A-Z_][A-Z0-9_]*=' } | ForEach-Object {
        $name, $value = $_ -split '=', 2
        Set-Item -Path "env:$($name.Trim())" -Value $value.Trim()
    }
} else {
    Write-Warning ".env 파일이 없습니다. .env.example 을 복사해 값을 채워주세요. (AI 기능 없이 부팅은 가능)"
}
if (-not $env:SPRING_PROFILES_ACTIVE) { $env:SPRING_PROFILES_ACTIVE = "dev" }

& "$root\backend\gradlew.bat" -p "$root\backend" bootJar -x test --console=plain
if ($LASTEXITCODE -ne 0) { throw "bootJar build failed" }

$jar = Get-ChildItem "$root\backend\build\libs\*.jar" | Select-Object -First 1
Write-Host "Starting $($jar.Name) (host infra) ..."
& java -jar $jar.FullName
