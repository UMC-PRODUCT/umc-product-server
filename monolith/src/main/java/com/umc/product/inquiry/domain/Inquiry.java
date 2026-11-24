package com.umc.product.inquiry.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.umc.product.common.BaseEntity;
import com.umc.product.inquiry.domain.enums.InquiryParticipant;
import com.umc.product.inquiry.domain.enums.InquiryStatus;
import com.umc.product.inquiry.domain.enums.InquiryTeam;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "inquiry",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_inquiry_chat_room", columnNames = "chat_room_id"),
        @UniqueConstraint(
            name = "uq_inquiry_author_client_request",
            columnNames = {"author_member_id", "client_request_id"}
        )
    },
    indexes = @Index(
        name = "idx_inquiry_author_last_message",
        columnList = "author_member_id, last_message_at, id"
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry extends BaseEntity {

    public static final int MAX_TITLE_CODE_POINTS = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_member_id", nullable = false)
    private Long authorMemberId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_team", length = 40)
    private InquiryTeam targetTeam;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryStatus status;

    @Column(name = "chat_room_id", nullable = false)
    private Long chatRoomId;

    @Column(name = "last_message_at", nullable = false)
    private Instant lastMessageAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "client_request_id", nullable = false)
    private UUID clientRequestId;

    public static Inquiry create(
        Long authorMemberId,
        Long categoryId,
        InquiryTeam targetTeam,
        String title,
        Long chatRoomId,
        UUID clientRequestId,
        Instant createdAt
    ) {
        Inquiry inquiry = new Inquiry();
        inquiry.authorMemberId = Objects.requireNonNull(authorMemberId);
        inquiry.categoryId = Objects.requireNonNull(categoryId);
        inquiry.targetTeam = targetTeam;
        inquiry.title = normalizeTitle(title);
        inquiry.status = InquiryStatus.RECEIVED;
        inquiry.chatRoomId = Objects.requireNonNull(chatRoomId);
        inquiry.lastMessageAt = Objects.requireNonNull(createdAt);
        inquiry.clientRequestId = Objects.requireNonNull(clientRequestId);
        return inquiry;
    }

    public InquiryParticipant participantOf(Long memberId) {
        return authorMemberId.equals(memberId) ? InquiryParticipant.AUTHOR : InquiryParticipant.STAFF;
    }

    public boolean isClosable() {
        return status.isClosable();
    }

    public void recordMessage(InquiryParticipant sender, Instant sentAt) {
        InquiryStatus next = status.afterMessageFrom(sender);
        if (next != InquiryStatus.CLOSED) {
            closedAt = null;
        }
        status = next;
        if (sentAt.isAfter(lastMessageAt)) {
            lastMessageAt = sentAt;
        }
    }

    public void close(Instant now) {
        if (!isClosable()) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_ALREADY_CLOSED);
        }
        status = InquiryStatus.CLOSED;
        closedAt = now;
    }

    private static String normalizeTitle(String title) {
        String trimmed = Objects.requireNonNull(title).strip();
        int length = trimmed.codePointCount(0, trimmed.length());
        if (length < 1 || length > MAX_TITLE_CODE_POINTS) {
            throw new InquiryDomainException(InquiryErrorCode.INVALID_INQUIRY_TITLE);
        }
        return trimmed;
    }
}
