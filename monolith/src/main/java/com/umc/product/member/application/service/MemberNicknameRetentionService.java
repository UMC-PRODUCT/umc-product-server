package com.umc.product.member.application.service;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.member.application.port.out.LockSchoolNicknamePort;
import com.umc.product.member.application.port.out.ReserveNicknamePort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.MemberRejoinKey;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class MemberNicknameRetentionService {

    private final LockSchoolNicknamePort lockSchoolNicknamePort;
    private final ReserveNicknamePort reserveNicknamePort;
    private final MemberGraduationQueryService graduationQueryService;

    public void retainOnWithdrawal(Member member) {
        if (member.getSchoolId() == null) {
            return;
        }
        lockSchoolNicknamePort.lockSchool(member.getSchoolId());
        reserveNicknamePort.reserve(
            member.getSchoolId(),
            member.getNickname(),
            MemberRejoinKey.fromVerifiedEmail(member.getEmail()),
            graduationQueryService.hasGraduatedHistory(Set.of(member.getId()))
        );
    }
}
