#!/bin/bash
set -e

# ------------------------------------------------------------------
# 변수 할당 (GitHub Actions에서 envs로 넘어온 값들)
# ------------------------------------------------------------------
# 주의: YAML에서 envs로 넘겨준 변수 이름과 정확히 일치해야 합니다.
# ------------------------------------------------------------------

# 필수 환경 변수 체크
: "${ENVIRONMENT:?ENVIRONMENT 변수가 설정되지 않았습니다.}"
: "${DOCKERHUB_USERNAME:?DOCKERHUB_USERNAME 변수가 설정되지 않았습니다.}"
: "${DOCKERHUB_TOKEN:?DOCKERHUB_TOKEN 변수가 설정되지 않았습니다.}"
: "${DOCKER_IMAGE_NAME:?DOCKER_IMAGE_NAME 변수가 설정되지 않았습니다.}"
: "${IMAGE_TAG:?IMAGE_TAG 변수가 설정되지 않았습니다.}"
: "${APP_DIR_PRODUCTION:?APP_DIR_PRODUCTION 변수가 설정되지 않았습니다.}"
: "${APP_DIR_DEVELOPMENT:?APP_DIR_DEVELOPMENT 변수가 설정되지 않았습니다.}"
: "${APPLICATION_PROD:?APPLICATION_PROD 변수가 설정되지 않았습니다.}"
: "${APPLICATION_DEV:?APPLICATION_DEV 변수가 설정되지 않았습니다.}"
: "${APPLICATION_SECRET:?APPLICATION_SECRET 변수가 설정되지 않았습니다.}"


echo "=============================="
echo "🚀 배포 시작: $ENVIRONMENT 환경"
echo "=============================="

# [1] Docker 설치 확인
echo "[1] Docker 설치 확인"
if which docker > /dev/null 2>&1; then
  echo "✅ Docker 인식됨: $(which docker)"
else
  echo "⚠️  PATH에 /usr/local/bin 추가"
  export PATH="$PATH:/usr/local/bin"
  if which docker > /dev/null 2>&1; then
    echo "✅ Docker 인식됨: $(which docker)"
  else
    echo "❌ Docker를 찾을 수 없습니다"
    exit 1
  fi
fi

# [2] 환경별 배포 디렉토리 설정
# YAML의 ${{ secrets... }} 대신 환경변수 $APP_DIR_PRODUCTION 등을 사용
if [[ "$ENVIRONMENT" == "prod" ]]; then
  APP_DIR=$APP_DIR_PRODUCTION
elif [[ "$ENVIRONMENT" == "dev" ]]; then
  APP_DIR=$APP_DIR_DEVELOPMENT
else
  echo "❌ 알 수 없는 환경입니다: $ENVIRONMENT"
  exit 1
fi

echo "📂 배포 경로: $APP_DIR"

# [3] 설정 파일 생성
mkdir -p $APP_DIR/config
echo "$APPLICATION_PROD" > $APP_DIR/config/application-prod.yml
echo "$APPLICATION_DEV" > $APP_DIR/config/application-dev.yml
echo "$APPLICATION_SECRET" > $APP_DIR/config/application-secret.yml

# 보안을 위해 권한 설정 (선택사항)
chmod 600 $APP_DIR/config/application-*.yml
echo "✅ 환경 설정 파일 생성 완료"

# [4] Docker Hub 로그인
echo ""
echo "[2] Docker Hub 로그인"
echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin

if [ $? -eq 0 ]; then
  echo "✅ Docker Hub 로그인 성공"
else
  echo "❌ Docker Hub 로그인 실패"
  exit 1
fi

# [5] Docker Compose 실행
cd $APP_DIR

# 롤백 및 버전 관리를 위해 태그 지정 (docker-compose.yml에서 ${TAG}를 쓴다고 가정)
export TAG=$IMAGE_TAG
export DOCKER_IMAGE_NAME=$DOCKER_IMAGE_NAME

# 기존 컨테이너 중지 및 최신 이미지 Pull & 실행
docker compose pull
docker compose up -d

if [ $? -eq 0 ]; then
  echo "✅ 컨테이너 재시작 성공"
else
  echo "❌ 컨테이너 재시작 실패"
  exit 1
fi

# 미사용 이미지 정리
docker image prune -f

echo ""
echo "=============================="
echo "🎉 배포 완료!"
echo "=============================="
echo "환경: $ENVIRONMENT"
echo "이미지: $DOCKER_IMAGE_NAME:$ENVIRONMENT-latest"
