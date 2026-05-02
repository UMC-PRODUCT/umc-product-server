# Step 4: controller-wire

## 읽어야 할 파일

먼저 아래 파일들을 읽고 프로젝트의 아키텍처와 설계 의도를 파악하라:

- `/CLAUDE.md`
- `/docs/harness/ARCHITECTURE.md`
- `/src/main/java/com/umc/product/curriculum/adapter/in/web/v2/OriginalWorkbookCommandV2Controller.java`
- `/src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumCommandV2Controller.java`
- `/src/main/java/com/umc/product/curriculum/adapter/in/web/v2/CurriculumQueryV2Controller.java`
- `/src/main/java/com/umc/product/curriculum/adapter/in/web/v2/dto/request/CreateOriginalWorkbookRequest.java`
- `/src/main/java/com/umc/product/curriculum/adapter/in/web/v2/dto/request/ChangeOriginalWorkbookStatusRequest.java`
- `/src/main/java/com/umc/product/curriculum/application/port/in/command/ManageOriginalWorkbookUseCase.java`
- `/src/main/java/com/umc/product/authorization/domain/ResourceType.java`
- `/src/main/java/com/umc/product/authorization/domain/PermissionType.java`
- `/src/main/java/com/umc/product/global/security/MemberPrincipal.java`
- `/src/main/java/com/umc/product/global/security/annotation/CurrentMember.java`

이전 step에서 생성/수정된 파일:
- `/src/main/java/com/umc/product/curriculum/adapter/in/web/v2/dto/request/EditOriginalWorkbookRequest.java` (step 2)
- `/src/main/java/com/umc/product/curriculum/application/service/command/OriginalWorkbookCommandService.java` (step 3)

## 작업

`OriginalWorkbookCommandV2Controller`의 5개 엔드포인트를 모두 구현하라. 현재 모든 메서드가 `throw new NotImplementedException()` 상태이다.

### 주입할 의존성

`@RequiredArgsConstructor`를 활용하여 아래 필드를 추가하라:

```java
private final ManageOriginalWorkbookUseCase manageOriginalWorkbookUseCase;
```

> `ManageOriginalWorkbookUseCase` 인터페이스를 주입받는다. `OriginalWorkbookCommandService` 구현체를 직접 주입받지 마라.

### 권한 어노테이션

**중요**: `ResourceType.ORIGINAL_WORKBOOK`을 사용하라. `ResourceType.CURRICULUM`이 아니다.
`ORIGINAL_WORKBOOK`이 지원하는 권한은 `PermissionType.MANAGE`와 `PermissionType.RELEASE`뿐이다. 다른 PermissionType을 사용하면 런타임 예외가 발생한다.

각 엔드포인트별 권한:

| 엔드포인트 | resourceType | permission | resourceId |
|-----------|-------------|-----------|-----------|
| create (READY) | ORIGINAL_WORKBOOK | MANAGE | 없음 |
| create (DRAFT) | ORIGINAL_WORKBOOK | MANAGE | 없음 |
| edit | ORIGINAL_WORKBOOK | MANAGE | `"#originalWorkbookId"` |
| delete | ORIGINAL_WORKBOOK | MANAGE | `"#originalWorkbookId"` |
| changeStatus (배포) | ORIGINAL_WORKBOOK | RELEASE | 없음 |

### 1. `POST /api/v2/curriculums/original-workbooks` — createOriginalWorkbook (READY)

```java
@CheckAccess(resourceType = ResourceType.ORIGINAL_WORKBOOK, permission = PermissionType.MANAGE)
@PostMapping
public Long createOriginalWorkbook(@Valid @RequestBody CreateOriginalWorkbookRequest request) {
    return manageOriginalWorkbookUseCase.create(request.toCommand(OriginalWorkbookStatus.READY));
}
```

### 2. `POST /api/v2/curriculums/original-workbooks/draft` — createOriginalWorkbookAsDraft (DRAFT)

```java
@CheckAccess(resourceType = ResourceType.ORIGINAL_WORKBOOK, permission = PermissionType.MANAGE)
@PostMapping("/draft")
public Long createOriginalWorkbookAsDraft(@Valid @RequestBody CreateOriginalWorkbookRequest request) {
    return manageOriginalWorkbookUseCase.create(request.toCommand(OriginalWorkbookStatus.DRAFT));
}
```

### 3. `PATCH /api/v2/curriculums/original-workbooks/{originalWorkbookId}` — editOriginalWorkbook

현재 `@RequestBody`가 없다. 추가하라:

```java
@CheckAccess(
    resourceType = ResourceType.ORIGINAL_WORKBOOK,
    resourceId = "#originalWorkbookId",
    permission = PermissionType.MANAGE
)
@PatchMapping("/{originalWorkbookId}")
public void editOriginalWorkbook(
    @PathVariable Long originalWorkbookId,
    @RequestBody EditOriginalWorkbookRequest request
) {
    manageOriginalWorkbookUseCase.edit(request.toCommand(originalWorkbookId));
}
```

### 4. `DELETE /api/v2/curriculums/original-workbooks/{originalWorkbookId}` — deleteOriginalWorkbook

```java
@CheckAccess(
    resourceType = ResourceType.ORIGINAL_WORKBOOK,
    resourceId = "#originalWorkbookId",
    permission = PermissionType.MANAGE
)
@DeleteMapping("/{originalWorkbookId}")
public void deleteOriginalWorkbook(@PathVariable Long originalWorkbookId) {
    manageOriginalWorkbookUseCase.delete(originalWorkbookId);
}
```

### 5. `PATCH /api/v2/curriculums/original-workbooks/status` — changeOriginalWorkbookStatus

`ChangeOriginalWorkbookStatusCommand`가 `requestedMemberId`를 요구한다. `@CurrentMember`로 인증된 멤버 ID를 주입하라:

```java
@CheckAccess(resourceType = ResourceType.ORIGINAL_WORKBOOK, permission = PermissionType.RELEASE)
@PatchMapping("/status")
public void changeOriginalWorkbookStatus(
    @Valid @RequestBody List<ChangeOriginalWorkbookStatusRequest> requests,
    @CurrentMember MemberPrincipal memberPrincipal
) {
    List<ChangeOriginalWorkbookStatusCommand> commands = requests.stream()
        .map(r -> r.toCommand(memberPrincipal.getMemberId()))
        .toList();
    manageOriginalWorkbookUseCase.changeStatusForRelease(commands);
}
```

### 반환 타입 수정

현재 컨트롤러의 `createOriginalWorkbook`, `createOriginalWorkbookAsDraft`의 반환 타입이 `Long`이다.
CLAUDE.md에 따르면 "반환 타입은 반드시 Response DTO 객체여야 한다. Primitive Type 금지."

그러나 **이 step에서는 반환 타입을 수정하지 마라**. 컨트롤러에 이미 선언된 시그니처를 변경하면 다른 코드와의 충돌이 발생할 수 있으며, 반환 타입 변경은 별도 리팩토링 phase에서 다룬다.

## Acceptance Criteria

```bash
./gradlew compileJava
./gradlew test --tests "com.umc.product.curriculum.*"
```

## 검증 절차

1. 위 AC 커맨드를 실행한다.
2. 아키텍처 체크리스트를 확인한다:
    - `NotImplementedException`이 컨트롤러에서 완전히 제거되었는가?
    - 컨트롤러가 `ManageOriginalWorkbookUseCase` 인터페이스에만 의존하는가?
    - `@CheckAccess`의 `resourceType`이 `ResourceType.ORIGINAL_WORKBOOK`인가?
    - `changeStatus` 엔드포인트만 `PermissionType.RELEASE`이고, 나머지는 `PermissionType.MANAGE`인가?
    - 응답이 `ApiResponse`로 래핑되지 않았는가?
3. 결과에 따라 `phases/original-workbook-command/index.json`의 step 4를 업데이트한다:
    - 성공 → `"status": "completed"`, `"summary": "OriginalWorkbookCommandV2Controller 5개 엔드포인트 구현 완료 — ORIGINAL_WORKBOOK 권한 체계 적용, MANAGE/RELEASE 분리"`
    - 수정 3회 시도 후에도 실패 → `"status": "error"`, `"error_message": "구체적 에러 내용"`

## 금지사항

- `ResourceType.CURRICULUM`을 사용하지 마라. 이유: 원본 워크북 전용 리소스 타입 `ORIGINAL_WORKBOOK`이 이미 존재한다.
- `PermissionType.WRITE`, `PermissionType.DELETE`를 사용하지 마라. 이유: `ORIGINAL_WORKBOOK`이 지원하는 권한은 `MANAGE`와 `RELEASE`뿐이다. 다른 타입은 런타임에 `AuthorizationDomainException`이 발생한다.
- `OriginalWorkbookCommandService`를 직접 주입받지 마라. 이유: 헥사고날 아키텍처에서 Driving Adapter는 Port(UseCase 인터페이스)에만 의존해야 한다.
- 응답을 `ApiResponse`로 래핑하지 마라. 이유: `GlobalResponseWrapper`가 자동 처리한다.
- 비즈니스 로직을 컨트롤러에 추가하지 마라. 이유: 컨트롤러는 UseCase 위임만 담당한다.
- 기존 테스트를 깨뜨리지 마라.
