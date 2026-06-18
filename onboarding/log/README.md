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

## 추가 로깅 계획

### 1. 인증과 인가 audit 보강

- 대상: 로그인 성공/실패, OAuth 계정 연결/해제, 비밀번호 재설정 요청, 접근 거부.
- 방식: 개인 식별 원문은 제외하고 `actorMemberId`, `provider`, `clientType`, `result`, `reasonCode` 중심으로 audit event 또는 security event를 남긴다.
- 주의: 사용자 존재 여부를 노출하지 않는 enumeration 방어 흐름은 결과 문구와 필드를 더 엄격히 제한한다.

### 2. 외부 연동 metric 분리

- 대상: OAuth provider, Figma, Discord, Slack, Telegram, SES, S3/CloudFront, LLM provider.
- 현재: 일부는 `ExternalApiCallLogger`로 `provider`, `operation`, `result`, `durationMs`, `errorClass`를 남긴다.
- 추가: Micrometer `Timer`/`Counter`로 `provider`, `operation`, `result` 태그를 남겨 대시보드와 알람에 사용한다.

### 3. 스토리지 audit와 metric 보강

- 대상: 업로드 URL 발급, 업로드 완료 확인, 파일 삭제, 다운로드 URL 발급 실패.
- audit 후보: 파일 생성/삭제는 actor와 fileId를 남긴다.
- metric 후보: presigned URL 발급 실패율, S3/CloudFront 호출 지연 시간, 삭제 실패 건수.
- 보안 기준: signed URL, signature, private key, storage object URL 원문은 로그 금지.

### 4. 알림 발송 관측성 개선

- 대상: FCM audience 발송, invalid token 비활성화, webhook flush, SES 발송.
- 추가: 성공/실패 건수를 metric으로 분리하고, 로그에는 batch size와 실패 reason class만 남긴다.
- 주의: 알림 title/content는 사용자 입력 또는 운영 메시지를 포함할 수 있으므로 장기적으로 원문 로그를 줄인다.

### 5. 배치와 스케줄러 표준화

- 대상: email verification retention, figma dispatch retention, workbook auto release, matching round deadline.
- 표준 필드: `jobName`, `trigger`, `threshold`, `processed`, `deleted`, `durationMs`, `result`, `errorClass`.
- 추가: batch job 공통 helper를 두면 시작/종료/실패 로그와 metric을 일관되게 남길 수 있다.

### 6. 과도한 INFO 정리

- 후보: `AuthorizationService`의 권한 평가 시작/subject attribute, JWT 검증 실패 INFO, Firebase 초기화 세부 로그, FCM topic deprecated warning.
- 방향: 요청마다 반복되는 진단 로그는 DEBUG로 내리고, 운영자가 즉시 알아야 하는 상태 변화만 INFO/WARN으로 유지한다.

### 7. 테스트/시드 로그 격리

- `test` 도메인의 seed 로그는 운영 관측성과 성격이 다르다.
- 장기적으로 별도 profile/logger name 또는 admin seed 전용 structured event로 분리한다.
