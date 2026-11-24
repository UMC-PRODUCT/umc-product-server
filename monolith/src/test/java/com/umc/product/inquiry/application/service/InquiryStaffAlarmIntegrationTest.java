package com.umc.product.inquiry.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import com.umc.product.global.event.adapter.out.EventPayloadDeserializer;
import com.umc.product.global.event.adapter.out.persistence.EventOutboxJpaRepository;
import com.umc.product.global.event.domain.EventOutbox;
import com.umc.product.inquiry.application.port.in.command.CloseInquiryUseCase;
import com.umc.product.inquiry.application.port.in.command.CreateInquiryUseCase;
import com.umc.product.inquiry.application.port.in.command.SendInquiryMessageUseCase;
import com.umc.product.inquiry.application.port.in.command.UpdateInquiryStaffAlarmSettingUseCase;
import com.umc.product.inquiry.application.port.in.command.dto.CloseInquiryCommand;
import com.umc.product.inquiry.application.port.in.command.dto.CreateInquiryCommand;
import com.umc.product.inquiry.application.port.in.command.dto.SendInquiryMessageCommand;
import com.umc.product.inquiry.application.port.in.command.dto.UpdateInquiryStaffAlarmSettingCommand;
import com.umc.product.member.domain.MemberSystemRoleType;
import com.umc.product.notification.application.event.FcmNotificationRequestedEvent;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.InquiryFixture;
import com.umc.product.support.fixture.MemberFixture;

@DisplayName("문의 운영진 알림")
class InquiryStaffAlarmIntegrationTest extends IntegrationTestSupport {

    private static final Set<String> INQUIRY_ALARM_EVENTS = Set.of(
        "inquiry.created", "inquiry.author.messaged", "inquiry.staff.replied"
    );
    private static final String FCM_REQUESTED = "notification.fcm.requested";
    private static final String TITLE = "앱 로그인이 안 돼요";

    @Autowired
    CreateInquiryUseCase createInquiryUseCase;
    @Autowired
    SendInquiryMessageUseCase sendInquiryMessageUseCase;
    @Autowired
    CloseInquiryUseCase closeInquiryUseCase;
    @Autowired
    UpdateInquiryStaffAlarmSettingUseCase updateInquiryStaffAlarmSettingUseCase;
    @Autowired
    EventOutboxJpaRepository eventOutboxJpaRepository;
    @Autowired
    EventPayloadDeserializer eventPayloadDeserializer;
    @Autowired
    ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    MemberFixture memberFixture;
    @Autowired
    InquiryFixture inquiryFixture;

    private Long authorId;
    private Long staffId;
    private Long otherStaffId;
    private Long mutedStaffId;
    private Long categoryId;
    private long seenOutboxId;

    @BeforeEach
    void setUp() {
        authorId = memberFixture.일반("author").getId();
        staffId = 운영진("staff");
        otherStaffId = 운영진("other-staff");
        mutedStaffId = 운영진("muted-staff");
        Long superAdminId = memberFixture.일반("super-admin").getId();
        memberFixture.시스템_역할_부여(superAdminId, MemberSystemRoleType.SUPER_ADMIN);
        updateInquiryStaffAlarmSettingUseCase.update(new UpdateInquiryStaffAlarmSettingCommand(mutedStaffId, false));
        categoryId = inquiryFixture.카테고리("UMC 서비스", 1).getId();
    }

    @Test
    @DisplayName("새 문의는 알림을 켠 UMC_PRODUCT 운영진에게만 보내고, SUPER_ADMIN만 가진 회원과 알림을 끈 운영진은 뺀다")
    void 새_문의_알림() {
        // when
        Long inquiryId = 등록(authorId, UUID.randomUUID());

        // then
        assertThat(알림_요청()).singleElement().satisfies(request -> {
            assertThat(request.memberIds()).containsExactlyInAnyOrder(staffId, otherStaffId);
            assertThat(request.data()).containsEntry("type", "INQUIRY_NEW")
                .containsEntry("inquiryId", String.valueOf(inquiryId));
            assertThat(request.title()).isEqualTo("새 문의가 왔어요");
            assertThat(request.body()).isEqualTo(TITLE);
            assertThat(request.deepLink()).isEqualTo("umc://inquiry/" + inquiryId);
        });
    }

    @Test
    @DisplayName("작성자가 종료된 문의에 다시 쓰면 같은 운영진에게 새 메시지 알림을 보낸다")
    void 작성자_메시지_알림() {
        // given
        Long inquiryId = 등록(authorId, UUID.randomUUID());
        closeInquiryUseCase.close(new CloseInquiryCommand(inquiryId, staffId));
        이전_이벤트_건너뛰기();

        // when
        보내기(inquiryId, authorId, UUID.randomUUID());

        // then
        assertThat(알림_요청()).singleElement().satisfies(request -> {
            assertThat(request.memberIds()).containsExactlyInAnyOrder(staffId, otherStaffId);
            assertThat(request.data()).containsEntry("type", "INQUIRY_AUTHOR_MESSAGE")
                .containsEntry("inquiryId", String.valueOf(inquiryId));
            assertThat(request.title()).isEqualTo("문의에 새 메시지가 왔어요");
            assertThat(request.body()).isEqualTo(TITLE);
        });
    }

    @Test
    @DisplayName("운영진 메시지는 작성자에게만 알리고 운영진에게는 알리지 않는다")
    void 운영진_메시지() {
        // given
        Long inquiryId = 등록(authorId, UUID.randomUUID());
        이전_이벤트_건너뛰기();

        // when
        보내기(inquiryId, staffId, UUID.randomUUID());

        // then
        assertThat(알림_요청()).singleElement().satisfies(request -> {
            assertThat(request.memberIds()).containsExactly(authorId);
            assertThat(request.data()).containsEntry("type", "INQUIRY_REPLY");
        });
    }

    @Test
    @DisplayName("같은 clientRequestId·clientMessageId 재전송은 운영진 알림을 다시 보내지 않는다")
    void 재전송_알림_없음() {
        // given
        UUID clientRequestId = UUID.randomUUID();
        UUID clientMessageId = UUID.randomUUID();
        Long inquiryId = 등록(authorId, clientRequestId);
        보내기(inquiryId, authorId, clientMessageId);

        // when
        등록(authorId, clientRequestId);
        보내기(inquiryId, authorId, clientMessageId);

        // then
        assertThat(알림_요청()).extracting(request -> request.data().get("type"))
            .containsExactlyInAnyOrder("INQUIRY_NEW", "INQUIRY_AUTHOR_MESSAGE");
    }

    @Test
    @DisplayName("운영진이 자기 문의를 쓰면 작성자로 보고 다른 운영진에게만 알린다")
    void 운영진_본인_문의() {
        // when
        Long inquiryId = 등록(staffId, UUID.randomUUID());
        보내기(inquiryId, staffId, UUID.randomUUID());

        // then
        assertThat(알림_요청()).hasSize(2).allSatisfy(request -> {
            assertThat(request.memberIds()).containsExactly(otherStaffId);
            assertThat(request.data().get("type")).isIn("INQUIRY_NEW", "INQUIRY_AUTHOR_MESSAGE");
        });
    }

    @Test
    @DisplayName("알릴 운영진이 없으면 FCM 요청을 만들지 않는다")
    void 대상_없음() {
        // given
        updateInquiryStaffAlarmSettingUseCase.update(new UpdateInquiryStaffAlarmSettingCommand(staffId, false));
        updateInquiryStaffAlarmSettingUseCase.update(new UpdateInquiryStaffAlarmSettingCommand(otherStaffId, false));

        // when
        등록(authorId, UUID.randomUUID());

        // then
        assertThat(알림_요청()).isEmpty();
    }

    private Long 운영진(String name) {
        Long memberId = memberFixture.일반(name).getId();
        memberFixture.시스템_역할_부여(memberId, MemberSystemRoleType.UMC_PRODUCT);
        return memberId;
    }

    private Long 등록(Long memberId, UUID clientRequestId) {
        return createInquiryUseCase.create(new CreateInquiryCommand(
            memberId, categoryId, null, TITLE, "로그인 버튼을 눌러도 반응이 없어요", List.of(), clientRequestId
        ));
    }

    private void 보내기(Long inquiryId, Long senderId, UUID clientMessageId) {
        sendInquiryMessageUseCase.send(
            new SendInquiryMessageCommand(inquiryId, senderId, "추가 내용이에요", List.of(), clientMessageId)
        );
    }

    /**
     * 테스트 컨텍스트는 outbox relay를 끄므로, relay가 하듯 문의 이벤트를 꺼내 발행하고 그 결과로 남은 FCM 요청을 읽는다.
     */
    private List<FcmNotificationRequestedEvent> 알림_요청() {
        outbox().stream()
            .filter(row -> INQUIRY_ALARM_EVENTS.contains(row.getEventType()))
            .forEach(row -> applicationEventPublisher.publishEvent(eventPayloadDeserializer.deserialize(row)));
        return outbox().stream()
            .filter(row -> FCM_REQUESTED.equals(row.getEventType()))
            .map(row -> (FcmNotificationRequestedEvent) eventPayloadDeserializer.deserialize(row))
            .toList();
    }

    private void 이전_이벤트_건너뛰기() {
        seenOutboxId = eventOutboxJpaRepository.findAll().stream()
            .mapToLong(EventOutbox::getId)
            .max()
            .orElse(0L);
    }

    private List<EventOutbox> outbox() {
        return eventOutboxJpaRepository.findAll().stream()
            .filter(row -> row.getId() > seenOutboxId)
            .toList();
    }
}
