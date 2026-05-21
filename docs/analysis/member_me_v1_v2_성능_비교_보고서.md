# `/me` 성능 비교 보고서 — v1 vs v2

UMC PRODUCT Backend / 작성일: 2026-05-19 / 대상 PR: [#885](https://github.com/UMC-PRODUCT/umc-product-server/pull/885)

## TL;DR

| 항목 | v1 (PR 이전) | v1 (PR 적용 후) | v2 (BFF) |
|---|---:|---:|---:|
| DB 쿼리 수 (N=3 기수) | **12** | **6** | **8** |
| DB 쿼리 수 (N 기수) | **3N+3** | **6** | **8** |
| 트랜잭션 경계 | 분리(6+) | 분리(6+) | **단일 readOnly** |
| 응답 데이터 | 프로필 + 이력 | 동일 | + 활동일 / 활성기수 강조 / 운영진 / 기수별 점수 |
| 시점 일관성 | 약함 | 약함 | **보장** |
| RTT(클라이언트 호출 수) | 1 + α | 1 + α | **1** |

`α`는 클라이언트가 "현재 운영진인지", "총 활동일 며칠인지" 등을 별도 API로 보충해야 했던 호출 수입니다.

---

## 비교 환경 — 가정

회원 A (`memberId=10`) 라는 한 사용자가 다음 데이터를 보유한다고 가정합니다.

- 참여 기수: **3개** (6기 WEB / 7기 SPRINGBOOT / 8기 SPRINGBOOT)
- 챌린저 상태: 모두 ACTIVE (가장 최근 8기는 활성 기수)
- 보유 상벌점: 총 30건 (기수별 평균 10건)
- 활성 기수에 `SCHOOL_PRESIDENT` 운영진 1건

쿼리 수 모델링은 JPA 단일 쿼리 단위로 계산합니다 (커넥션 풀·캐시 hit 제외, worst case).

---

## v1 (PR 이전) — N+1 패턴이 다층

### 호출 그래프

```
GET /api/v1/member/me
       ↓
MemberQueryController.getMyProfile
       ↓
MemberInfoResponseAssembler.fromMemberId(memberId)
       │
       ├── getMemberUseCase.getById(memberId)                          ── Q1
       │
       ├── getChallengerUseCase.getAllByMemberId(memberId)
       │     └─ ChallengerQueryService.getAllByMemberId (PR 이전)
       │          ├─ loadChallengerPort.getAllByMemberId               ── Q2
       │          └─ for each challenger:
       │              └─ getChallengerPointUseCase                     ── Q3, Q4, Q5  (N+1)
       │                  .getListByChallengerId(challenger.id)
       │
       ├── for each challenger:                                        # adapter loop
       │     ├── getGisuUseCase.getById(gisuId)                        ── Q6, Q7, Q8  (N+1)
       │     └── getChapterUseCase.byGisuAndSchool(gisuId, schoolId)   ── Q9, Q10, Q11 (N+1)
       │
       └── getMemberProfileUseCase.getMemberProfileById(memberId)      ── Q12
```

### 쿼리 수: **3N + 3 = 12** (N=3)

| 쿼리 출처 | 횟수 |
|---|---:|
| `member.findById` (+school join) | 1 |
| `challenger.findByMemberId` | 1 |
| `challenger_point.findByChallengerId` (챌린저당 1회) | N = 3 |
| `gisu.findById` (챌린저당 1회) | N = 3 |
| `chapter.byGisuAndSchool` (챌린저당 1회) | N = 3 |
| `member_profile.findByMemberId` | 1 |
| **합계** | **12** |

핵심 문제는 `ChallengerQueryService` 의 `getChallengerInfoFromChallenger` 가 stream 내부에서 `getChallengerPointUseCase.getListByChallengerId` 를 매번 호출하던 것이었습니다.

---

## v1 (PR 적용 후, T1·D13 반영) — N+1 제거

### 호출 그래프

```
GET /api/v1/member/me
       ↓
MemberInfoResponseAssembler.fromMemberId
       │
       ├── getMemberUseCase.getById                                    ── Q1
       │
       ├── getChallengerUseCase.getAllByMemberId
       │     └─ ChallengerQueryService.getAllByMemberId (D13)
       │          ├─ loadChallengerPort.getAllByMemberId               ── Q2
       │          └─ toChallengerInfoListBatch
       │              └─ getMapByChallengerIds (IN 쿼리 1회)           ── Q3
       │
       ├── getGisuUseCase.getByIds(gisuIds)         (IN 쿼리 1회)      ── Q4
       ├── getChapterUseCase.getChapterMapByGisuIdsAndSchoolIds        ── Q5
       └── getMemberProfileUseCase.getMemberProfileById                ── Q6
```

### 쿼리 수: **6** (N과 무관, O(1))

| 쿼리 | 횟수 |
|---|---:|
| `member` | 1 |
| `challenger.findByMemberId` | 1 |
| `challenger_point.findByChallengerIdIn` (IN) | 1 |
| `gisu.findByIdIn` (IN) | 1 |
| `chapter` map (gisuIds × schoolIds 조인) | 1 |
| `member_profile` | 1 |
| **합계** | **6** |

3기수 기준 **12 → 6, 50% 감소**. 10기수 가정 시 **33 → 6, 82% 감소**.

응답 스키마는 v1 그대로 유지(회귀 없음). 단순히 N+1만 사라진 상태입니다.

---

## v2 (BFF) — 단일 트랜잭션 + 추가 정보

### 호출 그래프

```
GET /api/v2/member/me
       ↓
MemberQueryV2Controller.getMySummary
       ↓
MemberSummaryV2QueryService.getSummaryByMemberId  (@Transactional(readOnly=true))
       │   ← 8개 외부 호출이 모두 같은 트랜잭션 안
       │
       ├── getMemberUseCase.getById                                    ── Q1
       ├── getMemberProfileUseCase.getMemberProfileById                ── Q2
       │
       ├── getChallengerUseCase.getAllByMemberId
       │     ├─ loadChallengerPort.getAllByMemberId                    ── Q3
       │     └─ getMapByChallengerIds (IN)                             ── Q4
       │
       ├── getGisuUseCase.findActiveGisu()  (Optional)                 ── Q5
       ├── getGisuUseCase.getByIds(gisuIds) (IN)                       ── Q6
       ├── getChapterUseCase.getChapterMapByGisuIdsAndSchoolIds        ── Q7
       ├── getChallengerRoleUseCase.getAllRoleTypesByChallengerIds(IN) ── Q8
       │
       └── ChallengerActivityPeriodService.calculateActivityPeriod
             (사전-fetch한 데이터 메모리 계산 → 0 쿼리)
       ↓
MemberSummaryV2Response
```

### 쿼리 수: **8** (N과 무관, O(1))

| 쿼리 | 횟수 | v1과의 차이 |
|---|---:|---|
| `member` | 1 | 동일 |
| `member_profile` | 1 | 동일 |
| `challenger.findByMemberId` | 1 | 동일 |
| `challenger_point.findByChallengerIdIn` | 1 | 동일 |
| **`gisu.findActiveByIsActive`** | 1 | **v2 신규** — 활성 기수 식별 |
| `gisu.findByIdIn` | 1 | 동일 |
| `chapter` map | 1 | 동일 |
| **`challenger_role.findByChallengerIdIn`** | 1 | **v2 신규** — 운영진 RoleType |
| 활동일 계산 | 0 | 메모리 (`GisuInfo.activityDays`) |
| **합계** | **8** | +2 (정보량 ↑) |

v1과 비교해 쿼리가 2개 더 발생하지만, 그 2개로 활동일 합산·운영진 여부·기수별 점수 강조 정보까지 모두 응답에 담깁니다.

---

## 트랜잭션 경계 — 가장 큰 구조적 차이

### v1 (이전·이후 동일)

`MemberInfoResponseAssembler` 는 `@Component` 입니다. Spring이 트랜잭션을 열어주지 않습니다. 내부에서 호출하는 각 UseCase Service(`MemberQueryService`, `ChallengerQueryService`, `GisuQueryService` …)가 각자 `@Transactional(readOnly=true)` 를 가지고 있으므로 **호출마다 별도 트랜잭션** 이 열렸다 닫힙니다.

```
[Q1: member]      open tx → commit
[Q2,Q3: challenger+points]   open tx → commit
[Q4: gisu]        open tx → commit
[Q5: chapter]     open tx → commit
[Q6: profile]     open tx → commit
```

→ **시점 일관성 보장 안 됨**. 회원이 8기 챌린저 등록 직후 호출하면 Q2에서는 8기가 보이는데 Q4에서 기수 정보를 받지 못해 schema-skew 가능성 (확률은 낮지만 race window 존재).

### v2

`MemberSummaryV2QueryService` 가 `@Service @Transactional(readOnly=true)`. Controller가 service를 호출하는 순간 트랜잭션이 열리고, **8개 외부 호출이 모두 같은 트랜잭션 안에서 단일 스냅샷에 대해 실행** 됩니다.

```
open tx
  [Q1..Q8 모두 같은 스냅샷]
commit
```

→ **시점 일관성 보장**. 활성 기수 전환·점수 적재 같은 짧은 write가 응답 중간에 들어와도 응답은 일관된 한 시점을 반영합니다.

---

## 응답 페이로드 비교

### v1 응답 (`MemberInfoResponse`)

```json
{
  "id": 10,
  "name": "...",
  "email": "...",
  "schoolId": 1,
  "schoolName": "...",
  "profileImageLink": null,
  "status": "ACTIVE",
  "roles": [...],
  "challengerRecords": [
    { "challengerId": 1, "gisuId": 6, "gisu": 7, "chapterId": 3, "chapterName": "...",
      "part": "WEB", "challengerStatus": "ACTIVE",
      "challengerPoints": [...], "points": [...],   // 중복 (deprecated)
      "totalPoints": 3.0,
      "memberStatus": "ACTIVE", "status": "ACTIVE", // 중복 (deprecated)
      ... },
    ...
  ],
  "profile": {...}
}
```

빠진 정보: **활동일, 활성 기수 강조, 운영진 여부**. 클라이언트가 이 정보를 원하면 추가 API 호출이 필요합니다.

### v2 응답 (`MemberSummaryV2Response`)

```json
{
  "id": 10, "name": "...", "email": "...", "schoolId": 1,
  "schoolName": "...", "profileImageLink": null, "status": "ACTIVE",
  "profile": {...},

  "totalActivityDays": 547,           // ← 추가
  "currentGisuMemberInfo": {          // ← 추가, 휴지기엔 null
    "gisuId": 8, "generation": 9,
    "challenger": { "challengerId": 3, "part": "SPRINGBOOT",
                    "challengerStatus": "ACTIVE",
                    "points": [...], "totalPoints": 4.0 },
    "isAdmin": true,
    "roleTypes": ["SCHOOL_PRESIDENT"]
  },

  "challengerHistory": [              // ← 정제됨 (deprecated 필드 제거)
    { "challengerId": 3, "gisuId": 8, "generation": 9,
      "chapterId": 3, "chapterName": "...",
      "part": "SPRINGBOOT", "challengerStatus": "ACTIVE",
      "points": [...], "totalPoints": 4.0, "roleTypes": [...] },
    ...최신 기수 우선 정렬
  ]
}
```

v1 대비 페이로드는 약 15~25% 증가 (활동일 `long` 1개, `currentGisuMemberInfo` 객체 1개)지만, deprecated 중복 필드 제거로 **상쇄 효과** 가 있어 실측 크기는 비슷한 수준이 될 가능성이 높습니다.

---

## 시나리오 walkthrough — 회원 A (3기수 + 활성 기수 회장) 호출

| 단계 | v1 (이전) | v1 (PR 후) | v2 |
|---|---|---|---|
| 클라이언트 호출 | `GET /me` | `GET /me` | `GET /me` |
| 백엔드 쿼리 수 | 12 | 6 | 8 |
| 트랜잭션 수 | ~6 | ~6 | 1 |
| 활동일 표시 | 별도 API 필요 | 별도 API 필요 | 응답에 포함 |
| 활성 기수 챌린저 강조 | 클라이언트 계산 | 클라이언트 계산 | 응답에 포함 |
| 운영진 여부 | 별도 API 호출 또는 roles 파싱 | 동일 | boolean 한 줄 |
| 총 클라이언트 RTT | 1 + α (활동일/운영진 별도) | 1 + α | **1** |

α 가 단순히 2회였다고 가정해도 v2 시 클라이언트 입장에서 **RTT 1/3로 축소** 됩니다.

---

## 한계와 후속 측정 계획

본 보고서의 수치는 **JPA 쿼리 카운트 기반 모델링** 이며, 실제 응답 시간은 측정하지 않았습니다. PR 머지 후 다음을 권장합니다.

1. **Hibernate statistics 로깅** (`spring.jpa.properties.hibernate.generate_statistics=true`) 로 N=3·5·10 시 실제 쿼리 수 검증
2. **JMeter 또는 k6** 로 동시 100요청 시 p50/p95 응답 시간 측정 — v1(PR 후) vs v2
3. **PG `pg_stat_statements`** 로 `getAllByMemberId` 호출 빈도와 평균 시간 추적 (D13 효과)
4. **Application logs**: `/api/v1/member/me` 와 `/api/v2/member/me` 의 처리 시간 분포 비교 (1주 단위)

특히 v2의 신규 쿼리 2건(`findActiveGisu`, `getAllRoleTypesByChallengerIds`)이 hot path 부하에 미치는 영향을 모니터링 대상으로 두면 좋습니다.

---

## 결론

- **v1 (PR 이전 → PR 적용 후)**: 같은 응답 모델에서 **3N+3 → 6** 쿼리로 떨어지며 챌린저 수에 비례하던 비용이 사라졌습니다.
- **v2 (BFF)**: v1보다 쿼리 2건이 추가되지만, 그 비용으로 **활동일·활성 기수 운영진·기수별 점수 강조** 까지 한 응답으로 제공하고 **단일 readOnly 트랜잭션** 으로 시점 일관성을 보장합니다.
- 클라이언트 입장에서는 v2가 **RTT 1회** 에 필요한 모든 정보를 받게 되어, v1+보조 API 호출 조합을 단일 호출로 대체할 수 있습니다.
