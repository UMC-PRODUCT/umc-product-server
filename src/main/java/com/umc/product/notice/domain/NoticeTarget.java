package com.umc.product.notice.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.notice.domain.enums.NoticeTab;
import com.umc.product.notice.domain.enums.NoticeTargetRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


/**
 * 각 공지사항이 어떤 대상에 대한 것인지 명시함
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notice_target")
public class NoticeTarget extends BaseEntity {
    @Id
    @Column(name = "notice_id")
    private Long noticeId;

    @Column(name = "target_gisu_id")
    private Long targetGisuId;

    @Column(name = "target_chapter_id")
    private Long targetChapterId;

    @Column(name = "target_school_id")
    private Long targetSchoolId;

    @Column(name = "target_challenger_part", columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Enumerated(EnumType.STRING)
    private List<ChallengerPart> targetChallengerPart;

    @Column(name = "target_staff_roles", columnDefinition = "text[]", nullable = false)
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Enumerated(EnumType.STRING)
    private List<NoticeTargetRole> targetRoles;

    @Column(name = "notice_tab", nullable = false)
    @Enumerated(EnumType.STRING)
    private NoticeTab noticeTab; // targetRole로는 구분이 어려운 공지사항 탭 구분 (공지사항 조회 시 탭 구분 용도)

    @Builder
    private NoticeTarget(
        Long noticeId,
        Long targetGisuId,
        Long targetChapterId,
        Long targetSchoolId,
        List<ChallengerPart> targetChallengerPart,
        List<NoticeTargetRole> targetRoles,
        NoticeTab noticeTab) {
        this.noticeId = noticeId;
        this.targetGisuId = targetGisuId;
        this.targetChapterId = targetChapterId;
        this.targetSchoolId = targetSchoolId;
        this.targetChallengerPart = targetChallengerPart;
        this.targetRoles = targetRoles;
        this.noticeTab = noticeTab;
    }

    /**
     * 공지사항 권한 정보 업데이트
     */
    public void update(
        Long targetGisuId,
        Long targetChapterId,
        Long targetSchoolId,
        List<ChallengerPart> targetChallengerPart,
        List<NoticeTargetRole> targetRoles) {
        this.targetGisuId = targetGisuId;
        this.targetChapterId = targetChapterId;
        this.targetSchoolId = targetSchoolId;
        this.targetChallengerPart = targetChallengerPart;
        this.targetRoles = targetRoles;
    }

    /**
     * 운영진 공지 여부 확인
     */
    public boolean isStaffNotice() {
        return targetRoles != null && !targetRoles.contains(NoticeTargetRole.CHALLENGER);
    }

    /**
     * 전체 공지사항인지 확인 (모든 대상이 null)
     */
    public boolean isGlobalNotice() {
        return targetGisuId == null
            && targetChapterId == null
            && targetSchoolId == null
            && (targetChallengerPart == null || targetChallengerPart.isEmpty());
    }
}
