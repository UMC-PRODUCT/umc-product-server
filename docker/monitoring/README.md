# 자체 호스팅 모니터링 스택 (ADR-014 Phase 1)

[ADR-014](../../docs/adr/014-self-hosted-monitoring-stack-migration.md) 의 Phase 1 docker-compose 스택. 앱이 이미 사용 중인 **OTLP push 송신을 그대로 두고 URL 만 본 스택의 endpoint 로 바꾸면** metrics / traces / logs 가 자체 호스팅 백엔드로 흘러 들어간다.

```
앱 ──OTLP──▶ otel-collector ─┬─▶ prometheus  (metrics)
                              ├─▶ tempo       (traces)
                              └─▶ loki        (logs, OTLP receiver)

앱 ──Loki push API──▶ loki    (기존 loki4j 어펜더, URL 만 바꿔 사용 가능)
```

## 빠른 시작

```bash
cd docker/monitoring
cp env.example .env
docker compose up -d
```

기본 접속:

- Grafana: <http://localhost:3000> (`.env` 의 `GRAFANA_ADMIN_USER` / `GRAFANA_ADMIN_PASSWORD`)
- Prometheus: <http://localhost:9090>
- Loki: <http://localhost:3100>
- Tempo: <http://localhost:3200>
- OTel Collector OTLP: gRPC `:4317`, HTTP `:4318`

## 앱 환경변수 매핑

운영 / 로컬 앱의 환경변수만 본 스택의 endpoint 로 바꾸면 OTLP 송신이 그대로 자체 호스팅 백엔드로 전달된다.

| 앱 환경변수 | Grafana Cloud (기존) | Self-hosted (본 스택) |
|---|---|---|
| `PROM_URL` | `https://otlp-gateway-*.grafana.net/.../v1/metrics` | `http://<host>:4318/v1/metrics` |
| `TEMPO_URL` | `https://otlp-gateway-*.grafana.net/...` | `http://<host>:4318` |
| `LOKI_URL` | `https://logs-*.grafana.net/loki/api/v1/push` | `http://<host>:3100/loki/api/v1/push` |
| `PROM_AUTH` / `TEMPO_AUTH` | `Basic ...` | Phase 1 미사용 (Phase 2 에서 Basic Auth + TLS 추가) |
| `LOKI_USERNAME` / `GRAFANA_API_KEY` | Cloud credential | Phase 1 미사용 |

- `<host>` 는 운영 서버에서 본 스택에 도달 가능한 hostname. Phase 1 로컬 검증 단계에서는 `localhost`, 운영 이관 단계에서는 Cloudflare Tunnel 의 public hostname (예: `otel.umc.it.kr`) 으로 교체.
- **Metrics / Traces** 는 OTel Collector (`4318/v1/metrics` / `4318` ) 로 직접 송신. Collector 가 내부에서 prometheus / tempo 로 분배한다.
- **Logs** 는 두 가지 경로:
    1. 기존 `loki4j` 어펜더 그대로 — `LOKI_URL` 만 `http://<host>:3100/loki/api/v1/push` 로 변경.
    2. OTLP 로 보내려면 OTel Collector 의 `4318/v1/logs` 로 보내면 Loki 의 OTLP receiver (`/otlp/v1/logs`) 가 받는다.

## 디렉터리 구조

```
docker/monitoring/
├── env.example               # 호스트 포트 / Grafana admin / Discord webhook (`.env.*` 가 gitignore 에 걸려 점 없는 이름 사용)
├── README.md                 # 본 파일
├── docker-compose.yml        # 스택 정의 (Commit 2 이후)
└── config/
    ├── prometheus/           # metric backend config
    ├── tempo/                # trace backend config
    ├── loki/                 # log backend config
    ├── otel-collector/       # OTLP 게이트웨이 config
    ├── alertmanager/         # 알람 라우팅 / Discord webhook
    └── grafana/
        ├── dashboards/       # provisioned dashboards (JSON)
        └── provisioning/
            ├── datasources/  # prometheus / tempo / loki datasource
            └── dashboards/   # dashboard provider 정의
```

## 컴포넌트 책임

| 컴포넌트 | 역할 |
|---|---|
| `otel-collector` | 앱의 OTLP 송신을 받아 prometheus / tempo / loki 로 분배. 향후 Cloud + Self dual-write 가 필요해지면 exporter 추가만 하면 된다. |
| `prometheus` | OTLP write receiver (`/api/v1/otlp/v1/metrics`) 활성. 자체 scrape 는 prometheus / node-exporter 만. |
| `tempo` | OTLP 4317/4318 native 수신. 로컬 디스크 backend, 기본 7일 보존. |
| `loki` | Loki push API + OTLP logs receiver 양쪽 지원. 기본 30일 보존. |
| `grafana` | 3종 datasource 와 ADR-016 의 api-performance dashboard 자동 provisioning. |
| `alertmanager` | Prometheus alerting rule 의 라우팅. Discord webhook 으로 송신. |
| `node-exporter` | 호스트 메트릭 (CPU / disk / network) 노출, prometheus 가 scrape. |

## 운영 주의사항

- 본 스택은 **Phase 1 로컬 검증용** 이다. Phase 2 에서는 Basic Auth / TLS / Cloudflare Tunnel / object storage backend 등이 추가된다 (ADR-014 §Phase 2~3).
- Grafana 의 `GRAFANA_ADMIN_PASSWORD` 는 운영 전 반드시 강한 값으로 교체.
- Loki / Tempo / Prometheus 의 retention 은 docker volume 디스크 크기에 직결된다. 운영 노드의 디스크 모니터링 (`node-exporter` 의 `node_filesystem_avail_bytes`) 필수.

## 참고

- [ADR-014: 모니터링 스택을 Grafana Cloud 에서 자체 홈서버로 이관한다](../../docs/adr/014-self-hosted-monitoring-stack-migration.md)
- [ADR-016: API 로그를 MDC 기반 JSON 구조화 로그로 전환한다](../../docs/adr/016-structured-json-logging-with-mdc.md) — Loki 의 `| json` 쿼리 / dashboard 의 LogQL 룰
