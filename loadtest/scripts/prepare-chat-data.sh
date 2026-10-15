#!/usr/bin/env bash
# Chat Smoke 테스트에 사용할 회원 2명과 방 1개를 준비하고, 검증된 ID를 JSON으로 저장한다.
# 매 실행마다 새 데이터를 생성한다. 메시지 송수신과 성능 측정은 이후 k6 시나리오가 담당한다.

# 미정의 변수와 파이프라인 중간의 실패도 오류로 취급해 잘못된 ID로 다음 단계를 진행하지 않게 한다.
set -euo pipefail

if [ "${1:-}" = "--help" ]; then
  printf '사용법: SUT_URL=http://localhost:8080 bash loadtest/scripts/prepare-chat-data.sh\n'
  printf '필요 도구: curl, jq, uuidgen, git\n'
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

for tool in curl jq uuidgen git; do
  command -v "$tool" >/dev/null 2>&1 || fail "필요한 명령어가 없습니다: $tool"
done

# 실행한 디렉터리와 관계없이 저장 위치를 고정하고, UUID로 회원 정보와 결과 파일의 충돌을 피한다.
REPO_ROOT="$(git -C "$(dirname "$0")" rev-parse --show-toplevel)"
RUN_ID="$(uuidgen | tr '[:upper:]' '[:lower:]')"
OUTPUT_DIR="$REPO_ROOT/loadtest/k6/data/chat"
OUTPUT_FILE="$OUTPUT_DIR/$RUN_ID.json"
TEMP_FILE=""
SENDER_ID=""
RECEIVER_ID=""
THREAD_ID=""

# 실패 시 미완성 파일만 지운다. API로 이미 생성한 데이터는 남기고, 추적할 수 있도록 ID를 출력한다.
on_exit() {
  local status="$?"
  if [ -n "$TEMP_FILE" ]; then
    rm -f "$TEMP_FILE"
  fi
  if [ "$status" -ne 0 ]; then
    printf '[오류] 준비 중단: runId=%s, senderId=%s, receiverId=%s, threadId=%s\n' \
      "$RUN_ID" "${SENDER_ID:--}" "${RECEIVER_ID:--}" "${THREAD_ID:--}" >&2
    printf '실패 이전에 생성된 데이터가 있다면 그대로 남습니다. 자동 삭제하거나 생성 요청을 재시도하지 않습니다.\n' >&2
  fi
}
trap on_exit EXIT

# API 응답은 stdout, 안내·오류는 stderr로 분리한다. $(api ...)나 jq에는 응답만 전달되어야 한다.
# 생성 요청은 응답이 유실돼도 서버에서 성공했을 수 있으므로 자동 재시도하지 않는다.
api() {
  local method="$1" path="$2" body="${3:-}" token="${4:-}"
  local args=(--fail --silent --show-error --connect-timeout 5 --max-time 30 --request "$method")
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
    --arg nickname "ct-${RUN_ID:0:12}-$label" \
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

printf '[2/5] 발신자와 수신자 생성\n' >&2
SENDER_ID="$(create_member a)"
RECEIVER_ID="$(create_member b)"
[ "$SENDER_ID" != "$RECEIVER_ID" ] || fail '발신자와 수신자의 memberId가 같습니다.'

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
printf '[3/5] 토큰 발급 및 방 생성\n' >&2
SENDER_TOKEN="$(get_token "$SENDER_ID")"
RECEIVER_TOKEN="$(get_token "$RECEIVER_ID")"
# A는 생성자로 자동 참여하므로 초대 목록에는 B만 넣는다. 외부 API가 반환한 threadId를 사용한다.
THREAD_BODY="$(jq -n --arg title "Chat Smoke $RUN_ID" --arg receiverId "$RECEIVER_ID" '
  {title: $title, description: "Chat 부하테스트 준비 데이터", category: "FREE",
   icon: "💬", memberIds: [($receiverId | tonumber)]}
')"
THREAD_ID="$(api POST '/api/v1/community/threads' "$THREAD_BODY" "$SENDER_TOKEN" | response_id threadId)" \
  || fail '방 생성 실패. 생성자와 초대 대상의 상태를 확인하세요.'
printf '방 생성: threadId=%s\n' "$THREAD_ID" >&2

# 4. 생성 응답만 믿지 않고 각 회원의 토큰으로 실제 참여 여부·인원·역할·삭제 상태를 검증한다.
verify_membership() {
  local token="$1" role="$2"
  api GET "/api/v1/community/threads/$THREAD_ID" '' "$token" | jq -e --arg role "$role" '
    .success == true and .result.isJoined == true
    and (.result.memberCount | tostring) == "2" and .result.myRole == $role
    and .result.deletedAt == null
  ' >/dev/null || fail "방 참여 검증 실패: role=$role"
}

printf '[4/5] 두 회원의 방 참여 상태 검증\n' >&2
verify_membership "$SENDER_TOKEN" OWNER
verify_membership "$RECEIVER_TOKEN" MEMBER

# 5. 모든 검증이 끝난 뒤 임시 파일을 완성된 파일명으로 옮겨, 불완전한 JSON이 입력으로 쓰이지 않게 한다.
# memberIds의 첫 회원은 발신자(OWNER), 두 번째 회원은 수신자(MEMBER)다.
printf '[5/5] 실행별 시드 파일 저장\n' >&2
mkdir -p "$OUTPUT_DIR"
TEMP_FILE="$(mktemp "$OUTPUT_DIR/.$RUN_ID.XXXXXX")"
jq -n --arg runId "$RUN_ID" --arg baseUrl "$BASE_URL" --arg schoolId "$SCHOOL_ID" \
  --arg senderId "$SENDER_ID" --arg receiverId "$RECEIVER_ID" --arg threadId "$THREAD_ID" '
  {runId: $runId, baseUrl: $baseUrl, schoolId: $schoolId,
   memberIds: [$senderId, $receiverId],
   rooms: [{threadId: $threadId, memberIds: [$senderId, $receiverId]}]}
' > "$TEMP_FILE"
mv "$TEMP_FILE" "$OUTPUT_FILE"
TEMP_FILE=""
printf '준비 완료: %s\n' "$OUTPUT_FILE"
