package com.umc.product.inquiry.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.umc.product.chat.adapter.out.persistence.ChatMessageJpaRepository;
import com.umc.product.chat.domain.MessageContentType;
import com.umc.product.chat.domain.exception.ChatErrorCode;
import com.umc.product.global.event.adapter.out.persistence.EventOutboxJpaRepository;
import com.umc.product.inquiry.application.port.in.command.CloseInquiryUseCase;
import com.umc.product.inquiry.application.port.in.command.CreateInquiryUseCase;
import com.umc.product.inquiry.application.port.in.command.SendInquiryMessageUseCase;
import com.umc.product.inquiry.application.port.in.command.dto.CloseInquiryCommand;
import com.umc.product.inquiry.application.port.in.command.dto.CreateInquiryCommand;
import com.umc.product.inquiry.application.port.in.command.dto.SendInquiryMessageCommand;
import com.umc.product.inquiry.application.port.in.query.GetInquiryDetailUseCase;
import com.umc.product.inquiry.application.port.in.query.GetInquiryMessagesUseCase;
import com.umc.product.inquiry.application.port.in.query.ListMyInquiriesUseCase;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryDetailInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryDetailQuery;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryMessageInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryMessagePageInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryMessagesQuery;
import com.umc.product.inquiry.application.port.in.query.dto.InquirySummaryInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquirySummaryPageInfo;
import com.umc.product.inquiry.application.port.in.query.dto.MyInquiriesQuery;
import com.umc.product.inquiry.domain.enums.InquiryParticipant;
import com.umc.product.inquiry.domain.enums.InquiryStatus;
import com.umc.product.inquiry.domain.enums.InquiryStatusGroup;
import com.umc.product.inquiry.domain.enums.InquiryTeam;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;
import com.umc.product.storage.domain.FileMetadata;
import com.umc.product.storage.domain.enums.FileCategory;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.InquiryFixture;
import com.umc.product.support.fixture.MemberFixture;

class InquiryCommandIntegrationTest extends IntegrationTestSupport {

    private static final String STAFF_REPLIED = "inquiry.staff.replied";

    @Autowired
    CreateInquiryUseCase createInquiryUseCase;
    @Autowired
    SendInquiryMessageUseCase sendInquiryMessageUseCase;
    @Autowired
    CloseInquiryUseCase closeInquiryUseCase;
    @Autowired
    GetInquiryDetailUseCase getInquiryDetailUseCase;
    @Autowired
    GetInquiryMessagesUseCase getInquiryMessagesUseCase;
    @Autowired
    ListMyInquiriesUseCase listMyInquiriesUseCase;
    @Autowired
    EventOutboxJpaRepository eventOutboxJpaRepository;
    @Autowired
    ChatMessageJpaRepository chatMessageJpaRepository;
    @Autowired
    MemberFixture memberFixture;
    @Autowired
    InquiryFixture inquiryFixture;

    private Long authorId;
    private Long staffId;
    private Long strangerId;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        authorId = memberFixture.일반("author").getId();
        staffId = memberFixture.일반("staff").getId();
        strangerId = memberFixture.일반("stranger").getId();
        inquiryFixture.운영진으로_지정(staffId);
        categoryId = inquiryFixture.카테고리("UMC 서비스", 1).getId();
    }

    @Test
    @DisplayName("같은 clientRequestId로 다시 등록하면 새로 만들지 않고 기존 문의를 돌려준다")
    void 등록_멱등() {
        // given
        UUID clientRequestId = UUID.randomUUID();

        // when
        Long first = createInquiryUseCase.create(등록_요청(clientRequestId));
        Long retried = createInquiryUseCase.create(등록_요청(clientRequestId));

        // then
        assertThat(retried).isEqualTo(first);
        InquiryMessagePageInfo messages = 메시지_조회(first, authorId);
        assertThat(messages.messages()).singleElement()
            .satisfies(message -> {
                assertThat(message.content()).isEqualTo("로그인 버튼을 눌러도 반응이 없어요");
                assertThat(message.senderType()).isEqualTo(InquiryParticipant.AUTHOR);
            });
        assertThat(상세(first, authorId).status()).isEqualTo(InquiryStatus.RECEIVED);
    }

    @Test
    @DisplayName("작성자와 운영진은 문의를 볼 수 있고, 제3자는 볼 수도 보낼 수도 없다")
    void 접근_권한() {
        // given
        Long inquiryId = 문의_등록();

        // when & then
        assertThat(상세(inquiryId, authorId).canClose()).isFalse();
        assertThat(상세(inquiryId, staffId).canClose()).isTrue();
        assertThat(메시지_조회(inquiryId, staffId).messages()).hasSize(1);

        assertThatThrownBy(() -> 상세(inquiryId, strangerId))
            .extracting("baseCode").isEqualTo(InquiryErrorCode.INQUIRY_ACCESS_DENIED);
        assertThatThrownBy(() -> 메시지_조회(inquiryId, strangerId))
            .extracting("baseCode").isEqualTo(InquiryErrorCode.INQUIRY_ACCESS_DENIED);
        assertThatThrownBy(() -> 보내기(inquiryId, strangerId, "끼어들기", UUID.randomUUID()))
            .extracting("baseCode").isEqualTo(InquiryErrorCode.INQUIRY_ACCESS_DENIED);
    }

    @Test
    @DisplayName("운영진이 처음 답하면 진행 중이 되고 작성자 알림 이벤트가 한 번 남는다")
    void 운영진_첫_답변() {
        // given
        Long inquiryId = 문의_등록();

        // when
        InquiryMessageInfo reply = 보내기(inquiryId, staffId, "확인해 볼게요", UUID.randomUUID());

        // then
        assertThat(reply.senderType()).isEqualTo(InquiryParticipant.STAFF);
        assertThat(상세(inquiryId, authorId).status()).isEqualTo(InquiryStatus.IN_PROGRESS);
        assertThat(이벤트_수(STAFF_REPLIED)).isEqualTo(1);
    }

    @Test
    @DisplayName("작성자 메시지는 상태를 바꾸지 않고 알림 이벤트도 남기지 않는다")
    void 작성자_추가_메시지() {
        // given
        Long inquiryId = 문의_등록();

        // when
        보내기(inquiryId, authorId, "추가로 스크린샷도 보내요", UUID.randomUUID());

        // then
        assertThat(상세(inquiryId, authorId).status()).isEqualTo(InquiryStatus.RECEIVED);
        assertThat(이벤트_수(STAFF_REPLIED)).isZero();
    }

    @Test
    @DisplayName("종료된 문의에 작성자가 메시지를 보내면 다시 진행 중이 된다")
    void 종료_후_작성자_메시지() {
        // given
        Long inquiryId = 문의_등록();
        closeInquiryUseCase.close(new CloseInquiryCommand(inquiryId, staffId));
        assertThat(상세(inquiryId, authorId).closedAt()).isNotNull();

        // when
        보내기(inquiryId, authorId, "아직 해결이 안 됐어요", UUID.randomUUID());

        // then
        InquiryDetailInfo detail = 상세(inquiryId, authorId);
        assertThat(detail.status()).isEqualTo(InquiryStatus.IN_PROGRESS);
        assertThat(detail.closedAt()).isNull();
    }

    @Test
    @DisplayName("같은 clientMessageId로 다시 보내면 메시지·상태 전이·알림을 다시 하지 않는다")
    void 메시지_재전송_멱등() {
        // given
        Long inquiryId = 문의_등록();
        UUID clientMessageId = UUID.randomUUID();
        InquiryMessageInfo first = 보내기(inquiryId, staffId, "확인해 볼게요", clientMessageId);
        closeInquiryUseCase.close(new CloseInquiryCommand(inquiryId, staffId));

        // when
        InquiryMessageInfo retried = 보내기(inquiryId, staffId, "확인해 볼게요", clientMessageId);

        // then
        assertThat(retried.messageId()).isEqualTo(first.messageId());
        assertThat(상세(inquiryId, authorId).status()).isEqualTo(InquiryStatus.CLOSED);
        assertThat(이벤트_수(STAFF_REPLIED)).isEqualTo(1);
        assertThat(메시지_조회(inquiryId, authorId).messages()).hasSize(2);
    }

    @Test
    @DisplayName("문의 종료는 운영진만 할 수 있고, 이미 종료된 문의는 다시 종료할 수 없다")
    void 종료_권한() {
        // given
        Long inquiryId = 문의_등록();

        // when & then
        assertThatThrownBy(() -> closeInquiryUseCase.close(new CloseInquiryCommand(inquiryId, authorId)))
            .extracting("baseCode").isEqualTo(InquiryErrorCode.INQUIRY_STAFF_ONLY);

        closeInquiryUseCase.close(new CloseInquiryCommand(inquiryId, staffId));
        assertThat(상세(inquiryId, staffId).canClose()).isFalse();
        assertThatThrownBy(() -> closeInquiryUseCase.close(new CloseInquiryCommand(inquiryId, staffId)))
            .isInstanceOf(InquiryDomainException.class)
            .extracting("baseCode").isEqualTo(InquiryErrorCode.INQUIRY_ALREADY_CLOSED);
    }

    @Test
    @DisplayName("내 문의 목록은 진행 중과 종료를 나누고 마지막 메시지 최신순으로 커서를 이어 간다")
    void 내_문의_목록() {
        // given
        Long older = 문의_등록();
        Long closed = 문의_등록();
        Long newer = 문의_등록();
        closeInquiryUseCase.close(new CloseInquiryCommand(closed, staffId));
        보내기(older, staffId, "답변이에요", UUID.randomUUID());

        // when
        InquirySummaryPageInfo firstPage = listMyInquiriesUseCase.listMine(
            new MyInquiriesQuery(authorId, InquiryStatusGroup.OPEN, null, 1)
        );
        InquirySummaryPageInfo secondPage = listMyInquiriesUseCase.listMine(
            new MyInquiriesQuery(authorId, InquiryStatusGroup.OPEN, firstPage.nextCursor(), 1)
        );
        InquirySummaryPageInfo closedPage = listMyInquiriesUseCase.listMine(
            new MyInquiriesQuery(authorId, InquiryStatusGroup.CLOSED, null, 20)
        );

        // then
        assertThat(firstPage.inquiries()).extracting(InquirySummaryInfo::inquiryId).containsExactly(older);
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(secondPage.inquiries()).extracting(InquirySummaryInfo::inquiryId).containsExactly(newer);
        assertThat(secondPage.hasNext()).isFalse();
        assertThat(closedPage.inquiries()).extracting(InquirySummaryInfo::inquiryId).containsExactly(closed);
        assertThat(firstPage.inquiries().get(0).category().name()).isEqualTo("UMC 서비스");
        assertThat(listMyInquiriesUseCase.listMine(new MyInquiriesQuery(staffId, InquiryStatusGroup.OPEN, null, 20))
            .inquiries()).isEmpty();
    }

    @Test
    @DisplayName("문의 첨부파일은 메시지에 담기고, 다른 용도로 올린 파일은 첨부할 수 없다")
    void 첨부파일() {
        // given
        Long inquiryId = 문의_등록();
        FileMetadata attachment = inquiryFixture.업로드된_파일(authorId, FileCategory.INQUIRY_ATTACHMENT);
        FileMetadata profileImage = inquiryFixture.업로드된_파일(authorId, FileCategory.PROFILE_IMAGE);

        // when
        InquiryMessageInfo sent = sendInquiryMessageUseCase.send(new SendInquiryMessageCommand(
            inquiryId, authorId, null, List.of(attachment.getId()), UUID.randomUUID()
        ));

        // then
        assertThat(sent.attachments()).singleElement()
            .satisfies(file -> assertThat(file.fileId()).isEqualTo(attachment.getId()));
        assertThatThrownBy(() -> sendInquiryMessageUseCase.send(new SendInquiryMessageCommand(
            inquiryId, authorId, "프로필 사진", List.of(profileImage.getId()), UUID.randomUUID()
        ))).extracting("baseCode").isEqualTo(InquiryErrorCode.INVALID_INQUIRY_ATTACHMENT);
    }

    @Test
    @DisplayName("첨부가 모두 이미지면 IMAGE, 문서면 FILE 메시지로 남기고, 둘을 섞으면 거절한다")
    void 첨부_종류() {
        // given
        Long inquiryId = 문의_등록();
        FileMetadata image = inquiryFixture.업로드된_파일(authorId, FileCategory.INQUIRY_ATTACHMENT);
        FileMetadata pdf = inquiryFixture.업로드된_파일(
            authorId, FileCategory.INQUIRY_ATTACHMENT, "manual.pdf", "application/pdf"
        );

        // when
        InquiryMessageInfo imageMessage = 첨부_보내기(inquiryId, List.of(image.getId()));
        InquiryMessageInfo fileMessage = 첨부_보내기(inquiryId, List.of(pdf.getId()));

        // then
        assertThat(chatMessageJpaRepository.findById(imageMessage.messageId()).orElseThrow().getContentType())
            .isEqualTo(MessageContentType.IMAGE);
        assertThat(chatMessageJpaRepository.findById(fileMessage.messageId()).orElseThrow().getContentType())
            .isEqualTo(MessageContentType.FILE);
        assertThatThrownBy(() -> 첨부_보내기(inquiryId, List.of(image.getId(), pdf.getId())))
            .extracting("baseCode").isEqualTo(InquiryErrorCode.INQUIRY_ATTACHMENT_MIXED_TYPES);
    }

    @Test
    @DisplayName("Chat 첨부 정책이 받지 않는 파일은 문의 첨부로 올렸어도 보낼 수 없다")
    void 첨부_Chat_정책() {
        // given
        Long inquiryId = 문의_등록();
        FileMetadata disguised = inquiryFixture.업로드된_파일(
            authorId, FileCategory.INQUIRY_ATTACHMENT, "notes.pdf", "text/plain"
        );

        // when & then
        assertThatThrownBy(() -> 첨부_보내기(inquiryId, List.of(disguised.getId())))
            .extracting("baseCode").isEqualTo(ChatErrorCode.CHAT_MESSAGE_INVALID_FILE_TYPE);
    }

    private InquiryMessageInfo 첨부_보내기(Long inquiryId, List<String> fileMetadataIds) {
        return sendInquiryMessageUseCase.send(new SendInquiryMessageCommand(
            inquiryId, authorId, null, fileMetadataIds, UUID.randomUUID()
        ));
    }

    private Long 문의_등록() {
        return createInquiryUseCase.create(등록_요청(UUID.randomUUID()));
    }

    private CreateInquiryCommand 등록_요청(UUID clientRequestId) {
        return new CreateInquiryCommand(
            authorId,
            categoryId,
            InquiryTeam.PLATFORM,
            "앱 로그인이 안 돼요",
            "로그인 버튼을 눌러도 반응이 없어요",
            List.of(),
            clientRequestId
        );
    }

    private InquiryMessageInfo 보내기(Long inquiryId, Long senderId, String content, UUID clientMessageId) {
        return sendInquiryMessageUseCase.send(
            new SendInquiryMessageCommand(inquiryId, senderId, content, List.of(), clientMessageId)
        );
    }

    private InquiryDetailInfo 상세(Long inquiryId, Long requesterId) {
        return getInquiryDetailUseCase.getDetail(new InquiryDetailQuery(inquiryId, requesterId));
    }

    private InquiryMessagePageInfo 메시지_조회(Long inquiryId, Long requesterId) {
        return getInquiryMessagesUseCase.getMessages(new InquiryMessagesQuery(inquiryId, requesterId, null, 30));
    }

    private long 이벤트_수(String eventType) {
        return eventOutboxJpaRepository.findAll().stream()
            .filter(outbox -> eventType.equals(outbox.getEventType()))
            .count();
    }
}
