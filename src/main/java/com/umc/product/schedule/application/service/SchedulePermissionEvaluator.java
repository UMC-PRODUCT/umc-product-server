package com.umc.product.schedule.application.service;

import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.schedule.application.port.out.LoadSchedulePort;
import com.umc.product.schedule.domain.Schedule;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Schedule(일정) 리소스에 대한 권한 평가
 */
@Component
@RequiredArgsConstructor
public class SchedulePermissionEvaluator implements ResourcePermissionEvaluator {

    private final LoadSchedulePort loadSchedulePort;
    private final GetGisuUseCase getGisuUseCase;

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.SCHEDULE;
    }

    @Override
    public boolean evaluate(SubjectAttributes subjectAttributes, ResourcePermission resourcePermission) {

        PermissionType permission = resourcePermission.permission();
        Long memberId = subjectAttributes.memberId();

        // READ (일정 조회), WRITE (일정 생성)
        // '챌린저 활동 기록이 있는 사용자'만 가능
        if (permission == PermissionType.READ || permission == PermissionType.WRITE) {
            // 챌린저 활동 기록이 있는 사용자인지 확인
            return !subjectAttributes.gisuChallengerInfos().isEmpty();
        }

        // EDIT (일정 수정), DELETE (일정 삭제)
        // '생성자 본인' 또는 '해당 일정 기수의 최고 운영 관리자'만 가능
        if (permission == PermissionType.EDIT || permission == PermissionType.DELETE) {

            // resourceId가 없으면 false 리턴
            if (resourcePermission.resourceId() == null) {
                return false;
            }

            Schedule schedule = loadSchedulePort.findById(resourcePermission.getResourceIdAsLong())
                .orElseThrow(() -> new ScheduleDomainException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

            // 해당 일정의 기수에서 최고 운영 관리자 권한이 있다면 즉시 통과
            Long targetGisuId = getGisuUseCase.getGisuByDate(schedule.getStartsAt()).gisuId();
            boolean isAdmin = subjectAttributes.roleAttributes().stream()
                .filter(role -> role.gisuId().equals(targetGisuId))
                .anyMatch(role -> role.roleType().isSuperAdmin());
            if (isAdmin) {
                return true;
            }

            // 최고 운영 관리자가 아니라면 일정 생성자 본인인지 확인
            return schedule.getAuthorMemberId().equals(memberId);
        }

        return false;
    }
}
