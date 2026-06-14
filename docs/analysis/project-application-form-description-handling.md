# 프로젝트 지원 폼 description 처리 변경 보고서

작성일: 2026-06-15

## 배경

`PUT /api/v1/projects/{projectId}/application-form`는 본문이 지원 폼의 새 전체 상태가 되는 API다.
기존 구현에서는 옵션 변경은 반영되지만, 일부 경로에서 title/description 메타데이터가 반영되지 않았다.

확인된 원인은 다음과 같다.

- `IN_PROGRESS` 상태에서 활성 매칭 차수가 없을 때 질문 수정은 기존 질문을 직접 수정하지 않고 `forkQuestion()`으로 새 질문을 만든다.
- 기존 fork 경로는 원본 질문의 title/description/type/isRequired를 복사한 뒤, 요청 본문의 질문 메타데이터를 새 질문에 다시 적용하지 않았다.
- 신규 폼 생성 시 `CreateDraftFormCommand.description`은 만들어졌지만 `Form.createDraft(...)`로 전달되지 않았다.
- 신규 질문 생성 시 `CreateQuestionCommand.description`은 만들어졌지만 `Question.create(...)`로 전달되지 않았다.
- survey 도메인의 update 메서드는 PATCH 의미라서 `description == null`을 "변경 없음"으로 처리했고, 지원 폼 PUT의 full replace 의미와 충돌했다.

## 변경 내용

질문 fork 경로를 보정했다.

- `ProjectApplicationFormCommandService`는 `forkQuestion()` 후 반환된 새 questionId에 대해 `updateQuestion()`을 즉시 호출한다.
- 새 질문에는 요청의 type, title, description, isRequired가 반영된다.
- 이후 옵션은 기존처럼 새 questionId 기준으로 생성/정렬된다.

신규 생성 경로에서 description을 저장하도록 보정했다.

- `Form.createDraft(...)`에 description 저장 경로를 추가했다.
- `Question.create(...)`에 description 저장 경로를 추가했다.
- `FormCommandService.createDraft()`와 `QuestionCommandService.createQuestion()`이 command의 description을 도메인 팩토리로 전달한다.

지원 폼 PUT에서 description 삭제 의미를 명시했다.

- `UpdateFormCommand`, `UpdateFormSectionCommand`, `UpdateQuestionCommand`에 `clearDescription` 플래그를 추가했다.
- 기본값은 `null/false`로 취급하므로 survey 도메인의 기존 PATCH 의미는 유지된다.
- PROJECT-106 지원 폼 PUT 경로에서만 요청 description이 null이면 `clearDescription = true`로 전달한다.
- 따라서 지원 폼 PUT에서 description 필드를 null로 보내거나 생략하면 기존 description이 삭제된다.

## 빈 문자열 주관식 응답 평가

지원서 답변 저장은 `ProjectApplicationCommandService`가 survey `ManageFormResponseUseCase.updateDraft()`에 위임한다.
주관식 검증은 `FormResponseCommandService.validateAnswerAgainstQuestion()`에서 수행된다.

현재 정책은 다음과 같다.

- `SHORT_TEXT`, `LONG_TEXT`의 `textValue == null`은 `INVALID_ANSWER_FORMAT`이다.
- `textValue == ""`도 `String.isBlank()`에 걸려 `INVALID_ANSWER_FORMAT`이다.
- `textValue == "   "`처럼 공백만 있는 문자열도 `INVALID_ANSWER_FORMAT`이다.
- 빈 답변을 의도할 때는 해당 answer entry를 보내지 않아야 한다.
- 필수 질문의 답변 entry를 생략하면 draft 저장은 가능하지만, 제출 시 `REQUIRED_QUESTION_NOT_ANSWERED`로 거부된다.

`PORTFOLIO`는 텍스트 또는 파일 중 하나가 필요하다.

- `textValue == ""` 또는 공백 문자열만 있고 fileIds가 비어 있으면 `INVALID_ANSWER_FORMAT`이다.
- fileIds가 있으면 textValue가 빈 문자열이어도 파일 응답으로 유효하다.

## 검증

변경 중 실행한 주요 검증 명령은 다음과 같다.

```bash
./gradlew test --tests "com.umc.product.project.application.service.command.ProjectApplicationFormCommandServiceTest"
./gradlew test --tests "com.umc.product.survey.application.service.command.FormCommandServiceTest" --tests "com.umc.product.survey.application.service.command.QuestionCommandServiceTest"
./gradlew test --tests "com.umc.product.survey.application.service.command.FormResponseCommandServiceTest"
```

최종 회귀 검증은 전체 테스트로 확인한다.

```bash
./gradlew test
```
