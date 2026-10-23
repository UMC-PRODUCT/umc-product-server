package com.umc.product.project.adapter.in.web.dto.request;

import java.util.List;

import com.umc.product.project.application.port.in.command.dto.TransferMatchingChapterCommand;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TransferMatchingChapterRequest(
    @NotNull(message = "기수 ID는 필수입니다") Long gisuId,

    @NotEmpty(message = "이동 대상은 1명 이상이어야 합니다") @Size(max = 200, message = "한 번에 200명까지 이동할 수 있습니다") List<@Valid @NotNull(message = "이동 대상 항목은 비어 있을 수 없습니다") Target> transfers
) {

    public record Target(
        @NotNull(message = "회원 ID는 필수입니다") Long memberId,
        @NotNull(message = "이동지부 ID는 필수입니다") Long toChapterId,
        @Size(max = 255, message = "이동 사유는 255자 이하여야 합니다") String reason
    ) {
    }

    public TransferMatchingChapterCommand toCommand(Long requesterMemberId) {
        return TransferMatchingChapterCommand.builder()
            .requesterMemberId(requesterMemberId)
            .gisuId(gisuId)
            .targets(transfers.stream()
                .map(target -> new TransferMatchingChapterCommand.Target(
                    target.memberId(), target.toChapterId(), target.reason()))
                .toList())
            .build();
    }
}
