#!/bin/bash
set -euo pipefail
export PATH="/usr/bin:/bin:$PATH"

# Configurable defaults
APP_HOME=${APP_HOME:-/opt/umc-product}
PID_FILE=${PID_FILE:-app/${JAR_NAME:-umc-product-backend.jar}%.jar.pid}
PID_FILE=${PID_FILE:-app/${JAR_NAME:-umc-product-backend.jar}}
PID_FILE=${PID_FILE:-app/${JAR_NAME:-umc-product-backend.jar}.pid}
PID_FILE=${PID_FILE:-app/${JAR_NAME:-umc-product-backend.jar}.pid}
PID_FILE=${PID_FILE:-app/${JAR_NAME:-umc-product-backend.jar}.pid}
PID_FILE=${PID_FILE:-app/${JAR_NAME:-umc-product-backend.jar}.pid}
PID_FILE=${PID_FILE}
PORT=${PORT:-8080}
PROCESS_MATCH=${PROCESS_MATCH:-${JAR_NAME:-umc-product-backend.jar}}

echo "=== [APPLICATION_STOP] 애플리케이션 종료 ==="

cd "${APP_HOME}" || {
    echo "❌ 디렉토리 이동 실패: ${APP_HOME}"
    exit 1
}

echo ""
echo "☕ 1. Spring Boot 애플리케이션 종료 중..."

if [ -f "${PID_FILE}" ]; then
    PID=$(cat "${PID_FILE}")

    if ps -p "$PID" > /dev/null 2>&1; then
        echo "   🛑 SIGTERM 신호 전송 (PID: $PID)"
        kill -SIGTERM "$PID" || true

        echo "   ⏳ Graceful Shutdown 대기 중... (최대 30초)"
        for i in {1..30}; do
            if ! ps -p "$PID" > /dev/null 2>&1; then
                echo "   ✅ 애플리케이션이 정상 종료되었습니다 (${i}초 소요)"
                break
            fi
            sleep 1
        done

        if ps -p "$PID" > /dev/null 2>&1; then
            echo "   ⚠️  30초 내에 종료되지 않았습니다"
            echo "   🔨 강제 종료를 수행합니다 (SIGKILL)"
            kill -9 "$PID" 2>/dev/null || true
            sleep 2
            echo "   ✅ 프로세스를 강제 종료했습니다"
        fi
    else
        echo "   ℹ️  애플리케이션이 이미 종료되어 있습니다"
    fi

    rm -f "${PID_FILE}" || true
else
    echo "   ℹ️  PID 파일이 없습니다: ${PID_FILE}"
fi

# 포트 사용 프로세스 강제 종료 (좀비 프로세스 대응)
if command -v lsof >/dev/null 2>&1; then
    if lsof -ti:${PORT} 2>/dev/null | xargs -r kill -9 2>/dev/null; then
        echo "   🔫 포트 ${PORT}을 사용하는 좀비 프로세스를 강제 종료했습니다"
        sleep 1
    fi
else
    # fallback: ss + awk
    JAVA_PROCESS=$(ss -tlnp 2>/dev/null | awk -v p=":${PORT}" '$0~p{match($0, /pid=([0-9]+)/, arr); print arr[1]; exit}' || true)
    if [ -n "$JAVA_PROCESS" ]; then
        echo "   🔫 포트 ${PORT}을 사용하는 좀비 프로세스 강제 종료 (PID: $JAVA_PROCESS)"
        kill -9 "$JAVA_PROCESS" 2>/dev/null || true
        sleep 1
    fi
fi

echo ""
echo "=== [APPLICATION_STOP] 완료 ==="