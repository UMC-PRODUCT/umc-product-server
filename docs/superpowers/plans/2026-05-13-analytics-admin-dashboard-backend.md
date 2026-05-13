# Analytics Admin Dashboard Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 운영진 메인 대시보드 MVP를 위해 `analytics` 조회 도메인과 `/api/v1/admin/...` 대시보드 API를 추가한다.

**Architecture:** `analytics`는 쓰기 모델이 아니라 운영진용 read model 도메인으로 둔다. Controller는 UseCase만 호출하고, QueryService는 권한 스코프를 확정한 뒤 analytics 전용 Port를 통해 DTO projection만 조회한다. JPA Entity를 응답하거나 다른 도메인 Repository를 application/service에서 직접 주입하지 않는다.

**Tech Stack:** Java 21, Spring Boot 3.5, JPA, QueryDSL, PostgreSQL, Flyway, JUnit 5, Mockito, Testcontainers

---

## 기준 문서와 현재 코드 차이

- 기준 기획안: `docs/backlog/운영진_대시보드_기획안.md`
- 실제 서버 URL prefix는 기존 Controller 기준 `/api/v1`이다. 기획안의 `/v1/admin/...`은 서버 코드에서는 `/api/v1/admin/...`로 구현한다.
- 기획안의 `application.status = PENDING`은 현재 BE 모델에서는 `ProjectApplicationStatus.SUBMITTED`가 처리 대기 상태다.
- 기획안의 `project.status IN (OPEN, IN_PROGRESS)` 중 현재 BE에는 `OPEN`이 없다. MVP의 진행 중 프로젝트는 `ProjectStatus.IN_PROGRESS`로 계산한다.
- 하위 호환성을 위해 기존 API인 `GET /api/v1/challenger/search/offset`와 `GET /api/v1/member/me`는 변경하지 않는다.
- 위험군 챌린저 조회와 운영진 대시보드 권한 컨텍스트는 신규 `analytics` 도메인의 admin dashboard API로 제공한다.

## API 확정안

### MVP P0

1. `GET /api/v1/admin/dashboard/summary`
   - Query: `gisuId?`, `chapterId?`, `schoolId?`
   - Response: `AdminDashboardSummaryResponse`
   - 포함: active challenger count, weekly new member count and delta, active school/chapter count, monthly point positive/negative sum, in-progress project count, submitted project application count, challenger status distribution.

2. `GET /api/v1/admin/schools/summary`
   - Query: `gisuId`, `chapterId?`, `search?`, `riskThreshold?`, `page?`, `size?`, `sort?`
   - Response: `PageResponse<AdminSchoolSummaryResponse>`
   - 기본 정렬: `riskChallengerCount,desc`

3. `GET /api/v1/admin/dashboard/action-queue`
   - Query: `gisuId?`, `riskThreshold?`
   - Response: `AdminDashboardActionQueueResponse`
   - 포함: submitted project application count, open matching round count, unsent notice count, weekly new risk challenger count, upcoming graduation challenger count.

4. `GET /api/v1/admin/dashboard/risk-challengers`
   - Query: `gisuId?`, `chapterId?`, `schoolId?`, `riskThreshold?`, `page?`, `size?`, `sort?`
   - Response: `PageResponse<AdminRiskChallengerResponse>`
   - 포함: name, schoolName, part, pointSum, latestNegativePoint
   - 기존 `GET /api/v1/challenger/search/offset`는 변경하지 않는다.

### P1

1. `GET /api/v1/admin/dashboard/context`
   - Response: `AdminDashboardContextResponse`
   - `roleType`, `gisuId`, `chapterId`, `schoolId`, `responsiblePart`, `scopeType` 포함
   - 기존 `GET /api/v1/member/me`는 변경하지 않는다.

2. `GET /api/v1/admin/stats/signups`
   - Query: `from`, `to`, `groupBy=DAY|WEEK`, `gisuId?`, `chapterId?`, `schoolId?`, `part?`
   - Response: bucket list

3. `GET /api/v1/admin/stats/points/daily`
   - Query: `from`, `to`, `gisuId?`, `chapterId?`, `schoolId?`
   - Response: daily positive/negative point buckets

### P2

1. `GET /api/v1/admin/stats/points/by-type`
2. `GET /api/v1/admin/parts/summary`

## 권한과 스코프 정책

스코프 위반은 자동 덮어쓰기보다 `403`을 반환한다. 단, 요청 필터가 비어 있으면 서버가 사용자 권한 스코프를 자동 적용한다.

- `SUPER_ADMIN`, `CENTRAL_*`: 요청한 `chapterId`, `schoolId`, `part` 필터 허용
- `CHAPTER_PRESIDENT`: 본인 `chapterId` 밖 요청은 `403`
- `SCHOOL_PRESIDENT`, `SCHOOL_VICE_PRESIDENT`, `SCHOOL_ETC_ADMIN`: 본인 `schoolId` 밖 요청은 `403`
- `SCHOOL_PART_LEADER`: 본인 `schoolId`와 `responsiblePart` 밖 요청은 `403`
- 일반 챌린저 또는 역할 없음: dashboard API 전체 `403`

동일 사용자가 여러 역할을 가지면 요청한 `gisuId` 안에서 가장 넓은 권한을 우선한다.

우선순위:
`SUPER_ADMIN` > `CENTRAL_*` > `CHAPTER_PRESIDENT` > `SCHOOL_*` > `SCHOOL_PART_LEADER`

## 패키지 구성

Create:

- `src/main/java/com/umc/product/analytics/domain/AdminAnalyticsScope.java`
- `src/main/java/com/umc/product/analytics/domain/AdminAnalyticsScopeType.java`
- `src/main/java/com/umc/product/analytics/domain/AdminAnalyticsSort.java`
- `src/main/java/com/umc/product/analytics/domain/AnalyticsErrorCode.java`
- `src/main/java/com/umc/product/analytics/domain/AnalyticsDomainException.java`
- `src/main/java/com/umc/product/analytics/application/port/in/query/GetAdminDashboardSummaryUseCase.java`
- `src/main/java/com/umc/product/analytics/application/port/in/query/GetAdminSchoolSummaryUseCase.java`
- `src/main/java/com/umc/product/analytics/application/port/in/query/GetAdminDashboardActionQueueUseCase.java`
- `src/main/java/com/umc/product/analytics/application/port/in/query/GetAdminDashboardContextUseCase.java`
- `src/main/java/com/umc/product/analytics/application/port/in/query/GetAdminRiskChallengerUseCase.java`
- `src/main/java/com/umc/product/analytics/application/port/in/query/dto/AdminDashboardSummaryInfo.java`
- `src/main/java/com/umc/product/analytics/application/port/in/query/dto/AdminDashboardActionQueueInfo.java`
- `src/main/java/com/umc/product/analytics/application/port/in/query/dto/AdminDashboardContextInfo.java`
- `src/main/java/com/umc/product/analytics/application/port/in/query/dto/AdminRiskChallengerInfo.java`
- `src/main/java/com/umc/product/analytics/application/port/in/query/dto/AdminSchoolSummaryInfo.java`
- `src/main/java/com/umc/product/analytics/application/port/in/query/dto/AdminDashboardQuery.java`
- `src/main/java/com/umc/product/analytics/application/port/in/query/dto/AdminRiskChallengerQuery.java`
- `src/main/java/com/umc/product/analytics/application/port/in/query/dto/AdminSchoolSummaryQuery.java`
- `src/main/java/com/umc/product/analytics/application/port/out/LoadAdminDashboardAnalyticsPort.java`
- `src/main/java/com/umc/product/analytics/application/port/out/LoadAdminSchoolAnalyticsPort.java`
- `src/main/java/com/umc/product/analytics/application/port/out/LoadAdminRiskChallengerAnalyticsPort.java`
- `src/main/java/com/umc/product/analytics/application/service/query/AdminAnalyticsQueryService.java`
- `src/main/java/com/umc/product/analytics/application/service/query/AdminAnalyticsScopeResolver.java`
- `src/main/java/com/umc/product/analytics/application/service/evaluator/AdminAnalyticsPermissionEvaluator.java`
- `src/main/java/com/umc/product/analytics/adapter/in/web/AdminDashboardController.java`
- `src/main/java/com/umc/product/analytics/adapter/in/web/AdminSchoolAnalyticsController.java`
- `src/main/java/com/umc/product/analytics/adapter/in/web/dto/request/AdminDashboardRequest.java`
- `src/main/java/com/umc/product/analytics/adapter/in/web/dto/request/AdminRiskChallengerRequest.java`
- `src/main/java/com/umc/product/analytics/adapter/in/web/dto/request/AdminSchoolSummaryRequest.java`
- `src/main/java/com/umc/product/analytics/adapter/in/web/dto/response/AdminDashboardSummaryResponse.java`
- `src/main/java/com/umc/product/analytics/adapter/in/web/dto/response/AdminDashboardActionQueueResponse.java`
- `src/main/java/com/umc/product/analytics/adapter/in/web/dto/response/AdminDashboardContextResponse.java`
- `src/main/java/com/umc/product/analytics/adapter/in/web/dto/response/AdminRiskChallengerResponse.java`
- `src/main/java/com/umc/product/analytics/adapter/in/web/dto/response/AdminSchoolSummaryResponse.java`
- `src/main/java/com/umc/product/analytics/adapter/out/persistence/AdminDashboardAnalyticsPersistenceAdapter.java`
- `src/main/java/com/umc/product/analytics/adapter/out/persistence/AdminDashboardAnalyticsQueryRepository.java`
- `src/main/java/com/umc/product/analytics/adapter/out/persistence/AdminSchoolAnalyticsPersistenceAdapter.java`
- `src/main/java/com/umc/product/analytics/adapter/out/persistence/AdminSchoolAnalyticsQueryRepository.java`
- `src/main/java/com/umc/product/analytics/adapter/out/persistence/AdminRiskChallengerAnalyticsPersistenceAdapter.java`
- `src/main/java/com/umc/product/analytics/adapter/out/persistence/AdminRiskChallengerAnalyticsQueryRepository.java`
- `src/main/java/com/umc/product/analytics/adapter/out/persistence/row/AdminDashboardSummaryRow.java`
- `src/main/java/com/umc/product/analytics/adapter/out/persistence/row/AdminDashboardActionQueueRow.java`
- `src/main/java/com/umc/product/analytics/adapter/out/persistence/row/AdminRiskChallengerRow.java`
- `src/main/java/com/umc/product/analytics/adapter/out/persistence/row/AdminSchoolSummaryRow.java`

Modify:

- `src/main/java/com/umc/product/authorization/domain/ResourceType.java`
- `src/main/java/com/umc/product/challenger/application/service/ChallengerSearchService.java`

Do not modify:

- `src/main/java/com/umc/product/challenger/adapter/in/web/dto/request/SearchChallengerRequest.java`
- `src/main/java/com/umc/product/challenger/application/port/in/query/dto/SearchChallengerQuery.java`
- `src/main/java/com/umc/product/challenger/adapter/in/web/dto/response/SearchChallengerResponse.java`
- `src/main/java/com/umc/product/member/adapter/in/web/dto/response/MemberInfoResponse.java`
- `src/main/java/com/umc/product/member/adapter/in/web/assembler/MemberInfoResponseAssembler.java`

Test:

- `src/test/java/com/umc/product/analytics/application/service/query/AdminAnalyticsScopeResolverTest.java`
- `src/test/java/com/umc/product/analytics/application/service/query/AdminAnalyticsQueryServiceTest.java`
- `src/test/java/com/umc/product/analytics/adapter/out/persistence/AdminDashboardAnalyticsQueryRepositoryTest.java`
- `src/test/java/com/umc/product/analytics/adapter/out/persistence/AdminSchoolAnalyticsQueryRepositoryTest.java`
- `src/test/java/com/umc/product/analytics/adapter/out/persistence/AdminRiskChallengerAnalyticsQueryRepositoryTest.java`
- `src/test/java/com/umc/product/analytics/adapter/in/web/AdminDashboardControllerTest.java`

## 핵심 DTO 형태

```java
public record AdminDashboardQuery(
    Long requesterMemberId,
    Long gisuId,
    Long chapterId,
    Long schoolId
) {
    public static AdminDashboardQuery of(Long requesterMemberId, Long gisuId, Long chapterId, Long schoolId) {
        return new AdminDashboardQuery(requesterMemberId, gisuId, chapterId, schoolId);
    }
}
```

```java
public record AdminAnalyticsScope(
    AdminAnalyticsScopeType type,
    Long gisuId,
    Long chapterId,
    Long schoolId,
    ChallengerPart responsiblePart,
    ChallengerRoleType roleType
) {
    public boolean isCentralScope() {
        return type == AdminAnalyticsScopeType.CENTRAL;
    }
}
```

```java
public enum AdminAnalyticsScopeType {
    CENTRAL,
    CHAPTER,
    SCHOOL,
    SCHOOL_PART
}
```

```java
public record AdminDashboardSummaryInfo(
    long activeChallengerCount,
    long newMemberCountThisWeek,
    double newMemberDeltaPercent,
    long activeSchoolCount,
    long activeChapterCount,
    PointSumInfo monthlyPointSum,
    long projectInProgressCount,
    long pendingApplicationCount,
    Map<ChallengerStatus, Long> challengerStatusDistribution
) {
    public record PointSumInfo(long positive, long negative) {
        public static PointSumInfo of(long positive, long negative) {
            return new PointSumInfo(positive, negative);
        }
    }
}
```

```java
public record AdminSchoolSummaryInfo(
    Long schoolId,
    String schoolName,
    Long chapterId,
    String chapterName,
    long activeChallengerCount,
    StaffInfo president,
    StaffInfo vicePresident,
    PartLeaderRatioInfo partLeaderRatio,
    double averagePointSum,
    long riskChallengerCount,
    long newMemberCountThisWeek
) {
    public record StaffInfo(Long challengerId, String name) {
        public static StaffInfo of(Long challengerId, String name) {
            return new StaffInfo(challengerId, name);
        }
    }

    public record PartLeaderRatioInfo(long assigned, long totalRunningParts) {
        public static PartLeaderRatioInfo of(long assigned, long totalRunningParts) {
            return new PartLeaderRatioInfo(assigned, totalRunningParts);
        }
    }
}
```

## Task 1: Analytics 권한 리소스와 스코프 Resolver

**Files:**

- Create: `src/main/java/com/umc/product/analytics/domain/AdminAnalyticsScope.java`
- Create: `src/main/java/com/umc/product/analytics/domain/AdminAnalyticsScopeType.java`
- Create: `src/main/java/com/umc/product/analytics/application/service/query/AdminAnalyticsScopeResolver.java`
- Create: `src/main/java/com/umc/product/analytics/application/service/evaluator/AdminAnalyticsPermissionEvaluator.java`
- Modify: `src/main/java/com/umc/product/authorization/domain/ResourceType.java`
- Test: `src/test/java/com/umc/product/analytics/application/service/query/AdminAnalyticsScopeResolverTest.java`

- [ ] **Step 1: ResourceType에 ANALYTICS 추가**

```java
ANALYTICS("analytics", "운영진 대시보드", Set.of(PermissionType.READ)),
```

- [ ] **Step 2: AdminAnalyticsPermissionEvaluator 작성**

```java
@Component
public class AdminAnalyticsPermissionEvaluator implements ResourcePermissionEvaluator {
    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.ANALYTICS;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {
        if (resourcePermission.permission() != PermissionType.READ) {
            return false;
        }
        return subjectAttributes.roleAttributes().stream()
            .map(RoleAttribute::roleType)
            .anyMatch(roleType -> roleType.isSuperAdmin()
                || roleType.isAtLeastCentralMember()
                || roleType == ChallengerRoleType.CHAPTER_PRESIDENT
                || roleType.isAtLeastSchoolAdmin());
    }
}
```

- [ ] **Step 3: ScopeResolver의 결정 규칙 구현**

`AdminAnalyticsScopeResolver.resolve(memberId, requestedGisuId, requestedChapterId, requestedSchoolId, requestedPart)`는 다음 순서로 동작한다.

1. `requestedGisuId`가 없으면 기존 `GetGisuUseCase.getActiveGisu()` 또는 `GetGisuUseCase.getActiveGisuId()`로 활성 기수를 선택한다.
2. `GetChallengerRoleUseCase.findAllByMemberId(memberId)`로 해당 기수의 역할을 필터링한다.
3. 최고 권한을 고른다.
4. 요청 필터가 권한 밖이면 `AnalyticsDomainException(RESOURCE_ACCESS_DENIED)`를 던진다.
5. 필터가 비어 있으면 권한 스코프 값을 채운다.

- [ ] **Step 4: ScopeResolver 단위 테스트 작성**

테스트 이름:

- `중앙_운영진은_요청한_지부와_학교_필터를_그대로_사용한다`
- `지부장은_다른_지부를_요청하면_거부된다`
- `학교_운영진은_학교_필터가_없어도_본인_학교로_스코프가_고정된다`
- `학교_파트장은_담당_파트까지_스코프에_포함된다`
- `운영진_역할이_없으면_대시보드_접근이_거부된다`

Run:

```bash
./gradlew test --tests "com.umc.product.analytics.application.service.query.AdminAnalyticsScopeResolverTest"
```

Expected: 신규 테스트 PASS

## Task 2: Dashboard Summary와 Action Queue API

**Files:**

- Create: `src/main/java/com/umc/product/analytics/application/port/in/query/GetAdminDashboardSummaryUseCase.java`
- Create: `src/main/java/com/umc/product/analytics/application/port/in/query/GetAdminDashboardActionQueueUseCase.java`
- Create: `src/main/java/com/umc/product/analytics/application/port/in/query/dto/AdminDashboardSummaryInfo.java`
- Create: `src/main/java/com/umc/product/analytics/application/port/in/query/dto/AdminDashboardActionQueueInfo.java`
- Create: `src/main/java/com/umc/product/analytics/application/port/out/LoadAdminDashboardAnalyticsPort.java`
- Create: `src/main/java/com/umc/product/analytics/application/service/query/AdminAnalyticsQueryService.java`
- Create: `src/main/java/com/umc/product/analytics/adapter/out/persistence/AdminDashboardAnalyticsPersistenceAdapter.java`
- Create: `src/main/java/com/umc/product/analytics/adapter/out/persistence/AdminDashboardAnalyticsQueryRepository.java`
- Create: `src/main/java/com/umc/product/analytics/adapter/in/web/AdminDashboardController.java`
- Test: `src/test/java/com/umc/product/analytics/application/service/query/AdminAnalyticsQueryServiceTest.java`
- Test: `src/test/java/com/umc/product/analytics/adapter/out/persistence/AdminDashboardAnalyticsQueryRepositoryTest.java`

- [ ] **Step 1: UseCase 인터페이스 작성**

```java
public interface GetAdminDashboardSummaryUseCase {
    AdminDashboardSummaryInfo getSummary(AdminDashboardQuery query);
}

public interface GetAdminDashboardActionQueueUseCase {
    AdminDashboardActionQueueInfo getActionQueue(AdminDashboardActionQueueQuery query);
}
```

- [ ] **Step 2: QueryService 구현**

`AdminAnalyticsQueryService`는 `@Transactional(readOnly = true)`를 클래스에 붙이고 두 UseCase를 구현한다. 메서드 내부는 `scopeResolver.resolve(...)` 후 `loadAdminDashboardAnalyticsPort.getSummary(scope)` 또는 `getActionQueue(scope, riskThreshold)`만 호출한다.

- [ ] **Step 3: Summary 집계 쿼리 구현**

`AdminDashboardAnalyticsQueryRepository`는 Entity fetch 없이 projection만 조회한다.

집계 기준:

- 활동 챌린저: `challenger.status = ACTIVE`
- 신규 회원 이번 주: `member.created_at >= weekStart`
- 전주 대비: `prevWeekCount == 0`이면 이번 주도 0일 때 `0.0`, 이번 주가 1 이상이면 `100.0`
- 월간 포인트: `challenger_point.created_at >= monthStart`
- 가산/감점: `point_value`가 있으면 그 값을 우선하고 없으면 `PointType.getValue()`
- 진행 프로젝트: `project.status = IN_PROGRESS`
- 처리 대기 지원서: `project_application.status = SUBMITTED`
- 상태 분포: `ACTIVE`, `GRADUATED`, `EXPELLED`, `WITHDRAWN` 모두 키를 포함하고 값이 없으면 0

- [ ] **Step 4: Action Queue 집계 쿼리 구현**

집계 기준:

- `pendingApplicationCount`: `ProjectApplicationStatus.SUBMITTED`
- `inProgressMatchingCount`: `startsAt <= now <= endsAt`
- `unsentNoticeCount`: `shouldSendNotification = true and notifiedAt is null`
- `newRiskMemberCountThisWeek`: `member.createdAt >= weekStart and pointSum <= riskThreshold`
- `upcomingGraduationCount`: `gisu.endAt <= now + 14 days`이면 해당 스코프의 active challenger count, 아니면 0

- [ ] **Step 5: Controller 작성**

```java
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard | 운영진 대시보드", description = "운영진 메인 대시보드 집계 API")
public class AdminDashboardController {
    private final GetAdminDashboardSummaryUseCase getAdminDashboardSummaryUseCase;
    private final GetAdminDashboardActionQueueUseCase getAdminDashboardActionQueueUseCase;

    @GetMapping("summary")
    @CheckAccess(resourceType = ResourceType.ANALYTICS, permission = PermissionType.READ)
    AdminDashboardSummaryResponse getSummary(
        @CurrentMember MemberPrincipal memberPrincipal,
        @ParameterObject AdminDashboardRequest request
    ) {
        return AdminDashboardSummaryResponse.from(
            getAdminDashboardSummaryUseCase.getSummary(request.toQuery(memberPrincipal.getMemberId()))
        );
    }
}
```

- [ ] **Step 6: 테스트 작성**

테스트 이름:

- `summary_중앙_운영진은_전체_스코프의_KPI를_조회한다`
- `summary_학교_운영진은_본인_학교_데이터만_조회한다`
- `actionQueue_SUBMITTED_지원서를_처리_대기로_계산한다`
- `actionQueue_알림_요청됐지만_notifiedAt이_null인_공지수를_계산한다`

Run:

```bash
./gradlew test --tests "com.umc.product.analytics.application.service.query.AdminAnalyticsQueryServiceTest" --tests "com.umc.product.analytics.adapter.out.persistence.AdminDashboardAnalyticsQueryRepositoryTest"
```

Expected: 신규 테스트 PASS

## Task 3: 학교/지부별 현황 테이블 API

**Files:**

- Create: `src/main/java/com/umc/product/analytics/application/port/in/query/GetAdminSchoolSummaryUseCase.java`
- Create: `src/main/java/com/umc/product/analytics/application/port/in/query/dto/AdminSchoolSummaryQuery.java`
- Create: `src/main/java/com/umc/product/analytics/application/port/in/query/dto/AdminSchoolSummaryInfo.java`
- Create: `src/main/java/com/umc/product/analytics/application/port/out/LoadAdminSchoolAnalyticsPort.java`
- Create: `src/main/java/com/umc/product/analytics/adapter/out/persistence/AdminSchoolAnalyticsPersistenceAdapter.java`
- Create: `src/main/java/com/umc/product/analytics/adapter/out/persistence/AdminSchoolAnalyticsQueryRepository.java`
- Create: `src/main/java/com/umc/product/analytics/adapter/in/web/AdminSchoolAnalyticsController.java`
- Test: `src/test/java/com/umc/product/analytics/adapter/out/persistence/AdminSchoolAnalyticsQueryRepositoryTest.java`

- [ ] **Step 1: API 요청 DTO 작성**

`riskThreshold` 기본값은 `-8`이다. `sort`가 없으면 `riskChallengerCount,desc`를 적용한다.

허용 정렬:

- `riskChallengerCount,desc`
- `activeChallengerCount,desc`
- `schoolName,asc`
- `averagePointSum,asc`
- `averagePointSum,desc`

- [ ] **Step 2: 집계 쿼리 구현**

한 학교 행은 다음 CTE 또는 QueryDSL equivalent로 계산한다.

1. 대상 챌린저: `gisuId`, `scope`, `ACTIVE` 기준으로 challenger, member, school, chapter를 조인한다.
2. 챌린저별 포인트 합계: `challenger_point`를 `challenger_id`로 group by 한다.
3. 학교별 집계: active count, average point, risk count, weekly new count를 group by 한다.
4. 회장과 부회장: `challenger_role.role_type in (SCHOOL_PRESIDENT, SCHOOL_VICE_PRESIDENT)`를 학교별로 left join 한다.
5. 파트장 배치율: `SCHOOL_PART_LEADER`의 distinct responsiblePart 수를 `assigned`, 학교 내 active challenger distinct part 수를 `totalRunningParts`로 계산한다.

- [ ] **Step 3: Controller 작성**

```java
@RestController
@RequestMapping("/api/v1/admin/schools")
@RequiredArgsConstructor
public class AdminSchoolAnalyticsController {
    private final GetAdminSchoolSummaryUseCase getAdminSchoolSummaryUseCase;

    @GetMapping("summary")
    @CheckAccess(resourceType = ResourceType.ANALYTICS, permission = PermissionType.READ)
    PageResponse<AdminSchoolSummaryResponse> getSchoolSummaries(
        @CurrentMember MemberPrincipal memberPrincipal,
        @ParameterObject Pageable pageable,
        @ParameterObject AdminSchoolSummaryRequest request
    ) {
        return PageResponse.of(
            getAdminSchoolSummaryUseCase.getSchoolSummaries(request.toQuery(memberPrincipal.getMemberId(), pageable)),
            AdminSchoolSummaryResponse::from
        );
    }
}
```

- [ ] **Step 4: 테스트 작성**

테스트 이름:

- `학교_요약은_위험군_수_내림차순이_기본이다`
- `지부_스코프에서는_해당_지부의_학교만_조회된다`
- `검색어가_있으면_학교명_부분_일치만_조회된다`
- `파트장_배치율은_배치된_파트수와_운영중인_파트수로_계산된다`
- `포인트가_없는_챌린저는_0점으로_평균에_포함된다`

Run:

```bash
./gradlew test --tests "com.umc.product.analytics.adapter.out.persistence.AdminSchoolAnalyticsQueryRepositoryTest"
```

Expected: 신규 테스트 PASS

## Task 4: 위험군 챌린저 검색 확장

**Files:**

- Modify: `src/main/java/com/umc/product/challenger/adapter/in/web/dto/request/SearchChallengerRequest.java`
- Modify: `src/main/java/com/umc/product/challenger/application/port/in/query/dto/SearchChallengerQuery.java`
- Modify: `src/main/java/com/umc/product/challenger/application/port/in/query/dto/SearchChallengerItemInfo.java`
- Modify: `src/main/java/com/umc/product/challenger/application/port/out/SearchChallengerPort.java`
- Modify: `src/main/java/com/umc/product/challenger/adapter/out/persistence/ChallengerQueryRepository.java`
- Modify: `src/main/java/com/umc/product/challenger/application/service/ChallengerSearchService.java`
- Modify: `src/main/java/com/umc/product/challenger/adapter/in/web/dto/response/SearchChallengerResponse.java`
- Test: `src/test/java/com/umc/product/challenger/adapter/out/persistence/ChallengerQueryRepositoryTest.java`
- Test: `src/test/java/com/umc/product/challenger/application/service/ChallengerSearchServiceTest.java`

- [ ] **Step 1: Query DTO 확장**

```java
public record SearchChallengerQuery(
    Long challengerId,
    String name,
    String nickname,
    String keyword,
    Long schoolId,
    Long chapterId,
    ChallengerPart part,
    Long gisuId,
    List<ChallengerStatus> statuses,
    Double pointSumLte,
    Double pointSumGte,
    ChallengerRoleType roleType,
    boolean includeLatestNegativePoint
) {
}
```

- [ ] **Step 2: Request DTO 확장**

`status` 단건과 `statuses` 복수 요청을 모두 허용한다. 둘 다 없으면 기존 호환성을 위해 `List.of(ChallengerStatus.ACTIVE)`를 유지한다.

- [ ] **Step 3: QueryRepository 확장**

포인트 조건은 page content 조회 전에 적용되어야 한다. `challenger_point` 합계 subquery를 사용해 `pointSumLte`, `pointSumGte`를 DB에서 필터링한다.

정렬 규칙:

- `pointSum,asc`: 포인트 합계 오름차순
- `pointSum,desc`: 포인트 합계 내림차순
- `createdAt,desc`: 챌린저 생성일 내림차순
- sort 미지정: 기존 `partOrder asc, gisuId desc, member.name asc`

- [ ] **Step 4: latestNegativePoint 조회 추가**

`includeLatestNegativePoint=true`일 때만 현재 페이지의 challengerId set을 기준으로 최신 감점 포인트를 한 번에 조회한다.

감점 판정:

```java
pointValue < 0
```

응답 형태:

```java
public record LatestNegativePointInfo(
    PointType pointType,
    Instant createdAt,
    double score
) {
    public static LatestNegativePointInfo of(PointType pointType, Instant createdAt, double score) {
        return new LatestNegativePointInfo(pointType, createdAt, score);
    }
}
```

- [ ] **Step 5: 테스트 작성**

테스트 이름:

- `pointSumLte가_있으면_포인트_합계가_임계치_이하인_챌린저만_조회한다`
- `pointSum_오름차순_정렬은_위험군이_먼저_나온다`
- `includeLatestNegativePoint가_false이면_최근_감점_조회는_호출하지_않는다`
- `includeLatestNegativePoint가_true이면_페이지_대상자의_최신_감점만_응답한다`
- `status를_지정하지_않으면_기존처럼_ACTIVE만_조회한다`

Run:

```bash
./gradlew test --tests "com.umc.product.challenger.adapter.out.persistence.ChallengerQueryRepositoryTest" --tests "com.umc.product.challenger.application.service.ChallengerSearchServiceTest"
```

Expected: 신규 테스트와 기존 challenger 검색 테스트 PASS

## Task 5: member/me 권한 컨텍스트 확장

**Files:**

- Modify: `src/main/java/com/umc/product/member/adapter/in/web/dto/response/MemberInfoResponse.java`
- Modify: `src/main/java/com/umc/product/member/adapter/in/web/assembler/MemberInfoResponseAssembler.java`
- Create: `src/main/java/com/umc/product/member/adapter/in/web/dto/response/PrimaryAdminScopeResponse.java`
- Test: `src/test/java/com/umc/product/member/adapter/in/web/assembler/MemberInfoResponseAssemblerTest.java`

- [ ] **Step 1: Response DTO 추가**

```java
public record PrimaryAdminScopeResponse(
    ChallengerRoleType roleType,
    Long gisuId,
    Long chapterId,
    Long schoolId,
    ChallengerPart responsiblePart,
    AdminAnalyticsScopeType scopeType
) {
    public static PrimaryAdminScopeResponse from(AdminAnalyticsScope scope) {
        return new PrimaryAdminScopeResponse(
            scope.roleType(),
            scope.gisuId(),
            scope.chapterId(),
            scope.schoolId(),
            scope.responsiblePart(),
            scope.type()
        );
    }
}
```

- [ ] **Step 2: MemberInfoResponse에 필드 추가**

기존 필드는 유지하고 마지막에 `PrimaryAdminScopeResponse primaryAdminScope`를 추가한다. 운영진 역할이 없으면 `null`을 반환한다.

- [ ] **Step 3: 테스트 작성**

테스트 이름:

- `me_응답은_기존_roles를_유지하면서_primaryAdminScope를_포함한다`
- `운영진_역할이_없으면_primaryAdminScope는_null이다`

Run:

```bash
./gradlew test --tests "com.umc.product.member.adapter.in.web.assembler.MemberInfoResponseAssemblerTest"
```

Expected: 신규 테스트 PASS

## Task 6: 문서와 RestDocs

**Files:**

- Create: `src/test/java/com/umc/product/analytics/adapter/in/web/AdminDashboardControllerTest.java`
- Create: `src/test/java/com/umc/product/analytics/adapter/in/web/AdminSchoolAnalyticsControllerTest.java`
- Modify: `docs/backlog/운영진_대시보드_기획안.md`

- [ ] **Step 1: Controller RestDocs 테스트 작성**

테스트 이름:

- `대시보드_summary_API_문서화`
- `대시보드_actionQueue_API_문서화`
- `학교별_summary_API_문서화`

- [ ] **Step 2: 기획안 BE 섹션 갱신**

반영 항목:

- 실제 prefix는 `/api/v1`
- 처리 대기 지원서는 `ProjectApplicationStatus.SUBMITTED`
- 진행 프로젝트는 `ProjectStatus.IN_PROGRESS`
- 스코프 위반 정책은 `403`
- 위험군 검색은 기존 challenger search 확장으로 제공

- [ ] **Step 3: 문서 빌드**

Run:

```bash
./gradlew asciidoctor
```

Expected: RestDocs snippets와 AsciiDoc 빌드 PASS

## 검증 명령

개별 작업 완료 후:

```bash
./gradlew test --tests "com.umc.product.analytics.*"
```

전체 회귀:

```bash
./gradlew compileJava
./gradlew compileTestJava
./gradlew test
```

문서:

```bash
./gradlew asciidoctor
```

## 완료 기준

- 운영진 권한이 없는 사용자는 모든 admin dashboard endpoint에서 `403`을 받는다.
- 중앙 운영진, 지부장, 학교 운영진, 학교 파트장이 같은 API를 호출해도 서버가 각자 권한 범위로 집계한다.
- 학교 요약 테이블은 N+1 없이 한 페이지를 단일 집계 쿼리로 반환한다.
- 위험군 챌린저 리스트는 FE가 전체 페이지를 순회하지 않아도 `pointSumLte`로 서버 필터링된다.
- 신규 API 응답은 Entity가 아니라 Response DTO만 반환한다.
- `./gradlew compileJava`가 통과한다.
- `./gradlew compileTestJava`가 통과한다.
- `./gradlew test`가 통과한다.
