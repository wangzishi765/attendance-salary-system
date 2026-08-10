@echo off
chcp 65001 >nul
echo ============================================
echo   启动 考勤薪资管理系统
echo ============================================

if not exist "backend\target\hrms.jar" (
  echo 未找到 backend\target\hrms.jar，请先运行 build.bat 进行构建。
  pause
  exit /b 1
)

echo 系统启动后请访问： http://localhost:8080
echo 默认管理员： admin / admin123
echo 默认员工：   E001 / 123456
echo （按 Ctrl+C 可停止）
echo.
java -jar backend\target\hrms.jar
pause
