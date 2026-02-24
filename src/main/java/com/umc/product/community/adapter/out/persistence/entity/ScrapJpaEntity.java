package com.umc.product.community.adapter.out.persistence.entity;

import com.umc.product.common.BaseEntity;
import com.umc.product.community.domain.Scrap;
import com.umc.product.community.domain.Scrap.ScrapId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "scrap",
    uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "challenger_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScrapJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "challenger_id", nullable = false)
    private Long challengerId;

    private ScrapJpaEntity(Long postId, Long challengerId) {
        this.postId = postId;
        this.challengerId = challengerId;
    }

    public static ScrapJpaEntity from(Scrap scrap) {
        return new ScrapJpaEntity(scrap.getPostId(), scrap.getChallengerId());
    }

    public Scrap toDomain() {
        return Scrap.reconstruct(new ScrapId(id), postId, challengerId);
    }

    public static ScrapJpaEntity of(Long postId, Long challengerId) {
        return new ScrapJpaEntity(postId, challengerId);
    }
}

