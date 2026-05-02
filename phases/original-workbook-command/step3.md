# Step 3: service-and-test

## 읽어야 할 파일

먼저 아래 파일들을 읽고 프로젝트의 아키텍처와 설계 의도를 파악하라:

- `/CLAUDE.md`
- `/docs/harness/ARCHITECTURE.md`
- `/docs/harness/ADR.md`
- `/src/main/java/com/umc/product/curriculum/application/port/in/command/ManageOriginalWorkbookUseCase.java`
- `/src/main/java/com/umc/product/curriculum/application/port/in/command/dto/workbook/CreateOriginalWorkbookCommand.java`
- `/src/main/java/com/umc/product/curriculum/application/port/in/command/dto/workbook/EditOriginalWorkbookCommand.java`
- `/src/main/java/com/umc/product/curriculum/application/port/in/command/dto/workbook/ChangeOriginalWorkbookStatusCommand.java`
- `/src/main/java/com/umc/product/curriculum/application/port/out/LoadOriginalWorkbookPort.java`
- `/src/main/java/com/umc/product/curriculum/application/port/out/SaveOriginalWorkbookPort.java`
- `/src/main/java/com/umc/product/curriculum/application/port/out/LoadChallengerWorkbookPort.java`
- `/src/main/java/com/umc/product/curriculum/application/port/out/LoadWeeklyCurriculumPort.java`
- `/src/main/java/com/umc/product/curriculum/application/service/command/OriginalWorkbookCommandService.java`
- `/src/main/java/com/umc/product/curriculum/domain/OriginalWorkbook.java`
- `/src/main/java/com/umc/product/curriculum/domain/enums/OriginalWorkbookStatus.java`
- `/src/main/java/com/umc/product/curriculum/domain/enums/OriginalWorkbookType.java`
- `/src/main/java/com/umc/product/curriculum/domain/exception/CurriculumErrorCode.java`
- `/src/test/java/com/umc/product/curriculum/application/service/command/CurriculumCommandServiceTest.java`

이전 step에서 생성/수정된 파일:
- `/src/main/java/com/umc/product/curriculum/domain/OriginalWorkbook.java` (step 0: edit 메서드)
- `/src/main/java/com/umc/product/curriculum/application/port/out/LoadOriginalWorkbookPort.java` (step 1: getById, batchGetByIds)
- `/src/main/java/com/umc/product/curriculum/application/port/out/SaveOriginalWorkbookPort.java` (step 1: delete, saveAll)
- `/src/main/java/com/umc/product/curriculum/application/port/out/LoadChallengerWorkbookPort.java` (step 1: existsByOriginalWorkbookId)

## 작업

### 파일 수정: `src/main/java/com/umc/product/curriculum/application/service/command/OriginalWorkbookCommandService.java`

`OriginalWorkbookCommandService`는 `ManageOriginalWorkbookUseCase`와 `AutoReleaseWorkbookUseCase` 두 인터페이스를 구현한다.
이 step에서는 **`ManageOriginalWorkbookUseCase`의 4개 메서드만** 구현하고, **`releaseAllDue()`는 `NotImplementedException`을 그대로 유지**하라.

현재 서비스에 없는 Port 의존성을 추가하라 (`@RequiredArgsConstructor` 사용):
- `LoadWeeklyCurriculumPort loadWeeklyCurriculumPort` (create 시 WeeklyCurriculum 조회용)
- `LoadChallengerWorkbookPort loadChallengerWorkbookPort` (delete 시 배포 여부 확인용)

#### `create(CreateOriginalWorkbookCommand command) → Long`

1. `loadWeeklyCurriculumPort.getById(command.weeklyCurriculumId())`로 `WeeklyCurriculum`을 조회한다. (없으면 Port 내부에서 자동 예외 던짐)
2. `command.initialStatus()`에 따라 도메인 팩토리 메서드를 선택한다:
   - `DRAFT` → `OriginalWorkbook.createAsDraft(weeklyCurriculum, command.title(), command.description(), command.url(), command.content(), command.type())`
   - `READY` → `OriginalWorkbook.createAsReady(weeklyCurriculum, command.title(), command.description(), command.url(), command.content(), command.type())`
   - `RELEASED` 또는 다른 상태 → `CurriculumDomainException(CurriculumErrorCode.INVALID_WORKBOOK_STATUS)` 던짐
3. `saveOriginalWorkbookPort.save(workbook).getId()`를 반환한다.

#### `edit(EditOriginalWorkbookCommand command)`

1. `loadOriginalWorkbookPort.getById(command.originalWorkbookId())`로 워크북을 조회한다.
2. `workbook.edit(command.title(), command.description(), command.url(), command.content())`를 호출한다.
3. `saveOriginalWorkbookPort.save(workbook)`를 호출한다.

#### `delete(Long originalWorkbookId)`

1. `loadOriginalWorkbookPort.getById(originalWorkbookId)`로 워크북을 조회한다.
2. `loadChallengerWorkbookPort.existsByOriginalWorkbookId(originalWorkbookId)`를 확인한다.
   - `true`이면 `CurriculumDomainException(CurriculumErrorCode.WORKBOOK_HAS_SUBMISSIONS)` 던짐
3. `saveOriginalWorkbookPort.delete(workbook)`를 호출한다.

#### `changeStatusForRelease(List<ChangeOriginalWorkbookStatusCommand> commands)`

N+1을 방지하기 위해 **반드시 배치 조회**를 사용하라:

1. ID 목록 추출: `commands.stream().map(ChangeOriginalWorkbookStatusCommand::originalWorkbookId).toList()`
2. 배치 조회: `loadOriginalWorkbookPort.batchGetByIds(ids)` — 하나라도 없으면 Port 내부에서 자동 예외 던짐
3. Map으로 인덱싱: `workbooks.stream().collect(Collectors.toMap(OriginalWorkbook::getId, w -> w))`
4. 각 command에 대해 `workbookById.get(command.originalWorkbookId()).changeStatus(command.status(), command.requestedMemberId())` 호출 — 유효하지 않은 상태 전환 시 도메인에서 자동 예외 던짐
5. 배치 저장: `saveOriginalWorkbookPort.saveAll(new ArrayList<>(workbookById.values()))`

---

### 파일 생성: `src/test/java/com/umc/product/curriculum/application/service/command/OriginalWorkbookCommandServiceTest.java`

`CurriculumCommandServiceTest`와 동일한 패턴을 따르라:
- `@ExtendWith(MockitoExtension.class)` + `@Nested` 클래스로 메서드별 그룹화
- `@DisplayName` + Given/When/Then 구조
- **테스트 메서드명은 반드시 한국어**

**Mocks:**
```java
@Mock LoadOriginalWorkbookPort loadOriginalWorkbookPort;
@Mock SaveOriginalWorkbookPort saveOriginalWorkbookPort;
@Mock LoadWeeklyCurriculumPort loadWeeklyCurriculumPort;
@Mock LoadChallengerWorkbookPort loadChallengerWorkbookPort;
@InjectMocks OriginalWorkbookCommandService sut;
```

**테스트에서 OriginalWorkbook 생성 전략:**
- `OriginalWorkbook`은 **실제 도메인 객체**를 사용하라 (Mockito mock이 아님). 도메인 메서드(`edit`, `changeStatus`)의 실제 동작을 검증해야 하기 때문이다.
- `OriginalWorkbook.createAsReady(...)` 또는 `createAsDraft(...)`를 호출해 생성한다. `WeeklyCurriculum` 인자는 Mockito mock을 사용해도 무방하다.
- ID가 필요한 경우 `ReflectionTestUtils.setField(workbook, "id", 1L)`로 주입하라.

#### `@Nested @DisplayName("원본 워크북 생성")` — Create 그룹

1. **READY 상태로 생성 성공**
   - `loadWeeklyCurriculumPort.getById(anyLong())` → mock WeeklyCurriculum 반환
   - `saveOriginalWorkbookPort.save(any())` → ID=1L인 OriginalWorkbook 반환
   - 반환 ID가 1L인지 확인, `save` 호출 검증

2. **DRAFT 상태로 생성 성공**
   - 위와 동일, `initialStatus = DRAFT`

3. **RELEASED 상태로 직접 생성 시 예외 발생**
   - `initialStatus = RELEASED` → `CurriculumDomainException(INVALID_WORKBOOK_STATUS)` 발생
   - `saveOriginalWorkbookPort.save`가 **호출되지 않음** 검증 (`then(saveOriginalWorkbookPort).should(never()).save(any())`)

4. **존재하지 않는 WeeklyCurriculum이면 예외 전파**
   - `loadWeeklyCurriculumPort.getById(anyLong())` → `CurriculumDomainException` 던지도록 stub
   - 서비스에서 해당 예외가 전파되는지 확인

#### `@Nested @DisplayName("원본 워크북 수정")` — Edit 그룹

5. **수정 성공 — title 변경 반영됨**
   - 실제 OriginalWorkbook 생성 후 `loadOriginalWorkbookPort.getById(1L)` stub
   - `sut.edit(command)` 호출
   - 실제 workbook의 title이 변경되었는지 `assertThat` 검증
   - `saveOriginalWorkbookPort.save(workbook)` 호출 검증

6. **존재하지 않는 워크북 수정 시 예외 발생**
   - `loadOriginalWorkbookPort.getById(anyLong())` → `CurriculumDomainException` 던지도록 stub
   - 예외 전파 확인

#### `@Nested @DisplayName("원본 워크북 삭제")` — Delete 그룹

7. **배포받은 챌린저가 없으면 삭제 성공**
   - 실제 OriginalWorkbook 생성 후 stub
   - `existsByOriginalWorkbookId(1L)` → `false`
   - `saveOriginalWorkbookPort.delete(workbook)` 호출 검증

8. **배포받은 챌린저가 있으면 예외 발생**
   - `existsByOriginalWorkbookId(1L)` → `true`
   - `CurriculumDomainException(WORKBOOK_HAS_SUBMISSIONS)` 발생
   - `saveOriginalWorkbookPort.delete`가 **호출되지 않음** 검증

#### `@Nested @DisplayName("원본 워크북 상태 일괄 변경")` — ChangeStatus 그룹

9. **READY → RELEASED 배치 변경 성공**
   - `OriginalWorkbook.createAsReady(...)` 2개 생성, ID 주입
   - `loadOriginalWorkbookPort.batchGetByIds(List.of(1L, 2L))` → 2개 반환 stub
   - command 2개 (각 status=RELEASED, requestedMemberId=10L)로 서비스 호출
   - 두 workbook의 `originalWorkbookStatus`가 RELEASED인지 `assertThat` 검증
   - `saveOriginalWorkbookPort.saveAll(any())` 호출 검증

10. **유효하지 않은 상태 전환 시 예외 발생 (RELEASED → DRAFT 불가)**
    - `createAsReady()` 후 `changeStatus(RELEASED, null)`을 먼저 호출해 RELEASED 상태 워크북 준비
    - command status=DRAFT로 서비스 호출
    - `CurriculumDomainException(INVALID_WORKBOOK_STATUS_TRANSITION)` 발생

## Acceptance Criteria

```bash
./gradlew compileJava
./gradlew test --tests "com.umc.product.curriculum.application.service.command.OriginalWorkbookCommandServiceTest"
```

## 검증 절차

1. 위 AC 커맨드를 실행한다.
2. 아키텍처 체크리스트를 확인한다:
    - `@Transactional`이 클래스 레벨에 선언되어 있는가?
    - JPA Repository를 직접 주입받지 않고 Port 인터페이스만 사용하는가?
    - `changeStatusForRelease`가 `batchGetByIds` 한 번의 쿼리로 처리하는가? (반복문 내 개별 `getById` 호출 금지)
    - `releaseAllDue()`가 여전히 `NotImplementedException`인가?
3. 결과에 따라 `phases/original-workbook-command/index.json`의 step 3을 업데이트한다:
    - 성공 → `"status": "completed"`, `"summary": "OriginalWorkbookCommandService create/edit/delete/changeStatusForRelease 구현 완료, 10개 단위 테스트(@Nested) 통과"`
    - 수정 3회 시도 후에도 실패 → `"status": "error"`, `"error_message": "구체적 에러 내용"`

## 금지사항

- `OriginalWorkbookCommandService`에서 `JpaRepository`를 직접 주입받지 마라. 이유: 헥사고날 아키텍처 위반.
- `changeStatusForRelease`에서 반복문 내에서 `getById`를 개별 호출하지 마라. 이유: N+1 쿼리 문제 — ADR-003 위반.
- `releaseAllDue()`를 구현하지 마라. 이유: 이 step의 범위를 벗어난 별도 기능이다. `NotImplementedException` 유지.
- 테스트 메서드명을 영어로 작성하지 마라. 이유: CLAUDE.md 컨벤션상 테스트 이름은 한국어로 작성한다.
- 기존 테스트를 깨뜨리지 마라.
