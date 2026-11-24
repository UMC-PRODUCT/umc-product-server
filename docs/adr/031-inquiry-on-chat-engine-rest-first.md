# ADR-031: 문의 도메인을 Chat 엔진 소비 도메인으로 만들고 1차는 REST로 연다

## 상태

Accepted. [ADR-011](011-inquiry-domain-with-websocket-stomp.md)을 대체한다.

## 배경

UMC Desk(macOS)에 UMC PRODUCT 팀 문의 기능을 넣는다. 사용자가 문의를 남기면 운영진(`UMC_PRODUCT` 또는
`SUPER_ADMIN` 시스템 역할)이 답하고, 같은 문의 안에서 대화를 이어 간다. 나중에 모바일 앱도 같은 API를 쓸 수 있다.

ADR-011은 문의 전용 메시지 테이블과 STOMP 구독을 전제로 했다. 그 뒤 ADR-026에서 Chat 엔진(방·메시지·첨부·멱등)과
소비 도메인(리소스 권한·상태·외부 계약)의 책임을 나눴고, AUTHZ-10에서 문의 답변·관리 권한을 `UMC_PRODUCT` 시스템
역할로 정했다. 이번 결정은 그 둘을 문의에 적용한다.

전체 기능은 4단계로 나눈다. 1단계 문의 접수와 대화, 2단계 담당자·라벨·운영 목록, 3단계 FAQ, 4단계 인기 순위다.
이 ADR은 1단계만 결정한다.

## 결정

1. `inquiry` 도메인은 Chat 엔진의 소비 도메인이다. 문의 한 건이 Chat 방 하나(`inquiry.chat_room_id`, unique)를
   갖고, 대화 본문과 첨부는 Chat 메시지로 저장한다. 첫 메시지가 문의 내용이다. 메시지 수정·삭제·리액션·멘션·답장은
   열지 않는다. inquiry는 Chat의 공개 UseCase와 DTO만 쓰고 Chat 코드는 바꾸지 않는다.
2. 상태는 `Inquiry` 엔티티의 메서드로만 바뀌고, 전이 규칙은 `InquiryStatus.afterMessageFrom` 한 곳에 둔다.

   | 현재 상태 | 운영진 메시지 | 작성자 메시지 | 종료(운영진만) |
   | --- | --- | --- | --- |
   | `RECEIVED` | `IN_PROGRESS` | `RECEIVED` | `CLOSED` |
   | `IN_PROGRESS` | `IN_PROGRESS` | `IN_PROGRESS` | `CLOSED` |
   | `CLOSED` | `IN_PROGRESS` | `IN_PROGRESS` | 409 `INQUIRY_ALREADY_CLOSED` |

   다시 열리면 `closed_at`을 비우고, 메시지마다 `last_message_at`을 갱신한다. 운영진이 자기 문의에 쓰면 작성자로 본다.
3. 담당 팀(`InquiryTeam`, null은 "모르겠어요")은 표시와 필터에만 쓴다. 열람 권한은 "작성자 또는 운영진" 하나뿐이고,
   운영진 판정은 `CheckChallengerAuthorityUseCase.isUmcProduct`로만 한다.
4. 문의 방은 Chat 조회 범위 `PUBLIC`으로 만든다. inquiry가 먼저 작성자 또는 운영진인지 확인한 뒤 엔진 조회를 부른다.
   2026-11-24 기준 Chat 방을 읽는 경로는 모두 소비 도메인을 거친다. Chat에는 inbound adapter가 없고, GraphQL 스키마에
   Chat이 없으며, Community의 조회·신고·실시간 relay는 `chatRoomId`를 `CommunityThread`로 찾지 못하면 멈춘다.
   그래서 운영진이 읽기만 할 때 방 멤버로 넣는 쓰기를 GET에 만들 필요가 없다. 운영진이 처음 메시지를 보낼 때만
   `JoinChatRoomUseCase`로 조용히 넣는다. 문의 행을 비관적 잠금한 뒤 멤버 여부를 먼저 확인하므로 같은 방 join이 겹치지
   않는다. Chat의 `CHAT_MEMBER_ALREADY_EXISTS`를 잡아 넘기면 바깥 트랜잭션이 rollback-only가 되므로 예외에 기대지 않는다.
   Chat 방을 `chatRoomId`만으로 읽는 새 경로를 만들면 이 결정을 다시 봐야 한다.
5. 등록은 `unique(author_member_id, client_request_id)`로, 메시지는 Chat 엔진의 `clientMessageId` dedup으로 멱등이다.
   dedup 결과면 상태 전이와 알림을 다시 하지 않는다. 같은 등록 요청이 동시에 오면 늦은 쪽은 409를 받고, 재시도하면
   기존 문의를 받는다.
6. 1차 전달은 REST, 커서 조회, FCM이다. 운영진의 중복 아닌 메시지는 `inquiry.staff.replied` 이벤트를 outbox에 남기고,
   커밋 뒤 notification 리스너가 작성자에게 푸시한다(`data.type=INQUIRY_REPLY`, `data.inquiryId`,
   딥링크 `umc://inquiry/{inquiryId}`). 작성자 메시지는 이번 단계에서 알리지 않는다. API·딥링크·푸시 데이터에
   플랫폼 구분을 넣지 않는다.
7. 첨부는 `FileCategory.INQUIRY_ATTACHMENT`(`private/inquiry`)로 올린, 보낸 사람 본인의 업로드 완료 파일만 받는다.
   첨부 링크는 문의 메시지 응답에서만 만들어지므로 문의 접근 정책이 곧 첨부 열람 권한이다.
   메시지 종류는 첨부 구성으로 정한다. 첨부가 없으면 `TEXT`, 모두 이미지면 `IMAGE`, 그 외는 `FILE`이고, Chat 공통
   첨부 검증(`ValidateChatAttachmentsUseCase`)을 거친다. Chat 메시지 하나는 한 종류만 담으므로 이미지와 문서를 섞은
   첨부는 400 `INQUIRY_ATTACHMENT_MIXED_TYPES`로 거절한다.
8. 카테고리는 `inquiry_category` 테이블에 두고 관리 API 없이 SQL로 추가·비활성화한다.

## 후속

- 실시간(STOMP) 전달은 2차에서 Community처럼 per-member fan-out으로 붙인다. 그때 구독 권한도 4번 정책을 쓴다.
- 2단계(담당자·라벨·운영 목록), 3단계(FAQ), 4단계(인기 순위)는 별도 결정으로 다룬다.
