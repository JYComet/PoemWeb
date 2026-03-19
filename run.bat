@echo off
chcp 65001

REM 诗韵雅集 - 一键运行脚本
REM 2026-03-12

echo ====================================
echo 诗韵雅集 - 启动脚本
echo ====================================

REM 检查Python是否安装
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未检测到Python，请先安装Python 3.6或以上版本
    pause
    exit /b 1
)

echo 检测到Python，版本：
python --version

REM 检查并安装Flask
echo 检查Flask依赖...
pip show flask >nul 2>&1
if %errorlevel% neq 0 (
    echo 未安装Flask，正在安装...
    python -m pip install flask
    if %errorlevel% neq 0 (
        echo 安装Flask失败，请检查网络连接
        pause
        exit /b 1
    )
    echo Flask安装成功
) else (
    echo Flask已安装
)

REM 启动后端服务
echo ====================================
echo 启动后端服务...
echo ====================================
echo 服务将运行在 http://localhost:5000
echo 请在浏览器中访问上述地址
echo 按 Ctrl+C 停止服务
echo ====================================

python backend\app.py

pause