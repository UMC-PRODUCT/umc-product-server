# ADR-031: 문의 도메인을 Chat 엔진 소비 도메인으로 만들고 보내기는 REST로 연다

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
   Chat 방을 `chatRoomId`만으로 읽는 새 경로를 만들면 이 결정을 다시 봐야 한다. Chat 메시지 조회 UseCase를 Chat과
   소비 도메인 밖에서 쓰면 `ChatConsumerDependencyArchTest`가 실패한다.
5. 등록은 `unique(author_member_id, client_request_id)`로, 메시지는 Chat 엔진의 `clientMessageId` dedup으로 멱등이다.
   dedup 결과면 상태 전이와 알림을 다시 하지 않는다. 같은 등록 요청이 동시에 오면 늦은 쪽은 409를 받고, 재시도하면
   기존 문의를 받는다. 같은 `clientRequestId`는 내용 비교 없이 기존 문의를 돌려준다. 제목·본문이 달라도 새로 만들거나
   거절하지 않는다.
6. 1차 전달은 REST, 커서 조회, FCM이다. 운영진의 중복 아닌 메시지는 `inquiry.staff.replied` 이벤트를 outbox에 남기고,
   커밋 뒤 notification 리스너가 작성자에게 푸시한다(`data.type=INQUIRY_REPLY`, `data.inquiryId`,
   딥링크 `umc://inquiry/{inquiryId}`). 운영진 이름은 넣지 않는다(11번). 운영진에게 가는 푸시는 10번이다.
   API·딥링크·푸시 데이터에 플랫폼 구분을 넣지 않는다.
7. 첨부는 `FileCategory.INQUIRY_ATTACHMENT`(`private/inquiry`)로 올린, 보낸 사람 본인의 업로드 완료 파일만 받는다.
   첨부 링크는 문의 메시지 응답에서만 만들어지므로 문의 접근 정책이 곧 첨부 열람 권한이다.
   메시지 종류는 첨부 구성으로 정한다. 첨부가 없으면 `TEXT`, 모두 이미지면 `IMAGE`, 그 외는 `FILE`이고, Chat 공통
   첨부 검증(`ValidateChatAttachmentsUseCase`)을 거친다. Chat 메시지 하나는 한 종류만 담으므로 이미지와 문서를 섞은
   첨부는 400 `INQUIRY_ATTACHMENT_MIXED_TYPES`로 거절한다.
8. 카테고리는 `inquiry_category` 테이블에 두고 관리 API 없이 SQL로 추가·비활성화한다.
9. 실시간 수신은 STOMP로 받기만 한다(2026-11-24 추가, 2026-11-25 사람별 큐로 바꿈). 보내기는 6번의 REST
   (`POST /api/v1/inquiries/{inquiryId}/messages`, `clientMessageId` 멱등)를 그대로 쓰고 STOMP SEND 경로는 만들지 않는다.
   - 주소는 회원마다 하나인 `/user/queue/inquiries/events`다. 모든 문의의 이벤트가 이 큐로 오고 `inquiryId`로 구분한다.
     처음에는 문의마다 `/topic/inquiries/{inquiryId}`를 두었지만, topic은 구독자 모두에게 같은 내용을 보내므로 11번처럼
     받는 사람마다 보낸 사람 표시를 다르게 할 수 없어 Community(`/user/queue/community/threads/events`)와 같은
     사람별 큐로 바꿨다.
   - 받는 사람은 서버가 정한다. 문의 작성자와 `UMC_PRODUCT` 시스템 역할을 직접 가진 회원 전원이다. 알림 설정(10번)과
     상관없다. `SUPER_ADMIN`만 가진 회원은 받지 않는다. 작성자가 운영진이면 한 번만 보낸다. 운영진 목록은 relay 때마다
     `ListMemberSystemRoleUseCase.listUmcProductMemberIds`로 읽으므로 권한을 회수하면 그 뒤 이벤트부터 받지 않는다.
   - 구독 권한은 `InquiryStompSubscriptionAuthorizer`가 본다. 로그인한 회원이면 구독할 수 있고, 자기에게 보낸 이벤트만
     받는다.
   - 한 주소로 두 종류가 오고 `type`으로 구분한다. `MESSAGE_CREATED`는 `inquiryId`와 REST 메시지 응답과 같은 모양의
     `message`를 담는다. `message`의 보낸 사람 표시는 받는 사람에 따라 11번대로 다르다. `STATUS_CHANGED`는 `inquiryId`,
     `status`, `closedAt`, `lastMessageAt`, `occurredAt`을 담고 운영진 첫 답변, 종료, 재오픈 때 보낸다. 상태가 그대로인
     메시지에는 보내지 않는다. dedup된 재전송에는 아무것도 보내지 않는다.
   - 메시지는 Chat의 `chat.message.created`를, 상태는 `inquiry.status.changed`를 outbox에 남기고 커밋 뒤 REALTIME
     그룹에서 보낸다. 상태 이벤트는 커밋 시점 스냅숏(작성자 ID 포함)을 담으므로 relay 때 문의를 다시 읽지 않는다. 문의
     방이 아닌 Chat 이벤트는 무시한다. 전달은 at-least-once다. 같은 `messageId`가 두 번 올 수 있고, 메시지와 상태
     이벤트의 도착 순서는 보장하지 않는다. 클라이언트는 이미 반영한 것보다 `occurredAt`이 오래된 `STATUS_CHANGED`를
     버린다. `occurredAt`은 문의 행을 잠근 트랜잭션에서 정하므로 문의 하나 안에서는 커밋 순서대로 늘어난다.
   - `chat.message.created`는 Community와 문의 리스너가 함께 받는다. 한쪽이 실패하면 outbox 행 전체가 다시 시도되어
     다른 쪽도 다시 보낼 수 있다. 둘 다 at-least-once라 중복 전달로 끝난다.
   - 외부 RabbitMQ relay는 broker 이름에 `/`를 받지 않으므로 `RelayDestinationCodec`이 broker 쪽에서 세션 큐
     `/queue/inquiries/events-user{sessionId}`를 `/queue/inquiries.events-user{sessionId}`로 바꾼다. 클라이언트는 항상
     `/user/queue/inquiries/events`를 쓴다.
   - 연결이 끊긴 동안의 이벤트는 다시 보내지 않는다. 클라이언트는 다시 연결해 구독한 뒤 REST 메시지 첫 페이지와 상세를
     다시 불러 `messageId`로 합친다.
   - 첨부 `fileLink`는 REST 응답처럼 60분짜리 presigned 링크다. 메시지 하나에 한 번만 만들어 두 가지 표시에 같이
     쓴다. 화면을 오래 열어 두어 만료되면 REST로 다시 불러온다.
   - FCM 푸시(6번, 10번)는 앱이 꺼져 있을 때를 위해 그대로 둔다.
10. 운영진도 FCM을 받는다(2026-11-24 추가, 2026-11-25 받는 사람을 줄임). UMC PRODUCT 팀은 50명쯤이라 모든 문의와
    메시지를 전원에게 보내면 푸시가 쏟아진다. 그래서 새 문의는 받고 싶은 사람만, 이어지는 메시지는 그 문의에 답한
    사람만 받는다.
    - 운영진은 `UMC_PRODUCT` 시스템 역할을 직접 가진 회원이다. `SUPER_ADMIN`만 가진 회원은 문의를 볼 수 있지만(3번)
      알림은 받지 않는다. 목록은 member의 `ListMemberSystemRoleUseCase.listUmcProductMemberIds`로 읽고, 받는 사람 계산은
      inquiry의 `ListInquiryStaffAlarmRecipientsUseCase` 한 곳에서 한다. 작성자는 늘 뺀다.
    - 새 문의(`inquiry.created`, `data.type=INQUIRY_NEW`)는 새 문의 알림을 켠 운영진에게 보낸다.
    - 작성자 쪽 메시지(`inquiry.author.messaged`, `data.type=INQUIRY_AUTHOR_MESSAGE`, 종료된 문의 재오픈 포함)는 그
      문의에 메시지를 보낸 적 있는 운영진(참여 운영진)에게 알림 설정과 상관없이 보낸다. 참여 운영진이 없으면 새 문의
      알림을 켠 운영진에게 보낸다. 참여 운영진은 Chat 방 멤버(`GetChatRoomUseCase`)와 현재 운영진 목록의 교집합이다.
      운영진은 첫 메시지를 보낼 때만 방에 들어가므로(4번) 방 멤버가 곧 답한 사람이고, 운영진 권한을 잃은 사람은
      교집합에서 빠진다. 별도 테이블은 두지 않는다.
    - 운영진끼리의 메시지, 같은 `clientRequestId`·`clientMessageId` 재전송에는 보내지 않는다. 운영진이 자기 문의에
      쓰면 2번대로 작성자로 보고 본인은 뺀다. 제목은 고정 문구, 본문은 문의 제목이다. 문의 내용과 운영진 이름은 넣지
      않는다. 받는 사람이 없으면 FCM 요청을 만들지 않는다. 받는 사람은 outbox relay 때 계산하므로 그 사이 바꾼 설정과
      권한이 반영된다.
    - 설정은 운영진마다 "새 문의 알림" 켜기/끄기 하나다(`inquiry_staff_alarm_setting`). 행이 없으면 꺼짐으로 본다.
      문의별로 끄는 설정은 없다. 운영진(3번 판정)이 `GET`·`PUT /api/v1/inquiries/admin/alarm-setting`으로 자기 설정만
      보고 바꾼다. 같은 값을 다시 보내도 결과가 같다. 운영진이 아니면 403 `INQUIRY_STAFF_ONLY`다. `UMC_PRODUCT` 역할이
      없는 `SUPER_ADMIN`은 설정 API를 쓸 수 있지만 설정과 상관없이 운영진 알림을 받지 않는다.
    - 설정 화면이 나오기 전에는 운영진이 Swagger에서 `PUT /api/v1/inquiries/admin/alarm-setting`에
      `{"enabled": true}`를 보내야 새 문의 알림을 받는다.
    - 2단계에서 담당자를 만들면 "배정된 문의는 담당자에게만 알린다" 규칙을 이 계산에 더한다.
11. 문의한 사람에게 운영진은 한 이름 "UMC PRODUCT"로만 보인다(2026-11-25 추가). 카카오 비즈니스 채널처럼 질문한
    사람은 운영진 누가 답했는지 모르고, 운영진끼리는 누가 답했는지 실명으로 본다.
    - 보는 사람이 운영진(3번 판정, `UMC_PRODUCT` 또는 `SUPER_ADMIN`)이면 `senderType`, `senderMemberId`, `senderName`(실명)을
      그대로 준다. 운영진이 아니면 `senderType=STAFF`인 메시지의 `senderName`을 `"UMC PRODUCT"`, `senderMemberId`를
      `null`로 준다. `senderType`은 그대로 `STAFF`다. 작성자 자기 메시지는 그대로다.
    - REST 메시지 조회, 메시지 전송 응답, 실시간 `MESSAGE_CREATED`(9번)가 모두 같은 규칙이다. 판정은
      `InquiryMessageAssembler` 한 곳에서 한다. 실시간은 메시지 하나로 운영진용과 작성자용 두 가지를 만들고 받는
      사람에 맞춰 보낸다.
    - 운영진 답장 푸시(6번)에도 운영진 이름을 넣지 않는다.
    - 1단계 문의 API(#1603)와 이 결정(#1604)은 같은 배포로 내보낸다. #1603만 나가면 질문자에게 운영진 이름과 회원
      ID가 보인다.

## 후속

- 2단계(담당자·라벨·운영 목록), 3단계(FAQ), 4단계(인기 순위)는 별도 결정으로 다룬다.
