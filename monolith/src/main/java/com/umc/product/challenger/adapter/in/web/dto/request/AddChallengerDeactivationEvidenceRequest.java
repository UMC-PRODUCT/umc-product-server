package com.umc.product.challenger.adapter.in.web.dto.request;

import java.util.List;

import org.hibernate.validator.constraints.URL;

import com.umc.product.challenger.application.port.in.command.dto.AddChallengerDeactivationEvidenceCommand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddChallengerDeactivationEvidenceRequest(
    @Size(max = 5, message = "사진은 최대 5장까지 등록할 수 있습니다.") List<
        @NotBlank(message = "이미지 ID는 비어 있을 수 없습니다.") String
    > imageIds,

    @Size(max = 3, message = "링크는 최대 3개까지 등록할 수 있습니다.") List<
        @NotBlank(message = "링크는 비어 있을 수 없습니다.") @Size(max = 255, message = "링크 길이 제한을 초과했습니다.") @URL(message = "올바른 링크 형식이 아닙니다.") String
    > links
) {
    public AddChallengerDeactivationEvidenceCommand toCommand(Long challengerId, Long addedByMemberId) {
        return AddChallengerDeactivationEvidenceCommand.of(
            challengerId,
            imageIds,
            links,
            addedByMemberId
        );
    }
}
