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
@Table(name = "notice_image")
public class NoticeImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @Column(name = "image_id", nullable = false)
    private Long imageId;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Builder
    private NoticeImage(Notice notice, Long imageId, Integer displayOrder) {
        this.notice = notice;
        this.imageId = imageId;
        this.displayOrder = displayOrder;
    }

    public static NoticeImage create(Long imageId, Notice notice, int displayOrder) {
        return NoticeImage.builder()
                .imageId(imageId)
                .notice(notice)
                .displayOrder(displayOrder)
                .build();
    }

}
