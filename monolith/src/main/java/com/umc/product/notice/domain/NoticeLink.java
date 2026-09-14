package com.umc.product.notice.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notice_link")
public class NoticeLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @Column(nullable = false)
    private String link;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Builder
    private NoticeLink(Notice notice, String link, Integer displayOrder) {
        this.notice = notice;
        this.link = link;
        this.displayOrder = displayOrder;
    }

    public static NoticeLink create(String link, Notice notice, int displayOrder) {
        return NoticeLink.builder()
            .link(link)
            .notice(notice)
            .displayOrder(displayOrder)
            .build();
    }

}
