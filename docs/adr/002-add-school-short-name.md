# ADR-002: School 엔티티에 정식 이름과 별개로 축약형 이름 필드를 둔다

## Status

Proposed

## Context

`com.umc.product.organization.domain.School` 는 학교 정식 이름 한 가지만 보관한다.

```java
@Column(nullable = false)
private String name;
```

운영상 다음과 같은 요구가 늘어나고 있다.

- 모바일 / 챌린저 카드 / 일정 카드 등 좁은 영역에서 "동국대학교 서울캠퍼스" 같은 긴 정식 이름을 한 줄에 표시하기 어렵다.
- 검색·필터링 UI에서 `keyword` 매칭이 정식 이름에만 동작해서, 사용자가 "동대" / "동국대" 처럼 통상적으로 부르는 축약형으로 검색하면 결과가 나오지 않는다.
- 챌린저 검색(`/api/v1/challenger/search/cursor`)·회원 검색(`/api/v1/member/search`)·스터디 그룹 응답(`StudyGroupMemberResponse.schoolName`) 등 여러 응답이 학교명을 직접 사용하는데, FE 가 정식 이름을 매번 자체적으로 줄여 표시하면 표기가 일관되지 않는다.
- "동국대" 같은 축약형은 학교마다 통상적으로 정해진 한두 가지 표기법이 있으므로, 서버에서 정규화된 값을 내려주는 편이 일관성에 유리하다.

그러나 현실적으로 다음 제약도 있다.

- 모든 학교가 항상 표준 축약형을 가지지는 않는다(예: 분교, 캠퍼스 단위, 전공대학 등). 즉 일부 학교는 축약형이 비어 있어도 운영상 문제가 없어야 한다.
- 한국 대학들의 축약형은 서로 충돌할 수 있다(예: "한대" 가 한양대인지 한국대학교인지). 따라서 시스템 차원에서 축약형의 유일성을 보장하기는 어렵다.
- 기존 `school` 테이블에는 약 100여 건의 데이터가 이미 존재하므로, 새 컬럼을 추가하더라도 백필을 강제하지 않는 형태로 도입돼야 한다.

## Decision

우리는 `School` 엔티티에 정식 이름(`name`)과 별개로 **축약형 이름(`shortName`)** 필드를 단일 컬럼으로 추가하기로 결정한다.

- 컬럼 정의는 다음과 같다.
    - 컬럼명: `short_name`
    - 타입: `VARCHAR(20)` (한국 대학 축약형이 보통 2~6자임을 감안한 여유분)
    - **NULL 허용**: 축약형이 명확하지 않은 학교가 존재하므로 강제하지 않는다.
    - **UNIQUE 제약 두지 않는다**: 동일/유사 축약형이 학교 간 충돌하는 케이스가 실제로 존재한다.
- 도메인 노출: `School.getShortName()` 으로 조회한다. 부분 수정용으로 `updateShortName(String)` 도메인 메서드를 별도로 추가한다(`updateName`, `updateRemark` 와 동일한 패턴).
- 응답 DTO 노출: `SchoolDetailResponse`, `SchoolListItemResponse` 같이 학교를 직접 표현하는 응답에 `shortName` 필드를 추가한다. 다만 다른 도메인이 학교명을 임베드하여 보여주는 경우(`StudyGroupMemberResponse.schoolName` 등)는 단계적으로 변경한다.
- 검색: 학교 검색·챌린저 검색의 `keyword` 매칭 범위를 `name` 외에 `shortName` 으로 확장한다(`OR LIKE`).
- 마이그레이션: 컬럼 추가만 수행하고, 초기 값 백필은 별도 운영 작업으로 분리한다.

## Alternatives Considered

### 1. 단일 `short_name` 컬럼 추가 (선택안)

`school` 테이블에 nullable `VARCHAR` 컬럼을 한 개 추가한다.

장점:

- 스키마가 단순하다. JPA 엔티티에도 필드 한 줄과 도메인 메서드 한 개만 추가된다.
- QueryDSL 검색 조건에 `school.shortName.containsIgnoreCase(keyword)` 만 추가하면 되므로 기존 검색 로직 변경이 작다.
- nullable로 두면 기존 데이터 마이그레이션 부담이 없다.

단점:

- 학교당 축약형은 단 한 가지만 저장할 수 있어, "동대" 와 "동국대" 처럼 두 가지 통용 표기가 모두 통하길 바라는 케이스에는 한 가지를 선택해야 한다.
- 표시용·검색용·정렬용을 모두 한 컬럼으로 처리해야 해서, 추후 용도가 분화되면 또 다른 컬럼이 필요해질 수 있다.

선택한 이유:
현재 요구는 "한 학교당 통상 표기 1개" 수준이고, 검색 보조 / 좁은 영역 표시 용도가 대부분이다. 다중 별칭이 필요한 케이스는 아직 검증되지 않은 가설이므로, YAGNI 관점에서 가장 단순한 안을 선택한다.

### 2. 별도 `school_alias` 테이블 추가

`school_alias(id, school_id, alias, alias_type)` 같은 자식 테이블을 만들어 학교당 N개의 별칭을 보관한다.

장점:

- 한 학교에 여러 통용 표기(공식 축약형, 학교 내부 약어, 영어명 등)를 모두 등록할 수 있다.
- `alias_type` 으로 표시용 / 검색용 / 영문 표기 등을 구분할 수 있다.
- 추후 검색 가중치(공식 축약형 vs 내부 약어) 같은 메타데이터를 붙이기 좋다.

단점:

- 새 테이블·새 Repository·새 Adapter 가 필요해 코드 변경량이 커진다.
- 학교 한 건을 가져올 때마다 `LEFT JOIN` 또는 추가 쿼리가 발생한다. 검색 정렬 시 N+1 / 중복 row 처리도 신경써야 한다.
- 가장 흔한 사용 케이스("화면에 짧은 이름 한 개 보여주기")에 비해 인프라가 과하다.

선택하지 않은 이유:
다중 별칭 요구가 아직 명확하지 않다. 단일 컬럼으로 시작하고, 다중 별칭이 실제로 필요해지는 시점에 이 ADR을 보완하거나 새 ADR로 마이그레이션 결정을 따로 기록하는 편이 변경 비용 대비 합리적이다.

### 3. 애플리케이션 레벨 정적 매핑

`School.id` → 축약형 매핑을 코드(예: `Map<Long, String>`)나 리소스 파일(예: `school-short-names.yml`)에 둔다.

장점:

- DB 스키마를 변경하지 않아도 되어 마이그레이션이 필요 없다.
- 코드 리뷰 단계에서 학교명 정합성을 한 번에 점검할 수 있다.

단점:

- 학교가 추가될 때마다 코드/리소스 변경과 배포가 필요해 운영 친화적이지 않다.
- 운영진이 어드민 화면에서 직접 수정할 수 없다.
- 검색 로직을 매핑과 DB 양쪽에서 결합하는 형태가 되어 복잡해진다.

선택하지 않은 이유:
학교 추가/수정은 어드민이 운영 중 수행하는 활동이므로, 학교 메타데이터는 DB 에 함께 두는 편이 변경 친화적이다.

### 4. `name` 필드에 괄호로 함께 표기 (예: `"동국대학교 (동대)"`)

스키마 변경 없이 `name` 자체에 축약형을 포함해 저장한다.

장점:

- 마이그레이션이 필요 없다.

단점:

- 표시 영역마다 다른 길이의 텍스트가 필요하므로, 결국 화면 단에서 다시 파싱해야 한다.
- 검색 시 사용자가 입력한 "동대" 가 괄호 안 텍스트와 매칭되도록 별도 처리해야 하고, 정렬·표시 모두에 부작용이 있다.
- 정식 이름 자체가 변형되어 외부 시스템과의 연계(공식 학사 시스템, 통계 등) 시 혼선이 생긴다.

선택하지 않은 이유:
정식 이름의 정합성을 깨뜨리고, 이후 모든 소비자가 파싱 로직을 알아야 하는 약한 결합 모델이 된다.

### 5. JSON 형 `aliases` 컬럼

`school` 테이블에 `aliases jsonb` 같은 단일 JSON 컬럼을 두고 그 안에 여러 표기를 넣는다.

장점:

- 별도 테이블 없이 다중 별칭을 저장할 수 있다.
- 스키마 변화 비용이 적다.

단점:

- 검색 인덱스 구성이 어려워, `keyword` 매칭 시 풀스캔 또는 별도 GIN 인덱스가 필요하다.
- 도메인 모델에서 별칭을 단순 String 으로 다루지 못해 추가 매핑이 필요하다.
- 운영진이 어드민 화면에서 수정할 때 입력 검증이 까다롭다.

선택하지 않은 이유:
PostgreSQL JSON 활용 자체는 가능하지만, 단일 학교당 한 표기 수준의 요구에는 도구가 과하다. 다중 별칭이 필요한 시점에는 4번 안(별도 테이블) 이 더 명시적이다.

## Consequences

### Positive

- FE 가 좁은 영역에 표시할 짧은 학교명을 서버에서 일관되게 받을 수 있다.
- 사용자가 통상 표기로 검색할 때(예: "동대") 결과가 노출되어 검색 체감 품질이 향상된다.
- 단일 컬럼 추가에 그치므로 도메인 모델·DTO·QueryDSL 변경량이 작고, 기존 데이터 마이그레이션 부담도 없다.

### Negative

- 한 학교당 축약형 한 개만 저장되므로, 두 가지 통용 표기를 동시에 노출하려면 추가 의사결정이 필요하다.
- `shortName` 백필이 운영 작업으로 별도 분리되므로, 도입 직후에는 일부 학교에서 응답 값이 비어 있다. FE 가 null 케이스를 그래스풀 처리해야 한다.
- 학교 등록·수정 어드민 흐름(`AdminSchoolControllerApi`)과 요청 DTO(`CreateSchoolRequest`, `UpdateSchoolRequest`) 에 입력 필드를 추가해야 한다.

### Neutral / Trade-offs

- `shortName` 에 UNIQUE 제약을 두지 않으므로, 두 학교가 같은 축약형을 사용할 가능성을 운영 정책으로 막아야 한다(어드민 화면에서 입력 시 확인). 시스템이 강제하지는 않는다.
- 추후 다중 별칭(영문명, 캠퍼스 약어 등) 요구가 정식화되면, 이 ADR 을 superseded 처리하고 별도 `school_alias` 테이블로 이전하는 결정을 새 ADR 로 남길 수 있다.

## Implementation Notes

### Domain

`com.umc.product.organization.domain.School` 에 다음 변경을 가한다.

```java
@Column(name = "short_name", length = 20, nullable = true)
private String shortName;

public void updateShortName(String shortName) {
    if (StringUtils.hasText(shortName)) {
        this.shortName = shortName;
    }
}
```

`School.create(...)` 시그니처는 가능하면 다음과 같이 확장하되, 기존 호출자(`new School.create(name, remark)`)와의 호환을 위해 오버로드 또는 builder 활용을 검토한다.

```java
public static School create(String name, String remark, String shortName) { ... }
```

### Persistence Migration

Flyway 마이그레이션 파일을 새로 추가한다.

```sql
-- V2026.05.07.??.??__add_short_name_to_school.sql
ALTER TABLE school
    ADD COLUMN short_name VARCHAR(20);
```

백필은 별도 운영 작업(어드민 어플리케이션을 통한 수동 입력 또는 일회성 스크립트)으로 분리한다. 백필이 끝나기 전까지 응답에서 `shortName` 은 null 로 내려갈 수 있다는 점을 FE 와 합의한다.

### Adapter / DTO

- 요청 DTO: `CreateSchoolRequest`, `UpdateSchoolRequest` 에 `shortName` 필드를 추가한다.
- 응답 DTO: `SchoolDetailResponse`, `SchoolListItemResponse`, `SchoolNameListResponse` 등 학교를 직접 노출하는 곳에 `shortName` 필드를 추가한다. 학교명을 임베드해 사용하는 응답(`StudyGroupMemberResponse.schoolName` 등)은 단계적으로 검토한다.
- 검색 Adapter: `SchoolQueryRepository.searchSchools` 의 `keyword` 매칭 조건을 다음과 같이 확장한다.
  ```java
  private BooleanExpression keywordContains(String keyword) {
      if (!StringUtils.hasText(keyword)) return null;
      return school.name.containsIgnoreCase(keyword)
          .or(school.shortName.containsIgnoreCase(keyword));
  }
  ```

### Operational

- 어드민 운영진에게 학교별 축약형 백필 작업이 필요하다는 점을 안내한다.
- 어드민 화면에서 동일 축약형이 입력될 때 경고를 띄우는 UX 를 권장한다(시스템 강제 아님).

## References

- 코드 위치: [`School.java`](../../src/main/java/com/umc/product/organization/domain/School.java)
- 검색 Adapter: [`SchoolQueryRepository.java`](../../src/main/java/com/umc/product/organization/adapter/out/persistence/school/SchoolQueryRepository.java)
- 학교명을 사용하는 응답 DTO: `SchoolDetailResponse`, `SchoolListItemResponse`, `SchoolNameListResponse`, `StudyGroupMemberResponse`
- 관련 운영 정책 / 백필 절차: N/A (별도 합의 필요)
