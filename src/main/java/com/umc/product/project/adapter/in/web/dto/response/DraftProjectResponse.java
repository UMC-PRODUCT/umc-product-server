package com.umc.product.project.adapter.in.web.dto.response;

import com.umc.product.project.adapter.in.web.dto.common.MemberBrief;
import com.umc.product.project.application.port.in.query.dto.ProjectInfo;
import com.umc.product.project.domain.enums.ProjectStatus;
import java.util.List;
import lombok.Builder;

/**
 * PM이 자신의 Draft를 복원하기 위한 응답 (PROJECT-103).
 * <p>
 * 작성자 PM만 조회하므로 {@code toPublic()} 마스킹 불필요. 이미지는 storage 도메인 컨벤션에 따라
 * end-user에 fileId를 노출하지 않고 access URL({@code thumbnailImageUrl}/{@code logoImageUrl})만 제공합니다.
 * 사진 미변경은 PATCH에서 해당 fileId 필드를 null로 보내 표현합니다.
 * <p>
 * {@code applicationFormId} 가 null 이 아니면 PM이 PROJECT-106 으로 폼을 1회 이상 작성한 상태이며,
 * 폼 전체 구조는 별도 PROJECT-106-GET 호출로 조회한다 (등록 1단계 응답을 슬림하게 유지).
 */
@Builder
public record DraftProjectResponse(
    Long id,
    ProjectStatus status,
    String name,
    String description,
    String thumbnailImageUrl,
    String logoImageUrl,
    String externalLink,
    MemberBrief productOwner,
    List<MemberBrief> coProductOwners,
    Long applicationFormId
) {
    public static DraftProjectResponse from(
        ProjectInfo info,
        MemberBrief productOwner,
        List<MemberBrief> coProductOwners,
        Long applicationFormId
    ) {
        return DraftProjectResponse.builder()
            .id(info.id())
            .status(info.status())
            .name(info.name())
            .description(info.description())
            .thumbnailImageUrl(info.thumbnailImageUrl())
            .logoImageUrl(info.logoImageUrl())
            .externalLink(info.externalLink())
            .productOwner(productOwner)
            .coProductOwners(coProductOwners)
            .applicationFormId(applicationFormId)
            .build();
    }
}
