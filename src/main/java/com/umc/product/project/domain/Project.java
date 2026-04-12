package com.umc.product.project.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.project.domain.enums.ProjectStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "project")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long gisuId; // 프로젝트가 속한 기수

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;
    // ABORTED로 status를 변경하고자 하는 경우 반드시 ProjectMember 또한 변경하여야 합니다.

    @Column(length = 100, nullable = false)
    private String name; // 프로젝트명

    @Column(length = 200)
    private String description; // 프로젝트 설명

    // 프로젝트를 생성한, PO의 ID 값입니다.
    // Plan 챌린저가 여러 명 있더라도 해당 프로젝트에 대한 전권을 가지고 있는 PO는 한 명으로 유지하고자 합니다.
    @Column(nullable = false)
    private Long productOwnerMemberId;

    // 프로젝트 지원 폼은, 파트별로 섹션을 나누어서 가지고 있어야 합니다.
    // Design, FE, BE 각각 섹션을 나누어서 가지고 있으며, 지원자의 파트에 따라서 자동으로 해당 섹션으로 렌더링해서 제공하여야 합니다.
    // 기존에는 ElementCollection, CollectionTable을 통해서 저장했으나, FormSection마다 조회 및 작성 가능한 파트를 제한하여야 하고,
    // 추후 요구사항에 보다 유연하게 대응하기 위해서 한정적인 매핑 테이블이 아닌 별도의 엔티티로 분리하였습니다.

    // 고려사항:
    // TO는 Embedded로 전부 때려박아서 관리할지, 별도의 Entity를 분리할지 고려하였으나
    // 추후 확장성을 고려하여 분리하는 방향으로 결정하였습니다.
    // (다음 기수부터 Web-Server 통합 가능성 고려)
}
