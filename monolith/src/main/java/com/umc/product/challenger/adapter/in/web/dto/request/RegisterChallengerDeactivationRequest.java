package com.umc.product.challenger.adapter.in.web.dto.request;

import java.util.List;

import org.hibernate.validator.constraints.URL;

import com.umc.product.challenger.application.port.in.command.dto.RegisterChallengerDeactivationCommand;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterChallengerDeactivationRequest(
    @NotNull(message = "탈부 사유는 필수입니다.") ChallengerDeactivationReason reason,

    @Size(max = 100, message = "상세 사유 글자 수 제한을 초과했습니다.") String reasonDetail,

    @Size(max = 100, message = "메모 글자 수 제한을 초과했습니다.") String memo,

    @Size(max = 5, message = "사진은 최대 5장까지 등록할 수 있습니다.") List<@NotBlank(message = "이미지 ID는 비어 있을 수 없습니다.") String> imageIds,

    @Size(max = 3, message = "링크는 최대 3개까지 등록할 수 있습니다.") List<
        @NotBlank(message = "링크는 비어 있을 수 없습니다.") @Size(max = 255, message = "링크 길이 제한을 초과했습니다.") @URL(message = "올바른 링크 형식이 아닙니다.") String
    > links
) {
    public RegisterChallengerDeactivationCommand toCommand(Long challengerId, Long registeredByMemberId) {
        return RegisterChallengerDeactivationCommand.of(
            challengerId,
            reason,
            reasonDetail,
            memo,
            imageIds,
            links,
            registeredByMemberId
        );
    }
}
