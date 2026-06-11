# ADR-006: 프로덕트팀은 기능 조직과 Squad를 분리한 조직 표시 모델로 관리한다

## Status

Proposed

## Context

2026년 6월 기준 UMC PRODUCT는 기존 iOS, Android, Web, Server, Design 중심의 기술·플랫폼 조직에서 기능 조직과 목적 조직을 분리하는 구조로 개편하려 한다. 개편안의 핵심은 "소속과 실행의 분리"다. 기능 조직은 멤버의 소속, 전문성, 품질 기준, 권한 기준을 관리하고, Squad는 특정 제품 목표를 수행하는 실행 단위로 운영한다. Product HQ는 전체 방향, 우선순위, 리소스 조율, 출시 기준을 담당한다.

이 서버에는 프로덕트팀을 아카이빙하고 공개 조회하는 API가 필요하다. 기존 설계안은 `ProductTeamPart` enum과 `ProductTeamMembership` 하나로 기수별 활동을 표현했지만, 개편안의 챕터·파트 계층과 기수와 독립적인 Squad 활동을 충분히 담지 못한다. 또한 프로덕트팀은 별도 권한 도메인이 아니라 조직도와 활동 이력 표시용 모델이므로 `ResourceType.PRODUCT_TEAM` 같은 권한 리소스를 추가하지 않는다.

아키텍처 제약도 함께 지켜야 한다. `organization` 도메인 하위에서 Hexagonal Architecture를 유지하고, 도메인 간 객체 참조는 하지 않으며 멤버는 `memberId`로만 참조한다. 웹 어댑터는 UseCase만 호출하고, 조회 응답은 domain entity가 아니라 `Info`와 Response DTO로 매핑한다.

### 문제점

1. **기능 조직은 enum으로 고정하기 어렵다.** 3기 이후 Product HQ, 클라이언트 챕터, 플랫폼 챕터, 디자인 챕터, 각 파트의 구성은 기수마다 달라질 수 있다. `ProductTeamPart` enum만으로는 부모·자식 구조, 표시명, 정렬, 활성 여부를 기수별로 관리할 수 없다.

2. **Squad는 기수에 직접 종속되지 않는다.** Squad는 특정 제품 목표를 수행하는 목적 조직이며, 기수 기간과 겹쳐 보일 수는 있지만 특정 `ProductTeamGeneration`에 FK로 묶이지 않아야 한다. 1기/2기 활동도 기간과 참여자로 소급 등록할 수 있어야 한다.

3. **역할과 표시용 포지션의 의미가 다르다.** `PRODUCT_LEAD`, `PART_LEAD`는 조직 운영상 role이고, `SERVER_DEVELOPER`, `PRODUCT_DESIGNER`는 사람이 수행한 position이다. 두 개념을 분리하지 않으면 권한 판단과 화면 표시가 섞인다.

4. **참여 상태와 기여 유형은 현재 영속 모델의 핵심 축이 아니다.** 개편안에는 Core, Active, Support, Pause, Alumni 같은 참여 상태가 있지만, 이번 API 요구사항은 담당 영역과 활동 이력을 보여주는 것이다. 기수 독립 Squad에서 필요해질 수 있는 기여 강도는 후속 요구로 남기고, 현재는 role, position, responsibilityTitle, responsibilityDescription으로 담당 범위를 표현한다.

### 결정이 필요한 이유

현재 PR은 아직 `develop`에 merge되지 않았으므로 기존 product team migration과 엔티티를 갈아엎을 수 있다. 이 시점에 모델을 바로잡지 않으면 이후에는 운영 데이터 마이그레이션 비용이 커지고, enum 기반 모델을 확장하기 위해 호환성 레이어를 계속 추가해야 한다.

## Decision

우리는 프로덕트팀을 `organization` 도메인 하위의 조직도·활동 이력 표시 모델로 두고, 기능 조직과 Squad를 분리해 저장하기로 결정한다.

1. **프로덕트팀 기수는 기존 UMC `Gisu`와 분리한다.** `ProductTeamGeneration`은 `generation`, `startAt`, `endAt`, `isActive`만 갖는 독립 시즌 모델이다.

2. **프로덕트팀 멤버 프로필은 별도 archive profile로 저장한다.** `ProductTeamMember`는 `memberId`, `introduction`, `profileImageId`를 저장한다. 조회 응답에는 member 기본 프로필 이미지와 프로덕트팀 전용 프로필 이미지를 모두 내려준다.

3. **기능 조직은 기수별 tree로 모델링한다.** `ProductTeamFunctionalUnit`은 `productTeamGenerationId`, `parentUnitId`, `type`, `code`, `name`, `description`, `sortOrder`, `isActive`를 가진다. `type`은 `PRODUCT_HQ`, `CHAPTER`, `PART`다.

4. **기능 조직 활동은 멤버십으로 저장한다.** `ProductTeamFunctionalMembership`은 `productTeamMember`, `productTeamGenerationId`, `functionalUnitId`, `role`, `position`, `responsibilityTitle`, `responsibilityDescription`을 가진다. 한 멤버는 같은 기수에서 챕터와 파트 멤버십을 동시에 가질 수 있고, 같은 기수에서 여러 role·position을 겸임할 수 있다.

5. **Squad는 기수와 독립된 목적 조직으로 저장한다.** `ProductTeamSquad`는 `code`, `name`, `description`, `startAt`, `endAt`, `sortOrder`, `isActive`를 가진다. `startAt`과 `endAt`은 nullable이며 둘 다 있으면 `startAt < endAt`만 검증한다.

6. **Squad 참여자는 별도 목록으로 저장한다.** `ProductTeamSquadParticipant`는 `squad`, `productTeamMember`, `role`, `position`, `responsibilityTitle`, `responsibilityDescription`을 가진다. Squad role은 `SQUAD_LEAD`, `MEMBER`만 둔다.

7. **권한은 별도 product team resource가 아니라 서비스 정책으로 판단한다.** 중앙 총괄단 이상은 전체 수정 가능하다. 활성 프로덕트팀 기수의 `PRODUCT_LEAD` 또는 `PRODUCT_VICE_LEAD`도 프로덕트팀 조직도, 멤버십, Squad를 수정할 수 있다. 일반 멤버는 본인의 `ProductTeamMember` 자기소개와 프로덕트팀 전용 프로필 이미지만 수정할 수 있다.

8. **기수별 조직도 조회는 기간 겹침으로 Squad를 계산한다.** `GET /api/v1/product-team/organization-chart?productTeamGenerationId=`는 해당 기수의 기능 조직 tree와, 기간이 기수 기간과 겹치는 Squad를 함께 반환한다. 기간이 없는 Squad는 전체 Squad 조회에는 포함하지만 기수별 자동 매칭에는 포함하지 않는다.

9. **용어는 API와 코드에서 Squad로 통일한다.** API, 엔티티, ADR 제목과 결정명에는 Squad를 사용한다.

### 단계적 진행 / PR 분할

- **Phase 1 (이 PR / 본 ADR)**: 엔티티, migration, port, service, 공개 조회 API, 관리 API, 테스트를 기능 조직 + Squad 모델로 재작성한다.
- **Phase 2 (별도 PR)**: 운영 화면 요구가 확정되면 조직도 tree 응답에 멤버 배치, Squad별 참여자 상세, seed/admin import API를 추가한다.
- **Phase 3 (시점 미정)**: 참여 상태(Core, Active, Support, Pause, Alumni)나 기여 유형이 실제 운영 지표로 필요해지면 Squad 참여 모델에 별도 컬럼 또는 이력 테이블로 추가한다.

## Alternatives Considered

### 대안 A: 기존 enum 기반 ProductTeamPart 유지

`ProductTeamPart` enum과 단일 멤버십 테이블로 파트, role, position을 계속 표현한다.

장점:

- 구현량이 가장 작다.
- 조회 조건이 단순하다.

단점:

- 기수별 조직 구성이 바뀔 때 enum 배포가 필요하다.
- 챕터와 파트의 부모·자식 구조를 표현하기 어렵다.
- Product HQ와 Squad를 같은 축에 끼워 넣게 되어 의미가 흐려진다.

선택하지 않은 이유:

- 개편안의 핵심인 기능 조직 tree와 기수 독립 Squad를 표현할 수 없다.

### 대안 B: 기능 조직과 Squad를 같은 unit으로 일반화

하나의 `ProductTeamUnit` 테이블에 `FUNCTIONAL`, `SQUAD` 같은 type을 두고 모든 조직을 같은 방식으로 저장한다.

장점:

- 테이블 수가 줄어든다.
- 조직도 구현을 하나의 abstraction으로 통일할 수 있다.

단점:

- 기능 조직은 기수별 소속이고 Squad는 기간 기반 실행 단위라는 생명주기가 다르다.
- 기능 조직 role과 Squad role의 unique 정책이 다르다.
- 기간 없는 Squad와 기수별 기능 조직의 조회 규칙이 한 테이블 안에서 복잡해진다.

선택하지 않은 이유:

- 데이터 모델은 단순해 보이지만 서비스 규칙과 제약 조건이 오히려 불명확해진다.

### 대안 C: Squad를 ProductTeamGeneration에 직접 종속

`ProductTeamSquad`가 `productTeamGenerationId` FK를 갖도록 설계한다.

장점:

- 기수별 조직도 조회가 단순하다.
- 한 기수 안의 Squad만 관리하면 되는 초기 화면에는 편하다.

단점:

- Squad가 기수 경계를 넘거나 1기/2기 활동을 소급 등록할 때 중복 데이터가 생긴다.
- 기수와 독립적으로 운영되는 목적 조직이라는 개편 방향과 맞지 않는다.

선택하지 않은 이유:

- Squad는 기수에 귀속된 조직이 아니라 제품 목표와 기간으로 묶이는 실행 단위다.

### 대안 D: 별도 product-team 도메인 분리

`organization`이 아니라 `productteam` 같은 새 도메인 패키지를 만든다.

장점:

- 프로덕트팀 관련 모델을 물리적으로 독립시킬 수 있다.
- 향후 기능이 커졌을 때 소유 경계가 분명하다.

단점:

- 현재 요구는 조직도와 활동 이력 표시이며, 별도 bounded context로 분리할 정도의 행위가 많지 않다.
- `organization`의 기수·학교·권한 조회 UseCase와 함께 다루는 흐름이 많아 초기 비용이 커진다.

선택하지 않은 이유:

- 현재는 organization 하위 archive 모델로 충분하고, 도메인 분리는 실제 운영 행위가 커진 뒤 결정해도 된다.

## Consequences

### Positive

- 기수별 기능 조직 구성을 DB 데이터로 유연하게 바꿀 수 있다.
- 한 멤버가 같은 기수에서 여러 챕터·파트·직책·포지션을 겸임한 기록을 보존할 수 있다.
- Squad를 기수와 독립적으로 생성하고 기간 겹침으로 표시할 수 있어 과거 활동 소급 등록이 쉽다.
- 권한 리소스를 새로 만들지 않아 기존 authorization 도메인의 관리 부담이 늘지 않는다.

### Negative

- 테이블과 API 수가 늘어나 초기 구현·테스트 범위가 커진다.
- 조직도 조회는 기능 조직과 Squad를 함께 조합해야 하므로 단일 enum 필터보다 쿼리와 매핑이 복잡하다.
- nullable 기간을 허용하는 Squad는 운영자가 기간 없는 항목과 기수 자동 매칭 제외 규칙을 이해해야 한다.

### Neutral / Trade-offs

- `position`은 기능 조직과 Squad에서 재사용한다. 화면 표시와 검색은 단순해지지만, Squad 전용 position이 필요해지면 enum 확장이 필요하다.
- `responsibilityTitle`과 `responsibilityDescription`으로 담당 범위를 자유롭게 표현한다. 구조화된 통계에는 약하지만, 초기 archive 요구에는 유연하다.
- 참여 상태와 기여 유형은 이번 영속 모델에서 제외한다. 실제 운영에서 안정적으로 쓰일 때 추가하는 편이 schema churn을 줄인다.

## Implementation Notes

### 변경 영역 요약

1. **도메인** (`com.umc.product.organization.domain.*`): `ProductTeamGeneration`, `ProductTeamMember`, `ProductTeamFunctionalUnit`, `ProductTeamFunctionalMembership`, `ProductTeamSquad`, `ProductTeamSquadParticipant`를 사용한다. `ProductTeamPart`, `ProductTeamMembership`, `ProductTeamRole`은 제거한다.

2. **응용 / Port** (`...application.service.*`, `...application.port.*`): generation, member, functional unit, squad command/query usecase를 분리한다. 권한 판단은 `ProductTeamAccessPolicy`에서 중앙 총괄단 또는 활성 기수 product lead/vice lead 여부로 처리한다.

3. **어댑터 (in)** (`...adapter.in.web.*`): 공개 조회 API는 generations, members, functional-units, squads, organization-chart를 제공한다. 관리 API는 서비스 권한 정책을 통과해야 한다.

4. **어댑터 (out)** (`...adapter.out.persistence.productteam.*`): JPA repository와 persistence adapter는 기능 조직 멤버십과 Squad 참여자를 별도 port로 제공한다.

5. **DB / 마이그레이션** (`src/main/resources/db/migration/V2026.06.04.10.00__create_product_team_archive.sql`): `product_team_generation`, `product_team_member`, `product_team_functional_unit`, `product_team_functional_membership`, `product_team_squad`, `product_team_squad_participant`를 생성한다.

6. **테스트** (`src/test/...`): 기능 조직 겸임, unique 제약, Squad 기간 검증, 기수별 Squad 기간 겹침 조회, 본인 프로필 수정과 활동 수정 거부를 검증한다.

### DB 제약

- `product_team_generation.generation`은 unique다.
- active product team generation은 partial unique index로 1개만 허용한다.
- `product_team_member.member_id`는 unique다.
- 기능 조직은 `(product_team_generation_id, type, code)`가 unique다.
- 기능 멤버십은 `(product_team_member_id, product_team_generation_id, functional_unit_id, role, position, responsibility_title)` 중복을 막는다.
- `PRODUCT_LEAD`, `PRODUCT_VICE_LEAD`는 기수당 각 1명만 허용한다.
- `CHAPTER_LEAD`, `PART_LEAD`는 기능 조직 단위당 1명만 허용한다.
- Squad `code`는 unique다.
- Squad 참여자는 `(squad_id, product_team_member_id, role, position, responsibility_title)` 중복을 막는다.
- `SQUAD_LEAD`는 Squad당 1명만 허용한다.

## References

- 첨부 문서: `UMC PRODUCT 조직 개편안 제안서`
- 구현 파일: `src/main/resources/db/migration/V2026.06.04.10.00__create_product_team_archive.sql`
