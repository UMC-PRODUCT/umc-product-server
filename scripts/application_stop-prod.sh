#!/bin/bash
set -euo pipefail
export PATH="/usr/bin:/bin:$PATH"

# Configurable defaults
APP_HOME=${APP_HOME:-/opt/umc-product}
JAR_NAME=${JAR_NAME:-umc-product-backend.jar}
PID_FILE=${PID_FILE:-app/${JAR_NAME%.jar}.pid}
PORT=${PORT:-8080}

echo "=== [APPLICATION_STOP] 강제 종료 확인 ==="

cd "${APP_HOME}" || {
    echo "❌ 디렉토리 이동 실패: ${APP_HOME}"
    exit 1
}

echo ""
echo "☕ 1. Spring Boot 애플리케이션 종료 여부 확인..."

if [ -f "${PID_FILE}" ]; then
    PID=$(cat "${PID_FILE}")

    if ps -p "$PID" > /dev/null 2>&1; then
        echo "   ⚠️  프로세스가 여전히 실행 중입니다 (PID: $PID)"
        echo "   🔨 강제 종료를 수행합니다 (SIGKILL)"
        kill -9 "$PID" 2>/dev/null || true
        sleep 2

        if ps -p "$PID" > /dev/null 2>&1; then
            echo "   ❌ 애플리케이션 강제 종료 실패"
            exit 1
        else
            echo "   ✅ 프로세스를 강제 종료했습니다"
        fi
    else
        echo "   ✅ Graceful Shutdown이 정상 완료되었습니다"
    fi

    rm -f "${PID_FILE}"
else
    echo "   ℹ️  PID 파일이 없습니다: ${PID_FILE}"
fi

# 포트 사용 프로세스 강제 종료
if command -v lsof >/dev/null 2>&1; then
    if lsof -ti:${PORT} 2>/dev/null | xargs -r kill -9 2>/dev/null; then
        echo "   🔫 포트 ${PORT}을 사용하는 좀비 프로세스를 강제 종료했습니다"
        sleep 1
    fi
fi

echo ""
echo "=== [APPLICATION_STOP] 완료 ==="
