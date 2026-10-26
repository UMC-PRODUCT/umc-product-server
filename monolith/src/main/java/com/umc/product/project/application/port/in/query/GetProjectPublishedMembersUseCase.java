package com.umc.product.project.application.port.in.query;

import java.util.List;
import java.util.Map;

import com.umc.product.project.application.port.in.query.dto.ProjectMatchingRosterInfo;

/**
 * 공개 팀원 명단을 일괄 조회한다. 조회 권한이 없거나 조회에 실패한 프로젝트는 제외한다.
 */
public interface GetProjectPublishedMembersUseCase {
    Map<Long, ProjectMatchingRosterInfo> get(List<Long> projectIds, Long memberId);
}
