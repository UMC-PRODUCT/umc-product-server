# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

UMC Product Team 백엔드 서버 - UMC 공식 웹사이트 및 모바일 애플리케이션(Android/iOS)을 지원하는 Spring Boot 기반 API 서버입니다.

### Tech Stack

| Category   | Technology                                |
|------------|-------------------------------------------|
| Framework  | Spring Boot 3.5, Java 21                  |
| Database   | PostgreSQL 18.x, Flyway Migration         |
| ORM        | JPA, QueryDSL                             |
| Auth       | JWT (io.jsonwebtoken 0.12.5)              |
| Docs       | OpenAPI/Swagger                           |
| Monitoring | Prometheus Metrics, OpenTelemetry Tracing |

### Endpoints

| Endpoint     | URL                                         | Port |
|--------------|---------------------------------------------|------|
| Application  | `http://localhost:8080`                     | 8080 |
| Swagger UI   | `http://localhost:8080/docs`                | 8080 |
| OpenAPI JSON | `http://localhost:8080/docs-json`           | 8080 |
| Actuator     | `http://localhost:9090/actuator`            | 9090 |
| Prometheus   | `http://localhost:9090/actuator/prometheus` | 9090 |

---

## Architecture

### Hexagonal Architecture (Ports & Adapters)

```
{domain}/
├── domain/                    # Core Business Logic
│   ├── {Entity}.java         # Entity (도메인 모델)
│   ├── {Domain}{Type}.java   # Enum
│   └── vo/                   # Value Objects
│
├── application/               # Application Layer
│   ├── port/
│   │   ├── in/               # Inbound Ports (UseCase)
│   │   │   ├── command/      # 상태 변경 UseCase
│   │   │   └── query/        # 조회 UseCase
│   │   └── out/              # Outbound Ports
│   │       ├── Load{Domain}Port.java
│   │       └── Save{Domain}Port.java
│   └── service/              # UseCase Implementations
│       ├── command/
│       │   └── {Domain}CommandService.java
│       └── query/
│           └── {Domain}QueryService.java
│
└── adapter/                   # Infrastructure Layer
    ├── in/                   # Driving Adapters
    │   ├── web/
    │   │   ├── {Domain}Controller.java
    │   │   └── dto/
    │   │       ├── request/
    │   │       └── response/
    │   └── scheduler/
    └── out/                  # Driven Adapters
        ├── persistence/
        │   ├── {Domain}Repository.java
        │   ├── {Domain}QueryRepository.java
        │   └── {Domain}PersistenceAdapter.java
        └── external/
            └── {Service}Adapter.java
```

### Global Package Structure

```
com.umc.product/
├── global/
│   ├── config/               # Spring configurations
│   ├── security/             # JWT, Authentication
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── annotation/       # @Public, @CurrentUser
│   │   └── resolver/
│   ├── exception/            # Exception handling
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BusinessException.java
│   │   └── constant/         # ErrorCode, Domain
│   └── response/             # API response
│       ├── ApiResponse.java
│       └── code/
└── {domain}/                 # Feature packages
```

### Domain List & Dependencies

```
Level 0              Level 1              Level 2              Level 3
────────────────────────────────────────────────────────────────────────
common ◄──────────── 모든 도메인이 의존

member ◄───────────── organization
                          │
                          ▼
              ┌───────────┴───────────┐
              │      challenger       │
              └───────────┬───────────┘
                          │
       ┌──────────────────┼──────────────────┐
       ▼                  ▼                  ▼
  curriculum          schedule            notice
                                            
community ◄────────── member, challenger

form ◄─────────────── member (독립적)
```

| Domain       | Description                     | Dependencies             |
|--------------|---------------------------------|--------------------------|
| common       | 공통 (BaseEntity, Exception, DTO) | 없음                       |
| member       | 사용자, OAuth, 약관                  | common                   |
| organization | 기수, 지부, 학교, 스터디                 | common, member           |
| challenger   | 챌린저, 역할, 상벌점                    | member, organization     |
| curriculum   | 커리큘럼, 워크북, 미션                   | challenger               |
| schedule     | 일정, 출석                          | challenger, organization |
| notice       | 공지사항, 읽음, 알림                    | challenger, organization |
| community    | 게시글, 댓글, 번개모임                   | member, challenger       |
| form         | 지원서 폼, 질문, 응답                   | member                   |

---

## Naming Conventions

### Quick Reference Table

| Type                    | Pattern                      | Example                            |
|-------------------------|------------------------------|------------------------------------|
| **Entity**              | `{Domain}`                   | `User`, `Challenger`, `Notice`     |
| **Enum**                | `{Domain}{Status\|Type}`     | `ChallengerStatus`, `ScheduleType` |
| **Value Object**        | `{Name}`                     | `Location`, `AttendanceWindow`     |
| **Command UseCase**     | `{Action}{Domain}UseCase`    | `RegisterChallengerUseCase`        |
| **Query UseCase**       | `Get{Domain}UseCase`         | `GetChallengerUseCase`             |
| **Load Port**           | `Load{Domain}Port`           | `LoadChallengerPort`               |
| **Save Port**           | `Save{Domain}Port`           | `SaveChallengerPort`               |
| **External Port**       | `{Action}{Domain}Port`       | `SendNotificationPort`             |
| **Command Service**     | `{Domain}CommandService`     | `ChallengerCommandService`         |
| **Query Service**       | `{Domain}QueryService`       | `ChallengerQueryService`           |
| **Controller**          | `{Domain}Controller`         | `ChallengerController`             |
| **Request DTO**         | `{Action}{Domain}Request`    | `RegisterChallengerRequest`        |
| **Response DTO**        | `{Domain}{Purpose}Response`  | `ChallengerListResponse`           |
| **Command DTO**         | `{Action}{Domain}Command`    | `RegisterChallengerCommand`        |
| **Info DTO**            | `{Domain}Info`               | `ChallengerInfo`                   |
| **JPA Repository**      | `{Entity}Repository`         | `ChallengerRepository`             |
| **Query Repository**    | `{Domain}QueryRepository`    | `PostQueryRepository`              |
| **Persistence Adapter** | `{Domain}PersistenceAdapter` | `ChallengerPersistenceAdapter`     |
| **External Adapter**    | `{Provider}{Service}Adapter` | `KakaoOAuthAdapter`                |
| **Scheduler**           | `{Domain}Scheduler`          | `AttendanceScheduler`              |

### Action Verbs for UseCase

| Action     | Usage     | Example                    |
|------------|-----------|----------------------------|
| `Register` | 회원/챌린저 등록 | `RegisterUserUseCase`      |
| `Create`   | 리소스 생성    | `CreateNoticeUseCase`      |
| `Update`   | 수정        | `UpdateProfileUseCase`     |
| `Delete`   | 삭제        | `DeletePostUseCase`        |
| `Assign`   | 할당/부여     | `AssignRoleUseCase`        |
| `Submit`   | 제출        | `SubmitWorkbookUseCase`    |
| `Approve`  | 승인        | `ApproveAttendanceUseCase` |
| `Get`      | 조회        | `GetChallengerUseCase`     |
| `Search`   | 검색        | `SearchPostUseCase`        |

### Manage 통합 옵션

CUD(Create, Update, Delete)를 하나의 인터페이스로 통합하고 싶다면 `Manage` 접두사를 사용할 수 있습니다.

| 대상 | 개별형 | 통합형 |
|------|--------|--------|
| **UseCase** | `CreateSchoolUseCase`, `UpdateSchoolUseCase`, `DeleteSchoolUseCase` | `ManageSchoolUseCase` |
| **Port** | `SaveSchoolPort` | `ManageSchoolPort` |

---

## Code Examples

### 1. Entity

```java

@Entity
@Table(name = "challenger")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenger extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;  // ID 참조만 (다른 도메인 Entity 직접 참조 금지)

    @Column(nullable = false)
    private Long gisuId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengerPart part;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengerStatus status;

    @Builder
    private Challenger(Long userId, Long gisuId, ChallengerPart part) {
        this.userId = userId;
        this.gisuId = gisuId;
        this.part = part;
        this.status = ChallengerStatus.ACTIVE;
    }

    // Domain Logic (상태 변경은 도메인 메서드로만)
    public void graduate() {
        validateActive();
        this.status = ChallengerStatus.GRADUATED;
    }

    public void expel() {
        validateActive();
        this.status = ChallengerStatus.EXPELLED;
    }

    private void validateActive() {
        if (this.status != ChallengerStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_CHALLENGER_STATUS);
        }
    }
}
```

### 2. UseCase (Port In)

```java
// Command UseCase
public interface RegisterChallengerUseCase {
    Long register(RegisterChallengerCommand command);
}

// Query UseCase
public interface GetChallengerUseCase {
    ChallengerInfo getById(Long challengerId);

    ChallengerInfo getByUserAndGisu(Long userId, Long gisuId);

    boolean existsById(Long challengerId);
}

// Command Record
public record RegisterChallengerCommand(
        Long userId,
        Long gisuId,
        ChallengerPart part
) {
    public RegisterChallengerCommand {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(gisuId, "gisuId must not be null");
        Objects.requireNonNull(part, "part must not be null");
    }

    public Challenger toEntity() {
        return Challenger.builder()
                .userId(userId)
                .gisuId(gisuId)
                .part(part)
                .build();
    }
}
```

### 3. Port (Port Out)

```java
// Load Port
public interface LoadChallengerPort {
    Optional<Challenger> findById(Long id);

    Optional<Challenger> findByUserIdAndGisuId(Long userId, Long gisuId);

    List<Challenger> findByGisuId(Long gisuId);

    boolean existsByUserIdAndGisuId(Long userId, Long gisuId);
}

// Save Port
public interface SaveChallengerPort {
    Challenger save(Challenger challenger);

    void delete(Challenger challenger);
}

// External Port
public interface SendNotificationPort {
    void send(Long userId, String title, String body);

    void sendAll(List<Long> userIds, String title, String body);
}
```

### 4. Service

```java

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengerCommandService implements RegisterChallengerUseCase {

    private final LoadChallengerPort loadChallengerPort;
    private final SaveChallengerPort saveChallengerPort;
    private final GetUserInfoUseCase getUserInfoUseCase;      // 다른 도메인은 UseCase로
    private final GetOrganizationUseCase getOrganizationUseCase;

    @Override
    public Long register(RegisterChallengerCommand command) {
        // 1. 사용자 존재 확인 (다른 도메인)
        if (!getUserInfoUseCase.existsById(command.userId())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 2. 기수 유효성 확인 (다른 도메인)
        GisuInfo gisu = getOrganizationUseCase.getGisuById(command.gisuId());
        if (!gisu.isActive()) {
            throw new BusinessException(ErrorCode.GISU_NOT_ACTIVE);
        }

        // 3. 중복 등록 확인
        if (loadChallengerPort.existsByUserIdAndGisuId(command.userId(), command.gisuId())) {
            throw new BusinessException(ErrorCode.CHALLENGER_ALREADY_EXISTS);
        }

        // 4. 저장
        Challenger challenger = command.toEntity();
        return saveChallengerPort.save(challenger).getId();
    }
}

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // Query는 readOnly
public class ChallengerQueryService implements GetChallengerUseCase {

    private final LoadChallengerPort loadChallengerPort;
    private final GetUserInfoUseCase getUserInfoUseCase;

    @Override
    public ChallengerInfo getById(Long challengerId) {
        Challenger challenger = loadChallengerPort.findById(challengerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGER_NOT_FOUND));

        UserInfo member = getUserInfoUseCase.getById(challenger.getUserId());
        return ChallengerInfo.of(challenger, member);
    }
}
```

### 5. Adapter

```java
// Persistence Adapter
@Component
@RequiredArgsConstructor
public class ChallengerPersistenceAdapter implements LoadChallengerPort, SaveChallengerPort {

    private final ChallengerRepository repository;

    @Override
    public Optional<Challenger> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Challenger save(Challenger challenger) {
        return repository.save(challenger);
    }

    // ...
}

// Controller
@RestController
@RequestMapping("/api/v1/challengers")
@RequiredArgsConstructor
@Tag(name = "Challenger", description = "챌린저 관리 API")
public class ChallengerController {

    private final RegisterChallengerUseCase registerUseCase;
    private final GetChallengerUseCase getUseCase;

    @PostMapping
    @Operation(summary = "챌린저 등록")
    public ApiResponse<Long> register(
            @AuthenticationPrincipal SecurityUser member,
            @Valid @RequestBody RegisterChallengerRequest request) {
        Long id = registerUseCase.register(request.toCommand(member.getUserId()));
        return ApiResponse.success(id);
    }

    @GetMapping("/{challengerId}")
    @Operation(summary = "챌린저 상세 조회")
    public ApiResponse<ChallengerResponse> getById(@PathVariable Long challengerId) {
        ChallengerInfo info = getUseCase.getById(challengerId);
        return ApiResponse.success(ChallengerResponse.from(info));
    }
}
```

### 6. Request/Response DTO

```java
// Request
public record RegisterChallengerRequest(
                @NotNull(message = "기수 ID는 필수입니다")
                Long gisuId,

                @NotNull(message = "파트는 필수입니다")
                ChallengerPart part
        ) {
    public RegisterChallengerCommand toCommand(Long userId) {
        return new RegisterChallengerCommand(userId, gisuId, part);
    }
}

// Response
public record ChallengerResponse(
        Long id,
        String userName,
        String part,
        String status,
        int generationNumber
) {
    public static ChallengerResponse from(ChallengerInfo info) {
        return new ChallengerResponse(
                info.id(),
                info.userName(),
                info.part().name(),
                info.status().name(),
                info.generationNumber()
        );
    }
}
```

---

## Cross-Domain Communication

### Rules

1. **ID Reference Only**: 다른 도메인 Entity를 직접 참조하지 않고 ID만 저장
2. **UseCase Call**: 다른 도메인 정보가 필요하면 해당 도메인의 Query UseCase 호출
3. **Event Publishing**: 도메인 간 느슨한 결합이 필요하면 이벤트 사용

```java
// ❌ Bad: 다른 도메인 Entity 직접 참조
@ManyToOne
private User member;

// ✅ Good: ID만 저장
@Column(nullable = false)
private Long userId;

// ✅ Good: 필요시 UseCase로 조회
UserInfo memberInfo = getUserInfoUseCase.getById(challenger.getUserId());
```

### Event-Based Communication

```java
// Publisher (schedule 도메인)
@Service
@RequiredArgsConstructor
public class AttendanceCommandService {
    private final ApplicationEventPublisher eventPublisher;

    public void checkAttendance(...) {
        attendance.check(status);

        eventPublisher.publishEvent(new AttendanceCheckedEvent(
                attendance.getChallengerId(),
                attendance.getStatus()
        ));
    }
}

// Listener (challenger 도메인)
@Component
@RequiredArgsConstructor
public class AttendanceEventListener {
    private final AddRewardPenaltyUseCase addRewardPenaltyUseCase;

    @EventListener
    @Transactional
    public void handleAttendanceChecked(AttendanceCheckedEvent event) {
        if (event.status() == AttendanceStatus.ABSENT) {
            addRewardPenaltyUseCase.addPenalty(
                    event.challengerId(),
                    RewardPenaltyType.WARNING,
                    "무단 결석"
            );
        }
    }
}
```

---

## API Response Format

### Success

```json
{
  "success": true,
  "data": {
    ...
  },
  "error": null
}
```

### Error

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "CHALLENGER_NOT_FOUND",
    "message": "챌린저를 찾을 수 없습니다."
  }
}
```

### Pagination

```json
{
  "success": true,
  "data": {
    "content": [
      ...
    ],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "hasNext": true
  },
  "error": null
}
```

---

## Common Enums

### ChallengerStatus

```java
ACTIVE,      // 활동중
GRADUATED,   // 수료
EXPELLED,    // 제명
WITHDRAWN    // 자진탈부
```

### ChallengerPart

```java
PLAN,DESIGN,WEB,IOS,ANDROID,SPRINGBOOT,NODEJS
```

### RoleType

```java
// 중앙
CENTRAL_PRESIDENT,CENTRAL_VICE_PRESIDENT,
CENTRAL_DIRECTOR,CENTRAL_MANAGER,CENTRAL_PART_LEADER,

// 지부
CHAPTER_LEADER,CHAPTER_STAFF,

// 학교
SCHOOL_PRESIDENT,SCHOOL_VICE_PRESIDENT,
SCHOOL_PART_LEADER,SCHOOL_STAFF,

// 일반
CHALLENGER
```

### OrganizationType

```java
CENTRAL,  // 중앙운영사무국
CHAPTER,  // 지부
SCHOOL    // 학교
```

### AttendanceStatus

```java
PENDING,          // 대기
PRESENT,          // 출석
PRESENT_PENDING,  // 출석 승인 대기
LATE,             // 지각
LATE_PENDING,     // 지각 승인 대기
ABSENT,           // 결석
EXCUSED,          // 인정결석
EXCUSED_PENDING   // 인정결석 승인 대기
```

---

## Testing Guide

### Unit Test Example

```java

@ExtendWith(MockitoExtension.class)
class ChallengerCommandServiceTest {

    @Mock
    LoadChallengerPort loadChallengerPort;
    @Mock
    SaveChallengerPort saveChallengerPort;
    @Mock
    GetUserInfoUseCase getUserInfoUseCase;
    @Mock
    GetOrganizationUseCase getOrganizationUseCase;

    @InjectMocks
    ChallengerCommandService sut;

    @Test
    void 챌린저_등록_성공() {
        // given
        var command = new RegisterChallengerCommand(1L, 1L, ChallengerPart.SPRINGBOOT);

        given(getUserInfoUseCase.existsById(1L)).willReturn(true);
        given(getOrganizationUseCase.getGisuById(1L))
                .willReturn(new GisuInfo(1L, 9, true));
        given(loadChallengerPort.existsByUserIdAndGisuId(1L, 1L)).willReturn(false);
        given(saveChallengerPort.save(any())).willAnswer(inv -> {
            Challenger c = inv.getArgument(0);
            ReflectionTestUtils.setField(c, "id", 1L);
            return c;
        });

        // when
        Long result = sut.register(command);

        // then
        assertThat(result).isEqualTo(1L);
        then(saveChallengerPort).should().save(any(Challenger.class));
    }

    @Test
    void 존재하지_않는_사용자면_예외() {
        // given
        var command = new RegisterChallengerCommand(999L, 1L, ChallengerPart.SPRINGBOOT);
        given(getUserInfoUseCase.existsById(999L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> sut.register(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }
}
```

---

## Git Conventions

### Commit Message

```
tag: description

# Tags (lowercase)
feat:     새로운 기능
fix:      버그 수정
refactor: 리팩토링
docs:     문서 수정
test:     테스트 추가/수정
chore:    빌드, 설정 변경

# Example
feat: implement challenger registration
fix: resolve null pointer in attendance check
```

### PR Review Priority (Pn)

| Level | Description  | Example        |
|-------|--------------|----------------|
| P1    | Critical     | 보안 취약점, 데이터 손실 |
| P2    | Significant  | 성능, 확장성 문제     |
| P3    | Code Quality | 가독성, 컨벤션       |
| P4    | Alternative  | 주관적 개선         |
| P5    | Minor        | 오타, 질문         |

 
---

## Important Rules

### Must Do ✅

- Entity 상태 변경은 도메인 메서드로만
- 다른 도메인은 UseCase로 접근
- Controller는 UseCase에만 의존
- Command Service에 `@Transactional`
- Query Service에 `@Transactional(readOnly = true)`
- Request/Response DTO에 변환 메서드 (`toCommand()`, `from()`)

### Must Not ❌

- Entity에 `@Setter` 사용
- Controller에서 비즈니스 로직 처리
- 다른 도메인 Entity 직접 참조 (`@ManyToOne private User member`)
- 순환 의존성 (도메인 간 양방향 의존)
- Adapter에서 트랜잭션 관리

---

## Code Generation Request Format

### When Requesting New Feature

```
{domain} 도메인의 {UseCase}를 구현해줘.

- 기능 설명
- 비즈니스 규칙
- 검증 조건
- 연관 도메인
```

### Example Request

```
curriculum 도메인의 SubmitWorkbookUseCase를 구현해줘.

- 챌린저가 주차별 워크북의 미션을 제출
- 미션 타입(LINK, MEMO, PLAIN)에 따른 검증 필요
- 제출 시 ChallengerWorkbook 상태를 PENDING으로 변경
- 이미 제출된 워크북은 재제출 불가
```

### Expected Response

1. UseCase 인터페이스
2. Command record
3. Service 구현체
4. Port 인터페이스
5. 단위 테스트

---

## Configuration Notes

| Setting          | Value      | Note                |
|------------------|------------|---------------------|
| Default Profile  | `local`    | 실수로 운영 접근 방지        |
| Application Port | 8080       |                     |
| Management Port  | 9090       | Actuator 전용         |
| HikariCP Pool    | 50         | 고정 커넥션 수            |
| OSIV             | `false`    | 지연 로딩은 Service에서 처리 |
| JPA DDL          | `validate` | 스키마 변경은 Flyway로만    |
| Trace Sampling   | 0.1 (10%)  |                     |

---

## References

- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Get Your Hands Dirty on Clean Architecture](https://github.com/thombergs/buckpal)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
