# ADR-007: Kakao 로그인은 Authorization Code + Redirect URI 흐름을 추가로 지원하도록 확장한다

## Status

Proposed

## Context

UMC PRODUCT 서비스는 현재 Kakao 로그인을 다음과 같은 구조로 처리하고 있다.

- `POST /api/v1/auth/login/kakao` 가 [`KakaoLoginRequest(String accessToken)`](../../src/main/java/com/umc/product/authentication/adapter/in/web/dto/request/KakaoLoginRequest.java)를 받는다.
- [`AuthenticationController#kakaoOAuthLogin`](../../src/main/java/com/umc/product/authentication/adapter/in/web/AuthenticationController.java#L42-L48)에서 [`AccessTokenLoginCommand(provider, token)`](../../src/main/java/com/umc/product/authentication/application/port/in/command/dto/AccessTokenLoginCommand.java)로 변환해 [`OAuthAuthenticationService#accessTokenLogin`](../../src/main/java/com/umc/product/authentication/application/service/OAuthAuthenticationService.java#L74-L91)으로 위임한다.
- [`OAuthTokenVerificationAdapter`](../../src/main/java/com/umc/product/authentication/adapter/out/external/OAuthTokenVerificationAdapter.java)가 provider에 따라 [`KakaoTokenVerifier#verifyAccessToken`](../../src/main/java/com/umc/product/authentication/adapter/out/external/KakaoTokenVerifier.java#L46-L82)을 호출해 `https://kapi.kakao.com/v2/user/me`로 사용자 정보를 조회한다.

이 흐름은 **클라이언트가 Kakao SDK로 직접 access token까지 받아둔 경우**(주로 iOS/Android 네이티브 SDK)에만 적합하다. 그러나 다음 두 가지 요구가 발생했다.

1. **웹/하이브리드 클라이언트에서 redirect URI 기반 OAuth 흐름이 필요하다.**
   - Kakao 로그인 페이지에서 `redirect_uri`로 돌아오는 표준 OAuth2 authorization code grant 흐름을 그대로 사용하고 싶다.
   - 이 경우 클라이언트가 가진 것은 access token이 아니라 **authorization code + 인가 시 사용된 redirect URI** 이다.
   - Kakao token endpoint(`https://kauth.kakao.com/oauth/token`)는 [code 교환 시 `redirect_uri`가 인가 요청 때와 정확히 일치해야 한다](https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#request-token). 즉 서버는 어떤 `redirect_uri`로 발급된 code인지 알고 있어야 한다.
2. **client secret을 서버에서만 관리해야 한다.**
   - Kakao token endpoint 호출에 사용되는 `client_secret`(보안 모드 설정 시)은 서버에서만 보관해야 하므로, 프론트가 token 교환을 대신 수행하게 둘 수 없다.

현재 구조는 다음과 같은 한계를 가진다.

- `KakaoLoginRequest`가 access token 한 가지 필드만 받는 단일 record라 redirect URI를 추가하면 access token 흐름과의 의미가 섞인다.
- `AccessTokenLoginCommand`는 이름이 곧 의미(access token 기반)인 record라 authorization code를 같은 record에 끼워 넣으면 명명 규약([Domain/Application 계층 의미 기반 네이밍](../../CLAUDE.md#read-operation-methods-usecase--adapterrepository))과 충돌한다.
- `VerifyOAuthTokenPort#verify(provider, token)`는 토큰 한 개만 받는 시그니처라 `redirect_uri`처럼 흐름별로 다른 파라미터를 표현할 수 없다.
- ADR-001에서 Apple은 동일한 문제(다른 흐름·파라미터)를 [`verifyAppleAuthorizationCode(code, clientType)`](../../src/main/java/com/umc/product/authentication/application/port/out/VerifyOAuthTokenPort.java#L34) + `AppleAuthorizationCodeResult`로 분리해 해결한 선례가 있다. Kakao에도 같은 방향을 적용하는 것이 자연스럽다.

따라서 **기존 access token 흐름은 그대로 유지**하면서, **authorization code + redirect URI 흐름을 부수적으로 추가**할 수 있는 확장 설계가 필요하다.

## Decision

우리는 Kakao 로그인의 두 가지 진입 방식(access token, authorization code)을 **Command/Port/Verifier 계층에서 명시적으로 분리**하고, **Request DTO 레벨에서는 진입 방식별로 record를 분리**하기로 결정한다. 컨트롤러는 두 record를 각각의 엔드포인트(`/login/kakao`, `/login/kakao/code`)에 매핑하여, 진입 방식이 URL과 DTO 양쪽에서 구분되도록 한다.

구체적인 결정사항은 다음과 같다.

1. **요청 DTO 분리**
   - 기존 `KakaoLoginRequest(String accessToken)`은 유지한다(하위 호환).
   - 신규 record `KakaoCodeLoginRequest(@NotBlank String authorizationCode, @NotBlank String redirectUri)`를 `authentication/adapter/in/web/dto/request` 패키지에 추가한다.
   - `redirectUri`는 클라이언트가 Kakao 인가 요청에 사용한 값을 그대로 전달하며, 서버는 [화이트리스트 검증](#redirect-uri-화이트리스트) 후 Kakao token endpoint 호출에 동일 값으로 사용한다.

2. **Application Command 분리**
   - 기존 `AccessTokenLoginCommand(provider, token)`은 유지한다.
   - 신규 record `AuthorizationCodeLoginCommand(OAuthProvider provider, String authorizationCode, String redirectUri)`를 추가한다.
   - 차후 Google 등 다른 provider에서 동일한 흐름이 필요할 경우 같은 Command를 재사용할 수 있도록 `provider`를 유지한다.

3. **UseCase 확장**
   - [`OAuthAuthenticationUseCase`](../../src/main/java/com/umc/product/authentication/application/port/in/command/OAuthAuthenticationUseCase.java)에 다음 메서드를 추가한다.

     ```java
     OAuthTokenLoginResult authorizationCodeLogin(AuthorizationCodeLoginCommand command);
     ```
   - 내부 구현은 access token 흐름과 동일하게 `VerifyOAuthTokenPort`를 통해 검증한 뒤 `loginWithOAuth2Attributes(...)` 공통 메서드로 위임한다(코드 재사용).

4. **Outbound Port 확장**
   - [`VerifyOAuthTokenPort`](../../src/main/java/com/umc/product/authentication/application/port/out/VerifyOAuthTokenPort.java)에 Kakao 전용이 아닌 **provider 일반화된** 메서드를 추가한다.

     ```java
     OAuth2Attributes verifyAuthorizationCode(
         OAuthProvider provider,
         String authorizationCode,
         String redirectUri
     );
     ```
   - Apple은 `client_id`(=`ClientType`) 분기와 refresh token 반환이 필요하므로 [기존 `verifyAppleAuthorizationCode`](../../src/main/java/com/umc/product/authentication/application/port/out/VerifyOAuthTokenPort.java#L34)를 그대로 둔다. Kakao는 refresh token 보관이 현재 요구사항이 아니므로 `OAuth2Attributes`만 반환한다(향후 필요 시 `KakaoAuthorizationCodeResult` 도입 가능).

5. **Kakao Verifier 확장**
   - `KakaoTokenVerifier`에 다음을 추가한다.

     ```java
     OAuth2Attributes verifyAuthorizationCode(String authorizationCode, String redirectUri);
     ```
   - 동작:
     1. `https://kauth.kakao.com/oauth/token`에 `grant_type=authorization_code`로 POST하여 access token을 교환한다(`client_id`/`client_secret`은 서버 설정값 사용).
     2. 받은 access token을 기존 `verifyAccessToken(accessToken)` 흐름에 재사용하여 `OAuth2Attributes`를 만든다.
   - 즉 신규 메서드는 **token 교환 책임만** 가지며, 사용자 정보 매핑은 기존 메서드를 재사용해 중복을 피한다.

6. **Adapter Facade 라우팅**
   - [`OAuthTokenVerificationAdapter#verifyAuthorizationCode`](../../src/main/java/com/umc/product/authentication/adapter/out/external/OAuthTokenVerificationAdapter.java)는 `switch(provider)` 분기로 `KAKAO`만 우선 구현하고, 다른 provider는 `UnsupportedOperationException`이 아닌 `AuthenticationDomainException`(예: `UNSUPPORTED_OAUTH_FLOW`)을 반환한다.
   - Apple은 `verifyAppleAuthorizationCode` 경로를 계속 사용한다(필드와 결과 타입이 다르므로 통합하지 않는다).

7. **Controller 라우팅**
   - 새 엔드포인트 `POST /api/v1/auth/login/kakao/code`를 추가하고, `KakaoCodeLoginRequest`를 받는다.
   - 기존 `POST /api/v1/auth/login/kakao`는 access token 흐름으로 유지한다.
   - 두 핸들러 모두 결과를 `processOAuthLoginResult(OAuthProvider.KAKAO, result)` 같은 기존 helper로 위임해 JWT 발급 / 신규회원 분기를 공통화한다.

8. **Redirect URI 화이트리스트**
   - `AppleOAuthProperties`처럼 `KakaoOAuthProperties`를 도입하여 `client-id`, `client-secret`, `allowed-redirect-uris: List<String>`를 보관한다.
   - `KakaoTokenVerifier.verifyAuthorizationCode`는 호출 직후 `redirectUri ∈ allowedRedirectUris`인지 검증한다. 불일치 시 `AuthenticationErrorCode.INVALID_OAUTH_TOKEN`을 던진다.
   - 이는 [공개 클라이언트가 임의 redirect URI를 주입해 Kakao 응답을 가로채는 경우를 차단](https://datatracker.ietf.org/doc/html/rfc6749#section-10.6)하기 위한 방어선이다.

## Alternatives Considered

### 단일 `KakaoLoginRequest`에 nullable 필드 추가

`KakaoLoginRequest(String accessToken, String authorizationCode, String redirectUri)` 형태로 모든 필드를 한 record에 모으고, 컨트롤러/서비스에서 비어 있지 않은 쪽을 보고 분기하는 방식이다.

장점:

- DTO 클래스 수가 늘지 않는다.
- 엔드포인트도 하나만 유지된다.
- 클라이언트 입장에서 "Kakao 로그인 = 한 엔드포인트" 라는 단순한 인상을 준다.

단점:

- record 안에서 "필드 A 또는 (필드 B + 필드 C)" 같은 상호배타 조건을 표현해야 해서 `@AssertTrue` 같은 검증을 record 내부에 끼워 넣어야 한다. 검증이 빠지면 두 흐름이 동시에 들어오거나 모두 누락된 요청이 통과될 수 있다.
- Swagger 문서가 모든 필드를 optional로 표기하게 되어, 클라이언트가 어느 조합이 유효한지 문서만 보고 알 수 없다.
- `AccessTokenLoginCommand` ↔ `AuthorizationCodeLoginCommand` 의미 분리 원칙(`CLAUDE.md` "Read Operation Methods" 와 같은 의미 기반 네이밍 컨벤션)과 충돌한다.

선택하지 않은 이유:
"한 record에 두 가지 흐름을 섞는다"는 모호함이, "record/엔드포인트가 하나 늘어난다"는 비용보다 크다. 인증 흐름은 의미가 다르면 타입도 분리해야 디버깅과 운영이 명확해진다.

### `KakaoLoginRequest`를 `sealed interface` + 두 구현체

`sealed interface KakaoLoginRequest permits KakaoAccessTokenLoginRequest, KakaoCodeLoginRequest`로 만들고, Jackson 다형성 역직렬화(`@JsonTypeInfo(use = DEDUCTION)` 또는 `property = "type"`)로 단일 엔드포인트가 두 형태를 모두 받게 하는 방식이다.

장점:

- 엔드포인트 한 개로 유지된다.
- 컴파일러가 분기 누락을 잡아준다.
- 흐름별 의미 분리는 유지된다.

단점:

- Jackson polymorphic deserialization은 디버깅이 어렵고, 잘못된 페이로드에 대한 오류 메시지가 직관적이지 않다.
- DEDUCTION 방식은 필드 충돌 시 깨지기 쉽고, discriminator 방식(`type: "code"`)은 클라이언트와 별도 약속이 필요하다.
- Swagger/OpenAPI 문서에서 oneOf 표현이 SDK 자동 생성기마다 다르게 처리된다(특히 정적 타입 SDK에서 깨지기 쉽다).

선택하지 않은 이유:
"한 엔드포인트 유지"라는 작은 이득을 위해, 직렬화/문서화의 모호함을 끌어들이게 된다. URL이 흐름을 식별하는 가장 단순한 메커니즘이고, 클라이언트도 보통 두 흐름을 동시에 사용하지 않는다.

### Apple처럼 `KakaoAuthorizationCodeResult`까지 분리

Apple은 `AppleAuthorizationCodeResult(attrs, refreshToken, clientId)`를 반환한다. Kakao도 동일하게 별도 결과 타입을 만들어 일관성을 맞추는 방식이다.

장점:

- Apple/Kakao가 같은 패턴으로 보인다.
- 추후 refresh token이나 scope 정보를 보관해야 할 때 확장 지점이 이미 마련되어 있다.

단점:

- 현재 Kakao는 refresh token을 회원 단위로 영속화하지 않는다(연동 해제는 Admin Key 또는 사용자 access token 재발급으로 수행). 결과 타입이 1필드(`attrs`)뿐인 wrapper가 되어 의미가 없다.
- 미래에만 의미 있는 추상화는 [CLAUDE.md의 "Don't design for hypothetical future requirements"](../../CLAUDE.md) 원칙에 어긋난다.

선택하지 않은 이유:
지금은 `OAuth2Attributes` 단일 반환으로 충분하다. 필요해질 때 도입한다(YAGNI). 단, 도입 가능성이 있으므로 Port 시그니처는 `OAuth2Attributes verifyAuthorizationCode(...)`로 두되, 향후 `KakaoAuthorizationCodeResult`로 바꿔도 호출부 변경 범위가 좁도록 KAKAO 분기만 어댑터 내부에 가둔다.

### 신규 흐름만 두고 access token 흐름은 제거(Deprecate)

OAuth2 표준은 authorization code grant이므로, 표준 흐름 하나로 통일하자는 방안이다.

장점:

- 흐름이 한 개로 줄어 코드 양이 작아진다.
- 보안적으로도 일관된다.

단점:

- 이미 Kakao 모바일 SDK 기반으로 access token을 받아 보내는 모바일 클라이언트가 존재한다. 즉 제거는 클라이언트 측 변경을 강제한다.
- Kakao는 모바일 SDK(`UserApi.shared.loginWithKakaoTalk(...)`)가 token까지 직접 발급하는 흐름을 공식적으로 권장한다. 모바일에서 redirect_uri 흐름을 강제하면 UX가 크게 저하된다.

선택하지 않은 이유:
모바일 네이티브 SDK 사용을 포기해야 하므로 ADR-001에서 Apple "단일 client_id로 통합"을 거부한 것과 같은 이유로 부적절하다.

## Consequences

### Positive

- 웹 OAuth 표준(authorization code grant)을 정식으로 지원하게 되어, 브라우저에서 redirect URI를 거치는 모든 클라이언트(예: 운영툴, 외부 임베드 페이지)와 호환된다.
- Kakao `client_secret`이 프론트로 노출되지 않는다. token 교환은 전적으로 서버에서 수행된다.
- `KakaoLoginRequest`와 `AccessTokenLoginCommand`는 그대로 두므로, 기존 모바일 클라이언트의 동작과 API 계약이 깨지지 않는다.
- Application 계층의 Command 분리(`AccessTokenLoginCommand` vs `AuthorizationCodeLoginCommand`)로 인증 흐름의 의도가 코드만 봐도 드러난다.
- Apple과 동일한 "Port에 흐름별 메서드 추가" 패턴을 따라 일관성이 유지된다. 향후 Google 등에서 같은 흐름이 필요하면 `verifyAuthorizationCode`의 `switch` 분기에 추가만 하면 된다.

### Negative

- 엔드포인트가 한 개 늘어난다(`/login/kakao/code`). 클라이언트 팀에 두 진입점의 사용 시점을 명확히 공지해야 한다.
- Kakao 환경 설정에 `client-secret`, `allowed-redirect-uris`가 추가되어 모든 환경(local/dev/prod)에서 누락 없이 세팅해야 한다. 한쪽이라도 비면 해당 환경 로그인 전체가 실패한다.
- `redirect_uri` 화이트리스트 관리 책임이 서버에 생긴다. Kakao Developers Console에 등록한 redirect URI와 서버 화이트리스트가 어긋나면 디버깅이 까다롭다(둘 다 일치해야 함).
- `KakaoTokenVerifier`가 외부 호출(token endpoint, user info)을 두 번 수행하게 되어 단건 로그인 지연이 소폭 늘어난다(~수십 ms 추가).

### Neutral / Trade-offs

- `verifyAuthorizationCode(provider, code, redirectUri)`를 일반화 시그니처로 두지만 Apple은 여전히 별도 메서드를 사용한다. "일반화된 형태"와 "특수화된 형태"가 공존하는 어색함이 있으나, Apple의 `clientType`/`refreshToken`/`clientId` 요구가 다른 provider와 다르므로 강제로 한 시그니처에 욱여넣지 않는 편이 명확하다(ADR-001의 결정 연장선).
- Kakao token endpoint를 호출한 뒤 다시 user info endpoint를 호출한다(2-hop). 1-hop으로 줄이려면 token 응답에 포함된 id_token을 직접 파싱해야 하는데, Kakao id_token 검증은 JWKS 캐싱 등 추가 작업이 필요하므로 본 결정에서는 보류한다.
- `client_secret`을 사용할지 여부는 Kakao 앱의 "보안 모드" 설정과 동기화되어야 한다. 보안 모드 활성 시 서버 호출에 누락되면 `KOE320` 류 오류가 발생한다.

## Implementation Notes

### 패키지/파일 추가

```
authentication/adapter/in/web/dto/request/KakaoCodeLoginRequest.java   # 신규
authentication/application/port/in/command/dto/AuthorizationCodeLoginCommand.java  # 신규
authentication/adapter/out/external/KakaoOAuthProperties.java          # 신규
```

### 기존 파일 변경 지점

- [`OAuthAuthenticationUseCase`](../../src/main/java/com/umc/product/authentication/application/port/in/command/OAuthAuthenticationUseCase.java) — `authorizationCodeLogin(AuthorizationCodeLoginCommand)` 추가
- [`OAuthAuthenticationService`](../../src/main/java/com/umc/product/authentication/application/service/OAuthAuthenticationService.java) — 신규 메서드 구현. 내부에서 `verifyAuthorizationCode(...)` 호출 후 `loginWithOAuth2Attributes(...)` 재사용
- [`VerifyOAuthTokenPort`](../../src/main/java/com/umc/product/authentication/application/port/out/VerifyOAuthTokenPort.java) — `verifyAuthorizationCode(provider, code, redirectUri)` 추가
- [`OAuthTokenVerificationAdapter`](../../src/main/java/com/umc/product/authentication/adapter/out/external/OAuthTokenVerificationAdapter.java) — KAKAO 분기 추가, 나머지는 `UNSUPPORTED_OAUTH_FLOW` 예외
- [`KakaoTokenVerifier`](../../src/main/java/com/umc/product/authentication/adapter/out/external/KakaoTokenVerifier.java) — `verifyAuthorizationCode(code, redirectUri)` 추가(token endpoint 호출 + 기존 `verifyAccessToken` 재사용)
- [`AuthenticationController`](../../src/main/java/com/umc/product/authentication/adapter/in/web/AuthenticationController.java) — `POST /login/kakao/code` 핸들러 추가, 기존 `/login/kakao` 유지
- [`AuthenticationControllerInterface`](../../src/main/java/com/umc/product/authentication/adapter/in/web/swagger/AuthenticationControllerInterface.java) — Swagger 정의 추가
- [`AuthenticationErrorCode`](../../src/main/java/com/umc/product/authentication/domain/exception/AuthenticationErrorCode.java) — `UNSUPPORTED_OAUTH_FLOW`, `INVALID_OAUTH_REDIRECT_URI` 추가 검토

### 환경 변수

`application.yml`의 `app.oauth2.kakao` 섹션:

```yaml
app:
  oauth2:
    kakao:
      client-id: ${KAKAO_CLIENT_ID}
      client-secret: ${KAKAO_CLIENT_SECRET}      # Kakao 앱 보안 모드 사용 시 필수
      admin-key: ${KAKAO_ADMIN_KEY}              # 기존 unlink-by-admin 용도
      allowed-redirect-uris:
        - https://app.umc-product.com/oauth/kakao/callback
        - https://dev.umc-product.com/oauth/kakao/callback
        - http://localhost:5173/oauth/kakao/callback   # local only
```

### Token Exchange 호출 형태(Kakao 공식 스펙)

```text
POST https://kauth.kakao.com/oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code
client_id={REST API KEY}
redirect_uri={요청 시 사용한 redirect_uri}
code={authorization_code}
client_secret={선택, 보안 모드 시 필수}
```

응답의 `access_token`을 기존 `verifyAccessToken(...)` 흐름에 그대로 전달한다.

### 검증 순서 권장

1. `KakaoCodeLoginRequest` Bean Validation (`@NotBlank`)
2. `redirectUri ∈ allowedRedirectUris` (서버 화이트리스트)
3. Kakao token endpoint 교환 (실패 시 `INVALID_OAUTH_TOKEN`)
4. Kakao user info 조회 (기존 로직 재사용)
5. `loginWithOAuth2Attributes(...)` — 기존/신규 회원 분기

### 테스트 가이드

- `KakaoTokenVerifier#verifyAuthorizationCode`는 Kakao token endpoint와 user info endpoint를 모두 호출하므로, `MockRestServiceServer` 또는 WireMock으로 두 호출을 한 테스트에서 stub해야 한다.
- 화이트리스트 검증 실패, token endpoint 4xx 응답, user info 4xx 응답 세 가지 실패 경로에 대해 각각 `void Kakao_인가코드_로그인_redirectUri_불일치_실패()` 등 `@DisplayName`을 한국어로 단 테스트를 추가한다.
- `OAuthAuthenticationService#authorizationCodeLogin`은 verifier를 mocking한 단위 테스트만으로도 충분하다(기존 `accessTokenLogin` 테스트 구조 재사용).

### 운영 시 주의사항

- Kakao Developers Console에 등록된 Redirect URI와 서버 `allowed-redirect-uris`가 어긋나면 token 교환이 `KOE320`/`invalid_grant`로 실패한다. 둘 다 동일하게 관리해야 한다.
- `client_secret`은 절대 로그에 남기지 않는다(현재 `KakaoTokenVerifier`에 로그는 ID/email 수준까지만 노출되도록 유지).
- 모바일 클라이언트는 기존 `/login/kakao`(access token 흐름)을 그대로 사용해도 되며, 강제로 신규 엔드포인트로 옮길 필요가 없다.

## References

- 관련 기존 코드
    - [`KakaoLoginRequest`](../../src/main/java/com/umc/product/authentication/adapter/in/web/dto/request/KakaoLoginRequest.java)
    - [`AccessTokenLoginCommand`](../../src/main/java/com/umc/product/authentication/application/port/in/command/dto/AccessTokenLoginCommand.java)
    - [`KakaoTokenVerifier`](../../src/main/java/com/umc/product/authentication/adapter/out/external/KakaoTokenVerifier.java)
    - [`OAuthAuthenticationService`](../../src/main/java/com/umc/product/authentication/application/service/OAuthAuthenticationService.java)
    - [`VerifyOAuthTokenPort`](../../src/main/java/com/umc/product/authentication/application/port/out/VerifyOAuthTokenPort.java)
- 관련 선행 ADR
    - [ADR-001: Apple 로그인은 ClientType 기반으로 client_id를 분기한다](001-apple-signin-client-type-routing.md)
- Kakao 공식 문서
    - [Kakao 로그인 REST API — 토큰 받기](https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#request-token)
    - [Kakao 로그인 REST API — 사용자 정보 가져오기](https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#req-user-info)
    - [Kakao 로그인 REST API — 연결 끊기](https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#unlink)
- 표준
    - [RFC 6749 §10.6 — Authorization Code Redirection URI Manipulation](https://datatracker.ietf.org/doc/html/rfc6749#section-10.6)
