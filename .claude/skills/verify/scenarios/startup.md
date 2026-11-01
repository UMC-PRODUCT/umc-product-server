# startup: 기동과 설정값 반영

설정, 프로필, 환경변수, `@ConfigurationProperties`, 기동 시 빈 조건, 의존성 버전을 바꿨을 때 쓴다.

## 확인하는 것

- 빈 DB에서 `local` 프로필로 기동이 끝까지 된다.
- 바꾼 설정값이 실제로 반영된다.

## 실행

1. `verify.sh up`이 성공하면 기동 자체는 증명된 것이다. `evidence/health.json`과 `evidence/flyway.txt`가 그 증거다.
2. 바꾼 설정이 관측 가능한 곳을 하나 골라 호출한다.
   - 관리 엔드포인트: `verify.sh call info -sS http://127.0.0.1:<MANAGEMENT_PORT>/actuator/info` (포트는 `doctor` 출력이나 `.verify/current/state.env`에 있다)
   - 설정에 따라 열리고 닫히는 경로: 예를 들어 `app.test-api.enabled`는 `verify.sh call test-health {api}/test/health-check`로 확인한다.
3. 설정이 응답으로 드러나지 않으면 `app.log`에서 해당 빈이나 설정이 적용된 로그 줄을 찾아 증거에 적는다.

## 주의

- 새 필수 환경변수(기본값 없는 `${VAR}`)를 추가했다면 `scripts/verify.sh`의 `write_app_env`에도 더미 값을 넣어야 기동된다. 이 스크립트가 실패하는 것이 곧 "팀원 로컬 환경도 깨진다"는 신호다.
- yml 기본값이 빈 값이어도 코드에서 공백을 막는 설정이 있다. `@Validated` 붙은 `*Properties` 클래스의 `@NotBlank`를 보면 찾을 수 있다. 예를 들어 메일 제공자가 `ses`이면 `SES_ACCESS_KEY_ID`, `SES_SECRET_ACCESS_KEY`가 비어 있을 때 기동이 실패한다. local 프로필은 메일 제공자 기본값이 `log`(보내지 않고 로그만 남김)라서 이 키가 필요 없다.
- 값의 형식을 검사하는 설정이 있다. 데모데이 스탬프 암호화 키는 Base64로 디코딩해 32바이트여야 하고, JWT 비밀값은 HMAC 키로 쓸 만큼 길어야 한다.
