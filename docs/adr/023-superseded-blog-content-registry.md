# ADR-023: 블로그 상호작용 대상 registry 유지 결정

## 상태

Superseded by [ADR-024](024-introduce-blog-cms.md)

## 설명

초기에는 프론트엔드 정적 블로그 콘텐츠를 서버가 저장하지 않고, 댓글/좋아요 대상 식별만을 위해 `BlogContent` registry를 유지하기로 결정했다.

이후 요구가 변경되어 서버가 본문, 시리즈, 해시태그, 작성자, 공개 상태를 관리하는 CMS 책임을 갖게 되었다. 따라서 본 ADR의 핵심 결정인 “본문 없는 interaction target registry” 모델은 더 이상 적용하지 않는다.
