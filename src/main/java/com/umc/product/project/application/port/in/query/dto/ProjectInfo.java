package com.umc.product.project.application.port.in.query.dto;

import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.enums.ProjectStatus;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

/**
 * 프로젝트 도메인 전반의 조회 UseCase가 공유하는 Info DTO (도메인 공유 DTO).
 * <p>
 * Entity + 부가 정보를 조합해 생성합니다. Controller/Assembler가 다시 Web Response DTO로 변환합니다.
 * <ul>
 *   <li>{@code thumbnailImageUrl}/{@code logoImageUrl} — storage 도메인에서 resolve해 Service가 주입</li>
 *   <li>{@code coProductOwnerMemberIds} — {@code project_member} 중 PLAN 파트(메인 PM 제외)로부터 추출</li>
 * </ul>
 */
@Builder
public record ProjectInfo(
    Long id,
    ProjectStatus status,
    String name,
    String description,
    String thumbnailFileId,
    String thumbnailImageUrl,
    String logoFileId,
    String logoImageUrl,
    String externalLink,
    Long gisuId,
    Long chapterId,
    Long productOwnerMemberId,
    List<Long> coProductOwnerMemberIds,
    List<ProjectPartQuotaInfo> partQuotas,
    Instant createdAt,
    Instant updatedAt
) {
    /**
     * Project 엔티티와 부가 조회 결과를 조립해 ProjectInfo를 만듭니다.
     *
     * @param project                    Project 엔티티
     * @param coProductOwnerMemberIds    보조 PM Member ID 목록 (없으면 {@code List.of()})
     * @param partQuotas                 파트별 TO 정보 목록 (DRAFT 상태면 비어있음)
     * @param thumbnailImageUrl          썸네일 CDN URL (없으면 null)
     * @param logoImageUrl               로고 CDN URL (없으면 null)
     */
    public static ProjectInfo from(
        Project project,
        List<Long> coProductOwnerMemberIds,
        List<ProjectPartQuotaInfo> partQuotas,
        String thumbnailImageUrl,
        String logoImageUrl
    ) {
        return ProjectInfo.builder()
            .id(project.getId())
            .status(project.getStatus())
            .name(project.getName())
            .description(project.getDescription())
            .thumbnailFileId(project.getThumbnailFileId())
            .thumbnailImageUrl(thumbnailImageUrl)
            .logoFileId(project.getLogoFileId())
            .logoImageUrl(logoImageUrl)
            .externalLink(project.getExternalLink())
            .gisuId(project.getGisuId())
            .chapterId(project.getChapterId())
            .productOwnerMemberId(project.getProductOwnerMemberId())
            .coProductOwnerMemberIds(coProductOwnerMemberIds)
            .partQuotas(partQuotas)
            .createdAt(project.getCreatedAt())
            .updatedAt(project.getUpdatedAt())
            .build();
    }
}
