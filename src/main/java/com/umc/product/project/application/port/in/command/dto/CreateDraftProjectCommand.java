package com.umc.product.project.application.port.in.command.dto;

import java.util.Objects;
import lombok.Builder;

/**
 * 프로젝트 Draft 생성 Command. 최초 생성 시점엔 기수/PO/요청자만 확정하고,
 * 이후 정보는 {@link UpdateProjectCommand}로 갱신합니다.
 * <p>
 * {@code requesterMemberId} 는 호출자(API 발행자)이고, {@code productOwnerMemberId} 는 임명될 PM.
 * PM 본인이 생성하면 두 값이 같고, 운영진이 다른 PLAN 챌린저를 임명하면 다르다.
 * 호출자는 creator 로 저장되어 DRAFT 단계 EDIT 권한 + audit 에 사용된다.
 */
@Builder
public record CreateDraftProjectCommand(
    Long gisuId,
    Long productOwnerMemberId,
    Long requesterMemberId
) {
    public CreateDraftProjectCommand {
        Objects.requireNonNull(gisuId, "gisuId must not be null");
        Objects.requireNonNull(productOwnerMemberId, "productOwnerMemberId must not be null");
        Objects.requireNonNull(requesterMemberId, "requesterMemberId must not be null");
    }
}
