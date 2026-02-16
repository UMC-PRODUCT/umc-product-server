package com.umc.product.member.application.port.in.command.dto;

import com.umc.product.member.adapter.in.web.dto.request.TermConsentStatus;
import com.umc.product.terms.application.port.in.command.dto.CreateTermConsentCommand;
import lombok.Builder;

/**
 * 회원가입 시 약관 동의 여부를 나타내기 위한 DTO
 * <p>
 * Term 도메인거 아니고 Member꺼 맞습니다
 */
@Builder
public record TermConsents(
    Long termId,
    boolean isAgreed
) {
    public static TermConsents fromRequest(TermConsentStatus request) {
        return new TermConsents(
            request.termsId(),
            request.isAgreed()
        );
    }

    public CreateTermConsentCommand toCommand(Long memberId) {
        return CreateTermConsentCommand.builder()
            .memberId(memberId)
            .termId(this.termId())
            .isAgreed(this.isAgreed())
            .build();
    }
}
