#!/bin/bash
set -euo pipefail
export PATH="/usr/bin:/bin:$PATH"

# Configurable defaults
APP_HOME=${APP_HOME:-/opt/umc-product}
JAR_NAME=${JAR_NAME:-umc-product-backend.jar}
JAR_FILE=${JAR_FILE:-app/${JAR_NAME}}
PID_FILE=${PID_FILE:-app/${JAR_NAME%.jar}.pid}
SPRING_PROFILE=${SPRING_PROFILE:-dev}
PORT=${PORT:-8080}
APP_USER=${APP_USER:-ubuntu}

echo "=== [APPLICATION_START] 애플리케이션 시작 ==="

cd "${APP_HOME}" || {
    echo "❌ 디렉토리 이동 실패: ${APP_HOME}"
    exit 1
}

echo "☕ 1. Spring Boot 애플리케이션 시작 중..."

# JAR 파일 확인
if [ -f "${JAR_FILE}" ]; then
    echo "   📄 JAR 파일 확인됨: ${JAR_FILE}"
else
    echo "   ❌ JAR 파일을 찾을 수 없습니다: ${JAR_FILE}"
    exit 1
fi

# 기존 프로세스 종료 (PID 파일 기반)
if [ -f "${PID_FILE}" ]; then
    OLD_PID=$(cat "${PID_FILE}")
    if ps -p "${OLD_PID}" > /dev/null 2>&1; then
        echo "   🛑 기존 애플리케이션 프로세스 종료 중 (PID: ${OLD_PID})"
        kill -SIGTERM "${OLD_PID}" || true
        sleep 5
        if ps -p "${OLD_PID}" > /dev/null 2>&1; then
            kill -SIGKILL "${OLD_PID}" || true
        fi
    fi
    rm -f "${PID_FILE}" || true
fi

# JVM 옵션
JVM_OPTS=(
  -Xms512m
  -Xmx1024m
  -XX:+UseG1GC
  -Xlog:gc*:file=${APP_HOME}/logs/gc.log:time,tags:filecount=5,filesize=10M
  -Duser.timezone=Asia/Seoul
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=${APP_HOME}/logs/heapdump.hprof
)

export SPRING_PROFILES_ACTIVE="${SPRING_PROFILE}"
echo "   🌍 환경 프로파일: ${SPRING_PROFILE}"

echo "   🚀 Spring Boot 애플리케이션 시작 중 (포트: ${PORT})..."
nohup java "${JVM_OPTS[@]}" \
    -Dspring.profiles.active="${SPRING_PROFILE}" \
    -jar "${JAR_FILE}" \
    > "${APP_HOME}/logs/application.log" 2>&1 &

# PID 저장
APP_PID=$!
echo "${APP_PID}" > "${PID_FILE}"
chown "${APP_USER}" "${PID_FILE}" 2>/dev/null || true
echo "   ✅ 애플리케이션 시작 완료 (PID: ${APP_PID})"
