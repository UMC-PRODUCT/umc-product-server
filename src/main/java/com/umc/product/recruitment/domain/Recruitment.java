package com.umc.product.recruitment.domain;

import com.umc.product.recruitment.domain.enums.RecruitmentPhase;
import com.umc.product.recruitment.domain.enums.RecruitmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recruitment {

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

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "notice_title")
    private String noticeTitle;

    @Column(name = "notice_content")
    private String noticeContent;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecruitmentPhase phase = RecruitmentPhase.BEFORE_APPLY;

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

        return recruitment;
    }

    private static void validateDraftContext(Long schoolId, Long gisuId, Long formId) {
        if (schoolId == null || gisuId == null || formId == null) {
            throw new IllegalStateException("Recruitment draft context invalid");
        }
    }

    private static String normalizeTitle(String title) {
        if (title == null) {
            return null;
        }
        String t = title.trim();
        return t.isBlank() ? null : t;
    }
}
