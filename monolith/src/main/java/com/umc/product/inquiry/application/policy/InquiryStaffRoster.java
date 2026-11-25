package com.umc.product.inquiry.application.policy;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.umc.product.member.application.port.in.query.ListMemberSystemRoleUseCase;

import lombok.RequiredArgsConstructor;

/**
 * 문의를 맡을 수 있는 운영진 명단. {@code UMC_PRODUCT} 역할을 직접 가진 회원만 들어가고 {@code SUPER_ADMIN}만 가진 회원은
 * 빠진다. 문의 열람·처리 권한(AUTHZ-10, {@link InquiryAccessPolicy})과 달리 담당자 후보·내 담당·운영진 알림·실시간 받는
 * 사람을 정한다.
 */
@Component
@RequiredArgsConstructor
public class InquiryStaffRoster {

    private final ListMemberSystemRoleUseCase listMemberSystemRoleUseCase;

    /** 조회할 때마다 새로 읽는다. 순서는 회원 역할 조회 결과 순서를 따른다. */
    public Set<Long> memberIds() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(listMemberSystemRoleUseCase.listUmcProductMemberIds()));
    }

    public boolean isAssignable(Long memberId) {
        return memberId != null && memberIds().contains(memberId);
    }
}
