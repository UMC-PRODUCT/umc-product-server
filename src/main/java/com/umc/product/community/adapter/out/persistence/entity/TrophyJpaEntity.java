package com.umc.product.community.adapter.out.persistence.entity;

import com.umc.product.common.BaseEntity;
import com.umc.product.community.domain.Trophy;
import com.umc.product.community.domain.Trophy.ChallengerId;
import com.umc.product.community.domain.Trophy.TrophyId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trophy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrophyJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long challengerId;

    @Column(nullable = false)
    private Integer week;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String url;

    private TrophyJpaEntity(Long challengerId, Integer week, String title, String content, String url) {
        this.challengerId = challengerId;
        this.week = week;
        this.title = title;
        this.content = content;
        this.url = url;
    }

    public static TrophyJpaEntity from(Trophy trophy) {
        return new TrophyJpaEntity(
            trophy.getChallengerId().id(),
            trophy.getWeek(),
            trophy.getTitle(),
            trophy.getContent(),
            trophy.getUrl()
        );
    }

    public Trophy toDomain() {
        return Trophy.reconstruct(
            new TrophyId(id),
            new ChallengerId(challengerId),
            week,
            title,
            content,
            url
        );
    }
}
