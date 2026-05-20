# 자체 호스팅 모니터링 스택 (ADR-014 Phase 1)

[ADR-014](../../docs/adr/014-self-hosted-monitoring-stack-migration.md) 의 Phase 1 docker-compose 스택. 앱이 이미 사용 중인 **OTLP push 송신을 그대로 두고 URL 만 본 스택의 endpoint 로 바꾸면** metrics / traces / logs 가 자체 호스팅 백엔드로 흘러 들어간다.

```
앱 ──OTLP──▶ otel-collector ─┬─▶ prometheus  (metrics)
                              ├─▶ tempo       (traces)
                              └─▶ loki        (logs, OTLP receiver)
```

## 빠른 시작

```bash
cd docker/monitoring
cp env.example .env
OTEL_INGEST_TOKEN=$(openssl rand -hex 32)
sed -i.bak "s/^OTEL_INGEST_TOKEN=.*/OTEL_INGEST_TOKEN=$OTEL_INGEST_TOKEN/" .env
docker compose up -d
```

Linux 호스트에서 `node-exporter`까지 함께 띄울 때:

```bash
docker compose --profile linux up -d
```

기본 접속:

- Grafana: <http://localhost:13000> (`.env` 의 `GRAFANA_ADMIN_USER` / `GRAFANA_ADMIN_PASSWORD`)
- Prometheus: <http://localhost:19090>
- Loki: <http://localhost:13100>
- Tempo: <http://localhost:13200>
- OTel Collector OTLP: gRPC `:4317`, HTTP `:4318`

## 앱 환경변수 매핑

운영 / 로컬 앱의 환경변수만 본 스택의 endpoint 로 바꾸면 OTLP 송신이 그대로 자체 호스팅 백엔드로 전달된다.

| 앱 환경변수 | Grafana Cloud (기존) | Self-hosted (본 스택) |
|---|---|---|
| `OTEL_URL` | `https://otlp-gateway-*.grafana.net/otlp` | `http://<host>:4318` |
| `PROM_URL` | `https://otlp-gateway-*.grafana.net/.../v1/metrics` | 미설정 시 `${OTEL_URL}/v1/metrics` 사용 |
| `TEMPO_URL` | `https://otlp-gateway-*.grafana.net/.../v1/traces` | 미설정 시 `${OTEL_URL}/v1/traces` 사용 |
| `OTEL_LOGS_URL` | 별도 구성 없음 | 미설정 시 `${OTEL_URL}/v1/logs` 사용 |
| `OTEL_AUTH_HEADER` | `Basic ...` 또는 `Bearer ...` | `Bearer <OTEL_INGEST_TOKEN>` |
| `PROM_AUTH_HEADER` / `TEMPO_AUTH_HEADER` / `OTEL_LOGS_AUTH_HEADER` | 신호별 override | 미설정 시 `OTEL_AUTH_HEADER` 사용 |

- `<host>` 는 운영 서버에서 본 스택에 도달 가능한 hostname. Phase 1 로컬 검증 단계에서는 `localhost`, 운영 이관 단계에서는 Cloudflare Tunnel 의 public hostname (예: `otel.umc.it.kr`) 으로 교체.
- 앱은 기본적으로 `OTEL_URL` 과 `OTEL_AUTH_HEADER` 만 설정하면 metrics / traces / logs 를 모두 Collector 로 보낸다.
- 개별 신호별 endpoint 를 분리해야 할 때만 `PROM_URL`, `TEMPO_URL`, `OTEL_LOGS_URL` 을 override 한다.
- compose 의 host port 는 기본적으로 `127.0.0.1` 에만 bind 된다. 외부 앱 서버에서 보내야 하면 Cloudflare Tunnel / Tailscale / WireGuard / reverse proxy 등으로 TLS 와 접근 제어를 먼저 둔다.

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
| `otel-collector` | 앱의 OTLP metrics / traces / logs 송신을 받아 prometheus / tempo / loki 로 분배. 향후 Cloud + Self dual-write 가 필요해지면 exporter 추가만 하면 된다. |
| `prometheus` | OTLP write receiver (`/api/v1/otlp/v1/metrics`) 활성. 자체 scrape 는 prometheus / node-exporter 만. |
| `tempo` | OTLP 4317/4318 native 수신. 로컬 디스크 backend, 기본 7일 보존. |
| `loki` | OTLP logs receiver 를 통해 Collector 에서 들어온 로그 저장. 기본 30일 보존. |
| `grafana` | 3종 datasource 와 ADR-016 의 api-performance dashboard 자동 provisioning. |
| `alertmanager` | Prometheus alerting rule 의 라우팅. Discord webhook 으로 송신. |
| `node-exporter` | Linux 호스트 메트릭 (CPU / disk / network) 노출, `linux` profile 에서만 실행. prometheus 가 scrape. |

## 운영 주의사항

- 본 스택은 **Phase 1 로컬 검증용** 이다. Phase 2 에서는 TLS / Cloudflare Tunnel / object storage backend 등이 추가된다 (ADR-014 §Phase 2~3).
- Grafana 의 `GRAFANA_ADMIN_PASSWORD` 는 운영 전 반드시 강한 값으로 교체.
- Loki / Tempo / Prometheus 의 retention 은 docker volume 디스크 크기에 직결된다. 운영 노드의 디스크 모니터링 (`node-exporter` 의 `node_filesystem_avail_bytes`) 필수.

## 서비스 식별자 통일 (OTel service.name)

모든 signal (metrics / traces / logs) 은 **`service.name`을 단일 식별자**로 사용한다 (Phase 2 정책).

| 설정 | 값 |
|---|---|
| `management.opentelemetry.resource-attributes.service.name` | `${spring.application.name}` (기본 `x-umc-product`) |
| `management.opentelemetry.resource-attributes.loki.resource.labels` | `service.name` (Loki 에서 라벨로 승격) |
| `management.metrics.tags` | `instance` 만 (instance 는 유지) |

이렇게 하면:
- Prometheus 메트릭도 `service_name` 라벨 보유.
- Loki 로그도 `service_name` 라벨 보유 (structured metadata 가 아니라 라벨로 승격).
- Grafana 대시보드가 Prometheus / Loki / Tempo 모두 `service_name` 기준으로 필터링 가능.

## 참고

- [ADR-014: 모니터링 스택을 Grafana Cloud 에서 자체 홈서버로 이관한다](../../docs/adr/014-self-hosted-monitoring-stack-migration.md)
- [ADR-016: API 로그를 MDC 기반 JSON 구조화 로그로 전환한다](../../docs/adr/016-structured-json-logging-with-mdc.md) — OTLP log attributes / dashboard 의 LogQL 룰
