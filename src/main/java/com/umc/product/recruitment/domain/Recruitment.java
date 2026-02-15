package com.umc.product.recruitment.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.recruitment.domain.enums.RecruitmentStatus;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recruitment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id")
    private Long schoolId;

    @Column(name = "gisu_id")
    private Long gisuId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RecruitmentStatus status;

    private String title;

    @Column(name = "form_id", nullable = false)
    private Long formId;

//    @Builder.Default
//    @Column(name = "is_active", nullable = false)
//    private Boolean isActive = true;

    @Column(name = "notice_title")
    private String noticeTitle;

    @Column(name = "notice_content")
    private String noticeContent;

//    @Builder.Default
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private RecruitmentPhase phase = RecruitmentPhase.BEFORE_APPLY;

    @Column(name = "max_preferred_part_count")
    private Integer maxPreferredPartCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "interview_time_table", columnDefinition = "jsonb")
    private Map<String, Object> interviewTimeTable;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "root_recruitment_id", nullable = false)
    private Long rootRecruitmentId;

    @Column(name = "parent_recruitment_id")
    private Long parentRecruitmentId;


    public static Recruitment createDraft(
        Long schoolId,
        Long gisuId,
        Long formId,
        String title
    ) {
        validateDraftContext(schoolId, gisuId, formId);

        Recruitment recruitment = new Recruitment();
        recruitment.schoolId = schoolId;
        recruitment.gisuId = gisuId;
        recruitment.formId = formId;
        recruitment.status = RecruitmentStatus.DRAFT;
        recruitment.title = normalizeTitle(title);

        recruitment.interviewTimeTable = null;

        return recruitment;
    }

    private static void validateDraftContext(Long schoolId, Long gisuId, Long formId) {
        if (schoolId == null || gisuId == null || formId == null) {
            throw new IllegalStateException("Recruitment draft context invalid");
        }
    }

    public void changeTitle(String title) {
        requireDraftEditable();

        String normalizedTitle = normalizeTitle(title);
        this.title = normalizedTitle;
        this.noticeTitle = normalizeTitle(title);
    }

    public void changeNoticeContent(String noticeContent) {
        requireDraftEditable();
        this.noticeContent = normalizeNoticeContent(noticeContent);
    }

    public void changeMaxPreferredPartCount(Integer maxPreferredPartCount) {
        requireDraftEditable();
        this.maxPreferredPartCount = maxPreferredPartCount;
    }

    public void changeInterviewTimeTable(Map<String, Object> interviewTimeTable) {
        requireDraftEditable();
        if (interviewTimeTable != null) {
            this.interviewTimeTable = interviewTimeTable;
        }
    }

    private void requireDraftEditable() {
        if (this.status != RecruitmentStatus.DRAFT) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_DRAFT);
        }
//        if (Boolean.FALSE.equals(this.isActive)) {
//            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_INACTIVE);
//        }
    }

    private static String normalizeTitle(String title) {
        if (title == null) {
            return null;
        }
        String t = title.trim();
        return t.isBlank() ? null : t;
    }

    private static String normalizeNoticeContent(String content) {
        if (content == null) {
            return null;
        }
        String c = content.trim();
        return c.isBlank() ? null : c;
    }

    public boolean isPublished() {
        return this.status == RecruitmentStatus.PUBLISHED;
    }

    public void publish(Instant now) {

        if (isPublished()) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_ALREADY_PUBLISHED);
        }

        this.status = RecruitmentStatus.PUBLISHED;

        if (this.publishedAt == null) {
            this.publishedAt = now;
        }
    }

    public void changeInterviewTimeTableSlotMinutes(int slotMinutes) {
        if (slotMinutes <= 0) {
            throw new IllegalArgumentException("slotMinutes must be positive");
        }

        Map<String, Object> tt = this.interviewTimeTable;
        if (tt == null) {
            tt = new HashMap<>();
        }

        tt.put("slotMinutes", slotMinutes);
        this.interviewTimeTable = tt;
    }

}
