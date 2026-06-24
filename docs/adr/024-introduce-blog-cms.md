# ADR-024: Blog CMS를 서버 도메인으로 도입한다

## 상태

Accepted

## 배경

기존 블로그 댓글/좋아요 기능은 프론트엔드 정적 파일의 `type + slug`를 서버 내부 registry로 정규화하는 구조였다. 하지만 블로그 본문 CRUD, 작성자 정보, 시리즈, 해시태그, SEO path 제공 요구가 추가되면서 서버가 콘텐츠 원본을 관리해야 한다.

## 결정

서버에 `blog` 도메인을 도입하고, 기존 `blog` 명칭은 제거한다.

1. Java package, class prefix, DB table, API path, 권한 리소스는 모두 `blog` 기준으로 통일한다.
2. `BlogContent`는 본문을 포함한 CMS 게시글 aggregate가 된다.
3. `BlogContent.slug`는 SEO canonical URL의 기준이므로 유지한다.
4. `BlogSeries`는 별도 aggregate로 두고, 게시글과 시리즈는 `BlogSeriesContent` join entity로 다대다 연결한다.
5. `BlogHashtag`는 global unique master entity로 두고, 게시글과 해시태그는 `BlogContentHashtag` join entity로 연결한다.
6. 기존 댓글/좋아요는 `BlogContent.id`를 기준으로 유지한다.
7. public 조회는 `PUBLISHED` 콘텐츠와 공개 게시글이 존재하는 시리즈/해시태그만 노출한다.
8. 백엔드는 SEO용 HTML을 직접 생성하지 않는다. FE가 public API와 `/api/v1/blog/seo/paths`를 이용해 SSG/SSR을 수행한다.

## 이유

- 게시글 slug는 canonical URL과 외부 공유 링크의 안정성을 보장한다.
- 시리즈 slug와 해시태그 slug는 landing page URL을 제공하지만, 게시글 canonical을 대체하지 않는다.
- `@OneToMany` 컬렉션 없이 join entity를 두면 기존 Hexagonal/DDD 규칙을 지키면서도 다대다 연결과 순서 관리를 명확히 표현할 수 있다.
- 댓글/좋아요 테이블은 `content_id` FK를 유지하면 기존 집계, cursor pagination, soft-delete 댓글 정책을 재사용할 수 있다.
- FE SSG/SSR 책임을 분리하면 API 서버가 프론트 빌드/배포 파이프라인에 결합되지 않는다.

## 결과

- `/api/v1/blog` backward compatibility는 제공하지 않는다. feature가 develop에 병합되기 전이므로 `/api/v1/blog`만 지원한다.
- 콘텐츠 생성은 `SUPER_ADMIN`, 수정은 작성자, 삭제는 작성자 또는 `SUPER_ADMIN`으로 제한한다.
- 시리즈도 동일한 권한 정책을 따른다.
- 해시태그는 별도 CRUD 없이 콘텐츠 저장 시 upsert한다.
- 복구 API와 FE revalidation webhook은 이번 범위에서 제외한다.
