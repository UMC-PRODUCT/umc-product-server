#!/usr/bin/env bash
# Chat 테스트에 사용할 회원과 방을 준비하고, 검증된 ID를 JSON으로 저장한다.
# 매 실행마다 새 데이터를 생성한다. 메시지 송수신과 성능 측정은 이후 k6 시나리오가 담당한다.

# 미정의 변수와 파이프라인 중간의 실패도 오류로 취급해 잘못된 ID로 다음 단계를 진행하지 않게 한다.
set -euo pipefail

if [ "${1:-}" = "--help" ]; then
  printf '사용법: SUT_URL=http://localhost:8080 bash loadtest/scripts/prepare-chat-data.sh\n'
  printf '필요 도구: curl, jq, uuidgen, git\n'
  printf 'CHAT_USERS=2 CHAT_ROOMS=1: 사용자 수와 방 수\n'
  printf 'CHAT_ROOM_MEMBERS=\x27[[1,2,3],[2,4]]\x27: 방별 사용자 번호 (생략하면 균등 분배)\n'
  printf '앱 설정: APP_TEST_API_ENABLED=true, APP_SEED_ENABLED=true\n'
  printf '재실행마다 새 데이터를 추가하며 기존 데이터를 삭제하지 않습니다.\n'
  exit 0
fi

fail() {
  printf '[오류] %s\n' "$*" >&2
  exit 1
}

# 실행 대상은 SUT_URL로 명시하고, 경로를 붙일 때 슬래시가 중복되지 않도록 끝의 /를 제거한다.
[ "$#" -eq 0 ] || fail '지원하지 않는 인자입니다. --help를 확인하세요.'
[ -n "${SUT_URL:-}" ] || fail 'SUT_URL에 실행 중인 앱 주소를 지정하세요.'
BASE_URL="${SUT_URL%/}"
case "$BASE_URL" in
  http://*|https://*) ;;
  *) fail 'SUT_URL은 http:// 또는 https://로 시작해야 합니다.' ;;
esac
USER_COUNT="${CHAT_USERS:-2}"
ROOM_COUNT="${CHAT_ROOMS:-1}"
for count in "$USER_COUNT" "$ROOM_COUNT"; do
  [[ "$count" =~ ^[1-9][0-9]*$ ]] && [ "${#count}" -le 4 ] \
    && [ "$count" -le 1000 ] || fail 'CHAT_USERS·CHAT_ROOMS는 1~1000 사이 정수여야 합니다.'
done

for tool in curl jq uuidgen git; do
  command -v "$tool" >/dev/null 2>&1 || fail "필요한 명령어가 없습니다: $tool"
done

# 기본 배치는 사용자를 방마다 나누고, 명시한 목록은 여러 방에 같은 사용자가 참여하는 구성도 허용한다.
ROOM_LAYOUT="${CHAT_ROOM_MEMBERS:-}"
if [ -z "$ROOM_LAYOUT" ]; then
  [ "$ROOM_COUNT" -le "$USER_COUNT" ] || fail '기본 균등 배치에서 방 수는 사용자 수 이하여야 합니다.'
  ROOM_LAYOUT="$(jq -nc --argjson users "$USER_COUNT" --argjson rooms "$ROOM_COUNT" '
    [range(0; $rooms) as $r | [range(1; $users + 1) | select(((. - 1) * $rooms / $users | floor) == $r)]]
  ')"
fi
ROOM_LAYOUT="$(printf '%s' "$ROOM_LAYOUT" | jq -ce --argjson users "$USER_COUNT" --argjson rooms "$ROOM_COUNT" '
  select(type == "array" and length == $rooms)
  | select(all(.[]; type == "array" and length > 0))
  | select(all(.[]; length == (unique | length)))
  | select(all(.[][]; type == "number" and floor == . and . >= 1 and . <= $users))
  | select((flatten | unique | sort) == [range(1; $users + 1)])
')" || fail 'CHAT_ROOM_MEMBERS에는 각 방의 사용자 번호를 중복 없이 지정하고 모든 사용자를 포함하세요.'

# 실행한 디렉터리와 관계없이 저장 위치를 고정하고, UUID로 회원 정보와 결과 파일의 충돌을 피한다.
REPO_ROOT="$(git -C "$(dirname "$0")" rev-parse --show-toplevel)"
RUN_ID="$(uuidgen | tr '[:upper:]' '[:lower:]')"
OUTPUT_DIR="$REPO_ROOT/loadtest/k6/data/chat"
OUTPUT_FILE="$OUTPUT_DIR/$RUN_ID.json"
TEMP_FILE=""
ROOMS_TEMP_FILE=""
CREATED_IDS=""
THREAD_ID=""

# 실패 시 미완성 파일만 지운다. API로 이미 생성한 데이터는 남기고, 추적할 수 있도록 ID를 출력한다.
on_exit() {
  local status="$?"
  if [ -n "$TEMP_FILE" ]; then
    rm -f "$TEMP_FILE"
  fi
  if [ -n "$ROOMS_TEMP_FILE" ]; then
    rm -f "$ROOMS_TEMP_FILE"
  fi
  if [ "$status" -ne 0 ]; then
    printf '[오류] 준비 중단: runId=%s, memberIds=%s, threadId=%s\n' \
      "$RUN_ID" "${CREATED_IDS:--}" "${THREAD_ID:--}" >&2
    printf '실패 이전에 생성된 데이터가 있다면 그대로 남습니다. 자동 삭제하거나 생성 요청을 재시도하지 않습니다.\n' >&2
  fi
}
trap on_exit EXIT

# API 응답은 stdout, 안내·오류는 stderr로 분리한다. $(api ...)나 jq에는 응답만 전달되어야 한다.
# 생성 요청은 응답이 유실돼도 서버에서 성공했을 수 있으므로 자동 재시도하지 않는다.
api() {
  local method="$1" path="$2" body="${3:-}" token="${4:-}"
  local args=(--fail --silent --show-error --connect-timeout 5 --max-time 30 --request "$method")
  if [[ "$path" == /test/* ]] && [ -n "${TEST_API_BASIC_AUTH:-}" ]; then
    args+=(--header "Authorization: Basic $TEST_API_BASIC_AUTH")
  fi
  if [ -n "$body" ]; then
    args+=(--header 'Content-Type: application/json' --data "$body")
  fi
  if [ -n "$token" ]; then
    args+=(--header "Authorization: Bearer $token")
  fi
  curl "${args[@]}" "$BASE_URL$path" || {
    printf '[오류] API 호출 실패: %s %s\n' "$method" "$path" >&2
    return 1
  }
}

# 성공 응답에서 양의 정수 ID만 추출한다. 출력은 시드 파일에서 사용할 문자열로 통일한다.
response_id() {
  jq -er --arg field "$1" '
    select(.success == true) | .result[$field] | select(. != null)
    | tostring | select(test("^[1-9][0-9]*$"))
  '
}

# 1. 데이터 생성 전에 앱 응답과 기존 학교를 확인한다. 여러 학교가 있으면 가장 작은 ID를 선택한다.
printf '[1/5] 앱 및 학교 확인: %s\n' "$BASE_URL" >&2
api GET '/test/health-check' >/dev/null
SCHOOL_ID="$(api GET '/api/v1/schools/all' | jq -er '
  select(.success == true) | .result.schools | sort_by(.schoolId)
  | .[0].schoolId | select(. != null) | tostring | select(test("^[1-9][0-9]*$"))
')" || fail '유효한 학교를 조회하지 못했습니다. API 응답과 Flyway 마이그레이션을 확인하세요.'

# 2. 기존 회원 시딩 API로 생성한다. jq --arg가 문자열을 JSON에 맞게 이스케이프하며,
# 학교 ID만 API 요청 형식에 맞춰 숫자로 변환한다. 함수의 stdout에는 생성된 memberId만 남긴다.
create_member() {
  local label="$1" body member_id
  body="$(jq -n \
    --arg name "Chat 테스트 $label" \
    --arg nickname "ct${RUN_ID:0:12}-$label" \
    --arg email "chat-$RUN_ID-$label@example.com" \
    --arg schoolId "$SCHOOL_ID" \
    '{name: $name, nickname: $nickname, email: $email, schoolId: ($schoolId | tonumber)}')"
  member_id="$(api POST '/test/seed/member' "$body" | response_id memberId)" || {
    printf '[오류] 회원 %s 생성 실패. APP_SEED_ENABLED와 시딩 설정을 확인하세요.\n' "$label" >&2
    return 1
  }
  printf '회원 %s 생성: memberId=%s\n' "$label" "$member_id" >&2
  printf '%s' "$member_id"
}

# 테스트 토큰 API는 JSON 객체가 아닌 JWT 문자열을 반환한다. 여기서는 서명이 아닌 응답 형식만 검사한다.
get_token() {
  local token
  token="$(api GET "/test/token/access?memberId=$1")" || return 1
  [[ "$token" =~ ^[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+$ ]] || {
    printf '[오류] memberId=%s의 토큰 발급 응답이 올바르지 않습니다.\n' "$1" >&2
    return 1
  }
  printf '%s' "$token"
}

# 3. 준비용 토큰은 로그나 결과 파일에 저장하지 않는다. k6 실행 시 회원 ID로 새 토큰을 발급받는다.
# 4. 생성 응답만 믿지 않고 각 회원의 토큰으로 실제 참여 여부·인원·역할·삭제 상태를 검증한다.
verify_membership() {
  local token="$1" role="$2" member_count="$3"
  api GET "/api/v1/community/threads/$THREAD_ID" '' "$token" | jq -e --arg role "$role" --arg count "$member_count" '
    .success == true and .result.isJoined == true
    and (.result.memberCount | tostring) == $count and .result.myRole == $role
    and .result.deletedAt == null
  ' >/dev/null || fail "방 참여 검증 실패: role=$role"
}

mkdir -p "$OUTPUT_DIR"
ROOMS_TEMP_FILE="$(mktemp "$OUTPUT_DIR/.$RUN_ID.rooms.XXXXXX")"
MEMBER_IDS=()
TOKENS=()
printf '[2/5] 회원 %s명 생성 및 토큰 준비\n' "$USER_COUNT" >&2
for ((USER_INDEX = 1; USER_INDEX <= USER_COUNT; USER_INDEX++)); do
  MEMBER_ID="$(create_member "$USER_INDEX")"
  case " $CREATED_IDS " in *" $MEMBER_ID "*) fail '중복 memberId가 반환됐습니다.' ;; esac
  CREATED_IDS="$CREATED_IDS $MEMBER_ID"
  MEMBER_IDS+=("$MEMBER_ID")
  TOKENS+=("$(get_token "$MEMBER_ID")")
done
ALL_MEMBERS="$(printf '%s\n' "${MEMBER_IDS[@]}" | jq -R . | jq -s .)"

printf '[3/5] 방 %s개 생성\n' "$ROOM_COUNT" >&2
for ((ROOM_INDEX = 0; ROOM_INDEX < ROOM_COUNT; ROOM_INDEX++)); do
  INDICES=()
  while IFS= read -r index; do INDICES+=("$index"); done < <(
    printf '%s' "$ROOM_LAYOUT" | jq -r --argjson room "$ROOM_INDEX" '.[$room][]'
  )
  ROOM_MEMBERS=()
  for index in "${INDICES[@]}"; do ROOM_MEMBERS+=("${MEMBER_IDS[$((index - 1))]}"); done
  OWNER_INDEX="$((INDICES[0] - 1))"
  MEMBERS_JSON="$(printf '%s\n' "${ROOM_MEMBERS[@]}" | jq -R . | jq -s .)"
  THREAD_BODY="$(jq -n --arg title "Chat $RUN_ID $ROOM_INDEX" --argjson members "$MEMBERS_JSON" '
    {title: $title, description: "Chat 부하테스트 준비 데이터", category: "FREE",
     icon: "💬", memberIds: ($members[1:] | map(tonumber))}
  ')"
  THREAD_ID="$(api POST '/api/v1/community/threads' "$THREAD_BODY" "${TOKENS[$OWNER_INDEX]}" | response_id threadId)" \
    || fail '방 생성 실패. 생성자와 초대 대상의 상태를 확인하세요.'
  printf '방 생성: threadId=%s, memberCount=%s\n' "$THREAD_ID" "${#INDICES[@]}" >&2
  for ((POSITION = 0; POSITION < ${#INDICES[@]}; POSITION++)); do
    index="${INDICES[$POSITION]}"
    ROLE=MEMBER
    [ "$POSITION" -ne 0 ] || ROLE=OWNER
    verify_membership "${TOKENS[$((index - 1))]}" "$ROLE" "${#INDICES[@]}"
  done
  jq -n --arg threadId "$THREAD_ID" --argjson members "$MEMBERS_JSON" \
    '{threadId: $threadId, memberIds: $members}' >> "$ROOMS_TEMP_FILE"
done

# 여러 방에 참여한 회원도 전체 목록에는 한 번만 저장한다. 토큰은 파일에 포함하지 않는다.
printf '[5/5] 검증된 시드 파일 저장\n' >&2
TEMP_FILE="$(mktemp "$OUTPUT_DIR/.$RUN_ID.XXXXXX")"
jq -es --arg runId "$RUN_ID" --arg baseUrl "$BASE_URL" --arg schoolId "$SCHOOL_ID" \
  --argjson members "$ALL_MEMBERS" --argjson roomCount "$ROOM_COUNT" '
  select(length == $roomCount) |
  {runId: $runId, baseUrl: $baseUrl, schoolId: $schoolId, memberIds: $members, rooms: .}
' "$ROOMS_TEMP_FILE" > "$TEMP_FILE"
mv "$TEMP_FILE" "$OUTPUT_FILE"
TEMP_FILE=""
printf '준비 완료: %s\n' "$OUTPUT_FILE"
