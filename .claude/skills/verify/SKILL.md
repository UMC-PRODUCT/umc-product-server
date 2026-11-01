---
name: verify
description: UMC 서버를 일회용 PostGIS와 local 프로필로 실제로 띄워, 테스트로는 안 보이는 변경을 확인한다. 설정·환경변수·의존성, Security 필터·JWT·CORS, Flyway 마이그레이션을 바꿨을 때 쓴다. 도메인 규칙·유스케이스·쿼리 변경은 테스트로 확인하고 이 스킬을 쓰지 않는다.
---

# UMC 서버 실행 확인

이 스킬은 "띄워 봐야만 아는 것"만 확인한다. 비즈니스 규칙이 맞는지는 도메인 테스트와 사람이 판단한다.

## 1. 먼저 띄울 일인지 판단한다

| 이번 변경 | 할 일 |
|---|---|
| `application.yml`, 프로필, 환경변수, `@ConfigurationProperties`, 기동 시 빈 조건, 의존성 버전 | [startup](scenarios/startup.md) |
| `SecurityConfig`, JWT, 인증 필터, CORS, rate limit | [security](scenarios/security.md) |
| `monolith/src/main/resources/db/migration` 추가·수정 | [flyway](scenarios/flyway.md) |
| 도메인 규칙, 유스케이스, 조회 쿼리, DTO 변환 | 띄우지 않는다. 해당 테스트를 돌린다 |

여러 행에 걸치면 해당 시나리오를 모두 한 인스턴스에서 실행한다.

## 2. 띄운다

```bash
.claude/skills/verify/scripts/verify.sh up
```

- `:app:bootJar`(테스트 제외)를 빌드하고, 테스트와 같은 PostGIS 이미지를 빈 포트에 띄운다. arm64는 `docker/test/postgis/Dockerfile`로 만든 `umc-product-postgis-test:18.2-postgis`, 그 외는 `postgis/postgis:18-3.6`이다.
- 기본값 없는 필수 환경변수를 매 실행마다 새 더미 값으로 채운다. 비밀값은 `.verify/run-*/app.env`에만 있고 `down`에서 지운다.
- 준비 완료는 두 조건을 모두 만족할 때다. `/actuator/health`가 `UP`이고, `flyway_schema_history`의 성공한 버전 수가 `V*__*.sql` 파일 수와 같다. 프로세스가 응답하는 것만으로는 준비 완료가 아니다.
- 기본 대기 시간은 300초다. `VERIFY_READY_TIMEOUT`으로 바꾼다.

## 3. 쓸 만한지 점검한다

무언가 이상해 보이면 먼저 실행한다. 읽기만 한다.

```bash
.claude/skills/verify/scripts/verify.sh doctor
```

프로세스가 살아 있는지, API 포트를 이번에 띄운 프로세스가 쓰는지, health가 `UP`인지, 프로필이 `local`인지, DB 컨테이너가 떠 있는지 본다. 하나라도 실패하면 `down` 후 다시 `up`한다. 다른 사람이 띄운 서버에는 요청을 보내지 않는다.

## 4. 시나리오를 실행한다

요청은 `call`로 보낸다. 요청과 응답 전체가 증거 폴더에 남는다. 인자 안의 `{api}`는 이번 인스턴스 주소로 바뀐다.

```bash
.claude/skills/verify/scripts/verify.sh call <이름> <curl 인자...>
```

- [startup](scenarios/startup.md): 기동 자체와 설정값 반영
- [security](scenarios/security.md): 공개·보호 경로, 토큰, CORS
- [flyway](scenarios/flyway.md): 빈 DB 적용과 재기동 시 재적용 없음

## 5. 증거

`.verify/run-<시각>/evidence/`에 남는다. 커밋하지 않는다.

- `health.json`, `flyway.txt`: `up`이 남긴다.
- `<이름>.txt`: `call`로 보낸 요청과 응답 전체다.
- `app.log`: `down`이 복사한다. `restart-app`을 썼다면 재기동 전 로그가 `app-before-restart.log`로 함께 남는다.
- 로그의 일부 값(프로필 이름, SQL 주석 등)은 로그 마스킹 때문에 `[REDACTED]`로 보인다. 확인할 값은 응답이나 DB 조회로 남긴다.

증명 기준은 다음과 같다.

- 바꾼 경로를 실제 HTTP로 호출한다. 내부 메서드를 직접 부르지 않는다.
- 요청과 결과 상태를 함께 남긴다. 상태 코드만 적지 않는다.
- DB가 바뀌는 변경은 응답과 함께 DB 상태도 확인한다. `docker exec <컨테이너> psql -U verify -d umc_product -c '...'`를 쓰고, 결과를 증거 폴더에 저장한다.

## 6. 정리한다

```bash
.claude/skills/verify/scripts/verify.sh down
```

이번에 띄운 앱 프로세스와 DB 컨테이너만 지운다. 프로세스 이름으로 죽이지 않는다. 증거 폴더는 남는다. 실패한 시도 뒤에도 매번 실행한다.

## 이 스킬이 확인하지 않는 것

- 도메인 규칙: 규칙 테스트와 PR 리뷰에서 확인한다.
- 카카오·구글·애플 OAuth, S3: 주소가 코드나 클라이언트에 고정돼 있어 로컬에서 대체하지 않는다. preview 환경에서 확인한다.
- 메일, OpenAI, Slack·Discord 웹훅의 실제 발송: 필요하면 해당 작업에서 `SMTP_HOST`, `OPENAI_BASE_URL`, 웹훅 URL을 가짜 서버로 돌리는 시나리오를 추가한다.
- 아웃박스 릴레이, 스케줄러, WebSocket: 해당 영역을 바꾸는 작업에서 시나리오를 추가한다.
