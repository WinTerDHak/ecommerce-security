$ErrorActionPreference = "Stop"

Write-Host "========================================="
Write-Host "  INITIALIZING LOCAL DEVELOPMENT...      "
Write-Host "========================================="

# 1. Start PostgreSQL
Write-Host "1. Checking Docker PostgreSQL..."
$containerStatus = docker inspect -f '{{.State.Running}}' ecommerce_postgres 2>$null
if ($containerStatus -ne 'true') {
    Write-Host "   Starting ecommerce_postgres container..."
    docker start ecommerce_postgres
    Start-Sleep -Seconds 3
} else {
    Write-Host "   ecommerce_postgres is already running."
}

# 2. Reset Frontend Environment for Local
Write-Host "2. Resetting frontend/.env for localhost..."
Set-Content -Path ".\frontend\.env" -Value "VITE_API_URL=http://localhost:8081/api"

# 3. Start Backend Server
Write-Host "3. Starting Backend Server (localhost:8081)..."
$env:FRONTEND_URL = "http://localhost:5173"
# Updated JWT_SECRET to 64 chars to fix the WeakKeyException (HS512 requires 512 bits)
$env:JWT_SECRET = "TESTSECRETKEY12345678901234567890TESTSECRETKEY12345678901234567890"
$env:JWT_EXPIRATION_MS = "86400000"
$env:JWT_REFRESH_EXPIRATION_MS = "604800000"

Start-Process -FilePath "cmd.exe" -ArgumentList "/c .\mvnw.cmd spring-boot:run" -WorkingDirectory ".\backend" -WindowStyle Normal

# 4. Start Frontend Server
Write-Host "4. Starting Frontend Server (localhost:5173)..."
Start-Process -FilePath "cmd.exe" -ArgumentList "/c npm run dev" -WorkingDirectory ".\frontend" -WindowStyle Normal

# 5. Print Final Summary
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  LOCAL DEVELOPMENT ENVIRONMENT IS UP    " -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host " Frontend: http://localhost:5173" -ForegroundColor White
Write-Host " Backend:  http://localhost:8081" -ForegroundColor White
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Close the opened 'cmd' windows if you want to stop the servers."
