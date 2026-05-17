# ADR-017: Test 도메인 시딩 API 통합 도입 (Member · Challenger · Project)

## Status

Proposed

## Context

`feat/alpha-env-dummy-data-seeding` 브랜치의 일련의 커밋(`abbb8c71`,
`d762db73`, `e0f93508`, `b9262f58`, `16120387`)에서 alpha 환경 부팅 시
자동으로 더미 회원(Member)을 시딩하는 인프라가 도입되었다.

```
src/main/java/com/umc/product/global/seed/
├── AlphaSeedRunner.java          # ApplicationRunner 기반 부팅 시 1회 자동 실행
├── AlphaDummyMemberFactory.java  # ID/PW + OAuth Command 생성
└── AlphaSeedProperties.java      # 시딩 설정
```

이 인프라는 다음 특성을 가진다.

- `@Profile("alpha") + @ConditionalOnProperty("app.seed.alpha.enabled")` 이중 가드.
- `ApplicationRunner` 기반 부팅 시 1회 자동 실행. 외부 트리거 없음.
- 멱등성 보장(현재 회원 수 > 임계값이면 스킵).
- 시딩 대상은 **Member 뿐**(ID/PW + OAuth).
- `AlphaSeedRunner` 가 `LoadMemberPort.countAllMembers()` 를 직접 호출. Hexagonal
  관점에서 Port 직접 의존이 1지점 존재.

그러나 다음 요구가 신규로 발생했다.

1. **운영진 대시보드 FE 작업, QA 시나리오 검증, 데모 환경 구성**에서 부팅 시
   자동 시딩만으로는 부족하다. 임의 시점에 N명·N건을 추가로 채우는 흐름이 필요하다.
2. Member 외에 **Challenger(챌린저)** 와 **Project + ProjectMember(프로젝트와
   프로젝트 멤버)** 까지 시딩되어야 운영진 대시보드와 챌린저·프로젝트 통계 화면이
   의미 있는 데이터를 보여준다.
3. 두 신규 도메인(Challenger, Project)은 "지부 정합성"과 "파트 정합성" 같은 도메인
   제약을 만족해야 하므로, 단순한 무작위 시딩은 시각화 단계에서 어색한 데이터로
   드러난다.
4. 기존 `AlphaSeedRunner` 는 Hexagonal Port 직접 호출이 섞여 있다. 시딩 코드를
   확장하면서 같은 위반을 늘리지 않도록 한 곳에서 원칙을 다시 정리할 필요가 있다.

### 도메인 모델 제약

#### Member · School · Chapter 관계

- `Member.schoolId (Long)` — School 도메인의 ID 참조만. 직접 객체 참조 없음.
- `ChapterSchool` 매핑 — 어떤 Chapter가 어떤 School을 포함하는지.
- Chapter 자체는 `gisuId` 를 가진다(기수별 활성 지부).

#### Challenger 도메인

```
Challenger { id, memberId, part: ChallengerPart, gisuId, status, modificationReason, modifiedBy }
```

Challenger 는 `chapterId` 또는 `schoolId` 를 직접 가지지 않는다. 따라서 "기수의
지부별 학교마다 파트별 N명" 분포 시딩은 **적절한 schoolId를 가진 Member를 만들고
그 Member로 챌린저를 등록**하는 작업으로 환원된다.

#### Project · ProjectMember 도메인

```
Project { id, gisuId, chapterId, productOwnerMemberId, productOwnerSchoolId,
          createdByMemberId, name, description, status }
ProjectMember { id, projectId, memberId, part: ChallengerPart, isLeader,
                description, status, decidedMemberId, decidedAt }
```

- Project 의 `chapterId` 는 PO(Product Owner) Member 의 schoolId 가 매핑된 Chapter
  로 결정된다(`CreateDraftProjectUseCase` 내부 정책).
- ProjectMember.part 는 `ChallengerPart` 를 재사용 — `PLAN, DESIGN, WEB, ANDROID,
  IOS, NODEJS, SPRINGBOOT, ADMIN`.
- 한 프로젝트 멤버의 데이터 정합성: **같은 학교(School)에 속한 Member 들로만
  구성**한다(한 School 은 한 Chapter 에 속하므로 결과적으로 같은 지부 보장).
  코드 레벨 제약은 없으며 서비스 레벨에서 보장한다.

### 사용 가능한 UseCase 자산

| 영역 | UseCase | 시그니처 |
|---|---|---|
| Member 등록 | `RegisterIdPwMemberUseCase` | `register(IdPwRegisterMemberCommand): Long` |
| Member 등록 | `RegisterOAuthMemberUseCase` | `register / batchRegister` |
| Member 조회 | `GetMemberUseCase` | `findAllIdsBySchoolId(Long): Set<Long>` 등 |
| 약관 조회 | `GetTermUseCase` | 필수 약관 ID 조회 |
| Chapter 조회 | `GetChapterUseCase` | `getAllChapters / getChaptersBySchool / getChaptersWithSchoolsByGisuId` |
| Gisu 조회 | `GetGisuUseCase` | `getActiveGisu / getList / getById` |
| Challenger 일괄 등록 | `ManageChallengerUseCase` | `createChallengerBulk(...)` (이미 `validateEnvIsNotProduction()` 가드 내장) |
| Project 생성 | `CreateDraftProjectUseCase` | `create(CreateDraftProjectCommand): Long` (gisuId, productOwnerMemberId, requesterMemberId) |
| ProjectMember 추가 | `AddProjectMemberUseCase` | `add(AddProjectMemberCommand): Long` (projectId, memberId, part, requesterMemberId) |

**단 한 곳의 결손**: `AlphaSeedRunner` 가 직접 호출하던 `LoadMemberPort.countAllMembers()`
에 대응하는 UseCase 메서드가 없다.

### 기존 Test 도메인 패턴

`com.umc.product.test` 패키지는 이미 존재하며 `@Profile("local | dev")` 가드의
`TestController` 가 헬스 체크·FCM·웹훅 알람·토큰 발급 등 운영 외 환경 전용
디버깅/검증 엔드포인트 모음 역할을 한다. 단 Hexagonal 분리는 적용되어 있지
않다(`test/controller/TestController.java` 단일 파일).

## Decision

우리는 **모든 시딩 인프라를 `test` 도메인 아래로 통합 이전하면서, 다음 5가지 원칙을
동시에 적용**하기로 결정한다.

1. **Hexagonal 엄격 준수**: 시딩 서비스는 다른 도메인의 **UseCase 만** 주입·호출한다.
   Port·Repository 직접 호출을 금지한다.
2. **REST API 단일 트리거**: `ApplicationRunner` 기반 부팅 시 자동 시딩은 제거한다.
   모든 시딩은 명시적인 HTTP 호출로만 트리거된다.
3. **Profile 가드 확장**: `@Profile("!prod")` 로 `prod` 를 제외한 모든 환경에서
   활성화한다. 새 환경(예: `staging`)이 추가되어도 별도 수정 없이 자동 포함된다.
   prod 보호는 (a) 빈 등록 차단, (b) `validateEnvIsNotProduction()` 런타임 가드
   양쪽으로 유지한다.
4. **시딩 범위 확장**: Member 외에 **Challenger 분포 시딩**과 **Project + ProjectMember
   시딩**을 추가한다.
5. **데이터 정합성 보장**: 한 프로젝트의 멤버는 모두 같은 School(=같은 Chapter)에
   속한 Member 풀에서 선발하고, "프론트엔드/백엔드"는 명시적인 ChallengerPart 그룹에
   매핑한다.

세부 결정은 다음과 같다.

### 위치 및 패키지 구조

```
src/main/java/com/umc/product/test/
├── application/
│   ├── port/in/command/
│   │   ├── SeedMembersUseCase.java
│   │   ├── SeedChallengersUseCase.java
│   │   └── SeedProjectsUseCase.java
│   └── service/
│       ├── MemberSeedService.java
│       ├── ChallengerSeedService.java
│       ├── ProjectSeedService.java
│       ├── DummyMemberFactory.java        # 기존 AlphaDummyMemberFactory 이전
│       ├── PartAssignmentPolicy.java      # FE/BE → ChallengerPart 매핑 정책
│       └── SeedProperties.java            # 기존 AlphaSeedProperties 이전
└── adapter/in/web/
    ├── SeedController.java                # POST /test/seed/{members,challengers,projects}
    └── dto/
        ├── SeedMembersRequest, SeedMembersResponse
        ├── SeedChallengersRequest, SeedChallengersResponse
        └── SeedProjectsRequest, SeedProjectsResponse
```

이전 후 `src/main/java/com/umc/product/global/seed/` 는 비워지므로 디렉터리를 삭제한다.

### Port 직접 사용 제거

`AlphaSeedRunner` 가 사용하던 `LoadMemberPort.countAllMembers()` 를 대체하기 위해
`GetMemberUseCase` 인터페이스에 다음을 추가한다.

```java
long countAll();
```

Member 도메인의 Query 어댑터에서 구현한다. 이 한 줄의 인터페이스 확장이 시딩 측의
Port 직접 의존을 0으로 만든다.

### 환경 가드

- `@Profile("!prod")` 로 컨트롤러·서비스 빈 등록 자체를 차단.
- `@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")`
  로 환경 변수 단계 차단.
- `ManageChallengerUseCase.createChallengerBulk` 의 `validateEnvIsNotProduction()`
  런타임 가드는 그대로 유지.

기존 properties 키 `app.seed.alpha.*` 는 `app.seed.*` 로 일반화한다(`alpha` 한정
의미를 제거).

### 멤버 시딩 (Member)

`POST /test/seed/members`

```json
Request:
{
  "count": 200,
  "force": false
}
Response:
{
  "registered": 200,
  "skipped": false,
  "reason": null
}
```

- **ID/PW 회원만 시딩한다**. OAuth 회원 시딩은 제거되었다(아래 "OAuth 제거" 참조).
- 모든 더미 회원은 동일한 비밀번호 (`app.seed.default-password`)를 사용해 테스트 편의를
  최적화한다. loginId 만 시퀀스로 구분된다.
- `force=false`(기본) — 기존 회원 수가 `properties.skipIfMemberCountGreaterThan`
  초과면 `skipped=true` 로 반환(기존 멱등성 유지).
- `force=true` — 멱등성 체크 무시.
- 각 register 호출은 자체 트랜잭션이며, 외부 트랜잭션이 묶이지 않도록 서비스 메서드는
  `@Transactional(propagation = NOT_SUPPORTED)` 로 의도를 명시한다(실패 격리).
- loginId 시퀀스 오프셋은 `GetMemberUseCase.countAll()` 기반.

#### OAuth 제거

초기 ADR 안은 ID/PW · OAuth 양쪽 시딩을 모두 포함했으나, 실 운영 외 사용 시나리오에서
- 더미 OAuth provider 응답을 가짜로 만드는 비용이 크고,
- QA/시연용으로는 ID/PW 단일 경로로도 충분하며,
- OAuth 시딩의 sequence 가 호출마다 1부터 다시 시작해 UNIQUE 제약 충돌이 발생하는 한계가
  있었기 때문에 OAuth 경로 자체를 제거한다.

OAuth 검증이 필요한 시나리오는 운영진이 실 provider 흐름으로 별도 처리한다.

### 챌린저 시딩 (Challenger 분포 시딩)

`POST /test/seed/challengers`

```json
Request:
{
  "gisuId": 9,
  "countPerPartPerSchool": 2,
  "parts": ["WEB", "ANDROID", "IOS", "NODEJS", "SPRINGBOOT", "DESIGN", "PLAN"],
  "chapterIds": null
}
Response:
{
  "gisuId": 9,
  "totalCreated": 84,
  "totalFailed": 0,
  "perCellSummary": [
    {"chapterId": 1, "schoolId": 11, "part": "WEB", "created": 2, "failed": 0},
    ...
  ]
}
```

- `chapterIds == null` → 해당 기수의 모든 Chapter.
- `parts == null` → ADMIN 제외 모든 파트.
- 처리: `getChaptersWithSchoolsByGisuId(gisuId)` 로 (Chapter, School) 셀 목록 확보 →
  각 (Chapter, School, Part) 셀마다 `DummyMemberFactory.nextIdPwCommandWithSchool(seq, schoolId)`
  로 Member 생성 → `ManageChallengerUseCase.createChallengerBulk` 로 Challenger 생성.
- 셀 단위 트랜잭션(실패 격리).

### 프로젝트 시딩 (Project + ProjectMember)

`POST /test/seed/projects`

```json
Request:
{
  "projectCount": 10,
  "gisuId": null
}
Response:
{
  "createdProjectIds": [1, 2, ...],
  "skippedChapters": [{"chapterId": 7, "reason": "INSUFFICIENT_POOL"}],
  "failedCount": 0
}
```

- `gisuId == null` → `GetGisuUseCase.getActiveGisu()`.
- 한 프로젝트 구성: PLAN ×1 + 프론트엔드 ×N_fe + 백엔드 ×N_be (N_fe, N_be ∈ {5, 6}).
  - 프론트엔드 슬롯: `{WEB, ANDROID, IOS}` 중 균등 무작위.
  - 백엔드 슬롯: `{NODEJS, SPRINGBOOT}` 중 균등 무작위.
- 풀 추출 단위는 **School**. 활성 기수의 (Chapter, School) 셀 목록을
  `GetChapterUseCase.getChaptersWithSchoolsByGisuId(gisuId)` 로 확보 → School 단위로
  round-robin 순회 → `GetMemberUseCase.findAllIdsBySchoolId(schoolId)` 풀에서 슬롯
  총원만큼 중복 없이 추출 → 부족하면 해당 school skip 후 다음 school 시도.
- 응답의 `skippedChapters` 는 정확히는 "skip된 (Chapter, School) 페어 + 사유"를 담는다
  (구조는 동일, 명세상 chapterId 와 schoolId 양쪽 모두 포함).
- PLAN 슬롯에 배정된 Member 를 PO(`productOwnerMemberId`)와 `requesterMemberId` 양쪽으로 사용.
  PO Member 의 schoolId 가 곧 풀에서 추출한 schoolId 이므로, `CreateDraftProjectUseCase`
  내부에서 Project.chapterId 도 자연스럽게 매칭되는 chapter 로 결정된다.
- `projectCount` 충족 또는 모든 (Chapter, School) 후보 소진 시 종료. 부분 성공을
  응답에 반영.
- 멤버 중복 배정 방지: 같은 호출 안에서 이미 어떤 프로젝트에 할당된 Member 는 다른
  프로젝트에 재사용하지 않는다(`uk_project_member_project_member` 위반 방지).

## Alternatives Considered

### A1. `AlphaSeedRunner` 를 그대로 유지하고 API 만 추가 (Hybrid)

장점: 기존 부팅 시 자동 시딩 동작을 보존.
단점: 트리거 진입점이 둘로 갈라져 멱등성·실행 시점 추적이 복잡해진다. 어느 단계에서
어떤 데이터가 들어왔는지 운영 디버깅 시 혼란.
선택하지 않은 이유: "임의 시점 수동 트리거"가 1순위 요구이고, 자동 시딩이 필수가
아니라면 진입점을 단일화하는 것이 코드와 운영 양쪽에서 단순하다. ApplicationRunner를
제거하고 API 단일화하면 alpha 환경에서도 운영진이 명시적으로 트리거하면 된다.

### A2. 별도 `seeding` 정식 도메인 신설 (Hexagonal 정식 도메인 승격)

장점: 헥사고날 일관성을 끝까지 유지.
단점: 시딩은 비즈니스 도메인이 아니라 운영 외 환경 전용 도구다. 정식 도메인으로
올리면 패키지 명세상 prod 빌드에 포함되는 의미가 되어 의도와 충돌한다. `test`
도메인이 이미 같은 정체성(운영 외 도구)을 표현한다.
선택하지 않은 이유: 정체성 측면에서 `test/` 가 더 정확한 위치이며, `test` 도메인
내부에서도 application/adapter 분리는 적용 가능하다(아래 A3 참고).

### A3. `test` 도메인 안에서도 Hexagonal 분리하지 않고 단일 컨트롤러로 통합

장점: 기존 `TestController` 와 동일한 단순 패턴.
단점: 시딩 로직(파트 분배, 풀 선발, 셀 분포 등)은 단순 디버깅 엔드포인트와 달리
응집된 도메인 로직을 가진다. 단일 컨트롤러 안에 두면 테스트 단위가 흐려지고, 다른
UseCase를 다수 조립하는 책임이 컨트롤러로 새어 들어간다.
선택하지 않은 이유: `test` 도메인은 "운영 외 전용 도구"라는 정체성을 가지되, 내부의
로직 복잡도가 일정 수준 이상이면 application/adapter 분리를 적용하는 것이 코드베이스
전체 컨벤션과 더 정합적이다. 기존 `TestController` 는 그대로 두고 시딩 부분만 분리한다.

### A4. Port 직접 사용을 허용 (cross-domain UseCase 호출 비용 회피)

장점: `GetMemberUseCase.countAll()` 같은 신규 메서드를 만들지 않아도 된다.
단점: CLAUDE.md 의 "Domain A는 Domain B의 Port/Repository에 직접 의존 금지" 원칙을
위반한다. 시딩처럼 cross-domain 조립이 많은 코드일수록 Port 직접 의존이 누적되기
쉽고, 이후 도메인 캡슐화 작업이 어려워진다.
선택하지 않은 이유: 헥사고날 원칙은 시딩 코드에서도 동일하게 지킨다. 단 한 줄의
UseCase 메서드 추가로 Port 직접 의존을 0으로 만들 수 있다.

### A5. Gradle task 또는 외부 스크립트로 시딩 (서버 외부 실행)

장점: 운영 코드와 완전히 격리.
단점: 서버 내부의 도메인 검증(`MemberRegistrationValidator`, `CredentialPolicy`,
필수 약관 동의 등) 을 우회해 실제 가입 흐름과 다른 형태의 데이터가 만들어진다. FE/QA
가 운영 외 환경에서 즉시 호출하기에도 불편하다.
선택하지 않은 이유: 도메인 검증 흐름의 재사용이 데이터 일관성 보장의 핵심이다.

## Consequences

### Positive

- alpha/dev/local 환경에서 챌린저 화면, 운영진 대시보드, 프로젝트 통계 API 를 검증할
  수 있는 분포된 데이터가 생긴다.
- 트리거 진입점이 REST API 로 단일화되어 운영진 디버깅과 자동화 시나리오 양쪽에서
  추적이 단순해진다.
- 시딩 서비스가 UseCase 만 호출하므로 도메인 캡슐화가 유지되고, 향후 도메인 내부
  구조 변경 시 시딩 측 수정 비용이 0에 가깝다.
- `GetMemberUseCase.countAll()` 같은 작은 UseCase 확장이 시딩 외 다른 운영 조회에도
  재사용 가능한 자산이 된다.
- prod 보호가 3중(빈 등록 차단 · property 차단 · 런타임 가드)으로 유지된다.

### Negative

- 기존 alpha 부팅 시 자동 시딩이 사라진다. alpha 환경 초기화 시 운영진이 명시적으로
  `/test/seed/*` 를 호출해야 한다. 운영 가이드 문서가 별도로 필요할 수 있다.
- 시딩 서비스가 다수의 외부 UseCase(`GetMemberUseCase`, `RegisterIdPwMemberUseCase`,
  `ManageChallengerUseCase`, `CreateDraftProjectUseCase`, `AddProjectMemberUseCase`,
  `GetChapterUseCase`, `GetGisuUseCase`, `GetTermUseCase`) 를 조립한다. 의존이
  많지만, 그 본질은 "여러 도메인을 조합하는 운영 외 도구"라는 정체성과 일치한다.
- `test` 도메인 안에서도 application/adapter 분리가 적용되어 기존 `TestController`
  (단일 파일) 와 패키지 구조 차이가 생긴다.

### Neutral / Trade-offs

- 멤버 시딩의 멱등성(`force` 플래그)은 호출자가 명시적으로 결정해야 하므로 API
  사용자에게 약간의 인지 부담을 준다. 대신 한 번 시딩한 환경을 의도적으로 재시딩하는
  케이스가 명시적으로 표현된다.
- 프로젝트 시딩은 부분 성공이 가능하다(특정 chapter의 풀 부족). 호출자는 응답의
  `skippedChapters` 를 반드시 확인해야 한다. 대신 "되는 만큼은 채워준다" 라는 운영
  편의가 확보된다.
- 같은 chapter 안에서 멤버 풀이 충분하면 여러 프로젝트가 같은 chapter 로 만들어진다.
  멤버 중복 배정은 같은 호출 안에서 차단하지만, 호출을 반복하면 동일 멤버가 여러
  프로젝트에 들어갈 수 있다. 시딩의 목적상 허용한다.

## Implementation Notes

### 커밋 단위 작업 계획

기존 alpha 시딩 커밋 시퀀스의 분해 원칙(properties → 골격 → 기능 단위 → 테스트)을
유지하되, **신규 원칙 적용 → 이전 → 기능 추가 → 테스트** 순서로 재배치한다.

#### Commit 1. `refactor: GetMemberUseCase 에 countAll 메서드 추가`

- `member/application/port/in/query/GetMemberUseCase.java` 에 `long countAll();` 추가.
- Member Query 어댑터(또는 Service) 에 구현. JPA 측 카운트 호출은 기존 Port 활용
  가능.
- 단위 테스트 동반.

#### Commit 2. `refactor: 시딩 인프라를 global/seed 에서 test 도메인으로 이전`

- `global/seed/AlphaSeedRunner.java` 제거(ApplicationRunner 자체 폐기).
- `global/seed/AlphaDummyMemberFactory.java` → `test/application/service/DummyMemberFactory.java`.
- `global/seed/AlphaSeedProperties.java` → `test/application/service/SeedProperties.java`.
- properties 키 `app.seed.alpha.*` → `app.seed.*` 로 일반화.
- `LoadMemberPort` 직접 의존 제거 → `GetMemberUseCase.countAll()` 호출로 전환.
- 빈 등록은 일단 제거 상태(다음 커밋에서 새 서비스가 채움).

#### Commit 3. `feat: test 도메인 멤버 시딩 API 추가`

- `SeedMembersUseCase` (in-port) + `MemberSeedService` 신설.
- `SeedController.POST /test/seed/members` + `SeedMembersRequest/Response`.
- `force` 플래그 처리, 시퀀스 오프셋 계산, 트랜잭션 정책(ID/PW per-call, OAuth batch)
  반영.
- 활성 환경: `@Profile("!prod") + @ConditionalOnProperty("app.seed.enabled")`.

#### Commit 4. `feat: test 도메인 챌린저 분포 시딩 API 추가`

- `SeedChallengersUseCase` + `ChallengerSeedService` 신설.
- `DummyMemberFactory.nextIdPwCommandWithSchool(seq, schoolId)` 추가(기존 무작위
  schoolId 변형의 schoolId 지정 가능 형).
- `SeedController.POST /test/seed/challengers` + DTO.
- `GetChapterUseCase.getChaptersWithSchoolsByGisuId` 로 셀 목록 확보, 셀 단위
  트랜잭션 격리, `ManageChallengerUseCase.createChallengerBulk` 호출.

#### Commit 5. `feat: test 도메인 프로젝트 시딩 API 추가`

- `SeedProjectsUseCase` + `ProjectSeedService` 신설.
- `PartAssignmentPolicy` 신설(FE/BE → ChallengerPart 매핑, 슬롯 무작위 분배).
- `SeedController.POST /test/seed/projects` + DTO.
- `GetChapterUseCase.getChaptersWithSchoolsByGisuId(gisuId)` 로 (Chapter, School)
  셀 목록 확보, School 단위 round-robin, `GetMemberUseCase.findAllIdsBySchoolId(schoolId)`
  로 풀 확보, `CreateDraftProjectUseCase.create` → `AddProjectMemberUseCase.add` 반복.
- 같은 호출 안에서의 멤버 중복 배정 차단 로직.

#### Commit 6. `test: 시딩 서비스 3종 단위 테스트 추가`

- `MemberSeedServiceTest` — force 처리, 멱등성 분기, 트랜잭션 단위 검증.
- `ChallengerSeedServiceTest` — 셀 단위 실패 격리, parts 기본값, chapterIds 필터.
- `ProjectSeedServiceTest` — PartAssignmentPolicy 분배 균등성, 풀 부족 skip,
  멤버 중복 배정 차단.
- 컨트롤러 통합 테스트는 별도 통합 테스트 슬라이스에서 필요 시 추가.

### 환경 가드 체크리스트

- `@Profile("!prod")` — 빈 등록 단계 차단.
- `@ConditionalOnProperty("app.seed.enabled")` — properties 단계 차단.
- `ManageChallengerUseCase.createChallengerBulk` 의 `validateEnvIsNotProduction()`
  — 런타임 차단(이미 존재).
- prod profile 활성화 시 위 3중 어디서도 통과하지 못함.

### 주의점

- ID/PW 시딩 loginId 시퀀스는 `GetMemberUseCase.countAll()` 결과를 오프셋으로 삼는다.
  여러 번 호출되어도 충돌하지 않는다(기존 `alpha_user_0001` prefix 컨벤션은 유지).
- 프로젝트 시딩에서 `CreateDraftProjectCommand` 는 `gisuId`, `productOwnerMemberId`,
  `requesterMemberId` 만 받는다. Project 의 `chapterId` 와 `productOwnerSchoolId` 는
  PO Member 의 schoolId 로부터 UseCase 내부에서 자동 도출된다(현재 정책).
- 프로젝트 시딩의 멤버 풀은 `Member.schoolId` 기반이며 ChapterSchool 매핑이 정확하다는
  전제를 깔고 있다. 매핑 데이터가 비어 있는 (chapter, school) 셀은 풀이 0이 되어
  자연스럽게 skip 된다.
- 프로젝트 시딩이 의미 있게 동작하려면 같은 school 에 11~13 명 이상의 Member 가
  존재해야 한다. 기존 alpha 멤버 시딩은 schoolId 를 1~38 범위 무작위로 분포시키므로,
  한 school 당 약 (시딩 수 / 38)명이 들어간다. 풀 부족이 빈번하면 멤버 시딩 호출량을
  먼저 늘려야 한다.
- 약관 동의는 `GetTermUseCase.getRequiredTermIds()` 동적 조회를 그대로 재사용한다.

## Operator Guide

### 활성화 조건

| 항목 | 값 |
|---|---|
| Spring Profile | `prod` 가 아닌 모든 프로파일 (`local`, `test`, `dev`, `alpha`, …) |
| Property | `app.seed.enabled=true` |

`alpha` 프로파일은 `application.yml` 의 multi-document 오버라이드로 기본값이 `true` 로
설정되어 있다. 다른 환경은 `APP_SEED_ENABLED=true` 환경변수로 명시 활성화해야 한다.

### 호출 순서 (필수)

다섯 API 는 의존성이 있어 다음 순서로 호출한다.

1. **`POST /test/seed/members`** — Member 풀을 채운다. Challenger·Project 시딩이
   사용할 Member 가 없으면 후속 시딩이 모두 skip 된다.
2. **`POST /test/seed/challengers`** — 활성 기수의 (Chapter, School, Part) 셀별로
   더미 Member + Challenger 를 함께 만든다. 멤버 시딩과 사실상 동일 효과지만 챌린저까지
   함께 생성되므로 챌린저 화면 검증에 유리하다.
3. **`POST /test/seed/projects`** — 같은 school 의 Member 풀에서 슬롯(PLAN ×1 +
   FE 5~6 + BE 5~6 = 11~13)을 뽑아 프로젝트를 N 개 만든다. 한 school 에 최소 11명이
   있어야 하므로 1번에서 충분히 시딩하지 않으면 `partialProjects` / `skippedChapters`
   에 잡힌다.
4. **`POST /test/seed/community`** — 활성 기수의 챌린저 풀에서 작성자를 뽑아 게시글 ·
   댓글 · 트로피를 시딩한다. 챌린저 풀이 비어있으면 `skipped=true` 로 반환되므로 2번을
   먼저 호출해야 한다.
5. **`POST /test/seed/curriculum`** — 활성 기수에 ADMIN 제외 파트별로 Curriculum →
   WeeklyCurriculum → OriginalWorkbook(MAIN, READY) → Mission 골격을 생성한다.
   `releaseRequesterMemberId` 를 함께 보내면 워크북을 READY → RELEASED 로 전환해
   Phase 2 (챌린저 워크북 배포·미션 제출) 작업의 입구를 열어둔다. Phase 2 시딩은
   본 PR 의 범위가 아니다 — 시간 제약·권한·상태 의존성 때문에 별도 ADR 로 다룰 예정이다.

### 권장 호출 예시 (alpha 환경 초기화)

```bash
# 1. 풀 채우기: 200명 (chapter당 약 5명, school 마다 5~6명)
curl -X POST $BASE/test/seed/members -H 'Content-Type: application/json' \
  -d '{"count": 200, "force": false}'

# 2. 챌린저 분포: 활성 기수, 셀당 2명
curl -X POST $BASE/test/seed/challengers -H 'Content-Type: application/json' \
  -d '{"countPerPartPerSchool": 2}'

# 3. 프로젝트: 10건
curl -X POST $BASE/test/seed/projects -H 'Content-Type: application/json' \
  -d '{"projectCount": 10}'

# 4. 커뮤니티: 게시글 30, 게시글당 댓글 3, 트로피 10
curl -X POST $BASE/test/seed/community -H 'Content-Type: application/json' \
  -d '{"postCount": 30, "commentsPerPost": 3, "trophyCount": 10}'

# 5. 커리큘럼: 8 주차, 워크북당 미션 2개, releaseRequesterMemberId 지정 시 RELEASED 까지 전환
curl -X POST $BASE/test/seed/curriculum -H 'Content-Type: application/json' \
  -d '{"weeksPerCurriculum": 8, "missionsPerWorkbook": 2, "releaseRequesterMemberId": 1}'
```

### 응답 해석

- `SeedMembersResponse.skipped=true` — 이미 임계값 이상 회원이 존재. `force=true` 로
  재호출하면 시딩한다(이전 회원 위에 시퀀스가 오프셋되어 충돌하지 않음).
- `SeedChallengersResponse.perCellSummary[i].memberFailed > 0` — 해당 셀의 멤버
  생성에서 실패가 발생. 보통 `MemberRegistrationValidator` 의 dynamic 검증 실패가
  원인이다(필수 약관 변경 등).
- `SeedChallengersResponse.perCellSummary[i].challengerFailed > 0` — 멤버는 만들었지만
  챌린저 bulk 생성이 트랜잭션 단위로 실패. 셀 전체 챌린저가 롤백됨.
- `SeedProjectsResponse.skippedChapters[i].reason = "INSUFFICIENT_POOL ..."` —
  해당 school 의 풀이 11명 미만. 1번 시딩을 늘려야 한다.
- `SeedProjectsResponse.partialProjects[i]` — 프로젝트는 만들어졌지만 일부 멤버
  add 가 실패. orphan project 가 잔존하므로 운영자 판단 후 정리 필요.
- `SeedCommunityResponse.skipped=true` — 챌린저 풀이 비어있음. 2번(/test/seed/challengers)
  를 먼저 호출.
- `SeedCommunityResponse.postFailed/commentFailed/trophyFailed > 0` — 각 도메인의
  Create UseCase 가 검증·정책 변경 등으로 실패한 케이스. 로그로 원인 추적.
- `SeedCurriculumResponse.curriculumFailed > 0` — 동일 (gisuId, part) 의 커리큘럼이
  이미 존재하는 경우. 파트별 1개 unique 제약 위반.
- `SeedCurriculumResponse.weeklyCurriculumFailed > 0` — 동일 커리큘럼 내 (week, isExtra)
  조합이 unique. 동일 주차 재호출 시 발생.
- `SeedCurriculumResponse.originalWorkbookFailed > 0` — WeeklyCurriculum 의 시작 일시가
  과거이고 진행 중이 아닌 경우. 시딩 시점이 마이그레이션과 어긋났을 가능성.
- `SeedCurriculumResponse.released=true / releaseFailed=0` — 모든 워크북이 RELEASED 로
  전환되어 챌린저가 즉시 배포받을 수 있는 상태. `releaseRequesterMemberId` 미지정 시는
  `released=false` (READY 상태 유지).

### 자주 발생하는 운영 이슈

| 증상 | 원인 | 대응 |
|---|---|---|
| `skipped=true` 가 떨어진다 | 임계값 초과 (`skipIfMemberCountGreaterThan`) 또는 챌린저 풀 비어있음 | `force=true` 재호출 / 임계값 조정 / 챌린저 시딩 선행 |
| 모든 셀 skip | 활성 기수 미설정 또는 ChapterSchool 매핑 비어있음 | `/api/v1/admin/gisu` / chapter-school seed 마이그레이션 확인 |
| `INSUFFICIENT_POOL` 빈발 | school 당 멤버 < 11 | members 호출을 더 큰 수로 다시 |

## References

- 기존 시딩 인프라(이전 대상)
    - `src/main/java/com/umc/product/global/seed/AlphaSeedRunner.java`
    - `src/main/java/com/umc/product/global/seed/AlphaDummyMemberFactory.java`
    - `src/main/java/com/umc/product/global/seed/AlphaSeedProperties.java`
- 활용 UseCase
    - `src/main/java/com/umc/product/member/application/port/in/query/GetMemberUseCase.java`
    - `src/main/java/com/umc/product/member/application/port/in/command/RegisterIdPwMemberUseCase.java`
    - `src/main/java/com/umc/product/challenger/application/port/in/command/ManageChallengerUseCase.java`
    - `src/main/java/com/umc/product/project/application/port/in/command/CreateDraftProjectUseCase.java`
    - `src/main/java/com/umc/product/project/application/port/in/command/AddProjectMemberUseCase.java`
    - `src/main/java/com/umc/product/organization/application/port/in/query/GetChapterUseCase.java`
    - `src/main/java/com/umc/product/organization/application/port/in/query/GetGisuUseCase.java`
- 기존 Test 도메인
    - `src/main/java/com/umc/product/test/controller/TestController.java`
- 관련 커밋: `abbb8c71`, `d762db73`, `e0f93508`, `b9262f58`, `16120387`
