# ADR-025: OAuth 해제 시 local credential 기반 로그인 수단 불변식 보장

## Status

Accepted

## Context

2026년 6월 기준 회원은 OAuth 계정 또는 이메일/비밀번호 local credential로 로그인할 수 있다. `Member.passwordHash`가 존재하면 local credential이 등록된 상태이고, OAuth 연결은 `member_oauth` row로 관리된다.

현재 `OAuthAuthenticationService#unlinkOAuth`는 회원의 local credential 보유 여부와 무관하게 연결된 OAuth가 1개 이하이면 해제를 거부한다. 이 정책은 OAuth 전용 회원의 로그인 수단 상실은 막지만, local credential을 등록한 회원도 마지막 OAuth를 해제할 수 없게 만든다.

또한 같은 회원의 OAuth 해제 요청이 동시에 들어오면 각 요청이 같은 기존 OAuth 개수를 보고 통과한 뒤 모두 삭제될 수 있다. `member_oauth`와 `member.passwordHash`를 함께 보는 cross-table 불변식은 DB CHECK 제약만으로 표현하기 어렵고, 이 프로젝트의 Hexagonal Architecture 규칙상 authentication 도메인이 member repository나 out port를 직접 사용할 수도 없다. 도메인 간 접근은 공개 UseCase를 통해서만 이뤄져야 한다.

Provider-side OAuth revoke는 provider별 token 보관 정책과 retry/outbox 설계가 필요하다. Google/Kakao는 현재 요청 access token이 있을 때만 revoke 가능하고, Apple도 저장된 refresh token과 client id가 있어야 한다. 따라서 provider-side 연결 해지 보장은 이번 결정의 범위에서 제외한다.

### 문제점

1. local credential을 가진 회원도 마지막 OAuth를 해제할 수 없어 실제 로그인 수단 정책과 맞지 않는다.
2. local credential이 없는 회원은 OAuth가 최소 1개 남아야 하지만, 동시 해제 요청에서는 이 불변식이 깨질 수 있다.
3. provider-side revoke까지 한 트랜잭션에서 강하게 보장하려면 외부 API 호출과 DB 변경의 원자성 문제가 생긴다.

## Decision

우리는 OAuth 해제 정책을 local credential 존재 여부에 따라 분기하기로 결정한다.

1. local credential이 존재하는 회원은 연결된 OAuth가 0개가 되어도 허용한다.
2. local credential이 없는 회원은 일반 OAuth 해제 후에도 OAuth가 최소 1개 남아야 한다.
3. Authentication 도메인은 Member 도메인의 공개 UseCase를 통해 member row를 `PESSIMISTIC_WRITE`로 잠그고 credential 상태만 조회한다.
4. OAuth 해제 검사는 lock 획득 후 수행하여 같은 회원의 동시 해제 요청을 직렬화한다.
5. 탈퇴 흐름은 기존처럼 모든 OAuth row를 삭제하고 member 삭제로 이어지므로 마지막 OAuth 보호 정책을 우회한다.

### 단계적 진행 / PR 분할

- **Phase 1 (이 PR / 본 ADR)**: local credential 기반 OAuth 해제 정책과 member row lock을 적용한다. API 문서와 오류 메시지는 실제 정책을 반영하도록 수정한다.
- **Phase 2 (별도 PR)**: provider-side revoke를 강하게 보장할 수 있도록 provider token 저장, outbox/retry, 실패 보상 정책을 설계한다.

## Alternatives Considered

### 대안 A: 기존처럼 OAuth 최소 1개 고정

local credential 여부와 관계없이 마지막 OAuth 해제를 계속 막는다.

장점:

- 구현 변경량이 가장 작다.
- OAuth 전용 회원의 로그인 수단 상실은 계속 막을 수 있다.

단점:

- local credential을 가진 회원이 OAuth 연결을 완전히 제거할 수 없다.
- 계정 보안 설정에서 사용자가 기대하는 “연동 해제” 동작과 맞지 않는다.

선택하지 않은 이유:

- 요구사항이 “local credential이 존재하면 OAuth가 없어도 된다”로 명확하므로 현재 정책은 비즈니스 규칙을 위반한다.

### 대안 B: DB constraint 또는 trigger로 불변식 보장

`member.password_hash`와 `member_oauth` 개수를 DB trigger로 함께 검사한다.

장점:

- 애플리케이션 버그나 동시성 문제에도 DB가 최후의 방어선이 된다.

단점:

- cross-table count 기반 trigger는 마이그레이션과 운영 복잡도가 높다.
- 도메인 정책이 DB trigger 안에 숨어 테스트와 변경이 어려워진다.

선택하지 않은 이유:

- 이번 변경은 기존 애플리케이션 구조 안에서 해결 가능하며, DB schema 변경 없이도 요구사항을 충족할 수 있다.

### 대안 C: 공개 UseCase와 member row lock으로 보장

Member 도메인이 credential 상태 조회와 row lock을 공개 UseCase로 제공하고, Authentication 도메인은 그 UseCase만 호출한다.

장점:

- Hexagonal Architecture의 도메인 간 의존 규칙을 지킨다.
- 기존 `LoadMemberPort#findByIdForUpdate` 경로를 재사용해 구현 범위가 작다.
- 같은 member 기준 OAuth 해제 요청을 직렬화할 수 있다.

단점:

- DB 제약이 아니므로 모든 OAuth 해제 경로가 동일한 UseCase를 거쳐야 한다.
- member row lock으로 인해 같은 회원의 로그인 수단 변경 요청은 순차 처리된다.

선택한 이유:

- 이번 요구사항의 정확성과 동시성 방어를 가장 작은 변경으로 달성하며, 현재 아키텍처 규칙을 깨지 않는다.

## Consequences

### Positive

- local credential을 등록한 회원은 OAuth를 모두 해제할 수 있다.
- local credential이 없는 회원은 일반 해제 흐름에서 OAuth 최소 1개를 유지한다.
- 같은 회원의 OAuth 동시 해제 요청이 member row lock으로 직렬화된다.

### Negative

- OAuth 해제 시 Member 도메인의 공개 UseCase 호출이 추가되어 도메인 간 협력이 늘어난다.
- lock 획득 때문에 같은 회원의 로그인 수단 변경 요청은 짧게 대기할 수 있다.
- provider-side OAuth revoke 보장은 아직 optional 동작으로 남는다.

### Neutral / Trade-offs

- DB schema 변경 없이 애플리케이션 계층에서 불변식을 보장한다. 추후 OAuth 해제 경로가 늘어나면 반드시 동일한 정책 서비스를 재사용해야 한다.

## Implementation Notes

### 변경 영역 요약

1. **응용 / Port** (`member.application.port.in.query`): credential 상태를 lock과 함께 조회하는 공개 UseCase 메서드와 DTO를 추가한다.
2. **응용 / Service** (`authentication.application.service`): OAuth 해제 시 credential 상태에 따라 마지막 OAuth 해제 가능 여부를 판단한다.
3. **어댑터 (in)** (`authentication.adapter.in.web`): Swagger 설명을 실제 정책에 맞게 수정한다.
4. **테스트** (`src/test/...`): OAuth 해제 정책과 credential 상태 lock 조회 단위 테스트를 추가한다.

### 기타 참고

- provider-side revoke 실패 보상, provider token 저장, outbox/retry는 Phase 2에서 설계한다.
- 이번 변경은 DB 마이그레이션을 포함하지 않는다.

## References

- [ADR-017: 이메일을 로그인 식별자로 사용](./017-email-as-login-identifier.md)
