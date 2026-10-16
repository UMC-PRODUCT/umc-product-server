#!/usr/bin/env bash
# Chat Smoke·반복 부하를 실행하고 매번 새로운 out/chat 폴더에 조건과 결과를 보존한다.
set -euo pipefail

if [ "${1:-}" = "--help" ]; then
  printf '사용법: SEED_FILE=<시드 경로> [PROFILE=load] [SCENARIO=chat-message] bash loadtest/scripts/run-chat-test.sh [실행 이름]\n'
  printf '시드 경로는 명령을 실행한 디렉터리 기준입니다. PROFILE·SCENARIO는 script.js에 등록된 조합을 사용하세요.\n'
  printf '부하 설정: BASE_URL, CHAT_USERS, CHAT_ROOMS, CHAT_USERS_PER_VU 또는 CHAT_VU_USERS, CHAT_SENDERS\n'
  printf '시간 설정: DURATION, CHAT_MESSAGE_INTERVAL_MS, CHAT_TIMEOUT_MS, CHAT_STARTUP_TIMEOUT_MS\n'
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
  DURATION CHAT_MESSAGE_INTERVAL_MS CHAT_TIMEOUT_MS CHAT_STARTUP_TIMEOUT_MS \
  CHAT_ACK_P95_MS CHAT_ACK_P99_MS CHAT_DELIVERY_P95_MS CHAT_DELIVERY_P99_MS; do
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
jq -e '{runId, baseUrl, schoolId, memberIds,
  rooms: (if (.rooms | type) == "array" then [.rooms[] | {threadId, memberIds}] else .rooms end)}' \
  "$SEED_FILE" > "$RUN_DIR/seed.json"
cd "$K6_DIR"
set +e
# 선택한 이름을 그대로 전달한다. 지원 여부는 공통 러너의 등록 정보와 조건 기록 함수로 확인한다.
PROFILE="$PROFILE" SCENARIO="$SCENARIO" SEED_FILE="$RUN_DIR/seed.json" CHAT_RUN_DIR="$RUN_DIR" \
  k6 run --no-usage-report --no-color script.js 2>&1 | tee "$RUN_DIR/console.log"
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
