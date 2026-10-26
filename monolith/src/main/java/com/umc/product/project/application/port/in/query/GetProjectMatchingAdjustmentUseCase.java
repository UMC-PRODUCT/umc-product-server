package com.umc.product.project.application.port.in.query;

import java.util.List;
import java.util.Map;

import com.umc.product.project.application.port.in.query.dto.ProjectMatchingRosterInfo;

/**
 * 요청한 모든 프로젝트의 조정 권한을 검증하고 조정 명단을 조회하는 UseCase.
 * 조정 시작 전·랜덤 매칭 미완료·최종 공개 이후에는 현재 공개 명단을 반환합니다.
 */
public interface GetProjectMatchingAdjustmentUseCase {
    Map<Long, ProjectMatchingRosterInfo> get(List<Long> projectIds, Long requesterMemberId);
}
