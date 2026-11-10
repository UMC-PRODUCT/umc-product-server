# 부하 테스트 진단 가이드 — 메트릭 읽는 법

대시보드("부하 테스트 (k6)") 앞에서 "지금 뭐가 병목인가"를 판정하는 절차.
핵심 프레임: **① 행(k6)이 증상, ②~⑤ 행이 용의자.** 진단은 단일 지표가 아니라 조합 패턴 매칭이다.

## 1. 증상 조합 매트릭스

| 증상 조합 | 병목 | 다음 확인 |
|----------|------|----------|
| TPS 평탄 + 앱 CPU ≈100% | **CPU 바운드** — 연산·JSON 직렬화·GC | ② GC pause 같이 증가하면 힙 문제, 아니면 순수 연산 비용 |
| TPS 평탄 + **앱 CPU 낮음**(<50%) + p95 급등 | **대기 병목** — CPU 가 아닌 무언가에 줄 서 있음. 가장 흔한 케이스 | ② Hikari pending → ③ DB → 외부 API 순서로 추적 |
| Hikari pending>0 + DB CPU 높음 | DB 가 못 받아줌 — 쿼리/인덱스 문제 | ③ 락·④ `Disk — S avg queue` 확인. 쿼리 튜닝 대상 |
| Hikari pending>0 + DB CPU 낮음 | 풀 크기 부족 또는 **트랜잭션이 커넥션을 오래 점유** | ③ 커넥션 상태별 — `idle in transaction` 누적이면 트랜잭션 범위 문제 |
| Tomcat busy=max + Hikari 여유 | 스레드풀 병목 — DB 아닌 느린 것(외부 API 등)에 스레드가 묶임 | 트레이싱으로 느린 구간 특정 |
| p95 만 튀고 avg 정상 | **tail latency** — GC stop-the-world, 락 경합, 특정 느린 쿼리 | ② GC pause 스파이크와 시각 일치 확인 |
| VUs 급증 (arrival-rate 실행 중) | SUT 지연으로 동시성이 축적되는 중 — 그 자체가 포화 신호 | 포화점을 이미 지났다는 뜻 |
| ④ `CPU — U 사용률` ≈100% | **측정 무효 가능** — 호스트가 포화. 이 패널은 호스트 전체라 운영 앱·DB 부하가 섞이고, k6 를 같은 서버에서 돌리면 그 몫도 섞인다. 생성기만 따로 보는 패널은 없다 | rate 를 낮추거나 k6 를 다른 머신에서 돌린다. 시험 대상 몫은 ② `앱 컨테이너 CPU (limit 대비)`·⑤ `DB 컨테이너 CPU (limit 대비)`로 따로 본다 |
| ④ `Disk — S avg queue` 1 이상 지속 + `Disk — U busy` 상승 | 디스크 한계 — IO 대기 큐 형성. DB 데이터도 이 서버 디스크에 있다 | ③ 임시파일 쓰기와 시각 비교. 스토리지 설정 |
| ③ 캐시 적중률 하락 + temp bytes 증가 | 워킹셋 > shared_buffers, 정렬/해시가 work_mem 초과 | 쿼리·메모리 파라미터 |
| 에러율 급등 + k6 dropped iterations | k6 VU 풀(maxVUs) 고갈 — 생성기 설정 문제이지 SUT 문제 아님 | profiles.js vuPool 상향 |
| 요청이 **시작조차 못 함**(connection reset/timeout) | **accept 큐 넘침** 의심 — Tomcat 이 accept 를 못 따라가 연결이 큐에서 버려짐. 커넥션 pending·run queue 에 이은 세 번째 대기열 | ④ `Network — S accept 큐 오버플로`는 호스트 network namespace 만 본다. Pod 안 Tomcat·Traefik 의 넘침은 여기에 잡히지 않으므로 0 이어도 무죄가 아니다. ② Tomcat busy=max 동반이면 스레드풀이 원인, 아니면 `server.tomcat.accept-count`(기본 100)와 Pod 의 `net.core.somaxconn` |
| ④ PSI memory some 상승 (+ `Memory — E OOM kill` > 0) | **메모리 부족** — PSI 상승은 할당하던 스레드가 회수를 기다리며 멈춘 시간이고, 그 지연이 응답시간에 그대로 얹힌다. OOM kill 이 0 이 아니면 커널이 이미 프로세스를 죽였다 | Memory U 구성에서 페이지 캐시가 얇아지는 시각과 맞춰 본다. majflt 동반이면 코드 페이지까지 evict 된 상태. OOM kill 이 있으면 같은 시각 Argo CD 에서 OOMKilled Pod 를 찾는다 |
| pswpout > 0 | 스왑 아웃 — 힙이 디스크로 나가는 중. GC 가 디스크를 훑기 시작하면 회복 불가 | 즉시 중단. 스왑을 껐는데도 0 이 아니면 설정 오류 |
| ④ run queue 높음 + **CPU 사용률은 낮음** + steal 상승 | **하이퍼바이저가 코어를 회수 중** | CPU 크레딧 패널은 없다. steal 이 오른 구간의 측정은 무효로 본다 |
| ④ `Traefik 메모리 (limit 대비)`가 limit(512Mi) 근처 | 모든 요청이 지나는 Traefik 이 OOMKilled 직전 — 죽으면 전체 요청이 끊긴다 | ① 에러율 급등과 시각 비교. `Memory — E OOM kill` 확인 |
| ② JVM blocked 스레드 급증 | 락 또는 커넥션 대기 — run queue 의 JVM 판 | Hikari pending 과 시각 비교. runnable 이 코어 수보다 훨씬 높으면 CPU 경쟁 |

- **앱 CPU**는 ② `앱 컨테이너 CPU (limit 대비)`로 본다. ② `앱 CPU 사용률 (JVM이 보는 process vs host)`의 `process_cpu_usage`는 JVM 이 보는 CPU 2개 기준이고 limit 은 1.5코어라, limit 에 붙어도 약 75% 로 보인다.
- **DB CPU**는 ⑤ `DB 컨테이너 CPU (limit 대비)`로 본다.
- ③ DB 내부 행은 dev·preview 에 postgres-exporter 가 생기기 전까지 비어 있다. 그동안 ③ 을 보라는 줄(커넥션 상태별·락·캐시 적중률·임시파일)은 판정할 수 없다.

읽는 순서: 헤더 스탯으로 이상 감지 → ① 행에서 증상 확정 → 공유 크로스헤어로 같은 시각의 ②~⑤ 를 위에서 아래로.
④ 행은 **PSI 패널이 관문**이다 — memory/io/cpu 가 모두 0 이면 그 자원은 무죄이므로 아래 패널을 볼 필요 없다.

## 2. 커넥션 풀은 어떤 자원을 먹나

요청 흐름과 연쇄 구조:

```
요청 → Tomcat 스레드 점유 → DB 필요 시 Hikari 커넥션 대여
                              └ 커넥션 없으면 pending 대기 — 그동안 Tomcat 스레드도 같이 묶임
```

- Hikari 고갈은 Tomcat 스레드 고갈로 **연쇄**된다. pending 이 0 이 아니면 이미 병목.
- 비용은 DB 쪽이 크다: PostgreSQL 은 **커넥션당 OS 프로세스 1개** + 커넥션당 수 MB 메모리(work_mem 등). t4g.small(2GB) 기준 커넥션 수십 개면 메모리 압박.
- **풀을 키운다고 빨라지지 않는다.** DB CPU 가 한계면 커넥션 추가는 컨텍스트 스위칭만 늘린다. Hikari 권장 공식: `코어 수 × 2 + 디스크 수` — 작게 유지가 정석.
- 실효 동시성 = min(Tomcat max threads, Hikari pool, DB max_connections). **셋 중 최솟값이 시스템의 동시 처리 한도.**

## 3. k6 시나리오 설계 규칙

- **엔드포인트 단위가 아니라 사용자 행동 단위.** "홈 화면 로드" = 3콜 병렬 batch (`scenarios/*/home.js`). API 를 하나씩 따로 두드리는 건 벤치마크지 시나리오가 아니다.
- arrival-rate 모델에서는 think time(sleep) 불필요 — 부하량은 rate 가 결정한다. (VU 모델에서만 의미 있음)
- **요청마다 `scenario` 태그 필수** — 어느 콜이 느린지 분해되지 않으면 진단 불가.
- 대상 ID 는 seed 에서 **랜덤 픽** — 같은 ID 만 반복하면 캐시가 결과를 낙관 왜곡.
- thresholds = SLO 를 코드에 명시 (`config/profiles.js`).
- 쓰기 시나리오는 매 실행 DB 상태를 바꾼다 — 리셋 전략(destroy/apply 또는 snapshot)과 같이 설계.

## 4. 단일 시나리오 vs 혼합 (운영과의 간극)

2단계로 접근한다:

1. **단일 시나리오 breakpoint = 성분 분석.** 각 시나리오의 한계·병목 특성을 개별로 파악한다. 혼합에서 문제가 터지면 원인 분리가 안 되므로 반드시 이게 먼저.
2. **혼합(mix) = 통합 리허설.** k6 `options.scenarios` 에 여러 시나리오를 rate 비율로 동시 등록 (예: home 70% + project-read 25% + 쓰기 5%). 비율은 운영 접근로그/APM 에서 추출하고, 추정이면 기록에 명시한다.

혼합에서만 보이는 것: 쓰기 락이 읽기 p95 를 밀어올림 · 무거운 집계가 커넥션 풀을 점유해 가벼운 API 도 함께 느려짐 · DB 버퍼 캐시 경합.

혼합해도 운영과 여전히 다른 것(한계로 인정하고 기록): 캐시 워밍 상태, 데이터 신선도, 백그라운드 잡(스케줄러), 외부 API 지연. 시간 누적 문제(메모리·커넥션 누수)는 혼합이 아니라 **soak**(장시간 고정 부하)가 잡는다.
