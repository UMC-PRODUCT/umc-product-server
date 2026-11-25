package com.umc.product.inquiry.domain;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
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

    public static final int MAX_TITLE_CODE_POINTS = 50;

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

    @Column(name = "assignee_member_id")
    private Long assigneeMemberId;

    public static Inquiry create(
        Long authorMemberId,
        Long categoryId,
        InquiryTeam targetTeam,
        String content,
        Long chatRoomId,
        UUID clientRequestId,
        Instant createdAt
    ) {
        Inquiry inquiry = new Inquiry();
        inquiry.authorMemberId = Objects.requireNonNull(authorMemberId);
        inquiry.categoryId = Objects.requireNonNull(categoryId);
        inquiry.targetTeam = targetTeam;
        inquiry.title = titleOf(content);
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

    /**
     * 담당자가 운영진 권한을 잃으면 저장된 값과 상관없이 미배정으로 본다.
     */
    public Long effectiveAssigneeId(Collection<Long> staffMemberIds) {
        return assigneeMemberId != null && staffMemberIds.contains(assigneeMemberId) ? assigneeMemberId : null;
    }

    /**
     * 요청자가 본 담당자와 지금 담당자가 다르면 덮어쓰지 않고 거절한다. 담당자가 그대로면 이력을 남기지 않는다.
     */
    public Optional<InquiryHistory> changeAssignee(
        Long actorMemberId,
        Long newAssigneeId,
        Long expectedAssigneeId,
        Collection<Long> staffMemberIds
    ) {
        Long current = effectiveAssigneeId(staffMemberIds);
        if (!Objects.equals(current, expectedAssigneeId)) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_ASSIGNEE_CHANGED);
        }
        if (newAssigneeId != null && !staffMemberIds.contains(newAssigneeId)) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_ASSIGNEE_NOT_STAFF);
        }
        assigneeMemberId = newAssigneeId;
        if (Objects.equals(current, newAssigneeId)) {
            return Optional.empty();
        }
        return Optional.of(InquiryHistory.assigneeChanged(id, actorMemberId, current, newAssigneeId));
    }

    public Optional<InquiryHistory> changeTeam(Long actorMemberId, InquiryTeam newTeam) {
        if (targetTeam == newTeam) {
            return Optional.empty();
        }
        InquiryHistory history = InquiryHistory.teamChanged(id, actorMemberId, targetTeam, newTeam);
        targetTeam = newTeam;
        return Optional.of(history);
    }

    /**
     * 등록 화면에 제목 칸이 없어서 내용의 첫 번째 비지 않은 줄을 제목으로 쓴다. 목록 한 줄에 들어가도록 자른다.
     */
    static String titleOf(String content) {
        String firstLine = Objects.requireNonNull(content).lines()
            .map(String::strip)
            .filter(line -> !line.isEmpty())
            .findFirst()
            .orElseThrow(() -> new InquiryDomainException(InquiryErrorCode.INQUIRY_CONTENT_REQUIRED));
        if (firstLine.codePointCount(0, firstLine.length()) <= MAX_TITLE_CODE_POINTS) {
            return firstLine;
        }
        return firstLine.substring(0, firstLine.offsetByCodePoints(0, MAX_TITLE_CODE_POINTS)).strip();
    }
}
