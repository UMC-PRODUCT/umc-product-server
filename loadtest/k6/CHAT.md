# Chat 부하 테스트

Chat WebSocket/STOMP 부하테스트의 실행 방법과 이슈별 시나리오 추가 방법을 안내한다.
처음에는 아래 두 테스트로 환경과 측정이 정상 동작하는지 확인한다.

- **두 사용자 Smoke** — 인증·연결부터 메시지 ACK와 상대방 수신까지 기본 동작 확인.
- **소규모 반복 부하** — 여러 사용자의 반복 송수신에서 전송량·지연·실패 측정과 실행별 결과 저장 확인.

이후 개선 작업에서는 이 기본 시나리오의 인원·방·전송 조건을 조절하거나 필요한 행동·검증을 추가한다.
각 이슈의 문제를 재현할 조건을 정하고, 같은 조건으로 개선 전후를 비교한다.

**데이터 준비 → Smoke → 반복 부하 → 결과 확인** 순서로 실행한다. 아래 명령은 모두 저장소 루트 기준이다.

## 실행

로컬 앱·PostgreSQL이 실행 중이어야 한다. 필요한 도구는 `k6`, `jq`, `curl`, `uuidgen`, `git`, `bash`다.
앱의 `APP_TEST_API_ENABLED`, `APP_SEED_ENABLED`가 켜져 있어야 한다.
학교 데이터가 하나 이상 필요하므로 조회에 실패하면 로컬 DB의 Flyway 적용 상태를 확인한다.

### 1. 두 사용자 Smoke

회원 두 명과 함께 참여한 방 하나를 준비한다.

```bash
SUT_URL=http://localhost:8080 CHAT_USERS=2 CHAT_ROOMS=1 \
  bash loadtest/scripts/prepare-chat-data.sh
```

출력된 실제 시드 경로를 넣고 실행한다.

```bash
CHAT_SMOKE_SEED='loadtest/k6/data/chat/<출력된 UUID>.json'

BASE_URL=http://localhost:8080 PROFILE=smoke SEED_FILE="$CHAT_SMOKE_SEED" \
  bash loadtest/scripts/run-chat-test.sh smoke
```

1 VU가 두 사용자의 연결·인증·구독을 준비하고, 메시지 한 건의 ACK와 상대방 수신을 확인한다.
정상이면 `Chat Smoke 송수신 성공`과 7개 check 성공이 나온다. 시드의 **첫 방에 정확히 두 명**이 있어야 한다.

### 2. 소규모 반복 부하

Smoke가 성공하면 4명·1방 데이터를 별도로 준비한다.

```bash
SUT_URL=http://localhost:8080 CHAT_USERS=4 CHAT_ROOMS=1 \
  bash loadtest/scripts/prepare-chat-data.sh

CHAT_LOAD_SEED='loadtest/k6/data/chat/<이번에 출력된 UUID>.json'

BASE_URL=http://localhost:8080 PROFILE=load SEED_FILE="$CHAT_LOAD_SEED" \
CHAT_USERS=4 CHAT_ROOMS=1 CHAT_MESSAGE_INTERVAL_MS=1000 DURATION=30s \
  bash loadtest/scripts/run-chat-test.sh small-load
```

기본은 사용자당 1 VU이며 모두 메시지를 보낸다. ACK를 기다린 뒤 **직전 전송 시작부터 1초가 되도록 남은 시간만** 쉰다.
ACK가 늦으면 전송량도 줄어든다. HTTP 테스트의 `RATE`처럼 초당 요청 수를 고정하는 방식은 아니다.
`DURATION`은 전송 구간이며, 전체 실행에는 준비·마지막 응답 대기·종료 시간이 더해진다.

데이터 준비 스크립트는 실행할 때마다 새 회원·방을 만든다.
준비 중 실패해도 이미 생성된 데이터는 남으며, 자동 삭제·롤백하지 않는다.

## 설정

회원·방은 **준비할 때**, 전송 방식은 **실행할 때** 정한다. 사용자 번호는 DB ID가 아니라 시드 `memberIds`의 1부터 시작하는 순서다.

| 변수 | 기본값 | 용도 |
|---|---|---|
| `SUT_URL` / `BASE_URL` | 준비 시 필수 / `http://localhost:8080` | 준비 / 실행 대상 앱 주소. 두 주소가 같아야 함 |
| `SEED_FILE` | 필수 | 준비된 시드 파일 경로 |
| `PROFILE` / `SCENARIO` | 래퍼: `load` / `chat-message` | 실행할 프로필·시나리오. 현재 Chat은 `smoke`, `load` 지원 |
| `CHAT_USERS` / `CHAT_ROOMS` | `2` / `1` | 회원·방 수. 반복 부하 실행 시 시드의 수와 일치해야 함 |
| `CHAT_ROOM_MEMBERS` | 균등 분배 | 준비할 방별 사용자 번호. 예: `[[1,2,3],[2,4]]` |
| `CHAT_SENDERS` | 전체 사용자 | 반복 부하에서 메시지를 보낼 사용자 번호. 예: `[1]` |
| `CHAT_USERS_PER_VU` | `1` | VU당 사용자 수 |
| `CHAT_VU_USERS` | 미지정 | VU별 직접 배정. 예: `[[1,3],[2,4]]`. `CHAT_USERS_PER_VU`와 동시 지정 불가 |
| `CHAT_MESSAGE_INTERVAL_MS` / `DURATION` | `1000` / `30s` | 반복 전송 간격(ms) / 전송 구간 |
| `CHAT_TIMEOUT_MS` | `10000` | 응답 대기·전달 지연 판정 시간(ms) |
| `CHAT_STARTUP_TIMEOUT_MS` | `10000` | 반복 부하 시작 전 연결·구독 준비 시간(ms) |
| `CHAT_RUN_NOTES` | 빈 문자열 | 앱·DB 버전, 데이터 규모, 서버 지표 위치 등 실행 메모 |

Smoke에는 반복 부하의 인원·배정·전송 설정이 적용되지 않는다. 설정 범위와 검증은 [config](config/) 참조.

방 참여와 VU 배정은 독립적이다. 모든 사용자는 적어도 한 방에 참여하고, 반복 부하에서는 정확히 한 VU에 배정되어야 한다.
여러 방에 참여한 송신자는 자신의 방을 순환한다. 수신 전용 사용자도 메시지를 한 건 이상 받아야 성공하므로 같은 방에 송신자를 포함한다.

예를 들어 4명을 두 방에 겹쳐 참여시키려면 준비 명령에 다음 설정을 사용한다.

```bash
SUT_URL=http://localhost:8080 CHAT_USERS=4 CHAT_ROOMS=2 \
CHAT_ROOM_MEMBERS='[[1,2,3],[2,4]]' \
  bash loadtest/scripts/prepare-chat-data.sh
```

이후 실행할 때는 이 시드와 `CHAT_USERS=4 CHAT_ROOMS=2`를 지정한다. 방 구성을 바꾸려면 JSON만 고치지 말고 실제 데이터를 다시 준비한다.

## 결과 확인

실행마다 `loadtest/k6/out/chat/<UTC 시각>-<실행 이름>.<임의 문자열>/`에 저장한다. 같은 이름으로 재실행해도 덮어쓰지 않는다.

| 파일 | 내용 |
|---|---|
| `summary.json` / `console.log` | k6 집계 결과 / 실행 로그 |
| `conditions.json` | 기본값을 포함한 설정, 사용자·방·VU 배정, k6 옵션 |
| `run-meta.json` | 커밋·미커밋 변경 여부, 실행 시각·종료 코드, k6 버전, 메모 |
| `seed.json` | 실제 사용한 회원·방 구성. DB 스냅샷은 아님 |

먼저 오류·누락을 확인하고 지연과 실제 전송량을 읽는다.

- **ACK 지연** (`umc_chat_ack_latency_ms`): 전송 → 서버의 생성 ACK 수신.
- **전달 지연** (`umc_chat_delivery_latency_ms`): 전송 → 상대방의 메시지 수신. ACK 이후 시간이 아니다.
- **전송·수신 수**: `umc_chat_message_attempts`, `umc_chat_ack_successes`, `umc_chat_delivery_received`.
- **실패·누락**: `umc_chat_failures`, `umc_chat_delivery_missing`, `umc_chat_delivery_late`. `umc_chat_completion_missing`은 송신 완료 신호를 못 받아 예상 건수를 확정하지 못한 경우다.
- **중복**: `umc_chat_duplicate_events`.

늦게라도 정상 수신한 메시지는 실제 전달 지연을 통계에 포함하고, `delivery_late`에도 집계한다.
끝내 수신하지 못한 메시지는 누락으로 집계하며 지연 값을 임의로 넣지 않는다. 지연 통계와 실패·누락 건수를 함께 확인한다.
Smoke는 ACK와 상대방 수신을 합쳐 메시지 성공을 판정하고, 반복 부하는 ACK와 전달을 따로 집계한다.
상대방 수신 수에서는 자기 메시지 수신을 제외한다. Counter의 `/s`는 준비·종료 시간도 포함하므로 전송 구간의 처리량과 구분한다.

실패한 실행도 기록하지만 초기화 실패 시 일부 파일이 없을 수 있다. 종료 코드와 로그부터 확인한다.
실행별 기록에는 래퍼를 사용한다. 직접 실행 방식은 [공통 k6 실행](../README.md#공통-k6-실행) 참조.

### 개선 전후 비교

비교할 사용자·방 구성, 전송 간격·시간, 앱·DB 버전과 데이터 규모를 `CHAT_RUN_NOTES`에 남긴다.
시드를 재사용해도 메시지·Outbox 데이터는 늘어나며, 기록된 Git 커밋이 실행 중인 앱의 빌드를 보장하지는 않는다.

서버 지표를 함께 보는 기준은 [관측](../README.md#관측) 참조. 로컬 앱의 수집 여부는 별도로 확인해야 하며, Chat 래퍼는 서버 지표를 저장하지 않는다.
준비·종료 확인용 메시지는 측정에서 제외하지만 서버에서는 실제 처리되므로 준비 조건도 동일하게 유지한다.
측정은 Community·Chat·Outbox·WebSocket을 거치는 경로의 지연이다. 로컬 결과를 운영 최대 수용량으로 해석하지 않는다.

## 새 시나리오 추가하기

인원·방·송신자·간격만 다르면 기존 설정을 사용한다. 행동이나 성공 조건이 달라질 때 새 시나리오를 추가한다.

1. [scenarios/load/chat-message.js](scenarios/load/chat-message.js)를 바탕으로 새 파일을 만든다. Smoke는 [scenarios/smoke/chat-message.js](scenarios/smoke/chat-message.js) 참조.
2. `runChatUsers`에 넘기는 사용자별 **`act`(행동), `checks`(성공 조건)** 를 작성한다. 기본 송신·수신 행동은 [chat-message-behavior.js](scenarios/helpers/chat-message-behavior.js)를 참고한다.
3. `getRunConditions(seed)`에 실제 적용한 설정과 새 행동의 조건을 담는다. 동기적으로 JSON 저장 가능한 객체를 반환하며 토큰은 넣지 않는다.
4. [script.js](script.js)에 import하고 해당 `REGISTRY` 프로필에 `fn`, `requiresSeed`, `getOptions`, `validateSeed`, `prepareRun`, `getRunConditions`를 연결한다. Smoke처럼 공통 시작 시각이 필요 없으면 `prepareRun`은 생략한다.
5. 래퍼에 `SCENARIO=<등록한 이름>`을 지정해 작게 실행한다. 조건 기록 함수가 없으면 래퍼는 실행을 거부한다.

전체 디렉터리는 [k6 구조](README.md#구조) 참조. Chat 행동을 바꿀 때는 아래 코드를 재사용한다.
`act`의 반환값은 `checks`에서 `state.actionResult`로 확인할 수 있다.

| 공통 코드 | 역할 |
|---|---|
| [chat-client.js](lib/chat/chat-client.js) | 사용자 한 명의 인증·연결·TEXT 송수신 |
| [chat-load-session.js](lib/chat/chat-load-session.js) | 사용자별 준비·행동 실행·측정·종료 |
| [chat-load.js](lib/chat/chat-load.js), [chat-metrics.js](lib/chat/chat-metrics.js) | 사용자·방 구성 검증, VU 간 전달 추적, 지표 집계 |

현재 반복 부하는 순차 TEXT 생성과 ACK 확인을 기준으로 한다. 병렬 전송·재시도·다른 명령을 추가할 때는 응답 검증과 전달 추적도 함께 확인한다.
새 시드 필드가 필요하면 준비 스크립트와 실행 래퍼의 시드 복사 대상도 수정한다.

코드 변경 후 회귀 테스트와 실제 Smoke·소규모 부하를 확인한다.

```bash
node --test loadtest/k6/tests/*.test.mjs
./gradlew check
```
