package com.umc.product.project.application.port.in.query.dto;

import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.enums.MatchingType;
import java.util.List;
import lombok.Builder;

/**
 * 본인 지원 내역 카드 1건의 Info DTO.
 * <p>
 * Service 가 Project + PartQuota + MatchingRound + 썸네일 URL 까지 조립한 형태. Web Assembler 가 Member 정보(PM 닉네임/실명/학교) 를 추가해 최종
 * Response 로 변환한다.
 * <p>
 * 카드는 두 경로로 합성된다.
 * <ul>
 *   <li>{@link #of} -- application 기반 카드. matchingRoundId/Phase 가 실제 라운드 엔티티 값으로 채워진다.</li>
 *   <li>{@link #ofRandomMatching} -- {@code ProjectMember.application=null} 인 랜덤 매칭/운영진 강제 배정 카드.
 *       {@code matchingRoundId} 는 {@code null} 이며, {@code matchingRoundPhase} 는 {@link MatchingRoundPhaseView#RANDOM_MATCHING} 으로
 *       표시된다. {@code matchingRoundType} 은 본인 챌린저 파트로부터 추론된 값이 채워진다 (회의로 확정된 정책).</li>
 * </ul>
 *
 * @param applicationId      application 기반 카드만 값을 가지며 랜덤 매칭 카드는 {@code null}
 * @param matchingRoundId    application 기반 카드만 값을 가지며 랜덤 매칭 카드는 {@code null}
 * @param matchingRoundType  매칭 종류. 두 경로 모두 NOT NULL (랜덤 매칭은 본인 파트로 추론)
 * @param matchingRoundPhase 표시용 phase. 도메인 enum 직접 노출을 피하기 위한 표시용 enum. 두 경로 모두 NOT NULL
 */
@Builder
public record MyProjectApplicationCardInfo(
    Long applicationId,
    Long projectId,
    String projectName,
    String projectThumbnailImageUrl,
    Long productOwnerMemberId,
    List<ProjectPartQuotaInfo> partQuotas,
    Long matchingRoundId,
    MatchingType matchingRoundType,
    MatchingRoundPhaseView matchingRoundPhase,
    ProjectApplicationViewStatus status
) {
    /**
     * ProjectApplication 과 부가 조회 결과를 조립해 카드 Info 를 만든다.
     *
     * @param application       지원서 엔티티 (applicationForm.project / appliedMatchingRound 가 fetch 된 상태)
     * @param partQuotas        프로젝트의 파트별 TO 정보
     * @param thumbnailImageUrl 썸네일 CDN URL (없으면 {@code null})
     */
    public static MyProjectApplicationCardInfo of(
        ProjectApplication application,
        List<ProjectPartQuotaInfo> partQuotas,
        String thumbnailImageUrl
    ) {
        Project project = application.getApplicationForm().getProject();
        ProjectMatchingRound round = application.getAppliedMatchingRound();

        return MyProjectApplicationCardInfo.builder()
            .applicationId(application.getId())
            .projectId(project.getId())
            .projectName(project.getName())
            .projectThumbnailImageUrl(thumbnailImageUrl)
            .productOwnerMemberId(project.getProductOwnerMemberId())
            .partQuotas(partQuotas)
            .matchingRoundId(round.getId())
            .matchingRoundType(round.getType())
            .matchingRoundPhase(MatchingRoundPhaseView.from(round.getPhase()))
            .status(ProjectApplicationViewStatus.from(application.getStatus()))
            .build();
    }

    /**
     * {@code application=null} + ACTIVE 인 ProjectMember 로부터 랜덤 매칭/운영진 강제 배정 카드를 만든다.
     * <p>
     * application 이 없으므로 {@code applicationId} 와 {@code matchingRoundId} 는 {@code null} 로 채워지며, status 는 ProjectMember
     * 의 ACTIVE 가 곧 합격 의미이므로 {@link ProjectApplicationViewStatus#APPROVED} 로 표시한다.
     *
     * @param member            ACTIVE + application=null 인 ProjectMember (project 가 fetch 된 상태)
     * @param partQuotas        프로젝트의 파트별 TO 정보
     * @param thumbnailImageUrl 썸네일 CDN URL (없으면 {@code null})
     * @param matchingType      본인 챌린저 파트로부터 추론된 매칭 종류
     */
    public static MyProjectApplicationCardInfo ofRandomMatching(
        ProjectMember member,
        List<ProjectPartQuotaInfo> partQuotas,
        String thumbnailImageUrl,
        MatchingType matchingType
    ) {
        Project project = member.getProject();

        return MyProjectApplicationCardInfo.builder()
            .applicationId(null)
            .projectId(project.getId())
            .projectName(project.getName())
            .projectThumbnailImageUrl(thumbnailImageUrl)
            .productOwnerMemberId(project.getProductOwnerMemberId())
            .partQuotas(partQuotas)
            .matchingRoundId(null)
            .matchingRoundType(matchingType)
            .matchingRoundPhase(MatchingRoundPhaseView.RANDOM_MATCHING)
            .status(ProjectApplicationViewStatus.APPROVED)
            .build();
    }
}
