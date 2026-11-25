# Inquiry Domain

## 역할

`inquiry` 도메인은 UMC PRODUCT 팀 문의의 접수, 상태, 작성자와 운영진 사이의 대화를 관리한다.
설계 결정은 [ADR-031](../../adr/031-inquiry-on-chat-engine-rest-first.md)에 있다. 실시간 수신은 ADR-031 9번이다.

## 책임

- 문의 유형(`inquiry_category`)을 제공한다. 유형은 관리 API 없이 SQL로 추가하거나 비활성화한다.
- 문의를 등록하고 `RECEIVED`, `IN_PROGRESS`, `CLOSED` 상태를 바꾼다.
- 작성자와 운영진(`UMC_PRODUCT` 또는 `SUPER_ADMIN`)만 문의를 읽고 쓰게 한다.
- 운영진이 답하면 작성자 푸시용 이벤트를 남긴다.
- 문의한 사람에게는 운영진 메시지를 "UMC PRODUCT" 한 이름으로 보여 주고, 운영진끼리는 실명으로 보여 준다(ADR-031 11번).
- 새 문의는 알림을 켠 운영진에게, 작성자의 이어지는 메시지는 그 문의에 답한 운영진에게 푸시한다(ADR-031 10번).
  운영진은 자기 새 문의 알림을 `GET`·`PUT /api/v1/inquiries/admin/alarm-setting`으로 켜고 끈다. 기본값은 꺼짐이다.
- 작성자와 운영진에게 새 메시지와 상태 변화를 STOMP 사람별 큐 `/user/queue/inquiries/events`로 보낸다.
  보내기는 REST만 받는다.

## 배포와 운영

- 1단계 문의 API(#1603)와 운영진 표시·알림(#1604)은 같은 배포로 내보낸다. #1603만 나가면 질문자에게 운영진 이름과
  회원 ID가 보인다.
- 설정 화면이 나오기 전에는 새 문의 알림을 받으려는 운영진이 Swagger에서
  `PUT /api/v1/inquiries/admin/alarm-setting`에 `{"enabled": true}`를 보낸다. 켜지 않아도 자기가 답한 문의의 작성자
  메시지 알림은 받는다.

## 보낸 사람 표시

| 보는 사람 | 운영진 메시지 | 작성자 메시지 |
| --- | --- | --- |
| 운영진(`UMC_PRODUCT` 또는 `SUPER_ADMIN`) | `senderMemberId`, `senderName`(실명) 그대로 | 그대로 |
| 그 밖의 사람(작성자) | `senderMemberId=null`, `senderName="UMC PRODUCT"` | 그대로 |

`senderType`은 어느 쪽이든 그대로다. REST 메시지 조회, 메시지 전송 응답, 실시간 `MESSAGE_CREATED`가 같은 규칙이고
판정은 `InquiryMessageAssembler` 한 곳에서 한다.

## 실시간 수신

| 항목 | 내용 |
| --- | --- |
| 구독 주소 | `/user/queue/inquiries/events` (연결은 커뮤니티와 같은 `/ws/websocket`). 모든 문의의 이벤트가 오고 `inquiryId`로 구분한다 |
| 받는 사람 | 문의 작성자와 `UMC_PRODUCT` 역할 회원 전원. 알림 설정과 상관없고 `SUPER_ADMIN`만 가진 회원은 받지 않는다 |
| 구독 권한 | 로그인한 회원. 서버가 받는 사람을 정해 그 사람 큐로만 보낸다 |
| `MESSAGE_CREATED` | `inquiryId`, `message`(REST 메시지 응답과 같은 필드, 보낸 사람 표시는 받는 사람에 맞춤) |
| `STATUS_CHANGED` | `inquiryId`, `status`, `closedAt`, `lastMessageAt`, `occurredAt`. 운영진 첫 답변, 종료, 재오픈 때. 이미 반영한 것보다 `occurredAt`이 오래되면 버린다 |
| 보내지 않는 경우 | 같은 `clientMessageId` 재전송, 상태가 그대로인 메시지 |
| 재연결 | 다시 구독한 뒤 REST 메시지 첫 페이지와 상세를 다시 불러 `messageId`로 합친다 |

흐름은 `ChatMessageCreatedEvent`·`InquiryStatusChangedEvent` → `InquiryRealtimeEventListener` →
`InquiryRealtimeRelayService` → `InquiryRealtimeBroadcastPort`다. 둘 다 outbox REALTIME 그룹에서 커밋 뒤 보낸다.

## 푸시 알림

| 언제 | 받는 사람 | `data.type` | 제목 / 본문 |
| --- | --- | --- | --- |
| 운영진 메시지 | 작성자 | `INQUIRY_REPLY` | 문의 답변이 도착했어요 / 고정 문구(운영진 이름 없음) |
| 새 문의 등록 | 새 문의 알림을 켠 운영진 | `INQUIRY_NEW` | 새 문의가 왔어요 / 문의 제목 |
| 작성자 메시지(재오픈 포함) | 그 문의에 답한 운영진(설정 무관). 없으면 새 문의 알림을 켠 운영진 | `INQUIRY_AUTHOR_MESSAGE` | 문의에 새 메시지가 왔어요 / 문의 제목 |

모두 `data.inquiryId`와 딥링크 `umc://inquiry/{inquiryId}`를 담는다. 운영진은 `UMC_PRODUCT` 시스템 역할을 직접 가진
회원이고 작성자는 뺀다. `SUPER_ADMIN`만 가진 회원은 받지 않는다. 답한 운영진은 문의 Chat 방 멤버 중 지금 운영진인
사람이다. 계산은 `ListInquiryStaffAlarmRecipientsUseCase`가 하고, 받는 사람이 없으면 FCM 요청을 만들지 않는다. 같은
`clientRequestId`·`clientMessageId` 재전송에는 보내지 않는다.

## 경계

대화 본문과 첨부는 Chat 엔진 메시지로 저장한다. inquiry는 Chat의 공개 UseCase만 쓰고, 외부 응답에 `chatRoomId`를
내보내지 않는다. 푸시 발송은 notification 도메인의 `InquiryAlarmEventListener`가 맡는다. 첨부 파일은
storage의 `INQUIRY_ATTACHMENT` 카테고리로 올린 파일만 받는다.

## UX Writing Notes

권한 오류는 누가 할 수 있는지 알려 준다(`UMC PRODUCT 운영진만 할 수 있어요`). 상태 충돌은 새로고침을 안내한다.
