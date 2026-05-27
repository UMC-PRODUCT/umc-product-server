# ADR-005: 프로덕트팀은 기수와 별도 생명주기를 갖는 organization 하위 Aggregate로 모델링한다

## Status

Proposed

## Context

UMC PRODUCT는 챌린저 운영 외에 산하 운영 조직으로 **프로덕트팀**(이하 "Product Team")을 별도 보유한다. Product Team은 챌린저 기수(`Gisu`)와 다른 생명주기를 갖는다.

- 챌린저 기수는 학기 단위로 시작/종료하지만, Product Team은 자체적인 시작일과 종료일을 가지며 여러 기수에 걸쳐 활동한다.
- Product Team 산하 하위 팀(이하 "Subteam")은 다음 다섯 개로 고정되어 있다.
    - 프론트엔드팀 (`FRONTEND`)
    - 디자인팀 (`DESIGN`)
    - 서버팀 (`SERVER`)
    - 모바일팀 — iOS (`MOBILE_IOS`)
    - 모바일팀 — Android (`MOBILE_ANDROID`)
- 운영 인원의 역할은 기수마다 달라진다. 같은 사람이 13기에는 디자인팀 팀장이었다가 14기에는 디자인팀 총괄단으로 옮길 수 있다. 따라서 "역할"은 사람-팀 간 영구 속성이 아니라 **(member, subteam, gisu) 단위의 임명 기록**이다.
- 역할은 두 종류로 한정된다.
    - 팀장 (`TEAM_LEADER`)
    - 팀 총괄단 (`GENERAL_MANAGER`)

기존 organization 도메인의 모델링 컨벤션은 다음과 같다(코드 인용은 [organization 패키지](../../src/main/java/com/umc/product/organization)).

- [Gisu](../../src/main/java/com/umc/product/organization/domain/Gisu.java)는 `Embedded GisuPeriod(startAt, endAt)` VO로 기간을 표현하고, 검증을 VO 생성자에서 수행한다.
- [StudyGroup](../../src/main/java/com/umc/product/organization/domain/StudyGroup.java)은 `gisuId` 같은 cross-aggregate 참조를 ID로만 들고, 같은 도메인 내 부모(`Chapter` → `Gisu`) 참조는 `@ManyToOne(fetch = LAZY)`로 둔다.
- [StudyGroupMember](../../src/main/java/com/umc/product/organization/domain/StudyGroupMember.java)는 같은 도메인의 부모(`StudyGroup`)를 `@ManyToOne`으로 참조한다.
- [ChallengerPart](../../src/main/java/com/umc/product/common/domain/enums/ChallengerPart.java)는 챌린저(수강생) 분류 enum이며, 운영 조직(Product Team) 분류와는 의미가 다르다. ADR-003에서도 챌린저 part를 외부 시스템 라우팅에 그대로 재사용하지 않기로 한 바 있다.

본 ADR이 결정해야 하는 사항은 다음과 같다.

- Product Team의 위치와 Aggregate 경계
- Subteam을 enum으로 둘 것인지 별도 엔티티로 둘 것인지
- 역할을 표현하는 데이터 모델
- "팀 총괄단"이 subteam 단위인지 Product Team 전체 단위인지
- 기수 변동에 따른 임명 기록의 형태
- 챌린저 part(`ChallengerPart`) 재사용 여부

## Decision

우리는 다음과 같이 결정한다.

1. **Product Team은 organization 도메인 하위의 신규 Aggregate Root로 둔다.** 패키지는 `com.umc.product.organization.domain.ProductTeam` 등 기존 organization 도메인과 동일한 위치에 배치한다.
2. **Product Team은 자체 생명주기를 갖는다.** `ProductTeamPeriod(startAt, endAt)` Embedded VO로 시작/종료 시각을 표현하고, VO 생성자에서 `startAt < endAt` 검증과 NULL 검증을 수행한다(`GisuPeriod`와 같은 패턴).
3. **Subteam은 enum으로 모델링한다.** `ProductSubteam { FRONTEND, DESIGN, SERVER, MOBILE_IOS, MOBILE_ANDROID }`는 `organization.domain.enums` 하위에 둔다. 새로운 subteam이 추가되는 일은 거의 없으며, 5개 고정으로 둔 채 enum 분기에서 컴파일러가 누락을 감지하게 한다.
4. **챌린저 part(`ChallengerPart`)와 Product Subteam은 분리한다.** 의미가 다르므로 enum을 재사용하지 않는다. 이름이 비슷해도 의미상 다른 도메인이다(ADR-003의 동일 원칙).
5. **역할은 별도 엔티티 `ProductTeamMembership`으로 모델링한다.** 한 행은 "어떤 Product Team에서, 어떤 기수에, 어떤 subteam의, 어떤 사람이, 어떤 역할을 수행했는가"를 표현한다.
    - 컬럼: `product_team_id`(FK), `gisu_id`(BIGINT, ID 참조), `subteam`(enum), `member_id`(BIGINT, ID 참조), `role`(enum)
    - **부모(`ProductTeam`) 참조는 `@ManyToOne(fetch = LAZY)`로 둔다**(같은 organization 도메인 내부, CLAUDE.md의 `@ManyToOne` 예외 적용).
    - **`Gisu`는 다른 Aggregate**이므로 `@ManyToOne` 금지, `gisuId(Long)`로만 참조한다.
    - **`Member`도 다른 도메인**이므로 `memberId(Long)`로만 참조한다.
6. **역할 enum은 `ProductTeamRole { TEAM_LEADER, GENERAL_MANAGER }`로 두 종류만 정의한다.** displayName은 한국어("팀장", "팀 총괄단")로 매핑한다.
7. **"팀장"은 (gisu, subteam) 단위로 1명만 허용한다.** 즉 "13기 디자인팀 팀장"은 1명이다. DB 무결성은 **부분 UNIQUE 인덱스**로 강제한다: `UNIQUE (product_team_id, gisu_id, subteam) WHERE role = 'TEAM_LEADER'` (PostgreSQL의 partial unique index).
8. **"팀 총괄단"은 (gisu, subteam) 단위로 다수 허용한다.** subteam마다 여러 명이 임명될 수 있다.
9. **GENERAL_MANAGER도 subteam을 NOT NULL로 강제한다.** 사용자 정의의 "각 팀의 팀장 / 팀 총괄단"이라는 표현은 둘 다 subteam에 귀속됨을 전제로 한다. Product Team 전체 총괄(예: PM/PO)이 별도로 필요해지면 후속 ADR에서 별도 역할로 도입한다.
10. **Product Team 자체에는 챌린저 같은 "현재 활성" 플래그를 두지 않는다.** `ProductTeamPeriod`로부터 `isActiveAt(Instant)` 도메인 메서드로 도출한다. `Gisu.isActive`는 운영 정책상 별도 토글이 필요했던 사정이므로 동일 패턴을 강제하지 않는다.

## Alternatives Considered

### 1. Subteam을 별도 엔티티 테이블(`product_subteam`)로 분리

각 subteam을 행으로 하는 테이블을 만들고, `ProductTeamMembership`이 `subteam_id`(FK)를 갖는 방식이다.

장점:

- 신규 subteam(예: AI팀, 데이터팀) 추가 시 코드 변경 없이 행 추가만으로 가능하다.
- subteam별 메타데이터(영문 코드, 설명, 디스코드 채널 ID 등)를 행 컬럼으로 운영 가능하다.

단점:

- 현 요구사항은 "5개 고정"이며, 신규 subteam 추가는 운영 의사결정이 동반되는 이벤트라 코드 + DB 변경이 함께 일어나는 편이 안전하다.
- 도메인 코드(`switch (subteam)`)에서 enum이 주는 컴파일러 보호(누락 감지)를 잃는다.
- 메타데이터가 필요해지더라도 application property 또는 별도 매핑 테이블로 충분히 표현 가능하다.

선택하지 않은 이유:
요구사항이 "5개 고정"으로 명시되어 있으므로, enum이 단순하고 안전하다. 미래에 subteam 추가가 빈번해지면 enum → 엔티티 마이그레이션을 별도 ADR로 다룬다.

### 2. `ChallengerPart` enum 재사용

기존 `ChallengerPart`(PLAN, DESIGN, WEB, ANDROID, IOS, NODEJS, SPRINGBOOT, ADMIN)를 그대로 subteam에 사용하는 방식이다.

장점:

- 신규 enum이 늘지 않는다.
- 기존 챌린저 분류와 일관된 라벨을 공유한다.

단점:

- `ChallengerPart`는 챌린저 학습 트랙 분류이며, Product Team의 운영 조직 단위와 의미가 다르다. 예를 들어 챌린저는 `WEB`/`NODEJS`/`SPRINGBOOT`로 세분화되지만, Product Team의 서버팀은 단일 단위다. 챌린저는 `PLAN`/`ADMIN`이 있지만 Product Team에는 없다.
- 의미가 같지 않은 enum을 재사용하면 분기 누락(예: `WEB`이 들어왔는데 Product Team 분기에서 처리되지 않음)이 컴파일러로 보호되지 않는다.
- ADR-003에서도 동일한 이유로 `ChallengerPart`를 외부 시스템 라우팅에 재사용하지 않기로 결정했다.

선택하지 않은 이유:
이름이 비슷하다고 의미가 같지 않다. 의미 분리를 우선한다.

### 3. 역할 정보를 `ProductTeam`/`ProductSubteam` 자체에 비정규화 저장

예: `ProductSubteam` 행에 `current_leader_member_id` 컬럼을 두는 방식.

장점:

- "현재 팀장은 누구냐" 질문에 한 row 조회로 답할 수 있다.
- 스키마가 작아 보인다.

단점:

- "기수별로 수행한 역할"이라는 요구사항을 정면으로 위반한다. 과거 13기 팀장이 누구였는지 추적할 수 없게 된다.
- 운영 사고(사람이 잘못 갱신)로 과거 임명 기록이 영구 손실될 수 있다.
- 동일 인물이 기수마다 다른 역할을 가지는 케이스를 표현할 수 없다.

선택하지 않은 이유:
기수별 임명 이력 보존이 본 요구사항의 핵심이다. 비정규화는 정보 손실을 만든다.

### 4. "팀 총괄단"을 Product Team 전체 단위로 둔다

`ProductTeamMembership`에서 GENERAL_MANAGER는 subteam을 NULL로 두고, "13기 Product Team 총괄단"이 subteam과 무관하게 운영되는 모델이다.

장점:

- "총괄단"이라는 단어의 일반적 의미상 더 자연스러울 수 있다(특정 팀이 아니라 조직 전체 총괄).

단점:

- 사용자가 명시한 표현은 "**각 팀의** 팀장 / 팀 총괄단"이며, "각 팀의" 한정사가 subteam 단위 귀속을 강하게 시사한다.
- subteam을 NULL 허용하는 순간 정합성 검증이 복잡해진다(role = LEADER일 때만 subteam NOT NULL 등).
- Product Team 전체 단위 운영진(PM/PO)이 정말 필요해지면 별도 역할(예: `PRODUCT_LEAD`)을 enum에 추가하는 편이 의미가 명확하다.

선택하지 않은 이유:
요구사항 텍스트의 가장 자연스러운 해석은 subteam 귀속이다. 모호한 의미를 NULL로 표현하는 대신 "subteam NOT NULL + 새 역할은 enum 추가로 해결" 정책을 유지한다.

### 5. Product Team을 organization이 아닌 신규 도메인으로 분리

`com.umc.product.productteam` 같은 별도 도메인 패키지를 신설하는 방식이다.

장점:

- Product Team은 챌린저 운영(기수, 학교, 챕터, 스터디그룹)과 책임이 다르다는 점을 패키지 경계로 표현할 수 있다.

단점:

- Product Team은 결국 기수(`Gisu`)와 강한 연관(임명 기록의 키)을 갖는다. organization 도메인 안에 두면 `gisuId` 참조가 가까워 응집도가 높다.
- 신규 도메인을 만들 만큼의 외부 시스템 통합이나 별도 운영 책임이 본 ADR 시점에는 없다.

선택하지 않은 이유:
organization은 이미 학교/기수/챕터/스터디그룹 등 운영 조직을 담는 컨텍스트다. Product Team도 그 일종이다. 도메인 신설 비용이 가져다주는 분리 가치를 정당화하지 못한다.

## Consequences

### Positive

- Product Team이 챌린저 기수와 독립된 생명주기를 갖는 구조가 명확히 표현된다. `Gisu` 종료가 Product Team 종료를 강제하지 않는다.
- 기수별 임명 이력이 `ProductTeamMembership`에 영구 보존되어, "13기 디자인팀 팀장이 누구였지?"와 같은 질의에 단일 쿼리로 답할 수 있다.
- "팀장 1명"이라는 운영 규칙이 부분 UNIQUE 인덱스로 DB 무결성에 의해 보장된다. 운영 사고로 같은 기수에 두 명 팀장이 등록되는 상황이 원천 차단된다.
- enum 기반 subteam은 새로운 분기 추가 시 컴파일러가 누락을 감지하게 해준다.
- 기존 organization 도메인 컨벤션(VO/Embedded, `@ManyToOne` 부모, ID 참조)을 그대로 따라 코드 일관성이 유지된다.

### Negative

- subteam이 enum이므로 신규 subteam 추가에 코드 변경 + 마이그레이션이 필요하다(엔티티 분리 대안 대비).
- `ProductTeamMembership`은 (gisu, subteam) 축으로 빠르게 누적된다. 5 subteam × N개 역할 × M기수만큼 행이 늘어난다. 다만 운영 인원 규모상 절대값은 작다.
- "Product Team 전체 총괄"이 필요해지면 enum 확장 + 마이그레이션이 필요하다. 본 ADR은 이를 일부러 미래로 미룬다.
- 한 사람이 동시에 여러 subteam에 다른 역할로 임명되는 케이스(예: 디자인팀 팀장 + 서버팀 총괄단)가 의도되었는지 정책 결정이 필요하다. 본 ADR은 이를 허용한다(UNIQUE 제약은 (gisu, subteam, role=LEADER)에 한정). 운영상 금지가 필요하면 추가 제약을 별도 ADR로 도입한다.

### Neutral / Trade-offs

- `ProductTeam` 엔티티가 정말 단일 인스턴스로 운영될지(전사 단일 product team) 또는 복수 인스턴스로 운영될지(예: 서비스 단위 product team)는 운영 정책에 달려 있다. 본 모델은 1:N으로 두어 양쪽을 모두 허용한다. 단일 인스턴스 가정이 강해지면 application 레벨에서 단일 row 보장 정책을 추가할 수 있다.
- `ProductTeam.isActive` 플래그를 두지 않고 `period`에서 도출한다. `Gisu.isActive`처럼 운영진이 강제로 비활성화하고 싶은 케이스가 등장하면 플래그를 추가한다.
- `ProductTeamMembership`에 `period` 같은 자체 기간을 두지 않는다. 임명은 기수 시작/종료에 자연 종속된다고 가정한다. 기수 중간 임명·해임 추적이 필요하면 컬럼 추가가 필요하다.

## Implementation Notes

### 도메인 패키지 구조 (신규)

```
com.umc.product.organization/
├── domain/
│   ├── ProductTeam.java                    # Aggregate Root
│   ├── ProductTeamMembership.java          # 기수별 임명 기록
│   ├── enums/
│   │   ├── ProductSubteam.java             # FRONTEND, DESIGN, SERVER, MOBILE_IOS, MOBILE_ANDROID
│   │   └── ProductTeamRole.java            # TEAM_LEADER, GENERAL_MANAGER
│   └── vo/
│       └── ProductTeamPeriod.java          # startAt, endAt Embedded VO
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── command/
│   │   │   │   ├── RegisterProductTeamUseCase.java
│   │   │   │   ├── UpdateProductTeamUseCase.java
│   │   │   │   └── AssignProductTeamMembershipUseCase.java
│   │   │   └── query/
│   │   │       ├── GetProductTeamUseCase.java
│   │   │       └── ListProductTeamMembershipUseCase.java
│   │   └── out/
│   │       ├── command/
│   │       │   ├── SaveProductTeamPort.java
│   │       │   └── SaveProductTeamMembershipPort.java
│   │       └── query/
│   │           ├── LoadProductTeamPort.java
│   │           └── LoadProductTeamMembershipPort.java
│   └── service/
│       ├── ProductTeamCommandService.java
│       ├── ProductTeamQueryService.java
│       ├── ProductTeamMembershipCommandService.java
│       └── ProductTeamMembershipQueryService.java
└── adapter/
    ├── in/
    │   └── web/
    │       ├── ProductTeamCommandController.java
    │       ├── ProductTeamQueryController.java
    │       ├── dto/request/
    │       └── dto/response/
    └── out/
        └── persistence/
            └── productteam/
                ├── ProductTeamPersistenceAdapter.java
                ├── ProductTeamJpaRepository.java
                ├── ProductTeamMembershipPersistenceAdapter.java
                └── ProductTeamMembershipJpaRepository.java
```

### Domain 코드 골격

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_team")
public class ProductTeam extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Embedded
    private ProductTeamPeriod period;

    @Builder(access = AccessLevel.PRIVATE)
    private ProductTeam(String name, ProductTeamPeriod period) {
        validate(name, period);
        this.name = name;
        this.period = period;
    }

    public static ProductTeam create(String name, Instant startAt, Instant endAt) {
        return ProductTeam.builder()
            .name(name)
            .period(ProductTeamPeriod.of(startAt, endAt))
            .build();
    }

    public boolean isActiveAt(Instant moment) {
        return !moment.isBefore(period.getStartAt()) && moment.isBefore(period.getEndAt());
    }

    public void rename(String name) { /* ... */ }
    public void extendUntil(Instant newEndAt) { /* ... */ }

    private static void validate(String name, ProductTeamPeriod period) { /* ... */ }
}
```

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "product_team_membership",
    indexes = {
        @Index(name = "ix_ptm_gisu_subteam", columnList = "gisu_id, subteam"),
        @Index(name = "ix_ptm_member", columnList = "member_id")
    }
)
public class ProductTeamMembership extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_team_id", nullable = false)
    private ProductTeam productTeam;     // 같은 도메인 부모 → ManyToOne 허용

    @Column(name = "gisu_id", nullable = false)
    private Long gisuId;                 // cross-aggregate → ID 참조만

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductSubteam subteam;

    @Column(name = "member_id", nullable = false)
    private Long memberId;               // cross-domain → ID 참조만

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductTeamRole role;
}
```

부분 UNIQUE는 JPA `@UniqueConstraint`로 표현이 어려우므로 Flyway에서 직접 생성한다.

### Flyway 마이그레이션

`V2026.MM.DD.HH.MM__create_product_team.sql` (날짜는 PR 머지 시점 기준):

```sql
CREATE TABLE product_team (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_at TIMESTAMP WITH TIME ZONE NOT NULL,
    end_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_product_team_period CHECK (start_at < end_at)
);

CREATE TABLE product_team_membership (
    id BIGSERIAL PRIMARY KEY,
    product_team_id BIGINT NOT NULL REFERENCES product_team(id),
    gisu_id BIGINT NOT NULL,
    subteam VARCHAR(32) NOT NULL,
    member_id BIGINT NOT NULL,
    role VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX ix_ptm_gisu_subteam
    ON product_team_membership (gisu_id, subteam);

CREATE INDEX ix_ptm_member
    ON product_team_membership (member_id);

-- (product_team_id, gisu_id, subteam) 안에 LEADER는 1명만 허용
CREATE UNIQUE INDEX uk_ptm_leader_per_subteam
    ON product_team_membership (product_team_id, gisu_id, subteam)
    WHERE role = 'TEAM_LEADER';

-- (gisu, subteam, member) 단위 중복 임명 방지(같은 사람이 같은 팀에서 동일 역할로 두 번 등록 불가)
CREATE UNIQUE INDEX uk_ptm_unique_assignment
    ON product_team_membership (product_team_id, gisu_id, subteam, member_id, role);
```

### 검증 규칙 (도메인)

- `ProductTeamPeriod`: `startAt`/`endAt` NOT NULL, `startAt.isBefore(endAt)`. 위반 시 `OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_PERIOD_INVALID)` 등 신규 에러코드 추가.
- `ProductTeamMembership.create`: `productTeam`/`gisuId`/`subteam`/`memberId`/`role` NOT NULL.
- 팀장 중복(`(gisu, subteam, role=LEADER)` 단위 1명)은 1차로 도메인 메서드(`ProductTeamMembership.appointLeader(...)`)에서 사전 검증하되, 최종 보증은 DB unique 인덱스에 위임한다.

### 권한 정책

- Product Team 등록/수정/삭제와 멤버십 임명은 admin role만 가능. 기존 `OrganizationPermissionEvaluator` 패턴(`*PermissionEvaluator`)을 따라 `ProductTeamPermissionEvaluator`를 추가.
- 조회는 인증된 사용자에게 노출하되, member_id 기반 PII는 응답 DTO에서 마스킹/제한이 필요한지 보안 검토.

## Implementation Plan (Commit 단위)

각 커밋은 단독으로 빌드/테스트가 통과해야 하며, Conventional Commits(`<type>: <subject>`)를 따른다. 묶음 PR 제목은 `[Feat] organization 도메인에 ProductTeam 모델 추가`로 한다.

1. `chore: organization 도메인에 ProductTeam 패키지 스켈레톤 추가`
    - `domain/ProductTeam.java`, `domain/ProductTeamMembership.java`, `domain/enums/`, `domain/vo/`, `application/port/in/...`, `application/port/out/...`, `application/service/...`, `adapter/in/web/...`, `adapter/out/persistence/productteam/...` 빈 파일/패키지 정리.
    - `OrganizationErrorCode`에 신규 에러코드 자리만 추가(메시지 미정 placeholder는 두지 않고, 다음 커밋에서 정식 추가).

2. `feat: ProductSubteam, ProductTeamRole enum 추가`
    - `ProductSubteam { FRONTEND, DESIGN, SERVER, MOBILE_IOS, MOBILE_ANDROID }` + 한국어 displayName(`프론트엔드팀` 등) + sortOrder.
    - `ProductTeamRole { TEAM_LEADER("팀장"), GENERAL_MANAGER("팀 총괄단") }`.
    - 두 enum 모두 `from(String)` 정적 팩토리는 사용처가 명확해질 때까지 추가하지 않는다(YAGNI).

3. `feat: ProductTeamPeriod VO 추가`
    - `GisuPeriod`와 동일한 구조의 Embedded VO. 생성자 검증(NULL, `startAt < endAt`).
    - `OrganizationErrorCode`에 `PRODUCT_TEAM_START_AT_REQUIRED`, `PRODUCT_TEAM_END_AT_REQUIRED`, `PRODUCT_TEAM_PERIOD_INVALID` 추가.

4. `feat: ProductTeam Aggregate Root 도메인 작성`
    - 엔티티 + private Builder + `create(name, startAt, endAt)` 정적 팩토리.
    - 도메인 메서드: `rename(String)`, `extendUntil(Instant)`, `isActiveAt(Instant)`.
    - `OrganizationErrorCode`에 `PRODUCT_TEAM_NAME_REQUIRED` 등 추가.
    - 단위 테스트: 정상 생성, 잘못된 period, 이름 누락 케이스 한국어 `@DisplayName`.

5. `feat: ProductTeamMembership 엔티티와 도메인 메서드 작성`
    - `@ManyToOne(fetch = LAZY) ProductTeam`, `gisuId/memberId` Long 참조, subteam/role enum.
    - 정적 팩토리: `appointLeader(productTeam, gisuId, subteam, memberId)`, `appointGeneralManager(...)` 두 개로 명시적으로 나눠 구현체에서 role을 안전하게 강제.
    - 단위 테스트: 두 정적 팩토리 각각 동작, NULL 검증.

6. `feat: product_team / product_team_membership Flyway 마이그레이션 추가`
    - 위 SQL 적용. partial unique index 두 개 명시.
    - 마이그레이션 파일명은 PR 머지 직전 시각 기준으로 부여하여 충돌 회피.
    - 로컬에서 `./gradlew bootRun` 또는 Testcontainers 통합 테스트로 마이그레이션 적용 확인.

7. `feat: ProductTeam Persistence Adapter와 Repository 작성`
    - `ProductTeamJpaRepository extends JpaRepository<ProductTeam, Long>`: JPA 컨벤션의 `findById`, `existsByName` 등.
    - `ProductTeamPersistenceAdapter`: `Save/LoadProductTeamPort` 구현. CLAUDE.md의 read 메서드 시맨틱을 따라 `getById` / `findById` / `listAll` 분리.
    - 통합 테스트(Testcontainers): 저장/조회 happy path.

8. `feat: ProductTeamMembership Persistence Adapter와 Repository 작성`
    - `ProductTeamMembershipJpaRepository`: `findAllByProductTeamIdAndGisuId`, `existsByProductTeamIdAndGisuIdAndSubteamAndRole` 같은 JPA 메서드.
    - `ProductTeamMembershipPersistenceAdapter`: `Save/LoadProductTeamMembershipPort` 구현.
    - 통합 테스트: 같은 (gisu, subteam) 안에 LEADER 두 번 등록 시 DB 무결성 위반(`DataIntegrityViolationException`)을 검증.

9. `feat: ProductTeam 등록·수정·조회 UseCase 및 Service 작성`
    - `RegisterProductTeamUseCase`, `UpdateProductTeamUseCase`, `GetProductTeamUseCase` interface(port/in).
    - `ProductTeamCommandService` (`@Transactional`), `ProductTeamQueryService` (`@Transactional(readOnly = true)`) — CQRS 분리.
    - 단위 테스트(MockitoExtension): 각 UseCase happy path + 검증 실패 분기.

10. `feat: ProductTeamMembership 임명·해임·조회 UseCase 및 Service 작성`
    - `AssignProductTeamLeaderUseCase`, `AssignProductTeamGeneralManagerUseCase`, `RevokeProductTeamMembershipUseCase`, `ListProductTeamMembershipUseCase` interface.
    - `ProductTeamMembershipCommandService`/`ProductTeamMembershipQueryService`.
    - 도메인 메서드 사전 검증과 DB 무결성 위반의 두 단계 모두 테스트.

11. `feat: ProductTeam admin 컨트롤러와 응답 DTO 작성`
    - `ProductTeamCommandController`, `ProductTeamQueryController`(adapter/in/web). 입력 DTO는 `record` + `@Valid` + record `compact constructor` 검증.
    - Response DTO: `ProductTeamResponse`, `ProductTeamMembershipResponse`(record). Entity 직접 노출 금지(CLAUDE.md).
    - admin role 가드: `ProductTeamPermissionEvaluator` 추가 또는 기존 `*PermissionEvaluator` 패턴 따라가기.
    - Swagger API 문서 인터페이스(`ProductTeamCommandControllerApi` 등) 작성.

12. `test: ProductTeam 흐름 통합 테스트 추가 (선택)`
    - Spring Boot Test 기반 end-to-end: 등록 → 멤버십 임명(LEADER 1, GENERAL_MANAGER 2) → 조회 → LEADER 중복 임명 시 409.
    - 한국어 `@DisplayName`, Given/When/Then.

13. `docs: ProductTeam 운영 가이드 작성 (선택)`
    - `docs/guides/product-team.md`: Product Team 등록 절차, 기수별 임명 절차, 운영 사고 대응(잘못 임명한 LEADER 정정 등).
    - 본 ADR과 상호 링크.

## References

- 관련 ADR
    - [ADR-003: Figma 댓글 Discord 포워딩](003-figma-comment-discord-forwarder.md) — 의미가 다른 enum의 재사용 금지 원칙
- 기존 코드
    - [Gisu](../../src/main/java/com/umc/product/organization/domain/Gisu.java), [GisuPeriod](../../src/main/java/com/umc/product/organization/domain/vo/GisuPeriod.java) — 자체 lifecycle + Embedded period VO 패턴 참고
    - [Chapter](../../src/main/java/com/umc/product/organization/domain/Chapter.java), [StudyGroupMember](../../src/main/java/com/umc/product/organization/domain/StudyGroupMember.java) — 같은 도메인 내 부모 `@ManyToOne` 패턴 참고
    - [StudyGroup](../../src/main/java/com/umc/product/organization/domain/StudyGroup.java) — cross-aggregate ID 참조(`gisuId`) 패턴 참고
    - [ChallengerPart](../../src/main/java/com/umc/product/common/domain/enums/ChallengerPart.java) — 의미상 분리 대상
- 외부 자료
    - [PostgreSQL Partial Indexes](https://www.postgresql.org/docs/current/indexes-partial.html) — LEADER 1명 제약 구현 근거
