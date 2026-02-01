package com.umc.product.notice.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.common.domain.enums.ChallengerPart;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(name = "target_challenger_part")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<ChallengerPart> targetChallengerPart;

    @Builder
    private NoticeTarget(
        Long noticeId,
        Long targetGisuId,
        Long targetChapterId,
        Long targetSchoolId,
        List<ChallengerPart> targetChallengerPart) {
        this.noticeId = noticeId;
        this.targetGisuId = targetGisuId;
        this.targetChapterId = targetChapterId;
        this.targetSchoolId = targetSchoolId;
        this.targetChallengerPart = targetChallengerPart;
    }

    /**
     * 공지사항 권한 정보 업데이트
     */
    public void update(
        Long targetGisuId,
        Long targetChapterId,
        Long targetSchoolId,
        List<ChallengerPart> targetChallengerPart) {
        this.targetGisuId = targetGisuId;
        this.targetChapterId = targetChapterId;
        this.targetSchoolId = targetSchoolId;
        this.targetChallengerPart = targetChallengerPart;
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
