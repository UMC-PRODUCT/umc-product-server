# Figma Domain

## 역할

`figma` 도메인은 Figma OAuth 연결, 감시 파일 등록, 댓글 동기화, 댓글 분류, Discord 멘션 라우팅, 댓글 요약을 담당한다.

## 책임

- Figma 연결 정보를 등록하고 갱신한다.
- 감시할 Figma 파일과 라우팅 도메인을 관리한다.
- Figma 댓글을 동기화하고 도메인별로 분류한다.
- Discord 멘션과 댓글 요약을 생성한다.

## 경계

Figma와 Discord는 외부 시스템이다. 외부 API 실패는 도메인 예외로 감싸고, 재시도 가능 여부를 메시지에 드러낸다. 내부 토큰이나 암호화 세부 구현은 사용자 문구에 노출하지 않는다.

## UX Writing Notes

`access token`, `state`, `domain_key` 같은 기술 용어는 운영자가 식별해야 할 때만 쓴다. 일반 오류는 `Figma 연결이 만료됐어요. 다시 연결해주세요`처럼 원인과 행동을 나눠 쓴다.
