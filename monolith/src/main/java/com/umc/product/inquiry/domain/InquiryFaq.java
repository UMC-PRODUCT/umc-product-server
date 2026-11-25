package com.umc.product.inquiry.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

import com.umc.product.common.BaseEntity;
import com.umc.product.inquiry.domain.enums.InquiryFaqStatus;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대표 질문(FAQ). 운영진은 수정본({@link #draft})을 고치고, 발행할 때만 수정본이 공개되는 발행본({@link #published})이 된다.
 * 그래서 발행 중에 고쳐도 기존 발행본이 그대로 공개된다. 검증 조직은 늘 UMC PRODUCT라 저장하지 않는다.
 */
@Entity
@Table(name = "inquiry_faq")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryFaq extends BaseEntity {

    public static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
    public static final String VERIFIED_BY = "UMC PRODUCT";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryFaqStatus status;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "question", column = @Column(name = "draft_question", nullable = false)),
        @AttributeOverride(name = "answer", column = @Column(name = "draft_answer")),
        @AttributeOverride(name = "aliases",
            column = @Column(name = "draft_aliases", columnDefinition = "text[]", nullable = false)),
        @AttributeOverride(name = "categoryId", column = @Column(name = "draft_category_id")),
        @AttributeOverride(name = "audienceId", column = @Column(name = "draft_audience_id")),
        @AttributeOverride(name = "endDate", column = @Column(name = "draft_end_date")),
        @AttributeOverride(name = "relatedFaqId", column = @Column(name = "draft_related_faq_id"))
    })
    private InquiryFaqContent draft;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "question", column = @Column(name = "published_question")),
        @AttributeOverride(name = "answer", column = @Column(name = "published_answer")),
        @AttributeOverride(name = "aliases",
            column = @Column(name = "published_aliases", columnDefinition = "text[]")),
        @AttributeOverride(name = "categoryId", column = @Column(name = "published_category_id")),
        @AttributeOverride(name = "audienceId", column = @Column(name = "published_audience_id")),
        @AttributeOverride(name = "endDate", column = @Column(name = "published_end_date")),
        @AttributeOverride(name = "relatedFaqId", column = @Column(name = "published_related_faq_id"))
    })
    private InquiryFaqContent published;

    @Column(name = "published_search_text")
    private String publishedSearchText;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "published_by_member_id")
    private Long publishedByMemberId;

    @Column(name = "created_by_member_id", nullable = false)
    private Long createdByMemberId;

    @Version
    @Column(nullable = false)
    private Long version;

    public static InquiryFaq create(InquiryFaqContent draft, Long createdByMemberId) {
        InquiryFaq faq = new InquiryFaq();
        faq.status = InquiryFaqStatus.DRAFT;
        faq.draft = Objects.requireNonNull(draft);
        faq.createdByMemberId = Objects.requireNonNull(createdByMemberId);
        return faq;
    }

    public void saveDraft(InquiryFaqContent content) {
        if (id != null && id.equals(content.getRelatedFaqId())) {
            throw new InquiryDomainException(InquiryErrorCode.INVALID_INQUIRY_FAQ_RELATED);
        }
        draft = content;
    }

    /**
     * 수정본을 발행본으로 만든다. 검증 항목은 발행 순간의 확인일 뿐이라 저장하지 않는다.
     */
    public void publish(Long publisherMemberId, boolean verified, Instant now) {
        if (!verified) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_FAQ_VERIFICATION_REQUIRED);
        }
        if (!draft.isPublishable()) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_FAQ_INCOMPLETE);
        }
        status = status.publish();
        published = draft;
        publishedSearchText = draft.searchText();
        publishedAt = now;
        publishedByMemberId = publisherMemberId;
    }

    public void archive() {
        status = status.archive();
    }

    /** 발행 중이고 종료일(한국 시간)이 지나지 않았을 때만 사용자에게 보인다. */
    public boolean isPublicAt(Instant now) {
        return status == InquiryFaqStatus.PUBLISHED && published.isVisibleOn(LocalDate.ofInstant(now, SERVICE_ZONE));
    }

    /**
     * 운영진 여럿이 같은 대표 질문을 고칠 때 앞사람의 변경을 덮어쓰지 않도록, 요청자가 본 버전과 지금 버전이 다르면 거절한다.
     */
    public void requireVersion(long expectedVersion) {
        if (version == null || version != expectedVersion) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_FAQ_CHANGED);
        }
    }

    public boolean hasUnpublishedChanges() {
        return published != null && !published.equals(draft);
    }
}
