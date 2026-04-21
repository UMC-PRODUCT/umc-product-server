package com.umc.product.notice.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notice_vote")
public class NoticeVote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @Column(name = "vote_id", nullable = false)
    private Long voteId;

    // 시작일 00:00(KST) ~ 마감일 23:59(KST) = (마감일+1) 00:00(KST) exclusive
    // 투표는 반드시 기간이 설정되어야 합니다.
    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at_exclusive", nullable = false)
    private Instant endsAtExclusive;

    @Builder
    private NoticeVote(Notice notice, Long voteId, Instant startsAt, Instant endsAtExclusive) {
        this.notice = notice;
        this.voteId = voteId;
        this.startsAt = startsAt;
        this.endsAtExclusive = endsAtExclusive;
    }

    public static NoticeVote create(Long voteId, Notice notice, Instant startsAt, Instant endsAtExclusive) {
        return NoticeVote.builder()
            .voteId(voteId)
            .notice(notice)
            .startsAt(startsAt)
            .endsAtExclusive(endsAtExclusive)
            .build();
    }
}
