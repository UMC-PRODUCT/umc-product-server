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

    @Column(name = "root_recruitment_id")
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

        // 최초 생성 후 setRootToSelf 호출하여 root 설정
        recruitment.parentRecruitmentId = null;
        recruitment.rootRecruitmentId = null;

        return recruitment;
    }

    // 추가 모집 생성 (Extension)
    public static Recruitment createExtension(
        Recruitment baseRecruitment, // 부모 모집
        Long formId,                 // 새로 복제된 폼 ID
        String title                 // 사용자가 입력한 새 제목
    ) {
        // (1) 검증: 부모가 배포된 상태여야 함
        if (!baseRecruitment.isPublished()) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.BASE_RECRUITMENT_NOT_PUBLISHED);
        }

        Recruitment extension = new Recruitment();

        // (2) 불변 정보 상속 (학교, 기수)
        extension.schoolId = baseRecruitment.getSchoolId();
        extension.gisuId = baseRecruitment.getGisuId();

        // (3) 입력값 반영 (제목, 폼, 상태)
        extension.title = normalizeTitle(title);
        extension.formId = formId;
        extension.status = RecruitmentStatus.DRAFT;

        // (4) 계층 구조 설정
        extension.parentRecruitmentId = baseRecruitment.getId();
        extension.rootRecruitmentId = baseRecruitment.getEffectiveRootId(); // 부모의 뿌리를 상속

        // (5) 데이터 동기화 (면접 시간표 등 Root/Base 정책 상속)
        // baseRecruitment가 이미 Root와 동기화된 상태라고 가정하고 복사
        extension.syncRootData(baseRecruitment);

        return extension;
    }

    // 기준 모집 Root 설정 (Service에서 호출)
    // insert(save) 후 ID가 생기면, 이 메서드를 호출해서 root = id로 업데이트
    public void setRootToSelf() {
        if (this.id == null) {
            throw new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_ID_MISSING);
        }
        this.rootRecruitmentId = this.id;
    }

    // 데이터 동기화 로직 (면접 시간표 복사)
    public void syncRootData(Recruitment source) {
        // 면접 시간표, 희망 파트 최대 개수 복사. 날짜 등은 RecruitmentSchedule에서 처리
        this.interviewTimeTable = source.getInterviewTimeTable();
        this.maxPreferredPartCount = source.getMaxPreferredPartCount();
    }

    // 5. Root ID 조회 편의 메서드 (Null 안전)
    public Long getEffectiveRootId() {
        if (this.rootRecruitmentId != null) {
            return this.rootRecruitmentId;
        }
        // rootRecruitmentId가 없다면 자기 자신이 Root임을 의미함
        return this.id;
    }

    public boolean isRoot() {
        return this.parentRecruitmentId == null;
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
