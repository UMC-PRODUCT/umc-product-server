#!/usr/bin/env bash
# Chat Smoke·반복 부하를 실행하고 매번 새로운 out/chat 폴더에 조건과 결과를 보존한다.
set -euo pipefail

if [ "${1:-}" = "--help" ]; then
  printf '사용법: SEED_FILE=<시드 경로> [PROFILE=load] [SCENARIO=chat-message] bash loadtest/scripts/run-chat-test.sh [실행 이름]\n'
  printf '시드 경로는 명령을 실행한 디렉터리 기준입니다. PROFILE·SCENARIO는 script.js에 등록된 조합을 사용하세요.\n'
  printf '부하 설정: BASE_URL, CHAT_USERS, CHAT_ROOMS, CHAT_USERS_PER_VU 또는 CHAT_VU_USERS, CHAT_SENDERS\n'
  printf '시간 설정: DURATION, CHAT_MESSAGE_INTERVAL_MS, CHAT_TIMEOUT_MS, CHAT_STARTUP_TIMEOUT_MS, CHAT_CONTROL_INTERVAL_MS\n'
  printf '메시지: CHAT_WARMUP=30s, CHAT_MAX_INFLIGHT=128 (사용자별 응답 대기 상한)\n'
  printf '리액션: SCENARIO=chat-reaction CHAT_REACTION_MODE=load|duplicate CHAT_USERS=10|50\n'
  printf '리액션 시간: CHAT_REACTION_INTERVAL_MS=1000 CHAT_REACTION_WARMUP=30s DURATION=2m (예비 실행용 기본값)\n'
  printf '중복 검증: CHAT_REACTION_ROUNDS=10, 같은 회원의 연결 2개로 실행\n'
  printf '버전 기록: CHAT_LOCK_VARIANT=baseline|removed CHAT_SERVER_REVISION=<실제 배포 SHA> (락을 바꾸는 설정 아님)\n'
  printf '비교 실행: CHAT_COMPARISON=1이면 variant와 전체 배포 SHA가 필수입니다. CHAT_TEST_ID로 DB 기록과 연결합니다.\n'
  printf '동일 메시지 재사용: 리액션 첫 실행의 conditions.json을 다음 SEED_FILE로 지정\n'
  printf '선택 성능 기준(ms): CHAT_ACK_P95_MS, CHAT_ACK_P99_MS, CHAT_DELIVERY_P95_MS, CHAT_DELIVERY_P99_MS\n'
  printf 'CHAT_RUN_NOTES에 앱·DB 버전, JVM 옵션, 실행 전 데이터 규모, 서버 지표 위치를 기록할 수 있습니다.\n'
  exit 0
fi

fail() { printf '[오류] %s\n' "$*" >&2; exit 1; }
[ "$#" -le 1 ] || fail '실행 이름 하나만 지정하세요. --help를 확인하세요.'
SCENARIO="${SCENARIO:-chat-message}"
LABEL="${1:-$SCENARIO}"
[[ "$LABEL" =~ ^[a-zA-Z0-9][a-zA-Z0-9_-]*$ ]] || fail '실행 이름에는 영문·숫자·하이픈·밑줄만 사용하세요.'
for tool in k6 jq git mktemp; do
  command -v "$tool" >/dev/null 2>&1 || fail "필요한 명령어가 없습니다: $tool"
done
[ -n "${SEED_FILE:-}" ] && [ -f "$SEED_FILE" ] || fail 'SEED_FILE에 준비된 시드 파일을 지정하세요.'
PROFILE="${PROFILE:-load}"
REPO_ROOT="$(git -C "$(dirname "$0")" rev-parse --show-toplevel)"
K6_DIR="$REPO_ROOT/loadtest/k6"
if [ "${CHAT_COMPARISON:-0}" = "1" ]; then
  [[ "${CHAT_LOCK_VARIANT:-}" =~ ^(baseline|removed)$ ]] || fail '비교 실행에는 CHAT_LOCK_VARIANT=baseline|removed가 필요합니다.'
  [[ "${CHAT_SERVER_REVISION:-}" =~ ^[a-f0-9]{40}$ ]] || fail 'CHAT_SERVER_REVISION에 실제 배포된 전체 커밋 SHA를 지정하세요.'
fi
export CHAT_TEST_ID="${CHAT_TEST_ID:-$(date -u +%Y%m%dT%H%M%SZ)-$LABEL-$$}"
[[ "$CHAT_TEST_ID" =~ ^[a-zA-Z0-9][a-zA-Z0-9_-]{0,99}$ ]] || fail 'CHAT_TEST_ID는 영문, 숫자, 하이픈, 밑줄로 된 100자 이하 식별자여야 합니다.'
# 커밋하지 않은 파일도 포함한다. 버전 이름이 아닌 실제 계측과 부하 코드의 동일성을 비교한다.
fingerprint() {
  git -C "$REPO_ROOT" ls-files -co --exclude-standard -- "$@" | LC_ALL=C sort -u | while IFS= read -r file; do
    hash="$(git -C "$REPO_ROOT" hash-object -- "$REPO_ROOT/$file")"
    printf '%s %s\n' "$file" "$hash"
  done | git -C "$REPO_ROOT" hash-object --stdin
}
export CHAT_HARNESS_HASH="$(fingerprint loadtest/k6/config loadtest/k6/lib loadtest/k6/scenarios loadtest/k6/script.js loadtest/scripts)"
export CHAT_INSTRUMENTATION_HASH="$(fingerprint \
  monolith/src/main/java/com/umc/product/global/observability/chat \
  monolith/src/main/java/com/umc/product/global/config/ChatMetricsConfig.java \
  monolith/src/main/java/com/umc/product/global/config/WebSocketMessageBrokerConfig.java \
  monolith/src/main/resources/application.yml)"
mkdir -p "$K6_DIR/out/chat"
# 같은 이름으로 연속 실행해도 이전 결과를 덮어쓰지 않도록 mktemp로 폴더를 생성한다.
RUN_DIR="$(mktemp -d "$K6_DIR/out/chat/$(date -u +%Y%m%dT%H%M%SZ)-$LABEL.XXXXXX")"
K6_EXIT=""
STARTED_AT="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
printf '결과 폴더: %s\n' "$RUN_DIR"

# k6가 실패하거나 결과 저장에 실패해도 종료 시각과 종료 코드를 기록한다.
finish() {
  local status="$?"
  trap - EXIT
  if [ -f "$RUN_DIR/run-meta.json" ]; then
    jq --arg endedAt "$(date -u +%Y-%m-%dT%H:%M:%SZ)" --argjson exitCode "$status" \
      --arg k6Exit "$K6_EXIT" \
      '. + {endedAt: $endedAt, exitCode: $exitCode, k6ExitCode: (if $k6Exit == "" then null else ($k6Exit | tonumber) end)}' \
      "$RUN_DIR/run-meta.json" > "$RUN_DIR/.run-meta.tmp" \
      && mv "$RUN_DIR/.run-meta.tmp" "$RUN_DIR/run-meta.json" || status=1
  fi
  exit "$status"
}
trap finish EXIT

# 환경변수 전체나 로컬 설정 파일을 복사하지 않는다. 테스트 설정과 사용자가 남긴 설명만 저장한다.
INPUTS='{}'
for name in BASE_URL CHAT_USERS CHAT_ROOMS CHAT_USERS_PER_VU CHAT_VU_USERS CHAT_SENDERS \
  DURATION CHAT_MESSAGE_INTERVAL_MS CHAT_TIMEOUT_MS CHAT_STARTUP_TIMEOUT_MS CHAT_CONTROL_INTERVAL_MS CHAT_WARMUP CHAT_MAX_INFLIGHT \
  CHAT_ACK_P95_MS CHAT_ACK_P99_MS CHAT_DELIVERY_P95_MS CHAT_DELIVERY_P99_MS \
  CHAT_REACTION_MODE CHAT_REACTION_INTERVAL_MS CHAT_REACTION_WARMUP CHAT_REACTION_EMOJI CHAT_REACTION_ROUNDS \
  CHAT_LOCK_VARIANT CHAT_SERVER_REVISION CHAT_TEST_ID CHAT_COMPARISON CHAT_HARNESS_HASH CHAT_INSTRUMENTATION_HASH; do
  if printenv "$name" >/dev/null; then
    INPUTS="$(jq -c --arg name "$name" --arg value "$(printenv "$name")" '. + {($name): $value}' <<< "$INPUTS")"
  fi
done
jq -n --arg label "$LABEL" --arg startedAt "$STARTED_AT" --arg profile "$PROFILE" --arg scenario "$SCENARIO" \
  --arg commit "$(git -C "$REPO_ROOT" rev-parse HEAD)" \
  --arg status "$(git -C "$REPO_ROOT" status --porcelain)" \
  --arg k6Version "$(k6 version)" --arg platform "$(uname -sm)" \
  --arg seedSource "$SEED_FILE" --arg notes "${CHAT_RUN_NOTES:-}" --argjson inputs "$INPUTS" \
  '{label: $label, startedAt: $startedAt, profile: $profile, scenario: $scenario,
    commit: $commit, dirty: ($status != ""), changedFiles: $status, k6Version: $k6Version,
    platform: $platform, seedSource: $seedSource, inputs: $inputs, notes: $notes}' > "$RUN_DIR/run-meta.json"

# 복사한 시드를 실제 입력으로 사용해, 원본 파일이 바뀌어도 사용한 데이터 구성이 남도록 한다.
jq -e '(.reactionSeed // .) | {runId, baseUrl, schoolId, memberIds,
  rooms: (if (.rooms | type) == "array" then [.rooms[] | {threadId, memberIds}
    + (if has("messageIds") then {messageIds} else {} end)] else .rooms end)}' \
  "$SEED_FILE" > "$RUN_DIR/seed.json"
cd "$K6_DIR"
set +e
# 선택한 이름을 그대로 전달한다. 지원 여부는 공통 러너의 등록 정보와 조건 기록 함수로 확인한다.
PROFILE="$PROFILE" SCENARIO="$SCENARIO" SEED_FILE="$RUN_DIR/seed.json" CHAT_RUN_DIR="$RUN_DIR" \
  k6 run --no-usage-report --no-color --out "json=$RUN_DIR/metrics.json.gz" script.js 2>&1 | tee "$RUN_DIR/console.log"
PIPE_CODES=("${PIPESTATUS[@]}")
set -e
K6_EXIT="${PIPE_CODES[0]}"
if [ "$K6_EXIT" -ne 0 ]; then
  printf '[안내] k6 종료 코드=%s. 실패한 실행의 기록도 보존했습니다.\n' "$K6_EXIT" >&2
  exit "$K6_EXIT"
fi
[ "${PIPE_CODES[1]}" -eq 0 ] || fail '실행 로그 저장에 실패했습니다.'
for file in summary.json conditions.json; do
  jq -e 'type == "object" and .available != false' "$RUN_DIR/$file" >/dev/null || fail "$file 저장을 확인하지 못했습니다."
done
printf '실행과 결과 저장 완료: %s\n' "$RUN_DIR"
