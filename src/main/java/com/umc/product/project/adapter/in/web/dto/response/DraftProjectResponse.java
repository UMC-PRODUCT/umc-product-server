package com.umc.product.project.adapter.in.web.dto.response;

import com.umc.product.project.adapter.in.web.dto.common.ApplicationQuestionItem;
import com.umc.product.project.adapter.in.web.dto.common.MemberBrief;
import com.umc.product.project.application.port.in.query.dto.ProjectInfo;
import com.umc.product.project.domain.enums.ProjectStatus;
import java.util.List;
import lombok.Builder;

/**
 * PM이 자신의 Draft를 복원하기 위한 응답 (PROJECT-103).
 * <p>
 * 편집 재개용이라 {@code thumbnailFileId}/{@code logoFileId}(UUID)도 함께 내려줍니다.
 * 작성자 PM만 조회하므로 {@code toPublic()} 마스킹 불필요.
 */
@Builder
public record DraftProjectResponse(
    Long id,
    ProjectStatus status,
    String name,
    String description,
    String thumbnailFileId,
    String thumbnailImageUrl,
    String logoFileId,
    String logoImageUrl,
    String externalLink,
    MemberBrief productOwner,
    List<MemberBrief> coProductOwners,
    Long applicationFormId,
    List<ApplicationQuestionItem> questions
) {
    public static DraftProjectResponse from(
        ProjectInfo info,
        MemberBrief productOwner,
        List<MemberBrief> coProductOwners,
        List<ApplicationQuestionItem> questions
    ) {
        return DraftProjectResponse.builder()
            .id(info.id())
            .status(info.status())
            .name(info.name())
            .description(info.description())
            .thumbnailFileId(info.thumbnailFileId())
            .thumbnailImageUrl(info.thumbnailImageUrl())
            .logoFileId(info.logoFileId())
            .logoImageUrl(info.logoImageUrl())
            .externalLink(info.externalLink())
            .productOwner(productOwner)
            .coProductOwners(coProductOwners)
            .applicationFormId(info.applicationFormId())
            .questions(questions)
            .build();
    }
}
