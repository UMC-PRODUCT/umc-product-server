# k6 지표 요약

## 내장 지표

| 지표 | 타입 | 의미 | Grafana에서 보는 방법 |
|---|---|---|---|
| `http_reqs` | Counter | 발생한 HTTP 요청 수 | 초당 요청 수(RPS), API별 요청량 |
| `http_req_duration` | Trend | 요청 전체 시간. sending + waiting + receiving 포함 | p50/p95/p99 latency |
| `http_req_failed` | Rate | k6 기준 실패한 HTTP 요청 비율 | 전체/endpoint별 실패율 |
| `http_req_blocked` | Trend | TCP 연결 슬롯 대기, DNS 등 요청 전 대기 시간 | 부하 생성기/네트워크 병목 의심 |
| `http_req_connecting` | Trend | TCP 연결 시간 | 연결 재사용/네트워크 문제 확인 |
| `http_req_tls_handshaking` | Trend | TLS handshake 시간 | HTTPS 부하 시 인증서/네트워크 비용 확인 |
| `http_req_sending` | Trend | 요청 바디 전송 시간 | 큰 payload 또는 네트워크 병목 확인 |
| `http_req_waiting` | Trend | 서버 첫 byte 대기 시간(TTFB) | 서버 처리 시간에 가장 가까운 HTTP 지표 |
| `http_req_receiving` | Trend | 응답 바디 수신 시간 | 큰 응답/네트워크 병목 확인 |
| `checks` | Rate | `check()` 성공률 | 기능 검증 실패율 |
| `iterations` | Counter | default/exec 함수 완료 횟수 | 사용자 flow 처리량 |
| `iteration_duration` | Trend | iteration 1회 총 수행 시간 | flow 전체 latency |
| `vus` | Gauge | 현재 활성 VU 수 | 목표 동시 사용자 도달 여부 |
| `vus_max` | Gauge | 사용 가능한 최대 VU 수 | VU capacity 확인 |
| `data_received` | Counter | 받은 데이터 크기 | 응답 payload 비용 |
| `data_sent` | Counter | 보낸 데이터 크기 | 요청 payload 비용 |
| `dropped_iterations` | Counter | arrival-rate executor에서 시작하지 못한 iteration 수 | k6 VU 부족 또는 SUT 지연 악화 |

## 스크립트 커스텀 지표

| 지표 | 타입 | 의미 | 기본 threshold |
|---|---|---|---|
| `notice_journey_duration` | Trend | 로그인 이후 공지 flow 1회 전체 시간 | `p(95)<5000` |
| `notice_login_success` | Rate | 이메일 로그인 성공률 | `rate>0.99` |
| `notice_list_success` | Rate | 공지 목록/복귀 목록 성공률 | `rate>0.99` |
| `notice_detail_success` | Rate | 공지 상세 조회 성공률 | `rate>0.99` |
| `notice_record_read_success` | Rate | 읽음 처리 성공률 | `rate>0.99` |
| `notice_read_status_success` | Rate | 읽기 현황 조회 성공률 | `rate>0.99` |
| `notice_list_empty_total` | Counter | 공지 목록이 비어 상세 조회를 못 한 횟수 | 별도 threshold 없음 |

## 자주 설정하는 옵션

| 설정 | 예시 | 용도 |
|---|---|---|
| `scenarios` | `ramping-vus`, `per-vu-iterations`, `constant-arrival-rate` | 동시 사용자 수 또는 처리량 모델 선택 |
| `thresholds` | `http_req_failed: ["rate<0.01"]` | 성능/SLO 실패 기준 선언 |
| `summaryTrendStats` | `["p(90)", "p(95)", "p(99)"]` | CLI summary에 표시할 percentile 선택 |
| `tags` | `api=notice_list`, `flow=notice_login_1000_vus` | Grafana 필터링과 threshold 범위 지정 |
| `userAgent` | `umc-product-k6-notice-login/1.0` | 서버 로그에서 k6 트래픽 식별 |
| `noConnectionReuse` | `true` | connection reuse를 끄고 최악 조건 확인 |
| `discardResponseBodies` | `true` | body가 필요 없는 테스트에서 k6 메모리 사용량 감소 |

## VU와 RPS 해석

| 개념 | 의미 | 이 스크립트 기준 |
|---|---|---|
| VU | 동시에 행동 중인 가상 사용자 수 | `TARGET_VUS=1000`이면 최대 1000명이 flow 반복 |
| iteration/s | 사용자 flow 시작/완료 처리량 | `noticeJourney()` 완료 횟수 |
| HTTP RPS | 실제 초당 HTTP 요청 수 | `iteration/s * flow당 요청 수`에 가까움 |
| think time | 사용자가 화면을 읽거나 다음 행동까지 기다리는 시간 | `THINK_MIN_MS`, `THINK_MAX_MS` |

따라서 `TARGET_VUS=1000`은 `1000 RPS`가 아니다. 평균 flow 시간이 10초이고 flow당 요청이 6개라면, 이론상 대략 `1000 / 10 * 6 = 600 HTTP RPS` 수준이 된다.
