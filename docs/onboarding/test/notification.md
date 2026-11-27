# Notification 테스트 케이스

- 테스트 파일: 9개
- 테스트 케이스: 31개
- 분류 기준: `Controller`, `UseCase`, `Repository`, `E2E`, `Scheduler`, `Domain`, `External Adapter`, `Support`

| 카테고리 | 케이스 수 |
|---|---:|
| Controller / Inbound Adapter | 1 |
| UseCase / Application Service | 11 |
| Repository / Outbound Persistence | 2 |
| E2E / Integration | 14 |
| Domain | 3 |

## Controller / Inbound Adapter

### FcmAdminControllerTest
- 위치: `monolith/src/test/java/com/umc/product/notification/adapter/in/web/FcmAdminControllerTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [60](../../../monolith/src/test/java/com/umc/product/notification/adapter/in/web/FcmAdminControllerTest.java#L60) | 기존 관리자 FCM 요청의 응답과 푸시 payload를 유지한다 | `POST /api/v1/notifications/admin/fcm/messages`에 기존 발송 요청을 보냄 | HTTP `202`, `requestId`와 `queuedAt` 반환, 기존 `noticeId`와 딥링크 유지, 이력 저장 요청과 응답 필드 추가 없음 |

## UseCase / Application Service

### FcmOutboxServiceTest
- 위치: `monolith/src/test/java/com/umc/product/notification/application/service/FcmOutboxServiceTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [25](../../../monolith/src/test/java/com/umc/product/notification/application/service/FcmOutboxServiceTest.java#L25) | SUBSCRIBE 이벤트는 토픽 기반 비활성화로 인해 즉시 FAILED 처리된다 | 조건 SUBSCRIBE 이벤트는 토픽 기반 비활성화로 인해 즉시 FAILED 처리된다 | 성공: 검증 assertThat(loadFcmOutboxPort.findPendingEvents()).isEmpty(); |
| [38](../../../monolith/src/test/java/com/umc/product/notification/application/service/FcmOutboxServiceTest.java#L38) | UNSUBSCRIBE 이벤트는 토픽 기반 비활성화로 인해 즉시 FAILED 처리된다 | 조건 UNSUBSCRIBE 이벤트는 토픽 기반 비활성화로 인해 즉시 FAILED 처리된다 | 성공: 검증 assertThat(loadFcmOutboxPort.findPendingEvents()).isEmpty(); |
| [51](../../../monolith/src/test/java/com/umc/product/notification/application/service/FcmOutboxServiceTest.java#L51) | PENDING 이벤트가 없으면 아무것도 처리하지 않는다 | 조건 PENDING 이벤트가 없으면 아무것도 처리하지 않는다 | 성공: 검증 assertThat(loadFcmOutboxPort.findPendingEvents()).isEmpty(); |
| [58](../../../monolith/src/test/java/com/umc/product/notification/application/service/FcmOutboxServiceTest.java#L58) | 여러 PENDING 이벤트가 있으면 모두 FAILED 처리된다 | 조건 여러 PENDING 이벤트가 있으면 모두 FAILED 처리된다 | 성공: 검증 assertThat(loadFcmOutboxPort.findPendingEvents()).isEmpty(); |

### FcmServiceTest
- 위치: `monolith/src/test/java/com/umc/product/notification/application/service/FcmServiceTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [28](../../../monolith/src/test/java/com/umc/product/notification/application/service/FcmServiceTest.java#L28) | 신규 토큰 등록 시 FCM 토큰이 활성 상태로 저장된다 | 호출 registerFcmToken(memberId, request) | 성공: 검증 assertThat(tokens).hasSize(1); assertThat(tokens.get(0).getFcmToken()).isEqualTo("new-token"); assertThat(tokens.get(0).isActive()).isTrue(); |
| [44](../../../monolith/src/test/java/com/umc/product/notification/application/service/FcmServiceTest.java#L44) | 동일 토큰 재등록 시 INSERT 없이 활성화만 된다 | 호출 registerFcmToken(memberId, new FcmRegistrationRequest("existing-token")) | 성공: 검증 assertThat(tokens).hasSize(1); assertThat(tokens.get(0).getFcmToken()).isEqualTo("existing-token"); assertThat(tokens.get(0).isActive()).isTrue(); |
| [61](../../../monolith/src/test/java/com/umc/product/notification/application/service/FcmServiceTest.java#L61) | 새 기기 토큰 등록 시 기존 토큰과 함께 저장된다 | 호출 registerFcmToken(memberId, new FcmRegistrationRequest("new-device-token")) | 성공: 검증 assertThat(tokens).hasSize(2); assertThat(tokens).extracting(FcmToken::getFcmToken); .containsExactlyInAnyOrder("old-token", "new-device-token"); |

### FcmNotificationOutboxFlowTest
- 위치: `monolith/src/test/java/com/umc/product/notification/application/event/FcmNotificationOutboxFlowTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [63](../../../monolith/src/test/java/com/umc/product/notification/application/event/FcmNotificationOutboxFlowTest.java#L63) | 기존 FCM outbox JSON을 읽어 수신자와 푸시 내용을 유지한다 | 이력 저장 필드가 없는 기존 형식의 FCM 발송 요청 JSON을 처리 | 이력 저장 없이 기존 수신 대상, 푸시 데이터, 딥링크를 담은 발송 배치 등록 |
| [113](../../../monolith/src/test/java/com/umc/product/notification/application/event/FcmNotificationOutboxFlowTest.java#L113) | FCM 요청 이벤트는 event_outbox에 저장되고 relay 후 배치 이벤트도 event_outbox에 저장된다 | 이력 저장을 요청한 발송과 첨부 이미지 URL, 활성 토큰 501개 | 이력 저장 호출과 500개·1개 발송 배치 등록, 기존 푸시 데이터와 딥링크 유지, 발송 배치에 `recordHistory` 미포함 |

### FcmNotificationRequestedEventListenerTest
- 위치: `monolith/src/test/java/com/umc/product/notification/application/event/FcmNotificationRequestedEventListenerTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [37](../../../monolith/src/test/java/com/umc/product/notification/application/event/FcmNotificationRequestedEventListenerTest.java#L37) | 활성 토큰을 500개 단위 배치 이벤트로 분리한다 | 이력 저장을 요청한 발송과 활성 토큰 501개 | 수신자 이력 저장을 호출하고 토큰을 500개·1개로 나누어 배치 등록 |
| [68](../../../monolith/src/test/java/com/umc/product/notification/application/event/FcmNotificationRequestedEventListenerTest.java#L68) | 활성 토큰이 없거나 FCM이 비활성화되어도 대상 회원의 알림을 저장한다 | 이력 저장을 요청한 발송에 활성 토큰이 없거나 FCM이 비활성 상태 | 수신자 이력 저장을 호출하고 발송 배치는 등록하지 않음 |

## Repository / Outbound Persistence

### NotificationHistoryPersistenceAdapterTest
- 위치: `monolith/src/test/java/com/umc/product/notification/adapter/out/persistentce/NotificationHistoryPersistenceAdapterTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [36](../../../monolith/src/test/java/com/umc/product/notification/adapter/out/persistentce/NotificationHistoryPersistenceAdapterTest.java#L36) | 회원 행을 삭제해도 알림 이력에는 수신 회원 ID가 남는다 | 회원 탈퇴 UseCase를 거치지 않고 회원 행을 직접 삭제 | DB에서 알림 이력이 자동 삭제되지 않음 |
| [58](../../../monolith/src/test/java/com/umc/product/notification/adapter/out/persistentce/NotificationHistoryPersistenceAdapterTest.java#L58) | 동일 발송 요청을 재처리하면 회원별 이력이 한 건씩 남고 별도 리마인더는 추가된다 | 두 회원의 동일 요청을 두 번 저장하고 한 회원에게 별도 리마인더 저장 | 기존 요청은 회원별 한 건, 대상 회원에게만 리마인더 이력 추가 |

## E2E / Integration

### NotificationHistoryQueryIntegrationTest
- 위치: `monolith/src/test/java/com/umc/product/notification/adapter/in/web/NotificationHistoryQueryIntegrationTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [46](../../../monolith/src/test/java/com/umc/product/notification/adapter/in/web/NotificationHistoryQueryIntegrationTest.java#L46) | 본인 알림을 생성 시각과 ID로 정렬하고 이미지 URL과 딥링크를 반환한다 | 동일 시각에 저장된 본인 알림과 다른 회원 알림을 두 페이지로 조회 | HTTP `200`, 본인 알림만 최신순으로 반환하고 이미지 URL·딥링크 제공 |
| [86](../../../monolith/src/test/java/com/umc/product/notification/adapter/in/web/NotificationHistoryQueryIntegrationTest.java#L86) | 다음 페이지를 보기 전에 새 알림이 저장되어도 기존 커서의 조회가 밀리지 않는다 | 첫 페이지를 받은 뒤 더 최신인 알림을 저장 | 다음 페이지는 기존 커서에서 이어지고 첫 페이지를 새로 조회하면 새 알림이 표시됨 |
| [122](../../../monolith/src/test/java/com/umc/product/notification/adapter/in/web/NotificationHistoryQueryIntegrationTest.java#L122) | 다른 회원의 커서와 존재하지 않는 커서는 모두 빈 목록을 반환한다 | 타인 알림 ID와 미존재 ID를 각각 커서로 전달 | HTTP `200`, 빈 목록, `hasNext=false`, `nextCursor=null` |
| [142](../../../monolith/src/test/java/com/umc/product/notification/adapter/in/web/NotificationHistoryQueryIntegrationTest.java#L142) | 인증 토큰 없이 호출하면 401을 반환한다 | 인증 토큰 없이 `GET /api/v1/notifications` 호출 | HTTP `401` |
| [148](../../../monolith/src/test/java/com/umc/product/notification/adapter/in/web/NotificationHistoryQueryIntegrationTest.java#L148) | 회원이 삭제되면 유효한 토큰이 남아 있어도 알림 이력을 조회할 수 없다 | 회원과 이력 생성 후 회원 행만 삭제하고 기존 토큰으로 호출 | HTTP `403`, 알림 이력 미반환 |
| [169](../../../monolith/src/test/java/com/umc/product/notification/adapter/in/web/NotificationHistoryQueryIntegrationTest.java#L169) | 잘못된 페이지 크기나 커서는 400을 반환한다 | `size=0`, `size=101`, `cursor=-1`, 문자열 커서를 각각 전달 | HTTP `400` |

### NotificationHistoryTransactionIntegrationTest
- 위치: `monolith/src/test/java/com/umc/product/notification/application/event/NotificationHistoryTransactionIntegrationTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [108](../../../monolith/src/test/java/com/umc/product/notification/application/event/NotificationHistoryTransactionIntegrationTest.java#L108) | 두 트랜잭션이 같은 요청과 회원의 이력을 저장하면 한 건만 커밋된다 | 동일 요청의 동일 수신자 이력을 동시에 저장 | 이력 한 건만 커밋 |
| [149](../../../monolith/src/test/java/com/umc/product/notification/application/event/NotificationHistoryTransactionIntegrationTest.java#L149) | FCM 배치 저장 단계가 실패하면 이력도 롤백되고 재시도에서 함께 커밋된다 | 이력과 발송 배치 등록 직후 예외 발생 | 첫 시도에서 둘 다 롤백되고 재시도에서 이미지 URL을 포함한 이력과 발송 배치가 함께 커밋됨 |
| [194](../../../monolith/src/test/java/com/umc/product/notification/application/event/NotificationHistoryTransactionIntegrationTest.java#L194) | 이력 저장이 실패하면 푸시를 보류하고 재시도로 이력을 커밋한 뒤에만 발송한다 | PostgreSQL에서 첫 이력 INSERT 실패 후 재시도 | 실패 시 배치 등록과 Firebase 호출이 없고 재시도에서 이력이 커밋된 뒤 Firebase 모의 호출이 한 번 실행됨 |
| [253](../../../monolith/src/test/java/com/umc/product/notification/application/event/NotificationHistoryTransactionIntegrationTest.java#L253) | 구버전 소비자가 신버전 요청을 먼저 처리하면 푸시 배치는 남고 알림 이력은 누락된다 | 이력 저장 기능이 없는 소비자로 동일 발송 요청 처리 | 기존 데이터와 딥링크가 담긴 푸시 배치 한 건 등록, 알림 이력 없음 |
| [303](../../../monolith/src/test/java/com/umc/product/notification/application/event/NotificationHistoryTransactionIntegrationTest.java#L303) | 탈퇴와 발송이 겹쳐도 탈퇴한 수신자만 제외되고 다른 수신자의 이력은 남는다 | 탈퇴 트랜잭션이 회원 잠금을 먼저 확보한 뒤 이력 저장 시도 | 탈퇴 회원의 기존 이력을 삭제하고 새 이력은 다른 수신자에게만 저장 |
| [353](../../../monolith/src/test/java/com/umc/product/notification/application/event/NotificationHistoryTransactionIntegrationTest.java#L353) | 알림 저장이 먼저 시작되면 탈퇴는 저장이 끝난 뒤 탈퇴 회원의 이력만 삭제한다 | 이력 저장 트랜잭션이 잠금을 먼저 확보한 뒤 탈퇴 시도 | 기존 이력과 방금 저장한 이력에서 탈퇴 회원의 수신분만 삭제 |
| [400](../../../monolith/src/test/java/com/umc/product/notification/application/event/NotificationHistoryTransactionIntegrationTest.java#L400) | 탈퇴 트랜잭션이 롤백되면 회원과 그 회원의 수신 알림 이력이 복구된다 | 탈퇴 처리로 회원과 수신 이력을 삭제한 뒤 예외 발생 | 롤백 후 회원과 수신 이력이 복구되고 다른 수신자의 이력도 조회됨 |
| [425](../../../monolith/src/test/java/com/umc/product/notification/application/event/NotificationHistoryTransactionIntegrationTest.java#L425) | 이력 기록을 요청하지 않은 일반 푸시는 공지 딥링크가 있어도 이력 없이 배치를 등록한다 | 이력 기록 지정 없이 공지 상세 딥링크가 담긴 푸시 요청 처리 | 기존 요청 형식으로 발송 배치 한 건 등록, 알림 이력 없음 |

## Domain

### FcmOutboxEventTest
- 위치: `monolith/src/test/java/com/umc/product/notification/domain/FcmOutboxEventTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [9](../../../monolith/src/test/java/com/umc/product/notification/domain/FcmOutboxEventTest.java#L9) | create는 매번 새로운 인스턴스와 새로운 eventId를 반환한다 | 조건 create는 매번 새로운 인스턴스와 새로운 eventId를 반환한다 | 성공: 검증 assertThat(first.eventId()).isNotEqualTo(second.eventId()); |
| [20](../../../monolith/src/test/java/com/umc/product/notification/domain/FcmOutboxEventTest.java#L20) | create로 생성된 이벤트의 occurredAt은 호출 시점 근처여야 한다 | 조건 create로 생성된 이벤트의 occurredAt은 호출 시점 근처여야 한다 | 성공: 검증 assertThat(event.occurredAt()).isBetween(before, after); |
| [34](../../../monolith/src/test/java/com/umc/product/notification/domain/FcmOutboxEventTest.java#L34) | eventType은 'fcm.outbox.created'이다 | 조건 eventType은 'fcm.outbox.created'이다 | 성공: 검증 assertThat(event.eventType()).isEqualTo("fcm.outbox.created"); |
