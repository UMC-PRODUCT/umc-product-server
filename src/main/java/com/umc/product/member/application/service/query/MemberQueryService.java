package com.umc.product.member.application.service.query;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.exception.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService implements GetMemberUseCase {

    private final LoadMemberPort loadMemberPort;

    @Override
    public MemberInfo getById(Long userId) {
        Member member = loadMemberPort.findById(userId)
                .orElseThrow(() -> new BusinessException(Domain.MEMBER, MemberErrorCode.MEMBER_NOT_FOUND));
        return MemberInfo.from(member);
    }

    @Override
    public MemberInfo getByEmail(String email) {
        Member member = loadMemberPort.findByEmail(email)
                .orElseThrow(() -> new BusinessException(Domain.MEMBER, MemberErrorCode.MEMBER_NOT_FOUND));
        return MemberInfo.from(member);
    }

    @Override
    public boolean existsById(Long userId) {
        return loadMemberPort.existsById(userId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return loadMemberPort.existsByEmail(email);
    }
}
