# ADR-023: 테크 블로그 상호작용 대상은 TechBlogContent로 정규화한다

## Status

Proposed

## Context

2026년 6월 기준 테크 블로그의 본문 콘텐츠는 프론트엔드 정적 파일로 운영된다. 백엔드는 글의 제목, 본문, 발행 상태, 파일 경로를 관리하지 않는다.

이번 PR에서는 정적 콘텐츠에 대한 댓글, 대댓글, 콘텐츠 좋아요, 댓글 좋아요를 `techblog` 도메인으로 추가한다.

- 공개 API: `GET /api/v1/tech-blog/contents/{type}/{slug}/like`
- 공개 API: `GET /api/v1/tech-blog/contents/{type}/{slug}/comments?cursor=&size=&sort=createdAt,desc`
- 쓰기 API: `POST /api/v1/tech-blog/contents/{type}/{slug}/like`
- 쓰기 API: `POST /api/v1/tech-blog/contents/{type}/{slug}/comments`
- 쓰기 API: `PATCH /api/v1/tech-blog/contents/{type}/{slug}/comments/{commentId}`
- 쓰기 API: `DELETE /api/v1/tech-blog/contents/{type}/{slug}/comments/{commentId}`
- 쓰기 API: `POST /api/v1/tech-blog/contents/{type}/{slug}/comments/{commentId}/like`

서버는 `type + slug` 조합을 외부 식별자로 받고, 내부에서는 `tech_blog_content.id`를 댓글/좋아요의 대상 식별자로 사용한다.

- 도메인: `com.umc.product.techblog.domain.TechBlogContent`
- 댓글 테이블: `tech_blog_comment.content_id`
- 콘텐츠 좋아요 테이블: `tech_blog_content_like.content_id`
- 콘텐츠 식별 unique key: `tech_blog_content(content_type, slug)`

이 프로젝트는 Hexagonal Architecture를 따른다. 도메인은 다른 도메인의 Aggregate를 직접 참조하지 않고 ID로 경계를 유지해야 한다. 테크 블로그의 프론트엔드 정적 파일 역시 백엔드 Aggregate가 아니므로, 백엔드는 정적 파일의 본문 모델을 소유하지 않는다.

### 문제점

1. FE 정적 파일과 서버 도메인 모델의 경계가 혼동될 수 있다. `TechBlogContent`라는 이름만 보면 서버가 블로그 본문을 소유하는 것처럼 보이지만, 실제 책임은 `content_type + slug`를 내부 interaction target id로 canonicalize하는 것이다.
2. 댓글과 좋아요는 동일한 대상에 붙어야 한다. `tech_blog_comment`, `tech_blog_content_like`, `tech_blog_comment_like`가 각자 `content_type + slug`를 들고 있으면 같은 대상 식별 규칙이 여러 테이블과 쿼리에 반복된다.
3. slug 직접 저장 방식은 초기 구현은 단순하지만, 댓글/좋아요 데이터가 늘수록 text key 기반 인덱스와 unique key가 반복된다. `BIGINT content_id` 기반 인덱스보다 크고 비교 비용이 높다.
4. 정적 콘텐츠 자체는 FE가 관리하더라도, 서버의 상호작용 대상에는 운영 상태가 붙을 수 있다. 예를 들어 댓글 잠금, 숨김, 신고 기반 차단, slug migration, 특정 글의 interaction 비활성화 같은 상태는 FE 정적 파일과 별도로 서버가 관리할 수 있다.

### 결정이 필요한 이유

테크 블로그 댓글/좋아요 기능은 초기에는 작게 보이지만, cursor pagination, soft delete placeholder, 댓글 좋아요 집계, 콘텐츠 좋아요 집계, 권한 기반 삭제를 함께 가진다. 이 기능들이 모두 같은 콘텐츠 대상을 기준으로 묶이므로, 대상 식별 방식을 PR 단계에서 명확히 결정해야 한다.

이 결정을 남기지 않으면 미래의 독자는 “FE 정적 파일인데 왜 백엔드에 `TechBlogContent` row가 있는가?”라는 질문을 다시 하게 된다. 이 ADR은 `TechBlogContent`를 FE 콘텐츠 복제본이 아니라 서버 상호작용 대상 registry로 유지하는 이유를 기록한다.

## Decision

우리는 테크 블로그 댓글/좋아요의 대상 식별자를 `TechBlogContent`로 정규화해서 유지하기로 결정한다.

1. `TechBlogContent`는 FE 정적 콘텐츠의 본문 모델이 아니라 interaction target registry로 사용한다.
2. 외부 API는 계속 `{type}/{slug}`를 사용한다. `type`은 `blog`, `release`만 허용한다.
3. 쓰기 요청에서 `type + slug`에 해당하는 `TechBlogContent`가 없으면 lazy create한다.
4. 댓글과 콘텐츠 좋아요는 `content_type + slug`를 직접 저장하지 않고 `content_id`를 FK로 저장한다.
5. `tech_blog_content(content_type, slug)` unique key를 통해 외부 식별자의 정합성을 보장한다.
6. 현재 이름 `TechBlogContent`는 유지한다. 단, 문서와 코드 주석에서는 “본문 콘텐츠 모델”이 아니라 “상호작용 대상”이라는 의미를 명확히 한다.

### 단계적 진행 / PR 분할

- **Phase 1 (이 PR / 본 ADR)**: `TechBlogContent`를 유지하고 댓글/좋아요 테이블이 `content_id`를 참조하도록 구현한다.
- **Phase 2 (별도 PR, 필요 시)**: 운영 요구가 생기면 `TechBlogContent`에 `comment_closed`, `hidden`, `migrated_slug` 같은 interaction metadata를 추가한다.
- **Phase 3 (별도 PR, 필요 시)**: 정적 콘텐츠 배포 파이프라인과 서버 registry를 연결해야 할 경우 slug 등록/검증 자동화를 별도로 설계한다.

## Alternatives Considered

### 대안 A: 댓글과 좋아요 테이블에 `content_type + slug` 직접 저장

`tech_blog_comment`, `tech_blog_content_like`가 각각 `content_type`, `slug`를 직접 컬럼으로 갖는 방식이다.

장점:

- 별도 `tech_blog_content` 테이블이 필요 없다.
- 외부 API의 `{type}/{slug}`와 DB 컬럼이 바로 대응되어 직관적이다.
- 최초 구현에서 lazy create 로직이 줄어든다.

단점:

- 댓글 조회 인덱스가 `(content_type, slug, parent_comment_id, created_at, id)`처럼 길어진다.
- 콘텐츠 좋아요 unique key도 `(content_type, slug, member_id)`가 되어 text key가 반복된다.
- 댓글과 콘텐츠 좋아요가 같은 대상 식별 규칙을 각자 보관한다.
- slug 관련 제약, 정규화, migration 정책이 여러 테이블에 퍼진다.
- FK/cascade로 상호작용 데이터를 한 번에 정리하기 어렵다.

선택하지 않은 이유:

- 기능이 댓글 하나만이면 충분히 가능한 선택이지만, 콘텐츠 좋아요와 댓글/대댓글/댓글 좋아요가 함께 들어오면서 대상 식별 규칙을 한 곳에 모으는 편이 더 안정적이다. 특히 text key를 여러 인덱스와 unique key에 반복하는 비용을 피하고 싶다.

### 대안 B: `TechBlogContent` 없이 댓글 테이블만 target registry처럼 사용

최초 댓글이 생성될 때 `tech_blog_comment`에 `content_type + slug`를 저장하고, 콘텐츠 좋아요는 별도 `content_type + slug` 기준으로 관리하는 방식이다.

장점:

- 댓글만 기준으로 보면 테이블 수가 줄어든다.
- 댓글이 없는 콘텐츠에 대한 row를 만들지 않아도 된다.

단점:

- 댓글이 없지만 좋아요만 있는 콘텐츠를 표현하기 어렵다.
- 댓글과 콘텐츠 좋아요가 같은 target id를 공유하지 못한다.
- 콘텐츠 좋아요 조회가 댓글 존재 여부와 무관해야 하는 현재 API와 맞지 않는다.
- 상호작용 대상 상태를 붙일 공통 위치가 없다.

선택하지 않은 이유:

- `GET /like`와 `POST /like`는 댓글 존재 여부와 독립적으로 동작해야 한다. 댓글 테이블을 target registry로 겸용하면 좋아요만 있는 콘텐츠의 상태 관리가 어색해진다.

### 대안 C: `TechBlogContent`를 서버 콘텐츠 CMS 모델로 확장

`TechBlogContent`에 제목, 본문, 발행 상태, 파일 경로 등을 추가해 서버가 테크 블로그 콘텐츠를 직접 소유하는 방식이다.

장점:

- 서버가 콘텐츠 상태를 완전히 통제할 수 있다.
- slug 검증, 공개 여부, 발행일, 검색 메타데이터를 한 곳에서 관리할 수 있다.
- 향후 CMS로 전환하기 쉽다.

단점:

- 2026년 6월 기준 운영 방식인 FE 정적 파일 관리와 충돌한다.
- 백엔드가 필요 이상의 콘텐츠 책임을 갖게 된다.
- 정적 파일 배포와 DB 상태 동기화 문제가 생긴다.

선택하지 않은 이유:

- 현재 요구는 정적 FE 콘텐츠에 상호작용을 붙이는 것이다. 서버가 본문 콘텐츠를 소유하는 CMS 모델은 문제 범위를 과하게 넓힌다.

### 대안 D: `TechBlogContent`를 interaction target registry로 유지

`TechBlogContent`는 `content_type + slug`와 내부 PK만 관리하고, 댓글/좋아요는 `content_id`로 참조하는 방식이다.

장점:

- 댓글/좋아요가 동일한 대상 id를 공유한다.
- `BIGINT content_id` 기반 FK, index, unique key를 사용할 수 있다.
- `content_type + slug` unique 보장이 한 테이블에 모인다.
- cascade delete와 운영 상태 확장이 단순하다.
- FE 정적 콘텐츠의 본문 소유권을 백엔드로 가져오지 않는다.

단점:

- 실제 본문을 저장하지 않는 `TechBlogContent` row가 존재하므로 이름만 보면 오해가 생길 수 있다.
- 쓰기 시 lazy create가 필요하다.
- `type + slug`를 `content_id`로 변환하기 위한 조회가 한 번 추가된다.

선택한 이유:

- 현재 요구의 핵심은 “정적 콘텐츠 본문 관리”가 아니라 “정적 콘텐츠를 대상으로 한 상호작용 관리”다. `TechBlogContent`를 registry로 두면 FE와 BE의 책임 경계를 유지하면서도 댓글/좋아요 저장 모델을 단순하게 유지할 수 있다.

## Consequences

### Positive

- 댓글, 대댓글, 콘텐츠 좋아요가 같은 `content_id`를 기준으로 묶인다.
- `tech_blog_comment` 조회 인덱스는 `content_id, parent_comment_id, created_at, id` 중심으로 구성할 수 있다.
- `tech_blog_content_like` unique key는 `content_id + member_id`로 짧게 유지된다.
- `content_type + slug` 정합성은 `tech_blog_content` unique key 하나로 관리된다.
- 향후 댓글 잠금, 숨김, slug migration 같은 서버 상호작용 상태를 붙일 수 있는 anchor가 있다.

### Negative

- `TechBlogContent`가 본문 없는 registry row라는 점을 문서로 설명해야 한다.
- 단순 slug 직접 저장 방식보다 테이블이 하나 더 많다.
- 최초 쓰기 시 `type + slug`에 대한 lazy create 경로가 필요하며, 동시 최초 쓰기에 대한 unique 충돌 방어가 필요하다.

### Neutral / Trade-offs

- slug 변경을 쉽게 하기 위한 결정만은 아니다. slug 변경은 부가 효과이며, 핵심은 상호작용 대상의 canonical id를 만드는 것이다.
- 서버가 `TechBlogContent` row를 갖지만, FE 정적 파일의 존재 여부를 강하게 검증하지는 않는다. 존재하지 않는 slug에도 사용자가 쓰기 요청을 보내면 registry row가 생길 수 있다. 필요하면 Phase 3에서 정적 콘텐츠 manifest 검증을 추가한다.
- 이름은 `TechBlogContent`로 유지한다. `TechBlogContentRef`나 `TechBlogInteractionTarget`이 의미상 더 직접적일 수 있지만, 팀의 현재 도메인 네이밍과 API 문맥에서는 `TechBlogContent`가 더 자연스럽다.

## Implementation Notes

### 변경 영역 요약

1. **도메인** (`com.umc.product.techblog.domain.*`): `TechBlogContent`는 `contentType`, `slug`, 내부 `id`를 가진다. 본문/제목/발행 상태는 갖지 않는다.
2. **응용 / Port** (`...application.service.*`, `...application.port.*`): 쓰기 UseCase는 `type + slug`로 `TechBlogContent`를 조회하고, 없으면 lazy create한 뒤 `content_id`로 댓글/좋아요를 저장한다.
3. **어댑터 (in)** (`...adapter.in.web.*`): API path는 계속 `/api/v1/tech-blog/contents/{type}/{slug}`를 사용한다.
4. **어댑터 (out)** (`...adapter.out.persistence.*`): `TechBlogPersistenceAdapter`는 `content_id` 기반으로 댓글/좋아요를 저장하고 조회한다. 동시 최초 생성은 `INSERT ... ON CONFLICT DO NOTHING`으로 방어한다.
5. **DB / 마이그레이션** (`src/main/resources/db/migration/V2026.06.03.03.00__create_tech_blog_interactions.sql`): `tech_blog_content`, `tech_blog_content_like`, `tech_blog_comment`, `tech_blog_comment_like`를 생성한다.
6. **테스트** (`src/test/...`): 없는 콘텐츠 조회는 빈 상태를 반환하고, 쓰기 시 lazy create되는 흐름을 통합 테스트로 고정한다.

### 기타 참고

- `TechBlogContent` row는 FE 정적 파일의 존재 증명이 아니다. 상호작용 저장을 위한 서버 내부 target registry다.
- FE 정적 콘텐츠 manifest와 서버 registry를 강하게 연결하려면 별도 결정이 필요하다.
- slug 직접 저장 방식으로 되돌릴 경우 댓글/좋아요 테이블의 FK, unique key, 조회 index, cursor query를 함께 재설계해야 한다.

## References

- PR: `#933`
- 관련 코드: `src/main/java/com/umc/product/techblog/domain/TechBlogContent.java`
- 관련 migration: `src/main/resources/db/migration/V2026.06.03.03.00__create_tech_blog_interactions.sql`
