package com.umc.product.inquiry.application.policy;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.umc.product.member.application.port.in.query.ListMemberSystemRoleUseCase;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InquiryStaffRoster {

    private final ListMemberSystemRoleUseCase listMemberSystemRoleUseCase;

    public Set<Long> memberIds() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(listMemberSystemRoleUseCase.listUmcProductMemberIds()));
    }

    public boolean isAssignable(Long memberId) {
        return memberId != null && memberIds().contains(memberId);
    }
}
