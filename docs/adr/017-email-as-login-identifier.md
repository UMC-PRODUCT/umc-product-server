# ADR-017: 회원의 로그인 식별자를 loginId에서 email로 변경한다

## Status

Accepted

## Context

현재 UMC PRODUCT 백엔드는 ID/PW 로그인을 위해 별도의 `loginId`(5-20자, 영문 소문자·숫자·특수문자 일부) 필드를 사용한다. OAuth 회원은 `loginId`가 `NULL`이며 `email`만 보유하고, ID/PW 회원은 `loginId`와 비밀번호로 로그인한다. 이메일은 회원가입 단계에서 이메일 인증 토큰(`EmailVerification`)을 통해 검증되어 `Member.email` 컬럼에 저장된다.

현재 `member` 테이블 컬럼 정의 기준:

- `email VARCHAR(100) NOT NULL` — **UNIQUE 제약 없음**.
- `login_id VARCHAR(20) UNIQUE NULL` — `^[a-z0-9._-]{5,20}$` 형식 제약 (PR #867에서 영문 소문자만 허용으로 강화됨).

### 문제점

1. **사용자 식별자 이원화**: 사용자가 이메일과 별개의 `loginId`를 추가로 기억해야 한다. 가입 시 이미 이메일 인증을 거치므로 이메일은 신뢰 가능한 식별자이지만, 로그인에는 별도 식별자가 또 필요하다.
2. **운영/지원 비용 증가**: 비밀번호 분실, 계정 복구, 고객 지원 등 모든 흐름에서 어떤 식별자를 기준으로 문의했는지 분기 처리가 필요하다.
3. **OAuth/ID-PW 회원 식별 일관성 부족**: OAuth 회원은 `email` 기반으로 식별되고, ID/PW 회원은 `loginId` 기반이라 도메인 정책에 비일관성이 있다.
4. **신규 기능 확장 제약**: 이메일 기반 인증 연동(매직 링크, 비밀번호 재설정 이메일, 이메일 변경 시 재인증 등) 흐름을 설계할 때마다 두 식별자 사이의 매핑·동기화 정책을 함께 고려해야 한다.

### 결정이 필요한 이유

`loginId`는 회원가입 시 추가로 받는 자유 식별자이며, 이미 검증된 이메일 외에 사용자에게 또 다른 식별자를 기억하도록 강요한다. 이메일을 그대로 로그인 식별자로 사용하면 사용자 경험과 도메인 정책 일관성이 모두 개선된다.

## Decision

우리는 **회원의 ID/PW 로그인 식별자를 `loginId`에서 `email`로 완전히 전환**하기로 결정한다.

- `Member.email` 컬럼에 `UNIQUE` 제약을 추가하고, ID/PW 회원의 로그인 식별자로 사용한다.
- `Member.loginId` 필드 및 `member.login_id` 컬럼은 단계적으로 제거한다.
- API 경로:
    - 신규: `POST /api/v1/auth/login/email`
    - 신규: `POST /api/v1/auth/register/email`
    - 신규: `GET /api/v1/auth/email/availability`
    - 제거: `POST /api/v1/auth/login/id-pw`
    - 제거: `POST /api/v1/auth/register/id-pw`
    - 제거: `GET /api/v1/auth/login-id/availability`
- 이메일 기반 회원가입은 기존과 동일하게 `EmailVerification` 토큰(`emailVerificationToken`)으로 이메일을 검증한 뒤 비밀번호와 함께 등록한다.
- `CredentialPolicy`의 `loginId` 형식 정책은 제거하고, 이메일 형식 검증은 Spring `@Email` 기반으로 정책에 추가한다.
- 본 변경은 다음 두 개의 PR로 분리해 진행한다.
    1. **PR A**: `member.email` UNIQUE 제약 마이그레이션 + 본 ADR 문서화.
    2. **PR B**: 도메인 / Persistence / Application / Web 레이어 전환과 기존 `loginId` 코드/컬럼 제거. 변경 범위가 넓지만 의존성이 강하게 묶여 있어 단일 PR로 통합하되, 리뷰 단위로 보기 쉽도록 **여러 커밋으로 분리**한다.

## Alternatives Considered

### 대안 A: 현행 유지 (loginId 별도 식별자)

장점:

- 코드/데이터베이스 변경이 없다.
- 사용자가 이메일을 변경해도 로그인에 영향이 없다(이메일은 단순 연락 채널).
- 한 이메일로 여러 계정을 허용하는 정책(예: 부계정)을 자연스럽게 지원할 수 있다.

단점:

- 사용자가 추가 식별자를 기억해야 한다.
- 비밀번호 찾기/계정 복구 등 모든 흐름에서 이메일과 `loginId`를 매핑하는 추가 단계가 필요하다.
- OAuth 회원과 ID/PW 회원의 식별자 정책이 다르다는 본질적 비일관성이 남는다.

선택하지 않은 이유: 현재 서비스 운영상 한 이메일당 하나의 계정 정책으로 충분하며, 이메일을 식별자로 사용했을 때 얻는 UX/운영 이점이 더 크다고 판단했다.

### 대안 B: loginId와 email 모두 로그인 허용 (Dual identifier)

장점:

- 기존 ID/PW 사용자 경험을 끊지 않고 이메일 로그인을 추가할 수 있다.
- 마이그레이션 기간 동안 점진적 전환이 가능하다.

단점:

- 인증 분기 로직이 복잡해진다(입력값이 이메일인지 `loginId`인지 판별).
- 두 식별자 모두에 대해 unique 제약과 검증을 유지해야 하므로 정책이 더 복잡해진다.
- 장기적으로도 두 식별자가 공존하므로 본 결정이 해결하려는 본질적 일원화를 달성하지 못한다.

선택하지 않은 이유: 일원화가 목적인 결정에서 dual 식별자를 유지하면 단기 호환성은 얻지만 장기 부채만 늘어난다. PR 분할로 위험을 충분히 통제할 수 있으므로 완전 대체를 택한다.

### 대안 C: 단일 PR 빅뱅

장점:

- 변경 범위가 한 번에 정리되어 코드/문서가 깔끔하다.
- 변경 기간 동안의 중간 상태가 없다.

단점:

- 단일 PR이 수십 개 파일을 건드려 리뷰 부하가 크다.
- 운영 DB의 이메일 중복 데이터, 마이그레이션 실패 시 롤백 비용이 크다.
- 코드 변경과 DB UNIQUE 제약 변경을 분리하기 어려워, 운영 환경에서 사전 데이터 정리 시간을 확보할 수 없다.

선택하지 않은 이유: 데이터 변경(UNIQUE 제약)은 먼저 안전하게 적용하고, 코드 변경은 그 뒤에 안전하게 진행하기 위해 **UNIQUE 제약 PR을 별도로 분리**한다. 단, 도메인/Application/Web 변경은 의존성이 강하게 묶여 있어 단일 PR로 통합하되 커밋으로만 단위를 나눈다.

## Consequences

### Positive

- 사용자는 이메일 하나만 기억하면 로그인할 수 있다.
- OAuth 회원과 ID/PW 회원의 식별자 정책이 `email` 기반으로 일원화된다.
- 비밀번호 재설정, 계정 복구, 이메일 기반 인증 연동 흐름을 단순하게 설계할 수 있다.
- `CredentialPolicy`에서 `loginId` 형식 정책(영문 소문자 한정 등)의 유지보수 부담이 사라진다.

### Negative

- 운영 DB에 동일 이메일 중복 회원이 존재한다면 사전 데이터 정리가 필수다.
- 프론트엔드, 모바일 등 클라이언트가 신규 API로 전환해야 한다.
- 이메일 변경 시 로그인 식별자가 함께 바뀌므로, 이메일 변경 흐름에서 추가 재인증/세션 무효화 등의 정책 점검이 필요해질 수 있다.
- 기존 ID/PW 회원에게 "이제 이메일로 로그인하세요" 안내가 필요하다(공지/이메일 발송 등 운영 측 작업).

### Neutral / Trade-offs

- 이메일 형식 검증을 Spring `@Email`로 채택하므로, 매우 이질적인 국제화 이메일(IDN 등)은 표준 라이브러리 동작에 의존한다.
- 한 이메일당 하나의 계정 정책으로 못박히므로, 부계정 등 멀티 계정이 필요한 미래 시나리오에서는 별도 식별자(예: handle)를 추가 도입해야 한다.
- `Member.loginId` 필드/컬럼은 PR B에서 한 번에 제거되므로, PR A 머지 후 PR B 머지 전까지의 짧은 기간 동안 DB UNIQUE 제약만 추가된 상태가 된다. 이 상태에서는 기존 ID/PW 회원가입/로그인이 계속 동작한다.

## Implementation Notes

### 변경 영역 요약

1. **도메인** (`com.umc.product.member.domain.Member`)
    - `registerCredential(String loginId, String encodedPassword)` → `registerCredential(String encodedPassword)` 시그니처 축소. 식별자는 이미 `Member.email`로 보유한다.
    - `hasCredential()` 판단 기준: `email != null && passwordHash != null`.
    - `validateLoginId(...)` 제거.

2. **Persistence** (`com.umc.product.member.adapter.out.persistence`)
    - `MemberJpaRepository.findByLoginId / existsByLoginId` 제거 후 `findByEmail / existsByEmail` 도입.
    - `MemberPersistenceAdapter`, `LoadMemberPort` 시그니처 변경.

3. **Application Service**
    - `MemberCredentialQueryService.findCredentialByLoginId(...)` → `findCredentialByEmail(...)`.
    - `CredentialAuthenticationService.loginByIdPw(...)` → `loginByEmail(...)`.
    - `CredentialAvailabilityQueryService.isLoginIdAvailable(...)` → `isEmailAvailable(...)`.

4. **API (adapter/in/web)**
    - 신규 엔드포인트:
        - `POST /api/v1/auth/login/email`
        - `POST /api/v1/auth/register/email`
        - `GET /api/v1/auth/email/availability`
    - 제거 엔드포인트:
        - `POST /api/v1/auth/login/id-pw`
        - `POST /api/v1/auth/register/id-pw`
        - `GET /api/v1/auth/login-id/availability`
    - DTO: `LoginByEmailRequest`, `EmailAvailabilityResponse`, `EmailRegisterMemberRequest` 신규. 기존 `LoginByIdPwRequest`, `LoginIdAvailabilityResponse`, `IdPwRegisterMemberRequest`, `RegisterCredentialRequest`의 `loginId` 필드는 제거.

5. **CredentialPolicy**
    - `loginId` 정책(`^[a-z0-9._-]{5,20}$`) 제거.
    - 이메일 형식 검증은 Spring `@Email` 기반으로 통일하고, 길이 제약(예: 100자)을 함께 강제한다.
    - 비밀번호 정책은 유지.

6. **Database Migration (Flyway)**
    - PR A: `member.email`에 UNIQUE 제약 추가.
        ```sql
        ALTER TABLE member ADD CONSTRAINT uk_member_email UNIQUE (email);
        ```
    - PR B (제거 마이그레이션):
        ```sql
        ALTER TABLE member DROP CONSTRAINT IF EXISTS member_login_id_check;
        ALTER TABLE member DROP COLUMN IF EXISTS login_id;
        ```

### 사전 분석 SQL

운영 DB에 이메일 중복 회원이 있는지 PR A 머지 전에 확인해야 한다.

```sql
SELECT email, COUNT(*) AS cnt
FROM member
GROUP BY email
HAVING COUNT(*) > 1;
```

중복이 발견되면 도메인/운영 정책에 따라 별도 정리(병합 또는 비활성화)를 PR A 머지 전에 수행한다.

### 회원가입 흐름

기존 이메일 인증 흐름은 유지한다.

1. 클라이언트가 `EmailAuthenticationController`로 이메일 인증 코드 발급/검증.
2. 검증 완료 시 `emailVerificationToken`이 발급된다.
3. 클라이언트는 `POST /api/v1/auth/register/email`에 `emailVerificationToken`, `password` 등을 전달.
4. 서버는 토큰을 검증해 이메일을 확정한 뒤 `Member`를 생성하고 `registerCredential(encodedPassword)`로 자격증명을 등록한다.

## References

- PR #867: `[Feat] 로그인 ID 형식을 영문 소문자만 허용하도록 변경`
- Flyway: `V2026.04.28.19.30__add_credential_columns_to_member.sql`
- Flyway: `V2026.04.28.21.00__add_member_credentials_check_constraint.sql`
- Flyway: `V2026.05.15.21.45__tighten_member_login_id_to_lowercase.sql`
