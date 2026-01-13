package com.umc.product.notice.domain;

import com.umc.product.challenger.domain.enums.ChallengerPart;
import com.umc.product.challenger.domain.enums.OrganizationType;
import com.umc.product.challenger.domain.enums.RoleType;
import com.umc.product.common.BaseEntity;
import com.umc.product.notice.domain.enums.NoticeClassification;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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

    private boolean shouldNotify; /* 알림발송 여부 */

    private Instant notifiedAt; /* 알림발송 시각 */

    /*
    * target 관련
    */


    /*
    * 어떤 scope에서 작성된 공지인가?
    * 원래 OrganizationType이었으나, PART가 OrganizationType에는 필요 없어서 별도의 enum 사용
    */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NoticeClassification scope;

    @Column(name = "organization_id")
    private Long organizationId; /* scope에 따른 조직(중앙, 학교 등) ID */

    @Column(name = "target_gisu_id")
    private Long targetGisuId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "target_roles", columnDefinition = "varchar[]")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private List<RoleType> targetRoles = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "target_parts", columnDefinition = "varchar[]")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private List<ChallengerPart> targetParts = new ArrayList<>();

}
