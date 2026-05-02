# Step 2: request-dto

## 읽어야 할 파일

먼저 아래 파일들을 읽고 프로젝트의 아키텍처와 설계 의도를 파악하라:

- `/CLAUDE.md`
- `/docs/harness/ARCHITECTURE.md`
- `/src/main/java/com/umc/product/curriculum/adapter/in/web/v2/dto/request/CreateOriginalWorkbookRequest.java`
- `/src/main/java/com/umc/product/curriculum/adapter/in/web/v2/dto/request/ChangeOriginalWorkbookStatusRequest.java`
- `/src/main/java/com/umc/product/curriculum/application/port/in/command/dto/workbook/EditOriginalWorkbookCommand.java`

이전 step에서 생성/수정된 파일:
- `/src/main/java/com/umc/product/curriculum/application/port/out/LoadOriginalWorkbookPort.java` (step 1: findById → getById, batchGetByIds 추가)

## 작업

원본 워크북 수정 엔드포인트에 필요한 Request DTO를 생성하라.

### 파일 생성: `src/main/java/com/umc/product/curriculum/adapter/in/web/v2/dto/request/EditOriginalWorkbookRequest.java`

**요구사항:**

- Java `record`로 작성하라.
- 필드: `title` (String), `description` (String), `url` (String), `content` (String)
- **모든 필드는 선택적(nullable)이다.** 제공된 필드만 수정되고, 미제공(null) 필드는 기존 값을 유지한다.
- 따라서 `@NotNull`, `@NotBlank` 같은 validation 어노테이션을 붙이지 마라.
- `toCommand(Long originalWorkbookId)` 인스턴스 메서드를 포함하라. `EditOriginalWorkbookCommand`를 빌더로 생성해 반환한다.

**예상 시그니처:**

```java
public record EditOriginalWorkbookRequest(
    String title,
    String description,
    String url,
    String content
) {
    public EditOriginalWorkbookCommand toCommand(Long originalWorkbookId) { ... }
}
```

`EditOriginalWorkbookCommand`는 `application/port/in/command/dto/workbook/` 에 이미 존재한다. 직접 파일을 읽고 필드를 확인한 뒤 매핑하라.

## Acceptance Criteria

```bash
./gradlew compileJava
```

## 검증 절차

1. 위 AC 커맨드를 실행한다.
2. 아키텍처 체크리스트를 확인한다:
    - `adapter/in/web/v2/dto/request` 패키지에 생성되었는가?
    - `toCommand()` 메서드가 `application` 레이어의 Command 객체를 생성하는가?
    - record가 immutable인가? (`@Setter` 없음)
3. 결과에 따라 `phases/original-workbook-command/index.json`의 step 2를 업데이트한다:
    - 성공 → `"status": "completed"`, `"summary": "EditOriginalWorkbookRequest record 생성 (4개 nullable 필드, toCommand(Long) 변환 메서드)"`
    - 수정 3회 시도 후에도 실패 → `"status": "error"`, `"error_message": "구체적 에러 내용"`

## 금지사항

- `@Setter`를 추가하지 마라. 이유: CRITICAL 규칙 위반이며 record는 immutable이다.
- 필드에 `@NotNull`, `@NotBlank`를 붙이지 마라. 이유: 모든 필드가 선택적(patch semantics)이다.
- Request DTO에 비즈니스 로직(유효성 검사 이상의 조건 처리)을 넣지 마라. 이유: 도메인 메서드의 책임이다.
- 기존 테스트를 깨뜨리지 마라.
