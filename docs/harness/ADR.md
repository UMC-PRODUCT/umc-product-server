# ADR — Architecture Decision Records

## ADR-001: Hexagonal Architecture 채택

**날짜:** 2024  
**상태:** 확정

### 결정

전체 서버를 Hexagonal Architecture (Ports & Adapters) 기반으로 설계한다.

### 이유

- 도메인 로직을 JPA, Spring MVC, 외부 API로부터 격리하여 테스트 용이성 확보
- Port 인터페이스를 교체하는 것만으로 인프라를 교체 가능
- CQRS 패턴과 자연스럽게 결합 가능

### 결과

- 모든 도메인은 `domain` / `application` / `adapter` 3계층으로 구성된다
- `application/service`는 JPA, HTTP 등 인프라 타입을 직접 임포트하지 않는다
- Controller는 UseCase 인터페이스만 주입받는다

---

## ADR-002: CQRS — Command/Query 서비스 분리

**날짜:** 2024  
**상태:** 확정

### 결정

상태 변경(Command)과 조회(Query)의 서비스 클래스와 UseCase 인터페이스를 분리한다.

### 이유

- 쓰기 경로에 `@Transactional`, 읽기 경로에 `@Transactional(readOnly = true)`를 명확히 적용
- 각 서비스의 의존성·책임 범위가 좁아져 God Service 방지

### 결과

```
{Domain}CommandService  — @Transactional
{Domain}QueryService    — @Transactional(readOnly = true)

ManageXxxUseCase        — Command UseCase
GetXxxUseCase           — Query UseCase
```

---

## ADR-003: `@OneToMany` 금지 — ID 참조 + IN 쿼리 배치 로딩

**날짜:** 2024  
**상태:** 확정

### 결정

- 모든 JPA Entity에서 `@OneToMany` 컬렉션 매핑을 금지한다.
- 자식 컬렉션 탐색은 Port 레벨의 IN 쿼리 메서드로 처리한다.

### 이유 (DDD 관점)

1. **Small Aggregate 원칙**: `@OneToMany`는 Aggregate를 거대하게 만들어 동시성 충돌·메모리 낭비를 유발한다.
2. **트랜잭션 경계**: DDD에서 트랜잭션 1개 = Aggregate 1개 수정. `@OneToMany`는 부모 트랜잭션이 자식 생성까지 포함한다.
3. **Repository 패턴**: 자식 Aggregate Root는 자신의 Port(Repository)를 통해서만 조회되어야 한다.

### 이유 (성능 관점)

- `@BatchSize`는 `@OneToMany`가 만든 N+1을 Hibernate 레벨에서 패치하는 임시방편이다.
- Port IN 쿼리는 동일한 최적화를 더 명시적·테스트 가능한 방식으로 구현한다.

### 결과

```java
// 금지
@OneToMany(mappedBy = "curriculum")
private List<WeeklyCurriculum> weeklyCurriculums;

// 권장
List<WeeklyCurriculum> findByCurriculumIdIn(List<Long> curriculumIds);
```

---

## ADR-004: 크로스 도메인 ID 참조

**날짜:** 2024  
**상태:** 확정

### 결정

다른 도메인의 Aggregate(Entity)를 직접 참조하지 않고, ID(Long) 값만 저장한다.

### 이유

- 도메인 간 JPA 연관관계가 없으므로 스키마 결합을 방지한다
- 도메인 B의 로직 변경이 도메인 A에 전파되지 않는다
- 크로스 도메인 조회는 해당 도메인의 UseCase를 통해서만 수행한다

### 결과

```java
// ChallengerWorkbook 내부
@Column(name = "member_id", nullable = false)
private Long memberId;   // ✅ ID 참조

// 데이터가 필요할 때
ChallengerInfo info = getChallengerUseCase.getByMemberIdAndGisuId(memberId, gisuId);
```

---

## ADR-005: Spring Data JPA + QueryDSL 조합

**날짜:** 2024  
**상태:** 확정

### 결정

단순 단건·목록 조회는 Spring Data JPA 쿼리 메서드(메서드명 파싱)를 사용하고, 복잡한 동적 쿼리·집계는 QueryDSL로 작성한다.

### 이유

- Spring Data JPA로 단순 CRUD를 선언적으로 처리
- 복잡한 JOIN·동적 조건은 타입 세이프 QueryDSL로 컴파일 타임 검증

### 결과

- JPA Repository: `{Entity}JpaRepository extends JpaRepository<T, Long>` — 메서드명 쿼리
- QueryDSL Repository: `{Domain}QueryRepository` — `JPAQueryFactory` 주입
- PersistenceAdapter에서 두 Repository를 조합하여 Port를 구현한다

---

## ADR-006: Flyway 마이그레이션

**날짜:** 2024  
**상태:** 확정

### 결정

모든 DB 스키마 변경은 Flyway 마이그레이션 파일로 관리한다. DDL auto는 비활성화.

### 이유

- 환경별(local/dev/prod) 스키마 일관성 보장
- 롤백 경로 명확화

### 결과

- 파일 위치: `src/main/resources/db/migration/`
- 네이밍: `V{version}__{description}.sql`
- Entity 변경 시 반드시 마이그레이션 파일을 함께 생성한다

---

## ADR-007: JWT 인증 + Casbin 인가

**날짜:** 2024  
**상태:** 확정

### 결정

- **인증**: `io.jsonwebtoken` 0.12.5 기반 Access/Refresh JWT
- **인가**: Casbin RBAC 기반 권한 제어

### 결과

- `authentication` 패키지: 토큰 발급·검증
- `authorization` 패키지: Casbin 정책 로딩·평가
- 권한 정책 변경은 Casbin 정책 파일에서 관리 (코드 수정 없이 적용 가능)
