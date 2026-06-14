# Project · Survey 도메인 사용자 Flow 및 미구현 영역 분석 보고서

- **작성일**: 2026-05-12
- **분석 범위**: `src/main/java/com/umc/product/project`, `src/main/java/com/umc/product/survey`, Flyway 마이그레이션, 현재 open PR
- **분석 대상 PR**
  - `#843` [Refactor] 프로젝트 여러개 등록 가능하도록 DRAFT 룰 확장 (creator 기준)
  - `#840` [Feat] 프로젝트 매칭 선발 도메인 API 개발 (PM 토글 + 자동 선발 + 동적 스케줄러)
  - `#780` [Release] v2.0.0 (Survey 도메인이 Recruitment 를 대체했다는 history 확인용)
- **목적**: 두 도메인의 현재 구현 상태를 사용자 입장 flow 로 정리하고, flow 별로 아직 채워지지 않은 영역을 식별해 다음 작업 우선순위를 도출

---

## 1. 도메인 개요

### 1.1 Project 도메인
헥사고날 아키텍처 (`domain` / `application` / `adapter`) 를 엄격히 따릅니다. UMC PRODUCT 의 한 기수 안에서 **프로젝트 등록 → 모집 → 매칭 → 팀 구성 → 종료** 까지의 생명주기를 담당합니다.

**핵심 엔티티 (7개)**

| 엔티티 | 책임 | 위치 |
|---|---|---|
| `Project` | 프로젝트 본체 · 상태 머신 (DRAFT/PENDING_REVIEW/IN_PROGRESS/COMPLETED/ABORTED) | [Project.java](src/main/java/com/umc/product/project/domain/Project.java) |
| `ProjectApplication` | 챌린저 지원서 · 상태 머신 (DRAFT/SUBMITTED/APPROVED/REJECTED) | [ProjectApplication.java](src/main/java/com/umc/product/project/domain/ProjectApplication.java) |
| `ProjectApplicationForm` | 프로젝트별 지원 폼 (Survey `Form` 으로 위임) | [ProjectApplicationForm.java](src/main/java/com/umc/product/project/domain/ProjectApplicationForm.java) |
| `ProjectApplicationFormPolicy` | 폼 섹션 가시성 정책 (COMMON / PART) | [ProjectApplicationFormPolicy.java](src/main/java/com/umc/product/project/domain/ProjectApplicationFormPolicy.java) |
| `ProjectMember` | 프로젝트 팀원 (ACTIVE/COMPLETED/WITHDRAWN/DISMISSED) | [ProjectMember.java](src/main/java/com/umc/product/project/domain/ProjectMember.java) |
| `ProjectMatchingRound` | 매칭 차수 (지부별 · 차수 · 타입 단위, 일정 + 합불 마감) | [ProjectMatchingRound.java](src/main/java/com/umc/product/project/domain/ProjectMatchingRound.java) |
| `ProjectPartQuota` | 파트별 정원(TO) | [ProjectPartQuota.java](src/main/java/com/umc/product/project/domain/ProjectPartQuota.java) |

**상태 머신 요약**

```
Project:
  DRAFT --submit--> PENDING_REVIEW --publish--> IN_PROGRESS --complete--> COMPLETED
   │                       │                        │
   └───────────────────────┴────── abort(reason) ──┴──> ABORTED

ProjectApplication:
  DRAFT --submit--> SUBMITTED --approve/reject--> APPROVED/REJECTED
                              (auto-decide 도 동일 전이를 사용 — PR #840)
```

### 1.2 Survey 도메인
독립적인 범용 설문 엔진. **Project 의 지원 폼**, **Notice 의 투표** 두 곳이 소비자입니다.

| 엔티티 | 책임 |
|---|---|
| `Form` / `FormSection` / `Question` / `QuestionOption` | 폼 구조 (DRAFT/PUBLISHED) |
| `FormResponse` | 한 응답자의 응답 루트 (DRAFT/SUBMITTED) |
| `Answer` / `AnswerChoice` | 질문별 답변 (텍스트·파일·시간·객관식 선택) |

**중요**: Survey 도메인은 **자체 Controller 가 없습니다.** Project 도메인의 `ProjectApplicationFormController` 와 Notice 의 투표 컨트롤러가 진입점입니다.

### 1.3 Recruitment 도메인은 폐기됨
`V2026.04.13.00.08__delete_recruitment_domain.sql` 에서 Recruitment 관련 7개 테이블이 일괄 DROP 되었습니다. Survey 가 그 자리를 완전히 대체합니다. 단 다음 잔재는 남아있습니다.

- `QuestionType` enum 에 `PREFERRED_PART` (현재 미사용, deprecated)
- `Form` 의 `starts_at` / `ends_at_exclusive` 컬럼은 V2026.04.21 마이그레이션으로 삭제 완료

---

## 2. 사용자 역할 정의

본 보고서에서 사용하는 사용자(actor) 역할입니다.

| Actor | 정의 | 권한 진입 |
|---|---|---|
| **PM (Project Manager / PO)** | `Project.productOwnerMemberId` 에 매핑된 PLAN 파트 챌린저 | EDIT (자기 프로젝트), APPROVE (지원서 합불) |
| **Creator** | 프로젝트 DRAFT 를 작성한 주체 (`Project.createdByMemberId`). PM 본인일 수도, 운영진일 수도 있음 | DRAFT 단계 EDIT |
| **챌린저** | 기수에 속한 일반 챌린저 (DESIGN/BE/FE 등). PLAN 챌린저는 PM 후보 | WRITE (프로젝트 지원), 자기 지원서 READ/EDIT/DELETE |
| **지부장 / 학교장** | 운영진. `chapter`/`school` scope 의 MANAGE 권한 | MANAGE (publish, 매칭 차수 운영) |
| **중앙 총괄단 (SUPER_ADMIN)** | 글로벌 운영진 | DELETE 포함 모든 권한 |
| **시스템 (스케줄러)** | `MatchingRoundDeadlineScheduler` — 합불 마감 시각 + 10분 후 자동 발화 (PR #840) | `AutoDecideProjectMatchingRoundUseCase` 호출 |

---

## 3. 사용자 관점 Flow

각 flow 는 **Actor**, **트리거 API / 이벤트**, **상태 전이**, **선후행 의존성**, **현재 구현 상태** 순으로 정리합니다. API ID 는 코드의 `Swagger summary` 기준입니다.

### Flow A. PM 또는 운영진이 프로젝트 DRAFT 를 생성 · 작성 · 제출

**Actor**: PLAN 파트 챌린저 (자기 자신을 PO 로) 또는 운영진 (다른 PLAN 챌린저를 PO 로 임명)

**선결 조건**: 대상 기수가 존재하고, PO 후보가 해당 기수의 PLAN 파트 챌린저여야 함

```
1. [PROJECT-101] POST /api/v1/projects
   → Project.createDraft() · status = DRAFT
   → (creator, gisu) DRAFT 1개 제약 적용 (PR #843)

2. [PROJECT-103] GET /api/v1/projects/me/draft?gisuId={gisuId}
   → 화면 재진입 시 본인이 작성 중인 DRAFT 확인 (creator 기준)

3. [PROJECT-102] PATCH /api/v1/projects/{projectId}
   → Project.updateBasicInfo() · name/description/링크/로고/썸네일 보완

4. [PROJECT-106] PUT /api/v1/projects/{projectId}/application-form
   → Survey Form/Section/Question/Option 3계층 diff 동기화
   → 섹션별 ProjectApplicationFormPolicy(COMMON / PART) 설정

5. [PROJECT-105] PUT /api/v1/projects/{projectId}/part-quotas
   → 파트별 정원(TO) 입력 — PLAN/DESIGN/BE/FE

6. [PROJECT-107] POST /api/v1/projects/{projectId}/submit
   → Project.submit() · DRAFT → PENDING_REVIEW
   → (PR #843) 이 시점에 DRAFT 슬롯이 풀려 같은 creator 가 다음 DRAFT 시작 가능
```

**PR #843 으로 인한 룰 변경 (이 flow 의 핵심 차이)**

| 항목 | AS-IS (v2.0.0) | TO-BE (PR #843) |
|---|---|---|
| 동일 기수 PO 중복 | (PO, gisu) 1개 강제 (모든 상태) | **제거** — PO 가 같은 기수에 여러 프로젝트 가능 |
| Creator DRAFT 슬롯 | 없음 | **(creator, gisu) DRAFT 1개** · PostgreSQL partial unique index |
| 차단 에러 코드 | `PROJECT_DUPLICATE_IN_GISU` | `PROJECT_DRAFT_ALREADY_IN_PROGRESS` |
| 슬롯 해제 시점 | — | DRAFT → PENDING_REVIEW 전이 즉시 |

**시나리오**

- 일반 PM 흐름: PM-A 가 본인을 PO 로 DRAFT 1개 작성 → 제출 → 동일 기수 두 번째 프로젝트 DRAFT 작성 가능
- 운영진 일괄 등록: 운영진(requester) 이 PM-A 를 PO 로 DRAFT 만든 뒤 제출하면 그 즉시 PM-B 를 PO 로 새 DRAFT 시작 가능 (운영진 본인이 creator 로 슬롯 점유)

**미구현 / 잔여 작업**

- 없음 — PR #843 이 룰 변경의 코드 / 마이그레이션 / 테스트를 모두 포함

---

### Flow B. 운영진의 프로젝트 공개 (PENDING_REVIEW → IN_PROGRESS)

**Actor**: 중앙 총괄단 또는 해당 지부장

**선결 조건**: 대상 프로젝트가 `PENDING_REVIEW` 상태

```
[PROJECT-108] POST /api/v1/projects/{projectId}/publish
  → ProjectPermissionEvaluator.MANAGE 권한 검사
  → Project.publish() · PENDING_REVIEW → IN_PROGRESS
  → 이후부터 챌린저가 지원 가능
```

**미구현 / 잔여 작업**

- (P2) **publish 단계의 정원 / 폼 유효성 재검증 미흡**: `Project.submit()` 시점 검증과 publish 시점 검증이 별도 가드 없이 같은 도메인 메서드만 호출하는 구조. 정책 결정(예: 운영진이 publish 직전 폼을 마지막으로 확인하는 hook) 이 필요한지 확정 필요

---

### Flow C. 운영진의 매칭 차수 운영 (CRUD)

**Actor**: 중앙 총괄단 또는 해당 지부장 (scope 는 Service 내부 검증)

```
[MATCHING-001] GET  /api/v1/project/matching-rounds            (목록 · chapterId/time 필터)
[MATCHING-101] POST /api/v1/project/matching-rounds            (생성)
[MATCHING-102] PATCH /api/v1/project/matching-rounds/{id}      (일정/메타 수정)
[MATCHING-103] DELETE /api/v1/project/matching-rounds/{id}     (삭제, 연관 지원서 있으면 409)
```

**도메인 불변식**

- `startsAt < endsAt < decisionDeadline` 순서 강제
- 같은 (type, phase, chapter) 조합 UNIQUE (`uk_project_matching_round_type_phase_chapter`)
- `validateNoOverlap`: 같은 지부 내 기간 중첩 차단

**PR #840 으로 추가된 점**

- 매칭 차수 생명주기(create/update/delete) 마다 `TaskScheduler` 에 1회성 task 를 등록/취소
- `decisionDeadline + 10분` 에 자동 마감 finalize task 발화
- 트랜잭션 commit 후(`afterCommit`) 등록 → 롤백 시 stale task 방지
- JVM 부팅 시 `@PostConstruct` 로 미처리 round 모두 재등록, deadline+10 이 과거면 즉시 발화

**미구현 / 잔여 작업**

| 우선 | 항목 | 근거 |
|---|---|---|
| **P1** | **1차 매칭 시작 후 모든 차수 일정 PATCH 차단 정책 미구현** (= QA-14). 현재 `ProjectMatchingRound.reschedule()` / Service 모두 시작 이후 잠금 가드 없음. FE 가 disable 했더라도 BE 단에서 직접 PATCH 호출은 통과 | QA-14, [ProjectMatchingRound.java](src/main/java/com/umc/product/project/domain/ProjectMatchingRound.java) 의 `validateDates` / `reschedule` |

---

### Flow D. 챌린저 지원서 작성 → 제출

**Actor**: 대상 기수의 DESIGN/BE/FE 챌린저 (PLAN 챌린저는 본인 프로젝트 대상 지원 불가 규칙은 별도 확인 필요)

**선결 조건**

- 부모 프로젝트가 `IN_PROGRESS`
- 본인 파트 정원(`ProjectPartQuota`) 보유
- 해당 매칭 차수가 OPEN (`startsAt ≤ now ≤ endsAt`)
- 차수 타입이 본인 파트와 매칭 (DESIGN → `PLAN_DESIGN`, BE/FE → `PLAN_DEVELOPER`)
- 기수 내 이미 ACTIVE 인 ProjectMember 가 아닐 것

```
1. [APPLY-001] POST /api/v1/projects/{projectId}/applications
   → ProjectApplication.create() · status = DRAFT
   → 내부적으로 Survey FormResponse(status=DRAFT) 함께 생성

2. [APPLY-002] PUT  /api/v1/projects/{projectId}/applications/me
   → FormResponse 답변 임시저장 (여러 번 가능, 형식 검증만)

3. [APPLY-003] POST /api/v1/projects/{projectId}/applications/me/submit
   → ProjectApplication.submit() · DRAFT → SUBMITTED
   → submittedAt 기록 · 필수 답변 누락 검증
```

**가시성 규칙 (ProjectApplicationPermissionEvaluator)**

- DRAFT 지원서는 **본인만** 조회 가능
- SUBMITTED 부터 PO/Sub-PM/운영진에게 노출

**미구현 / 잔여 작업**

| 우선 | 항목 | 위치 |
|---|---|---|
| **P1** | `ProjectApplication.cancel()` (지원 철회) 도메인 메서드가 **빈 본문**. 사용자가 자신의 DRAFT/SUBMITTED 지원서를 철회할 수 있는 시나리오를 차단 중 | [ProjectApplication.java#L149](src/main/java/com/umc/product/project/domain/ProjectApplication.java) |
| **P1** | DELETE 엔드포인트(예: `DELETE /api/v1/projects/{projectId}/applications/me`) 미존재. 권한 모델은 `ProjectApplicationPermissionEvaluator.DELETE` 가 이미 정의되어 있으나 호출처가 없음 |  |
| **P2** | 동일 (form, round, applicant) 에 cancel 후 재지원 시 UK 충돌을 어떻게 풀지 미정 — soft delete (CANCELLED status 추가) 또는 UK 완화 결정 필요 |  |

---

### Flow E. PM 의 지원자 합 / 불 결정 (수동)

**Actor**: 해당 프로젝트의 PO (또는 위임된 Sub-PM)

```
1. [APPLY-101] GET /api/v1/projects/{projectId}/applications
       ?matchingRoundId=&part=&status=
   → 지원자 목록 조회 (DRAFT 제외)

2. [APPLY-102] GET /api/v1/projects/{projectId}/applications/{applicationId}
   → 지원서 단건 상세 (폼 구조 + 답변 + 첨부 파일)

3. [APPLY-103] PATCH /api/v1/projects/{projectId}/applications/{applicationId}/decision   ← PR #840
   → ApplicationDecisionStatus = APPROVED | REJECTED
   → ProjectApplication.approve()/reject()
   → 잔여 quota 검증 (validateRemainingQuota) → QUOTA_EXCEEDED 시 409
   → REJECTED 후 최소 선발 규정 미충족 시 → MINIMUM_SELECTION_REQUIRED 409, 기존 status 유지
   → 차수 종료 후 시도 시 → MATCHING_ROUND_LOCKED 400
```

**미구현 / 잔여 작업**

| 우선 | 항목 |
|---|---|
| **P1** | `ProjectApplicationQueryController` 의 [APPLY-101], [APPLY-102] 에 **`@CheckAccess` 가 아직 적용되지 않음**. 현재는 일반 챌린저가 호출해도 다른 프로젝트 지원자를 조회할 수 있는 보안 이슈. 코드 내 TODO 명시 ([ProjectApplicationQueryController.java](src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationQueryController.java) 87, 122 라인) |
| **P2** | `ProjectQueryService` N+1 문제 — 페이지당 (3 × size + 1) 쿼리 발생 (코드 TODO) — 지원자 목록이 커질수록 영향 증가 |

---

### Flow F. 매칭 차수 자동 마감 + 자동 선발 (시스템) ⭐ PR #840

**Actor**: 시스템 스케줄러 (`MatchingRoundDeadlineScheduler` → `MatchingRoundDeadlineHandler`)

**트리거**: `ProjectMatchingRound.decisionDeadline + 10분` 도달

```
1. 스케줄러 발화
   → AutoDecideProjectMatchingRoundUseCase 호출
   → ProjectMatchingRoundFinalizationCommandService 실행 (멱등 보장)

2. 정책 매트릭스 평가 (per Project × per Part)
   ─ Designer 정책 (DesignerMatchingPolicy)
     · 지원자 ≥ 2명 → 최소 1명 합격 강제
     · 지원자 1명 → PM 결정 자유 (자동 합격 시키지 않음)
   ─ Developer 정책 (DeveloperMatchingPolicy)
     · 지원자 ≥ TO × 100% → 최소 ceil(TO × 0.5) 명 합격 강제
     · TO × 50% ~ 100% → 최소 ceil(TO × 0.25) 명 합격 강제
     · TO × 50% 미만 → 자동 합격 강제 없음

3. 자동 선발 알고리즘
   ─ 이미 PM 이 APPROVED 한 인원 유지
   ─ 부족분 → SUBMITTED 풀에서 random pick
   ─ 그래도 부족하면 → REJECTED 풀에서 override 합격

4. ProjectApplication.applyAutoDecision() 으로 상태 전이
   → SUBMITTED/REJECTED → APPROVED 또는 SUBMITTED → REJECTED

5. APPROVED 결과 → ProjectMember 자동 생성 (part, decidedAt, application FK)
6. ProjectMatchingRound.executeAutoDecision() 으로 실행 기록 (autoDecisionExecutedAt)
```

**Manual override**: `POST /api/v1/project/matching-rounds/{matchingRoundId}/auto-decide` (운영진용 동일 진입점)

**도메인 가드 / 멱등성**

- `autoDecisionExecutedAt` 이 이미 채워진 round 는 재실행 차단
- update 트랜잭션 롤백 시 `afterCommit` 으로 미뤄진 schedule 콜백이 실행되지 않음 → in-memory task ↔ DB 상태 일관성 유지

**미구현 / 잔여 작업**

- (P3) 차수 종료 후 ProjectMember 가 자동 등록되는 부수효과의 후속 알림(FCM/Discord 등) 연결은 본 PR 범위 외. 추후 필요 시 별도 use case 로 신설

---

### Flow G. 본인 지원 내역 조회

**Actor**: 챌린저 본인

```
[APPLY-004] GET /api/v1/projects/me/applications?gisuId={gisuId}&status={status}
  → MyProjectApplicationResponse 카드 리스트
     1) application 카드 — SUBMITTED/APPROVED/REJECTED (DRAFT 제외)
     2) RANDOM_MATCHING 카드 — ACTIVE 멤버이면서 application 이 없는 경우
        (THIRD 라운드 이후 자동 랜덤 매칭 / 운영진 강제 배정 케이스)
  → 정렬: 매칭 라운드 시작일 ASC → 갱신일 DESC
```

**미구현 / 잔여 작업**

- (P3) RANDOM_MATCHING 카드가 합성되는 트리거(THIRD 라운드 후 자동 랜덤 매칭 / 운영진 강제 배정) 의 진입점은 아직 명시적으로 노출된 API 가 없음 — Flow H 의 `POST /members` 또는 별도 random matching use case 신설 필요 여부 검토

---

### Flow H. 프로젝트 팀원 관리 · 소유권 양도

**Actor**: PO (또는 운영진의 강제 배정)

```
[PROJECT-004]  POST   /api/v1/projects/{projectId}/members         (팀원 추가)
[PROJECT-005]  DELETE /api/v1/projects/{projectId}/members/{memberId} (팀원 제거)
[PROJECT-104]  POST   /api/v1/projects/{projectId}/transfer-ownership  (소유권 양도)
```

**팀원 제거 정책**

- DRAFT / PENDING_REVIEW: hard delete
- IN_PROGRESS: soft delete (`status = DISMISSED`, 사유 저장)
- COMPLETED / ABORTED: 불가

**소유권 양도 (PR #843 변경 반영)**

- 새 PO 가 PLAN 파트 챌린저 여부 검증
- (PR #843 이전) 새 PO 가 같은 기수에 다른 프로젝트의 PO 였으면 차단 → **(PR #843 이후) 차단 제거 — 동일 기수에서 PO 가 여러 프로젝트를 가질 수 있음**
- `productOwnerSchoolId`, `chapterId` 도 새 PO 기준으로 동기화

**미구현 / 잔여 작업**

- (P2) `ProjectMemberJpaRepository` 의 QueryDSL 마이그레이션이 일부 미적용 (코드 TODO) — 일관성 차원의 후속 정리
- (P3) 운영진이 강제 배정 시 PM 에게 통보하는 알림 채널이 명시되어 있지 않음

---

### Flow I. 프로젝트 생명주기 마감 (COMPLETED / ABORTED)

**Actor**: 운영진 (또는 시스템에 의한 기수 종료 일괄 처리)

```
정상 종료: Project.complete() · IN_PROGRESS → COMPLETED
와해:    Project.abort(reason, decidedByMemberId) · 어떤 상태든 → ABORTED
```

**미구현 / 잔여 작업**

| 우선 | 항목 |
|---|---|
| **P1** | **현재 코드에 `complete` / `abort` 를 외부에서 트리거할 수 있는 Controller endpoint 가 노출되어 있지 않음.** 도메인 메서드(`Project.complete()`, `Project.abort()`) 와 상태 enum 만 존재. 기수 종료 시 일괄 종료 흐름 또는 운영진 단건 abort 흐름의 API · UseCase 정의 필요 |
| **P2** | 종료 시 `ProjectMember.status` (ACTIVE → COMPLETED/WITHDRAWN) 일괄 전이 로직 미정. 종료 use case 가 부수효과로 멤버 상태도 갱신해야 일관성 유지 |

---

### Flow J. Survey 폼 자체 운영 (직접 호출 경로)

**Actor**: 현재로서는 **Project 도메인의 Service 만**

Survey 도메인은 **자체 Controller 가 없습니다.** 외부에서 직접 호출 가능한 진입점은 다음 둘만 존재합니다.

- `ProjectApplicationFormController` (`/api/v1/projects/{projectId}/application-form`) — PUT/GET
- Notice 도메인의 투표 컨트롤러 (`POST /api/v1/notices/{noticeId}/votes` 등)

**Survey UseCase 의 미구현 / 결정 보류 항목 (코드 TODO 기준)**

| 우선 | 항목 |
|---|---|
| **P2** | `ManageFormUseCase.updateForm()`: 발행된 폼도 수정 가능한지 정책 미확정 |
| **P2** | `ManageFormSectionUseCase`: 발행된 폼 / 응답이 들어온 폼의 섹션 구조 변경 차단 정책 미확정 |
| **P2** | `ManageQuestionUseCase`: 발행된 폼에서 질문 수정 불가(=`SURVEY_NOT_DRAFT`) 정책 적용 미확정. Project 도메인의 `shouldFork` 플래그로 Copy-on-Write 우회로만 존재 |
| **P2** | `ManageQuestionOptionUseCase`: 발행된 폼 / 응답이 들어온 폼의 선택지 변경 차단 정책 미확정 |
| **P3** | `QuestionCommandService`: 질문 type 변경 시 기존 응답(Answer) 무효화 정책 미정 (현재는 보존) |
| **P3** | `CreateDraftFormCommand`: `form.title` 이 `nullable=false` 라 최초 생성 시 빈 값 허용 불가 — UX 결정 필요 |
| **P3** | `AnswerChoice` 의 집계 JPQL 미작성 (코드 TODO) — 투표 선택지별 카운트 등 결과 집계 API 가 추가될 때 같이 필요 |
| **P3** | **Survey 도메인 전용 통합 테스트 없음** — Project 통합 테스트가 간접적으로 커버 |

---

## 4. 미구현 영역 종합 정리

### 4.1 차단성 (P1) — 운영 배포 전 반드시 해결

| # | 영역 | 항목 | 근거 |
|---|---|---|---|
| 1 | Flow D · 보안 | `ProjectApplicationQueryController` [APPLY-101], [APPLY-102] 에 `@CheckAccess` 미적용 → 일반 챌린저가 다른 프로젝트 지원자 조회 가능 | 코드 TODO ([ProjectApplicationQueryController.java#L87](src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationQueryController.java#L87), L122) |
| 2 | Flow D | 지원서 철회 (`ProjectApplication.cancel()`) 도메인 메서드가 빈 본문, DELETE 엔드포인트도 부재 | [ProjectApplication.java#L44](src/main/java/com/umc/product/project/domain/ProjectApplication.java#L44), L149 |
| 3 | Flow I | 프로젝트 `complete` / `abort` 를 외부에서 트리거할 수 있는 Controller / UseCase 부재 | 코드 전수 조사 결과 endpoint 없음 |

### 4.2 정책 미확정 (P2) — 결정만 되면 빠르게 구현 가능

| # | 영역 | 항목 |
|---|---|---|
| 1 | Flow J | Survey 폼 / 섹션 / 질문 / 선택지의 **발행 후 편집 정책** 4건 미확정 (Form, FormSection, Question, QuestionOption) |
| 2 | Flow D | 지원 철회 후 재지원 시 UK (`uk_project_application_form_member_matching_round`) 충돌 처리 방식 미정 |
| 3 | Flow B | publish 직전 폼 / 정원 재검증 hook 필요 여부 미정 |
| 4 | Flow I | 종료 시 `ProjectMember.status` 일괄 전이 정책 미정 |
| 5 | Flow C (QA-14) | **1차 매칭 시작 후 모든 차수 일정 PATCH 잠금 정책 미구현**. FE 는 비활성화 표시하나 BE 가드 부재 → 직접 PATCH 호출 시 통과 |
| 6 | Flow E (QA-11) | **Admin 의 매칭 종료 후 합/불 토글 우회 정책 미구현**. 현재 `MATCHING_ROUND_LOCKED` 가드는 권한 무관하게 일괄 차단 → 운영진 override 분기 필요 |
| 7 | Flow H (QA-16) | **운영진의 임의 팀원 배정 endpoint 권한 분기 미구현**. 현재 `POST /projects/{id}/members` 는 `EDIT` (PO 권한) 만 통과 → MANAGE 권한자에게도 허용하는 분기 필요. ACTIVE 멤버 후보 필터링 (다른 프로젝트 중복 차단) 도 함께 |

### 4.3 성능 / 일관성 / UX (P3) — 후속 작업

| # | 영역 | 항목 |
|---|---|---|
| 1 | Flow E · 성능 | `ProjectQueryService` N+1: 페이지당 (3 × size + 1) 쿼리 (코드 TODO) |
| 2 | Flow H · 일관성 | `ProjectMemberJpaRepository` QueryDSL 마이그레이션 미완 (코드 TODO) |
| 3 | Flow G | RANDOM_MATCHING 카드 합성 트리거 (랜덤 매칭 / 강제 배정) 의 명시적 API 미존재 |
| 4 | Flow F · 알림 | 자동 선발 후 합/불 통보 채널 연결 (FCM / Discord) |
| 5 | Flow J · 테스트 | Survey 도메인 단위 / 통합 테스트 부재 (현재 Project 통합 테스트로 간접 커버) |
| 6 | Flow J · UX | `Form.title` nullable 정책, `QuestionType.PREFERRED_PART` 레거시 enum 정리 |
| 7 | Flow J · 기능 | 폼 응답 결과 집계 API (선택지별 카운트 등) 부재 |

---

## 5. 권장 다음 작업

PR 의존성을 고려한 추천 순서입니다. 상세 PR 묶음은 §6.5 (구현 실행 계획) 참고.

1. **PR-1** (§6.5) — PR #843 / #840 회귀 안전망 테스트 (Flow A · F · E 일부)
2. **PR-2** (§6.5) — P1 차단성 3건 (권한 분기 / 지원 철회 / 프로젝트 종료)
3. **PR-3** (§6.5) — 노션 QA 미구현 3건 (QA-11 Admin override / QA-14 1차 시작 후 PATCH 잠금 / QA-16 운영진 강제 배정)
4. **PR-4** (§6.5) — 응답 메타 보강 + 잔여 회귀 안전망 (노션 QA 의 응답 스키마 검증 9건)
5. **PR-5** (§6.5) — P2 정책 미확정 항목 7건을 **단일 ADR 1편** 으로 결정 후 후속 PR (Survey 발행 후 편집 / 지원 철회 후 재지원 UK / publish hook / 종료 시 멤버 전이 / 1차 시작 후 PATCH 잠금 범위 / Admin override 사유 모델 / 운영진 임의 배정 후보 정책)
6. P3 성능 항목 (N+1, QueryDSL 마이그레이션) 은 별도 refactor PR 로 분리 — 기능 PR 과 섞으면 review 부담 증가

---

## 6. e2e 검증 시나리오 (Project Integration Test 매핑)

> **출처**: 본 섹션의 시나리오는 (1) 코드 베이스(도메인 메서드 / 권한
> evaluator / Flyway UK / PR #843·#840 본문) 와 (2) 노션 [📌 UPMS 관련 QA](https://www.notion.so/UPMS-QA-35e39ff67177804eb1d5c76e0f2fd9ca)
> 데이터베이스의 QA 시나리오 25건을 종합하여 정의했습니다. 각 노션 항목은
> `QA-NN` 형식으로 식별합니다 (매핑은 §6.2 표 참고).

### 6.1 노션 QA 시나리오 25건 분류 (백엔드 검증 가능성)

각 QA 항목을 **백엔드 e2e 테스트로 검증 가능한 것 / FE 단독 / 분석 범위 외**
3분류로 정리합니다.

| QA | 권한 | 기능 | 테스트 케이스 (요약) | 분류 | 매핑 Flow |
|---|---|---|---|---|---|
| QA-01 | Plan Chal, Admin | 프로젝트 등록 | 지원 문항 화면에서 [임시 저장] | 🟦 **BE** | Flow A (폼 PUT) |
| QA-02 | Plan Chal | 지원 현황 | 매칭 기간 종료 후 합/불/대기 토글 시도 (PM) | 🟦 **BE** | Flow E |
| QA-03 | Plan Chal, Admin | 지원 현황 | 지원 현황 탭 진입 — 통계·필터·페이지네이션 | 🟦 **BE 부분** | Flow E (목록 API) |
| QA-04 | Plan Chal, Admin | 프로젝트 등록 | 등록 중 페이지 이탈 모달 | ⬜ **FE only** | — |
| QA-05 | Admin | 공지 | 공지 작성 | ⚪ 범위 외 | Notice 도메인 |
| QA-06 | Plan Chal, Admin | 프로젝트 등록 | [등록하기] 버튼 → 등록 완료 모달 | 🟦 **BE** | Flow A (submit) |
| QA-07 | Plan Chal, Other Chal | 공지 | 타 지부 공지 열람 차단 | ⚪ 범위 외 | Notice access scope |
| QA-08 | Plan Chal, Admin | 프로젝트 등록 | 기본 정보 다음 → 2단계 건너뛰고 3단계 | ⬜ **FE only** | — |
| QA-09 | Other Chal | 프로젝트 목록 | 내 지부 프로젝트 클릭 → [지원하기] 활성 | 🟦 **BE** | Flow D (진입 조건) |
| QA-10 | Admin | 공지 | 공지 수정 | ⚪ 범위 외 | Notice 도메인 |
| QA-11 | Admin | 지원 현황 | 매칭 기간 종료 후 합/불 토글 (Admin 우회) | 🟦 **BE 미구현** | Flow E (관리자 override) |
| QA-12 | Other Chal | 프로젝트 목록 | 본인이 지원한 프로젝트 → [내 지원서 확인하기] | 🟦 **BE** | Flow D + G |
| QA-13 | Admin, Plan Chal, Other Chal | 프로젝트 관리 | 상태 변경 후 지원자 화면에서 일치 반영 | 🟦 **BE** | Flow E ↔ G |
| QA-14 | Admin | 매칭 차수 설정 | 1차 매칭 시작 이후 차수 기간 PATCH 차단 | 🟦 **BE 미구현** | Flow C |
| QA-15 | Admin | 매칭 차수 설정 | 일정 누락 후 저장 → validation 차단 | 🟦 **BE** | Flow C (POST/PATCH validation) |
| QA-16 | Admin | 매칭 현황 | 빈칸 클릭 → 운영진 임의 배정 | 🟦 **BE 미구현** | Flow H (운영진 add-member) |
| QA-17 | Admin | 매칭 차수 설정 | 입력 후 다른 탭 이탈 모달 | ⬜ **FE only** | — |
| QA-18 | Admin | 매칭 현황 | 배정 팀원 이름 클릭 → 파트 지원서 모달 + 상태 재변경 | 🟦 **BE** | Flow E (재토글) |
| QA-19 | Admin | 프로젝트 목록 | 프로젝트 클릭 → [모집 문항 보기] 노출 (Admin) | 🟦 **BE** | Flow J (Admin 폼 READ) |
| QA-20 | Plan Chal | 프로젝트 목록 | 내 지부 프로젝트 → [모집 문항 보기] (PM) | 🟦 **BE** | Flow J (PM 폼 READ) |
| QA-21 | Plan Chal, Admin | 프로젝트 등록 | 섹션 사용 토글 + 문항 편집 | 🟦 **BE** | Flow J (PUT diff) |
| QA-22 | Other Chal | 프로젝트 목록 | 타 지부 프로젝트 → 지원 버튼 숨김 | 🟦 **BE** | Flow D (access scope) |
| QA-23 | Other Chal | 프로젝트 목록 | 지원하지 않은 타 프로젝트 열람 → 지원 버튼 숨김 | 🟦 **BE** | Flow D (access scope) |
| QA-24 | Plan Chal, Admin | 프로젝트 등록 | 1단계 미완료 상태 → 다음 단계 클릭 차단 | ⬜ **FE only** | — |
| QA-25 | Plan Chal, Admin | 프로젝트 등록 | 1단계 필수 누락 후 [다음] | 🟦 **BE 부분** | Flow A (submit validation) |

**요약**

- 🟦 **BE 검증 가능 (16건)**: e2e 테스트로 직접 옮기는 대상
- 🟦 **BE 미구현 (3건)**: QA-11 (Admin 매칭 종료 후 우회), QA-14 (1차 시작 후 PATCH 차단), QA-16 (운영진 임의 배정) — 도메인 / endpoint 보강 후 RED → GREEN
- ⬜ **FE only (4건)**: 페이지 이탈 모달, 세그먼트 이동, 툴팁 등 — 백엔드 변경 없음
- ⚪ **분석 범위 외 (2건)**: Notice 도메인 — 본 보고서는 Project/Survey 한정이라 제외 (다만 QA-07 의 "지부별 공지 access scope" 패턴은 Project Flow D 의 access scope 검증과 동일 구조)

---

### 6.2 테스트 인프라 — 어디에 어떻게 작성하나

본 프로젝트는 `IntegrationTestSupport` 패턴이 이미 정착되어 있어 각 flow 를
바로 e2e 통합 테스트로 옮길 수 있습니다.

**베이스 ([IntegrationTestSupport.java](src/test/java/com/umc/product/support/IntegrationTestSupport.java))**

- `@SpringBootTest` + Testcontainers PostgreSQL/PostGIS 로 전체 Spring
  컨텍스트 부트업, MockMvc 자동 구성
- `@DatabaseIsolation` — 각 테스트 종료 후 모든 테이블 TRUNCATE (FK CASCADE)
- 외부 시스템(메일/JWT/FCM/GCS/StoragePort) 만 `@MockitoBean` 으로 대체.
  **도메인 / 애플리케이션 빈은 모킹 금지**

**Fixture (src/test/java/com/umc/product/support/fixture/)**

- 이미 구비: `MemberFixture`, `GisuFixture`, `ChapterFixture`, `SchoolFixture`,
  `ChallengerFixture`, `ChallengerRoleFixture`
- **신규 필요**: `ProjectFixture`, `ProjectApplicationFormFixture`,
  `ProjectMatchingRoundFixture`, `ProjectPartQuotaFixture`,
  `ProjectApplicationFixture`, `ProjectMemberFixture`
  - 패턴은 [GisuFixture](src/test/java/com/umc/product/support/fixture/GisuFixture.java) 동일
    (Save Port 를 주입받아 도메인 팩토리 메서드로 생성·저장)

**참고 예시**: [ProjectMatchingRoundControllerIntegrationTest.java](src/test/java/com/umc/product/project/adapter/in/web/ProjectMatchingRoundControllerIntegrationTest.java) —
이 클래스의 구조(인증 헬퍼 + Fixture 주입 + MockMvc DSL)를 그대로 차용

**컨벤션 (CLAUDE.md 발췌)**

- 테스트 클래스명: `{Domain}IntegrationTest` 또는 시나리오 단위 `{Flow}E2ETest`
- 메서드명은 한국어 (`@DisplayName` 또는 메서드 본명)
- Given / When / Then 주석으로 단계 구분
- `new` 로 도메인 엔티티 직접 생성 금지 — Fixture 또는 도메인 팩토리 메서드 사용
- 통합 테스트 클래스에 `@Transactional` 부여 금지 (운영 트랜잭션 경계 위배)

### 6.3 Flow ↔ Test 시나리오 매핑

각 시나리오는 `Given (사전 상태)` → `When (HTTP 호출 또는 시스템 이벤트)`
→ `Then (HTTP 응답 + DB 상태 + 도메인 불변식)` 으로 표현하며, 검증 포인트가
하나라도 깨지면 실패해야 합니다. **상태표(Status)** 는 본 보고서 기준 추천
상태입니다 (✅ 즉시 구현 가능 / 🟡 차단 항목 해결 필요 / 🔴 미구현 — RED
테스트로 먼저 작성 권장).

---

#### Flow A — `ProjectDraftE2ETest` (PR #843 룰 검증)

| # | 시나리오 | Given | When | Then 검증 | QA | Status |
|---|---|---|---|---|---|---|
| A1 | PLAN 챌린저가 본인을 PO 로 DRAFT 생성 후 PROJECT-103 으로 재진입 | 기수+지부+PLAN 챌린저(memberId=100) | POST /projects → GET /projects/me/draft?gisuId | 201 응답 projectId 와 GET 응답 projectId 동일, `project.status=DRAFT`, `created_by_member_id=100`, `product_owner_member_id=100` | — | ✅ |
| A2 | 동일 creator 가 같은 기수에 두 번째 DRAFT 시도 → 409 차단 | A1 직후 DRAFT 보유 | POST /projects 재호출 | HTTP 409 `PROJECT_DRAFT_ALREADY_IN_PROGRESS`, DB row 수 변화 없음 | — | ✅ |
| A3 | A1 의 DRAFT 를 submit 한 직후 같은 creator 가 새 DRAFT 시도 → 허용 | A1 의 DRAFT → POST submit | POST /projects 재호출 | 201, 새 projectId, partial unique index `uk_project_creator_gisu_draft` 충돌 없음 | — | ✅ |
| A4 | PO 가 같은 기수에 이미 다른 프로젝트 PO 인 경우 새 DRAFT 가 별도 creator 로 들어와도 허용 | PM-A 가 기수=1 PO 인 PENDING_REVIEW 프로젝트 보유 | 운영진(creator=200) 이 PM-A 를 PO 로 DRAFT 생성 | 201, `project.product_owner_member_id=PM-A`, `created_by_member_id=200` (= PR #843 의 핵심 — PO 중복 차단 제거 검증) | — | ✅ |
| A5 | DRAFT 단계 폼 PUT/정원 PUT → submit | A1 의 DRAFT | PUT /application-form → PUT /part-quotas → POST /submit | submit 응답 200, `project.status=PENDING_REVIEW`, `form` / `form_section` / `question` / `question_option` / `project_part_quota` 행 존재 | QA-01, QA-06 | ✅ |
| A6 | 운영진이 PM-A 임명 DRAFT → 제출 → 즉시 PM-B 임명 DRAFT 시작 | 운영진 creator=200 | POST /projects(PO=PM-A) → submit → POST /projects(PO=PM-B) | 두 번째 POST 도 201 (PR #843 시나리오 #4) | — | ✅ |
| A7 | submit 시 필수 항목 누락 → 400 | DRAFT, name 또는 part_quotas 미입력 | POST /submit | 400 `PROJECT_SUBMIT_VALIDATION_FAILED`, 상태 미변경 | QA-25 | ✅ |

**필요 Fixture 추가**: `ProjectFixture.draftBy(creatorMemberId, productOwnerMemberId, gisuId, chapterId)`

---

#### Flow B — `ProjectPublishE2ETest`

| # | 시나리오 | Given | When | Then 검증 | QA | Status |
|---|---|---|---|---|---|---|
| B1 | 지부장이 PENDING_REVIEW 프로젝트 publish | PENDING_REVIEW 프로젝트 + 지부장 권한 | POST /projects/{id}/publish | 200, `project.status=IN_PROGRESS` | — | ✅ |
| B2 | 일반 챌린저가 publish 시도 → 403 | PENDING_REVIEW + 일반 챌린저 | 동일 호출 | 403, 상태 미변경 | — | ✅ |
| B3 | DRAFT 상태에서 publish 시도 → 400 | DRAFT | 동일 호출 | 400 (도메인 invalid state), 상태 미변경 | — | ✅ |
| B4 | 이미 IN_PROGRESS 인 프로젝트 publish → 400 | IN_PROGRESS | 동일 호출 | 400 | — | ✅ |

---

#### Flow C — `ProjectMatchingRoundIntegrationTest` (확장)

기존 [ProjectMatchingRoundControllerIntegrationTest](src/test/java/com/umc/product/project/adapter/in/web/ProjectMatchingRoundControllerIntegrationTest.java) 에 통합.

| # | 시나리오 | Given | When | Then 검증 | QA | Status |
|---|---|---|---|---|---|---|
| C1 | 지부장이 본인 지부 매칭 차수 생성 | 지부장 권한 | POST /project/matching-rounds | 201, DB 행 존재 | — | ✅ (기존) |
| C2 | 다른 지부에 매칭 차수 생성 시도 → 403 | 지부장 | 다른 chapterId 로 POST | 403 | — | ✅ |
| C3 | 동일 (type, phase, chapter) 중복 생성 → 409 | C1 직후 | 동일 조합 POST | 409, UK `uk_project_matching_round_type_phase_chapter` 충돌 | — | ✅ |
| C4 | startsAt ≥ endsAt 인 요청 → 400 | — | POST 잘못된 날짜 | 400 (`validateDates`) | QA-15 | ✅ |
| C5 | 동일 지부 기간 중복 → 409 | C1 직후 | 겹치는 기간 POST | 409 (`validateNoOverlap`) | — | ✅ |
| C6 | 연관 지원서가 있는 차수 삭제 → 409 | C1 직후 지원서 1건 | DELETE | 409, 행 미삭제 | — | ✅ |
| C7 | 차수 update 시 스케줄러 task 재등록 | C1 + 스케줄러 spy | PATCH (decisionDeadline 변경) | 기존 task cancel + 새 task register, JVM 재시작 후에도 `@PostConstruct` 로 재등록 | — | ✅ (PR #840) |
| C8 | 일정 일부 누락 → 400 | startsAt 또는 decisionDeadline 미입력 | POST | 400, 명확한 필드별 에러 메시지 (각 일정 필드 단위로) | QA-15 | ✅ |
| C9 | **1차 매칭 startsAt 이 경과한 시점 이후, 1차 ~ 랜덤 매칭의 모든 차수 일정 PATCH 차단** | 1차 round `startsAt < now` 인 round 가 chapter 에 존재 | 어떤 round 든 PATCH (startsAt/endsAt/decisionDeadline 중 하나) | 400 `MATCHING_ROUND_PERIOD_LOCKED` 같은 도메인 에러, 행 미변경 | **QA-14** | 🔴 **미구현** |

---

#### Flow D — `ProjectApplicationLifecycleE2ETest`

| # | 시나리오 | Given | When | Then 검증 | QA | Status |
|---|---|---|---|---|---|---|
| D1 | DESIGN 챌린저가 PLAN_DESIGN 차수 OPEN 상태에서 APPLY-001/002/003 흐름 | IN_PROGRESS 프로젝트 + DESIGN 정원 보유 + 매칭 차수 OPEN | POST → PUT → POST submit | 각 단계 응답 200/201, `project_application.status` 가 `DRAFT → SUBMITTED`, `submitted_at` 기록, `form_response.status=SUBMITTED` | — | ✅ |
| D2 | BE 챌린저가 PLAN_DESIGN 차수에 지원 시도 → 400 | 동일 + BE 챌린저 | POST APPLY-001 | 400 (차수 타입 불일치) | — | ✅ |
| D3 | 정원이 0 인 파트에 지원 시도 → 400 | 정원 0 | POST APPLY-001 | 400 | — | ✅ |
| D4 | 차수 OPEN 시각 이전 / 이후 지원 → 400 | 차수 startsAt 미도래 또는 endsAt 경과 | POST APPLY-001 | 400 (`isOpenAt=false`) | — | ✅ |
| D5 | 동일 (form, round, applicant) 중복 SUBMITTED 차단 | D1 SUBMITTED 직후 재호출 | POST APPLY-001 | 409, UK `uk_project_application_form_member_matching_round` 충돌 | — | ✅ |
| D6 | 본인 SUBMITTED 지원서를 다른 챌린저가 GET 시도 → 403 | D1 SUBMITTED | 다른 챌린저 토큰으로 APPLY-102 호출 | 403 (현재는 미적용 → **RED 로 우선 작성**) | — | 🟡 P1 #1 |
| D7 | 본인이 SUBMITTED 지원서 cancel | D1 SUBMITTED | DELETE /projects/{}/applications/me | 200, `status=CANCELLED` (또는 행 삭제 후 재지원 허용) | — | 🔴 P1 #2 |
| D8 | DRAFT 상태 지원서가 다른 사용자에게 노출되지 않음 | D1 DRAFT 단계 | 다른 챌린저로 APPLY-101 호출 | 응답 목록에 미포함 | — | ✅ |
| D9 | 본인 지부 IN_PROGRESS 프로젝트 GET → 응답에 지원 가능 플래그 (`canApply=true`) | 일반 챌린저 + 본인 지부 IN_PROGRESS 프로젝트 (미지원) | GET /projects/{id} | 응답 메타에 `canApply=true` (또는 동치 필드) — FE 가 [지원하기] 버튼 활성화 판단 근거 | QA-09, QA-20 | ✅ (응답 스키마 확인 필요) |
| D10 | 본인이 이미 지원한 프로젝트 GET → `myApplicationId` 노출 + `canApply=false` | D1 SUBMITTED 후 | GET /projects/{id} | 응답 메타에 `myApplicationId` 채워짐, FE 는 [내 지원서 확인하기] 분기 | QA-12 | ✅ (응답 스키마 확인 필요) |
| D11 | 타 지부 프로젝트 GET → 지원 불가 (`canApply=false`) | 일반 챌린저 + 타 지부 IN_PROGRESS 프로젝트 | GET /projects/{id} | 200 응답, 메타에 `canApply=false`, POST APPLY-001 시도 시 400/403 | QA-22, QA-23 | ✅ (access scope) |
| D12 | DRAFT/PENDING_REVIEW 프로젝트 GET → 일반 챌린저 차단 | 비공개 상태 프로젝트 | 일반 챌린저 GET | 403 (`ProjectPermissionEvaluator.READ` 분기) | — | ✅ |

> D7 은 도메인 메서드 `ProjectApplication.cancel()` 와 DELETE endpoint 구현이
> 선행되어야 합니다. 테스트를 **RED 상태로 먼저 작성** 하면 P1 #2 의 진척을
> 자연스럽게 강제할 수 있습니다.

---

#### Flow E — `ProjectApplicationDecisionE2ETest` (PR #840 [APPLY-103])

| # | 시나리오 | Given | When | Then 검증 | QA | Status |
|---|---|---|---|---|---|---|
| E1 | PO 가 SUBMITTED 지원서를 APPROVED 로 토글 | SUBMITTED + PO 인증 | PATCH /applications/{id}/decision (APPROVED) | 200, `status=APPROVED`, `status_changed_member_id=PO`, `status_changed_at` 기록 | QA-02 (positive) | ✅ |
| E2 | 잔여 quota 초과 토글 → 409 | TO=2 + 이미 APPROVED 2건 | PATCH (APPROVED) | 409 `QUOTA_EXCEEDED` (PR #840 시나리오 C) | — | ✅ |
| E3 | **PM 이 차수 종료 후 토글 → 400** | endsAt 경과 또는 auto-decide 완료 + PO 인증 | PATCH | 400 `MATCHING_ROUND_LOCKED` (PR #840 시나리오 D) | **QA-02** | ✅ |
| E4 | APPROVED → REJECTED 재토글, 단 최소 선발 규정 유지 필요 | E1 직후 + 최소 선발 인원 충족 | PATCH (REJECTED) | 200, `status=REJECTED`, 사유 저장 | QA-18 | ✅ |
| E5 | 다른 프로젝트 PO 가 토글 시도 → 403 | 다른 PO 토큰 | PATCH | 403 | — | ✅ |
| E6 | 일반 챌린저가 APPLY-101 로 다른 프로젝트 지원자 목록 조회 → 403 | — | 다른 프로젝트 GET | 403 (**현재는 통과해 버리는 보안 이슈 — RED 로 작성**) | — | 🟡 P1 #1 |
| E7 | **Admin 이 차수 종료 후에도 합/불 토글 가능 (PM 잠금 우회)** | endsAt 경과 + 중앙 총괄단 / 지부장 인증 | PATCH (APPROVED) | 200, `status=APPROVED`, audit 사유 기록 (`Admin override`) | **QA-11** | 🔴 **미구현** |
| E8 | 지원자 목록 조회 — matchingRound/part/school/status 필터링 | SUBMITTED N건 (학교 / 파트 / 상태 mix) | GET /projects/{id}/applications?matchingRoundId=&part=&school=&status= | 각 조합별 정확한 필터링, 페이지네이션 동작 | QA-03 | ✅ (현재 status·matchingRoundId·part 만 지원 — school 필터 추가 필요시 확장) |
| E9 | 지원자 목록 응답에 통계 메타 포함 | SUBMITTED·APPROVED·REJECTED mix | GET 동일 | 응답에 `summary` (총 지원자 / 파트별 배정 현황 / 차수별 지원율) 메타 포함 — FE 도넛/막대 그래프 데이터 | QA-03 | 🟡 응답 스키마 확장 필요 (응답 assembler 확인) |

**필요 Fixture**: `ProjectApplicationFixture.submitted(formId, roundId, applicantMemberId)`

---

#### Flow F — `MatchingRoundFinalizationE2ETest` (PR #840 핵심)

본 flow 의 검증은 `decisionDeadline + 10분` 도달 시 자동 발화 / manual override
모두 검증해야 하므로 **스케줄러 발화를 시간으로 의존하지 말고 `AutoDecideProjectMatchingRoundUseCase`
를 직접 호출**하는 형태로 구성합니다. (CLAUDE.md 의 "비결정적 시간/식별자는 Clock 빈으로" 규약)

| # | 시나리오 (PR #840 매트릭스 기반) | Given | When | Then 검증 | QA | Status |
|---|---|---|---|---|---|---|
| F1 | PM 무결정 + deadline 도달 (TO=4 개발자, 지원자 8명) | 8 SUBMITTED · PM 결정 0 | UseCase.autoDecide(roundId) | APPROVED ≥ 2 (Developer 정책 ceil(TO×0.5))·나머지 REJECTED·`project_member` 행 = APPROVED 수 | — | ✅ |
| F2 | PM 일부 결정 (TO=4, 5명, PM 1 APPROVED) | 1 APPROVED + 4 SUBMITTED | UseCase 호출 | needed=1 → SUBMITTED 풀에서 random 1명 추가 APPROVED, 최종 APPROVED=2 | — | ✅ |
| F3 | 다중 차수 누적 (1차 1명 합격 후 2차) | 1차 종료 후 ACTIVE 멤버 1, 2차 SUBMITTED N | UseCase(2차) | quota 입력값 = 전체 TO − ACTIVE 멤버 수 (PR #840 시나리오 E) | — | ✅ |
| F4 | Designer 정책 — 지원자 ≥2 면 최소 1 강제 | 지원자 2 + PM 미결정 | UseCase 호출 | 정확히 1명 APPROVED | — | ✅ |
| F5 | Designer 정책 — 지원자 1 이면 자유 (자동 합격 강제 없음) | 지원자 1 + PM 미결정 | UseCase 호출 | APPROVED=0 가능 | — | ✅ |
| F6 | 멱등성 — 같은 round 두 번 호출 | F1 결과 | UseCase 재호출 | 두 번째 호출은 no-op (`autoDecisionExecutedAt` 비교) | — | ✅ |
| F7 | update 트랜잭션 롤백 시 scheduler 미발화 | round PATCH 후 강제 예외 | — | `afterCommit` 콜백 미실행 → in-memory task ↔ DB 일관 (PR #840 시나리오 F) | — | ✅ |
| F8 | JVM 재시작 시 미처리 round 재등록 | 차수 created 상태 + 컨텍스트 재기동 | `@PostConstruct` 동작 | 스케줄러에 task 등록됨, deadline+10 이 과거이면 즉시 발화 (PR #840 시나리오 G) | — | ✅ |

> F7/F8 은 PR #840 본문에서 가장 까다로운 시나리오로 명시되어 있어 회귀
> 방지 차원에서 반드시 e2e 로 박아두는 것이 좋습니다.

---

#### Flow G — `MyProjectApplicationQueryE2ETest`

| # | 시나리오 | Given | When | Then 검증 | QA | Status |
|---|---|---|---|---|---|---|
| G1 | 본인 지원 내역 카드 정렬 (라운드 startsAt ASC → updatedAt DESC) | 동일 챌린저의 SUBMITTED 2건 + APPROVED 1건 | GET /projects/me/applications?gisuId | DRAFT 제외, 정렬 순서 검증 | — | ✅ |
| G2 | ACTIVE 멤버이면서 application 없음 → RANDOM_MATCHING 카드 합성 | 운영진 강제 배정으로 ProjectMember.ACTIVE | 동일 호출 | 응답에 RANDOM_MATCHING 카드 1건 포함 | — | ✅ |
| G3 | status 필터링 | SUBMITTED·APPROVED·REJECTED 혼재 | GET ?status=APPROVED | APPROVED 만 반환 | — | ✅ |
| G4 | **PM 의 합/불 변경이 본인 지원 카드에 즉시 일치 반영** | 챌린저 SUBMITTED → PM 이 APPROVED 토글 (E1) | PM 토글 직후 챌린저 토큰으로 GET /projects/me/applications | 응답 카드의 `status=APPROVED`, `status_changed_at` 이 E1 직후 시각 (= 캐싱 / 동기화 지연 없음) | **QA-13** | ✅ |

---

#### Flow H — `ProjectMemberManagementE2ETest`

| # | 시나리오 | Given | When | Then 검증 | QA | Status |
|---|---|---|---|---|---|---|
| H1 | DRAFT 단계 멤버 추가/삭제 → hard delete | DRAFT + member 추가 직후 | DELETE /members/{memberId} | 200, `project_member` 행 삭제 | — | ✅ |
| H2 | IN_PROGRESS 단계 멤버 삭제 → soft delete | IN_PROGRESS + ACTIVE 멤버 | DELETE | 200, `status=DISMISSED`, 사유 저장 | — | ✅ |
| H3 | COMPLETED 단계 멤버 삭제 시도 → 400 | COMPLETED | DELETE | 400 | — | 🟡 (Flow I 의 종료 endpoint 필요) |
| H4 | 소유권 양도 — 새 PO 가 PLAN 챌린저 | IN_PROGRESS + 양도 대상 PLAN 챌린저 | POST /transfer-ownership | 200, `product_owner_member_id` 변경, `product_owner_school_id` / `chapter_id` 동기화 | — | ✅ |
| H5 | 양도 대상이 동일 기수에 이미 PO 인 경우 → 허용 (PR #843) | 양도 대상이 다른 프로젝트의 PO | POST /transfer-ownership | 200 (이전 룰은 차단) | — | ✅ |
| H6 | 양도 대상이 PLAN 챌린저 아님 → 400 | 양도 대상 BE | 동일 호출 | 400 `PROJECT_OWNER_NOT_PLAN_CHALLENGER` | — | ✅ |
| H7 | **운영진이 미배정 파트 빈자리에 챌린저 강제 배정** | 매칭 종료 후 파트 정원 < TO + ACTIVE 멤버 부족 + 중앙 총괄단/지부장 인증 | POST /projects/{id}/members (해당 파트의 ACTIVE 멤버 없는 챌린저) | 201, `project_member` 행 생성 (`application_id=null`, `decided_member_id=Admin`, `status=ACTIVE`), 다른 프로젝트에서 ACTIVE 인 챌린저는 후보에서 제외 | **QA-16** | 🔴 **미구현 (운영진 권한 분기)** |
| H8 | 운영진이 H7 으로 배정한 챌린저가 다시 다른 프로젝트에 배정되지 않음 | H7 직후 | 다른 프로젝트에 H7 챌린저로 다시 POST /members | 409 또는 400 (이미 ACTIVE 멤버 보유) | QA-16 (보조) | 🔴 미구현 |

---

#### Flow I — `ProjectLifecycleEndE2ETest`

| # | 시나리오 | Given | When | Then 검증 | QA | Status |
|---|---|---|---|---|---|---|
| I1 | 정상 종료: IN_PROGRESS → COMPLETED + ACTIVE 멤버 일괄 COMPLETED | IN_PROGRESS + ACTIVE 멤버 N | POST `(미구현 endpoint)` /complete | `project.status=COMPLETED`, `project_member.status=COMPLETED` | — | 🔴 P1 #3 |
| I2 | 와해: 어떤 상태든 ABORTED + 사유 기록 | IN_PROGRESS | POST `(미구현 endpoint)` /abort with reason | `project.status=ABORTED`, `status_changed_reason` 기록 | — | 🔴 P1 #3 |
| I3 | COMPLETED / ABORTED 에서 모든 수정 액션 차단 | I1 종료 직후 | PATCH /, POST /members, PUT /part-quotas 등 | 모두 400 (도메인 가드) | — | ✅ (도메인 가드는 이미 있음, endpoint 추가 시 e2e 확인) |

> I1/I2 는 endpoint 가 없어 **테스트를 먼저 RED 로 작성** → 구현 강제.

---

#### Flow J — Survey 폼 운영 (Project 통합 테스트로 간접 검증)

| # | 시나리오 | Given | When | Then 검증 | QA | Status |
|---|---|---|---|---|---|---|
| J1 | PUT /application-form 3계층 diff — 섹션 추가/수정/삭제 | 폼 1개 (sec=2, q=4, opt=8) | 변형된 본문 PUT | DB 가 본문과 정확히 일치 (section/question/option 수와 orderNo) | QA-01, QA-21 | ✅ |
| J2 | 챌린저 GET 시 본인 파트 + COMMON 만 노출 | 폼 + 정책 (DESIGN PART 섹션 1, COMMON 1) | DESIGN 챌린저로 GET | 2 섹션 반환, BE 챌린저로 호출 시 COMMON 1 + 본인 파트 0 | — | ✅ |
| J3 | IN_PROGRESS 프로젝트에서 폼 변경 시 질문 fork (Copy-on-Write) | 응답 1건이 들어온 question | PUT 으로 동일 question 수정 | `question.parent_question_id` 설정된 신규 row 생성, 원본 `is_active=false`, 기존 응답은 원본 question 참조 유지 | — | ✅ |
| J4 | 발행된 폼에서 질문 type 변경 시 응답 처리 | RADIO + 응답 보존 | type → SHORT_TEXT 변경 | (정책 미확정 — RED 테스트로 의도 명문화하고 P2 결정 유도) | — | 🟡 P2 |
| J5 | 특정 파트의 '섹션 사용' 토글 — 비활성 파트 섹션은 정책에 미포함 | 폼 + DESIGN 만 PART 정책 | BE 챌린저 GET | DESIGN 섹션 미노출, COMMON 만 노출 | QA-21 | ✅ |
| J6 | 질문 type — 주관식 / 단일 / 복수 / 파일 / 포트폴리오 모두 PUT 가능 | 5종 question_type 본문 | PUT | DB `question.type` 정확히 반영, 옵션 타입엔 `question_option` 행 존재 | QA-21 | ✅ |
| J7 | Admin 이 프로젝트 상세 GET 시 [모집 문항 보기] 가능 — 폼 응답 정상 노출 | IN_PROGRESS + Admin 인증 | GET /projects/{id}/application-form | 200, 전체 섹션 노출 (admin 마스킹 없음) | **QA-19**, QA-20 | ✅ |

---

### 6.4 우선 작성 추천 순서

다음은 **테스트 작성 → 구현 강제** 흐름을 고려한 추천 순서입니다. RED 로 시작하는
항목은 테스트를 먼저 머지하면 미구현 부담이 가시화됩니다.

| 순서 | 테스트 클래스 / 시나리오 | 의도 | 노션 QA 커버 |
|---|---|---|---|
| 1 | `ProjectDraftE2ETest` (Flow A 전체) | PR #843 회귀 방지 — 가장 가치 높음 | QA-01, QA-06, QA-25 |
| 2 | `MatchingRoundFinalizationE2ETest` (Flow F 전체) | PR #840 의 멱등 / 롤백 / 재등록 회귀 방지 | — |
| 3 | `ProjectApplicationDecisionE2ETest` E1~E5, E8~E9 | PM 합/불 토글 — 정상 동선 + 차수 잠금 (QA-02) | QA-02, QA-03, QA-18 |
| 4 | Flow D-D6, E-E6 (보안 RED) | P1 #1 — `ProjectApplicationQueryController` 권한 분기 강제 | — |
| 5 | Flow D-D7 (cancel RED) | P1 #2 — 지원 철회 endpoint + cancel() 구현 강제 | — |
| 6 | Flow I (`ProjectLifecycleEndE2ETest` RED) | P1 #3 — complete / abort endpoint 강제 | — |
| 7 | Flow C-C9 (1차 시작 후 PATCH 잠금 RED) | **QA-14** P2 #5 강제 | **QA-14** |
| 8 | Flow E-E7 (Admin 토글 우회 RED) | **QA-11** P2 #6 강제 | **QA-11** |
| 9 | Flow H-H7, H8 (운영진 강제 배정 RED) | **QA-16** P2 #7 강제 | **QA-16** |
| 10 | Flow D-D9~D12 (응답 메타 검증) | 프로젝트 상세 응답의 `canApply` / `myApplicationId` 노출 확인 | QA-09, QA-12, QA-22, QA-23 |
| 11 | Flow G-G4 (PM 토글 ↔ 챌린저 카드 동기화) | 실시간 일치 반영 | **QA-13** |
| 12 | Flow J-J5~J7 (폼 PUT / GET 변형 케이스) | 발행 전 폼 편집 회귀 안전망 | QA-19, QA-20, QA-21 |
| 13 | 잔여 (B / C 기본 / H1~H6 / J1~J4) | 일반 회귀 안전망 | QA-15 |

### 6.5 구현 실행 계획 — PR 단위 분리 제안

각 PR 은 **테스트 클래스 + 필요한 도메인/Adapter 변경 + Fixture 추가** 를 한 묶음으로
머지하며, 노션 QA ID 를 PR 본문에 명시하여 추적성을 확보합니다.

#### PR-1: 회귀 안전망 (PR #843 / #840 머지 직후, 동일 sprint)

- **목표**: 두 PR 의 핵심 동작 회귀 방지
- **신설 파일**
  - `src/test/java/com/umc/product/project/e2e/ProjectDraftE2ETest.java` — Flow A 전체 (A1~A7)
  - `src/test/java/com/umc/product/project/e2e/MatchingRoundFinalizationE2ETest.java` — Flow F 전체 (F1~F8)
  - `src/test/java/com/umc/product/project/e2e/ProjectApplicationDecisionE2ETest.java` — E1~E5, E8 (Admin 우회 E7 제외)
  - `src/test/java/com/umc/product/support/fixture/ProjectFixture.java`
  - `src/test/java/com/umc/product/support/fixture/ProjectMatchingRoundFixture.java`
  - `src/test/java/com/umc/product/support/fixture/ProjectApplicationFormFixture.java`
  - `src/test/java/com/umc/product/support/fixture/ProjectApplicationFixture.java`
  - `src/test/java/com/umc/product/support/fixture/ProjectPartQuotaFixture.java`
- **변경 없음 (테스트만)**
- **노션 QA 커버**: QA-01, QA-02, QA-03, QA-06, QA-18, QA-25

#### PR-2: P1 보안 / 미구현 fix-pack (RED → GREEN 순서로 묶음 머지)

- **목표**: P1 #1~#3 (Flow §4.1) 일괄 해소
- **신설 / 변경**
  1. **권한 분기 적용** — [ProjectApplicationQueryController](src/main/java/com/umc/product/project/adapter/in/web/ProjectApplicationQueryController.java) APPLY-101 / APPLY-102 에 `@CheckAccess` 적용 (코드 TODO 제거)
  2. **지원 철회 구현**
     - [ProjectApplication.cancel()](src/main/java/com/umc/product/project/domain/ProjectApplication.java) 본문 작성
     - `DELETE /api/v1/projects/{projectId}/applications/me` endpoint + `CancelProjectApplicationUseCase` / Service 신설
     - 재지원 UK 충돌 회피 — soft delete 컬럼 추가 또는 `CANCELLED` 상태 + UK 변경 마이그레이션 (정책 결정 후)
  3. **프로젝트 종료 endpoint**
     - `POST /api/v1/projects/{projectId}/complete`, `POST /api/v1/projects/{projectId}/abort`
     - `CompleteProjectUseCase`, `AbortProjectUseCase` + ACTIVE 멤버 일괄 전이 부수효과
- **신설 테스트**
  - Flow D-D6 (보안 RED→GREEN), D-D7 (cancel)
  - Flow I 전체 (I1, I2, I3)
- **노션 QA 커버**: 없음 (보안 / lifecycle 은 노션 시나리오 외)

#### PR-3: 노션 QA 미구현 3건 (QA-11 / QA-14 / QA-16)

- **목표**: §4.2 의 P2 #5~#7 일괄 해소
- **신설 / 변경**
  1. **QA-14 — 1차 시작 후 차수 일정 잠금**
     - `ProjectMatchingRound.validateReschedule(Instant now)` 추가 — chapter 의 1차 round `startsAt < now` 이면 전체 round `reschedule` 차단
     - `ProjectErrorCode.MATCHING_ROUND_PERIOD_LOCKED` 추가
     - `ProjectMatchingRoundCommandService.update` 진입에서 동일 chapter 의 다른 round 도 함께 호출하여 가드
  2. **QA-11 — Admin override**
     - `ProjectApplication.applyDecisionWithOverride(MemberRole role, ...)` 또는 별도 분기로 `MATCHING_ROUND_LOCKED` 우회
     - `ProjectApplicationCommandService.decide` 의 `validateIsMutableAt` 호출 시 admin 권한이면 skip
     - audit 사유에 `Admin override` 명시 필수
  3. **QA-16 — 운영진 임의 배정**
     - `POST /projects/{id}/members` 권한 분기 추가: `EDIT` (PO) 또는 `MANAGE` (총괄단 / 지부장)
     - 후보 필터링 Service — `LoadProjectMemberPort.listActiveMembersByGisu` 로 다른 프로젝트 ACTIVE 멤버 제외
     - `decided_member_id` 에 운영진 ID 기록
- **신설 테스트**
  - Flow C-C9, E-E7, H-H7/H8 (모두 RED → GREEN)
- **노션 QA 커버**: **QA-11, QA-14, QA-16**

#### PR-4: 회귀 안전망 확장 (선택)

- **목표**: 응답 스키마 보강 + 잔여 회귀 안전망
- **변경**
  - Flow D-D9~D12 의 응답 메타(`canApply`, `myApplicationId`) 가 현재 응답 DTO 에 없다면 Assembler 보강
  - Flow E-E9 의 통계 메타 (`summary` 도넛 / 막대 데이터) 응답 추가 (FE 가 별도 endpoint 로 합치는 것 보다 한 응답으로 묶는 것이 round-trip 적음)
- **신설 테스트**
  - Flow B / C 기본 (B1~B4, C1~C8), G4, H1~H6, J1~J7
- **노션 QA 커버**: QA-09, QA-12, QA-13, QA-15, QA-19, QA-20, QA-21, QA-22, QA-23

#### PR-5: 정책 미확정 자율 결정 (별도 ADR 동반)

- §4.2 의 P2 #1~#4 (발행 후 폼 편집 정책 / cancel 후 재지원 UK / publish hook / 종료 시 멤버 전이) 를 단일 ADR 로 묶고, 결정된 항목별 후속 PR

---

### 6.6 작성 시 주의사항

- 통합 테스트 클래스에 `@Transactional` 부여 금지 — `IntegrationTestSupport`
  주석 참조
- 도메인 빈을 `@MockitoBean` 으로 덮어쓰지 말 것 — 외부 시스템(메일/JWT/FCM/
  Storage)만 허용
- 시간 의존(Flow F, C7) 은 `Clock` 빈을 운영 코드에 두고 테스트에서 고정값
  주입. `Thread.sleep` 으로 스케줄러 발화 기다리지 말 것
- 자동 증가 ID 직접 비교 금지 — `isNotNull()` 또는 의미 단위(상태/관계/사유)로 검증
- 도메인 엔티티 `new` 금지 — Fixture 또는 도메인 팩토리 메서드 (`Project.createDraft()` 등) 사용

---

## 7. 참고 문서

- [v2.0.0 Release PR #780](https://github.com/) — Project / Survey 도메인 초기 인프라 history
- 본 보고서 작성에 사용된 코드 위치
  - Project 도메인: [src/main/java/com/umc/product/project/](src/main/java/com/umc/product/project/)
  - Survey 도메인: [src/main/java/com/umc/product/survey/](src/main/java/com/umc/product/survey/)
  - 마이그레이션: [src/main/resources/db/migration/](src/main/resources/db/migration/)
  - 통합 테스트 베이스: [IntegrationTestSupport.java](src/test/java/com/umc/product/support/IntegrationTestSupport.java)
  - 통합 테스트 참고 예시: [ProjectMatchingRoundControllerIntegrationTest.java](src/test/java/com/umc/product/project/adapter/in/web/ProjectMatchingRoundControllerIntegrationTest.java)
- 외부 자료
  - 노션 [📌 UPMS 관련 QA](https://www.notion.so/UPMS-QA-35e39ff67177804eb1d5c76e0f2fd9ca) — 25건 QA 시나리오 반영 완료 (§6.1)
