package com.umc.product.project.application.port.in.command.dto;

import java.util.Objects;
import lombok.Builder;

/**
 * 프로젝트 Draft 생성 Command.
 * <p>
 * PROJECT-101 요청이 {@code toCommand(memberId)}로 변환되어 전달됩니다.
 * 최초 생성 시점에는 기수와 작성자만 확정하며, 나머지 정보는
 * {@code UpdateProjectCommand}를 통해 단계적으로 저장됩니다.
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
