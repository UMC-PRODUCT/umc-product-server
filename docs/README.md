# Authorization Domain

UMC Product 서버의 권한 관리 도메인입니다.

## 📚 Overview

Role 기반 권한 체크 시스템을 제공하며, `@CheckAccess` 어노테이션을 통해 선언적으로 메서드 실행 전 권한을 검증할 수 있습니다.

## 🏗️ Architecture

```
authorization/
├── domain/                          # 핵심 비즈니스 로직
│   ├── PermissionType.java         # 권한 타입 Enum
│   ├── ResourcePermission.java     # 리소스 권한 VO
│   └── exception/
│
├── application/                     # UseCase & Ports
│   ├── port/
│   │   ├── in/
│   │   │   └── CheckPermissionUseCase.java
│   │   └── out/
│   │       ├── LoadChallengerRolePort.java
│   │       └── EvaluatePolicyPort.java
│   └── service/
│       └── AuthorizationService.java
│
└── adapter/                         # Infrastructure
    ├── in/
    │   └── aspect/
    │       ├── CheckAccess.java            # 어노테이션
    │       └── AccessControlAspect.java    # AOP
    └── out/
        ├── persistence/
        │   └── ChallengerRoleAdapter.java
        └── policy/
            └── SimplePolicyEvaluator.java  # 단순 정책 평가기
```

## 🚀 사용법

### 1. 기본 사용 - Controller에서 권한 체크

```java
@RestController
@RequestMapping("/api/v1/curriculum")
@RequiredArgsConstructor
public class CurriculumController {

    private final SubmitWorkbookUseCase submitWorkbookUseCase;

    @PostMapping("/workbooks/{workbookId}/submit")
    @CheckAccess(
        resourceType = ResourceType.CURRICULUM,
        resourceId = "#workbookId",
        permission = PermissionType.WRITE
    )
    public ApiResponse<Void> submitWorkbook(
            @PathVariable Long workbookId,
            @Valid @RequestBody SubmitWorkbookRequest request) {

        submitWorkbookUseCase.submit(request.toCommand(workbookId));
        return ApiResponse.success();
    }
}
```

### 2. SpEL 표현식 활용

```java
// 파라미터 직접 참조
@CheckAccess(
    resourceType = ResourceType.SCHEDULE,
    resourceId = "#scheduleId",
    permission = PermissionType.APPROVE
)
public void approveAttendance(Long scheduleId, Long challengerId) {
    // ...
}

// Request DTO 필드 참조
@CheckAccess(
    resourceType = ResourceType.NOTICE,
    resourceId = "#request.noticeId",
    permission = PermissionType.DELETE
)
public void deleteNotice(@RequestBody DeleteNoticeRequest request) {
    // ...
}

// 리소스 타입 전체에 대한 권한 (resourceId 생략)
@CheckAccess(
    resourceType = ResourceType.CURRICULUM,
    resourceId = "",  // 또는 생략
    permission = PermissionType.MANAGE
)
public void createCurriculum(@RequestBody CreateCurriculumRequest request) {
    // ...
}
```

### 3. Service Layer에서 직접 호출

```java
@Service
@RequiredArgsConstructor
public class NoticeCommandService {

    private final CheckPermissionUseCase checkPermissionUseCase;

    public void updateNotice(Long memberId, Long noticeId, UpdateNoticeCommand command) {
        // 권한 체크 (권한 없으면 예외 발생)
        checkPermissionUseCase.checkOrThrow(
            memberId,
            ResourcePermission.of(ResourceType.NOTICE, noticeId, PermissionType.WRITE)
        );

        // 비즈니스 로직 수행
        // ...
    }
}
```

### 4. 조건부 권한 체크

```java
@Service
@RequiredArgsConstructor
public class PostQueryService {

    private final CheckPermissionUseCase checkPermissionUseCase;
    private final LoadPostPort loadPostPort;

    public PostDetailInfo getPostDetail(Long memberId, Long postId) {
        Post post = loadPostPort.findById(postId)
            .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 비공개 게시글은 작성자 또는 운영진만 조회 가능
        if (!post.isPublic()) {
            boolean isAuthor = post.getAuthorId().equals(memberId);
            boolean isStaff = checkPermissionUseCase.check(
                memberId,
                ResourcePermission.ofType(ResourceType.COMMUNITY, PermissionType.MANAGE)
            );

            if (!isAuthor && !isStaff) {
                throw new BusinessException(ErrorCode.POST_ACCESS_DENIED);
            }
        }

        return PostDetailInfo.from(post);
    }
}
```

## 🏗️ Architecture - Strategy Pattern

Authorization 도메인은 Strategy Pattern을 사용하여 리소스 타입별로 다른 권한 평가 로직을 제공합니다.

```
SimplePolicyEvaluator (Orchestrator)
  ├── CurriculumPermissionEvaluator   → CURRICULUM 권한 평가
  ├── SchedulePermissionEvaluator     → SCHEDULE 권한 평가
  ├── NoticePermissionEvaluator       → NOTICE 권한 평가
  ├── CommunityPermissionEvaluator    → COMMUNITY 권한 평가
  └── DefaultPermissionEvaluator      → 기본 권한 매트릭스 (Strategy 없는 경우)
```

### 새로운 리소스 타입 추가하기

1. `ResourceType` Enum에 추가
```java
public enum ResourceType {
    CURRICULUM("curriculum", "커리큘럼"),
    MY_NEW_RESOURCE("my-resource", "새 리소스"),  // 추가
    ...
}
```

2. `ResourcePermissionEvaluator` 구현체 생성
```java
@Component
public class MyResourcePermissionEvaluator implements ResourcePermissionEvaluator {

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.MY_NEW_RESOURCE;
    }

    @Override
    public boolean evaluate(List<ChallengerRoleType> roles, String resourceId, PermissionType permission) {
        // 커스텀 권한 로직
        return DefaultPermissionEvaluator.hasPermission(roles, permission);
    }
}
```

Spring이 자동으로 Bean을 찾아서 등록합니다!

## 🔐 권한 매트릭스

현재 `DefaultPermissionEvaluator`에 정의된 Role별 기본 권한:

| Role                      | READ | WRITE | DELETE | APPROVE | MANAGE |
|---------------------------|------|-------|--------|---------|--------|
| CENTRAL_PRESIDENT         | ✅    | ✅     | ✅      | ✅       | ✅      |
| CENTRAL_VICE_PRESIDENT    | ✅    | ✅     | ✅      | ✅       | ✅      |
| CENTRAL_DIRECTOR          | ✅    | ✅     | ❌      | ✅       | ❌      |
| CENTRAL_MANAGER           | ✅    | ✅     | ❌      | ❌       | ❌      |
| CENTRAL_PART_LEADER       | ✅    | ✅     | ❌      | ✅       | ❌      |
| CHAPTER_LEADER            | ✅    | ✅     | ❌      | ✅       | ❌      |
| CHAPTER_STAFF             | ✅    | ✅     | ❌      | ❌       | ❌      |
| SCHOOL_PRESIDENT          | ✅    | ✅     | ❌      | ✅       | ❌      |
| SCHOOL_VICE_PRESIDENT     | ✅    | ✅     | ❌      | ✅       | ❌      |
| SCHOOL_PART_LEADER        | ✅    | ✅     | ❌      | ✅       | ❌      |
| SCHOOL_STAFF              | ✅    | ✅     | ❌      | ❌       | ❌      |
| CHALLENGER                | ✅    | ❌     | ❌      | ❌       | ❌      |

> **Note**: MANAGE 권한이 있으면 모든 권한(READ, WRITE, DELETE, APPROVE)을 자동으로 가집니다.

## ⚙️ 설정

### Spring AOP 활성화

`@EnableAspectJAutoProxy`는 Spring Boot에서 자동 설정되므로 별도 설정 불필요합니다.

### 커스텀 에러 메시지

```java
@CheckAccess(
    resourceType = ResourceType.CURRICULUM,
    resourceId = "#workbookId",
    permission = PermissionType.WRITE,
    message = "워크북 제출 권한이 없습니다."  // 커스텀 메시지
)
```

## 🎯 리소스별 권한 정책

### CURRICULUM (커리큘럼)
- READ: 모든 챌린저
- WRITE: 운영진 이상
- 향후 확장: 파트별 워크북 제한 가능

### SCHEDULE (일정)
- READ: 모든 챌린저
- WRITE: 운영진 이상
- APPROVE: 운영진만 (출석 승인)
- DELETE: 중앙 운영진만

### NOTICE (공지사항)
- READ: 모든 챌린저
- WRITE: 운영진만
- DELETE: 중앙 운영진만

### COMMUNITY (커뮤니티)
- READ/WRITE: 모든 챌린저
- DELETE: 운영진 또는 작성자

## 🧪 테스트

```java
@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    LoadChallengerRolePort loadChallengerRolePort;
    @Mock
    EvaluatePolicyPort evaluatePolicyPort;

    @InjectMocks
    AuthorizationService sut;

    @Test
    void 권한이_있으면_true_반환() {
        // given
        Long memberId = 1L;
        ResourcePermission permission = ResourcePermission.of(
            "curriculum", "123", PermissionType.WRITE
        );

        given(loadChallengerRolePort.findRolesByMemberId(memberId))
            .willReturn(List.of(ChallengerRoleType.SCHOOL_PRESIDENT));
        given(evaluatePolicyPort.evaluate(any(), any(), any(), any()))
            .willReturn(true);

        // when
        boolean result = sut.check(memberId, permission);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 권한이_없으면_checkOrThrow는_예외_발생() {
        // given
        Long memberId = 1L;
        ResourcePermission permission = ResourcePermission.of(
            "curriculum", "123", PermissionType.DELETE
        );

        given(loadChallengerRolePort.findRolesByMemberId(memberId))
            .willReturn(List.of(ChallengerRoleType.CHALLENGER));
        given(evaluatePolicyPort.evaluate(any(), any(), any(), any()))
            .willReturn(false);

        // when & then
        assertThatThrownBy(() -> sut.checkOrThrow(memberId, permission))
            .isInstanceOf(AuthorizationDomainException.class)
            .hasFieldOrPropertyWithValue("errorCode",
                AuthorizationErrorCode.RESOURCE_ACCESS_DENIED);
    }
}
```

## 🔄 Policy Engine 교체 (Casbin)

현재 `SimplePolicyEvaluator`는 하드코딩된 권한 매트릭스를 사용합니다.

더 복잡한 권한 관리가 필요하면 **Casbin** 같은 Policy Engine으로 교체할 수 있습니다.

자세한 내용은 아래 **Casbin 통합 가이드**를 참고하세요.

## 📖 참고 문서

- [Spring AOP Reference](https://docs.spring.io/spring-framework/reference/core/aop.html)
- [SpEL Expression Guide](https://docs.spring.io/spring-framework/reference/core/expressions.html)
- [jCasbin GitHub](https://github.com/casbin/jcasbin)
