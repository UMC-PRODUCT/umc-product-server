#!/usr/bin/env bash
# Preview DB의 락 대기를 한 연결에서 관측한다. SQL 원문과 비밀번호는 출력하지 않는다.
set -euo pipefail

if [ "${1:-}" = "--help" ]; then
  printf '사용법: PGDATABASE=umc_product_pr<번호> bash loadtest/scripts/watch-reaction-locks.sh > locks.jsonl\n'
  printf '접속: PGHOST, PGPORT, PGUSER, PGPASSFILE 또는 libpq의 .pgpass/서비스 설정 사용\n'
  printf 'PSQL_BIN으로 psql 경로 지정 가능. psql 16 이상과 pg_read_all_stats 조회 권한 필요\n'
  printf 'LOCK_WATCH_INTERVAL_MS=200 LOCK_WATCH_SAMPLES=1500 (기본 5분)\n'
  printf '각 줄은 DB 시각, 대기 세션 수, 쿼리 종류와 차단 PID를 담은 JSON입니다.\n'
  printf 'waitAgeMs는 관측 시점의 진행 중 대기 시간이며 요청별 전체 락 대기 시간은 아닙니다.\n'
  exit 0
fi

fail() { printf '[오류] %s\n' "$*" >&2; exit 1; }
[ "$#" -eq 0 ] || fail '지원하지 않는 인자입니다. --help를 확인하세요.'
[[ "${PGDATABASE:-}" =~ ^umc_product_pr[0-9]+$ ]] || fail 'PGDATABASE에 대상 Preview DB 이름을 지정하세요.'
PSQL_BIN="${PSQL_BIN:-psql}"
command -v "$PSQL_BIN" >/dev/null 2>&1 || fail 'psql이 필요합니다. PSQL_BIN으로 설치 경로를 지정할 수 있습니다.'
INTERVAL="${LOCK_WATCH_INTERVAL_MS:-200}"
SAMPLES="${LOCK_WATCH_SAMPLES:-1500}"
[[ "$INTERVAL" =~ ^[1-9][0-9]{0,4}$ ]] && [ "$INTERVAL" -ge 100 ] && [ "$INTERVAL" -le 60000 ] \
  || fail 'LOCK_WATCH_INTERVAL_MS는 100~60000 사이 정수여야 합니다.'
[[ "$SAMPLES" =~ ^[1-9][0-9]{0,5}$ ]] || fail 'LOCK_WATCH_SAMPLES는 1~999999 사이 정수여야 합니다.'
SECONDS_VALUE="$(awk -v milliseconds="$INTERVAL" 'BEGIN { printf "%.3f", milliseconds / 1000 }')"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# 관측 연결 자체가 시험 대상 풀을 사용하지 않도록 psql로 직접 접속한다.
# PGOPTIONS는 libpq가 읽으며 셸 명령행이나 결과 파일에 자격증명을 복사하지 않는다.
export PGOPTIONS="${PGOPTIONS:-} -c default_transaction_read_only=on -c statement_timeout=2000"
export PGAPPNAME=chat-reaction-lock-watch
ACCESS="$("$PSQL_BIN" -X --no-password --quiet --no-align --tuples-only --set=ON_ERROR_STOP=1 \
  -c "SELECT pg_has_role(current_user, 'pg_read_all_stats', 'USAGE') AND current_database() = '$PGDATABASE'")"
[ "$ACCESS" = t ] || fail '대상 Preview DB와 다른 세션의 통계 조회 권한(pg_read_all_stats)을 확인하세요.'
"$PSQL_BIN" -X --no-password --quiet --no-align --tuples-only --set=ON_ERROR_STOP=1 \
  --set="watch_seconds=$SECONDS_VALUE" --set="sample_count=$SAMPLES" \
  --file="$SCRIPT_DIR/reaction-lock-snapshot.sql" | sed -n '/^{/p'
