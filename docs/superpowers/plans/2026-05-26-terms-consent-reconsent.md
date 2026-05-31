# Terms Consent Reconsent Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 약관 동의를 `termId` 기준으로 증빙하고, 이메일 회원가입의 동의 저장 누락을 수정한 뒤 재동의 차단 기능을 단계적으로 도입한다.

**Architecture:** `member` 도메인은 `term` 도메인의 공개 UseCase만 호출한다. 약관 동의 저장과 조회는 `term` 도메인 내부에서 `termId` 기준으로 처리하며, API 차단은 이후 PR에서 JWT 인증 이후의 중앙 필터/인터셉터로 분리한다.

**Tech Stack:** Java 21, Spring Boot 3.5, JPA/Hibernate, Flyway, JUnit 5, Mockito, PostgreSQL.

---

## PR 분할 요약

1. **PR 1: 약관 동의 증빙 기반 보강**
   - 이메일 회원가입 동의 저장 누락 수정
   - `term_consent.term_id`, `term_consent_log.term_id` 추가
   - `TermConsent`/`TermConsentLog`/조회 로직을 `termId` 기준으로 전환
2. **PR 2: 재동의 상태 Query API**
   - `GetRequiredTermConsentStatusUseCase` 추가
   - 현재 활성 필수 약관 대비 미동의 목록 반환
3. **PR 3: 재동의 미완료 사용자 API 제한**
   - JWT 인증 이후 `TermConsentEnforcementFilter` 또는 `HandlerInterceptor` 추가
   - 약관 조회, 재동의 제출, 토큰 갱신, 로그아웃, 탈퇴 등 allowlist 적용
4. **PR 4: 선택 동의/마케팅/운영 메타데이터**
   - 선택 동의 철회 API
   - 마케팅 수신 동의 처리 결과 저장/통지
   - 약관 변경 사유, 시행일, 요약 문구 관리

## 커밋 단위 계획

### Commit 1: `docs: record term consent reconsent plan`

**Files:**
- Create: `docs/adr/022-versioned-term-consent-proof.md`
- Create: `docs/superpowers/plans/2026-05-26-terms-consent-reconsent.md`

- [x] **Step 1: ADR 작성**

`docs/adr/022-versioned-term-consent-proof.md`에 현재 문제, 결정, 대안, PR 분할, 구현 메모를 기록한다.

- [x] **Step 2: 실행 계획 보고서 작성**

`docs/superpowers/plans/2026-05-26-terms-consent-reconsent.md`에 PR 분할과 커밋 단위 계획을 저장한다.

- [x] **Step 3: 문서 커밋**

Run:

```bash
git add docs/adr/022-versioned-term-consent-proof.md docs/superpowers/plans/2026-05-26-terms-consent-reconsent.md
git commit -m "docs: record term consent reconsent plan"
```

Expected: commit succeeds.

### Commit 2: `fix: persist email signup term consent`

**Files:**
- Create: `src/test/java/com/umc/product/member/application/service/EmailMemberRegisterServiceTest.java`
- Modify: `src/main/java/com/umc/product/member/application/service/EmailMemberRegisterService.java`
- Modify: `src/main/java/com/umc/product/member/adapter/in/web/MemberCommandController.java`

- [x] **Step 1: 실패 테스트 작성**

`EmailMemberRegisterServiceTest`에 이메일 회원가입 성공 시 전달된 약관 동의마다 `ManageTermAgreementUseCase#createTermConsent`가 호출되는 테스트를 추가한다.

- [x] **Step 2: 실패 확인**

Run:

```bash
./gradlew test --tests "com.umc.product.member.application.service.EmailMemberRegisterServiceTest"
```

Expected: compile 또는 test failure. 원인은 `EmailMemberRegisterService`가 `ManageTermAgreementUseCase`를 주입받지 않거나 호출하지 않기 때문이다.

- [x] **Step 3: 구현**

`EmailMemberRegisterService`에 `ManageTermAgreementUseCase`를 주입하고 `created.getId()` 기준으로 `command.termConsents()`를 저장한다. OAuth 회원가입 요청에도 `@Valid`를 추가한다.

- [x] **Step 4: 통과 확인**

Run:

```bash
./gradlew test --tests "com.umc.product.member.application.service.EmailMemberRegisterServiceTest"
```

Expected: PASS.

### Commit 3: `feat: store term id in consent records`

**Files:**
- Create: `src/main/resources/db/migration/V2026.05.26.10.00__add_term_id_to_term_consent.sql`
- Modify: `src/main/java/com/umc/product/term/domain/TermConsent.java`
- Modify: `src/main/java/com/umc/product/term/domain/TermConsentLog.java`
- Modify: `src/main/java/com/umc/product/term/application/service/command/TermAgreementCommandService.java`
- Modify: `src/main/java/com/umc/product/term/application/service/query/TermAgreementQueryService.java`
- Modify: `src/main/java/com/umc/product/term/application/port/out/LoadTermPort.java`
- Modify: `src/main/java/com/umc/product/term/application/port/out/LoadTermConsentPort.java`
- Modify: `src/main/java/com/umc/product/term/adapter/out/persistence/TermRepository.java`
- Modify: `src/main/java/com/umc/product/term/adapter/out/persistence/TermPersistenceAdapter.java`
- Modify: `src/main/java/com/umc/product/term/adapter/out/persistence/TermConsentRepository.java`
- Modify: `src/main/java/com/umc/product/term/adapter/out/persistence/TermConsentPersistenceAdapter.java`
- Modify: `src/test/java/com/umc/product/term/application/port/in/command/ManageTermAgreementUseCaseTest.java`
- Create: `src/test/java/com/umc/product/term/application/port/in/query/TermAgreementQueryServiceTest.java`
- Modify: `src/test/java/com/umc/product/integration/term/TermAgreementIntegrationTest.java`
- Modify: `src/test/java/com/umc/product/support/fixture/TermFixture.java`

- [x] **Step 1: 실패 테스트 작성**

`ManageTermAgreementUseCaseTest`에서 저장되는 `TermConsent`와 `TermConsentLog`가 `termId`를 포함하는지 검증한다. 통합 테스트에서는 같은 `TermType`의 새 약관에 재동의할 수 있는지 검증한다.

- [x] **Step 2: 실패 확인**

Run:

```bash
./gradlew test --tests "com.umc.product.term.application.port.in.command.ManageTermAgreementUseCaseTest" --tests "com.umc.product.integration.term.TermAgreementIntegrationTest"
```

Expected: compile 또는 test failure. 원인은 `TermConsent`/`TermConsentLog`에 `termId`가 없고 중복 검사가 `termType` 기준이기 때문이다.

- [x] **Step 3: 구현**

`termId` 필드, repository 조회 메서드, service 저장/조회 로직, Flyway migration을 추가한다.

- [ ] **Step 4: 통과 확인**

Run:

```bash
./gradlew test --tests "com.umc.product.term.application.port.in.command.ManageTermAgreementUseCaseTest" --tests "com.umc.product.integration.term.TermAgreementIntegrationTest"
```

Expected: PASS.

현 작업 환경에서는 `TermAgreementIntegrationTest`가 Testcontainers Docker 클라이언트 초기화 실패로 실행되지 않았다. Docker가 준비된 CI 또는 로컬 환경에서 재실행해야 한다. 대신 아래 Docker 비의존 단위 테스트는 통과했다.

```bash
./gradlew test --tests "com.umc.product.member.application.service.EmailMemberRegisterServiceTest" --tests "com.umc.product.term.application.port.in.command.ManageTermAgreementUseCaseTest" --tests "com.umc.product.term.application.port.in.query.TermAgreementQueryServiceTest"
```

- [ ] **Step 5: PR 1 범위 전체 검증**

Run:

```bash
./gradlew test --tests "com.umc.product.member.application.service.EmailMemberRegisterServiceTest" --tests "com.umc.product.term.application.port.in.command.ManageTermAgreementUseCaseTest" --tests "com.umc.product.integration.term.TermAgreementIntegrationTest"
```

Expected: PASS.

## 후속 PR 상세 계획

### PR 2: 재동의 상태 Query API

**Commit:** `feat: add required term consent status query`

- `term.application.port.in.query.GetRequiredTermConsentStatusUseCase` 생성
- `RequiredTermConsentStatusInfo` 생성
- `LoadTermConsentPort#listByMemberIdAndTermIds` 추가
- `TermAgreementQueryService` 또는 별도 `TermConsentStatusQueryService`에서 현재 활성 필수 약관과 회원 동의 약관을 비교
- Controller는 별도 `GET /api/v1/terms/consent-status/me`로 추가

### PR 3: 재동의 미완료 사용자 API 제한

**Commit 1:** `feat: add term consent enforcement filter`

- JWT 인증 이후 실행되는 `TermConsentEnforcementFilter` 추가
- `CurrentMember` 기반 memberId 추출 대신 `SecurityContextHolder`의 `MemberPrincipal` 사용
- allowlist는 설정 프로퍼티로 시작하되 기본값을 코드에 둔다

**Commit 2:** `test: cover term consent enforcement filter`

- 미인증 요청은 필터가 우회
- 인증 요청 중 약관 조회/재동의 제출은 우회
- 인증 요청 중 일반 API는 `RECONSENT_REQUIRED`로 차단

### PR 4: 선택 동의와 운영 메타데이터 확장

**Commit 1:** `feat: add optional term withdrawal`

- 선택 약관 철회 UseCase 추가
- `TermConsentLog`에 `WITHDRAWN` 저장

**Commit 2:** `feat: add term publication metadata`

- `term.effective_at`, `term.summary`, `term.change_reason`, `term.reconsent_required` 추가
- 관리자 약관 생성 요청 DTO 확장

## 검증 기준

- PR 1은 최소 아래 테스트가 통과해야 한다.

```bash
./gradlew test --tests "com.umc.product.member.application.service.EmailMemberRegisterServiceTest" --tests "com.umc.product.term.application.port.in.command.ManageTermAgreementUseCaseTest" --tests "com.umc.product.integration.term.TermAgreementIntegrationTest"
```

- 전체 PR 제출 전 가능한 경우 `./gradlew test`를 실행한다.

## Self-Review

- Spec coverage: 이메일 회원가입 저장 누락, `termId` 기반 증빙, PR 분할 계획을 포함했다.
- Placeholder scan: 후속 PR은 실행 전 재검토가 필요하지만, PR 1 작업은 파일과 검증 명령을 명시했다.
- Type consistency: `termId`, `TermConsent`, `TermConsentLog`, `ManageTermAgreementUseCase`, `LoadTermPort`, `LoadTermConsentPort` 명칭을 현재 코드 기준으로 맞췄다.
