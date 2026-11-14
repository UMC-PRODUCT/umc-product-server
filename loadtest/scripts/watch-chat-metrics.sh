#!/usr/bin/env bash
# 접근 가능한 관리 포트에서 Prometheus 원본을 보관한다. DB 및 앱 설정은 바꾸지 않는다.
set -euo pipefail
fail() { printf '[오류] %s\n' "$*" >&2; exit 1; }
if [ "${1:-}" = "--help" ]; then
  printf 'CHAT_METRICS_URL=http://127.0.0.1:<포워딩 포트>/actuator/prometheus CHAT_TEST_ID=<실행 ID> bash loadtest/scripts/watch-chat-metrics.sh <저장 폴더>\n'
  printf 'CHAT_METRICS_INTERVAL_SECONDS=5 CHAT_METRICS_SAMPLES=120. 원본 누적 histogram과 수집 시각을 저장합니다.\n'
  exit 0
fi
[ "$#" = 1 ] || fail '저장 폴더 하나를 지정하세요.'
[[ "${CHAT_METRICS_URL:-}" =~ ^https?:// ]] || fail 'CHAT_METRICS_URL에 접근 가능한 Prometheus 관리 URL을 지정하세요.'
[[ "${CHAT_TEST_ID:-}" =~ ^[a-zA-Z0-9][a-zA-Z0-9_-]{0,99}$ ]] || fail 'CHAT_TEST_ID가 필요합니다.'
INTERVAL="${CHAT_METRICS_INTERVAL_SECONDS:-5}"
SAMPLES="${CHAT_METRICS_SAMPLES:-120}"
[[ "$INTERVAL" =~ ^[1-9][0-9]{0,2}$ ]] || fail '수집 간격은 1~999초 정수여야 합니다.'
[[ "$SAMPLES" =~ ^[1-9][0-9]{0,5}$ ]] || fail '표본 수는 1~999999 정수여야 합니다.'
for tool in curl jq; do command -v "$tool" >/dev/null || fail "$tool 명령이 필요합니다."; done
mkdir -p "$1"
OUT="$(mktemp -d "$1/$CHAT_TEST_ID-metrics.XXXXXX")"
printf '서버 메트릭 저장 폴더: %s\n' "$OUT" >&2
for ((index=1; index<=SAMPLES; index++)); do
  started="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
  file="$(printf '%06d.prom' "$index")"
  curl --fail --silent --show-error --connect-timeout 3 --max-time 10 \
    "$CHAT_METRICS_URL" -o "$OUT/$file.tmp" || fail '수집 실패. 이 실행의 서버 관측에는 공백이 있습니다.'
  # 로그인 HTML이나 잘못된 포트의 응답을 정상 수집으로 처리하지 않는다.
  if ! LC_ALL=C grep -q '^jvm_' "$OUT/$file.tmp"; then fail 'JVM Prometheus 메트릭 응답이 아닙니다.'; fi
  mv "$OUT/$file.tmp" "$OUT/$file"
  jq -cn --arg testId "$CHAT_TEST_ID" --arg startedAt "$started" \
    --arg endedAt "$(date -u +%Y-%m-%dT%H:%M:%SZ)" --arg file "$file" \
    '{testId:$testId,startedAt:$startedAt,endedAt:$endedAt,file:$file}' >> "$OUT/samples.jsonl"
  if [ "$index" -lt "$SAMPLES" ]; then sleep "$INTERVAL"; fi
done
