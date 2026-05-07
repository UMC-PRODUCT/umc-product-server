package com.umc.product.project.application.port.in.query.dto;

import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import java.util.List;
import lombok.Builder;

/**
 * 본인 지원 내역 카드 1건의 Info DTO.
 * <p>
 * Service 가 Project + PartQuota + MatchingRound + 썸네일 URL 까지 조립한 형태. Web Assembler 가 Member 정보(PM 닉네임/실명/학교) 를 추가해 최종
 * Response 로 변환한다.
 * <p>
 * 매칭 라운드는 표시용 라벨 대신 {@link MatchingType} 과 {@link MatchingPhase} 의 enum 조합으로 노출한다 — 라벨 합성은 Web 레이어 책임.
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
    MatchingPhase matchingRoundPhase,
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
            .matchingRoundPhase(round.getPhase())
            .status(ProjectApplicationViewStatus.from(application.getStatus()))
            .build();
    }
}
