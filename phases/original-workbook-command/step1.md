# Step 1: port-adapter

## 읽어야 할 파일

먼저 아래 파일들을 읽고 프로젝트의 아키텍처와 설계 의도를 파악하라:

- `/CLAUDE.md`
- `/docs/harness/ARCHITECTURE.md`
- `/docs/harness/ADR.md`
- `/src/main/java/com/umc/product/curriculum/application/port/out/LoadOriginalWorkbookPort.java`
- `/src/main/java/com/umc/product/curriculum/application/port/out/SaveOriginalWorkbookPort.java`
- `/src/main/java/com/umc/product/curriculum/application/port/out/LoadChallengerWorkbookPort.java`
- `/src/main/java/com/umc/product/curriculum/adapter/out/persistence/OriginalWorkbookPersistenceAdapter.java`
- `/src/main/java/com/umc/product/curriculum/adapter/out/persistence/OriginalWorkbookJpaRepository.java`
- `/src/main/java/com/umc/product/curriculum/adapter/out/persistence/ChallengerWorkbookPersistenceAdapter.java`
- `/src/main/java/com/umc/product/curriculum/adapter/out/persistence/ChallengerWorkbookJpaRepository.java`
- `/src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java`

이전 step에서 생성/수정된 파일:
- `/src/main/java/com/umc/product/curriculum/domain/OriginalWorkbook.java` (step 0: edit 메서드 추가)

## 네이밍 컨벤션 (반드시 준수)

CLAUDE.md의 Port 메서드 네이밍 규칙:

| Prefix | 반환 타입 | 의미 |
|--------|---------|------|
| `get[By]` | `T` | 엔티티가 반드시 존재. 없으면 즉시 예외 던짐 |
| `find[By]` | `Optional<T>` | 없을 수도 있음. Optional 반환 |
| `batchGet[By]` | `List<T>` | 입력 목록 전체가 반드시 존재. 크기 불일치 시 예외 던짐 |
| `list[By]` | `List<T>` | 없으면 빈 List 반환 (예외 없음) |

## 작업

### 1. `LoadOriginalWorkbookPort` 수정

**파일**: `src/main/java/com/umc/product/curriculum/application/port/out/LoadOriginalWorkbookPort.java`

현재 `findById(Long id)` 메서드가 존재하는데, 이 메서드는 엔티티가 없으면 예외를 던진다.
**예외를 던지는 메서드는 `get` prefix를 사용해야 한다.**

따라서 아래를 수행하라:
- `findById(Long id)` → **`getById(Long id)`로 이름 변경** (반환 타입 `OriginalWorkbook` 유지)
- 아래 메서드를 **추가**:

```java
// 배치 조회 — IDs 중 하나라도 없으면 CurriculumDomainException(WORKBOOK_NOT_FOUND) 던짐
List<OriginalWorkbook> batchGetByIds(List<Long> ids);
```

> 기존의 `findReleasedByWeeklyCurriculumId`, `findUnreleasedWithStartDateBefore`는 건드리지 마라.

### 2. `SaveOriginalWorkbookPort` 수정

**파일**: `src/main/java/com/umc/product/curriculum/application/port/out/SaveOriginalWorkbookPort.java`

CLAUDE.md Section 13에 따라 Save Port는 `save`, `saveAll`, `delete`를 함께 선언한다. 현재 `save`만 있으므로 추가하라:

```java
List<OriginalWorkbook> saveAll(List<OriginalWorkbook> workbooks);
void delete(OriginalWorkbook workbook);
```

### 3. `OriginalWorkbookPersistenceAdapter` 수정

**파일**: `src/main/java/com/umc/product/curriculum/adapter/out/persistence/OriginalWorkbookPersistenceAdapter.java`

Port 변경에 맞춰 Adapter를 수정하라:

- **`findById` → `getById`로 rename**: 기존 구현 `originalWorkbookJpaRepository.findById(id).orElseThrow(...)` 로직은 그대로 유지한다.
- **`batchGetByIds` 추가**:
  ```java
  @Override
  public List<OriginalWorkbook> batchGetByIds(List<Long> ids) {
      List<OriginalWorkbook> result = originalWorkbookJpaRepository.findAllById(ids);
      if (result.size() != ids.size()) {
          throw new CurriculumDomainException(CurriculumErrorCode.WORKBOOK_NOT_FOUND);
      }
      return result;
  }
  ```
- **`saveAll` 추가**: `originalWorkbookJpaRepository.saveAll(workbooks)` 위임.
- **`delete` 추가**: `originalWorkbookJpaRepository.delete(workbook)` 위임.

`OriginalWorkbookJpaRepository`는 `JpaRepository<OriginalWorkbook, Long>`을 상속하므로 `findAllById`, `saveAll`, `delete`는 이미 존재한다. JpaRepository에 별도 메서드를 추가하지 마라.

### 4. `LoadChallengerWorkbookPort` 수정

**파일**: `src/main/java/com/umc/product/curriculum/application/port/out/LoadChallengerWorkbookPort.java`

삭제 전 배포 여부 확인에 필요한 메서드를 추가하라:

```java
boolean existsByOriginalWorkbookId(Long originalWorkbookId);
```

### 5. `ChallengerWorkbookJpaRepository` 수정

**파일**: `src/main/java/com/umc/product/curriculum/adapter/out/persistence/ChallengerWorkbookJpaRepository.java`

Spring Data JPA 네이밍 컨벤션에 따라 추가하라:

```java
boolean existsByOriginalWorkbookId(Long originalWorkbookId);
```

### 6. `ChallengerWorkbookPersistenceAdapter` 수정

**파일**: `src/main/java/com/umc/product/curriculum/adapter/out/persistence/ChallengerWorkbookPersistenceAdapter.java`

위에서 추가한 Port 메서드를 구현하라:

```java
@Override
public boolean existsByOriginalWorkbookId(Long originalWorkbookId) {
    return challengerWorkbookJpaRepository.existsByOriginalWorkbookId(originalWorkbookId);
}
```

## Acceptance Criteria

```bash
./gradlew compileJava
```

## 검증 절차

1. 위 AC 커맨드를 실행한다.
2. 아키텍처 체크리스트를 확인한다:
    - `LoadOriginalWorkbookPort`에 `findById`가 사라지고 `getById`로 대체되었는가?
    - Port 인터페이스 → Adapter → JpaRepository 의존 방향이 올바른가?
    - JpaRepository에 중복 메서드(JpaRepository 기본 제공 메서드를 재선언)를 추가하지 않았는가?
3. 결과에 따라 `phases/original-workbook-command/index.json`의 step 1을 업데이트한다:
    - 성공 → `"status": "completed"`, `"summary": "LoadOriginalWorkbookPort.findById → getById rename + batchGetByIds 추가, SaveOriginalWorkbookPort delete/saveAll 추가, LoadChallengerWorkbookPort existsByOriginalWorkbookId 추가, 각 Adapter 구현 완료"`
    - 수정 3회 시도 후에도 실패 → `"status": "error"`, `"error_message": "구체적 에러 내용"`

## 금지사항

- `findReleasedByWeeklyCurriculumId`, `findUnreleasedWithStartDateBefore`를 삭제하거나 수정하지 마라. 이유: `CurriculumQueryService`와 `WorkbookAutoReleaseScheduler`가 사용 중이므로 컴파일이 깨진다.
- `OriginalWorkbookJpaRepository`에 `findAllById`, `saveAll`, `delete` 같이 `JpaRepository`가 이미 제공하는 메서드를 다시 선언하지 마라. 이유: 불필요한 중복이고 기본 구현과 충돌할 수 있다.
- 기존 테스트를 깨뜨리지 마라.
