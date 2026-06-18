# Logging Onboarding

이 문서는 `feature/logging-hardening` 기준으로 운영 코드의 로그 사용 현황과 추가 개선 계획을 정리한다.

## 읽는 순서

1. [domains.md](./domains.md): 도메인별 현재 로그 인벤토리
2. 아래 정책: 로그로 남길 정보와 audit/metric으로 분리할 정보의 기준
3. 아래 개선 계획: 이후 작업을 나눌 수 있는 단위

## 현재 기준

- 일반 애플리케이션 로그는 장애 원인 분석, 배치/스케줄러 실행 상태, 외부 연동 실패 원인, 운영 흐름 추적에 사용한다.
- 재식별 가능한 OAuth provider id, subject, email, response body, 수신자 주소 원문은 로그에 남기지 않는다.
- 외부 API 호출 결과와 지연 시간은 `ExternalApiCallLogger`의 `external_api_called` 구조화 이벤트로 남긴다.
- 생성/수정/삭제처럼 추적 책임이 필요한 사용자/관리자 작업은 일반 로그만으로 끝내지 않고 audit log 대상인지 검토한다.
- 반복적으로 집계해야 하는 성공률, 실패율, 지연 시간, 처리량은 로그보다 metric 후보로 본다.
- 운영 metric은 `OperationalMetrics`를 통해 남긴다. 허용 tag는 `domain`, `operation`, `result`, `provider`, `jobName` 계열로 제한하고, URL/email/긴 식별자처럼 cardinality가 높은 값은 `other`로 축약한다.

## 적용 현황

### 1. 과도한 INFO와 민감 가능 로그 정리

- `AuthorizationService`의 요청별 권한 평가 시작/subject 상세 로그는 DEBUG로 낮췄고, 접근 거부만 WARN과 security metric으로 남긴다.
- JWT 검증 실패 로그는 반복 발생 가능성을 고려해 DEBUG로 낮췄다.
- Firebase 초기화 로그에서 service account email, private key algorithm/format, credential 길이/접두어를 제거했다.
- FCM/webhook 로그에서 title/content/to 원문을 제거하고 대상 수, token 수, platform 수, content length 중심으로 바꿨다.
- S3/CloudFront 로그에서 signed URL, signature, final URL 원문이 남지 않도록 정리했다.

### 2. Audit action과 command audit 보강

- `AuditAction`에 `LOGIN`, `LINK`, `UNLINK`, `ACCESS_DENIED`, `PUBLISH`, `CANCEL`, `REMIND`, `REORDER`, `FINALIZE`를 추가했다.
- 인증/인가: 이메일 로그인 성공, OAuth 로그인 성공/회원가입 필요 흐름, OAuth 연결/해제, ChallengerRole 생성/수정/삭제를 audit 대상으로 선언했다.
- 회원/약관/조직/챌린저: 이메일 변경, 비밀번호 변경, 프로필 수정/삭제, 약관 생성/동의, 기수/조직 멤버/스쿼드/챌린저 기록 변경을 audit 대상으로 선언했다.
- 커리큘럼/일정/커뮤니티/블로그/공지/설문/피드백/프로젝트/스토리지의 주요 생성, 수정, 삭제, 제출, 배포, 리마인드, 재정렬, 확정 작업을 audit 대상으로 선언했다.
- audit target/description에는 email, providerId, OAuth subject, command 객체 전체, 사용자 입력 본문을 넣지 않는다.

### 3. Metric 보강

- `OperationalMetrics`를 추가해 외부 호출, batch job, notification, security event metric을 표준화했다.
- `ExternalApiCallLogger`는 기존 structured log schema를 유지하면서 provider/operation/result/latency metric을 함께 기록한다.
- email verification retention, workbook auto release, figma dispatch retention, matching round deadline은 `jobName`, `processed`, `durationMs`, `result` 기반 batch metric을 기록한다.
- FCM audience 발송과 webhook 발송은 provider/operation/result/count 기반 notification metric을 기록한다.
- S3 upload URL 생성, object metadata 조회, object delete, CloudFront signed URL 생성은 storage external metric을 기록한다.
- 로그인 실패, 비밀번호 재설정 실패, 접근 거부는 audit log가 아니라 security event metric으로 집계한다.

### 4. 정책 테스트

- 민감 필드 로그 금지 테스트: `email={}`, `body={}`, `providerId={}`, `sub={}`, `signedUrl={}`, `signature={}`, notification title/content/to, storage URL 원문을 검출한다.
- command 객체 전체 로그 금지 테스트: `command={}`, `commands={}` 직접 로깅을 검출한다.
- audit coverage 테스트: 주요 CommandService 상태 변경 메서드가 `@Audited`인지 검증한다.
- metric recorder 테스트: 허용 tag schema와 high-cardinality 값 축약을 검증한다.

## 남은 개선 계획

### 1. 인증과 인가 audit 보강

- 실패 로그인/접근 거부는 현재 security metric으로 집계한다. 실패까지 audit log에 저장하려면 `@Audited`와 별도로 실패 이벤트 발행 API가 필요하다.
- 사용자 존재 여부를 노출하지 않는 enumeration 방어 흐름은 audit description에도 reason 원문을 넣지 않는다.

### 2. 외부 연동 metric 분리

- OAuth provider, Figma, Discord, Slack, Telegram, SES는 `ExternalApiCallLogger`를 통해 metric까지 기록한다.
- LLM은 기존 `LlmMetrics`를 유지한다. 필요하면 provider별 latency/token/failure를 `OperationalMetrics`와 같은 naming policy로 맞춘다.

### 3. 스토리지 audit와 metric 보강

- 파일 upload URL 발급, upload confirm, delete는 audit 대상으로 선언했다.
- S3/CloudFront 호출 metric은 어댑터에서 기록한다.

### 4. 알림 발송 관측성 개선

- FCM과 webhook 성공/실패 건수 metric은 추가했다.
- invalid token 비활성화와 SES template rendering 실패는 후속으로 별도 counter를 둘 수 있다.

### 5. 배치와 스케줄러 표준화

- 주요 scheduler/handler는 `jobName`, `processed`, `durationMs`, `result`, `errorClass` 기반 로그와 metric을 사용한다.
- scheduler가 더 늘어나면 공통 helper로 시작/종료/실패 로그와 metric 호출을 묶는다.

### 6. 과도한 INFO 정리

- 이번 변경에서 주요 후보는 정리했다.
- 남은 4xx/business exception WARN 과다는 request summary와 통합하거나 sampling을 검토한다.

### 7. 테스트/시드 로그 격리

- `test` 도메인의 seed 로그는 운영 관측성과 성격이 다르다.
- 장기적으로 별도 profile/logger name 또는 admin seed 전용 structured event로 분리한다.
