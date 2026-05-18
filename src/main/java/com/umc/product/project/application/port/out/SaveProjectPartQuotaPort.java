package com.umc.product.project.application.port.out;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.ProjectPartQuota;
import java.util.Collection;
import java.util.List;

/**
 * ProjectPartQuota 영속화 Port (Driven / Port Out).
 */
public interface SaveProjectPartQuotaPort {

    ProjectPartQuota save(ProjectPartQuota quota);

    List<ProjectPartQuota> saveAll(Collection<ProjectPartQuota> quotas);

    /**
     * 프로젝트의 특정 파트 quota row 들을 일괄 삭제한다 (PROJECT-105 diff 적용 시).
     */
    void deleteByProjectIdAndPartIn(Long projectId, Collection<ChallengerPart> parts);
}
