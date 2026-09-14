package com.umc.product.project.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;
import java.util.Objects;
import lombok.Builder;

/**
 * 파트별 정원 일괄 갱신 Command (PROJECT-105).
 * <p>
 * PUT 시멘틱 — 본문이 곧 새 상태가 된다. 본문에 있는 파트만 유지/추가, 빠진 파트는 삭제.
 */
@Builder
public record UpdatePartQuotasCommand(
    Long projectId,
    List<Entry> entries,
    Long requesterMemberId
) {
    public UpdatePartQuotasCommand {
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(entries, "entries must not be null");
        Objects.requireNonNull(requesterMemberId, "requesterMemberId must not be null");
    }

    @Builder
    public record Entry(ChallengerPart part, Long quota) {
        public Entry {
            Objects.requireNonNull(part, "part must not be null");
            Objects.requireNonNull(quota, "quota must not be null");
        }
    }
}
