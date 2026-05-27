package com.umc.product.maintenance.adapter.out.bypass;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.maintenance.application.port.out.MaintenanceBypassPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 현재 운영진 모델 ({@code ChallengerRole}) 기반 bypass 구현.
 * <p>
 * 통과 기준: 해당 사용자가 보유한 ChallengerRole 중 하나라도 {@code SUPER_ADMIN} 이면 통과.
 * 향후 {@code Member.role} 컬럼이 도입되면 본 구현체를 신규 구현으로 교체한다.
 */
@Component
@RequiredArgsConstructor
public class ChallengerRoleBasedBypassPolicy implements MaintenanceBypassPolicy {

    private final GetChallengerRoleUseCase getChallengerRoleUseCase;

    @Override
    public boolean shouldBypass(Long memberId) {
        if (memberId == null) {
            return false;
        }
        return getChallengerRoleUseCase.findAllByMemberId(memberId).stream()
            .anyMatch(role -> role.roleType().isSuperAdmin());
    }
}
