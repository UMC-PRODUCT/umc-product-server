# Inquiry Domain

## 역할

`inquiry` 도메인은 UMC PRODUCT 팀 문의의 접수, 상태, 작성자와 운영진 사이의 대화, 운영진의 처리 관리, 대표 질문(FAQ)과
인기 순위를 맡는다. 설계 결정은 [ADR-031](../../adr/031-inquiry-on-chat-engine-rest-first.md)에 있다. 화면 기준은 Figma
`UMC Mac OS > Hi-fi > 06 · 문의 · Hi-fi`이고, 서버 결정이 Figma와 다르면 Figma를 따른다.

## 책임

- 문의 분류(`inquiry_category`)와 대표 질문 대상(`inquiry_faq_audience`)을 제공한다. 둘 다 관리 API 없이 SQL로 추가하거나
  비활성화한다.
- 문의를 등록하고 `RECEIVED`, `IN_PROGRESS`, `CLOSED` 상태를 바꾼다. 제목은 내용 첫 줄(최대 50자)로 서버가 만든다.
- 작성자와 운영진(`UMC_PRODUCT` 또는 `SUPER_ADMIN`)만 문의를 읽고 쓰게 한다. 보낸 사람은 누구에게나 실명으로 보인다.
- 운영진이 담당자·받는 팀·라벨·대표 질문 연결을 바꾸고, 담당자와 팀 변경은 처리 이력에 남긴다.
- 대표 질문을 초안 → 발행 → 보관으로 관리하고, 사용자에게 목록·상세·검색·추천·인기 순위로 보여 준다.
- 새 메시지와 상태 변화를 사람별 STOMP 큐로, 새 순위 신호를 공용 topic으로 보낸다. 보내기는 REST만 받는다.

## API

모든 API는 로그인이 필요하다. "운영진"은 `UMC_PRODUCT` 또는 `SUPER_ADMIN`이고, 아니면 403 `INQUIRY-0003`이다.

| 메서드·경로 | 누가 | 내용 |
| --- | --- | --- |
| `GET /api/v1/inquiries/categories` | 누구나 | 활성 문의 분류 |
| `GET /api/v1/inquiries/teams` | 누구나 | 받는 팀 7개와 담당 범위, 맨 뒤 `team=null` "어느 팀인지 모르겠어요" |
| `POST /api/v1/inquiries` | 누구나 | 등록. `categoryId`, `targetTeam`, `content`, `fileMetadataIds`, `clientRequestId`, `referencedFaqId`(선택) |
| `GET /api/v1/inquiries/me` | 누구나 | 내 문의 목록 |
| `GET /api/v1/inquiries/{id}` | 작성자·운영진 | 상세. 라벨·담당자·이력은 없다 |
| `GET`·`POST /api/v1/inquiries/{id}/messages` | 작성자·운영진 | 메시지 조회·전송 |
| `POST /api/v1/inquiries/admin/{id}/close` | 운영진 | 종료 |
| `GET`·`PUT /api/v1/inquiries/admin/alarm-setting` | 운영진 | 새 문의 알림 설정 |
| `GET /api/v1/inquiries/admin` | 운영진 | 운영 목록. `tab`(ALL·MINE·UNASSIGNED), `status`, `categoryId`, `labelId`(여러 번), `unclassified`, `cursor`, `size`. 탭별 개수 포함 |
| `GET /api/v1/inquiries/admin/{id}` | 운영진 | 운영 상세. 상세 + 담당자, 라벨, 이력, 연결된 대표 질문, 참고한 대표 질문 |
| `GET /api/v1/inquiries/admin/assignee-candidates` | 운영진 | 담당자 후보(`UMC_PRODUCT` 전원) |
| `PUT /api/v1/inquiries/admin/{id}/assignee` | 운영진 | `assigneeMemberId`, `expectedAssigneeMemberId`. 본 담당자와 다르면 409 `INQUIRY-0015` |
| `PUT /api/v1/inquiries/admin/{id}/team` | 운영진 | 받는 팀 변경 |
| `PUT /api/v1/inquiries/admin/{id}/labels` | 운영진 | 라벨 집합 통째로 바꾸기 |
| `PUT /api/v1/inquiries/admin/{id}/faq` | 운영진 | 대표 질문 연결·해제(`faqId` null) |
| `POST /api/v1/inquiries/admin/{id}/faq/draft` | 운영진 | 문의 제목·분류로 새 초안을 만들어 연결 |
| `GET`·`POST /api/v1/inquiries/admin/labels`, `PATCH`·`DELETE .../labels/{labelId}` | 운영진 | 라벨 관리. 이름 중복 409 |
| `GET /api/v1/inquiries/admin/faqs` | 운영진 | 대표 질문 목록(`status`, `categoryId`). 해결 수 포함 |
| `GET /api/v1/inquiries/admin/faqs/{faqId}` | 운영진 | 수정본·발행본·발행한 사람·해결 수 |
| `POST /api/v1/inquiries/admin/faqs` | 운영진 | 초안 만들기 |
| `PUT /api/v1/inquiries/admin/faqs/{faqId}/draft` | 운영진 | 초안 저장(수정본 통째로 바꾸기) |
| `POST /api/v1/inquiries/admin/faqs/{faqId}/publish` | 운영진 | 검증 체크 3개 모두 true로 발행 |
| `POST /api/v1/inquiries/admin/faqs/{faqId}/archive` | 운영진 | 보관(발행 중일 때만) |
| `GET /api/v1/inquiries/admin/faqs/answer-needed` | 운영진 | 답변 준비 필요 초안 |
| `GET /api/v1/inquiries/admin/faq-audiences` | 운영진 | 대상 목록 |
| `GET /api/v1/inquiries/faqs` | 누구나 | 공개 대표 질문 목록(`categoryId`) |
| `GET /api/v1/inquiries/faqs/{faqId}` | 누구나 | 공개 상세. 공개 중이 아니면 404 |
| `GET /api/v1/inquiries/faqs/search` | 누구나 | `keyword`로 질문·별칭·답변 검색, 최대 20개 |
| `GET /api/v1/inquiries/faqs/suggestion` | 누구나 | "먼저 확인해 보세요" 1개 또는 null |
| `GET /api/v1/inquiries/faqs/rankings` | 누구나 | `period`(DAY·WEEK) 인기 순위 상위 5개 |
| `POST /api/v1/inquiries/faqs/{faqId}/resolution` | 누구나 | "해결됐어요". 회원마다 한 번 |

## 배포와 운영

- 설정 화면이 나오기 전에는 새 문의 알림을 받으려는 운영진이 Swagger에서
  `PUT /api/v1/inquiries/admin/alarm-setting`에 `{"enabled": true}`를 보낸다. 켜지 않아도 자기가 맡거나 답한 문의의
  작성자 메시지 알림은 받는다.
- 대표 질문 대상과 문의 분류는 SQL로 더한다. 라벨은 API로 관리한다.
- 순위 신호 주기는 `app.inquiry.faq-ranking-refresh-interval`(기본 `PT5M`)이다.

## 대표 질문 상태

| 지금 | 초안 저장 | 발행(검증 3개) | 보관 |
| --- | --- | --- | --- |
| `DRAFT` | 수정본만 바뀜 | `PUBLISHED` | 409 `INQUIRY-0028` |
| `PUBLISHED` | 수정본만 바뀌고 기존 발행본 공개 유지 | 수정본이 새 발행본 | `ARCHIVED` |
| `ARCHIVED` | 수정본만 바뀜 | `PUBLISHED` | 409 `INQUIRY-0028` |

사용자에게는 `PUBLISHED`이고 종료일(한국 날짜)이 지나지 않은 것만 보인다. 순위·검색·추천도 같은 조건이다.

## 실시간 수신

| 항목 | 사람별 큐 | 순위 신호 |
| --- | --- | --- |
| 구독 주소 | `/user/queue/inquiries/events` | `/topic/inquiries/ranking` |
| 받는 사람 | 문의 작성자와 `UMC_PRODUCT` 역할 회원 전원. `SUPER_ADMIN`만 가진 회원은 받지 않는다 | 구독한 로그인 회원 누구나 |
| 구독 권한 | 로그인한 회원. 서버가 받는 사람을 정해 그 사람 큐로만 보낸다 | 로그인한 회원 |
| 이벤트 | `MESSAGE_CREATED`(`inquiryId`, `message`), `STATUS_CHANGED`(`inquiryId`, `status`, `closedAt`, `lastMessageAt`, `occurredAt`) | `RANKING_UPDATED`(`period`, `refreshedAt`) |
| 언제 | 새 메시지, 운영진 첫 답변·종료·재오픈 | 5분마다 계산해 상위 5개가 바뀌었을 때 |
| 보내지 않는 경우 | 같은 `clientMessageId` 재전송, 상태가 그대로인 메시지, 담당자·라벨·팀·대표 질문 변경 | 순위가 그대로일 때 |
| 중복·재연결 | at-least-once. 다시 구독한 뒤 REST로 다시 불러 `messageId`로 합친다 | 인스턴스마다 따로 계산해 겹칠 수 있다. 앱은 "새 순위 보기"에서 REST로 다시 부른다 |

메시지 흐름은 `ChatMessageCreatedEvent`·`InquiryStatusChangedEvent` → `InquiryRealtimeEventListener` →
`InquiryRealtimeRelayService` → `InquiryRealtimeBroadcastPort`다. 둘 다 outbox REALTIME 그룹에서 커밋 뒤 보낸다. 순위 신호는
`InquiryFaqRankingRefreshScheduler` → `InquiryFaqRankingSignalService`가 outbox 없이 바로 보낸다.

## 푸시 알림

| 언제 | 받는 사람 | `data.type` | 제목 / 본문 |
| --- | --- | --- | --- |
| 운영진 메시지 | 작성자 | `INQUIRY_REPLY` | 문의 답변이 도착했어요 / 고정 문구(운영진 이름 없음) |
| 새 문의 등록 | 새 문의 알림을 켠 운영진 | `INQUIRY_NEW` | 새 문의가 왔어요 / 문의 제목 |
| 작성자 메시지(재오픈 포함) | 담당자가 있으면 담당자만. 없으면 그 문의에 답한 운영진(설정 무관), 그도 없으면 새 문의 알림을 켠 운영진 | `INQUIRY_AUTHOR_MESSAGE` | 문의에 새 메시지가 왔어요 / 문의 제목 |
| 다른 운영진이 담당자로 지정 | 지정된 운영진. 스스로 맡으면 없음 | `INQUIRY_ASSIGNED` | 문의를 맡았어요 / 문의 제목 |

모두 `data.inquiryId`와 딥링크 `umc://inquiry/{inquiryId}`를 담는다. 운영진은 `UMC_PRODUCT` 시스템 역할을 직접 가진
회원이고 작성자는 뺀다. 담당자가 `UMC_PRODUCT` 역할을 잃으면 미배정으로 본다. 계산은
`ListInquiryStaffAlarmRecipientsUseCase`가 하고, 받는 사람이 없으면 FCM 요청을 만들지 않는다.

## 경계

대화 본문과 첨부는 Chat 엔진 메시지로 저장한다. inquiry는 Chat의 공개 UseCase만 쓰고, 외부 응답에 `chatRoomId`를
내보내지 않는다. 푸시 발송은 notification 도메인의 `InquiryAlarmEventListener`가 맡는다. 첨부 파일은
storage의 `INQUIRY_ATTACHMENT` 카테고리(파일 하나 50MB)로 올린 파일만 받는다.

## UX Writing Notes

권한 오류는 누가 할 수 있는지 알려 준다(`UMC PRODUCT 운영진만 할 수 있어요`). 상태·담당자 충돌은 새로고침을 안내한다.
