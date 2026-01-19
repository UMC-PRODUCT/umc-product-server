package com.umc.product.notice.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.notice.domain.enums.NoticeClassification;
import com.umc.product.notice.domain.enums.NoticeStatus;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "notice")
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(name = "author_challenger_id", nullable = false)
    private Long authorChallengerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NoticeStatus status; /* DRAFT, PUBLISHED */

    private boolean shouldNotify; /* 알림발송 여부 */

    private Instant notifiedAt; /* 알림발송 시각 */

    /*
     * target 관련
     */

    /*
     * 어떤 scope에서 작성된 공지인가?
     * 원래 OrganizationType이었으나, PART가 OrganizationType에는 필요 없어서 별도의 enum 사용
     */
    @Column(name = "scope")
    @Enumerated(EnumType.STRING)
    private NoticeClassification scope;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "organization_ids", columnDefinition = "bigint[]")
    @Builder.Default
    private List<Long> organizationIds = new ArrayList<>(); /* scope에 따른 조직(중앙, 학교 등) ID */

    @Column(name = "target_gisu_id")
    private Long targetGisuId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "target_roles", columnDefinition = "varchar[]")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private List<ChallengerRoleType> targetRoles = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "target_parts", columnDefinition = "varchar[]")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private List<ChallengerPart> targetParts = new ArrayList<>();


    public static Notice draft(String title, String content, Long authorChallengerId,
                               NoticeClassification scope,
                               List<Long> organizationIds,
                               Long targetGisuId,
                               List<ChallengerRoleType> targetRoles,
                               List<ChallengerPart> targetParts,
                               boolean shouldNotify) {

        validateDraftCreation(title, content, authorChallengerId, scope);

        return Notice.builder()
                .title(title)
                .content(content)
                .authorChallengerId(authorChallengerId)
                .status(NoticeStatus.DRAFT)
                .scope(scope)
                .organizationIds(organizationIds != null ? organizationIds : new ArrayList<>())
                .targetGisuId(targetGisuId)
                .targetRoles(targetRoles != null ? targetRoles : new ArrayList<>())
                .targetParts(targetParts != null ? targetParts : new ArrayList<>())
                .shouldNotify(shouldNotify)
                .notifiedAt(null)
                .build();
    }

    public void update(String title, String content,
                                     NoticeClassification scope,
                                     List<Long> organizationIds,
                                     Long targetGisuId,
                                     List<ChallengerRoleType> targetRoles,
                                     List<ChallengerPart> targetParts,
                                     boolean shouldNotify,
                                     NoticeStatus status) {

        validateCanUpdate(title, content, scope);

        this.title = title;
        this.content = content;
        this.scope = scope;
        if (organizationIds != null) this.organizationIds = new ArrayList<>(organizationIds);
        this.targetGisuId = targetGisuId;
        if (targetRoles != null) this.targetRoles = new ArrayList<>(targetRoles);
        if (targetParts != null) this.targetParts = new ArrayList<>(targetParts);
        this.shouldNotify = shouldNotify;
        this.status = status;
    }

    public boolean isDraft() {
        return this.status == NoticeStatus.DRAFT;
    }

    public boolean isPublished() {
        return this.status == NoticeStatus.PUBLISHED;
    }

    public void publish() {
        validateCanPublish();
        this.status = NoticeStatus.PUBLISHED;

        if (this.shouldNotify && this.notifiedAt == null) {
            this.notifiedAt = Instant.now();
        }
    }

    /*
     * 검증 메서드
     */
    public void validateCanSendReminder() {
        if (this.status != NoticeStatus.PUBLISHED) {
            throw new NoticeDomainException(NoticeErrorCode.INVALID_NOTICE_STATUS_FOR_REMINDER);
        }
    }

    private static void validateDraftCreation(String title, String content,
                                              Long authorChallengerId,
                                              NoticeClassification scopes) {
        // 필수값 null 체크
        if (title == null) {
            throw new NoticeDomainException(NoticeErrorCode.INVALID_NOTICE_TITLE);
        }

        if (content == null) {
            throw new NoticeDomainException(NoticeErrorCode.INVALID_NOTICE_CONTENT);
        }

        if (authorChallengerId == null) {
            throw new NoticeDomainException(NoticeErrorCode.AUTHOR_REQUIRED);
        }

        if (scopes == null) {
            throw new NoticeDomainException(NoticeErrorCode.NOTICE_SCOPE_REQUIRED);
        }
    }

    private void validateCanUpdate(String title, String content,
                                          NoticeClassification scope) {
        if (title == null) {
            throw new NoticeDomainException(NoticeErrorCode.INVALID_NOTICE_TITLE);
        }

        if (content == null) {
            throw new NoticeDomainException(NoticeErrorCode.INVALID_NOTICE_CONTENT);
        }

        if (scope == null) {
            throw new NoticeDomainException(NoticeErrorCode.NOTICE_SCOPE_REQUIRED);
        }
    }

    private void validateCanPublish() {
        if (this.status == NoticeStatus.PUBLISHED) {
            throw new NoticeDomainException(NoticeErrorCode.ALREADY_PUBLISHED_NOTICE);
        }

        if (this.title == null || this.title.isBlank()) {
            throw new NoticeDomainException(NoticeErrorCode.INVALID_NOTICE_TITLE);
        }

        if (this.content == null || this.content.isBlank()) {
            throw new NoticeDomainException(NoticeErrorCode.INVALID_NOTICE_CONTENT);
        }

        if (this.scope == null) {
            throw new NoticeDomainException(NoticeErrorCode.NOTICE_SCOPE_REQUIRED);
        }
    }

    public boolean hasScope(NoticeClassification scope) {
        return this.scope != null;
    }

    public boolean hasTargetRoles() {
        return this.targetRoles != null && !this.targetRoles.isEmpty();
    }

    public boolean hasTargetParts() {
        return this.targetParts != null && !this.targetParts.isEmpty();
    }
}
