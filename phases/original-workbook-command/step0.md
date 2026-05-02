# Step 0: domain-edit-method

## 읽어야 할 파일

먼저 아래 파일들을 읽고 프로젝트의 아키텍처와 설계 의도를 파악하라:

- `/CLAUDE.md`
- `/docs/harness/ARCHITECTURE.md`
- `/docs/harness/ADR.md`
- `/src/main/java/com/umc/product/curriculum/domain/OriginalWorkbook.java`
- `/src/main/java/com/umc/product/curriculum/domain/enums/OriginalWorkbookStatus.java`

## 작업

`OriginalWorkbook` 엔티티에 수정(edit) 도메인 메서드를 추가하라.

### 파일: `src/main/java/com/umc/product/curriculum/domain/OriginalWorkbook.java`

다음 시그니처의 `public` 메서드를 추가하라:

```java
public void edit(String title, String description, String url, String content)
```

**필드별 갱신 규칙:**
- `title`: `org.springframework.util.StringUtils.hasText(title)`가 true일 때만 갱신한다. blank 또는 null이면 기존 값을 유지한다.
- `description`, `url`, `content`: null이 **아닌** 경우에만 갱신한다. null을 전달하면 기존 값을 유지한다 (명시적으로 null로 세팅하는 것은 허용하지 않음).

**상태 제약 없음**: API 명세상 "따로 제한 없이 수정이 가능하다"고 명시되어 있으므로, `originalWorkbookStatus`에 따른 수정 제한을 두지 마라. RELEASED 상태도 수정 가능하다.

## Acceptance Criteria

```bash
./gradlew compileJava
./gradlew test --tests "com.umc.product.curriculum.domain.*"
```

## 검증 절차

1. 위 AC 커맨드를 실행한다.
2. 아키텍처 체크리스트를 확인한다:
    - `domain` 레이어가 `application`이나 `adapter` 패키지를 import하지 않는가?
    - `@Setter`를 사용하지 않고 명시적 도메인 메서드로만 상태를 변경하는가?
    - `@OneToMany`가 추가되지 않았는가?
3. 결과에 따라 `phases/original-workbook-command/index.json`의 step 0을 업데이트한다:
    - 성공 → `"status": "completed"`, `"summary": "OriginalWorkbook.edit(title, description, url, content) 도메인 메서드 추가 — blank/null 필드는 기존 값 유지"`
    - 수정 3회 시도 후에도 실패 → `"status": "error"`, `"error_message": "구체적 에러 내용"`

## 금지사항

- `@Setter`를 추가하지 마라. 이유: CLAUDE.md CRITICAL 규칙 위반.
- `@OneToMany`를 추가하지 마라. 이유: ADR-003 위반.
- 기존 `createAsDraft`, `createAsReady`, `changeStatus` 메서드를 건드리지 마라. 이유: 이미 사용 중이므로 부수 효과가 발생한다.
- 기존 테스트(`OriginalWorkbookStatusTransitionTest`)를 깨뜨리지 마라.
