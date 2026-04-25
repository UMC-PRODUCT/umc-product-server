package com.umc.product.project.application.port.in.command.dto;

import java.util.Objects;
import lombok.Builder;

/**
 * 프로젝트 Draft 생성 Command. 최초 생성 시점엔 기수와 작성자만 확정하고,
 * 이후 정보는 {@link UpdateProjectCommand}로 갱신합니다.
 */
@Builder
public record CreateDraftProjectCommand(
    Long gisuId,
    Long productOwnerMemberId
) {
    public CreateDraftProjectCommand {
        Objects.requireNonNull(gisuId, "gisuId must not be null");
        Objects.requireNonNull(productOwnerMemberId, "productOwnerMemberId must not be null");
    }
}
