# ADR-017: 프로젝트 지원/매칭 현황 조회를 ProjectMember 기준으로 통합한다

## Status

Accepted (2026-05-19)

## Context

2026년 5월 기준 프로젝트 도메인의 통계 조회는 지원 현황과 매칭 현황이 분리되어 있었다.

- 지원 현황: `ProjectApplicationStatisticsQueryRepository` 가 `ProjectApplication` 기준으로 차수별 지원자 row 를 조회했다.
- 매칭 현황: `ProjectMemberStatisticsQueryRepository` 가 `ProjectMember` 기준으로 매칭 멤버 row 를 조회했다.
- 응용 계층도 `ApplicationStatisticsQueryService` 와 `MatchingStatisticsQueryService` 로 나뉘어 있었다.
- Web API 도 `/api/v1/projects/statistics/applications`, `/api/v1/projects/statistics/matchings` 로 분리되어 있었다.

이 구조는 "지원 집계"와 "매칭 집계"를 각각 숫자로 내려줄 때는 동작하지만, 최종 사용자 화면에서 실제로 필요한 질문과 어긋난다. 화면은 프로젝트별 최종 팀원(`ProjectMember`)을 먼저 보고, 해당 멤버가 이 프로젝트에 어떤 차수의 지원서를 작성했는지, 그 지원서가 어떤 `MatchingType` / `MatchingPhase` 에 연결되는지를 함께 알아야 한다.

### 문제점

1. **두 Repository 의 조회 축이 사실상 중복된다.** 두 쿼리는 모두 프로젝트, 멤버, 매칭 차수의 조합을 얻기 위해 별도 경로를 탄다. 결과적으로 지원/매칭 분리 API 는 같은 화면을 그리기 위해 서로 다른 집계 모델을 클라이언트에서 다시 맞춰야 한다.

2. **강제 배정 또는 랜덤 배정 멤버를 자연스럽게 표현하기 어렵다.** `ProjectMember.application` 은 nullable 이다. 지원서 없이 배정된 멤버도 최종 팀원에는 포함되어야 하지만, 기존 매칭 통계는 application inner join 으로 이런 멤버를 제외했다.

3. **동일 멤버의 여러 차수 지원 이력을 잃을 수 있다.** 같은 멤버는 동일 프로젝트의 동일 차수에는 중복 지원할 수 없지만, 여러 차수에 걸쳐 같은 프로젝트에 다시 지원할 수 있다. 따라서 프로젝트 멤버 1명당 지원서 1건을 선택하면 지원 현황이 부정확해진다.

4. **PM 소유 프로젝트 기준 조회가 요구사항과 맞지 않는다.** 기존 서비스는 운영진과 PM 챌린저의 호출자를 구분해 scope 를 나눴다. 새 요구사항은 PM이 "자기가 PO인 프로젝트"를 조회하는 방식이 아니라, 단건 `projectId` 또는 `chapterId` 를 명시해 해당 범위의 지원/매칭 현황을 받는 방식이다.

### 결정이 필요한 이유

이번 변경은 단순 DTO 변경이 아니라 조회 모델의 기준을 바꾸는 작업이다. 분리 통계 구조를 남긴 채 새 API 를 추가하면 같은 도메인 사실을 두 모델이 서로 다르게 해석할 위험이 커진다. 특히 application nullable 멤버와 여러 차수 지원 이력은 운영 화면에서 누락되기 쉬운 데이터이므로, 조회 기준을 하나로 고정해야 한다.

## Decision

우리는 프로젝트 지원/매칭 현황 조회를 **`ProjectMember` 기준의 단일 조회 모델**로 통합하기로 결정한다.

1. API 는 지원 현황과 매칭 현황을 분리하지 않는다.
2. 조회 범위는 `projectId` 단건 또는 `chapterId` 전체로만 받는다.
3. `ACTIVE ProjectMember` 를 먼저 조회한 뒤, 각 멤버가 해당 프로젝트에 작성한 `ProjectApplication` 전체를 `projectId + memberId` 기준으로 조회한다.
4. `ProjectApplication` 은 `SUBMITTED`, `APPROVED`, `REJECTED` 만 지원 현황으로 포함한다. `DRAFT`, `CANCELLED` 는 현황 조회에서 제외한다.
5. 같은 멤버가 같은 프로젝트에 여러 차수로 지원한 경우 각 지원서를 모두 `applications[]` 에 포함한다.
6. 지원서 없이 합류한 멤버는 `applications: []` 로 반환한다.
7. 각 지원서에는 연결된 `ProjectMatchingRound` 의 `id`, `type`, `phase` 를 포함한다.

### 단계적 진행 / PR 분할

- **Phase 1 (이 PR / 본 ADR)**: 기존 분리 통계 구현을 제거하고 `ProjectStatistics` 단일 UseCase, Port, QueryRepository, Controller, Response DTO 를 추가한다.
- **Phase 2 (별도 PR 후보)**: 권한 정책이 확정되면 `chapterId` 전체 조회에 대해 중앙 운영진/지부장 scope 를 더 엄격히 적용한다.
- **Phase 3 (시점 미정)**: 최종 팀원 조회 화면에서 멤버 상세 정보가 필요해지면 `Member` / `Challenger` Query UseCase 를 조합한 별도 assembler 응답을 추가한다.

## Alternatives Considered

### 대안 A: 기존 지원/매칭 분리 API 유지

기존 `ApplicationStatisticsQueryService` 와 `MatchingStatisticsQueryService` 를 유지하고 클라이언트가 두 API 를 함께 호출한다.

장점:

- 기존 구현을 거의 유지할 수 있다.
- 차수별 count 중심 화면에는 재사용 가능하다.

단점:

- 최종 팀원과 지원 이력을 클라이언트에서 재조합해야 한다.
- application nullable 멤버가 매칭 통계에서 누락될 수 있다.
- 같은 멤버의 여러 차수 지원 이력을 프로젝트 멤버 기준으로 표현하기 어렵다.

선택하지 않은 이유:

- 새 요구사항의 기준 데이터는 count 가 아니라 `Project -> ProjectMember -> ProjectApplication history` 이다. 기존 분리 모델은 이 구조를 우회적으로 표현하므로 유지 가치보다 혼란이 크다.

### 대안 B: `ProjectMember.application` FK 만 사용

`ProjectMember.application` 이 있으면 해당 지원서 1건만 응답하고, 없으면 강제 배정으로 판단한다.

장점:

- 쿼리가 단순하다.
- 최종 합류를 만든 지원서를 직접 볼 수 있다.

단점:

- 현재 강제 배정뿐 아니라 기존 생성 경로에서도 application 이 세팅되지 않을 수 있다.
- 같은 멤버가 같은 프로젝트에 여러 차수로 지원한 이력을 잃는다.
- FK 1개는 "최종 합류 근거"일 수는 있어도 "지원 현황 전체"가 아니다.

선택하지 않은 이유:

- 사용자는 지원 현황에 여러 차수 지원 이력이 모두 반영되어야 한다고 명시했다. 따라서 FK 단건 조회는 요구사항을 충족하지 못한다.

### 대안 C: ProjectApplication 기준으로 시작하고 ProjectMember 를 left join

지원서를 먼저 조회한 뒤 최종 멤버 여부를 left join 으로 붙인다.

장점:

- 지원 이력 전체를 빠뜨리지 않는다.
- 지원 현황 중심 화면에는 자연스럽다.

단점:

- 지원서 없이 합류한 멤버는 별도 union 쿼리 또는 추가 조회가 필요하다.
- 최종 팀원 목록이 없는 프로젝트 멤버를 표현하기 어렵다.

선택하지 않은 이유:

- 새 화면의 루트는 최종 프로젝트 구성원이다. 따라서 `ProjectMember` 를 기준으로 시작하는 편이 더 직접적이다.

## Consequences

### Positive

- 지원/매칭 현황을 한 API 응답에서 일관되게 볼 수 있다.
- application nullable 멤버도 최종 팀원으로 누락되지 않는다.
- 동일 멤버의 여러 차수 지원 이력을 모두 표현할 수 있다.
- 기존 중복 Repository / Service / DTO 를 제거해 유지보수 표면이 줄어든다.

### Negative

- 기존 `/statistics/applications`, `/statistics/matchings` API 는 제거되므로 클라이언트 전환이 필요하다.
- chapter 단위 조회는 프로젝트와 멤버 수에 따라 응답 크기가 커질 수 있다.
- count 기반 통계가 다시 필요해지면 새 응답을 기반으로 별도 집계 API 를 재설계해야 한다.

### Neutral / Trade-offs

- `ProjectMember.application` FK 를 직접 사용하지 않고 `projectId + memberId` 로 지원서를 찾는다. FK 하나의 의미는 약해지지만, 여러 차수 지원 이력을 보존한다.
- 응답에는 멤버 실명/닉네임을 포함하지 않는다. 다른 도메인의 멤버 정보를 직접 참조하지 않는 Hexagonal Architecture 규칙을 지키기 위해, 필요 시 별도 assembler 에서 `GetMemberUseCase` 를 조합한다.

## Implementation Notes

### 변경 영역 요약

1. **응용 / Port**
   - 추가: `GetProjectStatisticsUseCase`
   - 추가: `ProjectStatisticsInfo`, `ProjectMemberStatisticsInfo`, `ProjectMemberApplicationStatisticsInfo`, `ProjectMatchingRoundStatisticsInfo`
   - 추가: `LoadProjectStatisticsPort`
   - 추가: `ProjectStatisticsMemberRow`, `ProjectStatisticsApplicationRow`
   - 제거: `GetApplicationStatisticsUseCase`, `GetMatchingStatisticsUseCase`, `LoadApplicationStatisticsPort`, `LoadMatchingStatisticsPort`

2. **응용 / Service**
   - 추가: `ProjectStatisticsQueryService`
   - 제거: `ApplicationStatisticsQueryService`, `MatchingStatisticsQueryService`

3. **어댑터 (in)**
   - 변경: `ProjectStatisticsQueryController`
   - 변경: `ProjectResponseAssembler`
   - 추가: `ProjectStatisticsResponse`
   - 제거: `ApplicationStatisticsResponse`, `MatchingStatisticsResponse`

4. **어댑터 (out)**
   - 추가: `ProjectStatisticsQueryRepository`, `ProjectStatisticsPersistenceAdapter`
   - 제거: `ProjectApplicationStatisticsQueryRepository`, `ProjectApplicationStatisticsPersistenceAdapter`
   - 제거: `ProjectMemberStatisticsQueryRepository`, `ProjectMemberStatisticsPersistenceAdapter`

5. **테스트**
   - 추가: `ProjectStatisticsQueryServiceTest`
   - 추가: `ProjectStatisticsQueryControllerTest`

### 새 API

```text
GET /api/v1/projects/{projectId}/statistics
GET /api/v1/projects/statistics?chapterId={chapterId}
```

### 롤백 시 주의사항

기존 분리 API 를 되살리려면 제거된 UseCase/Port/Service/Repository/Response 를 함께 복구해야 한다. 새 `ProjectStatistics` 구현과 기존 분리 구현을 동시에 유지하면 통계 기준이 달라질 수 있으므로, 롤백은 API 단위로 일괄 수행해야 한다.

## References

- `src/main/java/com/umc/product/project/domain/ProjectMember.java`
- `src/main/java/com/umc/product/project/domain/ProjectApplication.java`
- `src/main/java/com/umc/product/project/domain/ProjectMatchingRound.java`
