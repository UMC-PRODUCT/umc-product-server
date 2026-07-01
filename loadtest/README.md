# k6 공지사항 로그인 Flow 부하 테스트

## 목적

`notice-login-1000-vus.js`는 약 1000명의 사용자가 동시에 다음 흐름을 수행할 때 API와 DB가 SLO를 만족하는지 확인한다.

1. 이메일/PW 로그인
2. 공지사항 목록 조회
3. 공지사항 상세 조회
4. 공지사항 읽음 처리
5. 공지사항 읽기 현황 조회
6. 다시 공지사항 목록 조회

기본 executor는 `ramping-vus`다. 즉 `TARGET_VUS`는 HTTP RPS가 아니라 동시에 flow를 수행하는 가상 사용자 수다.

## 사전 조건

- production 대상 실행 금지. staging 또는 local 부하 테스트 환경에서만 실행한다.
- 테스트 대상 서버가 실행 중이어야 한다. 기본값은 `BASE_URL=http://localhost:8080`이다.
- 공지 목록 조회에는 `GISU_ID`가 필요하다.
- `INCLUDE_READ_STATUS=true`일 때는 대상 계정이 해당 공지의 `CHECK` 권한을 가져야 한다. 일반 챌린저 계정으로 실행하면 `GET /api/v1/notices/{noticeId}/read-status`가 403을 반환할 수 있다.
- 1000 VU를 신뢰성 있게 검증하려면 서로 다른 로그인 계정 1000개를 준비한다.

## 테스트 계정 파일

로컬 계정 파일은 repository에 커밋하지 않는다. 다음 명령으로 `users.local.json`을 생성한다.

```bash
COUNT=1000 \
EMAIL_DOMAIN=test.umc.it.kr \
PASSWORD='password12!' \
node loadtest/tools/generate-users.mjs > loadtest/data/users.local.json
```

시딩 API를 사용할 경우 non-prod profile에서 `APP_SEED_ENABLED=true`를 켜고 아래 순서로 데이터를 만든다.

```bash
curl -X POST "$BASE_URL/test/seed/members" \
  -H 'Content-Type: application/json' \
  -d '{"count":1000,"force":true}'

curl -X POST "$BASE_URL/test/seed/challengers" \
  -H 'Content-Type: application/json' \
  -d '{"gisuId":1,"countPerPartPerSchool":10,"parts":null,"chapterIds":null}'

curl -X POST "$BASE_URL/test/seed/notice" \
  -H 'Content-Type: application/json' \
  -d '{"gisuId":1,"authorMemberId":1,"globalCount":5,"perChapterCount":0,"perSchoolCount":0,"perPartCount":0,"parts":null}'
```

`authorMemberId`는 공지 생성 권한이 있는 멤버로 바꿔야 한다.

## 실행

지속 부하 기본 실행:

```bash
k6 run \
  -e BASE_URL=http://localhost:8080 \
  -e GISU_ID=1 \
  -e TARGET_VUS=1000 \
  -e RAMP_UP=5m \
  -e HOLD=10m \
  -e RAMP_DOWN=2m \
  loadtest/scenarios/load/notice-login-1000-vus.js
```

1000명이 flow를 1회씩만 수행하는 동시성 점검:

```bash
k6 run \
  -e BASE_URL=http://localhost:8080 \
  -e GISU_ID=1 \
  -e TARGET_VUS=1000 \
  -e RUN_ONCE=true \
  loadtest/scenarios/load/notice-login-1000-vus.js
```

Prometheus remote write로 Grafana에서 확인:

```bash
K6_PROMETHEUS_RW_SERVER_URL=http://localhost:19090/api/v1/write \
K6_PROMETHEUS_RW_TREND_STATS=p(50),p(90),p(95),p(99),avg,min,max \
k6 run -o experimental-prometheus-rw \
  -e BASE_URL=http://localhost:8080 \
  -e GISU_ID=1 \
  -e TARGET_VUS=1000 \
  --tag env=staging \
  --tag service=umc-product-server \
  --tag testid=notice-login-$(date +%Y%m%d-%H%M%S) \
  loadtest/scenarios/load/notice-login-1000-vus.js
```

현재 `docker/monitoring`의 Prometheus는 OTLP receiver는 켜져 있지만 remote write receiver는 기본으로 열려 있지 않다. k6 remote write를 직접 받으려면 Prometheus 실행 옵션에 `--web.enable-remote-write-receiver`를 추가하거나, k6 결과 전용 Prometheus/Mimir tenant를 사용한다.

## 주요 환경변수

| 변수 | 기본값 | 설명 |
|---|---:|---|
| `BASE_URL` | `http://localhost:8080` | 테스트 대상 API 서버 |
| `USERS_FILE` | `../../data/users.local.json` | k6 스크립트 기준 로그인 계정 파일 경로 |
| `TARGET_VUS` | `1000` | 동시 사용자 수 |
| `RUN_ONCE` | `false` | `true`면 각 VU가 flow를 1회만 수행 |
| `ALLOW_USER_REUSE` | `false` | 계정 수가 VU보다 적을 때 재사용 허용 |
| `GISU_ID` | 없음 | 공지 조회 필수 파라미터 |
| `NOTICE_ID` | 없음 | 지정하면 목록 첫 항목 대신 해당 공지 상세 조회 |
| `NOTICE_TAB` | `CHALLENGER` | 공지 탭 |
| `INCLUDE_RECORD_READ` | `true` | 읽음 처리 포함 여부 |
| `INCLUDE_READ_STATUS` | `true` | 읽기 현황 조회 포함 여부 |
| `THINK_MIN_MS` / `THINK_MAX_MS` | `500` / `2000` | 사용자 행동 간 대기 시간 |

## 해석 기준

- `vus`가 1000까지 도달했는지 먼저 확인한다.
- `http_reqs`는 실제 HTTP RPS이고, `iterations`는 전체 사용자 flow 반복 횟수다.
- flow 하나가 최대 6개 요청을 보내므로, 대략 `HTTP RPS = iteration/s * flow당 요청 수`로 해석한다.
- `notice_journey_duration` p95가 높아지면 사용자 여정 전체가 느려진 것이다.
- `api` 태그별 `http_req_duration` p95/p99로 로그인, 목록, 상세, 읽음 처리, 읽기 현황 중 병목 endpoint를 분리한다.
- Spring Boot 메트릭에서는 `http_server_requests_seconds`, HikariCP active/pending/timeout, JVM heap/GC pause를 함께 본다.

상세 지표 요약은 [metrics.md](./metrics.md)를 참고한다.
