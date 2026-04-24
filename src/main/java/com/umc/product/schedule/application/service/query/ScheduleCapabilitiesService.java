package com.umc.product.schedule.application.service.query;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ChallengerRoleInfo;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.schedule.application.port.in.query.GetScheduleCapabilitiesUseCase;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleCapabilitiesInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleCapabilitiesService implements GetScheduleCapabilitiesUseCase {
    // TODO : 중앙 운영진, 학교 파트장, 기타운영진 최대 초대 가능 인원수 확인 필요

    private final GetChallengerUseCase getChallengerUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;

    @Override
    public ScheduleCapabilitiesInfo getCapabilities(Long memberId) {

        // 챌린저 활동 기록이 없으면 일정 생성 불가
        if (getChallengerUseCase.getAllByMemberId(memberId).isEmpty()) {
            return ScheduleCapabilitiesInfo.notAllowed();
        }

        // 현재 활성 기수 조회
        Long activeGisuId = getGisuUseCase.getActiveGisu().gisuId();

        // 현재 기수의 역할 조회
        List<ChallengerRoleInfo> currentGisuRoles = getChallengerRoleUseCase.findAllByMemberId(memberId).stream()
            .filter(role -> role.gisuId().equals(activeGisuId))
            .toList();

        // 역할이 없으면 일반 챌린저
        if (currentGisuRoles.isEmpty()) {
            return ScheduleCapabilitiesInfo.forChallenger();
        }

        // 가장 높은 권한 기준으로 capabilities 반환
        return determineCapabilities(currentGisuRoles);
    }


    // 역할 목록에서 가장 높은 권한을 기준으로 capabilities 반환
    // 우선순위 : CentralCore > ChapterPresident > SchoolCore > CentralMember > SchoolAdmin > Challenger
    private ScheduleCapabilitiesInfo determineCapabilities(List<ChallengerRoleInfo> roles) {
        int highestPriority = Integer.MAX_VALUE;

        for (ChallengerRoleInfo role : roles) {
            int priority = getRolePriority(role.roleType());
            if (priority < highestPriority) {
                highestPriority = priority;
            }
        }

        return mapPriorityToCapabilities(highestPriority);
    }

    // 역할의 우선순위 반환 (낮을수록 높은 권한)
    private int getRolePriority(ChallengerRoleType roleType) {
        if (roleType.isAtLeastCentralCore()) {
            return 1;
        }
        if (roleType == ChallengerRoleType.CHAPTER_PRESIDENT) {
            return 2;
        }
        if (roleType.isAtLeastSchoolCore()) {
            return 3;
        }
        if (roleType.isAtLeastCentralMember()) {
            return 4;
        }
        if (roleType.isAtLeastSchoolAdmin()) {
            return 5;
        }
        return Integer.MAX_VALUE;
    }


    // 우선순위를 capabilities로 매핑
    private ScheduleCapabilitiesInfo mapPriorityToCapabilities(int priority) {
        return switch (priority) {
            case 1 -> ScheduleCapabilitiesInfo.forCentralCore();
            case 2 -> ScheduleCapabilitiesInfo.forChapterPresident();
            case 3 -> ScheduleCapabilitiesInfo.forSchoolCore();
            case 4 -> ScheduleCapabilitiesInfo.forCentralMember();
            case 5 -> ScheduleCapabilitiesInfo.forSchoolAdmin();
            default -> ScheduleCapabilitiesInfo.forChallenger();
        };
    }
}
