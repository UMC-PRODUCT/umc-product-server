#!/bin/bash
set -euo pipefail
export PATH="/usr/bin:/bin:$PATH"

# Configurable defaults
APP_HOME=${APP_HOME:-/opt/umc-product}
JAR_NAME=${JAR_NAME:-umc-product-backend.jar}
JAR_FILE=${JAR_FILE:-app/${JAR_NAME}}
PID_FILE=${PID_FILE:-app/${JAR_NAME%.jar}.pid}
SPRING_PROFILE=${SPRING_PROFILE:-dev}
APP_USER=${APP_USER:-ubuntu}

echo "=== [BEFORE_INSTALL] 배포 준비 ==="

# 기존 애플리케이션 안전하게 종료 (프로세스 이름 또는 JAR 파일로 매칭)
PROCESS_MATCH=${PROCESS_MATCH:-${JAR_NAME}}
if pgrep -f "${PROCESS_MATCH}" > /dev/null; then
    echo "☕ 기존 애플리케이션을 안전하게 종료합니다..."
    pkill -SIGTERM -f "${PROCESS_MATCH}" || true

    for i in {1..10}; do
        if ! pgrep -f "${PROCESS_MATCH}" > /dev/null; then
            echo "   ✅ 기존 애플리케이션 종료 완료 (${i}초 소요)"
            break
        fi
        sleep 1
    done

    if pgrep -f "${PROCESS_MATCH}" > /dev/null; then
        echo "   🔨 강제 종료를 진행합니다..."
        pkill -SIGKILL -f "${PROCESS_MATCH}" || true
        sleep 2
        echo "   ✅ 기존 애플리케이션 강제 종료 완료"
    fi
else
    echo "☕ 실행 중인 애플리케이션이 없습니다"
fi

# PID 파일 정리
rm -f "${PID_FILE}" 2>/dev/null || true

# 배포 디렉토리 생성 및 권한 설정
echo "📁 배포 디렉토리 생성 및 권한 설정..."
mkdir -p "${APP_HOME}/app" "${APP_HOME}/scripts" "${APP_HOME}/logs"
chown -R "${APP_USER}" "${APP_HOME}" 2>/dev/null || true

# jq 설치 확인 및 설치 (선택적)
if ! command -v jq &> /dev/null; then
    echo "🔧 jq가 설치되어 있지 않습니다. (선택 사항)"
    if command -v yum &> /dev/null; then
        yum install -y jq &>/dev/null 2>&1 && echo "✅ jq 설치 완료 (yum)" || true
    elif command -v apt-get &> /dev/null; then
        apt-get install -y jq &>/dev/null 2>&1 && echo "✅ jq 설치 완료 (apt-get)" || true
    else
        echo "⚠️  패키지 매니저를 찾을 수 없습니다. 수동 설치 필요할 수 있습니다."
    fi
else
    echo "✅ jq가 이미 설치되어 있습니다"
fi

# 기존 JAR 파일 삭제 (있다면)
if [ -f "${APP_HOME}/${JAR_FILE}" ]; then
    echo "🗑️  기존 JAR 파일 삭제: ${APP_HOME}/${JAR_FILE}"
    rm -f "${APP_HOME}/${JAR_FILE}"
fi

echo "=== [BEFORE_INSTALL] 완료 ==="
