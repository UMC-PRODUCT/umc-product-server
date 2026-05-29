#!/bin/bash
set -euo pipefail

DEPLOY_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "${DEPLOY_ROOT}/.env.deploy"
APP_REPLICAS="${APP_REPLICAS:-1}"

export PATH="$PATH:/usr/local/bin:/usr/bin"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "배포 시작: ${ECR_IMAGE_NAME}:${IMAGE_TAG}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if ! command -v docker >/dev/null 2>&1; then
  echo "docker를 찾을 수 없습니다." >&2
  exit 1
fi
DOCKER_BIN="$(command -v docker)"

if "${DOCKER_BIN}" compose version >/dev/null 2>&1; then
  COMPOSE_CMD="${DOCKER_BIN} compose"
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE_CMD="docker-compose"
else
  echo "docker compose를 찾을 수 없습니다." >&2
  exit 1
fi

echo "[1] ECR 인증 (IAM Role)"
ECR_PASSWORD="$(aws ecr get-login-password --region "${AWS_DEFAULT_REGION}")"
"${DOCKER_BIN}" login --username AWS --password "${ECR_PASSWORD}" "${ECR_REGISTRY}"

echo "[2] 앱 디렉토리 준비"
mkdir -p "${SERVER_APP_DIRECTORY}"
cd "${SERVER_APP_DIRECTORY}"

echo "[3] Compose 파일 복사"
cp "${DEPLOY_ROOT}/docker/develop/docker-compose.yml" ./docker-compose.yml
cp "${DEPLOY_ROOT}/docker/develop/nginx.conf" ./nginx.conf

echo "[4] S3에서 app.env 다운로드"
aws s3 cp "${S3_APP_ENV_URI}" app.env

echo "[5] .env 파일 생성"
{
  echo "NGINX_CONTAINER_NAME=umc-product-nginx-dev"
  echo "IMAGE_NAME=${ECR_IMAGE_NAME}"
  echo "IMAGE_TAG=${IMAGE_TAG}"
  echo "HTTP_PORT=8080"
  echo "MANAGEMENT_PORT=9090"
  echo "SPRING_PROFILES_ACTIVE=dev"
  echo "APP_REPLICAS=${APP_REPLICAS}"
  echo "POSTGRES_CONTAINER_NAME=umc-product-postgres-dev"
  echo "POSTGRES_DB=development"
  echo "POSTGRES_USER=${POSTGRES_USER}"
  echo "POSTGRES_PASSWORD=${POSTGRES_PASSWORD}"
  echo "VALKEY_CONTAINER_NAME=umc-product-valkey-dev"
} > .env

echo "[6] 이미지 Pull"
${COMPOSE_CMD} pull app

echo "[7] 컨테이너 시작 및 헬스체크 대기"
if ! ${COMPOSE_CMD} up -d --scale "app=${APP_REPLICAS}" --remove-orphans --wait; then
  echo "컨테이너 헬스체크 실패. 로그:"
  ${COMPOSE_CMD} logs --tail=100 postgres || true
  ${COMPOSE_CMD} logs --tail=100 valkey   || true
  ${COMPOSE_CMD} logs --tail=100 app      || true
  ${COMPOSE_CMD} logs --tail=100 nginx    || true
  exit 1
fi

${COMPOSE_CMD} ps

echo "[8] 배포 메타데이터 저장"
cp "${DEPLOY_ROOT}/.env.deploy" /etc/codedeploy-app.env

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "배포 완료"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
