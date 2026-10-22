# 매칭 자동 선발과 4차 랜덤 매칭·결과 발표

## 목적

1~3차는 PM 결정 마감 이후 최소 선발 인원을 보충하고, 남은 TO는 별도의 `FOURTH` 차수에서 랜덤 매칭한다. `PLAN_DESIGN`과 `PLAN_DEVELOPER` 모두 같은 일정 구조를 사용한다. 발표 시각이 있는 차수는 멤버를 `PENDING_PUBLICATION`으로 생성한 뒤 발표 작업에서 `ACTIVE`로 변경한다.

랜덤 매칭으로 생성된 멤버는 지원서가 없으므로 `ProjectMember.application = null`로 저장되고, `assignedMatchingRound`에 해당 차수가 기록된다. 사용자 지원 내역 화면에서는 이 케이스를 `RANDOM_MATCHING` 카드로 합성한다.

## 실행 진입점

자동 선발 진입점은 `AutoDecideProjectMatchingRoundUseCase.autoDecide(matchingRoundId, executedByMemberId)` 하나다.

- 1~3차 스케줄러 호출: `decisionDeadline + app.project.matching-round.deadline-buffer-minutes` 시점에 실행한다. 기본 buffer는 1분이다.
- 4차 스케줄러 호출: `startsAt`에 랜덤 매칭을 실행한다.
- 결과 발표: `resultPublishAt`에 `PublishProjectMatchingResultUseCase.publish`를 호출한다.
- 운영진 수동 호출: `POST /api/v1/project/matching-rounds/{matchingRoundId}/auto-decide`.
- `executedByMemberId == null`이면 스케줄러 호출로 보고 권한 검증을 생략한다.
- `executedByMemberId != null`이면 슈퍼어드민, 중앙운영사무국 총괄단 이상 또는 해당 지부 지부장만 실행할 수 있다.
- `autoDecisionExecutedAt`이 이미 있으면 멱등 no-op으로 종료한다.
- 수동 실행도 1~3차는 결정 마감 이후, 4차는 시작 시각부터 가능하다. 그 전에는 `PROJECT_MATCHING_ROUND_NOT_FINALIZABLE` 예외가 발생한다.

관련 구현:

- `ProjectMatchingRoundFinalizationCommandService`
- `MatchingRoundDeadlineScheduler`
- `MatchingRoundDeadlineHandler`
- `MatchingResultPublicationHandler`
- `ProjectMatchingResultPublicationCommandService`
- `ProjectMatchingRoundController`

## 전체 처리 순서

1. 1~3차는 지원서와 최소 선발 정책으로 선발할 인원을 정한다. 4차는 지원서 없이 잔여 TO에 랜덤 매칭한다.
2. 팀원을 `PENDING_PUBLICATION`으로 저장하고 차수의 자동 선발 완료 시각을 기록한다. 지원서는 PM의 마지막 결정값을 유지한다.
3. 발표 작업에서 멤버를 `PENDING_PUBLICATION`에서 `ACTIVE`로 변경하고 지원서 합불을 반영한 뒤 공개 완료 시각을 기록한다.

1~3차에서 생성한 멤버는 `application`이 연결되고, 4차 랜덤 매칭은 `application = null`이다. 두 경우 모두 `assignedMatchingRound`에 해당 차수를 기록한다. 발표 시각이 없는 기존 차수는 자동 선발 시 `ACTIVE` 멤버를 생성하고 지원서 합불을 즉시 반영한다.

## 1~3차: 지원서 기준 자동 선발

지원서 자동 선발은 1~3차에서 공통으로 실행된다. 단, 여기서는 `PLAN_DEVELOPER` 기준만 설명한다.

### 입력 지원서

대상 지원서는 해당 `matchingRoundId`의 지원서 중 아래 상태만 포함한다.

- `SUBMITTED`
- `APPROVED`
- `REJECTED`

`DRAFT`와 `CANCELLED`는 정책 입력에서 제외하고 상태도 변경하지 않는다.

### 그룹 기준

지원자는 챌린저 도메인에서 `memberId + gisuId`로 파트를 조회한 뒤, 아래 키로 그룹화한다.

- `projectId`
- `ChallengerPart`

즉, 같은 프로젝트라도 `WEB`, `ANDROID`, `IOS`, `NODEJS`, `SPRINGBOOT` 파트는 서로 독립적으로 정책을 적용한다.

### 남은 TO 산정

정책에 넘기는 `quota`는 전체 TO가 아니라 남은 TO다.

```text
남은 TO = ProjectPartQuota.quota - 해당 프로젝트/파트의 ACTIVE 및 PENDING_PUBLICATION ProjectMember 수
```

음수가 되면 0으로 처리한다.

### PLAN_DEVELOPER 최소 선발 규칙

`DeveloperMatchingPolicy.minimumRequired(applicantsCount, quota)`는 다음 규칙을 사용한다.

| 조건 | 최소 선발 인원 |
| --- | --- |
| 지원자 수 >= 남은 TO | `ceil(남은 TO * 0.5)` |
| 지원자 수 > 남은 TO * 0.5 | `ceil(남은 TO * 0.25)` |
| 지원자 수 <= 남은 TO * 0.5 | `0` |

예시:

- 남은 TO 6, 지원자 6명: 최소 3명
- 남은 TO 6, 지원자 4명: 최소 2명
- 남은 TO 6, 지원자 3명: 최소 0명
- 남은 TO 0: 최소 0명

### 자동 합불 결정 순서

정책은 다음 순서로 최종 합격자를 정한다.

1. 기존 `APPROVED` 지원자는 그대로 합격 유지.
2. 기존 합격자 수가 최소 선발 인원보다 적으면 `SUBMITTED` 풀에서 랜덤 보충.
3. 그래도 부족하면 `REJECTED` 풀에서 랜덤 보충한다. 이 경우 PM의 기존 불합격 결정을 override한다.
4. 합격자로 선정되지 않은 `SUBMITTED`와 `REJECTED`는 모두 최종 `REJECTED`.

발표 시각이 있는 차수는 위 결과로 `PENDING_PUBLICATION` 멤버만 생성하고, 지원서 상태는 발표 작업에서 변경한다. 실제 상태가 변경되는 지원서에만 다음 메타데이터를 기록한다. 기존 상태가 최종 합불과 같으면 PM의 변경 정보를 유지한다.

- `statusChangedMemberId = executedByMemberId`
- `statusChangeReason = "auto-decide"`
- `statusChangedAt = now`

## 4차: 잔여 TO 랜덤 매칭

`phase == FOURTH`인 차수의 시작 시각에 실행한다. 3차 자동 선발에서는 랜덤 매칭을 실행하지 않는다.

1~3차는 `startsAt < endsAt < decisionDeadline < resultPublishAt`, 4차는 `startsAt < endsAt = decisionDeadline < resultPublishAt`으로 입력한다. 모든 차수의 `resultPublishAt`은 `decisionDeadline`으로부터 최소 5분 이후여야 한다. 같은 지부·유형에서는 이전 차수의 발표 시각이 다음 차수의 시작 시각보다 늦을 수 없다.

1~3차 자동 선발이 PM 마감 후 기본 1분 뒤 실행되므로, 발표 전 선발 처리 시간을 확보하기 위해 최소 5분 간격을 둔다.

4차의 `endsAt`과 `decisionDeadline`은 일정 검증에 사용하며, 랜덤 매칭은 `startsAt`, 결과 발표는 `resultPublishAt`을 기준으로 한다.

### 대상 프로젝트

대상 프로젝트는 현재 구현 기준으로 아래 조건을 만족하는 모든 프로젝트다.

- `chapterId == round.chapterId`
- `status == IN_PROGRESS`

주의: 대상 프로젝트 조회에는 `gisuId` 필터가 없다. 후보 챌린저 조회에 사용할 `gisuId`는 조회된 프로젝트 목록의 첫 번째 프로젝트에서 가져온다.

### 대상 파트

`PLAN_DESIGN`은 `DESIGN`, `PLAN_DEVELOPER`는 아래 개발 파트를 대상으로 한다.

- `WEB`
- `ANDROID`
- `IOS`
- `NODEJS`
- `SPRINGBOOT`

`PLAN`과 `ADMIN`은 랜덤 매칭 대상에서 제외한다. `WEB_PRODUCT_ENGINEER`와 `MOBILE_PRODUCT_ENGINEER`의 랜덤 매칭 지원은 이번 구현에 포함하지 않는다.

### 잔여 TO 산정

각 프로젝트/파트별 잔여 TO는 다음처럼 계산한다.

```text
잔여 TO = ProjectPartQuota.quota
       - 기존 ACTIVE 및 PENDING_PUBLICATION ProjectMember 수
```

0 이하인 프로젝트/파트는 배정 후보에서 제외한다. 모든 프로젝트/파트의 잔여 TO가 0이면 랜덤 배정 단계는 종료한다.

### 후보자 풀

후보자는 챌린저 검색 UseCase로 조회한다.

검색 조건:

- `chapterId = round.chapterId`
- `gisuId = 대상 프로젝트 목록 첫 번째 프로젝트의 gisuId`
- `statuses = [ACTIVE]`
- 페이지 크기 500으로 cursor pagination 조회

학교 기준 지부 검색 결과에서 다른 지부로 이동한 챌린저를 제외하고, 현재 지부로 이동해 온 ACTIVE 챌린저를 추가한다. 조회된 후보 목록은 먼저 전체 셔플된다. 이후 순회하면서 아래 후보는 제외한다.

- 같은 기수에서 이미 ACTIVE 또는 PENDING_PUBLICATION `ProjectMember`인 멤버
- 이번 랜덤 매칭에서 이미 선발한 멤버
- 해당 매칭 유형의 대상 파트가 아닌 멤버
- 후보자의 파트와 같은 잔여 TO가 더 이상 없는 경우

### 배정 방식

후보자를 셔플된 순서대로 순회한다.

1. 후보자의 파트와 같은 잔여 TO가 있는 `(projectId, part)` 목록을 만든다.
2. 그 목록 중 하나를 랜덤으로 선택한다.
3. `ProjectMember.createForMatching`으로 `assignedMatchingRound`가 연결된 `PENDING_PUBLICATION` 멤버를 생성한다.
4. 해당 `(projectId, part)`의 잔여 TO를 1 감소시킨다.
5. 모든 잔여 TO가 0이 되거나 후보자를 모두 소진하면 종료한다.

프로젝트 선택은 잔여 TO 수로 가중치를 주지 않는다. 가능한 `(projectId, part)` key 중 하나를 균등 랜덤으로 선택한다.

## 결과 발표

발표 작업은 자동 선발이 완료되고 발표 시각에 도달한 미공개 차수에 대해 실행한다. 프로젝트가 `IN_PROGRESS`인 `PENDING_PUBLICATION` 멤버를 `ACTIVE`로 변경하고, 해당 멤버에 연결된 지원서는 `APPROVED`, 나머지 판정 대상 지원서는 `REJECTED`로 변경한다. 프로젝트가 `IN_PROGRESS`가 아닌 경우 멤버·지원서는 변경하지 않으며, `DRAFT`, `CANCELLED` 지원서도 변경하지 않는다.

팀원 상태, 지원서 합불, 차수의 `resultPublishedAt`은 같은 트랜잭션에서 저장한다. 지원자 합불 공개는 시각 경과만으로 이루어지지 않고 발표 작업 완료를 기준으로 한다. PM은 발표 전까지 기존 결정값을 조회한다. 공개 팀원 목록·인원수는 `ACTIVE`를 기준으로 한다.

자동 선발과 발표는 각각 1회성 작업으로 예약한다. 실패 시 주기적으로 재시도하지 않으며, 서버 시작 시 DB에서 미처리 차수를 조회해 예약한다. 발표 시점에 자동 선발이 완료되지 않았다면 공개를 건너뛴다. 자동 선발 완료나 수동 실행 이후에 발표 작업을 다시 예약하지 않는다. 공개하지 않은 실행은 `skipped`, 공개 완료는 `success`, 예외가 발생한 실행은 `failure` 지표로 기록한다.

## 운영상 유의사항

- 잔여 TO 랜덤 배정은 점수, 지원 이력, 선호 프로젝트, 학교, 역할을 고려하지 않는다.
- 후보자가 부족하면 남은 TO는 그대로 남는다.
- 랜덤 배정자는 지원서가 없으므로 `ProjectApplication` 상태가 변경되지 않는다.
- 랜덤 배정자는 `ProjectMember.application = null`로 저장되며, 일반 지원서 합격자와 구분된다.
- 중복 제외 기준은 같은 기수의 `ACTIVE`·`PENDING_PUBLICATION` 멤버와 이번 실행에서 이미 선발한 멤버다.
- 프로젝트 중단 API는 `ACTIVE`와 `PENDING_PUBLICATION` 멤버를 `WITHDRAWN`으로 변경한다. 완료 API는 기존처럼 `ACTIVE` 멤버만 `COMPLETED`로 변경한다. 이후 발표 작업은 `ABORTED`, `COMPLETED` 프로젝트의 멤버와 지원서를 변경하지 않는다.
- 스케줄러는 기본 활성화되어 있으며 `scheduler.matching-round-deadline.enabled=false`로 끌 수 있다.

## 테스트 기준

관련 단위 테스트는 `ProjectMatchingRoundFinalizationCommandServiceTest`에 있다.

주요 케이스:

- `차수_유형에_맞는_파트만_4차에서_랜덤_매칭하고_PENDING_PUBLICATION으로_생성한다`
- `일반_차수_종료_후에는_잔여_TO_랜덤_매칭을_하지_않는다`
- `개발자_4차_시작_전에는_랜덤_매칭을_실행할_수_없다`
- `이전_차수_active_멤버가_있으면_남은_TO_기준으로_정책이_적용된다`
- `남은_TO가_0이면_의무_없음으로_처리되어_SUBMITTED는_모두_REJECTED`

## 배포 시 확인 사항

- 3차 종료 시 랜덤 매칭은 실행하지 않으므로 지부별로 4차 차수를 등록해야 한다. 배포 당시 진행 중인 3차도 같은 처리를 따른다.
- 차수 생성 요청에는 `resultPublishAt`이 필수다. 프론트의 입력·요청 변경과 서버 배포 순서를 맞춰야 한다.
- 기존 차수의 일정은 이번 검증 추가로 자동 변경되지 않는다. 이미 등록된 차수도 이전 발표 시각과 다음 시작 시각을 확인한다.
