package com.umc.product.inquiry.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;

import com.umc.product.global.event.adapter.out.EventPayloadDeserializer;
import com.umc.product.global.event.adapter.out.persistence.EventOutboxJpaRepository;
import com.umc.product.global.event.domain.EventOutbox;
import com.umc.product.inquiry.adapter.out.persistence.InquiryStaffAlarmSettingJpaRepository;
import com.umc.product.inquiry.application.port.in.command.ChangeInquiryAssigneeUseCase;
import com.umc.product.inquiry.application.port.in.command.CloseInquiryUseCase;
import com.umc.product.inquiry.application.port.in.command.CreateInquiryUseCase;
import com.umc.product.inquiry.application.port.in.command.SendInquiryMessageUseCase;
import com.umc.product.inquiry.application.port.in.command.UpdateInquiryStaffAlarmSettingUseCase;
import com.umc.product.inquiry.application.port.in.command.dto.ChangeInquiryAssigneeCommand;
import com.umc.product.inquiry.application.port.in.command.dto.CloseInquiryCommand;
import com.umc.product.inquiry.application.port.in.command.dto.CreateInquiryCommand;
import com.umc.product.inquiry.application.port.in.command.dto.SendInquiryMessageCommand;
import com.umc.product.inquiry.application.port.in.command.dto.UpdateInquiryStaffAlarmSettingCommand;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryStaffAlarmSettingInfo;
import com.umc.product.member.domain.MemberSystemRoleType;
import com.umc.product.notification.application.event.FcmNotificationRequestedEvent;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.InquiryFixture;
import com.umc.product.support.fixture.MemberFixture;

@DisplayName("문의 운영진 알림")
class InquiryStaffAlarmIntegrationTest extends IntegrationTestSupport {

    private static final Set<String> INQUIRY_ALARM_EVENTS = Set.of(
        "inquiry.created", "inquiry.author.messaged", "inquiry.staff.replied", "inquiry.assigned"
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
    ChangeInquiryAssigneeUseCase changeInquiryAssigneeUseCase;
    @Autowired
    UpdateInquiryStaffAlarmSettingUseCase updateInquiryStaffAlarmSettingUseCase;
    @Autowired
    InquiryStaffAlarmSettingJpaRepository inquiryStaffAlarmSettingJpaRepository;
    @Autowired
    EventOutboxJpaRepository eventOutboxJpaRepository;
    @Autowired
    EventPayloadDeserializer eventPayloadDeserializer;
    @Autowired
    ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    MemberFixture memberFixture;
    @Autowired
    InquiryFixture inquiryFixture;

    private Long authorId;
    private Long staffId;
    private Long otherStaffId;
    private Long quietStaffId;
    private Long superAdminId;
    private Long categoryId;
    private long seenOutboxId;

    @BeforeEach
    void setUp() {
        authorId = memberFixture.일반("author").getId();
        staffId = 운영진("staff");
        otherStaffId = 운영진("other-staff");
        quietStaffId = 운영진("quiet-staff");
        superAdminId = memberFixture.일반("super-admin").getId();
        memberFixture.시스템_역할_부여(superAdminId, MemberSystemRoleType.SUPER_ADMIN);
        알림(staffId, true);
        알림(otherStaffId, true);
        알림(superAdminId, true);
        categoryId = inquiryFixture.카테고리("계정·로그인", 2).getId();
    }

    @Test
    @DisplayName("새 문의는 알림을 켠 UMC_PRODUCT 운영진에게만 보내고, 설정한 적 없는 운영진과 SUPER_ADMIN만 가진 회원은 뺀다")
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
    @DisplayName("새 문의 알림을 켠 운영진이 없으면 FCM 요청을 만들지 않는다")
    void 새_문의_대상_없음() {
        // given
        알림(staffId, false);
        알림(otherStaffId, false);

        // when
        등록(authorId, UUID.randomUUID());

        // then
        assertThat(알림_요청()).isEmpty();
    }

    @Test
    @DisplayName("작성자가 다시 쓰면 그 문의에 답한 운영진에게 알림 설정과 상관없이 보내고, 종료 뒤 재오픈도 같다")
    void 작성자_메시지_참여_운영진() {
        // given
        Long inquiryId = 등록(authorId, UUID.randomUUID());
        보내기(inquiryId, quietStaffId, UUID.randomUUID());
        closeInquiryUseCase.close(new CloseInquiryCommand(inquiryId, staffId));
        이전_이벤트_건너뛰기();

        // when
        보내기(inquiryId, authorId, UUID.randomUUID());

        // then
        assertThat(알림_요청()).singleElement().satisfies(request -> {
            assertThat(request.memberIds()).containsExactly(quietStaffId);
            assertThat(request.data()).containsEntry("type", "INQUIRY_AUTHOR_MESSAGE")
                .containsEntry("inquiryId", String.valueOf(inquiryId));
            assertThat(request.title()).isEqualTo("문의에 새 메시지가 왔어요");
            assertThat(request.body()).isEqualTo(TITLE);
        });
    }

    @Test
    @DisplayName("답한 운영진이 없는 문의에 작성자가 다시 쓰면 새 문의 알림을 켠 운영진에게 보낸다")
    void 작성자_메시지_참여자_없음() {
        // given
        Long inquiryId = 등록(authorId, UUID.randomUUID());
        이전_이벤트_건너뛰기();

        // when
        보내기(inquiryId, authorId, UUID.randomUUID());

        // then
        assertThat(알림_요청()).singleElement().satisfies(request -> {
            assertThat(request.memberIds()).containsExactlyInAnyOrder(staffId, otherStaffId);
            assertThat(request.data()).containsEntry("type", "INQUIRY_AUTHOR_MESSAGE");
        });
    }

    @Test
    @DisplayName("SUPER_ADMIN만 가진 회원만 답한 문의에 작성자가 다시 쓰면 참여 운영진이 없다고 보고 새 문의 알림을 켠 운영진에게 보낸다")
    void 슈퍼_어드민만_답한_문의() {
        // given
        Long inquiryId = 등록(authorId, UUID.randomUUID());
        보내기(inquiryId, superAdminId, UUID.randomUUID());
        이전_이벤트_건너뛰기();

        // when
        보내기(inquiryId, authorId, UUID.randomUUID());

        // then
        assertThat(알림_요청()).singleElement().satisfies(request -> {
            assertThat(request.memberIds()).containsExactlyInAnyOrder(staffId, otherStaffId);
            assertThat(request.data()).containsEntry("type", "INQUIRY_AUTHOR_MESSAGE");
        });
    }

    @Test
    @DisplayName("답했던 운영진이 UMC_PRODUCT 역할을 잃으면 작성자 메시지 알림에서 뺀다")
    void 권한_잃은_참여자() {
        // given
        Long inquiryId = 등록(authorId, UUID.randomUUID());
        보내기(inquiryId, quietStaffId, UUID.randomUUID());
        보내기(inquiryId, otherStaffId, UUID.randomUUID());
        jdbcTemplate.update(
            "delete from member_system_role where member_id = ? and role_type = 'UMC_PRODUCT'", quietStaffId
        );
        이전_이벤트_건너뛰기();

        // when
        보내기(inquiryId, authorId, UUID.randomUUID());

        // then
        assertThat(알림_요청()).singleElement()
            .satisfies(request -> assertThat(request.memberIds()).containsExactly(otherStaffId));
    }

    @Test
    @DisplayName("담당자가 있는 문의에 작성자가 다시 쓰면 답한 운영진이 있어도 담당자에게만 보낸다")
    void 작성자_메시지_담당자() {
        // given
        Long inquiryId = 등록(authorId, UUID.randomUUID());
        보내기(inquiryId, otherStaffId, UUID.randomUUID());
        담당(inquiryId, staffId, quietStaffId, null);
        이전_이벤트_건너뛰기();

        // when
        보내기(inquiryId, authorId, UUID.randomUUID());

        // then
        assertThat(알림_요청()).singleElement().satisfies(request -> {
            assertThat(request.memberIds()).containsExactly(quietStaffId);
            assertThat(request.data()).containsEntry("type", "INQUIRY_AUTHOR_MESSAGE");
        });
    }

    @Test
    @DisplayName("담당자가 UMC_PRODUCT 역할을 잃으면 미배정으로 보고 답한 운영진에게 보낸다")
    void 작성자_메시지_권한_잃은_담당자() {
        // given
        Long inquiryId = 등록(authorId, UUID.randomUUID());
        보내기(inquiryId, otherStaffId, UUID.randomUUID());
        담당(inquiryId, staffId, quietStaffId, null);
        jdbcTemplate.update(
            "delete from member_system_role where member_id = ? and role_type = 'UMC_PRODUCT'", quietStaffId
        );
        이전_이벤트_건너뛰기();

        // when
        보내기(inquiryId, authorId, UUID.randomUUID());

        // then
        assertThat(알림_요청()).singleElement()
            .satisfies(request -> assertThat(request.memberIds()).containsExactly(otherStaffId));
    }

    @Test
    @DisplayName("다른 운영진이 담당자로 지정하면 지정된 사람에게 알리고, 스스로 맡으면 알리지 않는다")
    void 담당자_지정_알림() {
        // given
        Long inquiryId = 등록(authorId, UUID.randomUUID());
        Long selfAssignedId = 등록(authorId, UUID.randomUUID());
        이전_이벤트_건너뛰기();

        // when
        담당(inquiryId, staffId, quietStaffId, null);
        담당(selfAssignedId, staffId, staffId, null);

        // then
        assertThat(알림_요청()).singleElement().satisfies(request -> {
            assertThat(request.memberIds()).containsExactly(quietStaffId);
            assertThat(request.data()).containsEntry("type", "INQUIRY_ASSIGNED")
                .containsEntry("inquiryId", String.valueOf(inquiryId));
            assertThat(request.title()).isEqualTo("문의를 맡았어요");
            assertThat(request.body()).isEqualTo(TITLE);
            assertThat(request.deepLink()).isEqualTo("umc://inquiry/" + inquiryId);
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
    @DisplayName("운영진이 자기 문의를 쓰면 작성자로 보고 본인을 빼고 알린다")
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
    @DisplayName("설정 행이 없는 운영진이 알림 켜기를 동시에 두 번 보내도 둘 다 성공하고 설정 행은 하나다")
    void 첫_설정_동시_변경() throws Exception {
        // given
        Long racingStaffId = 운영진("racing-staff");
        var command = new UpdateInquiryStaffAlarmSettingCommand(racingStaffId, true);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = CompletableFuture.supplyAsync(() -> 동시_변경(ready, start, command), executor);
            var second = CompletableFuture.supplyAsync(() -> 동시_변경(ready, start, command), executor);
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();

            // when
            start.countDown();
            var results = List.of(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS));

            // then
            assertThat(results).extracting(InquiryStaffAlarmSettingInfo::enabled).containsExactly(true, true);
            assertThat(inquiryStaffAlarmSettingJpaRepository.findAll())
                .filteredOn(setting -> setting.getMemberId().equals(racingStaffId))
                .singleElement()
                .satisfies(setting -> assertThat(setting.isEnabled()).isTrue());
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private void 담당(Long inquiryId, Long actorId, Long assigneeId, Long expectedAssigneeId) {
        changeInquiryAssigneeUseCase.changeAssignee(
            new ChangeInquiryAssigneeCommand(inquiryId, actorId, assigneeId, expectedAssigneeId)
        );
    }

    private void 알림(Long memberId, boolean enabled) {
        updateInquiryStaffAlarmSettingUseCase.update(new UpdateInquiryStaffAlarmSettingCommand(memberId, enabled));
    }

    private InquiryStaffAlarmSettingInfo 동시_변경(
        CountDownLatch ready, CountDownLatch start, UpdateInquiryStaffAlarmSettingCommand command
    ) {
        ready.countDown();
        try {
            if (!start.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("동시 설정 변경 시작 대기 시간 초과");
            }
            return updateInquiryStaffAlarmSettingUseCase.update(command);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private Long 운영진(String name) {
        Long memberId = memberFixture.일반(name).getId();
        memberFixture.시스템_역할_부여(memberId, MemberSystemRoleType.UMC_PRODUCT);
        return memberId;
    }

    private Long 등록(Long memberId, UUID clientRequestId) {
        return createInquiryUseCase.create(new CreateInquiryCommand(
            memberId, categoryId, null, TITLE + "\n로그인 버튼을 눌러도 반응이 없어요", List.of(), clientRequestId
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
