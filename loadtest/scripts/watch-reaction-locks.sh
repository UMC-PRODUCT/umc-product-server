#!/usr/bin/env bash
# Preview DB의 락 대기를 한 연결에서 관측한다. SQL 원문과 비밀번호는 출력하지 않는다.
set -euo pipefail

if [ "${1:-}" = "--help" ]; then
  printf '사용법: PGDATABASE=umc_product_pr<번호> bash loadtest/scripts/watch-reaction-locks.sh > locks.jsonl\n'
  printf '접속: PGHOST, PGPORT, PGUSER, PGPASSFILE 또는 libpq의 .pgpass/서비스 설정 사용\n'
  printf 'PSQL_BIN으로 psql 경로 지정 가능. psql 16 이상 필요\n'
  printf '서버와 같은 DB 계정 또는 통계 조회 권한이 있는 계정으로 접속하세요.\n'
  printf '시작 전 다른 세션의 상태와 쿼리 정보가 실제로 조회되는지 검사합니다.\n'
  printf 'LOCK_WATCH_INTERVAL_MS=200 LOCK_WATCH_SAMPLES=1500 (기본 5분)\n'
  printf '각 줄은 DB 시각, 대기 세션 수, 쿼리 종류와 차단 PID를 담은 JSON입니다.\n'
  printf 'waitAgeMs는 관측 시점의 진행 중 대기 시간이며 요청별 전체 락 대기 시간은 아닙니다.\n'
  printf 'CHAT_TEST_ID로 실행 식별자를 기록합니다. 차단 세션의 idle SQL은 마지막 실행 SQL입니다.\n'
  exit 0
fi

fail() { printf '[오류] %s\n' "$*" >&2; exit 1; }
[ "$#" -eq 0 ] || fail '지원하지 않는 인자입니다. --help를 확인하세요.'
[[ "${PGDATABASE:-}" =~ ^umc_product_pr[0-9]+$ ]] || fail 'PGDATABASE에 대상 Preview DB 이름을 지정하세요.'
[ -n "${PGHOST:-}${PGSERVICE:-}" ] || fail 'PGHOST 또는 PGSERVICE를 지정하세요. GUI의 SSH 터널은 이 터미널과 자동 공유되지 않습니다.'
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
ACCESS="$("$PSQL_BIN" -X --no-password --dbname="$PGDATABASE" --quiet --no-align --tuples-only --set=ON_ERROR_STOP=1 \
  -c "SELECT CASE
        WHEN current_database() <> '$PGDATABASE' THEN 'wrong_database'
        WHEN count(*) = 0 THEN 'no_sessions'
        WHEN count(*) FILTER (WHERE state IS NULL OR query IS NULL
                              OR query = '<insufficient privilege>' OR state = 'disabled') > 0
          THEN 'unreadable_sessions'
        ELSE 'ok'
      END
      FROM pg_stat_activity
      WHERE datname = current_database() AND pid <> pg_backend_pid()
        AND (backend_type = 'client backend' OR backend_type IS NULL)")"
case "$ACCESS" in
  ok) ;;
  wrong_database) fail '접속한 DB가 PGDATABASE와 다릅니다.' ;;
  no_sessions) fail '확인할 다른 DB 세션이 없습니다. Preview 서버가 DB에 연결된 뒤 다시 실행하세요.' ;;
  unreadable_sessions) fail '조회할 수 없는 세션 정보가 있습니다. 서버와 같은 DB 계정, 통계 조회 권한과 track_activities 설정을 확인하세요.' ;;
  *) fail 'DB 세션 관측 가능 여부를 확인하지 못했습니다.' ;;
esac

"$PSQL_BIN" -X --no-password --dbname="$PGDATABASE" --quiet --no-align --tuples-only --set=ON_ERROR_STOP=1 \
  --set="watch_seconds=$SECONDS_VALUE" --set="sample_count=$SAMPLES" \
  --set="test_id=${CHAT_TEST_ID:-unassigned}" \
  --file="$SCRIPT_DIR/reaction-lock-snapshot.sql" | sed -n '/^{/p'
