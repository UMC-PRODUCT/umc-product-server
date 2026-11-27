package com.umc.product.member.application.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.member.application.port.in.query.GetMemberEmailHintUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberEmailHintInfo;
import com.umc.product.member.application.port.out.LoadMemberOAuthProviderPort;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberEmailHintQueryService implements GetMemberEmailHintUseCase {

    private static final List<OAuthProvider> DISPLAY_ORDER = List.of(
        OAuthProvider.KAKAO, OAuthProvider.APPLE, OAuthProvider.GOOGLE);
    private final LoadMemberPort loadMemberPort;
    private final LoadMemberOAuthProviderPort loadMemberOAuthProviderPort;

    @Override
    public MemberEmailHintInfo getByIdentity(Long schoolId, String name, String nickname) {
        Member member = loadMemberPort.findUniqueForEmailLookup(schoolId, name, nickname)
            .orElseThrow(() -> new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND));
        List<OAuthProvider> linked = loadMemberOAuthProviderPort.listByMemberId(member.getId());
        return new MemberEmailHintInfo(maskEmail(member.getEmail()),
            LocalDate.ofInstant(member.getCreatedAt(), ZoneId.of("Asia/Seoul")),
            DISPLAY_ORDER.stream().filter(linked::contains).toList());
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 0 || at != email.lastIndexOf('@') || at == email.length() - 1) {
            return "***";
        }
        String domain = email.substring(at);
        if (at <= 2) {
            return "***" + domain;
        }
        if (at <= 3) {
            return email.substring(0, 1) + "***" + domain;
        }
        return email.substring(0, 2) + "***" + email.charAt(at - 1) + domain;
    }
}
