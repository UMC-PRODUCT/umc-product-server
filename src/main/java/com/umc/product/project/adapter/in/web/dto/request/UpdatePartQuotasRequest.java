package com.umc.product.project.adapter.in.web.dto.request;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.command.dto.UpdatePartQuotasCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * PROJECT-105 요청 — 파트별 정원 일괄 갱신 (PUT 시멘틱).
 */
public record UpdatePartQuotasRequest(
    @NotEmpty(message = "entries 는 1개 이상이어야 합니다")
    @Valid
    List<Entry> entries
) {
    public record Entry(
        @NotNull(message = "part 는 필수입니다")
        ChallengerPart part,

        @NotNull(message = "quota 는 필수입니다")
        @Positive(message = "quota 는 1 이상이어야 합니다")
        Long quota
    ) {}

    public UpdatePartQuotasCommand toCommand(Long projectId, Long requesterMemberId) {
        List<UpdatePartQuotasCommand.Entry> commandEntries = entries.stream()
            .map(e -> UpdatePartQuotasCommand.Entry.builder()
                .part(e.part())
                .quota(e.quota())
                .build())
            .toList();
        return UpdatePartQuotasCommand.builder()
            .projectId(projectId)
            .entries(commandEntries)
            .requesterMemberId(requesterMemberId)
            .build();
    }
}
