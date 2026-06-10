package com.umc.product.maintenance.adapter.out.bypass;

import com.umc.product.maintenance.application.port.out.MaintenanceBypassPolicy;
import com.umc.product.member.application.port.in.query.GetMemberRoleUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 전역 관리자 권한 기반 bypass 구현.
 */
@Component
@RequiredArgsConstructor
public class ChallengerRoleBasedBypassPolicy implements MaintenanceBypassPolicy {

    private final GetMemberRoleUseCase getMemberRoleUseCase;

    @Override
    public boolean shouldBypass(Long memberId) {
        if (memberId == null) {
            return false;
        }
        return getMemberRoleUseCase.isAdmin(memberId);
    }
}
