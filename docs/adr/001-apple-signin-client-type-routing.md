# ADR-001: Apple 로그인은 ClientType 기반으로 client_id를 분기한다

## Status

Accepted

## Context

UMC PRODUCT 서비스는 Web, iOS, Android 세 플랫폼에서 Apple 로그인을 지원해야 한다.

Apple Sign-In은 같은 Apple 계정이라도 플랫폼별로 서로 다른 `client_id`를 사용하도록 설계되어 있다.

- **iOS Native (Sign in with Apple)**: 앱의 **Bundle ID** (예: `com.umc.product.ios`)
- **Web / Android (Sign in with Apple JS, 웹 플로우)**: Apple Developer Console에 등록한 **Services ID** (예: `com.umc.product`)

서비스 초기에는 단일 `client_id`(`com.umc.product`)만으로 Apple token endpoint를 호출하고 있었다. 이로 인해 다음과 같은 문제가 발생했다.

1. **authorization code 교환 실패**: iOS 네이티브에서 발급된 authorization code를 단일 Services ID로 교환하려 하면 Apple이 `invalid_grant`를 반환한다. authorization code는 발급 당시의 `client_id`로만 교환할 수 있기 때문이다.
2. **id_token audience 불일치**: id_token의 `aud` claim에는 발급 당시의 `client_id`가 들어 있으므로, 잘못된 `client_id`로 audience를 검증하면 토큰이 모두 거부된다.
3. **revoke 호출 실패 위험**: Apple `/auth/revoke` 엔드포인트도 발급 당시 사용한 `client_id`를 정확히 동일하게 요구한다. 즉, 회원 탈퇴/연동 해제 시점에도 발급 시점의 `client_id`를 알아야 한다.

또한 `client_secret`은 `client_id`를 subject claim으로 갖는 ES256 JWT이므로, `client_id`가 바뀌면 `client_secret`도 함께 새로 생성되어야 한다. 다시 말해 단일 클라이언트 정보를 서버가 들고 있는 구조로는 멀티 플랫폼을 지원할 수 없다.

따라서 Apple 로그인 흐름 전체에서 "어느 플랫폼에서 발급된 코드/토큰인가"를 식별할 수 있는 메커니즘이 필요했다.

## Decision

우리는 Apple 로그인 요청에 클라이언트 플랫폼을 식별하는 `ClientType`을 명시적으로 포함하고, 서버가 이를 기준으로 `client_id`를 분기하도록 결정한다.

구체적인 결정사항은 다음과 같다.

1. `common.domain.enums`에 `ClientType { ANDROID, IOS, WEB }` enum을 추가한다. 이 enum은 Apple 분기 전용이 아닌, 향후 다른 플랫폼별 분기 처리에도 공용으로 재사용한다.
2. `AppleLoginRequest`에 `ClientType clientType` 필드를 `@NotNull`로 추가하여, 클라이언트가 자신의 플랫폼을 명시적으로 신고하도록 한다.
3. `AppleOAuthProperties`를 단일 `client-id`에서 `ios-client-id`, `web-client-id`로 분리하고, `resolveClientId(ClientType)` 메서드로 매핑을 캡슐화한다. WEB과 ANDROID는 동일하게 Services ID를 사용한다.
4. `AppleTokenVerifier`의 `verifyAuthorizationCode`, `verifyIdToken`, `generateClientSecret`, `revokeToken`을 모두 `clientId` 기준으로 동작시킨다.
5. `MemberOAuth` 엔티티와 `member_oauth` 테이블에 `apple_client_id` 컬럼을 추가하여, refresh token 발급 시점에 사용된 `client_id`를 저장한다. revoke 시 이 값을 그대로 재사용한다.
6. 기존 APPLE 행은 단일 Services ID를 사용하던 시절에 발급된 것이므로 `com.umc.product`로 backfill하는 Flyway 마이그레이션을 적용한다.

## Alternatives Considered

### 단일 client_id(Services ID)로 통합

Apple Services ID 하나만 사용하고, iOS 네이티브에서도 웹 기반 Sign in with Apple JS 플로우를 사용하도록 강제하는 방식이다.

장점:

- 서버가 단일 `client_id` / 단일 `client_secret`만 관리하면 된다.
- 코드 분기점이 발생하지 않는다.

단점:

- iOS 사용자에게 네이티브 Sign in with Apple 시트 대신 웹뷰 기반 로그인을 강제하게 되어, 플랫폼 사용자 경험이 크게 저하된다.
- Apple 정책상 iOS 앱이 다른 OAuth 로그인을 제공하면서 Sign in with Apple을 함께 제공할 때는 네이티브 Sign in with Apple 사용이 사실상 요구된다.

선택하지 않은 이유:
iOS 네이티브 로그인 UX를 포기하는 비용이 서버 코드 분기 한 곳을 줄이는 이득보다 훨씬 크다. App Review 정책 측면에서도 위험이 있다.

### User-Agent 또는 헤더 기반 자동 추론

서버에서 `User-Agent`, `X-Platform` 등 HTTP 헤더를 분석해 플랫폼을 자동으로 추론하는 방식이다.

장점:

- 클라이언트가 별도 필드를 보내지 않아도 된다.
- API 스펙이 단순해 보인다.

단점:

- User-Agent는 위변조와 변형이 자유로워 신뢰할 수 없다. WebView in iOS, 하이브리드 앱, 임베디드 브라우저 등 모호한 케이스가 다수 존재한다.
- 잘못 추론된 한 번의 요청이 `invalid_grant`로 실패하면 사용자는 로그인 자체가 안 되는 강한 장애를 겪는다.
- 추론 로직 자체가 시간이 지나며 OS/브라우저 변동에 취약해진다.

선택하지 않은 이유:
플랫폼 식별자는 인증 흐름의 핵심 정보다. 추론에 의존하면 디버깅과 운영 부담이 늘고, 잘못 추론되었을 때의 실패가 사용자에게 직접 노출된다. 클라이언트가 자기 플랫폼을 신고하는 것이 가장 단순하고 정확하다.

### 클라이언트가 client_id를 직접 전송

요청 본문에 `client_id`를 그대로 받아 그대로 Apple에 전달하는 방식이다.

장점:

- 서버가 매핑 테이블을 들고 있을 필요가 없다.
- 새 플랫폼 추가 시 서버 코드를 건드리지 않아도 된다.

단점:

- 서버가 허용하는 `client_id` 화이트리스트 검증이 필수로 추가된다. 결국 서버가 매핑 정보를 알고 있어야 하므로 이득이 사라진다.
- 클라이언트가 잘못된 `client_id`를 보내면 `client_secret`이 그에 맞지 않아 모든 호출이 실패한다.
- 기밀 자산은 아니지만, `client_id`라는 인증 식별자를 클라이언트가 스스로 결정하도록 하는 것은 책임 분리 측면에서 부적절하다.

선택하지 않은 이유:
`client_id`는 서버가 관리해야 하는 자산이며, 클라이언트는 "나는 어떤 플랫폼이다"라는 의도만 신고하면 된다. enum 기반 분기가 책임 경계상 더 깔끔하다.

### 플랫폼별로 별도 엔드포인트 분리 (`/oauth/apple/ios`, `/oauth/apple/web`)

URL 경로 자체를 플랫폼별로 분리하는 방식이다.

장점:

- 라우팅 시점에 플랫폼이 식별된다.
- 컨트롤러 메서드가 작아진다.

단점:

- API 표면이 N배로 늘어나고 Swagger 문서·테스트 코드도 늘어난다.
- 신규 플랫폼 추가 시마다 새 엔드포인트를 추가해야 한다.
- 컨트롤러는 입력 받는 필드만 다를 뿐 비즈니스 로직은 100% 동일하므로 중복 코드를 만들 가능성이 크다.

선택하지 않은 이유:
플랫폼 차이는 한 필드(`clientType`)만으로 표현 가능한 분기다. 엔드포인트를 늘릴 만한 의미 단위 차이가 아니다.

## Consequences

### Positive

- iOS 네이티브 Sign in with Apple과 Web/Android 웹 플로우를 동시에 정상 지원할 수 있다.
- `client_id` 매핑 책임이 서버 한곳(`AppleOAuthProperties.resolveClientId`)에 모여 있어 변경 영향 범위가 좁다.
- `member_oauth.apple_client_id`가 영속화되므로 회원 탈퇴 시점의 revoke가 발급 시점과 동일한 `client_id`로 정확하게 호출된다. 이는 Apple 정책(연동 해제 보장) 준수에 필수적이다.
- `ClientType` enum이 Apple 외 다른 플랫폼별 분기에도 재사용 가능한 공용 자산으로 자리 잡는다.

### Negative

- 클라이언트가 `clientType`을 누락 또는 잘못 보내면 `invalid_grant` 또는 audience mismatch로 로그인이 실패한다. API 계약상 필수값으로 강제하고, 실패 로그에 `clientType`을 남겨 원인 파악을 빠르게 해야 한다.
- 환경설정이 늘어난다(`app.oauth2.apple.ios-client-id`, `web-client-id` 두 값을 모든 환경에 정확히 세팅해야 한다).
- DB 컬럼(`apple_client_id`)이 추가되어 기존 데이터에 backfill이 필요하다. 이미 마이그레이션으로 처리했으나, 멀티 환경(local/dev/prod) 모두에서 실행을 누락하지 않도록 주의해야 한다.

### Neutral / Trade-offs

- `apple_client_id`를 평문으로 저장한다. 이 값 자체는 Apple Developer Console에 공개된 식별자이므로 비밀로 취급할 필요는 없으나, 향후 운영상 의미가 변하면 재검토가 필요하다.
- 플랫폼이 늘어나면 `ClientType`과 `resolveClientId` 두 곳을 동시에 수정해야 한다. enum 분기 누락을 컴파일러가 잡아주도록 `switch`는 default 없이 작성한다(현재 코드 상태와 동일).
- Web과 Android가 동일한 Services ID를 공유한다. Apple 입장에서 이 둘은 같은 클라이언트로 보이며, 추후 Android에 별도 클라이언트가 필요해지면 enum 매핑을 갱신하면 된다.

## Implementation Notes

### 환경 변수

`application.yml`의 `app.oauth2.apple` 섹션에 다음 두 값을 분리해서 관리한다.

```yaml
app:
  oauth2:
    apple:
      ios-client-id: com.umc.product.ios   # iOS Bundle ID
      web-client-id: com.umc.product       # Apple Developer Services ID (Web/Android 공용)
      team-id: ...
      key-id: ...
      private-key: ...
```

### 핵심 코드 위치

- `src/main/java/com/umc/product/common/domain/enums/ClientType.java` — 플랫폼 enum
- `src/main/java/com/umc/product/authentication/adapter/in/web/dto/request/AppleLoginRequest.java` — 클라이언트가 `clientType`을 신고하는 진입점
- `src/main/java/com/umc/product/authentication/adapter/out/external/AppleOAuthProperties.java` — `resolveClientId(ClientType)` 매핑
- `src/main/java/com/umc/product/authentication/adapter/out/external/AppleTokenVerifier.java` — code 교환·id_token 검증·revoke가 모두 `clientId` 기반으로 동작
- `src/main/java/com/umc/product/authentication/domain/MemberOAuth.java` — `appleClientId` 필드와 `updateAppleCredentials(refreshToken, clientId)` 도메인 메서드
- `src/main/resources/db/migration/V2026.05.06.20.00__add_apple_client_id_to_member_oauth.sql` — `apple_client_id` 컬럼 추가 및 backfill

### 회원 가입/연동 흐름

신규 Apple 회원 가입 시에는 `OAuthRegisterMemberCommand` → `LinkOAuthCommand`까지 `appleClientId`가 그대로 전파되어, `MemberOAuth` 레코드에 발급 당시 `client_id`가 저장된다. 기존 회원의 토큰 갱신 시에는 `OAuthAuthenticationService.updateAppleRefreshToken(provider, providerId, refreshToken, clientId)`를 통해 `client_id`도 함께 갱신한다.

### 운영 시 주의사항

- 새 환경(stage, prod 등)을 띄울 때 두 client_id 값을 모두 누락 없이 세팅해야 한다. 한쪽이 비면 해당 플랫폼 로그인이 전부 실패한다.
- 기존 데이터의 `apple_client_id`는 `com.umc.product`로 backfill되었다. 만약 실제로는 iOS Bundle ID로 발급된 회원이 있었다면 해당 회원의 revoke가 실패할 수 있으므로, 실제 운영 데이터에 iOS 네이티브 Apple 로그인 사용자가 존재했는지 확인이 필요하다.
- `clientType`이 누락된 요청에 대해서는 `@NotNull` 검증으로 400을 반환하고 있으므로, 클라이언트 팀에 필수 필드임을 명확히 공지한다.

### 새 플랫폼 추가 절차

1. `ClientType`에 새 값 추가
2. `AppleOAuthProperties.resolveClientId`의 `switch`에 매핑 추가 (default 없이 작성하므로 컴파일러가 누락을 감지)
3. 필요한 경우 `application.yml`에 새 client_id 항목 추가
4. Apple Developer Console에 해당 플랫폼의 식별자 등록

## References

- 관련 커밋
    - `46bc8315` feat: 클라이언트 플랫폼 구분용 ClientType enum 추가
    - `c742708a` refactor: Apple Sign-In 토큰 검증을 플랫폼별 client_id로 분기
    - `c848c88c` feat: Apple 회원가입 흐름에 appleClientId 전파
- Apple 공식 문서
    - [Sign in with Apple REST API — TokenResponse](https://developer.apple.com/documentation/signinwithapplerestapi/tokenresponse)
    - [Sign in with Apple REST API — Revoke Tokens](https://developer.apple.com/documentation/signinwithapplerestapi/revoke-tokens)
    - [Creating a client secret](https://developer.apple.com/documentation/accountorganizationaldatasharing/creating-a-client-secret)
