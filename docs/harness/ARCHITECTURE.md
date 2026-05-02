# ARCHITECTURE — UMC PRODUCT Backend

## 아키텍처 패턴

**Hexagonal Architecture (Ports & Adapters)** 를 엄격하게 적용한다. 도메인 로직이 인프라(JPA, HTTP, 외부 API)로부터 완전히 격리되어야 한다.

---

## 패키지 구조

```
com.umc.product/
├── {domain}/
│   ├── domain/                  # 핵심 비즈니스 로직
│   │   ├── {Entity}.java        # JPA Entity + 도메인 메서드
│   │   ├── enums/               # 도메인 열거형
│   │   └── exception/           # 도메인 예외 (DomainException, ErrorCode)
│   │
│   ├── application/             # 유스케이스 계층
│   │   ├── port/
│   │   │   ├── in/              # Inbound Port (UseCase 인터페이스)
│   │   │   │   ├── command/     # 상태 변경 UseCase
│   │   │   │   │   └── dto/     # Command DTO (record)
│   │   │   │   └── query/       # 조회 UseCase
│   │   │   │       └── dto/     # Info DTO (record)
│   │   │   └── out/             # Outbound Port (Load/Save 인터페이스)
│   │   └── service/
│   │       ├── command/         # {Domain}CommandService
│   │       └── query/           # {Domain}QueryService
│   │
│   └── adapter/                 # 인프라 계층
│       ├── in/
│       │   └── web/
│       │       └── v{N}/        # 버전별 REST Controller
│       │           └── dto/
│       │               ├── request/   # Request DTO (record)
│       │               └── response/  # Response DTO (record)
│       └── out/
│           └── persistence/     # JPA Repository, PersistenceAdapter
│
├── common/                      # 공유 도메인 모델 (ChallengerPart 등 열거형)
├── global/                      # 전역 설정 (Security, Exception Handler, Response)
├── authentication/              # 인증 (JWT 발급·검증)
├── authorization/               # 인가 (Casbin)
└── audit/                       # Audit 로깅
```

---

## 현재 도메인 목록

| 도메인 | 설명 |
|--------|------|
| `curriculum` | 커리큘럼·워크북·미션·제출물·피드백 관리 (핵심) |
| `challenger` | 챌린저(부원) 등록·관리 |
| `member` | 멤버 계정·인증 |
| `organization` | 기수·파트·스터디 그룹 |
| `community` | 게시판 |
| `notice` | 공지사항 |
| `survey` | 설문 |
| `schedule` | 일정 관리 |
| `notification` | 알림 |
| `storage` | 파일 저장 |
| `term` | 약관 |
| `project` | 프로젝트 |

---

## 의존성 방향 규칙

```
adapter/in  →  application/port/in  ←  application/service
                                              ↓
                                       application/port/out
                                              ↓
                                        adapter/out
```

- **허용:** `adapter/in` → `application/service` (UseCase 주입)
- **허용:** `adapter/out` → `application/port/out` (Port 구현)
- **허용:** `application/service` → `domain`
- **금지:** `domain`이 `application` 또는 `adapter`에 의존
- **금지:** `adapter/in`이 `adapter/out` 또는 Repository에 직접 의존
- **금지:** 도메인 간 직접 Entity 참조 (ID만 참조)

---

## 크로스 도메인 통신

도메인 A가 도메인 B의 데이터가 필요하면:
1. 도메인 B의 **UseCase 인터페이스**만 주입한다.
2. 도메인 B의 Entity·Repository에 직접 접근하지 않는다.

```java
// ✅ 올바른 방식
private final GetChallengerUseCase getChallengerUseCase;

// ❌ 금지
private final ChallengerJpaRepository challengerJpaRepository;
```

---

## JPA 관계 매핑 규칙

| 관계 | 허용 여부 | 이유 |
|------|-----------|------|
| `@ManyToOne(fetch = LAZY)` | ✅ 허용 | 같은 도메인 내 부모 참조 |
| `@OneToMany` | ❌ 금지 | Aggregate 비대화, N+1, 트랜잭션 경계 침범 |

역방향 컬렉션 탐색은 Port의 IN 쿼리 메서드로 처리한다:
```java
List<WeeklyCurriculum> findByCurriculumIdIn(List<Long> curriculumIds);
```

---

## CQRS 규칙

| 분류 | 클래스명 | 트랜잭션 |
|------|---------|---------|
| 상태 변경 | `{Domain}CommandService` | `@Transactional` |
| 조회 | `{Domain}QueryService` | `@Transactional(readOnly = true)` |

---

## N+1 방지 원칙

목록 조회 시 반복 단건 조회는 금지한다. 반드시 IN 쿼리 배치 로딩 후 메모리 그룹핑으로 처리한다.

```java
// ❌ N+1 발생
weeklyCurriculums.forEach(wc ->
    loadWorkbookPort.findByWeeklyCurriculumId(wc.getId())  // N번 쿼리
);

// ✅ 배치 조회
List<Long> ids = weeklyCurriculums.stream().map(WeeklyCurriculum::getId).toList();
List<OriginalWorkbook> all = loadWorkbookPort.findByWeeklyCurriculumIdIn(ids);  // 1번 쿼리
Map<Long, List<OriginalWorkbook>> byWcId = all.stream()
    .collect(Collectors.groupingBy(wb -> wb.getWeeklyCurriculum().getId()));
```

---

## 네이밍 컨벤션

| 대상 | 패턴 | 예시 |
|------|------|------|
| Entity | `{Domain}` | `Curriculum`, `ChallengerWorkbook` |
| UseCase (in) | `{Action}{Domain}UseCase` | `GetCurriculumUseCase` |
| Port (out) | `{Action}{Domain}Port` | `LoadCurriculumPort`, `SaveWorkbookPort` |
| Service | `{Domain}CommandService` / `{Domain}QueryService` | `CurriculumQueryService` |
| Controller | `{Domain}Controller` / `{Domain}V2Controller` | `CurriculumQueryV2Controller` |
| Adapter | `{Domain}PersistenceAdapter` | `CurriculumPersistenceAdapter` |
| JPA Repository | `{Entity}JpaRepository` | `OriginalWorkbookJpaRepository` |

### Port 메서드 네이밍

| 접두사 | 반환 | 동작 |
|--------|------|------|
| `get[By]` | `T` | 반드시 존재. 없으면 즉시 예외 |
| `find[By]` | `Optional<T>` | 없을 수 있음. 예외 금지 |
| `list[By]` / `findBy...In` | `List<T>` | 빈 리스트 반환 가능 |
| `batchGet[By]` | `List<T>` | 전부 존재해야 함. 누락 시 예외 |
| `search[By]` | `List<T>` | 복합 조건 동적 조회 |

---

## 검증·빌드 명령어

```bash
# 컴파일 검증
./gradlew compileJava

# 테스트 실행 (JUnit 5 + Testcontainers)
./gradlew test

# 서버 실행 (local 프로필)
./gradlew bootRun

# REST Docs 빌드
./gradlew asciidoctor
```
