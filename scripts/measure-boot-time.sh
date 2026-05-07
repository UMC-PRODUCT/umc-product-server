#!/usr/bin/env bash
# Spring Boot 컨테이너 부팅 시간 측정 (재사용 가능).
#
# 사용법:
#   ./scripts/measure-boot-time.sh <image[:tag]>
#   ITERATIONS=5 ENV_FILE=.env.local ./scripts/measure-boot-time.sh "$IMG"
#   IMG=$(./scripts/build-app-image.sh | tail -n1) ENV_FILE=.env.local ./scripts/measure-boot-time.sh "$IMG"
# PLATFORM=linux/amd64
# ITERATIONS=3

# 환경변수:
#   CPUS                   (default: 0.5)        docker --cpus
#   MEMORY                 (default: 1g)         docker --memory
#   ITERATIONS             (default: 1)          반복 측정 횟수
#   TIMEOUT_SECONDS        (default: 600)        부팅 대기 한도
#   MODE                   (default: log)        log | health
#                              log    = "Started ... in X.Y seconds" 로그가 등장할 때까지
#                              health = 컨테이너 내부 curl 로 /actuator/health 200 까지
#   HEALTH_PATH            (default: /actuator/health)
#   MANAGEMENT_PORT        (default: 9090)
#   ENV_FILE               (default: 없음)        docker --env-file 로 전달
#   SPRING_PROFILES_ACTIVE (default: local)
#
# 주의: 애플리케이션이 부팅에 필수 환경변수(DATABASE_URL, JWT_*, FIGMA_TOKEN_ENCRYPTION_KEY, ...)
#       를 요구하므로 보통 ENV_FILE 로 .env 를 함께 넘겨야 정상 부팅됩니다.

set -euo pipefail

IMAGE="${1:?사용법: $0 <image[:tag]>}"
CPUS="${CPUS:-0.5}"
MEMORY="${MEMORY:-1g}"
ITERATIONS="${ITERATIONS:-}"
TIMEOUT_SECONDS="${TIMEOUT_SECONDS:-600}"
MODE="${MODE:-log}"
HEALTH_PATH="${HEALTH_PATH:-/actuator/health}"
MANAGEMENT_PORT="${MANAGEMENT_PORT:-9090}"
ENV_FILE="${ENV_FILE:-"src/main/resources/.env.local"}"
SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-local}"
READY_PATTERN='Started .* in [0-9]+\.[0-9]+ seconds'

cid=""
log_pid=""
RESULT_FILE="$(mktemp)"
stop_log_stream() {
  if [[ -n "$log_pid" ]]; then
    kill "$log_pid" 2>/dev/null || true
    wait "$log_pid" 2>/dev/null || true
    log_pid=""
  fi
}
cleanup() {
  stop_log_stream
  if [[ -n "$cid" ]]; then
    docker rm -f "$cid" >/dev/null 2>&1 || true
  fi
  rm -f "$RESULT_FILE"
}
trap cleanup EXIT INT TERM

now() { python3 -c 'import time; print(f"{time.time():.3f}")'; }

run_one() {
  local iter="$1"
  local name="bootbench-${iter}-$$"
  local extra=()
  [[ -n "$ENV_FILE" ]] && extra+=(--env-file "$ENV_FILE")

  echo ""
  echo "▶ iteration ${iter}/${ITERATIONS}"

  local t0
  t0=$(now)
  cid=$(docker run -d \
    --name "$name" \
    --cpus="$CPUS" \
    --memory="$MEMORY" \
    -e SPRING_PROFILES_ACTIVE="$SPRING_PROFILES_ACTIVE" \
    "${extra[@]}" \
    "$IMAGE")

  # 부팅 중 컨테이너 로그를 실시간으로 함께 출력 ('│' 들여쓰기로 스크립트 status 와 구분).
  docker logs -f "$cid" 2>&1 | sed 's/^/    │ /' &
  log_pid=$!

  local ready_wall="" spring_self="n/a"
  local deadline=$(( $(date +%s) + TIMEOUT_SECONDS ))

  while (( $(date +%s) < deadline )); do
    if ! docker inspect -f '{{.State.Running}}' "$cid" 2>/dev/null | grep -q true; then
      stop_log_stream
      echo "  ✗ 컨테이너가 비정상 종료됨 — 최근 로그:"
      docker logs --tail 40 "$cid" 2>&1 | sed 's/^/    /'
      docker rm -f "$cid" >/dev/null 2>&1 || true
      cid=""
      return 1
    fi

    if [[ "$MODE" == "log" ]]; then
      local line
      line=$(docker logs "$cid" 2>&1 | grep -E "$READY_PATTERN" | head -n1 || true)
      if [[ -n "$line" ]]; then
        ready_wall=$(now)
        spring_self=$(echo "$line" | grep -oE '[0-9]+\.[0-9]+ seconds' | head -n1 | grep -oE '[0-9]+\.[0-9]+' || echo "n/a")
        break
      fi
    else
      if docker exec "$cid" curl -sf "http://localhost:${MANAGEMENT_PORT}${HEALTH_PATH}" >/dev/null 2>&1; then
        ready_wall=$(now)
        break
      fi
    fi
    sleep 0.5
  done

  # ready/timeout 직후 로그 스트림 중지 — wall/self-report 라인이 깔끔히 출력되도록.
  stop_log_stream

  if [[ -z "$ready_wall" ]]; then
    echo "  ✗ TIMEOUT_SECONDS=${TIMEOUT_SECONDS}s 안에 부팅 완료 신호를 받지 못했습니다."
    docker logs --tail 40 "$cid" 2>&1 | sed 's/^/    /'
    docker rm -f "$cid" >/dev/null 2>&1 || true
    cid=""
    return 1
  fi

  local wall
  wall=$(python3 -c "print(f'{$ready_wall - $t0:.3f}')")
  echo "  wall-clock         : ${wall}s   (docker run → ready)"
  echo "  spring self-report : ${spring_self}s  (JVM 시작 → Application Started)"
  printf 'RESULT|%s|%s|%s\n' "$iter" "$wall" "$spring_self" >>"$RESULT_FILE"

  docker rm -f "$cid" >/dev/null 2>&1 || true
  cid=""
}

echo "════════════════════════════════════════"
echo " Boot time measurement"
echo "  IMAGE     : ${IMAGE}"
echo "  CPUS      : ${CPUS}"
echo "  MEMORY    : ${MEMORY}"
echo "  ITERATIONS: ${ITERATIONS}"
echo "  MODE      : ${MODE}"
[[ -n "$ENV_FILE" ]] && echo "  ENV_FILE  : ${ENV_FILE}"
echo "════════════════════════════════════════"

fail_count=0
for ((i=1; i<=ITERATIONS; i++)); do
  run_one "$i" || fail_count=$((fail_count + 1))
done

echo ""
echo "════════════════════════════════════════"
echo " Summary"
echo "════════════════════════════════════════"
printf '%-6s %-14s %-18s\n' "iter" "wall(s)" "spring-self(s)"
awk -F'|' '$1=="RESULT" {printf "%-6s %-14s %-18s\n", $2, $3, $4}' "$RESULT_FILE"

python3 - "$RESULT_FILE" <<'PY'
import sys
path = sys.argv[1]
walls, springs = [], []
with open(path) as f:
    for line in f:
        parts = line.strip().split('|')
        if len(parts) < 4 or parts[0] != 'RESULT':
            continue
        try: walls.append(float(parts[2]))
        except ValueError: pass
        try: springs.append(float(parts[3]))
        except ValueError: pass

def stats(xs):
    return (min(xs), sum(xs)/len(xs), max(xs)) if xs else None

print()
w = stats(walls)
s = stats(springs)
if w: print(f"wall         : min={w[0]:.3f}s  avg={w[1]:.3f}s  max={w[2]:.3f}s  (n={len(walls)})")
if s: print(f"spring-self  : min={s[0]:.3f}s  avg={s[1]:.3f}s  max={s[2]:.3f}s  (n={len(springs)})")
PY

if (( fail_count > 0 )); then
  echo ""
  echo "⚠️  실패한 iteration: ${fail_count}/${ITERATIONS}"
  exit 1
fi
