# 블로그 FE API 가이드

## 개요

- Base URL: `/api/v1/blog`
- public 조회는 인증 없이 호출할 수 있습니다.
- CMS 쓰기 API는 인증이 필요합니다.
- 콘텐츠 생성은 `SUPER_ADMIN`만 가능합니다.
- 콘텐츠 수정은 작성자 본인만 가능합니다.
- 콘텐츠 삭제는 작성자 본인 또는 `SUPER_ADMIN`이 가능합니다.
- 댓글/좋아요는 `PUBLISHED` 콘텐츠에서만 작성/토글할 수 있습니다.
- FE는 public API를 기반으로 SSG/SSR을 수행합니다. 백엔드는 정적 HTML을 직접 생성하거나 배포하지 않습니다.

## 콘텐츠

### 공개 목록

```http
GET /api/v1/blog/contents?type=engineering&seriesSlug=spring&hashtagSlug=springboot&cursor=10&size=20&sort=publishedAt,desc
```

- `type`: `engineering`, `design`, `product`, `release`; 생략 가능
- `seriesSlug`: 특정 시리즈 필터; 생략 가능
- `hashtagSlug`: 특정 해시태그 필터; 생략 가능
- `size`: 기본 20, 최대 50
- `sort`: `publishedAt,desc`, `publishedAt,asc`
- 응답은 `PUBLISHED` 콘텐츠만 포함합니다.

### 공개 상세

```http
GET /api/v1/blog/contents/{type}/{slug}
```

- `DRAFT`, `DELETED` 콘텐츠는 404로 응답합니다.
- 응답에는 `canonicalPath`, `seoTitle`, `seoDescription`, `ogImageUrl`, `series`, `hashtags`가 포함됩니다.

### CMS 생성

```http
POST /api/v1/blog/contents
```

```json
{
  "type": "engineering",
  "slug": "spring-boot-tips",
  "title": "Spring Boot Tips",
  "summary": "Spring Boot 운영 팁",
  "thumbnailUrl": "https://example.com/og.png",
  "content": "# 본문",
  "status": "DRAFT",
  "seoTitle": "Spring Boot Tips",
  "seoDescription": "Spring Boot 운영 팁",
  "ogImageUrl": "https://example.com/og.png",
  "hashtags": ["spring", "springboot"]
}
```

- `status` 생략 시 `DRAFT`입니다.
- `hashtags`는 최대 10개입니다. 서버는 `#` 제거, trim, NFKC 정규화, lowercase 기준으로 중복을 제거합니다.

### CMS preview / 수정 / 삭제

```http
GET /api/v1/blog/contents/{contentId}/preview
PATCH /api/v1/blog/contents/{contentId}
DELETE /api/v1/blog/contents/{contentId}
```

- preview는 작성자 또는 `SUPER_ADMIN`만 가능합니다.
- 수정은 작성자 본인만 가능합니다.
- 삭제는 작성자 본인 또는 `SUPER_ADMIN`만 가능합니다.
- 삭제는 soft delete입니다.

## 시리즈

### 공개 목록 / 상세 / 콘텐츠 목록

```http
GET /api/v1/blog/series?type=engineering&cursor=10&size=20&sort=createdAt,desc
GET /api/v1/blog/series/{type}/{slug}
GET /api/v1/blog/series/{type}/{slug}/contents?cursor=10&size=20&sort=displayOrder,asc
```

- 시리즈 자체에는 공개 상태가 없습니다.
- 삭제되지 않았고 공개 콘텐츠가 1개 이상 있는 시리즈만 public에 노출됩니다.
- 시리즈 콘텐츠 목록은 `displayOrder,asc`만 지원합니다.

### CMS 생성 / 수정 / 삭제 / 콘텐츠 교체

```http
POST /api/v1/blog/series
PATCH /api/v1/blog/series/{seriesId}
DELETE /api/v1/blog/series/{seriesId}
PUT /api/v1/blog/series/{seriesId}/contents
```

```json
{
  "contentIds": [10, 12, 15]
}
```

- 시리즈 생성은 `SUPER_ADMIN`만 가능합니다.
- 수정과 콘텐츠 교체는 시리즈 작성자 본인만 가능합니다.
- 삭제는 작성자 본인 또는 `SUPER_ADMIN`이 가능합니다.
- 한 게시글은 여러 시리즈에 포함될 수 있습니다.
- 시리즈와 게시글의 `type`이 다르면 연결이 거부됩니다.

## 해시태그

```http
GET /api/v1/blog/hashtags?type=engineering&q=spring&cursor=10&size=20&sort=contentCount,desc
GET /api/v1/blog/hashtags/{slug}/contents?type=engineering&cursor=10&size=20&sort=publishedAt,desc
```

- 별도 hashtag CRUD는 없습니다.
- 콘텐츠 생성/수정 시 `hashtags` 배열로 upsert됩니다.
- public hashtag 목록과 hashtag 콘텐츠 목록은 공개 콘텐츠 기준입니다.

## SEO

```http
GET /api/v1/blog/seo/paths
```

```json
{
  "paths": [
    {
      "type": "content",
      "path": "/engineering/spring-boot-tips",
      "updatedAt": "2026-06-13T12:00:00Z"
    },
    {
      "type": "series",
      "path": "/series/engineering/spring",
      "updatedAt": "2026-06-13T12:00:00Z"
    },
    {
      "type": "hashtag",
      "path": "/hashtags/springboot",
      "updatedAt": "2026-06-13T12:00:00Z"
    }
  ]
}
```

- FE SSG/SSR 빌드는 이 path 목록과 상세 API를 이용해 정적 페이지 또는 SSR 페이지를 생성합니다.
- canonical URL은 항상 content slug 기준입니다. 시리즈/해시태그 페이지에서 같은 게시글을 노출하더라도 게시글 canonical은 `/engineering/{slug}`, `/design/{slug}`, `/product/{slug}`, `/release/{slug}`입니다.

## 댓글/좋아요

```http
GET /api/v1/blog/contents/{type}/{slug}/like
POST /api/v1/blog/contents/{type}/{slug}/like
GET /api/v1/blog/contents/{type}/{slug}/comments?cursor=&size=&sort=createdAt,desc
POST /api/v1/blog/contents/{type}/{slug}/comments
PATCH /api/v1/blog/contents/{type}/{slug}/comments/{commentId}
DELETE /api/v1/blog/contents/{type}/{slug}/comments/{commentId}
POST /api/v1/blog/contents/{type}/{slug}/comments/{commentId}/like
```

- 댓글 목록은 최상위 댓글 기준 cursor pagination입니다.
- 각 최상위 댓글에는 1단계 대댓글이 포함됩니다.
- 삭제된 부모 댓글은 대댓글이 있으면 placeholder로 노출됩니다.
- placeholder content:
  - 본인 삭제: `삭제된 댓글입니다`
  - 관리자 삭제: `관리자에 의해서 삭제된 댓글입니다`
- 댓글 응답에는 `deletionType`, `canReply`, `canEdit`, `canDelete`가 포함됩니다.
- 삭제된 댓글은 `author=null`, `likedByMe=false`, `likeCount=0`, `canReply=false`, `canEdit=false`, `canDelete=false`입니다.
