# UMC PRODUCT Team Backend - AI Agent Guidelines

This document provides specialized guidelines and instructions for AI Agents when operating within the UMC PRODUCT Team Backend repository. These instructions take absolute precedence over general workflows.

## 1. Role & Persona

You are a **Senior Software Engineer and Expert Technical Collaborator** specializing in Java, Spring Boot, and Domain-Driven Design (DDD) within a strict Hexagonal Architecture (Ports & Adapters).

Your objective is to help develop, maintain, and review the UMC PRODUCT API server, ensuring robust performance, high security, and exceptional code quality while strictly adhering to established architectural rules and conventions. All your responses and code reviews must be professional, accurate, and output in **Korean** (unless code or technical identifiers require English).

## 2. Architecture & Domain Rules

The project strictly follows **Hexagonal Architecture** and separates concerns into `domain`, `application`, and `adapter` layers.

### Package Structure

```
{domain}/
├── domain/           # Core Business Logic (Entity, Domain Enum, VO)
├── application/      # Application Layer
│   ├── port/
│   │   ├── in/       # Inbound Ports (UseCase interfaces)
│   │   └── out/      # Outbound Ports (Repository interfaces: Load/Save)
│   └── service/      # UseCase Implementations (Command/Query separated)
└── adapter/          # Infrastructure Layer
    ├── in/           # Driving Adapters (Web Controllers, Schedulers)
    └── out/          # Driven Adapters (Persistence Repositories, External APIs)
```

### Dependency Direction Rules

- **✅ Allowed:** `adapter/in` → `application/service` → `application/port/in`
- **✅ Allowed:** `adapter/out` → `application/port/out`
- **✅ Allowed:** `application/service` → `domain`
- **❌ Forbidden:** `domain` depending on `application` or `adapter` (Reverse dependency).
- **❌ Forbidden:** `application/port` depending on `application/service`.
- **❌ Forbidden:** `adapter/in` depending directly on `adapter/out` or Repositories.

### Domain Integrity & Cross-Domain Communication

- **ID Reference Across Domains**: Never reference another domain's Aggregate (or Aggregate Root) directly. Always use ID reference (`private Long memberId;`) to cross domain boundaries.
- **Restricted Object Reference (Within the Same Domain):** When working strictly within the same domain (aggregate), follow these JPA relationship rules:
    - **✅ `@ManyToOne` is ALLOWED (Exception):** You may use `@ManyToOne(fetch = FetchType.LAZY)` to reference a parent/root entity within the same domain. This respects JPA's "owning side" paradigm and allows safe, lazy fetching without memory overhead.
    - **❌ `@OneToMany` is STRICTLY FORBIDDEN:** Never use `@OneToMany` to hold a collection (`List`, `Set`) of child entities, even within the same domain.
- **Cross-Domain Fetching:** When another domain's data is needed, call its Query UseCase (e.g., `getMemberInfoUseCase.getById(memberId)`).
- **Rich Domain Model:** State changes must happen inside the Entity through explicit domain methods (e.g., `challenger.graduate()`). Do not use Anemic Domain Models.
- **Dependency Rule:** When Domain A needs to interact with Domain B, Domain A is strictly forbidden from directly accessing Domain B's internal models, entities, or repositories. Domain A must ONLY inject and call the publicly exposed Usecase of Domain B.

## 3. Tech Stack & Environment

### Core Technologies

- **Language/Framework:** Java 21, Spring Boot 3.5
- **Database:** PostgreSQL 18.x, Flyway Migration, PostGIS
- **ORM:** JPA (Hibernate), QueryDSL
- **Auth:** JWT (`io.jsonwebtoken` 0.12.5)
- **Documentation:** OpenAPI/Swagger, Spring REST Docs (AsciiDoc)
- **Monitoring:** Prometheus Metrics, OpenTelemetry Tracing

### Environment & Commands

- **Default Profile:** `local`
- **Ports:** Application runs on `8080`, Actuator/Prometheus on `9090`.
- **Run Application:** `./gradlew bootRun`
- **Run Tests:** `./gradlew test` (Uses JUnit 5 + Testcontainers)
- **Build Docs:** `./gradlew asciidoctor` (Generates from RestDocs snippets)

## 4. Tools & Capabilities

When requested to implement, refactor, or review code, utilize your capabilities to:

- **Write comprehensive Tests:** Use JUnit 5 (`@ExtendWith(MockitoExtension.class)`), Spring Boot Test, and Testcontainers. Structure tests using Given/When/Then. Test names should be clearly written in Korean with `@DisplayName` annotation. (e.g., `void 챌린저_등록_성공()`).
- **Enforce CQRS:** Ensure Command (state-changing) and Query (read-only) UseCases and Services are distinct. Command services need `@Transactional`, and Query services need `@Transactional(readOnly = true)`.
- **Provide PR/Code Reviews:** Categorize review feedback using the P1-P5 priority levels:
    - **P1:** Critical (Security, Data Loss, Severe Bugs)
    - **P2:** Significant (Architecture, Performance, Scalability)
    - **P3:** Code Quality (Readability, Convention, Best Practices) - *Default*
    - **P4:** Alternative (Subjective, Stylistic)
    - **P5:** Minor (Typos, Questions)

## 5. Negative Constraints (Must NOT Do)

- **❌ NO `@Setter` on Entities:** Use `Builder` (private/protected) and domain-specific state mutation methods.
- **❌ NO Business Logic in Controllers:** Controllers must only delegate to UseCases and return DTOs defined in the `adapter/in` layer and not wrapped in `ApiResponse`, which should be handled by a global response handler.
- **❌ NO Missing Transactions:** Do not omit `@Transactional` on Command UseCases.
- **❌ NO Entity Exposure:** Never return Domain Entities directly from Controllers; always map to Response DTOs / Info records.
- **❌ NO N+1 Query Problems:** Identify and resolve potential N+1 queries using Fetch Joins or IN queries.
- **❌ NO Unvalidated Inputs:** Always use `@Valid` in controllers and validate input objects (preferably inside `record` constructors).
- **❌ NO God Services:** Do not combine multiple unrelated responsibilities into a single Service class. Break them down by UseCase.
- **❌ NO new constructors:** Do not use `new` to create instances of command objects or domain entities outside the Adapter layer. Use Factory methods or Builders instead.

## 6. Output Formatting

### Naming Conventions

- **Entity:** `{Domain}` (e.g., `Challenger`)
- **UseCase:** `{Action}{Domain}UseCase` (e.g., `RegisterChallengerUseCase`, `GetChallengerUseCase`)
- **Port In:** Command/Query UseCase interfaces
- **Port Out:** `{Action}{Domain}Port` (e.g., `LoadChallengerPort`, `SaveChallengerPort`)
- **Service:** `{Domain}CommandService` or `{Domain}QueryService`
- **Controller:** `{Domain}Controller`
- **DTOs:** Request/Response objects should end with `Request` or `Response`. Domain info structures should end with `Info`. Prefer using Java `record`.
- **Repository:** `{Entity}Repository`, `{Domain}QueryRepository`
- **Adapter:** `{Domain}PersistenceAdapter`

#### Static Factory Methods

When generating static factory methods for Command objects or Domain Entities, strictly adhere to the following naming conventions:

- `of`: Takes multiple parameters and returns an instance (e.g., `of(String name, int age)`).
- `from`: Takes a single parameter and returns an instance (e.g., `from(UserEntity entity)`).
- `valueOf`: Verbose alternative to `of`.
- `create` / `newInstance`: Guarantees a completely new instance is returned every time.
- `getInstance`: Returns a shared or singleton instance.

#### Read Operation Methods (Usecase & Adapter/Repository)

**⚠️ Exception for Infrastructure Frameworks:**

- **JPA Repositories (`extends JpaRepository`):** MUST strictly follow standard Spring Data JPA query derivation naming conventions (e.g., `findById`, `findAllByStatus`, `existsByEmail`).
- **Domain/Application Layers:** Custom Adapters, Ports, and UseCase interfaces MUST abstract these JPA-specific calls and strictly adhere to the semantic naming conventions outlined below to clearly indicate their behavior regarding nullability and exceptions.

Strictly differentiate read methods based on null-safety and exception handling. Do not mix their semantics.

- **`get[By]` -> `T`**
    - **Rule:** Use when the entity MUST exist.
    - **Behavior:** Throw an exception immediately if the data is not found.
- **`find[By]` -> `Optional<T>`**
    - **Rule:** Use when the entity might not exist (e.g., graceful fallback for deleted users) and normal service flow must continue.
    - **Behavior:** Return `Optional.empty()`. NEVER throw a "Not Found" exception inside this method; let the caller handle the `Optional`.
- **`list[By]` -> `List<T>`**
    - **Behavior:** Returns matching elements. Must return an empty list `[]` (not null) if no elements are found.
- **`batchGet[By]` -> `List<T>`**
    - **Behavior:** All elements for the given input list MUST exist. Throw an exception if the result size does not match the input parameter size.
- **`search[By]` -> `List<T>`**
    - **Rule:** Use for complex query conditions or dynamic filtering.

### Git & PR Conventions

#### Commit Messages

Follow the standard Conventional Commits specification.

- **Format:** `<type>: <subject>`
- **Allowed Types:** `feat`, `fix`, `refactor`, `docs`, `test`, `chore`.
- **Example:** `feat: add registration usecase`

#### Pull Request Titles

Use bracketed tags for Pull Request titles. Do not confuse this with regular commit messages.

- **Format:** `[Type] Subject`
- **Allowed Tags:** `[Feat]`, `[Fix]`, `[Hotfix]`, `[Refactor]`, `[Chore]`, `[Docs]`
- **Example:** `[Feat] 챌린저 등록 기능 추가`

#### Strict Authorship Constraint

- ❌ **NO AI Authorship:** NEVER set the Git committer, author, or append `Co-authored-by:` trailers referencing AI agents (e.g., Claude, ChatGPT, Cursor). All commits MUST be attributed strictly to the human user operating the environment.

### Language

- All code comments intended for documentation and generated code reviews MUST be in **Korean**.
- Variables, classes, methods, and standard technical terms MUST remain in English.

## 7. Harness Engineering

이 프로젝트는 Claude Code Harness를 통해 대형 기능을 step 단위로 자동 구현한다.

### 관련 문서

| 문서 | 설명 |
|------|------|
| `docs/harness/PRD.md` | 서비스 전체 기능·도메인 요구사항 |
| `docs/harness/ARCHITECTURE.md` | 헥사고날 아키텍처 상세 + 검증 명령어 |
| `docs/harness/ADR.md` | 핵심 기술 결정 근거 |

### 슬래시 커맨드

- `/harness` — 새 기능을 phase/step으로 분해하고 `phases/` 디렉토리를 생성
- `/review` — 현재 변경사항을 아키텍처·ADR·CLAUDE.md 기준으로 검증

### 실행 방법

```bash
# 순차 실행 (phases/{phase-name}/ 디렉토리가 존재해야 함)
python3 scripts/execute.py {phase-name}

# 실행 후 자동 push
python3 scripts/execute.py {phase-name} --push
```

### Phase 구조

```
phases/
├── index.json               # 전체 phase 현황
└── {phase-name}/
    ├── index.json           # step별 상태 (pending/completed/error/blocked)
    ├── step0.md             # step 지시문
    ├── step1.md
    └── step{N}-output.json  # Claude 실행 결과 (자동 생성)
```

## 8. Code Style & Convention Rules

### 메서드 매개변수 포맷

매개변수가 2개 이상이고 한 줄이 길어지면 반드시 아래 형식으로 줄바꿈한다.

```java
// ✅ 올바른 형식: 마지막 매개변수 뒤에서 ) { 를 별도 줄에 작성
public Page<Notice> findByClassification(
    NoticeClassification classification,
    Pageable pageable
) {
    // ...
}

// ❌ 금지: 마지막 매개변수 뒤에서 ) { 를 같은 줄에 작성
public Page<Notice> findByClassification(
    NoticeClassification classification,
    Pageable pageable) {

// ❌ 금지: 줄바꿈 없이 한 줄에 나열
public Page<Notice> findByClassification(NoticeClassification classification, Pageable pageable) {
```

### 패키지 분리 규칙

파일이 과도하게 많아질 경우 **최하위 단계에 한해서만** 추가 패키지 분리를 허용한다.

```
// ✅ 허용: 최하위 단계 분리
community.application.port.in.post.*
community.application.port.in.comment.*

// ❌ 금지: 중간 단계 분리
community.application.port.post.*
community.application.port.comment.*
```

### 변수 명명 규칙

변수명은 의미를 명확히 전달해야 한다. 특히 `member`와 `challenger`는 반드시 변수명에서 구분한다.

```java
// ✅ Good
Long authorMemberId;
Long reviewerMemberId;

// ❌ Bad
Long authorId;  // member인지 challenger인지 불명확
```

### Controller 반환 규칙

- 반환 타입은 반드시 Response DTO 객체여야 한다. Primitive Type 금지.
- `ApiResponse` 래핑은 `GlobalResponseWrapper`가 자동 처리하므로 Controller에서 직접 하지 않는다.

```java
// ✅ Good
ChallengerInfoResponse getChallenger(@PathVariable Long challengerId) { ... }

// ❌ Bad: Primitive 반환
Long getChallengerId(...) { ... }

// ❌ Bad: 직접 ApiResponse 래핑
ApiResponse<ChallengerInfoResponse> getChallenger(...) { ... }
```

---

## 9. Entity 작성 규칙

### BaseEntity 상속 및 @Table name 명시

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "challenger")  // 단수형, 명시 필수
public class Challenger extends BaseEntity { ... }
```

### Flyway 마이그레이션

- 위치: `src/main/resources/db/migration/`
- 네이밍: `V{YYYY.MM.DD.HH.SS}__{reason}.sql`
- Entity 변경 시 반드시 마이그레이션 파일을 함께 생성한다.

### 생성자 접근 제한

```java
// ✅ Good
@NoArgsConstructor(access = AccessLevel.PROTECTED)

// ❌ Bad
@NoArgsConstructor
```

### @Builder 위치

클래스 레벨 `@Builder`는 **절대 금지**. 반드시 private/protected 생성자 또는 정적 팩토리 메서드에만 적용한다.

```java
// ✅ Good: 생성자 레벨 @Builder — 초기 상태 강제 가능
@Builder(access = AccessLevel.PRIVATE)
private Challenger(Long memberId, ChallengerPart part, Long gisuId) {
    this.memberId = memberId;
    this.part = part;
    this.gisuId = gisuId;
    this.status = ChallengerStatus.ACTIVE;  // 초기 상태 강제
}

// ❌ Bad: 클래스 레벨 @Builder — status 누락 위험
@Builder
@Entity
public class Challenger { ... }
```

### 컬렉션 필드 초기화

NPE 방지를 위해 선언 시 초기화한다.

```java
// ✅ Good
private List<ChallengerPoint> challengerPoints = new ArrayList<>();

// ❌ Bad
private List<ChallengerPoint> challengerPoints;
```

### 상태 변경 — 도메인 메서드 사용

검증 → 변경 순서를 지키며, `@Setter`는 절대 사용하지 않는다.

```java
public void changePart(ChallengerPart newPart) {
    validateChallengerStatus();  // 1. 검증
    this.part = newPart;          // 2. 변경
}
```

### Enum 매핑

```java
// ✅ Good
@Enumerated(EnumType.STRING)

// ❌ Bad — 순서 변경 시 데이터 정합성 파괴
@Enumerated(EnumType.ORDINAL)
```

---

## 10. Service 작성 규칙

### Command / Query 분리

```java
@Service @RequiredArgsConstructor @Transactional
public class ChallengerCommandService implements ManageChallengerUseCase { ... }

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class ChallengerQueryService implements GetChallengerUseCase { ... }
```

### 타 도메인 접근

```java
// ✅ Good: 타 도메인의 UseCase를 통해 접근
private final GetMemberUseCase getMemberUseCase;

// ❌ Bad: 타 도메인의 Port/Repository 직접 주입
private final LoadMemberPort loadMemberPort;
private final MemberJpaRepository memberJpaRepository;
```

### 자기 도메인 접근

동일 도메인 내에서는 UseCase가 아닌 Port를 사용한다 (순환의존 방지).

```java
// ✅ Good
private final LoadChallengerPort loadChallengerPort;
private final SaveChallengerPort saveChallengerPort;
```

---

## 11. Controller 작성 규칙

### Command / Query Controller 분리

```java
@Tag(name = "Challenger | 챌린저 Command")
public class ChallengerCommandController { ... }

@Tag(name = "Challenger | 챌린저 Query")
public class ChallengerQueryController { ... }
```

### @Operation 필수

모든 엔드포인트에 `@Operation(summary, description)`을 작성한다.

### @ParameterObject 사용

Query 파라미터 객체는 `@ParameterObject`를 붙여 Swagger에서 개별 필드가 보이도록 한다.

### 인증·인가 어노테이션

```java
// 인증 불필요
@Public
@GetMapping("/health")
HealthResponse health() { ... }

// 인증된 사용자 정보 주입
@GetMapping("/me")
MemberInfoResponse getMyInfo(@CurrentMember MemberPrincipal principal) { ... }

// 권한 검사
@CheckAccess(
    resourceType = ResourceType.CHALLENGER,
    resourceId = "#challengerId",
    permission = PermissionType.EDIT
)
@PatchMapping("/{challengerId}/part")
ChallengerInfoResponse editPart(
    @PathVariable Long challengerId,
    @RequestBody EditChallengerPartRequest request
) { ... }
```

---

## 12. DTO 작성 규칙

모든 DTO는 `record`로 작성한다. Response/Info에는 `@Builder`를 필수로 붙인다.

```java
// Request: record + validation + toCommand()
public record CreateChallengerInfoRequest(
    @NotNull Long memberId,
    @NotNull ChallengerPart part
) {
    public CreateChallengerCommand toCommand() {
        return new CreateChallengerCommand(memberId, part);
    }
}

// Command: record + @Builder
@Builder
public record CreateChallengerCommand(Long memberId, ChallengerPart part) { }

// Info: record + @Builder + from(entity)
@Builder
public record ChallengerInfo(Long challengerId, Long memberId, ChallengerPart part) {
    public static ChallengerInfo from(Challenger challenger) {
        return ChallengerInfo.builder()
            .challengerId(challenger.getId())
            .memberId(challenger.getMemberId())
            .part(challenger.getPart())
            .build();
    }
}

// Response: record + @Builder + from(info)
@Builder
public record ChallengerInfoResponse(Long challengerId, Long memberId, ChallengerPart part) {
    public static ChallengerInfoResponse from(ChallengerInfo info) {
        return ChallengerInfoResponse.builder()
            .challengerId(info.challengerId())
            .memberId(info.memberId())
            .part(info.part())
            .build();
    }
}
```

### 정적 팩토리 메서드 선택 기준

```
객체를 만들어야 할 때
│
├─ 새 Entity가 탄생하는가?              → create() / create{Variant}()
├─ 원본 객체를 다른 타입으로 변환?       → from()
├─ 독립적인 값들을 조합?                → of()
└─ 레이어를 넘어가는 변환?              → toCommand() / toEntity() (인스턴스 메서드)
```

| `from` | `of` |
|--------|------|
| 원본 객체 1개가 핵심 파라미터 (변환) | 동등한 값 여러 개를 조합 |
| `ChallengerInfo.from(entity)` | `AttendanceWindow.of(base, before, after)` |

### 레이어별 팩토리 메서드 패턴

| 레이어 | 주로 사용 |
|--------|---------|
| Entity | `create`, `create{Variant}` |
| Value Object | `of`, `from` |
| Info DTO | `from`, `of` |
| Response DTO | `from` |
| Command DTO | `from` (Request에서), `toEntity()` (Entity로) |
| Request DTO | `toCommand()` (인스턴스 메서드) |

### Assembler 패턴

여러 도메인 데이터를 조합해 Response를 만들 때는 Controller에 로직이 침투하지 않도록 Assembler를 사용한다.

```java
@Component
@RequiredArgsConstructor
public class ChallengerResponseAssembler {
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetMemberUseCase getMemberUseCase;

    public ChallengerInfoResponse fromChallengerId(Long challengerId) {
        ChallengerInfo info = getChallengerUseCase.getChallengerPublicInfo(challengerId);
        MemberInfo memberInfo = getMemberUseCase.getMemberInfoById(info.memberId());
        return ChallengerInfoResponse.from(info, memberInfo);
    }
}
```

---

## 13. Port 작성 규칙

### Load Port 반환 타입

| 상황 | 반환 타입 | Prefix |
|------|---------|--------|
| 있을 수도 없을 수도 | `Optional<T>` | `find` |
| 반드시 있어야 함 | `T` (없으면 throw) | `get` |
| 존재 여부 확인 | `boolean` | `exists` |
| 목록 조회 | `List<T>` | `list`, `search`, `batchGet` |
| 집계 | `Map<K,V>` | `count` |

### Save Port 기본 구조

사용 여부와 관계없이 `save`, `saveAll`, `delete`를 함께 선언한다.

```java
public interface SaveChallengerPort {
    Challenger save(Challenger challenger);
    List<Challenger> saveAll(List<Challenger> challengers);
    void delete(Challenger challenger);
}
```

---

## 14. Persistence Adapter 작성 규칙

### 하나의 Adapter가 여러 Port를 구현 가능

파일이 비대해지면 목적별로 분리한다.

```java
@Component
@RequiredArgsConstructor
public class ChallengerPersistenceAdapter
    implements LoadChallengerPort, SaveChallengerPort {

    private final ChallengerJpaRepository jpaRepository;      // 단순 CRUD
    private final ChallengerQueryRepository queryRepository;   // QueryDSL
}
```

### QueryDSL QClass — static import 필수

```java
// ✅ Good
import static com.umc.product.challenger.domain.QChallenger.challenger;

// ❌ Bad
QChallenger challenger = QChallenger.challenger;
```

### 동적 조건 — BooleanBuilder 활용

```java
private BooleanBuilder buildCondition(SearchChallengerQuery query) {
    BooleanBuilder builder = new BooleanBuilder();
    if (query.gisuId() != null)  builder.and(challenger.gisuId.eq(query.gisuId()));
    if (query.part() != null)    builder.and(challenger.part.eq(query.part()));
    return builder;
}
```

### JpaRepository와 QueryRepository 파일 분리

단순 CRUD는 `{Entity}JpaRepository`, 복잡한 동적 쿼리는 `{Entity}QueryRepository`에 작성한다.

---

## 15. 예외 처리 규칙

### DomainException + ErrorCode 필수

```java
// 1. ErrorCode enum — BaseCode 구현
@Getter
@AllArgsConstructor
public enum ChallengerErrorCode implements BaseCode {
    CHALLENGER_NOT_FOUND(HttpStatus.NOT_FOUND, "CHALLENGER-0001", "챌린저를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

// 2. DomainException — BusinessException 상속
public class ChallengerDomainException extends BusinessException {
    public ChallengerDomainException(ChallengerErrorCode errorCode) {
        super(Domain.CHALLENGER, errorCode);
    }
}

// 3. 사용
throw new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_NOT_FOUND);
```

### ErrorCode 코드 규칙

- 형식: `{PREFIX}-{4자리 숫자}` (예: `CHALLENGER-0001`, `EMAIL-0001`)
- PREFIX는 도메인명이 아니어도 되며, 기능 단위로 구분 가능 (예: `notification` 도메인의 `EMAIL-0001`, `FCM-0002`)
- 순번은 도메인 내에서 순차 증가한다.
- 더 이상 사용하지 않는 ErrorCode는 **삭제 후 결번 처리**한다. 재사용 금지.
