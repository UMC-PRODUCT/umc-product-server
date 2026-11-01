# security: 공개·보호 경로, 토큰, CORS

`SecurityConfig`, JWT, 인증 필터, CORS, rate limit을 바꿨을 때 쓴다.

## 확인하는 것

- 공개 경로는 토큰 없이 열린다.
- 보호 경로는 토큰 없이 막히고, 유효한 토큰이면 인증 필터를 통과한다.
- 허용한 출처의 CORS 사전 요청은 허용되고, 허용하지 않은 출처는 허용되지 않는다.

## 실행

```bash
V=.claude/skills/verify/scripts/verify.sh

# 공개 경로 (local에서만 열리는 test API)
$V call public-health {api}/test/health-check

# 보호 경로, 토큰 없음
$V call me-no-token {api}/api/v1/member/me

# 보호 경로, local 전용 발급 토큰
TOKEN=$($V token 1)
$V call me-with-token -H "Authorization: Bearer $TOKEN" {api}/api/v1/member/me

# CORS 사전 요청: 허용 출처(verify.sh가 CORS_ALLOWED_ORIGIN_PATTERNS로 넣은 값)와 비허용 출처
$V call cors-allowed -X OPTIONS -H 'Origin: http://localhost:3000' -H 'Access-Control-Request-Method: GET' {api}/api/v1/member/me
$V call cors-denied -X OPTIONS -H 'Origin: https://evil.example' -H 'Access-Control-Request-Method: GET' {api}/api/v1/member/me
```

## 판정

- `public-health`는 200이다.
- `me-no-token`은 401이다.
- `me-with-token`은 401이 아니다. 빈 DB라 회원 1이 없으므로, 인증 필터를 통과한 뒤 404 `MEMBER-0001`(사용자를 찾을 수 없음)이 나오는 것이 정상이다.
- `cors-allowed`는 200이고 `Access-Control-Allow-Origin: http://localhost:3000` 헤더가 있다. `cors-denied`는 403이고 그 헤더가 없다.

## 주의

- `/test/token/access`는 local에서만 열리는 테스트 전용 엔드포인트다. 보안 필터 반응을 보는 준비 단계로만 쓴다. 로그인 흐름 자체를 바꿨다면 실제 로그인 경로로 시나리오를 추가한다.
- rate limit이 켜져 있다(`API_RATE_LIMIT_ENABLED` 기본 true). 같은 경로를 짧은 시간에 많이 부르면 429가 나올 수 있다.
