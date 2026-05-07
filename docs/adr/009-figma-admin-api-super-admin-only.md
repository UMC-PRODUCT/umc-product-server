# ADR-007: Figma admin 컨트롤러는 SUPER_ADMIN 만 접근 가능하도록 CheckAccess + PermissionEvaluator 로 보호한다

## Status

Accepted (2026-05-07): 4 개 커밋(ResourceType.FIGMA · evaluator · 컨트롤러 어노테이션 일괄 적용 · 단위 테스트) 으로 본 ADR 의 결정을 그대로 구현 완료.

## Context

ADR-003 / ADR-005 의 결정에 따라 `com.umc.product.figma` 도메인에는 다음 admin 컨트롤러들이 생성되어 있다 (2026-05-07 기준).

- [FigmaRoutingDomainController](../../src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java) — 라우팅 도메인 / 멘션 등록·삭제·조회
- [FigmaWatchedFileController](../../src/main/java/com/umc/product/figma/adapter/in/web/FigmaWatchedFileController.java) — 폴링 대상 파일 등록·활성화·비활성화·조회
- [FigmaSyncController](../../src/main/java/com/umc/product/figma/adapter/in/web/FigmaSyncController.java) — 즉시 동기화 트리거·preview
- [FigmaDigestController](../../src/main/java/com/umc/product/figma/adapter/in/web/FigmaDigestController.java) — 임의 시간창 catch-up 발송
- [FigmaOAuthController](../../src/main/java/com/umc/product/figma/adapter/in/web/FigmaOAuthController.java) — Figma OAuth 위임 인증 시작 / 콜백

이 컨트롤러들은 모두 `/api/v1/admin/figma/...` prefix 를 갖고 있어 의도상 운영자 전용이지만, 실제로는 어떤 인가(authorization) 도 적용되어 있지 않다. 즉, JWT 만 통과하면 일반 챌린저도 호출 가능한 상태다. 다음과 같은 운영 위험이 있다.

1. **Discord webhook 권한 노출** — `POST /admin/figma/routing-domains` 와 mention 등록 endpoint 가 인가 없이 노출되어 있으면, 임의 사용자가 자신의 Discord 채널 webhook URL 로 라우팅 도메인을 등록하거나 멘션 대상을 자기 자신으로 바꿀 수 있다. 이는 ADR-005 의 webhook URL 마스킹 정책을 무력화한다.
2. **OAuth 위임 탈취** — `GET /admin/figma/oauth` 의 start endpoint 가 누구나 호출 가능하면, state 가 호출자 memberId 로 묶이는 구조상 일반 사용자가 자신을 owner 로 하는 Figma 통합을 등록할 수 있다. 그 결과 실제 운영자가 의도하지 않은 owner 가 `figma_integration` 에 들어가고, 모든 polling/digest 가 그 사용자 권한으로 진행된다.
3. **Figma rate limit 소진** — `POST /admin/figma/sync`, `POST /admin/figma/digest` 가 인가 없이 노출되면 누구나 동기화/digest 를 임의로 트리거해 Figma API 의 분당 호출 한도를 빠르게 소진시킬 수 있다.
4. **보안 모니터링 공백** — 인가가 없으면 어떤 호출이 정상이고 어떤 호출이 비정상인지 분리할 수 없어, 운영자 ID 기반 감사(audit) 가 불가능하다.

기술 환경 / 제약 사항은 다음과 같다.

- 프로젝트는 자체 인가 추상으로 `@CheckAccess(resourceType, resourceId, permission)` 어노테이션과 `AccessControlAspect` 를 사용한다 (`com.umc.product.authorization`). aspect 가 SecurityContext 에서 `MemberPrincipal` 을 꺼내 `CheckPermissionUseCase.check(memberId, ResourcePermission)` 를 호출하고, `ResourceType` 에 매칭되는 `ResourcePermissionEvaluator` 가 실제 판단을 내린다.
- 신규 `ResourceType` 은 enum 에 추가해야 하며, 각 evaluator 는 `@Component` 로 등록되면 자동으로 `AuthorizationService` 가 수집해 사용한다.
- `ChallengerRoleType.SUPER_ADMIN` 은 이미 정의되어 있고, `roleType().isSuperAdmin()` 헬퍼가 존재한다. `TermPermissionEvaluator`, `SchedulePermissionEvaluator` 등이 이미 같은 패턴을 사용 중이라 신규 evaluator 도 동일 형태로 작성할 수 있다.
- `FigmaOAuthController#callback` 은 브라우저 redirect 로 호출되어 JWT 가 실리지 않으므로 이미 `@Public` 으로 노출되어 있어야 한다 (state 안에 owner memberId 가 묶임). 따라서 callback 은 인가 적용 대상에서 명시적으로 제외해야 한다.
- `AccessControlAspect.extractMemberId()` 는 인증되지 않은 호출에 대해 `AccessDeniedException` 을 던진다. 즉 `@CheckAccess` 가 붙은 메서드는 사실상 인증 + 인가가 동시에 강제된다.

따라서 이번 결정에서는 다음을 정해야 한다.

- 신규 `ResourceType` 을 추가할 것인가, 기존 enum 을 재사용할 것인가.
- 권한 정책의 단위 — 단일 `MANAGE` 권한으로 통합할지, `READ`/`MANAGE` 를 분리할지.
- 모든 admin endpoint 에 어노테이션을 거는지, 일부만 거는지 (특히 OAuth callback).
- evaluator 의 판단 기준 — SUPER_ADMIN 만 통과시킬지, 중앙운영사무국 총괄단까지 허용할지.

## Decision

우리는 다음과 같이 결정한다.

1. **ResourceType.FIGMA 신규 추가**. `com.umc.product.authorization.domain.ResourceType` 에 `FIGMA("figma", "Figma 통합", Set.of(READ, MANAGE))` 를 추가한다. 권한 단위는 두 개로 분리한다.
   - `READ` — 모든 GET 조회 endpoint.
   - `MANAGE` — POST / DELETE 류의 모든 상태 변경 endpoint (라우팅 도메인 등록·삭제·멘션 추가/삭제, watched file 등록/활성화/비활성화, 즉시 sync 트리거, digest 발송, OAuth start).
2. **`FigmaPermissionEvaluator` 추가**. `ResourceType.FIGMA` 의 모든 권한(`READ`, `MANAGE`) 에 대해 SUPER_ADMIN 인 사용자만 `true` 를 반환한다. 다른 역할은 거부한다. 지원하지 않는 권한 enum 이 들어오면 그대로 false 반환 (방어적 기본값).
3. **모든 figma admin 엔드포인트에 `@CheckAccess` 적용** (단, `FigmaOAuthController#callback` 제외).
   - 5 개 컨트롤러의 모든 `@GetMapping` 에 `@CheckAccess(resourceType=FIGMA, permission=READ)`.
   - 모든 `@PostMapping` / `@DeleteMapping` 에 `@CheckAccess(resourceType=FIGMA, permission=MANAGE)`.
   - `FigmaOAuthController#callback` 은 브라우저 redirect 로 JWT 가 실리지 않으므로 기존 `@Public` 을 유지하고 `@CheckAccess` 를 적용하지 않는다. owner memberId 위변조 방지는 state 의 단기 수명 + owner-binding 로직에 의존한다.
4. **resourceId 는 명시하지 않는다**. Figma admin 데이터는 운영자 단일 그룹의 글로벌 자산이므로, 리소스 단위 권한 차등을 도입하지 않는다. `@CheckAccess` 는 resourceId 없이 type-level 권한 체크 형태로 사용한다 (`ResourcePermission.ofType(...)`). 향후 멀티 테넌시 / 파트별 위임이 필요해지면 별도 ADR 로 도입한다.
5. **`isSuperAdmin()` 단일 기준 채택**. 중앙운영사무국 총괄단(`isAtLeastCentralCore()`) 까지 허용하지 않는다. Figma 통합 자체가 외부 OAuth 자산을 다루는 인프라 작업이며, 잘못 등록되면 모든 채널이 망가질 수 있어 권한 분리 단위를 최소로 둔다.

## Alternatives Considered

### 1. ResourceType 을 새로 추가하지 않고 기존 enum 을 재사용

예: `ResourceType.NOTICE` 또는 `ResourceType.AUDIT` 의 권한 체계를 빌려 쓰는 방식.

장점:

- 신규 enum 추가가 없어 마이그레이션이 단순하다.
- evaluator 도 새로 만들 필요가 없다.

단점:

- 의미가 어긋난다. Figma 통합은 공지/감사와 무관한 외부 통합 인프라이며, 권한 정책이 달라질 수 있다.
- 향후 권한 미세조정(예: digest 만 별도 권한) 이 필요해지면 다른 도메인의 enum 을 변형해야 한다.
- 로그/감사에서 `AUDIT` 권한 체크와 `FIGMA` 권한 체크가 같은 ResourceType 으로 기록되어 구분이 어려워진다.

선택하지 않은 이유:
ResourceType 은 도메인 구분 라벨로 동작하므로, Figma 작업은 별도 enum 으로 분리하는 것이 응집도와 향후 확장성 측면에서 명확하다.

### 2. 단일 `MANAGE` 권한으로 모든 endpoint 통합

GET 도 `MANAGE` 로 묶어 권한 단위를 하나만 두는 방식.

장점:

- enum 정의가 가장 단순하다.
- evaluator 가 분기 없이 SUPER_ADMIN 단일 체크만 하면 된다.

단점:

- 권한 모델 자체로는 "조회는 가능하지만 변경은 불가" 같은 정책 분리가 불가능해진다. 향후 운영자 권한 등급이 분리되면 (예: 읽기 전용 admin) 다시 enum 을 쪼개야 한다.
- 코드를 처음 보는 사람이 GET endpoint 에 `MANAGE` 권한이 붙어 있는 것을 보고 의미를 파악하기 어렵다.

선택하지 않은 이유:
지금은 두 권한 모두 SUPER_ADMIN 으로 귀결되더라도, 권한 어노테이션을 의미 정렬해 두면 향후 정책 분리 시점에 evaluator 한 군데만 수정하면 된다. 어노테이션 변경 비용이 더 크다.

### 3. `isAtLeastCentralCore()` 까지 허용

중앙운영사무국 총괄/부총괄까지 Figma admin API 접근을 허용하는 방식.

장점:

- 운영 동시성이 좋아진다 (SUPER_ADMIN 한 명이 부재해도 다른 총괄단이 즉시 대응 가능).
- 다른 평가기(`FcmPermissionEvaluator`) 와 권한 정책이 일관된다.

단점:

- Figma OAuth refresh token 등 민감 자산을 다루는 권한이 더 많은 사람에게 분산된다.
- 운영 사고(잘못된 webhook 등록 등) 가 발생했을 때 책임 추적이 어려워진다.
- Discord webhook URL 은 한 번 노출되면 회수가 어려워, 권한 보유자 수가 곧 risk surface 다.

선택하지 않은 이유:
ADR-003/005 에서 다룬 Figma 통합 자산의 위험도가 일반 알림(FCM) 보다 높고, 이번 단계의 운영 규모상 SUPER_ADMIN 으로도 충분히 대응 가능하다. 권한 확대는 운영 규모가 커진 시점의 별도 ADR 로 다룬다.

### 4. `@CheckAccess` 대신 Spring Security 의 `hasRole('SUPER_ADMIN')` / `@PreAuthorize`

Spring Security expression 으로 직접 표현하는 방식.

장점:

- Spring Security 기본 메커니즘만 사용하므로 학습 곡선이 낮다.
- 어노테이션 한 줄만 추가하면 된다.

단점:

- 프로젝트 전체가 이미 `@CheckAccess` 기반 인가 모델로 통일되어 있다. 일부만 다른 메커니즘을 쓰면 권한 정책이 두 곳에서 관리된다.
- 추후 권한 단위(READ/MANAGE 등) 분리가 필요해지면 평가기 패턴이 더 적합하다.
- 감사 로그를 일관된 한 곳(`AccessControlAspect`) 에서 남길 수 없다.

선택하지 않은 이유:
프로젝트 컨벤션과 일관되지 않으며, 향후 권한 정책 변경 시 두 군데를 동시에 관리해야 하는 비용이 크다.

### 5. OAuth callback 에도 `@CheckAccess` 를 강제

callback 에 `@Public` 을 제거하고 인증/인가를 강제.

장점:

- 인가 정책의 일관성이 가장 높다.
- 누가 callback 을 트리거했는지 SecurityContext 로 추적 가능.

단점:

- Figma OAuth 동의 후의 redirect 는 브라우저 navigation 이라 Authorization 헤더가 실리지 않는다 — 사실상 호출이 절대 불가능해진다.
- 동일 흐름을 그대로 두면서 인증을 강제하려면 redirect 를 임시 페이지 → 백엔드 호출로 다시 감싸는 별도 구조가 필요하다.

선택하지 않은 이유:
구조 비용이 큰 변경이며, 보안 위험은 state 의 단기 수명 + owner memberId binding 으로 이미 흡수된다 (state 가 1회용, 만료 짧음).

## Consequences

### Positive

- 모든 figma admin endpoint 가 SUPER_ADMIN 외 호출에 대해 `RESOURCE_ACCESS_DENIED` 로 거부되어, Discord webhook URL / 멘션 / OAuth owner / Figma rate limit 의 무단 변경 위험이 사라진다.
- 인가 실패가 `AccessControlAspect` 의 로그로 남아 감사 추적이 가능해진다.
- 프로젝트의 다른 도메인과 동일한 `@CheckAccess` 컨벤션을 따르므로, 이후 권한 정책을 바꿀 때 한 곳(evaluator) 에서 처리 가능하다.

### Negative

- SUPER_ADMIN 1 인이 부재 시, Figma 통합 운영(파일 등록, mention 변경, 즉시 sync 등) 이 즉시 막힌다. 권한 보유자 수가 1 이라는 가정이 운영 SPOF 가 된다.
- 신규 `ResourceType.FIGMA` 가 enum 에 추가되며, 이 enum 을 사용하는 모든 코드/문서를 향후 수정 시 함께 고려해야 한다.
- 컨트롤러마다 `@CheckAccess` 어노테이션이 늘어나 메서드 시그니처 위 코드 라인이 길어진다.

### Neutral / Trade-offs

- `READ` / `MANAGE` 두 권한을 분리해 두지만 evaluator 에서는 둘 다 SUPER_ADMIN 으로 귀결된다. 지금은 사실상 단일 권한과 동일하게 동작하지만, 추후 권한 분리 시점에 evaluator 만 고치면 된다 (어노테이션은 그대로 유지).
- `FigmaOAuthController#callback` 만 `@Public` 으로 남는 비대칭이 생긴다. 다만 callback 자체는 state 검증으로 안전하므로, 이 비대칭은 의도된 설계다.

## Implementation Notes

### ResourceType enum 추가

`com.umc.product.authorization.domain.ResourceType` 에 다음을 추가한다.

```java
// Figma 통합 admin
FIGMA("figma", "Figma 통합", Set.of(PermissionType.READ, PermissionType.MANAGE)),
```

### FigmaPermissionEvaluator 추가

위치: `com.umc.product.figma.application.service.evaluator.FigmaPermissionEvaluator`.

```java
@Component
public class FigmaPermissionEvaluator implements ResourcePermissionEvaluator {

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.FIGMA;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        return switch (resourcePermission.permission()) {
            case READ, MANAGE -> isSuperAdmin(subjectAttributes);
            default -> false;
        };
    }

    private boolean isSuperAdmin(SubjectAttributes subjectAttributes) {
        return subjectAttributes.roleAttributes().stream()
            .anyMatch(role -> role.roleType().isSuperAdmin());
    }
}
```

`@Component` 등록만으로 `AuthorizationService` 가 자동으로 수집한다. 별도 빈 등록 코드 없음.

### 컨트롤러 어노테이션 적용 가이드

| 컨트롤러 | endpoint | 어노테이션 |
|---|---|---|
| `FigmaRoutingDomainController` | `POST /` | `@CheckAccess(resourceType=FIGMA, permission=MANAGE)` |
| `FigmaRoutingDomainController` | `DELETE /{domainId}` | `@CheckAccess(... MANAGE)` |
| `FigmaRoutingDomainController` | `POST /{domainId}/mentions` | `@CheckAccess(... MANAGE)` |
| `FigmaRoutingDomainController` | `DELETE /mentions/{mentionId}` | `@CheckAccess(... MANAGE)` |
| `FigmaRoutingDomainController` | `GET /` | `@CheckAccess(... READ)` |
| `FigmaRoutingDomainController` | `GET /{domainId}` | `@CheckAccess(... READ)` |
| `FigmaRoutingDomainController` | `GET /{domainId}/mentions` | `@CheckAccess(... READ)` |
| `FigmaWatchedFileController` | `POST /` | `@CheckAccess(... MANAGE)` |
| `FigmaWatchedFileController` | `DELETE /{watchedFileId}` | `@CheckAccess(... MANAGE)` |
| `FigmaWatchedFileController` | `POST /{watchedFileId}/enable` | `@CheckAccess(... MANAGE)` |
| `FigmaWatchedFileController` | `GET /` | `@CheckAccess(... READ)` |
| `FigmaWatchedFileController` | `GET /{watchedFileId}` | `@CheckAccess(... READ)` |
| `FigmaSyncController` | `POST /` | `@CheckAccess(... MANAGE)` |
| `FigmaSyncController` | `POST /watched-files/{watchedFileId}` | `@CheckAccess(... MANAGE)` |
| `FigmaSyncController` | `GET /watched-files/{watchedFileId}/preview` | `@CheckAccess(... READ)` |
| `FigmaDigestController` | `POST /` | `@CheckAccess(... MANAGE)` |
| `FigmaOAuthController` | `GET /` (start) | `@CheckAccess(... MANAGE)` |
| `FigmaOAuthController` | `GET /callback` | (그대로 `@Public` 유지) |

### 주의 사항

- `AccessControlAspect.extractMemberId()` 는 SecurityContext 에 인증이 없으면 `AccessDeniedException` 을 던진다. 즉 `@CheckAccess` 메서드는 인증 필수다.
- `FigmaOAuthController#callback` 에 절대 `@CheckAccess` 를 붙이면 안 된다. Figma 의 redirect 가 Authorization 헤더 없이 들어오므로 호출이 100% 실패한다. PR 리뷰 체크리스트 항목으로 추가한다.
- evaluator 가 추가되면 `AuthorizationService` 의 시작 로그(`등록된 ResourcePermissionEvaluator: ...`) 에 `FIGMA` 가 함께 보여야 한다. 부팅 후 로그로 등록 여부를 검증할 수 있다.

### Implementation Plan (Commit 단위)

각 커밋은 단독 빌드/테스트 통과. Conventional Commits 규칙(`<type>: <subject>`) 준수.

1. `feat: Figma admin API 인가용 ResourceType.FIGMA + FigmaPermissionEvaluator 추가`
   - `ResourceType.FIGMA` enum 추가 (READ + MANAGE).
   - `FigmaPermissionEvaluator` 신규 (SUPER_ADMIN 만 통과).
   - 컨트롤러에는 아직 어노테이션을 붙이지 않음 (정책 도입과 적용 분리).
2. `feat: figma admin controllers 에 @CheckAccess 일괄 적용`
   - `FigmaRoutingDomainController`, `FigmaWatchedFileController`, `FigmaSyncController`, `FigmaDigestController`, `FigmaOAuthController#start` 의 모든 endpoint 에 어노테이션 추가.
   - `FigmaOAuthController#callback` 은 `@Public` 유지 (변경 없음).
3. `test: FigmaPermissionEvaluator 단위 테스트 추가`
   - SUPER_ADMIN 이면 READ/MANAGE 모두 true.
   - 그 외 역할 (CENTRAL_PRESIDENT, SCHOOL_PRESIDENT 등) 이면 false.
   - 지원하지 않는 권한(예: WRITE) 이 들어오면 false.
4. `docs: ADR-007 status 를 Accepted 로 갱신`
   - 본 ADR Status 변경.

## References

- 관련 ADR
    - [ADR-003: Figma 댓글 Discord 포워딩](003-figma-comment-discord-forwarder.md)
    - [ADR-005: Figma 라우팅/Watched File Query API 도입](005-figma-routing-and-watched-file-query-apis.md)
- 인가 인프라
    - [CheckAccess](../../src/main/java/com/umc/product/authorization/adapter/in/aspect/CheckAccess.java)
    - [AccessControlAspect](../../src/main/java/com/umc/product/authorization/adapter/in/aspect/AccessControlAspect.java)
    - [ResourcePermissionEvaluator](../../src/main/java/com/umc/product/authorization/application/port/out/ResourcePermissionEvaluator.java)
    - [AuthorizationService](../../src/main/java/com/umc/product/authorization/application/service/AuthorizationService.java)
- 참고할 evaluator (SUPER_ADMIN 단일 기준 패턴)
    - [TermPermissionEvaluator](../../src/main/java/com/umc/product/term/application/service/evaluator/TermPermissionEvaluator.java)
