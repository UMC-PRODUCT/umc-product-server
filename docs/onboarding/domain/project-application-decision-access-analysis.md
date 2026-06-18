# Project Application Decision Access Analysis

작성일: 2026-06-15

## 요약

보고 내용은 다음 두 경우를 분리해서 봐야 한다.

1. 지원자가 `CHAPTER_PRESIDENT`이면서 챌린저인 경우
2. 상태 변경을 시도한 요청자가 `CHAPTER_PRESIDENT`인 경우

현재 BE 코드 기준 결론은 다르다.

- 지원자가 `CHAPTER_PRESIDENT`인 것만으로 상태 변경이 막히는 로직은 확인되지 않았다.
- 상태 변경 요청자가 `CHAPTER_PRESIDENT`이지만 해당 프로젝트의 PO가 아니라면, 상태 변경이 막히는 것이 현재 정책상 정상이다.

따라서 "지부장인 지원자의 지원서를 PO가 변경하지 못했다"는 보고라면 FE 처리, 요청 계정, applicationId, 일시적 오류 가능성을 확인해야 한다. 반대로 "지부장이 직접 상태 변경을 시도했다"는 보고라면 BE 정책과 일치한다.

## 상태 변경 권한 경로

지원서 상태 변경 API는 다음 컨트롤러 경로를 사용한다.

```http
PATCH /api/v1/projects/{projectId}/applications/{applicationId}/decision
```

컨트롤러는 `@CheckAccess(resourceType = PROJECT_APPLICATION, permission = APPROVE)`로 진입 전 권한을 검사한다.

권한 평가는 `ProjectApplicationPermissionEvaluator.canApprove()`에서 처리된다. 현재 구현은 다음 조건만 통과시킨다.

1. 지원서 상태가 `SUBMITTED`, `APPROVED`, `REJECTED` 중 하나여야 한다.
2. 요청자 `memberId`가 부모 프로젝트의 `productOwnerMemberId`와 같아야 한다.

즉 `APPROVE` 권한은 지부장, 총괄단, 보조 PM 권한을 보지 않는다. 실제 코드도 `return isOwner(subject, project);`로 끝난다.

## 지원자가 CHAPTER_PRESIDENT인 경우

상태 변경 서비스인 `ProjectApplicationCommandService.decide()`는 지원자의 역할을 확인하지 않는다.

서비스가 확인하는 것은 다음이다.

- 대상 지원서 존재 여부
- `APPROVED` 변경 시 잔여 quota
- `REJECTED` 변경 시 최소 선발 규정
- 도메인 상태 전이 가능 여부
- 매칭 차수 잠금 여부

지원자의 `CHAPTER_PRESIDENT` 역할 여부는 이 흐름에 없다. 지원자가 지부장 역할을 가진 챌린저여도, 요청자가 프로젝트 PO이고 위 도메인 조건을 통과하면 상태 변경은 가능해야 한다.

## 요청자가 CHAPTER_PRESIDENT인 경우

요청자가 지부장이고 프로젝트 PO가 아니라면 현재 BE 정책상 상태 변경은 불가능하다.

관련 테스트도 같은 방향이다.

- 부모 PO의 `APPROVE`는 허용된다.
- 보조 PM의 `APPROVE`는 거부된다.
- 총괄단의 `APPROVE`도 거부된다.
- 지부장은 지원서 `READ`에는 허용되지만 `APPROVE` 권한에는 포함되어 있지 않다.

따라서 지부장이 운영진 화면에서 직접 합격/불합격을 누르는 UX가 필요하다면, 이는 클라이언트 오류가 아니라 BE 권한 정책 변경 요구사항이다.

## 확인해야 할 로그/재현 조건

보고를 확정하려면 다음 값을 확인해야 한다.

- 상태 변경을 누른 요청자 `memberId`
- 해당 프로젝트의 `productOwnerMemberId`
- 대상 지원서의 `applicantMemberId`
- 요청자의 `CHAPTER_PRESIDENT` 역할 `gisuId`, `organizationId(chapterId)`
- 응답 HTTP status와 error code

판정 기준은 다음이다.

- 응답이 403 `RESOURCE_ACCESS_DENIED`이고 요청자가 PO가 아니면 BE 정책상 정상 차단이다.
- 요청자가 PO인데 403이 발생했다면 권한 subject 생성, 토큰 계정, 프로젝트 PO 값, applicationId 매핑을 추가 조사해야 한다.
- 응답이 409라면 권한 문제가 아니라 quota 또는 최소 선발 규정 문제일 가능성이 높다.
- 응답이 400 `PROJECT_MATCHING_ROUND_LOCKED`라면 차수 결정 마감 이후 변경 시도다.

## 검증

다음 테스트를 실행해 현재 권한 정책과 상태 변경 서비스 동작을 확인했다.

```bash
./gradlew test --tests 'com.umc.product.project.application.service.evaluator.ProjectApplicationPermissionEvaluatorTest' --tests 'com.umc.product.project.application.service.command.ProjectApplicationCommandServiceTest'
```

결과:

```text
BUILD SUCCESSFUL in 11s
```
