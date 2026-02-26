package com.umc.product.notice.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "notice")
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 3000)
    private String content;

    @Column(name = "author_member_id", nullable = false)
    private Long authorMemberId;

    /* 알림발송 여부 */
    private boolean shouldSendNotification;

    /* 알림발송 시각 */
    private Instant notifiedAt;

    public static Notice create(String title, String content, Long authorMemberId, boolean shouldNotify) {
        return Notice.builder()
            .title(title)
            .content(content)
            .authorMemberId(authorMemberId)
            .shouldSendNotification(shouldNotify)
            .build();
    }

    public void updateTitleOrContent(String title, String content) {
        this.title = title;
        this.content = content;
    }

    /**
     * 공지사항 작성자와 일치하는 챌린저인지 확인함
     */
    public void validateAuthorMember(Long authorMemberId) {
        if (!this.authorMemberId.equals(authorMemberId)) {
            throw new NoticeDomainException(NoticeErrorCode.NOTICE_AUTHOR_MISMATCH);
        }
    }

    /**
     * 알림 전송이 필요한 공지인지 확인
     */
    public boolean isNotificationRequired() {
        return this.shouldSendNotification && this.notifiedAt == null;
    }

    /**
     * 알림 전송 시각 설정
     */
    public void markAsNotified(Instant notifiedAt) {
        this.notifiedAt = notifiedAt;
    }
}
