package com.umc.product.member.application.port.in.query.dto;

import java.time.LocalDate;
import java.util.List;

import com.umc.product.common.domain.enums.OAuthProvider;

public record MemberEmailHintInfo(String maskedEmail, LocalDate joinedOn, List<OAuthProvider> linkedProviders) {
}
