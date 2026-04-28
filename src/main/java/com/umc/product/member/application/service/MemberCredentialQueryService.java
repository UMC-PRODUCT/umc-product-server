package com.umc.product.member.application.service;

import com.umc.product.member.application.port.in.query.GetMemberCredentialUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberCredentialInfo;
import com.umc.product.member.application.port.out.LoadMemberPort;
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
    public Optional<MemberCredentialInfo> findCredentialByLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            return Optional.empty();
        }
        return loadMemberPort.findByLoginId(loginId)
            // 자격증명이 미등록된 (loginId/passwordHash 가 없는) 회원은 무시
            .filter(member -> member.getLoginId() != null && member.getPasswordHash() != null)
            .map(MemberCredentialInfo::from);
    }

    @Override
    public boolean existsByLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            return false;
        }
        return loadMemberPort.existsByLoginId(loginId);
    }
}
