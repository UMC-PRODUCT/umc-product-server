#!/usr/bin/env bash
# Spring Boot jar → Docker image 빌드 (재사용 가능).
#
# 사용법:
#   ./scripts/build-app-image.sh [IMAGE_TAG]
#
# 환경변수:
#   IMAGE_NAME    (default: umc-product-server)
#   SKIP_GRADLE   (1이면 ./gradlew bootJar 단계를 건너뜀, 이미 build/libs/*.jar 있을 때)
#   PLATFORM      (예: linux/amd64) — 비우면 호스트 native
#
# 정상 종료 시 마지막 라인에 "<image>:<tag>"만 출력되므로 다른 스크립트와 파이프 가능.
#   IMG=$(./scripts/build-app-image.sh | tail -n1)
#   ./scripts/measure-boot-time.sh "$IMG"

set -euo pipefail

IMAGE_NAME="${IMAGE_NAME:-umc-product-server}"
DEFAULT_TAG="bootbench-$(git rev-parse --short HEAD 2>/dev/null || date +%Y%m%d-%H%M%S)"
IMAGE_TAG="${1:-$DEFAULT_TAG}"
SKIP_GRADLE="${SKIP_GRADLE:-0}"
PLATFORM="${PLATFORM:-}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

{
  echo "════════════════════════════════════════"
  echo " App image build"
  echo "  IMAGE      : ${IMAGE_NAME}:${IMAGE_TAG}"
  echo "  SKIP_GRADLE: ${SKIP_GRADLE}"
  [[ -n "$PLATFORM" ]] && echo "  PLATFORM   : ${PLATFORM}"
  echo "════════════════════════════════════════"
} >&2

if [[ "$SKIP_GRADLE" != "1" ]]; then
  echo "▶ ./gradlew bootJar -x test" >&2
  ./gradlew bootJar -x test >&2
fi

if ! ls build/libs/*.jar >/dev/null 2>&1; then
  echo "✗ build/libs/*.jar 가 없습니다. SKIP_GRADLE=1 옵션을 끄거나 먼저 ./gradlew bootJar 를 실행하세요." >&2
  exit 1
fi

BUILD_ARGS=(-f docker/app/dockerfile -t "${IMAGE_NAME}:${IMAGE_TAG}")
[[ -n "$PLATFORM" ]] && BUILD_ARGS+=(--platform "$PLATFORM")

echo "▶ docker build ${BUILD_ARGS[*]} ." >&2
docker build "${BUILD_ARGS[@]}" . >&2

echo "" >&2
echo "✅ done" >&2

# 마지막 stdout 라인은 image ref 단독 — 파이프/명령 치환에 사용.
echo "${IMAGE_NAME}:${IMAGE_TAG}"
