# macOS 서버 소셜 로그인 개발·확인 안내

## 이번 기능의 범위

Apple·Google·Kakao 계정이 **이미 UMC 회원에 연결된 경우** 로그인한다. 미연결 계정은 `registration_required`로 복귀하며 회원·OAuth 연결을 생성하지 않는다. 같은 이메일이라는 이유만으로 계정을 합치지 않는다.

Mac 앱은 verifier와 state를 생성·보관하고 시스템 인증 브라우저를 연다. 서버는 제공자의 code를 교환해 신원을 확인하고 별도의 UMC 일회용 코드를 만든다. 최종 UMC Access/Refresh Token은 기존 PKCE token API로 받는다.

## 무엇을 왜 바꿨는가

| 변경 | 이유 | 먼저 읽을 코드 |
| --- | --- | --- |
| MACOS_APP / MACOS 추가 | 서비스와 플랫폼을 구별하고 JWT에서 Mac 요청을 식별 | ClientServiceType, ClientType |
| SsoClient에 native·tokenClientType 규칙 | 최초 발급과 갱신에서 플랫폼이 달라지는 문제 방지 | SsoClient, AuthenticationService |
| 로그인 시도 테이블 | 제공자에 다녀와도 처음 Mac 요청과 브라우저를 연결 | SsoSocialLoginAttempt |
| 별도 소비 트랜잭션 | 중복 callback 차단, 외부 실패로 소비가 되돌아가는 문제 방지 | SsoSocialLoginAttemptService |
| 짧은 브라우저 확인 쿠키 | 다른 브라우저로 옮긴 state로 로그인하지 못하도록 검증 | SsoSocialCookieWriter |
| provider 외부 Port/Adapter | application이 Google·Apple·Kakao HTTP 상세에 직접 의존하지 않음 | SsoSocialProviderPort, SsoSocialProviderAdapter |
| Google code 교환 | 기존에는 ID/access token 검증만 있었음 | GoogleTokenVerifier |
| Apple redirect URI와 nonce 검증 | 웹 callback 코드 교환과 로그인 시도 결속 | AppleTokenVerifier |
| 기존 Kakao code 교환 확장 | 기존 구현을 재사용하며 OIDC 사용 시 nonce 검증 | KakaoTokenVerifier |
| 기존 브라우저 SSO 서비스 확장 | 검증된 기존 회원에게만 내부 로그인 증명 발급 | SsoBrowserLoginCommandService |
| 새 시작·callback Controller | 외부 브라우저 redirect와 앱 복귀를 HTTP 경계에서 처리 | SsoSocialLoginController |
| Apple callback 한정 CORS | form POST의 Origin 처리에 필요한 범위만 허용 | SecurityConfig |

코드 경로는 `monolith/src/main/java/com/umc/product/` 아래이며, 대부분 authentication 내부다. Client 분류·보안 설정은 global, ClientType은 common에 있다.

## 요청 흐름을 따라 읽기

1. `SsoSocialLoginController.start()`가 query 입력을 Command로 바꾼다.
2. `SsoSocialLoginCommandService.start()`가 macos-app·정확한 redirect·S256 challenge를 검증한다.
3. provider state·nonce를 난수로 만들고, 브라우저 확인값을 만들거나 재사용한다.
4. provider state와 브라우저 확인값은 해시로 저장한다. Mac state·challenge·callback 주소는 같은 시도에 묶인다.
5. 제공자 로그인 화면으로 이동하고 서버 HTTPS callback으로 돌아온다.
6. `SsoSocialLoginAttemptService.consume()`이 DB 잠금으로 시도를 한 번 소비한다. 이 트랜잭션은 provider HTTP 호출 전에 커밋한다.
7. `SsoSocialProviderAdapter.exchange()`가 제공자 code를 교환한다. ID Token은 서명·issuer·audience를 검증하고 요청한 nonce를 대조한다.
8. 기존 OAuth 매칭이 provider + providerId로 UMC 회원을 찾는다. 미연결이면 가입 필요로 종료한다.
9. 기존 SSO 로그인 증명을 내부에서 만들고 기존 authorize에 전달한다. 일반 브라우저 SSO 쿠키를 새로 설정하지 않는다.
10. Mac으로 UMC code·원래 Mac state를 보낸다. Mac이 state 확인 후 verifier로 token API를 호출한다.

로그인 시도는 10분, 결속 쿠키는 30분이다. UMC 코드는 기존 설정(기본 3분)을 사용한다. 만료 시도는 매시간 정리하되, 만료·사용 여부 판정은 정리 작업과 독립적으로 수행한다.

## API 계약

시작 주소:

```text
GET /api/v1/auth/sso/social/{google|kakao|apple}/authorize
  ?client_id=macos-app
  &redirect_uri=umc-macos%3A%2F%2Fauth%2Fcallback
  &response_type=code
  &state=MAC_RANDOM_STATE
  &code_challenge=S256_CHALLENGE
  &code_challenge_method=S256
```

verifier는 시작 URL에 넣지 않는다. state는 앱에서 안전한 난수로 생성하고 최대 512자, challenge는 verifier의 SHA-256을 padding 없이 Base64URL 인코딩한 값이다.

제공자 등록 callback:

| 제공자 | 서버 경로 | 방식 |
| --- | --- | --- |
| Google | `/api/v1/auth/sso/social/google/callback` | GET |
| Kakao | `/api/v1/auth/sso/social/kakao/callback` | GET |
| Apple | `/api/v1/auth/sso/social/apple/callback` | form POST |

앱 복귀:

```text
umc-macos://auth/callback?code=UMC_CODE&state=MAC_STATE
umc-macos://auth/callback?error=registration_required&state=MAC_STATE
```

실패값은 `registration_required`, `access_denied`, `temporarily_unavailable`, `authentication_failed`다. state·브라우저·provider·만료 검증 실패는 앱으로 보내지 않고 서버 오류로 끝낸다. callback이 재사용되거나 외부 교환에 실패하면 새 로그인부터 시작한다.

토큰 교환:

```http
POST /api/v1/auth/sso/oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code&code=UMC_CODE&client_id=macos-app&redirect_uri=umc-macos%3A%2F%2Fauth%2Fcallback&code_verifier=ORIGINAL_VERIFIER
```

응답은 기존 SSO token API 형태를 유지한다. Mac 토큰을 갱신해도 MACOS_APP 서비스·MACOS 플랫폼이 유지된다.

## 실제 연동 전에 준비할 설정

기본적으로 신규 서버 소셜 시작 기능은 제공자별 disabled다. 실제 환경에 설정한 뒤 각각 enabled로 켠다. 기존 로그인 API를 켜거나 끄는 설정은 아니다.

| 환경 변수 | 값/용도 |
| --- | --- |
| SSO_SOCIAL_GOOGLE_ENABLED | Google 서버 소셜 시작 사용 여부 |
| SSO_SOCIAL_GOOGLE_CALLBACK_URI | 해당 환경의 HTTPS Google callback 전체 주소 |
| SSO_SOCIAL_GOOGLE_CLIENT_ID | 서버 web OAuth client ID |
| SSO_SOCIAL_GOOGLE_CLIENT_SECRET | 해당 web client secret. 비밀값 공급 체계 사용 |
| SSO_SOCIAL_KAKAO_ENABLED | Kakao 서버 소셜 시작 사용 여부 |
| SSO_SOCIAL_KAKAO_CALLBACK_URI | 해당 환경의 HTTPS Kakao callback 전체 주소 |
| SSO_SOCIAL_KAKAO_OIDC | Kakao 앱의 OIDC 설정을 사용하면 true |
| SSO_SOCIAL_APPLE_ENABLED | Apple 서버 소셜 시작 사용 여부 |
| SSO_SOCIAL_APPLE_CALLBACK_URI | 해당 환경의 HTTPS Apple callback 전체 주소 |

Kakao는 기존 `KAKAO_CLIENT_ID`, 필요한 경우 `KAKAO_CLIENT_SECRET`을 재사용한다. callback을 `KAKAO_ALLOWED_REDIRECT_URIS`에도 추가한다. OIDC를 켜면 콘솔도 OIDC를 사용하도록 설정하며 ID Token의 nonce 검증 실패를 userinfo 성공으로 덮지 않는다. OIDC를 사용하지 않는 기존 access token 조회 방식도 지원한다.

Apple은 기존 `APPLE_WEB_CLIENT_ID`, `APPLE_TEAM_ID`, `APPLE_KEY_ID`, `APPLE_PRIVATE_KEY`를 사용한다. Services ID·Return URL·기존 앱과의 연결을 콘솔에서 확인한다. 같은 사람이라도 다른 providerId로 확인되면 이메일만으로 기존 계정에 붙이지 않는다.

Google·Kakao·Apple 콘솔의 redirect URI는 설정값과 정확히 일치해야 한다. 서버 callback은 HTTPS이며 query·fragment·userinfo가 없어야 한다. 운영 주소나 secret을 추정해서 코드에 넣지 않는다.

Flyway 신규 테이블이 필요하다. prod/alpha는 저장소 런북대로 **migration 적용 후 앱 배포** 순서다. 기존 migration을 수정하지 않는다.

## 직접 확인하는 순서

1. 프로젝트 루트에서 `./gradlew check` 결과를 확인한다.
2. 실제 callback용 HTTPS 주소와 provider 콘솔·비밀값을 준비한다.
3. 설정 공급 후 `./gradlew :app:bootRun`으로 실행한다. local 프로필의 기존 비밀값 준비 방식도 그대로 필요하다.
4. Mac에서 scheme을 등록하고, `ASWebAuthenticationSession` 등 시스템 인증 브라우저로 시작 주소를 연다. callback까지 같은 인증 세션을 유지한다.
5. 연결된 회원으로 성공 → UMC code 수신 → 원래 verifier로 token 교환 → 갱신 후 플랫폼 유지 순서로 확인한다.
6. 로그인 취소, 미연결 계정, verifier 오류, callback 재시도도 확인한다.
7. Apple은 실제 form POST에서 쿠키가 전달되는지도 확인한다. 브라우저가 쿠키를 차단하면 검증을 생략하지 않고 다시 시작하게 한다.

`curl`로 시작 주소만 호출하면 provider 화면으로 보낼 Location을 확인할 수 있지만, 실제 사용자의 로그인·브라우저 쿠키·앱 scheme까지 확인한 것은 아니다. callback은 임의 code/state를 넣어 호출하면 성공하지 않는 것이 정상이다.

## 주요 자동 테스트

- `SsoSocialLoginAttemptTest`: 제공자·브라우저·만료·재사용 규칙.
- `SsoSocialLoginAttemptIntegrationTest`: 실제 DB 잠금과 독립 커밋.
- `SsoSocialLoginCommandServiceTest`: 기존 회원·가입 필요·취소·미검증 실패.
- `SsoSocialLoginControllerTest`: 공개 API·보안 쿠키·Apple Origin·실제 Security.
- `SsoFlowIntegrationTest`: 서버 callback→UMC code→기존 PKCE token 교환. 외부 제공자와 JWT 발급은 테스트 대역을 사용한다.
- 기존 Google·Apple·Kakao verifier 테스트: 서명·audience·nonce·코드 교환.
- `SsoSocialJwksFailureTest`: 캐시 미적중·키 교체 시 JWKS 5xx, 요청 및 응답 본문 읽기 중 시간 초과, 본문 읽기 중 연결 끊김의 서버 장애 분류와 기존 로그인 오류 호환성. 손상된 JSON은 인증 실패로 유지하는지도 확인한다.
- 기존 SSO/Authentication 테스트: 코드 재사용 방지와 최초 발급·갱신의 플랫폼 유지.

이 테스트 통과와 실제 제공자 콘솔·Mac 브라우저 연동 완료는 별도로 확인한다.
