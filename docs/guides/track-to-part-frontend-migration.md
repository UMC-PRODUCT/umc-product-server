# 프론트엔드 요청·응답 변경 요약

[PR #1337](https://github.com/UMC-PRODUCT/umc-product-server/pull/1337) + 현재 로컬 변경 기준. 아래 API의 URL·HTTP method는 유지된다. **리크루팅의 Track 요청·응답은 유지한다.**

## 공통 타입

- `ChallengerPart`에 `WEB_PRODUCT_ENGINEER`(웹 프로덕트 엔지니어), `MOBILE_PRODUCT_ENGINEER`(모바일 프로덕트 엔지니어) 추가. 기존 값 유지, `INFRA` 추가 없음.
- `infra`: boolean, 생략 시 `false`. 위 두 신규 Part에서만 `true` 허용.
- 챌린저 상세의 `part`는 비수강 운영진이면 `null` 가능. 직접 생성 요청에서는 필수.

학습 정보 전송 예시(관련 필드만):

```json
{"tracks": ["WEB_PRODUCT_ENGINEER"]}
```

↓

```json
{"part": "WEB_PRODUCT_ENGINEER", "infra": false}
```

## 1. 챌린저·회원

기본 경로: `/api/v1/challenger`

| API | Request | Response |
| --- | --- | --- |
| `POST` (기본 경로) | `tracks` 제거 → `part` 필수, `infra` 추가 | `tracks` 제거, `part` 유지, `infra` 추가 |
| `POST /batch` | 배열 각 항목에 동일 변경 | 배열 각 항목에 동일 변경 |
| `GET /{challengerId}` | 변경 없음 | `tracks` 제거, `part` 유지, `infra` 추가 |
| `PATCH /{challengerId}/part` | 기존 `{ "newPart": "WEB_PRODUCT_ENGINEER" }` 형태 유지 | 위와 동일 |
| `POST /{challengerId}/points` | 변경 없음 | 반환하는 챌린저 상세에 동일 변경 |

- v1 회원 `GET /api/v1/member/me`, `GET /api/v1/member/profile/{memberId}` 및 회원 수정·삭제 응답의 `challengerRecords[]`도 동일 변경.
- v2 회원 `/me`, v1/v2 회원 검색, v2 챌린저 검색에는 `infra`가 추가되지 않는다.
- Part 수정 API는 `infra`를 입력받지 않는다. 인프라를 허용하지 않는 Part로 변경하면 응답의 `infra=false`.

## 2. 챌린저 등록 코드

기본 경로: `/api/v1/challenger-record`

| API | Request | Response |
| --- | --- | --- |
| `POST` (기본 경로) | `track`, `tracks` 제거 → `part`, `infra` 사용 | `track`, `tracks` 제거, `part` 유지, `infra` 추가 |
| `POST /bulk` | 배열 각 항목에 동일 변경 | 기존 코드 ID 배열 유지 |
| `GET /code/{code}`, `GET /id/{id}` | 변경 없음 | `track`, `tracks` 제거, `part` 유지, `infra` 추가 |
| `POST /member` | 기존 `{ "code": "ABC123" }` 유지 | 상세 반환 없음. 회원·챌린저 재조회 |

- 일반 코드는 `part` 필수. `challengerRoleType`이 있으면 `part=null` 허용.
- `chapterId` 생략은 중앙 운영진 역할 + `part=null`일 때만 허용. `schoolId` 필수 유지.
- 현재 운영진 코드의 `part`는 **수강 Part와 담당 Part에 함께 반영**된다.

## 3. 커리큘럼·워크북

기본 경로: `/api/v2/curriculums`

| API | Request | Response |
| --- | --- | --- |
| `POST` (기본 경로) | body `track` 제거 → `{gisuId, part, title}`, `part` 필수 | 변경 없음 |
| `GET /overview` | query `track` 제거, `part` 필수. `gisuId`, `weekNo` 유지 | `track` 제거, `part` 유지 |
| `GET /progress/me` | query `track` → `part`(선택). 생략 시 본인 Part, 다른 Part 요청 시 403 | `track` 제거, `part` 유지 |
| `GET /weekly-best-workbooks` | query `tracks` 제거, `parts` 사용. 나머지 필터 유지 | 각 항목의 `track` → `part` |
| `GET /workbook-submissions` | 변경 없음 | 각 행의 `track` 제거, `part` 유지 |

필터 예시: `?gisuId=101&parts=WEB_PRODUCT_ENGINEER&parts=MOBILE_PRODUCT_ENGINEER`

## 4. 스터디그룹

기본 경로: `/api/v1/study-groups`

| API | Request | Response |
| --- | --- | --- |
| `POST` (기본 경로) | `track` 제거, `part` 필수. 나머지 필드 유지 | 변경 없음 |
| `PATCH /{studyGroupId}` | `track` 제거. `part` 생략·null이면 변경 없음 | 변경 없음 |
| `GET /managed`, `GET /{studyGroupId}` | 변경 없음 | `track` 제거, **`studyPart` 유지** |

**스터디 요청 키는 `part`, 조회 응답 키는 `studyPart`다.**

## 5. 기수·GraphQL

| API / 타입 | Response·스키마 변경 |
| --- | --- |
| `GET /api/v1/gisu/{gisuId}`, `/api/v1/gisu/active` | `result.learningType` 제거 |
| `GET /api/v1/gisu` | `result.content[].learningType` 제거 |
| `GET /api/v2/gisu` | `result.gisus[].learningType` 제거 |
| GraphQL `MemberChallenger` | `tracks` 제거, `part: ChallengerPart`(nullable) 유지, `infra: Boolean!` 추가 |
| GraphQL `Gisu` | `learningType` 제거. `GisuLearningType` enum도 제거 |
| GraphQL `ChallengerPart` | 신규 두 Part 추가 |

GraphQL query·fragment의 `MemberChallenger`는 `tracks` 대신 `part`, `infra`를 사용하고, `Gisu.learningType` selection은 제거한다. `MemberSearchChallenger`에는 `infra`가 추가되지 않는다. 리크루팅의 Track selection은 유지한다.

응답 참고: `GET /api/v1/challenger/search/offset`의 `result.partCounts`는 현재 신규 두 Part가 누락되어 있어 백엔드 보완이 필요하다.
