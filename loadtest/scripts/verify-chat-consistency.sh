#!/usr/bin/env bash
# 테스트가 끝난 전용 방의 저장 상태를 읽기 전용 SQL로 확인한다.
set -euo pipefail
fail() { printf '[오류] %s\n' "$*" >&2; exit 1; }
[ "$#" = 1 ] && [ -f "$1" ] || fail '사용법: PGDATABASE=umc_product_pr<번호> bash loadtest/scripts/verify-chat-consistency.sh <conditions.json>'
[[ "${PGDATABASE:-}" =~ ^umc_product_pr[0-9]+$ ]] || fail '대상 Preview DB를 PGDATABASE에 지정하세요.'
[ -n "${PGHOST:-}${PGSERVICE:-}" ] || fail 'PGHOST 또는 PGSERVICE를 지정하세요.'
PSQL_BIN="${PSQL_BIN:-psql}"
command -v "$PSQL_BIN" >/dev/null || fail 'PSQL_BIN에 psql 경로를 지정하세요.'
THREAD_IDS="$(jq -ce '[((.reactionSeed.rooms // .rooms // .targets)[]) | .threadId] | unique
  | select(length > 0 and all(.[]; type == "string" and test("^[1-9][0-9]*$")))' "$1")" \
  || fail 'conditions.json에 검증할 방 ID가 없습니다.'
RUN_ID="$(jq -r '.comparison.runId // ""' "$1")"
TEST_ID="$(jq -r '.comparison.testId // "unassigned"' "$1")"
export PGOPTIONS="${PGOPTIONS:-} -c default_transaction_read_only=on -c statement_timeout=30000"
export PGAPPNAME=chat-consistency-check
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
RESULT="$("$PSQL_BIN" -X --no-password --dbname="$PGDATABASE" --quiet --no-align --tuples-only --set=ON_ERROR_STOP=1 \
  --set="thread_ids=$THREAD_IDS" --set="run_id=$RUN_ID" --set="test_id=$TEST_ID" \
  --file="$SCRIPT_DIR/chat-consistency.sql")"
printf '%s\n' "$RESULT"
jq -e '.passed == true' <<< "$RESULT" >/dev/null || fail '정합성 위반이 있습니다. 결과 JSON을 확인하세요.'
