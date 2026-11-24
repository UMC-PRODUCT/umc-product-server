# Inquiry Domain

## 역할

`inquiry` 도메인은 UMC PRODUCT 팀 문의의 접수, 상태, 작성자와 운영진 사이의 대화를 관리한다.
설계 결정은 [ADR-031](../../adr/031-inquiry-on-chat-engine-rest-first.md)에 있다. 실시간 수신은 ADR-031 9번이다.

## 책임

- 문의 유형(`inquiry_category`)을 제공한다. 유형은 관리 API 없이 SQL로 추가하거나 비활성화한다.
- 문의를 등록하고 `RECEIVED`, `IN_PROGRESS`, `CLOSED` 상태를 바꾼다.
- 작성자와 운영진(`UMC_PRODUCT` 또는 `SUPER_ADMIN`)만 문의를 읽고 쓰게 한다.
- 운영진이 답하면 작성자 푸시용 이벤트를 남긴다.
- 문의 화면을 연 작성자·운영진에게 새 메시지와 상태 변화를 STOMP `/topic/inquiries/{inquiryId}`로 보낸다.
  보내기는 REST만 받는다.

## 실시간 수신

| 항목 | 내용 |
| --- | --- |
| 구독 주소 | `/topic/inquiries/{inquiryId}` (연결은 커뮤니티와 같은 `/ws/websocket`) |
| 구독 권한 | 작성자 또는 운영진. `InquiryStompSubscriptionAuthorizer`가 REST와 같은 `InquiryAccessPolicy`로 판정 |
| `MESSAGE_CREATED` | `inquiryId`, `message`(REST 메시지 응답과 같은 필드) |
| `STATUS_CHANGED` | `inquiryId`, `status`, `closedAt`, `lastMessageAt`, `occurredAt`. 운영진 첫 답변, 종료, 재오픈 때. 이미 반영한 것보다 `occurredAt`이 오래되면 버린다 |
| 보내지 않는 경우 | 같은 `clientMessageId` 재전송, 상태가 그대로인 메시지 |
| 재연결 | 다시 구독한 뒤 REST 메시지 첫 페이지와 상세를 다시 불러 `messageId`로 합친다 |

흐름은 `ChatMessageCreatedEvent`·`InquiryStatusChangedEvent` → `InquiryRealtimeEventListener` →
`InquiryRealtimeRelayService` → `BroadcastPort`다. 둘 다 outbox REALTIME 그룹에서 커밋 뒤 보낸다.

## 경계

대화 본문과 첨부는 Chat 엔진 메시지로 저장한다. inquiry는 Chat의 공개 UseCase만 쓰고, 외부 응답에 `chatRoomId`를
내보내지 않는다. 푸시 발송은 notification 도메인의 `InquiryAlarmEventListener`가 맡는다. 첨부 파일은
storage의 `INQUIRY_ATTACHMENT` 카테고리로 올린 파일만 받는다.

## UX Writing Notes

권한 오류는 누가 할 수 있는지 알려 준다(`UMC PRODUCT 운영진만 할 수 있어요`). 상태 충돌은 새로고침을 안내한다.
