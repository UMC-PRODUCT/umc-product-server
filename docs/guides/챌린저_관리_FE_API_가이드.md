# 챌린저 관리 FE API 가이드

> 작성일: 2026-05-06
>
> 본 문서는 두 가지 플로우에 사용되는 백엔드 API의 request/response 스펙을 FE 관점에서 정리한 것입니다.
>
> - **플로우 A — 운영진 흐름**: 회원 검색 → 회원의 챌린저 기록 조회 → 특정 챌린저 기록 상세 → 상벌점 부여
> - **플로우 B — 챌린저 기록 부여 및 정보 조회**: 6자리 코드 발급/사용/조회 + 신규 챌린저 등록

---

## 0. 공통 사항

### 0.1 Base URL & 인증

- 모든 엔드포인트는 `/api/v1/...` 접두사를 가진다.
- `[Public]` 으로 표시되지 않은 엔드포인트는 모두 **Authorization 헤더 필수**.
  ```
  Authorization: Bearer {accessToken}
  ```
- 본 문서의 모든 엔드포인트는 인증 필요 (Public 없음).

### 0.2 응답 래핑 (`ApiResponse`)

[GlobalResponseWrapper.java](src/main/java/com/umc/product/global/response/GlobalResponseWrapper.java) 에 의해 **모든 컨트롤러 응답은 자동으로 다음 형태로 래핑**된다.

**성공 응답:**

```json
{
  "success": true,
  "code": "COMMON-200",
  "message": "성공입니다.",
  "result": <컨트롤러가 리턴한 값>
}
```

**실패 응답 (에러):**

```json
{
  "success": false,
  "code": "CHALLENGER-0001",
  "message": "챌린저를 찾을 수 없습니다.",
  "result": null
}
```

> 본 문서의 "Response" 섹션에는 **`result` 안에 들어가는 값만** 기술한다. FE 에서는 항상 `response.result` 로 접근해야 한다.

### 0.3 페이지네이션 형식

#### Offset 기반 (`PageResponse<T>`)

[PageResponse.java](src/main/java/com/umc/product/global/response/PageResponse.java)

```json
{
  "content": [ /* T[] */ ],
  "page": 0,
  "size": 20,
  "totalElements": 153,
  "totalPages": 8,
  "hasNext": true,
  "hasPrevious": false
}
```

- 요청 쿼리: `?page=0&size=20&sort=id,desc` (Spring `Pageable` 표준)

#### Cursor 기반 (`CursorResponse<T>`)

[CursorResponse.java](src/main/java/com/umc/product/global/response/CursorResponse.java)

```json
{
  "content": [ /* T[] */ ],
  "nextCursor": 12345,
  "hasNext": true
}
```

- 요청 쿼리: `?cursor=12345&size=20` (`cursor` 미전달 시 첫 페이지). `size` 기본 20, 최대 50.

### 0.4 주요 Enum

| Enum                         | 값                                                                                                                                                                                                                                                   |
|------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ChallengerPart`             | `PLAN`, `DESIGN`, `WEB`, `ANDROID`, `IOS`, `NODEJS`, `SPRINGBOOT`, `ADMIN`                                                                                                                                                                          |
| `ChallengerStatus`           | `ACTIVE`, `GRADUATED`, `EXPELLED`, `WITHDRAWN`                                                                                                                                                                                                      |
| `MemberStatus`               | `ACTIVE`, `INACTIVE`, `WITHDRAWN`                                                                                                                                                                                                                   |
| `ChallengerRoleType`         | `SUPER_ADMIN` / `CENTRAL_PRESIDENT` / `CENTRAL_VICE_PRESIDENT` / `CENTRAL_OPERATING_TEAM_MEMBER` / `CENTRAL_EDUCATION_TEAM_MEMBER` / `CHAPTER_PRESIDENT` / `SCHOOL_PRESIDENT` / `SCHOOL_VICE_PRESIDENT` / `SCHOOL_PART_LEADER` / `SCHOOL_ETC_ADMIN` |
| `ChallengerDeactivationType` | `WITHDRAW` (탈부), `EXPEL` (제명)                                                                                                                                                                                                                       |
| `OrganizationType`           | `CENTRAL`, `CHAPTER`, `SCHOOL`                                                                                                                                                                                                                      |

#### `PointType` (상벌점 유형) — [PointType.java](src/main/java/com/umc/product/challenger/domain/enums/PointType.java)

각 유형은 **고정 점수**를 가지지만, 요청 시 `pointValue` 를 함께 보내면 **그 값으로 덮어쓸 수 있다** (`pointValue` null 이면 아래 표의 기본값 사용).

| Enum 값                           | 기본 점수 | 설명                    |
|----------------------------------|------:|-----------------------|
| `BLOG_CHALLENGE`                 |  +3.0 | 블로그 챌린지 참여 (매주 최대 +3) |
| `BEST_WORKBOOK_V2`               |  +2.0 | 베스트 워크북 (10기~)        |
| `UMC_EVENT_REVIEW`               |  +1.0 | 행사 리뷰어                |
| `PEER_REVIEW_SUBMISSION`         |  +1.0 | PeerReview 작성         |
| `OUT`                            |  +1.0 | 아웃                    |
| `WARNING`                        |   0.0 | 경고                    |
| `CUSTOM`                         |   0.0 | 자체 제도 운영 (ex. 가천대)    |
| `BEST_WORKBOOK`                  |  -0.5 | 베스트 워크북 (~9기, 레거시)    |
| `STUDY_LATE`                     |  -2.0 | 스터디 무단 지각             |
| `STUDY_ABSENT`                   |  -4.0 | 스터디 무단 불참             |
| `EVENT_LATE`                     |  -2.0 | 행사 무단 지각              |
| `EVENT_EARLY_LEAVE`              |  -2.0 | 행사 중도 퇴실              |
| `EVENT_LATE_CANCEL`              |  -4.0 | 행사 기간 외 취소            |
| `EVENT_NO_SHOW`                  | -10.0 | 노쇼 (무단 결석)            |
| `NO_WORKBOOK_MISSION`            |  -4.0 | 과제 미수행                |
| `PART_LEAD_FEEDBACK_LATE`        |  -4.0 | 기간 외 피드백              |
| `SCHOOL_CORE_MEETING_ABSENT`     |  -4.0 | 회의 무단 불참              |
| `SCHOOL_CORE_TASK_NOT_COMPLETED` |  -4.0 | 업무 무단 불이행             |

---

# 플로우 A — 운영진 흐름

운영진이 회원을 검색해서 특정 회원의 챌린저 기록(기수별 활동 이력)을 확인하고, 해당 챌린저 기록에 상벌점을 부여하는 흐름.

```
[A-1] 회원 검색
    ↓ memberId 획득
[A-2] 회원 정보 조회 (챌린저 기록 목록 포함)
    ↓ challengerId 선택
[A-3] 챌린저 기록 상세 조회 (상벌점 목록 포함)
    ↓
[A-4] 상벌점 부여 / [A-5] 사유 수정 / [A-6] 삭제
```

---

## A-1. 회원 검색 — `[MEMBER-103]`

[MemberQueryController.java:50-59](src/main/java/com/umc/product/member/adapter/in/web/MemberQueryController.java#L50-L59)

이름 / 닉네임 / 이메일 / 학교명을 키워드로 검색하고, 기수·파트·지부·학교로 필터링한다.

### 요청

```
GET /api/v1/member/search
```

**Query Parameters** (모두 optional)

| 이름          | 타입                    | 설명                    |
|-------------|-----------------------|-----------------------|
| `keyword`   | string                | 이름/닉네임/이메일/학교명 통합 키워드 |
| `gisuId`    | long                  | 기수 ID 필터              |
| `part`      | enum `ChallengerPart` | 파트 필터                 |
| `chapterId` | long                  | 지부 ID 필터              |
| `schoolId`  | long                  | 학교 ID 필터              |
| `page`      | int                   | 페이지 번호(0-base, 기본 0)  |
| `size`      | int                   | 페이지 크기(기본 20)         |
| `sort`      | string                | 정렬, 예: `id,desc`      |

### 응답 (`result` 내부)

[SearchMemberResponse.java](src/main/java/com/umc/product/member/adapter/in/web/dto/response/SearchMemberResponse.java)

```json
{
  "totalCount": 153,
  "page": {
    "content": [
      {
        "memberId": 12,
        "name": "홍길동",
        "nickname": "gildong",
        "email": null,
        "schoolId": 1,
        "schoolName": "가천대학교",
        "profileImageLink": "https://.../profile.png",
        "challengerId": 401,
        "gisuId": 9,
        "gisu": 9,
        "part": "WEB",
        "roleTypes": ["SCHOOL_PART_LEADER"]
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 153,
    "totalPages": 8,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

> `challengerId` / `gisuId` / `gisu` / `part` / `roleTypes` 는 해당 회원의 **현재(가장 최근) 챌린저 기록** 한 건 기준으로 채워진다. 전체 기록은 [A-2](#a-2-회원-정보-조회--member-101) 에서 조회한다.

---

## A-2. 회원 정보 조회 — `[MEMBER-101]`

[MemberQueryController.java:32-42](src/main/java/com/umc/product/member/adapter/in/web/MemberQueryController.java#L32-L42)

회원 프로필과 함께 **모든 챌린저 기록(기수별)** 을 한 번에 받아온다.

### 요청

```
GET /api/v1/member/profile/{memberId}
```

**Path Parameters**

| 이름         | 타입   | 설명       |
|------------|------|----------|
| `memberId` | long | 대상 회원 ID |

권한: `@CheckAccess(MEMBER, READ)`

### 응답 (`result` 내부)

[MemberInfoResponse.java](src/main/java/com/umc/product/member/adapter/in/web/dto/response/MemberInfoResponse.java)

```json
{
  "id": 12,
  "name": "홍길동",
  "nickname": "gildong",
  "email": null,
  "schoolId": 1,
  "schoolName": "가천대학교",
  "profileImageLink": "https://.../profile.png",
  "status": null,
  "roles": [
    {
      "id": 88,
      "challengerId": 401,
      "roleType": "SCHOOL_PART_LEADER",
      "organizationType": "SCHOOL",
      "organizationId": 1,
      "responsiblePart": "WEB",
      "gisuId": 9
    }
  ],
  "challengerRecords": [
    {
      "challengerId": 401,
      "memberId": 12,
      "gisuId": 9,
      "gisu": 9,
      "chapterId": 3,
      "chapterName": "수도권",
      "part": "WEB",
      "challengerStatus": "ACTIVE",
      "points": [],
      "challengerPoints": [],
      "totalPoints": 0.0,
      "roles": [],
      "name": "홍길동",
      "nickname": "gildong",
      "email": null,
      "schoolId": 1,
      "schoolName": "가천대학교",
      "profileImageLink": "https://.../profile.png",
      "memberStatus": null,
      "status": null
    }
  ],
  "profile": null
}
```

> **주의 — Public View:** 다른 회원 조회 시에는 보안상 `email`, `status`, `memberStatus` 가 모두 `null` 로 마스킹되며, 각 challengerRecord 의 `points` 도 빈 배열로 비워진다. 본인 조회 (`/me`) 와는 차이가 있음.

> **본인 프로필 조회:** `GET /api/v1/member/me` — 동일 응답 구조이며 마스킹 없음 (이번 플로우에는 운영진이 타인을 보는 경우만 사용).

> `challengerPoints` 는 deprecated 필드로 `points` 와 동일한 값. FE 에서는 `points` 를 사용할 것.

---

## A-3. 챌린저 기록 상세 조회 — `[CHALLENGER-101]`

[ChallengerQueryController.java:44-48](src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerQueryController.java#L44-L48)

특정 챌린저 기록 1건의 전체 상세 (상벌점 목록 + 역할 + 회원 정보 포함).

### 요청

```
GET /api/v1/challenger/{challengerId}
```

**Path Parameters**

| 이름             | 타입   | 설명        |
|----------------|------|-----------|
| `challengerId` | long | 챌린저 기록 ID |

### 응답 (`result` 내부)

[ChallengerInfoResponse.java](src/main/java/com/umc/product/challenger/adapter/in/web/dto/response/ChallengerInfoResponse.java)

```json
{
  "challengerId": 401,
  "memberId": 12,
  "gisuId": 9,
  "gisu": 9,
  "chapterId": 3,
  "chapterName": "수도권",
  "part": "WEB",
  "challengerStatus": "ACTIVE",
  "points": [
    {
      "id": 9001,
      "challengerId": 401,
      "pointType": "STUDY_LATE",
      "point": -2.0,
      "description": "5/2 스터디 15분 지각",
      "createdAt": "2026-05-02T13:05:00Z"
    }
  ],
  "challengerPoints": [ /* 위와 동일, deprecated */ ],
  "totalPoints": -2.0,
  "roles": [
    {
      "challengerRoleId": 88,
      "challengerId": 401,
      "roleType": "SCHOOL_PART_LEADER",
      "organizationType": "SCHOOL",
      "organizationId": 1,
      "responsiblePart": "WEB",
      "gisuId": 9,
      "gisu": 9
    }
  ],
  "name": "홍길동",
  "nickname": "gildong",
  "email": null,
  "schoolId": 1,
  "schoolName": "가천대학교",
  "profileImageLink": "https://.../profile.png",
  "memberStatus": "ACTIVE",
  "status": "ACTIVE"
}
```

> `points[].point` 는 `pointType` 의 기본값 또는 부여 시 명시한 `pointValue` 가 반영된 **실제 적용된 값**.
>
> `totalPoints` 는 모든 `points[].point` 의 합산.

**주요 에러:**

- `CHALLENGER-0001` (404) — 챌린저 기록 없음

---

## A-4. 챌린저 상벌점 부여 — `[POINT-001]`

[ChallengerPointCommandController.java:38-47](src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerPointCommandController.java#L38-L47)

권한: `@CheckAccess(CHALLENGER_POINT, WRITE)` — 중앙운영사무국 소속 또는 해당 챌린저의 **학교 회장단** 만 부여 가능.

### 요청

```
POST /api/v1/challenger/{challengerId}/points
Content-Type: application/json
```

**Path Parameters**

| 이름             | 타입   | 설명              |
|----------------|------|-----------------|
| `challengerId` | long | 부여 대상 챌린저 기록 ID |

**Request Body** — [GrantChallengerPointRequest.java](src/main/java/com/umc/product/challenger/adapter/in/web/dto/request/GrantChallengerPointRequest.java)

| 필드            | 타입               | 필수         | 설명                                                                      |
|---------------|------------------|------------|-------------------------------------------------------------------------|
| `pointType`   | `PointType` enum | ✅          | 상벌점 유형                                                                  |
| `pointValue`  | integer          | ⛔ optional | **null 이면 `pointType` 의 기본 점수 사용**. 명시 시 그 값으로 덮어씀 (`CUSTOM` 등 자체 제도용). |
| `description` | string           | ✅          | 부여 사유                                                                   |

```json
{
  "pointType": "STUDY_LATE",
  "pointValue": null,
  "description": "5/2 스터디 15분 지각"
}
```

또는 자체 제도용 커스텀 점수:

```json
{
  "pointType": "CUSTOM",
  "pointValue": -3,
  "description": "가천대 자체 규정 위반"
}
```

### 응답 (`result` 내부)

부여 후의 [ChallengerInfoResponse](#a-3-챌린저-기록-상세-조회--challenger-101) — 새로 부여된 상벌점이 `points` 배열에 포함되어 반환된다.

**주요 에러:**

- `CHALLENGER-0001` (404) — 챌린저 기록 없음
- `CHALLENGER-0004` (400) — 챌린저 상태 비유효 (이미 졸업/탈부 등)
- 권한 부족 — 401/403

---

## A-5. 챌린저 상벌점 사유 수정 — `[POINT-002]`

[ChallengerPointCommandController.java:55-62](src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerPointCommandController.java#L55-L62)

권한: `@CheckAccess(CHALLENGER_POINT, EDIT)` — 중앙운영사무국 또는 해당 챌린저의 학교 회장단.

### 요청

```
PATCH /api/v1/challenger/points/{challengerPointId}
Content-Type: application/json
```

**Path Parameters**

| 이름                  | 타입   | 설명                               |
|---------------------|------|----------------------------------|
| `challengerPointId` | long | 수정할 상벌점 row ID (= `points[].id`) |

**Request Body** — [EditChallengerPointRequest.java](src/main/java/com/umc/product/challenger/adapter/in/web/dto/request/EditChallengerPointRequest.java)

| 필드               | 타입     | 필수 | 설명     |
|------------------|--------|----|--------|
| `newDescription` | string | ✅  | 변경할 사유 |

```json
{ "newDescription": "5/2 스터디 12분 지각으로 정정" }
```

### 응답

`204 No Content` — `result: null`

```json
{ "success": true, "code": "COMMON-200", "message": "성공입니다.", "result": null }
```

**주요 에러:**

- `CHALLENGER-0007` (404) — 상벌점 row 없음

---

## A-6. 챌린저 상벌점 삭제 — `[POINT-003]`

[ChallengerPointCommandController.java:70-76](src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerPointCommandController.java#L70-L76)

권한: `@CheckAccess(CHALLENGER_POINT, DELETE)` — **중앙운영사무국 총괄단(`CENTRAL_PRESIDENT`, `CENTRAL_VICE_PRESIDENT`)** 만 가능.

### 요청

```
DELETE /api/v1/challenger/points/{challengerPointId}
```

### 응답

`204 No Content` — `result: null`

---

## A-보조: 챌린저 검색 (운영진용)

회원이 아닌 **챌린저 기록(기수+파트별 활동)** 을 직접 검색할 때 사용. A-1 (회원 검색) 과 결과 단위가 다르다 — A-1 은 회원 단위, 이 API 는 (회원, 기수) 단위.

### A-보조-1. Cursor 기반 — `[CHALLENGER-102]`

[ChallengerSearchController.java:39-50](src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerSearchController.java#L39-L50)

```
GET /api/v1/challenger/search/cursor
```

**Query Parameters** ([SearchChallengerCursorRequest.java](src/main/java/com/umc/product/challenger/adapter/in/web/dto/request/SearchChallengerCursorRequest.java))

| 이름          | 타입               | 설명                                           |
|-------------|------------------|----------------------------------------------|
| `cursor`    | long             | 직전 페이지 마지막 challengerId, 첫 페이지 시 미전달         |
| `size`      | int              | 기본 20, 최대 50                                 |
| `keyword`   | string           | 이름 또는 닉네임 통합 검색 (있으면 `name` / `nickname` 무시) |
| `name`      | string           | 이름 부분일치                                      |
| `nickname`  | string           | 닉네임 부분일치                                     |
| `schoolId`  | long             | 학교 ID                                        |
| `chapterId` | long             | 지부 ID                                        |
| `part`      | `ChallengerPart` | 파트                                           |
| `gisuId`    | long             | 기수 ID                                        |

> 모든 필터는 `AND` 조건. `keyword` 제공 시 `name`/`nickname` 은 무시되지만 다른 필터는 유효.
>
> 결과는 **`ACTIVE`** 상태의 챌린저로 한정됨.

**응답 (`result`)** — [CursorSearchChallengerResponse.java](src/main/java/com/umc/product/challenger/adapter/in/web/dto/response/CursorSearchChallengerResponse.java)

```json
{
  "cursor": {
    "content": [
      {
        "challengerId": 401,
        "memberId": 12,
        "gisuId": 9,
        "generation": 9,
        "gisu": 9,
        "part": "WEB",
        "name": "홍길동",
        "nickname": "gildong",
        "schoolName": "가천대학교",
        "pointSum": -2.0,
        "profileImageLink": "https://.../profile.png",
        "roleTypes": ["SCHOOL_PART_LEADER"]
      }
    ],
    "nextCursor": 401,
    "hasNext": true
  },
  "partCounts": [
    { "part": "PLAN", "count": 12 },
    { "part": "DESIGN", "count": 8 },
    { "part": "WEB", "count": 30 },
    { "part": "ANDROID", "count": 5 },
    { "part": "IOS", "count": 4 },
    { "part": "NODEJS", "count": 7 },
    { "part": "SPRINGBOOT", "count": 11 }
  ]
}
```

> `generation` 은 deprecated, `gisu` 사용 권장.
>
> `partCounts` 는 **현재 필터 조건에 매칭되는 전체** 의 파트별 카운트 (페이지 기준이 아님).

### A-보조-2. Offset 기반 — `[CHALLENGER-103]`

[ChallengerSearchController.java:52-64](src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerSearchController.java#L52-L64)

```
GET /api/v1/challenger/search/offset?page=0&size=20&sort=id,desc
```

쿼리 필터는 cursor 와 동일(`cursor` 제외, `page`/`size`/`sort` 추가).

**응답 (`result`)** — [SearchChallengerResponse.java](src/main/java/com/umc/product/challenger/adapter/in/web/dto/response/SearchChallengerResponse.java)

```json
{
  "page": {
    "content": [ /* SearchChallengerItemResponse[] (cursor 응답과 동일) */ ],
    "page": 0,
    "size": 20,
    "totalElements": 77,
    "totalPages": 4,
    "hasNext": true,
    "hasPrevious": false
  },
  "partCounts": [ /* 동일 */ ]
}
```

---

# 플로우 B — 챌린저 기록 부여 및 정보 조회

챌린저 기록(`Challenger`)을 만드는 두 가지 방식과, 기록을 조회하는 API.

```
[방식 1] 현재 기수 챌린저 등록 (관리자가 회원 ID로 직접 생성)
  [B-1] POST /api/v1/challenger
  [B-2] POST /api/v1/challenger/batch  (다건)

[방식 2] 과거 기수 코드 기반 등록 (관리자가 코드 발급 → 회원이 코드 입력)
  [B-3] POST /api/v1/challenger-record           (관리자 — 코드 발급)
  [B-4] POST /api/v1/challenger-record/bulk      (관리자 — 다건 코드 발급)
  [B-5] POST /api/v1/challenger-record/member    (회원 — 코드 사용)

[조회]
  [B-6] GET  /api/v1/challenger-record/code/{code}
  [B-7] GET  /api/v1/challenger-record/id/{id}
```

---

## B-1. 챌린저 직접 생성 — `[CHALLENGER-001]`

[ChallengerCommandController.java:40-46](src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandController.java#L40-L46)

권한: `@CheckAccess(CHALLENGER, WRITE)`

### 요청

```
POST /api/v1/challenger
Content-Type: application/json
```

**Request Body** — [CreateChallengerInfoRequest.java](src/main/java/com/umc/product/challenger/adapter/in/web/dto/request/CreateChallengerInfoRequest.java)

| 필드         | 타입               | 필수 | 설명       |
|------------|------------------|----|----------|
| `memberId` | long             | ✅  | 대상 회원 ID |
| `part`     | `ChallengerPart` | ✅  | 파트       |
| `gisuId`   | long             | ✅  | 기수 ID    |

```json
{ "memberId": 12, "part": "WEB", "gisuId": 10 }
```

### 응답 (`result`)

생성된 챌린저의 [ChallengerInfoResponse](#a-3-챌린저-기록-상세-조회--challenger-101).

---

## B-2. 챌린저 다건 생성 — `[CHALLENGER-002]`

[ChallengerCommandController.java:52-64](src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandController.java#L52-L64)

권한: `@CheckAccess(CHALLENGER, WRITE)`

### 요청

```
POST /api/v1/challenger/batch
Content-Type: application/json
```

**Request Body** — `CreateChallengerInfoRequest[]`

```json
[
  { "memberId": 12, "part": "WEB", "gisuId": 10 },
  { "memberId": 13, "part": "DESIGN", "gisuId": 10 }
]
```

### 응답 (`result`)

`ChallengerInfoResponse[]` — 각 항목은 [A-3 응답](#a-3-챌린저-기록-상세-조회--challenger-101) 과 동일.

> 한 건이라도 실패하면 트랜잭션 자체는 항목별로 별도 호출되므로 부분 성공이 발생할 수 있음. FE 에서는 입력 길이와 응답 길이를 비교하여 실패 항목을 식별할 것 (정밀한 실패 보고가 필요하면 단건 API 반복 호출 권장).

---

## B-3. 챌린저 기록 코드 발급 (관리자) — `[CHALLENGER-RECORD-002]`

[ChallengerRecordController.java:89-114](src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordController.java#L89-L114)

권한: `@CheckAccess(CHALLENGER_RECORD, WRITE)` — 중앙운영사무국 총괄단 전용. 9기 이전 기수의 활동 이력을 6자리 코드로 발급한다.

### 요청

```
POST /api/v1/challenger-record
Content-Type: application/json
```

**Request Body** — [CreateChallengerRecordRequest.java](src/main/java/com/umc/product/challenger/adapter/in/web/dto/request/CreateChallengerRecordRequest.java)

| 필드                   | 타입                   | 필수 | 설명                         |
|----------------------|----------------------|----|----------------------------|
| `gisuId`             | long                 | ✅  | 기수 ID                      |
| `chapterId`          | long                 | ✅  | 지부 ID                      |
| `schoolId`           | long                 | ✅  | 학교 ID                      |
| `part`               | `ChallengerPart`     | ✅  | 파트                         |
| `memberName`         | string               | ✅  | 챌린저 본명 (코드 사용 시 일치 검증에 사용) |
| `challengerRoleType` | `ChallengerRoleType` | ✅  | 해당 기수에서의 역할                |

```json
{
  "gisuId": 8,
  "chapterId": 3,
  "schoolId": 1,
  "part": "WEB",
  "memberName": "김챌린저",
  "challengerRoleType": "SCHOOL_PRESIDENT"
}
```

### 응답 (`result`)

[ChallengerRecordResponse.java](src/main/java/com/umc/product/challenger/adapter/in/web/dto/response/ChallengerRecordResponse.java)

```json
{
  "code": "AB12CD",
  "part": "WEB",
  "gisuId": 8,
  "gisu": 8,
  "schoolId": 1,
  "schoolName": "가천대학교",
  "chapterId": 3,
  "chapterName": "수도권",
  "memberName": "김챌린저",
  "challengerRoleType": "SCHOOL_PRESIDENT",
  "organizationId": 1
}
```

> `code` 가 발급된 6자리 문자열. 회원에게 전달하여 [B-5](#b-5-회원-계정에-챌린저-기록-추가-코드-사용--challenger-record-001) 에서 사용하게 한다.

---

## B-4. 챌린저 기록 코드 다건 발급 — `[CHALLENGER-RECORD-003]`

[ChallengerRecordController.java:123-147](src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordController.java#L123-L147)

권한: `@CheckAccess(CHALLENGER_RECORD, WRITE)`

### 요청

```
POST /api/v1/challenger-record/bulk
Content-Type: application/json
```

**Request Body** — `CreateChallengerRecordRequest[]` (B-3 의 객체 배열)

### 응답 (`result`)

성능 이슈로 ID 만 반환 — 실제 코드 / 정보는 [B-7](#b-7-id-로-챌린저-기록-조회--challenger-record-102) 로 개별 조회.

```json
[1001, 1002, 1003]
```

---

## B-5. 회원 계정에 챌린저 기록 추가 (코드 사용) — `[CHALLENGER-RECORD-001]`

[ChallengerRecordController.java:43-58](src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordController.java#L43-L58)

로그인한 회원이 6자리 코드를 입력하여 본인 계정에 챌린저 기록과 권한을 추가한다. 각 코드는 1회 사용 후 소진된다.

### 요청

```
POST /api/v1/challenger-record/member
Content-Type: application/json
Authorization: Bearer {accessToken}
```

**Request Body** — [AddChallengerRecordToMemberRequest.java](src/main/java/com/umc/product/challenger/adapter/in/web/dto/request/AddChallengerRecordToMemberRequest.java)

| 필드     | 타입     | 필수 | 설명     |
|--------|--------|----|--------|
| `code` | string | ✅  | 6자리 코드 |

```json
{ "code": "AB12CD" }
```

### 응답

`204 No Content` — `result: null`

**주요 에러:**

- `CHALLENGER-0012` (400) — 코드 없음 또는 이미 사용됨
- `CHALLENGER-0013` (400) — 코드의 `memberName` 과 현재 회원 이름 불일치
- `CHALLENGER-0014` (400) — 코드의 학교와 현재 회원 학교 불일치

---

## B-6. 코드로 챌린저 기록 조회 — `[CHALLENGER-RECORD-101]`

[ChallengerRecordController.java:64-70](src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordController.java#L64-L70)

권한: `@CheckAccess(CHALLENGER_RECORD, READ)`

### 요청

```
GET /api/v1/challenger-record/code/{code}
```

| 이름     | 타입     | 설명     |
|--------|--------|--------|
| `code` | string | 6자리 코드 |

### 응답 (`result`)

[ChallengerRecordResponse](#b-3-챌린저-기록-코드-발급-관리자--challenger-record-002) 와 동일.

---

## B-7. ID 로 챌린저 기록 조회 — `[CHALLENGER-RECORD-102]`

[ChallengerRecordController.java:76-82](src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerRecordController.java#L76-L82)

권한: `@CheckAccess(CHALLENGER_RECORD, READ)`

### 요청

```
GET /api/v1/challenger-record/id/{id}
```

| 이름   | 타입   | 설명                                                                                     |
|------|------|----------------------------------------------------------------------------------------|
| `id` | long | 챌린저 기록(코드) 의 DB ID — [B-4 bulk 응답](#b-4-챌린저-기록-코드-다건-발급--challenger-record-003) 으로 반환됨 |

### 응답 (`result`)

[ChallengerRecordResponse](#b-3-챌린저-기록-코드-발급-관리자--challenger-record-002) 와 동일.

---

## B-부록. 챌린저 기록 변경/삭제

### B-부록-1. 챌린저 비활성화 (제명/탈부) — `[CHALLENGER-003]`

[ChallengerCommandController.java:70-77](src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandController.java#L70-L77) · 권한: `@CheckAccess(CHALLENGER, DELETE)`

```
POST /api/v1/challenger/{challengerId}/deactivate
```

**Body** — [DeactivateChallengerRequest.java](src/main/java/com/umc/product/challenger/adapter/in/web/dto/request/DeactivateChallengerRequest.java)

```json
{
  "deactivationType": "EXPEL",
  "modifiedBy": 1,
  "reason": "출석 4회 누락"
}
```

- `deactivationType`: `WITHDRAW` (탈부) | `EXPEL` (제명)

응답: `204 No Content`

### B-부록-2. 챌린저 파트 변경 — `[CHALLENGER-004]`

[ChallengerCommandController.java:84-93](src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandController.java#L84-L93) · 권한: `@CheckAccess(CHALLENGER, EDIT)`

```
PATCH /api/v1/challenger/{challengerId}/part
```

**Body** — [EditChallengerPartRequest.java](src/main/java/com/umc/product/challenger/adapter/in/web/dto/request/EditChallengerPartRequest.java)

```json
{ "newPart": "DESIGN" }
```

응답: 변경 후 [ChallengerInfoResponse](#a-3-챌린저-기록-상세-조회--challenger-101).

### B-부록-3. 챌린저 하드 삭제 — `[CHALLENGER-005]`

[ChallengerCommandController.java:99-104](src/main/java/com/umc/product/challenger/adapter/in/web/ChallengerCommandController.java#L99-L104) · 권한: `@CheckAccess(CHALLENGER, DELETE)`

```
DELETE /api/v1/challenger/{challengerId}
```

응답: `204 No Content`. ⚠️ 복구 불가.

---

# 부록. 엔드포인트 요약 표

## 플로우 A — 운영진 흐름

| #      | 코드             | Method | Path                                            | 권한                        |
|--------|----------------|--------|-------------------------------------------------|---------------------------|
| A-1    | MEMBER-103     | GET    | `/api/v1/member/search`                         | 인증                        |
| A-2    | MEMBER-101     | GET    | `/api/v1/member/profile/{memberId}`             | `MEMBER:READ`             |
| A-3    | CHALLENGER-101 | GET    | `/api/v1/challenger/{challengerId}`             | 인증                        |
| A-4    | POINT-001      | POST   | `/api/v1/challenger/{challengerId}/points`      | `CHALLENGER_POINT:WRITE`  |
| A-5    | POINT-002      | PATCH  | `/api/v1/challenger/points/{challengerPointId}` | `CHALLENGER_POINT:EDIT`   |
| A-6    | POINT-003      | DELETE | `/api/v1/challenger/points/{challengerPointId}` | `CHALLENGER_POINT:DELETE` |
| A-보조-1 | CHALLENGER-102 | GET    | `/api/v1/challenger/search/cursor`              | 인증                        |
| A-보조-2 | CHALLENGER-103 | GET    | `/api/v1/challenger/search/offset`              | 인증                        |

## 플로우 B — 챌린저 기록 부여 및 조회

| #      | 코드                    | Method | Path                                           | 권한                        |
|--------|-----------------------|--------|------------------------------------------------|---------------------------|
| B-1    | CHALLENGER-001        | POST   | `/api/v1/challenger`                           | `CHALLENGER:WRITE`        |
| B-2    | CHALLENGER-002        | POST   | `/api/v1/challenger/batch`                     | `CHALLENGER:WRITE`        |
| B-3    | CHALLENGER-RECORD-002 | POST   | `/api/v1/challenger-record`                    | `CHALLENGER_RECORD:WRITE` |
| B-4    | CHALLENGER-RECORD-003 | POST   | `/api/v1/challenger-record/bulk`               | `CHALLENGER_RECORD:WRITE` |
| B-5    | CHALLENGER-RECORD-001 | POST   | `/api/v1/challenger-record/member`             | 인증                        |
| B-6    | CHALLENGER-RECORD-101 | GET    | `/api/v1/challenger-record/code/{code}`        | `CHALLENGER_RECORD:READ`  |
| B-7    | CHALLENGER-RECORD-102 | GET    | `/api/v1/challenger-record/id/{id}`            | `CHALLENGER_RECORD:READ`  |
| B-부록-1 | CHALLENGER-003        | POST   | `/api/v1/challenger/{challengerId}/deactivate` | `CHALLENGER:DELETE`       |
| B-부록-2 | CHALLENGER-004        | PATCH  | `/api/v1/challenger/{challengerId}/part`       | `CHALLENGER:EDIT`         |
| B-부록-3 | CHALLENGER-005        | DELETE | `/api/v1/challenger/{challengerId}`            | `CHALLENGER:DELETE`       |
