# 테크 블로그 댓글/좋아요 FE API 가이드

정적 프론트 콘텐츠(`blog`, `release`)의 slug 기반 좋아요, 댓글, 1단계 대댓글 API입니다.

## 공통 규칙

- Base URL: `/api/v1/tech-blog/contents/{type}/{slug}`
- `{type}`: `blog`, `release`만 허용합니다.
- `{slug}`: 프론트 콘텐츠 slug입니다. 서버 내부 PK와 별도로 저장됩니다.
- 성공 응답은 전역 `ApiResponse` envelope로 감싸져 `result` 아래에 실제 데이터가 들어갑니다.
- 댓글 content는 trim 후 1~1000자입니다.
- 비회원 댓글 nickname은 trim 후 1~20자입니다.
- 인증이 선택인 조회 API는 토큰이 있으면 `likedByMe`를 계산하고, 없으면 `false`로 내려갑니다.

## API 목록

| API ID | Method | Endpoint | 인증 | 설명 |
|---|---|---|---|---|
| TECH-BLOG-001 | GET | `/like` | 선택 | 콘텐츠 좋아요 수와 내 좋아요 여부 조회 |
| TECH-BLOG-002 | POST | `/like` | 필수 | 콘텐츠 좋아요 토글 |
| TECH-BLOG-003 | GET | `/comments` | 선택 | 최상위 댓글 cursor 조회와 1단계 대댓글 포함 조회 |
| TECH-BLOG-004 | POST | `/comments` | 선택 | 댓글 또는 1단계 대댓글 작성 |
| TECH-BLOG-005 | PATCH | `/comments/{commentId}` | 필수 | 본인 댓글 수정 |
| TECH-BLOG-006 | DELETE | `/comments/{commentId}` | 필수 | 본인/슈퍼 관리자 댓글 삭제 |
| TECH-BLOG-007 | POST | `/comments/{commentId}/like` | 필수 | 댓글 좋아요 토글 |

## 좋아요

### 콘텐츠 좋아요 상태 조회

```http
GET /api/v1/tech-blog/contents/blog/spring-boot-tips/like
```

```json
{
  "result": {
    "likedByMe": false,
    "likeCount": 12
  }
}
```

콘텐츠가 아직 DB에 없어도 `likedByMe=false`, `likeCount=0`을 반환합니다.

### 콘텐츠 좋아요 토글

```http
POST /api/v1/tech-blog/contents/blog/spring-boot-tips/like
Authorization: Bearer {accessToken}
```

```json
{
  "result": {
    "likedByMe": true,
    "likeCount": 13
  }
}
```

쓰기 시 콘텐츠가 없으면 서버가 lazy create합니다.

## 댓글 목록

```http
GET /api/v1/tech-blog/contents/blog/spring-boot-tips/comments?cursor=123&size=20&sort=createdAt,desc
```

### Query Parameter

| 이름 | 타입 | 기본값 | 설명 |
|---|---:|---:|---|
| cursor | long | 없음 | 이전 페이지의 `nextCursor`. 첫 페이지는 생략합니다. |
| size | int | 20 | 최상위 댓글 기준 페이지 크기입니다. 최대 50입니다. |
| sort | string | `createdAt,desc` | `createdAt,desc`, `createdAt,asc`만 지원합니다. |

`cursor`는 최상위 댓글 기준입니다. 각 최상위 댓글의 `replies`에는 1단계 대댓글이 포함되며, 대댓글은 페이지 크기에 포함되지 않습니다. 존재하지 않거나 현재 콘텐츠의 최상위 댓글이 아닌 `cursor`는 400으로 거부됩니다.

```json
{
  "result": {
    "content": [
      {
        "id": 123,
        "author": {
          "id": 1,
          "name": "홍길동",
          "nickname": "spring-master",
          "profileImageUrl": "https://example.com/profile.png"
        },
        "content": "좋은 글 감사합니다.",
        "createdAt": "2026-06-03T10:30:00Z",
        "likedByMe": false,
        "likeCount": 3,
        "deletionType": "NONE",
        "canReply": true,
        "canEdit": true,
        "canDelete": true,
        "replies": [
          {
            "id": 124,
            "author": {
              "id": null,
              "name": null,
              "nickname": "게스트",
              "profileImageUrl": null
            },
            "content": "저도 도움 됐습니다.",
            "createdAt": "2026-06-03T10:35:00Z",
            "likedByMe": false,
            "likeCount": 0,
            "deletionType": "NONE",
            "canReply": false,
            "canEdit": true,
            "canDelete": true,
            "replies": []
          }
        ]
      }
    ],
    "nextCursor": 123,
    "hasNext": true
  }
}
```

## 댓글 작성

```http
POST /api/v1/tech-blog/contents/blog/spring-boot-tips/comments
Content-Type: application/json
Authorization: Bearer {accessToken}
```

```json
{
  "content": "좋은 글 감사합니다.",
  "anonymous": false
}
```

비회원 작성은 토큰 없이 호출하고 `nickname`을 전달합니다.

```json
{
  "content": "좋은 글 감사합니다.",
  "anonymous": true,
  "nickname": "게스트"
}
```

대댓글 작성은 `parentCommentId`를 전달합니다.

```json
{
  "parentCommentId": 123,
  "content": "답글입니다.",
  "anonymous": false
}
```

대댓글은 1단계만 허용됩니다. 삭제된 부모 댓글에는 새 대댓글을 작성할 수 없습니다.

## 댓글 수정/삭제/좋아요

### 댓글 수정

```http
PATCH /api/v1/tech-blog/contents/blog/spring-boot-tips/comments/{commentId}
Authorization: Bearer {accessToken}
Content-Type: application/json
```

```json
{
  "content": "수정된 댓글입니다."
}
```

본인 댓글만 수정할 수 있습니다. 관리자 수정 API는 제공하지 않습니다.

### 댓글 삭제

```http
DELETE /api/v1/tech-blog/contents/blog/spring-boot-tips/comments/{commentId}
Authorization: Bearer {accessToken}
```

대댓글이 없는 댓글/답글은 hard delete되어 목록에서 사라집니다. 대댓글이 있는 최상위 댓글은 soft delete되고 placeholder로 내려갑니다.

댓글 본인인지 판단되면 `canEdit`/`canDelete`가 `true`로 내려가며, 슈퍼 관리자(`SUPER_ADMIN`)는 다른 사용자의 댓글에 대해 `canDelete`가 `true`로 내려갑니다.

### 댓글 좋아요 토글

```http
POST /api/v1/tech-blog/contents/blog/spring-boot-tips/comments/{commentId}/like
Authorization: Bearer {accessToken}
```

```json
{
  "result": {
    "likedByMe": true,
    "likeCount": 4
  }
}
```

삭제된 댓글에는 좋아요를 누를 수 없습니다.

## 삭제 댓글 응답 규칙

삭제된 댓글이 대댓글 유지를 위해 목록에 남는 경우 아래 규칙을 따릅니다.

| deletionType | content | author | likedByMe | likeCount | canReply | canEdit | canDelete |
|---|---|---|---:|---:|---:|---:|---:|
| USER_DELETED | `삭제된 댓글입니다` | 생략 | false | 0 | false | false | false |
| ADMIN_DELETED | `관리자에 의해서 삭제된 댓글입니다` | 생략 | false | 0 | false | false | false |

삭제된 부모 댓글의 모든 대댓글이 삭제되면 부모 댓글도 목록에서 더 이상 내려오지 않습니다.

## 주요 에러

| 상태 | 상황 |
|---:|---|
| 400 | 지원하지 않는 type, 잘못된 slug/content/nickname, invalid sort/cursor, 삭제된 부모에 대댓글 작성 |
| 401 | 인증 필수 API에서 토큰 누락 |
| 403 | 본인 댓글이 아닌 수정/삭제, 슈퍼 관리자 삭제 권한 없음 |
| 404 | 콘텐츠 또는 댓글을 찾을 수 없음 |
