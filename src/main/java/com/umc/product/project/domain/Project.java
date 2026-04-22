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
import lombok.Builder;
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

    @Column(nullable = false)
    private Long chapterId; // 프로젝트가 속한 지부

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;
    // ABORTED로 status를 변경하고자 하는 경우 반드시 ProjectMember 또한 변경하여야 합니다.

    // DRAFT 단계에서는 미입력 상태가 허용되므로 nullable.
    @Column(length = 100)
    private String name; // 프로젝트명

    @Column(length = 200)
    private String description; // 프로젝트 설명

    // storage 도메인 file_metadata.id는 UUID(VARCHAR)라 String으로 저장합니다.
    private String logoFileId; // 로고 파일 ID (UUID)
    private String thumbnailFileId; // 프로젝트 썸네일 이미지 파일 ID (UUID)


    @Column(length = 300)
    private String externalLink; // 프로젝트 외부 링크

    // 프로젝트를 생성한, PO의 ID 값입니다.
    // Plan 챌린저가 여러 명 있더라도 해당 프로젝트에 대한 전권을 가지고 있는 PO는 한 명으로 유지하고자 합니다.
    @Column(nullable = false)
    private Long productOwnerMemberId;

    private Long statusChangedByMemberId; // 프로젝트 상태를 변경한 멤버의 ID입니다. (예: ABORTED로 변경한 멤버)
    private String statusChangedReason; // 프로젝트 상태를 변경한 이유입니다. (예: ABORTED로 변경한 이유)

    // 연결된 지원 폼(Survey) ID. PM이 PROJECT-106으로 지원 문항을 저장할 때 연결됩니다.
    private Long applicationFormId;

    // 프로젝트 지원 폼은, 파트별로 섹션을 나누어서 가지고 있어야 합니다.
    // Design, FE, BE 각각 섹션을 나누어서 가지고 있으며, 지원자의 파트에 따라서 자동으로 해당 섹션으로 렌더링해서 제공하여야 합니다.
    // 기존에는 ElementCollection, CollectionTable을 통해서 저장했으나, FormSection마다 조회 및 작성 가능한 파트를 제한하여야 하고,
    // 추후 요구사항에 보다 유연하게 대응하기 위해서 한정적인 매핑 테이블이 아닌 별도의 엔티티로 분리하였습니다.

    // 고려사항:
    // TO는 Embedded로 전부 때려박아서 관리할지, 별도의 Entity를 분리할지 고려하였으나
    // 추후 확장성을 고려하여 분리하는 방향으로 결정하였습니다.
    // (다음 기수부터 Web-Server 통합 가능성 고려)

    @Builder(access = AccessLevel.PRIVATE)
    private Project(
        Long gisuId,
        Long chapterId,
        ProjectStatus status,
        String name,
        String description,
        String externalLink,
        String logoFileId,
        String thumbnailFileId,
        Long productOwnerMemberId
    ) {
        this.gisuId = gisuId;
        this.chapterId = chapterId;
        this.status = status;
        this.name = name;
        this.description = description;
        this.externalLink = externalLink;
        this.logoFileId = logoFileId;
        this.thumbnailFileId = thumbnailFileId;
        this.productOwnerMemberId = productOwnerMemberId;
    }

    /**
     * DRAFT 상태로 프로젝트를 생성합니다. 최초 생성 시 기수/지부/PO만 확정하고,
     * 나머지 정보(name, description 등)는 이후 updateBasicInfo로 단계별 저장합니다.
     *
     * @param gisuId               프로젝트가 속한 기수 ID
     * @param chapterId            프로젝트가 속한 지부 ID (PO의 소속 지부에서 결정)
     * @param productOwnerMemberId PO Member ID (PLAN 파트 챌린저여야 함)
     * @return DRAFT 상태의 프로젝트
     */
    public static Project createDraft(
        Long gisuId,
        Long chapterId,
        Long productOwnerMemberId
    ) {
        throw new UnsupportedOperationException("TODO: createDraft 구현 필요");
    }

    /**
     * 프로젝트 기본 정보를 부분 업데이트합니다. null 필드는 무시(= 변경하지 않음).
     * DRAFT 상태에서는 전체 필드 수정 가능하며, 공개 이후(IN_PROGRESS)의 제한 정책은 별도 확인 필요.
     *
     * @param name                 프로젝트명 (null이면 무시)
     * @param description          설명 (null이면 무시)
     * @param externalLink         외부 링크 (null이면 무시)
     * @param thumbnailFileId      썸네일 파일 ID UUID (null이면 무시)
     * @param logoFileId           로고 파일 ID UUID (null이면 무시)
     * @param productOwnerMemberId 새 PO Member ID (null이면 무시, PLAN 파트여야 함)
     */
    public void updateBasicInfo(
        String name,
        String description,
        String externalLink,
        String thumbnailFileId,
        String logoFileId,
        Long productOwnerMemberId
    ) {
        throw new UnsupportedOperationException("TODO: updateBasicInfo 구현 필요");
    }

    /**
     * DRAFT 상태에서 PENDING_REVIEW 상태로 전이합니다. PM 제출 액션.
     * - 현재 상태가 DRAFT가 아니면 {@code PROJECT_INVALID_STATE}.
     * - 필수 필드(name, applicationFormId) 미입력 시 {@code PROJECT_SUBMIT_VALIDATION_FAILED}.
     */
    public void submit() {
        throw new UnsupportedOperationException("TODO: submit 구현 필요");
    }

    /**
     * Survey 도메인의 지원 폼 ID를 연결합니다. PROJECT-106에서 Service가 호출합니다.
     *
     * @param applicationFormId survey 도메인의 Form ID
     */
    public void attachApplicationForm(Long applicationFormId) {
        throw new UnsupportedOperationException("TODO: attachApplicationForm 구현 필요");
    }

    /**
     * 기수가 종료되었을 때, 프로젝트를 완료 처리 합니다.
     */
    public void complete() {
        throw new UnsupportedOperationException("TODO: complete 구현 필요");
    }

    /**
     * 프로젝트가 중간에 와해되었을 때 사용합니다. 사유를 반드시 제공하여야 합니다.
     *
     * @param reason            와해 사유
     * @param decidedByMemberId 해당 사항을 결정한 운영진 Member ID
     */
    public void abort(String reason, Long decidedByMemberId) {
        throw new UnsupportedOperationException("TODO: abort 구현 필요");
    }

}
