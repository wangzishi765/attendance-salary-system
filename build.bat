@echo off
chcp 65001 >nul
echo ============================================
echo   构建 考勤薪资管理系统（前端 + 后端）
echo ============================================

echo.
echo [1/2] 构建前端...
cd frontend
call npm install
call npm run build
if %errorlevel% neq 0 (
  echo 前端构建失败，请确认已安装 Node 18+。
  cd ..
  pause
  exit /b 1
)
cd ..

echo.
echo [2/2] 打包后端（含前端）...
cd backend
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
  echo 后端打包失败，请确认已安装 JDK 8+ 与 Maven。
  cd ..
  pause
  exit /b 1
)
cd ..

echo.
echo ============================================
echo   构建完成！可运行 run.bat 启动，或：
echo   java -jar backend\target\hrms.jar
echo ============================================
pause
