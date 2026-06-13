package com.umc.product.member.application.service;

import com.umc.product.member.application.port.in.query.GetMemberCredentialUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberCredentialInfo;
import com.umc.product.member.application.port.in.query.dto.MemberCredentialStatusInfo;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberCredentialQueryService implements GetMemberCredentialUseCase {

    private final LoadMemberPort loadMemberPort;

    @Override
    public Optional<MemberCredentialInfo> findCredentialByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return loadMemberPort.findByEmail(email)
            // 자격증명이 미등록된 (비밀번호가 없는) 회원은 무시
            .filter(member -> member.getPasswordHash() != null)
            .map(MemberCredentialInfo::from);
    }

    @Override
    public Optional<MemberCredentialInfo> findCredentialByMemberId(Long memberId) {
        if (memberId == null) {
            return Optional.empty();
        }
        return loadMemberPort.findById(memberId)
            .filter(member -> member.getPasswordHash() != null)
            .map(MemberCredentialInfo::from);
    }

    @Override
    @Transactional
    public MemberCredentialStatusInfo getCredentialStatusForUpdate(Long memberId) {
        if (memberId == null) {
            throw new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND);
        }
        return loadMemberPort.findByIdForUpdate(memberId)
            .map(MemberCredentialStatusInfo::from)
            .orElseThrow(() -> new MemberDomainException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return loadMemberPort.existsByEmail(email);
    }
}
