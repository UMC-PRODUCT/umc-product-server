# Strategy Pattern으로 리소스별 권한 관리

Authorization 도메인은 **Strategy Pattern**을 사용하여 리소스 타입별로 다른 권한 평가 로직을 제공합니다.

## 🎯 왜 Strategy Pattern인가?

### 문제 상황

초기에는 모든 리소스에 대해 단순 Role 기반 권한만 체크했습니다:

```java
// ❌ Before: 모든 리소스가 같은 로직
if (role == CENTRAL_PRESIDENT) {
    return true;  // 모든 권한
}
return role.hasPermission(permission);
```

하지만 실제로는 **리소스별로 다른 권한 정책**이 필요합니다:

- **Curriculum**: 파트별로 워크북 수정 제한
- **Schedule**: 출석 승인은 운영진만, 삭제는 중앙 운영진만
- **Notice**: 작성은 운영진만, 삭제는 중앙 운영진 또는 작성자
- **Community**: 모든 챌린저가 작성 가능, 삭제는 운영진 또는 작성자

### 해결책: Strategy Pattern

각 리소스 타입별로 **독립적인 권한 평가 로직**을 Strategy로 분리합니다.

```
SimplePolicyEvaluator (Orchestrator)
  │
  ├──> CurriculumPermissionEvaluator   (CURRICULUM 전담)
  ├──> SchedulePermissionEvaluator     (SCHEDULE 전담)
  ├──> NoticePermissionEvaluator       (NOTICE 전담)
  └──> DefaultPermissionEvaluator      (기본 매트릭스)
```

## 🏗️ 구조

### 1. ResourceType Enum

리소스 타입을 **타입 안전하게** 관리:

```java
public enum ResourceType {
    CURRICULUM("curriculum", "커리큘럼"),
    SCHEDULE("schedule", "일정"),
    NOTICE("notice", "공지사항"),
    COMMUNITY("community", "커뮤니티"),
    FORM("form", "지원서"),
    ORGANIZATION("organization", "기수/지부/학교"),
    MEMBER("member", "사용자"),
}
```

### 2. Strategy Interface

```java
public interface ResourcePermissionEvaluator {

    ResourceType supportedResourceType();

    boolean evaluate(
        List<ChallengerRoleType> roles,
        String resourceId,
        PermissionType permission
    );
}
```

### 3. Concrete Strategies

각 리소스별 구현체:

```java
@Component
public class SchedulePermissionEvaluator implements ResourcePermissionEvaluator {

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.SCHEDULE;
    }

    @Override
    public boolean evaluate(List<ChallengerRoleType> roles, String resourceId, PermissionType permission) {
        // 기본 권한 체크
        boolean hasBasicPermission = DefaultPermissionEvaluator.hasPermission(roles, permission);

        // Schedule 특화 로직
        if (permission == PermissionType.APPROVE) {
            return roles.stream().anyMatch(DefaultPermissionEvaluator::isStaff);
        }

        if (permission == PermissionType.DELETE) {
            return DefaultPermissionEvaluator.hasCentralStaff(roles);
        }

        return hasBasicPermission;
    }
}
```

### 4. Orchestrator

Strategy를 선택하여 위임:

```java
@Component
public class SimplePolicyEvaluator implements EvaluatePolicyPort {

    private final Map<ResourceType, ResourcePermissionEvaluator> evaluators;

    public SimplePolicyEvaluator(List<ResourcePermissionEvaluator> evaluatorList) {
        // Spring이 모든 Strategy를 주입하여 Map으로 저장
        this.evaluators = evaluatorList.stream()
                .collect(Collectors.toMap(
                        ResourcePermissionEvaluator::supportedResourceType,
                        Function.identity()
                ));
    }

    @Override
    public boolean evaluate(
            List<ChallengerRoleType> roles,
            ResourceType resourceType,
            String resourceId,
            PermissionType permission
    ) {
        // 1. Strategy가 있으면 위임
        ResourcePermissionEvaluator evaluator = evaluators.get(resourceType);
        if (evaluator != null) {
            return evaluator.evaluate(roles, resourceId, permission);
        }

        // 2. 없으면 기본 매트릭스로 평가
        return DefaultPermissionEvaluator.hasPermission(roles, permission);
    }
}
```

## ✨ 장점

### 1. 개방-폐쇄 원칙 (OCP)

새로운 리소스 타입 추가 시 **기존 코드 수정 없이** Strategy만 추가:

```java
@Component
public class FormPermissionEvaluator implements ResourcePermissionEvaluator {

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.FORM;
    }

    @Override
    public boolean evaluate(List<ChallengerRoleType> roles, String resourceId, PermissionType permission) {
        // Form 전용 권한 로직
        return ...;
    }
}
```

Spring이 자동으로 감지하여 등록합니다!

### 2. 단일 책임 원칙 (SRP)

각 Strategy는 **하나의 리소스 타입**만 책임:

- `CurriculumPermissionEvaluator`: Curriculum 권한만
- `SchedulePermissionEvaluator`: Schedule 권한만
- `NoticePermissionEvaluator`: Notice 권한만

### 3. 테스트 용이성

리소스별로 **독립적인 테스트** 가능:

```java
@Test
void Schedule_삭제는_중앙운영진만_가능() {
    // given
    var evaluator = new SchedulePermissionEvaluator();
    var roles = List.of(ChallengerRoleType.SCHOOL_PRESIDENT);

    // when
    boolean result = evaluator.evaluate(roles, "123", PermissionType.DELETE);

    // then
    assertThat(result).isFalse();
}
```

### 4. 확장 가능성

각 Strategy 내부에서 **더 복잡한 로직** 추가 가능:

```java
@Component
@RequiredArgsConstructor
public class CurriculumPermissionEvaluator implements ResourcePermissionEvaluator {

    private final LoadWorkbookPort loadWorkbookPort;  // 외부 의존성 주입 가능

    @Override
    public boolean evaluate(List<ChallengerRoleType> roles, String resourceId, PermissionType permission) {
        // 1. 기본 권한 체크
        if (!DefaultPermissionEvaluator.hasPermission(roles, permission)) {
            return false;
        }

        // 2. 워크북별 세밀한 제어
        if (resourceId != null && permission == PermissionType.WRITE) {
            Workbook workbook = loadWorkbookPort.findById(Long.parseLong(resourceId));

            // 해당 파트의 파트장만 수정 가능
            return roles.stream().anyMatch(role ->
                role == ChallengerRoleType.SCHOOL_PART_LEADER
                && workbook.getPart() == getLeadingPart(role)
            );
        }

        return true;
    }
}
```

## 📂 파일 구조

```
authorization/
└── adapter/out/policy/
    ├── SimplePolicyEvaluator.java           # Orchestrator
    ├── ResourcePermissionEvaluator.java     # Strategy Interface
    └── evaluator/
        ├── DefaultPermissionEvaluator.java  # 기본 매트릭스 (static utility)
        ├── CurriculumPermissionEvaluator.java
        ├── SchedulePermissionEvaluator.java
        ├── NoticePermissionEvaluator.java
        └── CommunityPermissionEvaluator.java
```

## 🚀 사용 예시

### Controller에서 사용

```java
@PostMapping("/schedules/{scheduleId}/approve")
@CheckAccess(
    resourceType = ResourceType.SCHEDULE,  // Enum 사용!
    resourceId = "#scheduleId",
    permission = PermissionType.APPROVE
)
public ApiResponse<Void> approveAttendance(@PathVariable Long scheduleId) {
    // SchedulePermissionEvaluator가 자동으로 호출됨
    return ApiResponse.success();
}
```

### Service에서 직접 호출

```java
@Service
@RequiredArgsConstructor
public class NoticeCommandService {

    private final CheckPermissionUseCase checkPermissionUseCase;

    public void deleteNotice(Long memberId, Long noticeId) {
        // NoticePermissionEvaluator가 자동으로 호출됨
        checkPermissionUseCase.checkOrThrow(
            memberId,
            ResourcePermission.of(ResourceType.NOTICE, noticeId, PermissionType.DELETE)
        );

        // 비즈니스 로직
    }
}
```

## 🔄 vs 각 도메인에 Evaluator 배치

**Q: 왜 각 도메인(curriculum, schedule)에 Evaluator를 두지 않았나요?**

A: **의존성 방향**을 명확히 하기 위해서입니다.

### ❌ 각 도메인에 배치하면

```
authorization/               # 권한 체크 주체
  ↓ 의존
curriculum/                  # Evaluator 구현
  ↓ 의존
authorization/               # 순환 의존!
```

### ✅ Authorization 내부에서 Strategy Pattern 사용

```
authorization/               # 권한 관리 중앙화
  ├── Orchestrator
  └── Strategies/
      ├── CurriculumPermissionEvaluator
      ├── SchedulePermissionEvaluator
      └── NoticePermissionEvaluator

↑
모든 도메인이 authorization에만 의존
```

권한 관리는 **Cross-cutting concern**이므로 Authorization 도메인에서 중앙 관리하되,
각 리소스별 세밀한 제어는 **Strategy**로 분리하여 확장성을 확보합니다.

## 📖 참고

- [GoF Design Patterns - Strategy](https://refactoring.guru/design-patterns/strategy)
- [Spring Boot - Autowiring Collections](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html)
