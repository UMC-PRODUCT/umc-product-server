# Notification 테스트 케이스

`notification` 패키지의 테스트 파일 26개와 테스트 메서드 96개를 정리한다.
`@ParameterizedTest`는 입력별 실행 건수와 관계없이 메서드 하나로 집계한다.

## 알림 이력과 푸시 발송

| 테스트 | 메서드 수 | 검증 범위 |
|---|---:|---|
| [NotificationHistoryQueryIntegrationTest](../../../monolith/src/test/java/com/umc/product/notification/adapter/in/web/NotificationHistoryQueryIntegrationTest.java) | 6 | 본인 알림의 최신순·동일 시각 ID 정렬과 이미지·딥링크 응답, 새 알림 추가 후 커서 이어보기, 타인·미존재 커서의 빈 응답, 비인증 `401`, 탈퇴 회원 `403`, 잘못된 파라미터 `400` |
| [NotificationHistoryPersistenceAdapterTest](../../../monolith/src/test/java/com/umc/product/notification/adapter/out/persistentce/NotificationHistoryPersistenceAdapterTest.java) | 2 | 회원 행 삭제의 DB 자동 전파 여부, 동일 요청의 회원별 중복 방지와 별도 리마인더 저장 |
| [NotificationHistoryTransactionIntegrationTest](../../../monolith/src/test/java/com/umc/product/notification/application/event/NotificationHistoryTransactionIntegrationTest.java) | 7 | 동시 저장 중복 방지, 배치 등록 실패 후 롤백과 재시도, 이력 저장 실패 시 발송 보류 및 이력 커밋 후 Firebase 호출, 구버전 소비자의 이력 누락, 저장·탈퇴가 각각 먼저인 경우의 수신자별 정리, 탈퇴 롤백 시 회원과 이력 복구 |
| [FcmAdminControllerTest](../../../monolith/src/test/java/com/umc/product/notification/adapter/in/web/FcmAdminControllerTest.java) | 3 | 기존 관리자 발송 요청의 `202` 응답과 푸시 데이터·딥링크, 빈 대상 및 `null` 파트 거부 |
| [FcmNotificationCommandServiceTest](../../../monolith/src/test/java/com/umc/product/notification/application/service/FcmNotificationCommandServiceTest.java) | 1 | FCM 요청을 outbox 이벤트로 발행 |
| [FcmNotificationRequestedEventListenerTest](../../../monolith/src/test/java/com/umc/product/notification/application/event/FcmNotificationRequestedEventListenerTest.java) | 2 | 활성 토큰 500개 단위 배치, 활성 토큰이 없거나 FCM이 비활성화된 경우의 이력 저장 |
| [FcmNotificationOutboxFlowTest](../../../monolith/src/test/java/com/umc/product/notification/application/event/FcmNotificationOutboxFlowTest.java) | 3 | 기존 outbox JSON 역직렬화, 요청 트랜잭션에서 배치 등록, 트랜잭션 밖 FCM 발송과 부분 실패 토큰 재시도 |
| [FcmSendBatchRequestedEventListenerTest](../../../monolith/src/test/java/com/umc/product/notification/application/event/FcmSendBatchRequestedEventListenerTest.java) | 3 | 미등록 토큰 비활성화, 부분 실패 토큰만 재시도, 일시적 발송 예외의 outbox 재시도 전파 |
| [CommunityAlarmEventListenerTest](../../../monolith/src/test/java/com/umc/product/notification/adapter/in/event/CommunityAlarmEventListenerTest.java) | 14 | 채팅·초대 수신자 선정, 음소거·차단·멘션 처리, 알림 생략 조건, 본문 대체 문구, 제목·본문 길이 제한, 중복 요청 방지 |
| [InquiryAlarmEventListenerTest](../../../monolith/src/test/java/com/umc/product/notification/adapter/in/event/InquiryAlarmEventListenerTest.java) | 4 | 운영진 답변·새 문의·작성자 메시지의 대상과 푸시 데이터, 대상이 없는 경우 건너뛰기 |

## FCM 토큰과 발송 권한

| 테스트 | 메서드 수 | 검증 범위 |
|---|---:|---|
| [FcmControllerTest](../../../monolith/src/test/java/com/umc/product/notification/adapter/in/web/FcmControllerTest.java) | 5 | installation 등록·해제, 필수 `installationId` 검증, token-only 등록·해제 API 비활성화 |
| [FcmServiceTest](../../../monolith/src/test/java/com/umc/product/notification/application/service/FcmServiceTest.java) | 8 | 신규 토큰 활성화, 동일 토큰 재등록, 새 기기 토큰 추가, 토큰 회전, installation 소유권 이전, 중복 토큰 비활성화, 회원별 해제와 이전 소유자의 늦은 로그아웃 |
| [FcmTokenTest](../../../monolith/src/test/java/com/umc/product/notification/domain/FcmTokenTest.java) | 2 | 토큰 생성·재등록 시 등록 및 검증 시각 갱신과 활성화 |
| [FcmTokenValidationServiceTest](../../../monolith/src/test/java/com/umc/product/notification/application/service/FcmTokenValidationServiceTest.java) | 3 | FCM dry-run 검증, 유효하지 않은 토큰 비활성화, 일시 실패의 검증 시각 처리, 비활성 환경의 조회 생략 |
| [FcmTokenValidationSchedulerTest](../../../monolith/src/test/java/com/umc/product/notification/adapter/in/scheduler/FcmTokenValidationSchedulerTest.java) | 2 | 설정에 따른 토큰 검증 UseCase 호출 |
| [FirebaseFcmMessageAdapterTest](../../../monolith/src/test/java/com/umc/product/notification/adapter/out/external/fcm/FirebaseFcmMessageAdapterTest.java) | 1 | 멀티캐스트 부분 실패를 무효 토큰과 재시도 가능 토큰으로 분류 |
| [FirebaseFcmTokenValidationAdapterTest](../../../monolith/src/test/java/com/umc/product/notification/adapter/out/external/fcm/FirebaseFcmTokenValidationAdapterTest.java) | 1 | dry-run 멀티캐스트와 미등록 토큰 ID 반환 |
| [FcmPermissionEvaluatorTest](../../../monolith/src/test/java/com/umc/product/notification/application/service/evaluator/FcmPermissionEvaluatorTest.java) | 2 | 중앙운영사무국 총괄단과 일반 운영국원의 FCM 발송 권한 |

## 토픽 기반 FCM outbox

| 테스트 | 메서드 수 | 검증 범위 |
|---|---:|---|
| [FcmOutboxServiceTest](../../../monolith/src/test/java/com/umc/product/notification/application/service/FcmOutboxServiceTest.java) | 4 | `SUBSCRIBE`·`UNSUBSCRIBE` 이벤트의 즉시 `FAILED` 처리, 빈 `PENDING` 큐 건너뛰기, 여러 `PENDING` 이벤트의 `FAILED` 처리 |
| [FcmOutboxEventTest](../../../monolith/src/test/java/com/umc/product/notification/domain/FcmOutboxEventTest.java) | 3 | 생성 시 새 `eventId` 및 호출 시점의 `occurredAt`, `fcm.outbox.created` 이벤트 타입 |

## 이메일

| 테스트 | 메서드 수 | 검증 범위 |
|---|---:|---|
| [SendEmailServiceTest](../../../monolith/src/test/java/com/umc/product/notification/application/service/SendEmailServiceTest.java) | 2 | 인증 메일의 순수 텍스트와 제목, HTML 템플릿 및 변수 렌더링 |
| [EmailProviderConfigurationTest](../../../monolith/src/test/java/com/umc/product/notification/adapter/out/external/EmailProviderConfigurationTest.java) | 9 | SES·SMTP·LOG 제공자 선택, 운영 LOG 제한, 자격증명·TLS·신뢰 인증서 설정 검증 |
| [LogEmailAdapterTest](../../../monolith/src/test/java/com/umc/product/notification/adapter/out/external/log/LogEmailAdapterTest.java) | 1 | LOG 제공자의 실제 발송 생략과 로그 노출 범위 |
| [SesEmailAdapterTest](../../../monolith/src/test/java/com/umc/product/notification/adapter/out/external/ses/SesEmailAdapterTest.java) | 2 | UTF-8 본문, HTML 및 Configuration Set 발송 설정 |
| [SmtpEmailAdapterTest](../../../monolith/src/test/java/com/umc/product/notification/adapter/out/external/smtp/SmtpEmailAdapterTest.java) | 3 | 발신자 표시명·한국어 제목·HTML, UTF-8 인증 메일, 실패 예외와 로그의 민감정보 처리 |
| [SmtpTrustedCertificateTest](../../../monolith/src/test/java/com/umc/product/notification/adapter/out/external/smtp/SmtpTrustedCertificateTest.java) | 3 | 자체 서명 인증서 거부, 지정한 신뢰 인증서로 STARTTLS 발송, 호스트 이름 불일치 거부 |
