# ADR-031: 문의 도메인을 Chat 엔진 소비 도메인으로 만들고 보내기는 REST로 연다

## 상태

Accepted. [ADR-011](011-inquiry-domain-with-websocket-stomp.md)을 대체한다.

## 배경

UMC Desk(macOS)에 UMC PRODUCT 팀 문의 기능을 넣는다. 사용자가 문의를 남기면 운영진(`UMC_PRODUCT` 또는
`SUPER_ADMIN` 시스템 역할)이 답하고, 같은 문의 안에서 대화를 이어 간다. 나중에 모바일 앱도 같은 API를 쓸 수 있다.

ADR-011은 문의 전용 메시지 테이블과 STOMP 구독을 전제로 했다. 그 뒤 ADR-026에서 Chat 엔진(방·메시지·첨부·멱등)과
소비 도메인(리소스 권한·상태·외부 계약)의 책임을 나눴고, AUTHZ-10에서 문의 답변·관리 권한을 `UMC_PRODUCT` 시스템
역할로 정했다. 이번 결정은 그 둘을 문의에 적용한다.

전체 기능은 4단계로 나눈다. 1단계 문의 접수와 대화, 2단계 담당자·라벨·운영 목록, 3단계 FAQ(대표 질문), 4단계 인기
순위·답변 준비·검색이다. 이 ADR은 네 단계를 모두 결정한다(1단계 2026-11-24, 2~4단계와 Figma 맞추기 2026-11-25).

화면은 Figma `UMC Mac OS > Hi-fi > 06 · 문의 · Hi-fi`가 기준이다. 서버 결정이 Figma와 맞지 않으면 Figma대로 바꾼다.
그래서 처음에 정한 "질문자에게 운영진 이름을 숨긴다"(옛 11번)와 "등록 화면의 제목 칸"을 2026-11-25에 되돌렸다.

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
3. 받는 팀(`InquiryTeam`, null은 "모르겠어요")은 표시에만 쓴다. 운영 목록에도 팀 필터는 없다(13번).
   열람 권한은 "작성자 또는 운영진" 하나뿐이고, 운영진 판정은 `CheckChallengerAuthorityUseCase.isUmcProduct`로만 한다.
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
   딥링크 `umc://inquiry/{inquiryId}`). 푸시 문구는 고정이고 운영진 이름은 넣지 않는다. 운영진에게 가는 푸시는 10번이다.
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
     처음에는 문의마다 `/topic/inquiries/{inquiryId}`를 두었다. 받는 사람마다 보낸 사람 표시를 다르게 하려고
     Community(`/user/queue/community/threads/events`)와 같은 사람별 큐로 바꿨고, 그 표시 구분을 없앤 뒤에도 사람별
     큐를 유지한다. 화면을 열지 않은 운영진도 받고, 권한을 회수하면 다음 이벤트부터 바로 빠지기 때문이다.
   - 받는 사람은 서버가 정한다. 문의 작성자와 `UMC_PRODUCT` 시스템 역할을 직접 가진 회원 전원이다. 알림 설정(10번)과
     상관없다. `SUPER_ADMIN`만 가진 회원은 받지 않는다. 작성자가 운영진이면 한 번만 보낸다. 운영진 목록은 relay 때마다
     `ListMemberSystemRoleUseCase.listUmcProductMemberIds`로 읽으므로 권한을 회수하면 그 뒤 이벤트부터 받지 않는다.
   - 구독 권한은 `InquiryStompSubscriptionAuthorizer`가 본다. 로그인한 회원이면 구독할 수 있고, 자기에게 보낸 이벤트만
     받는다.
   - 한 주소로 두 종류가 오고 `type`으로 구분한다. `MESSAGE_CREATED`는 `inquiryId`와 REST 메시지 응답과 같은 모양의
     `message`를 담는다. 받는 사람 모두에게 같은 내용이다(11번). `STATUS_CHANGED`는 `inquiryId`,
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
   - 첨부 `fileLink`는 REST 응답처럼 60분짜리 presigned 링크다. 메시지 하나에 한 번만 만들어 받는 사람 모두에게
     같이 보낸다. 화면을 오래 열어 두어 만료되면 REST로 다시 불러온다.
   - FCM 푸시(6번, 10번)는 앱이 꺼져 있을 때를 위해 그대로 둔다.
10. 운영진도 FCM을 받는다(2026-11-24 추가, 2026-11-25 받는 사람을 줄임). UMC PRODUCT 팀은 50명쯤이라 모든 문의와
    메시지를 전원에게 보내면 푸시가 쏟아진다. 그래서 새 문의는 받고 싶은 사람만, 이어지는 메시지는 그 문의에 답한
    사람만 받는다.
    - 운영진은 `UMC_PRODUCT` 시스템 역할을 직접 가진 회원이다. `SUPER_ADMIN`만 가진 회원은 문의를 볼 수 있지만(3번)
      알림은 받지 않는다. 목록은 inquiry의 `InquiryStaffRoster`(member의 `listUmcProductMemberIds`를 읽는다) 한 곳에서
      얻고, 받는 사람 계산은 inquiry의 `ListInquiryStaffAlarmRecipientsUseCase` 한 곳에서 한다. 작성자는 늘 뺀다.
    - 새 문의(`inquiry.created`, `data.type=INQUIRY_NEW`)는 새 문의 알림을 켠 운영진에게 보낸다.
    - 작성자 쪽 메시지(`inquiry.author.messaged`, `data.type=INQUIRY_AUTHOR_MESSAGE`, 종료된 문의 재오픈 포함)는
      담당자(13번)가 있으면 담당자에게만 보낸다. 담당자가 없으면 그 문의에 메시지를 보낸 적 있는 운영진(참여 운영진)에게
      알림 설정과 상관없이 보내고, 참여 운영진도 없으면 새 문의 알림을 켠 운영진에게 보낸다. 참여 운영진은 Chat 방 멤버(`GetChatRoomUseCase`)와 현재 운영진 목록의 교집합이다.
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
    - 다른 운영진이 담당자로 지정하면 지정된 사람에게 "문의를 맡았어요"(`inquiry.assigned`,
      `data.type=INQUIRY_ASSIGNED`, 본문은 문의 제목)를 보낸다. 스스로 맡으면 보내지 않는다.
11. 운영진 메시지는 누가 보든 실명으로 보인다(2026-11-25 바꿈). Figma 문의 상세(Q2)가 질문자 화면에
    "이지원 · UMC PRODUCT 담당자 · 14:24"처럼 답한 운영진 이름을 보여 주기 때문이다. 처음에는 카카오 비즈니스 채널처럼
    질문자에게 "UMC PRODUCT" 한 이름만 보이게 했지만 Figma와 맞지 않아 되돌렸다.
    - REST 메시지 조회, 메시지 전송 응답, 실시간 `MESSAGE_CREATED`(9번)가 모두 `senderType`, `senderMemberId`,
      `senderName`(실명)을 그대로 준다. `InquiryMessageAssembler`는 보는 사람과 상관없이 한 가지 모양만 만든다.
    - 푸시 문구(6번)에는 여전히 운영진 이름을 넣지 않는다.
12. 등록과 분류는 Figma 등록 화면을 따른다(2026-11-25).
    - 등록 요청에 제목이 없다. 서버가 내용의 첫 번째 비지 않은 줄을 앞뒤 공백을 빼고 제목으로 저장하고, 50자를 넘으면
      자른다. 규칙은 `Inquiry.titleOf` 한 곳이다.
    - 문의 분류는 출석·과제, 계정·로그인, 프로젝트·매칭, 수료·활동 증명 순서다. 처음 시드 `UMC 서비스`·`기타`는 새
      마이그레이션에서 비활성화했다(행은 남긴다). 이미 적용된 마이그레이션은 고치지 않는다.
    - 첨부 한도는 `INQUIRY_ATTACHMENT` 파일 하나에 50MB다. 사진과 문서를 섞은 메시지는 그대로 거절하고 앱이 나눠 보낸다.
13. 운영 관리(2단계, 2026-11-25). 운영진(3번 판정) 누구나 한다. 화면은 운영진 문의 목록과 운영 상세다.
    - 문의를 열람·처리하는 권한(3번, AUTHZ-10)과 담당자가 될 수 있는 사람은 다르다(2026-11-25 결정). 담당자 후보,
      "내 담당" 탭, 운영진 알림(10번), 실시간 받는 사람(9번)은 `UMC_PRODUCT` 역할을 직접 가진 회원만이다.
      `SUPER_ADMIN`만 가진 회원은 문의를 보고 처리할 수 있지만 담당자가 될 수 없다. 이 명단은 `InquiryStaffRoster`
      (`memberIds()`, `isAssignable(id)`) 한 곳에서 읽는다. 그런 회원이 "내 담당"을 열면 쿼리 없이 빈 목록과 개수 0을
      받고, 운영 상세의 `canBeAssignee=false`로 앱이 "내가 맡기"를 숨긴다.
    - 담당자는 문의당 0~1명이고 후보는 `UMC_PRODUCT` 역할을 직접 가진 회원 전원이다. 지정·변경·해제와 "내가 맡기"는
      한 API(`PUT .../admin/{inquiryId}/assignee`)다. 요청에 화면에서 본 지금 담당자(`expectedAssigneeMemberId`)를 받고,
      문의 행을 잠근 뒤 지금 담당자와 다르면 409 `INQUIRY_ASSIGNEE_CHANGED`로 거절한다. 덮어쓰기 플래그는 없다. 앱이
      상세를 다시 불러와 재시도한다. 규칙은 `Inquiry.changeAssignee` 한 곳이다.
    - 담당자가 `UMC_PRODUCT` 역할을 잃으면 저장된 값과 상관없이 미배정으로 본다(`Inquiry.effectiveAssigneeId`). 목록 탭,
      상세, 알림이 모두 이 판정을 쓴다. 행을 고치는 배치는 없다.
    - 담당자 변경(`ASSIGNED`·`REASSIGNED`·`UNASSIGNED`)과 받는 팀 변경(`TEAM_CHANGED`)은 `inquiry_history`에 누가, 언제,
      이전→이후를 남긴다. 운영 상세의 이력 첫 줄 "문의 접수"(`RECEIVED`)는 저장하지 않고 문의 접수 시각과 작성자로 만든다.
      그래서 저장용 `InquiryHistoryType`에는 `RECEIVED`가 없고(DB CHECK와 같다), 응답용 종류 `InquiryHistoryInfo.Kind`가
      접수 줄을 더한다. 새 담당자에게 알릴지는 `InquiryHistory.notifiesNewAssignee`(다른 사람이 지정했을 때만)가 정한다.
    - 받는 팀은 운영진 누구나 바꾸고 질문자 화면에도 바뀐 팀이 보인다. 팀 이름과 담당 범위 설명은 `InquiryTeam`
      enum이 들고 `GET /api/v1/inquiries/teams`로 내려준다. 마지막 항목은 `team=null`인 "어느 팀인지 모르겠어요"다.
    - 라벨은 이름만 있고 이름이 고유하다. 운영진 누구나 만들기·이름 바꾸기·지우기(API만)를 하고, 지우면 붙은
      문의에서도 빠진다(FK `ON DELETE CASCADE`). 문의에 붙이는 것은 집합을 통째로 바꾸는 PUT이다. 질문자 응답에는
      라벨·담당자·이력이 없다. 시드는 출석 오류, 정보 확인, 기능 요청이다.
    - 운영 목록(`GET /api/v1/inquiries/admin`)은 탭(전체·내 담당·미배정)과 탭별 개수를 함께 준다. 개수에는 탭 말고 다른
      필터를 똑같이 적용한다. 개수는 첫 페이지에만 세고, `cursor`를 넘긴 다음 페이지 응답의 `counts`는 null이다. 필터는
      상태·카테고리(여러 개면 그중 하나), 라벨(여러 개면 하나라도 붙은 것), 미분류 토글이다. 받는 팀 필터는 없다.
      마지막 메시지 최신순, 불투명 커서이고 라벨·카테고리·담당자 이름은 묶어서 읽는다.
    - 운영진 쪽 처리 변경(담당자·라벨·팀·대표 질문 연결)은 실시간으로 보내지 않는다. 화면은 응답이나 다시 불러오기로
      갱신한다. 필요해지면 9번 사람별 큐에 이벤트 종류를 더한다.
14. 대표 질문(FAQ, 3단계, 2026-11-25). `inquiry_faq`에 두고 inquiry 도메인이 소유한다.
    - 상태는 초안(`DRAFT`) → 발행(`PUBLISHED`) → 보관(`ARCHIVED`)이고, 보관에서 다시 발행할 수 있다. 어느 상태에서든
      수정본을 발행할 수 있고 보관은 발행 중일 때만 한다. 전이는 `InquiryFaqStatus` 한 곳이다.
    - 한 행에 수정본(`draft_*`)과 발행본(`published_*`)을 같은 모양(`InquiryFaqContent`)으로 둔다. 발행 중에 고치면
      수정본만 바뀌고 기존 발행본이 계속 공개된다(`hasUnpublishedChanges`). "검증 후 발행"이 수정본을 발행본으로
      복사한다. 발행 요청은 검증 체크 3개(개인정보 없음, 적용 범위 확인, 답변 정확성 확인)가 모두 true여야 하고 체크는
      저장하지 않는다. 초안은 질문만 있으면 되고, 발행하려면 답변·카테고리·대상이 있어야 한다.
    - 칸은 공개용 질문, 별칭(검색용, 사용자에게 안 보임), 공개용 답변, 대상(`inquiry_faq_audience`, SQL로 추가, 시드
      "UMC 서비스 사용자"), 카테고리(문의 분류와 같은 목록), 종료일(선택), 관련 안내(다른 대표 질문 0~1개)다.
      수정일은 마지막 발행 시각, 발행한 사람도 남긴다. 검증 조직은 늘 "UMC PRODUCT"라 저장하지 않는다.
    - 종료일은 한국 날짜다. 그날까지 공개하고 다음 날부터 스케줄러 없이 조회 조건으로 빠진다.
    - 권한은 3번 운영진 판정 하나다. 별도 발행 권한은 없다.
    - 운영진 여럿이 같은 대표 질문을 고칠 수 있어 낙관적 잠금을 건다(2026-11-25 결정). `inquiry_faq.version`(JPA
      `@Version`)을 응답에 주고, 초안 저장·발행·보관 요청은 화면에서 본 `expectedVersion`을 받는다. 지금 버전과 다르거나
      그사이 다른 요청이 먼저 저장해 flush가 실패하면 409 `INQUIRY_FAQ_CHANGED`로 거절하고 덮어쓰지 않는다. 앱이 최신
      내용을 다시 불러와 재시도한다. 담당자(13번)와 같은 "덮어쓰기 없음" 방식이다.
    - 발행 직전에 수정본의 카테고리·대상이 아직 쓰이는지 다시 확인한다. 초안을 저장한 뒤 비활성으로 바뀌었으면 발행을
      거절한다. 옛 분류로 접수된 문의로 새 초안을 만들면 카테고리를 비워 둔다.
    - 문의는 대표 질문 0~1개에 연결된다(`inquiry.faq_id`). 초안에도 연결할 수 있고, 연결 화면에서 문의 제목·분류로
      새 초안을 만들어 바로 연결할 수 있다. 연결되지 않은 문의가 "미분류"다. 보관해도 연결은 유지한다. 운영 상세에서
      연결된 대표 질문과 참고한 대표 질문은 발행본이 있으면 발행본 질문, 없으면 수정본 질문으로 보여 준다.
    - 사용자 FAQ는 로그인 사용자만 본다. 목록·상세·검색·"먼저 확인해 보세요"는 발행 중이고 종료일이 지나지 않은 것만
      다루고, 보관하면 그 즉시 빠진다. 관련 안내는 그 대표 질문도 공개 중일 때만 보인다.
    - "해결됐어요"는 회원마다 대표 질문 하나에 한 번만 센다(`inquiry_faq_resolution` unique, `ON CONFLICT DO NOTHING`).
      수는 운영진 응답에만 있다.
    - 문의 등록 화면의 "먼저 확인해 보세요"는 고른 분류에서 최근 7일 가장 많이 물은 공개 대표 질문, 없으면 가장 최근에
      발행한 것 하나다. 등록 요청에 본 대표 질문 ID(`referencedFaqId`)를 받아 저장하고 운영 상세에 보여 준다. 공개 중이
      아닌 ID면 등록은 그대로 하고 이 값만 비워 둔다. 404로 거절하면 비공개 대표 질문이 있는지 드러나기 때문이다.
15. 인기 순위·답변 준비·검색(4단계, 2026-11-25).
    - 공개 순위는 공개 중인 대표 질문별로, 최근 24시간(`DAY`)·7일(`WEEK`) 안에 접수된 연결 문의를 남긴 서로 다른 작성자
      수다. 같은 사람이 여러 번 물어도 한 번이다. 미분류 문의는 세지 않는다. 상위 5개이고 같은 수면 최근 문의가 더
      최근인 쪽, 그다음 대표 질문 ID가 작은 쪽이 앞이다. REST는 부를 때마다 지금 기준으로 계산하고 `refreshedAt`은 계산
      시각이다. 집계 테이블은 두지 않는다.
    - 순위 신호는 5분(`app.inquiry.faq-ranking-refresh-interval`)마다 계산해, 그 인스턴스가 마지막으로 본 상위 5개
      (대표 질문 ID와 인원)와 다르면 공용 주소 `/topic/inquiries/ranking`으로 `{type: RANKING_UPDATED, period,
      refreshedAt}`만 보낸다. 개인정보가 없어 로그인한 누구나 구독한다. 마지막 순위는 인스턴스 메모리에만 두므로 여러
      인스턴스나 재시작 뒤에 신호가 겹칠 수 있고, 보내기에 실패하면 다음 주기에 다시 보낸다. 신호는 "다시 불러오라"는
      뜻뿐이라 at-least-once면 된다. 외부 relay는 broker 이름에 `/`를 받지 않아 `RelayDestinationCodec`이
      `/topic/inquiries.ranking`으로 바꾼다.
    - 답변 준비 필요(운영진 전용)는 한 번도 발행하지 않은 초안 중 최근 24시간 묻은 회원이 많은 순 10개다. 공개 순위에는
      나오지 않는다.
    - 검색은 공개 중인 대표 질문의 질문·별칭·답변 부분 일치이고 대소문자를 가리지 않는다. 발행할 때 세 칸을 줄바꿈으로
      이은 `published_search_text`를 만들어 둔다. 최대 20개, 없으면 빈 목록이다. 예시 칩은 앱이 분류 이름으로 그린다.

## 후속

- 운영진 처리 변경의 실시간 전달(13번 마지막 항목)은 필요해지면 9번 사람별 큐에 이벤트 종류를 더한다.
- 검색이 느려지면 `published_search_text`에 trigram 인덱스를 더한다.
