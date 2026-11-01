#!/usr/bin/env bash
# UMC 서버를 일회용 PostGIS와 local 프로필로 띄워 실행 확인을 돕는다.
# 상태와 증거는 레포 루트의 .verify/run-<시각>/ 에 남고, .verify/current 가 마지막 실행을 가리킨다.
#
# 사용법 (레포 루트에서):
#   .claude/skills/verify/scripts/verify.sh up            # jar 빌드, DB 기동, 앱 기동, 준비 확인
#   .claude/skills/verify/scripts/verify.sh doctor        # 지금 인스턴스가 쓸 만한지 읽기 전용 점검
#   .claude/skills/verify/scripts/verify.sh call <이름> <curl 인자...>   # 요청과 응답을 증거로 저장
#   .claude/skills/verify/scripts/verify.sh token <memberId>             # local 전용 access token 발급
#   .claude/skills/verify/scripts/verify.sh flyway        # 적용된 마이그레이션 수와 파일 수 비교
#   .claude/skills/verify/scripts/verify.sh restart-app   # DB는 두고 앱만 다시 띄움
#   .claude/skills/verify/scripts/verify.sh down          # 이번에 띄운 앱과 DB만 정리, 증거는 남김
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
VERIFY_DIR="$ROOT/.verify"
MIGRATION_DIR="$ROOT/monolith/src/main/resources/db/migration"
JAR="$ROOT/app/build/libs/app.jar"
READY_TIMEOUT_SECONDS="${VERIFY_READY_TIMEOUT:-300}"

die() { echo "verify: $*" >&2; exit 1; }
free_port() { python3 -I -c 'import socket; s=socket.socket(); s.bind(("127.0.0.1",0)); print(s.getsockname()[1]); s.close()'; }
random_secret() { python3 -I -c 'import secrets; print(secrets.token_urlsafe(48))'; }
random_aes_key() { python3 -I -c 'import base64, secrets; print(base64.b64encode(secrets.token_bytes(32)).decode())'; }

current_run() {
  [ -L "$VERIFY_DIR/current" ] || die "실행 중인 인스턴스가 없습니다. 먼저 up 을 실행하세요."
  RUN_DIR="$(cd "$VERIFY_DIR/current" && pwd -P)"
  # shellcheck disable=SC1091
  . "$RUN_DIR/state.env"
}

postgis_image() {
  if [ "$(uname -m)" = "arm64" ]; then
    local image="umc-product-postgis-test:18.2-postgis"
    if ! docker image inspect "$image" >/dev/null 2>&1; then
      echo "verify: $image 이미지를 빌드합니다 (테스트와 같은 이미지)" >&2
      docker build -q -t "$image" -f "$ROOT/docker/test/postgis/Dockerfile" "$ROOT/docker/test/postgis" >&2
    fi
    echo "$image"
  else
    echo "postgis/postgis:18-3.6"
  fi
}

write_app_env() {
  local db_port="$1"
  umask 077
  cat > "$RUN_DIR/app.env" <<EOF
SPRING_PROFILES_ACTIVE=local
SERVER_PORT=$SERVER_PORT
MANAGEMENT_PORT=$MANAGEMENT_PORT
DATABASE_URL=jdbc:postgresql://127.0.0.1:$db_port/umc_product
DATABASE_USERNAME=verify
DATABASE_PASSWORD=verify
JWT_ACCESS_TOKEN_SECRET=$(random_secret)
JWT_REFRESH_TOKEN_SECRET=$(random_secret)
JWT_OAUTH_VERIFICATION_TOKEN_SECRET=$(random_secret)
JWT_EMAIL_VERIFICATION_TOKEN_SECRET=$(random_secret)
JWT_SSO_LOGIN_TOKEN_SECRET=$(random_secret)
DEMODAY_PARTICIPANT_TOKEN_SECRET=$(random_secret)
DEMODAY_VOTE_QR_SIGNING_KEY=$(random_secret)
DEMODAY_VOTE_AUTHORIZATION_SIGNING_KEY=$(random_secret)
DEMODAY_STAMP_CREDENTIAL_ENCRYPTION_KEY=$(random_aes_key)
DEMODAY_QR_BASE_URL=http://127.0.0.1:$SERVER_PORT
APPLE_IOS_CLIENT_ID=verify.ios
APPLE_WEB_CLIENT_ID=verify.web
APPLE_TEAM_ID=VERIFYTEAM
APPLE_KEY_ID=VERIFYKEY
APPLE_PRIVATE_KEY=verify-dummy-not-a-key
S3_ACCESS_KEY_ID=verify
S3_SECRET_ACCESS_KEY=verify
S3_BUCKET_NAME=verify-bucket
EMAIL_NO_REPLY_ADDRESS=no-reply@verify.local
SES_ACCESS_KEY_ID=verify
SES_SECRET_ACCESS_KEY=verify
CORS_ALLOWED_ORIGIN_PATTERNS=http://localhost:3000
APP_OBSERVABILITY_TRACING_ENABLED=false
FCM_ENABLED=false
EOF
}

start_app() {
  : > "$RUN_DIR/app.log"
  (
    set -a
    # shellcheck disable=SC1091
    . "$RUN_DIR/app.env"
    set +a
    cd "$RUN_DIR"
    exec java -jar "$JAR"
  ) >> "$RUN_DIR/app.log" 2>&1 &
  APP_PID=$!
  sed -i '' -e "s/^APP_PID=.*/APP_PID=$APP_PID/" "$RUN_DIR/state.env"
}

wait_ready() {
  local deadline=$(( $(date +%s) + READY_TIMEOUT_SECONDS ))
  local status=""
  while [ "$(date +%s)" -lt "$deadline" ]; do
    if ! kill -0 "$APP_PID" 2>/dev/null; then
      tail -n 60 "$RUN_DIR/app.log" >&2
      die "앱 프로세스가 준비 전에 종료됐습니다. 전체 로그: $RUN_DIR/app.log"
    fi
    status="$(curl -fsS "http://127.0.0.1:$MANAGEMENT_PORT/actuator/health" 2>/dev/null || true)"
    if printf '%s' "$status" | grep -q '"status":"UP"'; then
      printf '%s\n' "$status" > "$RUN_DIR/evidence/health.json"
      return 0
    fi
    sleep 3
  done
  tail -n 60 "$RUN_DIR/app.log" >&2
  die "${READY_TIMEOUT_SECONDS}초 안에 health 가 UP 이 되지 않았습니다. 마지막 응답: ${status:-없음}"
}

flyway_check() {
  local expected applied
  expected="$(find "$MIGRATION_DIR" -maxdepth 1 -name 'V*__*.sql' | wc -l | tr -d ' ')"
  applied="$(docker exec "$DB_CONTAINER" psql -U verify -d umc_product -tAc \
    "select count(*) from flyway_schema_history where success and version is not null")"
  printf 'migration_files=%s applied_success=%s\n' "$expected" "$applied" | tee "$RUN_DIR/evidence/flyway.txt"
  [ "$expected" = "$applied" ] || die "적용된 마이그레이션 수가 파일 수와 다릅니다."
}

cmd_up() {
  command -v docker >/dev/null || die "docker 가 필요합니다."
  command -v java >/dev/null || die "Java 21 이 필요합니다."
  if [ -L "$VERIFY_DIR/current" ] && [ -f "$VERIFY_DIR/current/state.env" ]; then
    # shellcheck disable=SC1091
    . "$VERIFY_DIR/current/state.env"
    if kill -0 "${APP_PID:-0}" 2>/dev/null; then
      die "이미 띄운 인스턴스가 있습니다(PID $APP_PID). doctor 로 확인하거나 down 으로 정리하세요."
    fi
  fi

  echo "verify: app.jar 를 빌드합니다 (테스트 제외)" >&2
  (cd "$ROOT" && ./gradlew -q :app:bootJar -x test) >&2
  [ -f "$JAR" ] || die "$JAR 가 만들어지지 않았습니다."

  local stamp; stamp="$(date +%Y%m%d-%H%M%S)"
  RUN_DIR="$VERIFY_DIR/run-$stamp"
  mkdir -p "$RUN_DIR/evidence"
  ln -sfn "$RUN_DIR" "$VERIFY_DIR/current"

  SERVER_PORT="$(free_port)"; MANAGEMENT_PORT="$(free_port)"
  local db_port; db_port="$(free_port)"
  DB_CONTAINER="umc-verify-$stamp"

  docker run -d --name "$DB_CONTAINER" -p "127.0.0.1:$db_port:5432" \
    -e POSTGRES_DB=umc_product -e POSTGRES_USER=verify -e POSTGRES_PASSWORD=verify \
    "$(postgis_image)" >/dev/null
  until docker exec "$DB_CONTAINER" pg_isready -U verify -d umc_product >/dev/null 2>&1; do sleep 1; done
  # 초기화 스크립트가 끝나 재기동되는 동안 잠깐 응답하지 않으므로 실제 쿼리가 될 때까지 기다린다.
  until docker exec "$DB_CONTAINER" psql -U verify -d umc_product -tAc 'select 1' >/dev/null 2>&1; do sleep 1; done

  cat > "$RUN_DIR/state.env" <<EOF
SERVER_PORT=$SERVER_PORT
MANAGEMENT_PORT=$MANAGEMENT_PORT
DB_PORT=$db_port
DB_CONTAINER=$DB_CONTAINER
APP_PID=
EOF
  write_app_env "$db_port"
  start_app
  wait_ready
  flyway_check
  echo "verify: 준비 완료 api=http://127.0.0.1:$SERVER_PORT management=http://127.0.0.1:$MANAGEMENT_PORT run=$RUN_DIR"
}

cmd_doctor() {
  current_run
  local ok=1
  if kill -0 "$APP_PID" 2>/dev/null; then echo "process: PID $APP_PID 실행 중"; else echo "process: PID $APP_PID 없음"; ok=0; fi
  local owner; owner="$(lsof -nP -iTCP:"$SERVER_PORT" -sTCP:LISTEN -t 2>/dev/null | head -1 || true)"
  if [ "$owner" = "$APP_PID" ]; then echo "port: $SERVER_PORT 를 이번 프로세스가 사용 중"; else echo "port: $SERVER_PORT 소유자 ${owner:-없음} (기대 $APP_PID)"; ok=0; fi
  local health; health="$(curl -fsS "http://127.0.0.1:$MANAGEMENT_PORT/actuator/health" 2>/dev/null || true)"
  if printf '%s' "$health" | grep -q '"status":"UP"'; then echo "health: UP"; else echo "health: ${health:-응답 없음}"; ok=0; fi
  # 로그 마스킹이 프로필 이름을 가리므로, 넣은 값과 활성 프로필 개수로 확인한다.
  if grep -q '^SPRING_PROFILES_ACTIVE=local$' "$RUN_DIR/app.env" && grep -q 'The following 1 profile is active' "$RUN_DIR/app.log"; then
    echo "profile: local (활성 프로필 1개)"
  else
    echo "profile: local 확인 안 됨"; ok=0
  fi
  if [ "$(docker inspect -f '{{.State.Running}}' "$DB_CONTAINER" 2>/dev/null)" = "true" ]; then echo "db: $DB_CONTAINER 실행 중 (127.0.0.1:$DB_PORT)"; else echo "db: $DB_CONTAINER 없음"; ok=0; fi
  [ "$ok" = 1 ] && echo "doctor: 사용 가능" || die "doctor: 문제가 있습니다. down 후 다시 up 하세요."
}

cmd_call() {
  current_run
  [ $# -ge 2 ] || die "사용법: call <이름> <curl 인자...>"
  local name="$1"; shift
  local out="$RUN_DIR/evidence/$name.txt"
  local args=("$@")
  for i in "${!args[@]}"; do args[$i]="${args[$i]//\{api\}/http://127.0.0.1:$SERVER_PORT}"; done
  { printf '$ curl -sS -i'; printf ' %q' "${args[@]}"; printf '\n\n'; curl -sS -i "${args[@]}"; printf '\n'; } > "$out" 2>&1 || true
  sed -n '3p' "$out"
  echo "saved: $out"
}

cmd_token() {
  current_run
  [ $# -eq 1 ] || die "사용법: token <memberId>"
  curl -fsS "http://127.0.0.1:$SERVER_PORT/test/token/access?memberId=$1"
  echo
}

cmd_flyway() { current_run; flyway_check; }

cmd_restart_app() {
  current_run
  if kill -0 "$APP_PID" 2>/dev/null; then kill "$APP_PID"; while kill -0 "$APP_PID" 2>/dev/null; do sleep 1; done; fi
  cp "$RUN_DIR/app.log" "$RUN_DIR/evidence/app-before-restart.log"
  start_app
  wait_ready
  echo "verify: 앱을 다시 띄웠습니다 (PID $APP_PID)"
}

cmd_down() {
  current_run
  if [ -n "${APP_PID:-}" ] && kill -0 "$APP_PID" 2>/dev/null; then
    kill "$APP_PID"
    for _ in $(seq 1 30); do kill -0 "$APP_PID" 2>/dev/null || break; sleep 1; done
    kill -0 "$APP_PID" 2>/dev/null && kill -9 "$APP_PID"
  fi
  docker rm -f "$DB_CONTAINER" >/dev/null 2>&1 || true
  cp "$RUN_DIR/app.log" "$RUN_DIR/evidence/app.log" 2>/dev/null || true
  rm -f "$RUN_DIR/app.env"
  rm -f "$VERIFY_DIR/current"
  echo "verify: 정리 완료. 증거는 $RUN_DIR/evidence 에 남아 있습니다."
}

case "${1:-}" in
  up) shift; cmd_up "$@" ;;
  doctor) shift; cmd_doctor ;;
  call) shift; cmd_call "$@" ;;
  token) shift; cmd_token "$@" ;;
  flyway) shift; cmd_flyway ;;
  restart-app) shift; cmd_restart_app ;;
  down) shift; cmd_down ;;
  *) sed -n '2,15p' "$0"; exit 1 ;;
esac
