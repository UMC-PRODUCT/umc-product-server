package com.umc.product.notice.domain;

import com.umc.product.challenger.domain.enums.ChallengerPart;
import com.umc.product.challenger.domain.enums.RoleType;
import com.umc.product.common.BaseEntity;
import com.umc.product.notice.domain.enums.OrganizationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Column(name = "vote_id")
    private Long voteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganizationType scope;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "target_gisu_id")
    private Long targetGisuId;

    private Boolean shouldNotify; // 알림발송 여부

    private Instant notifiedAt;

    private List<RoleType> targetRoles;

    private List<ChallengerPart> targetParts;

    @Builder
    private Notice(String title, String content, Long authorChallengerId, Long voteId,
                   OrganizationType scope, Long organizationId, Long targetGisuId,
                   Boolean shouldNotify, Instant notifiedAt,
                   List<RoleType> targetRoles, List<ChallengerPart> targetParts) {
        this.title = title;
        this.content = content;
        this.authorChallengerId = authorChallengerId;
        this.voteId = voteId;
        this.scope = scope;
        this.organizationId = organizationId;
        this.targetGisuId = targetGisuId;
        this.shouldNotify = shouldNotify != null ? shouldNotify : false;
        this.notifiedAt = notifiedAt;
        this.targetRoles = targetRoles;
        this.targetParts = targetParts;
    }

}
